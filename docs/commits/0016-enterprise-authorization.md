# Commit 0016 — Enterprise Authorization Engine

Commit: `feat(eiam): implement enterprise authorization engine and policy evaluation`

The engine evaluates active tenant policies for a resource type and action. Applicable deny policies override allow policies. No matching allow policy results in default deny. Policy conditions may require a permission, role, resource ownership, and minimum authentication assurance level. Every evaluation is written to the enterprise audit log.
