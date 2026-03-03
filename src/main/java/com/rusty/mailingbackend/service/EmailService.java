package com.rusty.mailingbackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendNewsletter(String toEmail, List<String> categories, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject("[뉴스레터] 오늘의 뉴스");
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    public String renderNewsletterTemplate(List<String> categories) {
        Context context = new Context();
        context.setVariable("categories", categories);
        context.setVariable("newsItems", getHardcodedNewsForCategories(categories));
        return templateEngine.process("newsletter", context);
    }

    private List<String> getHardcodedNewsForCategories(List<String> categories) {
        return categories.stream()
                .map(cat -> "[%s] 오늘의 %s 뉴스 요약 (하드코딩)".formatted(cat, cat))
                .toList();
    }
}
