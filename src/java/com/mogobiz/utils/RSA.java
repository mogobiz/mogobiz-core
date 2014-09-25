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

    static String rsa = "RSA/NONE/PKCS1PADDING";
    public static byte[] encrypt(byte[] data, InputStream publicKey) throws Exception {
		Key pubKey = readKeyFromFile(publicKey, true);
		Cipher cipher = Cipher.getInstance(rsa);
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
	}
	public static String encrypt(String data, InputStream publicKey) throws Exception{
		Key pubKey = readKeyFromFile(publicKey, true);
		Cipher cipher = Cipher.getInstance(rsa);
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherData = cipher.doFinal(data.getBytes());
		return Base64.encodeBytes(cipherData, 0, cipherData.length, Base64.URL_SAFE);
	}

	public static byte[] decrypt(byte[] cipherData, InputStream privateKey) throws Exception {
		Key prvKey = readKeyFromFile(privateKey, false);
		Cipher cipher = Cipher.getInstance(rsa);
		cipher.init(Cipher.DECRYPT_MODE, prvKey);
		byte[] data = cipher.doFinal(cipherData);
		return data;
	}
	
	public static String decrypt(String cipherData, InputStream privateKey) throws Exception {
		Key prvKey = readKeyFromFile(privateKey, false);
		Cipher cipher = Cipher.getInstance(rsa);
		cipher.init(Cipher.DECRYPT_MODE, prvKey);
        System.out.println(cipher.getProvider().getInfo());
		byte[] data = cipher.doFinal(Base64.decode(cipherData, Base64.URL_SAFE));
		return new String(data);
	}
	public static void main(String[] args) throws Exception {
		RSA.genKeyPair(new File("/Users/hayssams/git/mogobiz/mogobiz-core/grails-app/conf/secretkeys"));
		String dataEncoded64 = RSA.encrypt("{\"storeName\":\"eCommerce\",\"storeCode\":\"ecommerce\",\"owneremail\":\"root@mogobiz.com\",\"ownerfirstname\":\"root\",\"ownerlastname\":\"root\"}", new FileInputStream("/Users/hayssams/git/mogobiz/mogobiz-core/grails-app/conf/secretkeys/public.key"));

//		System.out.println(dataEncoded64);
//		System.out.println("^^^^^^^^^^^^");
//		String data = RSA.decrypt("D3cHhsyst5__sz1Nh75Xk45EGYKFIn94EJb3xH585b0zsmZYjkDlXHo-UhVgkGqpw-aMldc5TDYbv0V54Tvmwbh9zBluhzkN9YN0ZAMaZ38DAKlVpee8bY-HQeB2Kgm9wxvDa_62XddvMIDM8ef4DrqWWhXCoO6lj-NiAha_oSzxTb42uAunBFM4Msl8pe0ctDUtyU5sIjTO4gjvjwQeJVKpRPtuGj4TpNANJaFGNbyiEgU-8ue6iD2oTdTTHwy4XOIlxDShgPkuKrzkjYMoHAr9SSCwV_HaxPJj9zL2PKu_GsV9D9isukI1F1jjdL9jt9-HF8PQGHMg2CfHka_oMA==", new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/private.key"));
//		System.out.println(dataEncoded64+"/"+data);
//		byte[] data2 = RSA.encrypt("1245673241657342671437432714632".getBytes(), new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/public.key"));
//		byte[]  data3 = RSA.decrypt("UfGPs5RXSnl74T6s6jUvBmlwt7I_hx4A6VrtxXgb3_6n1So2vM-9xnnGv7D1aAu_G1wp0LbaTYmLU3GduygE9TjzKuWbBG1QEqYQIu-igbWIX9JiR1yNk4MrK996bL_t9g8p46HSzBnN9QjhgtmXh7t_3wqUquAuOtLARxQj2DZETqh_hnk0UUbUhBerphnO0VamySNAu3jo3DOgBPidiC9AS8wk8iBHx8XzXaKNHJ4on-zlm_5GlY0zHSSTG2fEc-Fk7Zw-DEtk8MWMhrIKg-fjy257iCoR4YFFJWpxO1sRZQ5ooHimD4u_dJ85fTLrQogClaXU7oUg3vk1RIOFCg==", new FileInputStream("/Users/hayssams/git/mogopay-core/mogopay/web-app/WEB-INF/secretkeys/private.key"));
//		System.out.println(new String(data3));
//
//
//
        dataEncoded64 = RSA.encrypt("{\"storeName\":\"eCommerce\",\"storeCode\":\"ecommerce\",\"owneremail\":\"root@mogobiz.com\",\"ownerfirstname\":\"root\",\"ownerlastname\":\"root\"}", new FileInputStream("/Users/hayssams/git/mogobiz/mogobiz-core/grails-app/conf/secretkeys/public.key"));
		System.out.println("******");
		System.out.println(dataEncoded64);
        String data = RSA.decrypt(dataEncoded64, new FileInputStream("/Users/hayssams/git/mogobiz/mogobiz-core/grails-app/conf/secretkeys/private.key"));
        System.out.println(dataEncoded64+"/"+data);
        data = RSA.decrypt("S-GPor96y9ALQUd8145NDxxM2n8pwG61x87B5NLuviYue3mdFYk4iYEiUUvsZ5Y70sXFyEOuKfzOUQRDnYsK0AG3_SbzysJswNxTZ3j9KOvKCV3JCaIdYkA71gZBlA5Un9pY-PhzjpgNdU0csSwgQf9Z16fWPMzUYx6rkM6KVGuT5SEjU0eRk_YZucmdgC6xQdUgJTA6FKG4bAS7cbN_F9pCq5_83-ilnxC0Ng0sU_4kh5UAhUm3Fw5tjLCbSkarG-rWIg9QPZvWosaGP39bmXLA6WUixN9MWtu9OrCN-4aFyga2W1kaJGxyfY88YOetiBKdCEpRuHcq8BEO8U_gGQ==", new FileInputStream("/Users/hayssams/git/mogobiz/mogobiz-core/grails-app/conf/secretkeys/private.key"));
		System.out.println(dataEncoded64+"/"+data);
		System.out.println(data);

	}
}