package it.samfrafx.tradingjournal.bl.data.enums;

public enum StrutturaEnum {

	PROSTRUTTURA("Prostruttura"), 
	CONTROSTRUTTURA("Controstruttura"); 

	private final String descrizione;

	StrutturaEnum(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}

}
