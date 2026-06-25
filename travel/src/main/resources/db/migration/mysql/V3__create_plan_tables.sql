CREATE TABLE plan (
    plan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    region_name VARCHAR(100),
    region_id VARCHAR(255),
    budget INT,
    headcount INT,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    start_date DATE,
    end_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id)
);
