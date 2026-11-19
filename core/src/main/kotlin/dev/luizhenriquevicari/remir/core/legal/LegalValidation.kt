package dev.luizhenriquevicari.remir.core.legal

import dev.luizhenriquevicari.remir.core.validation.lengthBetween
import dev.luizhenriquevicari.remir.core.validation.notBlankTrimmed
import io.konform.validation.Validation
import io.konform.validation.constraints.minimum

object LegalValidation {
    const val CONTENT_MAX = 100_000

    val publish =
        Validation<PublishLegalDocumentRequest> {
            PublishLegalDocumentRequest::content {
                notBlankTrimmed()
                lengthBetween(1, CONTENT_MAX)
            }
        }

    val accept =
        Validation<AcceptLegalDocumentRequest> {
            AcceptLegalDocumentRequest::version { minimum(1) }
        }
}
