package it.samfrafx.tradingjournal.bl.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import it.samfrafx.tradingjournal.bl.data.StrategyInsightData;
import it.samfrafx.tradingjournal.datamodel.data.Trade;
import it.samfrafx.tradingjournal.datamodel.repository.TradeRepository;

@Service
public class StrategyInsightService {

	private final TradeRepository tradeRepository;

	public StrategyInsightService(TradeRepository tradeRepository) {
		this.tradeRepository = tradeRepository;
	}

	public List<StrategyInsightData> getStrategyInsights(String accountId, Integer year, String period, String type) {
		LocalDateTime[] range = resolvePeriodRange(year, period);

		List<Trade> trades = tradeRepository.findByAccountAndPeriodAndTipoTrade(
				accountId,
				range[0],
				range[1],
				0
		);

		Map<String, List<Trade>> groupedTrades = trades.stream()
				.flatMap(trade -> buildInsightKeys(trade, type).stream()
						.map(key -> new java.util.AbstractMap.SimpleEntry<>(key, trade)))
				.collect(Collectors.groupingBy(
						java.util.Map.Entry::getKey,
						Collectors.mapping(java.util.Map.Entry::getValue, Collectors.toList())
				));
		return groupedTrades.entrySet()
				.stream()
				.map(entry -> buildInsightData(entry.getKey(), entry.getValue()))
				.sorted(Comparator.comparing(StrategyInsightData::getTotalProfit).reversed())
				.collect(Collectors.toList());
	}

	private LocalDateTime[] resolvePeriodRange(Integer year, String period) {

		if (year == null) {
			throw new IllegalArgumentException("Anno obbligatorio");
		}

		if (period == null || period.isBlank()) {
			throw new IllegalArgumentException("Periodo obbligatorio");
		}

		String normalizedPeriod = period.trim().toUpperCase();

		LocalDate startDate;
		LocalDate endDate;

		switch (normalizedPeriod) {

			case "YEAR":
			case "ALL":
				startDate = LocalDate.of(year, 1, 1);
				endDate = LocalDate.of(year, 12, 31);
				break;

			case "Q1":
				startDate = LocalDate.of(year, 1, 1);
				endDate = LocalDate.of(year, 3, 31);
				break;

			case "Q2":
				startDate = LocalDate.of(year, 4, 1);
				endDate = LocalDate.of(year, 6, 30);
				break;

			case "Q3":
				startDate = LocalDate.of(year, 7, 1);
				endDate = LocalDate.of(year, 9, 30);
				break;

			case "Q4":
				startDate = LocalDate.of(year, 10, 1);
				endDate = LocalDate.of(year, 12, 31);
				break;

			default:
				int month = Integer.parseInt(normalizedPeriod);

				startDate = LocalDate.of(year, month, 1);
				endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
				break;
		}

		return new LocalDateTime[] {
				startDate.atStartOfDay(),
				endDate.atTime(LocalTime.MAX)
		};
	}

