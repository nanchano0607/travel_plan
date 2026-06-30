package com.min.edu.auth.repository;

import com.min.edu.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이메일 인증 코드 발급/검증 상태(email_verification)에 대한 데이터 접근을 담당하는 리포지토리.
 * 회원가입 전 단계라 계정이 없으므로 이메일(String)을 PK로 사용한다.
 *
 * @author 이유진
 * @version 1.0
 */
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
}
