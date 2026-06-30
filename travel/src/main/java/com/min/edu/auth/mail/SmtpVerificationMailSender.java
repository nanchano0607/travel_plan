package com.min.edu.auth.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Gmail SMTP(spring-boot-starter-mail)를 이용해 이메일 인증 코드를 발송하는 구현체
 *
 * @author 이유진
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpVerificationMailSender implements VerificationMailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendVerificationCode(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("[Travel] 이메일 인증 코드");
            helper.setText("인증 코드: " + code + "\n5분 이내에 입력해주세요.", false);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("이메일 인증 코드 발송 실패: {}", email, e);
            throw new IllegalStateException("이메일 발송에 실패했습니다.", e);
        }
    }
}
