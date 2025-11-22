package com.karandev.paymaster.exception;

public class PayrollAlreadyExistsException extends RuntimeException {
    public PayrollAlreadyExistsException(String message) {
        super(message);
    }
}
