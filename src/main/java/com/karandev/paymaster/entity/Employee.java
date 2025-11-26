package com.karandev.paymaster.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "employee")
@Data
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column()
    private UUID employeeId;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String empCode;

    private String name;
    private String email;
    private String contactNumber;
    private String department;
    private LocalDate birthdate;
    private String designation;
    private LocalDate joiningDate;
    private String password;
    private String passwordToken;
    private LocalDateTime tokenExpiry;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender ;

    @Enumerated(EnumType.STRING)
    private Role role = Role.EMPLOYEE;


    @Enumerated(EnumType.STRING)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;



    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private EmployeeSalaryStructure salaryStructure;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeePayroll> employeePayrolls = new ArrayList<>();

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
