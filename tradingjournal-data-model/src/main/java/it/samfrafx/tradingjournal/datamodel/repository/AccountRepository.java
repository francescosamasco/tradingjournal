package it.samfrafx.tradingjournal.datamodel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.samfrafx.tradingjournal.datamodel.data.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

}