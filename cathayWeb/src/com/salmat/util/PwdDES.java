package com.salmat.util;

import java.io.*;
import java.security.Security;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class PwdDES {
	public static final int kBufferSize = 8192;
	public static final String Algorithm = "DESede";
	private static final byte[] keyBytes = { 0x11, 0x4F, 0x22, 0x58, (byte) 0x88, 0x40,
	        0x38, 0x28, 0x25, 0x76, 0x51, 0x66, 0x42,  0x72, 0x55, 0x10,
	        0x77, 0x29, 0x30, 0x74,  0x48, 0x40, 0x36,  0x12 }; // 24字節的密鑰
	
	public static byte[] encryptMode(byte[] keybyte, byte[] src) {
	    try {
	      // 生成密鑰
	      SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);
	      // 加密
	      Cipher c1 = Cipher.getInstance(Algorithm);
	      c1.init(Cipher.ENCRYPT_MODE, deskey);
	      return c1.doFinal(src);
	    } catch (java.security.NoSuchAlgorithmException e1) {
	      e1.printStackTrace();
	    } catch (javax.crypto.NoSuchPaddingException e2) {
	      e2.printStackTrace();
	    } catch (java.lang.Exception e3) {
	      e3.printStackTrace();
	    }
	    return null;
	  }
	 
	  // keybyte加密密鑰，長度為24字節
	  // src為加密後的缓衝區
	  public static byte[] decryptMode(byte[] keybyte, byte[] src) {
	    try {
	      // 生成密鑰
	      SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);
	      // 解密
	      Cipher c1 = Cipher.getInstance(Algorithm);
	      c1.init(Cipher.DECRYPT_MODE, deskey);
	      return c1.doFinal(src);
	    } catch (java.security.NoSuchAlgorithmException e1) {
	      e1.printStackTrace();
	    } catch (javax.crypto.NoSuchPaddingException e2) {
	      e2.printStackTrace();
	    } catch (java.lang.Exception e3) {
	      e3.printStackTrace();
	    }
	    return null;
	  }
	  
	  public static String getDecPwd(String encPwd){
		  byte[] encByte = Base64.decodeBase64(encPwd.getBytes());
		  byte [] decByte = decryptMode(keyBytes, encByte);
		  return new String(decByte);
	  }
	  
	  public static String getEncPwd(String decPwd){
		  byte [] encByte = encryptMode(keyBytes, decPwd.getBytes());
		  String encodeString = new String(Base64.encodeBase64(encByte));
		  return encodeString;
	  }
		  	  

	  public static void main(String[] args) throws IOException {
		    String s="";
		    String temp="";
		    // 添加新安全算法,如果用JCE就要把它添加进去
		    Security.addProvider(new com.sun.crypto.provider.SunJCE());
		 
		    String szSrc = "aeolus";
		    if(args.length > 0)
		    	szSrc = args[0];
		    System.out.println("加密前的字串:" + szSrc);		    
		    //szSrc=temp;
		    String encPwd = PwdDES.getEncPwd(szSrc);
		    System.out.println("加密後的字串:" + encPwd);
		    

		    String decStr = PwdDES.getDecPwd(encPwd);
		    
		    System.out.println("解密後的字串:" + decStr);
		    decStr = PwdDES.getDecPwd("Bjjd18XTjlC9uCe+Y1u/dA==");
		    System.out.println("解密後的字串:" + decStr);
		    
		  }

}
