CREATE TABLE app_user (
  user_id UUID PRIMARY KEY,
  email VARCHAR(254) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(100) NOT NULL,
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE','SUSPENDED')),
  auth_version BIGINT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX uq_app_user_email_lower ON app_user(LOWER(email));

CREATE TABLE app_user_role (
  user_id UUID NOT NULL REFERENCES app_user(user_id),
  role VARCHAR(20) NOT NULL CHECK (role IN ('MEMBER','CHASER')),
  PRIMARY KEY(user_id, role)
);

CREATE TABLE auth_token (
  token_id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES app_user(user_id),
  purpose VARCHAR(30) NOT NULL CHECK (purpose IN ('VERIFY_EMAIL','RESET_PASSWORD')),
  token_hash VARCHAR(64) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_auth_token_expiry ON auth_token(expires_at);

CREATE TABLE creator (
  creator_id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  profile TEXT NOT NULL CHECK (char_length(profile) <= 3000),
  status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE','SUSPENDED')),
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE creator_application (
  application_id UUID PRIMARY KEY,
  applicant_id UUID NOT NULL REFERENCES app_user(user_id),
  name VARCHAR(100) NOT NULL,
  profile TEXT NOT NULL CHECK (char_length(profile) <= 3000),
  status VARCHAR(20) NOT NULL CHECK (status IN ('SUBMITTED','APPROVED','REJECTED')),
  reason VARCHAR(1000),
  reviewed_by UUID REFERENCES app_user(user_id),
  creator_id UUID REFERENCES creator(creator_id),
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX uq_creator_application_submitted ON creator_application(applicant_id) WHERE status='SUBMITTED';

CREATE TABLE creator_member (
  creator_id UUID NOT NULL REFERENCES creator(creator_id),
  user_id UUID NOT NULL REFERENCES app_user(user_id),
  role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER','EDITOR')),
  created_at TIMESTAMPTZ NOT NULL,
  PRIMARY KEY(creator_id, user_id)
);
CREATE UNIQUE INDEX uq_creator_owner ON creator_member(creator_id) WHERE role='OWNER';

CREATE TABLE asset (
  asset_id UUID PRIMARY KEY,
  creator_id UUID NOT NULL REFERENCES creator(creator_id),
  storage_key VARCHAR(300) NOT NULL UNIQUE,
  mime_type VARCHAR(100) NOT NULL,
  byte_size BIGINT NOT NULL CHECK (byte_size > 0),
  sha256 VARCHAR(64) NOT NULL,
  state VARCHAR(20) NOT NULL CHECK (state IN ('READY','BLOCKED','ABORTED')),
  original_name VARCHAR(255) NOT NULL,
  created_by UUID NOT NULL REFERENCES app_user(user_id),
  unreferenced_since TIMESTAMPTZ,
  cleanup_state VARCHAR(20) NOT NULL DEFAULT 'NONE' CHECK (cleanup_state IN ('NONE','CLAIMED','FAILED','DONE')),
  cleanup_after TIMESTAMPTZ,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_asset_creator_state ON asset(creator_id,state);

CREATE TABLE work (
  work_id UUID PRIMARY KEY,
  creator_id UUID NOT NULL REFERENCES creator(creator_id),
  kind VARCHAR(20) NOT NULL CHECK (kind IN ('IMAGE','AUDIO')),
  title VARCHAR(200) NOT NULL,
  description TEXT NOT NULL CHECK (char_length(description) <= 10000),
  credit VARCHAR(1000) NOT NULL,
  asset_id UUID NOT NULL REFERENCES asset(asset_id),
  archived BOOLEAN NOT NULL DEFAULT FALSE,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_work_creator_archive ON work(creator_id,archived,work_id);

CREATE TABLE story_fragment (
  story_id UUID PRIMARY KEY,
  work_id UUID NOT NULL REFERENCES work(work_id),
  title VARCHAR(200) NOT NULL,
  body TEXT NOT NULL CHECK (char_length(body) <= 10000),
  asset_id UUID REFERENCES asset(asset_id),
  position INTEGER NOT NULL CHECK (position >= 0),
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_story_work_position ON story_fragment(work_id,position,story_id);

CREATE TABLE room (
  room_id UUID PRIMARY KEY,
  creator_id UUID NOT NULL REFERENCES creator(creator_id),
  slug VARCHAR(80) NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
  state VARCHAR(20) NOT NULL CHECK (state IN ('PRIVATE','PUBLIC','SUSPENDED','ARCHIVED')),
  current_publication_id UUID,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_room_creator_state ON room(creator_id,state);

CREATE TABLE room_draft (
  room_id UUID PRIMARY KEY REFERENCES room(room_id),
  schema_version INTEGER NOT NULL CHECK (schema_version=1),
  config JSONB NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE room_publication (
  publication_id UUID PRIMARY KEY,
  room_id UUID NOT NULL REFERENCES room(room_id),
  revision INTEGER NOT NULL CHECK (revision > 0),
  schema_version INTEGER NOT NULL CHECK (schema_version=1),
  snapshot JSONB NOT NULL,
  status VARCHAR(20) NOT NULL CHECK (status IN ('SUBMITTED','APPROVED','REJECTED','WITHDRAWN')),
  submitted_by UUID NOT NULL REFERENCES app_user(user_id),
  reviewed_by UUID REFERENCES app_user(user_id),
  reason VARCHAR(1000),
  submitted_at TIMESTAMPTZ NOT NULL,
  reviewed_at TIMESTAMPTZ,
  version BIGINT NOT NULL DEFAULT 0,
  UNIQUE(room_id, revision),
  UNIQUE(room_id, publication_id)
);
CREATE UNIQUE INDEX uq_room_publication_submitted ON room_publication(room_id) WHERE status='SUBMITTED';
ALTER TABLE room ADD CONSTRAINT fk_room_current_publication
  FOREIGN KEY(room_id,current_publication_id) REFERENCES room_publication(room_id,publication_id);

CREATE TABLE publication_work (
  publication_id UUID NOT NULL REFERENCES room_publication(publication_id),
  work_id UUID NOT NULL REFERENCES work(work_id),
  position INTEGER NOT NULL CHECK (position >= 0),
  PRIMARY KEY(publication_id,work_id)
);

CREATE TABLE publication_asset (
  publication_id UUID NOT NULL REFERENCES room_publication(publication_id),
  asset_id UUID NOT NULL REFERENCES asset(asset_id),
  PRIMARY KEY(publication_id,asset_id)
);
CREATE INDEX idx_publication_asset_asset ON publication_asset(asset_id);

CREATE TABLE creve_event (
  event_id UUID PRIMARY KEY,
  slug VARCHAR(80) NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
  title VARCHAR(200) NOT NULL,
  description TEXT NOT NULL CHECK (char_length(description) <= 10000),
  starts_at TIMESTAMPTZ,
  ends_at TIMESTAMPTZ,
  state VARCHAR(20) NOT NULL CHECK (state IN ('DRAFT','PUBLISHED','CLOSED','SUSPENDED')),
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CHECK (starts_at IS NULL OR ends_at IS NULL OR starts_at < ends_at)
);

CREATE TABLE event_room (
  event_id UUID NOT NULL REFERENCES creve_event(event_id),
  room_id UUID NOT NULL REFERENCES room(room_id),
  state VARCHAR(20) NOT NULL CHECK (state IN ('INVITED','ACCEPTED','DECLINED','REMOVED')),
  position INTEGER NOT NULL CHECK (position >= 0),
  version BIGINT NOT NULL DEFAULT 0,
  invited_at TIMESTAMPTZ NOT NULL,
  responded_at TIMESTAMPTZ,
  PRIMARY KEY(event_id,room_id)
);
CREATE INDEX idx_event_room_state_position ON event_room(event_id,state,position);

CREATE TABLE visitor_identity (
  visitor_id UUID PRIMARY KEY,
  secret_hash VARCHAR(64) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL,
  last_seen_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE saved_room (
  user_id UUID NOT NULL REFERENCES app_user(user_id),
  room_id UUID NOT NULL REFERENCES room(room_id),
  created_at TIMESTAMPTZ NOT NULL,
  PRIMARY KEY(user_id,room_id)
);

CREATE TABLE artist_follow (
  user_id UUID NOT NULL REFERENCES app_user(user_id),
  creator_id UUID NOT NULL REFERENCES creator(creator_id),
  created_at TIMESTAMPTZ NOT NULL,
  PRIMARY KEY(user_id,creator_id)
);

CREATE TABLE content_report (
  report_id UUID PRIMARY KEY,
  target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('ROOM','WORK')),
  room_id UUID NOT NULL REFERENCES room(room_id),
  work_id UUID REFERENCES work(work_id),
  category VARCHAR(20) NOT NULL CHECK (category IN ('RIGHTS','ABUSE','OTHER')),
  detail VARCHAR(1000) NOT NULL,
  reporter_user_id UUID REFERENCES app_user(user_id),
  visitor_id UUID REFERENCES visitor_identity(visitor_id),
  state VARCHAR(20) NOT NULL CHECK (state IN ('OPEN','IN_REVIEW','CLOSED')),
  resolution VARCHAR(1000),
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CHECK ((reporter_user_id IS NULL) <> (visitor_id IS NULL)),
  CHECK ((target_type='WORK' AND work_id IS NOT NULL) OR target_type='ROOM')
);

CREATE TABLE audit_log (
  audit_id UUID PRIMARY KEY,
  actor_id UUID REFERENCES app_user(user_id),
  action VARCHAR(80) NOT NULL,
  target_type VARCHAR(40) NOT NULL,
  target_id UUID NOT NULL,
  result VARCHAR(40) NOT NULL,
  trace_id UUID NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_audit_target ON audit_log(target_type,target_id,occurred_at);

CREATE TABLE metric_event (
  metric_id UUID PRIMARY KEY,
  kind VARCHAR(30) NOT NULL CHECK (kind IN ('ROOM_VIEW','WORK_VIEW','AUDIO_START','STORY_VIEW','SHARE_UI','OUTBOUND_CLICK')),
  creator_id UUID NOT NULL REFERENCES creator(creator_id),
  room_id UUID NOT NULL REFERENCES room(room_id),
  publication_id UUID NOT NULL REFERENCES room_publication(publication_id),
  work_id UUID REFERENCES work(work_id),
  visitor_hash VARCHAR(64),
  occurred_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_metric_creator_kind ON metric_event(creator_id,kind,occurred_at);

CREATE TABLE business_result (
  result_id UUID PRIMARY KEY,
  creator_id UUID NOT NULL REFERENCES creator(creator_id),
  kind VARCHAR(20) NOT NULL CHECK (kind IN ('SALE','COST','INQUIRY','ORDER')),
  amount_minor BIGINT CHECK (amount_minor >= 0),
  currency VARCHAR(3),
  count INTEGER NOT NULL CHECK (count >= 0),
  occurred_on DATE NOT NULL,
  verification VARCHAR(20) NOT NULL CHECK (verification IN ('UNCONFIRMED','CONFIRMED')),
  note VARCHAR(1000),
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CHECK ((kind IN ('SALE','COST') AND amount_minor IS NOT NULL AND currency IS NOT NULL) OR kind IN ('INQUIRY','ORDER'))
);

CREATE TABLE official_content (
  content_id UUID PRIMARY KEY,
  slug VARCHAR(80) NOT NULL CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
  type VARCHAR(20) NOT NULL CHECK (type IN ('ABOUT','NEWS','CONTACT','PRIVACY','TERMS','VR')),
  title VARCHAR(200) NOT NULL,
  body TEXT NOT NULL,
  published BOOLEAN NOT NULL DEFAULT FALSE,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  UNIQUE(type,slug)
);

CREATE TABLE project_link (
  link_id UUID PRIMARY KEY,
  project_key VARCHAR(40) NOT NULL UNIQUE,
  label VARCHAR(100) NOT NULL,
  url VARCHAR(2048) NOT NULL,
  position INTEGER NOT NULL CHECK (position >= 0),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE mail_outbox (
  outbox_id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES app_user(user_id),
  template VARCHAR(80) NOT NULL,
  payload_ciphertext TEXT NOT NULL,
  state VARCHAR(20) NOT NULL CHECK (state IN ('PENDING','SENT','FAILED')),
  attempt_count INTEGER NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
  next_attempt_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_mail_outbox_delivery ON mail_outbox(state,next_attempt_at);

CREATE TABLE admin_guard (
  singleton_id INTEGER PRIMARY KEY CHECK (singleton_id=1),
  version BIGINT NOT NULL DEFAULT 0
);
