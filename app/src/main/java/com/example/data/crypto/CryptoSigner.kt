package com.example.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.data.model.MeshMessage
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Signature
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec

class CryptoSigner {

    private val keyStore: KeyStore? = runCatching {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }.getOrNull()

    // Host JVM / Robolectric fallback keypair
    private val fallbackKeyPair: KeyPair by lazy {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(256)
        kpg.generateKeyPair()
    }

    init {
        ensureKeyPairExists()
    }

    private fun ensureKeyPairExists() {
        val ks = keyStore ?: return
        runCatching {
            if (!ks.containsAlias(KEY_ALIAS)) {
                val kpg = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC,
                    ANDROID_KEYSTORE
                )
                val parameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                )
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .build()
                kpg.initialize(parameterSpec)
                kpg.generateKeyPair()
            }
        }
    }

    fun getDeviceId(): String {
        return runCatching {
            val pubKey = getPublicKey()
            val digest = MessageDigest.getInstance("SHA-256").digest(pubKey.encoded)
            digest.take(16).joinToString("") { "%02x".format(it) }
        }.getOrDefault("anon-device")
    }

    fun getPublicKeyBase64(): String = Base64.encodeToString(getPublicKey().encoded, Base64.NO_WRAP)

    private fun getPublicKey() =
        (keyStore?.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry)?.certificate?.publicKey
            ?: fallbackKeyPair.public

    fun signMessage(message: MeshMessage): String {
        return runCatching {
            val privateKey = (keyStore?.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry)?.privateKey
                ?: fallbackKeyPair.private
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initSign(privateKey)
            val payloadToSign = getCanonicalPayload(message)
            signature.update(payloadToSign.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
        }.getOrDefault("")
    }

    fun verifySignature(message: MeshMessage): Boolean {
        if (message.signature.isBlank() || message.senderPublicKey.isNullOrBlank()) return false
        return runCatching {
            val encodedKey = Base64.decode(message.senderPublicKey, Base64.NO_WRAP)
            val pubKey = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(encodedKey))
            val expectedId = MessageDigest.getInstance("SHA-256").digest(pubKey.encoded)
                .take(16).joinToString("") { "%02x".format(it) }
            if (expectedId != message.senderId) return false
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initVerify(pubKey)
            val payload = getCanonicalPayload(message)
            signature.update(payload.toByteArray(Charsets.UTF_8))
            val decodedSig = Base64.decode(message.signature, Base64.NO_WRAP)
            signature.verify(decodedSig)
        }.getOrDefault(false)
    }

    private fun getCanonicalPayload(message: MeshMessage): String {
        val fields = listOf(
            message.msgId,
            message.version.toString(),
            message.senderId,
            message.senderPublicKey.orEmpty(),
            message.type.name,
            message.category.name,
            message.priority.name,
            message.latitude?.toString().orEmpty(),
            message.longitude?.toString().orEmpty(),
            message.accuracyM?.toString().orEmpty(),
            message.peopleCount.toString(),
            message.note,
            message.lang,
            message.createdAt.toString()
        )
        return fields.joinToString(separator = "") { "${it.length}:$it" }
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "sos_mesh_ecdsa_key"
    }
}
