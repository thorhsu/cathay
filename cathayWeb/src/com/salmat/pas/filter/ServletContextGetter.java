package com.salmat.pas.filter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.acegisecurity.userdetails.User;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;

import com.fxdms.rmi.service.VoService;
import com.fxdms.rmi.service.impl.VoServiceImpl;
import com.salmat.pas.bo.AdmMenuService;
import com.salmat.pas.bo.AdminUserService;
import com.salmat.pas.vo.ActionHistory;
import com.salmat.pas.vo.AdmPageList;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.Constants;
import com.salmat.util.HibernateSessionFactory;

//此類別是一個filter，用來取得各種檔案與類別的contextpath、inputStream、URL等用的
public class ServletContextGetter implements Filter{
    
    private static ServletContext servletContext;
    private static String servletContextPath;
	private static AdminUserService adminUserService = new AdminUserService();  
	private static AdmMenuService admMenuService = new AdmMenuService();
	private static HashMap<String, String> urlMap = null;
	private static Logger logger = Logger.getLogger(ServletContextGetter.class);
	//private static HashMap<String, AdminUser> userMap = null;
	private AdminUser user = null;
	private static long count = 0;

	public ServletContextGetter(){		 
	}


	public void init(FilterConfig filterConfig) throws ServletException {
	   ServletContextGetter.servletContext = filterConfig.getServletContext();
	}
	//製造userId和 AdmUser的map
	public void setUser(String userId){
		if(userId != null){
			user = adminUserService.findUserByUserId(userId);
		}
		
	}
	public void setUrlMap(){
		if(urlMap == null){
			//製造URL和 使用者動作的map
			List<AdmPageList> pageList = null;
			pageList = admMenuService.getAdmPageListDao().findAllPage();
			urlMap = new HashMap<String, String>();
			for(AdmPageList page: pageList){
				if(page.getUrl() != null && !page.getUrl().equals(getServletContextPath() + "/secure/system/userActionQuery.jspx")){ //使用者歷程查詢不記錄					
				   urlMap.put(page.getUrl(), page.getDescription());
			    }
				
			}
		}			
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain arg2) throws IOException, ServletException {		
		Date now = new Date();
		setUrlMap();
		
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		if(servletContextPath == null)
			servletContextPath = request.getContextPath();
		String requestUri = request.getRequestURI().trim();		
		HttpSession session = request.getSession();
		
		String userId = (String)session.getAttribute(AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY);
		if(userId != null){
			setUser(userId);
            if(user == null){
            	request.setAttribute("userLocked", "無此帳號");
            }else{
   			   AdminUser loginUser = user.simpleClone();
   			   //登入或修改自己身份後才需要重新儲存在session中
   			   if(session.getAttribute(AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY) !=  null && session.getAttribute("loginUser") == null){
   				   session.setAttribute("loginUser", loginUser);
   			   }else{
   				   if(session.getAttribute(AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY) ==  null){   					   
   					   response.sendRedirect(servletContextPath + "/login.jspx");
   				   }
   			   }   			      
	            if(user.getUpdateDate() == null){
					session.setAttribute("loginMsg", "第一次登入，請重設密碼，謝謝");
				}else{
					Calendar cal = Calendar.getInstance();
					cal.setTime(now);
					cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE) - 180, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
					if(user.getUpdateDate().getTime() < cal.getTimeInMillis())
						session.setAttribute("loginMsg", "超過180天沒更新密碼，請重設密碼");
					else
				        session.removeAttribute("loginMsg");
				}
				if("0".equals(user.getStatus())){
					request.setAttribute("userLocked", "此帳號已被鎖定");
				}else if(user.getEnableEnd() != null && now.getTime() > user.getEnableEnd().getTime()){
					request.setAttribute("userLocked", "此帳號超過設定的生效結束日期");
				}else if(user.getEnableEnd() != null && now.getTime() > user.getEnableEnd().getTime()){
					request.setAttribute("userLocked", "此帳號尚未到達設定的生效起始日期");
				}else{
					request.setAttribute("userLocked", "密碼錯誤");
				}			   
            }
		}
		String mappingUrl = requestUri.replaceAll(servletContextPath, "");
		if(urlMap.get(mappingUrl) != null){
        	//因為都會來回各一次，所以只記錄 一次，logout只會有一次
           if((count%2 == 0 || (ServletContextGetter.getServletContextPath() + "/logout.jspx").equals(requestUri)) && user != null){
		     //以下寫入使用者記錄資料庫
             ActionHistory actionHistory = new ActionHistory();
             actionHistory.setAction(urlMap.get(mappingUrl));
             actionHistory.setActionPageUrl(mappingUrl);
             actionHistory.setInsertDate(now);
             actionHistory.setActionTime(now);
             actionHistory.setUpdateDate(now);
             actionHistory.setUserId(userId);
             actionHistory.setUserName(user.getUserName());
             Session hbsession = HibernateSessionFactory.getSession();
             Transaction tx = hbsession.beginTransaction();
             hbsession.save(actionHistory);
             tx.commit();
             if(hbsession.isOpen());
                hbsession.close();
           }
           count++;
        }
		
		getHtmlMenu(request);
		//insertScbhcSimappingtable();
		arg2.doFilter(req, res);
	}
	
	
	
	public void getHtmlMenu(HttpServletRequest req){
		GrantedAuthority[] roles = null;
		//User login information
		Object obj = null;
		if(SecurityContextHolder.getContext().getAuthentication() != null)
			obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(obj != null && obj instanceof User) {
			User user = (User)obj;
			roles = user.getAuthorities();		    
		}
		HttpSession session = req.getSession();
		String loginMsg = (String)session.getAttribute("loginMsg");
		
		
		String role = (roles == null || roles.length == 0 || roles[0].getAuthority() == null) ? 
				Constants.ROLE_ANONYMOUS : roles[0].getAuthority();
		
		//設定左側treeMenu的id
		admMenuService.setMenuId("menuList");
	    String menuData = "";
	    //如果有loginMsg，就會只剩登出和修改密碼
		if(loginMsg != null && !"".equals(loginMsg)){
			menuData = admMenuService.getMenuFirst();
		}else{
			menuData = admMenuService.getMenuData(role);
			
		}
		req.setAttribute("menuHtml", menuData);
		
	}

	public void destroy() {
		
	}
	
	public static String getServletContextPath(){
		if(servletContextPath != null)
			return servletContextPath;
		if(servletContext != null){
			servletContextPath = servletContext.getContextPath();
		    return servletContextPath;
		}else{
			return null;
		}
	}

	public static ServletContext getServletContext() {
		return servletContext;
	}
    
	public static InputStream getInputStream(String path) {		
		if (servletContext != null)
		  return servletContext.getResourceAsStream(path);
		else 
		  return null;
	}
	
	public static String getRealPath(String path){
		if (servletContext != null)
		  return servletContext.getRealPath(path);
		else 
		  return null;
	}
	
	public static URL getURL(String path) throws MalformedURLException{
		if (servletContext != null)
		  return servletContext.getResource(path);
		else 
	   	  return null;
	}
	

	public AdminUserService getAdminUserService() {
		return adminUserService;
	}

	public void setAdminUserService(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}
}
