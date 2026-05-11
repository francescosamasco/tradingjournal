package it.samfrafx.tradingjournal.bl.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.PerformanceData;
import it.samfrafx.tradingjournal.bl.util.DateUtils;
import it.samfrafx.tradingjournal.datamodel.data.Performance;
import it.samfrafx.tradingjournal.datamodel.data.Trade;
import it.samfrafx.tradingjournal.datamodel.repository.PerformanceRepository;
import it.samfrafx.tradingjournal.datamodel.repository.TradeRepository;

@Service
public class PerformanceService {


	private final PerformanceRepository performanceRepository;
	private final TradeRepository tradeRepository;

	public PerformanceService(
	        PerformanceRepository performanceRepository,
	        TradeRepository tradeRepository
	) {
	    this.performanceRepository = performanceRepository;
	    this.tradeRepository = tradeRepository;
	}

	public PerformanceData findByAccountIdAndWeek(String accountId, LocalDate date) {

		int year = date.getYear();
		int month = date.getMonthValue();

		WeekFields weekFields = WeekFields.ISO;
		int week = date.get(weekFields.weekOfWeekBasedYear());

		String idPerformance = year + "-" + month + "-" + week;

		return performanceRepository
				.findByAccountAndIdPerformance(accountId, idPerformance)
				.map(PerformanceData::from)
				.orElseThrow(() -> new IllegalStateException(
						"Performance settimanale non trovata per account " + accountId +
						" e performance " + idPerformance
						));
	}
	
	public PerformanceData findClosestPreviousPerformance(
	        String accountId,
	        LocalDate date
	) {
	    int year = date.getYear();
	    int month = date.getMonthValue();
	    int week = date.get(WeekFields.ISO.weekOfWeekBasedYear());

	    String idPerformance = year + "-" + month + "-" + week;

	    return performanceRepository
	            .findPreviousPerformances(accountId, idPerformance)
	            .stream()
	            .findFirst()
	            .map(PerformanceData::from)
	            .orElse(null);
	}
	

	// =========================
	// 🔹 PERFORMANCE MENSILE (lista settimane)
	// =========================
	public List<PerformanceData> getMonthlyPerformance(
			String idAccount,
			int year,
			int month
			) {

		String pattern = year + "-" + month + "-%";

		return performanceRepository
				.findByAccountAndMonth(idAccount, pattern)
				.stream()
				.map(PerformanceData::from)
				.toList();
	}

	// =========================
	// 🔹 PERFORMANCE SETTIMANA
	// =========================
	public PerformanceData getWeeklyPerformance(
			String idAccount,
			int year,
			int month,
			int week
			) {

		String idPerformance = year + "-" + month + "-" + week;

		return performanceRepository
				.findByAccountAndWeek(idAccount, idPerformance)
				.map(PerformanceData::from)
				.orElse(null);
	}

	public List<PerformanceData> getPerformances(
			String idAccount,
			Integer year,
			PeriodEnum period
			) {

		if (PeriodEnum.ALL.equals(period)) {
			return performanceRepository
					.findByAccount(idAccount)
					.stream()
					.map(PerformanceData::from)
					.toList();
		}

		if (period.isMonth()) {
			int month = Integer.parseInt(period.getId());
			return getMonthlyPerformance(idAccount, year, month);
		}

		if (period.isQuarter()) {
			return getQuarterPerformance(idAccount, year, period);
		}

		throw new IllegalArgumentException("Periodo non gestito: " + period);
	}

	public List<PerformanceData> getQuarterPerformance(
			String idAccount,
			int year,
			PeriodEnum period
			) {

		int startMonth;
		int endMonth;

		switch (period) {
		case Q1:
			startMonth = 1;
			endMonth = 3;
			break;
		case Q2:
			startMonth = 4;
			endMonth = 6;
			break;
		case Q3:
			startMonth = 7;
			endMonth = 9;
			break;
		case Q4:
			startMonth = 10;
			endMonth = 12;
			break;
		default:
			throw new IllegalArgumentException("Trimestre non valido: " + period);
		}

		return performanceRepository
				.findByAccountAndYear(idAccount, year)
				.stream()
				.map(PerformanceData::from)
				.filter(p -> p.getMonth() >= startMonth && p.getMonth() <= endMonth)
				.toList();
	}

