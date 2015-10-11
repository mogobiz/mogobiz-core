/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;

/**
 * Created by hayssams on 21/07/14.
 */
public class SymmetricCrypt {
    static public String encrypt(String clearText, String cryptoSecret, String cryptoAlgorithm) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(clearText.getBytes());
        byte[] bytes = stream.toByteArray();
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        SecretKeySpec cryptoKey = new SecretKeySpec(cryptoSecret.getBytes(), cryptoAlgorithm.split("/")[0]);
        cipher.init(Cipher.ENCRYPT_MODE, cryptoKey);
        bytes = cipher.doFinal(bytes);
        boolean useInitializationVector = cryptoAlgorithm.indexOf('/') < 0 ? false : cryptoAlgorithm.split("/")[1].toUpperCase() != "ECB";
        if (useInitializationVector) {
            byte[] iv = cipher.getIV();
            byte[] out2 = new byte[iv.length+1+bytes.length];
            out2[0] = (byte)iv.length;
            System.arraycopy(iv, 0, out2, 1, iv.length);
            System.arraycopy(bytes, 0, out2, 1+iv.length, bytes.length);
            bytes = out2;
        }
        String cryptedData = Base64.encodeBytes(bytes, 0, bytes.length, Base64.URL_SAFE);
        return cryptedData;
    }

    static public String decrypt(String cryptedData, String cryptoSecret, String cryptoAlgorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        SecretKeySpec cryptoKey = new SecretKeySpec(cryptoSecret.getBytes(), cryptoAlgorithm.split("/")[0]);
        boolean useInitializationVector = cryptoAlgorithm.indexOf('/') < 0 ? false : cryptoAlgorithm.split("/")[1].toUpperCase() != "ECB";
        byte[] cryptedBytes = Base64.decode(cryptedData, Base64.URL_SAFE);

        if (useInitializationVector) {
            int ivLen = cryptedBytes[0];
            IvParameterSpec ivSpec = new IvParameterSpec(cryptedBytes, 1, ivLen);

            cipher.init(Cipher.DECRYPT_MODE, cryptoKey, ivSpec);
            cryptedBytes = cipher.doFinal(cryptedBytes, 1 + ivLen, cryptedBytes.length - 1 - ivLen);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, cryptoKey);
            cryptedBytes = cipher.doFinal(cryptedBytes);
        }
        return new String(cryptedBytes);
    }
    public static void main(String[] args) throws Exception {
        String input = "{\"storeName\":\"eCommerce\",\"storeCode\":\"ecommerce\",\"owneremail\":\"root@mogobiz.com\",\"ownerfirstname\":\"root\",\"ownerlastname\":\"root\"}";
        String data = encrypt(input, "1234567890123456", "AES");
        String res = decrypt(data, "1234567890123456", "AES");
        System.out.println(res+"/"+data);
    }
}
