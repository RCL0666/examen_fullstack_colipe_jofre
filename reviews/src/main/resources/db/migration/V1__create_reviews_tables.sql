CREATE TABLE reviews (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    destination_id BINARY(16) NOT NULL,
    rating INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    comment TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5)
);

CREATE TABLE review_replies (
    id BINARY(16) NOT NULL,
    review_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    reply_text VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_review FOREIGN KEY (review_id) REFERENCES reviews(id)
);
