package com.salmat.pas.beans;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.salmat.pas.bo.AdminUserService;
import com.salmat.pas.bo.PasswordService;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AdminUserHistory;
import com.salmat.pas.vo.Constants;
import com.salmat.util.Tools;

public class ProfileEditBean extends BaseBean {
	private String userId;
	private String userName;
	private String userRoleName;
	private String userRoleDesc;
	private String password;
	private String passwordConfirm;
	private String email;
	private String enableStart;
	private String enableEnd;
	private String insertUser;
	private String updateUser;
	private Timestamp insertDate;
	private Timestamp updateDate;
	private AdminUser adminUser;
	private String initResult;
	private AdminUserService adminUserService;
	private PasswordService passwordService;
	private String result;
	
	public ProfileEditBean() {
	}

	/**
	 * 寫入資料庫前的資料驗證
	 * @return 錯誤訊息
	 */
	public String validData() {
		if(getAdminUser() == null) 
			return "請重新整理頁面！";
		else if(!getPassword().equals(getPasswordConfirm())) 
			return "兩個欄位密碼不同，請重新確認！";
		else if(getAdminUser().getUserId().equals(getPassword()))
			return "帳號不得與密碼相同";
		else if(pastUsedPwd(getAdminUser().getId()))
			return "此密碼曾在過去三次使用過，請重新設定密碼";
		
		
		
		return "";
	}

	public boolean pastUsedPwd(Long aid){
		String pwd = passwordService.encodeSha(getPassword());
		List<AdminUserHistory> list = adminUserService.getByAid(aid, getAdminUser().getUserId(), 3);
		boolean returnFlag = false;
		for(AdminUserHistory userHistory : list){
			if(pwd.equals(userHistory.getPassword())){
				returnFlag = true;
				break;
			}
		}
		return returnFlag;
		
	}
	
	/**
	 * 初始化頁面資訊
	 * @return
	 */
	private String initWebPage() {
		//初始化頁面資訊
		setAdminUser(adminUserService.findUserByUserId(getAcegiUser().getUsername()));
		if(getAdminUser() == null) {
			try {
				getResponse().sendRedirect("index.jspx");
			} catch (IOException e) {
				logger.info(e.getMessage());
			}
		} else {
			setUserId(getAdminUser().getUserId());
			setUserName(getAdminUser().getUserName());
			setEmail(getAdminUser().getEmail());
			setUserRoleName(getAdminUser().getAdminUserRole().getUserRoleName());
			setUserRoleDesc(getAdminUser().getAdminUserRole().getUserRoleDesc());
			setEnableStart(Tools.getDateFormat(getAdminUser().getEnableStart(), Constants.DATE_PATTERN));
			setEnableEnd(Tools.getDateFormat(getAdminUser().getEnableEnd(), Constants.DATE_PATTERN));
			setInsertUser(getAdminUser().getInsertUser());
			setUpdateUser(getAdminUser().getUpdateUser());
			setInsertDate(getAdminUser().getInsertDate());
			setUpdateDate(getAdminUser().getUpdateDate());
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
			getAdminUser().setPassword(passwordService.encodeSha(getPassword()));
			getAdminUser().setUserName(getUserName());
			getAdminUser().setUpdateUser(getAcegiUser().getUsername());
			getAdminUser().setUpdateDate(new Timestamp(System.currentTimeMillis()));
			try{
				AdminUserHistory userHistory = new AdminUserHistory();
				userHistory.setInsertUser(getAdminUser().getUserId());
				userHistory.setAdminUser(getAdminUser());
				userHistory.setAid(getAdminUser().getId());

				adminUserService.getAdminUserDao().saveOrUpdate(getAdminUser());
				adminUserService.getAdminUserDao().getHiberTemplate().saveOrUpdate(userHistory);
				setResult("修改資料成功！");
				this.getSession(true).removeAttribute("loginMsg");
				return "success";
			} catch(Exception ex) { //避免樂觀鎖定例外
				logger.error("", ex);
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

	public AdminUser getAdminUser() {
		return adminUser;
	}

	public void setAdminUser(AdminUser adminUser) {
		this.adminUser = adminUser;
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
	
	
}
