package it.samfrafx.tradingjournal.bl.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.Config;
import it.samfrafx.tradingjournal.bl.data.PerformanceData;
import it.samfrafx.tradingjournal.bl.data.TradeData;
import it.samfrafx.tradingjournal.bl.data.TradingConfig;
import it.samfrafx.tradingjournal.bl.data.enums.SetupEnum;
import it.samfrafx.tradingjournal.bl.data.enums.StrutturaEnum;
import it.samfrafx.tradingjournal.bl.data.enums.VotoSetupEnum;
import it.samfrafx.tradingjournal.bl.util.DateUtils;
import it.samfrafx.tradingjournal.datamodel.data.Trade;
import it.samfrafx.tradingjournal.datamodel.repository.TradeRepository;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final PerformanceService performanceService;

    public TradeService(
            TradeRepository tradeRepository,
            PerformanceService performanceService
    ) {
        this.tradeRepository = tradeRepository;
        this.performanceService = performanceService;
    }

    public List<TradeData> getTrades(String accountId, Integer year, PeriodEnum period) {

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account id obbligatorio");
        }

        if (period == null) {
            throw new IllegalArgumentException("Periodo obbligatorio");
        }

        if (!PeriodEnum.ALL.equals(period) && year == null) {
            throw new IllegalArgumentException("Anno obbligatorio");
        }

        if (PeriodEnum.ALL.equals(period)) {
            return getAllTrades(accountId);
        }

        if (period.isMonth()) {
            return getTradesByMonth(accountId, year, period);
        }

        return getTradesByPeriod(accountId, year, period);
    }

    private List<TradeData> getAllTrades(String accountId) {

        List<Trade> trades = tradeRepository.findAllByAccount(accountId);

        BigDecimal runningBalance = new BigDecimal("25000.00");

        return buildTradeDataList(trades, runningBalance);
    }

    private List<TradeData> getTradesByMonth(
            String accountId,
            Integer year,
            PeriodEnum period
    ) {

        int month = Integer.parseInt(period.getId());

        List<PerformanceData> performances =
                performanceService.getMonthlyPerformance(accountId, year, month);

        List<TradeData> result = new ArrayList<>();

        for (PerformanceData performance : performances) {

            LocalDateTime[] range = DateUtils.getStartEndOfWeek( performance.getYear(), performance.getMonth(), performance.getWeek()
            );

            LocalDateTime start = range[0];
            LocalDateTime end = range[1];

            List<Trade> weeklyTrades =
                    tradeRepository.findByAccountAndPeriod(accountId, start, end);

            BigDecimal runningBalance = performance.getBilancioIniziale();

            result.addAll(buildTradeDataList(weeklyTrades, runningBalance));
        }

        return result;
    }

    private List<TradeData> getTradesByPeriod(
            String accountId,
            Integer year,
            PeriodEnum period
    ) {

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
                throw new IllegalArgumentException("Periodo non gestito: " + period);
        }

        List<Trade> trades =
                tradeRepository.findByAccountAndPeriod(accountId, start, end);

        BigDecimal runningBalance = new BigDecimal("25000.00");

        return buildTradeDataList(trades, runningBalance);
    }

    private List<TradeData> buildTradeDataList(
            List<Trade> trades,
            BigDecimal runningBalance
    ) {

        List<TradeData> result = new ArrayList<>();

        if (runningBalance == null) {
            runningBalance = BigDecimal.ZERO;
        }

        for (Trade trade : trades) {
            TradeData dto = TradeData.from(trade, runningBalance);
            result.add(dto);

            runningBalance = dto.getCurrentBalance();
        }

        return result;
    }
    
    public BigDecimal sumProfitLossBeforeDateTime(
    		String accountId,
    		LocalDateTime dateTime) {

    	BigDecimal total = tradeRepository.sumProfitLossBeforeDateTime(
    			accountId,
    			dateTime
    	);

    	return total != null ? total : BigDecimal.ZERO;
    }
    
    public List<String> calculateConfluenze(String strutturaId, String setupId) {

        StrutturaEnum struttura = StrutturaEnum.fromDescrizione(strutturaId);
        SetupEnum setup = SetupEnum.fromDescrizione(setupId);

        return Optional.ofNullable(TradingConfig.CONFIG.get(struttura))
                .map(m -> m.get(setup))
                .map(Config::getConfluenze)
                .orElse(List.of());
    }
    
    public VotoSetupEnum getVotoSetupEnum(String strutturaId, String setupId, String confluenze) {

        StrutturaEnum struttura = StrutturaEnum.fromDescrizione(strutturaId);
        SetupEnum setup = SetupEnum.fromDescrizione(setupId);

        Config config = Optional.ofNullable(TradingConfig.CONFIG.get(struttura))
                .map(m -> m.get(setup))
                .orElse(null);

        if (config == null || confluenze == null || confluenze.isBlank()) {
            return VotoSetupEnum.NON_STRATEGIA;
        }

        String c = confluenze.toLowerCase();

        boolean match = config.getConfluenze().stream()
                .allMatch(c::contains);

        return match ? config.getVoto() : VotoSetupEnum.BASSO;
    }
    
    
}