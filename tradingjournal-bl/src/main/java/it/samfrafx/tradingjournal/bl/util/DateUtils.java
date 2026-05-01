package it.samfrafx.tradingjournal.bl.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DateUtils {

    private DateUtils() {}

    public static LocalDateTime[] getStartEndOfWeek(int year, int month, int week) {

        WeekFields weekFields = WeekFields.of(Locale.ITALY);

        LocalDate firstDayOfWeek = LocalDate
                .of(year, 1, 4)
                .with(weekFields.weekOfYear(), week)
                .with(weekFields.dayOfWeek(), 1); // lunedì

        LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(6);

        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

        if (firstDayOfWeek.isBefore(firstDayOfMonth)) {
            firstDayOfWeek = firstDayOfMonth;
        }

        if (lastDayOfWeek.isAfter(lastDayOfMonth)) {
            lastDayOfWeek = lastDayOfMonth;
        }

        LocalDateTime start = firstDayOfWeek.atStartOfDay();
        LocalDateTime end = lastDayOfWeek.atTime(23, 59, 59);

        return new LocalDateTime[]{start, end};
    }
    
    public static int getCurrentWeekNumber() {
        return LocalDate.now()
                .get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    public static List<Integer> getWeekNumbersInMonth(int year, int month) {

        List<Integer> weeks = new ArrayList<>();

        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        WeekFields wf = WeekFields.ISO;
        LocalDate current = firstDay;
        Integer lastAddedWeek = null;

        while (!current.isAfter(lastDay)) {
            int week = current.get(wf.weekOfWeekBasedYear());
            if (!Integer.valueOf(week).equals(lastAddedWeek)) {
                weeks.add(week);
                lastAddedWeek = week;
            }
            current = current.plusDays(1);
        }
        return weeks;
    }
}