CREATE TABLE messages (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    metadata JSON NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_message_session_id ON messages(session_id);
CREATE INDEX idx_message_created_at ON messages(created_at);
