document.addEventListener("DOMContentLoaded", function () {

	initEditTradeTagsSelect();
	initEditTradeConfluenzeSelect();
	initEditTradeDynamicSelects();

	initEditTradeProfitLossControl();
	initEditTradeAccountBalancePreview();

	initEditTradeButtons();
	initEditTradeSubmit();

});

/* =========================
   STATE
========================= */

let editTagsTomSelect = null;
let editConfluenzeTomSelect = null;
let currentEditTradeId = null;
let editBalanceDebounceTimer = null;

/* =========================
   OPEN EDIT MODAL
========================= */

function initEditTradeButtons() {
	document.querySelectorAll(".edit-trade-btn").forEach(function (btn) {
		btn.addEventListener("click", function () {
			const tradeId = btn.dataset.tradeId;

			if (!tradeId) return;

			loadTradeForEdit(tradeId);
		});
	});
}

function loadTradeForEdit(tradeId) {
	fetch("/dashboard/trade/" + encodeURIComponent(tradeId))
		.then(function (response) {
			if (!response.ok) {
				throw new Error("Errore caricamento trade");
			}

			return response.json();
		})
		.then(function (trade) {
			openEditTradeModal(trade);
		})
		.catch(function (error) {
			console.error(error);
			alert("Errore durante il caricamento del trade");
		});
}

function openEditTradeModal(trade) {
	const modalEl = document.getElementById("editTradeModal");
	const form = document.getElementById("editTradeForm");

	if (!modalEl || !form || !trade) return;

	currentEditTradeId = trade.idTrade || null;

	setEditInputValue("editTradeIdInput", trade.idTrade);

	setEditFieldValue(form, "dateOpen", formatEditDateTimeLocal(trade.dateOpen));
	setEditFieldValue(form, "asset", trade.asset);
	setEditFieldValue(form, "esito", trade.esito);
	setEditFieldValue(form, "profit", trade.profit);
	setEditFieldValue(form, "accountBalance", trade.currentBalance || trade.accountBalance);
	setEditFieldValue(form, "posizione", trade.posizione);
	setEditFieldValue(form, "struttura", trade.struttura);
	setEditFieldValue(form, "setup", trade.setup);
	setEditFieldValue(form, "risk", trade.risk);
	setEditFieldValue(form, "note", trade.note);
	setEditFieldValue(form, "tradeAnalysis", trade.tradeAnalysis);

	resetEditTradeVotoSetup(trade.votoSetup || "Non strategia");

	if (editTagsTomSelect) {
		editTagsTomSelect.clear(true);

		splitCsvValue(trade.tags).forEach(function (tag) {
			editTagsTomSelect.addItem(tag, true);
		});
	}

	resetEditTradeConfluenzeSelect("Caricamento confluenze...");

	if (trade.struttura && trade.setup) {
		loadEditTradeConfluenze(
			trade.struttura,
			trade.setup,
			splitCsvValue(trade.confluenze)
		);
	} else {
		resetEditTradeConfluenzeSelect("Seleziona struttura e setup...");
	}

	applyEditTradeProfitLossRules();

	const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
	modal.show();
}

/* =========================
   SUBMIT EDIT
========================= */

function initEditTradeSubmit() {
	const form = document.getElementById("editTradeForm");

	if (!form) return;

	form.addEventListener("submit", function (event) {
		event.preventDefault();

		const tradeId =
			currentEditTradeId ||
			document.getElementById("editTradeIdInput")?.value;

		if (!tradeId) {
			alert("Trade non valido");
			return;
		}

		const submitButton = form.querySelector("button[type='submit']");
		const originalText = submitButton ? submitButton.innerHTML : "";

		if (submitButton) {
			submitButton.disabled = true;
			submitButton.innerHTML = "Aggiornamento...";
		}

		const formData = new FormData(form);

		fetch("/dashboard/trade/update/" + encodeURIComponent(tradeId), {
			method: "PUT",
			body: formData
		})
			.then(function (response) {
				if (!response.ok) {
					throw new Error("Errore aggiornamento trade");
				}

				return response.json();
			})
			.then(function () {
				const modalElement = document.getElementById("editTradeModal");
				const modal = bootstrap.Modal.getInstance(modalElement);

				if (modal) {
					modal.hide();
				}

				window.location.reload();
			})
			.catch(function (error) {
				console.error(error);
				alert("Errore durante l'aggiornamento del trade");
			})
			.finally(function () {
				if (submitButton) {
					submitButton.disabled = false;
					submitButton.innerHTML = originalText;
				}
			});
	});
}

