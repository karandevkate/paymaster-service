package com.karandev.paymaster.service.impl;

import com.karandev.paymaster.dto.LoginResponse;
import com.karandev.paymaster.entity.Employee;
import com.karandev.paymaster.exception.EmployeeNotFoundException;
import com.karandev.paymaster.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public LoginResponse authenticate(String email, String password) {
        Employee employee = employeeRepository.findByEmail(email).orElse(null);

        if (employee == null) {
            log.warn("Employee not found with email: {}", email);
            throw new EmployeeNotFoundException("Invalid email or password");
        }

        if (!employee.getPassword().equals(password)) {
            log.warn("Invalid password attempt for email: {}", email);
            throw new RuntimeException("Invalid email or password");
        }

        String token = UUID.randomUUID().toString();

        return new LoginResponse(
                token,
                employee.getEmployeeId().toString(),
                employee.getName(),
                employee.getCompany().getCompanyId().toString(),
                employee.getRole()
        );
    }
}
