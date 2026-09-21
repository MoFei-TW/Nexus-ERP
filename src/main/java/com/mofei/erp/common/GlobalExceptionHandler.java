package com.mofei.erp.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<String, String> FIELD_NAME_MAP = Map.ofEntries(
            Map.entry("productId", "產品編號"),
            Map.entry("productName", "產品名稱"),
            Map.entry("attributes", "規格屬性"),
            Map.entry("price", "產品價格"),
            Map.entry("stock", "庫存數量"),
            Map.entry("employeeId", "負責員工編號"),
            Map.entry("status", "狀態"),
            Map.entry("employeeName", "員工姓名"),
            Map.entry("phone", "聯絡電話"),
            Map.entry("email", "電子郵件"),
            Map.entry("password", "密碼"),
            Map.entry("role", "角色"),
            Map.entry("customerId", "客戶編號"),
            Map.entry("customerName", "客戶姓名"),
            Map.entry("companyName", "公司抬頭"),
            Map.entry("taxId", "統一編號"),
            Map.entry("country", "國家"),
            Map.entry("city", "城市"),
            Map.entry("district", "區域"),
            Map.entry("address", "詳細地址"),
            Map.entry("orderId", "訂單編號"),
            Map.entry("totalAmount", "訂單金額")
    );

    private String resolveFieldName(String field) {
        if (field == null || field.isBlank()) {
            return "欄位";
        }
        return FIELD_NAME_MAP.getOrDefault(field, field);
    }

    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusinessException(BusinessException e) {
        log.warn("Business Exception: [code: {}, message: {}]", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: ", e);
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String field = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(s -> s != null && !s.isBlank())
                    .reduce((first, second) -> second)
                    .orElse(null);
            String fieldName = resolveFieldName(field);
            return R.fail(400, "欄位格式錯誤: " + fieldName + " 輸入格式不正確");
        } else if (cause instanceof JsonMappingException jme) {
            String field = jme.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(s -> s != null && !s.isBlank())
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (field != null) {
                return R.fail(400, "欄位格式錯誤: " + resolveFieldName(field) + " 輸入格式不正確");
            }
        }
        return R.fail(400, "請求參數格式不正確，請檢查輸入內容");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        log.warn("Validation Exception: {}", message);
        return R.fail(400, "參數檢驗失敗: " + message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        log.warn("Bind Exception: {}", message);
        return R.fail(400, "參數綁定失敗: " + message);
    }

    private String formatFieldError(FieldError error) {
        String fieldName = resolveFieldName(error.getField());
        String defaultMsg = error.getDefaultMessage();
        if (defaultMsg == null || defaultMsg.isBlank()) {
            return fieldName + " 輸入有誤";
        }
        if (defaultMsg.contains(fieldName) || defaultMsg.contains(error.getField())) {
            return defaultMsg;
        }
        return fieldName + " " + defaultMsg;
    }

    @ExceptionHandler({DuplicateKeyException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleDuplicateKeyException(Exception e) {
        log.warn("Database unique/integrity constraint violation: ", e);
        String message = resolveConstraintErrorMessage(e);
        return R.fail(400, message);
    }

    private String resolveDbColumnName(String colName) {
        if (colName == null || colName.isBlank()) {
            return "欄位";
        }
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : colName.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return resolveFieldName(sb.toString());
    }

    private String resolveConstraintErrorMessage(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String msg = current.getMessage();
            if (msg != null) {
                // 1. CHECK 條件約束 (格式與數值規則)
                if (msg.contains("CK_customer_email")) {
                    return "電子郵件格式不正確（需包含 @ 與有效網域，例如 service@example.com）";
                }
                if (msg.contains("CK_customer_phone")) {
                    return "聯絡電話格式不正確（格式須為 0912-345-678）";
                }
                if (msg.contains("CK_customer_tax_id")) {
                    return "統一編號格式不正確（格式須為 8888-8888）";
                }
                if (msg.contains("CK_product_price")) {
                    return "產品價格格式不正確（必須大於或等於0）";
                }
                if (msg.contains("CK_product_stock")) {
                    return "庫存數量格式不正確（必須大於或等於0）";
                }
                if (msg.contains("CK_product_status")) {
                    return "產品狀態不正確";
                }
                if (msg.contains("CK_orders_total_amount")) {
                    return "訂單金額格式不正確（必須大於或等於0）";
                }
                if (msg.contains("CK_orders_status")) {
                    return "訂單狀態不正確";
                }
                if (msg.contains("CK_order_detail_unit_price")) {
                    return "明細單價格式不正確";
                }
                if (msg.contains("CK_order_detail_quantity")) {
                    return "明細數量格式不正確";
                }

                // 2. FOREIGN KEY 外鍵約束 (關聯不存在)
                if (msg.contains("FK_customer_employee") || msg.contains("FK_product_employee") || msg.contains("FK_orders_employee")) {
                    return "負責員工編號不存在";
                }
                if (msg.contains("FK_orders_customer")) {
                    return "關聯客戶編號不存在";
                }
                if (msg.contains("FK_order_detail_orders")) {
                    return "關聯訂單編號不存在";
                }
                if (msg.contains("FK_order_detail_product")) {
                    return "關聯產品編號不存在";
                }

                // 3. PRIMARY KEY / UNIQUE KEY 唯一性約束 (重複資料)
                if (msg.contains("PK_customer")) {
                    return "客戶編號已存在";
                }
                if (msg.contains("PK_employee")) {
                    return "員工編號已存在";
                }
                if (msg.contains("PK_product")) {
                    return "產品編號已存在";
                }
                if (msg.contains("PK_orders")) {
                    return "訂單編號已存在";
                }
                if (msg.contains("PK_order_detail")) {
                    return "訂單明細編號已存在";
                }
                if (msg.contains("UK_product_name") || (msg.contains("product") && msg.contains("product_name"))) {
                    return "產品名稱已存在";
                }
                if (msg.contains("UK_customer_tax_id")) {
                    return "客戶統一編號已存在";
                }
                if (msg.contains("UK_customer_phone")) {
                    return "客戶電話已存在";
                }
                if (msg.contains("UK_employee_phone")) {
                    return "員工電話已存在";
                }
                if (msg.contains("UK_employee_email")) {
                    return "員工電子郵件已存在";
                }

                // 4. 動態抽取 MSSQL 欄位名稱 (例如 column 'email' 或 column 'phone')
                java.util.regex.Matcher colMatcher = java.util.regex.Pattern.compile("column ['\"]([a-zA-Z0-9_]+)['\"]", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(msg);
                if (colMatcher.find()) {
                    String colName = colMatcher.group(1);
                    String fieldName = resolveDbColumnName(colName);
                    if (msg.contains("CHECK") || msg.contains("條件約束")) {
                        return fieldName + " 格式不符合規範，請檢查輸入內容";
                    }
                    if (msg.contains("FOREIGN KEY") || msg.contains("外部索引鍵")) {
                        return fieldName + " 所選之關聯資料不存在";
                    }
                    if (msg.contains("PRIMARY KEY") || msg.contains("UNIQUE") || msg.contains("重複")) {
                        return fieldName + " 已存在，請更換其他數值";
                    }
                    return fieldName + " 輸入內容不符合資料庫規範";
                }

                // 5. 重複鍵值正則抽取
                if (msg.contains("UNIQUE KEY") || msg.contains("重複的索引鍵") || msg.contains("Duplicate entry")) {
                    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("重複的索引鍵值是 \\((.*?)\\)").matcher(msg);
                    if (matcher.find()) {
                        return "資料已存在: " + matcher.group(1);
                    }
                    return "資料重複，違反唯一鍵約束";
                }
            }
            current = current.getCause();
        }
        return "輸入資料不符合限制規範，請檢查各欄位輸入內容";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleGeneralException(Exception e) {
        log.error("Internal Server Error: ", e);
        return R.fail(500, "系統內部錯誤，請聯繫管理員");
    }
}
