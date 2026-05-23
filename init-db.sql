-- Enable PGVector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Spring AI PgVectorStore table for document embeddings
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding vector(768)
);

-- Create index for faster similarity search
CREATE INDEX IF NOT EXISTS idx_vector_store_embedding
ON vector_store USING ivfflat (embedding vector_cosine_ops);

-- Create index for metadata lookup
CREATE INDEX IF NOT EXISTS idx_vector_store_metadata
ON vector_store USING gin (metadata jsonb_path_ops);