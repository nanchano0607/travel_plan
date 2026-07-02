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

    @Override
    public void sendPasswordResetLink(String email, String rawToken) {
        try {
            String resetLink = "http://127.0.0.1:5173/reset-password?token=" + rawToken;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("[Travel] 비밀번호 재설정 링크");
            String html = """
                    <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:32px;background:#f9f9f9;border-radius:8px;">
                      <h2 style="color:#333;margin-bottom:8px;">비밀번호 재설정</h2>
                      <p style="color:#555;margin-bottom:24px;">아래 버튼을 클릭해 비밀번호를 재설정하세요.</p>
                      <a href="%s"
                         style="display:inline-block;padding:12px 28px;background:#4F46E5;color:#fff;text-decoration:none;border-radius:6px;font-size:15px;font-weight:bold;">
                        비밀번호 재설정하기
                      </a>
                      <p style="color:#999;font-size:12px;margin-top:24px;">링크는 30분간 유효합니다. 본인이 요청하지 않았다면 이 메일을 무시하세요.</p>
                    </div>
                    """.formatted(resetLink);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("비밀번호 재설정 메일 발송 실패: {}", email, e);
            throw new IllegalStateException("이메일 발송에 실패했습니다.", e);
        }
    }
}
