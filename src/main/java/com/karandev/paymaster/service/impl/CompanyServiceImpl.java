package com.karandev.paymaster.service.impl;

import com.karandev.paymaster.dto.CompanyResponseDto;
import com.karandev.paymaster.dto.CompanyUpdateDTO;
import com.karandev.paymaster.dto.CompanyRegisterWithAdminDto;
import com.karandev.paymaster.entity.*;
import com.karandev.paymaster.exception.CompanyNotFoundException;
import com.karandev.paymaster.helper.EmailService;
import com.karandev.paymaster.helper.UniqueEmployeeCodeGenerator;
import com.karandev.paymaster.repository.CompanyRepository;
import com.karandev.paymaster.repository.EmployeeRepository;
import com.karandev.paymaster.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CompanyServiceImpl implements CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyServiceImpl.class);

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;

    public CompanyServiceImpl(JavaMailSender mailSender, CompanyRepository companyRepository, EmployeeRepository employeeRepository, EmailService emailService) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void createCompany(CompanyRegisterWithAdminDto dto) {
        log.info("Creating company: {}", dto.getName());

        Company company = new Company();
        company.setName(dto.getName());
        company.setEmail(dto.getEmail());
        company.setContactNumber(dto.getContactNumber());
        company.setAddress(dto.getAddress());
        company.setRegistrationNumber(dto.getRegistrationNumber());

        Company savedCompany = companyRepository.save(company);
        log.info("Company saved with ID: {}", savedCompany.getCompanyId());

        Employee admin = new Employee();
        admin.setName(dto.getAdminName());
        admin.setEmail(dto.getAdminEmail());
        admin.setContactNumber(dto.getContactNumber());
        admin.setBirthdate(dto.getBirthDate());
        admin.setRole(Role.ADMIN);
        admin.setStatus(EmployeeStatus.ACTIVE);
        admin.setCompany(savedCompany);
        admin.setJoiningDate(LocalDate.now());
        admin.setDepartment("System Administration");
        admin.setDesignation("Admin");

        Gender genderValue = (dto.getGender() != null)
                ? Gender.valueOf(String.valueOf(dto.getGender()))
                : null;
        admin.setGender(genderValue);

        admin.setEmpCode(UniqueEmployeeCodeGenerator.generateEmpCode(dto.getName()));

        String token = UUID.randomUUID().toString();
        admin.setPasswordToken(token);
        admin.setTokenExpiry(LocalDateTime.now().plusHours(24));

        Employee employee = employeeRepository.save(admin);
        log.info("Admin created with ID: {}", employee.getEmployeeId());

        emailService.sendSetPasswordEmail(employee.getEmployeeId());
        log.info("Set password email sent to admin: {}", employee.getEmail());
    }


    public CompanyResponseDto MapEntitytoDto(Company company) {
        if (company == null) return null;

        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setCompanyId(company.getCompanyId()); // assuming entity has getId()
        dto.setName(company.getName());
        dto.setEmail(company.getEmail());
        dto.setContactNumber(company.getContactNumber());
        dto.setAddress(company.getAddress());
        dto.setRegistrationNumber(company.getRegistrationNumber());

        return dto;
    }


    @Override
    public CompanyResponseDto getCompanyById(UUID companyId) {
        log.info("Fetching company with ID: {}", companyId);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));

        return (MapEntitytoDto(company));
    }

    @Override
    public List<CompanyResponseDto> getAllCompanies() {
        log.info("Fetching all companies");
        return companyRepository.findAll()
                .stream()
                .map(this::MapEntitytoDto)
                .toList();
    }


    @Override
    public void updateCompany(UUID companyId, CompanyUpdateDTO dto) {
        log.info("Updating company with ID: {}", companyId);

        Company existing = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setContactNumber(dto.getContactNumber());
        existing.setAddress(dto.getAddress());
        existing.setRegistrationNumber(dto.getRegistrationNumber());

        companyRepository.save(existing);
        log.info("Company updated successfully");
    }


    @Override
    public void deleteCompany(UUID companyId) {
        log.info("Deleting company with ID: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));

        companyRepository.delete(company);
        log.info("Company deleted successfully");
    }
}
