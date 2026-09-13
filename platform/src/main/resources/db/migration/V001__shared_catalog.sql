CREATE TABLE events (
 event_id BIGSERIAL PRIMARY KEY,
 event_type VARCHAR(20) NOT NULL,
 slug VARCHAR(100) NOT NULL UNIQUE,
 title VARCHAR(150) NOT NULL,
 summary VARCHAR(300),
 description TEXT,
 main_image_url VARCHAR(500),
 start_at TIMESTAMP NOT NULL,
 end_at TIMESTAMP NOT NULL,
 venue_name VARCHAR(150),
 address VARCHAR(300),
 access TEXT,
 price VARCHAR(100),
 ticket_url VARCHAR(500),
 status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT','PUBLISHED','HIDDEN','ARCHIVED')),
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 CHECK(end_at>=start_at)
);
CREATE TABLE creators (
 creator_id BIGSERIAL PRIMARY KEY,
 slug VARCHAR(100) NOT NULL UNIQUE,
 name VARCHAR(100) NOT NULL,
 name_kana VARCHAR(100),
 profile TEXT,
 concept TEXT,
 genre VARCHAR(100),
 profile_image_url VARCHAR(500),
 website_url VARCHAR(500),
 status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT','PUBLISHED','HIDDEN','ARCHIVED')),
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);
CREATE TABLE artworks (
 artwork_id BIGSERIAL PRIMARY KEY,
 creator_id BIGINT NOT NULL,
 slug VARCHAR(100) NOT NULL UNIQUE,
 title VARCHAR(150) NOT NULL,
 description TEXT,
 background TEXT,
 concept TEXT,
 materials VARCHAR(300),
 production_year SMALLINT,
 main_image_url VARCHAR(500),
 status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT','PUBLISHED','HIDDEN','ARCHIVED')),
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 FOREIGN KEY(creator_id) REFERENCES creators(creator_id)
);
CREATE TABLE event_creators (
 participation_id BIGSERIAL PRIMARY KEY,
 event_id BIGINT NOT NULL,
 creator_id BIGINT NOT NULL,
 exhibition_title VARCHAR(150),
 display_order INTEGER NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 FOREIGN KEY(event_id) REFERENCES events(event_id),
 FOREIGN KEY(creator_id) REFERENCES creators(creator_id),
 UNIQUE(event_id,creator_id)
);
CREATE TABLE event_artworks (
 event_artwork_id BIGSERIAL PRIMARY KEY,
 event_id BIGINT NOT NULL,
 artwork_id BIGINT NOT NULL,
 exhibition_area VARCHAR(150),
 display_order INTEGER NOT NULL,
 qr_code_url VARCHAR(500),
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 FOREIGN KEY(event_id) REFERENCES events(event_id),
 FOREIGN KEY(artwork_id) REFERENCES artworks(artwork_id),
 UNIQUE(event_id,artwork_id)
);
CREATE TABLE media_files (
 media_id BIGSERIAL PRIMARY KEY,
 artwork_id BIGINT NOT NULL,
 media_type VARCHAR(20) NOT NULL,
 url VARCHAR(500) NOT NULL,
 alt_text VARCHAR(300),
 display_order INTEGER NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 FOREIGN KEY(artwork_id) REFERENCES artworks(artwork_id),
 CHECK(media_type IN ('IMAGE','VIDEO','AUDIO'))
);
CREATE TABLE creator_links (
 link_id BIGSERIAL PRIMARY KEY,
 creator_id BIGINT NOT NULL,
 label VARCHAR(80) NOT NULL,
 url VARCHAR(500) NOT NULL,
 display_order INTEGER NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 FOREIGN KEY(creator_id) REFERENCES creators(creator_id)
);
CREATE TABLE admins (
 admin_id BIGSERIAL PRIMARY KEY,
 email VARCHAR(254) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL,
 enabled BOOLEAN NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);

CREATE INDEX idx_event_public_time ON events(event_type,status,start_at DESC);
CREATE INDEX idx_creator_participation ON event_creators(event_id,display_order);
CREATE INDEX idx_artwork_exhibition ON event_artworks(event_id,display_order);
CREATE INDEX idx_artwork_creator ON artworks(creator_id,status);
CREATE INDEX idx_media_artwork ON media_files(artwork_id,display_order);
CREATE INDEX idx_creator_link ON creator_links(creator_id,display_order);
CREATE TABLE login_attempts (
 identity_hash VARCHAR(64) PRIMARY KEY, failures INTEGER NOT NULL,
 window_start TIMESTAMP NOT NULL, locked_until TIMESTAMP, updated_at TIMESTAMP NOT NULL
);
