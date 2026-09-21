# AGENT.md - 系統開發規範與技術架構守則

本檔案為 AI Agent 與開發團隊在進行專案開發時的最高指導規範。所有產出的程式碼與架構變動必須 100% 遵守以下條款。

一、 環境規格與技術堆疊 (Environment & Tech Stack)
1. JDK 版本：Java 21 (LTS)
2. 框架與建置：Spring Boot 3.2.x+（開啟虛擬線程 `spring.threads.virtual.enabled=true`）、Maven 多模組 (Multi-Module)
3. 持久層框架：MyBatis-Plus 3.5.x（資料庫採用 MySQL 8.0）
4. 前端技術：Thymeleaf 樣板引擎 + 原生 JavaScript (ES6+ Vanilla JS)

二、 後端技術架構與模組化單體規範 (Modular Monolith)
整體設計遵循「約定優於配置」與「高內聚低耦合」原則。初期部署為單一 JAR 檔，但各模組需具備未來拆分為獨立微服務的潛力。
Maven 模組劃分如下：
- erp-common：全局 Base Entity、統一 API 傳回值格式（R<T>）、全局 Exception 處理與通用 Util。
- erp-framework：封裝 Spring Boot 自動配置、Spring Security/JWT 認證授權、跨域（CORS）與 OpenAPI/Swagger 文件配置。
- core-domain：跨模組共享之核心業務實體、Sealed Domain Events 與 Enum。
- infrastructure：資料庫連接池（HikariCP）、MyBatis-Plus 設定、Redis 快取與底層基礎設施。
- module-user：用戶與權限業務領域模組。
- module-order：訂單與交易業務領域模組。

三、 後端四層分層架構與 API 規格 (Layered Architecture & API)
單一業務模組內部必須嚴格實施四層職責切分：
1. Entity / Domain 層：
   - PO：數據庫映射實體類。
   - DTO / VO：優先採用 Java 21 原生 `record` 進行定義，禁止不必要的 Setter 暴露。
   - Enums / Sealed Classes：狀態列舉與領域事件定義。
2. Repository / DAO 層：繼承 MyBatis-Plus 的 BaseMapper，僅處理數據存取。
3. Service 業務層：處理業務邏輯，明確控管 @Transactional 事務邊界。
4. Controller 層：
   - REST API：使用 @RestController，搭配 @Validated 進行參數校驗，統一傳回 R<T> 格式 JSON 數據。
   - 頁面路由：使用 @Controller 搭配 Thymeleaf 進行後端頁面渲染。

四、 前端架構與零依賴離線規範 (Offline Architecture & Pure Vanilla JS)
1. 視圖與資料綁定：採用 Thymeleaf 進行 SSR 頁面結構與初期數據渲染。
2. 非同步傳輸 (AJAX)：嚴格採用瀏覽器原生 ES6+ `fetch()` API 與 `async/await` 異步呼叫後端 REST API，禁止引入 Axios 或 jQuery。
3. DOM 操作與事件處理：純原生 JavaScript 操作，不使用任何 UI 框架（如 Vue/React）或外部函式庫。
4. 絕對離線與零外部依賴規範：
   - 專案內部「嚴格禁止使用 Node.js / npm 構建流程」，亦「嚴格禁止使用 CDN」引入外部資源。
   - 所有前端 JS/CSS 檔案均為專案自行撰寫的原生靜態檔案，確保在無外網連接的封閉環境下 100% 獨立運行。

五、 AI Agent 行為規範與程式碼品質要求 (Agent Guidelines & Code Quality)
1. 代碼風格：採用 Java 21 現代化寫法（多用 record、var 本地變數推導、Pattern Matching）；前端 JavaScript 使用 ES6+ 規範（const/let、Arrow Functions、Modules）。
2. 統一回應結構：所有 RESTful API 必須傳回 `R.ok(data)` 或 `R.fail(code, message)`。
3. 異常處理：禁止拋出裸 Exception，必須使用自訂 `BusinessException` 並由 `@RestControllerAdvice` 統一捕獲。
4. 測試規範：生成 Service 層邏輯時，必須同步生成基於 JUnit 5 的單元測試。
