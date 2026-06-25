ALTER TABLE local_auth
    DROP FOREIGN KEY fk_local_auth_user;

ALTER TABLE social_auth
    DROP FOREIGN KEY fk_social_auth_user;

ALTER TABLE plan
    DROP FOREIGN KEY fk_plan_user;

ALTER TABLE posts
    DROP FOREIGN KEY fk_posts_user;

ALTER TABLE comments
    DROP FOREIGN KEY fk_comments_user;

ALTER TABLE post_likes
    DROP FOREIGN KEY fk_post_likes_user;

ALTER TABLE user_auth
    MODIFY COLUMN user_id BIGINT NOT NULL AUTO_INCREMENT;

ALTER TABLE local_auth
    MODIFY COLUMN user_id BIGINT NOT NULL;

ALTER TABLE social_auth
    MODIFY COLUMN user_id BIGINT NOT NULL;

ALTER TABLE plan
    MODIFY COLUMN user_id BIGINT NOT NULL;

ALTER TABLE posts
    MODIFY COLUMN user_id BIGINT NOT NULL;

ALTER TABLE comments
    MODIFY COLUMN user_id BIGINT NOT NULL;

ALTER TABLE post_likes
    MODIFY COLUMN user_id BIGINT NOT NULL;

ALTER TABLE plan_item
    MODIFY COLUMN latitude DECIMAL(10, 8),
    MODIFY COLUMN longitude DECIMAL(11, 8);

ALTER TABLE local_auth
    ADD CONSTRAINT fk_local_auth_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id);

ALTER TABLE social_auth
    ADD CONSTRAINT fk_social_auth_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id);

ALTER TABLE plan
    ADD CONSTRAINT fk_plan_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id);

ALTER TABLE posts
    ADD CONSTRAINT fk_posts_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id);

ALTER TABLE comments
    ADD CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id);

ALTER TABLE post_likes
    ADD CONSTRAINT fk_post_likes_user
        FOREIGN KEY (user_id) REFERENCES user_auth(user_id);
