package it.samfrafx.tradingjournal.webapp.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.DashboardData;
import it.samfrafx.tradingjournal.bl.data.TradeData;
import it.samfrafx.tradingjournal.bl.data.chart.CalendarData;
import it.samfrafx.tradingjournal.bl.data.chart.DayData;
import it.samfrafx.tradingjournal.bl.data.chart.WeekData;
import it.samfrafx.tradingjournal.bl.data.enums.VotoSetupEnum;
import it.samfrafx.tradingjournal.bl.service.DashboardService;
import it.samfrafx.tradingjournal.bl.service.TradeService;
import it.samfrafx.tradingjournal.webapp.data.AccountSidebarData;
import it.samfrafx.tradingjournal.webapp.data.OptionData;

@Controller
public class DashboardViewController {

	private static final String DEFAULT_ACCOUNT_ID = "fa54de65-9679-406f-9bcd-d3110ab4cc6e";

	private final TradeService tradeService;
	private final DashboardService service;

	public DashboardViewController(DashboardService service, TradeService tradeService) {
		this.service = service;
		this.tradeService = tradeService;
	}

	@GetMapping("/dashboard")
	public String mainView(
			@RequestParam(name = "accountId", required = false) String accountId,
			@RequestParam(name = "year", required = false) Integer year,
			@RequestParam(name = "period", required = false) String period,
			@RequestParam(required = false, defaultValue = "false") Boolean excludeErrors,
			Model model
			) {

		if (accountId == null || accountId.isBlank()) {
			accountId = DEFAULT_ACCOUNT_ID;
		}

		LocalDate today = LocalDate.now();

		if (year == null) {
			year = today.getYear();
		}

		if (period == null || period.isBlank()) {
			period = String.valueOf(today.getMonthValue());
		}

		PeriodEnum periodEnum = PeriodEnum.getEnum(period);

		DashboardData dashboard = this.service.buildDashboard(accountId, year, periodEnum);
		List<TradeData> trades = this.tradeService.getTrades(accountId, year, periodEnum);

		CalendarData calendar = buildCalendarData(trades);

		model.addAttribute("title", "Dashboard");
		model.addAttribute("accountId", accountId);
		model.addAttribute("year", year);
		model.addAttribute("period", period);
		model.addAttribute("trades", trades);

		model.addAttribute("calendar", calendar);
		model.addAttribute("dashboard", dashboard);

		model.addAttribute("strutture", List.of(
				new OptionData("Prostruttura", "Prostruttura"),
				new OptionData("Controstruttura", "Controstruttura")
				));
		
		
		List<AccountSidebarData> accounts = List.of(
				new AccountSidebarData("fa54de65-9679-406f-9bcd-d3110ab4cc6e", "Personale 25k"),
				new AccountSidebarData("0000", "Demo test")
			);

		model.addAttribute("accounts", accounts);
		model.addAttribute("accountId", accountId);
		model.addAttribute("calendarYear", year);
		model.addAttribute("period", period);

		model.addAttribute("tags", List.of(
				new OptionData("1", "Errore1"),
				new OptionData("2", "Errore2"),
				new OptionData("3", "Tags1")
			));		
		model.addAttribute("months", List.of(
			new OptionData("1", "Gennaio"),
			new OptionData("2", "Febbraio"),
			new OptionData("3", "Marzo"),
			new OptionData("4", "Aprile"),
			new OptionData("5", "Maggio"),
			new OptionData("6", "Giugno"),
			new OptionData("7", "Luglio"),
			new OptionData("8", "Agosto"),
			new OptionData("9", "Settembre"),
			new OptionData("10", "Ottobre"),
			new OptionData("11", "Novembre"),
			new OptionData("12", "Dicembre")
		));
		
		
	    model.addAttribute("excludeErrors", excludeErrors);

		return "dashboard";
	}

	@GetMapping("/api/dashboard/confluenze")
	@ResponseBody
	public List<OptionData> getConfluenze(
			@RequestParam String struttura,
			@RequestParam String setup) {


		List<String> confluenze = tradeService.calculateConfluenze(struttura, setup);

		return confluenze.stream()
				.map(c -> new OptionData(c, c))
				.toList();
	}

	@GetMapping("/api/dashboard/voto-setup")
	@ResponseBody
	public OptionData getVotoSetup(
	        @RequestParam String struttura,
	        @RequestParam String setup,
	        @RequestParam String confluenze) {

	    VotoSetupEnum voto = tradeService.getVotoSetupEnum(struttura, setup, confluenze);

	    if (voto != VotoSetupEnum.ALTO && voto != VotoSetupEnum.MEDIO) {
	        voto = VotoSetupEnum.NON_STRATEGIA;
	    }

	    return new OptionData(
	            voto.name(),
	            voto.getDescrizione()
	    );
	}

	@GetMapping("/api/dashboard/account-balance-preview")
	@ResponseBody
	public BigDecimal getAccountBalancePreview( @RequestParam String accountId, @RequestParam String dateTime, @RequestParam BigDecimal profitLoss) {

		LocalDateTime tradeDateTime = LocalDateTime.parse(dateTime);

		return service.calculateAccountBalancePreview(
				accountId,
				tradeDateTime,
				profitLoss
		);
	}
	

