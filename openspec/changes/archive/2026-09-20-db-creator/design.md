# Design: MSSQL Database Creator Skill (db-creator)

## Context

See `proposal.md` for motivation.
The workspace enforces AI Agent governance rules (`.agents/skills/`, `openspec/`, `AGENT.md`). The target database environment is Microsoft SQL Server (MSSQL) on `localhost` (sa / 0000000), target database `MF`.
Five business entities need to be modeled and provisioned:
1. `Employee` (員工主檔)
2. `Product` (產品主檔)
3. `Customer` (客戶主檔)
4. `Order` (訂單主檔)
5. `OrderDetail` (訂單明細)

## Goals / Non-Goals

**Goals:**
- Provide a standardized Agent Skill `db-creator` at `.agents/skills/db-creator/SKILL.md` (and `.agents/skills/db-creator.md`) as a Senior Database Administrator & MSSQL Architect.
- Enforce strict MSSQL data type mapping (NVARCHAR, VARCHAR/CHAR, BIT, DATETIME2, DECIMAL(18,2)).
- Enforce business logic CHECK constraints (phone, email, unified business number / tax ID, non-negative amounts, positive quantities).
- Implement a 3-phase execution lifecycle with an explicit approval gate before executing SQL.
- Generate idempotent T-SQL DDL (`IF NOT EXISTS`) respecting foreign key dependencies.
- Produce comprehensive architecture documentation `MF-schema.md` (Conceptual Architecture, Mermaid ERD, Data Dictionary tables, and DDL script block).

**Non-Goals:**
- Developing backend application code (Spring Boot Java entities, DTOs, mappers, services).
- Populating production or mock data into database tables.
- Provisioning remote or cloud databases outside `localhost`.

## Decisions

### 1. Skill Placement and Format
- **Decision**: Create `.agents/skills/db-creator/SKILL.md` with standard YAML frontmatter (name, description), and mirror/link `.agents/skills/db-creator.md` to ensure compatibility with workspace skill discovery.
- **Rationale**: Follows modern Antigravity and OpenSpec skill conventions, allowing structured instructions, examples, and future resource expansion.

### 2. MSSQL Data Types and Constraints Mapping
- **Decision**:
  - `Employee.EmpId`: `CHAR(7)` (PK, format: YYYY + 3-digit serial).
  - `Product.ProdId`: `VARCHAR(7)` (PK, format: YYYY + 2-digit serial + 1-digit letter).
  - `Customer.CustId`: `VARCHAR(9)` (PK, format: YYYYMM + 3-digit serial).
  - `Order.OrderId`: `VARCHAR(25)` (PK, format: e.g. YYYYMMDD-XXXX).
  - `OrderDetail.DetailId`: `BIGINT IDENTITY(1,1)` (PK).
  - Monetary values (`Product.Price`, `Order.TotalAmount`, `OrderDetail.UnitPrice`, `OrderDetail.DiscountAmount`, `OrderDetail.SubtotalAmount`): `DECIMAL(18, 2)`.
  - Strings: `NVARCHAR` for multilingual text (names, addresses, titles); `VARCHAR` for formatted codes and hashes.
  - Passwords: `VARCHAR(100)` to securely store BCrypt/Argon2 hashes.
  - Audit fields: `CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()`, `UpdatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()`.
- **Alternatives Considered**: Using `DATETIME` instead of `DATETIME2` — rejected due to lower precision and legacy status in modern MSSQL.

### 3. Approval Gate Mechanism
- **Decision**: Phase 1 must present the complete schema inference and SQL preview, then halt execution. The Agent must not proceed to Phase 2 until receiving the exact user confirmation: `"確認無誤，開始建立資料庫"`.
- **Rationale**: Prevents unintentional schema changes and guarantees DBA oversight before any database modification.

### 4. Foreign Key Dependency and Execution Order
- **Decision**: Table creation order:
  1. `Employee` (獨立無前置外鍵)
  2. `Product` (FK -> Employee) & `Customer` (FK -> Employee)
  3. `Order` (FK -> Customer, Employee)
  4. `OrderDetail` (FK -> Order, Product)
- **Rationale**: Prevents foreign key reference errors during execution.

### 5. Snapshot Mechanism for Historical Integrity
- **Decision**: `Order` stores `RecipientName`, `RecipientPhone`, `RecipientAddress` snapshots; `OrderDetail` stores `ProductNameSnapshot` and `UnitPriceSnapshot`.
- **Rationale**: Prevents historical transaction discrepancies if customer contact info or product catalog prices change in the future.

## Risks / Trade-offs

- **[Risk: Local MSSQL server offline or credentials invalid]** → Skill includes connectivity testing and clear diagnostic messages before attempting DDL execution.
- **[Risk: Rerun overwriting existing data]** → DDL uses `IF NOT EXISTS` checks and avoids destructive `DROP TABLE` operations.
- **[Risk: Inconsistent manual schema edits]** → `MF-schema.md` serves as the single source of truth for the database schema, synchronized with the DDL script.
