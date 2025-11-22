package com.karandev.paymaster.service;

import com.karandev.paymaster.dto.EmployeeRequestDto;
import com.karandev.paymaster.dto.EmployeeResponseDto;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    void createEmployee(EmployeeRequestDto dto);
//    String generateEmpCode(String CompanyName);

    EmployeeResponseDto getEmployeeById(UUID employeeId);

    List<EmployeeResponseDto> getAllEmployees();

    void updateEmployee(UUID employeeId, EmployeeRequestDto dto);
    void deleteEmployee(UUID employeeId);

    List<EmployeeResponseDto> fetchEmployeeByCompanyId(UUID companyId);


    void setPassword(String token, String newPassword);
//    void sendSetPasswordEmail(UUID employeeId);
}

