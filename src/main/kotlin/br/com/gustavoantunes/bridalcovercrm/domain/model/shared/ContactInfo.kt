package br.com.gustavoantunes.bridalcovercrm.domain.model.shared

/**
 * Value Object that represents the contact information of a company.
 *
 * Groups all available contact methods, allowing validations
 * and operations related to communication information.
 */
data class ContactInfo(
    val email: Email? = null,
    val phone: Phone? = null,
    val website: String? = null,
    val socialMedia: Map<SocialMediaType, String> = emptyMap()
) {

    init {
        require(hasAtLeastOneContact()) { "Must have at least one contact method" }
        website?.let { require(isValidWebsite(it)) { "Invalid website: $it" } }
        socialMedia.forEach { (type, handle) ->
            require(handle.isNotBlank()) { "${type.name} handle cannot be empty" }
        }
    }

    companion object {
        private val WEBSITE_PATTERN = Regex(
            "^(https?://)?(www\\.)?[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]\\.[a-zA-Z]{2,}(/.*)?$"
        )

        private fun isValidWebsite(website: String): Boolean {
            return WEBSITE_PATTERN.matches(website.trim())
        }
    }

    /**
     * Checks if there is at least one contact method
     */
    private fun hasAtLeastOneContact(): Boolean {
        return email != null || phone != null || website != null || socialMedia.isNotEmpty()
    }

    /**
     * Checks if has email
     */
    fun hasEmail(): Boolean = email != null

    /**
     * Checks if has phone
     */
    fun hasPhone(): Boolean = phone != null

    /**
     * Checks if has website
     */
    fun hasWebsite(): Boolean = !website.isNullOrBlank()

    /**
     * Checks if has social media
     */
    fun hasSocialMedia(): Boolean = socialMedia.isNotEmpty()

    /**
     * Checks if the contact information is complete
     */
    fun isComplete(): Boolean {
        return hasEmail() && hasPhone()
    }

    /**
     * Returns the primary contact (email if available, otherwise phone)
     */
    fun getPrimaryContact(): String? {
        return when {
            hasEmail() -> email?.toString()
            hasPhone() -> phone?.toString()
            hasWebsite() -> website
            hasSocialMedia() -> socialMedia.values.firstOrNull()
            else -> null
        }
    }

    /**
     * Returns all available contacts as a list of strings
     */
    fun getAllContacts(): List<String> {
        val contacts = mutableListOf<String>()

        email?.let { contacts.add("Email: $it") }
        phone?.let { contacts.add("Phone: $it") }
        website?.let { contacts.add("Website: $it") }
        socialMedia.forEach { (type, handle) ->
            contacts.add("${type.getDisplayName()}: $handle")
        }

        return contacts
    }

    /**
     * Checks if it's a corporate email
     */
    fun hasCorporateEmail(): Boolean {
        return email?.isCorporateEmail() == true
    }

    /**
     * Checks if has mobile phone
     */
    fun hasMobilePhone(): Boolean {
        return phone?.isMobile() == true
    }

    /**
     * Returns a new instance with updated email
     */
    fun withEmail(newEmail: Email): ContactInfo {
        return copy(email = newEmail)
    }

    /**
     * Returns a new instance with updated phone
     */
    fun withPhone(newPhone: Phone): ContactInfo {
        return copy(phone = newPhone)
    }

    /**
     * Returns a new instance with updated website
     */
    fun withWebsite(newWebsite: String): ContactInfo {
        return copy(website = newWebsite)
    }

    /**
     * Returns a new instance with added social media
     */
    fun withSocialMedia(type: SocialMediaType, handle: String): ContactInfo {
        return copy(socialMedia = socialMedia + (type to handle))
    }

    /**
     * Returns a new instance with removed social media
     */
    fun withoutSocialMedia(type: SocialMediaType): ContactInfo {
        return copy(socialMedia = socialMedia - type)
    }

    /**
     * Calculates a completeness score (0-100)
     */
    fun getCompletenessScore(): Int {
        var score = 0

        if (hasEmail()) score += 30
        if (hasPhone()) score += 30
        if (hasWebsite()) score += 20
        if (hasSocialMedia()) score += 10
        if (hasCorporateEmail()) score += 5
        if (hasMobilePhone()) score += 5

        return minOf(score, 100)
    }
}