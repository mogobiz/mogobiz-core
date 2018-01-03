/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class SecureCodec {


	public static String encrypt(String text, String seed) throws Exception {
		SecretKeySpec keyspec;
		Cipher cipher;
		keyspec = new SecretKeySpec(hexToBytes(seed), "AES");

		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, keyspec);
			if (text == null || text.length() == 0)
				throw new Exception("Empty string");
			byte[] encrypted = null;
			encrypted = cipher.doFinal(text.getBytes());
			return bytesToHex(encrypted);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String decrypt(String code, String seed) throws Exception {
		SecretKeySpec keyspec;
		Cipher cipher;
		keyspec = new SecretKeySpec(hexToBytes(seed), "AES");

		if (code == null || code.length() == 0)
			throw new Exception("Empty string");

		byte[] decrypted = null;

		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, keyspec);
			decrypted = cipher.doFinal(hexToBytes(code));
		} catch (Exception e) {
			throw new Exception("[decrypt] " + e.getMessage());
		}
		return new String(decrypted);
	}

	public static String genKey() {
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance("AES");
			kgen.init(128); // 192 and 256 bits may not be available
			SecretKey skey = kgen.generateKey();
			return bytesToHex(skey.getEncoded());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static String bytesToHex(byte[] data) {
		if (data == null) {
			return null;
		}

		int len = data.length;
		String str = "";
		for (int i = 0; i < len; i++) {
			if ((data[i] & 0xFF) < 16)
				str = str + "0" + Integer.toHexString(data[i] & 0xFF);
			else
				str = str + Integer.toHexString(data[i] & 0xFF);
		}
		return str;
	}

	public static byte[] hexToBytes(String str) {
		if (str == null) {
			return null;
		} else if (str.length() < 2) {
			return null;
		} else {
			int len = str.length() / 2;
			byte[] buffer = new byte[len];
			for (int i = 0; i < len; i++) {
				buffer[i] = (byte) Integer.parseInt(
						str.substring(i * 2, i * 2 + 2), 16);
			}
			return buffer;
		}
	}
	public static void main(String args[]) throws Exception {
		
		System.out.println(Locale.getDefault().getLanguage());
		String aes = "5c3f3da15cae1bf2bc736b95bda10c78";
		String res = SecureCodec.encrypt("Hello World Hayssam", aes);
		String enc = "2bd1871219dd25dc9dcf1d1367ac5eccd0107810cd443a031cfe42d066291cb7aaa7dfde888ed7671c2f208d6f1217c007c8d7e63c34a2c6a15bbae51f2e41f8b8b9c7d48d7d142feb9da56608a071e79c2a3a50b133dfaef1c25fbce363a3171eb5b67017eafaf155f20dfc770d2dfc08975f595ebb41b49953157135cdd9c7cb19e042ad964da57a555e2bf07314110d648759dc77e2ceaac52302cf96ac2550ae3b8b9120d62770d9df9ed46c3a1023869fee1f74cbedb8a69925f166fae8";
		String dres = SecureCodec.decrypt(enc, aes);

		System.out.println(res);
		System.out.println(dres);
	}


}
