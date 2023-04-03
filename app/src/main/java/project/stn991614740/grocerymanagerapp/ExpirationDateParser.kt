package project.stn991614740.testbed

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern


class ExpirationDateParser {

//    val patterns = arrayOf("(\\d{1,4}[-/ ]\\d{1,2}[-/ ]\\d{1,4}|\\d{1,4}[-/ ]\\w{3,4}[-/ ]\\d{1,4})"
//        ,"(\\w{1,4}[-/ ]\\d{1,2}[-/ ]\\d{1,4}|\\d{1,4}[-/ ]\\w{3,4}[-/ ]\\d{1,4})",
//        "(\\w{1,4}[-/ ]\\d{1,4}|\\d{1,4}[-/ ]\\w{3,4}[-/ ])")

    val patterns = arrayOf(
        "(\\d{1,4}[-/ ]\\d{1,2}[-/ ]\\d{1,4}|\\d{1,4}[-/ ]\\w{3,4}[-/ ]\\d{1,4})",
        "(\\w{1,4}[-/ ]\\d{1,2}[-/ ]\\d{1,4}|\\d{1,4}[-/ ]\\w{3,4}[-/ ]\\d{1,4})",
        "(\\w{1,4}[-/ ]\\d{1,4}|\\d{1,4}[-/ ]\\w{3,4}[-/ ])",
        "(\\d{1,4}[-/ ]\\d{1,2}|\\d{1,4}[-/ ]\\w{3,4})"
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
            "MMM/yyyy"

        )
    }

    fun parseExpirationDate(input: String): Date? {
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
                        return dateFormat.parse(dateString)
                    } catch (e: ParseException) {
                        // Ignore, try the next format
                    }
                }
            }
        }
        return null
    }

//    fun parseExpirationDate(input: String): Date? {
//        //val pattern = Pattern.compile("(\\d{1,4}[-/ ]\\d{1,2}[-/ ]\\d{1,4}|\\d{1,4}[-/ ]\\w{3,4}[-/ ]\\d{1,4})")
//        var control = true
//        var count = 0
//        while(control){
//            // set pattern to iterate through array
//            val pattern = Pattern.compile(patterns[count])
//            val matcher = pattern.matcher(input)
//            if (matcher.find()) { // if match found then format
//                val dateString = matcher.group(1)
//                for (format in DATE_FORMATS) {
//                    try {
//                        val dateFormat = SimpleDateFormat(format, Locale.US)
//                        dateFormat.isLenient = false
//                        return dateFormat.parse(dateString)
//                    } catch (e: ParseException) {
//                        // Ignore, try the next format
//                    }
//                }
//            }
//            else{ // increase count by 1 if does not exceed array of patterns size
//                if((count+1) > patterns.size){
//                    control=false
//                }
//                else{
//                    count++
//                }
//            }
//        }
//        return null
//    }

}

