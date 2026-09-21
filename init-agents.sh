#!/bin/bash
# init-agents.sh - OpenSpec 規格架構、AI 治理環境與 Pipeline 初始化腳本

echo "🚀 [Agent Admin] 開始初始化 OpenSpec 規格庫、Governance 治理機制與 Git Hooks..."

# 1. 建立 OpenSpec 雙目錄與專案治理結構
echo "📁 建立 OpenSpec 雙目錄 (specs/changes) 與治理目錄..."
mkdir -p openspec/specs
mkdir -p openspec/changes
mkdir -p .agents/skills
mkdir -p .githooks
mkdir -p scripts
mkdir -p docs/tests
mkdir -p docs/security

# 2. 配置 Git Hooks 指向專案內部 .githooks 目錄（使其可被版控）
echo "🔗 綁定 Git Hooks 路徑..."
git config core.hooksPath .githooks

# 3. 建立核心流水線腳本 scripts/verify-pipeline.sh
echo "⚙️ 建立核心流水線驗證腳本 (scripts/verify-pipeline.sh)..."
cat << 'EOF' > scripts/verify-pipeline.sh
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
EOF

# 4. 建立 Pre-commit 攔截腳本 .githooks/pre-commit
echo "🛡️ 建立 Git Pre-commit 攔截腳本 (.githooks/pre-commit)..."
cat << 'EOF' > .githooks/pre-commit
#!/bin/bash
# .githooks/pre-commit
# 自動化門禁管制：強制驗證功能測試與 OWASP 資安綠燈狀態

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}==============================================${NC}"
echo -e "${YELLOW} [Quality Gate] 執行 Pre-commit 品質與資安門禁 ${NC}"
echo -e "${YELLOW}==============================================${NC}"

PIPELINE_SCRIPT="scripts/verify-pipeline.sh"

# 階段一：執行整合驗證腳本 (若腳本存在)
if [ -f "$PIPELINE_SCRIPT" ]; then
    echo -e "${YELLOW}正在執行核心流水線驗證: ${PIPELINE_SCRIPT}...${NC}"
    bash "$PIPELINE_SCRIPT"
    if [ $? -ne 0 ]; then
        echo -e "${RED}[ERROR] 自動化測試或資安稽核未通過，提交已被攔截！${NC}"
        exit 1
    fi
else
    # 階段二：備用靜態報告標籤檢查
    echo -e "未找到 ${PIPELINE_SCRIPT}，進行報告綠燈標籤掃描..."

    # 1. 檢測最新的測試報告
    LATEST_TEST_REPORT=$(ls -t docs/tests/*-test-*.md 2>/dev/null | head -n 1)
    if [ -z "$LATEST_TEST_REPORT" ]; then
        echo -e "${RED}[X] 攔截提交：找不到任何功能測試報告 (docs/tests/*-test-*.md)${NC}"
        echo -e "${RED}    請先執行 test-detect.md 進行測試流程！${NC}"
        exit 1
    fi

    if ! grep -q "\[STATUS: PASSED\]" "$LATEST_TEST_REPORT"; then
        echo -e "${RED}[X] 攔截提交：最新測試報告狀態非 PASSED (${LATEST_TEST_REPORT})${NC}"
        exit 1
    fi
    echo -e "${GREEN}[V] 功能測試門禁通過: ${LATEST_TEST_REPORT}${NC}"

    # 2. 檢測最新的 OWASP 資安報告
    LATEST_SEC_REPORT=$(ls -t docs/security/*-owasp-*.md 2>/dev/null | head -n 1)
    if [ -z "$LATEST_SEC_REPORT" ]; then
        echo -e "${RED}[X] 攔截提交：找不到任何資安稽核報告 (docs/security/*-owasp-*.md)${NC}"
        echo -e "${RED}    請先執行 hook-owasp.md 進行資安檢核！${NC}"
        exit 1
    fi

    if ! grep -q "\[STATUS: PASSED\]" "$LATEST_SEC_REPORT"; then
        echo -e "${RED}[X] 攔截提交：最新資安報告狀態非 PASSED (${LATEST_SEC_REPORT})${NC}"
        exit 1
    fi
    echo -e "${GREEN}[V] OWASP 資安門禁通過: ${LATEST_SEC_REPORT}${NC}"
fi

echo -e "${GREEN}==============================================${NC}"
echo -e "${GREEN} [PASSED] 所有品質門禁均開綠燈，允許 Git Commit ${NC}"
echo -e "${GREEN}==============================================${NC}"
exit 0
EOF

# 5. 強制賦予執行權限
echo "🔑 賦予腳本執行權限 (chmod +x)..."
chmod +x .githooks/pre-commit
chmod +x scripts/verify-pipeline.sh
chmod +x init-agents.sh 2>/dev/null || true

echo "🎉 [SUCCESS] OpenSpec 架構與 AI Agent 治理環境初始化完成！"
