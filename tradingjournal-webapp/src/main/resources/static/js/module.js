document.addEventListener("DOMContentLoaded", function () {

	initWeeklyPerformance();
	updateRingCircleFromMonthlyWinrate();

	initCalendarViewToggle();
	initCurrentMonthButton();
	initTradeDynamicSelects();
	initProfitLossControl();
	initAccountBalancePreview();
	
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
   CENTER TEXT DOUGHNUT PLUGIN
========================= */

const doughnutCenterTextPlugin = {
	id: "doughnutCenterText",

	beforeDraw(chart) {
		const { ctx, chartArea } = chart;

		if (!chartArea) return;

		const isEmpty = chart.config._isEmpty === true;
		const centerText = chart.config._centerText;

		if (isEmpty || !centerText) return;

		const centerX = (chartArea.left + chartArea.right) / 2;
		const centerY = (chartArea.top + chartArea.bottom) / 2;

		ctx.save();
		ctx.textAlign = "center";
		ctx.textBaseline = "middle";

		ctx.font = "900 18px Roboto";
		ctx.fillStyle = centerText.color || "#0f172a";
		ctx.fillText(centerText.main, centerX, centerY - 5);

		if (centerText.sub) {
			ctx.font = "700 11px Roboto";
			ctx.fillStyle = "#64748b";
			ctx.fillText(centerText.sub, centerX, centerY + 14);
		}

		ctx.restore();
	}
};

/* =========================
   WEEKLY PERFORMANCE
========================= */

let weeklyWinLossChart = null;
let weeklyOutcomeChart = null;

let currentWeekIndex = 0;
let weekKeysOrdered = [];

function initWeeklyPerformance() {
	const weeks = window.weekSummaryData || {};

	weekKeysOrdered = Object.keys(weeks).sort(function (a, b) {
		return Number(a) - Number(b);
	});

	if (!weekKeysOrdered.length) {
		renderEmptyWeeklyPerformance();
		initWeeklyModeSwitch();
		return;
	}

	const currentWeek = getCurrentIsoWeek();
	const currentWeekKey = String(currentWeek);

	currentWeekIndex = weekKeysOrdered.indexOf(currentWeekKey);

	if (currentWeekIndex === -1) {
		currentWeekIndex = weekKeysOrdered.length - 1;
	}

	renderSelectedWeek();
	initWeeklyNavigation();
	initWeeklyModeSwitch();
	initCurrentWeekButton();
}

function renderSelectedWeek() {
	const weekKey = weekKeysOrdered[currentWeekIndex];
	const week = window.weekSummaryData ? window.weekSummaryData[weekKey] : {};

	renderWeeklyStats(weekKey, week);
	renderWeeklyDoughnut(week);
	updateWeeklyNavigationState();
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
	setValueWithProfitClass("weeklyAmountStats", formatMoney(amount), amount);

	const label = document.getElementById("weeklyResultLabel");

	if (label) {
		label.textContent = amount > 0 ? "Profit" : amount < 0 ? "Loss" : "Flat";
		label.classList.remove("text-success", "text-danger");

		if (amount > 0) {
			label.classList.add("text-success");
		} else if (amount < 0) {
			label.classList.add("text-danger");
		}
	}

	setText("weeklyWinrate", winrate !== null ? winrate.toFixed(1) + "%" : "--");
	setText("weeklyRR", rr !== null ? rr.toFixed(2) : "--");
	setText("weeklyPercent", percent !== null ? formatSignedPercent(percent) : "--");
	setText("weeklyDays", days);
	setText("weeklyTrades", trades);

	setText("weeklyKpiWinrate", winrate !== null ? winrate.toFixed(1) + "%" : "--");
	setText("weeklyKpiRR", rr !== null ? rr.toFixed(2) : "--");

	setValueWithProfitClass(
		"weeklyKpiPercent",
		percent !== null ? formatSignedPercent(percent) : "--",
		percent !== null ? percent : 0
	);
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
	const winrate = total > 0 ? (wins / total) * 100 : 0;

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
					: [CHART_COLORS.win, CHART_COLORS.loss],
				borderWidth: 0,
				cutout: "72%",
				hoverOffset: isEmpty ? 0 : 6
			}]
		},
		options: getWeeklyDoughnutOptions(total),
		plugins: [emptyDoughnutPlugin, doughnutCenterTextPlugin]
	});

	weeklyWinLossChart.config._isEmpty = isEmpty;
	weeklyWinLossChart.config._centerText = {
		main: isEmpty ? "" : winrate.toFixed(1) + "%",
		sub: "Winrate",
		color: winrate >= 50 ? CHART_COLORS.win : CHART_COLORS.loss
	};

	weeklyWinLossChart.update();
}

