package com.flash.film.module.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendHtmlEmail(String toEmail, String subject, String templateName, Context context) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            String htmlContent = templateEngine.process("email/" + templateName, context);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Email sent successfully to: {} with subject: '{}'", toEmail, subject);

        } catch (MessagingException e) {
            log.error("Error sending email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendOtpRegisterEmail(String toEmail, String otpCode) {
        Context context = new Context();
        context.setVariable("email", toEmail);
        context.setVariable("otpCode", otpCode);

        sendHtmlEmail(
                toEmail,
                "[Flash DTF] Account Registration Verification",
                "otp-register",
                context
        );
    }
}
