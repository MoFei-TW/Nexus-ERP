# Design

## Context

參閱 `proposal.md`。在 Spring Boot 與 MSSQL 環境中，當觸發 CHECK 條件約束（如 `CK_customer_email`、`CK_customer_phone`、`CK_customer_tax_id`）、FOREIGN KEY 外鍵約束（如 `FK_customer_employee`）、PRIMARY KEY 主鍵約束（如 `PK_customer`）或 UNIQUE 唯一鍵約束時，Spring 均會包裝為 `DataIntegrityViolationException` 或 `DuplicateKeyException`。

先前 `GlobalExceptionHandler.resolveConstraintErrorMessage` 僅對少數 `UK_` 進行比對，其餘情況一律 fallback 回傳：
```
"資料已存在，請檢查輸入內容是否重複"
```
這導致即使是使用者輸入了格式錯誤的電子郵件（觸發 `CK_customer_email`）或格式錯誤的電話（觸發 `CK_customer_phone`），系統也會彈出「資料已存在」的嚴重誤導訊息，使用者完全無法辨識哪一項欄位有問題。

## Goals / Non-Goals

**Goals:**
- **杜絕模糊回傳文字**：徹底廢除「資料已存在，請檢查輸入內容是否重複」兜底提示，任何約束衝突必須精確指出「哪一個欄位」以及「錯誤原因」。
- **精準覆蓋所有資料庫約束**：
  - **CHECK 條件約束（格式錯誤）**：
    - `CK_customer_email` → `電子郵件格式不正確（需包含 @ 與有效網域名稱）`
    - `CK_customer_phone` → `聯絡電話格式不正確（格式須為 0912-345-678）`
    - `CK_customer_tax_id` → `統一編號格式不正確（格式須為 8888-8888）`
    - `CK_product_price` → `產品價格格式不正確（必須大於或等於0）`
    - `CK_product_stock` → `庫存數量格式不正確（必須大於或等於0）`
    - `CK_product_status` → `產品狀態不正確`
  - **FOREIGN KEY 外鍵約束（關聯不存在）**：
    - `FK_customer_employee` / `FK_product_employee` / `FK_orders_employee` → `負責員工編號不存在`
    - `FK_orders_customer` → `客戶編號不存在`
    - `FK_order_detail_orders` / `FK_order_detail_product` → `關聯之訂單或產品不存在`
  - **PRIMARY KEY / UNIQUE KEY 唯一性約束（資料重複）**：
    - `PK_customer` → `客戶編號已存在`
    - `PK_employee` → `員工編號已存在`
    - `PK_product` → `產品編號已存在`
    - `PK_orders` → `訂單編號已存在`
    - `UK_customer_phone` → `客戶電話已存在`
    - `UK_customer_tax_id` → `客戶統一編號已存在`
    - `UK_employee_phone` → `員工電話已存在`
    - `UK_employee_email` → `員工電子郵件已存在`
    - `UK_product_name` → `產品名稱已存在`
- **動態欄位解析機制**：若遇到未列舉之資料庫例外，透過正則表達式從例外字串中解析 `column '([^']+)'` 或約束名稱，轉換成中文欄位並回傳 `"[欄位名稱] 輸入內容不符合規範"`。
- **前端自動聚焦與高亮對齊**：前端視圖收到具體欄位錯誤訊息後，跳出精準 Alert，並自動將游標聚焦（focus）與高亮標註該輸入框。

**Non-Goals:**
- 修改資料庫 DDL 或刪除現有資料庫 CHECK/FK 約束。

## Decisions

### 1. 例外訊息階層式精準解析 (`GlobalExceptionHandler.java`)
- **第一層：特定約束代碼直接比對**
  - 比對 `CK_customer_email`、`CK_customer_phone`、`CK_customer_tax_id`、`FK_customer_employee`、`PK_customer` 等已知約束代碼，回傳最白話、最友善的錯誤訊息。
- **第二層：MSSQL 錯誤訊息正則抽取**
  - MSSQL 訊息範例：
    - `The INSERT statement conflicted with the CHECK constraint "CK_...". The conflict occurred in database "MOFEI", table "dbo.customer", column 'email'.`
    - `The INSERT statement conflicted with the FOREIGN KEY constraint "FK_...". ... column 'employee_id'.`
    - `Violation of PRIMARY KEY constraint 'PK_...'. Cannot insert duplicate key in object 'dbo.customer'. The duplicate key value is (...).`
  - 正則匹配 `column '([a-zA-Z0-9_]+)'` 或 `column "([a-zA-Z0-9_]+)"`：
    - 取得欄位名稱（如 `email`、`phone`、`tax_id`、`employee_id`）
    - 將底線轉為駝峰命名後透過 `FIELD_NAME_MAP` 映射為繁體中文（如 `電子郵件`、`聯絡電話`、`統一編號`、`負責員工`）
    - 根據約束類型（CHECK、FOREIGN KEY、PRIMARY KEY、UNIQUE）組合回傳訊息（如：`"電子郵件 輸入格式不正確"`、`"負責員工 資料不存在"`、`"客戶編號 已存在"`）。
- **第三層：安全兜底**
  - 若完全無法抽取欄位，回傳 `"輸入欄位資料不符合限制規範，請檢查輸入內容"`，絕不回傳「資料已存在，請檢查輸入內容是否重複」。

### 2. 前端高亮識別規則健全化 (`customer.html`)
- 在 `highlightErrorField` 的觸發邏輯中，針對包含 `電子郵件`、`email`、`電話`、`phone`、`統一編號`、`taxId`、`客戶編號`、`customerId`、`員工`、`employeeId` 等各類關鍵字精確映射至各 input/select 元素 ID，並自動聚焦。
