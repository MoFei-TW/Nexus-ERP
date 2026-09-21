# Tasks

## 1. 後端服務優化 (Backend Service Enhancements)

- [x] 1.1 在 `EmployeeServiceImpl.java` 中將員工分頁查詢排序改為依員工編號遞增 (`orderByAsc(Employee::getEmployeeId)`)，並透過單元測試驗證查詢結果依編號由小到大排列。
- [x] 1.2 在 `EmployeeServiceImpl.java` 中實作新增員工由系統自動配置最新流水編號邏輯（忽略傳入之編號），並驗證新增時自動產生正確編號。
- [x] 1.3 在 `EmployeeServiceImpl.java` 中新增員工電話與電子郵件重複性檢核（若已存在則拋出 400 BusinessException），並透過單元測試驗證編號、電話、信箱重複時均正確阻擋並回傳錯誤訊息。
- [x] 1.4 在 `EmployeeServiceImpl.java` 中新增修改員工時對員工編號、員工電話與電子郵件之重複性檢核（排除自身，若已存在則拋出 400 BusinessException），並透過單元測試驗證編號、電話、信箱重複時均正確阻擋並回傳錯誤訊息。

## 2. 前端介面優化 (Frontend UI Optimizations)

- [x] 2.1 修改 `src/main/resources/templates/employee.html`，將表格系統角色欄位從 `<span class="badge">${row.role}</span>` 調整為純文字渲染，並於瀏覽器檢視確認無 badge 標籤。
- [x] 2.2 修改 `src/main/resources/templates/employee.html` 之新增員工彈窗，將員工編號輸入框設為唯讀/停用（disabled）並顯示提示「由系統自動配置」，防止使用者手動輸入。

## 3. 整合驗證與測試 (Integration & Verification)

- [x] 3.1 執行後端完整測試（`mvn test`），確保所有既有測試與新增之員工主檔測試案例全數通過。
