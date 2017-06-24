package com.salmat.pas.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@SuppressWarnings("serial")
@Entity
@Table(name = "t_admpageuserrole")
@NamedQueries( {
		@NamedQuery(name = "AdminUserRole.findByUserRole", query = "from AdminUserRole where userRole=?"),
		@NamedQuery(name = "AdminUserRole.likeUserRole", query = "from AdminUserRole where userRole like ?"),
		@NamedQuery(name = "AdminUserRole.findByAll", query = "from AdminUserRole where isShow=1 and deleteDate is null")})
public class AdminUserRole implements Serializable {

	
	
	@Id
	private String userRole;
	private String userRoleName;
	private String userRoleDesc;
	//隱藏最高權限的admin，isShow = 0
	private String isShow;
	private String centerOnly;
	@Version
	private Integer version;
	private Timestamp insertDate;
	private Timestamp updateDate;
	private Timestamp deleteDate;
	private String insertUser;
	private String updateUser;
	private String deleteUser;

	public AdminUserRole() {
	}
/*
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
*/
	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getUserRoleName() {
		return userRoleName;
	}

	public void setUserRoleName(String userRoleName) {
		this.userRoleName = userRoleName;
	}

	public String getUserRoleDesc() {
		return userRoleDesc;
	}

	public void setUserRoleDesc(String userRoleDesc) {
		this.userRoleDesc = userRoleDesc;
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

	public String getIsShow() {
		return isShow;
	}

	public void setIsShow(String isShow) {
		this.isShow = isShow;
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
	
	public String getCenterOnly() {
		return centerOnly;
	}
	public void setCenterOnly(String centerOnly) {
		this.centerOnly = centerOnly;
	}
	public void setDeleteUser(String deleteUser) {
		this.deleteUser = deleteUser;
	}

	public String toString() {
		// return (new ReflectionToStringBuilder(this) {
		// protected boolean accept(Field f) {
		// return super.accept(f) && !f.getName().equals("userPassword");
		// }
		// }).toString();
		return ReflectionToStringBuilder.toString(this);
	}
}
