package io.nais.devrapid


import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("devrapid-git-push")
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
        val payload = String(call.receive(), Charsets.UTF_8)
        LOGGER.info(payload)
        if (verifyPayload(
                key = Configuration().ghWebhookSecret,
                payload = payload,
                signature = call.request.headers["X-Hub-Signature-256"]
            )) {
            call.respond(HttpStatusCode.OK)
            LOGGER.info("signature verified")
        } else {
            call.respond(HttpStatusCode.Forbidden)
            LOGGER.info("signature not verified")
        }
    }
}

fun verifyPayload(key: String, payload: String, signature: String?) =
    signature == "sha256=" + HmacUtils(HmacAlgorithms.HMAC_SHA_256, key).hmacHex(payload)