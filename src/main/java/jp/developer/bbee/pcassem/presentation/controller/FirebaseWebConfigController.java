package jp.developer.bbee.pcassem.presentation.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/firebase")
public class FirebaseWebConfigController {

    private final String apiKey;
    private final String authDomain;
    private final String projectId;

    public FirebaseWebConfigController(
            @Value("${firebase.web.api-key}") String apiKey,
            @Value("${firebase.web.auth-domain}") String authDomain,
            @Value("${firebase.project.id}") String projectId) {
        this.apiKey = apiKey;
        this.authDomain = authDomain;
        this.projectId = projectId;
    }

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return Map.of(
                "apiKey", apiKey,
                "authDomain", authDomain,
                "projectId", projectId
        );
    }
}
