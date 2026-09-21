# Proposal: 優化UI文字排版畫面與視圖完整渲染

## Why

目前 MOFEI ERP 系統的各個視圖模板（儀表板、員工、產品、客戶、訂單、登入頁面）在不同螢幕寬度或多欄位排版下，存在文字斷行、表格欄位名稱被擠壓折行/截斷、以及搜尋列與輸入框提示文字（Placeholder）因欄寬不足而無法完整顯示的問題。為了提升 ERP 系統的企業級使用者體驗與視覺一致性，需全面優化所有視圖模板的文字排版、表格渲染及輸入框尺寸規範。

## What Changes

- **文字排版防斷行規範**：
  - 表格標題（`th`）、操作按鈕（`.btn`）、狀態標籤（`.badge`）、表單標籤（`label`）、分頁資訊與導覽列元件，全面配置防不當折行（`white-space: nowrap`）。
  - 各模組之副標題、說明敘述與提示訊息採用結構化排版，避免文字破碎折行。
- **表格欄位完整渲染**：
  - 表格容器（`.table-card`）配置橫向滾動（`overflow-x: auto`），在寬度受限時保持表格欄位寬度與結構完整，防止欄位名稱遭到壓縮或折行。
  - 表格所有欄位名稱（`th`）完整呈現，並依欄位特性設定最適最小寬度（`min-width`），確保中英文標題、幣別符號與單位均完整呈現。
- **輸入框與提示訊息完整渲染**：
  - 調整搜尋列（`.search-bar`）與表單彈窗（Modal）中各輸入框（`.input-control` / `.select-control`）之最小寬度（`min-width`）與伸縮彈性（`flex`）。
  - 確保模糊搜尋、複合查詢與表單輸入框內的所有 Placeholder 提示訊息（如「搜尋客戶編號、姓名、統編、電話或地址...」）與預設值均完整可見，不被截斷。

## Capabilities

### New Capabilities
無。

### Modified Capabilities
- `mofei-erp-portal`: 新增視圖模板文字排版、表格欄位完整渲染與輸入框文字訊息完整渲染之規範與驗收場景。

## Impact

- **受影響檔案**：
  - 靜態樣式：`src/main/resources/static/css/mofei-style.css`
  - 前端表格控制：`src/main/resources/static/js/mofei-table.js`
  - 全域佈局與模板：`src/main/resources/templates/layout.html`
  - 各模組視圖模板：
    - `src/main/resources/templates/dashboard.html`
    - `src/main/resources/templates/employee.html`
    - `src/main/resources/templates/product.html`
    - `src/main/resources/templates/customer.html`
    - `src/main/resources/templates/order.html`
    - `src/main/resources/templates/login.html`
- **相依性與 API**：無需異動後端 Java 邏輯、REST API 或資料庫結構，維持純原生黑白灰極簡風格與 100% 離線可用性。
