CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE document_chunks ALTER COLUMN embedding TYPE vector(768) USING embedding::vector;

CREATE INDEX IF NOT EXISTS idx_chunk_embedding ON document_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
