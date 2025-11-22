package com.karandev.paymaster.dto;

import com.karandev.paymaster.entity.Company;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PayrollConfigurationRequestDto {
    private UUID companyId;
    private BigDecimal hraPercentage;
    private BigDecimal daPercentage;
    private BigDecimal pfPercentage;
    private BigDecimal taxSlab1Limit;
    private BigDecimal taxSlab1Percentage;
    private BigDecimal taxSlab2Limit;
    private BigDecimal taxSlab2Percentage;
    private BigDecimal taxSlab3Percentage;
}
