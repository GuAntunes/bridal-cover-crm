package br.com.gustavoantunes.bridalcovercrm.domain.model.shared

/**
 * Enum that represents the types of social media supported by the system.
 * 
 * Defines the main social media platforms used by companies
 * for communication and marketing.
 */
enum class SocialMediaType {
    FACEBOOK,
    INSTAGRAM,
    LINKEDIN,
    TWITTER,
    WHATSAPP;
    
    /**
     * Returns the platform's base URL
     */
    fun getBaseUrl(): String {
        return when (this) {
            FACEBOOK -> "https://facebook.com/"
            INSTAGRAM -> "https://instagram.com/"
            LINKEDIN -> "https://linkedin.com/company/"
            TWITTER -> "https://twitter.com/"
            WHATSAPP -> "https://wa.me/"
        }
    }
    
    /**
     * Checks if the platform requires a handle/username
     */
    fun requiresHandle(): Boolean {
        return this != WHATSAPP // WhatsApp uses phone number
    }
    
    /**
     * Returns the platform's display name
     */
    fun getDisplayName(): String {
        return when (this) {
            FACEBOOK -> "Facebook"
            INSTAGRAM -> "Instagram"
            LINKEDIN -> "LinkedIn"
            TWITTER -> "Twitter"
            WHATSAPP -> "WhatsApp"
        }
    }
    
    /**
     * Checks if it's a professional platform
     */
    fun isProfessional(): Boolean {
        return this == LINKEDIN
    }
    
    /**
     * Checks if it's a visual platform
     */
    fun isVisual(): Boolean {
        return this == INSTAGRAM
    }
    
    /**
     * Checks if it allows direct messaging
     */
    fun allowsDirectMessaging(): Boolean {
        return when (this) {
            WHATSAPP, FACEBOOK, INSTAGRAM, LINKEDIN -> true
            TWITTER -> false
        }
    }
    
    /**
     * Returns the complete URL for the profile/page
     */
    fun getProfileUrl(handle: String): String {
        return when (this) {
            WHATSAPP -> "${getBaseUrl()}$handle"
            else -> "${getBaseUrl()}$handle"
        }
    }
}

