package com.mogobiz.utils;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class RSA {

	public static void genKeyPair(File folder) throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.genKeyPair();
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(),
				RSAPublicKeySpec.class);
		RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(),
				RSAPrivateKeySpec.class);
        folder.mkdirs();
		saveToFile(new File(folder, "public.key"), pub.getModulus(), pub.getPublicExponent());
		saveToFile(new File(folder, "private.key"), priv.getModulus(), priv.getPrivateExponent());
	}

	private static void saveToFile(File file, BigInteger mod,
			BigInteger exp) throws IOException {
		ObjectOutputStream oout = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(file)));
		try {
			oout.writeObject(mod);
			oout.writeObject(exp);
		} catch (Exception e) {
			throw new IOException("Unexpected error", e);
		} finally {
			oout.close();
		}
	}

	private static Key readKeyFromFile(InputStream keyFile, boolean pub)
			throws IOException {
		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(keyFile));
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			KeyFactory fact = KeyFactory.getInstance("RSA");
			if (pub) {
				RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
				return fact.generatePublic(keySpec);
			}
			else {
				RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
				return fact.generatePrivate(keySpec);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Spurious serialisation error", e);
		} finally {
			oin.close();
		}
	}

	public static byte[] encrypt(byte[] data, InputStream publicKey) throws Exception {
		Key pubKey = readKeyFromFile(publicKey, true);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
	}

	public static String encrypt(String data, InputStream publicKey) throws Exception{
		Key pubKey = readKeyFromFile(publicKey, true);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherData = cipher.doFinal(data.getBytes());
		return Base64.encodeBytes(cipherData, 0, cipherData.length, Base64.URL_SAFE);
	}

	public static byte[] decrypt(byte[] cipherData, InputStream privateKey) throws Exception {
		Key prvKey = readKeyFromFile(privateKey, false);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, prvKey);
		byte[] data = cipher.doFinal(cipherData);
		return data;
	}
	
	public static String decrypt(String cipherData, InputStream privateKey) throws Exception {
		Key prvKey = readKeyFromFile(privateKey, false);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, prvKey);
		byte[] data = cipher.doFinal(Base64.decode(cipherData, Base64.URL_SAFE));
		return new String(data);
	}
	public static void main(String[] args) throws Exception {
		RSA.genKeyPair(new File("/Users/hayssams/git/mogobiz/mogobiz-core/grails-app/conf/secretkeys"));
//		String dataEncoded64 = RSA.encrypt("hayssam@saleh.fr;customer_email;1234567890123456;AZERAZERAZERAZERAZERAZER", new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/public.key"));
//		System.out.println(dataEncoded64);
//		System.out.println("^^^^^^^^^^^^");
//		String data = RSA.decrypt("D3cHhsyst5__sz1Nh75Xk45EGYKFIn94EJb3xH585b0zsmZYjkDlXHo-UhVgkGqpw-aMldc5TDYbv0V54Tvmwbh9zBluhzkN9YN0ZAMaZ38DAKlVpee8bY-HQeB2Kgm9wxvDa_62XddvMIDM8ef4DrqWWhXCoO6lj-NiAha_oSzxTb42uAunBFM4Msl8pe0ctDUtyU5sIjTO4gjvjwQeJVKpRPtuGj4TpNANJaFGNbyiEgU-8ue6iD2oTdTTHwy4XOIlxDShgPkuKrzkjYMoHAr9SSCwV_HaxPJj9zL2PKu_GsV9D9isukI1F1jjdL9jt9-HF8PQGHMg2CfHka_oMA==", new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/private.key"));
//		System.out.println(dataEncoded64+"/"+data);
//		byte[] data2 = RSA.encrypt("1245673241657342671437432714632".getBytes(), new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/public.key"));
//		byte[]  data3 = RSA.decrypt(data2, new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/private.key"));
//		System.out.println(new String(data3));
//
//
//
//		dataEncoded64 = RSA.encrypt("demande;1379067702831;039142286;yoann.baudy@ebiznext.com", new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/public.key"));
//		System.out.println("******");
//		System.out.println(dataEncoded64);
//		data = RSA.decrypt("g_U1K1bLqGRuz1v66Al7pcCa_TgwO48kdhh7hAcBGrOFCKw-08f-00okShMoBrvAo26zEFl2sDicr_f_AqRZ7LtM7ngzCpYU7Z-HRk7o75-IunVvtuo4hyY_bN3Q-wdnRPgsO_0-L6_q9-INL5CLcbwQ0pbP2NS4gcJrQ-Gn18vyvLWJVHNHffbiVW3mkzsJngAe8IMSsXhvWaMg0tR9PDnR_C78720HlGS5BMgabUTIsx5orGoUNBEjYVVMWwZtrdhTphm1XRgQExN8YrYqieoCuTdjUlQvqA80jjM0df73qOke163f1W1Jpzbvk4FuUW8a8DXVtJ0MdbUladhj2A==", new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/private.key"));
//		System.out.println(dataEncoded64+"/"+data);
//		System.out.println(data);

	}
}