document.addEventListener("DOMContentLoaded", function () {

	initWeeklyPerformance();
	initCalendarViewToggle();
	initCurrentMonthButton();

	if (window.TradingCalendar) {
		window.TradingCalendar.init();
	}

});

/* =========================
   CHART COLORS
========================= */

const CHART_COLORS = {
	win: "#4EBF94",
	loss: "#F06363",
	be: "#f8fafc",
	miss: "#e5e7eb",
	empty: "#e5e7eb"
};

/* =========================
   EMPTY DOUGHNUT PLUGIN
========================= */

const emptyDoughnutPlugin = {
	id: "emptyDoughnutText",

	beforeDraw(chart) {
		const { ctx, chartArea } = chart;

		if (!chartArea) return;

		const isEmpty = chart.config._isEmpty === true;

		if (!isEmpty) return;

		const centerX = (chartArea.left + chartArea.right) / 2;
		const centerY = (chartArea.top + chartArea.bottom) / 2;

		ctx.save();
		ctx.font = "700 12px Roboto";
		ctx.fillStyle = "#94a3b8";
		ctx.textAlign = "center";
		ctx.textBaseline = "middle";
		ctx.fillText("No data", centerX, centerY);
		ctx.restore();
	}
};

/* =========================
   WEEKLY DOUGHNUTS
========================= */

let weeklyWinLossChart = null;
let weeklyOutcomeChart = null;

function initWeeklyPerformance() {
	const weeks = window.weekSummaryData || {};
	const weekKeys = Object.keys(weeks);

	if (!weekKeys.length) {
		renderEmptyWeeklyPerformance();
		updateRingCircleFromWeekly({});
		return;
	}

	const currentWeek = getCurrentIsoWeek();

	const selectedWeekKey = weekKeys.includes(String(currentWeek))
		? String(currentWeek)
		: weekKeys[weekKeys.length - 1];

	const week = weeks[selectedWeekKey];

	renderWeeklyStats(selectedWeekKey, week);
	renderWeeklyDoughnut(week);
	updateRingCircleFromWeekly(week);
}

function renderWeeklyStats(weekNumber, week) {
	const amount = getNumberValue(week, ["amount", "total", "profit", "netPnl"], 0);
	const rr = getNumberValue(week, ["rr", "rrAverage", "riskReward"], null);
	const percent = getNumberValue(week, ["percent", "percentage", "profitPercent"], null);
	const days = getNumberValue(week, ["days", "tradingDays"], 0);
	const trades = getNumberValue(week, ["trades", "tradeCount", "totalTrades"], 0);
	const wins = getNumberValue(week, ["wins", "win", "winTrades"], 0);
	const losses = getNumberValue(week, ["losses", "loss", "lossTrades"], 0);

	const winrate = wins + losses > 0 ? (wins / (wins + losses)) * 100 : null;

	setText("weeklyTitle", "Week " + weekNumber);
	setValueWithProfitClass("weeklyAmount", formatMoney(amount), amount);

	const label = document.getElementById("weeklyResultLabel");
	if (label) {
		label.textContent = amount >= 0 ? "Profit" : "Loss";
		label.classList.toggle("text-success", amount >= 0);
		label.classList.toggle("text-danger", amount < 0);
	}

	setText("weeklyWinrate", winrate !== null ? winrate.toFixed(1) + "%" : "--");
	setText("weeklyRR", rr !== null ? rr.toFixed(2) : "--");
	setText("weeklyPercent", percent !== null ? formatSignedPercent(percent) : "--");
	setText("weeklyDays", days);
	setText("weeklyTrades", trades);
}

function renderWeeklyDoughnut(week) {
	renderWeeklyWinLossChart(week);
	renderWeeklyOutcomeChart(week);
}

