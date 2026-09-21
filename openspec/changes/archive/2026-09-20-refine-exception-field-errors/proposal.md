# Proposal

## Why

目前系統在新增或修改資料（例如客戶主檔）時，若使用者輸入的內容違反資料庫限制（如 `CK_customer_email` 電子郵件格式檢查、`CK_customer_phone` 電話格式檢查、`CK_customer_tax_id` 統一編號格式檢查、`PK_customer` 主鍵衝突或 `FK_customer_employee` 外鍵檢查），`GlobalExceptionHandler` 目前會將所有未比對到的 `DataIntegrityViolationException` 均回傳為模糊的通用提示：`「資料已存在，請檢查輸入內容是否重複」`。這導致即使是信箱格式錯誤或外鍵問題，使用者也看到「資料已存在」，無法得知究竟是哪一項欄位輸入錯誤。使用者明確要求杜絕此種模糊訊息，系統必須明確指出是哪一項欄位輸入錯誤。

## What Changes

- **徹底消除模糊的「資料已存在」訊息**：
  - 移除 `GlobalExceptionHandler` 中「資料已存在，請檢查輸入內容是否重複」的兜底文字，嚴格要求所有約束衝突必須解析出具體的「錯誤欄位」與「錯誤原因」。
- **全方位解析資料庫約束（CHECK / FOREIGN KEY / PRIMARY KEY / UNIQUE KEY）**：
  - **CHECK 格式約束**：
    - `CK_customer_email`: 明確指出「電子郵件格式不正確」
    - `CK_customer_phone`: 明確指出「聯絡電話格式不正確（格式須為 0912-345-678）」
    - `CK_customer_tax_id`: 明確指出「統一編號格式不正確（格式須為 8888-8888）」
    - `CK_product_price`: 明確指出「產品價格格式不正確」
    - `CK_product_stock`: 明確指出「庫存數量格式不正確」
    - `CK_product_status`: 明確指出「產品狀態不正確」
  - **FOREIGN KEY 外鍵約束**：
    - `FK_customer_employee` / `FK_product_employee` / `FK_orders_employee`: 明確指出「負責員工編號不存在」
    - `FK_orders_customer`: 明確指出「客戶編號不存在」
    - `FK_order_detail_product`: 明確指出「產品編號不存在」
  - **PRIMARY KEY / UNIQUE KEY 唯一鍵約束**：
    - `PK_customer`: 明確指出「客戶編號已存在」
    - `PK_employee`: 明確指出「員工編號已存在」
    - `PK_product`: 明確指出「產品編號已存在」
    - `PK_orders`: 明確指出「訂單編號已存在」
    - `UK_customer_phone`: 明確指出「客戶電話已存在」
    - `UK_customer_tax_id`: 明確指出「客戶統一編號已存在」
    - `UK_employee_email`: 明確指出「員工電子郵件已存在」
    - `UK_employee_phone`: 明確指出「員工電話已存在」
    - `UK_product_name`: 明確指出「產品名稱已存在」
  - **動態欄位解析與轉換**：當遇到未具名列舉的資料庫約束時，自動從錯誤訊息（如 `column 'email'`、`column 'phone'`）中抽取受影響的欄位名稱，並透過 `FIELD_NAME_MAP` 轉換為中文欄位名稱（如「電子郵件輸入內容不符合規範」），確保任何情況下都能明確指出問題欄位。
- **前端錯誤反饋與定位聚焦強化** (`customer.html`)：
  - 前端接收到明確的欄位錯誤提示後，跳出 Alert 顯示具體欄位錯誤，並精準自動聚焦（focus）與以紅色框線高亮標註該欄位。

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `mofei-erp-master-data`: 規範系統在任何資料建立或修改時，若發生資料庫約束（CHECK、FK、PK、UK）衝突或驗證失敗，必須明確指出具體錯誤欄位與原因，嚴禁返回模糊的「資料已存在，請檢查輸入內容是否重複」。

## Impact

- **後端程式碼**：
  - `GlobalExceptionHandler.java`: 重構 `resolveConstraintErrorMessage`，全面支援 MSSQL 各類約束名稱（CHECK, FK, PK, UQ）解析與欄位名稱正則抽取，消除模糊回傳文字。
- **前端視圖與腳本**：
  - `templates/customer.html`: 增強錯誤訊息欄位識別與高亮邏輯，支援格式錯誤與外鍵錯誤的精準對應。
- **測試案例**：
  - `MasterDataApiTest.java`: 補充各類資料庫約束（CHECK 條件約束、外鍵約束、主鍵約束）轉換為明確欄位錯誤訊息的驗證測試。
