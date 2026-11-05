CREATE TABLE participant_import_batches (
 import_id BIGSERIAL PRIMARY KEY,
 event_id BIGINT NOT NULL REFERENCES events(event_id),
 fingerprint VARCHAR(64) NOT NULL CHECK (fingerprint ~ '^[a-f0-9]{64}$'),
 created_count INTEGER NOT NULL CHECK (created_count >= 0),
 linked_count INTEGER NOT NULL CHECK (linked_count >= 0),
 skipped_count INTEGER NOT NULL CHECK (skipped_count >= 0),
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 UNIQUE(event_id,fingerprint)
);
