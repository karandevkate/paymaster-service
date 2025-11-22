package com.karandev.paymaster.service.impl;

import com.karandev.paymaster.dto.SalaryStructureRequestDto;
import com.karandev.paymaster.dto.SalaryStructureResponseDto;
import com.karandev.paymaster.entity.Employee;
import com.karandev.paymaster.entity.EmployeeSalaryStructure;
import com.karandev.paymaster.entity.PayrollConfiguration;
import com.karandev.paymaster.exception.EmployeeNotFoundException;
import com.karandev.paymaster.exception.PayrollConfigurationNotFoundException;
import com.karandev.paymaster.repository.EmployeeRepository;
import com.karandev.paymaster.repository.EmployeeSalaryStructureRepository;
import com.karandev.paymaster.repository.PayrollConfigurationRepository;
import com.karandev.paymaster.service.EmployeeSalaryStructureService;
import com.karandev.paymaster.service.PayrollConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeSalaryStructureServiceImpl implements EmployeeSalaryStructureService {

    private final EmployeeSalaryStructureRepository employeeSalaryStructureRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollConfigurationRepository payrollConfigurationRepository;
    private static final Logger log = LoggerFactory.getLogger(EmployeeSalaryStructureServiceImpl.class);

    public EmployeeSalaryStructureServiceImpl(EmployeeSalaryStructureRepository employeeSalaryStructureRepository,
                                              EmployeeRepository employeeRepository, PayrollConfigurationRepository payrollConfigurationRepository) {
        this.employeeSalaryStructureRepository = employeeSalaryStructureRepository;
        this.employeeRepository = employeeRepository;
        this.payrollConfigurationRepository = payrollConfigurationRepository;
    }

    private EmployeeSalaryStructure mapDtoToEntity(SalaryStructureRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(dto.getEmployeeId()));

        EmployeeSalaryStructure entity = new EmployeeSalaryStructure();
        entity.setEmployee(employee);
        entity.setCompany(employee.getCompany());
        entity.setGrossSalary(dto.getGrossSalary());
        entity.setBasicSalary(dto.getBasicSalary());
        entity.setSpecialAllowance(dto.getSpecialAllowance());

        return entity;
    }

    private SalaryStructureResponseDto mapEntityToDto(EmployeeSalaryStructure entity) {

        SalaryStructureResponseDto dto = new SalaryStructureResponseDto();
        dto.setSalaryStructureId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getEmployeeId());
        dto.setEmployeeName(entity.getEmployee().getName());
        dto.setCompanyId(entity.getCompany().getCompanyId());

        PayrollConfiguration config = payrollConfigurationRepository
                .findByCompany_CompanyId(entity.getCompany().getCompanyId())
                .orElseThrow(() ->
                        new PayrollConfigurationNotFoundException(entity.getCompany().getCompanyId())
                );

        BigDecimal basic = entity.getBasicSalary();
        BigDecimal allowance = entity.getSpecialAllowance();

        BigDecimal hra = basic
                .multiply(config.getHraPercentage())
                .divide(BigDecimal.valueOf(100));

        BigDecimal da = basic
                .multiply(config.getDaPercentage())
                .divide(BigDecimal.valueOf(100));



        BigDecimal grossSalary = basic
                .add(allowance)
                .add(hra)
                .add(da);


        dto.setBasicSalary(basic);
        dto.setSpecialAllowance(allowance);
        dto.setGrossSalary(grossSalary);

        return dto;
    }


    @Override
    @Transactional
    public void setEmployeeSalaryStructure(SalaryStructureRequestDto dto) {
        log.info("Creating salary structure for employeeId={} companyId={}", dto.getEmployeeId(), dto.getCompanyId());
        EmployeeSalaryStructure entity = mapDtoToEntity(dto);
        employeeSalaryStructureRepository.save(entity);
        log.info("Salary structure created with id={}", entity.getId());
    }

    @Override
    @Transactional
    public void udpateEmployeeSalaryStructure(SalaryStructureRequestDto dto) {
        log.info("Updating salary structure for employeeId={} companyId={}", dto.getEmployeeId(), dto.getCompanyId());

        EmployeeSalaryStructure existing = employeeSalaryStructureRepository
                .findByEmployee_EmployeeIdAndCompany_CompanyId(dto.getEmployeeId(), dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException(
                        "Salary structure not found for employeeId=" + dto.getEmployeeId() +
                                " and companyId=" + dto.getCompanyId()));

        PayrollConfiguration config = payrollConfigurationRepository
                .findByCompany_CompanyId(dto.getCompanyId())
                .orElseThrow(() -> new PayrollConfigurationNotFoundException(dto.getCompanyId()));

        existing.setBasicSalary(dto.getBasicSalary());
        existing.setSpecialAllowance(dto.getSpecialAllowance());

        BigDecimal basic = dto.getBasicSalary();
        BigDecimal allowance = dto.getSpecialAllowance();

        BigDecimal hra = basic.multiply(config.getHraPercentage()).divide(BigDecimal.valueOf(100));
        BigDecimal da = basic.multiply(config.getDaPercentage()).divide(BigDecimal.valueOf(100));

        BigDecimal gross = basic.add(allowance).add(hra).add(da);

        existing.setGrossSalary(gross);

        employeeSalaryStructureRepository.save(existing);

        log.info("Salary structure updated successfully for id={} | New Gross={}",
                existing.getId(), gross);
    }


    @Override
    public SalaryStructureResponseDto getSalaryStructureForEmployeeByEmpIdAndCompanyId(UUID employeeId, UUID companyId) {
        log.info("Fetching salary structure for employeeId={} companyId={}", employeeId, companyId);

        EmployeeSalaryStructure entity = employeeSalaryStructureRepository
                .findByEmployee_EmployeeIdAndCompany_CompanyId(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException(
                        "Salary structure not found for employeeId=" + employeeId +
                                " and companyId=" + companyId));

        log.info("Salary structure fetched successfully with id={}", entity.getId());
        return mapEntityToDto(entity);
    }
}
