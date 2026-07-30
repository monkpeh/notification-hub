# Notification Orchestration Platform — Build Roadmap

A personal-project recreation of the UCC Hub template management system, extended with an execution tier (campaigns → jobs → delivery), AWS infrastructure, and an AI layer — informed by both the original UCC Hub internship scope and the production-scale UCCC architecture (billions of messages, hybrid on-prem/AWS, multi-region).

---

## Terminology adopted from UCCC (production reference)

| UCCC term | Used in this design as | Why adopted |
|---|---|---|
| JCR (Job Communication Record) | `job_record` | Same purpose — one row per resolved template per customer |
| Guardrails (customer-level + daily-level) | `guardrail_config` / `guardrail_counter` | Split into two distinct enforcement scopes rather than one generic "consent" check |
| Communication Status enum (Delivered / Fail / Soft Bounce / Hard Bounce / Extraction) | `delivery_attempt.status` | Distinguishes soft vs. hard bounce and adds "extraction" as a controlled stop, not a failure |
| Extraction | `job_record.status = EXTRACTED` | A deliberate stop (enrichment failure or guardrail hit) is not the same as a delivery failure |
| RabbitMQ priority queueing | 3-tier SQS (critical / standard / batch) | Mission-critical sends immediately, batch sends within a window |
| Four-stage funnel (Ingestion → Enrichment → Delivery → Reporting) | Job lifecycle state machine | Cleaner backbone than a flat status field |
| Campaign → Parent Template → Child Template | Same hierarchy, same names | Directly reusable |

---

## Phase 1 — Foundation: schema, CRUD, RBAC, audit trail

**Build order matters here** — get this sequence wrong and you'll be retrofitting security or auditability onto code that wasn't shaped for it.

1. **Schema and migrations first.** Stand up `campaign`, `parent_template`, `child_template`, `template_config`, `comm_window` with Flyway (Java) or Alembic (Python) from commit one — never hand-edit a dev schema and let migrations catch up later.
2. **Repository / data access layer.** Plain repository classes over the ORM, no business logic yet. Decide the camelCase ↔ snake_case alias-mapping strategy once, here.
3. **Bare CRUD endpoints, no auth.** Validate the data model and relationships in isolation — is the parent-child cascade actually correct? — before security logic tangles into every request path. Treat as throwaway-if-needed.
4. **RBAC.** Roles table, role-permission mapping, a real identity provider (Cognito if on AWS) issuing JWTs, an authorization guard in front of CRUD. Mirror the UCC Hub tier system: `ADMIN`/`SUPER_ADMIN` = direct edit, `TEMPLATE_BUILDER`/automation roles = tiered/approval-required. Must precede audit — an audit record without a real actor identity is just a timestamp.
5. **Field validation service.** Constraint registration (enum checks, max length, immutable fields like `event_type`/`is_parent`) between controller and repository. Do this before wiring the approval flow so invalid edits are rejected before becoming pending requests.
6. **Wire CRUD through validation + tiering.** Edit path now does something real: role check → validation → direct persist (Tier 1) or pending edit request (Tier 2).
7. **Audit trail.** Append-only table: template id, field, old value, new value, actor, reason, timestamp, linked edit-request id. Hook in at the service layer, post-commit — respect the same transaction-boundary lesson as UCC Hub (don't let the audit write conflict with the edit's own transaction).
8. **Filtered audit read APIs.** Server-side filtering by template id, table, date range. Lowest risk, built last since it's read-only.

**Why this order and not RBAC-first:** security wrapped around an unvalidated data model just means you've secured the wrong thing. Get the shape of the data right, then lock it down, then make it accountable. Each step is also independently demoable.

---

## Phase 2 — Integrity engine and approval workflow

### Two-tier check architecture

```
IntegrityCheckEngine
├── StructuralCheck      (single template — cheap, runs on every edit)
├── ConfigCheck          (template + its configs — cheap, runs on every edit)
└── CollisionCheck       (cross-template/cross-campaign — expensive, scheduled + on-demand)
```

```java
void refreshForTemplate(Integer templateId);   // targeted — structural + config only, ~50ms
void refreshCollisions();                       // full cross-entity scan — scheduled
List<Violation> checkBeforeCommit(EditRequest);  // pre-edit gate, synchronous
```

### Structural / config checks (carried over + additions)

Orphan parent refs, zero-vs-null parent IDs, missing active configs, IVR configs missing `contactFlowID` — plus:

