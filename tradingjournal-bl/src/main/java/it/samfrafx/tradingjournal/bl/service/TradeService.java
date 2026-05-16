package it.samfrafx.tradingjournal.bl.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.Config;
import it.samfrafx.tradingjournal.bl.data.TradeData;
import it.samfrafx.tradingjournal.bl.data.TradingConfig;
import it.samfrafx.tradingjournal.bl.data.enums.SetupEnum;
import it.samfrafx.tradingjournal.bl.data.enums.StrutturaEnum;
import it.samfrafx.tradingjournal.bl.data.enums.VotoSetupEnum;
import it.samfrafx.tradingjournal.datamodel.data.Account;
import it.samfrafx.tradingjournal.datamodel.data.Trade;
import it.samfrafx.tradingjournal.datamodel.repository.AccountRepository;
import it.samfrafx.tradingjournal.datamodel.repository.TradeRepository;

@Service
public class TradeService {

	private final AccountRepository accountRepository;
    private final TradeRepository tradeRepository;

    public TradeService(
            TradeRepository tradeRepository,
            AccountRepository accountRepository
    ) {
        this.tradeRepository = tradeRepository;
        this.accountRepository = accountRepository;
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

        //if (PeriodEnum.ALL.equals(period)) {
        //    return getAllTrades(accountId);
        //}

        if (period.isMonth()) {
            int month = Integer.parseInt(period.getId());
            return getTradesByMonth(accountId, year, month);
        }

        return getTradesByPeriod(accountId, year, period);
    }
    
    public TradeData findById(String idTrade) {

        Trade trade = tradeRepository.findById(idTrade)
                .orElseThrow(() -> new IllegalArgumentException("Trade non trovato"));

        return TradeData.from(trade);
    }
    
    public void deleteById(String idTrade) {

        Trade trade = tradeRepository.findById(idTrade)
                .orElseThrow(() -> new IllegalArgumentException("Trade non trovato"));

        String accountId = trade.getIdAccount();
        LocalDateTime dateOpen = trade.getDateOpen();

        tradeRepository.delete(trade);

        //performanceService.recalculatePerformance(accountId, dateOpen);
    }
    
  

    private List<TradeData> getTradesByMonth(
            String accountId,
            Integer year,
            Integer month
    ) {

        LocalDateTime start = LocalDate.of(year, month, 1)
                .atStartOfDay();

        LocalDateTime end = LocalDate.of(year, month, 1)
                .withDayOfMonth(
                        LocalDate.of(year, month, 1).lengthOfMonth()
                )
                .atTime(LocalTime.MAX);

        List<Trade> trades = tradeRepository.findByAccountAndPeriod(
                accountId,
                start,
                end
        );

        return trades.stream()
                .map(TradeData::from)
                .toList();
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

       return trades.stream()
                .map(TradeData::from)
                .toList();
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
    
        
    
    /*********/
    public List<TradeData> findByAccountAndPeriod(
            String accountId,
            LocalDateTime start,
            LocalDateTime end
    ) {

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account obbligatorio");
        }

        if (start == null || end == null) {
            throw new IllegalArgumentException("Periodo obbligatorio");
        }

        return tradeRepository.findByAccountAndPeriod(
                accountId,
                start,
                end
        )
        .stream()
        .map(TradeData::from)
        .toList();
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

        trade.setTipoMovimento(
                data.getTipoMovimento() != null && !data.getTipoMovimento().isBlank()
                        ? data.getTipoMovimento()
                        : "trade"
        );

        trade.setDateOpen(data.getDateOpen());
        trade.setAsset(data.getAsset());
        trade.setEsito(data.getEsito());

        trade.setProfit(data.getProfit() != null ? data.getProfit() : BigDecimal.ZERO);
        trade.setRisk(data.getRisk() != null ? data.getRisk() : BigDecimal.ZERO);

        
       BigDecimal balance = calculateAccountBalanceForNewTrade(
                data.getAccountId(),
                data.getDateOpen(),
                trade.getProfit());
        
        trade.setAccountBalance( balance );

        trade.setPosizione(data.getPosizione());
        trade.setStruttura(data.getStruttura());
        trade.setSetup(data.getSetup());
        trade.setConfluenze(data.getConfluenze());

        VotoSetupEnum voto = VotoSetupEnum.NON_STRATEGIA;

        if (data.getVotoSetup() != null && !data.getVotoSetup().isBlank()) {
            voto = VotoSetupEnum.fromDescrizione(data.getVotoSetup());
        }

        trade.setVotoSetup(voto.getNumeric());

        trade.setTags(data.getTags());
        trade.setAnalisi(data.getAnalisi());
        trade.setNote(data.getNote());

        Trade saved = tradeRepository.save(trade);
        return TradeData.from(saved);
    }
    
