package com.karandev.paymaster.helper;

import com.karandev.paymaster.entity.EmployeePayroll;
import com.karandev.paymaster.repository.EmployeePayrollRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfGenerationService {

    private final EmployeePayrollRepository payrollRepository;

    private static final Color BORDER_COLOR = Color.BLACK;
    private static final Color HEADER_BG_COLOR = new Color(240, 240, 240);
    private static final NumberFormat INR = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MMM/yyyy");

    // Fonts
    private static final Font COMPANY_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
    private static final Font COMPANY_FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font ADDRESS_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
    private static final Font VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font TABLE_BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font SMALL_ITALIC_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

    public PdfGenerationService(EmployeePayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    public byte[] generateSalarySlipPdf(EmployeePayroll p) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        
        // Add Outer Border
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                cb.setColorStroke(BORDER_COLOR);
                cb.setLineWidth(1f);
                cb.rectangle(20, 20, document.getPageSize().getWidth() - 40, document.getPageSize().getHeight() - 40);
                cb.stroke();
            }
        });

        document.open();

        addHeader(document, p);
        addEmployeeDetails(document, p);
        addSalaryTable(document, p);
        addNetSalarySection(document, p);
        addFooter(document, p);

        document.close();
        return out.toByteArray();
    }

    private void addHeader(Document doc, EmployeePayroll p) throws DocumentException {
        // 1. Main Header Table (Name/Address on Left, Logo on Right)
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{75, 25});

        // Left Cell: Company Name + Address
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingLeft(10);
        leftCell.setPaddingTop(5);
        leftCell.setVerticalAlignment(Element.ALIGN_TOP);

        // Company Name Paragraph
        Paragraph namePara = new Paragraph(p.getCompany().getName().toUpperCase(), COMPANY_FONT);
        namePara.setSpacingAfter(2);
        leftCell.addElement(namePara);

        // Address Paragraph (With Fallback if missing or too short, like just "Pune")
        String dbAddress = p.getCompany().getAddress();
        String addressText;
        if (dbAddress != null && dbAddress.trim().length() > 10) {
            // Try to add some breaks to DB address if it's very long
            addressText = dbAddress.replace(", ", ",\n");
        } else {
            addressText = "Off. No. 12, Second Floor,\n" +
                          "Survey No. 74, Suyash Shroff Commercial Mall,\n" +
                          "Above Union Bank, Baner, Pune,\n" +
                          "Maharashtra - 411045.";
        }

        Paragraph addrPara = new Paragraph(addressText, ADDRESS_FONT);
        addrPara.setLeading(10f); // Tighter leading for multiple rows
        leftCell.addElement(addrPara);

        headerTable.addCell(leftCell);

        // Right Cell: Logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPaddingRight(10);

        try {
            Image logo = Image.getInstance("img.png");
            logo.scaleToFit(100, 60);
            logo.setAlignment(Image.RIGHT);
            logoCell.addElement(logo);
        } catch (Exception e) {
            logoCell.addElement(new Phrase(" "));
        }
        headerTable.addCell(logoCell);

        doc.add(headerTable);

        // 2. Payslip Month Row
        PdfPTable monthTable = new PdfPTable(1);
        monthTable.setWidthPercentage(100);
        monthTable.setSpacingBefore(8);

        String monthName = Month.of(p.getMonth()).name();
        PdfPCell monthCell = new PdfPCell(new Phrase("PAYSLIP FOR THE MONTH OF " + monthName + " " + p.getYear(), TITLE_FONT));
        monthCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        monthCell.setBackgroundColor(HEADER_BG_COLOR);
        monthCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        monthCell.setBorderColor(BORDER_COLOR);
        monthCell.setPaddingTop(6);
        monthCell.setPaddingBottom(6);

        monthTable.addCell(monthCell);
        doc.add(monthTable);
    }

    private void addEmployeeDetails(Document doc, EmployeePayroll p) throws DocumentException {
        // We use 4 columns: Label1 | Value1 | Label2 | Value2
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        // Adjust widths: Labels (25%) and Values (25%)
        table.setWidths(new float[]{25, 25, 25, 25});
        table.setSpacingBefore(0);
        table.setSpacingAfter(0);

        // Row 1
        addCell(table, "Employee Name", LABEL_FONT);
        addCell(table, p.getEmployee().getName(), VALUE_FONT);
        addCell(table, "PAN", LABEL_FONT);
        addCell(table, nullSafe(p.getEmployee().getPan(), "N/A"), VALUE_FONT);

        // Row 2
        addCell(table, "Employee Code", LABEL_FONT);
        addCell(table, p.getEmployee().getEmpCode(), VALUE_FONT);
        addCell(table, "Bank Name", LABEL_FONT);
        addCell(table, nullSafe(p.getEmployee().getBankName(), "N/A"), VALUE_FONT);

        // Row 3
        addCell(table, "Designation", LABEL_FONT);
        addCell(table, p.getEmployee().getDesignation(), VALUE_FONT);
        addCell(table, "Account No.", LABEL_FONT);
        addCell(table, nullSafe(p.getEmployee().getAccountNumber(), "N/A"), VALUE_FONT);

        // Row 4
        addCell(table, "Department", LABEL_FONT);
        addCell(table, nullSafe(p.getEmployee().getDepartment()), VALUE_FONT);
        addCell(table, "UAN No.", LABEL_FONT);
        addCell(table, nullSafe(p.getEmployee().getUanNumber(), "N/A"), VALUE_FONT);

        // Row 5
        addCell(table, "Location", LABEL_FONT);
        addCell(table, nullSafe(p.getEmployee().getLocation(), "N/A"), VALUE_FONT);
        addCell(table, "ESIC No.", LABEL_FONT);
        addCell(table, nullSafe(p.getEmployee().getEsicNumber(), "N/A"), VALUE_FONT);

        // Row 6
        addCell(table, "Date of Joining", LABEL_FONT);
        addCell(table, p.getEmployee().getJoiningDate() != null ? p.getEmployee().getJoiningDate().format(DATE_FORMAT) : "N/A", VALUE_FONT);
        addCell(table, "Days Paid", LABEL_FONT);
        addCell(table, String.valueOf(p.getDaysPaid() != null ? p.getDaysPaid() : 0), VALUE_FONT);

        doc.add(table);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorderColor(BORDER_COLOR);

        table.addCell(cell);
    }

    private void addSalaryTable(Document doc, EmployeePayroll p) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 15, 35, 15});

        // Header
        table.addCell(createTableHeaderCell("Earnings", Element.ALIGN_CENTER, true));
        table.addCell(createTableHeaderCell("(Rs.)", Element.ALIGN_RIGHT, true));
        table.addCell(createTableHeaderCell("Deductions", Element.ALIGN_CENTER, true));
        table.addCell(createTableHeaderCell("(Rs.)", Element.ALIGN_RIGHT, true));

        // Content Rows
        // Sub-Header
        table.addCell(createSubHeaderCell("Head"));
        table.addCell(createSubHeaderCell("(Rs.)"));
        table.addCell(createSubHeaderCell("Head"));
        table.addCell(createSubHeaderCell("(Rs.)"));

        addSalaryRow(table, "Basic", p.getBasicSalary(), "Provident fund", p.getPfEmployeeAmount());
        addSalaryRow(table, "House Rent Allowance", p.getHra(), "ESIC", nullSafe(p.getTotalEsicDeduction()));
        addSalaryRow(table, "Conveyance", p.getConveyance(), "Professional Tax", p.getProfessionalTaxAmount());
        addSalaryRow(table, "Other Allowance", p.getSpecialAllowance(), "", null);
        
        // Empty rows to match height if needed, but the image shows a few rows.
        // Totals
        BigDecimal totalEarnings = p.getGrossSalary();
        BigDecimal totalDeductions = p.getPfEmployeeAmount()
                .add(nullSafe(p.getTotalEsicDeduction()))
                .add(p.getProfessionalTaxAmount())
                .add(p.getIncomeTaxAmount());

        table.addCell(createTotalLabelCell("Total Earnings"));
        table.addCell(createTotalValueCell(totalEarnings.toPlainString()));
        table.addCell(createTotalLabelCell("Total Deduction"));
        table.addCell(createTotalValueCell(totalDeductions.toPlainString()));

        doc.add(table);
    }

    private void addNetSalarySection(Document doc, EmployeePayroll p) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        String netPayStr = "Net Payable: Rs. " + p.getNetSalary().toPlainString() + "/-";
        String inWords = " (Rs. " + toWords(p.getNetSalary()) + " Only)";
        
        PdfPCell cell = new PdfPCell(new Phrase(netPayStr + inWords, TITLE_FONT));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(10);
        table.addCell(cell);
        
        PdfPCell systemGeneratedCell = new PdfPCell(new Phrase("This is a System generated document, No signature is required. This document contains confidential information.", SMALL_ITALIC_FONT));
        systemGeneratedCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        systemGeneratedCell.setBorder(Rectangle.NO_BORDER);
        systemGeneratedCell.setPaddingTop(10);
        table.addCell(systemGeneratedCell);

        doc.add(table);
    }

    private void addFooter(Document doc, EmployeePayroll p) throws DocumentException {
        Paragraph footer = new Paragraph("\n" + p.getCompany().getName().toUpperCase(), COMPANY_FONT_FOOTER);
        footer.setAlignment(Element.ALIGN_LEFT);
        footer.setSpacingBefore(30);
        footer.setIndentationLeft(10);
        doc.add(footer);
    }

    private void addDetail(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingLeft(10);
        labelCell.setPaddingTop(2);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingLeft(10);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }

    private PdfPCell createTableHeaderCell(String text, int align, boolean withBg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setHorizontalAlignment(align);
        if (withBg) cell.setBackgroundColor(HEADER_BG_COLOR);
        cell.setPadding(5);
        cell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM);
        return cell;
    }

    private PdfPCell createSubHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TITLE_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(3);
        cell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM);
        return cell;
    }

    private PdfPCell createTotalLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(HEADER_BG_COLOR);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createTotalValueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(HEADER_BG_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(5);
        return cell;
    }

    private void addSalaryRow(PdfPTable table, String earnLabel, BigDecimal earnAmt, String dedLabel, BigDecimal dedAmt) {
        table.addCell(createBodyCell(earnLabel, Element.ALIGN_LEFT));
        table.addCell(createBodyCell(earnAmt != null ? earnAmt.toPlainString() : "0.00", Element.ALIGN_RIGHT));
        table.addCell(createBodyCell(dedLabel, Element.ALIGN_LEFT));
        table.addCell(createBodyCell(dedAmt != null ? dedAmt.toPlainString() : " ", Element.ALIGN_RIGHT));
    }

    private PdfPCell createBodyCell(String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_BODY_FONT));
        cell.setHorizontalAlignment(align);
        cell.setPadding(3);
        cell.setMinimumHeight(15f);
        return cell;
    }

    private String nullSafe(String str) {
        return str != null && !str.isBlank() ? str : "N/A";
    }

    private String nullSafe(String str, String defaultValue) {
        return str != null && !str.isBlank() ? str : defaultValue;
    }

    private BigDecimal nullSafe(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private String toWords(BigDecimal number) {
        if (number == null || number.compareTo(BigDecimal.ZERO) == 0) return "Zero";
        long rupees = number.longValue();
        int paise = number.subtract(new BigDecimal(rupees)).multiply(new BigDecimal(100)).intValue();
        StringBuilder words = new StringBuilder();
        words.append(numberToWords(rupees)).append(" Rupees");
        if (paise > 0) words.append(" and ").append(numberToWords(paise)).append(" Paise");
        return words.toString();
    }

    private String numberToWords(long number) {
        String[] ones = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        if (number == 0) return "";
        if (number < 20) return ones[(int) number];

        if (number < 100) return tens[(int) (number / 10)] + (number % 10 != 0 ? " " + ones[(int) (number % 10)] : "");
        if (number < 1000) return ones[(int) (number / 100)] + " Hundred" + (number % 100 != 0 ? " " + numberToWords(number % 100) : "");
        if (number < 100000) return numberToWords(number / 1000) + " Thousand" + (number % 1000 != 0 ? " " + numberToWords(number % 1000) : "");
        if (number < 10000000) return numberToWords(number / 100000) + " Lakh" + (number % 100000 != 0 ? " " + numberToWords(number % 100000) : "");
        return numberToWords(number / 10000000) + " Crore" + (number % 10000000 != 0 ? " " + numberToWords(number % 10000000) : "");
    }
}