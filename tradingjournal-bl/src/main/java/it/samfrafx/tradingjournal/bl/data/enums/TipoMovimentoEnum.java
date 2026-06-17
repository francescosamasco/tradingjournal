package it.samfrafx.tradingjournal.bl.data.enums;

public enum TipoMovimentoEnum {

	TRADE("Trade", 0), 
	PRELIEVO("Prelievo", -1),
	DEPOSITO("Deposito", 1);


	private final String descrizione;
	private final int numeric;

	TipoMovimentoEnum(String descrizione, int numeric) {
		this.descrizione = descrizione;
		this.numeric = numeric;
	}

	public String getDescrizione() {
		return descrizione;
	}
	
	public int getNumeric() {
		return this.numeric;
	}
	
	public static TipoMovimentoEnum fromDescrizione(String descrizione) {
	    if (descrizione == null || descrizione.trim().isEmpty()) {
	        return null; // oppure lancia eccezione se preferisci
	    }

	    for (TipoMovimentoEnum s : values()) {
	        if (s.getDescrizione().equalsIgnoreCase(descrizione.trim())) {
	            return s;
	        }
	    }
	    return null; // oppure IllegalArgumentException
	}
	
	public static TipoMovimentoEnum fromNumeric(Integer numeric) {
		if (numeric == null) {
			return null;
		}

		for (TipoMovimentoEnum s : values()) {
			if (s.getNumeric() == numeric) {
				return s;
			}
		}
		return null; // oppure throw new IllegalArgumentException(...)
	}

}
