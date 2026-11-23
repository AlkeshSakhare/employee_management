package com.employee.employee_management.service;

import com.employee.employee_management.dto.EmployeePatchDTO;
import com.employee.employee_management.dto.EmployeeRequestDTO;
import com.employee.employee_management.dto.EmployeeResponseDTO;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.exception.DuplicateEmailException;
import com.employee.employee_management.exception.EmployeeNotFoundException;
import com.employee.employee_management.mapper.EmployeeMapper;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.specification.EmployeeSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // Get all employees
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeMapper::toResponseDTO)
                .toList();
    }

    // Get employee by ID
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + id));

        return EmployeeMapper.toResponseDTO(employee);
    }

    // Save employee
    public EmployeeResponseDTO saveEmployee(EmployeeRequestDTO requestDTO) {
        if (employeeRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateEmailException("Email already registered: " + requestDTO.getEmail());
        }

        // Convert DTO into Entity
        Employee employee = EmployeeMapper.toEntity(requestDTO);

        // Save Entity into database
        Employee savedEmployee = employeeRepository.save(employee);

        // Convert Entity into Response DTO
        return EmployeeMapper.toResponseDTO(savedEmployee);
    }

    // Delete employee
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + id));

        employeeRepository.delete(employee);
    }

    // Update employee
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found with ID: " + id));

        if (employeeRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            throw new DuplicateEmailException(
                    "Email already registered: " + requestDTO.getEmail());
        }

        employee.setName(requestDTO.getName());
        employee.setEmail(requestDTO.getEmail());
        employee.setDepartment(requestDTO.getDepartment());
        employee.setSalary(requestDTO.getSalary());

        Employee updatedEmployee = employeeRepository.save(employee);

        return EmployeeMapper.toResponseDTO(updatedEmployee);
    }

    // Patch employee
    public EmployeeResponseDTO patchEmployee(Long id, EmployeePatchDTO patchDTO) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found with ID: " + id));

        if (patchDTO.getEmail() != null &&
                employeeRepository.existsByEmailAndIdNot(patchDTO.getEmail(), id)) {

            throw new DuplicateEmailException(
                    "Email already registered: " + patchDTO.getEmail());
        }

        if (patchDTO.getName() != null) {
            employee.setName(patchDTO.getName());
        }

        if (patchDTO.getEmail() != null) {
            employee.setEmail(patchDTO.getEmail());
        }

        if (patchDTO.getDepartment() != null) {
            employee.setDepartment(patchDTO.getDepartment());
        }

        if (patchDTO.getSalary() != null) {
            employee.setSalary(patchDTO.getSalary());
        }

        Employee updatedEmployee = employeeRepository.save(employee);

        return EmployeeMapper.toResponseDTO(updatedEmployee);
    }


    // Get all employees with pagination
    public Page<EmployeeResponseDTO> getAllEmployees(int page, int size) {

        // Create pagination request
        Pageable pageable = PageRequest.of(page, size);

        // Fetch employees from database
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        // Convert Entity into Response DTO
        return employeePage.map(EmployeeMapper::toResponseDTO);
    }

    // Get all employees with sorting
    public List<EmployeeResponseDTO> getAllEmployeesSorted(String sortBy, String direction) {

        Sort sort = getValidatedSort(sortBy, direction);

        List<Employee> employees = employeeRepository.findAll(sort);

        return employees.stream()
                .map(EmployeeMapper::toResponseDTO)
                .toList();
    }

    // Get employees with pagination and sorting
    public Page<EmployeeResponseDTO> getAllEmployees(
            int page, int size, String sortBy, String direction) {

        getPageValidate(page);

        getSizeValidated(size);

        Sort sort = getValidatedSort(sortBy, direction);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        return employeePage.map(EmployeeMapper::toResponseDTO);
    }

    private Sort getValidatedSort(String sortBy, String direction) {
        List<String> allowedSortFields = List.of("id", "name", "email", "department", "salary");

        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field. Allowed fields: " + allowedSortFields
            );
        }

        if (!direction.equalsIgnoreCase("asc") &&
                !direction.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException(
                    "Invalid sort direction. Allowed values: asc, desc"
            );
        }

        return direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
    }

    // Search employees by name
    public List<EmployeeResponseDTO> searchEmployeesByName(String name) {
        List<Employee> employees = employeeRepository.findByNameContainingIgnoreCase(name);
        if (employees.isEmpty()) {
            throw new EmployeeNotFoundException("No employees found with name: " + name);
        }
        return employees.stream()
                .map(EmployeeMapper::toResponseDTO)
                .toList();
    }

    // Search employees by department
    public List<EmployeeResponseDTO> getEmployeesByDepartment(String department) {
        List<Employee> employees = employeeRepository.findByDepartmentIgnoreCase(department);
        if (employees.isEmpty()) {
            throw new EmployeeNotFoundException("No employees found in department: " + department);
        }
        return employees.stream()
                .map(EmployeeMapper::toResponseDTO)
                .toList();
    }


    // Search employees by salary ranges
    public List<EmployeeResponseDTO> getEmployeesBySalaryRange(Double minSalary, Double maxSalary) {

        getSalaryRangeValidated(minSalary, maxSalary);

        List<Employee> employees = employeeRepository.findBySalaryBetween(minSalary, maxSalary);
        if (employees.isEmpty()) {
            throw new EmployeeNotFoundException("No employees found within salary range: " + minSalary + " - " + maxSalary);
        }
        return employees.stream()
                .map(EmployeeMapper::toResponseDTO)
                .toList();
    }

    // Get employee salary range with pagination
    public Page<EmployeeResponseDTO> getEmployeesBySalaryRangeWithPagination(
            Double minSalary, Double maxSalary,
            int page, int size,
            String sortBy, String direction) {

        // Validate salary
        getSalaryRangeValidated(minSalary, maxSalary);

        // Validate pagination
        getPageValidate(page);

        // Validate size
        getSizeValidated(size);

        // Create sorting and pagination
        Sort sort = getValidatedSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Apply Specification
        Page<Employee> employeePage = employeeRepository.findAll(
                EmployeeSpecification.hasSalaryBetween(minSalary, maxSalary),
                pageable
        );

        // Convert Entity Page into DTO Page
        return employeePage.map(EmployeeMapper::toResponseDTO);
    }

    private void getPageValidate(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
    }

    private void getSizeValidated(int size) {
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }
    }

    private void getSalaryRangeValidated(Double minSalary, Double maxSalary) {
        if (!Double.isFinite(minSalary) || !Double.isFinite(maxSalary)) {
            throw new IllegalArgumentException("Salary must be a finite number");
        }
        if (minSalary < 0) {
            throw new IllegalArgumentException("minSalary cannot be negative");
        }
        if (maxSalary < 0) {
            throw new IllegalArgumentException("maxSalary cannot be negative");
        }
        if (minSalary > maxSalary) {
            throw new IllegalArgumentException(
                    "Minimum salary cannot be greater than maximum salary");
        }
    }

    // Dynamic search using multiple optional filters
    public Page<EmployeeResponseDTO> filterEmployees(
            String name,
            String department,
            Double minSalary,
            Double maxSalary,
            int page,
            int size,
            String sortBy,
            String direction) {

        // Start with an empty specification (matches all employees)
        Specification<Employee> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        // Filter by employee name
        if (name != null && !name.isBlank()) {
            specification = specification.and(EmployeeSpecification.hasName(name.trim())
            );
        }

        // Filter by department
        if (department != null && !department.isBlank()) {
            specification = specification.and(
                    EmployeeSpecification.hasDepartment(department.trim())
            );
        }

        // Validate and filter by salary
        if (minSalary != null || maxSalary != null) {
            if (minSalary == null || maxSalary == null) {
                throw new IllegalArgumentException("Both minSalary and maxSalary must be provided together");
            }

            getSalaryRangeValidated(minSalary, maxSalary);

            specification = specification.and(EmployeeSpecification.hasSalaryBetween(minSalary, maxSalary));
        }

        // At least one filter must be provided
        if ((name == null || name.isBlank())
                && (department == null || department.isBlank())
                && minSalary == null && maxSalary == null) {

            throw new IllegalArgumentException("Please provide at least one search filter i.e. name, department, minSalary, maxSalary");
        }

        // Validate pagination
        getPageValidate(page);
        getSizeValidated(size);

        // Validate sorting
        Sort sort = getValidatedSort(sortBy, direction);

        Pageable pageable = PageRequest.of(page, size, sort);

        // Execute dynamic query
        Page<Employee> employeePage = employeeRepository.findAll(specification, pageable);

        // Convert Entity Page to DTO Page
        return employeePage.map(EmployeeMapper::toResponseDTO);
    }
}
