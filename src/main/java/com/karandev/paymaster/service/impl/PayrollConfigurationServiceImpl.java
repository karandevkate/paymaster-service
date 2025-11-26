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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PayrollConfigurationServiceImpl implements PayrollConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(PayrollConfigurationServiceImpl.class);

    private final PayrollConfigurationRepository payrollConfigRepo;
    private final CompanyRepository companyRepository;

    public PayrollConfigurationServiceImpl(PayrollConfigurationRepository payrollConfigRepo,
                                           CompanyRepository companyRepository) {
        this.payrollConfigRepo = payrollConfigRepo;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public void addCompanyPayrollConfiguration(PayrollConfigurationRequestDto dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(dto.getCompanyId()));

        List<PayrollConfiguration> configsToSave = new ArrayList<>();

        payrollConfigRepo.findByCompany_CompanyIdAndIsActiveTrue(dto.getCompanyId())
                .ifPresent(config -> {
                    config.setIsActive(false);
                    config.setUpdatedAt(LocalDateTime.now());
                    configsToSave.add(config);
                });

        PayrollConfiguration newConfig = createNewConfig(dto, company);
        configsToSave.add(newConfig);

        payrollConfigRepo.saveAll(configsToSave);

        log.info("New payroll configuration created and activated for companyId={}", dto.getCompanyId());
    }

    @Override
    @Transactional
    public void updateCompanyPayrollConfiguration(PayrollConfigurationRequestDto dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(dto.getCompanyId()));

        List<PayrollConfiguration> configsToSave = new ArrayList<>();

        payrollConfigRepo.findByCompany_CompanyIdAndIsActiveTrue(dto.getCompanyId())
                .ifPresent(config -> {
                    config.setIsActive(false);
                    config.setUpdatedAt(LocalDateTime.now());
                    configsToSave.add(config);
                });

        PayrollConfiguration newConfig = createNewConfig(dto, company);
        configsToSave.add(newConfig);

        payrollConfigRepo.saveAll(configsToSave);

        log.info("Payroll configuration updated: old deactivated, new activated for companyId={}", dto.getCompanyId());
    }

    @Override
    public PayrollConfigurationResponseDto fetchCompanyPayrollConfigurationByCompanyId(UUID companyId) {
        PayrollConfiguration config = payrollConfigRepo
                .findByCompany_CompanyIdAndIsActiveTrue(companyId)
                .orElseThrow(() -> new PayrollConfigurationNotFoundException(companyId));

        log.info("Fetched active payroll configuration for companyId={}", companyId);
        return mapEntityToDto(config);
    }


    private PayrollConfiguration createNewConfig(PayrollConfigurationRequestDto dto, Company company) {
        PayrollConfiguration config = new PayrollConfiguration();
        mapDtoToEntity(dto, config, company);
        config.setIsActive(true);
        LocalDateTime now = LocalDateTime.now();
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        return config;
    }

    private void mapDtoToEntity(PayrollConfigurationRequestDto dto,
                                PayrollConfiguration entity,
                                Company company) {
        entity.setCompany(company);

        entity.setHraApplicable(dto.getHraApplicable() != null ? dto.getHraApplicable() : true);
        entity.setConveyanceApplicable(dto.getConveyanceApplicable() != null ? dto.getConveyanceApplicable() : false);
        entity.setMedicalApplicable(dto.getMedicalApplicable() != null ? dto.getMedicalApplicable() : false);

        entity.setHraPercentage(dto.getHraPercentage());
        entity.setConveyanceAmount(dto.getConveyanceAmount());
        entity.setMedicalAllowanceAmount(dto.getMedicalAllowanceAmount());

        entity.setPfApplicable(dto.getPfApplicable() != null ? dto.getPfApplicable() : true);
        entity.setEsiApplicable(dto.getEsiApplicable() != null ? dto.getEsiApplicable() : false);
        entity.setPfEmployeePercentage(dto.getPfEmployeePercentage());
        entity.setPfEmployerPercentage(dto.getPfEmployerPercentage());
        entity.setEsiEmployeePercentage(dto.getEsiEmployeePercentage());
        entity.setEsiEmployerPercentage(dto.getEsiEmployerPercentage());

        entity.setTaxSlab1Limit(dto.getTaxSlab1Limit());
        entity.setTaxSlab1Rate(dto.getTaxSlab1Rate());
        entity.setTaxSlab2Limit(dto.getTaxSlab2Limit());
        entity.setTaxSlab2Rate(dto.getTaxSlab2Rate());
        entity.setTaxSlab3Limit(dto.getTaxSlab3Limit());
        entity.setTaxSlab3Rate(dto.getTaxSlab3Rate());
    }

    private PayrollConfigurationResponseDto mapEntityToDto(PayrollConfiguration entity) {
        PayrollConfigurationResponseDto dto = new PayrollConfigurationResponseDto();
        dto.setPayrollConfigurationId(entity.getPayrollConfigurationId());
        dto.setCompanyId(entity.getCompany().getCompanyId());

        dto.setHraApplicable(entity.getHraApplicable());
        dto.setConveyanceApplicable(entity.getConveyanceApplicable());
        dto.setMedicalApplicable(entity.getMedicalApplicable());

        dto.setHraPercentage(entity.getHraPercentage());
        dto.setConveyanceAmount(entity.getConveyanceAmount());
        dto.setMedicalAllowanceAmount(entity.getMedicalAllowanceAmount());

        dto.setPfApplicable(entity.getPfApplicable());
        dto.setEsiApplicable(entity.getEsiApplicable());
        dto.setPfEmployeePercentage(entity.getPfEmployeePercentage());
        dto.setPfEmployerPercentage(entity.getPfEmployerPercentage());
        dto.setEsiEmployeePercentage(entity.getEsiEmployeePercentage());
        dto.setEsiEmployerPercentage(entity.getEsiEmployerPercentage());

        dto.setTaxSlab1Limit(entity.getTaxSlab1Limit());
        dto.setTaxSlab1Rate(entity.getTaxSlab1Rate());
        dto.setTaxSlab2Limit(entity.getTaxSlab2Limit());
        dto.setTaxSlab2Rate(entity.getTaxSlab2Rate());
        dto.setTaxSlab3Limit(entity.getTaxSlab3Limit());
        dto.setTaxSlab3Rate(entity.getTaxSlab3Rate());

        dto.setIsActive(entity.getIsActive());

        return dto;
    }
}