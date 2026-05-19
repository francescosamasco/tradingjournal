package it.samfrafx.tradingjournal.datamodel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.samfrafx.tradingjournal.datamodel.data.Performance;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, String> {

	@Query("""
		    SELECT p
		    FROM Performance p
		    WHERE p.idAccount = :accountId
		      AND p.idPerformance >= :idPerformance
		    ORDER BY p.idPerformance ASC
		""")
		List<Performance> findFromPerformanceId(
		        @Param("accountId") String accountId,
		        @Param("idPerformance") String idPerformance
		);


		@Query("""
		    SELECT p
		    FROM Performance p
		    WHERE p.idAccount = :accountId
		      AND p.idPerformance LIKE :pattern
		    ORDER BY p.idPerformance ASC
		""")
		List<Performance> findByAccountAndMonth(
		        @Param("accountId") String accountId,
		        @Param("pattern") String pattern
		);

    // 🔹 singola settimana
    @Query("""
        SELECT p
        FROM Performance p
        WHERE p.idAccount = :idAccount
        AND p.idPerformance = :idPerformance
    """)
    Optional<Performance> findByAccountAndWeek(
            String idAccount,
            String idPerformance
    );
    
    @Query("SELECT p FROM Performance p WHERE p.idAccount = :idAccount")
    List<Performance> findByAccount(String idAccount);

    @Query("SELECT p FROM Performance p WHERE p.idAccount = :idAccount AND p.idPerformance LIKE CONCAT(:year, '-%')")
    List<Performance> findByAccountAndYear(
            @Param("idAccount") String idAccount,
            @Param("year") int year
    );
    
    @Query("""
    	    SELECT p
    	    FROM Performance p
    	    WHERE p.idAccount = :idAccount
    	    AND p.idPerformance = :idPerformance
    	""")
    	Optional<Performance> findByAccountAndIdPerformance(
    	        @Param("idAccount") String idAccount,
    	        @Param("idPerformance") String idPerformance
    	);
    
    
    @Query("""
    	    SELECT p
    	    FROM Performance p
    	    WHERE p.idAccount = :idAccount
    	    AND p.idPerformance < :idPerformance
    	    ORDER BY p.idPerformance DESC
    	""")
    	List<Performance> findPreviousPerformances(
    	        @Param("idAccount") String idAccount,
    	        @Param("idPerformance") String idPerformance
    	);
}