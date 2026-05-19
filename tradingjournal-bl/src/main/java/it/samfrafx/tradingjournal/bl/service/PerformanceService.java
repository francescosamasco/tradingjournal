package it.samfrafx.tradingjournal.bl.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.PerformanceData;
import it.samfrafx.tradingjournal.bl.data.TradeData;
import it.samfrafx.tradingjournal.bl.util.DateUtils;
import it.samfrafx.tradingjournal.datamodel.data.Performance;
import it.samfrafx.tradingjournal.datamodel.repository.PerformanceRepository;
import it.samfrafx.tradingjournal.datamodel.repository.TradeRepository;

@Service
public class PerformanceService {

	private final AccountService accountService;
	private final PerformanceRepository performanceRepository;
	private final TradeService tradeService;
	
	public PerformanceService(
	        PerformanceRepository performanceRepository,
	        TradeRepository tradeRepository,
	        AccountService accountService,
	        TradeService tradeService
	) {
	    this.performanceRepository = performanceRepository;
	    this.accountService = accountService;
	    this.tradeService = tradeService;
	}

	public PerformanceData findByAccountIdAndWeek(String accountId, LocalDate date) {

		String idPerformance = buildIdPerformance(date);

		return performanceRepository
				.findByAccountAndIdPerformance(accountId, idPerformance)
				.map(PerformanceData::from)
				.orElseThrow(() -> new IllegalStateException(
						"Performance settimanale non trovata per account " + accountId +
						" e performance " + idPerformance
						));
	}
	
	@Transactional
	public void recalculateFromTradeDate(
	        String accountId,
	        LocalDateTime tradeDateTime
	) {

	    if (accountId == null || accountId.isBlank()) {
	        throw new IllegalArgumentException("Account obbligatorio");
	    }

	    if (tradeDateTime == null) {
	        throw new IllegalArgumentException("Data trade obbligatoria");
	    }

	    LocalDate tradeDate = tradeDateTime.toLocalDate();

	    Integer year = tradeDate.getYear();
	    Integer month = tradeDate.getMonthValue();

	    WeekFields weekFields = WeekFields.of(Locale.ITALY);
	    Integer week = tradeDate.get(weekFields.weekOfWeekBasedYear());

	    String idPerformance = buildIdPerformance(year, month, week);

	    /*
	     * 1. Prima ricalcolo SEMPRE la settimana del trade.
	     * Se non esiste, updateWeeklyPerformance la crea.
	     * Se esiste, la aggiorna.
	     */
	    updateWeeklyPerformance(
	            accountId,
	            year,
	            month,
	            week
	    );

	    /*
	     * 2. Ora recupero tutte le performance dalla settimana del trade in poi.
	     * A questo punto la settimana esiste sicuramente.
	     */
	    List<Performance> performances =
	            performanceRepository.findFromPerformanceId(
	                    accountId,
	                    idPerformance
	            );

	    if (performances == null || performances.isEmpty()) {
	        return;
	    }

	    performances = performances.stream()
	            .sorted(Comparator.comparing(Performance::getIdPerformance))
	            .toList();

	    /*
	     * 3. Ricalcolo tutte le settimane successive.
	     * Salto quella già ricalcolata sopra per evitare doppio calcolo inutile.
	     */
	    for (Performance performance : performances) {

	        if (idPerformance.equals(performance.getIdPerformance())) {
	            continue;
	        }

	        String[] parts = performance.getIdPerformance().split("-");

	        Integer performanceYear = Integer.parseInt(parts[0]);
	        Integer performanceMonth = Integer.parseInt(parts[1]);
	        Integer performanceWeek = Integer.parseInt(parts[2]);

	        updateWeeklyPerformance(
	                accountId,
	                performanceYear,
	                performanceMonth,
	                performanceWeek
	        );
	    }
	}
	
	public PerformanceData findClosestPreviousPerformance(String accountId, LocalDate date) {

		String idPerformance = buildIdPerformance(date);

		return performanceRepository
				.findPreviousPerformances(accountId, idPerformance)
				.stream()
				.findFirst()
				.map(PerformanceData::from)
				.orElse(null);
	}

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

	public PerformanceData getWeeklyPerformance(
			String idAccount,
			int year,
			int month,
			int week
			) {

		String idPerformance = buildIdPerformance(year, month, week);

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
				.filter(p -> p.getMonth() != null)
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

		String idPerformance = buildIdPerformance(year, month, week);

		LocalDateTime[] range = DateUtils.getStartEndOfWeek(year, month, week);
		LocalDateTime start = range[0];
		LocalDateTime end = range[1];

		List<TradeData> trades = tradeService.findByAccountAndPeriod(
		        accountId,
		        start,
		        end
		);

		if (trades.isEmpty()) {
			performanceRepository
			.findByAccountAndIdPerformance(accountId, idPerformance)
			.ifPresent(performanceRepository::delete);
			return;
		}

		BigDecimal bilancioIniziale = calculateInitialBalanceForWeek( accountId, idPerformance );

		BigDecimal profitTotale = calculateProfitTotale(trades);

		BigDecimal bilancioFinale = bilancioIniziale
				.add(profitTotale)
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal profitPercent = calculateProfitPercent(
				bilancioIniziale,
				profitTotale
				);

		Integer tradesCount = trades.size();
		Integer winTrades = Math.toIntExact(trades.stream().filter(TradeData::isWin).count());
		Integer lossTrades = Math.toIntExact(trades.stream().filter(TradeData::isLoss).count());
		Integer beTrades = Math.toIntExact(trades.stream().filter(TradeData::isBe).count());
		Integer missTrades = Math.toIntExact(trades.stream().filter(TradeData::isMiss).count());

		BigDecimal winrate = calculateWinrate(winTrades, lossTrades);
		BigDecimal rr = calculateWeeklyRR(trades);

		Performance performance = getOrCreatePerformance(accountId, idPerformance);

		performance.setBilancioIniziale(bilancioIniziale);
		performance.setBilancioFinale(bilancioFinale);

		performance.setProfitTotale(profitTotale);
		performance.setProfitPercent(profitPercent);

		performance.setWinrate(winrate);
		performance.setRr(rr);

		performance.setTrades(tradesCount);
		performance.setWinTrades(winTrades);
		performance.setLossTrades(lossTrades);
		performance.setBeTrades(beTrades);
		performance.setMissTrades(missTrades);

		performanceRepository.save(performance);
	}

