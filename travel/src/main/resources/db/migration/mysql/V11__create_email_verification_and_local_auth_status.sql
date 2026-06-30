ALTER TABLE local_auth
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

CREATE TABLE email_verification (
    user_id BIGINT PRIMARY KEY,
    code_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME,
    fail_count INT NOT NULL DEFAULT 0,
    last_sent_at DATETIME NOT NULL,
    resend_count INT NOT NULL DEFAULT 0,
    window_started_at DATETIME NOT NULL,
    CONSTRAINT fk_email_verification_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id)
);
