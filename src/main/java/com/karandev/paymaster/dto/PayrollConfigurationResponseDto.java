package com.karandev.paymaster.dto;

import com.karandev.paymaster.entity.Company;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PayrollConfigurationResponseDto {
    private UUID payrollConfigurationId;
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
