# Design: AI Agent 治理體系與自動化流水線架構設計

## Context

見 `proposal.md` 之動機說明。目前工作空間處於初始狀態，僅有基礎 `openspec/` 目錄結構，尚未建立專案開發規範、Agent 技能指引、測試門禁與 Git Hook 防護。本設計著眼於以純本機、零外部依賴、純 Java / Bash 腳本為基礎，搭建完整的規格驅動開發（SDD）與品質防護網。

## Goals / Non-Goals

**Goals:**
- 建立檔案級別的 AI Agent 規範體系：包括架構總綱（`AGENT.md`）、SDD 提案技能（`sdd-creator.md`）、測試除錯技能（`test-detect.md`）與資安稽核機制（`hook-owasp.md`）。
- 建立自動化品質驗證與提交攔截機制：`scripts/verify-pipeline.sh` 與 `.githooks/pre-commit`。
- 提供一鍵式環境初始化腳本：`init-agents.sh`，完成目錄建立、Git Hook 綁定與權限設定。
- 達成 100% 離線運行、零外部網路依賴、零 Node.js/npm 依賴。

**Non-Goals:**
- 建立具體業務模組（如 `module-user`、`module-order`）之程式碼或資料庫表結構（此為後續功能變更之範疇）。
- 引入外部雲端 CI/CD 服務或第三方 SAST/DAST 平台（如 SonarQube/Snyk 外部 API）。
- 引入前端打包工具鏈（Webpack、Vite、npm 等）。

## Decisions

### 1. Git Hooks 版控策略：採用 `.githooks/` 與 `git config core.hooksPath`
- **決策**：將 Hook 腳本放置於專案目錄 `.githooks/`，並由 `init-agents.sh` 執行 `git config core.hooksPath .githooks`。
- **原因**：預設的 `.git/hooks/` 不受 Git 版本控制，團隊成員與各 AI Agent 環境無法自動同步。透過 `core.hooksPath` 可讓 Hook 納入專案版控並統一管理。
- **替代方案**：
  - 使用 Husky：需引入 Node.js 與 npm 生態系，違反本專案「純 Java + 原生前端、零 Node.js 依賴」架構原則。
  - 手動符號連結（symlink）：在 Windows 環境下可能遇到權限問題，相容性較差。

### 2. 測試框架與除錯架構：純 Java 原生與 JVM 離線測試
- **決策**：後端採用 JUnit 5 + Mockito + MockMvc，前端 SSR 頁面渲染採用 MockMvc + HtmlUnit，全部於 JVM 內離線執行；測試均標註 `@Transactional` 與 `@Rollback`。
- **原因**：確保在封閉、無外網連接的環境下 100% 獨立運行，不依賴外部瀏覽器驅動或 Node.js。
- **替代方案**：
  - Selenium / Playwright：需下載瀏覽器二進位檔（如 ChromeDriver），依賴外部環境且啟動耗時。
  - Jest / Cypress：需依賴 Node.js 環境。

### 3. 資安稽核策略：本機離線語意檢核與雙層門禁機制
- **決策**：資安稽核基於本機靜態規則（OWASP API Top 10）檢測，支援至多 3 次自動修復。`verify-pipeline.sh` 與 `pre-commit` 採用雙層檢查（主腳本執行與備用最新報告掃描），報告以 `[STATUS: PASSED]` 標籤作為硬性門禁。
- **原因**：確保資安檢查兼具執行速度與隱私安全性，不洩漏任何程式碼至外網。
- **替代方案**：
  - 呼叫雲端 SAST 服務：存在資料外洩風險且無法離線運行。

## Risks / Trade-offs

- **[Windows 與 Bash 相容性]** → Windows 環境下若使用 CMD/PowerShell 執行 Git Commit，Git for Windows 內建之 MSYS2/bash 能正常解析執行 `.githooks/pre-commit`。`init-agents.sh` 在 Windows 上可透過 Git Bash 執行。
- **[首次 Commit 無報告時的門禁攔截]** → 若尚未產出測試與資安報告，Pre-commit 會嚴格攔截。緩解方式：在 `AGENT.md` 與技能中明確定義 Phase 2 與 Phase 3 流程，要求提交前必先生成對應報告。
- **[測試執行時間對 Commit 體驗的影響]** → 採用 Maven `-q` 靜默模式與針對性測試，減少冗餘輸出並提升執行效能。

## Migration Plan

1. 實作階段建立 `init-agents.sh`、`AGENT.md`、`.agents/skills/sdd-creator.md`、`.agents/skills/test-detect.md`、`.agents/hook-owasp.md`。
2. 建立 `scripts/verify-pipeline.sh` 與 `.githooks/pre-commit`。
3. 執行 `bash init-agents.sh` 完成目錄初始化、Git Hooks 導向與權限設定。
4. 驗證 Git 狀態與 Hook 設定生效。
