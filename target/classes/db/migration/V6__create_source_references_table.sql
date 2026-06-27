CREATE TABLE source_references (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    chunk_id BIGINT NOT NULL,
    relevance_score DOUBLE PRECISION NOT NULL,
    excerpt VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_source_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_source_chunk FOREIGN KEY (chunk_id) REFERENCES document_chunks(id) ON DELETE CASCADE
);

CREATE INDEX idx_sources_message_id ON source_references(message_id);
