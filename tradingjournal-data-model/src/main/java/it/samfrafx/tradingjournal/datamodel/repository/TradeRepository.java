package it.samfrafx.tradingjournal.datamodel.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.samfrafx.tradingjournal.datamodel.data.Trade;

@Repository
public interface TradeRepository extends JpaRepository<Trade, String> {

	@Query("""
			    SELECT t
			    FROM Trade t
			    WHERE t.idAccount = :accountId
			    ORDER BY t.dateOpen DESC
			""")
	List<Trade> findAllByAccount(String accountId);


	@Query("""
			    SELECT t
			    FROM Trade t
			    WHERE t.idAccount = :accountId
			    AND t.dateOpen BETWEEN :start AND :end
			    ORDER BY t.dateOpen ASC
			""")
	List<Trade> findByAccountAndPeriod(
			String accountId,
			LocalDateTime start,
			LocalDateTime end
			);


}