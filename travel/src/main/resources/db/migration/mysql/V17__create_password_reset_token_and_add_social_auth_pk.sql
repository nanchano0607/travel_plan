-- 한 사용자가 여러 provider(Google, Naver, Kakao)를 연결할 수 있도록 복합 PK 추가
ALTER TABLE social_auth
    ADD PRIMARY KEY (user_id, provider);

-- 비밀번호 재설정 토큰 테이블
CREATE TABLE password_reset_token (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME    NOT NULL,
    used_at    DATETIME,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id)
);
