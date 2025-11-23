package com.karandev.paymaster.helper;

import com.karandev.paymaster.entity.Employee;
import com.karandev.paymaster.repository.EmployeeRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmployeeRepository employeeRepository;
    public EmailService(JavaMailSender mailSender, EmployeeRepository employeeRepository) {
        this.mailSender = mailSender;
        this.employeeRepository = employeeRepository;
    }

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendSalarySlip(
            String toEmail,
            String subject,
            String body,
            byte[] pdfBytes,
            String pdfFilename) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            DataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
            helper.addAttachment(Objects.requireNonNull(pdfFilename), dataSource);

            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't re-throw a RuntimeException if email failure shouldn't stop the payroll
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }

    @Transactional
    public void sendSetPasswordEmail(UUID employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String token = UUID.randomUUID().toString();
        employee.setPasswordToken(token);
        employee.setTokenExpiry(LocalDateTime.now().plusHours(24));

        employeeRepository.save(employee);

        String setPasswordUrl = frontendUrl + "/set-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(employee.getEmail());
        message.setSubject("Set Your Password");
        message.setText("Welcome! Please set your password using this link: " + setPasswordUrl);

        mailSender.send(message);
    }


}