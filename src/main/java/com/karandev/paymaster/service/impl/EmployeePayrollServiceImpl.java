
package com.karandev.paymaster.service.impl;

import com.karandev.paymaster.dto.EmployeePayrollResponseDto;
import com.karandev.paymaster.entity.*;
import com.karandev.paymaster.helper.EmailService;
import com.karandev.paymaster.helper.PdfGenerationService;
import com.karandev.paymaster.repository.*;
import com.karandev.paymaster.service.EmployeePayrollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeePayrollServiceImpl implements EmployeePayrollService {

    private static final Logger log = LoggerFactory.getLogger(EmployeePayrollServiceImpl.class);
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ESI_LIMIT = new BigDecimal("21000");

    private final EmployeePayrollRepository employeePayrollRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryStructureRepository employeeSalaryStructureRepository;
    private final CompanyRepository companyRepository;
    private final PayrollConfigurationRepository payrollConfigurationRepository;
    private final EmailService emailService;
    private final PdfGenerationService pdfGenerationService;

    public EmployeePayrollServiceImpl(EmployeePayrollRepository employeePayrollRepository,
                                      EmployeeRepository employeeRepository,
                                      EmployeeSalaryStructureRepository employeeSalaryStructureRepository,
                                      CompanyRepository companyRepository,
                                      PayrollConfigurationRepository payrollConfigurationRepository,
                                      EmailService emailService,
                                      PdfGenerationService pdfGenerationService) {
        this.employeePayrollRepository = employeePayrollRepository;
        this.employeeRepository = employeeRepository;
        this.employeeSalaryStructureRepository = employeeSalaryStructureRepository;
        this.companyRepository = companyRepository;
        this.payrollConfigurationRepository = payrollConfigurationRepository;
        this.emailService = emailService;
        this.pdfGenerationService = pdfGenerationService;
    }


    private BigDecimal calculateProfessionalTax(BigDecimal grossSalary, String gender, int month) {
        if (grossSalary == null || grossSalary.compareTo(ZERO) <= 0) {
            return ZERO;
        }

        boolean isFemale = "FEMALE".equalsIgnoreCase(gender != null ? gender.trim() : "");
        boolean isFebruary = (month == 2);

        BigDecimal pt;

        if (isFemale) {
            pt = grossSalary.compareTo(new BigDecimal("25000")) > 0 ? new BigDecimal("200") : ZERO;
        } else {
            if (grossSalary.compareTo(new BigDecimal("7500")) <= 0) {
                pt = ZERO;
            } else if (grossSalary.compareTo(new BigDecimal("10000")) <= 0) {
                pt = new BigDecimal("175");
            } else {
                pt = new BigDecimal("200");
            }
        }

        if (isFebruary && pt.compareTo(new BigDecimal("175")) > 0) {
            pt = new BigDecimal("300");
        }

        return pt;
    }


    private EmployeePayroll calculatePayroll(Employee employee, Company company,
                                             EmployeeSalaryStructure structure, PayrollConfiguration config,
                                             int month, int year) {

        BigDecimal basic = nullSafe(structure.getBasicSalary());
        BigDecimal specialAllowance = nullSafe(structure.getSpecialAllowance());
        BigDecimal bonus = nullSafe(structure.getBonusAmount());

        BigDecimal hra = ZERO;
        if (Boolean.TRUE.equals(config.getHraApplicable()) && config.getHraPercentage() != null) {
            hra = basic.multiply(config.getHraPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal conveyance = ZERO;
        if (Boolean.TRUE.equals(config.getConveyanceApplicable()) && config.getConveyanceAmount() != null) {
            conveyance = config.getConveyanceAmount();
        }

        BigDecimal medical = ZERO;
        if (Boolean.TRUE.equals(config.getMedicalApplicable()) && config.getMedicalAllowanceAmount() != null) {
            medical = config.getMedicalAllowanceAmount();
        }

        BigDecimal grossSalary = basic
                .add(hra)
                .add(conveyance)
                .add(medical)
                .add(specialAllowance)
                .add(bonus);

        BigDecimal pfEmployee = ZERO;
        BigDecimal pfEmployer = ZERO;
        if (Boolean.TRUE.equals(config.getPfApplicable())) {
            if (config.getPfEmployeePercentage() != null) {
                pfEmployee = basic.multiply(config.getPfEmployeePercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            if (config.getPfEmployerPercentage() != null) {
                pfEmployer = basic.multiply(config.getPfEmployerPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal esiEmployee = ZERO;
        BigDecimal esiEmployer = ZERO;
        if (Boolean.TRUE.equals(config.getEsiApplicable()) && grossSalary.compareTo(ESI_LIMIT) <= 0) {
            BigDecimal esiEmpRate = config.getEsiEmployeePercentage() != null
                    ? config.getEsiEmployeePercentage() : new BigDecimal("0.75");
            BigDecimal esiEmprRate = config.getEsiEmployerPercentage() != null
                    ? config.getEsiEmployerPercentage() : new BigDecimal("3.25");

            esiEmployee = grossSalary.multiply(esiEmpRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            esiEmployer = grossSalary.multiply(esiEmprRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal professionalTax = calculateProfessionalTax(grossSalary, employee.getGender().toString(), month);

        BigDecimal incomeTaxMonthly = ZERO;
        BigDecimal annualGross = grossSalary.multiply(BigDecimal.valueOf(12));
        if (annualGross.compareTo(ZERO) > 0 && config.getTaxSlab1Limit() != null) {
            BigDecimal annualTax = calculateProgressiveIncomeTax(annualGross, config);
            incomeTaxMonthly = annualTax.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalDeductions = pfEmployee
                .add(esiEmployee)
                .add(professionalTax)
                .add(incomeTaxMonthly);

        BigDecimal netSalary = grossSalary.subtract(totalDeductions);

        EmployeePayroll payroll = new EmployeePayroll();
        payroll.setCompany(company);
        payroll.setEmployee(employee);
        payroll.setMonth(month);
        payroll.setYear(year);
        payroll.setGeneratedAt(LocalDateTime.now());

        payroll.setBasicSalary(basic);
        payroll.setHra(hra);
        payroll.setConveyance(conveyance);
        payroll.setMedicalAllowance(medical);
        payroll.setSpecialAllowance(specialAllowance);
        payroll.setBonusAmount(bonus);
        payroll.setGrossSalary(grossSalary);

        payroll.setPfEmployeeAmount(pfEmployee);
        payroll.setPfEmployerAmount(pfEmployer);
        payroll.setEsiEmployeeAmount(esiEmployee);
        payroll.setEsiEmployerAmount(esiEmployer);
        payroll.setProfessionalTaxAmount(professionalTax);
        payroll.setIncomeTaxAmount(incomeTaxMonthly);

        payroll.setNetSalary(netSalary);

        return payroll;
    }

    private BigDecimal calculateProgressiveIncomeTax(BigDecimal annualIncome, PayrollConfiguration config) {
        BigDecimal tax = ZERO;
        BigDecimal income = annualIncome;

        BigDecimal slab1Limit = nullSafe(config.getTaxSlab1Limit());
        BigDecimal slab1Rate = nullSafe(config.getTaxSlab1Rate());
        BigDecimal slab2Limit = nullSafe(config.getTaxSlab2Limit());
        BigDecimal slab2Rate = nullSafe(config.getTaxSlab2Rate());
        BigDecimal slab3Rate = nullSafe(config.getTaxSlab3Rate());

        if (income.compareTo(slab1Limit) <= 0) {
            return income.multiply(slab1Rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        tax = tax.add(slab1Limit.multiply(slab1Rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        income = income.subtract(slab1Limit);

        BigDecimal slab2Width = slab2Limit.subtract(slab1Limit);
        if (income.compareTo(slab2Width) <= 0) {
            return tax.add(income.multiply(slab2Rate).divide(BigDecimal.valueOf(100),  2, RoundingMode.HALF_UP));
        }

        tax = tax.add(slab2Width.multiply(slab2Rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        income = income.subtract(slab2Width);

        tax = tax.add(income.multiply(slab3Rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        return tax;
    }


    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : ZERO;
    }


    private EmployeePayrollResponseDto mapToEmployeePayrollResponse(EmployeePayroll payroll) {
        EmployeePayrollResponseDto dto = new EmployeePayrollResponseDto();

        dto.setPayRollId(payroll.getPayRollId());
        dto.setEmployeeID(payroll.getEmployee().getEmployeeId());
        dto.setEmployeeName(payroll.getEmployee().getName());
        dto.setEmpCode(payroll.getEmployee().getEmpCode());
        dto.setDesignation(payroll.getEmployee().getDesignation());
        dto.setEmployeeEmail(payroll.getEmployee().getEmail());
        dto.setEmployeeContactNumber(payroll.getEmployee().getContactNumber());
        dto.setCompanyId(payroll.getCompany().getCompanyId());
        dto.setCompanyName(payroll.getCompany().getName());
        dto.setMonth(Month.of(payroll.getMonth()).toString());
        dto.setYear(payroll.getYear());

        dto.setBasicSalary(payroll.getBasicSalary());
        dto.setHra(payroll.getHra());
        dto.setConveyance(payroll.getConveyance());
        dto.setMedicalAllowance(payroll.getMedicalAllowance());
        dto.setSpecialAllowance(payroll.getSpecialAllowance());
        dto.setBonusAmount(payroll.getBonusAmount());
        dto.setGrossSalary(payroll.getGrossSalary());

        dto.setPfEmployeeAmount(payroll.getPfEmployeeAmount());
        dto.setPfEmployerAmount(payroll.getPfEmployerAmount());
        dto.setEsiEmployeeAmount(payroll.getEsiEmployeeAmount());
        dto.setEsiEmployerAmount(payroll.getEsiEmployerAmount());
        dto.setProfessionalTaxAmount(payroll.getProfessionalTaxAmount());
        dto.setIncomeTaxAmount(payroll.getIncomeTaxAmount());

        // Calculate total deductions here (not stored in DB)
        BigDecimal totalDeductions = payroll.getPfEmployeeAmount()
                .add(payroll.getEsiEmployeeAmount())
                .add(payroll.getProfessionalTaxAmount())
                .add(payroll.getIncomeTaxAmount());

        dto.setTotalDeductions(totalDeductions);
        dto.setNetSalary(payroll.getNetSalary());
        dto.setGeneratedAt(payroll.getGeneratedAt());

        return dto;
    }


    @Override
    public List<EmployeePayrollResponseDto> fetchEmployeePayrollByCompanyId(UUID companyId) {
        return employeePayrollRepository.findByCompany_CompanyIdOrderByYearDescMonthDesc(companyId)
                .stream()
                .map(this::mapToEmployeePayrollResponse)
                .toList();
    }

    @Override
    public List<EmployeePayrollResponseDto> fetchEmployeePayrollByCompanyIdAndEmployeeID(UUID employeeId, UUID companyId) {
        return employeePayrollRepository.findByEmployee_EmployeeIdAndCompany_CompanyIdOrderByYearDescMonthDesc(employeeId, companyId)
                .stream()
                .map(this::mapToEmployeePayrollResponse)
                .toList();
    }


    @Override
    @Transactional
    @Scheduled(fixedDelay = 30000)
    public void createEmployeePayrollByCompanyId() {
        log.info("Monthly payroll generation started at {}", LocalDateTime.now());

        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        List<Company> companies = companyRepository.findAll();

        for (Company company : companies) {
            UUID companyId = company.getCompanyId();

            PayrollConfiguration config = payrollConfigurationRepository
                    .findByCompany_CompanyIdAndIsActiveTrue(companyId)
                    .orElse(null);

            if (config == null) {
                log.warn("No active payroll configuration found for company: {}", company.getName());
                continue;
            }

            List<Employee> employees = employeeRepository.findAllByCompany_CompanyId(companyId);

            for (Employee employee : employees) {
                try {
                    boolean alreadyExists = employeePayrollRepository.existsByEmployee_EmployeeIdAndMonthAndYear(
                            employee.getEmployeeId(), currentMonth, currentYear);

                    if (alreadyExists) {
                        log.info("Payroll already generated for {} - {}/{}", employee.getName(), currentMonth, currentYear);
                        continue;
                    }

                    EmployeeSalaryStructure structure = employeeSalaryStructureRepository
                            .findByEmployee_EmployeeIdAndCompany_CompanyId(employee.getEmployeeId(), companyId)
                            .orElse(null);

                    if (structure == null || structure.getBasicSalary() == null || structure.getBasicSalary().compareTo(ZERO) <= 0) {
                        log.warn("Valid salary structure not found for employee: {} ({})", employee.getName(), employee.getEmpCode());
                        continue;
                    }

                    EmployeePayroll payroll = calculatePayroll(employee, company, structure, config, currentMonth, currentYear);
                    EmployeePayroll savedPayroll = employeePayrollRepository.save(payroll);

                    byte[] pdf = pdfGenerationService.generateSalarySlipPdf(savedPayroll);
                    String fileName = String.format("Salary_Slip_%s_%s_%d.pdf", employee.getEmpCode(), Month.of(currentMonth), currentYear);

                    emailService.sendSalarySlip(
                            employee.getEmail(),
                            company.getName() + " - Salary Slip for " + Month.of(currentMonth) + " " + currentYear,
                            "Dear " + employee.getName() + ",<br><br>Please find your salary slip attached for the month of <strong>" + Month.of(currentMonth) + " " + currentYear + "</strong>.<br><br>Regards,<br>" + company.getName() + " HR Team",
                            pdf,
                            fileName
                    );

                    log.info("Payroll generated and emailed to {} ({})", employee.getName(), employee.getEmpCode());

                } catch (Exception e) {
                    log.error("Failed to generate payroll for employee: {} ({})", employee.getName(), employee.getEmployeeId(), e);
                }
            }
        }

        log.info("Payroll generation completed at {}", LocalDateTime.now());
    }
}