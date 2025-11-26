package com.karandev.paymaster.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PayrollConfigurationRequestDto {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    // Allowance Applicability
    private Boolean hraApplicable;
    private Boolean conveyanceApplicable;
    private Boolean medicalApplicable;
    private Boolean bonusApplicable;

    // Allowance Values
    @DecimalMin(value = "0", message = "HRA percentage must be >= 0")
    @DecimalMax(value = "100", message = "HRA percentage must be <= 100")
    private BigDecimal hraPercentage;

    private BigDecimal conveyanceAmount;

    private BigDecimal medicalAllowanceAmount;



    @DecimalMin(value = "0", message = "Bonus percentage must be >= 0")
    @DecimalMax(value = "100", message = "Bonus percentage must be <= 100")
    private BigDecimal bonusPercentage;

    // PF / ESI Applicability
    private Boolean pfApplicable;
    private Boolean esiApplicable;

    @DecimalMin(value = "0", message = "PF Employee % must be >= 0")
    @DecimalMax(value = "100", message = "PF Employee % must be <= 100")
    private BigDecimal pfEmployeePercentage;

    @DecimalMin(value = "0", message = "PF Employer % must be >= 0")
    @DecimalMax(value = "100", message = "PF Employer % must be <= 100")
    private BigDecimal pfEmployerPercentage;

    @DecimalMin(value = "0", message = "ESI Employee % must be >= 0")
    @DecimalMax(value = "100", message = "ESI Employee % must be <= 100")
    private BigDecimal esiEmployeePercentage;

    @DecimalMin(value = "0", message = "ESI Employer % must be >= 0")
    @DecimalMax(value = "100", message = "ESI Employer % must be <= 100")
    private BigDecimal esiEmployerPercentage;

    @NotNull(message = "Professional tax amount is required")
    private BigDecimal professionalTax;

    // Tax Slabs
    private BigDecimal taxSlab1Limit;

    @DecimalMin(value = "0", message = "Tax slab 1 rate must be >= 0")
    @DecimalMax(value = "100", message = "Tax slab 1 rate must be <= 100")
    private BigDecimal taxSlab1Rate;

    private BigDecimal taxSlab2Limit;

    @DecimalMin(value = "0", message = "Tax slab 2 rate must be >= 0")
    @DecimalMax(value = "100", message = "Tax slab 2 rate must be <= 100")
    private BigDecimal taxSlab2Rate;

    private BigDecimal taxSlab3Limit;

    @DecimalMin(value = "0", message = "Tax slab 3 rate must be >= 0")
    @DecimalMax(value = "100", message = "Tax slab 3 rate must be <= 100")
    private BigDecimal taxSlab3Rate;
}