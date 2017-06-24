package com.salmat.pas.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Cascade;

import com.salmat.pas.conf.Constant;


public class JqgridAfpFile implements Serializable {	
	private String newBatchName; //檔名
	private String center;
	private Long serialNo; //一般會等同於newBatchNo，如果newBatchNo被設為vip後，此值可保留原來的值
	private String afpFileNm;
	private String batchOrOnline; //B, O, T, R
	private Long newBatchNo; //會一直增下去，調整vip後則設為0
	private String description;
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
	private String cycleDateStr;
	private Date presTime; //pres轉檔時間
	private Date printTime; //列印時間
	private Date bindTime;  //膠裝時間
	private Date verifyTime; //驗單時間
	private Date packTime; //裝箱時間
	private Date deliverTime; //交寄時間
	private Date beginTransferTime;
	private Date endTransferTime;
	private int volumns;

	public String getCycleDateStr() {
		return cycleDateStr;
	}
	public void setCycleDateStr(String cycleDateStr) {
		this.cycleDateStr = cycleDateStr;
	}

	public JqgridAfpFile() {}


	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}


	public Long getSerialNo() {
		return serialNo;
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

    public String getUpdateDateStr(){
    	if(updateDate == null)
    		return "";
    	else
    		return Constant.yyyyMMddHHmm.format(updateDate);
    }
	public int getVolumns() {
		return volumns;
	}
	public void setVolumns(int volumns) {
		this.volumns = volumns;
	}
	public Date getPresTime() {
		return presTime;
	}
	public String getPresTimeStr(){
    	if(presTime == null)
    		return "";
    	else
    		return Constant.yyyyMMddHHmm.format(presTime);
    }
	public void setPresTime(Date presTime) {
		this.presTime = presTime;
	}
	public Date getPrintTime() {
		return printTime;
	}
	public String getPrintTimeStr(){
    	if(printTime == null)
    		return "";
    	else
    		return Constant.yyyyMMddHHmm.format(printTime);
    }
	
	public void setPrintTime(Date printTime) {
		this.printTime = printTime;
	}
	public Date getBindTime() {
		return bindTime;
	}
	public String getBindTimeStr(){
    	if(bindTime == null)
    		return "";
    	else
    		return Constant.yyyyMMddHHmm.format(bindTime);
    }
	public void setBindTime(Date bindTime) {
		this.bindTime = bindTime;
	}
	public Date getVerifyTime() {
		return verifyTime;
	}
	public String getVerifyTimeStr(){
    	if(verifyTime == null)
    		return "";
    	else
    		return Constant.yyyyMMddHHmm.format(verifyTime);
    }
	public void setVerifyTime(Date verifyTime) {
		this.verifyTime = verifyTime;
	}
	public Date getPackTime() {
		return packTime;
	}
	public String getPackTimeStr(){
    	if(packTime == null)
    		return "";
    	else
    		return Constant.yyyyMMddHHmm.format(packTime);
    }
	public void setPackTime(Date packTime) {
		this.packTime = packTime;
	}
	
	public Date getDeliverTime() {
		return deliverTime;
	}
	public String getDeliverTimeStr(){
    	if(deliverTime == null)
    		return "";
    	else
    		return Constant.yyyyMMddHHmm.format(deliverTime);
    }
	public void setDeliverTime(Date deliverTime) {
		this.deliverTime = deliverTime;
	}
	public String getInsertDateStr(){
		if(insertDate == null)
    		return "";
    	else
    		return Constant.yyyyMMddHHmm.format(insertDate);
	}
	public void setBeginTransferTime(Date beginTransferTime) {
		this.beginTransferTime = beginTransferTime;
	}
	public Date getEndTransferTime() {
		return endTransferTime;
	}
	public Date getBeginTransferTime() {
		return beginTransferTime;
	}
	public void setEndTransferTime(Date endTransferTime) {
		this.endTransferTime = endTransferTime;
	}
	public String getEndTransferTimeStr() {
		if(endTransferTime == null)
			return "";
		return Constant.yyyyMMddHHmm.format(endTransferTime);
	}
	public String getBeginTransferTimeStr() {
		if(beginTransferTime == null)
			return "";
		return Constant.yyyyMMddHHmm.format(beginTransferTime);
	}
	public Integer getSheets() {
		return sheets;
	}
	public void setSheets(Integer sheets) {
		this.sheets = sheets;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
}
