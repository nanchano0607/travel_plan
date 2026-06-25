CREATE TABLE posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    destination VARCHAR(255),
    like_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    plan_id BIGINT,
    CONSTRAINT fk_posts_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id),
    CONSTRAINT fk_posts_plan
        FOREIGN KEY (plan_id) REFERENCES plan(plan_id)
);
