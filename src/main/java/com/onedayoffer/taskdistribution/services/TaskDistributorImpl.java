package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class TaskDistributorImpl implements TaskDistributor {

    private static final Integer MAX_LOAD_MINUTES = 7 * 60;

    @Override
    public void distribute(List<EmployeeDTO> employees, List<TaskDTO> tasks) {
        Set<TaskDTO> taskSet = new HashSet<>(tasks);
        Set<EmployeeDTO> employeeSet = new HashSet<>(employees);

        int taskCount = tasks.size();
        int employeeCount = employees.size();

        while (taskCount != 0 && employeeCount != 0) {
            for (EmployeeDTO employeeDTO: employeeSet) {
                Optional<EmployeeDTO> first = employees.stream()
                        .filter(employeeDTO1 -> employeeDTO.getFio().equals(employeeDTO1.getFio()))
                        .findFirst();
                if (first.isPresent()) {
                    EmployeeDTO employeeDTO1 = first.get();
                    Integer totalLeadTime = employeeDTO1.getTotalLeadTime();
                    if (totalLeadTime > MAX_LOAD_MINUTES) {
                        employeeCount--;
                        employeeSet.remove(employeeDTO1);
                        break;
                    } else {
                        List<TaskDTO> employeeDTOTasks = employeeDTO1.getTasks();
                        if (employeeDTOTasks == null) {
                            employeeDTOTasks = new ArrayList<>();
                        }
                        Optional<TaskDTO> maxPriorityTask = taskSet.stream()
                                .filter(task -> task.getLeadTime() >= totalLeadTime)
                                .min(Comparator.comparing(TaskDTO::getPriority));
                        if (maxPriorityTask.isPresent()) {
                            employeeDTOTasks.add(maxPriorityTask.get());
                            taskSet.remove(maxPriorityTask.get());
                            taskCount--;
                        } else {
                            employeeCount--;
                            employeeSet.remove(employeeDTO1);
                            break;
                        }
                    }
                }
            }
        }
    }
}
