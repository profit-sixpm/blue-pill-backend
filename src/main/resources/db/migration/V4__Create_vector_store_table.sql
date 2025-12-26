-- Vector 확장을 사용하기 위해 extension 생성
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Vector Store 테이블 생성
CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1536)
);

-- HNSW 인덱스 생성 (성능 최적화)
CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);

-- 코멘트 추가
COMMENT ON TABLE vector_store IS 'Spring AI Vector Store';
COMMENT ON COLUMN vector_store.id IS '문서 ID (UUID)';
COMMENT ON COLUMN vector_store.content IS '문서 내용 (Text)';
COMMENT ON COLUMN vector_store.metadata IS '메타데이터 (JSON)';
COMMENT ON COLUMN vector_store.embedding IS '임베딩 벡터 (1536차원)';
