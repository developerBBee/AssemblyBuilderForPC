package jp.developer.bbee.pcassem.model;

import org.springframework.lang.NonNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public record DeviceInfo (String id, String device, String url, String name, String imgurl, String detail,
                          Integer price, Integer rank, int flag1, int flag2,
                          String releasedate, Integer invisible, LocalDateTime createddate, LocalDateTime lastupdate) {

    private static final String DEFAULT_RELEASE_DATE = "20000101";
    private static final LocalDateTime DEFAULT_DATE_TIME = LocalDateTime.of(2000, 1, 1, 0, 0);

    @NonNull
    public static DeviceInfo from(@NonNull Map<String, Object> result) {
        return new DeviceInfo(
                (String) result.get("id"), (String) result.get("device"), (String) result.get("url"),
                (String) result.get("name"), (String) result.get("imgurl"), (String) result.get("detail"),
                toInteger(result.get("price")), toInteger(result.get("rank")),
                toIntOrDefault(result.get("flag1"), 0),
                toIntOrDefault(result.get("flag2"), 0),
                toReleaseDate(result.get("releasedate")), toInteger(result.get("invisible")),
                toLocalDateTime(result.get("createddate"), DEFAULT_DATE_TIME),
                toLocalDateTime(result.get("lastupdate"), DEFAULT_DATE_TIME)
        );
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(value.toString());
    }

    private static int toIntOrDefault(Object value, int defaultValue) {
        Integer integer = toInteger(value);
        return integer != null ? integer : defaultValue;
    }

    private static String toReleaseDate(Object value) {
        return value != null ? value.toString() : DEFAULT_RELEASE_DATE;
    }

    private static LocalDateTime toLocalDateTime(Object value, LocalDateTime defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.util.Date date) {
            return new Timestamp(date.getTime()).toLocalDateTime();
        }
        return Timestamp.valueOf(value.toString()).toLocalDateTime();
    }
}