CREATE TABLE comment_image (
    image_id BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,
    PRIMARY KEY (comment_id, image_id),
    CONSTRAINT fk_comment_image_image
        FOREIGN KEY (image_id) REFERENCES image(image_id),
    CONSTRAINT fk_comment_image_comment
        FOREIGN KEY (comment_id) REFERENCES comments(comment_id)
);
