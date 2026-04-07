package com.flash.film.module.notification.service;

import org.thymeleaf.context.Context;

public interface EmailService {
    void sendHtmlEmail(String toEmail, String subject, String templateName, Context context);
    void sendOtpRegisterEmail(String toEmail, String otpCode);
    void sendOtpForgotPasswordEmail(String toEmail, String otpCode);
}
