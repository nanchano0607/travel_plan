CREATE TABLE plan_item (
    plan_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    place_name VARCHAR(255),
    day_number INT NOT NULL,
    sequence INT,
    place_id VARCHAR(255),
    latitude FLOAT,
    longitude FLOAT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_item_plan
        FOREIGN KEY (plan_id) REFERENCES plan(plan_id)
);
