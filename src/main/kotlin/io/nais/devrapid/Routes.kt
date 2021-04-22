package io.nais.devrapid


import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import org.apache.commons.codec.digest.DigestUtils
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
//  signature = 'sha256=' + OpenSSL::HMAC.hexdigest(OpenSSL::Digest.new('sha256'), ENV['SECRET_TOKEN'], payload_body)

    post("github/webhook/push") {
        val payload = call.receiveText()
        val headers = call.request.headers
        LOGGER.info(headers.toString())
        val signature = headers["X-Hub-Signature-256"]
        if (verifyPayload(payload, signature)) {
            call.respond(HttpStatusCode.OK)
            LOGGER.info("signature verified")
        } else {
            call.respond(HttpStatusCode.Forbidden)
            LOGGER.info("signature not verified")
        }
    }

}

fun verifyPayload(payload: String, signature: String?): Boolean {
    val calculatedSignature = "sha256=" + HmacUtils(HmacAlgorithms.HMAC_SHA_256, "superhemmelig").hmacHex(payload)
    LOGGER.info("Signature: $signature")
    LOGGER.info("Calculated signature: $calculatedSignature")
    return calculatedSignature == signature
}