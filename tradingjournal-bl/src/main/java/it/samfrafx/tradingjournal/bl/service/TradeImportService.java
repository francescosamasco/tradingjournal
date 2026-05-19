package it.samfrafx.tradingjournal.bl.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.samfrafx.tradingjournal.bl.data.TradeData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeImportService {

    private final TradeService tradeService;
    private final PerformanceService performanceService;

    @Transactional
    public void importFromExcelText(String accountId, String rawText) {

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account obbligatorio");
        }

        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("Testo import obbligatorio");
        }

        String[] rows = rawText.split("\\R");

        LocalDateTime firstDate = null;

        for (int i = 1; i < rows.length; i++) {

            String row = rows[i];

            if (row == null || row.isBlank()) {
                continue;
            }

            String[] c = row.split("\\t", -1);

            TradeData data = new TradeData();

            data.setAccountId(accountId);
            data.setTipoMovimento("trade");

            data.setAsset(get(c, 0));
            data.setEsito(normalizeEsito(get(c, 1)));

            LocalDate date = LocalDate.parse(
                    get(c, 2),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );

            LocalTime time = LocalTime.parse(
                    normalizeTime(get(c, 3)),
                    DateTimeFormatter.ofPattern("H:mm")
            );

            LocalDateTime dateOpen = LocalDateTime.of(date, time);
            data.setDateOpen(dateOpen);

            if (firstDate == null || dateOpen.isBefore(firstDate)) {
                firstDate = dateOpen;
            }

            data.setPosizione(get(c, 4));
            data.setStruttura(get(c, 5));
            data.setSetup(get(c, 6));

            data.setProfit(parseItalianBigDecimal(get(c, 7)));

            data.setConfluenze(get(c, 9));
            data.setTags(get(c, 12));

            data.setVotoSetup(get(c, 14));
            data.setAnalisi(get(c, 15));
            data.setNote(get(c, 16));

            tradeService.save(data);
        }

        if (firstDate != null) {
            performanceService.recalculateFromTradeDate(
                    accountId,
                    firstDate
            );
        }
    }

    private String get(String[] values, int index) {
        if (values == null || index >= values.length) {
            return null;
        }

        String value = values[index];

        return value != null && !value.isBlank()
                ? value.trim()
                : null;
    }

    private String normalizeEsito(String esito) {

        if (esito == null) {
            return null;
        }

        return switch (esito.trim().toUpperCase()) {
            case "TP" -> "WIN";
            case "SL" -> "LOSS";
            case "BE" -> "BE";
            case "MISSED" -> "MISS";
            default -> esito.trim().toUpperCase();
        };
    }

    private String normalizeTime(String time) {

        if (time == null || time.isBlank()) {
            throw new IllegalArgumentException("Orario obbligatorio");
        }

        return time.trim();
    }

    private BigDecimal parseItalianBigDecimal(String value) {

        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }

        String normalized = value
                .trim()
                .replace(".", "")
                .replace(",", ".");

        return new BigDecimal(normalized)
                .setScale(2, RoundingMode.HALF_UP);
    }
}