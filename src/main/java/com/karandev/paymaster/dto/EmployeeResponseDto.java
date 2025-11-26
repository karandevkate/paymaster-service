package com.karandev.paymaster.dto;

import com.karandev.paymaster.entity.EmployeeStatus;
import com.karandev.paymaster.entity.Gender;
import com.karandev.paymaster.entity.Role;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EmployeeResponseDto {
    private UUID employeeId;
    private String name;
    private String email;
    private String contactNumber;
    private String department;
    private String empcode;
    private Gender gender;
    private LocalDate birthdate;
    private String designation;
    private LocalDate joiningDate;
    private UUID companyId;
    private Role role;
    private EmployeeStatus employeeStatus;
}
