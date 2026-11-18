package dev.luizhenriquevicari.remir.core.profile

import dev.luizhenriquevicari.remir.core.validation.byField
import io.konform.validation.Invalid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProfileValidationTest {
    @Test
    fun `valid display name passes`() {
        assertTrue(ProfileValidation.update(UpdateProfileRequest("Luiz")).isValid)
    }

    @Test
    fun `short display name fails on the displayName field`() {
        val result = assertIs<Invalid>(ProfileValidation.update(UpdateProfileRequest("L")))
        assertEquals(setOf("displayName"), result.errors.byField().keys)
    }

    @Test
    fun `display name with surrounding spaces fails`() {
        assertIs<Invalid>(ProfileValidation.update(UpdateProfileRequest(" Luiz ")))
    }
}
