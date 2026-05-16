package it.samfrafx.tradingjournal.bl.data;

import java.math.BigDecimal;

import it.samfrafx.tradingjournal.datamodel.data.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountData {

    private String id;

    private String description;

    private BigDecimal initialBalance;

    private BigDecimal profit;

    private String type;

    // =========================
    // FACTORY METHOD
    // =========================
    public static AccountData from(Account account) {

        if (account == null) {
            return null;
        }

        AccountData dto = new AccountData();

        dto.setId(account.getUuid());
        dto.setDescription(account.getDescription());

        dto.setInitialBalance(account.getInitialBalance());
        dto.setProfit(account.getProfit());

        dto.setType(account.getType());

        return dto;
    }

    // =========================
    // LOGICHE UTILI
    // =========================

    public BigDecimal getCurrentBalance() {

        BigDecimal initial = initialBalance != null
                ? initialBalance
                : BigDecimal.ZERO;

        BigDecimal currentProfit = profit != null
                ? profit
                : BigDecimal.ZERO;

        return initial.add(currentProfit);
    }

    public boolean isGrowth() {

        if (profit == null) {
            return false;
        }

        return profit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isLoss() {

        if (profit == null) {
            return false;
        }

        return profit.compareTo(BigDecimal.ZERO) < 0;
    }
}