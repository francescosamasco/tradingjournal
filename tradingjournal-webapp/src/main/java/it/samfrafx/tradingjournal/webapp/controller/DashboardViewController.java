package it.samfrafx.tradingjournal.webapp.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.TradeData;
import it.samfrafx.tradingjournal.bl.service.TradeService;

@Controller
public class DashboardViewController {

    private static final String DEFAULT_ACCOUNT_ID = "fa54de65-9679-406f-9bcd-d3110ab4cc6e";

    private final TradeService tradeService;

    public DashboardViewController(TradeService tradeService) {
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

        List<TradeData> trades = tradeService.getTradesByAccountAndPeriod(
                accountId,
                periodEnum,
                year
        );

        Map<String, Object> tradesByDate = buildTradesByDate(trades);

        model.addAttribute("title", "Dashboard");
        model.addAttribute("accountId", accountId);
        model.addAttribute("year", year);
        model.addAttribute("period", period);
        model.addAttribute("trades", trades);
        model.addAttribute("tradesByDate", tradesByDate);

        return "dashboard";
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
                tradesByDate.put(dateKey, dayData);
            }

            BigDecimal currentAmount = (BigDecimal) dayData.get("amount");
            Integer currentTrades = (Integer) dayData.get("trades");

            BigDecimal profit = trade.getProfit() != null ? trade.getProfit() : BigDecimal.ZERO;

            dayData.put("amount", currentAmount.add(profit));
            dayData.put("trades", currentTrades + 1);
        }

        return tradesByDate;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home");
        return "home";
    }
}