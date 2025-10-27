package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.contactAttempt

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.DomainObject
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Entity that represents a contact attempt with a Lead.
 * 
 * Each contact attempt records important information about
 * the channel used, result obtained, and necessary next steps.
 */
class ContactAttempt(
    override val id: ContactAttemptId,
    val leadId: LeadId,
    val attemptDate: LocalDateTime,
    val channel: ContactChannel,
    var result: ContactResult,
    var notes: String? = null,
    var nextFollowUp: LocalDate? = null,
    val scriptUsed: String? = null, // For now String, later can be ScriptId
    var duration: Duration? = null
) : DomainObject<ContactAttemptId>() {
    
    init {
        require(attemptDate <= LocalDateTime.now()) { "Attempt date cannot be in the future" }
        notes?.let { require(it.length <= 1000) { "Notes cannot have more than 1000 characters" } }
        nextFollowUp?.let { require(it >= LocalDate.now()) { "Follow-up date cannot be in the past" } }
        duration?.let { require(!it.isNegative) { "Duration cannot be negative" } }
    }
    
    /**
     * Schedules a follow-up for a specific date
     */
    fun scheduleFollowUp(date: LocalDate) {
        require(date >= LocalDate.now()) { "Follow-up date must be today or in the future" }
        require(result.requiresFollowUp()) { "Result ${result.name} does not require follow-up" }
        
        this.nextFollowUp = date
    }
    
    /**
     * Removes the follow-up scheduling
     */
    fun clearFollowUp() {
        this.nextFollowUp = null
    }
    
    /**
     * Checks if the attempt was successful
     */
    fun wasSuccessful(): Boolean {
        return result.isPositive()
    }
    
    /**
     * Checks if requires follow-up
     */
    fun requiresFollowUp(): Boolean {
        return result.requiresFollowUp() && nextFollowUp != null
    }
    
    /**
     * Checks if the follow-up is overdue
     */
    fun isFollowUpOverdue(): Boolean {
        return nextFollowUp?.let { it < LocalDate.now() } ?: false
    }
    
    /**
     * Checks if the follow-up is for today
     */
    fun isFollowUpToday(): Boolean {
        return nextFollowUp == LocalDate.now()
    }
    
    /**
     * Updates the attempt result
     */
    fun updateResult(newResult: ContactResult, newNotes: String? = null) {
        this.result = newResult
        newNotes?.let { this.notes = it }
        
        // If the new result doesn't require follow-up, clear the date
        if (!newResult.requiresFollowUp()) {
            clearFollowUp()
        }
    }
    
    /**
     * Adds or updates notes
     */
    fun updateNotes(newNotes: String) {
        require(newNotes.length <= 1000) { "Notes cannot have more than 1000 characters" }
        this.notes = newNotes
    }
    
    /**
     * Checks if the attempt was long (more than 10 minutes)
     */
    fun wasLongAttempt(): Boolean {
        return duration?.let { it > Duration.ofMinutes(10) } ?: false
    }
    
    /**
     * Calculates how many days until follow-up
     */
    fun getDaysUntilFollowUp(): Long? {
        return nextFollowUp?.let { 
            ChronoUnit.DAYS.between(LocalDate.now(), it)
        }
    }
    
    /**
     * Returns a summary of the attempt for logs/reports
     */
    fun getSummary(): String {
        return buildString {
            append("${channel.getDisplayName()} - ${result.getDisplayName()}")
            duration?.let { append(" (${it.toMinutes()}min)") }
            nextFollowUp?.let { append(" | Follow-up: $it") }
        }
    }
    
    /**
     * Checks if the attempt is recent (last 24 hours)
     */
    fun isRecent(): Boolean {
        return attemptDate.isAfter(LocalDateTime.now().minusDays(1))
    }
}
