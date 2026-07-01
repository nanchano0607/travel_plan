ALTER TABLE image ADD COLUMN post_id BIGINT;
ALTER TABLE image ADD CONSTRAINT fk_image_post
    FOREIGN KEY (post_id) REFERENCES posts(post_id);