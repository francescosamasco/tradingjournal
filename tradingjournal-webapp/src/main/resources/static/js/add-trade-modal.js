document.addEventListener("DOMContentLoaded", function () {

	initAddTradeTagsSelect();
	initAddTradeConfluenzeSelect();
	initAddTradeDynamicSelects();

	initAddTradeProfitLossControl();
	initAddTradeAccountBalancePreview();

	resetAddTradeVotoSetup();
	initAddTradeSubmit();
	
	initPrelievoMode();

});

let addTagsTomSelect = null;
let addConfluenzeTomSelect = null;

/* =========================
   PREPARE MODALE ADD
========================= */

function initPrelievoMode() {
	const assetSelect = document.querySelector('select[name="asset"]');
	const resultSelect = document.querySelector('select[name="result"]');
	const profitInput = document.querySelector('input[name="profitLoss"]');
	const rrInput = document.querySelector('input[name="rr"]');

	const fieldsToDisable = [
		'select[name="posizione"]',
		'select[name="struttura"]',
		'select[name="setup"]',
		'select[name="confluenze"]',
		'select[name="votoSetup"]',
		'input[name="rr"]'
	];

	if (!assetSelect || !resultSelect || !profitInput) return;

	assetSelect.addEventListener("change", function () {
		debugger;
		const isPrelievo = assetSelect.value === "PRELIEVO";

		if (isPrelievo) {
			resultSelect.value = "LOSS";
			resultSelect.disabled = true;

			profitInput.readOnly = false;

			if (profitInput.value && Number(profitInput.value) > 0) {
				profitInput.value = -Math.abs(Number(profitInput.value));
			}

			if (rrInput) {
				rrInput.value = "";
			}
		} else {
			resultSelect.disabled = false;
		}

		fieldsToDisable.forEach(selector => {
			const field = document.querySelector(selector);
			if (field) {
				field.disabled = isPrelievo;

				if (isPrelievo) {
					field.value = "";
				}
			}
		});

		updateAccountBalancePreview();
	});

	profitInput.addEventListener("input", function () {
		if (assetSelect.value === "PRELIEVO") {
			const value = Number(profitInput.value);

			if (value > 0) {
				profitInput.value = -Math.abs(value);
			}
		}

		updateAccountBalancePreview();
	});
}

function prepareAddTradeModal() {
	const form = document.getElementById("addTradeForm");

	const title = document.querySelector("#addTradeModal .modal-title");
	if (title) {
		title.textContent = "Aggiungi Trade";
	}

	if (form) {
		form.reset();
	}

	if (addTagsTomSelect) {
		addTagsTomSelect.clear(true);
	}

	resetAddTradeDynamicSelects();
}

window.prepareAddTradeModal = prepareAddTradeModal;

/* =========================
   SUBMIT ADD TRADE
========================= */

