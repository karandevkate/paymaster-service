package com.karandev.paymaster.controller;

import com.karandev.paymaster.dto.PayrollConfigurationRequestDto;
import com.karandev.paymaster.dto.PayrollConfigurationResponseDto;
import com.karandev.paymaster.service.PayrollConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payroll-configurations")
public class PayrollConfigurationController {

    private final PayrollConfigurationService payrollConfigurationService;
    private static final Logger log = LoggerFactory.getLogger(PayrollConfigurationController.class);
    public PayrollConfigurationController(PayrollConfigurationService payrollConfigurationService) {
        this.payrollConfigurationService = payrollConfigurationService;

    }

    @PostMapping
    public ResponseEntity<String> addPayrollConfiguration(@RequestBody PayrollConfigurationRequestDto dto) {
        log.info("Adding payroll configuration for companyId={}", dto.getCompanyId());
        payrollConfigurationService.addCompanyPayrollConfiguration(dto);
        log.info("EmployeePayroll configuration added successfully for companyId={}", dto.getCompanyId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("EmployeePayroll configuration added for companyId=" + dto.getCompanyId());
    }

    @PutMapping
    public ResponseEntity<String> updatePayrollConfiguration(@RequestBody PayrollConfigurationRequestDto dto) {
        log.info("Updating payroll configuration for companyId={}", dto.getCompanyId());
        payrollConfigurationService.updateCompanyPayrollConfiguration(dto);
        log.info("EmployeePayroll configuration updated successfully for companyId={}", dto.getCompanyId());
        return ResponseEntity.ok("EmployeePayroll configuration updated for companyId=" + dto.getCompanyId());
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<PayrollConfigurationResponseDto> getPayrollConfiguration(@PathVariable UUID companyId) {
        log.info("Fetching payroll configuration for companyId={}", companyId);
        PayrollConfigurationResponseDto responseDto =
                payrollConfigurationService.fetchCompanyPayrollConfigurationByCompanyId(companyId);
        log.info("Fetched payroll configuration for companyId={}", companyId);
        return ResponseEntity.ok(responseDto);
    }


}
