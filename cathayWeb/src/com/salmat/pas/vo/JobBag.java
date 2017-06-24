package com.salmat.pas.vo;

import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * JobBag entity. @author MyEclipse Persistence Tools
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "job_bag")
@NamedQueries({
	@NamedQuery(name="JobBag.findByAfp", query="from JobBag where afpName = ?")		
})

public class JobBag implements java.io.Serializable {				
	// Fields
    @Id
    @Column(name="JOB_BAG_NO")
	private String jobBagNo;  //UD0001140617001  
    @Column(name="JOB_CODE_ID")
	private String jobCodeId;  //UD0001
    @Column(name="idf_CUST_NO")
	private String custNo;  //UD
    @Column(name="PROG_NM")
	private String progNm; //國泰人壽保單
    @Column(name="IS_LP")
	private Boolean isLp;  //
    @Column(name="IS_MPS")
	private Boolean isMps;
    @Column(name="IS_LG")
	private Boolean isLg;    
    @Column(name="MP_DM_PS")
	private String mpDmPs;
    @Column(name="DISPATCH_TIME")
	private String dispatchTime;
    @Column(name="DEAD_TIME")
	private String deadTime;
    @Column(name="FILENAME")
    private String filename;
    @Column(name="CYCLE_DATE")
	private Date cycleDate;
    @Column(name="RECEIVE_DATE")
	private Date receiveDate;
    @Column(name="DEAD_LINE")
	private Date deadLine;
    @Column(name="ACCOUNTS")
	private Integer accounts;
    @Column(name="PAGES")
	private Integer pages;
    @Column(name="SHEETS")
	private Integer sheets;
    @Column(name="SPLITE_COUNT")
	private Integer spliteCount;
    @Column(name="JOB_BAG_STATUS")
	private String jobBagStatus;
    @Column(name="PREVSTATUS")
	private String prevStatus;
    @Column(name="AFP_NAME")
	private String afpName;
    @Column(name="IS_DAMAGE")
	private Boolean isDamage;
    @Column(name="HAS_DAMAGE")
	private Boolean hasDamage;
    @Column(name="IS_DELETED")
	private Boolean isDeleted;
    @Column(name="DELETED_REASON")
	private String deletedReason;
    @Column(name="DELETED_DATE")
	private Date deletedDate;
    @Column(name="LOG_FILENAME")
	private String logFilename;
    @Column(name="CREATE_DATE")
	private Date createDate;
    @Column(name="COMPLETED_DATE")
	private Date completedDate;
    @Column(name="DAMAGE_COUNT")
	private Integer damageCount;
    @Column(name="Notes")
	private String notes;
	public String getJobBagNo() {
		return jobBagNo;
	}
	public void setJobBagNo(String jobBagNo) {
		this.jobBagNo = jobBagNo;
	}
	public String getJobCodeId() {
		return jobCodeId;
	}
	public void setJobCodeId(String jobCodeId) {
		this.jobCodeId = jobCodeId;
	}
	public String getProgNm() {
		return progNm;
	}
	public void setProgNm(String progNm) {
		this.progNm = progNm;
	}
	public String getCustNo() {
		return custNo;
	}
	public void setCustNo(String custNo) {
		this.custNo = custNo;
	}
	public Boolean getIsLp() {
		return isLp;
	}
	public void setIsLp(Boolean isLp) {
		this.isLp = isLp;
	}
	public Boolean getIsMps() {
		return isMps;
	}
	public void setIsMps(Boolean isMps) {
		this.isMps = isMps;
	}
	public Boolean getIsLg() {
		return isLg;
	}
	public void setIsLg(Boolean isLg) {
		this.isLg = isLg;
	}
	public String getMpDmPs() {
		return mpDmPs;
	}
	public void setMpDmPs(String mpDmPs) {
		this.mpDmPs = mpDmPs;
	}
	public String getDispatchTime() {
		return dispatchTime;
	}
	public void setDispatchTime(String dispatchTime) {
		this.dispatchTime = dispatchTime;
	}
	public String getDeadTime() {
		return deadTime;
	}
	public void setDeadTime(String deadTime) {
		this.deadTime = deadTime;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Date getCycleDate() {
		return cycleDate;
	}
	public void setCycleDate(Date cycleDate) {
		this.cycleDate = cycleDate;
	}
	public Date getReceiveDate() {
		return receiveDate;
	}
	public void setReceiveDate(Date receiveDate) {
		this.receiveDate = receiveDate;
	}
	public Date getDeadLine() {
		return deadLine;
	}
	public void setDeadLine(Date deadLine) {
		this.deadLine = deadLine;
	}
	public Integer getAccounts() {
		return accounts;
	}
	public void setAccounts(Integer accounts) {
		this.accounts = accounts;
	}
	public Integer getPages() {
		return pages;
	}
	public void setPages(Integer pages) {
		this.pages = pages;
	}
	public Integer getSheets() {
		return sheets;
	}
	public void setSheets(Integer sheets) {
		this.sheets = sheets;
	}
	public Integer getSpliteCount() {
		return spliteCount;
	}
	public void setSpliteCount(Integer spliteCount) {
		this.spliteCount = spliteCount;
	}
	public String getJobBagStatus() {
		return jobBagStatus;
	}
	public void setJobBagStatus(String jobBagStatus) {
		this.jobBagStatus = jobBagStatus;
	}
	public String getPrevStatus() {
		return prevStatus;
	}
	public void setPrevStatus(String prevStatus) {
		this.prevStatus = prevStatus;
	}
	public String getAfpName() {
		return afpName;
	}
	public void setAfpName(String afpName) {
		this.afpName = afpName;
	}
	public Boolean getIsDamage() {
		return isDamage;
	}
	public void setIsDamage(Boolean isDamage) {
		this.isDamage = isDamage;
	}
	public Boolean getHasDamage() {
		return hasDamage;
	}
	public void setHasDamage(Boolean hasDamage) {
		this.hasDamage = hasDamage;
	}
	public Boolean getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	public String getDeletedReason() {
		return deletedReason;
	}
	public void setDeletedReason(String deletedReason) {
		this.deletedReason = deletedReason;
	}
	public Date getDeletedDate() {
		return deletedDate;
	}
	public void setDeletedDate(Date deletedDate) {
		this.deletedDate = deletedDate;
	}
	public String getLogFilename() {
		return logFilename;
	}
	public void setLogFilename(String logFilename) {
		this.logFilename = logFilename;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getCompletedDate() {
		return completedDate;
	}
	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
	}
	public Integer getDamageCount() {
		return damageCount;
	}
	public void setDamageCount(Integer damageCount) {
		this.damageCount = damageCount;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
			
	

}