package com.salmat.pas.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@SuppressWarnings("serial")
@Entity
@Table(name = "sysLog")
public class SysLog implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String logType;
	private String subject;
	private String messageBody;
	private Boolean isException;
	private String errorLog;
	private Date createDate;
	@Transient
	private String createDateStr;
	

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


	public SysLog() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public Boolean getIsException() {
		return isException;
	}

	public void setIsException(Boolean isException) {
		this.isException = isException;
	}

	public String getErrorLog() {
		return errorLog;
	}

	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public String getCreateDateStr() {
		if(createDate != null)
			return sdf.format(createDate);
		else
			return null;
	}
	public void setCreateDateStr(String createDateStr) {
		this.createDateStr = createDateStr;
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
