package it.samfrafx.tradingjournal.bl.data.enums;

public enum EsitoEnum {

	WIN("Win"), 
	LOSS("Loss"), 
	BE("Be"), 
	MISS("Miss");

	private final String descrizione;

	EsitoEnum(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}

}