function renderWeeklyWinLossChart(week) {
	const ctx = document.getElementById("weeklyWinLossChart");
	if (!ctx) return;

	const wins = getNumberValue(week, ["wins", "win", "winTrades"], 0);
	const losses = getNumberValue(week, ["losses", "loss", "lossTrades"], 0);

	const total = wins + losses;
	const isEmpty = total === 0;

	if (weeklyWinLossChart) {
		weeklyWinLossChart.destroy();
	}

	weeklyWinLossChart = new Chart(ctx, {
		type: "doughnut",
		data: {
			labels: isEmpty ? [] : ["Win", "Loss"],
			datasets: [{
				data: isEmpty ? [1] : [wins, losses],
				backgroundColor: isEmpty
					? [CHART_COLORS.empty]
					: [
						CHART_COLORS.win,
						CHART_COLORS.loss
					],
				borderWidth: 0,
				cutout: "72%",
				hoverOffset: isEmpty ? 0 : 6
			}]
		},
		options: getWeeklyDoughnutOptions(total),
		plugins: [emptyDoughnutPlugin]
	});

	weeklyWinLossChart.config._isEmpty = isEmpty;
	weeklyWinLossChart.update();
}

function renderWeeklyOutcomeChart(week) {
	const ctx = document.getElementById("weeklyOutcomeChart");
	if (!ctx) return;

	const wins = getNumberValue(week, ["wins", "win", "winTrades"], 0);
	const losses = getNumberValue(week, ["losses", "loss", "lossTrades"], 0);
	const breakeven = getNumberValue(week, ["breakeven", "be", "breakEven"], 0);
	const miss = getNumberValue(week, ["miss", "missed", "missTrades"], 0);

	const total = wins + losses + breakeven + miss;
	const isEmpty = total === 0;

	if (weeklyOutcomeChart) {
		weeklyOutcomeChart.destroy();
	}

	weeklyOutcomeChart = new Chart(ctx, {
		type: "doughnut",
		data: {
			labels: isEmpty ? [] : ["Win", "Loss", "BE", "Miss"],
			datasets: [{
				data: isEmpty ? [1] : [wins, losses, breakeven, miss],
				backgroundColor: isEmpty
					? [CHART_COLORS.empty]
					: [
						CHART_COLORS.win,
						CHART_COLORS.loss,
						CHART_COLORS.be,
						CHART_COLORS.miss
					],
				borderWidth: 0,
				cutout: "72%",
				hoverOffset: isEmpty ? 0 : 6
			}]
		},
		options: getWeeklyDoughnutOptions(total),
		plugins: [emptyDoughnutPlugin]
	});

	weeklyOutcomeChart.config._isEmpty = isEmpty;
	weeklyOutcomeChart.update();
}

function getWeeklyDoughnutOptions(total) {
	return {
		responsive: true,
		maintainAspectRatio: false,
		plugins: {
			legend: {
				display: total > 0,
				position: "bottom",
				labels: {
					usePointStyle: true,
					boxWidth: 8,
					font: {
						size: 11
					}
				}
			},
			tooltip: {
				enabled: total > 0,
				callbacks: {
					label: function (context) {
						return context.label + ": " + context.raw;
					}
				}
			}
		}
	};
}

function renderEmptyWeeklyPerformance() {
	setText("weeklyTitle", "Nessun dato");
	setText("weeklyAmount", "--");
	setText("weeklyWinrate", "--");
	setText("weeklyRR", "--");
	setText("weeklyPercent", "--");
	setText("weeklyDays", "--");
	setText("weeklyTrades", "--");
	setText("weeklyResultLabel", "--");

	renderWeeklyDoughnut({});
}

/* =========================
   RING CIRCLE BASATO SU WIN/LOSS
========================= */

function updateRingCircleFromWeekly(week) {
	const ring = document.querySelector(".ring-circle");
	if (!ring) return;

	const wins = getNumberValue(week, ["wins", "win", "winTrades"], 0);
	const losses = getNumberValue(week, ["losses", "loss", "lossTrades"], 0);

	const total = wins + losses;

	let winrate = 0;

	if (total > 0) {
		winrate = wins / total;
	}

	const degrees = Math.round(winrate * 360);
	const percentLabel = total > 0 ? Math.round(winrate * 100) + "%" : "--";

	let color = "#94a3b8";

	if (total > 0) {
		color = winrate >= 0.5 ? CHART_COLORS.win : CHART_COLORS.loss;
	}

	ring.style.background = `conic-gradient(
		${color} 0deg ${degrees}deg,
		#e5e7eb ${degrees}deg 360deg
	)`;

	ring.innerHTML = `<span>${percentLabel}</span>`;
}

