package com.karandev.paymaster.exception;

import java.util.UUID;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }

    public EmployeeNotFoundException(UUID employeeId) {
        super("Employee not found with ID: " + employeeId);
    }

}
