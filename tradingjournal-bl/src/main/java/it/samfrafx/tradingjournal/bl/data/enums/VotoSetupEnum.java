package it.samfrafx.tradingjournal.bl.data.enums;

public enum VotoSetupEnum {

	BASSO("Basso", 1), 
	MEDIO("Medio", 2), 
	ALTO("Alto", 3), 
	NON_STRATEGIA("Non strategia", 0);

	private final String descrizione;
	private final int numeric;

	VotoSetupEnum(String descrizione, int numeric) {
		this.descrizione = descrizione;
		this.numeric = numeric;
	}

	public String getDescrizione() {
		return descrizione;
	}
	
	public int getNumeric() {
		return numeric;
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
	
	public static VotoSetupEnum fromNumeric(int numeric) {

	    for (VotoSetupEnum s : values()) {
	        if (s.getNumeric() == numeric) {
	            return s;
	        }
	    }
	    return null; // oppure IllegalArgumentException
	}

}