function renderWeeklyOutcomeChart(week) {
	const ctx = document.getElementById("weeklyOutcomeChart");
	if (!ctx) return;

	const wins = getNumberValue(week, ["wins", "win", "winTrades"], 0);
	const losses = getNumberValue(week, ["losses", "loss", "lossTrades"], 0);
	const breakeven = getNumberValue(week, ["breakeven", "be", "breakEven", "beTrades"], 0);
	const miss = getNumberValue(week, ["miss", "missed", "missTrades"], 0);
	const percent = getNumberValue(week, ["percent", "percentage", "profitPercent"], null);

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
		plugins: [emptyDoughnutPlugin, doughnutCenterTextPlugin]
	});

	weeklyOutcomeChart.config._isEmpty = isEmpty;
	weeklyOutcomeChart.config._centerText = {
		main: percent !== null ? formatSignedPercent(percent) : "--",
		sub: "Profit %",
		color: percent !== null && percent < 0 ? CHART_COLORS.loss : CHART_COLORS.win
	};

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
	setText("weeklyAmountStats", "--");
	setText("weeklyWinrate", "--");
	setText("weeklyRR", "--");
	setText("weeklyPercent", "--");
	setText("weeklyDays", "--");
	setText("weeklyTrades", "--");
	setText("weeklyResultLabel", "--");
	setText("weeklyKpiWinrate", "--");
	setText("weeklyKpiRR", "--");
	setText("weeklyKpiPercent", "--");

	renderWeeklyDoughnut({});
}

function initWeeklyNavigation() {
	const prev = document.getElementById("prevWeek");
	const next = document.getElementById("nextWeek");

	if (prev) {
		prev.addEventListener("click", function () {
			if (currentWeekIndex > 0) {
				currentWeekIndex--;
				renderSelectedWeek();
			}
		});
	}

	if (next) {
		next.addEventListener("click", function () {
			if (currentWeekIndex < weekKeysOrdered.length - 1) {
				currentWeekIndex++;
				renderSelectedWeek();
			}
		});
	}
}

function updateWeeklyNavigationState() {
	const prev = document.getElementById("prevWeek");
	const next = document.getElementById("nextWeek");

	if (prev) {
		prev.disabled = currentWeekIndex <= 0;
		prev.classList.toggle("disabled", currentWeekIndex <= 0);
	}

	if (next) {
		next.disabled = currentWeekIndex >= weekKeysOrdered.length - 1;
		next.classList.toggle("disabled", currentWeekIndex >= weekKeysOrdered.length - 1);
	}
}

function initWeeklyModeSwitch() {
	const buttons = document.querySelectorAll(".weekly-switch-btn");

	const chartsView = document.getElementById("weeklyChartsView");
	const statsView = document.getElementById("weeklyStatsView");

	if (!buttons.length || !chartsView || !statsView) return;

	buttons.forEach(function (btn) {
		btn.addEventListener("click", function () {
			const mode = btn.dataset.mode;

			buttons.forEach(function (b) {
				b.classList.remove("active");
			});

			btn.classList.add("active");

			if (mode === "charts") {
				chartsView.classList.remove("d-none");
				statsView.classList.add("d-none");

				if (weeklyWinLossChart) weeklyWinLossChart.resize();
				if (weeklyOutcomeChart) weeklyOutcomeChart.resize();
			}

			if (mode === "stats") {
				chartsView.classList.add("d-none");
				statsView.classList.remove("d-none");
			}
		});
	});
}

