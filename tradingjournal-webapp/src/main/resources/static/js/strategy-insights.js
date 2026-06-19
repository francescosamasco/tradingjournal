document.addEventListener("DOMContentLoaded", function () {
	initStrategyInsights();
});

let strategyInsightsData = [];

function initStrategyInsights() {
	loadStrategyInsights();

	document.querySelectorAll("[data-insight-mode]").forEach(function (button) {
		button.addEventListener("click", function () {
			document.querySelectorAll("[data-insight-mode]").forEach(function (btn) {
				btn.classList.remove("active");
			});

			button.classList.add("active");

			renderStrategyInsights(button.dataset.insightMode);
		});
	});
}

function loadStrategyInsights() {
	const tbody = document.getElementById("strategyInsightsBody");

	if (!tbody) return;

	const params = new URLSearchParams({
		accountId: window.accountId || "",
		year: window.calendarYear || "",
		period: window.calendarMonth || ""
	});

	fetch("/api/dashboard/strategy-insights?" + params.toString())
		.then(function (response) {
			if (!response.ok) {
				throw new Error("Errore caricamento strategy insights");
			}

			return response.json();
		})
		.then(function (data) {
			strategyInsightsData = Array.isArray(data) ? data : [];
			renderStrategyInsights("best");
		})
		.catch(function (error) {
			console.error(error);

			tbody.innerHTML = `
				<tr>
					<td colspan="8" class="text-center text-danger py-4">
						Errore caricamento insights
					</td>
				</tr>
			`;
		});
}

function renderStrategyInsights(mode) {
	const tbody = document.getElementById("strategyInsightsBody");

	if (!tbody) return;

	let data = strategyInsightsData.slice();

	if (mode === "best") {
		data.sort(function (a, b) {
			return Number(b.totalProfit || 0) - Number(a.totalProfit || 0);
		});
	}

	if (mode === "worst") {
		data.sort(function (a, b) {
			return Number(a.totalProfit || 0) - Number(b.totalProfit || 0);
		});
	}

	if (mode === "errors") {
		data = data.filter(function (item) {
			return item.errors && item.errors !== "-";
		});

		data.sort(function (a, b) {
			return Number(b.totalTrades || 0) - Number(a.totalTrades || 0);
		});
	}

	data = data.slice(0, 10);

	if (!data.length) {
		tbody.innerHTML = `
			<tr>
				<td colspan="8" class="text-center text-muted py-4">
					Nessun insight disponibile
				</td>
			</tr>
		`;
		return;
	}

	tbody.innerHTML = data.map(function (item) {
		return `
			<tr>
				<td>${escapeHtml(item.setup || "-")}</td>
				<td>${renderBadge(item.tags)}</td>
				<td>${renderBadge(item.errors)}</td>
				<td class="text-end">${item.totalTrades || 0}</td>
				<td class="text-end fw-bold ${Number(item.totalProfit || 0) >= 0 ? "text-success" : "text-danger"}">
					${formatMoney(item.totalProfit)}
				</td>
				<td class="text-end">${formatPercent(item.winRate)}</td>
				<td class="text-end">${formatNumber(item.profitFactor)}</td>
				<td>${renderQuality(item.quality)}</td>
			</tr>
		`;
	}).join("");
}

function renderBadge(value) {
	if (!value || value === "-") return "-";

	return String(value).split(",").map(function (item) {
		return `<span class="tag-badge me-1">${escapeHtml(item.trim())}</span>`;
	}).join("");
}

function renderQuality(value) {
	if (!value) return "-";

	return `<span class="trade-badge">${escapeHtml(value)}</span>`;
}

function formatMoney(value) {
	const number = Number(value || 0);
	const sign = number > 0 ? "+" : "";

	return sign + number.toFixed(2);
}

function formatPercent(value) {
	if (value === null || value === undefined) return "-";
	return Number(value).toFixed(1) + "%";
}

function formatNumber(value) {
	if (value === null || value === undefined) return "-";
	return Number(value).toFixed(2);
}

function escapeHtml(value) {
	return String(value || "")
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;")
		.replaceAll("'", "&#039;");
}