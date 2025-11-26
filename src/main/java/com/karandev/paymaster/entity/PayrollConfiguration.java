package com.karandev.paymaster.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payroll_configuration")
@Data
public class PayrollConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID payrollConfigurationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;


    private Boolean hraApplicable = true;
    private Boolean conveyanceApplicable = false;
    private Boolean medicalApplicable = false;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal hraPercentage;

    private BigDecimal conveyanceAmount;

    private BigDecimal medicalAllowanceAmount;


    private Boolean pfApplicable = true;
    private Boolean esiApplicable = false;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal pfEmployeePercentage;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal pfEmployerPercentage;
    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal esiEmployeePercentage;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal esiEmployerPercentage;

    @DecimalMin("0")
    private BigDecimal taxSlab1Limit;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal taxSlab1Rate;

    @DecimalMin("0")
    private BigDecimal taxSlab2Limit;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal taxSlab2Rate;

    @DecimalMin("0")
    private BigDecimal taxSlab3Limit;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal taxSlab3Rate;

    private Boolean isActive = true;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
