package com.salmat.pas.bo;

import org.acegisecurity.providers.encoding.MessageDigestPasswordEncoder;

public class PasswordService {
	//密碼加密
	private String passwordEncoding;
	private String encodeHashAsBase64;
	
	public PasswordService() {
	}
	
	public String encodeSha(String password) {
        MessageDigestPasswordEncoder mdpeSha =
        	new MessageDigestPasswordEncoder(passwordEncoding);  
        mdpeSha.setEncodeHashAsBase64(Boolean.valueOf(encodeHashAsBase64));
		return mdpeSha.encodePassword(password, null);
	}

	public String getPasswordEncoding() {
		return passwordEncoding;
	}

	public void setPasswordEncoding(String passwordEncoding) {
		this.passwordEncoding = passwordEncoding;
	}

	public String getEncodeHashAsBase64() {
		return encodeHashAsBase64;
	}

	public void setEncodeHashAsBase64(String encodeHashAsBase64) {
		this.encodeHashAsBase64 = encodeHashAsBase64;
	}
	
}