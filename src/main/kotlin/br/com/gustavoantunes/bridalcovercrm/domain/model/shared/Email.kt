package br.com.gustavoantunes.bridalcovercrm.domain.model.shared

/**
 * Value Object that represents an email address.
 * 
 * Encapsulates email format validation rules,
 * ensuring that only valid emails are accepted in the system.
 */
data class Email(val value: String) {
    
    init {
        require(value.isNotBlank()) { "Email cannot be empty" }
        require(isValid()) { "Invalid email: $value" }
    }
    
    companion object {
        // Simplified regex for email validation
        private val EMAIL_PATTERN = Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        
        /**
         * Validates if a string represents a valid email
         */
        fun isValidEmail(email: String): Boolean {
            return email.isNotBlank() && 
                   email.length <= 254 && // RFC 5321 limit
                   EMAIL_PATTERN.matches(email.trim().lowercase())
        }
    }
    
    /**
     * Validates if the email is valid
     */
    private fun isValid(): Boolean {
        return isValidEmail(value)
    }
    
    /**
     * Returns the email domain (part after @)
     */
    fun getDomain(): String {
        return value.substringAfter("@").lowercase()
    }
    
    /**
     * Returns the local part of the email (part before @)
     */
    fun getLocalPart(): String {
        return value.substringBefore("@").lowercase()
    }
    
    /**
     * Checks if it's a corporate email (not from a free provider)
     */
    fun isCorporateEmail(): Boolean {
        val freeProviders = setOf(
            "gmail.com", "hotmail.com", "yahoo.com", "outlook.com",
            "uol.com.br", "bol.com.br", "terra.com.br", "ig.com.br"
        )
        return getDomain() !in freeProviders
    }
    
    /**
     * Checks if it's an email from a Brazilian provider
     */
    fun isBrazilianProvider(): Boolean {
        val brazilianProviders = setOf(
            "uol.com.br", "bol.com.br", "terra.com.br", "ig.com.br",
            "globo.com", "r7.com"
        )
        return getDomain() in brazilianProviders
    }
    
    /**
     * Returns the normalized email (lowercase and trimmed)
     */
    fun getNormalized(): String {
        return value.trim().lowercase()
    }
    
    /**
     * Masks the email for display (e.g.: j***@example.com)
     */
    fun getMasked(): String {
        val localPart = getLocalPart()
        val domain = getDomain()
        
        return when {
            localPart.length <= 2 -> "${localPart.first()}***@$domain"
            localPart.length <= 4 -> "${localPart.take(2)}***@$domain"
            else -> "${localPart.take(3)}***@$domain"
        }
    }
    
    override fun toString(): String = getNormalized()
}

