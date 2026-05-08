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

		const formattedDate = currentCalendarDate.toLocaleString("it-IT", {
			month: "long",
			year: "numeric"
		});

		title.textContent =
			formattedDate.charAt(0).toUpperCase() +
			formattedDate.slice(1);

		calendarGrid.innerHTML = "";

		renderDayNames(calendarGrid);

		const firstDay = getMondayBasedDayIndex(new Date(year, month, 1));
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
	
	function getMondayBasedDayIndex(date) {
		const day = date.getDay();

		return day === 0 ? 6 : day - 1;
	}

	function renderDayNames(calendarGrid) {
		const days = ["Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica"];
		
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
			cell.className = "cal-cell clickable-day";

			const dateKey = formatDateKey(year, month, day);
			const trade = tradesByDate[dateKey];

			cell.dataset.date = dateKey;

			cell.innerHTML = `
				<span class="cal-number">${String(day).padStart(2, "0")}</span>
			`;

			cell.addEventListener("click", function () {
				openAddTradeModal(dateKey);
			});

			if (trade) {
				applyTradeToCell(cell, trade);
				updateMonthData(monthData, trade);
			}

			calendarGrid.appendChild(cell);
		}

		return monthData;
	}

	function openAddTradeModal(dateKey) {
		const modalEl = document.getElementById("addTradeModal");
		const dateInput = document.getElementById("tradeDateInput");

		if (!modalEl || !dateInput) {
			console.warn("Modale aggiungi trade non trovata.");
			return;
		}

		resetAddTradeForm(modalEl);

		dateInput.value = dateKey + "T09:00";

		const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
		modal.show();
	}

	function resetAddTradeForm(modalEl) {
		const form = modalEl.querySelector("form");

		if (form) {
			form.reset();
		}

		const setupSelect = document.getElementById("setupSelect");
		const confluenzeSelect = document.getElementById("confluenzeSelect");

		//if (setupSelect) {
		//	resetSelect(setupSelect, "Seleziona setup");
		//}

		if (confluenzeSelect) {
			resetSelect(confluenzeSelect, "Seleziona confluenza");
		}
	}

	function resetSelect(select, placeholder) {
		select.innerHTML = "";

		const option = document.createElement("option");
		option.value = "";
		option.textContent = placeholder;

		select.appendChild(option);
	}

	function applyTradeToCell(cell, trade) {
		const amount = Number(trade.amount || 0);

		const profitPercent = trade.percentage != null
			? Number(trade.percentage)
			: null;

		cell.classList.add(amount >= 0 ? "positive" : "negative");

		const box = document.createElement("div");
		box.className = "trade-box";

		box.innerHTML = `
			<div class="trade-amount">${formatMoney(amount)}</div>
			<div class="trade-percent">
				${profitPercent != null ? formatPercent(profitPercent) : "--"}
			</div>
			<small>${Number(trade.trades || 0)} trade</small>
		`;

		cell.appendChild(box);
	}

	function formatPercent(value) {
		const numericValue = Number(value || 0);
		const sign = numericValue > 0 ? "+" : "";

		return sign + numericValue.toLocaleString("en-US", {
			minimumFractionDigits: 0,
			maximumFractionDigits: 1
		}) + "%";
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
			monthlyDays.textContent = monthData.tradeDays + " giorni";
		}

		if (monthlyTrades) {
			monthlyTrades.textContent = monthData.tradeCount + " trade";
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
				winrate: null,
				rrAverage: null,
				profitPercent: null
			};

			const amount = Number(week.amount || 0);
			const days = Number(week.days || 0);

			const card = document.createElement("div");
			card.className = "week-card" + (amount < 0 ? " negative" : "");

			card.innerHTML = `
				<strong>Settimana ${weekNumber}</strong>

				<span>${days > 0 ? formatMoney(amount) : "--"}</span>

				<div class="week-extra">
					<div>WR: <b>${week.winrate != null ? Number(week.winrate).toFixed(1) + "%" : "--"}</b></div>
					<div>RR: <b>${week.rrAverage != null ? Number(week.rrAverage).toFixed(1) : "--"}</b></div>
				</div>

				<div class="week-footer">
					<small>%: <b>${week.profitPercent != null ? Number(week.profitPercent).toFixed(1) + "%" : "--"}</b></small>
					<small>${days} giorni</small>
				</div>
			`;

			container.appendChild(card);
		});
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
		const tempDate = new Date(Date.UTC(
			date.getFullYear(),
			date.getMonth(),
			date.getDate()
		));

		const dayNum = tempDate.getUTCDay() || 7;

		tempDate.setUTCDate(tempDate.getUTCDate() + 4 - dayNum);

		const yearStart = new Date(Date.UTC(tempDate.getUTCFullYear(), 0, 1));

		return Math.ceil((((tempDate - yearStart) / 86400000) + 1) / 7);
	}

	function getRemainingCells(firstDay, totalDays) {
		const totalCellsUsed = firstDay + totalDays;
		return totalCellsUsed % 7 === 0 ? 0 : 7 - (totalCellsUsed % 7);
	}

	function formatDateKey(year, month, day) {
		return year + "-"
			+ String(month + 1).padStart(2, "0") + "-"
			+ String(day).padStart(2, "0");
	}

	function formatMoney(value) {
		const numericValue = Number(value || 0);
		const sign = numericValue >= 0 ? "$" : "-$";

		return sign + Math.abs(numericValue).toLocaleString("en-US", {
			minimumFractionDigits: 0,
			maximumFractionDigits: 2
		});
	}

	return {
		init: init,
		openAddTradeModal: openAddTradeModal
	};

})();