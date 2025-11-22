package com.karandev.paymaster.dto;

import com.karandev.paymaster.entity.Employee;
import com.karandev.paymaster.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String userId;
    private String username;
    private String companyId;
    private Role userRole;
}
