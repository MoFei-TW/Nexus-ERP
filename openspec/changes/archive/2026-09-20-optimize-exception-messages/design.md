# Design

## Context

在先前的操作中，當使用者於前端產品主檔視圖進行新增或修改時，若請求中包含無法解析的數值型態、或伺服器端發生非預期例外，系統直接進入 `GlobalExceptionHandler` 的 `Exception.class` 通用處理器，回傳 HTTP 500 及「系統內部錯誤，請聯繫管理員」。這種模糊的錯誤訊息無法讓使用者了解到底是哪個欄位填寫有誤，違反了防呆與明確回饋原則。

## Goals / Non-Goals

**Goals:**
- 在後端 `GlobalExceptionHandler` 中攔截 `HttpMessageNotReadableException`，解析出具體發生反序列化或型態錯誤的欄位（例如 `price` 或 `stock`），回傳 HTTP 400 及明確訊息「欄位格式錯誤: [欄位名稱] 輸入格式不正確」。
- 統一後端驗證例外訊息，明確指出欄位名稱與驗證限制。
- 前端 `product.html` 表單提交前加強前端驗證與提示，發生錯誤時自動定位並聚焦（focus）到錯誤欄位。
- 透過 Maven 乾淨重新編譯（`mvnw clean compile`），確保執行期 JVM 載入最新無編譯缺陷之 Class 檔，消除 500 系統錯誤。

**Non-Goals:**
- 不改變既有產品資料庫綱要與業務欄位定義。

## Decisions

### 1. 後端例外攔截細緻化 (`GlobalExceptionHandler`)
- **決策**：
  - 新增 `@ExceptionHandler(HttpMessageNotReadableException.class)`：
    - 若底層原因是 `InvalidFormatException`，解析 `getPath()` 取得欄位名稱（如 `price`），轉譯為中文「產品價格」並提示「欄位格式錯誤: 產品價格 必須為整數」。
    - 回傳 HTTP 400 與格式化訊息。
  - 優化 `MethodArgumentNotValidException`：
    - 逐一彙整欄位錯誤：`[欄位名稱]: [錯誤描述]`。

### 2. 前端錯誤反饋與欄位聚焦 (`product.html`)
- **決策**：
  - 表單提交前若驗證失敗，依序檢查名稱、價格、庫存、員工，彈出具體提示「輸入有誤：[欄位說明]」並呼叫 `focus()`。
  - 當後端回應非 200 時，解析 `result.message`，若包含對應欄位關鍵字（如「價格」、「員工」、「名稱」、「庫存」），自動聚焦該輸入元件並提示使用者修正。

### 3. 編譯產物與服務重啟
- **決策**：
  - 於實作階段執行 `mvnw clean compile`，重啟後端 Spring Boot 服務，確保執行環境與最新原始碼 100% 同步。

## Risks / Trade-offs

- **[Risk]** Jackson 巢狀路徑或特殊例外可能無具體欄位。
  - **Mitigation**：預設 fallback 為「請確認請求參數格式是否正確」，並記錄詳細 WARN 日誌。
