package com.salmat.pas.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Cascade;


public class AfpFileDisplay {	
	private String newBatchName; //檔名
	private String center;
	private Long serialNo; //一般會等同於newBatchNo，如果newBatchNo被設為vip後，此值可保留原來的值
	private String afpFileNm;
	private String batchOrOnline; //B, O, T, R
	private Long newBatchNo; //會一直增下去，調整vip後則設為1
	private boolean receipt;
	private String areaId;
	private String areaName;
	private int matched;
	private int notMatched;
	private int substracts;
	private int errors;
	private int verifiedErrs;
	private int volumns;
	private Integer sheets; //
	private Integer pages; //頁數
	private boolean ziped; 	
	private boolean gpged; //檔案日期
	private boolean transfered;
	private boolean unziped;
	private String status;
	private Date fileDate;
	private Date insertDate;
	private Date updateDate;
	//vip設定相關資訊
	private String vipModifierId;
	private String vipModifierName;
	private Date vipSetTime;


	public AfpFileDisplay() {}


	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}


	public Long getSerialNo() {
		return serialNo;
	}


	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}


	public void setSerialNo(Long serialNo) {
		this.serialNo = serialNo;
	}


	public String getNewBatchName() {
		return newBatchName;
	}


	public Long getNewBatchNo() {
		return newBatchNo;
	}


	public void setNewBatchNo(Long newBatchNo) {
		this.newBatchNo = newBatchNo;
	}


	public String getBatchOrOnline() {
		return batchOrOnline;
	}


	public void setBatchOrOnline(String batchOrOnline) {
		this.batchOrOnline = batchOrOnline;
	}


	public void setNewBatchName(String newBatchName) {
		this.newBatchName = newBatchName;
	}


	public String getCenter() {
		return center;
	}


	public void setCenter(String center) {
		this.center = center;
	}


	public String getAfpFileNm() {
		return afpFileNm;
	}


	public void setAfpFileNm(String afpFileNm) {
		this.afpFileNm = afpFileNm;
	}


	public boolean isZiped() {
		return ziped;
	}


	public void setZiped(boolean ziped) {
		this.ziped = ziped;
	}


	public boolean isGpged() {
		return gpged;
	}


	public void setGpged(boolean gpged) {
		this.gpged = gpged;
	}


	public boolean isTransfered() {
		return transfered;
	}


	public void setTransfered(boolean transfered) {
		this.transfered = transfered;
	}


	public boolean isUnziped() {
		return unziped;
	}


	public void setUnziped(boolean unziped) {
		this.unziped = unziped;
	}


	public Date getFileDate() {
		return fileDate;
	}


	public void setFileDate(Date fileDate) {
		this.fileDate = fileDate;
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

	public String getVipModifierId() {
		return vipModifierId;
	}


	public void setVipModifierId(String vipModifierId) {
		this.vipModifierId = vipModifierId;
	}


	public String getVipModifierName() {
		return vipModifierName;
	}


	public void setVipModifierName(String vipModifierName) {
		this.vipModifierName = vipModifierName;
	}


	public Integer getPages() {
		return pages;
	}


	public void setPages(Integer pages) {
		this.pages = pages;
	}


	public Date getVipSetTime() {
		return vipSetTime;
	}


	public void setVipSetTime(Date vipSetTime) {
		this.vipSetTime = vipSetTime;
	}
	
	
	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public Integer getSheets() {
		return sheets;
	}


	public void setSheets(Integer sheets) {
		this.sheets = sheets;
	}


	public boolean isReceipt() {
		return receipt;
	}


	public void setReceipt(boolean receipt) {
		this.receipt = receipt;
	}


	public String getAreaName() {
		return areaName;
	}


	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}


	public int getMatched() {
		return matched;
	}


	public void setMatched(int matched) {
		this.matched = matched;
	}


	public int getNotMatched() {
		return notMatched;
	}


	public void setNotMatched(int notMatched) {
		this.notMatched = notMatched;
	}


	public int getSubstracts() {
		return substracts;
	}


	public void setSubstracts(int substracts) {
		this.substracts = substracts;
	}


	public int getErrors() {
		return errors;
	}


	public void setErrors(int errors) {
		this.errors = errors;
	}


	public int getVerifiedErrs() {
		return verifiedErrs;
	}


	public void setVerifiedErrs(int verifiedErrs) {
		this.verifiedErrs = verifiedErrs;
	}


	public int getVolumns() {
		return volumns;
	}


	public void setVolumns(int volumns) {
		this.volumns = volumns;
	}
	
	

}
