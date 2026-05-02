window.TradingCalendar = (function () {

	let currentCalendarDate = new Date(
	    Number(window.calendarYear),
	    Number(window.calendarMonth) - 1,
	    1
	);
	
    const tradesByDate = window.tradingCalendarData || {};
    const weekSummaryData = window.weekSummaryData || {};

    function init() {
        const calendarGrid = document.getElementById("calendarGrid");
        const title = document.getElementById("calendarTitle");
        const prev = document.getElementById("prevMonth");
        const next = document.getElementById("nextMonth");

        if (!calendarGrid || !title) return;

        render();

        if (prev) {
			prev.addEventListener("click", function () {
			    goToMonth(-1);
			});
        }

        if (next) {
			next.addEventListener("click", function () {
			    goToMonth(1);
			});
        }
    }

	function goToMonth(offset) {
	    const newDate = new Date(
	        currentCalendarDate.getFullYear(),
	        currentCalendarDate.getMonth() + offset,
	        1
	    );

	    const params = new URLSearchParams(window.location.search);

	    params.set("year", newDate.getFullYear());
	    params.set("period", newDate.getMonth() + 1);

	    if (window.accountId) {
	        params.set("accountId", window.accountId);
	    }

	    window.location.href = "/dashboard?" + params.toString();
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
		const visibleWeeks = getVisibleWeeks(year, month, weeksCount);
		renderWeekSummary(weekSummaryData, weekSummary, visibleWeeks);
	}

	function getVisibleWeeks(year, month, weeksCount) {
	    const weeks = [];

	    for (let row = 0; row < weeksCount; row++) {
	        const referenceDate = new Date(year, month, 1 + (row * 7));
	        weeks.push(getIsoWeek(referenceDate));
	    }

	    return weeks;
	}

	function getIsoWeek(date) {
	    const tempDate = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
	    const dayNum = tempDate.getUTCDay() || 7;

	    tempDate.setUTCDate(tempDate.getUTCDate() + 4 - dayNum);

	    const yearStart = new Date(Date.UTC(tempDate.getUTCFullYear(), 0, 1));

	    return Math.ceil((((tempDate - yearStart) / 86400000) + 1) / 7);
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
            tradeCount: 0
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
                updateMonthData(monthData, trade);
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

    function updateMonthData(monthData, trade) {
        monthData.amount += Number(trade.amount || 0);
        monthData.tradeDays++;
        monthData.tradeCount += Number(trade.trades || 0);
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

	function renderWeekSummary(weeks, container, visibleWeeks) {
	    if (!container) return;

	    container.innerHTML = "";
	    container.style.gridTemplateRows = `repeat(${visibleWeeks.length}, 1fr)`;

	    visibleWeeks.forEach(function (weekNumber) {

	        const week = weeks[weekNumber] || {
	            amount: 0,
	            days: 0,
	            trades: 0,
	            rrAverage: null,
	            profitPercent: null
	        };

	        const amount = Number(week.amount || 0);
	        const days = Number(week.days || 0);

	        const card = document.createElement("div");
	        card.className = "week-card" + (amount < 0 ? " negative" : "");

	        card.innerHTML = `
	            <strong>Week ${weekNumber}</strong>

	            <span>${days > 0 ? formatMoney(amount) : "--"}</span>

				<div class="week-extra">
					<div>%: <b>${week.winrate != null ? Number(week.winrate).toFixed(1) + "%" : "--"}</b></div>
				    <div>RR: <b>${week.rrAverage != null ? Number(week.rrAverage).toFixed(1) : "--"}</b></div>
				</div>

				<div class="week-footer">
				    <small>WR: <b>${week.profitPercent != null ? Number(week.profitPercent).toFixed(1) + "%" : "--"}</b></small>
					<small>${days} days</small>
				</div>
	        `;

	        container.appendChild(card);
	    });
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
        const numericValue = Number(value || 0);
        const sign = numericValue >= 0 ? "$" : "-$";
        return sign + Math.abs(numericValue).toLocaleString("en-US");
    }

    return {
        init: init
    };

})();