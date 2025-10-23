package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared

/**
 * Value Object that represents a CNPJ (National Registry of Legal Entities).
 * 
 * Encapsulates the validation and formatting rules for Brazilian CNPJ,
 * ensuring that only valid CNPJs are accepted in the system.
 */
data class CNPJ(val value: String) {
    
    init {
        require(value.isNotBlank()) { "CNPJ cannot be empty" }
        require(isValid()) { "Invalid CNPJ: $value" }
    }
    
    companion object {
        /**
         * Creates a CNPJ from a string, removing formatting
         */
        fun fromString(value: String): CNPJ {
            val cleanValue = value.replace(Regex("[^0-9]"), "")
            return CNPJ(cleanValue)
        }
        
        /**
         * Validates if a string represents a valid CNPJ
         */
        fun isValidCNPJ(cnpj: String): Boolean {
            val digits = cnpj.replace(Regex("[^0-9]"), "")
            
            // Checks if it has 14 digits
            if (digits.length != 14) return false
            
            // Checks if not all digits are the same
            if (digits.all { it == digits[0] }) return false
            
            return validateCheckDigits(digits)
        }
        
        private fun validateCheckDigits(cnpj: String): Boolean {
            // Calculates the first check digit
            val weights1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
            var sum = 0
            for (i in 0..11) {
                sum += cnpj[i].digitToInt() * weights1[i]
            }
            val firstDigit = if (sum % 11 < 2) 0 else 11 - (sum % 11)
            
            if (cnpj[12].digitToInt() != firstDigit) return false
            
            // Calculates the second check digit
            val weights2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
            sum = 0
            for (i in 0..12) {
                sum += cnpj[i].digitToInt() * weights2[i]
            }
            val secondDigit = if (sum % 11 < 2) 0 else 11 - (sum % 11)
            
            return cnpj[13].digitToInt() == secondDigit
        }
    }
    
    /**
     * Validates if the CNPJ is valid
     */
    private fun isValid(): Boolean {
        return isValidCNPJ(value)
    }
    
    /**
     * Returns the formatted CNPJ (XX.XXX.XXX/XXXX-XX)
     */
    fun format(): String {
        val digits = getDigits()
        return "${digits.substring(0, 2)}.${digits.substring(2, 5)}.${digits.substring(5, 8)}/" +
                "${digits.substring(8, 12)}-${digits.substring(12, 14)}"
    }
    
    /**
     * Returns only the CNPJ digits
     */
    fun getDigits(): String {
        return value.replace(Regex("[^0-9]"), "")
    }
    
    /**
     * Returns the first 8 digits (CNPJ base number)
     */
    fun getBaseNumber(): String {
        return getDigits().substring(0, 8)
    }
    
    /**
     * Returns the branch digits (4 digits after the slash)
     */
    fun getBranchNumber(): String {
        return getDigits().substring(8, 12)
    }
    
    /**
     * Returns the check digits
     */
    fun getCheckDigits(): String {
        return getDigits().substring(12, 14)
    }
    
    /**
     * Checks if it's the head office (branch = 0001)
     */
    fun isHeadOffice(): Boolean {
        return getBranchNumber() == "0001"
    }
    
    override fun toString(): String = format()
}

