package com.karandev.paymaster.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PayrollConfigurationResponseDto {

    private UUID payrollConfigurationId;
    private UUID companyId;

    // Allowance Applicability
    private Boolean hraApplicable;
    private Boolean conveyanceApplicable;
    private Boolean medicalApplicable;
    private Boolean bonusApplicable;

    // Allowance Values
    private BigDecimal hraPercentage;
    private BigDecimal conveyanceAmount;
    private BigDecimal medicalAllowanceAmount;
    private BigDecimal bonusPercentage;

    // PF / ESI
    private Boolean pfApplicable;
    private Boolean esiApplicable;
    private BigDecimal pfEmployeePercentage;
    private BigDecimal pfEmployerPercentage;
    private BigDecimal esiEmployeePercentage;
    private BigDecimal esiEmployerPercentage;
    private BigDecimal professionalTax;

    // Tax Slabs
    private BigDecimal taxSlab1Limit;
    private BigDecimal taxSlab1Rate;
    private BigDecimal taxSlab2Limit;
    private BigDecimal taxSlab2Rate;
    private BigDecimal taxSlab3Limit;
    private BigDecimal taxSlab3Rate;

    private Boolean isActive;
}