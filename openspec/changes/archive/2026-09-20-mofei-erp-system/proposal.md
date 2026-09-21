# Proposal

## Why

MOFEI 企業需要一套具備高安全性、離線原生運作能力、架構嚴謹且極簡風格的現代化 ERP 系統（MOFEI ERP 系統網站，代號：2026-09-v1-MOFEI-ERP）。本變更旨在建立完整的 MOFEI ERP 系統規格與計畫，涵蓋 MSSQL 資料庫建置、後端模組化單體架構與 RESTful API、Thymeleaf 與原生 Vanilla JavaScript 前端、RBAC 權限控管與安全機制，以滿足企業對員工、產品、客戶與訂單的全面數位化管理需求。

## What Changes

- **資料庫與實體對齊 (MSSQL)**：建立 `MOFEI` 資料庫，包含員工主檔、產品主檔、客戶主檔、訂單主檔與訂單明細等 5 大資料表，嚴格統一資料庫欄位名稱、後端 Entity/DTO 與前端渲染屬性。
- **4 大主檔 CRUD 模組**：實作員工 (Employee)、產品 (Product)、客戶 (Customer)、訂單 (Order) 的完整 CRUD 功能，支援複合查詢（編號與名稱）、即時模糊搜尋、10 筆每頁分頁機制，以及員工主檔在職狀態軟刪除機制。
- **安全防護與敏感資訊遮蔽**：在視圖與 DTO/VO 中自動過濾並隱蔽密碼、雜湊鹽值等敏感欄位，落實 OWASP 防護。
- **首頁即時監控儀表板**：提供 4 大主檔即時統計與營運狀態監控數據卡片。
- **全局 Layout 與純原生離線前端**：建置公版樣式與 `layout.html`，包含動態即時時鐘（YYYY-MM-DD HH:mm:ss）、Logo 載入、主檔模組導航、頁尾版權宣告；嚴禁外部 UI 框架與 CDN，全站採用原生 HTML5/CSS3/Vanilla JS (ES6+) 與極簡黑白灰配色。
- **安全性與 RBAC 權限控制**：實作 `/login` 與 `/logout`，未登入僅允許檢視前端 UI 與按鈕效果但禁止後端操作；預設 `admin` 管理員密碼加密儲存；啟用 HttpOnly、Secure、SameSite=Strict Cookie 安全標籤；實作 Admin 與 Employee 階層式權限控制。

## Capabilities

### New Capabilities
- `mofei-erp-database`: MSSQL `MOFEI` 資料庫與資料表建置（員工主檔、產品主檔、客戶主檔、訂單主檔、訂單明細），並保證欄位名稱與後端 Entity/DTO/前端屬性完全對齊。
- `mofei-erp-auth`: 身份認證與 RBAC 權限管理（/login, /logout, 未登入唯讀限制, 預設加密 admin 帳號, HttpOnly/Secure/SameSite Cookie, Admin/Employee 角色授權）。
- `mofei-erp-master-data`: 4 大主檔（員工、產品、客戶、訂單）之 RESTful API 與 Thymeleaf/純 JS 視圖模組，包含 CRUD 操作、複合查詢、即時模糊搜尋、分頁機制與敏感資料過濾。
- `mofei-erp-portal`: 首頁即時監控儀表板與全局 Layout (layout.html)，包含即時時鐘、Logo、導航選單、版權頁尾，以及 100% 離線純原生黑白灰極簡 UI 樣式系統。

### Modified Capabilities
<!-- No existing capabilities are being modified -->

## Impact

- **後端架構**：依據 `AGENT.md`，於 Spring Boot 3.2.x+ / Java 21 專案結構中落實 Maven 模組化單體或對應分層，提供 MyBatis-Plus 持久層與統一 `R<T>` 回應。
- **資料庫**：連線本機 MSSQL Server (`localhost`, `sa`, `0000000`) 建立 `MOFEI` 資料庫與資料表。
- **前端視圖**：純 SSR (Thymeleaf) + 原生 Vanilla JS (fetch/async-await) + 原生 CSS，不引入任何 npm/node 構建與 CDN。
- **安全性與合規**：符合 OWASP API Top 10 與 Git pre-commit 門禁檢查。
