package dev.luizhenriquevicari.remir.core.profile

import dev.luizhenriquevicari.remir.core.validation.lengthBetween
import dev.luizhenriquevicari.remir.core.validation.notBlankTrimmed
import io.konform.validation.Validation

object ProfileValidation {
    const val DISPLAY_NAME_MIN = 2
    const val DISPLAY_NAME_MAX = 60

    val update = Validation<UpdateProfileRequest> {
        UpdateProfileRequest::displayName {
            notBlankTrimmed()
            lengthBetween(DISPLAY_NAME_MIN, DISPLAY_NAME_MAX)
        }
    }
}
