package dev.luizhenriquevicari.remir.server

import dev.luizhenriquevicari.remir.server.support.remirTestApplication
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthTest {
    @Test
    fun `GET health answers 200 without a token`() =
        remirTestApplication { client ->
            assertEquals(HttpStatusCode.OK, client.get("/health").status)
        }
}
