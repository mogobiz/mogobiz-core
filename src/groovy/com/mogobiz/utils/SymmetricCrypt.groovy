package com.mogobiz.utils

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by hayssams on 21/07/14.
 */
class SymmetricCrypt {
    static public String encrypt(String clearText, String cryptoSecret, String cryptoAlgorithm) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        stream.write(clearText.getBytes())
        byte[] bytes = stream.toByteArray()
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm)
        SecretKeySpec cryptoKey = new SecretKeySpec(cryptoSecret.getBytes(), cryptoAlgorithm.split('/')[0])
        cipher.init(Cipher.ENCRYPT_MODE, cryptoKey)
        bytes = cipher.doFinal(bytes)
        boolean useInitializationVector = cryptoAlgorithm.indexOf('/') < 0 ? false : cryptoAlgorithm.split('/')[1].toUpperCase() != 'ECB'
        if (useInitializationVector) {
            def iv = cipher.IV
            def output = [iv.length]
            output.addAll(iv)
            output.addAll(bytes)
            bytes = output as byte[]
        }
        String cryptedData = bytes.encodeBase64().toString()
        return cryptedData
    }

    static public String decrypt(String cryptedData, String cryptoSecret, String cryptoAlgorithm) {
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm)
        SecretKeySpec cryptoKey = new SecretKeySpec(cryptoSecret.getBytes(), cryptoAlgorithm.split('/')[0])
        boolean useInitializationVector = cryptoAlgorithm.indexOf('/') < 0 ? false : cryptoAlgorithm.split('/')[1].toUpperCase() != 'ECB'
        byte[] cryptedBytes = cryptedData.decodeBase64()

        if (useInitializationVector) {
            int ivLen = cryptedBytes[0]
            IvParameterSpec ivSpec = new IvParameterSpec(cryptedBytes, 1, ivLen)

            cipher.init(Cipher.DECRYPT_MODE, cryptoKey, ivSpec)
            cryptedBytes = cipher.doFinal(cryptedBytes, 1 + ivLen, cryptedBytes.length - 1 - ivLen)
        } else {
            cipher.init(Cipher.DECRYPT_MODE, cryptoKey)
            cryptedBytes = cipher.doFinal(cryptedBytes)
        }
        return new String(cryptedBytes)
    }
}
