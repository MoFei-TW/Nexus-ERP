# test-detect.md - 純 Java 原生自動化測試與除錯技能

本技能作為 AI Agent 在生成或修改程式碼後的「功能測試與自動除錯門禁 (Phase 2)」。

一、 分層測試範疇與 Java 原生框架規範 (Pure Java Testing Framework)
1. 後端單元與業務邏輯測試 (Service & Domain Layer)：
   - 強制採用 JUnit 5 與 Mockito。
   - 針對 Service 層商業邏輯進行 100% 覆蓋的單元測試。
2. 資料庫與持久層測試 (Repository & DAO Layer)：
   - 採用 @MybatisPlusTest 或 @DataJpaTest 結合 H2 / 本地 MySQL 進行 Mapper SQL 斷言測試。
3. REST API 介面測試 (Controller Layer)：
   - 採用 Spring MockMvc 測試 @RestController，驗證 HTTP 狀態碼、@Validated 參數校驗與 R<T> 統一 JSON 格式。
4. 前端頁面與渲染測試 (Thymeleaf View Layer)：
   - 採用 Spring MockMvc 結合 HtmlUnit (Java) 進行 Thymeleaf 頁面 DOM 結構驗證與數據綁定檢查。
   - 100% 於 JVM 內離線執行，嚴禁依賴 Node.js、npm 或外部瀏覽器驅動檔（如 ChromeDriver）。

二、 自動化除錯與狀態歸位機制 (Auto-Debugging & Reset)
1. 失敗診斷：測試未通過時，Agent 必須解析 JUnit Error Stack Trace 與斷言錯誤，精準定位問題點（如 NullPointer、SQL 錯誤、JSON 格式不符）。
2. 自動修復上限：Agent 自動修補程式碼並重新執行測試，最大嘗試次數為 3 次。若 3 次後仍未開綠燈，必須暫停並輸出錯誤報告，等待人工介入。
3. 資料歸位：所有測試案例必須標註 `@Transactional` 與 `@Rollback`，確保測試過程產生的 DB 資料自動歸位，不污染開發環境。

三、 檢核門禁與報告產出 (Quality Gate & Reporting)
1. 執行門禁：所有測試案例必須全數通過，並輸出帶有 `[STATUS: PASSED]` 標籤的 MD 報告至 `docs/tests/`，始得放行交付至下階段（OWASP 資安稽核）。
2. 報告產出：測試完成後，自動產生 MD 報告存於 `docs/tests/{模組名稱}-test-{yyyyMMdd-HHmmss}.md`。
3. 報告必備內容：測試案例數、通過率、斷言失敗診斷與自動修復歷程，最後一行必須明確標註 `[STATUS: PASSED]` 或 `[STATUS: FAILED]`。
