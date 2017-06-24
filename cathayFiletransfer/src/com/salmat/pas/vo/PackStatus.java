package com.salmat.pas.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Cascade;

@SuppressWarnings("serial")
@Entity
@Table(name = "packStatus")
@NamedQueries({
	@NamedQuery(name="PackStatus.findByPk", query="from PackStatus where packId = ?"),
	@NamedQuery(name="PackStatus.findByPks", query="from PackStatus where packId in (:packIds)"),
	@NamedQuery(name="PackStatus.findByPksOrderUniqueNo", query="from PackStatus where packId in (:packIds) order by firstUniqueNo"),
	@NamedQuery(name="PackStatus.findPkLike", query="from PackStatus where packId like ? order by packId desc"),
	@NamedQuery(name="PackStatus.findByCycleDateAndCenter", query="from PackStatus where cycleDate = ? and center = ? and batchOrOnline = ? order by status, firstUniqueNo"),
	@NamedQuery(name="PackStatus.deleteByCycleDateAndCenter", query="delete from PackStatus where cycleDate = ? and center = ? and batchOrOnline = ?"),
	@NamedQuery(name="PackStatus.findNonReported", query="from PackStatus where reported = false")
})
public class PackStatus implements Serializable {
	@Id
	private String packId; //yyMMddSubAreaId00n
	private Date cycleDate;
	private String subAreaId;
	private String serviceCenterNm;
	private String subAreaTel;
	private String subAreaName;
	private String areaAddress;
	private int status; // 0:裝箱準備 10:已列印標籤清單    20:裝箱中   30:裝箱完成  40:列印交寄清單 45:等待貨運 50:交寄完成  
	private String statusNm;
	private String center;
	private Date createDate;
	private boolean back;
	private Date updateDate;
	private int books;
	private int receipts;
	private Date policyScanDate;
	private Date receiptScanDate;
	private Date labelScanDate;
	private String newBatchNms;
	private String policyScanUser;
	private String receiptScanUser;
	private String labelScanUser;
	private String firstUniqueNo;
	private String logisticId;
	private Boolean packCompleted;
	private boolean reported;
	private Integer inusreCard;
	private String batchOrOnline; //B, O, T, R, G
	private String zipCode;
	private Double weight;
	
	
	@OneToMany
    @JoinColumn(name="packId") 
    @OrderBy("merger, uniqueNo, applyNo")
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private Set<ApplyData> applyDatas;
	
	@ManyToOne()
	@JoinColumn(name = "logisticId", insertable = false, updatable = false)
	private LogisticStatus logisticStatus;
	
	public int getBooks() {
		return books;
	}

	public void setBooks(int books) {
		this.books = books;
	}

	public String getSubAreaTel() {
		return subAreaTel;
	}

	public void setSubAreaTel(String subAreaTel) {
		this.subAreaTel = subAreaTel;
	}

	public String getSubAreaName() {
		return subAreaName;
	}

	public void setSubAreaName(String subAreaName) {
		this.subAreaName = subAreaName;
	}

	public String getPackId() {
		return packId;
	}

	public void setPackId(String packId) {
		this.packId = packId;
	}

	public Date getCycleDate() {
		return cycleDate;
	}

	public void setCycleDate(Date cycleDate) {
		this.cycleDate = cycleDate;
	}

	public String getSubAreaId() {
		return subAreaId;
	}

	public void setSubAreaId(String subAreaId) {
		this.subAreaId = subAreaId;
	}

	public String getAreaAddress() {
		return areaAddress;
	}