/* =========================
   VIEW SWITCH
========================= */

let calendarEquityChart = null;

function initCalendarViewToggle() {
	const buttons = document.querySelectorAll(".view-switch-btn");

	const calendarView = document.getElementById("calendarView");
	const statsView = document.getElementById("calendarStatsView");
	const equityView = document.getElementById("calendarEquityView");

	if (!buttons.length || !calendarView || !statsView || !equityView) return;

	buttons.forEach(function (btn) {
		btn.addEventListener("click", function () {
			const view = btn.dataset.view;

			buttons.forEach(function (b) {
				b.classList.remove("active");
			});

			btn.classList.add("active");

			calendarView.classList.add("d-none");
			statsView.classList.add("d-none");
			equityView.classList.add("d-none");

			if (view === "calendar") {
				calendarView.classList.remove("d-none");
			}

			if (view === "stats") {
				statsView.classList.remove("d-none");
				renderCalendarStatsView();
			}

			if (view === "equity") {
				equityView.classList.remove("d-none");
				renderCalendarEquityChart();
			}
		});
	});
}

function initCurrentMonthButton() {
	const btn = document.getElementById("currentMonthBtn");

	if (!btn) return;

	btn.addEventListener("click", function () {
		const today = new Date();
		const year = today.getFullYear();
		const month = today.getMonth() + 1;

		let url = "/dashboard?year=" + year + "&period=" + month;

		if (window.accountId) {
			url += "&accountId=" + encodeURIComponent(window.accountId);
		}

		window.location.href = url;
	});
}

/* =========================
   ACCOUNT STATS TABLE
========================= */

function renderCalendarStatsView() {
	const days = window.tradingCalendarData || {};
	const dayKeys = Object.keys(days).sort();

	let netProfit = 0;
	let totalProfit = 0;
	let totalLoss = 0;

	let winTrades = 0;
	let lossTrades = 0;
	let totalTrades = 0;

	let largestWin = null;
	let largestLoss = null;

	let maxConsecutiveWins = 0;
	let maxConsecutiveLoss = 0;
	let currentWins = 0;
	let currentLoss = 0;

	let longTrades = 0;
	let shortTrades = 0;

	dayKeys.forEach(function (dateKey) {
		const day = days[dateKey];

		const amount = getNumberValue(day, ["amount", "profit", "netPnl", "total"], 0);
		const trades = getNumberValue(day, ["trades", "tradeCount", "totalTrades"], 1);
		const position = getStringValue(day, ["position", "posizione", "type"], "");

		netProfit += amount;
		totalTrades += trades;

		if (amount > 0) {
			totalProfit += amount;
			winTrades += trades;

			largestWin = largestWin === null ? amount : Math.max(largestWin, amount);

			currentWins++;
			currentLoss = 0;

			maxConsecutiveWins = Math.max(maxConsecutiveWins, currentWins);
		} else if (amount < 0) {
			totalLoss += Math.abs(amount);
			lossTrades += trades;

			largestLoss = largestLoss === null ? amount : Math.min(largestLoss, amount);

			currentLoss++;
			currentWins = 0;

			maxConsecutiveLoss = Math.max(maxConsecutiveLoss, currentLoss);
		}

		if (position.toLowerCase() === "long") {
			longTrades += trades;
		}

		if (position.toLowerCase() === "short") {
			shortTrades += trades;
		}
	});

	const averageWin = winTrades > 0 ? totalProfit / winTrades : 0;
	const averageLoss = lossTrades > 0 ? totalLoss / lossTrades : 0;

	setValueWithProfitClass("statsTotalPnl", formatCurrency(netProfit), netProfit);

	setText("statsAverageWin", formatCurrency(averageWin));
	setText("statsAverageLoss", formatCurrency(averageLoss));

	setValueWithProfitClass("statsLargestWin", largestWin !== null ? formatCurrency(largestWin) : "--", largestWin || 0);
	setValueWithProfitClass("statsLargestLoss", largestLoss !== null ? formatCurrency(largestLoss) : "--", largestLoss || 0);

	setText("statsMaxConsecutiveWins", maxConsecutiveWins);
	setText("statsMaxConsecutiveLoss", maxConsecutiveLoss);

	setText("statsMaxDd", "--");
	setText("statsMaxDailyDd", "--");

	setText("statsTotalProfit", formatCurrency(totalProfit));
	setText("statsTotalLoss", formatCurrency(totalLoss));

	setText("statsLongTrade", longTrades);
	setText("statsShortTrade", shortTrades);

	setText("statsTrades", totalTrades);
	setText("statsDefaultRisk", "1");
}

