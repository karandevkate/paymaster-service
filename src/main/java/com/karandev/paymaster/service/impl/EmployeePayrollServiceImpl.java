package com.karandev.paymaster.service.impl;

import com.karandev.paymaster.dto.EmployeePayrollResponseDto;
import com.karandev.paymaster.entity.*;
import com.karandev.paymaster.exception.PayrollAlreadyExistsException;
import com.karandev.paymaster.exception.PayrollConfigurationNotFoundException;
import com.karandev.paymaster.helper.EmailService;
import com.karandev.paymaster.repository.*;
import com.karandev.paymaster.service.EmployeePayrollService;
import com.karandev.paymaster.helper.PdfGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
                                      PayrollConfigurationRepository payrollConfigurationRepository, EmailService emailService, PdfGenerationService pdfGenerationService) {
        this.employeePayrollRepository = employeePayrollRepository;
        this.employeeRepository = employeeRepository;
        this.employeeSalaryStructureRepository = employeeSalaryStructureRepository;
        this.companyRepository = companyRepository;
        this.payrollConfigurationRepository = payrollConfigurationRepository;
        this.emailService = emailService;
        this.pdfGenerationService = pdfGenerationService;
    }

    private EmployeePayrollResponseDto mapToEmployeePayrollResponse(EmployeePayroll employeePayroll) {
        EmployeePayrollResponseDto dto = new EmployeePayrollResponseDto();
        dto.setPayRollId(employeePayroll.getPayRollId());
        dto.setEmployeeID(employeePayroll.getEmployee().getEmployeeId());
        dto.setEmployeeName(employeePayroll.getEmployee().getName());
        dto.setEmpCode(employeePayroll.getEmployee().getEmpCode());
        dto.setDesignation(employeePayroll.getEmployee().getDesignation());
        dto.setEmployeeEmail(employeePayroll.getEmployee().getEmail());
        dto.setEmployeeContactNumber(employeePayroll.getEmployee().getContactNumber());

        dto.setCompanyId(employeePayroll.getCompany().getCompanyId());
        dto.setCompanyName(employeePayroll.getCompany().getName());

        dto.setMonth(employeePayroll.getMonth() != 0
                ? Month.of(employeePayroll.getMonth()).toString()
                : LocalDate.now().getMonth().toString());
        dto.setYear(employeePayroll.getYear() != 0
                ? employeePayroll.getYear()
                : LocalDate.now().getYear());

        dto.setBaseSalary(employeePayroll.getEmployee().getSalaryStructure().getBasicSalary());
        dto.setGrossSalary(employeePayroll.getGrossSalary());
        dto.setHra(employeePayroll.getHra());
        dto.setDa(employeePayroll.getDa());
        dto.setSpecialAllowance(employeePayroll.getSpecialAllowance());
        dto.setPfAmount(employeePayroll.getPfAmount());
        dto.setProfessionalTaxAmount(employeePayroll.getProfessionalTaxAmount());
        dto.setIncomeTaxAmount(employeePayroll.getIncomeTaxAmount());

        dto.setNetSalary(employeePayroll.getNetSalary());
        dto.setGeneratedAt(employeePayroll.getGeneratedAt());
        return dto;
    }

    @Override
    public List<EmployeePayrollResponseDto> fetchEmployeePayrollByCompanyId(UUID companyID) {
        List<EmployeePayroll> employeePayrollList = employeePayrollRepository
                .findByCompany_CompanyIdOrderByYearDescMonthDesc(companyID);

        log.info("Fetched {} payroll records for companyId={}", employeePayrollList.size(), companyID);

        return employeePayrollList.stream()
                .map(this::mapToEmployeePayrollResponse)
                .toList();
    }

    @Override
    public List<EmployeePayrollResponseDto> fetchEmployeePayrollByCompanyIdAndEmployeeID(UUID employeeID, UUID companyID) {
        List<EmployeePayroll> employeePayrollList = employeePayrollRepository
                .findByEmployee_EmployeeIdAndCompany_CompanyIdOrderByYearDescMonthDesc(employeeID, companyID);

        log.info("Fetched {} payroll records for employee {} of companyId={}",
                employeePayrollList.size(), employeeID, companyID);

        return employeePayrollList.stream()
                .map(this::mapToEmployeePayrollResponse)
                .toList();
    }

    @Override
