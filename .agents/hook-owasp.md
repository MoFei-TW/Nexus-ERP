# hook-owasp.md - OWASP API Top 10 自動化資安稽核機制

本機制為 AI Agent 於程式碼功能測試通過後，進行正式交付前之「自動化資安品質門禁 (Phase 3)」。

一、 稽核時機與離線檢核原則 (Execution & Offline Audit)
1. 觸發時機：Agent 完成程式碼撰寫並通過 `test-detect` 測試後，必須自動執行此 Hook 進行靜態資安掃描，全數通過後始得交付程式碼。
2. 離線規範：稽核過程必須 100% 於本機端執行（基於語意分析與靜態規則檢測），嚴禁呼叫任何外部網路 API 或第三方雲端掃描服務。
3. 修正上限：若發現漏洞，Agent 必須自動修復並重測，最高修復次數為 3 次。若 3 次後仍未通過，須於報告中標註失敗原因並暫停交付，等待人工介入。

二、 針對專案技術堆疊之 OWASP API Top 10 檢查細則
1. SQL 注入防護 (API8 / Injection)：
   - MyBatis-Plus XML 與 Mapper 之中，嚴禁使用 `${}` 變數替換，強制統一使用 `#{}` 預編譯參數。
2. XSS 跨站腳本防護 (API3 / Improper Data Handling)：
   - Thymeleaf 頁面必須優先使用 `th:text`，嚴禁無安全轉義的 `th:utext`。
   - 原生 JavaScript 處理 `fetch()` API 數據時，嚴禁對未轉義資料使用 `innerHTML`，必須採用 `textContent` 或 DOM 節點賦值。
3. 越權與身份驗證 (API1 BOLA / API5 BFLA)：
   - Service 層與 Controller 層必須校驗目前登入用戶（Spring Security Context）是否擁有操作該目標資源的權限，禁止僅憑前端傳入的 ID 即進行刪改操作。
4. 敏感到資料暴露與異常處理 (API3 / API8)：
   - 全局異常處理 (`@RestControllerAdvice`) 嚴禁將 Java 異常堆疊（Stack Trace）直接輸出至前端 API，統一包裝為 `R.fail(code, message)`。
   - DTO/VO 必須使用 Java 21 `record`，嚴禁回傳包含密碼、Salt 等敏感欄位的 Entity 實體。
5. 輸入參數驗證 (API6 / Unrestricted Resource Consumption)：
   - Controller 接收參數之 DTO 必須加上 `@Validated` 註解與 JSR-380 校驗標籤（如 `@NotBlank`, `@Size`）。
   - 分頁查詢必須設定最大 `size` 上限，防止記憶體耗盡。

三、 自動化資安稽核報告規範 (Audit Report Standard)
1. 儲存路徑與命名：`docs/security/{模組或類別名稱}-owasp-{yyyyMMdd-HHmmss}.md`。
2. 報告必備區塊：
   - 基本資訊：目標程式名稱、檢測時間、執行模組。
   - OWASP API Top 10 檢核矩陣：列出 10 項指標之檢測結果（PASS / FAIL / NA）。
   - 漏洞發現與修復紀錄：若有修復，載明原漏洞點、修復方式與驗證結果。
   - 最終門禁狀態：最後一行必須明確標註 `[STATUS: PASSED]` 或 `[STATUS: FAILED]`。
