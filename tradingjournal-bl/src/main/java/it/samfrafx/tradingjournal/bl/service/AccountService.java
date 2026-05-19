package it.samfrafx.tradingjournal.bl.service;

import java.util.List;

import org.springframework.stereotype.Service;

import it.samfrafx.tradingjournal.bl.data.AccountData;
import it.samfrafx.tradingjournal.datamodel.repository.AccountRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountData findById(String accountId) {

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account obbligatorio");
        }

        return accountRepository.findById(accountId)
                .map(AccountData::from)
                .orElseThrow(() -> new IllegalStateException(
                        "Account non trovato: " + accountId
                ));
    }

    public List<AccountData> findAll() {

        return accountRepository.findAll()
                .stream()
                .map(AccountData::from)
                .toList();
    }

    public AccountData getDefaultAccount() {

        return accountRepository.findAll()
                .stream()
                .findFirst()
                .map(AccountData::from)
                .orElseThrow(() -> new IllegalStateException(
                        "Nessun account configurato"
                ));
    }
}