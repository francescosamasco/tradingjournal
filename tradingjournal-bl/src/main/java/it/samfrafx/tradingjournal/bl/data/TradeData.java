package it.samfrafx.tradingjournal.bl.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import it.samfrafx.tradingjournal.bl.data.enums.VotoSetupEnum;
import it.samfrafx.tradingjournal.datamodel.data.Trade;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TradeData {

    private String idTrade;
    private String accountId;
    private String tipoMovimento;

    private String asset;
    private String esito;
    private LocalDateTime dateOpen;

    private String posizione;
    private String struttura;
    private String setup;
    private String confluenze;
    private String votoSetup;
    private String tags;

    private BigDecimal profit;
    private BigDecimal risk;
    private BigDecimal accountBalance;

    private String analisi;
    private String note;

    // =========================
    // FACTORY METHOD
    // =========================
    public static TradeData from(Trade t) {

        TradeData dto = new TradeData();

        dto.setIdTrade(t.getIdTrade());
        dto.setAccountId(t.getIdAccount());
        dto.setTipoMovimento(t.getTipoMovimento());

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
        dto.setAccountBalance(t.getAccountBalance());

        dto.setAnalisi(t.getAnalisi());
        dto.setNote(t.getNote());

        if (t.getVotoSetup() != null) {
            dto.setVotoSetup(VotoSetupEnum.fromNumeric(t.getVotoSetup()).getDescrizione());
        } else {
            dto.setVotoSetup(VotoSetupEnum.NON_STRATEGIA.getDescrizione());
        }

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


    // =========================
    // TIPO MOVIMENTO
    // =========================

    public boolean isTrade() {
        return "trade".equalsIgnoreCase(tipoMovimento);
    }

    public boolean isPrelievo() {
        return "prelievo".equalsIgnoreCase(tipoMovimento);
    }

    public boolean isDeposito() {
        return "deposito".equalsIgnoreCase(tipoMovimento);
    }

    // =========================
    // ESITI TRADE
    // =========================

    public boolean isWin() {
        return profit != null && profit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isLoss() {
        return profit != null && profit.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isBe() {
        return profit != null
                && profit.compareTo(BigDecimal.ZERO) == 0
                && "BE".equalsIgnoreCase(esito);
    }

    public boolean isMiss() {
        return "MISS".equalsIgnoreCase(esito);
    }

    public VotoSetupEnum getVotoSetupEnum() {
        return VotoSetupEnum.fromDescrizione(votoSetup);
    }
    
    public BigDecimal getRr() {

        if (profit == null || profit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (risk == null || risk.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (accountBalance == null || accountBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal riskAmount = accountBalance.subtract(profit)
                .multiply(risk)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        if (riskAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return profit.divide(
                riskAmount,
                2,
                RoundingMode.HALF_UP
        );
    }
    
    
}