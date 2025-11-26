package com.karandev.paymaster.helper;

import com.karandev.paymaster.entity.EmployeePayroll;
import com.karandev.paymaster.repository.EmployeePayrollRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Month;
import java.util.Locale;

@Service
public class PdfGenerationService {

    private final EmployeePayrollRepository payrollRepository;

    // Modern Color Scheme
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color EARNINGS_COLOR = new Color(39, 174, 96);      // Green
    private static final Color DEDUCTIONS_COLOR = new Color(231, 76, 60);    // Red
    private static final Color NET_SALARY_COLOR = new Color(22, 160, 133);   // Teal
    private static final Color TOTAL_ROW_BG = new Color(236, 240, 241);      // Light Grey
    private static final Color LABEL_COLOR = new Color(44, 62, 80);          // Dark Grey

    private static final NumberFormat INR = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    // Fonts
    private static final Font COMPANY_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, PRIMARY_COLOR);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, SECONDARY_COLOR);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(127, 140, 141));
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, LABEL_COLOR);
    private static final Font VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
    private static final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, LABEL_COLOR);
    private static final Font NET_PAY_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);

    public PdfGenerationService(EmployeePayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    public byte[] generateSalarySlipPdf(EmployeePayroll p) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 60, 60);
        PdfWriter.getInstance(document, out);
        document.open();

        addCompanyHeader(document, p);
        addEmployeeInfo(document, p);
        addEarningsAndDeductionsTable(document, p);
        addNetPaySummary(document, p);
        addFooterNote(document);

        document.close();
        return out.toByteArray();
    }

    private void addCompanyHeader(Document doc, EmployeePayroll p) throws DocumentException {
        // Company Name with colored background
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell headerCell = new PdfPCell(new Phrase(p.getCompany().getName(), COMPANY_FONT));
        headerCell.setBackgroundColor(new Color(236, 240, 241));
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(15);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(headerCell);

        doc.add(headerTable);

        if (p.getCompany().getAddress() != null) {
            Paragraph address = new Paragraph(p.getCompany().getAddress(), SUBTITLE_FONT);
            address.setAlignment(Element.ALIGN_CENTER);
            address.setSpacingBefore(8);
            doc.add(address);
        }

        Paragraph title = new Paragraph(
                "Salary Slip - " + Month.of(p.getMonth()).name().substring(0, 1).toUpperCase()
                        + Month.of(p.getMonth()).name().substring(1).toLowerCase() + " " + p.getYear(),
                TITLE_FONT
        );
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(15);
        title.setSpacingAfter(20);
        doc.add(title);
    }

    private void addEmployeeInfo(Document doc, EmployeePayroll p) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{25, 25, 25, 25});
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        addInfoCell(table, "Employee Name", p.getEmployee().getName());
        addInfoCell(table, "Employee Code", p.getEmployee().getEmpCode());
        addInfoCell(table, "Designation", p.getEmployee().getDesignation());
        addInfoCell(table, "Department", nullSafe(p.getEmployee().getDepartment()));

        doc.add(table);
    }

    private void addEarningsAndDeductionsTable(Document doc, EmployeePayroll p) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 15, 35, 15});
        table.setSpacingBefore(10);

        // Header with modern colors
        table.addCell(createHeaderCell("EARNINGS", EARNINGS_COLOR));
        table.addCell(createHeaderCell("AMOUNT", EARNINGS_COLOR));
        table.addCell(createHeaderCell("DEDUCTIONS", DEDUCTIONS_COLOR));
        table.addCell(createHeaderCell("AMOUNT", DEDUCTIONS_COLOR));

        // Rows
        addRow(table, "Basic Salary", p.getBasicSalary(), "PF (Employee)", p.getPfEmployeeAmount());
        addRow(table, "HRA", p.getHra(), "ESI (Employee)", p.getEsiEmployeeAmount());
        addRow(table, "Conveyance Allowance", p.getConveyance(), "Professional Tax", p.getProfessionalTaxAmount());
        addRow(table, "Medical Allowance", p.getMedicalAllowance(), "Income Tax (TDS)", p.getIncomeTaxAmount());
        addRow(table, "Special Allowance", p.getSpecialAllowance(), "", null);
        addRow(table, "Bonus", p.getBonusAmount(), "", null);

        // Total Row with modern styling
        BigDecimal totalEarnings = p.getGrossSalary();
        BigDecimal totalDeductions = p.getPfEmployeeAmount()
                .add(nullSafe(p.getEsiEmployeeAmount()))
                .add(p.getProfessionalTaxAmount())
                .add(p.getIncomeTaxAmount());

        table.addCell(createTotalLabelCell("Gross Salary"));
        table.addCell(createTotalAmountCell(formatCurrency(totalEarnings)));
        table.addCell(createTotalLabelCell("Total Deductions"));
        table.addCell(createTotalAmountCell(formatCurrency(totalDeductions)));

        doc.add(table);
    }

    private void addRow(PdfPTable table, String earning, BigDecimal earnAmt,
                        String deduction, BigDecimal dedAmt) {
        table.addCell(createLabelCell(earning));
        table.addCell(createAmountCell(formatCurrency(earnAmt)));

        String dedLabel = deduction != null && !deduction.isEmpty() ? deduction : " ";
        BigDecimal amt = dedAmt != null ? dedAmt : BigDecimal.ZERO;
        table.addCell(createLabelCell(dedLabel));
        table.addCell(createAmountCell(deduction != null && dedAmt != null ? formatCurrency(amt) : " "));
    }

    private void addNetPaySummary(Document doc, EmployeePayroll p) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(65);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(25);
        table.setSpacingAfter(15);

        PdfPCell labelCell = new PdfPCell(new Phrase("NET SALARY PAYABLE", NET_PAY_FONT));
        labelCell.setBackgroundColor(NET_SALARY_COLOR);
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        labelCell.setPadding(14);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell amountCell = new PdfPCell(new Phrase(formatCurrency(p.getNetSalary()), NET_PAY_FONT));
        amountCell.setBackgroundColor(NET_SALARY_COLOR);
        amountCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        amountCell.setPadding(14);
        amountCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(amountCell);

        doc.add(table);

        Paragraph inWords = new Paragraph(
                "Amount in Words: " + toWords(p.getNetSalary()) + " Only",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, LABEL_COLOR)
        );
        inWords.setAlignment(Element.ALIGN_RIGHT);
        inWords.setSpacingBefore(12);
        doc.add(inWords);
    }

    private void addFooterNote(Document doc) throws DocumentException {
        Paragraph note = new Paragraph(
                "This is a computer-generated salary slip and does not require a signature.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, new Color(149, 165, 166))
        );
        note.setAlignment(Element.ALIGN_CENTER);
        note.setSpacingBefore(35);
        doc.add(note);
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        table.addCell(createInfoLabelCell(label + ":"));
        table.addCell(createInfoValueCell(nullSafe(value)));
    }

    private PdfPCell createInfoLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, LABEL_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(10);
        cell.setBackgroundColor(new Color(250, 250, 250));
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell createInfoValueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, VALUE_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(10);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell createHeaderCell(String text, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell createLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, VALUE_FONT));
        cell.setPadding(8);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(189, 195, 199));
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell createAmountCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, VALUE_FONT));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(189, 195, 199));
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell createTotalLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TOTAL_FONT));
        cell.setBackgroundColor(TOTAL_ROW_BG);
        cell.setPadding(10);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(189, 195, 199));
        cell.setBorderWidth(1f);
        return cell;
    }

    private PdfPCell createTotalAmountCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TOTAL_FONT));
        cell.setBackgroundColor(TOTAL_ROW_BG);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(189, 195, 199));
        cell.setBorderWidth(1f);
        return cell;
    }

    private String formatCurrency(BigDecimal amount) {
        return amount != null ? INR.format(amount) : "₹0.00";
    }

    private String nullSafe(String str) {
        return str != null && !str.isBlank() ? str : "N/A";
    }

    private BigDecimal nullSafe(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private String toWords(BigDecimal number) {
        if (number == null || number.compareTo(BigDecimal.ZERO) == 0) return "Zero Rupees";

        long rupees = number.longValue();
        int paise = number.subtract(new BigDecimal(rupees)).multiply(new BigDecimal(100)).intValue();

        StringBuilder words = new StringBuilder();
        words.append(numberToWords(rupees)).append(" Rupees");

        if (paise > 0) {
            words.append(" and ").append(numberToWords(paise)).append(" Paise");
        }

        return words.toString();
    }

    private String numberToWords(long number) {
        String[] ones = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
                "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

        if (number < 20) return ones[(int) number];
        if (number < 100) return tens[(int) (number / 10)] + (number % 10 != 0 ? " " + ones[(int) (number % 10)] : "");
        if (number < 1000) return ones[(int) (number / 100)] + " Hundred" + (number % 100 != 0 ? " " + numberToWords(number % 100) : "");
        if (number < 100000) return numberToWords(number / 1000) + " Thousand" + (number % 1000 != 0 ? " " + numberToWords(number % 1000) : "");
        if (number < 10000000) return numberToWords(number / 100000) + " Lakh" + (number % 100000 != 0 ? " " + numberToWords(number % 100000) : "");

        return numberToWords(number / 10000000) + " Crore" + (number % 10000000 != 0 ? " " + numberToWords(number % 10000000) : "");
    }
}