package com.drivesense.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static final DateTimeFormatter HUMAN_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    public static final DateTimeFormatter ISO_SHORT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    public static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static String formatHuman(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(HUMAN_FORMATTER);
    }

    public static String formatDateOnly(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATE_ONLY_FORMATTER);
    }

    public static long calculateRentalDays(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            return 1;
        }
        Duration duration = Duration.between(start, end);
        long hours = duration.toHours();
        if (duration.toMinutesPart() > 0) {
            hours += 1;
        }
        long days = hours / 24;
        if (hours % 24 > 0) {
            days += 1;
        }
        return Math.max(1, days);
    }

    public static long calculateRentalHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            return 1;
        }
        Duration duration = Duration.between(start, end);
        long hours = duration.toHours();
        if (duration.toMinutesPart() > 0) {
            hours += 1;
        }
        return Math.max(1, hours);
    }
}
