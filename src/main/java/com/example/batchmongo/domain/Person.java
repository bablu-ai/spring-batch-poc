package com.example.batchmongo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model representing a person record from CSV input.
 * Used for batch processing with transformation capabilities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    private String name;
    private String email;
    private Integer age;
    private LocalDateTime processedAt;

    public Person(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
}
