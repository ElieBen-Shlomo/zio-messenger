CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_user_id BIGINT REFERENCES users(id),
    receiver_user_id BIGINT REFERENCES users(id),
    timestamp timestamp NOT NULL,
    message varchar(255)
);

