# Proposal: AI Agent 治理規範與自動化流水線建立

## Why

在引入 AI Agent 協助系統開發的過程中，若缺乏統一的架構約束、規格驅動流程與自動化品質門禁，容易導致代碼風格混亂、缺乏測試覆蓋、引入資安漏洞或架構失控。為了確保 AI Agent 與開發團隊在受控、合規、高質量的環境下協同作業，亟需在工作空間中一次性建立完整的 OpenSpec 規格驅動開發（SDD）體系與 AI Agent 自動化治理規範。

## What Changes

建立並配置完整的 OpenSpec 規格驅動與 AI Agent 自動化治理體系，包含：

- **環境初始化與 Git Hooks 綁定 (`init-agents.sh`)**：建立 OpenSpec 雙目錄（`specs/` 與 `changes/`）、治理目錄（`docs/tests/`、`docs/security/` 等），並將 Git hooks 自動導向專案內部 `.githooks/`。
- **系統開發規範與技術架構守則 (`AGENT.md`)**：規範 Java 21 LTS、Spring Boot 3.2.x+、MyBatis-Plus 3.5.x、MySQL 8.0、Thymeleaf + 原生 ES6+ JavaScript（零外部依賴、絕對離線）、模組化單體架構與分層職責。
- **OpenSpec 變更提案生成技能 (`.agents/skills/sdd-creator.md`)**：定義 Phase 0 規格變更提案生成流程，強制執行「先規格審查、後實作代碼」的提案管制門禁。
- **純 Java 原生自動化測試與除錯技能 (`.agents/skills/test-detect.md`)**：定義 Phase 2 功能測試與自動除錯門禁，使用 JUnit 5、MockMvc、HtmlUnit 進行離線測試與 3 次自動修復，產出測試報告門禁標籤。
- **OWASP API Top 10 自動化資安稽核機制 (`.agents/hook-owasp.md`)**：定義 Phase 3 離線資安稽核機制，針對 SQL 注入、XSS、越權、敏感資料與參數校驗進行離線靜態檢核與 3 次自動修復。
- **核心流水線與 Pre-commit 攔截腳本 (`scripts/verify-pipeline.sh`, `.githooks/pre-commit`)**：在 Git 提交前強制執行 Maven 測試、功能測試報告檢核與 OWASP 資安報告檢核，全數通過始允許提交。

## Capabilities

### New Capabilities
- `ai-agent-governance`: 涵蓋 OpenSpec 規格驅動流程、系統架構守則、測試與資安門禁機制，以及自動化 Pipeline 與 Pre-commit Hook 治理規範。

### Modified Capabilities
<!-- 無既有規格需要修改 -->

## Impact

- **專案結構**：新增 `.agents/`、`.githooks/`、`scripts/`、`docs/tests/`、`docs/security/` 目錄與相關腳本與規範文件。
- **Git 配置**：透過 `git config core.hooksPath .githooks` 啟用專案內部 Pre-commit 檢查。
- **開發流程**：所有後續功能開發與變更皆必須依循 OpenSpec SDD 提案流程，並在提交前通過測試與資安門禁。
