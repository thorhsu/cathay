package com.salmat.pas.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@SuppressWarnings("serial")
@Entity
@Table(name = "t_AdmPageUserHistory")
@NamedQueries({
	@NamedQuery(name="AdminUserHistory.findByUserIdAndPass", query="from AdminUser where userId=? and userPassword=?"),
	@NamedQuery(name="AdminUserHistory.findByUserId", query="from AdminUser where userId=?"),
	@NamedQuery(name="AdminUserHistory.deletById", query="delete from AdminUserHistory where aid = ?"),
	@NamedQuery(name="AdminUserHistory.deletByUserId", query="delete from AdminUserHistory where userId = ?")
})
public class AdminUserHistory implements Serializable {
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
//	@Column(name = "u_id") // 在欄位名稱與屬性名稱不同時使用
	private Long id;
	
	private Long aid;
	private String userId;
	private String userName;
	private String password;
	private String email;
	private String userRole;
	private Timestamp enableStart;
	private Timestamp enableEnd;
	private String status;
	private Date insertDate;
	private String insertUser;
	
	@Version
	private Integer version;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public Timestamp getEnableStart() {
		return enableStart;
	}

	public void setEnableStart(Timestamp enableStart) {
		this.enableStart = enableStart;
	}

	public Timestamp getEnableEnd() {
		return enableEnd;
	}

	public void setEnableEnd(Timestamp enableEnd) {
		this.enableEnd = enableEnd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Date getInsertDate() {
		return insertDate;
	}

	public void setInsertDate(Date insertDate) {
		this.insertDate = insertDate;
	}


	public String getInsertUser() {
		return insertUser;
	}

	public void setInsertUser(String insertUser) {
		this.insertUser = insertUser;
	}


	public String toString() {
//		return (new ReflectionToStringBuilder(this) {
//			protected boolean accept(Field f) {
//				return super.accept(f) && !f.getName().equals("userPassword");
//	        }
//	    }).toString();
		return ReflectionToStringBuilder.toString(this);
	}
	
	public Long getAid() {
		return aid;
	}

	public void setAid(Long aid) {
		this.aid = aid;
	}


	public void setNonReleventAdminUser(AdminUser adminUser){
		if(adminUser != null){
			   this.setAid(adminUser.getId());
			   this.setEmail(adminUser.getEmail());
			   this.setEnableEnd(adminUser.getEnableEnd());
			   this.setEnableStart(adminUser.getEnableStart());
			   this.setInsertDate(new Date());
			   this.setPassword(adminUser.getPassword());

			   this.setStatus(adminUser.getStatus());
			   this.setUserId(adminUser.getUserId());
			   this.setUserName(adminUser.getUserName());
			   this.setUserRole(adminUser.getUserRole());
		}
	}
	public void setAdminUser(AdminUser adminUser) {
		if(adminUser != null){
		   this.setAid(adminUser.getId());
		   this.setEmail(adminUser.getEmail());
		   this.setEnableEnd(adminUser.getEnableEnd());
		   this.setEnableStart(adminUser.getEnableStart());
		   this.setInsertDate(new Date());
		   this.setPassword(adminUser.getPassword());

		   this.setStatus(adminUser.getStatus());
		   this.setUserId(adminUser.getUserId());
		   this.setUserName(adminUser.getUserName());
		   this.setUserRole(adminUser.getUserRole());
		}
	}
	

}
