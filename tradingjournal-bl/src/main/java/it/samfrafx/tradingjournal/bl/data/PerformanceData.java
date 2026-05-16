package it.samfrafx.tradingjournal.bl.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

import it.samfrafx.tradingjournal.datamodel.data.Performance;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PerformanceData {

    private String id;
    private String idPerformance;
    private String idAccount;

    private BigDecimal bilancioIniziale;
    private BigDecimal bilancioFinale;

    private BigDecimal winrate;
    private BigDecimal rr;

    private BigDecimal profitTotale;
    private BigDecimal profitPercent;

    private Integer year;
    private Integer month;
    private Integer week;

    private Integer trades;
    private Integer winTrades;
    private Integer lossTrades;
    private Integer beTrades;
    private Integer missTrades;

    // =========================
    // FACTORY METHOD
    // =========================
    public static PerformanceData from(Performance p) {

        PerformanceData dto = new PerformanceData();

        dto.setId(p.getId());
        dto.setIdPerformance(p.getIdPerformance());
        dto.setIdAccount(p.getIdAccount());

        dto.setBilancioIniziale(p.getBilancioIniziale());
        dto.setBilancioFinale(p.getBilancioFinale());

        dto.setWinrate(p.getWinrate());
        dto.setRr(p.getRr());

        dto.setProfitTotale(p.getProfitTotale());
        dto.setProfitPercent(p.getProfitPercent());

        dto.setTrades(p.getTrades());
        dto.setWinTrades(p.getWinTrades());
        dto.setLossTrades(p.getLossTrades());
        dto.setBeTrades(p.getBeTrades());
        dto.setMissTrades(p.getMissTrades());

        return dto;
    }

    // =========================
    // LOGICHE UTILI
    // =========================

    public BigDecimal getReturnPercent() {

        if (profitPercent != null) {
            return profitPercent;
        }

        if (bilancioIniziale == null || bilancioIniziale.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (bilancioFinale == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal diff = bilancioFinale.subtract(bilancioIniziale);

        return diff.divide(bilancioIniziale, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getProfit() {

        if (profitTotale != null) {
            return profitTotale;
        }

        if (bilancioIniziale == null || bilancioFinale == null) {
            return BigDecimal.ZERO;
        }

        return bilancioFinale.subtract(bilancioIniziale);
    }

    public boolean isGrowth() {
        return getProfit().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isLoss() {
        return getProfit().compareTo(BigDecimal.ZERO) < 0;
    }

    public Integer getYear() {

        if (year != null) {
            return year;
        }

        if (idPerformance == null) {
            return null;
        }

        String[] parts = splitId();

        if (parts.length < 1) {
            return null;
        }

        return Integer.parseInt(parts[0]);
    }

    public Integer getMonth() {

        if (month != null) {
            return month;
        }

        if (idPerformance == null) {
            return null;
        }

        String[] parts = splitId();

        if (parts.length < 2) {
            return null;
        }

        return Integer.parseInt(parts[1]);
    }

    public Integer getWeek() {

        if (week != null) {
            return week;
        }

        if (idPerformance == null) {
            return null;
        }

        String[] parts = splitId();

        if (parts.length < 3) {
            return null;
        }

        return Integer.parseInt(parts[2]);
    }

    private String[] splitId() {

        if (idPerformance == null || !idPerformance.contains("-")) {
            return new String[0];
        }

        return idPerformance.split("-");
    }
}