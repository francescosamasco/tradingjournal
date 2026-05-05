package it.samfrafx.tradingjournal.bl.data.enums;

public enum VotoSetupEnum {

	BASSO("Basso"), 
	MEDIO("Medio"), 
	ALTO("Alto"), 
	NON_STRATEGIA("Non strategia");

	private final String descrizione;

	VotoSetupEnum(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}

}
