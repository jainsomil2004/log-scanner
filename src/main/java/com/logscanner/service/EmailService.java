package com.logscanner.service;

import com.logscanner.model.ErrorReport;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.to}")
    private String mailTo;

    @Value("${app.mail.from}")
    private String mailFrom;

    public void sendErrorAlert(ErrorReport report) {
        if (report.getTotalErrors() == 0) {
            log.info("No errors found — skipping email.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(mailTo);
            helper.setSubject("🚨 Log Error Alert — " + report.getTotalErrors()
                    + " error(s) detected in " + report.getLogFilePath());

            // Build HTML body via Thymeleaf
            Context ctx = new Context();
            ctx.setVariable("report", report);
            String htmlBody = templateEngine.process("error-email", ctx);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Alert email sent to {}", mailTo);

        } catch (Exception e) {
            log.error("Failed to send alert email", e);
        }
    }
}
