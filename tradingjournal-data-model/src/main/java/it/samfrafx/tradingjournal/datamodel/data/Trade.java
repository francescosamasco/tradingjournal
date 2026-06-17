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

    @Column(name = "date_open")
    private LocalDateTime dateOpen;

    @Column(name = "asset")
    private String asset;

    @Column(name = "esito")
    private String esito;

    @Column(name = "risk", precision = 11, scale = 1, nullable = false)
    private BigDecimal risk;

    @Column(name = "profit", precision = 11, scale = 2, nullable = false)
    private BigDecimal profit;

    @Column(name = "account_balance", precision = 11, scale = 2, nullable = false)
    private BigDecimal accountBalance;

    @Column(name = "posizione")
    private String posizione;

    @Column(name = "struttura")
    private String struttura;

    @Column(name = "setup")
    private String setup;

    @Column(name = "confluenze")
    private String confluenze;

    @Column(name = "voto_setup", nullable = false)
    private Integer votoSetup;

    @Column(name = "tags")
    private String tags;

    @Column(name = "analisi")
    private String analisi;

    @Column(name = "note")
    private String note;
    
    @Column(name = "tipo_movimento", nullable = false)
    private Integer tipoMovimento;
    
    @Column(name = "tipo_trade", nullable = false)
    private Integer tipoTrade;

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

    public Integer getTipoMovimento() {
        return tipoMovimento;
    }

    public void setTipoMovimento(Integer tipoMovimento) {
        this.tipoMovimento = tipoMovimento;
    }

    public LocalDateTime getDateOpen() {
        return dateOpen;
    }

    public void setDateOpen(LocalDateTime dateOpen) {
        this.dateOpen = dateOpen;
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

    public BigDecimal getRisk() {
        return risk;
    }

    public void setRisk(BigDecimal risk) {
        this.risk = risk;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
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

    public Integer getVotoSetup() {
        return votoSetup;
    }

    public void setVotoSetup(Integer votoSetup) {
        this.votoSetup = votoSetup;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAnalisi() {
        return analisi;
    }

    public void setAnalisi(String analisi) {
        this.analisi = analisi;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

	public Integer getTipoTrade() {
		return tipoTrade;
	}

	public void setTipoTrade(Integer tipoTrade) {
		this.tipoTrade = tipoTrade;
	}
}