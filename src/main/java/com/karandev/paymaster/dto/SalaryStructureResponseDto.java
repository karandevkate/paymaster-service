package com.karandev.paymaster.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SalaryStructureResponseDto {
    private UUID salaryStructureId;
    private UUID companyId;
    private UUID  employeeId;
    private String  employeeName;
    private BigDecimal basicSalary;
    private BigDecimal grossSalary;
    private BigDecimal specialAllowance;
}
