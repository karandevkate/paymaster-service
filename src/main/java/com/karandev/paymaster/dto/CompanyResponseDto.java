package com.karandev.paymaster.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CompanyResponseDto {

    private UUID companyId;

    private String name;
    private String email;
    private String contactNumber;
    private String address;
    private String registrationNumber;
}
