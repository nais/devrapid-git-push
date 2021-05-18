package io.nais.devrapid

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PushDataTest {

    @Test
    fun `happy case`() {
        val payload = File("src/test/resources/payload-webcommit.json").readText()
        val pushdata = PushData.fromJson(payload)
        assertThat(pushdata.latestCommit.toString()).isEqualTo("2021-04-26T10:52:34+02:00")
        assertThat(pushdata.latestCommitSha).isEqualTo("a13e3ca15abdb51c9c22f11fedb79a0df460cbab")
        assertThat(pushdata.webHookRecieved).isBefore(ZonedDateTime.now())
        assertThat(pushdata.filesAdded).isEqualTo(2)
        assertThat(pushdata.filesDeleted).isEqualTo(0)
        assertThat(pushdata.filesModified).isEqualTo(2)
        assertThat(pushdata.ref).isEqualTo("refs/heads/main")
        assertThat(pushdata.masterBranch).isEqualTo("main")
        assertThat(pushdata.repositoryName).isEqualTo("aiven-cost")
        assertThat(pushdata.programmingLanguage).isEqualTo("Kotlin")
        assertThat(pushdata.privateRepo).isFalse()
        assertThat(pushdata.organizationName).isEqualTo("navikt")
        assertThat(pushdata.commitMessages).isEqualTo(listOf("Update README.md", "Update README.md again Co-authored-by julenissen"))
        assertThat(pushdata.coAuthors).isEqualTo(1)

    }

    @Test
    fun `to protobuf`() {

        val now = ZonedDateTime.now()
        val data = PushData(
            latestCommit = now,
            latestCommitSha = "123",
            webHookRecieved = now,
            ref = "ref",
            masterBranch = "main",
            programmingLanguage = "kotlin",
            repositoryName = "reponame",
            privateRepo = false,
            organizationName = "nav",
            filesDeleted = 3,
            filesModified = 4,
            filesAdded = 5,
            commitMessages = listOf("commmit", "commit2"),
            coAuthors = 2
        )
        val message = data.toProtoBuf()

        assertEquals("123", message.latestCommitSha)
        assertEquals(now.toEpochSecond(), message.latestCommit.seconds)
        assertEquals(2, data.commitMessages.size)
    }

    @Test
    fun `detect push on master`(){
        val now = ZonedDateTime.now()
        val pushDataOnMaster = PushData(
            latestCommit = now,
            latestCommitSha = "123",
            webHookRecieved = now,
            ref = "refs/heads/main",
            masterBranch = "main",
            programmingLanguage = "kotlin",
            repositoryName = "reponame",
            privateRepo = false,
            organizationName = "nav",
            filesDeleted = 3,
            filesModified = 4,
            filesAdded = 5,
            commitMessages = listOf("commmit", "commit2"),
            coAuthors = 2
        )
        val pushDataOnBranch = PushData(
            latestCommit = now,
            latestCommitSha = "123",
            webHookRecieved = now,
            ref = "refs/heads/featurebranch",
            masterBranch = "main",
            programmingLanguage = "kotlin",
            repositoryName = "reponame",
            privateRepo = false,
            organizationName = "nav",
            filesDeleted = 3,
            filesModified = 4,
            filesAdded = 5,
            commitMessages = listOf("commmit", "commit2"),
            coAuthors = 2
        )
        assertTrue(pushDataOnMaster.pushOnMaster())
        assertFalse(pushDataOnBranch.pushOnMaster())
    }
}