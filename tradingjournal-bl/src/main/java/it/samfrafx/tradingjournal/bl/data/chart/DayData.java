package it.samfrafx.tradingjournal.bl.data.chart;

import java.math.BigDecimal;

public class DayData {

    private BigDecimal amount;
    private Integer trades;
    private BigDecimal startBalance;
    private BigDecimal percentage;

    public DayData() {
    }

    public DayData(BigDecimal amount, Integer trades, BigDecimal startBalance, BigDecimal percentage) {
        this.amount = amount;
        this.trades = trades;
        this.startBalance = startBalance;
        this.percentage = percentage;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getTrades() {
        return trades;
    }

    public void setTrades(Integer trades) {
        this.trades = trades;
    }

    public BigDecimal getStartBalance() {
        return startBalance;
    }

    public void setStartBalance(BigDecimal startBalance) {
        this.startBalance = startBalance;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}