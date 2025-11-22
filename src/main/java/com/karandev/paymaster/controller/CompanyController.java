package com.karandev.paymaster.controller;

import com.karandev.paymaster.dto.CompanyResponseDto;
import com.karandev.paymaster.dto.CompanyUpdateDTO;
import com.karandev.paymaster.dto.CompanyRegisterWithAdminDto;
import com.karandev.paymaster.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;
    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerCompany(@RequestBody CompanyRegisterWithAdminDto dto) {
        log.info("Registering company: {}", dto.getName());
        companyService.createCompany(dto);
        log.info("Company registered successfully: {}", dto.getName());
        return ResponseEntity.ok("Company registered successfully! Admin will receive a set-password email.");
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponseDto> getCompanyById(@PathVariable UUID companyId) {
        log.info("Fetching company with ID: {}", companyId);
        CompanyResponseDto company = companyService.getCompanyById(companyId);
        log.info("Fetched company: {}", company.getName());
        return ResponseEntity.ok(company);
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponseDto>> getAllCompanies() {
        log.info("Fetching all companies");
        List<CompanyResponseDto> companies = companyService.getAllCompanies();
        log.info("Fetched {} companies", companies.size());
        return ResponseEntity.ok(companies);
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<String> updateCompany(@PathVariable UUID companyId, @RequestBody CompanyUpdateDTO dto) {
        log.info("Updating company with ID: {}", companyId);
        companyService.updateCompany(companyId, dto);
        log.info("Company updated successfully: {}", companyId);
        return ResponseEntity.ok("Company updated successfully!");
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<String> deleteCompany(@PathVariable UUID companyId) {
        log.info("Deleting company with ID: {}", companyId);
        companyService.deleteCompany(companyId);
        log.info("Company deleted successfully: {}", companyId);
        return ResponseEntity.ok("Company deleted successfully!");
    }
}
