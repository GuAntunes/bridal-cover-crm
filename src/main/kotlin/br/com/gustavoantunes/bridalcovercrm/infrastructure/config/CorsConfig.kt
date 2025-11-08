package br.com.gustavoantunes.bridalcovercrm.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()

        // Allow credentials
        config.allowCredentials = true

        // Allow origins (adjust for production)
        config.addAllowedOriginPattern("*")

        // Allow headers
        config.addAllowedHeader("*")

        // Allow methods
        config.addAllowedMethod("GET")
        config.addAllowedMethod("POST")
        config.addAllowedMethod("PUT")
        config.addAllowedMethod("DELETE")
        config.addAllowedMethod("OPTIONS")
        config.addAllowedMethod("PATCH")

        // Max age
        config.maxAge = 3600L

        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}