	public void recalculatePerformance(String accountId, LocalDateTime dateOpen) {

	    if (accountId == null || accountId.isBlank()) {
	        throw new IllegalArgumentException("Account obbligatorio");
	    }

	    if (dateOpen == null) {
	        throw new IllegalArgumentException("Data trade obbligatoria");
	    }

	    int year = dateOpen.getYear();
	    int month = dateOpen.getMonthValue();

	    int week = dateOpen.get(WeekFields.ISO.weekOfWeekBasedYear());

	    updateWeeklyPerformance(accountId, year, month, week);
	}

	@Transactional
	public void updateWeeklyPerformance(
	        String accountId,
	        Integer year,
	        Integer month,
	        Integer week
	) {

	    LocalDateTime[] range = DateUtils.getStartEndOfWeek(year, month, week);

	    LocalDateTime start = range[0];
	    LocalDateTime end = range[1];

	    List<Trade> trades = tradeRepository.findByAccountAndPeriod(
	            accountId,
	            start,
	            end
	    );

	    String idPerformance = year + "-" + month + "-" + week;

	    if (trades.isEmpty()) {

	        performanceRepository
	                .findByAccountAndIdPerformance(accountId, idPerformance)
	                .ifPresent(performanceRepository::delete);
	        return;
	    }

	    BigDecimal bilancioIniziale = calculateInitialBalanceForWeek(accountId, trades);

		BigDecimal profitTotale = trades.stream()
				.map(Trade::getProfit)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal bilancioFinale = bilancioIniziale.add(profitTotale);

		long wins = trades.stream()
				.filter(t -> t.getProfit() != null)
				.filter(t -> t.getProfit().compareTo(BigDecimal.ZERO) > 0)
				.count();

		long losses = trades.stream()
				.filter(t -> t.getProfit() != null)
				.filter(t -> t.getProfit().compareTo(BigDecimal.ZERO) < 0)
				.count();

		BigDecimal winrate = BigDecimal.ZERO;

		if (wins + losses > 0) {
			winrate = BigDecimal.valueOf(wins)
					.divide(BigDecimal.valueOf(wins + losses), 4, RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(100))
					.setScale(1, RoundingMode.HALF_UP);
		}

		BigDecimal rr = calculateWeeklyRR(trades);

		Performance performance = performanceRepository
		        .findByAccountAndIdPerformance(accountId, idPerformance)
		        .orElseGet(() -> {
		            Performance p = new Performance();
		            p.setId(UUID.randomUUID().toString());
		            return p;
		        });

		performance.setIdPerformance(idPerformance);
		performance.setIdAccount(accountId);
		performance.setBilancioIniziale(bilancioIniziale);
		performance.setBilancioFinale(bilancioFinale);
		performance.setWinrate(winrate);
		performance.setRr(rr);

		performanceRepository.save(performance);
	}

	private BigDecimal calculateInitialBalanceForWeek(
			String accountId,
			List<Trade> weeklyTrades
			) {

		LocalDateTime firstTradeDate = weeklyTrades.stream()
				.map(Trade::getDateOpen)
				.filter(Objects::nonNull)
				.min(LocalDateTime::compareTo)
				.orElse(null);

		if (firstTradeDate == null) {
			return new BigDecimal("25000.00");
		}

		BigDecimal previousProfit = tradeRepository.sumProfitLossBeforeDateTime(
				accountId,
				firstTradeDate
				);

		if (previousProfit == null) {
			previousProfit = BigDecimal.ZERO;
		}

		return new BigDecimal("25000.00").add(previousProfit);
	}

	private BigDecimal calculateWeeklyRR(List<Trade> trades) {

		List<BigDecimal> risks = trades.stream()
				.map(Trade::getRisk)
				.filter(Objects::nonNull)
				.filter(r -> r.compareTo(BigDecimal.ZERO) > 0)
				.toList();

		if (risks.isEmpty()) {
			return BigDecimal.ZERO;
		}

		BigDecimal totalR = trades.stream()
				.filter(t -> t.getProfit() != null && t.getRisk() != null)
				.filter(t -> t.getRisk().compareTo(BigDecimal.ZERO) > 0)
				.map(t -> t.getProfit().divide(t.getRisk(), 4, RoundingMode.HALF_UP))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return totalR.divide(
				BigDecimal.valueOf(risks.size()),
				1,
				RoundingMode.HALF_UP
				);
	}

}
