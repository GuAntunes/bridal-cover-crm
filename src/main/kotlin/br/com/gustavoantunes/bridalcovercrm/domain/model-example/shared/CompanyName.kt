package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared

/**
 * Value Object that represents a company name.
 * 
 * Encapsulates business rules related to company name,
 * including format and size validations.
 */
data class CompanyName(val value: String) {
    
    init {
        require(value.isNotBlank()) { "Company name cannot be empty" }
        require(value.length >= MIN_LENGTH) { "Company name must have at least $MIN_LENGTH characters" }
        require(value.length <= MAX_LENGTH) { "Company name cannot have more than $MAX_LENGTH characters" }
        require(isValid()) { "Company name contains invalid characters" }
    }
    
    companion object {
        const val MIN_LENGTH = 2
        const val MAX_LENGTH = 200
        
        // Regex that allows letters, numbers, spaces and some special characters common in company names
        private val VALID_PATTERN = Regex("^[a-zA-ZÀ-ÿ0-9\\s\\-&.,()]+$")
    }
    
    /**
     * Validates if the name contains only allowed characters
     */
    private fun isValid(): Boolean {
        return VALID_PATTERN.matches(value.trim())
    }
    
    /**
     * Returns the formatted name (first letter of each word capitalized)
     */
    fun getFormattedName(): String {
        return value.trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }
    
    /**
     * Returns the length of the name
     */
    fun length(): Int = value.trim().length
    
    /**
     * Checks if the name is considered long
     */
    fun isLong(): Boolean = length() > 50
    
    /**
     * Returns an abbreviated version of the name (first words up to 30 characters)
     */
    fun getAbbreviated(): String {
        val trimmed = value.trim()
        if (trimmed.length <= 30) return trimmed
        
        val words = trimmed.split("\\s+".toRegex())
        var result = ""
        
        for (word in words) {
            if ((result + word).length > 30) break
            result += if (result.isEmpty()) word else " $word"
        }
        
        return if (result.isEmpty()) trimmed.substring(0, 30) else result
    }
    
    override fun toString(): String = value.trim()
}

