package dev.luizhenriquevicari.remir.app.features.legal

import dev.luizhenriquevicari.remir.core.legal.AcceptLegalDocumentRequest
import dev.luizhenriquevicari.remir.core.legal.AdminLegalDocumentResource
import dev.luizhenriquevicari.remir.core.legal.LegalAcceptanceResource
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentDto
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentResource
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentType
import dev.luizhenriquevicari.remir.core.legal.LegalStatusDto
import dev.luizhenriquevicari.remir.core.legal.MyLegalStatusResource
import dev.luizhenriquevicari.remir.core.legal.PublishLegalDocumentRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** HTTP calls of the legal feature, built on the `core` Resources. Errors surface as `ApiException`. */
class LegalApi(private val client: HttpClient) {
    suspend fun pending(): LegalStatusDto = client.get(MyLegalStatusResource()).body()

    suspend fun current(type: LegalDocumentType): LegalDocumentDto = client.get(LegalDocumentResource(type)).body()

    suspend fun accept(type: LegalDocumentType, version: Int) {
        client.post(LegalAcceptanceResource(type)) {
            contentType(ContentType.Application.Json)
            setBody(AcceptLegalDocumentRequest(version))
        }
    }

    suspend fun publish(type: LegalDocumentType, request: PublishLegalDocumentRequest): LegalDocumentDto =
        client.post(AdminLegalDocumentResource(type)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
