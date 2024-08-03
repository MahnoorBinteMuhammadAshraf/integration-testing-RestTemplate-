package com.advancedtaskmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Category
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @NotBlank
    String name;

    @OneToMany(mappedBy = "category")
    @Valid
    Set<Task>  tasks;

    // Method that uses builder internally
    public static Category createDefaultCategory() {
        return Category.builder().build();
    }
}
