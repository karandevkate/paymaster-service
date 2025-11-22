package com.karandev.paymaster.dto;

import com.karandev.paymaster.entity.Company;
import com.karandev.paymaster.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private  String employeeContactNumber;
    private UUID companyId;
    private String companyName;

        private String month;
        private Integer year;

    private BigDecimal baseSalary;
    private BigDecimal grossSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal specialAllowance;
    private BigDecimal pfAmount;
    private BigDecimal professionalTaxAmount;
    private BigDecimal incomeTaxAmount;
//    private BigDecimal lopDeduction;
    private BigDecimal netSalary;
    private LocalDateTime generatedAt;
}
