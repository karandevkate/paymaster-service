package com.karandev.paymaster.dto;


import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EmployeeRequestDto {
    private String name;
    private String email;
    private String contactNumber;
    private String department;
    private LocalDate birthdate;
    private String designation;
    private LocalDate joiningDate;
    private String password;
    private UUID companyId;
}

