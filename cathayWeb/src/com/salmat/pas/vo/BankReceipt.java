package com.salmat.pas.vo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@SuppressWarnings("serial")
@Entity
@Table(name = "bankReceipt")
@NamedQueries({
	@NamedQuery(name="BankReceipt.findByPk", query="from BankReceipt where bankReceiptId = ?")
})
public class BankReceipt implements Serializable {	
	@Id
	private String bankReceiptId;
	private Date issueDate;
	private String issueUser;
	private Date receiveDate;
	private String receiveUser;
	private Date matchDate;
	private String matchUser;
	private Date packDate;
	private String packUser;
	private String oldBatchName;
	private Date insertDate;
	private String status;
	private String exceptionStatus;
	private Integer dateSerialNo;
	private Date receiveTime;
	private String center;
	private Integer dateCenterSerialNo;
	private String fxBackReceiver;
	private Date fxBackReceiveDate;
	
	
	//@OneToOne
	//@JoinColumn(name = "oldBatchName", insertable = false, updatable = false, unique = true)
	@ManyToOne	
	@JoinColumn(name = "oldBatchName", insertable = false, updatable = false)
	private ApplyData applyData;
	
	public BankReceipt() {}

	public String getBankReceiptId() {
		return bankReceiptId;
	}

	public void setBankReceiptId(String bankReceiptId) {
		this.bankReceiptId = bankReceiptId;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public String getIssueUser() {
		return issueUser;
	}

	public void setIssueUser(String issueUser) {
		this.issueUser = issueUser;
	}

	public Date getMatchDate() {
		return matchDate;
	}

	public void setMatchDate(Date matchDate) {
		this.matchDate = matchDate;
	}

	public String getMatchUser() {
		return matchUser;
	}

	public void setMatchUser(String matchUser) {
		this.matchUser = matchUser;
	}

	public Date getPackDate() {
		return packDate;
	}

	public void setPackDate(Date packDate) {
		this.packDate = packDate;
	}

	public String getPackUser() {
		return packUser;
	}

	public void setPackUser(String packUser) {
		this.packUser = packUser;
	}

	public String getOldBatchName() {
		return oldBatchName;
	}

	public void setOldBatchName(String oldBatchName) {
		this.oldBatchName = oldBatchName;
	}

	public Date getInsertDate() {
		return insertDate;
	}

	public void setInsertDate(Date insertDate) {
		this.insertDate = insertDate;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	public ApplyData getApplyData() {
		return applyData;
	}

	public void setApplyData(ApplyData applyData) {
		this.applyData = applyData;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExceptionStatus() {
		return exceptionStatus;
	}

	public void setExceptionStatus(String exceptionStatus) {
		this.exceptionStatus = exceptionStatus;
	}

	public Date getReceiveDate() {
		return receiveDate;
	}

	public void setReceiveDate(Date receiveDate) {
		this.receiveDate = receiveDate;
	}

	public String getReceiveUser() {
		return receiveUser;
	}

	public void setReceiveUser(String receiveUser) {
		this.receiveUser = receiveUser;
	}

	public Integer getDateSerialNo() {
		return dateSerialNo;
	}

	public void setDateSerialNo(Integer dateSerialNo) {
		this.dateSerialNo = dateSerialNo;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getCenter() {
		return center;
	}

	public void setCenter(String center) {
		this.center = center;
	}

	public Integer getDateCenterSerialNo() {
		return dateCenterSerialNo;
	}

	public void setDateCenterSerialNo(Integer dateCenterSerialNo) {
		this.dateCenterSerialNo = dateCenterSerialNo;
	}

	public String getFxBackReceiver() {
		return fxBackReceiver;
	}

	public void setFxBackReceiver(String fxBackReceiver) {
		this.fxBackReceiver = fxBackReceiver;
	}

	public Date getFxBackReceiveDate() {
		return fxBackReceiveDate;
	}

	public void setFxBackReceiveDate(Date fxBackReceiveDate) {
		this.fxBackReceiveDate = fxBackReceiveDate;
	}
	
	
	
}
