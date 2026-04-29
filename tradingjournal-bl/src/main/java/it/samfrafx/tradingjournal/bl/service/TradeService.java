package it.samfrafx.tradingjournal.bl.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.TradeData;
import it.samfrafx.tradingjournal.datamodel.data.Trade;
import it.samfrafx.tradingjournal.datamodel.repository.TradeRepository;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public List<TradeData> getTradesByAccountAndPeriod(String accountId, PeriodEnum period, Integer year) {

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account id obbligatorio");
        }

        if (period == null) {
            throw new IllegalArgumentException("Periodo obbligatorio");
        }

        List<Trade> trades;

        if (PeriodEnum.ALL.equals(period)) {
            trades = tradeRepository.findAllByAccount(accountId);
        } else {
            if (year == null) {
                throw new IllegalArgumentException("Anno obbligatorio");
            }

            LocalDateTime start;
            LocalDateTime end;

            switch (period) {
                case Q1:
                    start = LocalDate.of(year, 1, 1).atStartOfDay();
                    end = LocalDate.of(year, 3, 31).atTime(23, 59, 59);
                    break;

                case Q2:
                    start = LocalDate.of(year, 4, 1).atStartOfDay();
                    end = LocalDate.of(year, 6, 30).atTime(23, 59, 59);
                    break;

                case Q3:
                    start = LocalDate.of(year, 7, 1).atStartOfDay();
                    end = LocalDate.of(year, 9, 30).atTime(23, 59, 59);
                    break;

                case Q4:
                    start = LocalDate.of(year, 10, 1).atStartOfDay();
                    end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
                    break;

                default:
                    int month = Integer.parseInt(period.getId());

                    LocalDate firstDay = LocalDate.of(year, month, 1);
                    LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

                    start = firstDay.atStartOfDay();
                    end = lastDay.atTime(23, 59, 59);
                    break;
            }

            trades = tradeRepository.findByAccountAndPeriod(accountId, start, end);
        }

        return trades.stream()
                .map(TradeData::from)
                .toList();
    }
}