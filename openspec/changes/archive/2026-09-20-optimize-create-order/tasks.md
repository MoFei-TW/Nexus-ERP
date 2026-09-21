# Tasks

## 1. 選擇客戶自動帶入客戶資訊

- [x] 1.1 在 `templates/order.html` 的 `<select id="formCustomerId">` 加入 `onchange="handleCustomerSelectChange(this)"`，實作由選取客戶自動帶入收件人姓名、電話、地址與負責業務員工，並驗證切換客戶時表單欄位即時自動更新

## 2. 訂單明細即時試算與總金額呈現

- [x] 2.1 在 `templates/order.html` 的新增訂單明細區域加入表頭「小計 (NT$)」與每行小計顯示元素，並於產品、數量、折讓變更時即時計算品項小計
- [x] 2.2 在 `templates/order.html` 的新增訂單彈窗底部新增「訂單預估總額」顯示，並實作 `recalcCreateModalTotal()` 函式，在品項新增、刪除或內容異動時即時連動計算整單總金額

## 3. 驗證與整合測試

- [x] 3.1 驗證新增訂單流程：測試選擇不同客戶時之資料自動填入，測試變更明細品項數量與折讓時總額即時試算，確認送出訂單後後端資料正確持久化
