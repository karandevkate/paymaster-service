package com.karandev.paymaster.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SalaryStructureRequestDto {
    private UUID  employeeId;
    private UUID companyId;
    private BigDecimal basicSalary;
    private BigDecimal grossSalary;
    private BigDecimal specialAllowance;
}
