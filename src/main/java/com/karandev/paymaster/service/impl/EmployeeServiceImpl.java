package com.karandev.paymaster.service.impl;

import com.karandev.paymaster.dto.EmployeeRequestDto;
import com.karandev.paymaster.dto.EmployeeResponseDto;
import com.karandev.paymaster.dto.EmployeeUpdateRequestDto;
import com.karandev.paymaster.entity.Company;
import com.karandev.paymaster.entity.Employee;
import com.karandev.paymaster.entity.EmployeeStatus;
import com.karandev.paymaster.entity.Gender;
import com.karandev.paymaster.exception.EmployeeNotFoundException;
import com.karandev.paymaster.helper.EmailService;
import com.karandev.paymaster.helper.UniqueEmployeeCodeGenerator;
import com.karandev.paymaster.repository.CompanyRepository;
import com.karandev.paymaster.repository.EmployeeRepository;
import com.karandev.paymaster.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;

    public EmployeeServiceImpl(CompanyRepository companyRepository,
                               EmployeeRepository employeeRepository,
                               EmailService emailService) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
    }

    private Employee mapDtoToEntity(EmployeeRequestDto dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found: " + dto.getCompanyId()));

        Employee employee = new Employee();
        employee.setCompany(company);
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setContactNumber(dto.getContactNumber());
        employee.setDepartment(dto.getDepartment());
        employee.setDesignation(dto.getDesignation());
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setBirthdate(dto.getBirthdate());
        employee.setPassword(dto.getPassword());
        employee.setGender(dto.getGender() != null ? dto.getGender() : Gender.MALE);
        employee.setEmpCode(UniqueEmployeeCodeGenerator.generateEmpCode(company.getName()));

        return employee;
    }

    private EmployeeResponseDto mapEntityToDto(Employee employee) {
        if (employee == null) return null;

        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setContactNumber(employee.getContactNumber());
        dto.setDepartment(employee.getDepartment());
        dto.setDesignation(employee.getDesignation());
        dto.setBirthdate(employee.getBirthdate());
        dto.setJoiningDate(employee.getJoiningDate());
        dto.setEmpcode(employee.getEmpCode());
        dto.setCompanyId(employee.getCompany() != null ? employee.getCompany().getCompanyId() : null);
        dto.setEmployeeStatus(employee.getStatus());
        dto.setGender(employee.getGender());
        dto.setRole(employee.getRole());
        return dto;
    }

    @Override
    @Transactional
    public void createEmployee(EmployeeRequestDto dto) {
        log.info("Creating employee: {}", dto.getEmail());

        Employee employee = mapDtoToEntity(dto);
        Employee saved = employeeRepository.save(employee);

        emailService.sendSetPasswordEmail(saved.getEmployeeId());

        log.info("Employee created | ID: {} | Code: {}", saved.getEmployeeId(), saved.getEmpCode());
    }

    @Override
    public EmployeeResponseDto getEmployeeById(UUID employeeId) {
        log.info("Fetching employee: {}", employeeId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        return mapEntityToDto(employee);
    }

    @Override
    public List<EmployeeResponseDto> getAllEmployees() {
        log.info("Fetching all employees");
        return employeeRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .toList();
    }

    @Override
    public List<EmployeeResponseDto> fetchEmployeeByCompanyId(UUID companyId) {
        log.info("Fetching employees for company: {}", companyId);
        return employeeRepository.findAllByCompany_CompanyId(companyId).stream()
                .map(this::mapEntityToDto)
                .toList();
    }

    @Override
    @Transactional
    public void updateEmployee(UUID employeeId, EmployeeUpdateRequestDto dto) {
        log.info("Updating employee profile: {}", employeeId);

        Employee existing = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setContactNumber(dto.getContactNumber());
        existing.setDepartment(dto.getDepartment());
        existing.setDesignation(dto.getDesignation());
        existing.setJoiningDate(dto.getJoiningDate());
        existing.setBirthdate(dto.getBirthdate());

        if (dto.getGender() != null) {
            existing.setGender(dto.getGender());
        }


        employeeRepository.save(existing);
        log.info("Employee profile updated successfully: {}", employeeId);
    }

    @Override
    @Transactional
    public void deleteEmployee(UUID employeeId) {
        log.info("Deleting employee: {}", employeeId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        employeeRepository.delete(employee);
        log.info("Employee deleted: {}", employeeId);
    }

    @Override
    @Transactional
    public void setPassword(String token, String newPassword) {
        log.info("Password reset request with token");

        Employee employee = employeeRepository.findByPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (employee.getTokenExpiry() == null || employee.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }

        employee.setPassword(newPassword);
        employee.setPasswordToken(null);
        employee.setTokenExpiry(null);
        employeeRepository.save(employee);

        log.info("Password updated successfully for employee: {}", employee.getEmployeeId());
    }


    @Override
    @Transactional
    public void deactivateEmployee(UUID employeeId) {
        log.info("Deactivating employee: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        if (employee.getStatus() == EmployeeStatus.INACTIVE) {
            log.info("Employee {} is already inactive", employeeId);
            return;
        }

        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        log.info("Employee deactivated successfully: {}", employeeId);
    }
}