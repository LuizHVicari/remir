package dev.luizhenriquevicari.remir.core.legal

import io.ktor.resources.Resource

/** `GET /legal/{type}`: public HTML page of the current version (the URL given to Google and the stores). */
@Resource("/legal/{type}")
class LegalPageResource(val type: LegalDocumentType)

/** `GET /legal-documents/{type}`: current version as JSON (Markdown content). */
@Resource("/legal-documents/{type}")
class LegalDocumentResource(val type: LegalDocumentType)

/** `POST /legal-documents/{type}/acceptances`: the current user accepts a version. */
@Resource("/legal-documents/{type}/acceptances")
class LegalAcceptanceResource(val type: LegalDocumentType)

/** `GET /me/legal`: documents the current user still has to accept. */
@Resource("/me/legal")
class MyLegalStatusResource

/** `POST /admin/legal-documents/{type}`: admin publishes a new version. */
@Resource("/admin/legal-documents/{type}")
class AdminLegalDocumentResource(val type: LegalDocumentType)
