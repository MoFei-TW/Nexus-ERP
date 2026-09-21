# Design

## Context

MOFEI 企業需要構建一套高安全性、純原生離線運行、極簡黑白灰視覺風格的 ERP 系統。相關業務背景與需求動機詳見 `proposal.md`。

本設計遵循 `AGENT.md` 之最高架構守則與專案規格：
- **後端環境**：Java 21 (LTS)、Spring Boot 3.2.x+（啟用虛擬線程）、MyBatis-Plus 3.5.x、MSSQL (`localhost`, `sa`, `0000000`, Database: `MOFEI`)。
- **後端分層架構**：PO 實體類、Java 21 `record` DTO/VO、MyBatis-Plus BaseMapper、Service 業務層（事務控管）、Controller（REST API 統一回傳 `R<T>`、頁面路由回傳 Thymeleaf）。
- **前端架構**：Thymeleaf 樣板引擎、純原生 JavaScript (ES6+ Vanilla JS, `fetch`, `async/await`)、純原生 CSS3、零外部 UI 框架、零 CDN 引用、100% 離線運行。
- **資安架構**：Spring Security / RBAC（Admin vs Employee）、Cookie 三大本柱 (`HttpOnly=true`, `Secure=true`, `SameSite=Strict`)、密碼加密存儲、OWASP API Top 10 防護。

## Goals / Non-Goals

**Goals:**
- **MSSQL 資料庫與實體對齊**：建立 `MOFEI` 資料庫，包含員工主檔 (`employee`)、產品主檔 (`product`)、客戶主檔 (`customer`)、訂單主檔 (`orders`)、訂單明細 (`order_detail`)，實體欄位命名 100% 一致。
- **4 大主檔 CRUD 模組**：
  - 員工主檔：支援在職狀態切換（軟刪除保護歷史訂單外鍵）。
  - 產品主檔：產品編號、名稱、規格、價格、庫存管理。
  - 客戶主檔：客戶編號、名稱、聯絡人、電話、地址管理。
  - 訂單主檔與明細：主從關聯交易、金額計算、訂單狀態追蹤。
- **查詢與分頁機制**：支援編號與名稱之複合查詢、即時前端模糊搜尋、固定每頁 10 筆之分頁控制。
- **全站 Layout 與極簡黑白灰風格**：
  - 頂端導覽列：載入 `images/logo/2026-09-10-00-logo by MF.jpg`、動態即時時鐘（`YYYY-MM-DD HH:mm:ss`）、4 大主檔跳轉連結。
  - 頁尾：顯示 `@2026 DataBase by MOFEI`。
  - 零色彩渲染：採用純黑白灰階 CSS 設計系統（CSS Variables: `--bg-primary`, `--text-primary`, `--border-color`, `--card-bg`）。
- **營運儀表板**：首頁即時顯示員工、產品、客戶、訂單之關鍵統計指標。
- **安全性與 RBAC 門禁**：
  - `/login` 與 `/logout` 機制，預設加密管理員 `admin` / `admin`。
  - 未登入用戶可檢視 UI 介面與按鈕觸發效果，但後端 CRUD API 嚴格攔截 (401 Unauthorized)。
  - 階層式授權：Admin (所有模組) vs Employee (業務模組)。

**Non-Goals:**
- 不引入外部前端框架（如 React, Vue, Bootstrap, Tailwind）或 Node.js/npm 建置流程。
- 不引入外部 CDN 資源（圖示、字型、第三方 JS 函式庫均內置或使用原生語意符號）。
- 不實作複雜的多租戶架構或分散式微服務拆分（維持高內聚之模組化單體）。
- 不介接外部金流或第三方物流 API。

## Decisions

### 1. 資料庫連線與持久層設計
- **決策**：採用 MSSQL 專用 JDBC 驅動 (`com.microsoft.sqlserver:mssql-jdbc`) 與 MyBatis-Plus 3.5.x，連線配置於 `application.yml`。
- **備選方案**：MySQL 或 H2。
- **理由**：規格書明確指定 DBMS 為 MSSQL (`localhost`, `sa`, `0000000`)，資料庫名稱為 `MOFEI`。使用 MyBatis-Plus 簡化單表 CRUD，複雜關聯以 XML/註解 SQL 處理。

