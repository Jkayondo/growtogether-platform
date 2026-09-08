# GT-EAIF-EXEC-001 — Shared text execution candidate

Status: IMPLEMENTED IN PATCH; BUILD, TEST AND RUNTIME VERIFICATION PENDING.
Authority: Chat 4E user direction to implement the next shared provider-neutral batch.
Input: uploaded working-tree archive, reported HEAD b7986c43602cf5dd28f5ec597c616e19b34d4114.
This is not a freeze, release-candidate tag, production authorisation or AI Teacher completion.

## Behaviour

Products submit through existing AiFoundationService.submit. For this text path, use
AiTextRequest.hash(exactInput) as the submitted input hash. Commit submission and any
required approval before calling AiTextExecutionService.execute(tenantId, requestId, exactInput).
No OpenAI call is permitted in a product module. The return value is an EDS document reference.
The new service is an internal application interface, not a new public HTTP endpoint.
The legacy provider-execution endpoint still publishes an intent; this batch does not wire an
event consumer or silently reroute it. A product integration must invoke this service explicitly.

The service requires a real GT tenant principal and ai.runtime.execute permission, enabled EAIF
execution, an ACTIVE/APPROVED request, unchanged input hash, current governance permission,
required human approval, and an active registered provider/model. HIGH/CRITICAL requests also
require approval when the existing high-risk setting is enabled. Claiming updates the existing
optimistically versioned request and audit in a separate committed transaction before dispatch.
Repeated PROCESSING, FAILED or SUCCEEDED requests are rejected; no automatic retry or failover.

The EIP AI gateway matches the tenant's connector code exactly to the EAIF provider code. It
reuses ExternalProviderExecutionGate, connector re-read, ExternalConnectorCredentialResolver
and ECS timeout resolution. The AI adapter contract is separate from the existing messaging
contract so Brevo's API does not change. Future approved adapters register their own connector
type and provider type; products keep the same interface.

The OpenAI adapter uses Responses text execution with store:false, stream:false, a configured
model and an output-token bound. It accepts only the official HTTPS OpenAI base URL and never
follows redirects. This restriction belongs to the OpenAI adapter, not the GT provider-neutral
architecture. It rejects incomplete output, refusals, malformed responses and HTTP errors;
raw provider errors, credentials and prompts are not attached to thrown exceptions.
Responses are capped at 2 MB while receiving; text output at 1 million characters.
Source: https://developers.openai.com/api/docs/guides/migrate-to-responses
store:false does not constitute a claim of zero data retention by the external provider.

Successful text passes existing FileUploadService validation/scanning/storage, then is registered
as a RESTRICTED EDS document. Request and audit completion share the document-registration
transaction. File bytes are not transactional: a later database failure can leave an unreferenced
stored file needing the existing operational reconciliation process. Crashes after dispatch may
leave PROCESSING requests. Do not reset or retry them automatically; external execution could
already have incurred cost. No background recovery worker is introduced in this batch.

## Configuration prerequisites — later secure setup

- Register a tenant provider and model using existing administration. Model capability must be
  CHAT, COMPLETION or SUMMARIZATION. No model is hard-coded. Default maximum output is 2048
  tokens when the model limit is absent; configured limits outside 1–16384 are rejected.
- Create a same-code EIP connector, type OPENAI_RESPONSES, auth API_KEY, official OpenAI base URL.
  Provider type is the existing OPENAI_COMPATIBLE enum. Other compatible hosts are not supported
  by this adapter. EAIF endpoint/credential-reference columns are preserved, but EIP connector
  configuration and encrypted credentials are authoritative for this execution path.
- Supply credentials through existing secure connector administration, never chat/source files.
- Satisfy existing EIP environment certification and external delivery gates; enable existing
  EAIF_PROVIDER_EXECUTION_ENABLED only for an authorised environment. No flags are enabled here.
- Ensure DEFAULT_AI_POLICY and required approval records exist. Execution permission alone
  does not approve the request. No permissions or role assignments are silently granted.
- Configure GT file policy to permit text/plain, with working storage and scanning. EDS existing
  classification, access and retention rules apply. Product-side review and output consumption
  remain the subsequent slice; generated text is not automatically an approved explanation.

## Scope and evidence

Two existing entity classes gain read accessors. No migrations, tables, dependency versions,
existing controllers, messaging adapters, frontend files, keys or provider setup are changed.
The source archive omits runtime configuration, and the authoring environment has a Java 17
runtime but no javac or Maven. Java compilation and the 19 added JUnit tests were NOT RUN here.
Patch applicability and exact byte reconstruction are checked separately during packaging.
JUnit tests use test doubles; they do not prove PostgreSQL transaction concurrency, file-system
recovery, authenticated HTTP behaviour, or live OpenAI execution. Those remain explicit gates.

The historical 1,379-test success is inherited evidence, not a result of this batch. Do not update
that count by adding test annotations. Run the candidate tests on the authoritative Java 21
workstation, then required regression and real database/runtime gates before claiming verification.

Next controlled action: preserve existing reports, check/apply the patch against unchanged
inputs, and run the targeted test command supplied in README.md. No commit or tag is automatic.


## Workstation verification — 8 September 2026

This update supersedes the earlier pending workstation-test status.
The original authoring-environment limitations remain historical evidence.

- New batch tests: 19 passed; no failures, errors or skips.
- Existing EAIF/EIP regression tests: 38 passed; no failures, errors or skips.
- Full working-tree suite: 1,402 passed; no failures, errors or skips.
  Completed 2026-09-08 at 07:44:18 EAT.
- After adding text/plain: schema smoke test passed (1 test).
  Completed 2026-09-08 at 08:23:00 EAT.
- File-foundation dependency commit:
  697e2706c2989602a8f41bf23535590b5cca3003.

Evidence source: workstation command output supplied by John Kayondo.
These results describe the complete working tree, not an isolated checkout.
The four-test difference beyond the 19 added tests remains unattributed.
Live provider execution, actual output storage, database concurrency,
teacher-facing E2E and baseline freeze remain pending.
The default file scanner is a placeholder and performs no malware scanning.

Expansion direction: extend the same EAIF for images, audio, video,
documents, structured outputs and embeddings. These remain future scope,
not implemented capabilities of this text-execution batch.