	@PostMapping("/dashboard/trade/add")
	@ResponseBody
	public ResponseEntity<?> addTrade(@ModelAttribute TradeData tradeData) {

	    TradeData saved = tradeService.save(tradeData);

	    return ResponseEntity.ok(Map.of(
	            "success", true,
	            "tradeId", saved.getIdTrade()
	    ));
	}

	private CalendarData buildCalendarData(List<TradeData> trades) {

		Map<String, DayData> days = new HashMap<>();
		Map<Integer, WeekData> weeks = new HashMap<>();

		for (TradeData trade : trades) {

			if (trade.getDateOpen() == null) {
				continue;
			}

			LocalDate date = trade.getDateOpen().toLocalDate();
			String dateKey = date.toString();

			BigDecimal profit = trade.getProfit() != null
					? trade.getProfit()
					: BigDecimal.ZERO;

			// =====================
			// DAY
			// =====================
			DayData day = days.get(dateKey);

			if (day == null) {
				day = new DayData(
						BigDecimal.ZERO,
						0,
						trade.getAccountBalance(),
						BigDecimal.ZERO
				);
				days.put(dateKey, day);
			}

			BigDecimal newDayAmount = day.getAmount().add(profit);

			BigDecimal dayPercentage = BigDecimal.ZERO;

			if (day.getStartBalance() != null
					&& day.getStartBalance().compareTo(BigDecimal.ZERO) != 0) {

				dayPercentage = newDayAmount
						.divide(day.getStartBalance(), 4, RoundingMode.HALF_UP)
						.multiply(BigDecimal.valueOf(100));
			}

			day.setAmount(newDayAmount);
			day.setTrades(day.getTrades() + 1);
			day.setPercentage(dayPercentage);

			// =====================
			// WEEK
			// =====================
			int weekNumber = getWeekOfYear(date);

			WeekData week = weeks.get(weekNumber);

			if (week == null) {
				week = new WeekData();
				week.setAmount(BigDecimal.ZERO);
				week.setTrades(0);
				week.setWinTrades(0);
				week.setLossTrades(0);
				week.setBeTrades(0);
				week.setMissTrades(0);
				week.setRrTotal(BigDecimal.ZERO);

				BigDecimal initialBalance = trade.getAccountBalance() != null
						? trade.getAccountBalance()
						: BigDecimal.ZERO;

				week.setBilancioIniziale(initialBalance);
				week.setBilancioFinale(initialBalance);

				weeks.put(weekNumber, week);
			}

			BigDecimal rr = trade.getReturnPercent() != null
					? trade.getReturnPercent()
					: BigDecimal.ZERO;

			BigDecimal newWeekAmount = week.getAmount().add(profit);
			BigDecimal newRrTotal = week.getRrTotal().add(rr);
			int newTradeCount = week.getTrades() + 1;

			int wins = week.getWinTrades();
			int losses = week.getLossTrades();
			int be = week.getBeTrades();
			int miss = week.getMissTrades();

			if (trade.isWin()) {
				wins++;
			} else if (trade.isLoss()) {
				losses++;
			} else if (trade.isBe()) {
				be++;
			} else if (trade.isMiss()) {
				miss++;
			}

			week.setWinTrades(wins);
			week.setLossTrades(losses);
			week.setBeTrades(be);
			week.setMissTrades(miss);

			week.setAmount(newWeekAmount);
			week.setTrades(newTradeCount);
			week.setRrTotal(newRrTotal);

			BigDecimal weekStartBalance = week.getBilancioIniziale() != null
					? week.getBilancioIniziale()
					: BigDecimal.ZERO;

			BigDecimal weekFinalBalance = weekStartBalance.add(newWeekAmount);
			week.setBilancioFinale(weekFinalBalance);

			// =====================
			// WINRATE
			// =====================
			int winLossTotal = wins + losses;

			if (winLossTotal > 0) {
				week.setWinrate(
						BigDecimal.valueOf(wins)
								.divide(BigDecimal.valueOf(winLossTotal), 4, RoundingMode.HALF_UP)
								.multiply(BigDecimal.valueOf(100))
				);
			} else {
				week.setWinrate(BigDecimal.ZERO);
			}

			// =====================
			// RR MEDIO
			// =====================
			if (newTradeCount > 0) {
				week.setRrAverage(
						newRrTotal.divide(
								BigDecimal.valueOf(newTradeCount),
								2,
								RoundingMode.HALF_UP
						)
				);
			}

			// =====================
			// PROFIT %
			// =====================
			if (weekStartBalance.compareTo(BigDecimal.ZERO) != 0) {
				BigDecimal profitPercent = newWeekAmount
						.divide(weekStartBalance, 4, RoundingMode.HALF_UP)
						.multiply(BigDecimal.valueOf(100));

				week.setProfitPercent(profitPercent);
			} else {
				week.setProfitPercent(BigDecimal.ZERO);
			}

			week.addDay(dateKey);
		}

		return new CalendarData(days, weeks);
	}


	private int getWeekOfYear(LocalDate date) {
		WeekFields weekFields = WeekFields.ISO;
		return date.get(weekFields.weekOfWeekBasedYear());
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home");
		return "home";
	}
}