	private List<String> buildInsightKeys(Trade trade, String type) {

		String setup = normalizeSingleValue(trade.getSetup());
		List<String> tags = splitCsv(trade.getTags());
		List<String> errors = splitCsv(trade.getErrors());

		String insightType = type != null ? type.trim().toUpperCase() : "SETUP";

		switch (insightType) {

			case "SETUP_TAG":
				if (tags.isEmpty()) {
					return List.of(setup + "|-|-" );
				}

				return tags.stream()
						.map(tag -> setup + "|" + tag + "|-")
						.collect(Collectors.toList());

			case "SETUP_ERROR":
				if (errors.isEmpty()) {
					return List.of(setup + "|-|-");
				}

				return errors.stream()
						.map(error -> setup + "|-|" + error)
						.collect(Collectors.toList());

			case "COMBO":
				return List.of(
						setup + "|" +
						normalizeCsvValue(trade.getTags()) + "|" +
						normalizeCsvValue(trade.getErrors())
				);

			case "SETUP":
			default:
				return List.of(setup + "|-|-");
		}
	}
	
	
	private List<String> splitCsv(String value) {

		if (value == null || value.isBlank()) {
			return List.of();
		}

		return Arrays.stream(value.split(","))
				.map(String::trim)
				.filter(s -> !s.isBlank())
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());
	}

	private StrategyInsightData buildInsightData(String key, List<Trade> trades) {

		String[] parts = key.split("\\|", -1);

		String setup = parts.length > 0 ? parts[0] : "";
		String tags = parts.length > 1 ? parts[1] : "";
		String errors = parts.length > 2 ? parts[2] : "";

		long totalTrades = trades.size();

		long wins = trades.stream()
				.filter(t -> "WIN".equalsIgnoreCase(t.getEsito()))
				.count();

		long losses = trades.stream()
				.filter(t -> "LOSS".equalsIgnoreCase(t.getEsito()))
				.count();

		long breakEven = trades.stream()
				.filter(t -> "BE".equalsIgnoreCase(t.getEsito()))
				.count();

		long miss = trades.stream()
				.filter(t -> "MISS".equalsIgnoreCase(t.getEsito()))
				.count();

		BigDecimal totalProfit = trades.stream()
				.map(Trade::getProfit)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal averageProfit = totalTrades > 0
				? totalProfit.divide(BigDecimal.valueOf(totalTrades), 2, RoundingMode.HALF_UP)
				: BigDecimal.ZERO;

		BigDecimal totalWin = trades.stream()
				.map(Trade::getProfit)
				.filter(Objects::nonNull)
				.filter(p -> p.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal totalLoss = trades.stream()
				.map(Trade::getProfit)
				.filter(Objects::nonNull)
				.filter(p -> p.compareTo(BigDecimal.ZERO) < 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.abs()
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal winRate = calculateRate(wins, totalTrades);
		BigDecimal lossRate = calculateRate(losses, totalTrades);
		BigDecimal profitFactor = calculateProfitFactor(totalWin, totalLoss);

		return StrategyInsightData.builder()
				.setup(emptyToDash(setup))
				.tags(emptyToDash(tags))
				.errors(emptyToDash(errors))
				.totalTrades(totalTrades)
				.wins(wins)
				.losses(losses)
				.breakEven(breakEven)
				.miss(miss)
				.totalProfit(totalProfit)
				.averageProfit(averageProfit)
				.totalWin(totalWin)
				.totalLoss(totalLoss)
				.winRate(winRate)
				.lossRate(lossRate)
				.profitFactor(profitFactor)
				.quality(calculateQuality(totalTrades, totalProfit, winRate, profitFactor, errors))
				.build();
	}

	private String normalizeSingleValue(String value) {

		if (value == null || value.isBlank()) {
			return "";
		}

		return value.trim();
	}

	private String normalizeCsvValue(String value) {

		if (value == null || value.isBlank()) {
			return "";
		}

		return Arrays.stream(value.split(","))
				.map(String::trim)
				.filter(s -> !s.isBlank())
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.joining(", "));
	}

	private String emptyToDash(String value) {

		if (value == null || value.isBlank()) {
			return "-";
		}

		return value;
	}

	private BigDecimal calculateRate(long value, long total) {

		if (total == 0) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(value)
				.multiply(BigDecimal.valueOf(100))
				.divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateProfitFactor(BigDecimal totalWin, BigDecimal totalLoss) {

		if (totalLoss.compareTo(BigDecimal.ZERO) == 0) {
			return totalWin.compareTo(BigDecimal.ZERO) > 0
					? BigDecimal.valueOf(999)
					: BigDecimal.ZERO;
		}

		return totalWin.divide(totalLoss, 2, RoundingMode.HALF_UP);
	}

	private String calculateQuality(
			long totalTrades,
			BigDecimal totalProfit,
			BigDecimal winRate,
			BigDecimal profitFactor,
			String errors) {

		if (totalTrades < 3) {
			return "Pochi dati";
		}

		boolean hasErrors = errors != null && !errors.isBlank();

		if (totalProfit.compareTo(BigDecimal.ZERO) > 0
				&& winRate.compareTo(BigDecimal.valueOf(60)) >= 0
				&& profitFactor.compareTo(BigDecimal.valueOf(1.5)) >= 0
				&& !hasErrors) {
			return "Ottima";
		}

		if (totalProfit.compareTo(BigDecimal.ZERO) > 0
				&& profitFactor.compareTo(BigDecimal.ONE) > 0) {
			return "Buona";
		}

		if (totalProfit.compareTo(BigDecimal.ZERO) < 0 || hasErrors) {
			return "Critica";
		}

		return "Neutra";
	}
}