package io.nais.devrapid

import java.time.LocalDateTime

class PushData (
    val latestCommitSha: String,
    val latestCommit: LocalDateTime,
    val webHookRecieved: LocalDateTime
    ){
}