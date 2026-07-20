ALTER TABLE eiam_user_account
  ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN locked_until TIMESTAMPTZ NULL,
  ADD COLUMN last_login_at TIMESTAMPTZ NULL;

CREATE TABLE eiam_user_session (
  id UUID PRIMARY KEY,
  tenant_id UUID NOT NULL,
  user_id UUID NOT NULL REFERENCES eiam_user_account(id),
  refresh_token_hash VARCHAR(64) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  last_used_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ NULL,
  revoke_reason VARCHAR(100) NULL,
  created_at TIMESTAMPTZ NOT NULL,
  created_by VARCHAR(150) NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  updated_by VARCHAR(150) NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
CREATE INDEX idx_eiam_session_tenant_user ON eiam_user_session(tenant_id,user_id);
CREATE INDEX idx_eiam_session_expiry ON eiam_user_session(expires_at);
CREATE INDEX idx_eiam_session_active ON eiam_user_session(tenant_id,user_id) WHERE revoked_at IS NULL;
