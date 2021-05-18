package io.nais.devrapid

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.Timestamp
import com.google.protobuf.Any
import io.nais.devrapid.github.Message
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
    val webHookRecieved: ZonedDateTime,
    val ref: String,
    val masterBranch: String,
    val programmingLanguage: String,
    val repositoryName: String,
    val privateRepo: Boolean,
    val organizationName: String,
    val filesDeleted: Int,
    val filesAdded: Int,
    val filesModified: Int,
    val commitMessages: List<String>,
    val coAuthors: Int

) {
    companion object Converter {
        fun fromJson(payload: String): PushData {
            val node: JsonNode = ObjectMapper().readTree(payload)
            val commitMessages = node.at("/commits").messages()
            return PushData(
                latestCommitSha = node.at("/head_commit/id").asText(),
                latestCommit = ZonedDateTime.from(
                    DateTimeFormatter.ISO_DATE_TIME.parse(
                        node.at("/head_commit/timestamp").asText()
                    )
                ),
                webHookRecieved = ZonedDateTime.now(ZoneId.of("Europe/Oslo")),
                ref = node.at("/ref").asText(),
                masterBranch = node.at("/repository/master_branch").asText(),
                programmingLanguage = node.at("/repository/language").asText(),
                repositoryName = node.at("/repository/name").asText(),
                privateRepo = node.at("/organization/private").asBoolean(),
                organizationName = node.at("/repository/organization").asText(),
                filesDeleted = node.at("/commits").count("/removed"),
                filesAdded = node.at("/commits").count("/added"),
                filesModified = node.at("/commits").count("/modified"),
                commitMessages = commitMessages,
                coAuthors = commitMessages.filter { it.toLowerCase().contains("co-authored-by") }.count()

            )
        }

        private fun JsonNode.count(operation: String): Int =
            this.asIterable().map { node -> node.at(operation).asIterable().count() }.sum()

        private fun JsonNode.messages(): List<String> =
            this.asIterable().map { node -> node.at("/message").asText() }
    }

    internal fun toProtoBuf(): Message.Push {

        val builder = Message.Push.getDefaultInstance().toBuilder()
        val latestCommit = Timestamp.newBuilder().setSeconds(latestCommit.toEpochSecond()).build()
        val webHookRecieved = Timestamp.newBuilder().setSeconds(webHookRecieved.toEpochSecond()).build()

        return builder
            .setLatestCommitSha(latestCommitSha)
            .setLatestCommit(latestCommit)
            .setWebHookRecieved(webHookRecieved)
            .setRef(ref)
            .setMasterBranch(masterBranch)
            .setProgrammingLanguage(programmingLanguage)
            .setRepositoryName(repositoryName)
            .setPrivateRepo(privateRepo)
            .setOrganizationName(organizationName)
            .setFilesAdded(filesAdded)
            .setFilesDeleted(filesDeleted)
            .setFilesModified(filesModified)
            .addAllCommitMessages(commitMessages)
            .setCoAuthors(coAuthors)
            .build()
    }

    fun send() {
        val props = createKafkaConfig()
        val topic = Configuration().topic
        val record = Any.pack(toProtoBuf()).toByteArray()

        KafkaProducer<String, ByteArray>(props).use { producer ->

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

    fun pushOnMaster(): Boolean {
        return ref.substringAfterLast("/") == masterBranch
    }


}