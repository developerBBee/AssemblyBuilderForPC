package jp.developer.bbee.pcassem;

import com.fasterxml.jackson.databind.ObjectMapper;
import jp.developer.bbee.pcassem.domain.migration.MigrationService;
import jp.developer.bbee.pcassem.presentation.controller.MigrationController;
import jp.developer.bbee.pcassem.presentation.data.MigrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MigrationControllerTest {

    @Mock
    private MigrationService migrationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final String VALID_GUEST_ID = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4";

    @BeforeEach
    void setUp() {
        MigrationController controller = new MigrationController(migrationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void migrate_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/migrate")
                        .contentType("application/json")
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(migrationService);
    }

    @Test
    void migrate_missingGuestId_returnsBadRequest() throws Exception {
        MigrationRequest req = new MigrationRequest();
        req.idToken = "valid-token";
        // guestId なし

        mockMvc.perform(post("/api/migrate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(migrationService);
    }

    @Test
    void migrate_tooShortGuestId_returnsBadRequest() throws Exception {
        MigrationRequest req = new MigrationRequest();
        req.idToken = "valid-token";
        req.guestId = "tooshort";

        mockMvc.perform(post("/api/migrate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(migrationService);
    }

    @Test
    void migrate_nonHexGuestId_returnsBadRequest() throws Exception {
        MigrationRequest req = new MigrationRequest();
        req.idToken = "valid-token";
        req.guestId = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"; // 32 chars, non-hex

        mockMvc.perform(post("/api/migrate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(migrationService);
    }

    @Test
    void migrate_missingIdToken_returnsBadRequest() throws Exception {
        MigrationRequest req = new MigrationRequest();
        req.guestId = VALID_GUEST_ID;
        // idToken なし

        mockMvc.perform(post("/api/migrate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(migrationService);
    }

    @Test
    void migrate_validRequest_returnsSuccess() throws Exception {
        MigrationRequest req = new MigrationRequest();
        req.idToken = "valid-token";
        req.guestId = VALID_GUEST_ID;

        when(migrationService.migrate("valid-token", VALID_GUEST_ID)).thenReturn(true);

        mockMvc.perform(post("/api/migrate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Migration successful"));
    }

    @Test
    void migrate_alreadyMigrated_returnsAlreadyMigrated() throws Exception {
        MigrationRequest req = new MigrationRequest();
        req.idToken = "valid-token";
        req.guestId = VALID_GUEST_ID;

        when(migrationService.migrate("valid-token", VALID_GUEST_ID)).thenReturn(false);

        mockMvc.perform(post("/api/migrate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Already migrated"));
    }

    @Test
    void migrate_serviceThrowsException_returnsInternalServerError() throws Exception {
        MigrationRequest req = new MigrationRequest();
        req.idToken = "valid-token";
        req.guestId = VALID_GUEST_ID;

        when(migrationService.migrate(any(), any())).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/migrate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
}
