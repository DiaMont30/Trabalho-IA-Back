CREATE TYPE pipeline_status AS ENUM ('QUEUED', 'PARSING', 'CHUNKING', 'EMBEDDING', 'READY', 'FAILED');

CREATE TABLE pipeline_jobs (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    status pipeline_status NOT NULL DEFAULT 'QUEUED',
    chunks_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP,
    CONSTRAINT fk_job_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

CREATE INDEX idx_jobs_document_id ON pipeline_jobs(document_id);
CREATE INDEX idx_jobs_status ON pipeline_jobs(status);
