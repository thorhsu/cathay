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
@Table(name = "packStatus")
@NamedQueries({
	@NamedQuery(name="PackStatus.findByPk", query="from PackStatus where packId = ?"),
	@NamedQuery(name="PackStatus.findByPks", query="from PackStatus where packId in (:packIds)"),
	@NamedQuery(name="PackStatus.findByPksOrderUniqueNo", query="from PackStatus where packId in (:packIds) order by firstUniqueNo")
})
public class PackStatus implements Serializable {
	@Id
	private String packId; //yyMMddSubAreaId00n
	private Date cycleDate;
	private String subAreaId;
	private String subAreaTel;
	private String subAreaName;
	private String areaAddress;
	private int status; // 0:裝箱準備 10:已列印標籤清單    20:裝箱中   30:裝箱完成  40:列印超峰清單  50:交寄完成  
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
	
	
	@OneToMany
    @JoinColumn(name="packId") 
    @OrderBy("merger, uniqueNo, applyNo")
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private Set<ApplyData> applyDatas;
	
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

}
