// src/main/java/com/karandev/paymaster/service/impl/EmployeeSalaryStructureServiceImpl.java

package com.karandev.paymaster.service.impl;

import com.karandev.paymaster.dto.SalaryStructureRequestDto;
import com.karandev.paymaster.dto.SalaryStructureResponseDto;
import com.karandev.paymaster.entity.*;
import com.karandev.paymaster.exception.EmployeeNotFoundException;
import com.karandev.paymaster.exception.PayrollConfigurationNotFoundException;
import com.karandev.paymaster.repository.EmployeeRepository;
import com.karandev.paymaster.repository.EmployeeSalaryStructureRepository;
import com.karandev.paymaster.repository.PayrollConfigurationRepository;
import com.karandev.paymaster.service.EmployeeSalaryStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmployeeSalaryStructureServiceImpl implements EmployeeSalaryStructureService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSalaryStructureServiceImpl.class);
    private static final BigDecimal ESI_WAGE_LIMIT = new BigDecimal("21000");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final EmployeeSalaryStructureRepository salaryStructureRepo;
    private final EmployeeRepository employeeRepo;
    private final PayrollConfigurationRepository configRepo;

    public EmployeeSalaryStructureServiceImpl(
            EmployeeSalaryStructureRepository salaryStructureRepo,
            EmployeeRepository employeeRepo,
            PayrollConfigurationRepository configRepo) {
        this.salaryStructureRepo = salaryStructureRepo;
        this.employeeRepo = employeeRepo;
        this.configRepo = configRepo;
    }

    @Override
    @Transactional
    public void setEmployeeSalaryStructure(SalaryStructureRequestDto dto) {
        PayrollConfiguration config = getActiveConfig(dto.getCompanyId());
        Employee employee = getEmployee(dto.getEmployeeId());

        EmployeeSalaryStructure structure = buildSalaryStructure(dto, config, employee);
        salaryStructureRepo.save(structure);

        log.info("Salary structure created → Employee: {} ({}), Gross: {}, CTC: {}",
                employee.getName(), employee.getEmpCode(), structure.getGrossSalary(), structure.getCtc());
    }

    @Override
    @Transactional
    public void updateEmployeeSalaryStructure(SalaryStructureRequestDto dto) {
        PayrollConfiguration config = getActiveConfig(dto.getCompanyId());
        Employee employee = getEmployee(dto.getEmployeeId());

        EmployeeSalaryStructure existing = salaryStructureRepo
                .findByEmployee_EmployeeIdAndCompany_CompanyId(dto.getEmployeeId(), dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Salary structure not found"));

        EmployeeSalaryStructure updated = buildSalaryStructure(dto, config, employee);
        copyCalculatedFields(existing, updated);
        salaryStructureRepo.save(existing);

        log.info("Salary structure updated → Employee: {} ({}), Gross: {}, CTC: {}",
                employee.getName(), employee.getEmpCode(), existing.getGrossSalary(), existing.getCtc());
    }

    private void copyCalculatedFields(EmployeeSalaryStructure target, EmployeeSalaryStructure source) {
        target.setBasicSalary(source.getBasicSalary());
        target.setHra(source.getHra());
        target.setConveyance(source.getConveyance());
        target.setMedicalAllowance(source.getMedicalAllowance());
        target.setSpecialAllowance(source.getSpecialAllowance());
        target.setBonusAmount(source.getBonusAmount());
        target.setGrossSalary(source.getGrossSalary());
        target.setIsPfApplicable(source.getIsPfApplicable());
        target.setIsEsicApplicable(source.getIsEsicApplicable());

        target.setPfEmployee(source.getPfEmployee());
        target.setPfEmployer(source.getPfEmployer());
        target.setEmployeeEsicContribution(source.getEmployeeEsicContribution());
        target.setEmployerEsicContribution(source.getEmployerEsicContribution());
        target.setTotalEsicDeduction(source.getTotalEsicDeduction());
        target.setProfessionalTax(source.getProfessionalTax());
        target.setIncomeTax(source.getIncomeTax());

        target.setNetSalary(source.getNetSalary());
        target.setCtc(source.getCtc());
        target.setUpdatedAt(LocalDateTime.now());
    }

    private EmployeeSalaryStructure buildSalaryStructure(SalaryStructureRequestDto dto,
                                                         PayrollConfiguration config,
                                                         Employee employee) {

        int currentMonth = LocalDateTime.now().getMonthValue();

        EmployeeSalaryStructure s = new EmployeeSalaryStructure();
        s.setEmployee(employee);
        s.setCompany(employee.getCompany());

        BigDecimal basic = dto.getBasicSalary().setScale(0, RoundingMode.HALF_UP);
        BigDecimal special = nullSafe(dto.getSpecialAllowance()).setScale(0, RoundingMode.HALF_UP);
        BigDecimal bonus = nullSafe(dto.getBonusAmount()).setScale(0, RoundingMode.HALF_UP);

        s.setBasicSalary(basic);
        s.setSpecialAllowance(special);
        s.setBonusAmount(bonus);

        s.setIsPfApplicable(dto.getIsPfApplicable() != null ? dto.getIsPfApplicable() : config.getIsPfApplicable());
        s.setIsEsicApplicable(dto.getIsEsicApplicable() != null ? dto.getIsEsicApplicable() : config.getIsEsicApplicable());

        BigDecimal hra = calcIfApplicable(basic, config.getHraApplicable(), config.getHraPercentage()).setScale(0, RoundingMode.HALF_UP);
        BigDecimal conveyance = calcIfApplicable(basic, config.getConveyanceApplicable(), config.getConveyancePercentage()).setScale(0, RoundingMode.HALF_UP);
        BigDecimal medical = config.getMedicalApplicable() ? nullSafe(config.getMedicalAllowanceAmount()).setScale(0, RoundingMode.HALF_UP) : ZERO;

        s.setHra(hra);
        s.setConveyance(conveyance);
        s.setMedicalAllowance(medical);

        BigDecimal grossSalary = basic
                .add(hra)
                .add(conveyance)
                .add(medical)
                .add(special)
                .add(bonus);

        s.setGrossSalary(grossSalary);

        BigDecimal pfEmployee = calcIfApplicable(basic, s.getIsPfApplicable(), config.getPfEmployeePercentage()).setScale(0, RoundingMode.HALF_UP);
        BigDecimal pfEmployer = calcIfApplicable(basic, s.getIsPfApplicable(), config.getPfEmployerPercentage()).setScale(0, RoundingMode.HALF_UP);

        s.setPfEmployee(pfEmployee);
        s.setPfEmployer(pfEmployer);

        BigDecimal employeeEsicContribution = ZERO;
        BigDecimal employerEsicContribution = ZERO;
        BigDecimal totalEsicDeduction = ZERO;
        if (Boolean.TRUE.equals(s.getIsEsicApplicable())) {
            // ESIC calculation: 0.75% for employee, 3.25% for employer. Round to nearest whole number.
            employeeEsicContribution = grossSalary.multiply(new BigDecimal("0.75")).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            employerEsicContribution = grossSalary.multiply(new BigDecimal("3.25")).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            totalEsicDeduction = employeeEsicContribution.add(employerEsicContribution);
        }
        s.setEmployeeEsicContribution(employeeEsicContribution);
        s.setEmployerEsicContribution(employerEsicContribution);
        s.setTotalEsicDeduction(totalEsicDeduction);

        s.setProfessionalTax(calculateProfessionalTax(grossSalary, employee.getGender(), currentMonth).setScale(0, RoundingMode.HALF_UP));
        s.setIncomeTax(calculateMonthlyIncomeTax(grossSalary, config).setScale(0, RoundingMode.HALF_UP));

        // Deduct both employee and employer ESI from gross salary as requested
        BigDecimal totalDeduction = pfEmployee
                .add(totalEsicDeduction)
                .add(s.getProfessionalTax())
                .add(s.getIncomeTax());

        s.setNetSalary(grossSalary.subtract(totalDeduction).setScale(0, RoundingMode.HALF_UP));

        BigDecimal annualCtc = grossSalary
                .add(pfEmployer)
                .add(employerEsicContribution)
                .multiply(BigDecimal.valueOf(12))
                .setScale(0, RoundingMode.HALF_UP);

        s.setCtc(annualCtc);

        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(s.getCreatedAt());

        return s;
    }

    private BigDecimal calcIfApplicable(BigDecimal base, Boolean applicable, BigDecimal percent) {
        if (Boolean.TRUE.equals(applicable) && percent != null && base != null) {
            return calcPercentage(base, percent);
        }
        return ZERO;
    }

    private BigDecimal calcPercentage(BigDecimal base, BigDecimal percent) {
        if (base == null || percent == null || base.compareTo(ZERO) <= 0) return ZERO;
        return base.multiply(percent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal nullSafe(BigDecimal val) {
        return val != null ? val : ZERO;
    }

    private PayrollConfiguration getActiveConfig(UUID companyId) {
        return configRepo.findByCompany_CompanyIdAndIsActiveTrue(companyId)
                .orElseThrow(() -> new PayrollConfigurationNotFoundException(companyId));
    }

    private Employee getEmployee(UUID employeeId) {
        return employeeRepo.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    // Maharashtra Professional Tax - 100% Compliant (2025)
    private BigDecimal calculateProfessionalTax(BigDecimal grossSalary, Gender gender, int month) {
        if (grossSalary == null || grossSalary.compareTo(ZERO) <= 0) {
            return ZERO;
        }

        boolean isFemale = gender == Gender.FEMALE;
        boolean isFebruary = (month == 2);
        BigDecimal pt = ZERO;

        if (isFemale) {
            if (grossSalary.compareTo(new BigDecimal("25000")) > 0) {
                pt = new BigDecimal("200");
            }
        } else {
            if (grossSalary.compareTo(new BigDecimal("7500")) <= 0) {
                return ZERO;
            } else if (grossSalary.compareTo(new BigDecimal("10000")) <= 0) {
                pt = new BigDecimal("175");
            } else {
                pt = new BigDecimal("200");
            }
        }

        // February surcharge: ₹300 instead of ₹200 (₹175 stays ₹175)
        if (isFebruary && pt.compareTo(new BigDecimal("175")) > 0) {
            pt = new BigDecimal("300");
        }

        return pt;
    }

    private BigDecimal calculateMonthlyIncomeTax(BigDecimal monthlyGross, PayrollConfiguration config) {
        if (monthlyGross == null || config == null) return ZERO;

        BigDecimal annualGross = monthlyGross.multiply(BigDecimal.valueOf(12));
        BigDecimal tax = ZERO;

        BigDecimal slab1 = nullSafe(config.getTaxSlab1Limit());
        BigDecimal slab2 = nullSafe(config.getTaxSlab2Limit());

        BigDecimal rate1 = nullSafe(config.getTaxSlab1Rate());
        BigDecimal rate2 = nullSafe(config.getTaxSlab2Rate());
        BigDecimal rate3 = nullSafe(config.getTaxSlab3Rate());

        if (annualGross.compareTo(slab1) <= 0) {
            tax = annualGross.multiply(rate1).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            tax = tax.add(slab1.multiply(rate1).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            BigDecimal remaining = annualGross.subtract(slab1);

            BigDecimal slab2Width = slab2.subtract(slab1);
            if (remaining.compareTo(slab2Width) <= 0) {
                tax = tax.add(remaining.multiply(rate2).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            } else {
                tax = tax.add(slab2Width.multiply(rate2).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                tax = tax.add(remaining.subtract(slab2Width).multiply(rate3).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }
        }

        return tax.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    @Override
    public SalaryStructureResponseDto getSalaryStructureForEmployeeByEmpIdAndCompanyId(UUID employeeId, UUID companyId) {
        EmployeeSalaryStructure entity = salaryStructureRepo
                .findByEmployee_EmployeeIdAndCompany_CompanyId(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException("Salary structure not found"));

        return mapToResponseDto(entity);
    }

    private SalaryStructureResponseDto mapToResponseDto(EmployeeSalaryStructure s) {
        SalaryStructureResponseDto dto = new SalaryStructureResponseDto();
        dto.setSalaryStructureId(s.getId());
        dto.setEmployeeId(s.getEmployee().getEmployeeId());
        dto.setEmployeeName(s.getEmployee().getName());
        dto.setCompanyId(s.getCompany().getCompanyId());

        dto.setBasicSalary(s.getBasicSalary());
        dto.setHra(s.getHra());
        dto.setConveyance(s.getConveyance());
        dto.setMedicalAllowance(s.getMedicalAllowance());
        dto.setSpecialAllowance(s.getSpecialAllowance());
        dto.setBonusAmount(s.getBonusAmount());
        dto.setGrossSalary(s.getGrossSalary());

        dto.setIsPfApplicable(s.getIsPfApplicable());
        dto.setIsEsicApplicable(s.getIsEsicApplicable());

        dto.setPfEmployee(s.getPfEmployee());
        dto.setPfEmployer(s.getPfEmployer());
        dto.setEmployeeEsicContribution(s.getEmployeeEsicContribution());
        dto.setEmployerEsicContribution(s.getEmployerEsicContribution());
        dto.setTotalEsicDeduction(s.getTotalEsicDeduction());
        dto.setProfessionalTax(s.getProfessionalTax());
        dto.setIncomeTax(s.getIncomeTax());

        dto.setNetSalary(s.getNetSalary());
        dto.setCtc(s.getCtc());

        return dto;
    }
}