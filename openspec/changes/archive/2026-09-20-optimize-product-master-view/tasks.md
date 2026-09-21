# Tasks

## 1. 後端產品模組排序與資料校驗

- [x] 1.1 調整 `ProductServiceImpl.queryPage` 查詢邏輯，將預設排序由 `createdAt DESC` 改為 `productId ASC`，並透過單元測試驗證分頁排序為產品編號由小到大。
- [x] 1.2 調整 `ProductServiceImpl.create` 產品編號自動配置機制，不接受使用者自訂編號，自動生成不重複之系統編號，並撰寫/驗證自動配置之測試案例。
- [x] 1.3 更新 `ProductDTO`，針對產品價格欄位增加整數檢核（`@Digits(integer = 10, fraction = 0, message = "產品價格必須為整數")` 與 `@DecimalMin(value = "0", message = "產品價格必須大於或等於0")`），並確保各欄位有明確的錯誤提示訊息。
- [x] 1.4 調整 `ProductServiceImpl.create` 與 `update` 中的負責員工檢核邏輯，若員工不存在時拋出明確例外（如 `負責員工編號不存在: xxx`），並驗證修改時不可更動產品編號。

## 2. 前端產品主檔視圖優化 (product.html)

- [x] 2.1 修改 `product.html` 中的產品新增與編輯彈窗，將 `formProductId` 設為唯讀/禁用狀態，並提示「由系統自動配置」。
- [x] 2.2 修改 `formPrice` 價格輸入框，將 `step` 設為 `1`、`min` 設為 `0`，並於前端送出前將價格轉換為整數處理。
- [x] 2.3 將 `formEmployeeId` 文字輸入框改為下拉選單 `<select>`，並實作非同步載入員工主檔（`/api/employee/page?size=100`），選項顯示姓名與編號，支援新增與編輯時正確選取與回填。
- [x] 2.4 優化 `handleFormSubmit` 錯誤處理機制，精確解析後端回傳之 400 錯誤與欄位錯誤訊息，明確告知使用者哪一項欄位輸入有誤。

## 3. 測試與驗證

- [x] 3.1 於 `MasterDataApiTest.java` 補充與執行產品編號升冪排序、自動配置產品編號、價格整數驗證與錯誤欄位訊息之整合測試，確保全部通過。
- [x] 3.2 存取系統視圖（`http://localhost:8080/product`），驗證新增產品、修改產品、下拉選單同步與例外錯誤提示之操作流程。
