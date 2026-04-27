package it.samfrafx.tradingjournal.datamodel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.samfrafx.tradingjournal.datamodel.data.Performance;

public interface PerformanceRepository extends JpaRepository<Performance, String> {

    // 🔹 performance per account
    List<Performance> findByIdAccount(String idAccount);

}