# Tasks

## 1. 後端例外攔截與約束解析精準化

- [x] 1.1 在 `GlobalExceptionHandler.java` 中重構 `resolveConstraintErrorMessage` 方法，完整支援 MSSQL CHECK 條件約束（如 `CK_customer_email`、`CK_customer_phone`、`CK_customer_tax_id` 等）、FOREIGN KEY 外鍵約束（如 `FK_customer_employee` 等）、PRIMARY KEY/UNIQUE 約束，並實作動態 `column '...'` 正則抽取與中文映射，徹底移除「資料已存在，請檢查輸入內容是否重複」的模糊字串。

## 2. 前端錯誤反饋與欄位聚焦同步升級

- [x] 2.1 在 `templates/customer.html` 中優化錯誤處理邏輯，確保接收到格式約束或外鍵約束錯誤時，Alert 正確顯示並透過 `highlightErrorField` 精準聚焦與高亮該欄位。

## 3. 測試與驗證

- [x] 3.1 在 `MasterDataApiTest.java` 中新增針對 CHECK 格式約束（如電子郵件、電話）、外鍵約束等例外轉換的測試案例，執行 `mvn test` 驗證所有約束均正確解析為具體欄位錯誤訊息。
- [x] 3.2 驗證在前端畫面輸入格式不符的電子郵件或電話時，彈窗不再出現模糊的「資料已存在」訊息，而是明確指出哪一項欄位輸入錯誤並自動聚焦。
