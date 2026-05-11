package it.samfrafx.tradingjournal.bl.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

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

    public TradeData update(TradeData data) {

        if (data == null || data.getIdTrade() == null || data.getIdTrade().isBlank()) {
            throw new IllegalArgumentException("Trade obbligatorio");
        }

        Trade trade = tradeRepository.findById(data.getIdTrade())
                .orElseThrow(() -> new IllegalArgumentException("Trade non trovato"));

        String oldAccountId = trade.getIdAccount();
        LocalDateTime oldDateOpen = trade.getDateOpen();

        trade.setDateOpen(data.getDateOpen());
        trade.setAsset(data.getAsset());
        trade.setEsito(data.getEsito());
        trade.setPosizione(data.getPosizione());

        trade.setStruttura(data.getStruttura());
        trade.setSetup(data.getSetup());
        trade.setConfluenze(data.getConfluenze());
        trade.setTags(data.getTags());

        trade.setProfit(data.getProfit());
        trade.setRisk(data.getRisk());
        trade.setNote(data.getNote());

        trade.setVotoSetup(
                VotoSetupEnum.fromDescrizione(data.getVotoSetup()).getNumeric()
        );

        int week = data.getDateOpen()
                .get(WeekFields.of(Locale.ITALY).weekOfWeekBasedYear());

        trade.setWeekN(week);

        Trade saved = tradeRepository.save(trade);

        performanceService.recalculatePerformance(oldAccountId, oldDateOpen);
        performanceService.recalculatePerformance(saved.getIdAccount(), saved.getDateOpen());

        return TradeData.from(saved, data.getAccountBalance());
    }
    
    
    public TradeData findById(String idTrade) {

        Trade trade = tradeRepository.findById(idTrade)
                .orElseThrow(() -> new IllegalArgumentException("Trade non trovato"));

        BigDecimal accountBalanceBeforeTrade = calculateAccountBalanceBeforeTrade(trade);

        return TradeData.from(trade, accountBalanceBeforeTrade);
    }
    
    public void deleteById(String idTrade) {

        Trade trade = tradeRepository.findById(idTrade)
                .orElseThrow(() -> new IllegalArgumentException("Trade non trovato"));

        String accountId = trade.getIdAccount();
        LocalDateTime dateOpen = trade.getDateOpen();

        tradeRepository.delete(trade);

        performanceService.recalculatePerformance(accountId, dateOpen);
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
    
    
    public TradeData save(TradeData data) {

        if (data == null) {
            throw new IllegalArgumentException("Trade obbligatorio");
        }

        if (data.getAccountId() == null || data.getAccountId().isBlank()) {
            throw new IllegalArgumentException("Account obbligatorio");
        }

        if (data.getDateOpen() == null) {
            throw new IllegalArgumentException("Data trade obbligatoria");
        }

        Trade trade = new Trade();

        trade.setIdTrade(UUID.randomUUID().toString());
        trade.setIdAccount(data.getAccountId());

        trade.setDateOpen(data.getDateOpen());
        trade.setAsset(data.getAsset());
        trade.setEsito(data.getEsito());
        trade.setPosizione(data.getPosizione());

        trade.setStruttura(data.getStruttura());
        trade.setSetup(data.getSetup());
        trade.setConfluenze(data.getConfluenze());
        trade.setTags(data.getTags());

        trade.setProfit(data.getProfit());
        trade.setRisk(data.getRisk());
        trade.setNote(data.getNote());
        
        trade.setVotoSetup( VotoSetupEnum.fromDescrizione(data.getVotoSetup()).getNumeric());
        
        int week = data.getDateOpen()
                .get(WeekFields.of(Locale.ITALY).weekOfWeekBasedYear());

        trade.setWeekN(week);

        Trade saved = tradeRepository.save(trade);
        
        this.performanceService.recalculatePerformance(
                saved.getIdAccount(),
                saved.getDateOpen());

        return TradeData.from(saved, data.getAccountBalance());
    }
    
    public BigDecimal calculateAccountBalancePreview(
            String accountId,
            LocalDateTime tradeDateTime,
            BigDecimal currentProfitLoss
    ) {

        LocalDateTime[] range = DateUtils.getStartEndOfWeek(
                tradeDateTime.getYear(),
                tradeDateTime.getMonthValue(),
                tradeDateTime.get(WeekFields.ISO.weekOfWeekBasedYear())
        );

        LocalDateTime weekStart = range[0];

        PerformanceData performance = null;

        try {
            performance = performanceService.findByAccountIdAndWeek(
                    accountId,
                    tradeDateTime.toLocalDate()
            );
        } catch (IllegalStateException ex) {
            performance = null;
        }

        BigDecimal bilancioBase = BigDecimal.ZERO;

        if (performance != null && performance.getBilancioIniziale() != null) {

            bilancioBase = performance.getBilancioIniziale();

        } else {

            PerformanceData previousPerformance =
                    performanceService.findClosestPreviousPerformance(
                            accountId,
                            tradeDateTime.toLocalDate()
                    );

            if (previousPerformance != null
                    && previousPerformance.getBilancioFinale() != null) {
                bilancioBase = previousPerformance.getBilancioFinale();
            }
        }

        BigDecimal previousTradesProfitLoss =
                tradeRepository.sumProfitLossBetweenDateTime(
                        accountId,
                        weekStart,
                        tradeDateTime
                );

        if (previousTradesProfitLoss == null) {
            previousTradesProfitLoss = BigDecimal.ZERO;
        }

        BigDecimal current = currentProfitLoss != null
                ? currentProfitLoss
                : BigDecimal.ZERO;

        return bilancioBase
                .add(previousTradesProfitLoss)
                .add(current);
    }
    
    private BigDecimal calculateAccountBalanceBeforeTrade(Trade trade) {

        PerformanceData performance = null;

        try {
            performance = performanceService.findByAccountIdAndWeek(
                    trade.getIdAccount(),
                    trade.getDateOpen().toLocalDate()
            );
        } catch (IllegalStateException ex) {
            performance = performanceService.findClosestPreviousPerformance(
                    trade.getIdAccount(),
                    trade.getDateOpen().toLocalDate()
            );
        }

        BigDecimal bilancioBase = BigDecimal.ZERO;

        if (performance != null && performance.getBilancioIniziale() != null) {
            bilancioBase = performance.getBilancioIniziale();
        }

        BigDecimal previousTradesProfitLoss =
                tradeRepository.sumProfitLossBeforeDateTimeExcludingTrade(
                        trade.getIdAccount(),
                        trade.getDateOpen(),
                        trade.getIdTrade()
                );

        if (previousTradesProfitLoss == null) {
            previousTradesProfitLoss = BigDecimal.ZERO;
        }

        return bilancioBase.add(previousTradesProfitLoss);
    }
    
    
    public BigDecimal calculateAccountBalancePreviewEdit(
            String accountId,
            String idTrade,
            LocalDateTime tradeDateTime,
            BigDecimal currentProfitLoss
    ) {

        PerformanceData performance = null;

        try {
            performance = performanceService.findByAccountIdAndWeek(
                    accountId,
                    tradeDateTime.toLocalDate()
            );
        } catch (IllegalStateException ex) {
            performance = performanceService.findClosestPreviousPerformance(
                    accountId,
                    tradeDateTime.toLocalDate()
            );
        }

        BigDecimal bilancioBase = BigDecimal.ZERO;

        if (performance != null && performance.getBilancioIniziale() != null) {
            bilancioBase = performance.getBilancioIniziale();
        }

        BigDecimal previousTradesProfitLoss =
                tradeRepository.sumProfitLossBeforeDateTimeExcludingTrade(
                        accountId,
                        tradeDateTime,
                        idTrade
                );

        if (previousTradesProfitLoss == null) {
            previousTradesProfitLoss = BigDecimal.ZERO;
        }

        BigDecimal current = currentProfitLoss != null
                ? currentProfitLoss
                : BigDecimal.ZERO;

        return bilancioBase
                .add(previousTradesProfitLoss)
                .add(current);
    }
    
}