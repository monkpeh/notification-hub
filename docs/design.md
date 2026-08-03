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
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
  datasource:
    url: jdbc:postgresql://localhost:5433/notifyhub
    username: notifyhub
    password: localdev
  flyway:
    enabled: true

ddl-auto: validate is deliberate - Hibernate fails loudly if the entity
mapping and migration-created schema ever drift, instead of silently
altering the table.

## Schema and migration order (FK dependency order)

V1  campaign
V2  template (self-referencing parent_template_id)
V3  template_config
V4  comm_window
V5  app_role, app_user (seeds four baseline roles)
V6  edit_request
V7  audit_log
V8  add AI_AGENT role
V9  add change_batch_id to audit_log
V10 add target_schema (edit_request, audit_log) + approved_with_override
    (edit_request)
V11 create dev schema + business tables (campaign, template,
    template_config, comm_window only)

All eleven apply before any @Entity class is written, so entities are
mapped against the final Phase 1 schema once, not retrofitted later.

V8__add_ai_agent_role.sql:
  INSERT INTO app_role (name) VALUES ('AI_AGENT');

V9__add_change_batch_id_to_audit_log.sql:
  ALTER TABLE audit_log ADD COLUMN change_batch_id UUID NOT NULL
    DEFAULT gen_random_uuid();

  Note: the column default only guarantees a non-null value for one-off
  inserts. The actual grouping behavior comes from application code -
  generate one UUID at the start of processing an edit action, and reuse
  that same value for every audit row written during that action.
  gen_random_uuid() is native to Postgres 13+, no extension needed.

V10__add_governance_columns.sql:
  ALTER TABLE edit_request ADD COLUMN target_schema VARCHAR(20)
    NOT NULL DEFAULT 'public';
  ALTER TABLE edit_request ADD COLUMN approved_with_override BOOLEAN
    NOT NULL DEFAULT false;
  ALTER TABLE audit_log ADD COLUMN target_schema VARCHAR(20)
    NOT NULL DEFAULT 'public';

V11__create_dev_schema.sql:
  CREATE SCHEMA IF NOT EXISTS dev;

  CREATE TABLE dev.campaign (
      id                BIGSERIAL PRIMARY KEY,
      name              VARCHAR(150) NOT NULL,
      business_purpose  VARCHAR(255),
      status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
      owner             VARCHAR(100),
      created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
      updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
  );

  CREATE TABLE dev.template (
      id                    BIGSERIAL PRIMARY KEY,
      campaign_id           BIGINT NOT NULL REFERENCES dev.campaign(id),
      parent_template_id    BIGINT REFERENCES dev.template(id),
      is_parent             BOOLEAN NOT NULL DEFAULT false,
      template_name         VARCHAR(150) NOT NULL,
      template_description  VARCHAR(255),
      customer_type         VARCHAR(20),
      language              VARCHAR(10),
      priority              INT,
      event_type            VARCHAR(50),
      status                VARCHAR(1) NOT NULL DEFAULT 'Y',
      created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
      updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
  );

  CREATE TABLE dev.template_config (
      id                     BIGSERIAL PRIMARY KEY,
      template_id            BIGINT NOT NULL REFERENCES dev.template(id),
      communication_medium   VARCHAR(20) NOT NULL,
      external_template_id   VARCHAR(100),
      use_active             BOOLEAN NOT NULL DEFAULT false,
      contact_flow_id        VARCHAR(100),
      created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
  );

  CREATE TABLE dev.comm_window (
      id            BIGSERIAL PRIMARY KEY,
      config_id     BIGINT NOT NULL REFERENCES dev.template_config(id),
      start_window  TIME NOT NULL,
      end_window    TIME NOT NULL,
      occurrence    VARCHAR(20)
  );

  -- app_role, app_user, edit_request, audit_log are intentionally NOT
  -- duplicated - identity and governance stay single-instance in public,
  -- shared across both targets. Only business data is schema-scoped.

  -- Written as explicit DDL rather than
  -- "CREATE TABLE dev.x (LIKE public.x INCLUDING ALL)" deliberately:
  -- Postgres's LIKE clause does not carry over foreign key constraints,
  -- which would silently produce a dev schema with no referential
  -- integrity. Explicit DDL avoids that trap.

## Multi-target schema architecture

public = live, dev = dev. The default schema is kept as-is rather than
renamed - renaming Postgres's default schema adds risk for no benefit.
Both names are used literally in code and in the target_schema columns.

This mirrors what MariaDB called "databases" at the internship - in
Postgres terms, that same same-server, same-connection, atomically
writable behavior is what schemas provide, not what Postgres calls
databases (which are hard connection-level boundaries).

Implementation: Hibernate's SCHEMA-based multi-tenancy.

TenantContext (ThreadLocal holder, request-scoped):
  public class TenantContext {
      private static final ThreadLocal<String> CURRENT =
          ThreadLocal.withInitial(() -> "public");
      public static void set(String schema) { CURRENT.set(schema); }
      public static String get() { return CURRENT.get(); }
      public static void clear() { CURRENT.remove(); }
  }

A servlet filter reads an X-Edit-Target header ("dev" or absent/anything
else defaults to "public"), sets TenantContext at request start, clears
it in a finally block at request end.

CurrentTenantIdentifierResolver:
  public class SchemaTenantResolver
      implements CurrentTenantIdentifierResolver<String> {
      public String resolveCurrentTenantIdentifier() {
          return TenantContext.get();
      }
      public boolean validateExistingCurrentSessions() { return true; }
  }

