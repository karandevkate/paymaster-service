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

    private Boolean isPfApplicable;
    private Boolean isEsicApplicable;

    // Critical Payslip Fields
    private BigDecimal grossEarnings;           // Gross PM(A) → 19726
    private BigDecimal grossMonthlyCtcBase;     // (A)+(B) PM → 20834

    @Deprecated
    private BigDecimal grossSalary;             // Legacy — will be removed

    // -------------------------------------------------------------------------
    // STATUTORY CONTRIBUTIONS (Employee Part - shown on payslip)
    // -------------------------------------------------------------------------
    private BigDecimal pfEmployee;
    private BigDecimal pfEmployer;
    private BigDecimal employeeEsicContribution;
    private BigDecimal employerEsicContribution;
    private BigDecimal totalEsicDeduction;
    private BigDecimal professionalTax;
    private BigDecimal incomeTax;
    private BigDecimal totalDeductions;

    // -------------------------------------------------------------------------
    // FINAL
    // -------------------------------------------------------------------------
    private BigDecimal netSalary;
    private BigDecimal ctc;         // Annual CTC including employer contributions
}