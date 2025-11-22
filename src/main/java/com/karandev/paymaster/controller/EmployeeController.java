package com.karandev.paymaster.controller;

import com.karandev.paymaster.dto.EmployeeRequestDto;
import com.karandev.paymaster.dto.EmployeeResponseDto;
import com.karandev.paymaster.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<String> createEmployee(@RequestBody EmployeeRequestDto dto) {
        log.info("Creating employee: {} in companyId={}", dto.getName(), dto.getCompanyId());
        employeeService.createEmployee(dto);
        log.info("Employee created successfully: {}", dto.getName());
        return ResponseEntity.ok("Employee created successfully!");
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable UUID employeeId) {
        log.info("Fetching employee with ID: {}", employeeId);
        EmployeeResponseDto employee = employeeService.getEmployeeById(employeeId);
        log.info("Fetched employee: {}", employee.getName());
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDto>> getAllEmployees() {
        log.info("Fetching all employees");
        List<EmployeeResponseDto> employees = employeeService.getAllEmployees();
        log.info("Fetched {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<String> updateEmployee(@PathVariable UUID employeeId, @RequestBody EmployeeRequestDto dto) {
        log.info("Updating employee with ID: {}", employeeId);
        employeeService.updateEmployee(employeeId, dto);
        log.info("Employee updated successfully: {}",employeeId);
        return ResponseEntity.ok("Employee updated successfully!");
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable UUID employeeId) {
        log.info("Deleting employee with ID: {}", employeeId);
        employeeService.deleteEmployee(employeeId);
        log.info("Employee deleted successfully: {}", employeeId);
        return ResponseEntity.ok("Employee deleted successfully!");
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<EmployeeResponseDto>> getEmployeesByCompanyId(@PathVariable UUID companyId) {
        log.info("Fetching employees for companyId: {}", companyId);
        List<EmployeeResponseDto> employees = employeeService.fetchEmployeeByCompanyId(companyId);
        log.info("Fetched {} employees for companyId={}", employees.size(), companyId);
        return ResponseEntity.ok(employees);
    }

    @PostMapping("/set-password")
    public ResponseEntity<String> setPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        log.info("Setting password using token: {}", token);
        employeeService.setPassword(token, newPassword);
        log.info("Password set successfully for token: {}", token);
        return ResponseEntity.ok("Password set successfully!");
    }

//    @PostMapping("/send")
//    public ResponseEntity<String> sendSetPasswordEmail(@RequestParam UUID employeeId) {
//        log.info("Sending set-password email to employeeId: {}", employeeId);
//        employeeService.sendSetPasswordEmail(employeeId);
//        log.info("Set-password email sent to employeeId: {}", employeeId);
//        return ResponseEntity.ok("Set-password email sent!");
//    }
}
