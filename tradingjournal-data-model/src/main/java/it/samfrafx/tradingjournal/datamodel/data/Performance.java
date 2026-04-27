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

    @Column(name = "bilancio_iniziale", nullable = false, precision = 11, scale = 2)
    private BigDecimal bilancioIniziale;

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
}