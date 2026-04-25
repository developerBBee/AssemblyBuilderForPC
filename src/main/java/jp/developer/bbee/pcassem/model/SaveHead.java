package jp.developer.bbee.pcassem.model;

import java.time.LocalDateTime;

public record SaveHead(String saveid, String guestid, String savename,
                       LocalDateTime createddate, LocalDateTime lastupdate) {}
