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
    private String accountId;
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

    private BigDecimal accountBalance;

    // =========================
    // FACTORY METHOD
    // =========================
    public static TradeData from(Trade t, BigDecimal balance) {

        TradeData dto = new TradeData();

        dto.setAccountId( t.getIdAccount());
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
        dto.setAccountBalance(balance);

        return dto;
    }

    // =========================
    // LOGICHE UTILI
    // =========================

    public BigDecimal getReturnPercent() {

        if (accountBalance == null || accountBalance.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (profit == null) {
            return BigDecimal.ZERO;
        }

        return profit.divide(accountBalance, 4, RoundingMode.HALF_UP)
                     .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getCurrentBalance() {
        return accountBalance != null && profit != null
                ? accountBalance.add(profit)
                : accountBalance;
    }

    // =========================
    // ESITI TRADE (FONDAMENTALE)
    // =========================

    public boolean isWin() {
        return profit != null && profit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isLoss() {
        return profit != null && profit.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isBe() {
        return profit != null && profit.compareTo(BigDecimal.ZERO) == 0
                && "BE".equalsIgnoreCase(esito);
    }

    public boolean isMiss() {
        return "MISS".equalsIgnoreCase(esito);
    }
}