package it.samfrafx.tradingjournal.bl;

import java.util.LinkedHashSet;
import java.util.Set;

public enum PeriodEnum {

    ALL("all"),
    Q1("Q1"),
    Q2("Q2"),
    Q3("Q3"),
    Q4("Q4"),
    GENNAIO("1"),
    FEBBRAIO("2"),
    MARZO("3"),
    APRILE("4"),
    MAGGIO("5"),
    GIUGNO("6"),
    LUGLIO("7"),
    AGOSTO("8"),
    SETTEMBRE("9"),
    OTTOBRE("10"),
    NOVEMBRE("11"),
    DICEMBRE("12");

    private final String id;

    PeriodEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static PeriodEnum getEnum(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Periodo non valorizzato");
        }

        for (PeriodEnum period : values()) {
            if (period.id.equalsIgnoreCase(value)) {
                return period;
            }
        }

        throw new IllegalArgumentException("Periodo non valido: " + value);
    }

    public static Set<PeriodEnum> getPeriods(String id) {
        PeriodEnum periodEnum = getEnum(id);
        Set<PeriodEnum> periods = new LinkedHashSet<>();

        switch (periodEnum) {
            case ALL -> {
                periods.addAll(getPeriods(Q1.id));
                periods.addAll(getPeriods(Q2.id));
                periods.addAll(getPeriods(Q3.id));
                periods.addAll(getPeriods(Q4.id));
            }
            case Q1 -> {
                periods.add(GENNAIO);
                periods.add(FEBBRAIO);
                periods.add(MARZO);
            }
            case Q2 -> {
                periods.add(APRILE);
                periods.add(MAGGIO);
                periods.add(GIUGNO);
            }
            case Q3 -> {
                periods.add(LUGLIO);
                periods.add(AGOSTO);
                periods.add(SETTEMBRE);
            }
            case Q4 -> {
                periods.add(OTTOBRE);
                periods.add(NOVEMBRE);
                periods.add(DICEMBRE);
            }
            default -> periods.add(periodEnum);
        }

        return periods;
    }

    public boolean isMonth() {
        return switch (this) {
            case GENNAIO, FEBBRAIO, MARZO,
                 APRILE, MAGGIO, GIUGNO,
                 LUGLIO, AGOSTO, SETTEMBRE,
                 OTTOBRE, NOVEMBRE, DICEMBRE -> true;
            default -> false;
        };
    }
    
    public boolean isQuarter() {
        return switch (this) {
            case Q1, Q2, Q3, Q4 -> true;
            default -> false;
        };
    }
}