package com.karandev.paymaster.dto;


import com.karandev.paymaster.entity.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EmployeeUpdateRequestDto {
    private String name;
    private String email;
    private String contactNumber;
    private String department;
    private LocalDate birthdate;
    private String designation;
    private Gender gender;
    private LocalDate joiningDate;
    private UUID companyId;
}

