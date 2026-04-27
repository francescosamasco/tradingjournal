window.TradingCalendar = (function () {

    let currentCalendarDate = new Date(2025, 3, 1); // aprile 2025

   const tradesByDate = window.tradingCalendarData || {};

    function init() {
        const calendarGrid = document.getElementById("calendarGrid");
        const title = document.getElementById("calendarTitle");
        const prev = document.getElementById("prevMonth");
        const next = document.getElementById("nextMonth");

        if (!calendarGrid || !title) return;

        render();

        if (prev) {
            prev.addEventListener("click", function () {
                currentCalendarDate.setMonth(currentCalendarDate.getMonth() - 1);
                render();
            });
        }

        if (next) {
            next.addEventListener("click", function () {
                currentCalendarDate.setMonth(currentCalendarDate.getMonth() + 1);
                render();
            });
        }
    }

    function render() {
        const calendarGrid = document.getElementById("calendarGrid");
        const title = document.getElementById("calendarTitle");
        const monthlyAmount = document.getElementById("monthlyAmount");
        const monthlyDays = document.getElementById("monthlyDays");
        const monthlyTrades = document.getElementById("monthlyTrades");
        const weekSummary = document.getElementById("weekSummary");

        const year = currentCalendarDate.getFullYear();
        const month = currentCalendarDate.getMonth();

        title.textContent = currentCalendarDate.toLocaleString("en-US", {
            month: "long",
            year: "numeric"
        });

        calendarGrid.innerHTML = "";

        renderDayNames(calendarGrid);

        const firstDay = new Date(year, month, 1).getDay();
        const totalDays = new Date(year, month + 1, 0).getDate();

        renderEmptyCells(calendarGrid, firstDay);

        const monthData = renderMonthDays(calendarGrid, year, month, totalDays);

        const remainingCells = getRemainingCells(firstDay, totalDays);
        renderEmptyCells(calendarGrid, remainingCells);

        const totalCalendarCells = firstDay + totalDays + remainingCells;
        const weeksCount = totalCalendarCells / 7;

        updateMonthlyStats(monthlyAmount, monthlyDays, monthlyTrades, monthData);
        renderWeekSummary(monthData.weeks, weekSummary, weeksCount);
    }

    function renderDayNames(calendarGrid) {
        const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

        days.forEach(function (day) {
            const dayName = document.createElement("div");
            dayName.className = "cal-day-name";
            dayName.textContent = day;
            calendarGrid.appendChild(dayName);
        });
    }

    function renderEmptyCells(calendarGrid, count) {
        for (let i = 0; i < count; i++) {
            const emptyCell = document.createElement("div");
            emptyCell.className = "cal-cell empty";
            calendarGrid.appendChild(emptyCell);
        }
    }

    function renderMonthDays(calendarGrid, year, month, totalDays) {
        const monthData = {
            amount: 0,
            tradeDays: 0,
            tradeCount: 0,
            weeks: {}
        };

        for (let day = 1; day <= totalDays; day++) {
            const cell = document.createElement("div");
            cell.className = "cal-cell";

            const dateKey = formatDateKey(year, month, day);
            const trade = tradesByDate[dateKey];

            cell.innerHTML = `
                <span class="cal-number">${String(day).padStart(2, "0")}</span>
            `;

            if (trade) {
                applyTradeToCell(cell, trade);
                updateMonthData(monthData, trade, year, month, day);
            }

            calendarGrid.appendChild(cell);
        }

        return monthData;
    }

    function applyTradeToCell(cell, trade) {
        cell.classList.add(trade.amount >= 0 ? "positive" : "negative");

        const box = document.createElement("div");
        box.className = "trade-box";

        box.innerHTML = `
            ${formatMoney(trade.amount)}
            <small>${trade.trades} trades</small>
        `;

        cell.appendChild(box);
    }

    function updateMonthData(monthData, trade, year, month, day) {
        monthData.amount += trade.amount;
        monthData.tradeDays++;
        monthData.tradeCount += trade.trades;

        const weekNumber = getWeekOfMonth(year, month, day);

        if (!monthData.weeks[weekNumber]) {
            monthData.weeks[weekNumber] = {
                amount: 0,
                days: 0
            };
        }

        monthData.weeks[weekNumber].amount += trade.amount;
        monthData.weeks[weekNumber].days++;
    }

    function updateMonthlyStats(monthlyAmount, monthlyDays, monthlyTrades, monthData) {
        if (monthlyAmount) {
            monthlyAmount.textContent = formatMoney(monthData.amount);
            monthlyAmount.className = monthData.amount >= 0 ? "text-success" : "text-danger";
        }

        if (monthlyDays) {
            monthlyDays.textContent = monthData.tradeDays + " days";
        }

        if (monthlyTrades) {
            monthlyTrades.textContent = monthData.tradeCount + " trades";
        }
    }

    function renderWeekSummary(weeks, container, weeksCount) {
        if (!container) return;

        container.innerHTML = "";
        container.style.gridTemplateRows = `repeat(${weeksCount}, 1fr)`;

        for (let i = 1; i <= weeksCount; i++) {
            const week = weeks[i] || {
                amount: 0,
                days: 0
            };

            const card = document.createElement("div");
            card.className = "week-card" + (week.amount < 0 ? " negative" : "");

            card.innerHTML = `
                <strong>Week ${i}</strong>
                <span>${week.days > 0 ? formatMoney(week.amount) : "--"}</span>
                <small>${week.days} days</small>
            `;

            container.appendChild(card);
        }
    }

    function getRemainingCells(firstDay, totalDays) {
        const totalCellsUsed = firstDay + totalDays;
        return totalCellsUsed % 7 === 0 ? 0 : 7 - (totalCellsUsed % 7);
    }

    function formatDateKey(year, month, day) {
        return year + "-" +
            String(month + 1).padStart(2, "0") + "-" +
            String(day).padStart(2, "0");
    }

    function formatMoney(value) {
        const sign = value >= 0 ? "$" : "-$";
        return sign + Math.abs(value).toLocaleString("en-US");
    }

    function getWeekOfMonth(year, month, day) {
        const firstDay = new Date(year, month, 1).getDay();
        return Math.ceil((day + firstDay) / 7);
    }

    return {
        init: init
    };

})();