package com.mofei.erp.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RTest {

    @Test
    @DisplayName("驗證 R.ok() 回應格式")
    void testOk() {
        R<String> r = R.ok("測試數據");
        assertEquals(200, r.code());
        assertEquals("操作成功", r.message());
        assertEquals("測試數據", r.data());
        assertNotNull(r.timestamp());
    }

    @Test
    @DisplayName("驗證 R.fail() 回應格式與錯誤碼")
    void testFail() {
        R<?> r = R.fail(400, "參數錯誤");
        assertEquals(400, r.code());
        assertEquals("參數錯誤", r.message());
        assertNull(r.data());
        assertNotNull(r.timestamp());
    }

    @Test
    @DisplayName("驗證 BusinessException 異常拋出與錯誤碼")
    void testBusinessException() {
        BusinessException ex = new BusinessException(404, "找不到目標資源");
        assertEquals(404, ex.getCode());
        assertEquals("找不到目標資源", ex.getMessage());
    }
}
