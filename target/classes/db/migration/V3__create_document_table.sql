CREATE TABLE documents (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    session_id BIGINT NULL,
    original_name VARCHAR(255) NOT NULL,
    storage_file_name VARCHAR(255) NOT NULL UNIQUE,
    storage_path VARCHAR(500) NOT NULL,
    type VARCHAR(10) NOT NULL,
    size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_document_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE SET NULL
);

CREATE INDEX idx_document_session_id ON documents(session_id);
CREATE UNIQUE INDEX uk_document_storage_file_name ON documents(storage_file_name);
