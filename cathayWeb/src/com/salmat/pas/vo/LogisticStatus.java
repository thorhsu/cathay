package com.salmat.pas.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Cascade;

@SuppressWarnings("serial")
@Entity
@Table(name = "logisticStatus")
@NamedQueries({
	@NamedQuery(name="LogisticStatus.findByPk", query="from LogisticStatus where logisticId = ?"),
	@NamedQuery(name="LogisticStatus.findByAddress", query="from LogisticStatus where address = ? and cycleDate = ? and packDone = false"),
})
public class LogisticStatus implements Serializable {
	@Id
	private String logisticId; 
	private Date cycleDate;
	private String address;
	private String tel;
	private String name;
	private Date scanDate;
	private Integer books;
	private Integer receipts;
	private Integer packs;
	private boolean packDone;
	private String firstUniqueNo;
	private String center;
	private String vendorId;
	private Boolean mailReceipt;
	private Date sentTime;
	private String batchOrOnline; //B, O, T, R, G
	private Double weight;
	
	
	@OneToMany
    @JoinColumn(name="logisticId")
	@OrderBy("firstUniqueNo")
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private Set<PackStatus> packStatuses;


	public String getLogisticId() {
		return logisticId;
	}


	public void setLogisticId(String logisticId) {
		this.logisticId = logisticId;
	}


	public Date getCycleDate() {
		return cycleDate;
	}


	public void setCycleDate(Date cycleDate) {
		this.cycleDate = cycleDate;
	}


	public Date getScanDate() {
		return scanDate;
	}


	public void setScanDate(Date scanDate) {
		this.scanDate = scanDate;
	}


	public Integer getBooks() {
		return books;
	}


	public void setBooks(Integer books) {
		this.books = books;
	}


	public Integer getReceipts() {
		return receipts;
	}


	public void setReceipts(Integer receipts) {
		this.receipts = receipts;
	}


	public Integer getPacks() {
		return packs;
	}


	public void setPacks(Integer packs) {
		this.packs = packs;
	}


	public Set<PackStatus> getPackStatuses() {
		return packStatuses;
	}


	public void setPackStatuses(Set<PackStatus> packStatuses) {
		this.packStatuses = packStatuses;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public boolean isPackDone() {
		return packDone;
	}


	public void setPackDone(boolean packDone) {
		this.packDone = packDone;
	}


	public String getFirstUniqueNo() {
		return firstUniqueNo;
	}

	public void setFirstUniqueNo(String firstUniqueNo) {
		this.firstUniqueNo = firstUniqueNo;
	}


	public String getCenter() {
		return center;
	}


	public void setCenter(String center) {
		this.center = center;
	}


	public String getVendorId() {
		return vendorId;
	}


	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}


	public String getTel() {
		return tel;
	}


	public void setTel(String tel) {
		this.tel = tel;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Date getSentTime() {
		return sentTime;
	}


	public void setSentTime(Date sentTime) {
		this.sentTime = sentTime;
	}


	public Boolean getMailReceipt() {
		return mailReceipt;
	}


	public void setMailReceipt(Boolean mailReceipt) {
		this.mailReceipt = mailReceipt;
	}


	public String getBatchOrOnline() {
		return batchOrOnline;
	}


	public void setBatchOrOnline(String batchOrOnline) {
		this.batchOrOnline = batchOrOnline;
	}


	public Double getWeight() {
		return weight;
	}


	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
		
}
