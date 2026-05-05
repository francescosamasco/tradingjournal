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
	
	public static EsitoEnum fromDescrizione(String descrizione) {
	    if (descrizione == null || descrizione.trim().isEmpty()) {
	        return null; // oppure lancia eccezione se preferisci
	    }

	    for (EsitoEnum s : values()) {
	        if (s.getDescrizione().equalsIgnoreCase(descrizione.trim())) {
	            return s;
	        }
	    }
	    return null; // oppure IllegalArgumentException
	}

}
