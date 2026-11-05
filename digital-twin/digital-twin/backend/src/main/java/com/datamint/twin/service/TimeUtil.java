package com.datamint.twin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** The Excel only has a date + shift, so a reading time is derived from shift start (A 06:00, B 14:00, C 22:00). */
public final class TimeUtil {
    private TimeUtil() {}

    public static int shiftHour(String shift) {
        if (shift == null) return 0;
        return switch (shift) { case "A" -> 6; case "B" -> 14; case "C" -> 22; default -> 0; };
    }

    public static LocalDateTime readingTime(LocalDate date, String shift, String componentId) {
        int seq = 0;
        try { seq = Integer.parseInt(componentId.replaceAll("\\D", "")); } catch (Exception ignored) { }
        return date.atTime(shiftHour(shift), 0).plusMinutes(seq % 480);
    }
}
