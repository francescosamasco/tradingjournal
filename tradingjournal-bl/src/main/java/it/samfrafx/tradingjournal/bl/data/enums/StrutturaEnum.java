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

	public static StrutturaEnum fromDescrizione(String descrizione) {
	    if (descrizione == null || descrizione.trim().isEmpty()) {
	        return null; // oppure lancia eccezione se preferisci
	    }

	    for (StrutturaEnum s : values()) {
	        if (s.getDescrizione().equalsIgnoreCase(descrizione.trim())) {
	            return s;
	        }
	    }
	    return null; // oppure IllegalArgumentException
	}
	
}