function initCurrentWeekButton() {
	const btn = document.getElementById("currentWeekBtn");
	if (!btn) return;

	btn.addEventListener("click", function () {
		const currentWeek = getCurrentIsoWeek();
		const index = weekKeysOrdered.indexOf(String(currentWeek));

		currentWeekIndex = index !== -1 ? index : weekKeysOrdered.length - 1;

		renderSelectedWeek();
	});
}

/* =========================
   MODALE TRADE: CONFLUENZE CHECKBOX
========================= */

function initProfitLossControl() {
	const resultSelect = document.querySelector("[name='result']");
	const profitLossInput = document.querySelector("[name='profitLoss']");

	if (!resultSelect || !profitLossInput) return;

	function applyProfitLossRules() {
		const result = resultSelect.value;

		// RESET BASE
		profitLossInput.readOnly = false;
		profitLossInput.disabled = false;

		// ❌ SE NON È SELEZIONATO NULLA → DISABILITA
		if (!result) {
			profitLossInput.value = "";
			profitLossInput.disabled = true;
			profitLossInput.placeholder = "Seleziona prima il risultato";
			return;
		}

		// ✅ WIN
		if (result === "WIN") {
			profitLossInput.min = "0";
			profitLossInput.removeAttribute("max");
			profitLossInput.placeholder = "Valore positivo";

			if (profitLossInput.value && Number(profitLossInput.value) < 0) {
				profitLossInput.value = "";
			}
		}

		// ❌ LOSS
		else if (result === "LOSS") {
			profitLossInput.max = "0";
			profitLossInput.removeAttribute("min");
			profitLossInput.placeholder = "Valore negativo";

			if (profitLossInput.value && Number(profitLossInput.value) > 0) {
				profitLossInput.value = "";
			}
		}

		// ➖ BE / MISS
		else if (result === "BE" || result === "MISS") {
			profitLossInput.value = "0";
			profitLossInput.readOnly = true;
			profitLossInput.min = "0";
			profitLossInput.max = "0";
			profitLossInput.placeholder = "0";
		}

		// 🔄 trigger ricalcolo balance
		profitLossInput.dispatchEvent(new Event("input"));
	}

	resultSelect.addEventListener("change", applyProfitLossRules);

	profitLossInput.addEventListener("input", function () {
		const result = resultSelect.value;
		const value = Number(profitLossInput.value);

		if (result === "WIN" && value < 0) {
			profitLossInput.value = "";
		}

		if (result === "LOSS" && value > 0) {
			profitLossInput.value = "";
		}

		if (result === "BE" || result === "MISS") {
			profitLossInput.value = "0";
		}
	});

	// stato iniziale
	applyProfitLossRules();
}

function initAccountBalancePreview() {
	const dateTimeInput = document.querySelector("[name='dateTime']");
	const profitLossInput = document.querySelector("[name='profitLoss']");
	const accountBalanceInput = document.querySelector("[name='accountBalance']");
	const accountIdInput = document.querySelector("[name='accountId']");
	const addTradeModal = document.getElementById("addTradeModal");

	if (!dateTimeInput || !profitLossInput || !accountBalanceInput || !accountIdInput) {
		return;
	}

	let debounceTimer = null;

	function calculateBalance() {
		clearTimeout(debounceTimer);

		debounceTimer = setTimeout(() => {
			const dateTime = dateTimeInput.value;
			const profitLoss = profitLossInput.value || "0";
			const accountId = accountIdInput.value;

			if (!dateTime || !accountId) {
				accountBalanceInput.value = "";
				return;
			}

			const params = new URLSearchParams({
				accountId: accountId,
				dateTime: dateTime,
				profitLoss: profitLoss
			});

			fetch(`/api/dashboard/account-balance-preview?${params.toString()}`)
				.then(response => {
					if (!response.ok) {
						throw new Error("Errore calcolo account balance");
					}
					return response.json();
				})
				.then(balance => {
					accountBalanceInput.value = Number(balance).toFixed(2);
				})
				.catch(error => {
					console.error(error);
					accountBalanceInput.value = "";
				});
		}, 250);
	}

	dateTimeInput.addEventListener("change", calculateBalance);
	dateTimeInput.addEventListener("input", calculateBalance);
	profitLossInput.addEventListener("input", calculateBalance);
	profitLossInput.addEventListener("change", calculateBalance);

	if (addTradeModal) {
		addTradeModal.addEventListener("shown.bs.modal", calculateBalance);
	}
}


