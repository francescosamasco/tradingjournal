package it.samfrafx.tradingjournal.bl.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.samfrafx.tradingjournal.bl.PeriodEnum;
import it.samfrafx.tradingjournal.bl.data.TradeData;
import it.samfrafx.tradingjournal.bl.data.enums.TipoMovimentoEnum;
import it.samfrafx.tradingjournal.bl.data.enums.TipoTrade;
import it.samfrafx.tradingjournal.bl.data.enums.VotoSetupEnum;
import it.samfrafx.tradingjournal.datamodel.data.Account;
import it.samfrafx.tradingjournal.datamodel.data.Setup;
import it.samfrafx.tradingjournal.datamodel.data.Trade;
import it.samfrafx.tradingjournal.datamodel.repository.AccountRepository;
import it.samfrafx.tradingjournal.datamodel.repository.SetupRepository;
import it.samfrafx.tradingjournal.datamodel.repository.TradeRepository;

@Service
public class TradeService {

	private final AccountRepository accountRepository;
    private final TradeRepository tradeRepository;
    private final SetupRepository setupRepository;
    public TradeService(
            TradeRepository tradeRepository,
            AccountRepository accountRepository,
            SetupRepository setupRepository
    ) {
        this.tradeRepository = tradeRepository;
        this.accountRepository = accountRepository;
        this.setupRepository = setupRepository;
    }

    public List<TradeData> getTrades(String accountId, Integer year, PeriodEnum period, boolean strategy) {

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
            return getTradesByMonth(accountId, year, month, strategy);
        }
        
