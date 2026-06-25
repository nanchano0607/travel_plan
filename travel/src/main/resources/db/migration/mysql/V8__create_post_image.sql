CREATE TABLE post_image (
    image_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, image_id),
    CONSTRAINT fk_post_image_image
        FOREIGN KEY (image_id) REFERENCES image(image_id),
    CONSTRAINT fk_post_image_post
        FOREIGN KEY (post_id) REFERENCES posts(post_id)
);
