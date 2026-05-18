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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.DashboardData;
import it.samfrafx.tradingjournal.bl.data.PerformanceData;
import it.samfrafx.tradingjournal.bl.data.TradeData;
import it.samfrafx.tradingjournal.bl.data.chart.CalendarData;
import it.samfrafx.tradingjournal.bl.data.chart.DayData;
import it.samfrafx.tradingjournal.bl.data.chart.WeekData;
import it.samfrafx.tradingjournal.bl.data.enums.VotoSetupEnum;
import it.samfrafx.tradingjournal.bl.service.DashboardService;
import it.samfrafx.tradingjournal.bl.service.PerformanceService;
import it.samfrafx.tradingjournal.bl.service.TradeService;
import it.samfrafx.tradingjournal.webapp.data.AccountSidebarData;
import it.samfrafx.tradingjournal.webapp.data.OptionData;

@Controller
public class DashboardViewController {

	private static final String DEFAULT_ACCOUNT_ID = "fa54de65-9679-406f-9bcd-d3110ab4cc6e";

	private final TradeService tradeService;
	private final PerformanceService performanceService;

	private final DashboardService service;

	public DashboardViewController(DashboardService service, TradeService tradeService, PerformanceService performanceService) {
		this.service = service;
		this.tradeService = tradeService;
		this.performanceService = performanceService;
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
		List<PerformanceData> performances = performanceService.getPerformances( accountId, year, periodEnum);
		List<TradeData> trades = this.tradeService.getTrades(accountId, year, periodEnum);

		CalendarData calendar = buildCalendarData(trades, performances);

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
				new OptionData("Tags1", "Tags1"),
				new OptionData("Tags2", "Tags2"),
				new OptionData("Tags3", "Tags3")
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

		return tradeService.calculateAccountBalanceForNewTrade(
				accountId,
				tradeDateTime,
				profitLoss
				);
	}


	@GetMapping("/api/dashboard/account-balance-preview-edit")
	@ResponseBody
	public BigDecimal getAccountBalancePreviewEdit(
			@RequestParam String accountId,
			@RequestParam String idTrade,
			@RequestParam String dateTime,
			@RequestParam BigDecimal profitLoss
			) {
		LocalDateTime tradeDateTime = LocalDateTime.parse(dateTime);

		//return tradeService.calculateAccountBalancePreviewEdit(
		//        accountId,
		//        idTrade,
		//        tradeDateTime,
		//        profitLoss
		//);

		return null;
	}

	@PostMapping("/dashboard/trade/add")
	@ResponseBody
	public ResponseEntity<?> addTrade(@ModelAttribute TradeData tradeData) {

		TradeData saved = tradeService.save(tradeData);

		this.performanceService.recalculateFromTradeDate(saved.getAccountId(),
				saved.getDateOpen());

		return ResponseEntity.ok(Map.of(
				"success", true,
				"tradeId", saved.getIdTrade()
				));
	}

	private CalendarData buildCalendarData(
			List<TradeData> trades,
			List<PerformanceData> performances
			) {

		Map<String, DayData> days = new HashMap<>();
		Map<Integer, WeekData> weeks = new HashMap<>();

		// =====================
		// WEEK DA PERFORMANCE
		// =====================
		for (PerformanceData performance : performances) {

			Integer weekNumber = performance.getWeek();

			if (weekNumber == null) {
				continue;
			}

			WeekData week = new WeekData();

			week.setAmount(
					performance.getProfit() != null
					? performance.getProfit()
							: BigDecimal.ZERO
					);

			week.setBilancioIniziale(
					performance.getBilancioIniziale() != null
					? performance.getBilancioIniziale()
							: BigDecimal.ZERO
					);

			week.setBilancioFinale(
					performance.getBilancioFinale() != null
					? performance.getBilancioFinale()
							: BigDecimal.ZERO
					);

			week.setWinrate(
					performance.getWinrate() != null
					? performance.getWinrate()
							: BigDecimal.ZERO
					);

			week.setRrAverage(
					performance.getRr() != null
					? performance.getRr()
							: BigDecimal.ZERO
					);

			week.setProfitPercent(
					performance.getReturnPercent() != null
					? performance.getReturnPercent()
							: BigDecimal.ZERO
					);

			week.setTrades(0);
			week.setWinTrades(0);
			week.setLossTrades(0);
			week.setBeTrades(0);
			week.setMissTrades(0);
			week.setRrTotal(BigDecimal.ZERO);

			weeks.put(weekNumber, week);
		}

		// =====================
		// DAY + DETTAGLI TRADE
		// =====================
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
			// WEEK DETTAGLI TRADE
			// =====================
			int weekNumber = getWeekOfYear(date);

			WeekData week = weeks.get(weekNumber);

			if (week == null) {
				week = new WeekData();

				week.setAmount(BigDecimal.ZERO);
				week.setBilancioIniziale(BigDecimal.ZERO);
				week.setBilancioFinale(BigDecimal.ZERO);
				week.setWinrate(BigDecimal.ZERO);
				week.setRrAverage(BigDecimal.ZERO);
				week.setProfitPercent(BigDecimal.ZERO);

				week.setTrades(0);
				week.setWinTrades(0);
				week.setLossTrades(0);
				week.setBeTrades(0);
				week.setMissTrades(0);
				week.setRrTotal(BigDecimal.ZERO);

				weeks.put(weekNumber, week);
			}

			week.setTrades(week.getTrades() + 1);

			if (trade.isWin()) {
				week.setWinTrades(week.getWinTrades() + 1);
			} else if (trade.isLoss()) {
				week.setLossTrades(week.getLossTrades() + 1);
			} else if (trade.isBe()) {
				week.setBeTrades(week.getBeTrades() + 1);
			} else if (trade.isMiss()) {
				week.setMissTrades(week.getMissTrades() + 1);
			}

			week.addDay(dateKey);
		}

		return new CalendarData(days, weeks);
	}

	private int getWeekOfYear(LocalDate date) {
		WeekFields weekFields = WeekFields.ISO;
		return date.get(weekFields.weekOfWeekBasedYear());
	}

	@DeleteMapping("/dashboard/trade/delete/{idTrade}")
	@ResponseBody
	public ResponseEntity<Void> deleteTrade(@PathVariable String idTrade) {

		TradeData trade = tradeService.findById(idTrade);

		String accountId = trade.getAccountId();
		LocalDateTime dateOpen = trade.getDateOpen();

		tradeService.deleteById(idTrade);

		this.performanceService.recalculateFromTradeDate(accountId,	 dateOpen);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/dashboard/trade/{idTrade}")
	@ResponseBody
	public TradeData getTrade(@PathVariable String idTrade) {
		return tradeService.findById(idTrade);
	}

	@PutMapping("/dashboard/trade/update/{idTrade}")
	@ResponseBody
	public TradeData updateTrade(
			@PathVariable String idTrade,
			TradeData data
			) {
		data.setIdTrade(idTrade);
		return tradeService.update(data);
	}


	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home");
		return "home";
	}
}