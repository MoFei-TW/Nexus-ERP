# db-creator.md - MSSQL 資料庫架構師與自動建庫技能

本技能作為 AI Agent 執行 Microsoft SQL Server (MSSQL) 資料庫架構設計、Schema 推論、互動確認門禁把關、資料庫建立與 `MF-schema.md` 文件生成的專業技能。

詳細完整技能規格亦已同步於 `.agents/skills/db-creator/SKILL.md`。

---

## 1. 目標資料庫伺服器與連線配置

* **Database Engine**: Microsoft SQL Server (MSSQL)
* **Host / Server**: `localhost`
* **User (Account)**: `sa`
* **Password**: `0000000`
* **Target Database Name**: `MF`

---

## 2. 核心業務實體

參照 `2026-09-18-02-MF-資料庫-資料表-欄位設定` 涵蓋之 5 大實體：
1. **員工主檔 (`employee`)**
2. **產品主檔 (`product`)**
3. **客戶主檔 (`customer`)**
4. **訂單主檔 (`orders`)**
5. **訂單明細 (`order_detail`)**

---

## 3. 三階段執行流程 (Execution Lifecycle)

### 階段一：Schema 設計推論與最佳實踐說明（待使用者確認門禁）
1. **MSSQL 資料型態精準映射**：
   - 變長字串：`NVARCHAR(n)`。
   - 固定/代碼字串：`VARCHAR(n)` / `CHAR(n)`。
   - 布林值：`BIT`。
   - 日期時間：`DATETIME2`（嚴禁舊式 `DATETIME`）。
   - 數值/金額：`DECIMAL(18, 2)`。
2. **約束與限制 (Constraints)**：
   - Primary Key、Foreign Key、Unique Key、Default Value、NOT NULL。
   - 業務邏輯 CHECK 約束：電話格式 `0912-345-678`、Email 格式、統一編號格式 `8888-8888`、金額與數量 `>= 0` / `> 0`。
3. **安全防護與歷史快照**：
   - 密碼儲存長度預留 `VARCHAR(100)`（供 BCrypt/Argon2 雜湊儲存，嚴禁明文）。
   - 歷史快照機制：訂單收件人姓名/電話/地址快照、明細商品名稱與成交單價快照。

> ⚠️ **確認門禁 (Approval Gate)**：
> 輸出階段一 Schema 說明與 SQL 規劃後，**必須暫停並等待使用者明確回覆「確認無誤，開始建立資料庫」**，方可進入階段二。

---

### 階段二：資料庫與資料表建立 (DB & Table Creation)
1. 連線至 `localhost` (sa / 0000000)。
2. 若 `MF` 資料庫不存在，則執行 `CREATE DATABASE [MF]`。
3. 切換至 `MF` 資料庫，並依相依順序建立資料表（`employee` -> `product` & `customer` -> `orders` -> `order_detail`）。

---

### 階段三：產出完整 Schema 報告與視覺化文件 (`MF-schema.md`)
建立完成後，生成符合 Markdown 規範之架構文件 `MF-schema.md`，包含：
1. **整體概念架構說明 (Database Conceptual Architecture)**：設計理念、實體關係、資安防護與歷史快照防失真機制。
2. **ERD 關聯圖 (Mermaid Tool)**：`mermaid` 語法 (`erDiagram`)，標示 PK、FK 與基數（1:1, 1:N, N:M）。
3. **完整 Data Dictionary (資料字典說明表格)**：5 張表之獨立說明表格（欄位名稱、型態、PK/FK、Nullability、預設值、約束/驗證規則、說明與業務邏輯）。
4. **完整 DDL 建立腳本 (Append Code Block)**：含 `IF NOT EXISTS` 判斷可重複執行之 T-SQL 完整程式碼區塊。
