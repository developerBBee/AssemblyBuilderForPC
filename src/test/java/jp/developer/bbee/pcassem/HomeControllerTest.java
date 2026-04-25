package jp.developer.bbee.pcassem;

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

import jp.developer.bbee.pcassem.DeviceInfoDao.SaveItem;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeControllerTest {

    @Mock
    private DeviceInfoDao dao;

    @Mock
    private FirestoreService firestoreService;

    private MockMvc mockMvc;

    private static final String FIREBASE_UID = "firebase-uid-12345";

    @BeforeEach
    void setUp() {
        when(dao.getTime()).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));
        when(dao.findRecordByIds(anyList())).thenReturn(Collections.emptyList());
        when(dao.getSaveItemsBySaveId(anyString())).thenReturn(Collections.emptyList());

        HomeController controller = new HomeController(dao, firestoreService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void top_noSession_assembliesDisplayHidden() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"))
                .andExpect(model().attribute("saveHeadVisible", "hidden"));

        verifyNoInteractions(firestoreService);
    }

    @Test
    void top_sessionHasFirebaseUid_emptyAssemblies_assembliesDisplayHidden() throws Exception {
        when(firestoreService.getAssemblies(FIREBASE_UID)).thenReturn(Collections.emptyList());
        when(firestoreService.getSaveHeadsRecent5(FIREBASE_UID)).thenReturn(Collections.emptyList());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("firebaseUid", FIREBASE_UID);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"));

        verify(firestoreService).getAssemblies(FIREBASE_UID);
        verify(firestoreService).getSaveHeadsRecent5(FIREBASE_UID);
    }

    @Test
    void top_sessionHasFirebaseUid_withAssembliesAndSaveHeads_modelIsPopulated() throws Exception {
        UserAssem ua = new UserAssem("id1", "device-001", "cpu", FIREBASE_UID,
                LocalDateTime.now(), LocalDateTime.now());
        SaveHead sh = new SaveHead("saveid12345678901234567890123456", FIREBASE_UID,
                "My Build", LocalDateTime.now(), LocalDateTime.now());
        DeviceInfo di = new DeviceInfo(
                "device-001", "cpu", "http://example.com", "Intel Core i9",
                "http://img.example.com/cpu.jpg", "detail", 50000, 1, 0, 0,
                "2024-01-01", 0, LocalDateTime.now(), LocalDateTime.now());

        when(firestoreService.getAssemblies(FIREBASE_UID)).thenReturn(List.of(ua));
        when(firestoreService.getSaveHeadsRecent5(FIREBASE_UID)).thenReturn(List.of(sh));
        when(dao.findRecordByIds(List.of("device-001"))).thenReturn(List.of(di));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("firebaseUid", FIREBASE_UID);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("assembliesList"))
                .andExpect(model().attributeExists("saveHeaderList"))
                .andExpect(model().attributeExists("totalPrice"));
    }

    @Test
    void top_firestoreException_assembliesAndSaveHeadHidden() throws Exception {
        when(firestoreService.getAssemblies(FIREBASE_UID)).thenThrow(new RuntimeException("Firestore unavailable"));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("firebaseUid", FIREBASE_UID);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"))
                .andExpect(model().attribute("saveHeadVisible", "hidden"));
    }

    @Test
    void saveConstruction_noSession_redirectsToRoot() throws Exception {
        mockMvc.perform(post("/save")
                        .param("deviceIdList", "device-001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(dao, never()).save(anyString(), anyString(), anyList());
        verifyNoInteractions(firestoreService);
    }

    @Test
    void saveConstruction_withSession_savesAndRedirectsToRec() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("firebaseUid", FIREBASE_UID);

        mockMvc.perform(post("/save")
                        .session(session)
                        .param("deviceIdList", "device-001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/rec/*"));

        verify(dao).save(anyString(), eq(FIREBASE_UID), anyList());
        verify(firestoreService).saveSaves(eq(FIREBASE_UID), isNull(), anyList(), anyMap());
    }

    // ── /rec/{saveId} テスト ──────────────────────────────────────────

    private static final String SAVE_ID = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4";

    @Test
    void restoreConstruction_firestoreHit_returnsRestoredList() throws Exception {
        SaveItem saveItem = new SaveItem(SAVE_ID, "device-001", 50000,
                LocalDateTime.now(), LocalDateTime.now());
        DeviceInfo di = new DeviceInfo("device-001", "cpu", "http://example.com", "Intel Core i9",
                "http://img.example.com/cpu.jpg", "detail", 55000, 1, 0, 0,
                "2024-01-01", 0, LocalDateTime.now(), LocalDateTime.now());

        when(firestoreService.getSaveItems(SAVE_ID)).thenReturn(Optional.of(List.of(saveItem)));
        when(dao.findRecordByIds(List.of("device-001"))).thenReturn(List.of(di));

        mockMvc.perform(get("/rec/" + SAVE_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("restoredList"));

        verify(firestoreService).getSaveItems(SAVE_ID);
        verify(dao, never()).restore(anyString());
    }

    @Test
    void restoreConstruction_firestoreMiss_h2Hit_returnsRestoredList() throws Exception {
        HomeController.RestoreDevice rd = new HomeController.RestoreDevice(
                SAVE_ID, "device-001", "cpu", "http://example.com", "Intel Core i9",
                "http://img.example.com/cpu.jpg", "detail", 50000, 55000);

        when(firestoreService.getSaveItems(SAVE_ID)).thenReturn(Optional.empty());
        when(dao.restore(SAVE_ID)).thenReturn(List.of(rd));

        mockMvc.perform(get("/rec/" + SAVE_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("restoredList"));

        verify(firestoreService).getSaveItems(SAVE_ID);
        verify(dao).restore(SAVE_ID);
    }

    @Test
    void restoreConstruction_neitherFound_returns404() throws Exception {
        when(firestoreService.getSaveItems(SAVE_ID)).thenReturn(Optional.empty());
        when(dao.restore(SAVE_ID)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/rec/" + SAVE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void restoreConstruction_firestoreError_returns503() throws Exception {
        when(firestoreService.getSaveItems(SAVE_ID)).thenThrow(new RuntimeException("Firestore unavailable"));
        when(dao.restore(SAVE_ID)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/rec/" + SAVE_ID))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void saveConstruction_firestoreException_stillRedirectsToRec() throws Exception {
        doThrow(new RuntimeException("Firestore error"))
                .when(firestoreService).saveSaves(anyString(), any(), anyList(), anyMap());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("firebaseUid", FIREBASE_UID);

        mockMvc.perform(post("/save")
                        .session(session)
                        .param("deviceIdList", "device-001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/rec/*"));

        verify(dao).save(anyString(), eq(FIREBASE_UID), anyList());
    }
}
