package dev.luizhenriquevicari.remir.server.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

/**
 * Transaction boundary. Called ONLY by Services.
 * Runs on `Dispatchers.IO` because JDBC blocks (see the KDoc of `suspendTransaction`).
 */
suspend fun <T> dbQuery(block: suspend JdbcTransaction.() -> T): T =
    withContext(Dispatchers.IO) { suspendTransaction { block() } }
