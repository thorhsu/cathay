package com.salmat.pas.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Cascade;

@SuppressWarnings("serial")
@Entity
@Table(name = "t_admpageuser")
@NamedQueries({
	@NamedQuery(name="AdminUser.findAllUser", query="from AdminUser "),
	@NamedQuery(name="AdminUser.findByUserIdAndPass", query="from AdminUser where userId=? and userPassword=?"),
	@NamedQuery(name="AdminUser.findByUserId", query="from AdminUser where userId=?"),
	@NamedQuery(name="AdminUser.findByUserRole", query="from AdminUser where userRole=?"),
	@NamedQuery(name="AdminUser.findById", query="from AdminUser where id=?"),
	@NamedQuery(name="AdminUser.deletById", query="delete from AdminUser where id = ?")
})
public class AdminUser implements Serializable {
	@ManyToOne()
	@JoinColumn( 
		name="userRole", 
		insertable = false, 
		updatable = false 
	)
	private AdminUserRole adminUserRole;
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
//	@Column(name = "u_id") // 在欄位名稱與屬性名稱不同時使用
	private Long id;
	private String userId;
	private String userName;
	private String password;
	private String email;
	private String userRole;
	private Timestamp enableStart;
	private Timestamp enableEnd;
	private String status;
	private String center;
	private Integer errorCounter;
	
	@Version
	private Integer version;
	private Timestamp insertDate;
	private Timestamp updateDate;
	private String insertUser;
	private String updateUser;
	

	public AdminUser() {}

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

	public String getCenter() {
		return center;
	}

	public void setCenter(String center) {
		this.center = center;
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

	public Timestamp getInsertDate() {
		return insertDate;
	}

	public void setInsertDate(Timestamp insertDate) {
		this.insertDate = insertDate;
	}

	public Timestamp getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}

	public String getInsertUser() {
		return insertUser;
	}

	public void setInsertUser(String insertUser) {
		this.insertUser = insertUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public AdminUserRole getAdminUserRole() {
		return adminUserRole;
	}

	public void setAdminUserRole(AdminUserRole adminUserRole) {
		this.adminUserRole = adminUserRole;
	}

	public String toString() {
//		return (new ReflectionToStringBuilder(this) {
//			protected boolean accept(Field f) {
//				return super.accept(f) && !f.getName().equals("userPassword");
//	        }
//	    }).toString();
		return ReflectionToStringBuilder.toString(this);
	}
	public Integer getErrorCounter() {
		return errorCounter;
	}

	public void setErrorCounter(Integer errorCounter) {
		if(errorCounter != null && errorCounter > 5)
			setStatus("0");
		this.errorCounter = errorCounter;
	}
	
	public AdminUser simpleClone(){
		AdminUser admin = new AdminUser();
		admin.setEmail(this.getEmail());
		admin.setEnableEnd(this.getEnableEnd());
		admin.setEnableStart(this.getEnableStart());
		admin.setErrorCounter(this.getErrorCounter());
		admin.setId(this.getId());
		admin.setInsertDate(this.getInsertDate());
		admin.setInsertUser(this.getInsertUser());
		admin.setPassword(this.getPassword());
		admin.setStatus(this.getStatus());
		admin.setUpdateDate(this.getUpdateDate());
		admin.setUpdateUser(this.getUpdateUser());
		admin.setUserId(this.getUserId());
		admin.setUserName(this.getUserName());
		admin.setUserRole(this.getUserRole());
		admin.setCenter(this.getCenter());
		
		return admin;
		
	}

}
