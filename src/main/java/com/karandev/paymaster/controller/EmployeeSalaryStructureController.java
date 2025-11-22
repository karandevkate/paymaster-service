package com.karandev.paymaster.controller;

import com.karandev.paymaster.dto.SalaryStructureRequestDto;
import com.karandev.paymaster.dto.SalaryStructureResponseDto;
import com.karandev.paymaster.service.EmployeeSalaryStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/salary-structures")
public class EmployeeSalaryStructureController {

    private final EmployeeSalaryStructureService salaryStructureService;
    private static final Logger log = LoggerFactory.getLogger(EmployeeSalaryStructureController.class);

    public EmployeeSalaryStructureController(EmployeeSalaryStructureService salaryStructureService) {
        this.salaryStructureService = salaryStructureService;
    }

    @PostMapping
    public ResponseEntity<String> createSalaryStructure(@RequestBody SalaryStructureRequestDto dto) {
        log.info("Creating salary structure for employeeId={} companyId={}", dto.getEmployeeId(), dto.getCompanyId());
        salaryStructureService.setEmployeeSalaryStructure(dto);
        return ResponseEntity.status(201).body("Salary structure created successfully");
    }

    @PutMapping
    public ResponseEntity<String> updateSalaryStructure(@RequestBody SalaryStructureRequestDto dto) {
        log.info("Updating salary structure for employeeId={} companyId={}", dto.getEmployeeId(), dto.getCompanyId());
        salaryStructureService.udpateEmployeeSalaryStructure(dto);
        return ResponseEntity.ok("Salary structure updated successfully");
    }

    @GetMapping("/{employeeId}/{companyId}")
    public ResponseEntity<SalaryStructureResponseDto> getSalaryStructure(
            @PathVariable UUID employeeId,
            @PathVariable UUID companyId) {
        log.info("Fetching salary structure for employeeId={} companyId={}", employeeId, companyId);
        SalaryStructureResponseDto dto = salaryStructureService.getSalaryStructureForEmployeeByEmpIdAndCompanyId(employeeId, companyId);
        return ResponseEntity.ok(dto);
    }
}
