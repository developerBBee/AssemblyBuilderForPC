package jp.developer.bbee.pcassem.model;

import java.time.LocalDateTime;

public record UserAssem(String id, String deviceid, String device, String guestid,
                        LocalDateTime createddate, LocalDateTime lastupdate) {}
