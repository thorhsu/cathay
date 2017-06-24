package com.salmat.pas.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AdminLoginBean {
	protected final Log logger = LogFactory.getLog(getClass());
	private String userId;
	private String userPassword;
//	private String txtValid;
//	private String errMsg;

	public AdminLoginBean() {
	}

	/**
	 * 決定是否可送出至Acegi驗證，會在validator之後
	 * @return success為可驗證
	 * 		   failure為不可驗證
	 */
	public String valid() {
		return "success";
	}
	public String toLogin(){
		return "toLogin";
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

//	public String getTxtValid() {
//		return txtValid;
//	}
//
//	public void setTxtValid(String txtValid) {
//		this.txtValid = txtValid;
//	}
//	public String getErrMsg() {
//		return errMsg;
//	}
//
//	public void setErrMsg(String errMsg) {
//		this.errMsg = errMsg;
//	}
}