package jp.developer.bbee.pcassem.domain.auth;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.stereotype.Component;

@Component
public class FirebaseIdTokenVerifier implements IdTokenVerifier {

    private final FirebaseApp firebaseApp;

    public FirebaseIdTokenVerifier(FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;
    }

    @Override
    public String verifyAndGetUid(String idToken) throws Exception {
        return FirebaseAuth.getInstance(firebaseApp).verifyIdToken(idToken).getUid();
    }
}
