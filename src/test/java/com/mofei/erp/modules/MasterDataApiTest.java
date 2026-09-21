package com.mofei.erp.modules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
class MasterDataApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證員工主檔查詢與分頁 API")
    void testEmployeePage() throws Exception {
        mockMvc.perform(get("/api/employee/page?current=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.records[0].employeeId").value("2026000"))
                .andExpect(jsonPath("$.data.records[0].password").doesNotExist()); // 確保密碼敏感欄位未暴露
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證產品主檔查詢與分頁 API (產品編號由小到大排序)")
    void testProductPage() throws Exception {
        mockMvc.perform(get("/api/product/page?current=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.records[0].productId").value("202601A"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證客戶主檔查詢與分頁 API (客戶編號由小到大排序)")
    void testCustomerPage() throws Exception {
        mockMvc.perform(get("/api/customer/page?current=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.records[0].customerId").value("202609001"))
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
                    com.fasterxml.jackson.databind.JsonNode records = root.path("data").path("records");
                    String prevId = null;
                    for (com.fasterxml.jackson.databind.JsonNode r : records) {
                        String currentId = r.path("customerId").asText();
                        if (prevId != null) {
                            org.junit.jupiter.api.Assertions.assertTrue(
                                    currentId.compareTo(prevId) >= 0,
                                    "客戶編號排序應由小到大，但 " + currentId + " 小於 " + prevId
                            );
                        }
                        prevId = currentId;
                    }
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證訂單主檔查詢與明細加載 API (訂單編號由小到大排序)")
    void testOrderPage() throws Exception {
        mockMvc.perform(get("/api/order/page?current=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.records[0].details").isArray())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
                    com.fasterxml.jackson.databind.JsonNode records = root.path("data").path("records");
                    String prevId = null;
                    for (com.fasterxml.jackson.databind.JsonNode r : records) {
                        String currentId = r.path("orderId").asText();
                        if (prevId != null) {
                            org.junit.jupiter.api.Assertions.assertTrue(
                                    currentId.compareTo(prevId) >= 0,
                                    "訂單編號排序應由小到大，但 " + currentId + " 小於 " + prevId
                            );
                        }
                        prevId = currentId;
                    }
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證訂單明細修改、新增產品與總額連動重算 API")
    void testUpdateOrderDetailsAndRecalculateTotal() throws Exception {
        // 建立測試訂單
        List<com.mofei.erp.modules.order.domain.OrderDetailDTO> initialDetails = List.of(
                new com.mofei.erp.modules.order.domain.OrderDetailDTO(
                        null, null, "202601A", "雲端ERP企業核心版", new java.math.BigDecimal("100"), 2, java.math.BigDecimal.ZERO, new java.math.BigDecimal("200")
                )
        );
        com.mofei.erp.modules.order.domain.OrderDTO createDto = new com.mofei.erp.modules.order.domain.OrderDTO(
                null,
                "202609001",
                "2026001",
                null,
                "處理中",
                "明細測試收件人",
                "0988-111-222",
                "台北市信義區測試路1號",
                initialDetails
        );

        String createRes = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/order")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode createdNode = objectMapper.readTree(createRes);
        String orderId = createdNode.path("data").path("orderId").asText();

        // 進行訂單明細修改與新增產品：
        // 項目1: 202601A，單價 100，數量 3，折讓 5 -> 小計 = 100*3 - 5 = 295
        // 項目2: 新增產品 202602B，單價 200，數量 1，折讓 75 -> 小計 = 200*1 - 75 = 125
        // 預期總額 = 295 + 125 = 420
        List<com.mofei.erp.modules.order.domain.OrderDetailDTO> updatedDetails = List.of(
                new com.mofei.erp.modules.order.domain.OrderDetailDTO(
                        null, orderId, "202601A", "雲端ERP企業核心版", new java.math.BigDecimal("100"), 3, new java.math.BigDecimal("5"), null
                ),
                new com.mofei.erp.modules.order.domain.OrderDetailDTO(
                        null, orderId, "202602B", "智慧商業BI報表模組", new java.math.BigDecimal("200"), 1, new java.math.BigDecimal("75"), null
                )
        );

        com.mofei.erp.modules.order.domain.OrderDTO updateDto = new com.mofei.erp.modules.order.domain.OrderDTO(
                orderId,
                "202609001",
                "2026001",
                null,
                "處理中",
                "明細測試收件人-已更新",
                "0988-111-222",
                "台北市信義區測試路1號",
                updatedDetails
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/order/" + orderId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.details.length()").value(2))
                .andExpect(jsonPath("$.data.totalAmount").value(420.0));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增員工自動配置員工編號 (不採用使用者輸入之編號)")
    void testCreateEmployeeAutoAssignId() throws Exception {
        String uniquePhone = String.format("09%02d-%03d-%03d",
                (int)(10 + (Math.random() * 80)),
                (int)(100 + (Math.random() * 800)),
                (int)(100 + (Math.random() * 800)));
        com.mofei.erp.modules.user.domain.EmployeeDTO dto = new com.mofei.erp.modules.user.domain.EmployeeDTO(
                "customId999",
                "測試自動編號員工",
                "0000000",
                uniquePhone,
                "auto.emp" + System.currentTimeMillis() + "@mofei.com",
                "在職",
                "EMPLOYEE"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/employee")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.employeeId").value(org.hamcrest.Matchers.startsWith("2026")))
                .andExpect(jsonPath("$.data.employeeId").value(org.hamcrest.Matchers.not("customId999")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增員工電話重複檢核 (400 錯誤)")
    void testCreateEmployeeDuplicatePhone() throws Exception {
        com.mofei.erp.modules.user.domain.EmployeeDTO dto = new com.mofei.erp.modules.user.domain.EmployeeDTO(
                null,
                "重複電話員工",
                "0000000",
                "0912-345-678", // 已存在之電話 (張小明)
                "unique.email" + System.currentTimeMillis() + "@mofei.com",
                "在職",
                "EMPLOYEE"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/employee")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("員工電話已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增員工電子郵件重複檢核 (400 錯誤)")
    void testCreateEmployeeDuplicateEmail() throws Exception {
        com.mofei.erp.modules.user.domain.EmployeeDTO dto = new com.mofei.erp.modules.user.domain.EmployeeDTO(
                null,
                "重複信箱員工",
                "0000000",
                "0977-888-999",
                "admin@mofei.com", // 已存在之信箱 (系統管理員)
                "在職",
                "EMPLOYEE"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/employee")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("電子郵件已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改員工資料保持原電話與信箱 (不拋出重複例外)")
    void testUpdateEmployeeSuccessWithSamePhoneAndEmail() throws Exception {
        com.mofei.erp.modules.user.domain.EmployeeDTO dto = new com.mofei.erp.modules.user.domain.EmployeeDTO(
                "2026001",
                "陳俊豪(修改名稱)",
                null,
                "0912-345-678", // 自身原電話
                "junhao.chen@mofei.com", // 自身原信箱
                "在職",
                "EMPLOYEE"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/employee/2026001")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.employeeName").value("陳俊豪(修改名稱)"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改員工電話重複檢核 (400 錯誤)")
    void testUpdateEmployeeDuplicatePhone() throws Exception {
        com.mofei.erp.modules.user.domain.EmployeeDTO dto = new com.mofei.erp.modules.user.domain.EmployeeDTO(
                "2026001",
                "陳俊豪",
                null,
                "0922-111-222", // 已被 2026002 使用
                "junhao.chen@mofei.com",
                "在職",
                "EMPLOYEE"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/employee/2026001")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("員工電話已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改員工電子郵件重複檢核 (400 錯誤)")
    void testUpdateEmployeeDuplicateEmail() throws Exception {
        com.mofei.erp.modules.user.domain.EmployeeDTO dto = new com.mofei.erp.modules.user.domain.EmployeeDTO(
                "2026001",
                "陳俊豪",
                null,
                "0912-345-678",
                "yating.lin@mofei.com", // 已被 2026002 使用
                "在職",
                "EMPLOYEE"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/employee/2026001")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("電子郵件已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改員工編號重複檢核 (400 錯誤)")
    void testUpdateEmployeeDuplicateId() throws Exception {
        com.mofei.erp.modules.user.domain.EmployeeDTO dto = new com.mofei.erp.modules.user.domain.EmployeeDTO(
                "2026002", // 企圖修改為已存在之 2026002
                "陳俊豪",
                null,
                "0912-345-678",
                "junhao.chen@mofei.com",
                "在職",
                "EMPLOYEE"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/employee/2026001")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("員工編號已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增產品自動配置產品編號 (不採用使用者輸入之編號)")
    void testCreateProductAutoAssignId() throws Exception {
        com.mofei.erp.modules.product.domain.ProductDTO dto = new com.mofei.erp.modules.product.domain.ProductDTO(
                "customProd999",
                "測試自動編號產品",
                "測試規格",
                new java.math.BigDecimal("88000"),
                20,
                "2026001",
                "正常販售"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/product")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.productId").value(org.hamcrest.Matchers.startsWith("2026")))
                .andExpect(jsonPath("$.data.productId").value(org.hamcrest.Matchers.not("customProd999")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增產品價格非整數檢核 (400 錯誤)")
    void testCreateProductNonIntegerPrice() throws Exception {
        com.mofei.erp.modules.product.domain.ProductDTO dto = new com.mofei.erp.modules.product.domain.ProductDTO(
                null,
                "測試非整數價格產品",
                "規格",
                new java.math.BigDecimal("99.99"),
                10,
                "2026001",
                "正常販售"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/product")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("產品價格必須為整數")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增產品價格負數檢核 (400 錯誤)")
    void testCreateProductNegativePrice() throws Exception {
        com.mofei.erp.modules.product.domain.ProductDTO dto = new com.mofei.erp.modules.product.domain.ProductDTO(
                null,
                "測試負數價格產品",
                "規格",
                new java.math.BigDecimal("-50"),
                10,
                "2026001",
                "正常販售"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/product")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("產品價格必須大於或等於0")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增產品負責員工不存在檢核 (400 錯誤)")
    void testCreateProductNonExistentEmployee() throws Exception {
        com.mofei.erp.modules.product.domain.ProductDTO dto = new com.mofei.erp.modules.product.domain.ProductDTO(
                null,
                "測試員工不存在產品",
                "規格",
                new java.math.BigDecimal("5000"),
                10,
                "9999999",
                "正常販售"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/product")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("負責員工編號不存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改產品成功 (整數價格與合法員工)")
    void testUpdateProductSuccess() throws Exception {
        com.mofei.erp.modules.product.domain.ProductDTO dto = new com.mofei.erp.modules.product.domain.ProductDTO(
                "202601A",
                "雲端ERP企業核心版(修改)",
                "修改規格",
                new java.math.BigDecimal("90000"),
                60,
                "2026002",
                "正常販售"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/product/202601A")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.productName").value("雲端ERP企業核心版(修改)"))
                .andExpect(jsonPath("$.data.price").value(90000));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改產品負責員工不存在檢核 (400 錯誤)")
    void testUpdateProductNonExistentEmployee() throws Exception {
        com.mofei.erp.modules.product.domain.ProductDTO dto = new com.mofei.erp.modules.product.domain.ProductDTO(
                "202601A",
                "雲端ERP企業核心版",
                null,
                new java.math.BigDecimal("88000"),
                50,
                "non_existent_emp",
                "正常販售"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/product/202601A")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("負責員工編號不存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增產品價格型態錯誤解析 (400 錯誤與明確欄位訊息)")
    void testCreateProductMalformedPrice() throws Exception {
        String jsonPayload = """
                {
                    "productName": "測試格式錯誤產品",
                    "price": "not_a_valid_number",
                    "stock": 10,
                    "employeeId": "2026001",
                    "status": "正常販售"
                }
                """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/product")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("欄位格式錯誤: 產品價格 輸入格式不正確")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增產品庫存型態錯誤解析 (400 錯誤與明確欄位訊息)")
    void testCreateProductMalformedStock() throws Exception {
        String jsonPayload = """
                {
                    "productName": "測試格式錯誤庫存產品",
                    "price": 1000,
                    "stock": "invalid_stock_format",
                    "employeeId": "2026001",
                    "status": "正常販售"
                }
                """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/product")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("欄位格式錯誤: 庫存數量 輸入格式不正確")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增產品空白名稱驗證 (400 錯誤與明確欄位訊息)")
    void testCreateProductEmptyName() throws Exception {
        com.mofei.erp.modules.product.domain.ProductDTO dto = new com.mofei.erp.modules.product.domain.ProductDTO(
                null,
                "",
                "規格",
                new java.math.BigDecimal("5000"),
                10,
                "2026001",
                "正常販售"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/product")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("產品名稱不能為空")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增產品名稱重複檢核 (400 錯誤與產品名稱已存在訊息)")
    void testCreateProductDuplicateName() throws Exception {
        com.mofei.erp.modules.product.domain.ProductDTO dto = new com.mofei.erp.modules.product.domain.ProductDTO(
                null,
                "雲端ERP企業核心版", // 已存在之產品名稱
                "規格",
                new java.math.BigDecimal("60000"),
                20,
                "2026001",
                "正常販售"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/product")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("產品名稱已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改產品名稱重複檢核 (400 錯誤與產品名稱已存在訊息)")
    void testUpdateProductDuplicateName() throws Exception {
        com.mofei.erp.modules.product.domain.ProductDTO dto = new com.mofei.erp.modules.product.domain.ProductDTO(
                "202602B",
                "雲端ERP企業核心版", // 企圖修改為 202601A 之名稱
                "規格",
                new java.math.BigDecimal("36000"),
                30,
                "2026001",
                "正常販售"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/product/202602B")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("產品名稱已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增客戶自動配置客戶編號 (不採用使用者輸入之編號)")
    void testCreateCustomerAutoAssignId() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                "customCust999",
                "測試自動編號客戶",
                "0988-999-001",
                "auto.cust@example.com",
                "自動編號企業",
                "7777-8888",
                "台灣",
                "台北市",
                "信義區",
                "信義路五段100號",
                "2026001"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/customer")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.customerId").value(org.hamcrest.Matchers.startsWith("2026")))
                .andExpect(jsonPath("$.data.customerId").value(org.hamcrest.Matchers.not("customCust999")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增客戶電話重複檢核 (400 錯誤與客戶電話已存在訊息)")
    void testCreateCustomerDuplicatePhone() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                null,
                "重複電話客戶",
                "0911-222-333", // 已存在之電話
                "unique.cust@example.com",
                "測試企業",
                "1111-2222",
                "台灣",
                "台北市",
                "大安區",
                "和平東路一段1號",
                "2026001"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/customer")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("客戶電話已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增客戶統一編號重複檢核 (400 錯誤與客戶統一編號已存在訊息)")
    void testCreateCustomerDuplicateTaxId() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                null,
                "重複統編客戶",
                "0988-999-002",
                "unique2.cust@example.com",
                "測試企業",
                "8888-8888", // 已存在之統編
                "台灣",
                "台北市",
                "大安區",
                "和平東路一段1號",
                "2026001"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/customer")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("客戶統一編號已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增客戶電子郵件重複檢核 (400 錯誤與客戶電子郵件已存在訊息)")
    void testCreateCustomerDuplicateEmail() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                null,
                "重複信箱客戶",
                "0988-999-003",
                "contact@mofei-tech.com", // 已存在之信箱
                "測試企業",
                "3333-4444",
                "台灣",
                "台北市",
                "大安區",
                "和平東路一段1號",
                "2026001"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/customer")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("客戶電子郵件已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改客戶成功 (保持原電話、統編與信箱)")
    void testUpdateCustomerSuccessWithSameData() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                "202609001",
                "摩菲科技股份有限公司(修改)",
                "0911-222-333", // 自身原電話
                "contact@mofei-tech.com", // 自身原信箱
                "摩菲科技股份有限公司",
                "8888-8888", // 自身原統編
                "台灣",
                "台北市",
                "信義區",
                "信義路五段7號89樓",
                "2026001"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/customer/202609001")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.customerName").value("摩菲科技股份有限公司(修改)"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改客戶電話重複檢核 (400 錯誤與客戶電話已存在訊息)")
    void testUpdateCustomerDuplicatePhone() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                "202609001",
                "摩菲科技",
                "0922-333-444", // 已被 202609002 使用
                "contact@mofei-tech.com",
                "摩菲科技",
                "8888-8888",
                "台灣",
                "台北市",
                "信義區",
                "信義路五段7號89樓",
                "2026001"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/customer/202609001")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("客戶電話已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改客戶統一編號重複檢核 (400 錯誤與客戶統一編號已存在訊息)")
    void testUpdateCustomerDuplicateTaxId() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                "202609001",
                "摩菲科技",
                "0911-222-333",
                "contact@mofei-tech.com",
                "摩菲科技",
                "1234-5678", // 已被 202609002 使用
                "台灣",
                "台北市",
                "信義區",
                "信義路五段7號89樓",
                "2026001"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/customer/202609001")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("客戶統一編號已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證修改客戶電子郵件重複檢核 (400 錯誤與客戶電子郵件已存在訊息)")
    void testUpdateCustomerDuplicateEmail() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                "202609001",
                "摩菲科技",
                "0911-222-333",
                "service@htc-digital.com", // 已被 202609002 使用
                "摩菲科技",
                "8888-8888",
                "台灣",
                "台北市",
                "信義區",
                "信義路五段7號89樓",
                "2026001"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/customer/202609001")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("客戶電子郵件已存在")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增客戶電子郵件格式錯誤解析 (400 錯誤與明確欄位訊息)")
    void testCreateCustomerInvalidEmailFormat() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                null,
                "測試信箱格式客戶",
                "0988-111-222",
                "invalid-email-no-domain",
                "測試企業",
                "1111-2222",
                "台灣",
                "台北市",
                "大安區",
                "和平東路一段1號",
                "2026001"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/customer")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("電子郵件")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("資料已存在"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增客戶電話格式錯誤解析 (400 錯誤與明確欄位訊息)")
    void testCreateCustomerInvalidPhoneFormat() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                null,
                "測試電話格式客戶",
                "0912345678", // 缺少橫線
                "valid.email@example.com",
                "測試企業",
                "1111-2222",
                "台灣",
                "台北市",
                "大安區",
                "和平東路一段1號",
                "2026001"
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/customer")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("聯絡電話")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("資料已存在"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("驗證新增客戶負責員工不存在錯誤解析 (400 錯誤與明確欄位訊息)")
    void testCreateCustomerNonExistentEmployee() throws Exception {
        com.mofei.erp.modules.customer.domain.CustomerDTO dto = new com.mofei.erp.modules.customer.domain.CustomerDTO(
                null,
                "測試無效員工客戶",
                "0988-111-333",
                "valid.emp@example.com",
                "測試企業",
                "1111-2222",
                "台灣",
                "台北市",
                "大安區",
                "和平東路一段1號",
                "9999999" // 不存在之員工編號
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/customer")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("負責員工編號不存在")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("資料已存在"))));
    }
}


