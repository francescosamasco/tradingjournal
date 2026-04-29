package it.samfrafx.tradingjournal.datamodel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.samfrafx.tradingjournal.datamodel.data.Performance;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, String> {

    // 🔹 performance per account
    List<Performance> findByIdAccount(String idAccount);

}