function initTradeDynamicSelects() {
	const modal = document.getElementById("addTradeModal");
	const strutturaSelect = document.getElementById("strutturaSelect");
	const setupSelect = document.getElementById("setupSelect");

	if (!modal) return;

	modal.addEventListener("hidden.bs.modal", function () {
		resetTradeDynamicSelects();
	});

	if (!strutturaSelect || !setupSelect) return;

	strutturaSelect.addEventListener("change", function () {
		resetConfluenzeBox("Seleziona struttura e setup");
		resetVotoSetup();

		const struttura = strutturaSelect.value;
		const setup = setupSelect.value;

		if (struttura && setup) {
			loadConfluenze(struttura, setup);
		}
	});

	setupSelect.addEventListener("change", function () {
		resetConfluenzeBox("Seleziona struttura e setup");
		resetVotoSetup();

		const struttura = strutturaSelect.value;
		const setup = setupSelect.value;

		if (struttura && setup) {
			loadConfluenze(struttura, setup);
		}
	});
}

function loadConfluenze(struttura, setup) {
	const box = document.getElementById("confluenzeCheckboxGroup");
	const hiddenInput = document.getElementById("confluenzeInput");

	if (!box || !hiddenInput) return;

	box.classList.add("disabled");
	box.innerHTML = `<div class="text-muted small">Caricamento confluenze...</div>`;
	hiddenInput.value = "";
	resetVotoSetup();

	const url = "/api/dashboard/confluenze"
		+ "?struttura=" + encodeURIComponent(struttura)
		+ "&setup=" + encodeURIComponent(setup);

	fetch(url)
		.then(function (response) {
			if (!response.ok) {
				throw new Error("Errore HTTP " + response.status);
			}

			return response.json();
		})
		.then(function (items) {
			renderConfluenzeCheckboxes(items);
		})
		.catch(function (error) {
			console.error("Errore caricamento confluenze:", error);
			box.innerHTML = `<div class="text-danger small">Errore caricamento confluenze</div>`;
			box.classList.add("disabled");
			hiddenInput.value = "";
			resetVotoSetup("Errore");
		});
}

function renderConfluenzeCheckboxes(items) {
	const box = document.getElementById("confluenzeCheckboxGroup");
	const hiddenInput = document.getElementById("confluenzeInput");

	if (!box || !hiddenInput) return;

	box.innerHTML = "";
	hiddenInput.value = "";

	if (!Array.isArray(items) || items.length === 0) {
		box.innerHTML = `<div class="text-muted small">Nessuna confluenza disponibile</div>`;
		box.classList.add("disabled");
		return;
	}

	items.forEach(function (item) {
		const id = item.id || item.value || item.codice || "";
		const label = item.descrizione || item.label || item.text || item.nome || "";

		const wrapper = document.createElement("label");
		wrapper.className = "confluenza-check";

		wrapper.innerHTML = `
			<input type="checkbox" value="${escapeHtml(id)}">
			<span>${escapeHtml(label)}</span>
		`;

		const checkbox = wrapper.querySelector("input");

		checkbox.addEventListener("change", function () {
			updateSelectedConfluenze();
			loadVotoSetup();
		});

		box.appendChild(wrapper);
	});

	box.classList.remove("disabled");
}

function updateSelectedConfluenze() {
	const box = document.getElementById("confluenzeCheckboxGroup");
	const hiddenInput = document.getElementById("confluenzeInput");

	if (!box || !hiddenInput) return [];

	const selected = Array.from(box.querySelectorAll('input[type="checkbox"]:checked'))
		.map(function (checkbox) {
			return checkbox.value;
		})
		.filter(function (value) {
			return value !== "";
		});

	hiddenInput.value = selected.join(",");

	return selected;
}

