package it.samfrafx.tradingjournal.bl.data.enums;

public enum SetupEnum {

	DIRETTA("Diretta"), 
	CONFERMA("Conferma"),
	MANIPOLAZIONE("Manipolazione"); 

	private final String descrizione;

	SetupEnum(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}

}
