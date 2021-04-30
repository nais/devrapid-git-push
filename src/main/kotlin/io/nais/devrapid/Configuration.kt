package io.nais.devrapid

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import java.util.*

private fun config() =
    systemProperties() overriding EnvironmentVariables overriding ConfigurationProperties.fromResource("defaults.properties")

data class Configuration(
    val ghWebhookSecret: String = config()[Key("GHWEBHOOK", stringType)],
    val topic:String = "nais-analyse.devrapid-temp"
    )


fun createKafkaConfig(): Properties {
    val props = Properties()
    props["bootstrap.servers"] = config()[Key("KAFKA_BROKERS", stringType)]
    props["security.protocol"] = "SSL"
    props["schema.registry.url"] = config()[Key("KAFKA_SCHEMA_REGISTRY", stringType)]
    props["schema.registry.user"] = config()[Key("KAFKA_SCHEMA_REGISTRY_USER", stringType)]
    props["schema.registry.password"] = config()[Key("KAFKA_SCHEMA_REGISTRY_PASSWORD", stringType)]
    props["ssl.truststore.location"] = config()[Key("KAFKA_TRUSTSTORE_PATH", stringType)]
    props["ssl.truststore.password"] = config()[Key("KAFKA_CREDSTORE_PASSWORD", stringType)]
    props["ssl.keystore.type"] = "PKCS12"
    props["ssl.keystore.location"] = config()[Key("KAFKA_KEYSTORE_PATH", stringType)]
    props["ssl.keystore.password"] = config()[Key("KAFKA_CREDSTORE_PASSWORD", stringType)]
//    props["ssl.key.password"] = "secret"
    props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
    props["value.serializer"] = "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer"
    return props
}

