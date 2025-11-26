package com.karandev.paymaster.service;

import com.karandev.paymaster.dto.EmployeePayrollResponseDto;
import com.karandev.paymaster.dto.SalaryStructureRequestDto;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface EmployeePayrollService {

   List<EmployeePayrollResponseDto> fetchEmployeePayrollByCompanyId(UUID companyID);
    List<EmployeePayrollResponseDto> fetchEmployeePayrollByCompanyIdAndEmployeeID(UUID employeeID,UUID companyID);

    void createEmployeePayrollByCompanyId() throws IOException;
//    void generatePayrollForCompanyManually(UUID companyId) throws IOException;
}
