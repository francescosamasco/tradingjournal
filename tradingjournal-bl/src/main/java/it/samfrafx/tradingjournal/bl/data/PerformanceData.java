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

        return dto;
    }

    // =========================
    // LOGICHE UTILI
    // =========================

    // 🔹 rendimento %
    public BigDecimal getReturnPercent() {

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

    // 🔹 profit assoluto
    public BigDecimal getProfit() {

        if (bilancioIniziale == null || bilancioFinale == null) {
            return BigDecimal.ZERO;
        }

        return bilancioFinale.subtract(bilancioIniziale);
    }

    // 🔹 crescita account
    public boolean isGrowth() {
        return getProfit().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isLoss() {
        return getProfit().compareTo(BigDecimal.ZERO) < 0;
    }
    
    private String[] splitId() {
        return idPerformance.split("-");
    }
    
    public Integer getYear() {
        if (idPerformance == null) return null;
        return Integer.parseInt(splitId()[0]);
    }

    public Integer getMonth() {
        if (idPerformance == null) return null;
        return Integer.parseInt(splitId()[1]);
    }

    public Integer getWeek() {
        if (idPerformance == null) return null;
        return Integer.parseInt(splitId()[2]);
    }
}