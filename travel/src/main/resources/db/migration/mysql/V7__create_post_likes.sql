CREATE TABLE post_likes (
    post_id BIGINT NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_post_likes_post
        FOREIGN KEY (post_id) REFERENCES posts(post_id),
    CONSTRAINT fk_post_likes_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id)
);