/* =========================
   TAGS
========================= */

function initEditTradeTagsSelect() {
	const tagsSelect = document.getElementById("editTagsSelect");

	if (!tagsSelect || typeof TomSelect === "undefined") return;

	if (tagsSelect.tomselect) {
		editTagsTomSelect = tagsSelect.tomselect;
		return;
	}

	editTagsTomSelect = new TomSelect(tagsSelect, {
		plugins: ["remove_button"],
		create: false,
		persist: false,
		placeholder: "Seleziona uno o più tags...",
		maxItems: null
	});
}

/* =========================
   CONFLUENZE
========================= */

function initEditTradeConfluenzeSelect() {
	const select = document.getElementById("editConfluenzeSelect");

	if (!select || typeof TomSelect === "undefined") return;

	if (select.tomselect) {
		editConfluenzeTomSelect = select.tomselect;
		return;
	}

	editConfluenzeTomSelect = new TomSelect(select, {
		plugins: ["remove_button"],
		create: false,
		persist: false,
		placeholder: "Seleziona struttura e setup...",
		maxItems: null,
		valueField: "value",
		labelField: "text",
		searchField: ["text"],
		onChange: function () {
			loadEditTradeVotoSetup();
		}
	});

	setEditTradeConfluenzeDisabled(true);
}

function initEditTradeDynamicSelects() {
	const modal = document.getElementById("editTradeModal");
	const strutturaSelect = document.getElementById("editStrutturaSelect");
	const setupSelect = document.getElementById("editSetupSelect");

	if (modal) {
		modal.addEventListener("hidden.bs.modal", function () {
			resetEditTradeDynamicSelects();
			currentEditTradeId = null;
		});
	}

	if (!strutturaSelect || !setupSelect) return;

	strutturaSelect.addEventListener("change", function () {
		resetEditTradeConfluenzeSelect("Seleziona struttura e setup...");
		resetEditTradeVotoSetup();

		const struttura = strutturaSelect.value;
		const setup = setupSelect.value;

		if (struttura && setup) {
			loadEditTradeConfluenze(struttura, setup);
		}
	});

	setupSelect.addEventListener("change", function () {
		resetEditTradeConfluenzeSelect("Seleziona struttura e setup...");
		resetEditTradeVotoSetup();

		const struttura = strutturaSelect.value;
		const setup = setupSelect.value;

		if (struttura && setup) {
			loadEditTradeConfluenze(struttura, setup);
		}
	});
}

function loadEditTradeConfluenze(struttura, setup, selectedValues) {
	if (!editConfluenzeTomSelect) return;

	resetEditTradeConfluenzeSelect("Caricamento confluenze...");
	resetEditTradeVotoSetup();

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
			renderEditTradeConfluenzeOptions(items);

			if (Array.isArray(selectedValues) && selectedValues.length) {
				selectedValues.forEach(function (value) {
					editConfluenzeTomSelect.addItem(value, true);
				});

				loadEditTradeVotoSetup();
			}
		})
		.catch(function (error) {
			console.error("Errore caricamento confluenze:", error);
			resetEditTradeConfluenzeSelect("Errore caricamento confluenze");
			resetEditTradeVotoSetup("Errore");
		});
}

function renderEditTradeConfluenzeOptions(items) {
	if (!editConfluenzeTomSelect) return;

	editConfluenzeTomSelect.clear(true);
	editConfluenzeTomSelect.clearOptions();

	if (!Array.isArray(items) || items.length === 0) {
		editConfluenzeTomSelect.settings.placeholder = "Nessuna confluenza disponibile";
		editConfluenzeTomSelect.inputState();
		setEditTradeConfluenzeDisabled(true);
		return;
	}

	items.forEach(function (item) {
		const value = item.id || item.value || item.codice || "";
		const text = item.descrizione || item.label || item.text || item.nome || value;

		if (!value) return;

		editConfluenzeTomSelect.addOption({
			value: String(value),
			text: String(text)
		});
	});

	editConfluenzeTomSelect.settings.placeholder = "Seleziona confluenze...";
	editConfluenzeTomSelect.refreshOptions(false);
	editConfluenzeTomSelect.inputState();

	setEditTradeConfluenzeDisabled(false);
}

