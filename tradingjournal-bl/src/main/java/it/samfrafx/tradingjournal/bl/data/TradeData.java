package it.samfrafx.tradingjournal.bl.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import it.samfrafx.tradingjournal.datamodel.data.Trade;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TradeData {

    private String idTrade;
    private String asset;
    private String esito;
    private LocalDateTime dateOpen;
    private String posizione;
    private String struttura;
    private String setup;
    private String confluenze;
    private String tags;
    private BigDecimal profit;
    private BigDecimal risk;
    private String note;
    private Integer weekN;

    // =========================
    // FACTORY METHOD
    // =========================
    public static TradeData from(Trade t) {

        TradeData dto = new TradeData();

        dto.setIdTrade(t.getIdTrade());
        dto.setAsset(t.getAsset());
        dto.setEsito(t.getEsito());
        dto.setDateOpen(t.getDateOpen());
        dto.setPosizione(t.getPosizione());
        dto.setStruttura(t.getStruttura());
        dto.setSetup(t.getSetup());
        dto.setConfluenze(t.getConfluenze());
        dto.setTags(t.getTags());
        dto.setProfit(t.getProfit());
        dto.setRisk(t.getRisk());
        dto.setNote(t.getNote());
        dto.setWeekN(t.getWeekN());

        return dto;
    }

    // =========================
    // LOGICHE UTILI
    // =========================

    public BigDecimal getRr() {
        if (risk == null || risk.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return profit.divide(risk, 2, RoundingMode.HALF_UP);
    }

    public boolean isWin() {
        return profit != null && profit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isLoss() {
        return profit != null && profit.compareTo(BigDecimal.ZERO) < 0;
    }
}