//    @Scheduled(initialDelay = 1000, fixedDelay = Long.MAX_VALUE)
    @Scheduled(cron = "*/30 * * * * *")
    public void createEmployeePayrollByCompanyId() throws IOException {
        log.info("Starting payroll generation for all companies at {}", LocalDateTime.now());

        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        List<Company> companies = companyRepository.findAll();
        log.info("Found {} companies to process", companies.size());

        for (Company company : companies) {
            UUID companyId = company.getCompanyId();
            log.info("Processing company: {} ({})", companyId, company.getName());

            PayrollConfiguration config = payrollConfigurationRepository
                    .findByCompany_CompanyId(companyId)
                    .orElseGet(() -> {
                        log.error("Skipping company {} - Payroll configuration not found", company.getName());
                        return null;
                    });

            if (config == null) {
                continue;
            }

            List<Employee> employees = employeeRepository.findAllByCompany_CompanyId(companyId);
            log.info("Found {} employees for company {}", employees.size(), company.getName());

            for (Employee employee : employees) {
                try {
                    boolean alreadyExists = employeePayrollRepository
                            .existsByEmployee_EmployeeIdAndMonthAndYear(
                                    employee.getEmployeeId(), currentMonth, currentYear);

                    if (alreadyExists) {
                        log.info("Payroll already generated for employee {} ({}) for {}-{} Skipping.",
                                employee.getEmployeeId(), employee.getName(), currentMonth, currentYear);
                        continue;
                    }

                    EmployeeSalaryStructure salaryStructure = employeeSalaryStructureRepository
                            .findByEmployee_EmployeeIdAndCompany_CompanyId(employee.getEmployeeId(), companyId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Salary structure not found for employee: " + employee.getEmployeeId()));

                    BigDecimal gross = salaryStructure.getGrossSalary();
                    if (gross == null || gross.compareTo(BigDecimal.ZERO) <= 0) {
                        log.warn("Skipping employee {} - Invalid or null gross salary", employee.getEmployeeId());
                        continue;
                    }

                    BigDecimal basic = salaryStructure.getBasicSalary();

                    BigDecimal pf = basic.multiply(config.getPfPercentage())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                    BigDecimal hraAmount = basic.multiply(config.getHraPercentage())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                    BigDecimal daAmount = basic.multiply(config.getDaPercentage())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                    BigDecimal specialAllowanceAmount = salaryStructure.getSpecialAllowance() != null
                            ? salaryStructure.getSpecialAllowance() : BigDecimal.ZERO;

                    BigDecimal professionalTax = BigDecimal.valueOf(200); // Make configurable if needed

                    BigDecimal yearlyGross = gross.multiply(BigDecimal.valueOf(12));
                    BigDecimal incomeTaxMonthly = calculateIncomeTax(yearlyGross, config)
                            .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

                    BigDecimal netSalary = gross
                            .subtract(pf)
                            .subtract(professionalTax)
                            .subtract(incomeTaxMonthly);

                    EmployeePayroll payroll = new EmployeePayroll();
                    payroll.setCompany(company);
                    payroll.setEmployee(employee);
                    payroll.setMonth(currentMonth);
                    payroll.setYear(currentYear);
                    payroll.setGrossSalary(gross);
                    payroll.setPfAmount(pf);
                    payroll.setHra(hraAmount);
                    payroll.setDa(daAmount);
                    payroll.setSpecialAllowance(specialAllowanceAmount);
                    payroll.setProfessionalTaxAmount(professionalTax);
                    payroll.setIncomeTaxAmount(incomeTaxMonthly);
                    payroll.setLopDeduction(BigDecimal.ZERO);
                    payroll.setNetSalary(netSalary);
                    payroll.setGeneratedAt(LocalDateTime.now());

                    EmployeePayroll savedPayroll = employeePayrollRepository.save(payroll);

                    byte[] pdfBytes = pdfGenerationService.generateSalarySlipPdf(savedPayroll);

                    String filename = String.format("SalarySlip_%s_%02d_%d.pdf",
                            employee.getEmpCode(), currentMonth, currentYear);

                    String subject = String.format("%s - Salary Slip for %02d/%d",
                            company.getName(), currentMonth, currentYear);

                    String body = String.format(
                            "Dear %s,<br><br>Please find attached your salary slip for <b>%02d/%d</b>.<br><br>" +
                                    "This is an auto-generated email. Please do not reply.<br><br>Regards,<br><b>%s Payroll Team</b>",
                            employee.getName(), currentMonth, currentYear, company.getName());

                    emailService.sendSalarySlip(employee.getEmail(), subject, body, pdfBytes, filename);

                    log.info("Successfully generated payroll and sent salary slip for employee: {} ({})",
                            employee.getEmployeeId(), employee.getName());

                } catch (Exception e) {
                    log.error("Failed to process payroll for employee {} in company {} for {}-{}",
                            employee.getEmployeeId(), company.getName(), currentMonth, currentYear, e);
                }
            }
        }

        log.info("Payroll generation completed at {}", LocalDateTime.now());
    }

    private BigDecimal calculateIncomeTax(BigDecimal yearlyIncome, PayrollConfiguration config) {
        BigDecimal tax = BigDecimal.ZERO;

        if (yearlyIncome.compareTo(config.getTaxSlab1Limit()) > 0) {
            BigDecimal slabIncome = config.getTaxSlab1Limit();
            tax = tax.add(slabIncome.multiply(config.getTaxSlab1Percentage()).divide(BigDecimal.valueOf(100)));
        }

        if (yearlyIncome.compareTo(config.getTaxSlab2Limit()) > 0) {
            BigDecimal slabIncome = config.getTaxSlab2Limit().subtract(config.getTaxSlab1Limit());
            tax = tax.add(slabIncome.multiply(config.getTaxSlab2Percentage()).divide(BigDecimal.valueOf(100)));
        }

        if (yearlyIncome.compareTo(config.getTaxSlab2Limit()) > 0) {
            BigDecimal slabIncome = yearlyIncome.subtract(config.getTaxSlab2Limit());
            tax = tax.add(slabIncome.multiply(config.getTaxSlab3Percentage()).divide(BigDecimal.valueOf(100)));
        }

        return tax;
    }


    public void generatePayrollForCompanyManually(UUID companyId) throws IOException {
        PayrollConfiguration config = payrollConfigurationRepository.findByCompany_CompanyId(companyId)
                .orElseThrow(() -> new PayrollConfigurationNotFoundException("Payroll config not found for company " + companyId));

        List<Employee> employees = employeeRepository.findAllByCompany_CompanyId(companyId);

        int currentMonth = 11;
        int currentYear = LocalDate.now().getYear();

        for (Employee employee : employees) {
            List<EmployeePayroll> existingPayrolls = employeePayrollRepository
                    .findByEmployee_EmployeeIdAndMonthAndYear(employee.getEmployeeId(), currentMonth, currentYear);

            if (!existingPayrolls.isEmpty()) {
                throw new PayrollAlreadyExistsException("Payroll already exists for company "
                        + config.getCompany().getName() + " for " + currentMonth + "-" + currentYear);
            }

            EmployeeSalaryStructure salaryStructure = employeeSalaryStructureRepository
                    .findByEmployee_EmployeeIdAndCompany_CompanyId(employee.getEmployeeId(), companyId)
                    .orElseThrow(() -> new RuntimeException("Salary Structure missing for employee " + employee.getEmployeeId()));

            BigDecimal gross = salaryStructure.getGrossSalary();
            if (gross == null) {
                throw new RuntimeException("Gross salary is null for employee " + employee.getEmployeeId());
            }

            BigDecimal pf =  salaryStructure.getBasicSalary().multiply(config.getPfPercentage().divide(new BigDecimal("100")));
            BigDecimal hraAmount =  salaryStructure.getBasicSalary().multiply(config.getHraPercentage().divide(new BigDecimal("100")));
            BigDecimal daAmount =  salaryStructure.getBasicSalary().multiply(config.getDaPercentage().divide(new BigDecimal("100")));
            BigDecimal specialAllowanceAmount =  salaryStructure.getBasicSalary().multiply(salaryStructure.getSpecialAllowance());

            BigDecimal professionalTax = BigDecimal.valueOf(200);
            BigDecimal yearlyGross = gross.multiply(BigDecimal.valueOf(12));
            BigDecimal incomeTax = calculateIncomeTax(yearlyGross, config)
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            BigDecimal netSalary = gross.subtract(pf).subtract(professionalTax).subtract(incomeTax);

            EmployeePayroll payroll = new EmployeePayroll();
            payroll.setCompany(config.getCompany());
            payroll.setEmployee(employee);
            payroll.setMonth(currentMonth);
            payroll.setYear(currentYear);
            payroll.setHra(hraAmount);
            payroll.setDa(daAmount);
            payroll.setSpecialAllowance(specialAllowanceAmount);
            payroll.setGrossSalary(gross);
            payroll.setPfAmount(pf);
            payroll.setProfessionalTaxAmount(professionalTax);
            payroll.setIncomeTaxAmount(incomeTax);
            payroll.setLopDeduction(BigDecimal.ZERO);
            payroll.setNetSalary(netSalary);
            payroll.setGeneratedAt(LocalDateTime.now());

            employeePayrollRepository.save(payroll);
            byte[] pdfBytes = pdfGenerationService.generateSalarySlipPdf(payroll);

            // --- 3. Send Email ---
            String filename = String.format("SalarySlip_%s_%d_%d.pdf", employee.getEmpCode(), currentMonth, currentYear);
            String subject = String.format("%s Salary Slip for %d/%d", employee.getCompany().getName(), currentMonth, currentYear);
            String body = String.format("Dear %s,<br><br>Please find your salary slip attached for %d/%d. This is an automated email.<br><br>Regards,<br>%s Payroll Team",
                    employee.getName(), currentMonth, currentYear,employee.getCompany().getName());

            // Assuming employee.getEmail() is correct and defined
            emailService.sendSalarySlip(employee.getEmail(), subject, body, pdfBytes, filename);
        }
    }
}
