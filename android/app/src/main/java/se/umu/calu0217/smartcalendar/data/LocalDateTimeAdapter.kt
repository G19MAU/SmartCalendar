package se.umu.calu0217.smartcalendar.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @ToJson
    fun toJson(value: LocalDateTime): String = value.format(formatter)

    @FromJson
    fun fromJson(value: String): LocalDateTime = LocalDateTime.parse(value, formatter)
}
