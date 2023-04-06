package project.stn991614740.grocerymanagerapp

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

class ExpirationDateParser {

    private val patterns = arrayOf(
        "(\\d{1,4}[-/ .]\\d{1,2}[-/ .]\\d{1,4}|\\d{1,4}[-/ .]\\w{3,4}[-/ .]\\d{1,4})",
        "(\\w{1,4}[-/ .]\\d{1,2}[-/ .]\\d{1,4}|\\d{1,4}[-/ .]\\w{3,4}[-/ .]\\d{1,4})",
        "(\\w{1,4}[-/ .]\\d{1,4}|\\d{1,4}[-/ .]\\w{3,4}[-/ .])",
        "(\\d{1,4}[-/ .]\\d{1,2}|\\d{1,4}[-/ .]\\w{3,4})"
    )

    companion object {
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

    fun parseExpirationDate(input: String): String? {
        for (patternIndex in patterns.indices) {
            val pattern = Pattern.compile(patterns[patternIndex])
            val matcher = pattern.matcher(input)
            if (matcher.find()) {
                val dateString = matcher.group(1)
                for (formatIndex in DATE_FORMATS.indices) {
                    val format = DATE_FORMATS[formatIndex]
                    try {
                        val dateFormat = SimpleDateFormat(format, Locale.US)
                        dateFormat.isLenient = false
                        val date = dateString?.let { dateFormat.parse(it) }
                        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                        return date?.let { dateFormatter.format(it) }
                    } catch (e: ParseException) {
                        // Ignore, try the next format
                    }
                }
            }
        }
        return null
    }
}
