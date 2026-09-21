# Tasks

## 1. 後端查詢排序調整

- [x] 1.1 修改 `CustomerServiceImpl.java` 中的 `queryPage` 方法，將 `wrapper.orderByDesc(Customer::getCreatedAt)` 調整為 `wrapper.orderByAsc(Customer::getCustomerId)`，確保客戶分頁查詢預設依客戶編號由小到大（升冪 ASC）排序。

## 2. 測試與驗證

- [x] 2.1 在 `MasterDataApiTest.java` 中新增或更新針對客戶分頁查詢的測試案例，執行 `mvn test` 驗證回傳的客戶資料列表嚴格依客戶編號由小到大排列。
- [x] 2.2 重新編譯並重啟服務，於前端客戶主檔模組視圖（`http://localhost:8080/customer`）驗證表格清單首欄客戶編號依由小到大順序呈現。
