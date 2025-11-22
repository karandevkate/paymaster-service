package com.karandev.paymaster.exception;

import java.util.UUID;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(String message) {
        super(message);
    }

    public CompanyNotFoundException(UUID companyId) {
        super("Company not found with ID: " + companyId);
    }
}
