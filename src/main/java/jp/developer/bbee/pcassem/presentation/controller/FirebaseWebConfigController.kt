package jp.developer.bbee.pcassem.presentation.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/firebase")
class FirebaseWebConfigController(
    @Value("\${firebase.web.api-key}") private val apiKey: String,
    @Value("\${firebase.web.auth-domain}") private val authDomain: String,
    @Value("\${firebase.project.id}") private val projectId: String,
) {
    @GetMapping("/config")
    fun getConfig(): Map<String, String> = mapOf(
        "apiKey" to apiKey,
        "authDomain" to authDomain,
        "projectId" to projectId,
    )
}
