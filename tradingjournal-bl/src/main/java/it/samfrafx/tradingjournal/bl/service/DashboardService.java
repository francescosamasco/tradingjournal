package it.samfrafx.tradingjournal.bl.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.DashboardData;
import it.samfrafx.tradingjournal.bl.data.PerformanceData;

@Service
public class DashboardService {

    private final PerformanceService performanceService;

    public DashboardService(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    public DashboardData buildDashboard(String accountId, Integer year, PeriodEnum period) {

        List<PerformanceData> performances =
                performanceService.getPerformances(accountId, year, period);

        DashboardData dashboard = new DashboardData();

        BigDecimal bilancioIniziale = getInitialBalance(performances);
        BigDecimal profitTotale = getTotalProfit(performances);
        BigDecimal bilancioFinale = bilancioIniziale.add(profitTotale);

        dashboard.setAccountId(accountId);
        dashboard.setPeriodEnum(period);

        dashboard.setBilancioIniziale(bilancioIniziale);
        dashboard.setBilancioFinale(bilancioFinale);

        dashboard.setProfitPercent(
                calculateProfitPercent(bilancioIniziale, bilancioFinale)
        );

        dashboard.setWinrate(averageWinrate(performances));
        dashboard.setRrAverage(averageRr(performances));
        dashboard.setProfitFactor(calculateProfitFactor(performances));

        return dashboard;
    }

    private BigDecimal getInitialBalance(List<PerformanceData> performances) {
        if (performances == null || performances.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = performances.get(0).getBilancioIniziale();

        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getTotalProfit(List<PerformanceData> performances) {
        if (performances == null || performances.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (PerformanceData p : performances) {
            if (p.getProfit() != null) {
                total = total.add(p.getProfit());
            }
        }

        return total;
    }

    private BigDecimal calculateProfitPercent(
            BigDecimal initialBalance,
            BigDecimal finalBalance
    ) {
        if (initialBalance == null ||
                initialBalance.compareTo(BigDecimal.ZERO) == 0 ||
                finalBalance == null) {
            return BigDecimal.ZERO;
        }

        return finalBalance
                .subtract(initialBalance)
                .divide(initialBalance, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal averageWinrate(List<PerformanceData> performances) {
        return average(performances, PerformanceData::getWinrate);
    }

    private BigDecimal averageRr(List<PerformanceData> performances) {
        return average(performances, PerformanceData::getRr);
    }

    private BigDecimal calculateProfitFactor(List<PerformanceData> performances) {
        return BigDecimal.ZERO;
    }

    private BigDecimal average(
            List<PerformanceData> performances,
            Function<PerformanceData, BigDecimal> extractor
    ) {
        if (performances == null || performances.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        for (PerformanceData p : performances) {
            BigDecimal value = extractor.apply(p);

            if (value != null) {
                total = total.add(value);
                count++;
            }
        }

        if (count == 0) {
            return BigDecimal.ZERO;
        }

        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
}