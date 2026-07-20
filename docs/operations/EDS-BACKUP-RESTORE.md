# EDS Backup and Restore

Back up PostgreSQL metadata and object storage as one consistency set. Record database WAL position and object-store snapshot identifier. Restore first into an isolated environment, run checksum reconciliation, then authorize production recovery. A restore test is required before production certification.
