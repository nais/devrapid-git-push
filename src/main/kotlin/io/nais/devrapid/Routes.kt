package io.nais.devrapid


import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.common.TextFormat
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.text.Charsets.UTF_8

private val LOGGER = LoggerFactory.getLogger("devrapid-git-push")

private val pushes =
    Gauge.build().name("github_pushes").help("pushes recevied from github").labelNames("team", "repository").create()
private val verifyPayload = Gauge.build().name("verified_payloads").help("verified payloads").create()
private val unverifyPayload = Gauge.build().name("unverified_payloads").help("unveridfied payloads").create()

fun Route.nais() {
    get("/internal/isalive") {
        call.respondText("UP")
    }
    get("/internal/isready") {
        call.respondText("UP")
    }
    get("/internal/prometheus") {
        val names = call.request.queryParameters.getAll("name")?.toSet() ?: emptySet()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004), HttpStatusCode.OK) {
            TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
        }
    }
}

fun Route.gitPushRoutes() {
    get("github/webhook/push") {
        call.respondText("ok")
    }

    post("github/webhook/push") {
        val payload = String(call.receive(), UTF_8)
        LOGGER.info(payload)
        if (verifyPayload(
                key = Configuration().ghWebhookSecret,
                payload = payload,
                signature = call.request.headers["X-Hub-Signature-256"]
            )
        ) {
            call.respond(HttpStatusCode.OK)
            LOGGER.debug("signature verified")
            verifyPayload.inc()
        } else {
            call.respond(HttpStatusCode.Forbidden)
            val pushdata = extractPushdata(payload)
            LOGGER.debug("signature not verified")
            unverifyPayload.inc()
        }
    }.also {
        pushes.labels("nais-analyse", "aiven-cost").inc()
    }

}

fun extractPushdata(payload: String): Any {
    return Pushdata("", "", LocalDateTime.now())
}

data class Pushdata(val repo: String, val sha: String, val timestamp: LocalDateTime)


fun verifyPayload(key: String, payload: String, signature: String?) =
    signature == "sha256=${HmacUtils(HmacAlgorithms.HMAC_SHA_256, key).hmacHex(payload)}"
