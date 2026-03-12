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
    private static final NumberFormat INR = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MMM/yyyy");

    // Fonts
    private static final Font COMPANY_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
    private static final Font ADDRESS_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

    public PdfGenerationService(EmployeePayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    public byte[] generateSalarySlipPdf(EmployeePayroll p) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
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
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(new Phrase("FIRST QUAD TECH SOLUTIONS", COMPANY_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);

        String address = "Off. No. 12, Second Floor, Survey No. 74, Suyash Shroff Commercial Mall, Above Union Bank, Baner, Pune, Maharashtra - 411045.";
        cell = new PdfPCell(new Phrase(address, ADDRESS_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Payslip for the month of " + Month.of(p.getMonth()).name() + " " + p.getYear(), TITLE_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPaddingTop(5);
        cell.setPaddingBottom(5);
//        cell.set(10);
        table.addCell(cell);

        doc.add(table);
    }

    private void addEmployeeDetails(Document doc, EmployeePayroll p) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{20, 30, 20, 30});
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        // Row 1
        addDetail(table, "Employee Name", p.getEmployee().getName());
        addDetail(table, "PAN", "N/A"); // Placeholders since these aren't in Entity yet
        
        // Row 2
        addDetail(table, "Employee Code", p.getEmployee().getEmpCode());
        addDetail(table, "Bank Name", "N/A");
        
        // Row 3
        addDetail(table, "Designation", p.getEmployee().getDesignation());
        addDetail(table, "Account No.", "N/A");
        
        // Row 4
        addDetail(table, "Department", nullSafe(p.getEmployee().getDepartment()));
        addDetail(table, "PF No.", "N/A");
        
        // Row 5
        addDetail(table, "Location", "Pune");
        addDetail(table, "UAN No.", "N/A");
        
        // Row 6
        addDetail(table, "Date of Joining", p.getEmployee().getJoiningDate() != null ? p.getEmployee().getJoiningDate().format(DATE_FORMAT) : "N/A");
        addDetail(table, "ESIC No.", "N/A");
        
        // Row 7
        addDetail(table, "Days Paid", "31");
        addDetail(table, "", "");

        doc.add(table);
    }

    private void addSalaryTable(Document doc, EmployeePayroll p) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 15, 35, 15});
        table.setSpacingBefore(5);

        // Header
        table.addCell(createTableHeaderCell("Earnings"));
        table.addCell(createTableHeaderCell("Head(Rs.)"));
        table.addCell(createTableHeaderCell("Deductions"));
        table.addCell(createTableHeaderCell("Head(Rs.)"));

        // Content Rows
        addSalaryRow(table, "Basic", p.getBasicSalary(), "Provident fund", p.getPfEmployeeAmount());
        addSalaryRow(table, "House Rent Allowance", p.getHra(), "ESIC", nullSafe(p.getTotalEsicDeduction()));
        addSalaryRow(table, "Conveyance", p.getConveyance(), "Professional Tax", p.getProfessionalTaxAmount());
        addSalaryRow(table, "Other Allowance", p.getSpecialAllowance(), "Income Tax", p.getIncomeTaxAmount());
        addSalaryRow(table, "Bonus", p.getBonusAmount(), "", null);
        addSalaryRow(table, "Medical", p.getMedicalAllowance(), "", null);

        // Totals
        BigDecimal totalEarnings = p.getGrossSalary();
        BigDecimal totalDeductions = p.getPfEmployeeAmount()
                .add(nullSafe(p.getTotalEsicDeduction()))
                .add(p.getProfessionalTaxAmount())
                .add(p.getIncomeTaxAmount());

        table.addCell(createLabelCell("Total Earnings", true));
        table.addCell(createValueCell(totalEarnings.toPlainString(), Element.ALIGN_RIGHT, true));
        table.addCell(createLabelCell("Total Deduction", true));
        table.addCell(createValueCell(totalDeductions.toPlainString(), Element.ALIGN_RIGHT, true));

        doc.add(table);
    }

    private void addNetSalarySection(Document doc, EmployeePayroll p) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        String netPayStr = "Net Payable: Rs. " + p.getNetSalary().toPlainString() + "/- (" + INR.format(p.getNetSalary()) + " Only)";
        String inWords = " (Rs. " + toWords(p.getNetSalary()) + " Only)";
        
        PdfPCell cell = new PdfPCell(new Phrase(netPayStr + inWords, TITLE_FONT));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(10);
        table.addCell(cell);

        doc.add(table);
    }

    private void addFooter(Document doc, EmployeePayroll p) throws DocumentException {
        Paragraph footer = new Paragraph("\n" + p.getCompany().getName().toUpperCase(), COMPANY_FONT);
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(30);
        doc.add(footer);

        Paragraph computerGenerated = new Paragraph("\n\nThis is a computer generated payslip.", ADDRESS_FONT);
        computerGenerated.setAlignment(Element.ALIGN_CENTER);
        doc.add(computerGenerated);
    }

    private void addDetail(PdfPTable table, String label, String value) {
        table.addCell(createLabelCell(label, false));
        table.addCell(createValueCell(value, Element.ALIGN_LEFT, false));
    }

    private PdfPCell createLabelCell(String text, boolean border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, LABEL_FONT));
        if (!border) cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private PdfPCell createValueCell(String text, int align, boolean border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, VALUE_FONT));
        cell.setHorizontalAlignment(align);
        if (!border) cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private PdfPCell createTableHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        return cell;
    }

    private void addSalaryRow(PdfPTable table, String earnLabel, BigDecimal earnAmt, String dedLabel, BigDecimal dedAmt) {
        table.addCell(createValueCell(earnLabel, Element.ALIGN_LEFT, true));
        table.addCell(createValueCell(earnAmt != null ? earnAmt.toPlainString() : "0.00", Element.ALIGN_RIGHT, true));
        table.addCell(createValueCell(dedLabel, Element.ALIGN_LEFT, true));
        table.addCell(createValueCell(dedAmt != null ? dedAmt.toPlainString() : " ", Element.ALIGN_RIGHT, true));
    }

    private String nullSafe(String str) {
        return str != null && !str.isBlank() ? str : "N/A";
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