package br.com.gustavoantunes.bridalcovercrm.domain.model.lead

/**
 * Enum that represents the possible sources of a Lead.
 *
 * The Lead source is important for marketing channel ROI analysis
 * and to understand which sources generate the best leads.
 */
enum class LeadSource {
    /** Lead manually entered into the system */
    MANUAL_ENTRY,

    /** Lead obtained through Google Places/Maps */
    GOOGLE_PLACES,

    /** Lead obtained through referral */
    REFERRAL,

    /** Lead that came through the website */
    WEBSITE,

    /** Lead obtained through cold calling */
    COLD_CALL,

    /** Lead obtained through social media */
    SOCIAL_MEDIA;

    /**
     * Checks if the source is automated (does not require manual intervention)
     */
    fun isAutomated(): Boolean {
        return when (this) {
            GOOGLE_PLACES, WEBSITE, SOCIAL_MEDIA -> true
            MANUAL_ENTRY, REFERRAL, COLD_CALL -> false
        }
    }

    /**
     * Checks if the source requires additional data verification
     */
    fun requiresVerification(): Boolean {
        return when (this) {
            GOOGLE_PLACES, COLD_CALL -> true
            MANUAL_ENTRY, REFERRAL, WEBSITE, SOCIAL_MEDIA -> false
        }
    }

    /**
     * Returns the user-friendly description of the source
     */
    fun getDisplayName(): String {
        return when (this) {
            MANUAL_ENTRY -> "Manual Entry"
            GOOGLE_PLACES -> "Google Places"
            REFERRAL -> "Referral"
            WEBSITE -> "Website"
            COLD_CALL -> "Cold Call"
            SOCIAL_MEDIA -> "Social Media"
        }
    }

    /**
     * Returns the source priority (higher value = higher priority)
     */
    fun getPriority(): Int {
        return when (this) {
            REFERRAL -> 5
            WEBSITE -> 4
            SOCIAL_MEDIA -> 3
            MANUAL_ENTRY -> 2
            GOOGLE_PLACES -> 2
            COLD_CALL -> 1
        }
    }
}