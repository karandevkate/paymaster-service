package com.karandev.paymaster.controller;

import com.karandev.paymaster.dto.LoginRequest;
import com.karandev.paymaster.dto.LoginResponse;
import com.karandev.paymaster.service.impl.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());
        LoginResponse response = authService.authenticate(request.getEmail(), request.getPassword());
        log.info("Login successful for email={}", request.getEmail());
        return ResponseEntity.ok(response);
    }
}
