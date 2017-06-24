package com.salmat.pas.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
//import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@Entity
@Table(name = "t_admpageuserauthority")
@NamedQueries({
	@NamedQuery(name="AdmPageUserAuthority.findByRoleNotAccess", query="from AdmPageUserAuthority where roleList not like ? and deleteDate is null"),
	@NamedQuery(name="AdmPageUserAuthority.findByRole", query="from AdmPageUserAuthority where roleList like ? and deleteDate is null"),
	@NamedQuery(name="AdmPageUserAuthority.findByUrl", query="from AdmPageUserAuthority where url = ? and deleteDate is null"),
	@NamedQuery(name="AdmPageUserAuthority.findByAllEnable", query="from AdmPageUserAuthority where deleteDate is null")
})
public class AdmPageUserAuthority implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
//	@Column(name = "n_id") // 在欄位名稱與屬性名稱不同時使用
	private Long id;
	private String roleList;
	private Integer pageId;
	private String url;
	@Version
	private Integer version;
	private Timestamp insertDate;
	private Timestamp updateDate;
	private Timestamp deleteDate;
	private String insertUser;
	private String updateUser;
	private String deleteUser;
	@Transient
	private List<String> allRoles;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getRoleList() {
		return roleList;
	}
	public void setRoleList(String roleList) {
		this.roleList = roleList;
		
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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
	public Integer getPageId() {
		return pageId;
	}
	public void setPageId(Integer pageId) {
		this.pageId = pageId;
	}
	public String toString() {
//		return (new ReflectionToStringBuilder(this) {
//			protected boolean accept(Field f) {
//				return super.accept(f) && !f.getName().equals("userPassword");
//	        }
//	    }).toString();
		return ReflectionToStringBuilder.toString(this);
	}
	
	public List<String> getAllRoles(){		
		if(roleList != null && !roleList.equals("")){
		   String [] roles = roleList.split(",");
		   if(roles.length > 0){
			  allRoles = new ArrayList();
			  for(String role: roles)
			     allRoles.add(role);
		   }
				
		}
		return allRoles;	
	}
	
	public void removeRole(String role){
		if(roleList != null && !roleList.equals("")){
			if(roleList.indexOf("," + role) >= 0)
			    setRoleList (StringUtils.remove(roleList, "," + role));
			else if(roleList.indexOf(role + ",") >= 0)
				setRoleList (StringUtils.remove(roleList, role + ","));
			else if(roleList.indexOf(role) >= 0)
				setRoleList (roleList = StringUtils.remove(roleList, role));
		}
	}
	
	
	public void addRole(String role){		
		List<String> allRoles = getAllRoles();
		if(roleList != null && !roleList.equals("") && role != null && roleList.indexOf(role) < 0 ){
			setRoleList(roleList + "," + role);
		}else if(roleList != null && role != null && roleList.indexOf(role) >= 0){
		}else{
			setRoleList(role);
		}
		
	}
	
	/*
    public List<AdminUserRole> getAdminUserRoles(){
    	
    	
    } 
    */
}
