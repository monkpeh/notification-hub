# Phase 1 Requirements — Foundation

## REQ-1: Campaign management
As an admin, I want to create and manage campaigns, so templates can be
grouped by business purpose.
- WHEN a valid campaign payload (name, status) is submitted THEN the system
  SHALL persist it and return its generated id.
- WHEN name exceeds 150 characters THEN the system SHALL reject with a 400.
- WHEN status is not ACTIVE/INACTIVE THEN the system SHALL reject it.
- WHEN a campaign is fetched by a nonexistent id THEN the system SHALL
  return 404.

## REQ-2: Template hierarchy (parent/child)
As an admin, I want templates in a one-level parent-child hierarchy scoped
to a campaign.
- WHEN isParent=true THEN parentTemplateId SHALL be null.
- WHEN a child template is created THEN parentTemplateId SHALL reference an
  existing template where isParent=true.
- WHEN parentTemplateId points to a template that is itself a child THEN
  the system SHALL reject it - max depth is one level.
- WHEN a template is created without a campaignId THEN the system SHALL
  reject it.

## REQ-3: Template configuration
As an admin, I want each template to have channel configs.
- WHEN communicationMedium is not in {EMAIL, SMS, IVR, PUSH, RCS} THEN the
  system SHALL reject it.
- WHEN communicationMedium=IVR THEN contactFlowId SHALL be required.
- WHEN useActive=true THEN only one active config per template per medium
  SHALL be allowed.

## REQ-4: Communication windows
- WHEN startWindow is later than endWindow THEN the system SHALL reject it.
- WHEN a window is created THEN it SHALL reference an existing
  template_config row.

## REQ-5: Roles and users
- WHEN the system starts THEN SUPER_ADMIN, ADMIN, TEMPLATE_BUILDER,
  APPROVER, and AI_AGENT SHALL exist as roles.
- WHEN a user is created THEN it SHALL reference exactly one role.
- WHEN an unauthenticated request hits a write endpoint THEN the system
  SHALL return 401.
- AI_AGENT is reserved for automation/AI-originated edits (see Phase 5) -
  it behaves identically to TEMPLATE_BUILDER for tiering purposes in
  Phase 1, but is tracked as a distinct actor identity.

## REQ-6: Role-based tiered access
- WHEN SUPER_ADMIN or ADMIN submits a valid edit THEN the system SHALL
  apply it immediately.
- WHEN TEMPLATE_BUILDER or AI_AGENT submits a valid edit THEN the system
  SHALL create a pending edit request instead of applying it.
- WHEN a non-approver attempts to approve/reject THEN the system SHALL
  return 403.

## REQ-7: Field validation service
- WHEN a registered field violates its constraint THEN the system SHALL
  reject the request before any database write, returning a
  ValidationError { fieldName, rejectedValue, reason } for each violation.
- WHEN a request modifies an immutable field (eventType, isParent,
  createdBy) THEN the system SHALL reject it regardless of role.

## REQ-8: Audit trail
- WHEN any edit is applied THEN the system SHALL write one audit record per
  changed field: old value, new value, actor, reason, timestamp.
- WHEN a single edit action changes multiple fields THEN every resulting
  audit row SHALL share the same change_batch_id, so the action can be
  reconstructed as one logical edit rather than N unrelated rows.
- WHEN an audit record is written THEN it SHALL capture which target
  schema (public/dev) the change applies to.
- Audit records SHALL be append-only - no update/delete endpoint exists.
- The audit write SHALL occur after the edit's own transaction commits.

## REQ-9: Filtered audit history API
- WHEN GET /audit-history is called with templateId, tableName, targetSchema,
  or a date range THEN the system SHALL apply all filters server-side,
  paginated.
- WHEN no filters are provided THEN the system SHALL return the most
  recent page of all records.

## REQ-10: Multi-target schema editing
As an admin, I want to direct an edit at either the live (public) or dev
schema, so I can test changes safely and explore multi-target editing
patterns without needing separate databases.
- WHEN an edit specifies a target of "public" or "dev" THEN the system
  SHALL route the write to that Postgres schema within a single database
  connection and transaction - no cross-connection coordination required.
- WHEN no target is specified THEN the system SHALL default to "public".
- WHEN an edit_request or audit_log row is created THEN it SHALL record
  which target schema the change applies to.
- A schema migration SHALL be applied to both public and dev together -
  the two schemas SHALL never be allowed to structurally diverge.

## REQ-11: Rate limiting
As an admin, I want to cap how many edits a single user can submit per
hour, so a runaway script or a mistake can't corrupt data at scale.
- WHEN a TEMPLATE_BUILDER or AI_AGENT exceeds 20 edit submissions within a
  rolling 1-hour window THEN the system SHALL reject further submissions
  with 429 until the window rolls forward.
- WHEN an ADMIN exceeds 50/hour, or a SUPER_ADMIN exceeds 100/hour THEN the
  same 429 behavior SHALL apply.
- WHEN a submission is rejected for rate limiting THEN the response SHALL
  include the window reset time.
- Rate limiting applies to edit submission only, not to approve/reject
  actions or read endpoints.

## REQ-12: Stale-edit conflict detection
As an approver, I want to know if a record has changed since a pending
edit was submitted against it, so I don't silently overwrite a more
recent change.
- WHEN a pending edit_request is listed THEN the system SHALL mark it
  stale if the field's current live value no longer matches the old_value
  captured at submission time.
- WHEN an approver attempts to approve a stale edit_request without an
  explicit override THEN the system SHALL return 409 Conflict, including
  the current value, the requested old value, and the requested new value.
- WHEN the approval request includes force=true THEN the system SHALL
  apply the edit despite staleness, and SHALL record that an override
  occurred.
