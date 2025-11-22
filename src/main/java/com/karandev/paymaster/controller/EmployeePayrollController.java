package com.karandev.paymaster.controller;

import com.karandev.paymaster.dto.EmployeePayrollResponseDto;
import com.karandev.paymaster.service.EmployeePayrollService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payrolls")
public class EmployeePayrollController {

    private final EmployeePayrollService employeePayrollService;

    public EmployeePayrollController(EmployeePayrollService employeePayrollService ) {
        this.employeePayrollService = employeePayrollService;

    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<EmployeePayrollResponseDto>> getPayrollByCompanyId(@PathVariable UUID companyId) {
        List<EmployeePayrollResponseDto> payrollDto = employeePayrollService.fetchEmployeePayrollByCompanyId(companyId);
        return ResponseEntity.ok(payrollDto);
    }

    @GetMapping("/company/{companyId}/employee/{employeeId}")
    public ResponseEntity<List<EmployeePayrollResponseDto>> getEmployeePayrolls(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("employeeId") UUID employeeId) {

        List<EmployeePayrollResponseDto> payrollList = employeePayrollService
                .fetchEmployeePayrollByCompanyIdAndEmployeeID(employeeId, companyId );

        return ResponseEntity.ok(payrollList);
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generatePayrollManually(@RequestParam UUID companyId) throws IOException {
        employeePayrollService.generatePayrollForCompanyManually(companyId);
        return ResponseEntity.ok("Payroll generated successfully for company.");
    }




}
