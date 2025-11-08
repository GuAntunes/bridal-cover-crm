package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.health

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/health")
class HealthController {

    @GetMapping
    fun health(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(
            HealthResponse(
                status = "UP",
                timestamp = LocalDateTime.now(),
                application = "Bridal Cover CRM",
                version = "0.0.1-SNAPSHOT"
            )
        )
    }
}

data class HealthResponse(
    val status: String,
    val timestamp: LocalDateTime,
    val application: String,
    val version: String
)

