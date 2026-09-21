# Proposal: MSSQL Database Creator Skill (db-creator)

## Why

Currently, database schema modeling, MSSQL database provisioning, approval verification, and architectural documentation (ERD & Data Dictionary) require manual intervention and lack a standardized AI Agent workflow. To support the "MF" enterprise system and adhere to project governance, we need an automated `db-creator` skill that guides database creation through safe approval gates and generates comprehensive architecture artifacts (`MF-schema.md`).

## What Changes

- Create a new Agent Skill `db-creator` under `.agents/skills/db-creator/SKILL.md` acting as a Senior Database Administrator & MSSQL Architect.
- Define a 3-phase execution lifecycle within the skill:
  - **Phase 1: Schema Design Inference & Best Practices Explanation**: Infer full schemas for 5 core tables (`Employee`, `Product`, `Customer`, `Order`, `OrderDetail`) based on business requirements, data type mappings (NVARCHAR, BIT, DATETIME2, DECIMAL), constraints (PK/FK/Unique/Check), and security practices (password hashing, default timestamps). Includes a mandatory user approval gate ("確認無誤，開始建立資料庫") before executing SQL.
  - **Phase 2: Database & Table Creation**: Connect to MSSQL at `localhost` (sa / 0000000), create the `MF` database if not exists, and execute idempotent DDL in foreign key dependency order (Employee -> Product/Customer -> Order -> OrderDetail).
  - **Phase 3: Schema Documentation & Visualization**: Generate `MF-schema.md` containing conceptual architecture, Mermaid ERD, comprehensive data dictionary tables, and complete T-SQL DDL scripts.

## Capabilities

### New Capabilities
- `db-creator`: MSSQL Database Administrator & Architect agent skill for schema inference, approval gating, database provisioning, and architectural documentation (`MF-schema.md`).

### Modified Capabilities
<!-- Existing capabilities whose REQUIREMENTS are changing. None in this change. -->

## Impact

- **Skills**: Adds `.agents/skills/db-creator/SKILL.md`.
- **Database**: Targets Microsoft SQL Server (`localhost`, user `sa`, database `MF`).
- **Artifacts**: Generates `MF-schema.md` upon skill invocation.
- **Workflow**: Adheres to OpenSpec proposal/apply lifecycle and safety approval gates.
