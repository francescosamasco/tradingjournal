package it.samfrafx.tradingjournal.datamodel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.samfrafx.tradingjournal.datamodel.data.Account;

public interface AccountRepository extends JpaRepository<Account, String> {

}