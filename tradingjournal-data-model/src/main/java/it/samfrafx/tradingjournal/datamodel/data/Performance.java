package it.samfrafx.tradingjournal.datamodel.data;

import java.math.BigDecimal;
import jakarta.persistence.*;

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

    @Column(name = "winrate", precision = 4, scale = 1)
    private BigDecimal winrate;

    @Column(name = "rr", precision = 4, scale = 1)
    private BigDecimal rr;

    // ===== GETTER / SETTER =====

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
}