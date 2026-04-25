package jp.developer.bbee.pcassem.presentation.controller;

import jp.developer.bbee.pcassem.domain.migration.MigrationService;
import jp.developer.bbee.pcassem.presentation.data.MigrationRequest;
import jp.developer.bbee.pcassem.presentation.data.MigrationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("api/migrate")
public class MigrationController {

    private static final Logger logger = LoggerFactory.getLogger(MigrationController.class);

    private final MigrationService migrationService;

    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MigrationResponse> handleInvalidBody() {
        return ResponseEntity.badRequest()
                .body(new MigrationResponse(false, "Invalid request body"));
    }

    @PostMapping
    public ResponseEntity<MigrationResponse> migrate(@RequestBody MigrationRequest request) {
        if (request.idToken == null || request.idToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MigrationResponse(false, "idToken is required"));
        }
        if (request.guestId == null || !request.guestId.matches("[0-9a-fA-F]{32}")) {
            return ResponseEntity.badRequest()
                    .body(new MigrationResponse(false, "guestId must be a 32-character hex string"));
        }
        try {
            boolean migrated = migrationService.migrate(request.idToken, request.guestId.toLowerCase(Locale.ROOT));
            String message = migrated ? "Migration successful" : "Already migrated";
            return ResponseEntity.ok(new MigrationResponse(true, message));
        } catch (Exception e) {
            logger.error("[MigrationController] Error: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MigrationResponse(false, "Migration failed due to an internal error"));
        }
    }
}
