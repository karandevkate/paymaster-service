package com.karandev.paymaster.service.impl;

import com.karandev.paymaster.dto.EmployeeRequestDto;
import com.karandev.paymaster.dto.EmployeeResponseDto;
import com.karandev.paymaster.entity.Company;
import com.karandev.paymaster.entity.Employee;
import com.karandev.paymaster.exception.CompanyNotFoundException;
import com.karandev.paymaster.exception.EmployeeNotFoundException;
import com.karandev.paymaster.helper.EmailService;
import com.karandev.paymaster.helper.UniqueEmployeeCodeGenerator;
import com.karandev.paymaster.repository.CompanyRepository;
import com.karandev.paymaster.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final CompanyRepository companyRepository;
    private final com.karandev.paymaster.repository.EmployeeRepository employeeRepository;
    private final EmailService emailService;
    public EmployeeServiceImpl(CompanyRepository companyRepository,
                               com.karandev.paymaster.repository.EmployeeRepository employeeRepository, EmailService emailService) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
    }





    public Employee mapDtoToEntity(EmployeeRequestDto dto) {
        if (dto.getCompanyId() == null) {
            throw new CompanyNotFoundException("Company ID must not be null");
        }

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(dto.getCompanyId()));

        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setContactNumber(dto.getContactNumber());
        employee.setDepartment(dto.getDepartment());
        employee.setDesignation(dto.getDesignation());
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setBirthdate(dto.getBirthdate());
        employee.setPassword(dto.getPassword());
        employee.setCompany(company);

        // Generate emp code using birthdate
        employee.setEmpCode(UniqueEmployeeCodeGenerator.generateEmpCode(company.getName()));

        return employee;
    }

    public EmployeeResponseDto MapEntitytoDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setContactNumber(employee.getContactNumber());
        dto.setDepartment(employee.getDepartment());
        dto.setBirthdate(employee.getBirthdate());
        dto.setEmpcode(employee.getEmpCode());
        dto.setDesignation(employee.getDesignation());
        dto.setJoiningDate(employee.getJoiningDate());
        dto.setCompanyId(employee.getCompany() != null ? employee.getCompany().getCompanyId() : null);
        dto.setEmployeeStatus(employee.getStatus());
        return dto;
    }

    @Override
    public void createEmployee(EmployeeRequestDto dto) {
        log.info("Creating employee: {}", dto);

        Employee employee = mapDtoToEntity(dto);
        Employee savedEmployee =employeeRepository.save(employee);
        emailService.sendSetPasswordEmail(savedEmployee.getEmployeeId());

        log.info("Employee created successfully with empCode: {}", employee.getEmpCode());
    }



    @Override
    public EmployeeResponseDto getEmployeeById(UUID employeeId) {
        log.info("Fetching employee with ID: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        return MapEntitytoDto(employee);
    }

    @Override
    public List<EmployeeResponseDto> getAllEmployees() {
        log.info("Fetching all employees");

        return employeeRepository.findAll().stream()
                .map(this::MapEntitytoDto)
                .toList();
    }


    @Override
    public void updateEmployee(UUID employeeId, EmployeeRequestDto dto) {
        log.info("Updating employee with ID: {}", employeeId);

        Employee existing = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setDepartment(dto.getDepartment());
        existing.setContactNumber(dto.getContactNumber());
        existing.setDesignation(dto.getDesignation());
        existing.setJoiningDate(dto.getJoiningDate());
        existing.setBirthdate(dto.getBirthdate());
        existing.setPassword(dto.getPassword());

        if (!existing.getCompany().getCompanyId().equals(dto.getCompanyId())) {
            Company company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new CompanyNotFoundException(dto.getCompanyId()));
            existing.setCompany(company);
        }

        employeeRepository.save(existing);
        log.info("Employee updated successfully");
    }


    @Override
    public void deleteEmployee(UUID employeeId) {
        log.info("Deleting employee with ID: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        employeeRepository.delete(employee);
        log.info("Employee deleted successfully");
    }

    @Override
    public List<EmployeeResponseDto> fetchEmployeeByCompanyId(UUID companyId) {
        log.info("Fetching employees for company with ID: {}", companyId);

        List<EmployeeResponseDto> employees = employeeRepository.findAllByCompany_CompanyId(companyId)
                .stream()
                .map(this::MapEntitytoDto)
                .toList();

        log.info("Found {} employees for company ID: {}", employees.size(), companyId);
        return employees;
    }




    public void setPassword(String token, String newPassword) {
        Employee employee = employeeRepository.findByPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (employee.getTokenExpiry() == null || employee.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        employee.setPassword(newPassword);
        employee.setPasswordToken(null);
        employee.setTokenExpiry(null);
        employeeRepository.save(employee);
    }


}
