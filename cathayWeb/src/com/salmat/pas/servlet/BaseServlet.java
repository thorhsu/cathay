package com.salmat.pas.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {
	 private Log logger = LogFactory.getLog(BaseServlet.class);
	 private String contextPath;
	/**
	 * 取得Acegi登入資訊
	 * @return
	 */
	public org.acegisecurity.userdetails.User getAcegiUser() {
		//由Acegi取得登入資訊
		Object obj = null;
		if(SecurityContextHolder.getContext().getAuthentication() != null)
			obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(obj != null && obj instanceof org.acegisecurity.userdetails.User) {
			return (org.acegisecurity.userdetails.User)obj;
		} else {
			return null;
		}
	}
	
	
	/**
	 * 檢查是否登入過期，For ajax servlet使用，以免重登入後Request servlet網址
	 */
	public void chkTimeout(HttpServletResponse resp, HttpServletRequest req ) {
		contextPath = req.getContextPath();
		if(getAcegiUser() == null) {
			try {
			
				resp.sendRedirect(contextPath + "/login.jspx");
			} catch (IOException e) {
                e.printStackTrace();
                logger.error("", e);
			}
		}
	}
}