        return null;
       // return getTradesByPeriod(accountId, year, period);
    }
    
    public TradeData findById(String idTrade) {

        Trade trade = tradeRepository.findById(idTrade)
                .orElseThrow(() -> new IllegalArgumentException("Trade non trovato"));

        return TradeData.from(trade);
    }
    
    @Transactional
    public void deleteById(String idTrade) {

        Trade trade = tradeRepository.findById(idTrade)
                .orElseThrow(() -> new IllegalArgumentException("Trade non trovato"));

        String accountId = trade.getIdAccount();
        LocalDateTime dateOpen = trade.getDateOpen();

        tradeRepository.delete(trade);

        BigDecimal previousBalance =
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

        recalculateNextTradesBalance(
                accountId,
                dateOpen.minusNanos(1),
                previousBalance
        );
    }
    
  

    private List<TradeData> getTradesByMonth(
            String accountId,
            Integer year,
            Integer month, 
            boolean strategy
    ) {

        LocalDateTime start = LocalDate.of(year, month, 1)
                .atStartOfDay();

        LocalDateTime end = LocalDate.of(year, month, 1)
                .withDayOfMonth(
                        LocalDate.of(year, month, 1).lengthOfMonth()
                )
                .atTime(LocalTime.MAX);

        Integer tipoTrade = strategy ? 1 : 0;
        
        List<Trade> trades = tradeRepository.findByAccountAndPeriodAndTipoTrade(
                accountId,
                start,
                end,
                tipoTrade
        );

        return trades.stream()
                .map(TradeData::from)
                .toList();
    }

   //private List<TradeData> getTradesByPeriod(
   //        String accountId,
   //        Integer year,
   //        PeriodEnum period
   //) {
   //
   //    LocalDateTime start;
   //    LocalDateTime end;
   //
   //    switch (period) {
   //
   //        case Q1:
   //            start = LocalDate.of(year, 1, 1).atStartOfDay();
   //            end = LocalDate.of(year, 3, 31).atTime(23, 59, 59);
   //            break;
   //
   //        case Q2:
   //            start = LocalDate.of(year, 4, 1).atStartOfDay();
   //            end = LocalDate.of(year, 6, 30).atTime(23, 59, 59);
   //            break;
   //
   //        case Q3:
   //            start = LocalDate.of(year, 7, 1).atStartOfDay();
   //            end = LocalDate.of(year, 9, 30).atTime(23, 59, 59);
   //            break;
   //
   //        case Q4:
   //            start = LocalDate.of(year, 10, 1).atStartOfDay();
   //            end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
   //            break;
   //
   //        default:
   //            throw new IllegalArgumentException("Periodo non gestito: " + period);
   //    }
   //
   //    List<Trade> trades =
   //            tradeRepository.findByAccountAndPeriod(accountId, start, end);
   //
   //   return trades.stream()
   //            .map(TradeData::from)
   //            .toList();
   //}

    public BigDecimal sumProfitLossBeforeDateTime(
    		String accountId,
    		LocalDateTime dateTime) {

    	BigDecimal total = tradeRepository.sumProfitLossBeforeDateTime(
    			accountId,
    			dateTime
    	);

    	return total != null ? total : BigDecimal.ZERO;
    }
    
    public List<String> calculateConfluenze(String accountId, String setupId) {

        if (accountId == null || accountId.isBlank()
                || setupId == null || setupId.isBlank()) {
            return List.of();
        }

        Account account = accountRepository.findById(accountId)
                .orElse(null);

        if (account == null || account.getStrategyId() == null || account.getStrategyId().isBlank()) {
            return List.of();
        }

        String strategyId = account.getStrategyId();

        Optional<Setup> setupOpt = setupRepository
                .findByStrategyIdAndDescriptionIgnoreCase(strategyId, setupId)
                .stream()
                .findFirst();

        if (setupOpt.isPresent()) {

            String confluences = setupOpt.get().getConfluences();

            if (confluences != null && !confluences.isBlank()) {
                return splitConfluenze(confluences);
            }
        }

        return tradeRepository
                .findDistinctConfluenzeByAccountAndSetup(accountId, setupId)
                .stream()
                .flatMap(confluenze -> splitConfluenze(confluenze).stream())
                .distinct()
                .collect(Collectors.toList());
    }
    
    private List<String> splitConfluenze(String confluences) {

        if (confluences == null || confluences.isBlank()) {
            return List.of();
        }

        return Arrays.stream(confluences.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
    
    public VotoSetupEnum getVotoSetupEnum(
            String setupId,
            String confluenze) {

        if (setupId == null || setupId.isBlank()
                || confluenze == null || confluenze.isBlank()) {
            return VotoSetupEnum.NON_STRATEGIA;
        }

        Set<String> selectedConfluences = Arrays.stream(confluenze.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Optional<Setup> setupOpt = setupRepository
                .findByDescriptionIgnoreCase(setupId)
                .stream()
                .filter(s -> {

                    Set<String> required = Arrays.stream(s.getConfluences().split(","))
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .collect(Collectors.toSet());

                    return selectedConfluences.containsAll(required);
                })
                .findFirst();

        if (setupOpt.isEmpty()) {
            return VotoSetupEnum.BASSO;
        }

        Integer voto = setupOpt.get().getVoto();

        if (voto == null) {
            return VotoSetupEnum.NON_STRATEGIA;
        }
	    return VotoSetupEnum.fromNumeric(voto);
    }
    
    
    public Integer calculateVotoSetup(String setupName, String selectedConfluences) {

        if (selectedConfluences == null || selectedConfluences.isBlank()) {
            return null;
        }

        List<String> confluenceList = Arrays.stream(selectedConfluences.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        return calculateVotoSetup(setupName, confluenceList);
    }
    
    public Integer calculateVotoSetup(String setupName, List<String> selectedConfluences) {

        if (setupName == null || setupName.isBlank()) {
            return null;
        }

        if (selectedConfluences == null || selectedConfluences.isEmpty()) {
            return null;
        }

        Set<String> selected = selectedConfluences.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return setupRepository.findByDescriptionIgnoreCase(setupName)
                .stream()
                .filter(setup -> matchesConfluences(setup, selected))
                .map(Setup::getVoto)
                .findFirst()
                .orElse(null);
    }

    private boolean matchesConfluences(Setup setup, Set<String> selectedConfluences) {

        if (setup.getConfluences() == null || setup.getConfluences().isBlank()) {
            return false;
        }

        Set<String> required = Arrays.stream(setup.getConfluences().split(","))
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return selectedConfluences.containsAll(required);
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

        return tradeRepository.findByAccountAndPeriodAndTipoTrade(
                accountId,
                start,
                end,
                0
        )
        .stream()
        .map(TradeData::from)
        .toList();
    }
    
    @Transactional
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

        trade.setTipoMovimento(TipoMovimentoEnum.TRADE.getNumeric());
        trade.setTipoTrade( TipoTrade.ACCOUNT.getNumeric());

        trade.setDateOpen(data.getDateOpen());
        trade.setAsset(data.getAsset());
        trade.setEsito(data.getEsito());

        trade.setProfit(data.getProfit() != null ? data.getProfit() : BigDecimal.ZERO);
        trade.setRisk(data.getRisk() != null ? data.getRisk() : BigDecimal.ZERO);

        BigDecimal balance = calculateAccountBalanceForNewTrade(
                data.getAccountId(),
                data.getDateOpen(),
                trade.getProfit()
        );

        trade.setAccountBalance(balance);

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
        trade.setErrors(data.getErrors());
        trade.setAnalisi(data.getAnalisi());
        trade.setNote(data.getNote());

        Trade saved = tradeRepository.save(trade);

        recalculateNextTradesBalance(
                saved.getIdAccount(),
                saved.getDateOpen(),
                saved.getAccountBalance()
        );

        return TradeData.from(saved);
    }
    
    public void recalculateNextTradesBalance(
            String accountId,
            LocalDateTime dateOpen,
            BigDecimal startingBalance
    ) {

        List<Trade> nextTrades = tradeRepository.findNextTrades(
                accountId,
                dateOpen
        );

        BigDecimal currentBalance = startingBalance != null
                ? startingBalance
                : BigDecimal.ZERO;

        for (Trade trade : nextTrades) {

            BigDecimal profit = trade.getProfit() != null
                    ? trade.getProfit()
                    : BigDecimal.ZERO;

            currentBalance = currentBalance.add(profit)
                    .setScale(2, RoundingMode.HALF_UP);

            trade.setAccountBalance(currentBalance);
        }

        tradeRepository.saveAll(nextTrades);
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

        trade.setDateOpen(data.getDateOpen());
        trade.setAsset(data.getAsset());
        trade.setEsito(data.getEsito());

        trade.setProfit(data.getProfit() != null ? data.getProfit() : BigDecimal.ZERO);
        trade.setRisk(data.getRisk() != null ? data.getRisk() : BigDecimal.ZERO);

        trade.setAccountBalance(
                calculateAccountBalanceForUpdateTrade(
                        trade.getIdAccount(),
                        trade.getIdTrade(),
                        trade.getDateOpen(),
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
        trade.setErrors(data.getErrors());
        trade.setAnalisi(data.getAnalisi());
        trade.setNote(data.getNote());

        Trade saved = tradeRepository.save(trade);

        recalculateNextTradesBalance(
                saved.getIdAccount(),
                saved.getDateOpen(),
                saved.getAccountBalance()
        );
        return TradeData.from(saved);
    }
    
    public BigDecimal calculateAccountBalanceForUpdateTrade(
            String accountId,
            String idTrade,
            LocalDateTime dateOpen,
            BigDecimal currentProfit
    ) {

        BigDecimal previousAccountBalance = tradeRepository
                .findTopByIdAccountAndDateOpenBeforeAndIdTradeNotOrderByDateOpenDesc(
                        accountId,
                        dateOpen,
                        idTrade
                )
                .map(Trade::getAccountBalance)
                .orElseGet(() ->
	                this.accountRepository.findById(accountId)
	                .map(Account::getInitialBalance)
	                .orElse(BigDecimal.ZERO)
                );

        BigDecimal profit = currentProfit != null
                ? currentProfit
                : BigDecimal.ZERO;

        return previousAccountBalance
                .add(profit)
                .setScale(2, RoundingMode.HALF_UP);
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
    
    
    public List<TradeData> findByAccount(String accountId) {

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException(
                    "Account obbligatorio"
            );
        }

        return tradeRepository
                .findByAccount(accountId)
                .stream()
                .map(TradeData::from)
                .toList();
    }
    
    
    //tas filtrare per strategia
    public List<String> getAllTags(String accountId) {
    	
    	Account account = this.accountRepository.findById(accountId)
    			 .orElseThrow(() -> new IllegalArgumentException("Trade non trovato"));
    	
        return tradeRepository.findByAccount( account.getUuid() ).stream()
        		.map( t -> t.getTags() )
                .filter(Objects::nonNull)
                .flatMap(tags -> Arrays.stream(tags.split(",")))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
  //tas filtrare per strategia
    public List<String> getAllErrors(String accountId) {
    	
    	Account account = this.accountRepository.findById(accountId)
    			 .orElseThrow(() -> new IllegalArgumentException("Trade non trovato"));
    	
        return tradeRepository.findByAccount( account.getUuid() ).stream()
        		.map( t -> t.getErrors() )
                .filter(Objects::nonNull)
                .flatMap(tags -> Arrays.stream(tags.split(",")))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    public List<String> getSetups(String accountId) {

        if (accountId == null || accountId.isBlank()) {
            return List.of();
        }

        Account account = accountRepository.findById(accountId)
                .orElse(null);

        if (account == null
                || account.getStrategyId() == null
                || account.getStrategyId().isBlank()) {
            return List.of();
        }

        return setupRepository.findDistinctDescriptionsByStrategyId(
                account.getStrategyId()
        );
    }
    
    
}