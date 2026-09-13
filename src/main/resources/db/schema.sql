CREATE TABLE events (
  id VARCHAR(64) PRIMARY KEY,
  slug VARCHAR(80) NOT NULL UNIQUE,
  event_type VARCHAR(24) NOT NULL,
  status VARCHAR(24) NOT NULL,
  name VARCHAR(160) NOT NULL,
  subtitle VARCHAR(180) NOT NULL,
  summary VARCHAR(240) NOT NULL,
  description TEXT NOT NULL,
  start_at TIMESTAMP WITH TIME ZONE NOT NULL,
  end_at TIMESTAMP WITH TIME ZONE NOT NULL,
  schedule_text VARCHAR(120) NOT NULL,
  venue_name VARCHAR(120) NOT NULL,
  venue_text VARCHAR(160) NOT NULL,
  main_image_url TEXT NOT NULL,
  postable BOOLEAN NOT NULL,
  display_order INTEGER NOT NULL
);

CREATE TABLE creators (
  id VARCHAR(64) PRIMARY KEY,
  slug VARCHAR(80) NOT NULL UNIQUE,
  status VARCHAR(24) NOT NULL,
  name VARCHAR(120) NOT NULL,
  artist_name VARCHAR(120),
  category VARCHAR(80) NOT NULL,
  profile TEXT NOT NULL,
  concept TEXT NOT NULL,
  icon_image_url TEXT NOT NULL,
  image_url TEXT NOT NULL,
  instagram VARCHAR(240),
  x VARCHAR(240),
  website VARCHAR(240),
  display_order INTEGER NOT NULL
);

CREATE TABLE artworks (
  id VARCHAR(64) PRIMARY KEY,
  slug VARCHAR(80) NOT NULL UNIQUE,
  event_id VARCHAR(64) NOT NULL,
  creator_id VARCHAR(64) NOT NULL,
  status VARCHAR(24) NOT NULL,
  title VARCHAR(160) NOT NULL,
  description TEXT NOT NULL,
  image_url TEXT NOT NULL,
  sub_image_url TEXT NOT NULL,
  background TEXT NOT NULL,
  intention TEXT NOT NULL,
  technique VARCHAR(120) NOT NULL,
  production_year VARCHAR(40) NOT NULL,
  display_order INTEGER NOT NULL
);

CREATE TABLE event_creators (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_id VARCHAR(64) NOT NULL,
  creator_id VARCHAR(64) NOT NULL,
  display_order INTEGER NOT NULL,
  UNIQUE (event_id, creator_id)
);

CREATE TABLE event_artworks (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_id VARCHAR(64) NOT NULL,
  artwork_id VARCHAR(64) NOT NULL,
  display_order INTEGER NOT NULL,
  UNIQUE (event_id, artwork_id)
);

CREATE TABLE messages (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_id VARCHAR(64) NOT NULL,
  creator_id VARCHAR(64),
  artwork_id VARCHAR(64),
  body VARCHAR(300) NOT NULL,
  display_name VARCHAR(30) NOT NULL,
  anonymous BOOLEAN NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  status VARCHAR(20) NOT NULL,
  petal_type INTEGER NOT NULL,
  petal_seed BIGINT NOT NULL,
  petal_x DOUBLE PRECISION NOT NULL,
  petal_y DOUBLE PRECISION NOT NULL,
  petal_angle DOUBLE PRECISION NOT NULL,
  petal_size DOUBLE PRECISION NOT NULL,
  session_hash VARCHAR(128) NOT NULL
);

CREATE INDEX idx_messages_event_status ON messages (event_id, status);
CREATE INDEX idx_messages_creator_status ON messages (event_id, creator_id, status);
CREATE INDEX idx_messages_artwork_status ON messages (event_id, artwork_id, status);
CREATE INDEX idx_messages_session_created ON messages (session_hash, created_at);

CREATE TABLE news (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  slug VARCHAR(80) NOT NULL UNIQUE,
  status VARCHAR(24) NOT NULL,
  title VARCHAR(160) NOT NULL,
  body TEXT NOT NULL,
  published_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE landing_pages (
  id VARCHAR(64) PRIMARY KEY,
  slug VARCHAR(80) NOT NULL UNIQUE,
  status VARCHAR(24) NOT NULL,
  title VARCHAR(160) NOT NULL,
  body TEXT NOT NULL
);

CREATE TABLE media_files (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  owner_type VARCHAR(40) NOT NULL,
  owner_id VARCHAR(64) NOT NULL,
  url TEXT NOT NULL,
  alt_text VARCHAR(160) NOT NULL,
  media_type VARCHAR(40) NOT NULL,
  display_order INTEGER NOT NULL
);

CREATE TABLE admins (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL
);
