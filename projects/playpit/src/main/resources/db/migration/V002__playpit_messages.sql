CREATE TABLE messages (
 message_id BIGSERIAL PRIMARY KEY,
 event_id BIGINT NOT NULL REFERENCES events(event_id),
 creator_id BIGINT REFERENCES creators(creator_id),
 artwork_id BIGINT REFERENCES artworks(artwork_id),
 message VARCHAR(300) NOT NULL CHECK(length(trim(message)) BETWEEN 1 AND 300),
 display_name VARCHAR(30), is_anonymous BOOLEAN NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('PENDING','PUBLISHED','HIDDEN','DELETED')),
 petal_type SMALLINT NOT NULL CHECK(petal_type BETWEEN 1 AND 5),
 petal_seed BIGINT NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 CHECK(creator_id IS NOT NULL OR artwork_id IS NOT NULL),
 UNIQUE(event_id,petal_seed)
);
CREATE INDEX idx_message_event_public ON messages(event_id,status,message_id);
CREATE INDEX idx_message_creator ON messages(event_id,creator_id,status,message_id);
CREATE INDEX idx_message_artwork ON messages(event_id,artwork_id,status,message_id);
CREATE INDEX idx_message_date ON messages(created_at);
-- Support state is separate from the design's messages columns. No raw IP/session id.
CREATE TABLE posting_sessions (
 session_hash VARCHAR(64) PRIMARY KEY, last_seen_at TIMESTAMP NOT NULL
);
CREATE TABLE message_submission_receipts (
 receipt_id BIGSERIAL PRIMARY KEY,
 session_hash VARCHAR(64) NOT NULL REFERENCES posting_sessions(session_hash) ON DELETE CASCADE,
 idempotency_key VARCHAR(64), request_hash VARCHAR(64) NOT NULL, body_hash VARCHAR(64) NOT NULL,
 message_id BIGINT NOT NULL REFERENCES messages(message_id),
 created_at TIMESTAMP NOT NULL, expires_at TIMESTAMP NOT NULL,
 UNIQUE(session_hash,idempotency_key)
);
CREATE INDEX idx_receipt_window ON message_submission_receipts(session_hash,created_at);
CREATE TABLE event_petal_sequences (
 event_id BIGINT PRIMARY KEY REFERENCES events(event_id), next_value BIGINT NOT NULL
);
CREATE TABLE message_reports (
 report_id BIGSERIAL PRIMARY KEY,
 message_id BIGINT NOT NULL REFERENCES messages(message_id),
 reason VARCHAR(300) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK(status IN ('OPEN','REVIEWED')),
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);
CREATE INDEX idx_report_open ON message_reports(status,created_at DESC);
CREATE TABLE analytics_events (
 analytics_id BIGSERIAL PRIMARY KEY,
 event_name VARCHAR(40) NOT NULL,
 event_id BIGINT REFERENCES events(event_id), creator_id BIGINT REFERENCES creators(creator_id), artwork_id BIGINT REFERENCES artworks(artwork_id),
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);
CREATE INDEX idx_analytics_event_time ON analytics_events(event_id,event_name,created_at);
