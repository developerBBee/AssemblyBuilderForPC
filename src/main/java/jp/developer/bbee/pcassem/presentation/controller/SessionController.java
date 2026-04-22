package jp.developer.bbee.pcassem.presentation.controller;

import javax.servlet.http.HttpServletRequest;
import jp.developer.bbee.pcassem.domain.auth.IdTokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    private final IdTokenVerifier idTokenVerifier;

    public SessionController(IdTokenVerifier idTokenVerifier) {
        this.idTokenVerifier = idTokenVerifier;
    }

    @PostMapping
    public ResponseEntity<Void> createSession(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String uid = idTokenVerifier.verifyAndGetUid(idToken);
            // セッション固定攻撃対策: 既存セッションがある場合のみIDを再生成する
            if (request.getSession(false) != null) {
                request.changeSessionId();
            }
            request.getSession(true).setAttribute("firebaseUid", uid);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.warn("[SessionController] Failed to verify idToken: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }
}
