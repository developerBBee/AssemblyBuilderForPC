package jp.developer.bbee.pcassem.presentation.controller

import jp.developer.bbee.pcassem.domain.auth.IdTokenVerifier
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/session")
class SessionController(private val idTokenVerifier: IdTokenVerifier) {

    private val logger = LoggerFactory.getLogger(SessionController::class.java)

    @GetMapping
    fun checkSession(request: HttpServletRequest): ResponseEntity<Void> {
        val session = request.getSession(false)
        return if (session != null && session.getAttribute("firebaseUid") != null)
            ResponseEntity.ok().build()
        else
            ResponseEntity.status(401).build()
    }

    @PostMapping
    fun createSession(@RequestBody body: Map<String, String>, request: HttpServletRequest): ResponseEntity<Void> {
        val idToken = body["idToken"]
        if (idToken.isNullOrBlank()) return ResponseEntity.badRequest().build()
        return try {
            val uid = idTokenVerifier.verifyAndGetUid(idToken)
            if (request.getSession(false) != null) request.changeSessionId()
            request.getSession(true).setAttribute("firebaseUid", uid)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.warn("[SessionController] Failed to verify idToken: {}", e.message)
            ResponseEntity.status(401).build()
        }
    }
}