| Check | What it catches |
|---|---|
| `CHILD_COUNT_ANOMALY` | Parent has fewer than ~8 or more than ~20 children (normal range per UCCC: 12–16) |
| `GUARDRAIL_MISSING` | Active child template with zero `guardrail_config` rows |

### Collision checks (new — cross-entity reasoning)

**`SHADOWED_CHILD_TEMPLATE`** — a child template that can never be selected because a sibling's targeting is a superset of its own.

```
for each parent_template p:
  for each pair (child_a, child_b) in children(p):
    if targeting(child_b) ⊇ targeting(child_a)
       and priority(child_b) >= priority(child_a)
       and evaluation_order(child_b) < evaluation_order(child_a):
      flag child_a as SHADOWED_CHILD_TEMPLATE, shadowed_by = child_b
```

**`CAMPAIGN_TARGET_OVERLAP`** — two active campaigns whose templates target overlapping segments within overlapping communication windows. Advisory, not blocking — surfaced for the campaign owner to confirm intent.

**`CUSTOMER_OVER_CONTACTED`** — union across *all* campaigns: has this customer received more than N total communications in a rolling window, regardless of which campaign sent them. Catches what per-campaign guardrails miss individually.

**`DAILY_VOLUME_ANOMALY`** — flags a template's daily volume trending toward its configured cap, as an early warning before `EXTRACTED` records appear en masse.

### Trigger and cost model

| Check tier | Trigger | Cost | Blocking? |
|---|---|---|---|
| Structural / config | Every edit (pre-commit) + 30-min full scan | ~50ms targeted | Yes, for hard violations |
| Collision | Scheduled (hourly) + on-demand pre-launch check | Seconds–minutes | No — advisory only |

### Approval workflow

```
Edit/launch submitted
        ↓
Structural + config check (synchronous, blocking)
   → hard violation? reject immediately, no request created
        ↓ pass
Role check
   → Tier 1 (Admin/Super Admin) ──────────┐
   → Tier 2 (Template Builder/Automation) │
        ↓                                  │
Collision check (async, cached)            │
   → attach current violations as          │
     context on the pending request        │
        ↓                                  │
Pending edit request created                │
   severity = HIGH (active jobs reference   │
   this template), MEDIUM (collision        │
   violations exist), LOW otherwise         │
        ↓                                  ↓
Approver reviews (sees collision context) Direct apply
        ↓                                  ↓
Approve → apply, audit, cache refresh    Apply, audit, cache refresh
Reject → audit rejection reason
```

**Key departure from UCC Hub:** Tier 1 users no longer bypass collision context silently. They can still apply directly, but a flagged collision surfaces as a non-blocking warning banner at apply time — logged in the audit record either way.

### Notification hook

Webex/Slack alerts on: new `SHADOWED_CHILD_TEMPLATE` or `CAMPAIGN_TARGET_OVERLAP` (alerts both affected campaign owners), `CUSTOMER_OVER_CONTACTED` threshold crossed (alerts ops).

### Build order within Phase 2

Structural/config checks first (direct port, testable against Phase 1's CRUD) → collision checks second (need realistic multi-campaign data to detect anything) → approval workflow last (consumes both check tiers' output).

---

## Phase 3 — Execution tier

### Job lifecycle

```
INGESTED → ENRICHING → ENRICHED → DELIVERING → DELIVERED | PARTIALLY_FAILED → REPORTED
```

Created per ingestion event (`source_type: API_SINGLE | FILE_BULK | QUEUE`). Transition into `ENRICHING` is the version-snapshot trigger point.

### Core tables

```sql
job (
  id, campaign_id, source_type, state, total_records,
  template_snapshot JSONB,   -- frozen at ENRICHING
  ingested_at, created_by
)

job_record (                 -- = JCR
  id, job_id, resolved_child_template_id, customer_ref,
  params JSONB, routing_reason, guardrail_result,
  idempotency_key UNIQUE, status, current_stage
)

delivery_attempt (
  id, job_record_id, channel, attempt_number, provider,
  provider_message_id, status, status_detail, sent_at, resolved_at
)
-- status: DELIVERED | FAIL | SOFT_BOUNCE | HARD_BOUNCE | EXTRACTED

guardrail_config (
  id, template_id, scope,     -- CUSTOMER | DAILY
  limit_count, window, action -- BLOCK | QUEUE | ALERT
)

guardrail_counter (
  scope_key, window_start, count   -- Redis-backed for real-time increments
)
```

