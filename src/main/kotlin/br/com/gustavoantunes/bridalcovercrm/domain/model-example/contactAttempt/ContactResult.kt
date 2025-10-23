package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.contactAttempt

/**
 * Enum that represents the possible results of a contact attempt with a Lead.
 * 
 * The contact result is fundamental to determine the next steps
 * in the Lead qualification and conversion process.
 */
enum class ContactResult {
    /** Could not establish contact (did not answer, voicemail, etc.) */
    NO_ANSWER,
    
    /** Lead showed interest in the product/service */
    INTERESTED,
    
    /** Lead showed no interest */
    NOT_INTERESTED,
    
    /** Lead requested that we contact them again */
    CALLBACK_REQUESTED,
    
    /** Meeting was scheduled with the Lead */
    MEETING_SCHEDULED,
    
    /** Lead requested a proposal */
    PROPOSAL_REQUESTED,
    
    /** Lead was converted during the contact */
    CONVERTED,
    
    /** Contact information is invalid */
    INVALID_CONTACT;
    
    /**
     * Checks if the result is positive (indicates progress in the funnel)
     */
    fun isPositive(): Boolean {
        return when (this) {
            INTERESTED, CALLBACK_REQUESTED, MEETING_SCHEDULED, 
            PROPOSAL_REQUESTED, CONVERTED -> true
            NO_ANSWER, NOT_INTERESTED, INVALID_CONTACT -> false
        }
    }
    
    /**
     * Checks if the result requires follow-up
     */
    fun requiresFollowUp(): Boolean {
        return when (this) {
            NO_ANSWER, INTERESTED, CALLBACK_REQUESTED, MEETING_SCHEDULED -> true
            NOT_INTERESTED, PROPOSAL_REQUESTED, CONVERTED, INVALID_CONTACT -> false
        }
    }
    
    /**
     * Checks if the result is terminal (requires no more actions)
     */
    fun isTerminal(): Boolean {
        return when (this) {
            NOT_INTERESTED, CONVERTED, INVALID_CONTACT -> true
            NO_ANSWER, INTERESTED, CALLBACK_REQUESTED, 
            MEETING_SCHEDULED, PROPOSAL_REQUESTED -> false
        }
    }
    
    /**
     * Returns the user-friendly description of the result
     */
    fun getDisplayName(): String {
        return when (this) {
            NO_ANSWER -> "No Answer"
            INTERESTED -> "Interested"
            NOT_INTERESTED -> "Not Interested"
            CALLBACK_REQUESTED -> "Callback Requested"
            MEETING_SCHEDULED -> "Meeting Scheduled"
            PROPOSAL_REQUESTED -> "Proposal Requested"
            CONVERTED -> "Converted"
            INVALID_CONTACT -> "Invalid Contact"
        }
    }
    
    /**
     * Returns the result score for qualification calculation (0-10)
     */
    fun getQualificationScore(): Int {
        return when (this) {
            CONVERTED -> 10
            PROPOSAL_REQUESTED -> 9
            MEETING_SCHEDULED -> 8
            INTERESTED -> 6
            CALLBACK_REQUESTED -> 4
            NO_ANSWER -> 2
            NOT_INTERESTED -> 0
            INVALID_CONTACT -> 0
        }
    }
}

