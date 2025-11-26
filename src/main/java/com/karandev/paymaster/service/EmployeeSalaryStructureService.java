package com.karandev.paymaster.service;

import com.karandev.paymaster.dto.SalaryStructureRequestDto;
import com.karandev.paymaster.dto.SalaryStructureResponseDto;

import java.util.UUID;

public interface EmployeeSalaryStructureService {

    void setEmployeeSalaryStructure(SalaryStructureRequestDto salaryStructureRequestDto);
    void updateEmployeeSalaryStructure(SalaryStructureRequestDto dto);

    SalaryStructureResponseDto getSalaryStructureForEmployeeByEmpIdAndCompanyId(UUID employeeId, UUID companyId);
}