/* =========================
   CALENDAR EQUITY LINE
========================= */

function renderCalendarEquityChart() {
	const ctx = document.getElementById("calendarEquityChart");
	if (!ctx) return;

	const days = window.tradingCalendarData || {};
	const keys = Object.keys(days).sort();

	let balance = 0;
	const labels = [];
	const data = [];

	keys.forEach(function (dateKey) {
		const day = days[dateKey];
		const amount = getNumberValue(day, ["amount", "profit", "netPnl", "total"], 0);

		balance += amount;

		labels.push(formatDateShort(dateKey));
		data.push(balance);
	});

	if (calendarEquityChart) {
		calendarEquityChart.destroy();
	}

	calendarEquityChart = new Chart(ctx, {
		type: "line",
		data: {
			labels: labels,
			datasets: [{
				label: "Equity Line",
				data: data,
				borderColor: CHART_COLORS.win,
				backgroundColor: "rgba(78, 191, 148, 0.12)",
				borderWidth: 2,
				tension: 0.35,
				pointRadius: 3,
				fill: true
			}]
		},
		options: {
			responsive: true,
			maintainAspectRatio: false,
			plugins: {
				legend: {
					display: true
				}
			},
			scales: {
				y: {
					ticks: {
						callback: function (value) {
							return formatCurrency(value);
						}
					}
				}
			}
		}
	});
}

/* =========================
   UTILS
========================= */

function getNumberValue(obj, keys, defaultValue) {
	if (!obj) return defaultValue;

	for (let i = 0; i < keys.length; i++) {
		const key = keys[i];

		if (obj[key] !== undefined && obj[key] !== null && obj[key] !== "") {
			const value = Number(obj[key]);

			if (!Number.isNaN(value)) {
				return value;
			}
		}
	}

	return defaultValue;
}

function getStringValue(obj, keys, defaultValue) {
	if (!obj) return defaultValue;

	for (let i = 0; i < keys.length; i++) {
		const key = keys[i];

		if (obj[key] !== undefined && obj[key] !== null) {
			return String(obj[key]);
		}
	}

	return defaultValue;
}

function setText(id, value) {
	const el = document.getElementById(id);
	if (el) {
		el.textContent = value;
	}
}

function setValueWithProfitClass(id, text, value) {
	const el = document.getElementById(id);
	if (!el) return;

	el.textContent = text;
	el.classList.remove("text-success", "text-danger");

	if (value > 0) {
		el.classList.add("text-success");
	} else if (value < 0) {
		el.classList.add("text-danger");
	}
}

function formatMoney(value) {
	const sign = value >= 0 ? "+" : "-";

	return sign + "$" + Math.abs(value).toLocaleString("en-US", {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});
}

function formatCurrency(value) {
	const sign = value < 0 ? "-" : "";

	return sign + "€ " + Math.abs(value).toLocaleString("it-IT", {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	});
}

function formatSignedPercent(value) {
	const sign = value >= 0 ? "+" : "-";
	return sign + Math.abs(value).toFixed(2) + "%";
}

function formatDateShort(dateKey) {
	const parts = dateKey.split("-");
	if (parts.length !== 3) return dateKey;

	return parts[2] + "/" + parts[1];
}

function getCurrentIsoWeek() {
	const date = new Date();

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