package jp.developer.bbee.pcassem;

import jp.developer.bbee.pcassem.UidMappingDao.UidMapping;
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
    private UidMappingDao uidMappingDao;

    @Mock
    private FirestoreService firestoreService;

    private MockMvc mockMvc;

    private static final String VALID_GUEST_ID = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4";
    private static final String FIREBASE_UID = "firebase-uid-12345";

    @BeforeEach
    void setUp() {
        when(dao.getTime()).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));
        when(dao.getSaveHeadRecent5(anyString())).thenReturn(Collections.emptyList());
        when(dao.getAssemCountList(anyString())).thenReturn(Collections.emptyMap());
        when(dao.findAllUserAssemByGuestId(anyString())).thenReturn(Collections.emptyList());

        HomeController controller = new HomeController(dao, uidMappingDao, firestoreService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void top_noGuestId_assembliesDisplayHidden() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"));

        verifyNoInteractions(uidMappingDao);
        verifyNoInteractions(firestoreService);
    }

    @Test
    void top_guestIdPresent_noUidMapping_usesH2Path() throws Exception {
        when(uidMappingDao.findByGuestId(VALID_GUEST_ID)).thenReturn(null);

        mockMvc.perform(get("/").param("guestId", VALID_GUEST_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        verify(uidMappingDao).findByGuestId(VALID_GUEST_ID);
        verifyNoInteractions(firestoreService);
        verify(dao).getSaveHeadRecent5(VALID_GUEST_ID);
        verify(dao).findAllUserAssemByGuestId(VALID_GUEST_ID);
    }

    @Test
    void top_guestIdPresent_uidMappingFound_usesFirestorePath() throws Exception {
        UidMapping uidMapping = new UidMapping(FIREBASE_UID, VALID_GUEST_ID, LocalDateTime.now());
        when(uidMappingDao.findByGuestId(VALID_GUEST_ID)).thenReturn(uidMapping);
        when(firestoreService.getAssemblies(FIREBASE_UID)).thenReturn(Collections.emptyList());
        when(firestoreService.getSaveHeadsRecent5(FIREBASE_UID)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/").param("guestId", VALID_GUEST_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"));

        verify(uidMappingDao).findByGuestId(VALID_GUEST_ID);
        verify(firestoreService).getAssemblies(FIREBASE_UID);
        verify(firestoreService).getSaveHeadsRecent5(FIREBASE_UID);
        verify(dao, never()).getSaveHeadRecent5(anyString());
        verify(dao, never()).findAllUserAssemByGuestId(anyString());
    }

    @Test
    void top_firestorePath_withAssembliesAndSaveHeads_modelIsPopulated() throws Exception {
        UidMapping uidMapping = new UidMapping(FIREBASE_UID, VALID_GUEST_ID, LocalDateTime.now());
        UserAssem ua = new UserAssem("id1", "device-001", "cpu", VALID_GUEST_ID,
                LocalDateTime.now(), LocalDateTime.now());
        SaveHead sh = new SaveHead("saveid12345678901234567890123456", VALID_GUEST_ID,
                "My Build", LocalDateTime.now(), LocalDateTime.now());
        DeviceInfo di = new DeviceInfo(
                "device-001", "cpu", "http://example.com", "Intel Core i9",
                "http://img.example.com/cpu.jpg", "detail", 50000, 1, 0, 0,
                "2024-01-01", 0, LocalDateTime.now(), LocalDateTime.now());

        when(uidMappingDao.findByGuestId(VALID_GUEST_ID)).thenReturn(uidMapping);
        when(firestoreService.getAssemblies(FIREBASE_UID)).thenReturn(List.of(ua));
        when(firestoreService.getSaveHeadsRecent5(FIREBASE_UID)).thenReturn(List.of(sh));
        when(dao.findRecordByIds(List.of("device-001"))).thenReturn(List.of(di));

        mockMvc.perform(get("/").param("guestId", VALID_GUEST_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("assembliesList"))
                .andExpect(model().attributeExists("saveHeaderList"))
                .andExpect(model().attributeExists("totalPrice"));
    }

    @Test
    void top_firestoreException_assembliesAndSaveHeadHidden() throws Exception {
        UidMapping uidMapping = new UidMapping(FIREBASE_UID, VALID_GUEST_ID, LocalDateTime.now());
        when(uidMappingDao.findByGuestId(VALID_GUEST_ID)).thenReturn(uidMapping);
        when(firestoreService.getAssemblies(FIREBASE_UID)).thenThrow(new RuntimeException("Firestore unavailable"));

        mockMvc.perform(get("/").param("guestId", VALID_GUEST_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("assembliesDisplay", "hidden"))
                .andExpect(model().attribute("saveHeadVisible", "hidden"));
    }
}
