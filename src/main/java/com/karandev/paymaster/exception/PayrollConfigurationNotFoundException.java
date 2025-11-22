package com.karandev.paymaster.exception;

import java.util.UUID;

public class PayrollConfigurationNotFoundException extends RuntimeException {
    public PayrollConfigurationNotFoundException(String message) {
        super(message);
    }

    public PayrollConfigurationNotFoundException(UUID companyId) {
        super("EmployeePayroll Configuration not found For a company: " + companyId);
    }

}
