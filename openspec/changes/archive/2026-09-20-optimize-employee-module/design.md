# Design: 員工主檔模組優化 (Employee Module Optimization)

## Context

針對 MOFEI ERP 管理者提出的員工主檔模組問題進行優化。背景與動機請參閱 `proposal.md`。
現行系統現況：
- 前端 `employee.html` 的系統角色以 `<span class="badge">` 標籤呈現。
- 後端 `EmployeeServiceImpl.queryPage` 採用 `wrapper.orderByDesc(Employee::getCreatedAt)`，非以員工編號排序。
- 新增員工時，前端表單開放使用者自行輸入員工編號，且後端若未輸入僅以 `count + 1` 產生，易因刪除或人為輸入導致流水號衝突。
- 後端新增員工未檢核電話（phone）與電子郵件（email）是否重複，容易產生重覆建檔問題。

## Goals / Non-Goals

**Goals:**
- 前端員工主檔列表系統角色欄位移除 badge HTML 標籤，以純文字呈現。
- 後端員工分頁查詢預設改依員工編號由小到大（遞增排序，`employeeId ASC`）排列。
- 新增員工表單之員工編號欄位改為不可編輯（由系統自動配置），後端新增時強制由系統生成最新員工編號。
- 新增員工時，後端強制檢核員工編號、員工電話與電子郵件之唯一性，重複時回傳 400 錯誤與明確提示。

**Non-Goals:**
- 修改資料庫實體表格結構（DDL 保持不變，沿用現有欄位）。
- 修改修改員工（PUT `/api/employee/{id}`）的編號唯讀規則（現已有 `disabled = true` 且後端不允許異動主鍵）。
- 修改非員工模組（如產品、客戶、訂單）的排序或防重規則。

## Decisions

### 1. 系統角色欄位純文字渲染
- **決定**：在 `employee.html` 的表格欄位定義中，將 `role` 欄位原本的 `render: row => `<span class="badge">${row.role}</span>`` 改為直接傳回字串 `render: row => row.role || ''` 或直接使用預設欄位取值 `{ field: 'role' }`。
- **替代方案**：保留 badge 但更改樣式。*捨棄原因*：管理者明確要求移除網頁標籤，直接顯示純文字。

### 2. 員工分頁查詢排序調整
- **決定**：在 `EmployeeServiceImpl.queryPage` 中，將 `wrapper.orderByDesc(Employee::getCreatedAt)` 修改為 `wrapper.orderByAsc(Employee::getEmployeeId)`。
- **替代方案**：在前端進行 client-side 排序。*捨棄原因*：系統採用後端分頁（每頁 10 筆），前端排序只能排當頁資料，無法達成全域由小到大排列。

### 3. 新增員工編號由系統自動配置
- **決定**：
  - 前端：新增彈窗開啟時，員工編號欄位設為唯讀/停用（`disabled = true`），並提示「由系統自動配置」，不讓使用者輸入。
  - 後端：在 `EmployeeServiceImpl.create` 中，不採用前端傳入之 `dto.employeeId()`，而是由後端自動計算新編號。透過查詢目前最大流水號（例如查詢以 `2026` 開頭之最新編號或依編號倒序取第一筆），計算出下一個編號（例如 `2026001`, `2026002`, ...），若資料庫為空則由 `2026001` 開始。
- **替代方案**：後端依然使用 `selectCount(null) + 1`。*捨棄原因*：若曾有軟刪除或歷史跳號，count+1 會產生重複 ID 碰撞。取最大編號遞增更為可靠。

### 4. 員工編號、電話與電子郵件防重檢核
- **決定**：
  - 在 `EmployeeServiceImpl.create` 執行持久化前，新增檢核：
    1. 員工編號：確保系統生成的 `employeeId` 未被使用（`employeeMapper.selectById(newId) == null`）。
    2. 員工電話：透過 `employeeMapper.selectOne(new LambdaQueryWrapper<Employee>().eq(Employee::getPhone, dto.phone()))` 檢查，若存在則拋出 `BusinessException(400, "員工電話已存在: " + dto.phone())`。
    3. 電子郵件：透過 `employeeMapper.selectOne(new LambdaQueryWrapper<Employee>().eq(Employee::getEmail, dto.email()))` 檢查，若存在則拋出 `BusinessException(400, "電子郵件已存在: " + dto.email())`。
- **替代方案**：僅依靠資料庫 Unique Constraint 觸發 SQL 例外。*捨棄原因*：資料庫層級錯誤會導致 500 內部伺服器錯誤且錯誤訊息不友善；由業務邏輯層檢核可給予使用者精確的 400 錯誤訊息。

## Risks / Trade-offs

- **[併發新增產生相同員工編號]** → 緩解措施：在生成流水編號時結合 `selectById` 驗證與迴圈/重試，或以交易確保一致性；在 ERP 內部管理系統之員工新增頻率下，此做法已兼具穩定性與效能。
- **[舊有測試案例依賴 createdAt 排序]** → 緩解措施：同步檢視並更新現有單元測試或 API 測試案例，確保預期順序符合新規範。
