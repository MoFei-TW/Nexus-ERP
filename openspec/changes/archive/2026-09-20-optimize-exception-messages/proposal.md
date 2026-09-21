# Proposal

## Why

目前使用者在產品主檔視圖（http://localhost:8080/product）進行新增或修改產品時，若欄位格式不正確、反序列化異常或發生伺服器錯誤，系統會跳出模糊的「系統內部錯誤，請聯繫管理員」，或是未精準標記錯誤欄位，導致使用者無法得知具體是哪一項欄位輸入有誤。為了與員工主檔模組體驗一致並大幅提升系統防呆能力，必須優化全域例外處理機制與前端錯誤提示，明確告知使用者哪一項欄位輸入有誤。

## What Changes

- **後端全域例外處理器增強 (`GlobalExceptionHandler`)**：
  - 新增對 `HttpMessageNotReadableException`（參數解析/型態不符）的專屬處理，解析出具體發生錯誤的欄位名稱（如 `price`），回傳 400 狀態碼與明確訊息「欄位格式錯誤: [欄位名稱] 輸入格式不正確」。
  - 優化 `MethodArgumentNotValidException` 與 `BindException` 訊息格式，將各欄位的驗證錯誤明確對應至中文欄位名稱（例如「產品名稱不能為空」、「產品價格必須為整數且大於或等於0」）。
  - 優化例外訊息結構，回傳明確的欄位錯誤資訊。
- **前端產品視圖錯誤提示與欄位定位優化 (`product.html`)**：
  - 表單送出前強化前端防呆驗證，若欄位檢驗失敗，彈出明確提示並自動聚焦（`focus()`）該錯誤輸入框。
  - 當後端回傳錯誤時，精確解析錯誤訊息並明確告知使用者是哪一項欄位輸入有誤，避免顯示模糊的通用內部錯誤。
- **系統編譯產物清理與重新建置**：
  - 確保伺服器運行乾淨且最新的 class 檔案，消除任何編譯期遺留問題導致的 500 伺服器錯誤。

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `mofei-erp-master-data`: 更新主檔例外處理規範，明確要求新增與修改產品發生任何驗證、型態或業務錯誤時，必須明確告知使用者具體錯誤欄位名稱與原因。

## Impact

- **後端程式碼**：
  - `GlobalExceptionHandler.java`: 增加 `HttpMessageNotReadableException` 等常見 Spring MVC 請求異常處理，提取具體錯誤欄位名稱。
- **前端視圖與腳本**：
  - `templates/product.html`: 優化表單驗證、錯誤提示字串與錯誤欄位聚焦高亮機制。
- **測試案例**：
  - `MasterDataApiTest.java`: 增補針對欄位格式錯誤、型態不符與空值檢驗之詳細錯誤訊息驗證測試。
