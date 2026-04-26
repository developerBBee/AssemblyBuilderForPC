package jp.developer.bbee.pcassem.presentation.controller

import jp.developer.bbee.pcassem.domain.migration.MigrationService
import jp.developer.bbee.pcassem.presentation.data.MigrationRequest
import jp.developer.bbee.pcassem.presentation.data.MigrationResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Locale

@RestController
@RequestMapping("api/migrate")
class MigrationController(private val migrationService: MigrationService) {

    private val logger = LoggerFactory.getLogger(MigrationController::class.java)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidBody(): ResponseEntity<MigrationResponse> =
        ResponseEntity.badRequest().body(MigrationResponse(false, "Invalid request body"))

    @PostMapping
    fun migrate(@RequestBody request: MigrationRequest): ResponseEntity<MigrationResponse> {
        val idToken = request.idToken
        val guestId = request.guestId
        if (idToken.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(MigrationResponse(false, "idToken is required"))
        }
        if (guestId == null || !guestId.matches(Regex("[0-9a-fA-F]{32}"))) {
            return ResponseEntity.badRequest().body(MigrationResponse(false, "guestId must be a 32-character hex string"))
        }
        return try {
            val migrated = migrationService.migrate(idToken, guestId.lowercase(Locale.ROOT))
            val message = if (migrated) "Migration successful" else "Already migrated"
            ResponseEntity.ok(MigrationResponse(true, message))
        } catch (e: Exception) {
            logger.error("[MigrationController] Error: {} - {}", e.javaClass.simpleName, e.message, e)
            ResponseEntity.internalServerError().body(MigrationResponse(false, "Migration failed due to an internal error"))
        }
    }
}
