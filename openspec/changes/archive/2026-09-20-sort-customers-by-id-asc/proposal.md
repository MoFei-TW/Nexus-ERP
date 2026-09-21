# Proposal: 客戶編號排序優化（由小到大）

## Why

目前客戶主檔視圖模組（`/customer`）在分頁查詢時，預設是依建立時間（`created_at`）降序排列。為符合業務使用者在檢視與查找客戶資料時的直覺習慣，提升系統一致性與操作體驗，需將客戶主檔清單改為依「客戶編號（Customer ID）由小到大（升冪 ASC）」排列。

## What Changes

- **修改客戶分頁查詢排序條件**：在後端 `CustomerServiceImpl.java` 的 `queryPage` 方法中，將預設排序由 `orderByDesc(Customer::getCreatedAt)` 改為 `orderByAsc(Customer::getCustomerId)`。
- **維持複合搜尋與分頁相容性**：在進行客戶編號、姓名篩選或模糊搜尋時，查詢結果依然維持由小到大排列。

## Capabilities

### New Capabilities
<!-- 無新增 Capabilities -->

### Modified Capabilities
- `mofei-erp-master-data`: 增訂客戶主檔清單需依客戶編號由小到大（升冪 ASC）排序之需求規格。

## Impact

- **後端程式碼**：`CustomerServiceImpl.java` 的 `queryPage` 排序邏輯。
- **測試**：更新或新增測試案例驗證 `CustomerServiceImpl` 分頁查詢之客戶編號升冪排列行為。
- **前端視圖**：前端客戶主檔（`http://localhost:8080/customer`）載入時，資料列表將依客戶編號從小到大呈現。
