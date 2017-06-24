package com.salmat.pas.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Cascade;


@SuppressWarnings("serial")
@Entity
@Table(name = "afpFile")
@NamedQueries({
	@NamedQuery(name="AfpFile.findByFileNm", query="from AfpFile where newBatchName = ?"),
	@NamedQuery(name="AfpFile.findLikeFileNm", query="from AfpFile where newBatchName like ?"),
	@NamedQuery(name="AfpFile.findNotFeedBack", query="from AfpFile where (feedback is null or feedback = false) and batchOrOnline <> 'T'"),
	@NamedQuery(name="AfpFile.findInNewBatchNames", query="from AfpFile where newBatchName in (:newBatchNames)"),
	@NamedQuery(name="AfpFile.findMaxSerialNo", query="select max(centerSerialNo) from AfpFile where insertDate > ? and center = ? and batchOrOnline = ? and newBatchName not like '%9999'"),
	@NamedQuery(name="AfpFile.findMaxReceiptSerialNo", query="select max(centerSerialNo) from AfpFile where insertDate > ? and center = ? and batchOrOnline = ? and receipt = true and newBatchName not like '%9999'"),
	@NamedQuery(name="AfpFile.findMaxBatNo", query="select max(serialNo) from AfpFile  where newBatchName not like '%9999'"),
	@NamedQuery(name="AfpFile.findByCycleDate", query="from AfpFile where cycleDate = ?")
})
public class AfpFile implements Serializable {	
	@Id 
	private String newBatchName; //檔名
	private String center;
	private Long serialNo; //一般會等同於newBatchNo，如果newBatchNo被設為vip後，此值可保留原來的值
	private String afpFileNm;
	private String batchOrOnline; //B, O, T, R
	private Long newBatchNo; //會一直增下去，調整vip後則設為1
	private int centerSerialNo;
	private boolean receipt;
	private Date cycleDate;
	private String areaId;
	private String packIds;
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
	private Date presTime; //pres轉檔時間
	private Date printTime; //列印時間
	private Date bindTime;  //膠裝時間
	private Date verifyTime; //驗單時間
	private Date packTime; //裝箱時間
	private Date deliverTime; //交寄時間
	private Date beginTransferTime;
	private Date endTransferTime;
	private Boolean feedback;
	

	
	@OneToMany
    @JoinColumn(name="newBatchName") 
    @OrderBy("areaId")
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private Set<ApplyData> applyDatas;
	

	public AfpFile() {}


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

	public String getPackIds() {
		return packIds;
	}
	public void setPackIds(String packIds) {
		this.packIds = packIds;
	}
	
	public Set<String> getPackIdSet(){
		if(packIds == null)
			return new HashSet<String>();
		else{
			String [] packIdArr = packIds.split(",");
			
			if(packIdArr == null || packIdArr.length == 0){
				return new HashSet<String>();
			}else{
				LinkedHashSet<String> set = new LinkedHashSet<String>();
				for(String packId : packIdArr){
					if(packId != null && !packId.trim().equals(""))
					    set.add(packId.trim());
				}
			    return set;
			}
		}
	}
	public void setPackIdSet(Set<String> packIdSet) {
		if(packIdSet == null || packIdSet.size() == 0)
			this.setPackIds(null);
		else{
		   String packIds = ",";
		   for(String packId: packIdSet){
			   if(packId != null && !packId.trim().equals(""))
				   packIds += (packId.trim() + ",");
		   }
		   this.setPackIds(packIds);
		}		
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

	public Set<ApplyData> getApplyDatas() {
		return applyDatas;
	}


	public void setApplyDatas(Set<ApplyData> applyDatas) {
		this.applyDatas = applyDatas;
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
	public int getCenterSerialNo() {
		return centerSerialNo;
	}


	public void setCenterSerialNo(int centerSerialNo) {
		this.centerSerialNo = centerSerialNo;
	}




	public Date getCycleDate() {
		return cycleDate;
	}


	public void setCycleDate(Date cycleDate) {
		this.cycleDate = cycleDate;
	}


	public Date getPresTime() {
		return presTime;
	}


	public void setPresTime(Date presTime) {
		this.presTime = presTime;
	}


	public Date getPrintTime() {
		return printTime;
	}


	public void setPrintTime(Date printTime) {
		this.printTime = printTime;
	}


	public Date getBindTime() {
		return bindTime;
	}


	public void setBindTime(Date bindTime) {
		this.bindTime = bindTime;
	}


	public Date getVerifyTime() {
		return verifyTime;
	}


	public void setVerifyTime(Date verifyTime) {
		this.verifyTime = verifyTime;
	}


	public Date getPackTime() {
		return packTime;
	}


	public void setPackTime(Date packTime) {
		this.packTime = packTime;
	}


	public Date getDeliverTime() {
		return deliverTime;
	}


	public void setDeliverTime(Date deliverTime) {
		this.deliverTime = deliverTime;
	}


	public Boolean getFeedback() {
		return feedback;
	}


	public void setFeedback(Boolean feedback) {
		this.feedback = feedback;
	}


	public Date getBeginTransferTime() {
		return beginTransferTime;
	}


	public void setBeginTransferTime(Date beginTransferTime) {
		this.beginTransferTime = beginTransferTime;
	}


	public Date getEndTransferTime() {
		return endTransferTime;
	}


	public void setEndTransferTime(Date endTransferTime) {
		this.endTransferTime = endTransferTime;
	}
	

}
