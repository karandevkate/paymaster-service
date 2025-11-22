package com.karandev.paymaster.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CompanyRegisterWithAdminDto {
    private String name;
    private String email;
    private String contactNumber;
    private String address;
    private String registrationNumber;

    private String adminName;
    private String adminEmail;
    private String adminContactNumber;
    private LocalDate birthDate;
}
