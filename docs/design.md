# Phase 1 Design — Foundation

## Database choice: PostgreSQL

Chosen over MariaDB because Phase 3 needs JSONB for job.template_snapshot
and Phase 5 needs pgvector for semantic search. Better to pay the
engine-switch cost once, now, while the schema is small.

## Local dev environment

Postgres runs via docker-compose.yml on port 5433 (not 5432) because this
Mac already has a system-wide Postgres 18 bound to 5432 via
postgresql-18.plist in /Library/LaunchDaemons.

application.yml:

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/notifyhub
    username: notifyhub
    password: localdev
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

ddl-auto: validate is deliberate - Hibernate fails loudly if the entity
mapping and migration-created schema ever drift, instead of silently
altering the table.

## Schema and migration order (FK dependency order)

V1 campaign
V2 template (self-referencing parent_template_id)
V3 template_config
V4 comm_window
V5 app_role, app_user (seeds the four baseline roles)
V6 edit_request
V7 audit_log

See src/main/resources/db/migration for the actual SQL.

## Package structure

com.jkmonkpeh.notifyhub
  campaign/     CampaignEntity, repository, service, controller
  template/     TemplateEntity (self-referencing), repository, service
  config/       TemplateConfigEntity, repository, service
  window/       CommWindowEntity, repository, service
  user/         AppUserEntity, AppRoleEntity, repositories
  editrequest/  EditRequestEntity, ApprovalService
  audit/        AuditLogEntity, AuditRepository, filtered read API
  security/     AuthFilter, role-check guard, JWT handling (Cognito in Phase 4)
  common/       FieldValidationService, alias-mapping registry, shared DTOs

## API surface (Phase 1)

POST /campaigns                              Tier 1
GET  /campaigns/{id}                         any authenticated
POST /templates                              Tier 1 direct / Tier 2 pending
PUT  /templates/{id}                         Tier 1 direct / Tier 2 pending
POST /templates/{id}/configs                 Tier 1 direct / Tier 2 pending
POST /configs/{id}/windows                    Tier 1 direct / Tier 2 pending
GET  /edit-requests?status=&templateId=       APPROVER/ADMIN/SUPER_ADMIN
POST /edit-requests/{id}/approve              APPROVER/ADMIN/SUPER_ADMIN
POST /edit-requests/{id}/reject               APPROVER/ADMIN/SUPER_ADMIN
GET  /audit-history?templateId=&tableName=&dateFrom=&dateTo=   any authenticated

## Transaction boundary note

Any post-edit side effect that reads its own write (cache refresh, derived
aggregation) must happen after the edit's own transaction commits - never
inside the same @Transactional method. Put such calls in the controller
layer, not the service layer.

## Authentication placeholder

Phase 1 uses a minimal custom AuthFilter reading a pid header, resolved to
an app_user row. Swappable for Cognito in Phase 4 without touching the RBAC
guard logic.
