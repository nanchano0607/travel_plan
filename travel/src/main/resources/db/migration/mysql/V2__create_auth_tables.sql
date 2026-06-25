CREATE TABLE local_auth (
    user_id BIGINT PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT fk_local_auth_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id)
);

CREATE TABLE social_auth (
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    linked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_social_auth_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id),
    CONSTRAINT uk_social_auth_provider_user
        UNIQUE (provider, provider_user_id)
);
