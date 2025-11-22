package com.karandev.paymaster.entity;

import jakarta.persistence.*;
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
    @Column()
    private UUID payrollConfigurationId;

    @OneToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private BigDecimal hraPercentage;
    private BigDecimal daPercentage;
    private BigDecimal pfPercentage;

    private BigDecimal taxSlab1Limit;
    private BigDecimal taxSlab1Percentage;

    private BigDecimal taxSlab2Limit;
    private BigDecimal taxSlab2Percentage;

    private BigDecimal taxSlab3Percentage;

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
