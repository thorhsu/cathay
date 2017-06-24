package com.salmat.pas.beans;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.faces.model.SelectItem;

import net.sf.json.JSONArray;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.salmat.pas.bo.AdminUserService;
import com.salmat.pas.bo.PasswordService;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AdminUserHistory;
import com.salmat.pas.vo.AdminUserRole;
import com.salmat.pas.vo.Constants;
import com.salmat.util.Tools;

public class AdminUserAddBean extends BaseBean {
	private String userId;
	private String userName;
	private String password;
	private String passwordConfirm;
	private String email;
	private String userRole;
	private String enableStart; 
	private String enableEnd;
	private String center;
	private String status;
	private List<SelectItem>userRoleList;
	private AdminUserMaintainBean maintainBean;
	private AdminUserService adminUserService;
	private PasswordService passwordService;
	private String roleListJson ;
	private String result;
	
	public AdminUserAddBean() {}
	/**
	 * 寫入資料庫前的資料驗證
	 * @return 錯誤訊息
	 */
	public String validData() {
		if(adminUserService.findUserByUserId(getUserId()) != null) 
	        return "此帳號已存在！"; 
		
		if(!getPassword().equals(getPasswordConfirm()))
			return "兩個欄位密碼不同，請重新確認！";
		
		if(Tools.getTimestampByString(enableStart, Constants.DATE_PATTERN).
				after(Tools.getTimestampByString(enableEnd, Constants.DATE_PATTERN)))
			return "結束日期不可早於開始日期！";
		return "";
	}
	
	/**
	 * 
	 * @return
	 */
	public String add() {
		String errMsg = validData();
		if(errMsg.equals("")) {
			AdminUser adminUser = new AdminUser();
			adminUser.setUserId(getUserId());
			adminUser.setPassword(passwordService.encodeSha(getPassword()));
			adminUser.setUserName(getUserName());
			adminUser.setEmail(getEmail());
			adminUser.setUserRole(getUserRole());
			adminUser.setEnableStart(Tools.getTimestampByString(getEnableStart(), Constants.DATE_PATTERN));
			adminUser.setEnableEnd(Tools.getTimestampByString(getEnableEnd(), Constants.DATE_PATTERN));
			adminUser.setStatus(getStatus());
			adminUser.setInsertUser(getAcegiUser().getUsername());
			adminUser.setInsertDate(new Timestamp(System.currentTimeMillis()));
			adminUser.setCenter(this.getCenter());
			try{
				AdminUserHistory userHistory = new AdminUserHistory();
				userHistory.setInsertUser(getAcegiUser().getUsername());
				userHistory.setAdminUser(adminUser);
				Session session = adminUserService.getAdminUserDao().getHiberTemplate().getSessionFactory().openSession();
				Transaction tx = session.beginTransaction();
				session.save(adminUser);
				session.save(userHistory);
				tx.commit();
				if(session.isOpen())
				    session.close();
				//新增資料成功後更新查詢頁面訊息並導回查詢頁
				//重新取得資料
				maintainBean.retrieveData();
				maintainBean.setResult("新增資料成功！");
				return "success";
			} catch(Exception ex) { //防止樂觀鎖定例外
				setResult("新增資料失敗：" + ex.getMessage());
				return "failure";
			}
		} else {
			//錯誤則於本頁顯示錯誤訊息
			setResult("新增資料失敗：" + errMsg);
			return "failure";
		}
	}

	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getEnableStart() {
		if(StringUtils.trimToEmpty(enableStart).equals("")) 
			return Tools.getCurrDate(Constants.DATE_PATTERN);
		return enableStart;
	}
	public String getEnableEnd() {
		if(StringUtils.trimToEmpty(enableEnd).equals("")) 
			return Tools.getCurrDate(Constants.DATE_PATTERN);
		return enableEnd;
	}
	public PasswordService getPasswordService() {
		return passwordService;
	}
	public void setPasswordService(PasswordService passwordService) {
		this.passwordService = passwordService;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	public String getRoleListJson() {
		if(roleListJson == null || "".equals(roleListJson)){
			getUserRoleList();
		}
		return roleListJson;
	}
	public void setRoleListJson(String roleListJson) {
		this.roleListJson = roleListJson;
	}
	public AdminUserMaintainBean getMaintainBean() {
		return maintainBean;
	}
	public void setMaintainBean(AdminUserMaintainBean maintainBean) {
		this.maintainBean = maintainBean;
	}
	public AdminUserService getAdminUserService() {
		return adminUserService;
	}
	public void setAdminUserService(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}
	public void setEnableStart(String enableStart) {
		this.enableStart = enableStart;
	}
	public void setEnableEnd(String enableEnd) {
		this.enableEnd = enableEnd;
	}
	public String getPasswordConfirm() {
		return passwordConfirm;
	}
	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}
	public List<SelectItem> getUserRoleList() {
		if(userRoleList == null){
		   List<SelectItem> sortItems = new ArrayList<SelectItem>();
		   List<AdminUserRole> roleList = adminUserService.getUserRoleList();
		   for(AdminUserRole userRole : roleList) {
			   sortItems.add(new SelectItem(userRole.getUserRole(), 
				  	userRole.getUserRoleName() + " - " + StringUtils.trimToEmpty(userRole.getUserRoleDesc())));
		   }
		   setUserRoleList(sortItems);
		   this.setRoleListJson(JSONArray.fromObject(roleList).toString());
		}
		return userRoleList;
	}
	public void setUserRoleList(List<SelectItem> userRoleList) {
		this.userRoleList = userRoleList;
	}
	public String getCenter() {
		return center;
	}
	public void setCenter(String center) {
		this.center = center;
	}

}
