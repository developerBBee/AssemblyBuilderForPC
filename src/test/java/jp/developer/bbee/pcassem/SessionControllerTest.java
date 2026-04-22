package jp.developer.bbee.pcassem;

import jp.developer.bbee.pcassem.domain.auth.IdTokenVerifier;
import jp.developer.bbee.pcassem.presentation.controller.SessionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    @Mock
    private IdTokenVerifier idTokenVerifier;

    private MockMvc mockMvc;

    private static final String VALID_ID_TOKEN = "valid-firebase-id-token";
    private static final String FIREBASE_UID = "firebase-uid-12345";

    @BeforeEach
    void setUp() {
        SessionController controller = new SessionController(idTokenVerifier);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createSession_missingIdToken_returns400() throws Exception {
        mockMvc.perform(post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(idTokenVerifier);
    }

    @Test
    void createSession_validToken_returns200AndSetsSessionAttribute() throws Exception {
        when(idTokenVerifier.verifyAndGetUid(VALID_ID_TOKEN)).thenReturn(FIREBASE_UID);

        mockMvc.perform(post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idToken\":\"" + VALID_ID_TOKEN + "\"}"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("firebaseUid", FIREBASE_UID));

        verify(idTokenVerifier).verifyAndGetUid(VALID_ID_TOKEN);
    }

    @Test
    void createSession_invalidToken_returns401() throws Exception {
        when(idTokenVerifier.verifyAndGetUid(anyString())).thenThrow(new Exception("Invalid token"));

        mockMvc.perform(post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idToken\":\"invalid-token\"}"))
                .andExpect(status().isUnauthorized());
    }
}
