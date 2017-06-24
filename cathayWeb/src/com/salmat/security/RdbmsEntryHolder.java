package com.salmat.security;

import java.io.Serializable;

import org.acegisecurity.ConfigAttributeDefinition;

/**
 * 
 * Roles URL value object
 * 
 */
public class RdbmsEntryHolder implements Serializable {

	private static final long serialVersionUID = 2317309106087370323L;

	// 保護的URL模式
	private String url;

	// 要求的角色集合
	private ConfigAttributeDefinition cad;

	public String getUrl() {
		return url;
	}

	public ConfigAttributeDefinition getCad() {
		return cad;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setCad(ConfigAttributeDefinition cad) {
		this.cad = cad;
	}

}
