package diaro

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val formatter = DateTimeFormatter.ofPattern(
    "dd MMMM yyyy, EEEE HH:mm",
    Locale.ENGLISH
)

val module = SimpleModule().apply {
    addSerializer(
        LocalDateTime::class.java,
        LocalDateTimeSerializer(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    )
}

val mapper = ObjectMapper().registerModule(module).setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

data class JournalEntry(val date: LocalDateTime, val title: String, val body: String)

fun main() {
    val textEntries = File("/Users/akryvtsun/entryExport.txt").readText()
        .split("-".repeat(96))
    //.take(3)
    val entries = textEntries
        // clean empty lines
        .map { entry ->
            entry.lines()
                .dropWhile { it.isBlank() }
                .dropLastWhile { it.isBlank() }
        }
        .map { entry ->
            var lines = entry
            val timestamp = LocalDateTime.parse(lines.first(), formatter)
            lines = lines.drop(2)

            val titleLine = lines.first()
            val result = ":::(.+):::".toRegex().find(titleLine)
            val title = result?.groups[1]?.value?.trim() ?: ""
            if (result != null) {
                lines = lines.drop(2)
            }

            val body = lines.joinToString(separator = "\n")

            JournalEntry(timestamp, title, body)
        }
    val json = mapper.writeValueAsString(entries)
    File("/Users/akryvtsun/journal_json.txt").writeText(json)
}