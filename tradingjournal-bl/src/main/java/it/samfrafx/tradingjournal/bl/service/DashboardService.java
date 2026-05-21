package it.samfrafx.tradingjournal.bl.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        dashboard.setProfitTotale(profitTotale);
        dashboard.setProfitPercent( getTotalProfitPercent(performances) );

        dashboard.setWinrate(calculateWinrate(performances));
        dashboard.setRrAverage(averageRr(performances));
        dashboard.setProfitFactor(calculateProfitFactor(performances));

        return dashboard;
    }

    private BigDecimal getTotalProfitPercent(List<PerformanceData> performances) {
        if (performances == null || performances.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (PerformanceData p : performances) {
            total = total.add(p.getReturnPercent());
        }

        return total.setScale(2, RoundingMode.HALF_UP);
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

    private BigDecimal averageWinrate(List<PerformanceData> performances) {
    	 if (performances == null || performances.isEmpty()) {
             return BigDecimal.ZERO;
         }
    	 List<PerformanceData> performanceFiltered = performances.stream()
	        .filter( p -> p.isGrowth() || p.isLoss())
 		        .collect(Collectors.toList());    
        return average(performanceFiltered, PerformanceData::getWinrate);
    }

    
    private BigDecimal calculateWinrate(List<PerformanceData> performances) {

        if (performances == null || performances.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int totalWin = performances.stream()
                .map(PerformanceData::getWinTrades)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int totalLoss = performances.stream()
                .map(PerformanceData::getLossTrades)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int totalTrades = totalWin + totalLoss;

        if (totalTrades == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(totalWin)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalTrades), 1, RoundingMode.HALF_UP);
    }
    private BigDecimal averageRr(List<PerformanceData> performances) {
    	 if (performances == null || performances.isEmpty()) {
             return BigDecimal.ZERO;
         }
    	 List<PerformanceData> performanceFiltered = performances.stream()
    		        .filter(PerformanceData::isGrowth)
    		        .collect(Collectors.toList());    	 
        return average(performanceFiltered, PerformanceData::getRr);
    }

    private BigDecimal calculateProfitFactor(List<PerformanceData> performances) {
        return BigDecimal.ZERO;
    }

    private BigDecimal average(   List<PerformanceData> performances,    Function<PerformanceData, BigDecimal> extractor
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