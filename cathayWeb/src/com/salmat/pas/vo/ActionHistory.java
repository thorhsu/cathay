package com.salmat.pas.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@SuppressWarnings("serial")
@Entity
@Table(name = "ActionHistory")
public class ActionHistory implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String userId;
	private String userName;
	private String action;
	private Date actionTime;
	private String actionPageUrl;
	private Date insertDate;
	private Date updateDate;
	private String messaage;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


	public ActionHistory() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}
	
	public String getActionDateTime(){
		if(actionTime != null)
			return sdf.format(actionTime);
		else 
			return "";
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Date getActionTime() {
		return actionTime;
	}

	public void setActionTime(Date actionTime) {
		this.actionTime = actionTime;
	}

	public String getActionPageUrl() {
		return actionPageUrl;
	}

	public void setActionPageUrl(String actionPageUrl) {
		this.actionPageUrl = actionPageUrl;
	}

	public Date getInsertDate() {
		return insertDate;
	}

	public void setInsertDate(Date insertDate) {
		this.insertDate = insertDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getMessaage() {
		return messaage;
	}

	public void setMessaage(String messaage) {
		this.messaage = messaage;
	}

	public String toString() {
		// return (new ReflectionToStringBuilder(this) {
		// protected boolean accept(Field f) {
		// return super.accept(f) && !f.getName().equals("userPassword");
		// }
		// }).toString();
		return ReflectionToStringBuilder.toString(this);
	}
}
