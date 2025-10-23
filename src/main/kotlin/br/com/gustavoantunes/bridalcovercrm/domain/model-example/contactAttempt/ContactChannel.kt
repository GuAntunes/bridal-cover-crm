package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.contactAttempt

/**
 * Enum that represents the available contact channels for communicating with Leads.
 * 
 * Defines the different means by which the team can contact
 * leads during the qualification and conversion process.
 */
enum class ContactChannel {
    /** Phone contact */
    PHONE,
    
    /** Email contact */
    EMAIL,
    
    /** WhatsApp contact */
    WHATSAPP,
    
    /** In-person contact */
    IN_PERSON,
    
    /** Contact through website (chat, form) */
    WEBSITE,
    
    /** Contact through social media */
    SOCIAL_MEDIA;
    
    /**
     * Checks if the channel requires direct personal contact
     */
    fun requiresPersonalContact(): Boolean {
        return when (this) {
            PHONE, WHATSAPP, IN_PERSON -> true
            EMAIL, WEBSITE, SOCIAL_MEDIA -> false
        }
    }
    
    /**
     * Checks if the channel is digital
     */
    fun isDigital(): Boolean {
        return this != IN_PERSON
    }
    
    /**
     * Checks if the channel allows real-time communication
     */
    fun isRealTime(): Boolean {
        return when (this) {
            PHONE, WHATSAPP, IN_PERSON, WEBSITE -> true
            EMAIL, SOCIAL_MEDIA -> false
        }
    }
    
    /**
     * Returns the user-friendly description of the channel
     */
    fun getDisplayName(): String {
        return when (this) {
            PHONE -> "Phone"
            EMAIL -> "Email"
            WHATSAPP -> "WhatsApp"
            IN_PERSON -> "In Person"
            WEBSITE -> "Website"
            SOCIAL_MEDIA -> "Social Media"
        }
    }
    
    /**
     * Returns the expected effectiveness of the channel (1-5, with 5 being most effective)
     */
    fun getEffectivenessRating(): Int {
        return when (this) {
            IN_PERSON -> 5
            PHONE -> 4
            WHATSAPP -> 4
            WEBSITE -> 3
            EMAIL -> 2
            SOCIAL_MEDIA -> 2
        }
    }
}

