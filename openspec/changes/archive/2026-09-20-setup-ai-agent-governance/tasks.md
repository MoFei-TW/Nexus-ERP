# Tasks: AI Agent 治理規範與自動化流水線建置

## 1. 基礎腳本與流水線建立 (Foundation & Pipeline Scripts)

- [x] 1.1 建立 `init-agents.sh` 初始化腳本，包含工作空間目錄建立、腳本內嵌生成、Git Hook 配置與權限賦予邏輯，並驗證腳本內容完整性。
- [x] 1.2 建立核心流水線驗證腳本 `scripts/verify-pipeline.sh`，實作 Maven JUnit 測試、功能測試報告門禁與 OWASP 資安報告門禁檢核，並驗證腳本可執行。
- [x] 1.3 建立 Git Pre-commit 攔截腳本 `.githooks/pre-commit`，實作整合驗證流水線呼叫與備用綠燈標籤掃描門禁，並驗證腳本語法正確。

## 2. 核心規範與 Agent 技能建立 (Governance Rules & Skills)

- [x] 2.1 建立系統開發規範守則 `AGENT.md`，詳述 Java 21、Spring Boot 3.2、MyBatis-Plus、模組化單體四層架構、離線原生 JavaScript 與 Agent 代碼品質標準，並確認文件已建立。
- [x] 2.2 建立 OpenSpec SDD 提案生成技能 `.agents/skills/sdd-creator.md`，定義 Phase 0 規格變更分析、雙目錄架構與提案審核門禁，並確認技能內容齊全。
- [x] 2.3 建立純 Java 自動化測試技能 `.agents/skills/test-detect.md`，定義 Phase 2 JUnit 5 / MockMvc / HtmlUnit 測試、自動除錯 3 次上限與 `docs/tests/` 報告規範，並確認內容齊全。
- [x] 2.4 建立 OWASP API Top 10 資安稽核機制 `.agents/hook-owasp.md`，定義 Phase 3 離線資安檢核細則、自動修復機制與 `docs/security/` 報告規範，並確認內容齊全。

## 3. 環境初始化執行與門禁綁定 (Execution & Verification)

- [x] 3.1 執行 `init-agents.sh` 腳本，自動建立所有治理目錄並為腳本設定執行權限（`chmod +x`），檢查終端執行結果為成功。
- [x] 3.2 驗證 Git Hooks 配置（`git config core.hooksPath` 設為 `.githooks`）與工作空間結構正確性。
