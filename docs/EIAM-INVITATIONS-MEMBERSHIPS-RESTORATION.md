# EIAM Invitations, Onboarding and Membership Restoration

This baseline restores the executable capability that was previously described but missing from the cumulative repository.

Implemented:

- Tenant-scoped organization invitations
- SHA-256 invitation token hashing
- Expiry, resend, revocation and single-use acceptance
- Role assignment from invitations
- New-user onboarding
- Tenant membership lifecycle
- Enterprise audit events
- EIAM permission protection

The acceptance token is returned only at invitation creation/resend so development environments can test the workflow. Production delivery must send the token through ENS and omit it from administrator-facing API responses.
