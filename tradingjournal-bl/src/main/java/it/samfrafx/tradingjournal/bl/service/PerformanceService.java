package it.samfrafx.tradingjournal.bl.service;

import java.util.List;

import org.springframework.stereotype.Service;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.PerformanceData;
import it.samfrafx.tradingjournal.datamodel.repository.PerformanceRepository;

@Service
public class PerformanceService {

    private final PerformanceRepository performanceRepository;

    public PerformanceService(PerformanceRepository performanceRepository) {
        this.performanceRepository = performanceRepository;
    }

    // =========================
    // 🔹 PERFORMANCE MENSILE (lista settimane)
    // =========================
    public List<PerformanceData> getMonthlyPerformance(
            String idAccount,
            int year,
            int month
    ) {

        String pattern = year + "-" + month + "-%";

        return performanceRepository
                .findByAccountAndMonth(idAccount, pattern)
                .stream()
                .map(PerformanceData::from)
                .toList();
    }

    // =========================
    // 🔹 PERFORMANCE SETTIMANA
    // =========================
    public PerformanceData getWeeklyPerformance(
            String idAccount,
            int year,
            int month,
            int week
    ) {

        String idPerformance = year + "-" + month + "-" + week;

        return performanceRepository
                .findByAccountAndWeek(idAccount, idPerformance)
                .map(PerformanceData::from)
                .orElse(null);
    }
    
    public List<PerformanceData> getPerformances(
            String idAccount,
            Integer year,
            PeriodEnum period
    ) {

        if (PeriodEnum.ALL.equals(period)) {
            return performanceRepository
                    .findByAccount(idAccount)
                    .stream()
                    .map(PerformanceData::from)
                    .toList();
        }

        if (period.isMonth()) {
            int month = Integer.parseInt(period.getId());
            return getMonthlyPerformance(idAccount, year, month);
        }

        if (period.isQuarter()) {
            return getQuarterPerformance(idAccount, year, period);
        }

        throw new IllegalArgumentException("Periodo non gestito: " + period);
    }
    
    public List<PerformanceData> getQuarterPerformance(
            String idAccount,
            int year,
            PeriodEnum period
    ) {

        int startMonth;
        int endMonth;

        switch (period) {
            case Q1:
                startMonth = 1;
                endMonth = 3;
                break;
            case Q2:
                startMonth = 4;
                endMonth = 6;
                break;
            case Q3:
                startMonth = 7;
                endMonth = 9;
                break;
            case Q4:
                startMonth = 10;
                endMonth = 12;
                break;
            default:
                throw new IllegalArgumentException("Trimestre non valido: " + period);
        }

        return performanceRepository
                .findByAccountAndYear(idAccount, year)
                .stream()
                .map(PerformanceData::from)
                .filter(p -> p.getMonth() >= startMonth && p.getMonth() <= endMonth)
                .toList();
    }
}
