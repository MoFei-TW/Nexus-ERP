#!/bin/bash
# scripts/verify-pipeline.sh
# 執行 Maven 純 Java 本地測試並驗證報告完整性

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "Step 1: 執行 Maven 單元測試與整合測試 (JUnit 5 + MockMvc)..."
mvn clean test -q

if [ $? -ne 0 ]; then
    echo -e "${RED}[FAIL] Maven JUnit 測試執行失敗！請修復程式碼後再試。${NC}"
    exit 1
fi

echo -e "Step 2: 檢查功能測試報告門禁 (docs/tests/)..."
LATEST_TEST=$(ls -t docs/tests/*-test-*.md 2>/dev/null | head -n 1)
if [ -z "$LATEST_TEST" ] || ! grep -q "\[STATUS: PASSED\]" "$LATEST_TEST"; then
    echo -e "${RED}[FAIL] 測試報告未通過或不存在 [STATUS: PASSED] 標記。${NC}"
    exit 1
fi

echo -e "Step 3: 檢查 OWASP API Top 10 資安報告門禁 (docs/security/)..."
LATEST_SEC=$(ls -t docs/security/*-owasp-*.md 2>/dev/null | head -n 1)
if [ -z "$LATEST_SEC" ] || ! grep -q "\[STATUS: PASSED\]" "$LATEST_SEC"; then
    echo -e "${RED}[FAIL] 資安報告未通過或不存在 [STATUS: PASSED] 標記。${NC}"
    exit 1
fi

echo -e "${GREEN}Pipeline 全部驗證通過！${NC}"
exit 0
