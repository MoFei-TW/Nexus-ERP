# Design

## Context

參見 `proposal.md`。使用者在產品主檔視圖進行新增或修改時，若輸入已存在的產品名稱，MSSQL 拋出 `SQLServerException: 違反 UNIQUE KEY 條件約束 'UK_product_name'`，在 Spring Boot 框架中被封裝為 `DuplicateKeyException`（或 `DataIntegrityViolationException`）。由於先前未在業務層（`ProductServiceImpl`）進行事前重複校驗，且全域例外處理器（`GlobalExceptionHandler`）未專屬處理唯一鍵例外，導致例外直接穿透至通用處理器，回傳 HTTP 500「系統內部錯誤，請聯繫管理員」，前端亦顯示模糊的「操作失敗: 系統內部錯誤，請聯繫管理員」。

## Goals / Non-Goals

**Goals:**
- **業務層事前校驗**：在 `ProductServiceImpl.create` 與 `update` 中，主動檢查產品名稱是否重複，重複時主動拋出 `BusinessException(400, "產品名稱已存在: " + productName)`。
- **全域唯一鍵例外攔截**：在 `GlobalExceptionHandler` 中新增 `@ExceptionHandler({DuplicateKeyException.class, DataIntegrityViolationException.class})`，解析底層資料庫條件約束（如 `UK_product_name`），回傳 HTTP 400 及明確訊息「產品名稱已存在」，徹底消滅因重複值引發的 500 系統錯誤。
- **前端錯誤導引與高亮**：確保 `product.html` 在收到「產品名稱已存在」等訊息時，精準彈出提示並自動聚焦（`focus()`）且高亮產品名稱輸入欄位。

**Non-Goals:**
- 不變更資料庫綱要與現有唯一鍵（`UK_product_name` 等）名稱。

## Decisions

### 1. 雙層防護架構（業務層事前防呆 + 全域例外攔截兜底）
- **決策**：
  - **第一層（業務層）**：在 `ProductServiceImpl` 的 `create` 與 `update` 呼叫 `productMapper.selectCount` 檢查同名產品（`update` 需排除自身 `productId`）。若已存在，直接拋出具體 `BusinessException`。
  - **第二層（全域例外層）**：在 `GlobalExceptionHandler` 捕獲 `DuplicateKeyException` 與 `DataIntegrityViolationException`，以遞迴解析 `getCause()` 尋找條件約束名稱（Constraint Name）或錯誤特徵，作為高併發或偶發穿透時的防護兜底，確保一律回傳 HTTP 400。

### 2. 約束名稱字典轉譯機制
- **決策**：在 `GlobalExceptionHandler` 中維護常見唯一鍵約束轉譯對照：
  - `UK_product_name` 或包含 `product` 與 `name` → 「產品名稱已存在」
  - `UK_customer_tax_id` → 「客戶統一編號已存在」
  - `UK_customer_phone` → 「客戶電話已存在」
  - `UK_employee_phone` → 「員工電話已存在」
  - `UK_employee_email` → 「員工電子郵件已存在」
  - 若無法辨識特定約束名稱，預設回傳「資料已存在，請檢查輸入內容是否重複」。

### 3. 前端焦點導引 (`product.html`)
- **決策**：
  - 前端收到包含「產品名稱已存在」或「名稱」之錯誤字串時，呼叫 `highlightErrorField('formProductName')`，提供即時聚焦與 3 秒紅框反饋。

## Risks / Trade-offs

- **[Risk]** 巢狀例外結構不同（例如某些情況為 `DataIntegrityViolationException` 包裹 `SQLServerException`，某些為 `DuplicateKeyException`）。
  - **Mitigation**：同時攔截 `DuplicateKeyException` 與 `DataIntegrityViolationException`，並透過輔助方法走訪整個例外鏈（`while (cause != null)`）尋找約束名稱或「重複」關鍵字。
