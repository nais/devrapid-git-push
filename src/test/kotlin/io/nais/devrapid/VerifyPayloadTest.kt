package io.nais.devrapid

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VerifyPayloadTest {

    private val key = "secret"
    private val payload = "payload"
    private val sha256hash = "sha256=b82fcb791acec57859b989b430a826488ce2e479fdf92326bd0a2e8375a42ba4"

    @Test
    internal fun `null signature returns false`() = assertFalse(verifyPayload(key, payload, null))

    @Test
    internal fun `empty signature returns false`() = assertFalse(verifyPayload(key, payload, ""))

    @Test
    internal fun `wrong signature returns false`() = assertFalse(verifyPayload(key, payload, "wrong"))

    @Test
    internal fun `correct signature returns true`() = assertTrue(verifyPayload(key, payload, sha256hash))
}