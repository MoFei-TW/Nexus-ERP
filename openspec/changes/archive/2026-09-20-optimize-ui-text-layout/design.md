# Design: 優化UI文字排版畫面與視圖完整渲染

## Context

MOFEI ERP 採用純原生技術棧（Thymeleaf + 原生 HTML5/CSS3/Vanilla JS），無外部 CSS/JS 框架（如 Bootstrap、Tailwind 或 Vue），且維持極簡黑白灰階設計。
目前在多欄位表格（例如客戶主檔達 11 欄、訂單主檔達 9 欄）與多欄位搜尋列中，因樣式未限定非換行屬性與最小寬度，導致：
1. 表格標題（`th`）、表單標籤（`label`）、按鈕（`.btn`）在空間受限時產生不雅斷行（如「訂單總」/「額 (NT$)」）。
2. `.table-card` 採用 `overflow: hidden;`，在視窗縮小或欄位較多時壓縮欄位寬度，造成欄位名稱與資料被截斷或強制折行。
3. 搜尋列（`.search-bar`）中的 `.search-field` 預設 `min-width: 180px`，使得較長的 Placeholder（如「搜尋客戶編號、姓名、統編、電話或地址...」）被裁切無法完整呈現。

## Goals / Non-Goals

**Goals:**
- 在全域樣式（`mofei-style.css`）與各視圖模板中建立完整的文字排版防斷行機制（`white-space: nowrap` 應用於所有標題、按鈕、標籤與關鍵欄位）。
- 確保所有表格欄位標題與內容完整渲染，`.table-card` 具備橫向滾動容器機制（`overflow-x: auto`），欄位名稱絕不被壓縮截斷。
- 確保所有搜尋列與彈窗輸入框（`input`、`select`）具備足夠的最小寬度與靈活彈性，使所有 Placeholder 提示訊息與文字輸入值完整呈現。
- 全面覆蓋所有視圖模板：`layout.html`、`dashboard.html`、`employee.html`、`product.html`、`customer.html`、`order.html`、`login.html` 以及 `mofei-table.js`。

**Non-Goals:**
- 不引入外部前端框架或 CDN（嚴格維持 100% 離線可用與零外部相依規範）。
- 不修改後端 Spring Boot 控制器、服務邏輯、REST API 格式或資料庫結構。
- 不改變系統現有黑白灰階視覺主題與核心業務邏輯。

## Decisions

### 1. 全域 CSS 樣式防斷行與滾動容器架構
- **決策**：在 `mofei-style.css` 中：
  - 為 `.data-table th` 統一配置 `white-space: nowrap;`、`word-break: keep-all;`，並保證適當內距（`padding: 0.75rem 1rem`）。
  - 將 `.table-card` 的 `overflow: hidden;` 調整為 `overflow-x: auto; overflow-y: hidden;`，並配置平滑橫向滾動支援（`-webkit-overflow-scrolling: touch;`）。
  - 為 `.btn`、`.badge`、`.search-field label`、`.navbar-title`、`.navbar-clock`、`.nav-link`、`.page-info` 等元素統一配置 `white-space: nowrap;`。
- **替代方案評估**：
  - *替代方案 A*：強制將表格縮小字體以塞進固定寬度。缺點：造成閱讀困難，且在小螢幕上依然會破版。
  - *決策優勢*：保持標準字級（0.75rem~0.875rem），透過橫向滾動容器完整保護欄位標題與資料結構。

### 2. 搜尋列與輸入框欄位彈性與 Placeholder 完整呈現
- **決策**：
  - 重構 `.search-bar` 與 `.search-field` 的彈性盒（flexbox）配置：
    - 將即時模糊搜尋（Keyword）欄位配置較大最小寬度（`min-width: 280px; flex: 2;`），使長提示文字（例如「搜尋客戶編號、姓名、統編、電話或地址...」）完全舒展。
    - 其它精準查詢欄位設定 `min-width: 200px; flex: 1;`。
  - 確保所有 `.input-control` 與 `.select-control` 具備適當內距、行高與 `box-sizing: border-box`，提示文字垂直置中且水平不截斷。
- **替代方案評估**：
  - *替代方案 A*：縮短各頁面的 placeholder 提示字串。缺點：降低使用者操作指引的友善性與清晰度。
  - *決策優勢*：同時保留詳細的繁體中文操作指引，並保證其在 UI 上完整呈現。

### 3. 表格動態渲染模組（`mofei-table.js`）文字排版優化
- **決策**：
  - 在 `mofei-table.js` 的 `renderRows()` 中，確保所有表格單元格具備文字防破版處理，數值、編號、電話、狀態與操作欄位一律標記 `white-space: nowrap;`。
  - 地址、備註或長品項名稱等文字欄位，設定合理的最小寬度（`min-width`），避免擠壓變形。
  - 分頁控制列（`renderPagination()`）中的分頁資訊與切換按鈕維持單行完整渲染。

### 4. 各視圖模板的細緻調整
- **決策**：
  - `layout.html`：頂端導覽列在視窗狹窄時啟用非折行滾動或彈性排列，防止 Logo、時鐘與使用者狀態重疊或折行。
  - `dashboard.html`：各監控卡片的指標標題、數值與副標題文本（例如「處理中：X 筆 | 總額：NT$ Y」）設定 `white-space: nowrap;` 或合理斷句結構。
  - `order.html`：訂單主表與訂單明細彈窗內的表格均配置欄位防斷行與容器橫向滾動，防止彈窗內 6 欄品項表格被擠壓。
  - `customer.html`、`product.html`、`employee.html`：表單彈窗內的複合欄位（如城市+區域、電話格式說明）排版優化，標籤與說明不折行。
  - `login.html`：登入卡片之標題、副標題與帳號密碼輸入框確保字級與提示完整顯示。

## Risks / Trade-offs

- **[Risk] 表格開啟 `overflow-x: auto` 後，可能出現橫向滾動條影響純黑白視覺。**
  - → **Mitigation**：自訂極簡原生滾動條樣式（`scrollbar-width: thin; scrollbar-color: var(--border-medium) var(--bg-surface);`），與現有黑白灰階風格完美融合。
- **[Risk] 搜尋欄位增加 `min-width` 後，在極小解析度下可能換行形成兩列。**
  - → **Mitigation**：`.search-bar` 原生採用 `flex-wrap: wrap`，形成多列時欄位整齊對齊，不破壞排版美觀，且比截斷提示文字更易於使用。
