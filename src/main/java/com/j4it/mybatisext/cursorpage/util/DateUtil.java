package com.j4it.mybatisext.cursorpage.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @author meanfu
 * @date 2019/07/04
 */
public class DateUtil {

    public static final DateTimeFormatter STANDARD_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter SHORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter SIMPLE_SHORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * String转换为LocalDate
     * @param dateStr
     * @param dateTimeFormatter
     * @return
     */
    public static Optional<LocalDate> stringToLocalDate(String dateStr, DateTimeFormatter dateTimeFormatter) {
        try {
            return Optional.of(LocalDate.parse(dateStr, dateTimeFormatter));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    public static LocalDateTime parseDateString(String dateStr, DateTimeFormatter dateTimeFormatter) {
        Optional<LocalDateTime> localDateOptional = Optional.of(LocalDateTime.parse(dateStr, dateTimeFormatter));
        if (localDateOptional.isPresent()) {
            return localDateOptional.get();
        } else {
            return null;
        }
    }

    /**
     * Date转换为LocalDate
     * @param date
     * @return
     */
    public static LocalDate dateToLocalDate(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }

        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * LocalDate转换为Date
     * @param localDate
     * @return
     */
    public static Date localDateToDate(LocalDate localDate) {
        if (Objects.isNull(localDate)) {
            return null;
        }

        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * LocalDateTime转Date
     * @param localDateTime
     * @return
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return null;
        }
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * convert java.util.Date to java.time.LocalDateTime
     *
     * @param dateToConvert
     * @return
     */
    public static LocalDateTime dateToLocalDateTime(Date dateToConvert) {
        if (Objects.isNull(dateToConvert)){
            return null;
        }

        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 获取时间戳
     * @param localDateTime
     * @return
     */
    public static long getEpochMilli(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return 0L;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
