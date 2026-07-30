# CLAUDE.md — Steering doc for this repo

This file is read automatically by Claude Code at the start of every session in this
project. Keep it current — it's the difference between Claude re-deriving context
every session and Claude picking up exactly where you left off.

## Project

**Notification Orchestration Platform** — a personal-project recreation of an
enterprise template-management system, extended with a campaign -> job -> delivery
execution tier, AWS infrastructure, and an AI layer.

Full phase-by-phase roadmap: docs/notification_platform_roadmap.md
Current phase spec: docs/requirements.md, docs/design.md, docs/tasks.md

**Current phase: Phase 1 — Foundation (schema, CRUD, RBAC, audit trail)**
Update this line as phases change. Don't let it silently go stale.

## Tech stack

- Java 21, Spring Boot 3, Gradle
- PostgreSQL (local via Docker Compose, port 5433 - see docs/design.md for why)
- Flyway for migrations
- Spring Data JPA / Hibernate

## Repo conventions

- Migrations are the only way schema changes happen. Never hand-edit the dev
  database. Every schema change is a new V{n}__description.sql file in
  src/main/resources/db/migration. Never edit an already-committed migration -
  add a new one.
- Package structure: com.jkmonkpeh.notifyhub.{campaign,template,config,window,
  user,editrequest,audit,security,common} - one package per domain concept.
- Entity fields use camelCase; columns use snake_case. Alias mapping is
  centralized in common/, not repeated per entity.
- Commit messages: "phase1: add campaign entity and repository" - phase
  prefix, lowercase, imperative mood.
- app_user not user as the actor table name - USER is reserved in Postgres.

## How Claude should work in this repo

1. Check docs/tasks.md before starting anything. Work top to bottom - the
   order encodes real dependencies, not an arbitrary checklist.
2. One task at a time. Implement the smallest next task, run tests, then
   stop and report back rather than chaining ahead unprompted.
3. A task isn't done until: the relevant migration applies cleanly from
   scratch, unit tests pass, and there's a note of what was verified.
4. Ask before adding a new dependency to build.gradle.
5. Don't jump ahead to a later phase's concerns - note them under
   "Deferred" in tasks.md instead.
6. When a design decision isn't covered in docs/design.md, propose it,
   state the tradeoff in a sentence or two, and ask before implementing.

## Build & test

./gradlew clean build       # compile + unit tests
./gradlew bootRun           # run locally against docker-compose Postgres
docker compose up -d        # start local Postgres

## Definition of done, overall

A phase is complete when every item in that phase's tasks.md is checked,
the app runs end-to-end locally against Docker Compose Postgres, and
there's at least one integration test per new capability against a real
database.
