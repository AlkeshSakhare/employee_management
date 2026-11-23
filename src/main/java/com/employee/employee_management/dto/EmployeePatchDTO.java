package com.employee.employee_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;

public class EmployeePatchDTO {

    @Email(message = "Please provide a valid email address")
    private String email;

    private String name;

    private String department;

    @Positive(message = "Salary must be greater than zero")
    private Double salary;

    public EmployeePatchDTO() {
    }

    // Generate getters and setters using IntelliJ

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }
}
