# Tasks

## 1. 後端客戶編號系統配置與流水號生成

- [x] 1.1 在 `CustomerServiceImpl.java` 中實作客戶編號自動配置邏輯（依當前年月 `yyyyMM` 搭配流水號如 `202609001` 依序推進），新增客戶時不接受或覆寫使用者自訂 ID，並加入防碰撞迴圈檢核，透過單元測試驗證自動配置生效。

## 2. 後端重複性檢核與例外處理強化

- [x] 2.1 在 `CustomerServiceImpl.java` 的 `create` 與 `update` 方法中實作電話（`phone`）、統一編號（`taxId`，有填寫時）、電子郵件（`email`，有填寫時）的唯一性檢核（修改時排除自身），若重複拋出明確 400 錯誤（如「客戶電話已存在: [電話]」），透過測試驗證重複例外回傳。
- [x] 2.2 在 `GlobalExceptionHandler.java` 之 `FIELD_NAME_MAP` 擴充客戶欄位對照（`taxId`、`companyName`、`country`、`city`、`district`、`address`），確保欄位校驗錯誤訊息精確易懂。

## 3. 前端客戶主檔視圖與表單優化

- [x] 3.1 在 `templates/customer.html` 資料表格中完整呈現資料庫欄位（補齊電子郵件 `email`、國家 `country` 等，排除修改時間 `updatedAt`），確認各欄位正確對齊與渲染。
- [x] 3.2 在 `templates/customer.html` 彈窗表單中將客戶編號設為禁用（disabled）並提示「由系統自動配置」，補齊「國家」輸入欄位（預設「台灣」），並將負責員工欄位升級為動態非同步載入之下拉選單（`/api/employee/page`）。
- [x] 3.3 在 `templates/customer.html` 中實作錯誤反饋機制，新增客戶與修改客戶發生重複或參數錯誤時跳出明確 Alert 提示，並透過 `highlightErrorField` 自動聚焦與紅色高亮問題欄位。

## 4. 測試與驗證

- [x] 4.1 在 `MasterDataApiTest.java` 中新增客戶自動編號、電話重複檢核、統編重複檢核、電子郵件重複檢核、修改排除自身等 API 測試案例，執行測試確認全數通過。
- [x] 4.2 驗證 `http://localhost:8080/customer` 前端視圖模組各項操作流程（完整欄位檢視、新增客戶自動編號、重複欄位提示與聚焦、修改客戶資料同步更新）。
