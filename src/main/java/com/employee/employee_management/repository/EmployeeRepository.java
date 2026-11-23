package com.employee.employee_management.repository;

import com.employee.employee_management.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    /*
    contains
    SELECT *
    FROM employees
   WHERE LOWER(name) LIKE LOWER('%john%');
     */
    List<Employee> findByNameContainingIgnoreCase(String name);

    /*
    exact department
    SELECT *
    FROM employees
    WHERE LOWER(department) = LOWER('IT');
     */
    List<Employee> findByDepartmentIgnoreCase(String department);

    /*
    salary range
    SELECT *
    FROM employees
    WHERE salary BETWEEN 50000 AND 100000;
    */
    List<Employee> findBySalaryBetween(Double minSalary, Double maxSalary);
}