function getEditTradeSelectedConfluenze() {
	if (!editConfluenzeTomSelect) return [];

	const value = editConfluenzeTomSelect.getValue();

	if (Array.isArray(value)) {
		return value.filter(function (item) {
			return item !== "";
		});
	}

	if (!value) return [];

	return String(value).split(",").filter(function (item) {
		return item !== "";
	});
}

function resetEditTradeDynamicSelects() {
	resetEditTradeConfluenzeSelect("Seleziona struttura e setup...");
	resetEditTradeVotoSetup();

	if (editTagsTomSelect) {
		editTagsTomSelect.clear(true);
	}

	const form = document.getElementById("editTradeForm");

	if (form) {
		form.reset();
	}
}

function resetEditTradeConfluenzeSelect(placeholder) {
	if (!editConfluenzeTomSelect) return;

	editConfluenzeTomSelect.clear(true);
	editConfluenzeTomSelect.clearOptions();
	editConfluenzeTomSelect.settings.placeholder = placeholder || "Seleziona struttura e setup...";
	editConfluenzeTomSelect.refreshOptions(false);
	editConfluenzeTomSelect.inputState();

	setEditTradeConfluenzeDisabled(true);
}

function setEditTradeConfluenzeDisabled(disabled) {
	const select = document.getElementById("editConfluenzeSelect");

	if (editConfluenzeTomSelect) {
		if (disabled) {
			editConfluenzeTomSelect.disable();
		} else {
			editConfluenzeTomSelect.enable();
		}
	}

	if (select) {
		select.disabled = disabled;
	}
}

/* =========================
   VOTO SETUP
========================= */

function loadEditTradeVotoSetup() {
	const strutturaSelect = document.getElementById("editStrutturaSelect");
	const setupSelect = document.getElementById("editSetupSelect");
	const votoSetupInput = document.getElementById("editVotoSetupInput");

	if (!strutturaSelect || !setupSelect || !votoSetupInput) return;

	const struttura = strutturaSelect.value;
	const setup = setupSelect.value;
	const confluenze = getEditTradeSelectedConfluenze();

	resetEditTradeVotoSetup();

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
			if (!data) {
				resetEditTradeVotoSetup();
				return;
			}

			const voto = data.id || data.value || data.codice || data.voto || "";
			const descrizione = data.descrizione || data.label || data.text || voto || "Non strategia";

			votoSetupInput.value = descrizione;

			applyEditTradeVotoSetupStyle(descrizione);
		})
		.catch(function (error) {
			console.error("Errore voto setup:", error);
			resetEditTradeVotoSetup("Errore");
		});
}

function resetEditTradeVotoSetup(label) {
	const votoSetupInput = document.getElementById("editVotoSetupInput");

	if (!votoSetupInput) return;

	const value = label || "Non strategia";

	votoSetupInput.value = value;
	applyEditTradeVotoSetupStyle(value);
}

function applyEditTradeVotoSetupStyle(value) {
	const input = document.getElementById("editVotoSetupInput");

	if (!input) return;

	input.classList.remove(
		"voto-setup-alto",
		"voto-setup-medio",
		"voto-setup-none"
	);

	const normalized = String(value || "").toLowerCase();

	if (normalized.includes("alto")) {
		input.classList.add("voto-setup-alto");
		return;
	}

	if (normalized.includes("medio")) {
		input.classList.add("voto-setup-medio");
		return;
	}

	input.classList.add("voto-setup-none");
}

/* =========================
   PROFIT / LOSS
========================= */

function initEditTradeProfitLossControl() {
	const form = document.getElementById("editTradeForm");

	if (!form) return;

	const resultSelect = form.querySelector("[name='esito']");
	const profitLossInput = form.querySelector("[name='profit']");

	if (!resultSelect || !profitLossInput) return;

	resultSelect.addEventListener("change", applyEditTradeProfitLossRules);

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

	applyEditTradeProfitLossRules();
}

