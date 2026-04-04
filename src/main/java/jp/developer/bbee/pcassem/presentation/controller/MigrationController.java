package jp.developer.bbee.pcassem.presentation.controller;

import javax.servlet.http.HttpSession;
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
    public ResponseEntity<MigrationResponse> migrate(@RequestBody MigrationRequest request, HttpSession session) {
        String guestId = (String) session.getAttribute("guestId");
        if (guestId == null || guestId.length() != 32) {
            return ResponseEntity.badRequest()
                    .body(new MigrationResponse(false, "No valid session"));
        }
        if (request.idToken == null || request.idToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MigrationResponse(false, "idToken is required"));
        }
        try {
            boolean migrated = migrationService.migrate(request.idToken, guestId);
            String message = migrated ? "Migration successful" : "Already migrated";
            return ResponseEntity.ok(new MigrationResponse(true, message));
        } catch (Exception e) {
            System.out.println("[MigrationController] Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new MigrationResponse(false, "Migration failed due to an internal error"));
        }
    }
}
