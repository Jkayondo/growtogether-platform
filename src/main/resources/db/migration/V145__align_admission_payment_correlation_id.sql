-- GT School Release 1
-- A12.4i — Admission payment correlation alignment
--
-- IMPROVEMENT:
-- Align admission payment gate evidence with the enterprise-wide
-- X-Correlation-ID / RequestContext contract.
--
-- V143 originally used UUID. Existing UUID values are safely retained
-- as their canonical text representation.

ALTER TABLE gts_admission_payment_gate_history
    ALTER COLUMN correlation_id TYPE VARCHAR(128)
    USING correlation_id::text;
