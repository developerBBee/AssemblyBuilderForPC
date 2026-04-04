package jp.developer.bbee.pcassem.presentation.controller;

import jp.developer.bbee.pcassem.domain.migration.MigrationService;
import jp.developer.bbee.pcassem.presentation.data.MigrationRequest;
import jp.developer.bbee.pcassem.presentation.data.MigrationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/migrate")
public class MigrationController {

    private final MigrationService migrationService;

    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping
    public ResponseEntity<MigrationResponse> migrate(@RequestBody MigrationRequest request) {
        if (request.guestId == null || request.guestId.length() != 32) {
            return ResponseEntity.badRequest()
                    .body(new MigrationResponse(false, "Invalid guestId"));
        }
        if (request.idToken == null || request.idToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MigrationResponse(false, "idToken is required"));
        }
        try {
            migrationService.migrate(request.idToken, request.guestId);
            return ResponseEntity.ok(new MigrationResponse(true, "Migration successful"));
        } catch (Exception e) {
            System.out.println("[MigrationController] Error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new MigrationResponse(false, e.getMessage()));
        }
    }
}
