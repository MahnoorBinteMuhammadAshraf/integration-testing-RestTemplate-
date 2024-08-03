package com.advancedtaskmanager.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskDTO {
    String title;
    String description;
    LocalDate scheduledDate;
    boolean completed;
    Integer priority;

    String category;
}
