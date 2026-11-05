ALTER TABLE event_creators ADD COLUMN space_type VARCHAR(16);
ALTER TABLE event_creators ADD CONSTRAINT ck_participant_space_type CHECK (space_type IS NULL OR space_type IN ('LARGE','ATELIER','MEDIUM','SMALL'));
