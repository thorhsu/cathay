package com.salmat.pas.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@SuppressWarnings("serial")
@Entity
@Table(name = "t_sys_admpageuser_delete")
@NamedQueries({
	@NamedQuery(name="AdminUserDelete.findById", query="from AdminUserDelete where id=?")
})
public class AdminUserDelete implements Serializable {
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
	private Integer version;
	private Timestamp insertDate;
	private String insertUser;
	private Timestamp updateDate;
	private String updateUser;
	private Timestamp deleteDate;
	private String deleteUser;
		
	public AdminUserDelete() {}

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

	public Timestamp getDeleteDate() {
		return deleteDate;
	}

	public void setDeleteDate(Timestamp deleteDate) {
		this.deleteDate = deleteDate;
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

	public String getDeleteUser() {
		return deleteUser;
	}

	public void setDeleteUser(String deleteUser) {
		this.deleteUser = deleteUser;
	}

	public String toString() {
//		return (new ReflectionToStringBuilder(this) {
//			protected boolean accept(Field f) {
//				return super.accept(f) && !f.getName().equals("userPassword");
//	        }
//	    }).toString();
		return ReflectionToStringBuilder.toString(this);
	}
}
