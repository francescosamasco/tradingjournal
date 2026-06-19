document.addEventListener("DOMContentLoaded", function () {
	initStrategyInsights();
});

let currentInsightType = "SETUP";

function initStrategyInsights() {
	bindStrategyInsightButtons();
	loadStrategyInsights(currentInsightType);
}

function bindStrategyInsightButtons() {
	document.querySelectorAll("[data-insight-type]").forEach(function (button) {
		button.addEventListener("click", function () {

			document.querySelectorAll("[data-insight-type]").forEach(function (btn) {
				btn.classList.remove("active");
			});

			button.classList.add("active");

			currentInsightType = button.dataset.insightType || "SETUP";
			loadStrategyInsights(currentInsightType);
		});
	});
}

function loadStrategyInsights(type) {
	const tbody = document.getElementById("strategyInsightsBody");

	if (!tbody) return;

	tbody.innerHTML = `
		<tr>
			<td colspan="8" class="text-center text-muted py-4">
				Caricamento insights...
			</td>
		</tr>
	`;

	const params = new URLSearchParams({
		accountId: window.accountId || "",
		year: window.calendarYear || "",
		period: window.calendarMonth || "",
		type: type || "SETUP"
	});

	fetch("/api/dashboard/strategy-insights?" + params.toString())
		.then(function (response) {
			if (!response.ok) {
				throw new Error("Errore caricamento strategy insights");
			}

			return response.json();
		})
		.then(function (data) {
			renderStrategyInsights(data);
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

function renderStrategyInsights(data) {
	const tbody = document.getElementById("strategyInsightsBody");

	if (!tbody) return;

	data = Array.isArray(data) ? data.slice() : [];

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
				<td class="text-end">${formatPercent(item.lossRate)}</td>
				<td class="text-end">${formatNumber(item.profitFactor)}</td>
				<td>${renderQuality(item.quality)}</td>
			</tr>
		`;
	}).join("");
}

function renderBadge(value) {
	if (!value || value === "-") return "-";

	return String(value)
		.split(",")
		.map(function (item) {
			const cleanItem = item.trim();

			if (!cleanItem) return "";

			return `<span class="tag-badge me-1">${escapeHtml(cleanItem)}</span>`;
		})
		.join("");
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
	if (value === null || value === undefined || value === "") return "-";

	return Number(value).toFixed(1) + "%";
}

function formatNumber(value) {
	if (value === null || value === undefined || value === "") return "-";

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