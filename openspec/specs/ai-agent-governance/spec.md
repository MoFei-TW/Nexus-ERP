# AI Agent 治理規範與自動化流水線 Specification

## Purpose

本規格定義工作空間中 OpenSpec 規格驅動開發（SDD）、AI Agent 開發與行為準則、純 Java 自動化測試門禁、OWASP API Top 10 離線資安稽核，以及 Git Pre-commit 自動化管制的系統行為合約與品質驗證標準。

## Requirements

### Requirement: 工作空間環境與治理結構初始化
系統 SHALL 提供初始化機制（`init-agents.sh`），自動建立 OpenSpec 雙目錄（`specs/`、`changes/`）、AI 治理目錄（`.agents/skills/`、`docs/tests/`、`docs/security/`、`scripts/`、`.githooks/`），並將 Git Hooks 導向專案內部 `.githooks/` 目錄，同時賦予必要腳本執行權限。

#### Scenario: 執行環境初始化
- **WHEN** 開發者或管理員執行 `init-agents.sh`
- **THEN** 系統建立完整的 OpenSpec 與治理目錄結構、設定 `git config core.hooksPath .githooks`，並確認 `scripts/verify-pipeline.sh` 與 `.githooks/pre-commit` 具備可執行權限

### Requirement: OpenSpec 規格驅動開發（SDD）提案門禁
系統 SHALL 要求所有新功能需求、系統重構或商業邏輯變更，於撰寫任何正式程式碼前，必須先透過 OpenSpec 流程建立變更提案（包含 `proposal.md`、`tasks.md` 與增量規格 `specs/`），並在取得明確審查核可後始得進入代碼實作階段。

#### Scenario: 需求進入實作前的提案審查
- **WHEN** AI Agent 接收到新功能或重構需求
- **THEN** Agent 建立對應之 OpenSpec 變更提案目錄與文件，並暫停等待使用者審核核可，未取得核可前不得撰寫專案代碼

#### Scenario: 規格變更後的歸檔同步
- **WHEN** 代碼實作完成並通過所有品質門禁
- **THEN** 系統允許將變更提案中的增量規格合併至 `openspec/specs/` 主規格庫中

### Requirement: 系統架構守則與代碼品質約束
系統 SHALL 在 `AGENT.md` 中明確規範技術堆疊（Java 21 LTS、Spring Boot 3.2.x+、MyBatis-Plus 3.5.x、MySQL 8.0、Thymeleaf、原生 ES6+ Vanilla JS），嚴格執行模組化單體、後端四層架構（Entity、Repository、Service、Controller）、統一 `R<T>` API 回應格式、自訂 `BusinessException` 異常處理，以及零外部依賴與絕對離線前端規範。

#### Scenario: 後端 API 與異常處理規範
- **WHEN** 後端 Controller 處理請求並回傳結果或拋出業務異常
- **THEN** 回應格式必須統一為 `R.ok(data)` 或由 `@RestControllerAdvice` 捕獲 `BusinessException` 並回傳 `R.fail(code, message)`，嚴禁將裸 Exception 或堆疊直接回傳前端

#### Scenario: 前端原生離線規範
- **WHEN** 實作前端頁面與互動邏輯
- **THEN** 僅使用 Thymeleaf SSR 與原生 ES6+ `fetch()` 及 Vanilla JS DOM 操作，嚴禁引入 Node.js/npm 建置流程、外部 CDN 或第三方程式庫（如 jQuery/Axios/Vue/React）

### Requirement: 純 Java 原生自動化測試與除錯門禁
系統 SHALL 實施純 Java 原生自動化測試規範（JUnit 5、Mockito、MockMvc、HtmlUnit），確保測試於 JVM 內部離線執行；測試案例必須標註 `@Transactional` 與 `@Rollback`；若測試失敗，系統支援最多 3 次自動修復除錯；測試完成後必須於 `docs/tests/` 產出帶有 `[STATUS: PASSED]` 標籤的測試報告。

#### Scenario: 自動化測試全部通過
- **WHEN** 執行測試流程且所有測試案例斷言成功
- **THEN** 系統於 `docs/tests/` 產出測試報告，且最後一行明確標註 `[STATUS: PASSED]`

#### Scenario: 測試失敗觸發自動除錯修復
- **WHEN** 單元測試或介面測試發生斷言失敗或錯誤
- **THEN** 系統分析 Error Stack Trace 並自動修復代碼重新測試，若在 3 次內修復成功則產出 `[STATUS: PASSED]` 報告；若超過 3 次仍失敗則標註 `[STATUS: FAILED]` 並暫停交付

### Requirement: OWASP API Top 10 自動化離線資安稽核門禁
系統 SHALL 於代碼測試通過後執行離線資安稽核（涵蓋 SQL 注入防護 `#{}` vs `${}`、XSS 防護、越權與身份驗證 BOLA/BFLA、敏感資料暴露遮蔽、`@Validated` 參數校驗），支援最多 3 次自動漏洞修復，並於 `docs/security/` 產出帶有 `[STATUS: PASSED]` 標籤的資安報告。

#### Scenario: 資安稽核合規通過
- **WHEN** 靜態資安掃描檢核 OWASP API Top 10 項目皆符合安全準則
- **THEN** 系統於 `docs/security/` 產出資安稽核報告，且最後一行明確標註 `[STATUS: PASSED]`

#### Scenario: 偵測到資安違規並自動修復
- **WHEN** 靜態掃描發現 SQL 注入風險（如 `${}`）或 XSS 風險（如 `th:utext`）
- **THEN** 系統自動修復為安全寫法（如 `#{}` 或 `th:text`）並重新驗證，記錄修復歷程後輸出報告

### Requirement: Pre-commit Hook 與流水線自動化攔截
系統 SHALL 透過 `.githooks/pre-commit` 在 Git 提交時自動執行 `scripts/verify-pipeline.sh`，依序驗證 Maven 測試、`docs/tests/` 最新測試報告綠燈標籤、`docs/security/` 最新資安報告綠燈標籤；任一項目未通過即強制中斷 Git Commit。

#### Scenario: 品質與資安門禁全數綠燈允許提交
- **WHEN** 開發者執行 `git commit`，且 Maven 測試、測試報告與資安報告均具備 `[STATUS: PASSED]`
- **THEN** Pre-commit Hook 驗證通過，允許 Git 完成 Commit 操作

#### Scenario: 門禁未通過攔截提交
- **WHEN** 開發者執行 `git commit`，但測試未通過、報告缺失或最新報告未標註 `[STATUS: PASSED]`
- **THEN** Pre-commit Hook 立即中斷提交，輸出錯誤訊息並阻止 Git Commit
