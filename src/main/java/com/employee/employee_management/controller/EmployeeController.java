package com.employee.employee_management.controller;

import com.employee.employee_management.dto.EmployeePatchDTO;
import com.employee.employee_management.dto.EmployeeRequestDTO;
import com.employee.employee_management.dto.EmployeeResponseDTO;
import com.employee.employee_management.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Get all employees
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        logger.info("Fetching all employees");
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/pagination")
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllEmployeesWithPagination(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        Page<EmployeeResponseDTO> employees = employeeService.getAllEmployees(page, size);
        return ResponseEntity.ok(employees);
    }

    /*
            If you don't specify name in @RequestParam, Spring uses the Java parameter name as the query parameter name.

            For example:
            @RequestParam(defaultValue = "name") String sortBy

            Spring expects the URL parameter sortBy.

            Equivalent explicit version:
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy

            Both work the same way.

            Remember:
            - sortBy (Java variable) → parameter name by default.
            - defaultValue = "name" → used when the query parameter is omitted.

            For example:GET /api/employees/sorting?sortBy=salary

            If you omit sortBy, Spring uses the default value "name".
     */
    @GetMapping("/sorting")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployeesSorted(
            @RequestParam(defaultValue = "name", name = "sortBy") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        List<EmployeeResponseDTO> employees =
                employeeService.getAllEmployeesSorted(sortBy, direction);

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/page-sort")
    public ResponseEntity<Page<EmployeeResponseDTO>> getEmployeesWithPaginationAndSorting(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<EmployeeResponseDTO> employees =
                employeeService.getAllEmployees(page, size, sortBy, direction);

        return ResponseEntity.ok(employees);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        EmployeeResponseDTO response = employeeService.saveEmployee(requestDTO);
        logger.info("Employee Created {}", response.toString());
        logger.info("Employee created successfully. Employee ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {
        EmployeeResponseDTO response = employeeService.updateEmployee(id, requestDTO);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {

        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> patchEmployee(@PathVariable Long id, @Valid @RequestBody EmployeePatchDTO patchDTO) {

        EmployeeResponseDTO response = employeeService.patchEmployee(id, patchDTO);
        return ResponseEntity.ok(response);
    }

    // Search employees by name
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponseDTO>> searchEmployeesByName(@RequestParam @NotBlank(message = "Name cannot be empty") String name) {

        List<EmployeeResponseDTO> employees = employeeService.searchEmployeesByName(name);

        return ResponseEntity.ok(employees);
    }

    // Filter employees by department
    @GetMapping("/department")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByDepartment(
            @RequestParam @NotBlank(message = "Department cannot be empty") String department) {

        List<EmployeeResponseDTO> employees = employeeService.getEmployeesByDepartment(department);

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/salary-range")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesBySalaryRange(
            @RequestParam Double minSalary,
            @RequestParam Double maxSalary) {

        List<EmployeeResponseDTO> employees = employeeService.getEmployeesBySalaryRange(minSalary, maxSalary);

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/salary-range/pagination")
    public ResponseEntity<Page<EmployeeResponseDTO>> getEmployeesBySalaryRangeWithPagination(
            @RequestParam Double minSalary,
            @RequestParam Double maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "salary") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<EmployeeResponseDTO> employees =
                employeeService.getEmployeesBySalaryRangeWithPagination(
                        minSalary, maxSalary, page, size, sortBy, direction);

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<EmployeeResponseDTO>> filterEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<EmployeeResponseDTO> employees =
                employeeService.filterEmployees(
                        name,
                        department,
                        minSalary,
                        maxSalary,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(employees);
    }

}
