package it.samfrafx.tradingjournal.bl.data.chart;

import java.util.Map;

public class CalendarData {

    private Map<String, DayData> days;
    private Map<Integer, WeekData> weeks;

    public CalendarData() {
    }

    public CalendarData(Map<String, DayData> days, Map<Integer, WeekData> weeks) {
        this.days = days;
        this.weeks = weeks;
    }

    public Map<String, DayData> getDays() {
        return days;
    }

    public void setDays(Map<String, DayData> days) {
        this.days = days;
    }

    public Map<Integer, WeekData> getWeeks() {
        return weeks;
    }

    public void setWeeks(Map<Integer, WeekData> weeks) {
        this.weeks = weeks;
    }
}