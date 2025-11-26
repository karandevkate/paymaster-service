package com.karandev.paymaster.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SalaryStructureResponseDto {

    private UUID salaryStructureId;
    private UUID companyId;
    private UUID employeeId;
    private String employeeName;

    // -------------------------------------------------------------------------
    // EARNINGS
    // -------------------------------------------------------------------------
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal conveyance;
    private BigDecimal medicalAllowance;
    private BigDecimal specialAllowance;
    private BigDecimal bonusAmount;

    // Critical Payslip Fields
    private BigDecimal grossEarnings;           // Gross PM(A) → 19726
    private BigDecimal grossMonthlyCtcBase;     // (A)+(B) PM → 20834

    @Deprecated
    private BigDecimal grossSalary;             // Legacy — will be removed

    // -------------------------------------------------------------------------
    // STATUTORY CONTRIBUTIONS (Employee Part - shown on payslip)
    // -------------------------------------------------------------------------
    private BigDecimal pfEmployee;
    private BigDecimal esiEmployee;
    private BigDecimal professionalTax;
    private BigDecimal incomeTax;
    private BigDecimal totalDeductions;

    // -------------------------------------------------------------------------
    // EMPLOYER CONTRIBUTIONS (Not shown on employee payslip, but needed for HR/CTC)
    // -------------------------------------------------------------------------
    private BigDecimal pfEmployer;   // e.g., 1560 (12% + admin charges)
    private BigDecimal esiEmployer; // e.g., ~642 (3.25% typically)

    // -------------------------------------------------------------------------
    // FINAL
    // -------------------------------------------------------------------------
    private BigDecimal netSalary;
    private BigDecimal ctc;         // Annual CTC including employer contributions
}