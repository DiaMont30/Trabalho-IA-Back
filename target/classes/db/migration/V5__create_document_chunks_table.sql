CREATE TABLE document_chunks (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    chunk_index INT NOT NULL,
    embedding TEXT,
    metadata JSON,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_chunk_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    UNIQUE(document_id, chunk_index)
);

CREATE INDEX idx_chunks_document_id ON document_chunks(document_id);