function applyEditTradeProfitLossRules() {
	const form = document.getElementById("editTradeForm");

	if (!form) return;

	const resultSelect = form.querySelector("[name='esito']");
	const profitLossInput = form.querySelector("[name='profit']");

	if (!resultSelect || !profitLossInput) return;

	const result = resultSelect.value;

	profitLossInput.readOnly = false;
	profitLossInput.disabled = false;
	profitLossInput.removeAttribute("min");
	profitLossInput.removeAttribute("max");

	if (!result) {
		profitLossInput.value = "";
		profitLossInput.disabled = true;
		profitLossInput.placeholder = "Seleziona prima il risultato";
		return;
	}

	if (result === "WIN") {
		profitLossInput.min = "0";
		profitLossInput.placeholder = "Valore positivo";

		if (profitLossInput.value && Number(profitLossInput.value) < 0) {
			profitLossInput.value = "";
		}
	} else if (result === "LOSS") {
		profitLossInput.max = "0";
		profitLossInput.placeholder = "Valore negativo";

		if (profitLossInput.value && Number(profitLossInput.value) > 0) {
			profitLossInput.value = "";
		}
	} else if (result === "BE" || result === "MISS") {
		profitLossInput.value = "0";
		profitLossInput.readOnly = true;
		profitLossInput.min = "0";
		profitLossInput.max = "0";
		profitLossInput.placeholder = "0";
	}

	profitLossInput.dispatchEvent(new Event("input"));
}

/* =========================
   ACCOUNT BALANCE PREVIEW EDIT
========================= */

function initEditTradeAccountBalancePreview() {
	const form = document.getElementById("editTradeForm");

	if (!form) return;

	const dateTimeInput = form.querySelector("[name='dateOpen']");
	const profitLossInput = form.querySelector("[name='profit']");

	if (!dateTimeInput || !profitLossInput) return;

	dateTimeInput.addEventListener("change", calculateEditTradeAccountBalance);
	dateTimeInput.addEventListener("input", calculateEditTradeAccountBalance);
	profitLossInput.addEventListener("input", calculateEditTradeAccountBalance);
	profitLossInput.addEventListener("change", calculateEditTradeAccountBalance);
}

function calculateEditTradeAccountBalance() {
	const form = document.getElementById("editTradeForm");

	if (!form) return;

	const dateTimeInput = form.querySelector("[name='dateOpen']");
	const profitLossInput = form.querySelector("[name='profit']");
	const accountBalanceInput = form.querySelector("[name='accountBalance']");
	const accountIdInput = form.querySelector("[name='accountId']");

	if (!dateTimeInput || !profitLossInput || !accountBalanceInput || !accountIdInput) {
		return;
	}

	clearTimeout(editBalanceDebounceTimer);

	editBalanceDebounceTimer = setTimeout(function () {
		const dateTime = dateTimeInput.value;
		const profitLoss = profitLossInput.value || "0";
		const accountId = accountIdInput.value;
		const idTrade =
			currentEditTradeId ||
			document.getElementById("editTradeIdInput")?.value ||
			"";

		if (!dateTime || !accountId || !idTrade) {
			accountBalanceInput.value = "";
			return;
		}

		const params = new URLSearchParams({
			accountId: accountId,
			idTrade: idTrade,
			dateTime: dateTime,
			profitLoss: profitLoss
		});

		fetch(`/api/dashboard/account-balance-preview-edit?${params.toString()}`)
			.then(function (response) {
				if (!response.ok) {
					throw new Error("Errore calcolo account balance edit");
				}

				return response.json();
			})
			.then(function (balance) {
				accountBalanceInput.value = Number(balance).toFixed(2);
			})
			.catch(function (error) {
				console.error(error);
				accountBalanceInput.value = "";
			});
	}, 250);
}

/* =========================
   UTILS
========================= */

function setEditFieldValue(form, name, value) {
	const field = form.querySelector("[name='" + name + "']");

	if (!field) return;

	field.value = value !== undefined && value !== null ? value : "";
}

function setEditInputValue(id, value) {
	const input = document.getElementById(id);

	if (!input) return;

	input.value = value !== undefined && value !== null ? value : "";
}

function formatEditDateTimeLocal(value) {
	if (!value) return "";

	return String(value).substring(0, 16);
}

function splitCsvValue(value) {
	if (!value) return [];

	if (Array.isArray(value)) {
		return value
			.map(function (item) {
				return String(item).trim();
			})
			.filter(Boolean);
	}

	return String(value)
		.split(",")
		.map(function (item) {
			return item.trim();
		})
		.filter(Boolean);
}