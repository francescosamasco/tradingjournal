package it.samfrafx.tradingjournal.datamodel.data;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "account", schema = "trade")
public class Account {

    @Id
    @Column(name = "uuid", nullable = false)
    private String uuid;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "initial_balance", precision = 11, scale = 2)
    private BigDecimal initialBalance;

    @Column(name = "profit", precision = 11, scale = 2)
    private BigDecimal profit;

    @Column(name = "type")
    private String type;

    // ===== GETTER / SETTER =====

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}