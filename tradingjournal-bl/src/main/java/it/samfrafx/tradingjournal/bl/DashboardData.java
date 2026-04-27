package it.samfrafx.tradingjournal.bl;

import java.math.BigDecimal;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class DashboardData {

	private String accountId;
	private PeriodEnum periodEnum;
	private BigDecimal bilancioIniziale;
	private BigDecimal bilancioFinale;
	private BigDecimal profitPercent;
	private BigDecimal winrate;
	private BigDecimal rrAverage;
	private BigDecimal profitFactor;

	//private LinkedList<PerformanceData> performances;	
	
	
}