`job_record.status = EXTRACTED` is a distinct terminal state with `extraction_reason` (`ENRICHMENT_FAILURE`, `DAILY_GUARDRAIL_HIT`, `MANUAL_STOP`) — a deliberate stop shouldn't look like a bug in dashboards.

### Version snapshotting

When `job.state` → `ENRICHING`, the entire template tree (parent, children, configs, windows) is resolved and frozen into `job.template_snapshot`. Every `job_record` routes against the snapshot, never the live tables. **This is the direct fix for the UCC Hub silent-failure scenario** — an admin editing a template mid-flight can no longer corrupt a running job. Edits only affect jobs that haven't reached `ENRICHING` yet.

### Idempotency

`idempotency_key = hash(job_id, customer_ref, resolved_child_template_id)` for bulk jobs, or a client-supplied key for `API_SINGLE`. Unique constraint on `job_record`:
- A file re-processed after a crash can't create duplicate records (insert conflicts, no-ops).
- A delivery worker retrying after a timeout checks `delivery_attempt` for an existing terminal status first.
- File jobs additionally dedupe on `(job_id, line_number)` to survive partial re-ingestion.

### Priority queueing

RabbitMQ priority ranking → 3 SQS queues (`critical`, `standard`, `batch`), workers polling in a weighted ratio (always drain `critical` first, split remaining capacity ~70/30 standard/batch). Achieves "mission-critical immediately, batch within a 4-hour window" without a broker needing native priority support.

### Guardrail enforcement point

Runs at `ENRICHING → ENRICHED`, before a record is ever queued for delivery. Customer-level checks `guardrail_counter` for that customer + message type; daily-level checks the global counter for that template. A tripped record gets `status = EXTRACTED` immediately — never reaches the delivery queue. Cheaper and safer than catching it at send time.

---

## Phase 4 — AWS hardening

| Problem this phase fixes | AWS service |
|---|---|
| Personal Webex tokens expiring every 12 hours | **Secrets Manager** with automatic rotation |
| Dev login bypass because auth framework broke | **Cognito** — hosted UI, JWT, groups → role tiers |
| "Archive audit table if it grows too big" | **S3 lifecycle → Glacier**, queried via **Athena** |
| Scheduled scan tied to one app instance | **EventBridge Scheduler** → Lambda |
| In-memory cache, dies on restart, not shared | **ElastiCache Redis** |

Core stack: ECS Fargate behind an ALB (or App Runner), Lambda for scans + delivery workers, RDS PostgreSQL in private subnets, SQS with DLQs, EventBridge for domain events, Step Functions for the fallback-medium chain, SES/SNS for delivery, WAF + KMS + least-privilege IAM, CloudWatch + X-Ray, Terraform or CDK, GitHub Actions → ECR → ECS.

**Cost note:** most of this fits the free tier; watch for an idle NAT Gateway (~$32/mo) — use VPC endpoints instead. Set a Budget alarm at $10 on day one.

---

## Phase 5 — AI layer

Principle: **AI proposes, the existing validation and approval pipeline decides.** AI never bypasses field validation, integrity checks, or RBAC — it's just another Tier-2 actor in the system already built.

1. **Natural-language rule authoring** — plain-English campaign/rule description → Bedrock/Claude returns strict JSON → runs through the normal validator → lands as a pending approval request.
2. **Integrity violation explanation + proposed patch** — plain-English cause, blast radius, and a suggested fix the user accepts or rejects.
3. **Semantic search over rules** via `pgvector` embeddings.
4. **Pre-send copy QA** — missing merge variables, SMS length overruns, missing unsubscribe language.
5. **Anomaly narration** — explain delivery drops or spikes in plain language.

Add a small **eval harness** (30–50 fixed inputs/expected outputs, run on every prompt change) — this is the difference between "called an LLM API" and "built an AI feature I can defend in an interview."

---

## Suggested pacing

| Weeks | Focus |
|---|---|
| 1–3 | Phase 1 — domain model, migrations, CRUD, RBAC, audit. Local Docker Compose, no AWS. |
| 4–5 | Phase 2 — integrity engine + approval workflow. |
| 6–7 | Phase 3 — execution tier, real SES delivery, idempotency. |
| 8–9 | Phase 4 — AWS hardening, Terraform, Cognito, CI/CD. |
| 10–12 | Phase 5 — AI layer + eval harness, then consent/compliance surface. |

Ship each phase as a working deployed thing before starting the next — a finished Phase 3 beats a half-built Phase 5.
