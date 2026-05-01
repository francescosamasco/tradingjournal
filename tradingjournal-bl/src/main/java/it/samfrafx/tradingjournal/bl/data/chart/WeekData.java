package it.samfrafx.tradingjournal.bl.data.chart;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class WeekData {

    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal profitPercent = BigDecimal.ZERO;
    private BigDecimal rrAverage = BigDecimal.ZERO;
    private BigDecimal rrTotal = BigDecimal.ZERO;

    private Integer trades = 0;

    private Set<String> uniqueDays = new HashSet<>();

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getProfitPercent() {
        return profitPercent;
    }

    public void setProfitPercent(BigDecimal profitPercent) {
        this.profitPercent = profitPercent;
    }

    public BigDecimal getRrAverage() {
        return rrAverage;
    }

    public void setRrAverage(BigDecimal rrAverage) {
        this.rrAverage = rrAverage;
    }

    public BigDecimal getRrTotal() {
        return rrTotal;
    }

    public void setRrTotal(BigDecimal rrTotal) {
        this.rrTotal = rrTotal;
    }

    public Integer getTrades() {
        return trades;
    }

    public void setTrades(Integer trades) {
        this.trades = trades;
    }

    public Integer getDays() {
        return uniqueDays.size();
    }

    public void addDay(String dateKey) {
        this.uniqueDays.add(dateKey);
    }
}