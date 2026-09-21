# sdd-creator.md - OpenSpec 驅動設計與變更提案生成技能

本技能作為 AI Agent 接收任何需求時的「Phase 0：OpenSpec 需求分析與規格變更提案關卡」。

一、 技能對齊與 OpenSpec 雙目錄架構
1. 觸發時機：當使用者提出新功能需求、系統重構或複雜邏輯調整時，必須優先調用此技能，嚴禁未經 OpenSpec 提案審核直接撰寫程式碼。
2. 架構對齊：分析需求時，必須同時對齊 `workspace/AGENT.md` 定義之技術堆疊（Java 21, Spring Boot 3.2, MyBatis-Plus, Vanilla JS）與 `openspec/specs/` 中既有的領域規格。
3. 雙目錄職責劃分：
   - `openspec/specs/`：存放專案長期生效、最新版本的「唯一真理源」（Source of Truth）規格。
   - `openspec/changes/{change-id}/`：存放目前進行中的變更提案與任務拆解。

二、 OpenSpec 變更提案 (Proposal) 生成流程
Agent 接收需求後，必須建立動態資料夾 `openspec/changes/{change-id}/`（例如：`openspec/changes/add-user-registration/`），並填入以下檔案：

1. `proposal.md`（提案說明書）：
   - **動機與目標**：說明本次變更理由與預期效益。
   - **影響範圍**：列出影響之 Maven 模組（如 `module-user`）與數據庫 Table 變動。
   - **API 合約與視圖異動**：定義 REST API (`R<T>`) 與 Thymeleaf/JS 異動範疇。
2. `tasks.md`（任務拆解）：
   - **Task Breakdown**：條列開發步驟（如：1. 建表 -> 2. DTO/PO -> 3. Service -> 4. Controller）。
   - **品質對齊點**：註記後續 `test-detect.md` 測試點與 `hook-owasp.md` 資安掃描重點。
3. `specs/` (增量規格 Delta)：
   - 若本次需求影響或擴充既有規格，於 `openspec/changes/{change-id}/specs/` 下建立或更新對應的子規格 MD 檔。

三、 提案確認與管制 (Approval Gate)
1. 提案建立完成後，Agent 必須暫停並主動提示開發者審閱 `openspec/changes/{change-id}/proposal.md`。
2. 取得開發者明確審核通過指令（如：「同意提案，開始實作」）後，始得進入 Code 實作階段（Phase 1）。
3. **歸檔機制**：代碼開發完成並通過 `verify-pipeline.sh` 與 Git Pre-commit 綠燈後，可執行 OpenSpec 歸檔操作，將 `openspec/changes/{change-id}/specs/` 的變更正式合併至 `openspec/specs/` 主規格庫中。