MultiTenantConnectionProvider (schema-switching, not per-tenant
datasources - one DataSource, one connection pool, schema set per
checkout):
  public class SchemaMultiTenantConnectionProvider
      implements MultiTenantConnectionProvider<String> {
      @Autowired private DataSource dataSource;

      public Connection getAnyConnection() throws SQLException {
          return dataSource.getConnection();
      }
      public Connection getConnection(String tenantIdentifier)
          throws SQLException {
          Connection c = getAnyConnection();
          c.setSchema(tenantIdentifier);
          return c;
      }
      public void releaseConnection(String tenantIdentifier,
          Connection connection) throws SQLException {
          connection.setSchema("public");
          connection.close();
      }
      // + releaseAnyConnection, isUnwrappableAs, unwrap,
      //   supportsAggressiveRelease boilerplate
  }

Spring Boot does not auto-configure Hibernate multi-tenancy - this needs
an explicit LocalContainerEntityManagerFactoryBean (or a
HibernatePropertiesCustomizer) setting hibernate.multi_tenant=SCHEMA plus
the two beans above. Full wiring happens in tasks.md Section 9, once
entities exist to test it against.

Because this is one connection with search_path switched per checkout,
an edit touching multiple business tables within one target still
commits/rolls back atomically as a single transaction - no distributed
transaction machinery needed, which was the whole point of choosing this
over separate databases.

## Rate limiting design

In-memory sliding window per app_user, scoped to edit submission only
(not reads, not approve/reject). ConcurrentHashMap<Long, Deque<Instant>>
keyed by user id: on submit, drop timestamps older than 1 hour from the
deque, reject with 429 if remaining count >= the role's limit, otherwise
push now() and allow.

Single-JVM only for Phase 1 - fine for local dev with one instance. If
this ever needs to survive multiple instances, that's the same Redis
already planned for Phase 4 (ElastiCache), not a new decision.

## Stale-edit conflict detection design

On GET /edit-requests: for each pending request, fetch the current live
value of the target field (schema-qualified by the request's
target_schema) and compare to the row's stored old_value. Mismatch =
mark isStale=true in the response DTO. Computed on read, not persisted.

On POST /edit-requests/{id}/approve: re-check staleness at approval time.
Stale + no force param -> 409 Conflict, body includes currentValue,
requestedOldValue, requestedNewValue. Stale + force=true -> apply anyway,
set approved_with_override=true on the edit_request row.

## Package structure

com.notifyhub.notifyhub
  campaign/     CampaignEntity, repository, service, controller
  template/     TemplateEntity (self-referencing), repository, service
  config/       TemplateConfigEntity, repository, service
  window/       CommWindowEntity, repository, service
  user/         AppUserEntity, AppRoleEntity, repositories
  editrequest/  EditRequestEntity, ApprovalService, stale-check logic
  audit/        AuditLogEntity, AuditRepository, filtered read API
  security/     AuthFilter, role-check guard, RateLimiterService,
                TenantContext, SchemaTenantResolver, JWT (Cognito Phase 4)
  common/       FieldValidationService, ValidationError DTO,
                alias-mapping registry, shared DTOs

## API surface (Phase 1)

POST /campaigns                              Tier 1
GET  /campaigns/{id}                         any authenticated
POST /templates                              Tier 1 direct / Tier 2 pending
PUT  /templates/{id}                         Tier 1 direct / Tier 2 pending
POST /templates/{id}/configs                 Tier 1 direct / Tier 2 pending
POST /configs/{id}/windows                    Tier 1 direct / Tier 2 pending
GET  /edit-requests?status=&templateId=       APPROVER/ADMIN/SUPER_ADMIN
POST /edit-requests/{id}/approve?force=       APPROVER/ADMIN/SUPER_ADMIN
POST /edit-requests/{id}/reject               APPROVER/ADMIN/SUPER_ADMIN
GET  /audit-history?templateId=&tableName=&targetSchema=&dateFrom=&dateTo=
     any authenticated

All write endpoints accept an optional X-Edit-Target header (public/dev,
default public).

## Transaction boundary note

Any post-edit side effect that reads its own write (cache refresh, derived
aggregation) must happen after the edit's own transaction commits - never
inside the same @Transactional method. Put such calls in the controller
layer, not the service layer.

## Authentication placeholder

Phase 1 uses a minimal custom AuthFilter reading a pid header, resolved to
an app_user row. Swappable for Cognito in Phase 4 without touching the RBAC
guard logic.

## camelCase / snake_case alias convention

Every entity in this project follows the same rule: Java fields are
camelCase, database columns are snake_case, and the mapping between them
is always explicit via @Column(name = "..."). There is no reliance on
Hibernate's default naming strategy to infer the conversion.

Example: TemplateEntity.templateName maps to template.template_name via
@Column(name = "template_name") - not left to Hibernate's
ImplicitNamingStrategy/PhysicalNamingStrategy to guess.

Why explicit over automatic: an implicit strategy works fine until a
column name doesn't follow the expected pattern (e.g. an existing column
abbreviated unusually, or a future migration adding a column with a name
that doesn't cleanly reverse-convert). Explicit @Column names mean the
mapping is always visible in the entity itself, and a schema mismatch
surfaces immediately via ddl-auto: validate rather than silently mapping
to the wrong column or failing in a way that's hard to trace back to the
naming strategy.

This is why every entity in this project explicitly names every column,
even when the camelCase-to-snake_case conversion would have been
unambiguous either way.
