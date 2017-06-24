package com.salmat.util;

import org.acegisecurity.providers.encoding.Md5PasswordEncoder;  
import org.acegisecurity.providers.encoding.MessageDigestPasswordEncoder;  
import org.acegisecurity.providers.encoding.ShaPasswordEncoder;  
  
public class PasswordProcess {  
  
      
    public static void processMd5() {  
          
        // 直接指定待採用的加密演算法（MD5）  
        MessageDigestPasswordEncoder mdpeMd5 = new MessageDigestPasswordEncoder("MD5");  
        // 生成32位的Hex版, 這也是encodeHashAsBase64的預設值  
        mdpeMd5.setEncodeHashAsBase64(false);  
        System.out.println(mdpeMd5.encodePassword("password", null));  
        // 生成24位的Base64版  
        mdpeMd5.setEncodeHashAsBase64(true);  
        System.out.println(mdpeMd5.encodePassword("password", null));  
          
        // 等效於上述代碼  
        Md5PasswordEncoder mpe = new Md5PasswordEncoder();  
        mpe.setEncodeHashAsBase64(false);  
        System.out.println(mpe.encodePassword("password", null));  
        mpe.setEncodeHashAsBase64(true);  
        System.out.println(mpe.encodePassword("password", null));  
    }  
      
    public static void processSha() {  
    	System.out.println("以SHA方式加密......................");  
          
        // 直接指定待採用的加密演算法（SHA）及加密強度（256）  
        MessageDigestPasswordEncoder mdpeSha = new MessageDigestPasswordEncoder("SHA-256");  
        mdpeSha.setEncodeHashAsBase64(false);  
        System.out.println(mdpeSha.encodePassword("aeolus", null));  
        mdpeSha.setEncodeHashAsBase64(true);  
        System.out.println(mdpeSha.encodePassword("aeolus", null));  
          
        // 等效於上述代碼  
        ShaPasswordEncoder spe = new ShaPasswordEncoder(256);         
        spe.setEncodeHashAsBase64(false);  
        System.out.println(spe.encodePassword("jw2222", null));  
        spe.setEncodeHashAsBase64(true);  
        System.out.println(spe.encodePassword("jw2222", null));  
    }  
      
    public static void processSalt() {  
    	System.out.println("以MD5方式加密、加私鑰(鹽)......................");  
          
        Md5PasswordEncoder mpe = new Md5PasswordEncoder();  
        mpe.setEncodeHashAsBase64(false);  
          
        // 等效的兩行地代碼  
        System.out.println(mpe.encodePassword("password{javaee}", null)); // javaee為密碼私鑰  
        System.out.println(mpe.encodePassword("password", "javaee")); // javaee為密碼私鑰  
        // 結果：87ce7b25b469025af0d5c6752038fb56  
    }  
      
    /** 
     * @param args 
     */  
    public static void main(String[] args) {  
//        processMd5();  
        processSha();  
//        processSalt();  
    }  
  
}  
