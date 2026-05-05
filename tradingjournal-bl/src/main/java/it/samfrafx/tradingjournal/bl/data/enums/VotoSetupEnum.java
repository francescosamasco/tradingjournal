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
	
	public static VotoSetupEnum fromDescrizione(String descrizione) {
	    if (descrizione == null || descrizione.trim().isEmpty()) {
	        return null; // oppure lancia eccezione se preferisci
	    }

	    for (VotoSetupEnum s : values()) {
	        if (s.getDescrizione().equalsIgnoreCase(descrizione.trim())) {
	            return s;
	        }
	    }
	    return null; // oppure IllegalArgumentException
	}

}
