package com.salmat.pas.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * JobBag entity. @author MyEclipse Persistence Tools
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "job_bag_splite")
@NamedQueries({
	@NamedQuery(name="JobBagSplit.findByAfp", query="from JobBagSplit where jobBagNo = ?")		
})
public class JobBagSplit implements java.io.Serializable{
        @Id
        @Column(name="JOB_BAG_SPLITE_NO")
		private String jobBagSpliteNo;
        @Column(name="IDF_JOB_BAG_NO")
		private String jobBagNo;
        @Column(name="LP_COMPLETED_DATE_BY_USER")
		private Date lpCompletedDateByUser;
        @Column(name="LP_COMPLETED_DATE_BY_MANAGER")
		private Date lpCompletedDateByManager;
        @Column(name="MP_COMPLETED_DATE_BY_USER")
		private Date mpCompletedDateByUser;
        @Column(name="MP_COMPLETED_DATE_BY_MANAGER")
		private Date mpCompletedDateByManager;
        @Column(name="LG_COMPLETED_DATE_BY_USER")
        private Date lgCompletedDateByUser;
        @Column(name="LG_COMPLETED_DATE_BY_MANAGER")
    	private Date lgCompletedDateByManager;
        
		public Date getLgCompletedDateByUser() {
			return lgCompletedDateByUser;
		}
		public void setLgCompletedDateByUser(Date lgCompletedDateByUser) {
			this.lgCompletedDateByUser = lgCompletedDateByUser;
		}
		public Date getLgCompletedDateByManager() {
			return lgCompletedDateByManager;
		}
		public void setLgCompletedDateByManager(Date lgCompletedDateByManager) {
			this.lgCompletedDateByManager = lgCompletedDateByManager;
		}
		public String getJobBagSpliteNo() {
			return jobBagSpliteNo;
		}
		public void setJobBagSpliteNo(String jobBagSpliteNo) {
			this.jobBagSpliteNo = jobBagSpliteNo;
		}
		public String getJobBagNo() {
			return jobBagNo;
		}
		public void setJobBagNo(String jobBagNo) {
			this.jobBagNo = jobBagNo;
		}
		public Date getLpCompletedDateByUser() {
			return lpCompletedDateByUser;
		}
		public void setLpCompletedDateByUser(Date lpCompletedDateByUser) {
			this.lpCompletedDateByUser = lpCompletedDateByUser;
		}
		public Date getLpCompletedDateByManager() {
			return lpCompletedDateByManager;
		}
		public void setLpCompletedDateByManager(Date lpCompletedDateByManager) {
			this.lpCompletedDateByManager = lpCompletedDateByManager;
		}
		public Date getMpCompletedDateByUser() {
			return mpCompletedDateByUser;
		}
		public void setMpCompletedDateByUser(Date mpCompletedDateByUser) {
			this.mpCompletedDateByUser = mpCompletedDateByUser;
		}
		public Date getMpCompletedDateByManager() {
			return mpCompletedDateByManager;
		}
		public void setMpCompletedDateByManager(Date mpCompletedDateByManager) {
			this.mpCompletedDateByManager = mpCompletedDateByManager;
		}
        
        

}
