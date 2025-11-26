package com.karandev.paymaster.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "salary_structure")
@Data
public class EmployeeSalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private BigDecimal basicSalary;

    private BigDecimal hra;

    private BigDecimal conveyance;

    private BigDecimal medicalAllowance;

    private BigDecimal specialAllowance;

    private BigDecimal bonusAmount;

    private BigDecimal grossSalary;

    private BigDecimal pfEmployee;

    private BigDecimal pfEmployer;

    private BigDecimal esiEmployee;

    private BigDecimal esiEmployer;

    private BigDecimal professionalTax;

    private BigDecimal incomeTax;

    private BigDecimal netSalary;

    private BigDecimal ctc;

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
