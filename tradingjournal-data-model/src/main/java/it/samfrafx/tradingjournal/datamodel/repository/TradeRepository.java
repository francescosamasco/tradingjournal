package it.samfrafx.tradingjournal.datamodel.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.samfrafx.tradingjournal.datamodel.data.Trade;

@Repository
public interface TradeRepository extends JpaRepository<Trade, String> {

	@Query("""
		    SELECT t
		    FROM Trade t
		    WHERE t.idAccount = :accountId
		    ORDER BY t.dateOpen ASC
		""")
		List<Trade> findByAccount(
		        @Param("accountId") String accountId
		);
	
	Optional<Trade> findTopByIdAccountAndDateOpenBeforeOrderByDateOpenDesc(
	        String idAccount,
	        LocalDateTime dateOpen
	);
	
	
	Optional<Trade> findTopByIdAccountAndDateOpenBeforeAndIdTradeNotOrderByDateOpenDesc(
	        String idAccount,
	        LocalDateTime dateOpen,
	        String idTrade
	);
	
	@Query("""
		    SELECT t
		    FROM Trade t
		    WHERE t.idAccount = :accountId
		      AND t.dateOpen > :dateOpen
		    ORDER BY t.dateOpen ASC
		""")
		List<Trade> findNextTrades(
		        @Param("accountId") String accountId,
		        @Param("dateOpen") LocalDateTime dateOpen
		);
	
	
	
	
	
	/* *****************************************  */
	
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

	
	@Query("""
			SELECT COALESCE(SUM(t.profit), 0)
			FROM Trade t
			WHERE t.idAccount = :accountId
			AND t.dateOpen < :dateTime
		""")
		BigDecimal sumProfitLossBeforeDateTime(
				@Param("accountId") String accountId,
				@Param("dateTime") LocalDateTime dateTime
		);
	@Query("""
		       SELECT COALESCE(SUM(t.profit), 0)
		       FROM Trade t
		       WHERE t.idAccount = :accountId
		       AND t.dateOpen < :dateTime
		       AND t.idTrade <> :idTrade
		       """)
		BigDecimal sumProfitLossBeforeDateTimeExcludingTrade(
		        String accountId,
		        LocalDateTime dateTime,
		        String idTrade
		);

	
	@Query("""
		       SELECT COALESCE(SUM(t.profit), 0)
		       FROM Trade t
		       WHERE t.idAccount = :accountId
		       AND t.dateOpen >= :start
		       AND t.dateOpen < :end
		       """)
		BigDecimal sumProfitLossBetweenDateTime(
		        @Param("accountId") String accountId,
		        @Param("start") LocalDateTime start,
		        @Param("end") LocalDateTime end
		);
}