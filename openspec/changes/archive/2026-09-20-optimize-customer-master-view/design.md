# Design

## Context

參閱 `proposal.md`。目前 `customer` 資料表包含 13 個欄位（`customer_id`、`customer_name`、`phone`、`email`、`company_name`、`tax_id`、`country`、`city`、`district`、`address`、`employee_id`、`created_at`、`updated_at`）。後端 `CustomerServiceImpl` 目前在建立客戶時仍接受前端傳入之 `customerId`，且缺少電話、統一編號與電子郵件的重複性檢核；`GlobalExceptionHandler` 之 `FIELD_NAME_MAP` 缺少客戶欄位（如 `taxId`、`companyName`、`country`、`city`、`district`、`address`）的中文對照；前端 `customer.html` 列表遺漏了 `email` 與 `country` 欄位，彈窗未提供 `country` 輸入介面且業務欄位為自由文字輸入，且未針對重複欄位提供聚焦高亮導引。

## Goals / Non-Goals

**Goals:**
- **完整欄位呈現**：視圖模組（表格清單與新增/修改彈窗）完整呈現資料庫中除了 `updated_at` 以外的所有欄位，補齊 `country`（國家）與 `email`（電子郵件）欄位，並將業務窗口提升為員工下拉式選單。
- **客戶編號系統配置**：前端新增客戶時客戶編號輸入框完全禁用（disabled）並標示「由系統自動配置」；後端新增客戶時無論是否傳入 ID，一律由系統依當月年月前綴（`yyyyMM`）加流水號（`001`、`002`...）自動產生，並進行防碰撞檢核。
- **明確告知重複欄位**：新增與修改客戶時，後端主動檢核 `phone`（客戶電話）、`taxId`（統一編號，若有填寫）、`email`（電子郵件，若有填寫）與 `customerId`（客戶編號）之唯一性（修改時排除自身），若重複拋出明確 400 錯誤；前端表單遭遇錯誤時彈窗明確提示並自動聚焦高亮該欄位。
- **例外處理健全**：擴充 `GlobalExceptionHandler.FIELD_NAME_MAP`，確保客戶欄位驗證失敗時訊息清晰友善。

**Non-Goals:**
- 修改資料庫 Schema 或增刪資料表欄位（現有 13 個欄位已足夠）。
- 變更訂單或員工等其他模組的既有商業邏輯。

## Decisions

### 1. 客戶編號自動配置機制
- **設計選擇**：後端採用依當年月 `yyyyMM` 為前綴，查詢目前以此前綴開頭的最大客戶編號，若無則以 `yyyyMM001` 起算；若有則取後 3 碼流水序號遞增 +1，並透過 `while (selectById(id) != null)` 迴圈防碰撞，最終自動寫入資料庫。
- **備選方案**：
  - 由前端生成：存在偽造與併發衝突風險，不符需求。
  - 使用 UUID：編號過長且不符合 ERP 傳統易讀性要求。
- **前端呈現**：新增客戶時輸入框設為 `disabled`，placeholder 設為「由系統自動配置」；修改客戶時亦為 `disabled` 唯讀顯示客戶編號。

### 2. 重複性檢核與明確例外提示
- **設計選擇**：在 `CustomerServiceImpl` 的 `create` 與 `update` 方法中，於資料庫寫入前透過 `LambdaQueryWrapper` 檢核：
  - `phone`：非空時檢查是否已被其他客戶使用。若重複拋出 `BusinessException(400, "客戶電話已存在: " + phone)`。
  - `taxId`：非空且有內容時檢查是否已被其他客戶使用。若重複拋出 `BusinessException(400, "客戶統一編號已存在: " + taxId)`。
  - `email`：非空且有內容時檢查是否已被其他客戶使用。若重複拋出 `BusinessException(400, "客戶電子郵件已存在: " + email)`。
  - 修改時均加入 `.ne(Customer::getCustomerId, customerId)` 排除自身。
- **前端錯誤反饋**：在 `customer.html` 中實作 `highlightErrorField(elementId)` 函式，解析錯誤訊息並將游標聚焦（focus）與以紅色框線動畫高亮顯示問題欄位（如 `formPhone`、`formTaxId`、`formEmail`）。

### 3. 視圖模組欄位完整呈現（排除修改時間）
- **表格呈現**：
  - 客戶編號 (`customerId`)
  - 客戶姓名 (`customerName`)
  - 聯絡電話 (`phone`)
  - 電子郵件 (`email`)
  - 公司抬頭 (`companyName`)
  - 統一編號 (`taxId`)
  - 國家 (`country`)
  - 所在城市與區域 (`city` + `district`)
  - 詳細地址 (`address`)
  - 專屬業務 (`employeeName (employeeId)`)
  - 建立時間 (`createdAt`)
  - 操作（修改、刪除）
  - **排除 `updatedAt`**（修改時間）。
- **彈窗表單呈現**：
  - 補齊「國家 (`formCountry`)」欄位，預設帶入「台灣」。
  - 「負責員工 (`formEmployeeId`)」改為 `<select>` 下拉選單，頁面載入時呼叫 `/api/employee/page?current=1&size=100` 動態載入員工主檔清單。
  - 欄位同步於新增客戶與修改客戶。

### 4. 全域例外攔截與欄位對照擴充
- 在 `GlobalExceptionHandler.FIELD_NAME_MAP` 擴充：
  - `taxId` -> "統一編號"
  - `companyName` -> "公司抬頭"
  - `country` -> "國家"
  - `city` -> "城市"
  - `district` -> "區域"
  - `address` -> "詳細地址"

## Risks / Trade-offs

- **[Risk] 舊資料中統一編號或信箱為空字串** → 檢核時判斷 `StringUtils.hasText(...)`，僅在使用者有輸入非空白內容時才進行唯一性檢核，避免多筆空值產生假性衝突。
- **[Risk] 表格欄位增多導致寬度不足** → 表格容器已有 `.table-card` 橫向滾動與彈性寬度佈局支援，關鍵欄位設定清晰對齊。
