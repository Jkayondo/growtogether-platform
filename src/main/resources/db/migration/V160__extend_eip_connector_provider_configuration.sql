-- V160
-- Provider-neutral non-secret configuration for EIP external connectors.
-- Secrets remain exclusively in credential_ciphertext.

ALTER TABLE eip_external_connectors
    ADD COLUMN provider_configuration TEXT;

COMMENT ON COLUMN eip_external_connectors.provider_configuration IS
    'Provider-specific non-secret configuration. Must never contain credentials, API keys, passwords or tokens.';
