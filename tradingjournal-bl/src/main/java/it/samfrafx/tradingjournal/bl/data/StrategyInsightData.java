package it.samfrafx.tradingjournal.bl.data;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyInsightData {

	private String setup;
	private String tags;
	private String errors;

	private Long totalTrades;

	private Long wins;
	private Long losses;
	private Long breakEven;
	private Long miss;

	private BigDecimal totalProfit;
	private BigDecimal averageProfit;

	private BigDecimal totalWin;
	private BigDecimal totalLoss;

	private BigDecimal winRate;
	private BigDecimal lossRate;

	private BigDecimal profitFactor;

	private String quality;
}