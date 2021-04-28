package io.nais.devrapid

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.Timestamp
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PushData(
    val latestCommitSha: String,
    val latestCommit: ZonedDateTime,
    val webHookRecieved: ZonedDateTime
) {
    companion object Converter {
        fun fromJson(payload: String): PushData {
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

        fun toProtoBuf(data: PushData): Message.Pushdata {

            val toBuilder = Message.Pushdata.getDefaultInstance().toBuilder()
            val latesCommit = Timestamp.newBuilder().setSeconds(data.latestCommit.toEpochSecond()).build()
            val webHoocReceived = Timestamp.newBuilder().setSeconds(data.webHookRecieved.toEpochSecond()).build()
            return toBuilder.setLatestCommitSha(data.latestCommitSha).setLatestCommit(latesCommit)
                .setWebHookRecieved(webHoocReceived).build()
        }

    }

}