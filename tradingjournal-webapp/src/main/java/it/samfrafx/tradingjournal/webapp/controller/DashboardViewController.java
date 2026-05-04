package it.samfrafx.tradingjournal.webapp.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.DashboardData;
import it.samfrafx.tradingjournal.bl.data.TradeData;
import it.samfrafx.tradingjournal.bl.data.chart.CalendarData;
import it.samfrafx.tradingjournal.bl.data.chart.DayData;
import it.samfrafx.tradingjournal.bl.data.chart.WeekData;
import it.samfrafx.tradingjournal.bl.service.DashboardService;
import it.samfrafx.tradingjournal.bl.service.TradeService;

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

		DashboardData dash = service.buildDashboard(accountId, year, periodEnum);

		List<TradeData> trades = tradeService.getTrades(accountId, year, periodEnum);

		CalendarData calendar = buildCalendarData(trades);

		model.addAttribute("title", "Dashboard");
		model.addAttribute("accountId", accountId);
		model.addAttribute("year", year);
		model.addAttribute("period", period);
		model.addAttribute("trades", trades);

		model.addAttribute("calendar", calendar);
		model.addAttribute("dashboard", dash);

		return "dashboard";
	}

	@PostMapping("/trades/add")
	public String addTrade(
	        @RequestParam String accountId,
	        @RequestParam String date,
	        @RequestParam String result,
	        @RequestParam(required = false) BigDecimal profitLoss,
	        @RequestParam(required = false) BigDecimal rr,
	        @RequestParam(required = false) String note,
	        @RequestParam Integer year,
	        @RequestParam Integer month
	) {

	 //  tradeService.addTrade(
	 //          accountId,
	 //          LocalDate.parse(date),
	 //          result,
	 //          profitLoss,
	 //          rr,
	 //          note
	 //  );

	    return "redirect:/dashboard?year=" + year
	            + "&period=" + month
	            + "&accountId=" + accountId;
	}
	
	private CalendarData buildCalendarData(List<TradeData> trades) {

		Map<String, DayData> days = new HashMap<>();
		Map<Integer, WeekData> weeks = new HashMap<>();

		for (TradeData trade : trades) {

			if (trade.getDateOpen() == null) {
				continue;
			}

			String dateKey = trade.getDateOpen().toLocalDate().toString();
			LocalDate date = trade.getDateOpen().toLocalDate();

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

			BigDecimal newAmount = day.getAmount().add(profit);

			BigDecimal percentage = BigDecimal.ZERO;
			if (day.getStartBalance() != null && day.getStartBalance().compareTo(BigDecimal.ZERO) != 0) {
				percentage = newAmount
						.divide(day.getStartBalance(), 4, RoundingMode.HALF_UP)
						.multiply(BigDecimal.valueOf(100));
			}

			day.setAmount(newAmount);
			day.setTrades(day.getTrades() + 1);
			day.setPercentage(percentage);

			// =====================
			// WEEK
			// =====================
			int weekNumber = getWeekOfYear(date);

			WeekData week = weeks.get(weekNumber);

			if (week == null) {
				week = new WeekData();
				weeks.put(weekNumber, week);
			}

			BigDecimal rr = trade.getReturnPercent() != null
					? trade.getReturnPercent()
							: BigDecimal.ZERO;

			BigDecimal newWeekAmount = week.getAmount().add(profit);
			BigDecimal newRrTotal = week.getRrTotal().add(rr);
			Integer newTradeCount = week.getTrades() + 1;
			Integer newWinTrades = week.getWinTrades();

			if (trade.isWin()) {
			    newWinTrades++;
			}

			week.setWinTrades(newWinTrades);
			week.setAmount(newWeekAmount);
			week.setTrades(newTradeCount);
			week.setWinrate(
			        BigDecimal.valueOf(newWinTrades)
			                .divide(BigDecimal.valueOf(newTradeCount), 4, RoundingMode.HALF_UP)
			                .multiply(BigDecimal.valueOf(100))
			);
			week.setRrTotal(newRrTotal);
			week.setRrAverage(
					newRrTotal.divide(
							BigDecimal.valueOf(newTradeCount),
							2,
							RoundingMode.HALF_UP
							)
					);
			week.addDay(dateKey);

			BigDecimal weekStartBalance = trade.getAccountBalance();

			if (weekStartBalance != null && weekStartBalance.compareTo(BigDecimal.ZERO) != 0) {
				BigDecimal profitPercent = newWeekAmount
						.divide(weekStartBalance, 4, RoundingMode.HALF_UP)
						.multiply(BigDecimal.valueOf(100));

				week.setProfitPercent(profitPercent);
			}
		}

		return new CalendarData(days, weeks);
	}


	private Map<String, Object> buildTradesByDate(List<TradeData> trades) {

		Map<String, Object> tradesByDate = new HashMap<>();

		for (TradeData trade : trades) {

			if (trade.getDateOpen() == null) {
				continue;
			}

			String dateKey = trade.getDateOpen().toLocalDate().toString();

			@SuppressWarnings("unchecked")
			Map<String, Object> dayData = (Map<String, Object>) tradesByDate.get(dateKey);

			if (dayData == null) {
				dayData = new HashMap<>();
				dayData.put("amount", BigDecimal.ZERO);
				dayData.put("trades", 0);
				dayData.put("startBalance", trade.getAccountBalance());
				dayData.put("percentage", BigDecimal.ZERO);

				tradesByDate.put(dateKey, dayData);
			}

			BigDecimal currentAmount = (BigDecimal) dayData.get("amount");
			Integer currentTrades = (Integer) dayData.get("trades");

			BigDecimal profit = trade.getProfit() != null
					? trade.getProfit()
							: BigDecimal.ZERO;

			BigDecimal newAmount = currentAmount.add(profit);

			BigDecimal startBalance = (BigDecimal) dayData.get("startBalance");

			BigDecimal percentage = BigDecimal.ZERO;

			if (startBalance != null && startBalance.compareTo(BigDecimal.ZERO) != 0) {
				percentage = newAmount
						.divide(startBalance, 4, RoundingMode.HALF_UP)
						.multiply(BigDecimal.valueOf(100));
			}

			dayData.put("amount", newAmount);
			dayData.put("trades", currentTrades + 1);
			dayData.put("percentage", percentage);
		}

		return tradesByDate;
	}

	private Map<Integer, Object> buildWeeksSummary(List<TradeData> trades) {

		Map<Integer, Object> weeksSummary = new HashMap<>();

		for (TradeData trade : trades) {

			if (trade.getDateOpen() == null) {
				continue;
			}

			LocalDate tradeDate = trade.getDateOpen().toLocalDate();

			int weekNumber = getWeekOfYear(tradeDate);

			@SuppressWarnings("unchecked")
			Map<String, Object> weekData = (Map<String, Object>) weeksSummary.get(weekNumber);

			if (weekData == null) {
				weekData = new HashMap<>();
				weekData.put("amount", BigDecimal.ZERO);
				weekData.put("days", 0);
				weekData.put("trades", 0);

				weeksSummary.put(weekNumber, weekData);
			}

			BigDecimal currentAmount = (BigDecimal) weekData.get("amount");
			Integer currentDays = (Integer) weekData.get("days");
			Integer currentTrades = (Integer) weekData.get("trades");

			BigDecimal profit = trade.getProfit() != null
					? trade.getProfit()
							: BigDecimal.ZERO;

			weekData.put("amount", currentAmount.add(profit));
			weekData.put("days", currentDays + 1);
			weekData.put("trades", currentTrades + 1);
		}

		return weeksSummary;
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