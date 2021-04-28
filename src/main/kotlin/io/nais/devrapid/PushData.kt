package io.nais.devrapid

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PushData(
    val latestCommitSha: String,
    val latestCommit: ZonedDateTime,
    val webHookRecieved: ZonedDateTime
) {
    companion object FromPushData {
        fun from(payload: String): PushData {
            val node: JsonNode = ObjectMapper().readTree(payload)
            return PushData(
                latestCommitSha = node.at("/head_commit/id").asText(),
                latestCommit = ZonedDateTime.from(
                    DateTimeFormatter.ISO_DATE_TIME.parse(
                        node.at("/head_commit/timestamp").asText()
                    )
                ),
                webHookRecieved = ZonedDateTime.now(ZoneId.of("Europe/Oslo"))
            )
        }
    }
}