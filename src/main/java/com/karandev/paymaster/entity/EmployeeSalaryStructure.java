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
    @Column()
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(precision = 19, scale = 2)
    private BigDecimal basicSalary;

    @Column(precision = 19, scale = 2)
    private BigDecimal grossSalary;

    @Column(precision = 19, scale = 2)
    private BigDecimal specialAllowance;

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
