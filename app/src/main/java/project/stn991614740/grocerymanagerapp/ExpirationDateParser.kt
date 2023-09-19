package project.stn991614740.grocerymanagerapp

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

class ExpirationDateParser {

    // This array holds regular expressions for various date patterns that the parser can recognize.
    private val patterns = arrayOf(
        "(\\d{1,4}[-/ .]\\d{1,2}[-/ .]\\d{1,4}|\\d{1,4}[-/ .]\\w{3,4}[-/ .]\\d{1,4})",
        "(\\w{1,4}[-/ .]\\d{1,2}[-/ .]\\d{1,4}|\\d{1,4}[-/ .]\\w{3,4}[-/ .]\\d{1,4})",
        "(\\w{1,4}[-/ .]\\d{1,4}|\\d{1,4}[-/ .]\\w{3,4}[-/ .])",
        "(\\d{1,4}[-/ .]\\d{1,2}|\\d{1,4}[-/ .]\\w{3,4})"
    )

    companion object {
        // This array holds different date formats that the parser can use to parse dates.
        private val DATE_FORMATS = arrayOf(
            "dd/MM/yy",
            "dd/MMM/yy",
            "dd/MM/yyyy",
            "dd/MMM/yyyy",
            "yyyy/MM/dd",
            "MMM/dd/yy",
            "MMM/dd/yyyy",
            "yyyy/MMM",
            "yy/MM/dd",
            "MM/dd",
            "MM/yyyy",
            "MMM/yyyy",
            "dd.MM.yy",
            "dd.MM.yyyy",
            "dd MMM yy",
            "dd MMM yyyy",
            "yyyy.MM.dd",
            "MMM dd yy",
            "MMM dd yyyy",
            "dd MMM",
            "yy.MM.dd",
            "MM.dd",
            "MM.yyyy",
            "MMM yyyy",
            "dd MM yy",
            "dd MM yyyy",
            "yyyy MM dd",
            "MMM dd yy",
            "MMM dd yyyy",
            "ddMMMyy",
            "ddMMMyyyy",
            "MMMyy",
            "MMMyyyy",
            "ddMMyy",
            "ddMMyyyy"
        )
    }

    // This function takes an input string and tries to parse an expiration date out of it.
    fun parseExpirationDate(input: String): String? {
        for (patternIndex in patterns.indices) {
            // For each pattern in the patterns array, create a new regex pattern and try to match it against the input string.
            val pattern = Pattern.compile(patterns[patternIndex])
            val matcher = pattern.matcher(input)
            if (matcher.find()) {
                // If a match is found, extract the matched date string and try to parse it using each date format in the DATE_FORMATS array.
                val dateString = matcher.group(1)
                for (formatIndex in DATE_FORMATS.indices) {
                    val format = DATE_FORMATS[formatIndex]
                    try {
                        val dateFormat = SimpleDateFormat(format, Locale.US)
                        dateFormat.isLenient = false
                        val date = dateString?.let { dateFormat.parse(it) }
                        // If a valid date object is obtained, format it as "yyyy/MM/dd" and return it.
                        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                        return date?.let { dateFormatter.format(it) }
                    } catch (e: ParseException) {
                        // If the current format fails to parse the date, ignore the exception and try the next format in the DATE_FORMATS array.
                    }
                }
            }
        }
        // If no valid date is obtained, return null.
        return null
    }
}
