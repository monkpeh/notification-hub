# Phase 1 Tasks — Foundation

Ordered top to bottom. Work sequentially.

## 1. Schema and migrations
- [x] Gradle project, Spring Boot 3, Java 21, Flyway, Postgres driver
- [x] docker-compose.yml, confirmed reachable on port 5433
- [x] V1__create_campaign.sql
- [x] V2__create_template.sql
- [x] V3__create_template_config.sql
- [x] V4__create_comm_window.sql
- [x] V5__create_role_and_user.sql (seed four baseline roles)
- [x] V6__create_edit_request.sql
- [x] V7__create_audit_log.sql
- [ ] V8__add_ai_agent_role.sql
- [ ] V9__add_change_batch_id_to_audit_log.sql
- [ ] V10__add_governance_columns.sql (target_schema x2, approved_with_override)
- [ ] V11__create_dev_schema.sql (business tables only, explicit DDL)
- [ ] Verify all eleven apply cleanly against a fresh database

## 2. Repository / data access layer
- [x] Entity classes for all seven public-schema tables, ddl-auto: validate
- [x] Repository interfaces (Spring Data JPA)
- [x] Document camelCase <-> snake_case alias strategy once, in common/

## 3. Bare CRUD endpoints, no auth
- [x] POST /campaigns, GET /campaigns/{id}
- [x] POST /templates, PUT /templates/{id}
- [x] POST /templates/{id}/configs
- [x] POST /configs/{id}/windows
- [x] Manually verify parent-child cascade and FK integrity in DBeaver

## 4. RBAC
- [ ] AuthFilter reads pid header, resolves to app_user
- [ ] Role-check guard in front of write endpoints
- [ ] 401 unauthenticated, 403 wrong-role
- [ ] Seed test users across all five roles (including AI_AGENT)

## 5. Field validation service
- [ ] Central FieldValidationService - enum, max length, immutable fields
- [ ] ValidationError DTO { fieldName, rejectedValue, reason }, returned as
      a list on 400 responses
- [ ] Wire into all write endpoints, before persistence
- [ ] Hierarchy rules (REQ-2), config/window rules (REQ-3/REQ-4)

## 6. Wire CRUD through validation + tiering
- [ ] Role check -> validation -> direct persist (Tier 1) or edit_request
      (Tier 2: TEMPLATE_BUILDER, AI_AGENT)
- [ ] GET/approve/reject edit-request endpoints, approver-only
- [ ] RateLimiterService - sliding window, 20/50/100 per hour by role,
      429 with reset time on exceed
- [ ] Stale-edit detection: isStale computed on GET /edit-requests
- [ ] Stale-edit enforcement: 409 on approve unless force=true;
      approved_with_override set when force is used

## 7. Audit trail
- [ ] audit_log write on every applied edit, one row per changed field
- [ ] change_batch_id: one UUID generated per edit action, shared across
      every audit row that action produces
- [ ] target_schema recorded on every audit_log and edit_request row
- [ ] Confirm post-commit timing relative to edit transaction
- [ ] No update/delete endpoint for audit_log

## 8. Filtered audit read API
- [ ] GET /audit-history with templateId, tableName, targetSchema,
      dateFrom, dateTo
- [ ] Manual verification across filter combinations

## 9. Multi-target schema editing
- [ ] TenantContext (ThreadLocal) + request filter reading X-Edit-Target
      header (dev/public, default public)
- [ ] SchemaTenantResolver (CurrentTenantIdentifierResolver)
- [ ] SchemaMultiTenantConnectionProvider (schema-switching via
      Connection.setSchema, single DataSource/pool)
- [ ] Spring Boot wiring: hibernate.multi_tenant=SCHEMA, register both
      beans (Spring Boot doesn't auto-configure this)
- [ ] Verify: same edit action targeting dev vs public writes only to the
      intended schema (confirm in DBeaver)
- [ ] Verify atomicity: an edit touching multiple business tables within
      one target commits/rolls back together as a single transaction

## Definition of done for Phase 1
- [ ] All nine sections checked
- [ ] ./gradlew clean build passes with no failing tests
- [ ] Integration test per domain object
- [ ] End-to-end manual run: campaign -> parent template -> child template
      -> config -> window -> Tier 2 edit -> Tier 1 approve -> audit history
      with correct change_batch_id grouping
- [ ] End-to-end manual run: same flow repeated with X-Edit-Target: dev,
      confirmed isolated from the public-schema data
- [ ] Rate limit triggers correctly for a TEMPLATE_BUILDER after 20
      submissions in an hour
- [ ] Stale-edit conflict returns 409, then succeeds with force=true

## Deferred to later phases
- Edit request archive job -> Phase 2
- Collision checks, shadowed-template detection -> Phase 2
- Job/campaign execution, delivery, guardrails -> Phase 3
- Cognito, Secrets Manager, real deployment -> Phase 4
- Any LLM-backed feature -> Phase 5
