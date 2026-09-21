# Spec Delta

## Purpose

Defines the system behavior, execution lifecycle, approval gates, and architectural documentation standards for the `db-creator` Agent Skill targeting Microsoft SQL Server (MSSQL).

## ADDED Requirements

### Requirement: db-creator 技能定義與角色設定
系統 SHALL 提供名為 `db-creator` 的 Agent Skill 檔案（路徑為 `.agents/skills/db-creator/SKILL.md`，可同時支援 `.agents/skills/db-creator.md`），角色設定為「資深 MSSQL 資料庫架構師（Senior Database Administrator & MSSQL Architect）」，定義其連線目標、欄位解析、三階段執行生命週期與品質產出規範。

#### Scenario: 載入 db-creator 技能
- **WHEN** AI Agent 接收到建立、推論、執行或輸出 MF 資料庫架構之需求
- **THEN** Agent 載入 `db-creator` 技能，遵循其架構師角色與三階段執行流程

### Requirement: 階段一：Schema 設計推論、型態映射與安全防護
系統 SHALL 於階段一依據業務欄位設定（涵蓋員工主檔、產品主檔、客戶主檔、訂單主檔、訂單明細）進行完整 Schema 推論與最佳實踐說明，包含：
1. **MSSQL 資料型態映射**：變長字串映射為 `NVARCHAR`、固定字串映射為 `VARCHAR`/`CHAR`、布林值映射為 `BIT`、日期時間映射為 `DATETIME2`、數值/金額映射為 `DECIMAL(p, s)`。
2. **約束與限制 (Constraints)**：設定 Primary Key、Foreign Key、Unique Key、Default Value、NOT NULL，以及業務邏輯 CHECK 約束（如電話號碼格式 `0912-345-678`、Email 格式、統一編號格式 `8888-8888`、庫存/金額/數量 `>= 0` 或 `> 0` 等）。
3. **安全與維護欄位**：密碼欄位長度預留雜湊演算法儲存空間（如 BCrypt/Argon2 使用 `VARCHAR(100)` 或 `NVARCHAR(128)`），系統時間預設使用 `GETDATE()` 或 `SYSDATETIME()`。

#### Scenario: 產出 Schema 設計預覽說明
- **WHEN** Agent 於階段一解析 5 張實體之業務需求與欄位定義
- **THEN** Agent 輸出結構完整之 Schema 預覽說明，詳細條列型態映射、PK/FK/UK/Default、CHECK 驗證規則與安全雜湊建議

### Requirement: 階段一：強制使用者確認門禁 (Approval Gate)
系統 SHALL 於階段一輸出 Schema 預覽說明後強制暫停執行，進入審查門禁（Approval Gate）。Agent 必須明確提示並等待使用者回覆「確認無誤，開始建立資料庫」，未取得該明確確認指令前，嚴禁執行任何資料庫建立或 SQL DDL 腳本。

#### Scenario: 未獲核可前暫停執行
- **WHEN** Agent 完成階段一 Schema 推論與說明
- **THEN** Agent 暫停執行並主動等待使用者審核，不進行連線執行或資料表建立

#### Scenario: 取得明確核可後進入階段二
- **WHEN** 使用者明確回覆「確認無誤，開始建立資料庫」
- **THEN** Agent 解除門禁並進入階段二執行作業

### Requirement: 階段二：MSSQL 資料庫與資料表建立
系統 SHALL 使用指定連線配置（Host: `localhost`、User: `sa`、Password: `0000000`）連線至 MSSQL Server；若 `MF` 資料庫不存在，則執行 `CREATE DATABASE [MF]`；切換至 `MF` 資料庫後，依外鍵相依性順序（員工主檔 -> 產品主檔/客戶主檔 -> 訂單主檔 -> 訂單明細）依序建立資料表與約束。

#### Scenario: 依序建立資料庫與資料表
- **WHEN** 階段一通過使用者審核進入階段二
- **THEN** Agent 連線至 `localhost` MSSQL，確保 `MF` 資料庫建立完成，並按相依順序建立 5 張資料表與約束

### Requirement: 階段三：產出完整 Schema 報告與視覺化文件 (MF-schema.md)
系統 SHALL 於資料庫建立完成後，生成符合 Markdown 規範之架構文件 `MF-schema.md`，內容必須包含：
1. **整體概念架構說明 (Database Conceptual Architecture)**：資料庫設計理念、核心實體關係簡述、資安防護邏輯（密碼雜湊）與歷史快照機制（如商品名稱快照、單價快照、收件地址快照）。
2. **ERD 關聯圖 (Mermaid Tool)**：使用 `mermaid` 語法 (`erDiagram`) 繪製實體關聯圖，標示 PK、FK 及 1:1, 1:N, N:M 基數關係。
3. **完整 Data Dictionary (資料字典說明表格)**：每張資料表獨立表格，欄位包含：欄位名稱、SQL 資料型態、PK/FK、Nullability、預設值、約束/驗證規則、說明與業務邏輯。
4. **完整 DDL 建立腳本 (Append Code Block)**：提供具備 `IF NOT EXISTS` 判斷可重複執行之完整 T-SQL 腳本。

#### Scenario: 產出結構完整之 MF-schema.md
- **WHEN** 階段二資料表建立完成進入階段三
- **THEN** Agent 生成 `MF-schema.md`，包含概念架構、Mermaid ERD、5 張表之資料字典表格及可重複執行的 T-SQL DDL 腳本區塊
