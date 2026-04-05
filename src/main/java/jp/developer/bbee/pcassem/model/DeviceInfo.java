package jp.developer.bbee.pcassem.model;

import org.springframework.lang.NonNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public record DeviceInfo (String id, String device, String url, String name, String imgurl, String detail,
                          Integer price, Integer rank, int flag1, int flag2,
                          String releasedate, Integer invisible, LocalDateTime createddate, LocalDateTime lastupdate) {

    @NonNull
    public static DeviceInfo from(@NonNull Map<String, Object> result) {
        return new DeviceInfo(
                (String) result.get("id"), (String) result.get("device"), (String) result.get("url"),
                (String) result.get("name"), (String) result.get("imgurl"), (String) result.get("detail"),
                (Integer) result.get("price"), (Integer) result.get("rank"),
                Optional.ofNullable((Integer) result.get("flag1")).orElse(0),
                Optional.ofNullable((Integer) result.get("flag2")).orElse(0),
                result.get("releasedate").toString(), (Integer) result.get("invisible"),
                ((Timestamp) result.get("createddate")).toLocalDateTime(),
                ((Timestamp) result.get("lastupdate")).toLocalDateTime()
        );
    }
}