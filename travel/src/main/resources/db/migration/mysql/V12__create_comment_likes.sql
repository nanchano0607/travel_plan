CREATE TABLE comment_likes (
                               comment_id BIGINT NOT NULL,
                               user_id    BIGINT NOT NULL,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (comment_id, user_id),
                               CONSTRAINT fk_comment_likes_comment
                                   FOREIGN KEY (comment_id) REFERENCES comments (comment_id),
                               CONSTRAINT fk_comment_likes_user
                                   FOREIGN KEY (user_id) REFERENCES user_auth (user_id)
);