function initAddTradeSubmit() {
	const form = document.getElementById("addTradeForm");

	if (!form) return;

	form.addEventListener("submit", function (event) {
		event.preventDefault();

		const submitButton = form.querySelector("button[type='submit']");
		const originalText = submitButton ? submitButton.innerHTML : "";

		if (submitButton) {
			submitButton.disabled = true;
			submitButton.innerHTML = "Salvataggio...";
		}

		const formData = new FormData(form);

		fetch("/dashboard/trade/add", {
			method: "POST",
			body: formData
		})
			.then(function (response) {
				if (!response.ok) {
					throw new Error("Errore salvataggio trade");
				}

				return response.json();
			})
			.then(function () {
				const modalElement = document.getElementById("addTradeModal");
				const modal = bootstrap.Modal.getInstance(modalElement);

				if (modal) {
					modal.hide();
				}

				form.reset();

				if (addTagsTomSelect) {
					addTagsTomSelect.clear(true);
				}

				resetAddTradeDynamicSelects();

				window.location.reload();
			})
			.catch(function (error) {
				console.error(error);
				alert("Errore durante il salvataggio del trade");
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

function initAddTradeTagsSelect() {
	const tagsSelect = document.getElementById("tagsSelect");

	if (!tagsSelect || typeof TomSelect === "undefined") return;
	if (tagsSelect.tomselect) return;

	addTagsTomSelect = new TomSelect(tagsSelect, {
		plugins: ["remove_button"],
		create: true,
		persist: false,
		createOnBlur: true,
		placeholder: "Scrivi o seleziona uno o più tags...",
		maxItems: null
	});
}

/* =========================
   CONFLUENZE
========================= */

function initAddTradeConfluenzeSelect() {
	const select = document.getElementById("confluenzeSelect");

	if (!select || typeof TomSelect === "undefined") return;

	if (select.tomselect) {
		addConfluenzeTomSelect = select.tomselect;
		return;
	}

	addConfluenzeTomSelect = new TomSelect(select, {
		plugins: ["remove_button"],
		create: false,
		persist: false,
		placeholder: "Seleziona struttura e setup...",
		maxItems: null,
		valueField: "value",
		labelField: "text",
		searchField: ["text"],
		onChange: function () {
			loadAddTradeVotoSetup();
		}
	});

	setAddTradeConfluenzeDisabled(true);
}

function initAddTradeDynamicSelects() {
	const modal = document.getElementById("addTradeModal");
	const strutturaSelect = document.getElementById("strutturaSelect");
	const setupSelect = document.getElementById("setupSelect");

	if (modal) {
		modal.addEventListener("hidden.bs.modal", function () {
			resetAddTradeDynamicSelects();
		});
	}

	if (!strutturaSelect || !setupSelect) return;

	strutturaSelect.addEventListener("change", function () {
		resetAddTradeConfluenzeSelect("Seleziona struttura e setup...");
		resetAddTradeVotoSetup();

		const struttura = strutturaSelect.value;
		const setup = setupSelect.value;

		if (struttura && setup) {
			loadAddTradeConfluenze(struttura, setup);
		}
	});

	setupSelect.addEventListener("change", function () {
		resetAddTradeConfluenzeSelect("Seleziona struttura e setup...");
		resetAddTradeVotoSetup();

		const struttura = strutturaSelect.value;
		const setup = setupSelect.value;

		if (struttura && setup) {
			loadAddTradeConfluenze(struttura, setup);
		}
	});
}

function loadAddTradeConfluenze(struttura, setup) {
	if (!addConfluenzeTomSelect) return;

	resetAddTradeConfluenzeSelect("Caricamento confluenze...");
	resetAddTradeVotoSetup();

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
			renderAddTradeConfluenzeOptions(items);
		})
		.catch(function (error) {
			console.error("Errore caricamento confluenze:", error);
			resetAddTradeConfluenzeSelect("Errore caricamento confluenze");
			resetAddTradeVotoSetup("Errore");
		});
}

function renderAddTradeConfluenzeOptions(items) {
	if (!addConfluenzeTomSelect) return;

	addConfluenzeTomSelect.clear(true);
	addConfluenzeTomSelect.clearOptions();

	if (!Array.isArray(items) || items.length === 0) {
		addConfluenzeTomSelect.settings.placeholder = "Nessuna confluenza disponibile";
		addConfluenzeTomSelect.inputState();
		setAddTradeConfluenzeDisabled(true);
		return;
	}

	items.forEach(function (item) {
		const value = item.id || item.value || item.codice || "";
		const text = item.descrizione || item.label || item.text || item.nome || value;

		if (!value) return;

		addConfluenzeTomSelect.addOption({
			value: String(value),
			text: String(text)
		});
	});

	addConfluenzeTomSelect.settings.placeholder = "Seleziona confluenze...";
	addConfluenzeTomSelect.refreshOptions(false);
	addConfluenzeTomSelect.inputState();

	setAddTradeConfluenzeDisabled(false);
}

function getAddTradeSelectedConfluenze() {
	if (!addConfluenzeTomSelect) return [];

	const value = addConfluenzeTomSelect.getValue();

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

function resetAddTradeDynamicSelects() {
	resetAddTradeConfluenzeSelect("Seleziona struttura e setup...");
	resetAddTradeVotoSetup();
}

function resetAddTradeConfluenzeSelect(placeholder) {
	if (!addConfluenzeTomSelect) return;

	addConfluenzeTomSelect.clear(true);
	addConfluenzeTomSelect.clearOptions();
	addConfluenzeTomSelect.settings.placeholder = placeholder || "Seleziona struttura e setup...";
	addConfluenzeTomSelect.refreshOptions(false);
	addConfluenzeTomSelect.inputState();

	setAddTradeConfluenzeDisabled(true);
}

function setAddTradeConfluenzeDisabled(disabled) {
	const select = document.getElementById("confluenzeSelect");

	if (addConfluenzeTomSelect) {
		if (disabled) {
			addConfluenzeTomSelect.disable();
		} else {
			addConfluenzeTomSelect.enable();
		}
	}

	if (select) {
		select.disabled = disabled;
	}
}

/* =========================
   VOTO SETUP
========================= */

function loadAddTradeVotoSetup() {
	const strutturaSelect = document.getElementById("strutturaSelect");
	const setupSelect = document.getElementById("setupSelect");
	const votoSetupInput = document.getElementById("votoSetupInput");

	if (!strutturaSelect || !setupSelect || !votoSetupInput) return;

	const struttura = strutturaSelect.value;
	const setup = setupSelect.value;
	const confluenze = getAddTradeSelectedConfluenze();

	resetAddTradeVotoSetup();

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
				resetAddTradeVotoSetup();
				return;
			}

			const voto = data.id || data.value || data.codice || data.voto || "";
			const descrizione = data.descrizione || data.label || data.text || voto || "Non strategia";

			votoSetupInput.value = descrizione;

			applyAddTradeVotoSetupStyle(descrizione);
		})
		.catch(function (error) {
			console.error("Errore voto setup:", error);
			resetAddTradeVotoSetup("Errore");
		});
}

function resetAddTradeVotoSetup(label) {
	const votoSetupInput = document.getElementById("votoSetupInput");

	if (!votoSetupInput) return;

	const value = label || "Non strategia";

	votoSetupInput.value = value;
	applyAddTradeVotoSetupStyle(value);
}

function applyAddTradeVotoSetupStyle(value) {
	const input = document.getElementById("votoSetupInput");

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

function initAddTradeProfitLossControl() {
	const form = document.getElementById("addTradeForm");

	if (!form) return;

	const resultSelect = form.querySelector("[name='esito']");
	const profitLossInput = form.querySelector("[name='profit']");

	if (!resultSelect || !profitLossInput) return;

	function applyProfitLossRules() {
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

	applyProfitLossRules();
}

/* =========================
   ACCOUNT BALANCE PREVIEW
========================= */

function initAddTradeAccountBalancePreview() {
	const form = document.getElementById("addTradeForm");
	const addTradeModal = document.getElementById("addTradeModal");

	if (!form) return;

	const dateTimeInput = form.querySelector("[name='dateOpen']");
	const profitLossInput = form.querySelector("[name='profit']");
	const accountBalanceInput = form.querySelector("[name='accountBalance']");
	const accountIdInput = form.querySelector("[name='accountId']");

	if (!dateTimeInput || !profitLossInput || !accountBalanceInput || !accountIdInput) {
		return;
	}

	let debounceTimer = null;

	function calculateBalance() {
		clearTimeout(debounceTimer);

		debounceTimer = setTimeout(function () {
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
				.then(function (response) {
					if (!response.ok) {
						throw new Error("Errore calcolo account balance");
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

	dateTimeInput.addEventListener("change", calculateBalance);
	dateTimeInput.addEventListener("input", calculateBalance);
	profitLossInput.addEventListener("input", calculateBalance);
	profitLossInput.addEventListener("change", calculateBalance);

	if (addTradeModal) {
		addTradeModal.addEventListener("shown.bs.modal", calculateBalance);
	}
}