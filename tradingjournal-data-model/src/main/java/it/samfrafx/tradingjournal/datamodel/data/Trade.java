package it.samfrafx.tradingjournal.datamodel.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trade", schema = "trade")
public class Trade {

    @Id
    @Column(name = "id_trade", nullable = false)
    private String idTrade;

    @Column(name = "id_account", nullable = false)
    private String idAccount;

    @Column(name = "asset")
    private String asset;

    @Column(name = "esito")
    private String esito;

    @Column(name = "date_open")
    private LocalDateTime dateOpen;

    @Column(name = "posizione")
    private String posizione;

    @Column(name = "struttura")
    private String struttura;

    @Column(name = "setup")
    private String setup;

    @Column(name = "confluenze")
    private String confluenze;

    @Column(name = "tags")
    private String tags;

    @Column(name = "profit", precision = 7, scale = 2)
    private BigDecimal profit;

    @Column(name = "risk", precision = 7, scale = 2)
    private BigDecimal risk;

    @Column(name = "analisi1")
    private String analisi1;

    @Column(name = "analisi2")
    private String analisi2;

    @Column(name = "note")
    private String note;

    @Column(name = "week_n")
    private Integer weekN;

    public String getIdTrade() {
        return idTrade;
    }

    public void setIdTrade(String idTrade) {
        this.idTrade = idTrade;
    }

    public String getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(String idAccount) {
        this.idAccount = idAccount;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getEsito() {
        return esito;
    }

    public void setEsito(String esito) {
        this.esito = esito;
    }

    public LocalDateTime getDateOpen() {
        return dateOpen;
    }

    public void setDateOpen(LocalDateTime dateOpen) {
        this.dateOpen = dateOpen;
    }

    public String getPosizione() {
        return posizione;
    }

    public void setPosizione(String posizione) {
        this.posizione = posizione;
    }

    public String getStruttura() {
        return struttura;
    }

    public void setStruttura(String struttura) {
        this.struttura = struttura;
    }

    public String getSetup() {
        return setup;
    }

    public void setSetup(String setup) {
        this.setup = setup;
    }

    public String getConfluenze() {
        return confluenze;
    }

    public void setConfluenze(String confluenze) {
        this.confluenze = confluenze;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public BigDecimal getRisk() {
        return risk;
    }

    public void setRisk(BigDecimal risk) {
        this.risk = risk;
    }

    public String getAnalisi1() {
        return analisi1;
    }

    public void setAnalisi1(String analisi1) {
        this.analisi1 = analisi1;
    }

    public String getAnalisi2() {
        return analisi2;
    }

    public void setAnalisi2(String analisi2) {
        this.analisi2 = analisi2;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getWeekN() {
        return weekN;
    }

    public void setWeekN(Integer weekN) {
        this.weekN = weekN;
    }
}