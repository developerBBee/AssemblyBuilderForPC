package jp.developer.bbee.pcassem;

import jp.developer.bbee.pcassem.domain.auth.IdTokenVerifier;
import jp.developer.bbee.pcassem.domain.firestore.FirestoreService;
import jp.developer.bbee.pcassem.model.DeviceInfo;
import jp.developer.bbee.pcassem.model.SaveHead;
import jp.developer.bbee.pcassem.model.UserAssem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeControllerTest {

    @Mock
    private DeviceInfoDao dao;

    @Mock
    private IdTokenVerifier idTokenVerifier;

    @Mock
    private FirestoreService firestoreService;

    private MockMvc mockMvc;

    private static final String VALID_ID_TOKEN = "valid-firebase-id-token";
    private static final String FIREBASE_UID = "firebase-uid-12345";

    @BeforeEach
    void setUp() {
        when(dao.getTime()).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));

        HomeController controller = new HomeController(dao, idTokenVerifier, firestoreService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void top_noIdToken_noSession_assembliesDisplayHidden() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"))
                .andExpect(model().attribute("saveHeadVisible", "hidden"));

        verifyNoInteractions(idTokenVerifier);
        verifyNoInteractions(firestoreService);
    }

    @Test
    void top_validIdToken_loadsFromFirestore() throws Exception {
        when(idTokenVerifier.verifyAndGetUid(VALID_ID_TOKEN)).thenReturn(FIREBASE_UID);
        when(firestoreService.getAssemblies(FIREBASE_UID)).thenReturn(Collections.emptyList());
        when(firestoreService.getSaveHeadsRecent5(FIREBASE_UID)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/").param("idToken", VALID_ID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"));

        verify(idTokenVerifier).verifyAndGetUid(VALID_ID_TOKEN);
        verify(firestoreService).getAssemblies(FIREBASE_UID);
        verify(firestoreService).getSaveHeadsRecent5(FIREBASE_UID);
    }

    @Test
    void top_invalidIdToken_assembliesDisplayHidden() throws Exception {
        when(idTokenVerifier.verifyAndGetUid(anyString())).thenThrow(new Exception("Invalid token"));

        mockMvc.perform(get("/").param("idToken", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"))
                .andExpect(model().attribute("saveHeadVisible", "hidden"));
    }

    @Test
    void top_sessionHasFirebaseUid_loadsFromFirestore() throws Exception {
        when(firestoreService.getAssemblies(FIREBASE_UID)).thenReturn(Collections.emptyList());
        when(firestoreService.getSaveHeadsRecent5(FIREBASE_UID)).thenReturn(Collections.emptyList());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("firebaseUid", FIREBASE_UID);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"));

        verifyNoInteractions(idTokenVerifier);
        verify(firestoreService).getAssemblies(FIREBASE_UID);
        verify(firestoreService).getSaveHeadsRecent5(FIREBASE_UID);
    }

    @Test
    void top_validIdToken_withAssembliesAndSaveHeads_modelIsPopulated() throws Exception {
        UserAssem ua = new UserAssem("id1", "device-001", "cpu", FIREBASE_UID,
                LocalDateTime.now(), LocalDateTime.now());
        SaveHead sh = new SaveHead("saveid12345678901234567890123456", FIREBASE_UID,
                "My Build", LocalDateTime.now(), LocalDateTime.now());
        DeviceInfo di = new DeviceInfo(
                "device-001", "cpu", "http://example.com", "Intel Core i9",
                "http://img.example.com/cpu.jpg", "detail", 50000, 1, 0, 0,
                "2024-01-01", 0, LocalDateTime.now(), LocalDateTime.now());

        when(idTokenVerifier.verifyAndGetUid(VALID_ID_TOKEN)).thenReturn(FIREBASE_UID);
        when(firestoreService.getAssemblies(FIREBASE_UID)).thenReturn(List.of(ua));
        when(firestoreService.getSaveHeadsRecent5(FIREBASE_UID)).thenReturn(List.of(sh));
        when(dao.findRecordByIds(List.of("device-001"))).thenReturn(List.of(di));

        mockMvc.perform(get("/").param("idToken", VALID_ID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("assembliesList"))
                .andExpect(model().attributeExists("saveHeaderList"))
                .andExpect(model().attributeExists("totalPrice"));
    }

    @Test
    void top_firestoreException_assembliesAndSaveHeadHidden() throws Exception {
        when(idTokenVerifier.verifyAndGetUid(VALID_ID_TOKEN)).thenReturn(FIREBASE_UID);
        when(firestoreService.getAssemblies(FIREBASE_UID)).thenThrow(new RuntimeException("Firestore unavailable"));

        mockMvc.perform(get("/").param("idToken", VALID_ID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"))
                .andExpect(model().attribute("saveHeadVisible", "hidden"));
    }
}
