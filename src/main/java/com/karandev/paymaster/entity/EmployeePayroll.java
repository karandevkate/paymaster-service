package com.karandev.paymaster.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee_payroll")
@Data
public class EmployeePayroll {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID payRollId;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private Integer month;
    private Integer year;

    @Column(precision = 19, scale = 2)
    private BigDecimal basicSalary;

    @Column(precision = 19, scale = 2)
    private BigDecimal hra;

    @Column(precision = 19, scale = 2)
    private BigDecimal conveyance;

    @Column(precision = 19, scale = 2)
    private BigDecimal medicalAllowance;

    @Column(precision = 19, scale = 2)
    private BigDecimal specialAllowance;

    @Column(precision = 19, scale = 2)
    private BigDecimal bonusAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal grossSalary;

    @Column(precision = 19, scale = 2)
    private BigDecimal pfEmployeeAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal pfEmployerAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal esiEmployeeAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal esiEmployerAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal professionalTaxAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal incomeTaxAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal netSalary;

    private LocalDateTime generatedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        generatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
