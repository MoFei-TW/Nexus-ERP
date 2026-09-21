# Design

## Context

現有 `templates/order.html` 的新增訂單彈窗（`createOrderModal`）已於前一階段優化為客戶與業務下拉選單，並在頁面載入時透過 `loadMasterOptions()` 快取了 `customerList`、`employeeList` 與 `productList`。
然而目前：
1. `<select id="formCustomerId">` 未綁定選取變更事件，選擇客戶後，收件人姓名、電話、地址與業務員工欄位仍為空白，使用者須逐一重複手動輸入。
2. 訂單明細品項容器（`detailRowsContainer`）僅有產品、數量、折讓輸入項，缺乏各行項目之「小計」顯示，且表單底部未提供「訂單預估總額」的即時連動計算。

## Goals / Non-Goals

**Goals:**
- 在 `createOrderModal` 中實現選擇客戶時的資料自動帶入：
  - 收件人姓名（`formRecipientName`）帶入 `customer.customerName`。
  - 收件人電話（`formRecipientPhone`）帶入 `customer.phone`。
  - 收件地址（`formRecipientAddress`）帶入客戶完整地址（組合國家、城市、行政區與詳細地址）。
  - 業務員工（`formEmployeeId`）自動切換為 `customer.employeeId`。
- 在 `createOrderModal` 中實現明細小計與整單總額即時試算：
  - 明細表頭加入「小計 (NT$)」。
  - 明細每一行顯示即時試算的小計（單價 × 數量 - 折讓）。
  - 產品選取變更、數量變更、折讓切換（0, 5, 75）、新增品項行或刪除品項行時，即時重算小計與彈窗底部之「訂單預估總額」。

**Non-Goals:**
- 不變更後端 API 與資料庫綱要（既有 `POST /api/order` 完全支援並能接收正確資料）。

## Decisions

### 1. 選擇客戶即時連動機制
- **決定**：在 `<select id="formCustomerId">` 加入 `onchange="handleCustomerSelectChange(this)"`。直接自已在記憶體中快取的 `customerList` 陣列尋找對應的客戶物件，零延遲自動填入收件人姓名、電話、地址及業務員工。
- **地址組合規則**：
  `[c.country, c.city, c.district, c.address].filter(Boolean).join(' ')`，若僅有 `address` 則直接使用 `c.address`。
- **業務員工連動**：
  若該客戶有配置 `employeeId`，自動將 `formEmployeeId.value` 指定為該 ID。

### 2. 明細品項即時計算與總金額呈現
- **決定**：
  - 明細輸入行加入小計欄位：`<span class="detail-subtotal" style="flex:1; text-align:right; font-weight:600;">NT$ 0</span>`。
  - 明細表頭同步調整為五欄配置：`產品編號 / 品項名稱 *` (flex: 2)、`數量 *` (flex: 1)、`折讓 (NT$) *` (flex: 1)、`小計 (NT$)` (flex: 1)、`操作` (width: 32px)。
  - 定義 `recalcCreateModalTotal()` 函式：
    遍歷 `#detailRowsContainer .detail-input-row`，根據選中產品的 `data-price`、`quantity` 與 `discount` 計算 `subtotal = Math.max(0, (unitPrice * qty) - discount)`，更新行內小計並累加至彈窗底部的 `#createModalTotal`。
  - 事件綁定：
    - 產品選單：`onchange="recalcCreateModalTotal()"`
    - 數量輸入：`oninput="recalcCreateModalTotal()"`
    - 折讓選單：`onchange="recalcCreateModalTotal()"`
    - 新增品項與刪除品項：執行後立即呼叫 `recalcCreateModalTotal()`。

## Risks / Trade-offs

- **[Risk] 客戶地址欄位部分為空** → **Mitigation**: 使用 `filter(Boolean).join(' ')` 安全組合，且收件人欄位維持可手動編輯，使用者仍可自行微調。
- **[Risk] 產品尚未選取時的計算** → **Mitigation**: 若未選取產品，單價預設為 0，小計顯示 NT$ 0，避免出現 NaN。
