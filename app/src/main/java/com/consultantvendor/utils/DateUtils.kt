package com.consultantvendor.utils

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.text.format.DateUtils
import androidx.core.content.ContextCompat
import com.consultantvendor.R
import com.consultantvendor.ui.loginSignUp.availability.OnTimeSelected
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object  DateUtils {

    val utcFormat = SimpleDateFormat(DateFormat.UTC_FORMAT_NORMAL, Locale.getDefault())

   /* fun openDatePicker(activity: Activity, listener: OnDateSelected, max: Boolean, min: Boolean) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(activity,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    var selectedDate = "$dayOfMonth/${monthOfYear.plus(1)}/$year"

                    selectedDate =
                            dateFormatChange(DateFormat.DATE_FORMAT_SLASH_YEAR, DateFormat.DATE_FORMAT_SLASH, selectedDate)
                    listener.onDateSelected(selectedDate)

                }, year, month, day
        )

        if (max)
            dpd.datePicker.maxDate = System.currentTimeMillis() - 36000
        if (min)
            dpd.datePicker.minDate = System.currentTimeMillis() - 36000

        dpd.show()
    }*/

    fun openDatePicker(activity: Activity, listener: OnDateSelected, max: Long?, min: Long?) {

        val picker = SingleDateAndTimePickerDialog.Builder(activity)
                .bottomSheet()
                .focusable()
                .backgroundColor(Color.BLACK)
                .displayHours(false)
                .displayMinutes(false)
                .displayDays(false)
                .displayMonth(true)
                .displayYears(true)
                .titleTextSize(16)
                .displayDaysOfMonth(true)
                .title(activity.getString(R.string.select_date))
                .mainColor(ContextCompat.getColor(activity, R.color.colorWhite))
                .titleTextColor(ContextCompat.getColor(activity, R.color.colorWhite))
                .listener {
                    //Toast.makeText(activity, it.toString(), Toast.LENGTH_SHORT).show()
                    when {
                        (min == null || it.time > min) && (max == null || it.time < max) -> {
                            var selectedDate = dateFormatFromMillis(DateFormat.DATE_FORMAT, it.time)
                            selectedDate = dateFormatChange(DateFormat.DATE_FORMAT, DateFormat.DATE_FORMAT_SLASH, selectedDate)
                            listener.onDateSelected(selectedDate)
                        }
                        min != null && it.time < min -> {
                            activity.longToast(activity.getString(R.string.select_future_date))
                        }
                        max != null && it.time > max -> {
                            activity.longToast(activity.getString(R.string.select_previos_date))
                        }
                    }
                }

        if (max != null) {
            picker.maxDateRange(Date(max))
            picker.defaultDate(Date((max - 86400000)))
        }
        if (min != null) {
            picker.minDateRange(Date(min))
            picker.defaultDate(Date((min + 86400000)))
        }

        picker.display()
    }


    fun dateFormatFromMillis(format: String, timeInMillis: Long?): String {
        val fmt = SimpleDateFormat(format, Locale.ENGLISH)
        return if (timeInMillis == null || timeInMillis == 0L)
            ""
        else
            fmt.format(timeInMillis)
    }


    fun dateFormatChange(formatFrom: String, formatTo: String, value: String): String {
        val originalFormat = SimpleDateFormat(formatFrom, Locale.ENGLISH)
        val targetFormat = SimpleDateFormat(formatTo, Locale.ENGLISH)
        val date = originalFormat.parse(value)
        return targetFormat.format(date)
    }

    fun localToUTC(dateFormat: String, datesToConvert: String): String {

        var dateToReturn = datesToConvert

        val sdf = SimpleDateFormat(dateFormat)
        sdf.timeZone = TimeZone.getDefault()
        var gmt: Date? = null

        val sdfOutPutToSend = SimpleDateFormat(DateFormat.DATE_FORMAT)
        sdfOutPutToSend.timeZone = TimeZone.getTimeZone("UTC")

        try {

            gmt = sdf.parse(datesToConvert)
            dateToReturn = sdfOutPutToSend.format(gmt)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return dateToReturn
    }


    fun getTimeAgo(createdAt: String?): String {
        val agoString: String

        if (createdAt == null) {
            return ""
        }

        utcFormat.timeZone = TimeZone.getTimeZone("Etc/UTC")
        val time = utcFormat.parse(createdAt).time
        val now = System.currentTimeMillis()

        val ago = DateUtils.getRelativeTimeSpanString(
                time, now, DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
        )

        return ago.toString()
    }

    fun getTimeAgoForMillis(millis: Long): String {

        val now = System.currentTimeMillis()

        return DateUtils.getRelativeTimeSpanString(
                millis, now, DateUtils.SECOND_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    fun getLocalTimeAgo(timeString: Long?, removeAgo: String): String {
        var agoString = ""

        timeString?.let {
            val now = System.currentTimeMillis()

            val ago = DateUtils.getRelativeTimeSpanString(
                    timeString,
                    now,
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_TIME
            )

            agoString = ago.toString()
        }

        return agoString
    }

    fun dateTimeFormatFromUTC(format: String, createdDate: String?): String {
        return if (createdDate == null || createdDate.isEmpty())
            ""
        else {
            utcFormat.timeZone = TimeZone.getTimeZone("Etc/UTC")

            val fmt = SimpleDateFormat(format, Locale.getDefault())
            fmt.format(utcFormat.parse(createdDate))
        }
    }


    fun getTime(context: Context, startTime: String, endTime: String?,
                isStart: Boolean, listener: OnTimeSelected) {
        var compareDate = true
        if (endTime == null)
            compareDate = false
        else if (isStart && endTime.isEmpty()) {
            compareDate = false
        } else if (startTime.isEmpty()) {
            compareDate = false
        }

        val sdf = SimpleDateFormat(DateFormat.TIME_FORMAT, Locale.ENGLISH)

        val endTime = if (endTime != null && endTime.isNotEmpty())
            sdf.parse(endTime)
        else Date()

        val startTime = if (startTime.isNotEmpty())
            sdf.parse(startTime)
        else Date()

        val cal = Calendar.getInstance()
        var selectedTime = ""
        var isError = false

//        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
//            cal.set(Calendar.HOUR_OF_DAY, hour)
//            cal.set(Calendar.MINUTE, minute)
//
//            val newTime = sdf.parse(sdf.format(cal.time))
//            val time = sdf.format(cal.time)
//
//            /* val different = if (isStart) newTime.time - endTime.time
//             else
//                 newTime.time - startTime.time*/
//
//            if (isStart) {
//                if (!compareDate || newTime.before(endTime)) {
//                    selectedTime = time
//                } else {
//                    isError = true
//                }
//
//            } else {
//                if (!compareDate || startTime.before(newTime)) {
//                    selectedTime = sdf.format(cal.time)
//                } else {
//                    isError = true
//                }
//            }
//
//            listener.onTimeSelected(Triple(selectedTime, isStart, isError))
//        }
//
//        CustomTimePickerDialog(
//                context, timeSetListener,
//                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
//                false
//        ).show()


        val picker = SingleDateAndTimePickerDialog.Builder(context)
            .customLocale(Locale.ENGLISH)
            .bottomSheet()
            .focusable()
            .curved()
            .backgroundColor(Color.BLACK)
            .displayHours(true)
            .displayMinutes(true)
            .displayDays(false)
            .displayMonth(false)
            .displayYears(false)
            .titleTextSize(16)
            .minutesStep(30)
            .displayDaysOfMonth(false)
            .title(context.getString(R.string.select_time))
            .mainColor(ContextCompat.getColor(context, R.color.colorWhite))
            .titleTextColor(ContextCompat.getColor(context, R.color.colorWhite))
            .listener {
                //Toast.makeText(activity, it.toString(), Toast.LENGTH_SHORT).show()
                cal.set(Calendar.HOUR_OF_DAY, dateFormatFromMillisBackend("HH", it.time).toInt())
                cal.set(Calendar.MINUTE, dateFormatFromMillisBackend("mm", it.time).toInt())


                val newTime = sdf.parse(sdf.format(cal.time))
                val time = sdf.format(cal.time)


                /* val different = if (isStart) newTime.time - endTime.time
                 else
                     newTime.time - startTime.time*/


                if (isStart) {
                    if (!compareDate || newTime.before(endTime)) {
                        selectedTime = time
                    } else {
                        isError = true
                    }
                } else {
                    if (!compareDate || startTime.before(newTime)) {
                        selectedTime = sdf.format(cal.time)
                    } else {
                        isError = true
                    }
                }
                listener.onTimeSelected(Triple(selectedTime, isStart, isError))

            }
        picker.display()
    }
}


fun dateFormatFromMillisBackend(format: String, timeInMillis: Long?): String {
    val fmt = SimpleDateFormat(format, Locale.ENGLISH)
    return if (timeInMillis == null || timeInMillis == 0L)
        ""
    else
        fmt.format(timeInMillis)
}


/*On Date selected listener*/
interface OnDateSelected {
    fun onDateSelected(date: String)
}

fun isYesterday(calendar: Calendar): Boolean {
    val tempCal = Calendar.getInstance()
    tempCal.add(Calendar.DAY_OF_MONTH, -1)
    return calendar.get(Calendar.DAY_OF_MONTH) == tempCal.get(Calendar.DAY_OF_MONTH)
}

object DateFormat {
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_TIME_FORMAT = "dd MMM yyyy · hh:mm a"
    const val TIME_FORMAT = "hh:mm a"
    const val TIME_FORMAT_24 = "HH:mm"
    const val MON_YEAR_FORMAT = "dd MMM, yyyy"
    const val MON_DAY_YEAR = "MMM dd, yyyy"
    const val MON_DATE = "MMM dd"
    const val MON_DATE_YEAR = "MMM dd, yy"
    const val DATE_FORMAT_SLASH = "MM/dd/yy"
    const val DATE_FORMAT_SLASH_YEAR = "dd/MM/yyyy"
    const val DATE_FORMAT_RENEW = "yyyy-MM-dd hh:mm"
    const val UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val UTC_FORMAT_NORMAL = "yyyy-MM-dd hh:mm:ss"
    const val MONTH_FORMAT = "MMM"
}
