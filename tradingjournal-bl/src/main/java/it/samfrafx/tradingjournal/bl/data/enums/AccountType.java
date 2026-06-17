package it.samfrafx.tradingjournal.bl.data.enums;

public enum AccountType {

	CFD(0, "CFD"), 
	FUTURES(1, "Futures");

	private final int numeric;
	private final String description;

	AccountType( int numeric, String description) {
		this.numeric = numeric;
		this.description = description;
	}

	public int getNumeric() {
		return this.numeric;
	}
	
	public String getDescription() {
		return this.description;
	}
	

	public static AccountType fromNumeric(Integer numeric) {
		if (numeric == null) {
			return null;
		}

		for (AccountType s : values()) {
			if (s.getNumeric() == numeric) {
				return s;
			}
		}
		return null; // oppure throw new IllegalArgumentException(...)
	}
	
}
