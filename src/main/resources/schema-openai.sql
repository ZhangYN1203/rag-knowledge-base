CREATE TABLE IF NOT EXISTS vector_store (
    id VARCHAR(255) PRIMARY KEY,
    content TEXT,
    metadata TEXT,
    embedding TEXT
);