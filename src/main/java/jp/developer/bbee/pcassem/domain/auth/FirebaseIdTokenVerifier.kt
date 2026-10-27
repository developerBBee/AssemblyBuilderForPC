package jp.developer.bbee.pcassem.domain.auth

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.springframework.stereotype.Component

@Component
class FirebaseIdTokenVerifier(private val firebaseApp: FirebaseApp) : IdTokenVerifier {

    override fun verifyAndGetUid(idToken: String): String =
        FirebaseAuth.getInstance(firebaseApp).verifyIdToken(idToken).uid
}
