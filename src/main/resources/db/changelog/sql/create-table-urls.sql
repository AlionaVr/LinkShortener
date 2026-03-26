CREATE TABLE IF NOT EXISTS urls
(
    id           BIGSERIAL PRIMARY KEY,
    short_url    VARCHAR(16) NOT NULL UNIQUE,
    original_url TEXT        NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMP            DEFAULT NULL
);

CREATE INDEX idx_urls_short_url ON urls (short_url);
CREATE INDEX idx_urls_expires_at ON urls (expires_at);