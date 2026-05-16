package it.samfrafx.tradingjournal.datamodel.data;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "performance", schema = "trade")
public class Performance {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "id_performance", nullable = false)
    private String idPerformance;

    @Column(name = "id_account", nullable = false)
    private String idAccount;

    @Column(name = "bilancio_iniziale", precision = 11, scale = 2)
    private BigDecimal bilancioIniziale;

    @Column(name = "bilancio_finale", precision = 11, scale = 2)
    private BigDecimal bilancioFinale;

    @Column(name = "profit_totale", precision = 11, scale = 2)
    private BigDecimal profitTotale;

    @Column(name = "profit_percent", precision = 7, scale = 1)
    private BigDecimal profitPercent;

    @Column(name = "winrate", precision = 5, scale = 1)
    private BigDecimal winrate;

    @Column(name = "rr", precision = 6, scale = 1)
    private BigDecimal rr;

    @Column(name = "trades")
    private Integer trades;

    @Column(name = "win_trades")
    private Integer winTrades;

    @Column(name = "loss_trades")
    private Integer lossTrades;

    @Column(name = "be_trades")
    private Integer beTrades;

    @Column(name = "miss_trades")
    private Integer missTrades;

    // =========================
    // GETTER / SETTER
    // =========================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdPerformance() {
        return idPerformance;
    }

    public void setIdPerformance(String idPerformance) {
        this.idPerformance = idPerformance;
    }

    public String getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(String idAccount) {
        this.idAccount = idAccount;
    }

    public BigDecimal getBilancioIniziale() {
        return bilancioIniziale;
    }

    public void setBilancioIniziale(BigDecimal bilancioIniziale) {
        this.bilancioIniziale = bilancioIniziale;
    }

    public BigDecimal getBilancioFinale() {
        return bilancioFinale;
    }

    public void setBilancioFinale(BigDecimal bilancioFinale) {
        this.bilancioFinale = bilancioFinale;
    }

    public BigDecimal getProfitTotale() {
        return profitTotale;
    }

    public void setProfitTotale(BigDecimal profitTotale) {
        this.profitTotale = profitTotale;
    }

    public BigDecimal getProfitPercent() {
        return profitPercent;
    }

    public void setProfitPercent(BigDecimal profitPercent) {
        this.profitPercent = profitPercent;
    }

    public BigDecimal getWinrate() {
        return winrate;
    }

    public void setWinrate(BigDecimal winrate) {
        this.winrate = winrate;
    }

    public BigDecimal getRr() {
        return rr;
    }

    public void setRr(BigDecimal rr) {
        this.rr = rr;
    }

    public Integer getTrades() {
        return trades;
    }

    public void setTrades(Integer trades) {
        this.trades = trades;
    }

    public Integer getWinTrades() {
        return winTrades;
    }

    public void setWinTrades(Integer winTrades) {
        this.winTrades = winTrades;
    }

    public Integer getLossTrades() {
        return lossTrades;
    }

    public void setLossTrades(Integer lossTrades) {
        this.lossTrades = lossTrades;
    }

    public Integer getBeTrades() {
        return beTrades;
    }

    public void setBeTrades(Integer beTrades) {
        this.beTrades = beTrades;
    }

    public Integer getMissTrades() {
        return missTrades;
    }

    public void setMissTrades(Integer missTrades) {
        this.missTrades = missTrades;
    }
}