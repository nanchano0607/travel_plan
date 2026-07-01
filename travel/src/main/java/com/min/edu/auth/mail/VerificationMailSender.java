package com.min.edu.auth.mail;

/**
 * 이메일 인증 코드 발송 채널을 추상화하는 인터페이스.
 * 발송 수단이 바뀌어도 {@link com.min.edu.auth.service.EmailVerificationService}는 영향받지 않도록 분리함
 *
 * @author 이유진
 * @version 1.0
 */
public interface VerificationMailSender {
    void sendVerificationCode(String email, String code);
    void sendPasswordResetLink(String email, String rawToken);
}
