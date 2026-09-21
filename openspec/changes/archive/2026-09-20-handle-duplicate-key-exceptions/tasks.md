# Tasks

## 1. 後端產品業務層與全域例外處理器強化

- [x] 1.1 於 `ProductServiceImpl.java` 的 `create` 與 `update` 方法中新增產品名稱唯一性檢核，若名稱重複則拋出 `BusinessException(400, "產品名稱已存在: " + dto.productName().trim())`。
- [x] 1.2 於 `GlobalExceptionHandler.java` 中新增 `@ExceptionHandler({DuplicateKeyException.class, DataIntegrityViolationException.class})`，解析資料庫唯一鍵條件約束（如 `UK_product_name` 轉譯為「產品名稱已存在」），回傳 HTTP 400 明確錯誤訊息，徹底杜絕 500 系統錯誤。

## 2. 前端產品主檔視圖與錯誤聚焦優化

- [x] 2.1 於 `product.html` 中優化錯誤處理機制，當後端回傳「產品名稱已存在」時，彈窗提示「操作失敗: 產品名稱已存在」並自動聚焦且高亮產品名稱輸入框。

## 3. 測試與驗證

- [x] 3.1 於 `MasterDataApiTest.java` 新增新增與修改產品名稱重複的測試案例，執行 `mvnw test -Dtest=MasterDataApiTest` 確認 400 狀態碼與「產品名稱已存在」訊息驗證通過。
- [x] 3.2 重新編譯並重啟背景 Spring Boot 服務，透過真實 API 測試重複產品名稱情境，確認不再出現 500 內部錯誤。
