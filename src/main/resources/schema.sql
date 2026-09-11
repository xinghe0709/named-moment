CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    password_hash VARCHAR(300) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_app_user_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS emotion_concept (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    language VARCHAR(50) NOT NULL,
    meaning TEXT NOT NULL,
    description TEXT NOT NULL,
    source_url VARCHAR(500) NOT NULL,
    embedding VECTOR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_emotion_concept_name_language UNIQUE (name, language)
);

CREATE TABLE IF NOT EXISTS emotion_record (
    id BIGSERIAL PRIMARY KEY,
    input_text TEXT NOT NULL,
    user_id BIGINT REFERENCES app_user(id),
    concept_id BIGINT NOT NULL REFERENCES emotion_concept(id),
    match_score INTEGER NOT NULL CHECK (match_score BETWEEN 0 AND 100),
    explanation TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE emotion_record
    ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES app_user(id);

CREATE INDEX IF NOT EXISTS idx_emotion_record_created_at
    ON emotion_record (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_emotion_record_user_created_at
    ON emotion_record (user_id, created_at DESC);
