# Tasks

## 1. 資料庫建立與實體對齊 (MSSQL Database Setup)

- [x] 1.1 撰寫 MSSQL 初始化 DDL 腳本 (`schema.sql` 與 `data.sql`)，建立 `MOFEI` 資料庫與 5 大資料表（`employee`, `product`, `customer`, `orders`, `order_detail`），並確認各欄位、主外鍵約束與索引建立成功。
- [x] 1.2 執行資料庫腳本於本機 MSSQL (`localhost`, `sa`, `0000000`)，並透過 SQL 查詢驗證 5 大資料表存在且結構與欄位名稱完全一致。

## 2. 後端基礎設施與持久層配置 (Backend Infrastructure & MyBatis-Plus)

- [x] 2.1 配置 Spring Boot 3.2.x+ 專案之 `pom.xml` 與 `application.yml`，引入 `mssql-jdbc`、MyBatis-Plus、Thymeleaf、Spring Security，並開啟虛擬線程 (`spring.threads.virtual.enabled=true`)，驗證應用程式可成功連線至 MSSQL。
- [x] 2.2 實作統一 API 回應格式 `R<T>`、全局自訂業務異常 `BusinessException` 與 `@RestControllerAdvice` 異常捕獲處理器，並透過單元測試驗證回傳 JSON 結構與錯誤碼。
- [x] 2.3 實作 5 大資料表之 MyBatis-Plus PO 實體類與 Java 21 原生 `record` DTO/VO，嚴格確保資料庫欄位名稱、Entity 屬性與 VO 屬性一致，並過濾遮蔽密碼與鹽值等敏感欄位。

## 3. 安全認證與 RBAC 權限管理 (Security & RBAC)

- [x] 3.1 實作 Spring Security 配置，設置 `/login` 與 `/logout` 端點，支援預設管理者 `admin` / `admin` 加密認證（BCrypt），並驗證登入成功後簽發之 Cookie 包含 `HttpOnly=true`, `Secure=true`, `SameSite=Strict`。
- [x] 3.2 實作階層式 RBAC 權限控管（Admin 存取所有模組，Employee 存取業務模組），並配置未登入訪客攔截器，確保未登入時後端 CRUD API 回傳 401 Unauthorized，但允許檢視前端靜態視圖。

## 4. 4 大主檔後端 CRUD API 實作 (Master Data CRUD APIs)

- [x] 4.1 實作員工主檔 (`employee`) 之 Mapper、Service 與 Controller，包含新增、修改、複合查詢（編號與名稱）、在職狀態控制（軟刪除），並透過 MockMvc 測試驗證 API 行為。
- [x] 4.2 實作產品主檔 (`product`) 之 Mapper、Service 與 Controller，包含 CRUD 與複合查詢，並透過 MockMvc 測試驗證。
- [x] 4.3 實作客戶主檔 (`customer`) 之 Mapper、Service 與 Controller，包含 CRUD 與複合查詢，並透過 MockMvc 測試驗證。
- [x] 4.4 實作訂單主檔與明細 (`orders`, `order_detail`) 之事務性 Service 與 Controller，支援訂單新增、明細關聯保存、修改與狀態更新，並透過 MockMvc 測試驗證。

## 5. 全局 Layout 與離線極簡樣式系統 (Global Layout & Offline Styling)

- [x] 5.1 建立純原生極簡黑白灰樣式表 (`mofei-style.css`)，定義色彩變數、按鈕、表格、輸入框與排版，確保 100% 離線可用且無任何外部 UI 框架或 CDN 依賴。
- [x] 5.2 建立 Thymeleaf 全局公版 `layout.html`，配置頂端導覽列（引用 `images/logo/2026-09-10-00-logo by MF.jpg`、即時動態時鐘 `YYYY-MM-DD HH:mm:ss`、4 大主檔跳轉選單）與頁尾版權宣告 (`@2026 DataBase by MOFEI`)，並在瀏覽器中驗證時間每秒動態更新與排版效果。

## 6. 4 大主檔前端視圖與互動機制 (Thymeleaf & Vanilla JS Views)

- [x] 6.1 實作通用 Vanilla JS 表格控制模組 (`mofei-table.js`)，封裝原生 `fetch()` 異步呼叫、每頁 10 筆上下分頁切換、以及即時模糊搜尋功能。
- [x] 6.2 實作員工主檔前端視圖頁面，整合模糊搜尋、分頁、新增彈窗、編輯與狀態切換（軟刪除）按鈕，並確認敏感欄位不予顯示。
- [x] 6.3 實作產品主檔前端視圖頁面，整合 CRUD 表單與表格互動，驗證欄位顯示與操作。
- [x] 6.4 實作客戶主檔前端視圖頁面，整合 CRUD 表單與表格互動，驗證欄位顯示與操作。
- [x] 6.5 實作訂單主檔與明細前端視圖頁面，支援訂單檢視、明細展開、新增與修改，驗證主從資料連動正確。

## 7. 首頁儀表板與即時監控 (Dashboard)

- [x] 7.1 實作儀表板後端統計 API，即時計算員工總數/在職數、產品總數/低庫存數、客戶總數、訂單總數/各狀態訂單數。
- [x] 7.2 實作首頁 Dashboard 視圖頁面，以極簡黑白灰卡片佈局展示 4 大主檔即時統計數據，並驗證頁面加載時自動拉取並動態呈現。

## 8. 整合驗證與資安門禁稽核 (Integration Testing & Quality Gate)

- [x] 8.1 執行 JUnit 5 與 MockMvc 整合測試，驗證 4 大主檔 CRUD、分頁、搜尋、未登入攔截與 RBAC 權限全數開綠燈。
- [x] 8.2 依據 `hook-owasp.md` 執行 OWASP API Top 10 靜態稽核，確認無 SQL 注入（`#{}`）、無未轉義 XSS (`textContent` / `th:text`)、敏感資料無外洩，並產生資安檢核報告。
