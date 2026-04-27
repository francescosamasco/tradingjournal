package it.samfrafx.tradingjournal.datamodel.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.samfrafx.tradingjournal.datamodel.data.Trade;

public interface TradeRepository extends JpaRepository<Trade, String> {

    // 🔹 Tutti i trade di un account
    List<Trade> findByIdAccount(String idAccount);

    // 🔹 Trade per range date
    List<Trade> findByDateOpenBetween(LocalDateTime start, LocalDateTime end);

    // 🔹 Trade per account + range date (fondamentale per dashboard)
    List<Trade> findByIdAccountAndDateOpenBetween(
            String idAccount,
            LocalDateTime start,
            LocalDateTime end
    );

    // 🔹 Trade per esito (win/loss)
    List<Trade> findByEsito(String esito);

}