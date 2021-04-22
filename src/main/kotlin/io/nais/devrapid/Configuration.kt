package io.nais.devrapid

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

private fun config() =
    systemProperties() overriding EnvironmentVariables overriding ConfigurationProperties.fromResource("defaults.properties")

data class Configuration(
    val ghWebhookSecret: String = config()[Key("GHWEBHOOKSECRET", stringType)],
    )