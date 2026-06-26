CREATE TABLE sessions (
    id BINARY(16) NOT NULL PRIMARY KEY,
    title VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    closed_at DATETIME NULL
);
