package io.nais.devrapid

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.Timestamp
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val LOGGER = LoggerFactory.getLogger("devrapid-git-push")

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


    }
    internal fun toProtoBuf(): Message.Pushdata {

        val builder = Message.Pushdata.getDefaultInstance().toBuilder()
        val latestCommit = Timestamp.newBuilder().setSeconds(latestCommit.toEpochSecond()).build()
        val webHookRecieved = Timestamp.newBuilder().setSeconds(webHookRecieved.toEpochSecond()).build()
        return builder
            .setLatestCommitSha(latestCommitSha)
            .setLatestCommit(latestCommit)
            .setWebHookRecieved(webHookRecieved)
            .build()
    }

    fun send () {
        val props = createKafkaConfig()
        val topic = Configuration().topic
        val record = toProtoBuf()

        KafkaProducer<String, Message.Pushdata>(props).use { producer ->

            val key = "alice"

            LOGGER.info("Producing record: $key\t$record")

            producer.send(ProducerRecord(topic, key, record)) { m: RecordMetadata, e: Exception? ->
                when (e) {
                    // no exception, good to go!
                    null -> LOGGER.info("Produced record to topic ${m.topic()} partition [${m.partition()}] @ offset ${m.offset()}")
                    // print stacktrace in case of exception
                    else -> LOGGER.error(e.toString())
                }
            }

            producer.flush()
        }
    }

}