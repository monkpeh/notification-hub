# Phase 1 Tasks — Foundation

Ordered top to bottom. Work sequentially.

## 1. Schema and migrations
- [x] Gradle project, Spring Boot 3, Java 21, Flyway, Postgres driver
- [x] docker-compose.yml, confirmed reachable on port 5433
- [ ] V1__create_campaign.sql
- [ ] V2__create_template.sql
- [ ] V3__create_template_config.sql
- [ ] V4__create_comm_window.sql
- [ ] V5__create_role_and_user.sql (seed four baseline roles)
- [ ] V6__create_edit_request.sql
- [ ] V7__create_audit_log.sql
- [ ] Verify all seven apply cleanly against a fresh database

## 2. Repository / data access layer
- [ ] Entity classes for all seven tables, ddl-auto: validate
- [ ] Repository interfaces (Spring Data JPA)
- [ ] Document camelCase <-> snake_case alias strategy once, in common/

## 3. Bare CRUD endpoints, no auth
- [ ] POST /campaigns, GET /campaigns/{id}
- [ ] POST /templates, PUT /templates/{id}
- [ ] POST /templates/{id}/configs
- [ ] POST /configs/{id}/windows
- [ ] Manually verify parent-child cascade and FK integrity in DBeaver

## 4. RBAC
- [ ] AuthFilter reads pid header, resolves to app_user
- [ ] Role-check guard in front of write endpoints
- [ ] 401 unauthenticated, 403 wrong-role
- [ ] Seed test users across all four roles

## 5. Field validation service
- [ ] Central FieldValidationService - enum, max length, immutable fields
- [ ] Wire into all write endpoints, before persistence
- [ ] Hierarchy rules (REQ-2), config/window rules (REQ-3/REQ-4)

## 6. Wire CRUD through validation + tiering
- [ ] Role check -> validation -> direct persist (Tier 1) or edit_request (Tier 2)
- [ ] GET/approve/reject edit-request endpoints, approver-only

## 7. Audit trail
- [ ] audit_log write on every applied edit, one row per changed field
- [ ] Confirm post-commit timing relative to edit transaction
- [ ] No update/delete endpoint for audit_log

## 8. Filtered audit read API
- [ ] GET /audit-history with templateId, tableName, dateFrom, dateTo
- [ ] Manual verification across filter combinations

## Definition of done for Phase 1
- [ ] All eight sections checked
- [ ] ./gradlew clean build passes
- [ ] Integration test per domain object
- [ ] End-to-end manual run: campaign -> parent template -> child template
      -> config -> window -> Tier 2 edit -> Tier 1 approve -> audit history

## Deferred to later phases
- Collision checks, shadowed-template detection -> Phase 2
- Job/campaign execution, delivery, guardrails -> Phase 3
- Cognito, Secrets Manager, real deployment -> Phase 4
- Any LLM-backed feature -> Phase 5
