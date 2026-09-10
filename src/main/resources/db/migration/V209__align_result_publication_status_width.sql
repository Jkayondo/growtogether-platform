-- GT-SCH-RESULT-PUBLICATION-BASE-STATUS-CONFORMITY-001
--
-- V202 introduced the inherited AuditedTenantEntity status column as
-- VARCHAR(30). The shared persistence contract defines status as VARCHAR(20).
-- Preserve V202 immutably and correct the width forward.

ALTER TABLE gts_result_publication
    ALTER COLUMN status TYPE VARCHAR(20);
