package dev.luizhenriquevicari.remir.server.support

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Base64
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/** In-memory RSA key pair + fake JWKS. Stands in for Clerk in tests. */
object TestJwt {
    const val ISSUER = "https://remir-test.clerk.accounts.dev"
    private const val KEY_ID = "test-key"

    private val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    private val publicKey = keyPair.public as RSAPublicKey
    private val privateKey = keyPair.private as RSAPrivateKey

    private val jwk: Jwk = Jwk.fromValues(
        mapOf(
            "kid" to KEY_ID,
            "kty" to "RSA",
            "alg" to "RS256",
            "use" to "sig",
            "n" to publicKey.modulus.toBase64Url(),
            "e" to publicKey.publicExponent.toBase64Url(),
        ),
    )

    /** In-memory equivalent of `$CLERK_ISSUER/.well-known/jwks.json`. */
    val jwkProvider = JwkProvider { kid -> jwk.takeIf { kid == KEY_ID } ?: throw SigningKeyNotFoundException(kid, null) }

    /** Creates a token shaped like Clerk's (`sub` = `user_...`). */
    fun token(
        subject: String,
        issuer: String = ISSUER,
        expiresIn: Duration = 5.minutes,
    ): String {
        val now = Instant.now()
        return JWT.create()
            .withKeyId(KEY_ID)
            .withIssuer(issuer)
            .withSubject(subject)
            .withIssuedAt(now)
            .withNotBefore(now)
            .withExpiresAt(now.plus(expiresIn.toJavaDuration()))
            .sign(Algorithm.RSA256(publicKey, privateKey))
    }

    /** Unsigned big-endian bytes, base64url without padding (JWK format). */
    private fun BigInteger.toBase64Url(): String {
        val bytes = toByteArray().let { if (it.size > 1 && it[0] == 0.toByte()) it.copyOfRange(1, it.size) else it }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
