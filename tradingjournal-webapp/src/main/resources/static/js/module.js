document.addEventListener("DOMContentLoaded", function () {

    initBalanceChart();
    initDrawdownChart();

    if (window.TradingCalendar) {
        window.TradingCalendar.init();
    }

    initTradesTable(); // 👈 QUI
});

/* =========================
   CHART: ACCOUNT BALANCE
========================= */

function initBalanceChart() {
    const ctx = document.getElementById("balanceChart");
    if (!ctx) return;

    new Chart(ctx, {
        type: "line",
        data: {
            labels: ["01/05", "08/05", "15/05", "22/05", "29/05"],
            datasets: [{
                label: "Account Balance",
                data: [25000, 25250, 25180, 25700, 26000],
                borderWidth: 2,
                tension: 0.35,
                pointRadius: 2
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: true
                }
            }
        }
    });
}

/* =========================
   CHART: DRAWDOWN
========================= */

function initDrawdownChart() {
    const ctx = document.getElementById("drawdownChart");
    if (!ctx) return;

    new Chart(ctx, {
        type: "line",
        data: {
            labels: ["1", "2", "3", "4", "5", "6", "7"],
            datasets: [{
                label: "Drawdown",
                data: [0, -50, -20, -120, -30, -90, -10],
                borderWidth: 2,
                tension: 0.35,
                pointRadius: 2
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: true
                }
            }
        }
    });
}

function initTradesTable() {

    const data = window.tradingCalendarData || {};
    const tbody = document.getElementById("tradesTableBody");

    if (!tbody) return;

    renderTradesTable(data, tbody);
}

function renderTradesTable(tradesByDate, tbody) {

    tbody.innerHTML = "";

    const keys = Object.keys(tradesByDate).sort();

    if (keys.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center text-muted py-4">
                    Nessun trade disponibile
                </td>
            </tr>
        `;
        return;
    }

    keys.forEach(function (dateKey) {

        const trade = tradesByDate[dateKey];
        const positive = trade.amount >= 0;

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${formatDateItalian(dateKey)}</td>
            <td class="${positive ? "trade-result-positive" : "trade-result-negative"}">
                ${formatMoney(trade.amount)}
            </td>
            <td>${trade.trades}</td>
            <td>
                <span class="trade-badge ${positive ? "positive" : "negative"}">
                    ${positive ? "Profit" : "Loss"}
                </span>
            </td>
        `;

        tbody.appendChild(row);
    });
}

function formatDateItalian(dateKey) {
    const parts = dateKey.split("-");
    return parts[2] + "/" + parts[1] + "/" + parts[0];
}

function formatMoney(value) {
    const sign = value >= 0 ? "$" : "-$";
    return sign + Math.abs(value).toLocaleString("en-US");
}