	public void setAreaAddress(String areaAddress) {
		this.areaAddress = areaAddress;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getStatusNm() {
		return statusNm;
	}

	public void setStatusNm(String statusNm) {
		this.statusNm = statusNm;
	}

	public String getCenter() {
		return center;
	}

	public void setCenter(String center) {
		this.center = center;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public boolean isBack() {
		return back;
	}

	public void setBack(boolean back) {
		this.back = back;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Date getPolicyScanDate() {
		return policyScanDate;
	}

	public void setPolicyScanDate(Date policyScanDate) {
		this.policyScanDate = policyScanDate;
	}

	public Date getReceiptScanDate() {
		return receiptScanDate;
	}

	public void setReceiptScanDate(Date receiptScanDate) {
		this.receiptScanDate = receiptScanDate;
	}

	public Date getLabelScanDate() {
		return labelScanDate;
	}

	public void setLabelScanDate(Date labelScanDate) {
		this.labelScanDate = labelScanDate;
	}

	public String getNewBatchNms() {
		return newBatchNms;
	}

	public void setNewBatchNms(String newBatchNms) {
		this.newBatchNms = newBatchNms;
	}
	
	public Set<String> getNewBatchNmSet(){
		if(newBatchNms == null)
			return new HashSet<String>();
		else{
			String [] newBatchNmArr = newBatchNms.split(",");
			
			if(newBatchNmArr == null || newBatchNmArr.length == 0){
				return new HashSet<String>();
			}else{
				LinkedHashSet<String> set = new LinkedHashSet<String>();
				for(String newBatchNm : newBatchNmArr){
					if(newBatchNm != null && !newBatchNm.trim().equals(""))
					    set.add(newBatchNm.trim());
				}
			    return set;
			}
		}
	}
	public void setNewBatchNmSet(Set<String> newBatchNmSet) {
		if(newBatchNmSet == null)
			this.setNewBatchNms(null);
		else{
		   String newBatchNms = ",";
		   for(String newBatchNm: newBatchNmSet){
			   if(newBatchNm != null && !newBatchNm.trim().equals(""))
				   newBatchNms += ( newBatchNm.trim() + ",");
		   }
		   this.setNewBatchNms(newBatchNms);
		}		
	}
	
	
	

	public String getPolicyScanUser() {
		return policyScanUser;
	}

	public void setPolicyScanUser(String policyScanUser) {
		this.policyScanUser = policyScanUser;
	}

	public String getReceiptScanUser() {
		return receiptScanUser;
	}

	public void setReceiptScanUser(String receiptScanUser) {
		this.receiptScanUser = receiptScanUser;
	}

	public String getLabelScanUser() {
		return labelScanUser;
	}

	public void setLabelScanUser(String labelScanUser) {
		this.labelScanUser = labelScanUser;
	}


	public Set<ApplyData> getApplyDatas() {
		return applyDatas;
	}

	public void setApplyDatas(Set<ApplyData> applyDatas) {
		this.applyDatas = applyDatas;
	}

	public int getReceipts() {
		return receipts;
	}

	public void setReceipts(int receipts) {
		this.receipts = receipts;
	}

	public String getFirstUniqueNo() {
		return firstUniqueNo;
	}

	public void setFirstUniqueNo(String firstUniqueNo) {
		this.firstUniqueNo = firstUniqueNo;
	}

	public String getLogisticId() {
		return logisticId;
	}

	public void setLogisticId(String logisticId) {
		this.logisticId = logisticId;
	}

	public Boolean getPackCompleted() {
		return packCompleted;
	}

	public void setPackCompleted(Boolean packCompleted) {
		this.packCompleted = packCompleted;
	}

	public LogisticStatus getLogisticStatus() {
		return logisticStatus;
	}

	public void setLogisticStatus(LogisticStatus logisticStatus) {
		this.logisticStatus = logisticStatus;
	}

	public boolean isReported() {
		return reported;
	}

	public void setReported(boolean reported) {
		this.reported = reported;
	}

	public Integer getInusreCard() {
		return inusreCard;
	}

	public void setInusreCard(Integer inusreCard) {
		this.inusreCard = inusreCard;
	}

	public String getBatchOrOnline() {
		return batchOrOnline;
	}

	public void setBatchOrOnline(String batchOrOnline) {
		this.batchOrOnline = batchOrOnline;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getServiceCenterNm() {
		return serviceCenterNm;
	}

	public void setServiceCenterNm(String serviceCenterNm) {
		this.serviceCenterNm = serviceCenterNm;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}
	

}
