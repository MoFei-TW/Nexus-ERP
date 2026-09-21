# Design: 客戶編號升冪排序

## Context

目前 `CustomerServiceImpl.java` 中的 `queryPage` 方法在建立查詢條件時，使用了 `wrapper.orderByDesc(Customer::getCreatedAt);`。這導致客戶清單預設按照建立時間倒序顯示。參見 `proposal.md`。

## Goals / Non-Goals

**Goals:**
- 將後端 `CustomerServiceImpl.queryPage` 的資料庫查詢排序改為依客戶編號由小到大（升冪 ASC，`wrapper.orderByAsc(Customer::getCustomerId)`）。
- 確保所有分頁查詢、即時模糊搜尋及複合條件篩選時，客戶編號皆維持由小到大排序。
- 在單元與整合測試中驗證排序行為正確。

**Non-Goals:**
- 不調整分頁大小（維持每頁 10 筆）。
- 不改變前端欄位展示與其他主檔模組（員工、產品、訂單）既有之排序邏輯。

## Decisions

### 決策 1：在 MyBatis-Plus `LambdaQueryWrapper` 層級指定 `orderByAsc(Customer::getCustomerId)`
- **原因**：`CustomerServiceImpl` 現有架構係透過 MyBatis-Plus `LambdaQueryWrapper` 組合 SQL 條件。在 wrapper 中指定 `orderByAsc(Customer::getCustomerId)` 能直接於 SQL 產出 `ORDER BY customer_id ASC`，善用客戶主鍵（Primary Key）聚集索引（Clustered Index），兼顧最高效能與分頁一致性。
- **替代方案**：
  - *記憶體排序（Java Stream API）*：若在查出分頁後於 Java 記憶體內排序，將破壞資料庫跨頁分頁邏輯，不可行。
  - *前端 JavaScript 排序*：僅能對當前頁 10 筆資料進行排序，跨頁仍為舊順序，不可行。

## Risks / Trade-offs

- **[風險] 既有測試案例預期建立時間排序** → **緩解措施**：檢查 `MasterDataApiTest.java`，更新或新增驗證客戶編號升冪排序之測試案例。
