package com.karandev.paymaster.helper;
import com.karandev.paymaster.repository.EmployeePayrollRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.karandev.paymaster.entity.EmployeePayroll;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class PdfGenerationService {


    private final EmployeePayrollRepository payrollRepository;

    public PdfGenerationService(EmployeePayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    public EmployeePayroll getPayrollData(UUID employeeId, Integer month, Integer year) {
        return payrollRepository.findByEmployee_EmployeeIdAndMonthAndYear(employeeId, month, year)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Payroll record not found for Employee ID: %s, Month: %d, Year: %d",
                                employeeId, month, year)
                ));
    }

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(0, 0, 0));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(0, 0, 0));
    private static final Font DATA_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(0, 0, 0));
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));


    public byte[] generateSalarySlipPdf(EmployeePayroll payroll) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        addCompanyHeader(document, payroll);

        addEmployeeAndMonthDetails(document, payroll);

        addEarningsAndDeductionsTable(document, payroll);

        addNetSalarySummary(document, payroll);

        document.close();
        return baos.toByteArray();
    }

    private void addCompanyHeader(Document document, EmployeePayroll payroll) throws DocumentException {
        // Title: Company Name
        Paragraph companyName = new Paragraph(payroll.getCompany().getName(), TITLE_FONT);
        companyName.setAlignment(Element.ALIGN_CENTER);
        document.add(companyName);

        // Company Address
        Paragraph address = new Paragraph(payroll.getCompany().getAddress(), DATA_FONT);
        address.setAlignment(Element.ALIGN_CENTER);
        document.add(address);

        // Subtitle: Salary Slip
        Paragraph subtitle = new Paragraph("SALARY SLIP", HEADER_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);
    }

    private void addEmployeeAndMonthDetails(Document document, EmployeePayroll payroll) throws DocumentException {
        PdfPTable table = new PdfPTable(2); // Two columns for key-value pairs
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        float[] columnWidths = {1f, 2f};
        table.setWidths(columnWidths);

        // Helper method to add a row (Key, Value)
        addRow(table, "Employee Name:", payroll.getEmployee().getName());
        addRow(table, "Employee Code:", payroll.getEmployee().getEmpCode());
        addRow(table, "Designation:", payroll.getEmployee().getDesignation());
        addRow(table, "Department:", payroll.getEmployee().getDepartment());
        addRow(table, "Month/Year:", payroll.getMonth() + " / " + payroll.getYear());

        document.add(table);
    }

    // Helper method to create and add cells to a table
    private void addRow(PdfPTable table, String key, String value) {
        PdfPCell keyCell = new PdfPCell(new Phrase(key, HEADER_FONT));
        keyCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(keyCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, DATA_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    private void addEarningsAndDeductionsTable(Document document, EmployeePayroll payroll) throws DocumentException {
        PdfPTable table = new PdfPTable(4); // Earnings (2) and Deductions (2) columns
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        float[] columnWidths = {2f, 1.5f, 2f, 1.5f};
        table.setWidths(columnWidths);

        // Table Headers
        addHeaderCell(table, "Earnings");
        addHeaderCell(table, "Amount");
        addHeaderCell(table, "Deductions");
        addHeaderCell(table, "Amount");

        // Data Rows
        addDataRow(table, "Basic Pay", formatCurrency(payroll.getEmployee().getSalaryStructure().getBasicSalary()),
                "PF Amount", formatCurrency(payroll.getPfAmount()));
        addDataRow(table, "HRA", formatCurrency(payroll.getHra()),
                "Professional Tax", formatCurrency(payroll.getProfessionalTaxAmount()));
        addDataRow(table, "DA", formatCurrency(payroll.getDa()),
                "Income Tax", formatCurrency(payroll.getIncomeTaxAmount()));
        addDataRow(table, "Special Allowance", formatCurrency(payroll.getSpecialAllowance()),
                "LOP Deduction", formatCurrency(payroll.getLopDeduction()));

        // Calculate Totals
        BigDecimal totalEarnings = payroll.getGrossSalary();
        BigDecimal totalDeductions = payroll.getPfAmount().add(payroll.getProfessionalTaxAmount()).add(payroll.getIncomeTaxAmount()).add(payroll.getLopDeduction());

        // Total Row
        addTotalRow(table, "Total Earnings", formatCurrency(totalEarnings),
                "Total Deductions", formatCurrency(totalDeductions));

        document.add(table);
    }

    // Helper methods for the main table
    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(200, 200, 200));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addDataRow(PdfPTable table, String eName, String eAmount, String dName, String dAmount) {
        table.addCell(new Phrase(eName, DATA_FONT));
        table.addCell(createAmountCell(eAmount));
        table.addCell(new Phrase(dName, DATA_FONT));
        table.addCell(createAmountCell(dAmount));
    }

    private void addTotalRow(PdfPTable table, String eName, String eAmount, String dName, String dAmount) {
        table.addCell(createBoldCell(eName));
        table.addCell(createBoldAmountCell(eAmount));
        table.addCell(createBoldCell(dName));
        table.addCell(createBoldAmountCell(dAmount));
    }

    private PdfPCell createAmountCell(String amount) {
        PdfPCell cell = new PdfPCell(new Phrase(amount, DATA_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private PdfPCell createBoldCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        return cell;
    }

    private PdfPCell createBoldAmountCell(String amount) {
        PdfPCell cell = new PdfPCell(new Phrase(amount, HEADER_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private void addNetSalarySummary(Document document, EmployeePayroll payroll) throws DocumentException {
        document.add(new Paragraph("\n"));

        // Net Salary Box
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50); // Make the net salary box smaller
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        float[] columnWidths = {2f, 1.5f};
        table.setWidths(columnWidths);

        PdfPCell netLabel = new PdfPCell(new Phrase("NET SALARY:", TITLE_FONT));
        netLabel.setBackgroundColor(new Color(220, 220, 220));
        netLabel.setPadding(8);

        PdfPCell netValue = new PdfPCell(new Phrase(formatCurrency(payroll.getNetSalary()), TITLE_FONT));
        netValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        netValue.setBackgroundColor(new Color(220, 220, 220));
        netValue.setPadding(8);

        table.addCell(netLabel);
        table.addCell(netValue);
        document.add(table);

        document.add(new Paragraph("\n"));

        // Footer/Disclaimer
        Paragraph footer = new Paragraph("Generated on: " + payroll.getGeneratedAt().toLocalDate()
                + ". This is a computer-generated statement.", DATA_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return CURRENCY_FORMAT.format(0);
        return CURRENCY_FORMAT.format(amount);
    }
}