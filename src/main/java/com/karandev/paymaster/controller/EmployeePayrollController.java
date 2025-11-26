package com.karandev.paymaster.controller;

import com.karandev.paymaster.dto.EmployeePayrollResponseDto;
import com.karandev.paymaster.entity.EmployeePayroll;
import com.karandev.paymaster.helper.PdfGenerationService;
import com.karandev.paymaster.repository.EmployeePayrollRepository;
import com.karandev.paymaster.service.EmployeePayrollService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Month;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payrolls")
public class EmployeePayrollController {

    private final EmployeePayrollService employeePayrollService;
    private final EmployeePayrollRepository employeePayrollRepository;
    private final PdfGenerationService pdfGenerationService;
    public EmployeePayrollController(EmployeePayrollService employeePayrollService, EmployeePayrollRepository employeePayrollRepository, PdfGenerationService pdfGenerationService) {
        this.employeePayrollService = employeePayrollService;
        this.employeePayrollRepository = employeePayrollRepository;
        this.pdfGenerationService = pdfGenerationService;
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
//
//    @PostMapping("/generate")
//    public ResponseEntity<String> generatePayrollManually(@RequestParam UUID companyId) throws IOException {
//        employeePayrollService.generatePayrollForCompanyManually(companyId);
//        return ResponseEntity.ok("Payroll generated successfully for company.");
//    }

    @GetMapping("/download/{payRollId}")
    public ResponseEntity<byte[]> downloadSalarySlip(@PathVariable UUID payRollId) {
        try {
            EmployeePayroll payroll = employeePayrollRepository.findById(payRollId)
                    .orElseThrow(() -> new RuntimeException("Payroll not found"));

            byte[] pdfBytes = pdfGenerationService.generateSalarySlipPdf(payroll);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    payroll.getEmployee().getEmpCode() + "_" +
                            Month.of(payroll.getMonth()) + "_" +
                            payroll.getYear() + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Payroll not found for ID: " + payRollId).getBytes());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("PDF generation failed: " + e.getMessage()).getBytes());
        }
    }

}
