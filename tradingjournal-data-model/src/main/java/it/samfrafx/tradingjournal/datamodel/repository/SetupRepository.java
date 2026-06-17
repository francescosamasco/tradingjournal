package it.samfrafx.tradingjournal.datamodel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.samfrafx.tradingjournal.datamodel.data.Setup;

@Repository
public interface SetupRepository extends JpaRepository<Setup, String> {

	List<Setup> findAllByOrderByDescriptionAsc();

	List<Setup> findByDescriptionIgnoreCase(String description);

	List<Setup> findByStrategyIdAndDescriptionIgnoreCase(String strategyId, String description);

	@Query("""
			select distinct s.description
			from Setup s
			where s.strategyId = :strategyId
			order by s.description
			""")
	List<String> findDistinctDescriptionsByStrategyId(@Param("strategyId") String strategyId);
}