-- 이메일 인증을 회원가입 이전 단계로 옮기면서, 아직 계정(user_auth)이 없는 이메일도
-- 인증을 받을 수 있도록 email_verification의 기준 키를 user_id에서 email로 변경한다.
DROP TABLE email_verification;

CREATE TABLE email_verification (
    email VARCHAR(255) PRIMARY KEY,
    code_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME,
    fail_count INT NOT NULL DEFAULT 0,
    last_sent_at DATETIME NOT NULL,
    resend_count INT NOT NULL DEFAULT 0,
    window_started_at DATETIME NOT NULL
);
