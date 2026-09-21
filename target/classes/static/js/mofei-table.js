/**
 * MOFEI ERP - 通用 Vanilla JS 表格控制模組 (mofei-table.js)
 * 規範: 純原生 JavaScript (ES6+), 原生 fetch(), 支援 10 筆分頁、複合查詢、即時模糊搜尋、XSS 防護 (textContent)
 */

class MofeiTable {
    constructor(config) {
        this.apiUrl = config.apiUrl;
        this.tableBodyId = config.tableBodyId;
        this.paginationId = config.paginationId;
        this.columns = config.columns || [];
        this.actions = config.actions || null;
        this.current = 1;
        this.size = config.size || 10;
        this.total = 0;
        this.pages = 1;
        this.keyword = "";
        this.filters = {};

        this.tableBodyEl = document.getElementById(this.tableBodyId);
        this.paginationEl = document.getElementById(this.paginationId);

        this.init();
    }

    init() {
        this.loadData();
    }

    setFilters(filters) {
        this.filters = { ...filters };
        this.current = 1;
        this.loadData();
    }

    setKeyword(keyword) {
        this.keyword = keyword || "";
        this.current = 1;
        this.loadData();
    }

    async loadData() {
        if (!this.tableBodyEl) return;

        // 構建 Query Params
        const params = new URLSearchParams({
            current: this.current,
            size: this.size
        });

        if (this.keyword) {
            params.append("keyword", this.keyword);
        }

        for (const [key, value] of Object.entries(this.filters)) {
            if (value !== undefined && value !== null && value !== "") {
                params.append(key, value);
            }
        }

        try {
            this.renderLoading();
            const res = await fetch(`${this.apiUrl}?${params.toString()}`);
            const result = await res.json();

            if (result.code === 200 && result.data) {
                const pageData = result.data;
                this.current = pageData.current;
                this.size = pageData.size;
                this.total = pageData.total;
                this.pages = pageData.pages || Math.ceil(this.total / this.size) || 1;

                this.renderRows(pageData.records);
                this.renderPagination();
            } else if (result.code === 401) {
                this.renderEmpty("未登入或憑證逾期，請先登入後操作");
            } else {
                this.renderEmpty(result.message || "載入資料失敗");
            }
        } catch (error) {
            console.error("載入表格失敗:", error);
            this.renderEmpty("網路連線異常，請稍後重試");
        }
    }

    renderLoading() {
        this.tableBodyEl.innerHTML = "";
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = this.columns.length + (this.actions ? 1 : 0);
        td.style.textAlign = "center";
        td.style.padding = "2rem";
        td.textContent = "資料載入中...";
        tr.appendChild(td);
        this.tableBodyEl.appendChild(tr);
    }

    renderEmpty(message = "暫無符合條件之資料") {
        this.tableBodyEl.innerHTML = "";
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = this.columns.length + (this.actions ? 1 : 0);
        td.style.textAlign = "center";
        td.style.padding = "2rem";
        td.style.color = "var(--text-secondary)";
        td.textContent = message;
        tr.appendChild(td);
        this.tableBodyEl.appendChild(tr);

        if (this.paginationEl) {
            this.paginationEl.innerHTML = "";
        }
    }

    renderRows(records) {
        this.tableBodyEl.innerHTML = "";

        if (!records || records.length === 0) {
            this.renderEmpty();
            return;
        }

        records.forEach((row, index) => {
            const tr = document.createElement("tr");

            // 渲染欄位
            this.columns.forEach(col => {
                const td = document.createElement("td");
                const fieldLower = (col.field || "").toLowerCase();
                if (col.nowrap || fieldLower.includes('id') || fieldLower.includes('phone') || 
                    fieldLower.includes('date') || fieldLower.includes('time') || 
                    fieldLower.includes('price') || fieldLower.includes('amount') || 
                    fieldLower.includes('total') || fieldLower.includes('status') ||
                    fieldLower.includes('tax') || fieldLower.includes('stock')) {
                    td.classList.add("cell-nowrap");
                }
                if (col.minWidth) {
                    td.style.minWidth = col.minWidth;
                }
                if (typeof col.render === "function") {
                    const node = col.render(row, index);
                    if (node instanceof HTMLElement) {
                        td.appendChild(node);
                    } else {
                        td.textContent = node !== undefined && node !== null ? String(node) : "-";
                    }
                } else {
                    const val = row[col.field];
                    td.textContent = val !== undefined && val !== null ? String(val) : "-";
                }
                tr.appendChild(td);
            });

            // 渲染操作欄位
            if (this.actions) {
                const actionTd = document.createElement("td");
                actionTd.classList.add("cell-nowrap");
                actionTd.style.whiteSpace = "nowrap";
                const actionContainer = this.actions(row, index);
                if (actionContainer instanceof HTMLElement) {
                    actionTd.appendChild(actionContainer);
                }
                tr.appendChild(actionTd);
            }

            this.tableBodyEl.appendChild(tr);
        });
    }

    renderPagination() {
        if (!this.paginationEl) return;
        this.paginationEl.innerHTML = "";

        // 資訊區
        const infoSpan = document.createElement("div");
        infoSpan.className = "page-info";
        infoSpan.textContent = `第 ${this.current} / ${this.pages} 頁，共 ${this.total} 筆資料 (每頁 ${this.size} 筆)`;

        // 按鈕區
        const btnGroup = document.createElement("div");
        btnGroup.style.display = "flex";
        btnGroup.style.gap = "0.5rem";

        const prevBtn = document.createElement("button");
        prevBtn.className = "btn btn-secondary btn-sm";
        prevBtn.textContent = "上一頁";
        prevBtn.disabled = this.current <= 1;
        prevBtn.onclick = () => {
            if (this.current > 1) {
                this.current--;
                this.loadData();
            }
        };

        const nextBtn = document.createElement("button");
        nextBtn.className = "btn btn-secondary btn-sm";
        nextBtn.textContent = "下一頁";
        nextBtn.disabled = this.current >= this.pages;
        nextBtn.onclick = () => {
            if (this.current < this.pages) {
                this.current++;
                this.loadData();
            }
        };

        btnGroup.appendChild(prevBtn);
        btnGroup.appendChild(nextBtn);

        this.paginationEl.appendChild(infoSpan);
        this.paginationEl.appendChild(btnGroup);
    }
}
