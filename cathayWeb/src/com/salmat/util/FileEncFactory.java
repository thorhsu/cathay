package com.salmat.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;

/**
 * 
 * @author Thor Hsu
 */
public class FileEncFactory {
	//檔案解密公鑰
	public static String publicKey = "MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYUAAoGBAOrgpA4aq+erh9U+OnmTGWj5VBuuswsVmeZ/hqTektHDppCCnmHHiUCK9F7Y945U8algFRGMCqQ6Dadz0o/pwwl5YwafsDSMxxDH5CEyxYL7WG2lgnYupY7cJWdsffekwJh08ItfaM6obHfyD4AzKq8sYAE/25RRLMY4RbJoTFiY";
	
	private Signature signature;
	private Cipher pkCipher;
	private Cipher aesCipher;
	private byte[] aesKey;
	private SecretKeySpec aeskeySpec;
	private int AES_Key_Size = 256;
	private static final int bufferedSize = 10240;
	static Logger logger = Logger.getLogger(FileEncFactory.class);

	public FileEncFactory() {
		try {
			signature = Signature.getInstance("DSA");
			// create RSA public key cipher
			pkCipher = Cipher.getInstance("RSA");
			// create AES shared key cipher
			aesCipher = Cipher.getInstance("AES");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加密檔案，使用私鑰
	 * 
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public void encodeFile(PrivateKey privateKey, File file, File enFile)
			throws Exception {
		FileOutputStream fos = null;
		BufferedOutputStream bs = null;
		FileInputStream fin = null;
		BufferedInputStream in = null;
		try {
			fos = new FileOutputStream(enFile);
		    bs = new BufferedOutputStream(fos);
			fin = new FileInputStream(file);
			in = new BufferedInputStream(fin);
			signature.initSign(privateKey);
			byte[] buf = new byte[bufferedSize];

			int bytesRead;
			byte [] signed = signature.sign();
			byte[] signedLen = new byte[1];
			signedLen[0] = (byte) (signed.length);
			bs.write(signedLen); //告知sign的長度			
			bs.write(signed); // 檔頭加入簽名，防止變造
			while ((bytesRead = in.read(buf)) > 0) {
				signature.update(buf); // 對讀入資料加密
				bs.write(buf, 0, bytesRead);
			}
			bs.flush();
			fos.flush();
			logger.info(file.getName() + "加密成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
			throw e;
		} finally {
			if(in != null)
				in.close();
			if(fin != null)
			   fin.close();
			if(bs != null)
				bs.close();
			if(fos != null)
			   fos.close();
			fin = null;
		}
	}

	/**
	 * 解密檔案，使用公鑰
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public void decodeFile(PublicKey publicKey, File enFile, File deFile)
			throws Exception{
		byte[] buffer = new byte[bufferedSize];
		
		FileOutputStream fos = null;
		BufferedOutputStream bs = null;
		FileInputStream fin = null;
		BufferedInputStream in = null;
		try {
			fin = new FileInputStream(enFile);
			in = new BufferedInputStream(fin);
			byte [] signlenB = new byte[1];
			in.read(signlenB);//先讀簽名有多長 
			int signLen = signlenB[0];
			
			byte[] signed = new byte[signLen]; //signature長度
			System.out.println(signed.length);
			in.read(signed); //檔頭為簽名
			
			if (signed != null) {
				signature.initVerify(publicKey);
				
				if (signature.verify(signed)) {
					fos = new FileOutputStream(deFile);
					bs = new BufferedOutputStream(fos);
					int bytesRead;
					while ((bytesRead = in.read(buffer)) > 0) {		   
					   signature.update(buffer);
					   bs.write(buffer, 0, bytesRead);	   
					}
					bs.flush();
					fos.flush();
					System.out.print("解密成功");
				} else {
					System.out.print("解密失敗");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
			throw e;
		} finally {
			if (in != null)
				in.close();
			if (fin != null)
				fin.close();
			if(bs != null)
				bs.close();
			if(fos != null)
				fos.close();
			in = null;
			fin = null;
			fos = null;
			
		}
	}

	public static PublicKey loadPublicKey(String publicKeyStr) {
		try {
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] buffer = decoder.decodeBuffer(publicKeyStr);
			KeyFactory keyFactory = KeyFactory.getInstance("DSA");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
			PublicKey publicKey = (PublicKey) keyFactory
					.generatePublic(keySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();

		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PrivateKey loadPrivateKey(String privateKeyStr) {
		try {
			byte[] buffer = new BASE64Decoder().decodeBuffer(privateKeyStr);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance("DSA");
			PrivateKey privateKey = (PrivateKey) keyFactory
					.generatePrivate(keySpec);
			return privateKey;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		try {

			/*
			 * KeyPairGenerator keyGen = KeyPairGenerator .getInstance("DSA",
			 * "SUN");
			 * 
			 * SecureRandom random = SecureRandom.getInstance("SHA1PRNG",
			 * "SUN"); keyGen.initialize(1024, random); KeyPair pair =
			 * keyGen.generateKeyPair(); PrivateKey priv = pair.getPrivate();
			 * String priString = Base64.encode(priv.getEncoded());
			 * System.out.println(priString); priv = loadPrivateKey(priString);
			 * 
			 * PublicKey pub = pair.getPublic(); String pubString =
			 * Base64.encode(pub.getEncoded()); System.out.println(pubString);
			 */
			PrivateKey pri = loadPrivateKey("MIIBSwIBADCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoEFgIUB4Tb8rnL99MjcnwLqakkYYLB6WQ=");
			PublicKey pub = loadPublicKey("MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYUAAoGBAOrgpA4aq+erh9U+OnmTGWj5VBuuswsVmeZ/hqTektHDppCCnmHHiUCK9F7Y945U8algFRGMCqQ6Dadz0o/pwwl5YwafsDSMxxDH5CEyxYL7WG2lgnYupY7cJWdsffekwJh08ItfaM6obHfyD4AzKq8sYAE/25RRLMY4RbJoTFiY");

			FileEncFactory ff = new FileEncFactory();
			// ff.encodeFile(priv, new File("C:\\tmp\\test\\single_1.tif"),
			// new File("C:\\tmp\\test\\single_1.tif.enc"));
//			ff.encodeFile(pri, new File("d:/tmp/Vmware_OnDemand.zip"),new File("d:/tmp/Vmware_OnDemand.zip.gpg"));
//			new File("d:/tmp/Vmware_OnDemand.zip").renameTo(new File("d:/tmp/Vmware_OnDemand.zip.test"));
			ff.decodeFile(pub, new File("D:/tmp/Vmware_OnDemand.zip.gpg"), new File("D:/tmp/Vmware_OnDemand.zip"));

//			ff.encodeFile(pri, new File("d:/tmp/tensionPAS.zip"),new File("d:/tmp/tensionPAS.zip.gpg"));
//		    new File("d:/tmp/tensionPAS.zip").renameTo(new File("d:/tmp/tensionPAS.zip.test"));
//			ff.decodeFile(pub, new File("D:/tmp/tensionPAS.zip.gpg"), new File("D:/tmp/tensionPAS.zip"));
			
//			ff.encodeFile(pri, new File("d:/tmp/myTest.zip"),new File("d:/tmp/myTest.zip.gpg"));
//			new File("d:/tmp/myTest.zip").renameTo(new File("d:/tmp/myTest.zip.test"));
			ff.decodeFile(pub, new File("D:/tmp/myTest.zip.gpg"), new File("D:/tmp/myTest.zip"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// A random AES key is generated to encrypt files. A key size (AES_Key_Size)
	// of 256 bits is standard for AES:

	public void makeKey() throws NoSuchAlgorithmException {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(AES_Key_Size);
		SecretKey key = kgen.generateKey();
		aesKey = key.getEncoded();
		aeskeySpec = new SecretKeySpec(aesKey, "AES");
	}

	// The file encryption and decryption routines then use the AES cipher:

	public void encrypt(File in, File out) throws IOException,
			InvalidKeyException {
		aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);

		FileInputStream is = new FileInputStream(in);
		CipherOutputStream os = new CipherOutputStream(
				new FileOutputStream(out), aesCipher);

		copy(is, os);

		os.close();
	}

	public void decrypt(File in, File out) throws IOException,
			InvalidKeyException {
		aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);

		CipherInputStream is = new CipherInputStream(new FileInputStream(in),
				aesCipher);
		FileOutputStream os = new FileOutputStream(out);

		copy(is, os);

		is.close();
		os.close();
	}

	private void copy(InputStream is, OutputStream os) throws IOException {
		int i;
		byte[] b = new byte[bufferedSize];
		while ((i = is.read(b)) != -1) {
			os.write(b, 0, i);
		}
	}

	// So that the files can be decrypted later, the AES key is encrypted to a
	// file using the RSA cipher. The RSA public key is assumed to be stored in
	// a file.

	public void saveKey(File out, File publicKeyFile) throws IOException,
			GeneralSecurityException {
		// read public key to be used to encrypt the AES key
		byte[] encodedKey = new byte[(int) publicKeyFile.length()];
		new FileInputStream(publicKeyFile).read(encodedKey);

		// create public key
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey pk = kf.generatePublic(publicKeySpec);

		// write AES key
		pkCipher.init(Cipher.ENCRYPT_MODE, pk);
		
		CipherOutputStream os = new CipherOutputStream(
				new FileOutputStream(out), pkCipher);
		os.write(aesKey);
		os.close();
	}

	// Before decryption can take place, the encrypted AES key must be decrypted
	// using the RSA private key:

	public void loadKey(File in, File privateKeyFile)
			throws GeneralSecurityException, IOException {
		// read private key to be used to decrypt the AES key
		byte[] encodedKey = new byte[(int) privateKeyFile.length()];
		new FileInputStream(privateKeyFile).read(encodedKey);

		// create private key
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedKey);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey pk = kf.generatePrivate(privateKeySpec);

		// read AES key
		pkCipher.init(Cipher.DECRYPT_MODE, pk);
		aesKey = new byte[AES_Key_Size / 8];
		CipherInputStream is = new CipherInputStream(new FileInputStream(in),
				pkCipher);
		is.read(aesKey);
		aeskeySpec = new SecretKeySpec(aesKey, "AES");
	}

	public static String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		FileEncFactory.publicKey = publicKey;
	}

}