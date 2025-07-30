package com.owensteel.starlingroundup.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/*

    Crypto utility

    Since Android Datastore doesn't natively support
    encryption yet, we must manually encrypt and decrypt
    key values using Cipher and Android Keystore

 */

// Standard name, used to load the Keystore provider
private const val KEYSTORE_PROVIDER = "AndroidKeyStore"

// Transformation string that defines cipher mode and padding
// Uses AES algorithm and GCM authentication mode, which doesn't
// require padding
private const val TRANSFORMATION = "AES/GCM/NoPadding"

class CryptoManager {

    private val keyAlias = "starling_api_key"
    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

    private fun getOrCreateSecretKey(): SecretKey {
        return (keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).apply {
                init(
                    KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        // Settings congruent to our specified transformation
                        // string (see above)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        // By setting this to false, we do not require user
                        // authentication each time the key is used, and we
                        // can encrypt and decrypt without prompting the user
                        // As long as the storage is encrypted, it is usually
                        // safe to allow background access to the token, and
                        // this saves frequent UI interruptions
                        .setUserAuthenticationRequired(false)
                        .build()
                )
        }.generateKey()
    }

    fun encrypt(data: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = cipher.iv
        // Encryption outputs binary data
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        // Pair the initialisation vector (IV) with the ciphertext
        // because GCM requires the same IV to decrypt later
        return iv to encrypted
    }

    fun decrypt(iv: ByteArray, encryptedData: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // We expect a 128-bit tag and, critically, the exact
        // same IV that was used in encryption earlier
        val spec = GCMParameterSpec(128,iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
        return cipher.doFinal(encryptedData).toString(Charsets.UTF_8)
    }

}