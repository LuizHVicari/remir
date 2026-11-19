package dev.luizhenriquevicari.remir.server.config

import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseConfigTest {
    private val base = DatabaseConfig(host = "db.example.com", port = 5432, name = "remir", user = "u", password = "p")

    @Test
    fun `jdbc url has no ssl parameter by default`() {
        assertEquals("jdbc:postgresql://db.example.com:5432/remir", base.jdbcUrl)
    }

    @Test
    fun `jdbc url carries the sslmode when configured`() {
        assertEquals("jdbc:postgresql://db.example.com:5432/remir?sslmode=require", base.copy(sslMode = "require").jdbcUrl)
    }
}