### 2. 前端極簡離線架構與 CSS 變數設計系統
- **決策**：全站採用單一原生 CSS 檔案 (`/static/css/mofei-style.css`) 定義黑白灰調色盤與組件樣式，不引用任何外部框架。
- **備選方案**：Bootstrap 或 Tailwind CDN。
- **理由**：`AGENT.md` 與 Plan F 明確禁止任何外部 UI 框架與 CDN 資源，要求 100% 離線運行與零色彩極簡黑白灰渲染。使用 CSS 變數可集中管理排版、間距、按鈕與表格樣式。

### 3. 員工主檔刪除保護機制 (軟刪除 / 狀態控制)
- **決策**：在 `employee` 資料表設置 `status` 欄位（例如：`ACTIVE` 在職 / `RESIGNED` 離職）。刪除操作僅更新狀態，避免實體物理刪除。
- **備選方案**：資料庫級別級聯刪除 (Cascade Delete) 或外鍵設為 NULL。
- **理由**：歷史訂單需要關聯原經辦員工，若物理刪除會導致歷史資料斷鏈或外鍵約束異常。

### 4. 動態即時時鐘實作機制
- **決策**：在前端 `layout.html` 內嵌原生 JavaScript，利用 `setInterval` 每一秒抓取目前時間並格式化為 `YYYY-MM-DD HH:mm:ss`，動態更新 DOM 節點。
- **備選方案**：WebSocket / SSE 伺服器推播。
- **理由**：客戶端計時無額外伺服器負載與網路開銷，簡單可靠且完全離線可用。

### 5. 認證與未登入唯讀限制
- **決策**：Spring Security 結合自訂 Authentication Filter / Interceptor：
  - 靜態資源與前端 HTML 視圖允許匿名存取（GET `/`、`/dashboard`、`/employee` 等），前端可展示 mock/預覽狀態。
  - 後端 RESTful API（POST/PUT/DELETE `/api/**`）強制要求驗證；未認證請求直接回應 401 JSON。
  - Cookie 設置 `HttpOnly=true; Secure=true; SameSite=Strict`。
- **備選方案**：完全封鎖未登入使用者的所有頁面訪問。
- **理由**：規格書 Plan G 明確要求：「未登入狀態下禁止執行計畫 B 之 CRUD 後端實際操作，僅可檢視前端 UI 與按鈕觸發效果」。

## Risks / Trade-offs

- **[MSSQL 本機連線失敗]** → 若本機 MSSQL 服務未啟動或 TCP/IP 1433 埠未開啟，將導致啟動失敗。
  - *緩解措施*：提供標準 SQL DDL 初始化腳本 (`schema.sql` 與 `data.sql`)，並於應用程式連線池設定合理逾時與健康檢查。
- **[純原生 JS 狀態管理與分頁]** → 無前端框架輔助時，分頁與搜尋的 DOM 更新較為繁瑣。
  - *緩解措施*：封裝輕量化通用原生模組 `mofei-table.js`，統一處理 `fetch`、表格渲染、模糊搜尋過濾與 10 筆分頁切換。
- **[密碼安全與敏感資訊洩漏]** → DTO 若不慎洩漏密碼欄位將違反 OWASP 資安規範。
  - *緩解措施*：使用 Java 21 `record` 明確宣告對外 DTO/VO，完全排除 `password` 與 `salt` 屬性；Controller 統一回傳 VO。

## Migration Plan

1. 執行 MSSQL 初始化腳本，建立 `MOFEI` 資料庫及 5 大資料表。
2. 建立 Spring Boot 後端模組與 Entity/Mapper/Service/Controller。
3. 建立前端 `layout.html`、CSS 樣式系統與 4 大主檔/Dashboard/Login 視圖。
4. 啟動應用程式並驗證 RBAC、CRUD、分頁與即時時鐘功能。
