package it.samfrafx.tradingjournal.bl.data.chart;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WeekData {

    // =========================
    // PERFORMANCE
    // =========================
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal profitPercent = BigDecimal.ZERO;

    // =========================
    // RR
    // =========================
    private BigDecimal rrAverage = BigDecimal.ZERO;
    private BigDecimal rrTotal = BigDecimal.ZERO;

    // =========================
    // TRADE COUNT
    // =========================
    private Integer trades = 0;

    // =========================
    // GIORNI UNICI
    // =========================
    private Set<String> uniqueDays = new HashSet<>();

    // =========================
    // ESITI TRADE
    // =========================
    private Integer winTrades = 0;
    private Integer lossTrades = 0;
    private Integer beTrades = 0;
    private Integer missTrades = 0;

    // =========================
    // KPI
    // =========================
    private BigDecimal winrate = BigDecimal.ZERO;

    // =========================
    // UTILS
    // =========================

    public Integer getDays() {
        return uniqueDays.size();
    }

    public void addDay(String dateKey) {
        this.uniqueDays.add(dateKey);
    }

    public int getWinLossTotal() {
        return winTrades + lossTrades;
    }

    public int getTotalOutcomeTrades() {
        return winTrades + lossTrades + beTrades + missTrades;
    }
}