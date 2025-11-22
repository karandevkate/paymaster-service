package com.karandev.paymaster.service;

import com.karandev.paymaster.dto.PayrollConfigurationRequestDto;
import com.karandev.paymaster.dto.PayrollConfigurationResponseDto;

import java.util.UUID;

public interface PayrollConfigurationService {

    void addCompanyPayrollConfiguration(PayrollConfigurationRequestDto payrollConfigurationRequestDto);

    void updateCompanyPayrollConfiguration(PayrollConfigurationRequestDto payrollConfigurationRequestDto);

    PayrollConfigurationResponseDto fetchCompanyPayrollConfigurationByCompanyId(UUID payrollConfigurationId);

}
