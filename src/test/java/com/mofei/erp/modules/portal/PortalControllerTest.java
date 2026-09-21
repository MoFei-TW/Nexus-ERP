package com.mofei.erp.modules.portal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("驗證儀表板統計 API")
    void testDashboardStats() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalEmployees", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalProducts", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalCustomers", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalOrders", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("驗證公版頁面路由皆可正常渲染 (訪客可檢視)")
    void testPublicPages() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));

        mockMvc.perform(get("/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));

        mockMvc.perform(get("/product"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));

        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));

        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
