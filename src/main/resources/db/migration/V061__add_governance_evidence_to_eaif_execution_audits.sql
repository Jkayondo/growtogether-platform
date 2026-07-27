ALTER TABLE eaif_execution_audits
    ADD COLUMN governance_policy_code VARCHAR(100);

ALTER TABLE eaif_execution_audits
    ADD COLUMN governance_decision VARCHAR(30);

ALTER TABLE eaif_execution_audits
    ADD COLUMN governance_reason VARCHAR(500);
