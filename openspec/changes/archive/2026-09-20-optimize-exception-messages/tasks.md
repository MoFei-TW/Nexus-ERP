# Tasks

## 1. 後端例外處理器強化 (GlobalExceptionHandler)

- [x] 1.1 於 `GlobalExceptionHandler.java` 中新增 `@ExceptionHandler(HttpMessageNotReadableException.class)`，解析欄位名稱（如 `price`）並回傳 HTTP 400 格式化錯誤訊息（如「欄位格式錯誤: 產品價格 輸入格式不正確」）。
- [x] 1.2 優化 `GlobalExceptionHandler.java` 中的 `MethodArgumentNotValidException` 與 `BindException` 訊息格式，確保格式為「[欄位名稱] [錯誤描述]」。
- [x] 1.3 執行 `mvnw clean compile` 與單元測試，確保專案編譯乾淨且無殘留舊 class。

## 2. 前端產品表單錯誤提示與欄位導引 (product.html)

- [x] 2.1 優化 `product.html` 中的表單驗證與提交流程，在發生錯誤時自動定位聚焦（focus）至問題欄位。
- [x] 2.2 強化後端錯誤代碼解析，若收到非 200 回應時，清楚將具體欄位錯誤呈現在使用者畫面上。

## 3. 整合驗證與重啟服務

- [x] 3.1 於 `MasterDataApiTest.java` 增補針對欄位格式錯誤與反序列化型態錯誤的測試案例並驗證通過。
- [x] 3.2 重啟背景 Spring Boot 服務，透過真實請求驗證產品新增與修改的欄位錯誤提示效果。
