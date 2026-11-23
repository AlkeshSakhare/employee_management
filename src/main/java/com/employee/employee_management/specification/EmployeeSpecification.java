package com.employee.employee_management.specification;

import com.employee.employee_management.entity.Employee;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

    //search by salary range
    public static Specification<Employee> hasSalaryBetween(Double minSalary, Double maxSalary) {
        /*
WHERE salary BETWEEN 50000 AND 100000
         */
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("salary"), minSalary, maxSalary);
    }

    //Search by employee name
    public static Specification<Employee> hasName(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );
    }

    //Search by department
    public static Specification<Employee> hasDepartment(String department) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("department")),
                        department.toLowerCase()
                );
    }
}
