
package com.karandev.paymaster.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EmployeePayrollResponseDto {

    private UUID payRollId;

    private UUID employeeID;
    private String employeeName;
    private String empCode;
    private String designation;
    private String employeeEmail;
    private String employeeContactNumber;

    private UUID companyId;
    private String companyName;

    private String month;   // e.g., "JANUARY"
    private Integer year;

    // --------------------- EARNINGS ---------------------
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal conveyance;
    private BigDecimal medicalAllowance;
    private BigDecimal specialAllowance;
    private BigDecimal bonusAmount;

    private BigDecimal grossSalary;        // Total Earnings (already present)

    // --------------------- DEDUCTIONS ---------------------
    private BigDecimal pfEmployeeAmount;
    private BigDecimal pfEmployerAmount;
    private BigDecimal esiEmployeeAmount;
    private BigDecimal esiEmployerAmount;
    private BigDecimal professionalTaxAmount;
    private BigDecimal incomeTaxAmount;

    private BigDecimal totalDeductions;     // Sum of all employee-side deductions

    // --------------------- FINAL ---------------------
    private BigDecimal netSalary;           // Take-home salary

    private LocalDateTime generatedAt;
}