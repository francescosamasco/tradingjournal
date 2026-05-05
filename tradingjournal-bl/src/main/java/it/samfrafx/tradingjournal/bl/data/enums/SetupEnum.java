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
	
	public static SetupEnum fromDescrizione(String descrizione) {
	    if (descrizione == null || descrizione.trim().isEmpty()) {
	        return null; // oppure lancia eccezione se preferisci
	    }

	    for (SetupEnum s : values()) {
	        if (s.getDescrizione().equalsIgnoreCase(descrizione.trim())) {
	            return s;
	        }
	    }
	    return null; // oppure IllegalArgumentException
	}

}