function loadVotoSetup() {
	const strutturaSelect = document.getElementById("strutturaSelect");
	const setupSelect = document.getElementById("setupSelect");
	const votoSetupSelect = document.getElementById("votoSetupSelect");

	if (!strutturaSelect || !setupSelect || !votoSetupSelect) return;

	const struttura = strutturaSelect.value;
	const setup = setupSelect.value;
	const confluenze = updateSelectedConfluenze();

	resetVotoSetup();

	if (!struttura || !setup || !confluenze.length) return;

	const url = "/api/dashboard/voto-setup"
		+ "?struttura=" + encodeURIComponent(struttura)
		+ "&setup=" + encodeURIComponent(setup)
		+ "&confluenze=" + encodeURIComponent(confluenze.join(","));

	fetch(url)
		.then(function (response) {
			if (!response.ok) {
				throw new Error("Errore HTTP " + response.status);
			}

			return response.json();
		})
		.then(function (data) {
			if (!data) return;

			const voto = data.id || data.value || data.codice || data.voto || "";
			const descrizione = data.descrizione || data.label || data.text || voto || "";

			votoSetupSelect.innerHTML = "";

			const option = document.createElement("option");
			option.value = voto;
			option.textContent = descrizione;
			option.selected = true;

			votoSetupSelect.appendChild(option);
			votoSetupSelect.disabled = false;
		})
		.catch(function (error) {
			console.error("Errore caricamento voto setup:", error);
			resetVotoSetup("Errore calcolo voto");
		});
}

function resetTradeDynamicSelects() {
	resetConfluenzeBox("Seleziona struttura e setup");
	resetVotoSetup();
}

function resetConfluenzeBox(label) {
	const box = document.getElementById("confluenzeCheckboxGroup");
	const hiddenInput = document.getElementById("confluenzeInput");

	if (box) {
		box.innerHTML = `<div class="text-muted small">${label || "Seleziona struttura e setup"}</div>`;
		box.classList.add("disabled");
	}

	if (hiddenInput) {
		hiddenInput.value = "";
	}
}

function resetVotoSetup(label) {
	const votoSetupSelect = document.getElementById("votoSetupSelect");
	if (!votoSetupSelect) return;

	votoSetupSelect.innerHTML = "";

	const option = document.createElement("option");
	option.value = "";
	option.textContent = label || "";

	votoSetupSelect.appendChild(option);
	votoSetupSelect.disabled = true;
}

/* =========================
   RING CIRCLE MENSILE
========================= */

function updateRingCircleFromMonthlyWinrate() {
	const ring = document.querySelector(".ring-circle");
	if (!ring) return;

	let winrate = null;

	document.querySelectorAll(".metric-head span").forEach(function (head) {
		if (head.textContent.trim() === "Trade win %") {
			const card = head.closest(".metric-card");
			const valueEl = card ? card.querySelector(".metric-main") : null;

			if (valueEl) {
				const value = parseFloat(
					valueEl.textContent
						.replace("%", "")
						.replace(",", ".")
						.trim()
				);

				if (!Number.isNaN(value)) {
					winrate = value;
				}
			}
		}
	});

	if (winrate === null) {
		ring.style.background = "conic-gradient(#e5e7eb 0deg, #e5e7eb 360deg)";
		ring.innerHTML = "<span>--</span>";
		return;
	}

	const safeWinrate = Math.max(0, Math.min(100, winrate));
	const degrees = Math.round((safeWinrate / 100) * 360);
	const color = safeWinrate >= 50 ? CHART_COLORS.win : CHART_COLORS.loss;

	ring.style.background = `conic-gradient(
		${color} 0deg ${degrees}deg,
		#e5e7eb ${degrees}deg 360deg
	)`;

	ring.innerHTML = `<span>${safeWinrate.toFixed(0)}%</span>`;
}

/* =========================
   VIEW SWITCH CALENDARIO
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

function escapeHtml(value) {
	return String(value)
		.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g, "&gt;")
		.replace(/"/g, "&quot;")
		.replace(/'/g, "&#039;");
}