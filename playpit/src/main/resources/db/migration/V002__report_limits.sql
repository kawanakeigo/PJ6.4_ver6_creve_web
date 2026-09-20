CREATE TABLE pp_report_subject_locks (
  subject_hash VARCHAR(64) PRIMARY KEY,
  last_used_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_reports_subject_message_time
  ON pp_reports(subject_hash, message_id, created_at DESC, report_id DESC);
