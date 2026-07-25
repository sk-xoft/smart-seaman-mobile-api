---
name: database-schema-maintainer
description: Maintain this repository's MySQL schema scripts and synchronized database documentation. Use when adding or changing tables, columns, keys, constraints, indexes, seed data, drop scripts, schema Markdown, or Mermaid/table diagrams under documents/mvp1.
---

# Database Schema Maintainer

Treat executable SQL scripts as the source of truth. Preserve unrelated user changes.

## Workflow

1. Read the relevant requirements and inspect all related tables before editing.
2. Check existing column types and collations on both sides of every foreign key.
3. Update `documents/mvp1/script/01_create_mvp1_tables.sql` for fresh-install DDL.
4. Update `02_seed_mvp1_master_data.sql` for repeatable master data and `03_create_core_indexes.sql` only for indexes intentionally applied separately.
5. Update `00_drop_mvp1_tables.sql` in reverse dependency order. Do not disable safety beyond its existing `FOREIGN_KEY_CHECKS` wrapper.
6. Synchronize `documents/mvp1/db_script_new_feature_v1.md` and any diagram explicitly in scope. Copy DDL exactly; keep business notes concise.
7. Validate formatting, dependency order, constraints, and documentation parity.

## Schema Rules

- Target MySQL 8.0+ and follow existing `InnoDB`, `utf8mb4`, and collation conventions.
- Use UUID `CHAR(36)` keys where new MVP1 tables use UUIDs.
- Name indexes and constraints uniquely and consistently with neighboring DDL.
- Add indexes for foreign keys and demonstrated query/filter patterns; avoid redundant indexes.
- Use `DECIMAL` for money, never floating-point types.
- Make seed scripts rerunnable with the repository's existing upsert convention.
- Do not invent `ON DELETE CASCADE`; preserve audit and financial history unless requirements explicitly demand cascading deletion.
- Flag schema defects found in the source SQL instead of silently changing business semantics while synchronizing docs.

## Verification

- Run `git diff --check`.
- Compare every documented `CREATE TABLE` block with `01_create_mvp1_tables.sql` table-by-table.
- Search for stale table, column, constraint, and singular/plural names.
- If a MySQL runtime is available, execute against an isolated disposable schema; never run DDL against a shared environment without explicit authorization.
- Report changed scripts, documentation, validations run, and anything not executable locally.
