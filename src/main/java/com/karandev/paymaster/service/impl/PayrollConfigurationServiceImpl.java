package com.karandev.paymaster.service.impl;

import com.karandev.paymaster.dto.PayrollConfigurationRequestDto;
import com.karandev.paymaster.dto.PayrollConfigurationResponseDto;
import com.karandev.paymaster.entity.Company;
import com.karandev.paymaster.entity.PayrollConfiguration;
import com.karandev.paymaster.exception.CompanyNotFoundException;
import com.karandev.paymaster.exception.PayrollConfigurationNotFoundException;
import com.karandev.paymaster.repository.CompanyRepository;
import com.karandev.paymaster.repository.PayrollConfigurationRepository;
import com.karandev.paymaster.service.PayrollConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PayrollConfigurationServiceImpl implements PayrollConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(PayrollConfigurationServiceImpl.class);

    private final PayrollConfigurationRepository payrollConfigurationRepository;
    private final CompanyRepository companyRepository;

    public PayrollConfigurationServiceImpl(PayrollConfigurationRepository payrollConfigurationRepository, CompanyRepository companyRepository) {
        this.payrollConfigurationRepository = payrollConfigurationRepository;
        this.companyRepository = companyRepository;
    }

    private PayrollConfigurationResponseDto mapEntityToDto(PayrollConfiguration payrollConfiguration) {
        PayrollConfigurationResponseDto responseDto = new PayrollConfigurationResponseDto();
        responseDto.setPayrollConfigurationId(payrollConfiguration.getPayrollConfigurationId());
        responseDto.setCompanyId(payrollConfiguration.getCompany().getCompanyId());
        responseDto.setHraPercentage(payrollConfiguration.getHraPercentage());
        responseDto.setDaPercentage(payrollConfiguration.getDaPercentage());
        responseDto.setPfPercentage(payrollConfiguration.getPfPercentage());
        responseDto.setTaxSlab1Limit(payrollConfiguration.getTaxSlab1Limit());
        responseDto.setTaxSlab1Percentage(payrollConfiguration.getTaxSlab1Percentage());
        responseDto.setTaxSlab2Limit(payrollConfiguration.getTaxSlab2Limit());
        responseDto.setTaxSlab2Percentage(payrollConfiguration.getTaxSlab2Percentage());
        responseDto.setTaxSlab3Percentage(payrollConfiguration.getTaxSlab3Percentage());
        return responseDto;
    }

    private PayrollConfiguration mapDtoToEnitty(PayrollConfigurationRequestDto dto){
        PayrollConfiguration payrollConfiguration = new PayrollConfiguration();
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(dto.getCompanyId()));
        payrollConfiguration.setCompany(company);
        payrollConfiguration.setHraPercentage(dto.getHraPercentage());
        payrollConfiguration.setDaPercentage(dto.getDaPercentage());
        payrollConfiguration.setPfPercentage(dto.getPfPercentage());
        payrollConfiguration.setTaxSlab1Limit(dto.getTaxSlab1Limit());
        payrollConfiguration.setTaxSlab1Percentage(dto.getTaxSlab1Percentage());
        payrollConfiguration.setTaxSlab2Limit(dto.getTaxSlab2Limit());
        payrollConfiguration.setTaxSlab2Percentage(dto.getTaxSlab2Percentage());
        payrollConfiguration.setTaxSlab3Percentage(dto.getTaxSlab3Percentage());

        return payrollConfiguration;
    }

    @Override
    public void addCompanyPayrollConfiguration(PayrollConfigurationRequestDto dto) {
        PayrollConfiguration payrollConfiguration = mapDtoToEnitty(dto);
        payrollConfigurationRepository.save(payrollConfiguration);
        log.info("Added payroll configuration for companyId={}", dto.getCompanyId());
    }

    @Override
    public void updateCompanyPayrollConfiguration(PayrollConfigurationRequestDto dto) {
        PayrollConfiguration existingConfig = payrollConfigurationRepository.findByCompany_CompanyId(dto.getCompanyId())
                .orElseThrow(() -> new PayrollConfigurationNotFoundException(dto.getCompanyId()));

        existingConfig.setCompany(existingConfig.getCompany());
        existingConfig.setHraPercentage(dto.getHraPercentage());
        existingConfig.setDaPercentage(dto.getDaPercentage());
        existingConfig.setPfPercentage(dto.getPfPercentage());
        existingConfig.setTaxSlab1Limit(dto.getTaxSlab1Limit());
        existingConfig.setTaxSlab1Percentage(dto.getTaxSlab1Percentage());
        existingConfig.setTaxSlab2Limit(dto.getTaxSlab2Limit());
        existingConfig.setTaxSlab2Percentage(dto.getTaxSlab2Percentage());
        existingConfig.setTaxSlab3Percentage(dto.getTaxSlab3Percentage());

        payrollConfigurationRepository.save(existingConfig);
        log.info("Updated payroll configuration  for companyId={}", dto.getCompanyId());
    }

    @Override
    public PayrollConfigurationResponseDto fetchCompanyPayrollConfigurationByCompanyId(UUID companyId) {
        PayrollConfiguration payrollConfiguration = payrollConfigurationRepository
                .findByCompany_CompanyId(companyId)
                .orElseThrow(() -> new PayrollConfigurationNotFoundException(companyId));

        log.info("Fetched payroll configuration for companyId={}", companyId);

        return mapEntityToDto(payrollConfiguration);
    }
}
