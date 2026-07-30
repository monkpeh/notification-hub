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
- WHEN the system starts THEN SUPER_ADMIN, ADMIN, TEMPLATE_BUILDER, and
  APPROVER SHALL exist as roles.
- WHEN a user is created THEN it SHALL reference exactly one role.
- WHEN an unauthenticated request hits a write endpoint THEN the system
  SHALL return 401.

## REQ-6: Role-based tiered access
- WHEN SUPER_ADMIN or ADMIN submits a valid edit THEN the system SHALL
  apply it immediately.
- WHEN TEMPLATE_BUILDER submits a valid edit THEN the system SHALL create a
  pending edit request instead of applying it.
- WHEN a non-approver attempts to approve/reject THEN the system SHALL
  return 403.

## REQ-7: Field validation service
- WHEN a registered field violates its constraint THEN the system SHALL
  reject the request before any database write.
- WHEN a request modifies an immutable field (eventType, isParent,
  createdBy) THEN the system SHALL reject it regardless of role.

## REQ-8: Audit trail
- WHEN any edit is applied THEN the system SHALL write one audit record per
  changed field: old value, new value, actor, reason, timestamp.
- Audit records SHALL be append-only - no update/delete endpoint exists.
- The audit write SHALL occur after the edit's own transaction commits.

## REQ-9: Filtered audit history API
- WHEN GET /audit-history is called with templateId, tableName, or a date
  range THEN the system SHALL apply all filters server-side, paginated.
- WHEN no filters are provided THEN the system SHALL return the most
  recent page of all records.
