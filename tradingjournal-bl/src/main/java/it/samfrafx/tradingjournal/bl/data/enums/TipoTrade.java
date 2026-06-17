package it.samfrafx.tradingjournal.bl.data.enums;

public enum TipoTrade {

	ACCOUNT(0), 
	SIMULATION(1);

	private final int numeric;

	TipoTrade( int numeric) {
		this.numeric = numeric;
	}

	public int getNumeric() {
		return this.numeric;
	}
	

	public static TipoTrade fromNumeric(Integer numeric) {
		if (numeric == null) {
			return null;
		}

		for (TipoTrade s : values()) {
			if (s.getNumeric() == numeric) {
				return s;
			}
		}
		return null; // oppure throw new IllegalArgumentException(...)
	}

}
