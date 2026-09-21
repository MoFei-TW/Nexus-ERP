# Proposal

## Why

當使用者在產品主檔視圖進行新增或修改產品時，若輸入了已存在的產品名稱（觸發資料庫唯一鍵條件約束 `UK_product_name`），由於目前後端未在業務層事前校驗，且全域例外處理器未專門攔截 Spring 的 `DuplicateKeyException` / `DataIntegrityViolationException`，導致例外直接落入通用處理器回傳 HTTP 500「系統內部錯誤，請聯繫管理員」。使用者無法得知是「產品名稱重複」，嚴重影響系統可用性與防呆體驗。

## What Changes

- **產品服務層唯一性事前檢核 (`ProductServiceImpl`)**：
  - 新增產品時，事前檢核 `productName` 是否已存在；若重複則主動拋出 `BusinessException(400, "產品名稱已存在: " + productName)`。
  - 修改產品時，若變更產品名稱，檢核是否有其他產品使用相同名稱；若重複則主動拋出 `BusinessException(400, "產品名稱已存在: " + productName)`。
- **全域唯一鍵例外攔截增強 (`GlobalExceptionHandler`)**：
  - 新增對 `DuplicateKeyException` 與 `DataIntegrityViolationException` 的專屬處理，解析底層條件約束名稱（如 `UK_product_name` 解析為「產品名稱已存在」，其他主檔如電話、統一編號等亦做對應解析），回傳 HTTP 400 與明確的欄位重複訊息，杜絕任何唯一性衝突拋出 500 錯誤。
- **前端產品主檔錯誤導引優化 (`product.html`)**：
  - 確保收到「產品名稱已存在」等重複錯誤訊息時，前端明確彈窗告知「操作失敗: 產品名稱已存在」，並自動聚焦且高亮產品名稱輸入框。

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `mofei-erp-master-data`: 擴充產品主檔規範，明確要求新增與修改產品時必須校驗產品名稱唯一性；當產品名稱重複或違反唯一鍵條件約束時，系統必須以 HTTP 400 明確拒絕並告知使用者「產品名稱已存在」，不得回傳 500 系統內部錯誤。

## Impact

- **後端程式碼**：
  - `ProductServiceImpl.java`: 增加新增與修改產品名稱重複檢核邏輯。
  - `GlobalExceptionHandler.java`: 增加 `DuplicateKeyException` / `DataIntegrityViolationException` 例外攔截與條件約束名稱轉譯。
- **前端視圖**：
  - `templates/product.html`: 確保重複名稱錯誤能精準對焦產品名稱欄位。
- **測試案例**：
  - `MasterDataApiTest.java`: 增補新增與修改重複產品名稱之單元測試與整合測試。
