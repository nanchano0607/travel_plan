CREATE TABLE ai_request_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_type VARCHAR(50) NOT NULL,
    usage_date DATE NOT NULL,
    request_count INT NOT NULL DEFAULT 0,
    last_requested_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_ai_request_usage_user_type_date
        UNIQUE (user_id, request_type, usage_date),
    CONSTRAINT fk_ai_request_usage_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id)
);
