package com.salmat.pas.beans;

import java.io.IOException;
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
import com.salmat.pas.validator.CommonDataValidator;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AdminUserHistory;
import com.salmat.pas.vo.AdminUserRole;
import com.salmat.pas.vo.Constants;
import com.salmat.util.Tools;

public class AdminUserEditBean extends BaseBean {
	private String userId;
	private String userName;
	private String password;
	private String passwordConfirm;
	private String email;
	private String userRole;
	private String enableStart;
	private String enableEnd;
	private String status;
	private String center;
	private String insertUser;
	private String updateUser;
	private Timestamp insertDate;
	private Timestamp updateDate;
	private List<SelectItem>userRoleList;
	private String roleListJson ;
	private AdminUser adminUser;
	private String initResult;
	private AdminUserMaintainBean maintainBean;
	private AdminUserService adminUserService;
	private PasswordService passwordService;
	private String result;
	
	public AdminUserEditBean() {
	}

	/**
	 * 寫入資料庫前的資料驗證
	 * @return 錯誤訊息
	 */
	public String validData() {
		System.out.println("valid test");
		if(getAdminUser() == null) return "請重新整理頁面！";
		//不可更改目前登入帳號的狀態，如：啟用更改為關閉
		if(getAdminUser().getUserId().equals(getAcegiUser().getUsername()) &&
				 !getAdminUser().getUserRole().equals(getUserRole()))
			return "不可更改目前登入帳號的權限！";
		
		if(getAdminUser().getUserId().equals(getAcegiUser().getUsername()) &&
				!getAdminUser().getStatus().equals(getStatus()))
			return "不可更改目前登入帳號的狀態！";

		if(!getPassword().equals(getPasswordConfirm())) 
			return "兩個欄位密碼不同，請重新確認！";
		
		if(Tools.getTimestampByString(enableStart, Constants.DATE_PATTERN).
				after(Tools.getTimestampByString(enableEnd, Constants.DATE_PATTERN)))
			return "結束日期不可早於開始日期！";
		return "";
	}

	/**
	 * 初始化頁面資訊
	 * @return
	 */
	private String initWebPage() {
		//初始化頁面資訊
		maintainBean.setResult("");
		String id = StringUtils.trimToEmpty(getParameter("id"));
		if(!id.equals("") && CommonDataValidator.isNumber(id, false)) {
			//第一次進入頁面init from request，若request 為空，則load store data from session
			setResult(""); //若為新修改request則重設錯誤訊息
			Session session = adminUserService.getAdminUserDao().getHiberTemplate().getSessionFactory().openSession();
			AdminUser adminUser = (AdminUser)session.load(AdminUser.class, new Long(id));
			//setAdminUser(adminUserService.findUserById(new Long(id)));
			setAdminUser(adminUser);
			//若為最高權限的admin，isShow = 0且無法以request進行資料顯示與修改
			if(getAdminUser() == null || getAdminUser().getAdminUserRole().getIsShow().equals(Constants.FALSE)) {
				try {
					getResponse().sendRedirect("adminUserMaintain.jspx");
				} catch (IOException e) {
					logger.info(e.getMessage());
				}
			} else {
				setUserId(getAdminUser().getUserId());
				setUserName(getAdminUser().getUserName());
				setEmail(getAdminUser().getEmail());
				setUserRole(getAdminUser().getUserRole());
				setEnableStart(Tools.getDateFormat(getAdminUser().getEnableStart(), Constants.DATE_PATTERN));
				setEnableEnd(Tools.getDateFormat(getAdminUser().getEnableEnd(), Constants.DATE_PATTERN));
				setStatus(getAdminUser().getStatus());
				setInsertUser(getAdminUser().getInsertUser());
				setUpdateUser(getAdminUser().getUpdateUser());
				setInsertDate(getAdminUser().getInsertDate());
				setUpdateDate(getAdminUser().getUpdateDate());
				this.setCenter(getAdminUser().getCenter());
			}
			if(session.isOpen())
				session.close();
		} else {
			//錯誤後進入頁面，不再更新webPage，若直接Key網址不傳參數進入，則返回查詢頁
			if(getAdminUser() == null) {
				try {
					getResponse().sendRedirect("adminUserMaintain.jspx");
				} catch (IOException e) {
					logger.info(e.getMessage());
				}
			} 
		}
		return "";
	}
	
	/**
	 * 
	 * @return
	 */
	public String modify() {
		setResult("");
		String errMsg = validData();
		if(errMsg.equals("")) {
			if(!getPassword().equals(""))
			     getAdminUser().setPassword(passwordService.encodeSha(getPassword()));
			getAdminUser().setUserName(getUserName());
			getAdminUser().setEmail(getEmail());
			getAdminUser().setUserRole(getUserRole());
			getAdminUser().setEnableStart(Tools.getTimestampByString(getEnableStart(), Constants.DATE_PATTERN));
			getAdminUser().setEnableEnd(Tools.getTimestampByString(getEnableEnd(), Constants.DATE_PATTERN));
			getAdminUser().setStatus(getStatus());
			getAdminUser().setUpdateUser(getAcegiUser().getUsername());
			getAdminUser().setUpdateDate(new Timestamp(System.currentTimeMillis()));			
			getAdminUser().setCenter(this.getCenter());
			try{
				AdminUserHistory userHistory = new AdminUserHistory();
				userHistory.setInsertUser(getAcegiUser().getUsername());
				userHistory.setAdminUser(getAdminUser());
				Session session = adminUserService.getAdminUserDao().getHiberTemplate().getSessionFactory().openSession();
				Transaction tx = session.beginTransaction();
				session.update(getAdminUser());
				session.save(userHistory);
				tx.commit();
				if(session.isOpen())
					session.close();
				//重新取得資料
				maintainBean.retrieveData();
				maintainBean.setResult("修改資料成功！");
				return "success";
			} catch(Exception ex) { //避免樂觀鎖定例外
				setResult("修改資料失敗：" + ex.getMessage());
				return "failure";
			}

		} else {
			setResult("修改資料失敗：" + errMsg);
			return "failure";
		}
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
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

	/**
	 * 於此初始化store，呼叫時請直接使用this.title
	 * @return
	 */
	public String getInitResult() {
		initResult = initWebPage();
		return initResult;
	}
	
	public void setInitResult(String initResult) {
		this.initResult = initResult;
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

	public String getEnableStart() {
		return enableStart;
	}

	public void setEnableStart(String enableStart) {
		this.enableStart = enableStart;
	}

	public String getEnableEnd() {
		return enableEnd;
	}

	public void setEnableEnd(String enableEnd) {
		this.enableEnd = enableEnd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public AdminUser getAdminUser() {
		return adminUser;
	}

	public void setAdminUser(AdminUser adminUser) {
		this.adminUser = adminUser;
	}

	public AdminUserMaintainBean getMaintainBean() {
		return maintainBean;
	}

	
	public String getCenter() {
		return center;
	}

	public void setCenter(String center) {
		this.center = center;
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

	public PasswordService getPasswordService() {
		return passwordService;
	}

	public void setPasswordService(PasswordService passwordService) {
		this.passwordService = passwordService;
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

	public String getRoleListJson() {
		if(roleListJson == null || "".equals(roleListJson)){
			getUserRoleList();
		}
		return roleListJson;
	}

	public void setRoleListJson(String roleListJson) {
		this.roleListJson = roleListJson;
	}
	
}