    public TradeData update(TradeData data) {

        if (data == null || data.getIdTrade() == null || data.getIdTrade().isBlank()) {
            throw new IllegalArgumentException("Trade obbligatorio");
        }

        if (data.getDateOpen() == null) {
            throw new IllegalArgumentException("Data trade obbligatoria");
        }

        Trade trade = tradeRepository.findById(data.getIdTrade())
                .orElseThrow(() -> new IllegalArgumentException("Trade non trovato"));

        String oldAccountId = trade.getIdAccount();
        LocalDateTime oldDateOpen = trade.getDateOpen();

        trade.setTipoMovimento(
                data.getTipoMovimento() != null && !data.getTipoMovimento().isBlank()
                        ? data.getTipoMovimento()
                        : "trade"
        );

        trade.setDateOpen(data.getDateOpen());
        trade.setAsset(data.getAsset());
        trade.setEsito(data.getEsito());

        trade.setProfit(data.getProfit() != null ? data.getProfit() : BigDecimal.ZERO);
        trade.setRisk(data.getRisk() != null ? data.getRisk() : BigDecimal.ZERO);

        trade.setAccountBalance(
                calculateAccountBalanceForUpdateTrade(
                        trade.getIdAccount(),
                        trade.getDateOpen(),
                        trade.getIdTrade(),
                        trade.getProfit()
                )
        );

        trade.setPosizione(data.getPosizione());
        trade.setStruttura(data.getStruttura());
        trade.setSetup(data.getSetup());
        trade.setConfluenze(data.getConfluenze());
        trade.setVotoSetup(
                data.getVotoSetup() != null && !data.getVotoSetup().isBlank()
                        ? VotoSetupEnum.fromDescrizione(data.getVotoSetup()).getNumeric()
                        : VotoSetupEnum.NON_STRATEGIA.getNumeric()
        );


        trade.setTags(data.getTags());
        trade.setAnalisi(data.getAnalisi());
        trade.setNote(data.getNote());

        Trade saved = tradeRepository.save(trade);

        // performanceService.recalculatePerformance(oldAccountId, oldDateOpen);
        // performanceService.recalculatePerformance(saved.getIdAccount(), saved.getDateOpen());

        return TradeData.from(saved);
    }
    
    private BigDecimal calculateAccountBalanceForUpdateTrade(
            String accountId,
            LocalDateTime dateOpen,
            String idTrade,
            BigDecimal currentProfit
    ) {

        BigDecimal previousAccountBalance =
                tradeRepository
                        .findTopByIdAccountAndDateOpenBeforeAndIdTradeNotOrderByDateOpenDesc(
                                accountId,
                                dateOpen,
                                idTrade
                        )
                        .map(Trade::getAccountBalance)
                        .orElse(BigDecimal.ZERO);

        BigDecimal profit = currentProfit != null
                ? currentProfit
                : BigDecimal.ZERO;

        return previousAccountBalance.add(profit);
    }
    
    public BigDecimal calculateAccountBalanceForNewTrade(
            String accountId,
            LocalDateTime dateOpen,
            BigDecimal currentProfit
    ) {

        BigDecimal previousAccountBalance =
                tradeRepository
                        .findTopByIdAccountAndDateOpenBeforeOrderByDateOpenDesc(
                                accountId,
                                dateOpen
                        )
                        .map(Trade::getAccountBalance)
                        .orElseGet(() ->
                                accountRepository.findById(accountId)
                                        .map(Account::getInitialBalance)
                                        .orElse(BigDecimal.ZERO)
                        );

        BigDecimal profit = currentProfit != null
                ? currentProfit
                : BigDecimal.ZERO;

        return previousAccountBalance.add(profit);
    }
    
    
    
    
    
    
    
    
    
    
}