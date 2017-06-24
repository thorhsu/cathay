package com.salmat.pas.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Cascade;

@SuppressWarnings("serial")
@Entity
@Table(name = "errorReport")
@NamedQueries({
	@NamedQuery(name="ErrorReport.notReported", query="from ErrorReport where reported = false order by id desc")
	
})
public class ErrorReport implements Serializable {	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String title; //
	private String errorType; //
	private Date errHappenTime; //
	private String messageBody;
	private boolean reported;
	private String oldBatchName;
	private Date reportTime;
	private Boolean exception;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public Date getErrHappenTime() {
		return errHappenTime;
	}

	public void setErrHappenTime(Date errHappenTime) {
		this.errHappenTime = errHappenTime;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public boolean isReported() {
		return reported;
	}

	public void setReported(boolean reported) {
		this.reported = reported;
	}

	public String getOldBatchName() {
		return oldBatchName;
	}

	public void setOldBatchName(String oldBatchName) {
		this.oldBatchName = oldBatchName;
	}

	public Date getReportTime() {
		return reportTime;
	}

	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}

	public Boolean getException() {
		return exception;
	}

	public void setException(Boolean exception) {
		this.exception = exception;
	}
	

}
