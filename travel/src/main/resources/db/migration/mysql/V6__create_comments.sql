CREATE TABLE comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id) REFERENCES posts(post_id),
    CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id),
    CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id)
);
