# Tasks

## 1. Skill 檔案建立與規範整合

- [x] 1.1 建立 `.agents/skills/db-creator/SKILL.md`，包含 YAML frontmatter（name: db-creator, description）與資深 MSSQL 架構師角色定義，驗證檔案格式與技能說明符合規範
- [x] 1.2 建立/同步 `.agents/skills/db-creator.md`，確保與工作空間治理目錄結構完全相容，驗證檔案內容與引用路徑正確

## 2. 三階段執行流程與確認門禁設計

- [x] 2.1 於 `SKILL.md` 中詳細定義「階段一：Schema 設計推論與最佳實踐說明」，包含 5 張表（員工主檔、產品主檔、客戶主檔、訂單主檔、訂單明細）之 MSSQL 資料型態映射、主外鍵約束、CHECK 驗證規則與安全雜湊建議，驗證欄位覆蓋度與型態準確度
- [x] 2.2 於 `SKILL.md` 中實作「階段一確認門禁 (Approval Gate)」，規範 Agent 必須輸出預覽並暫停等待使用者明確輸入「確認無誤，開始建立資料庫」，驗證未獲確認嚴禁執行階段二
- [x] 2.3 於 `SKILL.md` 中詳細定義「階段二：資料庫與資料表建立」，包含 MSSQL localhost (sa / 0000000) 連線配置、`IF NOT EXISTS CREATE DATABASE [MF]` 與依外鍵相依順序建立資料表之 T-SQL 執行規範，驗證建立順序符合外鍵相依性
- [x] 2.4 於 `SKILL.md` 中詳細定義「階段三：產出完整 Schema 報告與視覺化文件」，規範輸出檔名為 `MF-schema.md`，包含概念架構、Mermaid ERD、5 張表之獨立資料字典與可重複執行之 DDL 腳本區塊，驗證文件結構與章節規範

## 3. 驗證與治理規範對齊

- [x] 3.1 驗證 `db-creator` 技能可被 AI Agent 正確解析與調用，並符合專案 `AGENT.md` 與 OpenSpec 規範
- [x] 3.2 執行 OpenSpec 變更驗證（`openspec validate db-creator`），確認所有提案、規格、設計與任務檔案皆通過檢查
