package com.karandev.paymaster.dto;

import lombok.Data;

@Data
public class CompanyUpdateDTO {
    private String name;
    private String email;
    private String contactNumber;
    private String address;
    private String registrationNumber;
}
