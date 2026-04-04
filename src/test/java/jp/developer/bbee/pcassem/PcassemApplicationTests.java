package jp.developer.bbee.pcassem;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"gemini.api.key=dummy",
		"firebase.project.id=dummy-project",
		"firebase.web.api-key=dummy",
		"firebase.web.auth-domain=dummy.firebaseapp.com"
})
class PcassemApplicationTests {

	@MockBean
	FirebaseApp firebaseApp;

	@MockBean
	Firestore firestore;

	@Test
	void contextLoads() {
	}
}
