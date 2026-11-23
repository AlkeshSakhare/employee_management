package com.employee.employee_management.mapper;

import com.employee.employee_management.dto.EmployeeRequestDTO;
import com.employee.employee_management.dto.EmployeeResponseDTO;
import com.employee.employee_management.entity.Employee;

public class EmployeeMapper {

    // Convert Request DTO to Entity
    //This method converts incoming JSON data into an Employee entity.
    public static Employee toEntity(EmployeeRequestDTO dto) {

        Employee employee = new Employee();

        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setSalary(dto.getSalary());

        return employee;
    }

    // Convert Entity to Response DTO
  //  This method converts the database entity into a response DTO.
    public static EmployeeResponseDTO toResponseDTO(Employee employee) {

        EmployeeResponseDTO dto = new EmployeeResponseDTO();

        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setDepartment(employee.getDepartment());
        dto.setSalary(employee.getSalary());

        return dto;
    }
}

