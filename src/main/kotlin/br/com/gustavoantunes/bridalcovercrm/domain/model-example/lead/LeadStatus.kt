package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead

/**
 * Enum that represents the possible statuses of a Lead in the sales process.
 * 
 * The status represents the current stage of the Lead in the sales funnel,
 * allowing progress tracking and stage-based decision making.
 */
enum class LeadStatus {
    /** Newly created Lead, no contact has been made yet */
    NEW,
    
    /** Lead has been contacted at least once */
    CONTACTED,
    
    /** Lead has been qualified as a potential client */
    QUALIFIED,
    
    /** Proposal has been sent to the Lead */
    PROPOSAL_SENT,
    
    /** In negotiation process */
    NEGOTIATING,
    
    /** Lead has been converted to a client */
    CONVERTED,
    
    /** Lead has been lost/discarded */
    LOST;
    
    /**
     * Checks if the status represents a Lead still active in the process
     */
    fun isActive(): Boolean {
        return this != CONVERTED && this != LOST
    }
    
    /**
     * Checks if it's possible to transition to a new status
     */
    fun canTransitionTo(newStatus: LeadStatus): Boolean {
        return when (this) {
            NEW -> newStatus in listOf(CONTACTED, LOST)
            CONTACTED -> newStatus in listOf(QUALIFIED, LOST)
            QUALIFIED -> newStatus in listOf(PROPOSAL_SENT, LOST)
            PROPOSAL_SENT -> newStatus in listOf(NEGOTIATING, CONVERTED, LOST)
            NEGOTIATING -> newStatus in listOf(CONVERTED, PROPOSAL_SENT, LOST)
            CONVERTED -> false // Terminal status
            LOST -> false // Terminal status
        }
    }
    
    /**
     * Checks if the status is terminal (does not allow more transitions)
     */
    fun isTerminal(): Boolean {
        return this == CONVERTED || this == LOST
    }
    
    /**
     * Returns the next logical status in the sales funnel
     */
    fun getNextStatus(): LeadStatus? {
        return when (this) {
            NEW -> CONTACTED
            CONTACTED -> QUALIFIED
            QUALIFIED -> PROPOSAL_SENT
            PROPOSAL_SENT -> NEGOTIATING
            NEGOTIATING -> CONVERTED
            CONVERTED -> null
            LOST -> null
        }
    }
}