	private Performance getOrCreatePerformance(
			String accountId,
			String idPerformance
			) {

		return performanceRepository
				.findByAccountAndIdPerformance(accountId, idPerformance)
				.orElseGet(() -> {
					Performance p = new Performance();
					p.setId(UUID.randomUUID().toString());
					p.setIdAccount(accountId);
					p.setIdPerformance(idPerformance);
					return p;
				});
	}

	private BigDecimal calculateInitialBalanceForWeek(
			String accountId,
			String idPerformance
			) {

		BigDecimal previousFinalBalance = findPreviousFinalBalance(
				accountId,
				idPerformance
				);

		if (previousFinalBalance != null) {
			return previousFinalBalance.setScale(2, RoundingMode.HALF_UP);
		}

		return accountService.findById(accountId)
				.getInitialBalance()
				.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal findPreviousFinalBalance(
			String accountId,
			String idPerformance
			) {

		return performanceRepository
				.findPreviousPerformances(accountId, idPerformance)
				.stream()
				.findFirst()
				.map(Performance::getBilancioFinale)
				.filter(Objects::nonNull)
				.orElse(null);
	}

	private BigDecimal calculateProfitTotale(List<TradeData> trades) {

		return trades.stream()
				.map(TradeData::getProfit)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateProfitPercent(
			BigDecimal bilancioIniziale,
			BigDecimal profitTotale
			) {

		if (bilancioIniziale == null || bilancioIniziale.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}

		if (profitTotale == null) {
			return BigDecimal.ZERO;
		}

		return profitTotale
				.divide(bilancioIniziale, 4, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateWinrate(
			Integer wins,
			Integer losses
			) {

		int total = wins + losses;

		if (total == 0) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(wins)
				.divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateWeeklyRR(List<TradeData> trades) {

	    List<TradeData> validTrades = trades.stream()
	            .filter(t -> t.getProfit() != null)
	            .toList();

	    if (validTrades.isEmpty()) {
	        return BigDecimal.ZERO;
	    }

	    List<BigDecimal> wins = validTrades.stream()
	            .map(TradeData::getProfit)
	            .filter(p -> p.compareTo(BigDecimal.ZERO) > 0)
	            .toList();

	    List<BigDecimal> losses = validTrades.stream()
	            .map(TradeData::getProfit)
	            .filter(p -> p.compareTo(BigDecimal.ZERO) < 0)
	            .map(BigDecimal::abs)
	            .toList();

	    // =========================
	    // NO WIN
	    // =========================
	    if (wins.isEmpty()) {
	        return BigDecimal.ZERO;
	    }

	    // =========================
	    // CASO STANDARD
	    // media win / media loss
	    // =========================
	    if (!losses.isEmpty()) {

	        BigDecimal averageWin = wins.stream()
	                .reduce(BigDecimal.ZERO, BigDecimal::add)
	                .divide(
	                        BigDecimal.valueOf(wins.size()),
	                        4,
	                        RoundingMode.HALF_UP
	                );

	        BigDecimal averageLoss = losses.stream()
	                .reduce(BigDecimal.ZERO, BigDecimal::add)
	                .divide(
	                        BigDecimal.valueOf(losses.size()),
	                        4,
	                        RoundingMode.HALF_UP
	                );

	        if (averageLoss.compareTo(BigDecimal.ZERO) == 0) {
	            return BigDecimal.ZERO;
	        }

	        return averageWin.divide(
	                averageLoss,
	                2,
	                RoundingMode.HALF_UP
	        );
	    }

	    // =========================
	    // SOLO WIN
	    // =========================
	    List<BigDecimal> rrs = validTrades.stream()
	            .map(TradeData::getRr)
	            .filter(Objects::nonNull)
	            .filter(rr -> rr.compareTo(BigDecimal.ZERO) > 0)
	            .toList();

	    if (rrs.isEmpty()) {
	        return BigDecimal.ZERO;
	    }

	    return rrs.stream()
	            .reduce(BigDecimal.ZERO, BigDecimal::add)
	            .divide(
	                    BigDecimal.valueOf(rrs.size()),
	                    2,
	                    RoundingMode.HALF_UP
	            );
	}

	private String buildIdPerformance(LocalDate date) {

		int year = date.getYear();
		int month = date.getMonthValue();
		int week = date.get(WeekFields.ISO.weekOfWeekBasedYear());

		return buildIdPerformance(year, month, week);
	}

	private String buildIdPerformance(
			Integer year,
			Integer month,
			Integer week
			) {

		return year + "-" + month + "-" + week;
	}
}