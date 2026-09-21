# Design

## Context

現有訂單主檔與明細模組由後端 `OrderController`、`OrderServiceImpl` 與前端 Thymeleaf 視圖 `templates/order.html` 組成。
- 查詢排序目前在 `OrderServiceImpl.queryPage` 中寫死為 `wrapper.orderByDesc(Orders::getCreatedAt)`。
- 表格總額欄位在 `order.html` 的 `MofeiTable` 設定中使用了字串範本 `<strong>...</strong>`，而 `mofei-table.js` 採用 `textContent` 防護 XSS，導致標籤字串直接顯示在畫面上。
- 新增訂單視窗使用純文字輸入客戶編號與業務員工編號，缺乏防呆與聯動；訂單明細輸入行無表頭說明；折讓欄位為任意數值輸入。
- 訂單明細檢視彈窗為唯讀狀態，無法直接修改明細項目或新增產品品項；且後端 `OrderServiceImpl.update` 尚未處理明細行之重算與更新。

詳見 `proposal.md - Why` 與 `specs/mofei-erp-master-data/spec.md`。

## Goals / Non-Goals

**Goals:**
- 將訂單主檔查詢排序調整為依照訂單編號（`orderId`）由小到大遞增排列（ASC）。
- 移除訂單總額欄位中的 `<strong>` HTML 標籤，顯示乾淨的貨幣數值字串（如 `57,000`）。
- 新增訂單表單：
  - 客戶編號與業務員工編號提供下拉式選單（`<select>`），非同步載入主檔資料顯示「編號 - 名稱」。
  - 訂單明細品項明確顯示各欄位名稱（產品編號/選擇產品、數量、折讓、小計/操作）。
  - 折讓欄位採用下拉式選單，僅提供 `0`、`5`、`75` 三種選項。
- 訂單明細彈窗功能擴充：
  - 支援明細項目之欄位修改（數量調整、折讓下拉選單切換）。
  - 支援「新增產品」功能，可於彈窗中新增明細行項目。
  - 折讓欄位同步僅提供 `0`、`5`、`75` 下拉式選單。
  - 點擊「儲存修改」時，將更新資料送至後端，後端更新明細並重新計算小計與訂單總額（`totalAmount`）。

**Non-Goals:**
- 不變更資料庫綱要（DDL），沿用現有 `orders` 與 `order_detail` 資料表欄位結構。
- 不更動權限驗證或認證架構。

## Decisions

### 1. 訂單查詢排序調整為 `orderId ASC`
- **決定**：在 `OrderServiceImpl.queryPage` 中將 `wrapper.orderByDesc(Orders::getCreatedAt)` 修改為 `wrapper.orderByAsc(Orders::getOrderId)`。
- **替代方案**：依下單時間排序。但使用者明確需求為依訂單編號由小到大排列，利於循序查核。

### 2. 訂單總額渲染優化
- **決定**：在 `order.html` 中將 `render: row => <strong>${Number(row.totalAmount).toLocaleString()}</strong>` 修正為 `render: row => Number(row.totalAmount).toLocaleString()`。
- **替代方案**：建立 HTMLElement 節點（`document.createElement('strong')`）。但使用者要求「移除網頁標籤」，直接回傳純文字數字格式最為乾淨且完全相容 `mofei-table.js` 的 `textContent` 規範。

### 3. 客戶與業務員工下拉選單
- **決定**：在開啟新增訂單彈窗時（或頁面載入時），透過 `fetch('/api/customer/page?size=100')` 與 `fetch('/api/employee/page?size=100')` 載入活躍客戶與員工資料，動態產生 `<select>` 選項（格式：`編號 - 名稱`）。
- **替代方案**：仍使用文字輸入搭配 datalist。但下拉選單更具防呆效果，能避免輸入不存在的 ID。

### 4. 訂單明細品項表頭顯示與折讓限定選項
- **決定**：
  - 在明細品項容器上方建立明確的表頭列（產品編號/名稱、數量、折讓、操作）。
  - 將折讓欄位全面改為 `<select class="select-control detail-discount">`，提供選項：
    - `0` (NT$ 0)
    - `5` (NT$ 5)
    - `75` (NT$ 75)
- **同步原則**：此折讓下拉選單規範同步應用於「新增訂單」與「訂單明細修改」。

### 5. 訂單明細修改與新增產品之架構設計
- **決定**：
  - 前端：將 `orderDetailModal` 擴充為可編輯表單，明細列包含產品資訊（或產品選單）、數量輸入框、折讓下拉選單、小計即時計算與刪除按鈕；彈窗內提供「+ 新增產品」按鈕可動態追加列，並於底部提供「儲存修改」按鈕。
  - 後端：擴充 `OrderServiceImpl.update(String orderId, OrderDTO dto)`，當 `dto.details()` 傳入時：
    1. 驗證所有產品是否存在。
    2. 依產品單價、數量與折讓重新計算各明細行之小計，並累加至訂單總額 `totalAmount`。
    3. 刪除該訂單原有的 `order_detail` 記錄，批次寫入新的明細實體。
    4. 更新 `orders` 主檔的 `totalAmount` 與收件資訊，回傳更新後之 `OrderVO`。
- **替代方案**：新增獨立的 OrderDetailController。但在現行架構中，訂單與明細為整體聚合根（Aggregate Root），由 `OrderDTO` 與 `OrderService.update` 統一維護更為嚴謹且符合事務完整性。

## Risks / Trade-offs

- **[Risk] 明細全數刪除導致空訂單** → **Mitigation**: 前端送出修改前驗證至少保留一筆明細項目，後端 `OrderDTO` 保留 `@NotEmpty` 驗證，防止空明細儲存。
- **[Risk] 客戶或員工數量較多時下拉選單載入延遲** → **Mitigation**: 分頁 API 設定 `size=100` 或於頁面初始化時非同步預加載並快取，避免重複請求。
