# Proposal: 優化員工主檔模組 (Optimize Employee Module)

## Why

目前 MOFEI ERP 系統的員工主檔模組存在若干易用性與資料完整性問題：員工列表系統角色以 badge 標籤顯示造成視覺雜亂、員工編號未依序排列影響查閱效率、新增員工允許使用者手動輸入編號易造成人為編號衝突，且缺乏電話與電子郵件的唯一性防重檢核。本變更旨在提升管理者在員工主檔的操作體驗並確保員工資料的一致性與唯一性。

## What Changes

- **前端角色欄位渲染優化**：移除員工主檔表格中系統角色欄位的 `<span class="badge">` 網頁標籤，改以純文字顯示角色名稱。
- **員工編號預設依序排列**：後端分頁查詢員工列表時，預設排序規則由建立時間倒序調整為員工編號由小到大（遞增排序，`employeeId ASC`）。
- **新增員工編號改由系統配置**：新增員工視窗停用或不提供使用者手動輸入員工編號，後端在新增員工時一律由系統自動配置最新流水編號。
- **新增員工防重檢核增強**：新增員工時，後端強制檢核員工編號（employeeId）、員工電話（phone）與電子郵件（email），任一欄位若已存在資料庫中則拒絕新增並回傳明確錯誤提示。

## Capabilities

### New Capabilities

*(無)*

### Modified Capabilities

- `mofei-erp-master-data`: 修改員工主檔相關規範，包含員工列表依員工編號遞增排列、員工角色純文字顯示、新增員工編號由系統自動配置不提供使用者輸入，以及新增時對員工編號、電話、電子郵件進行重複性檢核。

## Impact

- **前端頁面**：`src/main/resources/templates/employee.html`（調整角色渲染邏輯與新增彈窗編號欄位行為）。
- **後端服務**：`src/main/java/com/mofei/erp/modules/user/service/impl/EmployeeServiceImpl.java`（調整分頁排序、系統發號與電話/信箱唯一性校驗）。
- **測試案例**：`src/test/java/com/mofei/erp/modules/` 相關 API 測試與單元測試。
