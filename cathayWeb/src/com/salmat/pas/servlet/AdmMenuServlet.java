package com.salmat.pas.servlet;

import java.io.*;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.*;


import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.acegisecurity.userdetails.User;

import com.salmat.pas.bo.AdmMenuService;
import com.salmat.pas.bo.AdminUserService;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.Constants;
import com.salmat.util.ApplicationContextFactory;

@SuppressWarnings("serial")
public class AdmMenuServlet extends BaseServlet {
	private AdmMenuService admMenuService;
	private AdminUserService admUserService;
	private AdmMenuService getMenuService() {
		if (admMenuService == null)
			admMenuService = (AdmMenuService) ApplicationContextFactory
					.getApplicationContext().getBean("admMenuService");
		return admMenuService;
	}
	private AdminUserService getAdmUserService() {
		if (admUserService == null)
			admUserService = (AdminUserService) ApplicationContextFactory
					.getApplicationContext().getBean("adminUserService");
		return admUserService;
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		GrantedAuthority[] roles = null;
		//User login information
		Object obj = null;
		if(SecurityContextHolder.getContext().getAuthentication() != null)
			obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(obj != null && obj instanceof User) {
			User user = (User)obj;
			roles = user.getAuthorities();
			/*
		    if(roles != null){
		    	for(GrantedAuthority authority : roles){
		    	}
		    }
		    */
		}
		HttpSession session = req.getSession();
		String loginMsg = (String)session.getAttribute("loginMsg");
		
		
		String role = (roles == null || roles.length == 0 || roles[0].getAuthority() == null) ? 
				Constants.ROLE_ANONYMOUS : roles[0].getAuthority();
		
		getMenuService().setMenuId("menuList");
	    String menuData = "";
	    //如果有loginMsg，就會只剩登出和修改密碼
		if(loginMsg != null && !"".equals(loginMsg)){
			menuData = getMenuService().getMenuFirst();
		}else{
			menuData = getMenuService().getMenuData(role);	
		}
		resp.setContentType("text/html;charset=UTF-8");
		resp.addHeader("Cache-Control", "no-cache, must-revalidate");
		resp.addHeader("Pragma"       , "no-cache");
		resp.addHeader("Expires"      , "Mon, 1 Jan 1990 00:00:00 GMT");					
		resp.addHeader("Last-Modified", (new Date()).toString());	
		PrintWriter out = resp.getWriter();
	
		out.println(menuData);
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
}