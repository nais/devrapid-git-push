package io.nais.devrapid

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class ExtractPayloadTest {

    @Test
    internal fun `happy case`() {
        val payload = File("src/test/resources/payload-webcommit.json").readText()
        val pushdata = PushData.from(payload)
        assertThat(pushdata.latestCommit.toString()).isEqualTo("2021-04-26T10:52:34+02:00")
        assertThat(pushdata.latestCommitSha).isEqualTo("a13e3ca15abdb51c9c22f11fedb79a0df460cbab")
        assertThat(pushdata.webHookRecieved).isBefore(ZonedDateTime.now())
    }


    @Test
    internal fun wtfISODAte() {
        val candidate = "\"2021-04-26T10:52:34+02:00\""
        DateTimeFormatter.ISO_DATE_TIME.parse(candidate)

    }

}