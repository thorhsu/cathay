package com.salmat.pas.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.salmat.pas.vo.SysLog;
import com.salmat.util.HibernateSessionFactory;


/**
 * 由JSF Beans繼承，提供共用methods
 */
public abstract class BaseBean {
	protected Log logger = LogFactory.getLog(this.getClass());
	
	private static ResourceBundle staticDataResource;
	public static ResourceBundle getStaticDataResource() {
		if(staticDataResource == null) 
			staticDataResource = ResourceBundle.getBundle("com/salmat/fubon/conf/staticData");
		return staticDataResource;
	}
	
	/**
	 * 取得Faces Context
	 * @return FacesContext
	 */
	public FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}
	
	/**
	 * 由JSF FacesContext取得Servlet Context
	 * @return ServletContext
	 */
	public ServletContext getServletContext() {
		return (ServletContext)getFacesContext().getExternalContext().getContext();
	}

	/**
	 * 由JSF FacesContext取得JSF Application
	 * @return JSF Application
	 */
	public Application getApplication() {
		return getFacesContext().getApplication();
	}
	
	/**
	 * 由JSF FacesContext取得HTML Session
	 * @param isCreate 是否於session不存在時重建
	 * @return HttpSession
	 */
	public HttpSession getSession(boolean isCreate) {
		return (HttpSession)getFacesContext().getExternalContext().getSession(isCreate);
	}
	
	/**
	 * 由JSF FacesContext取得HTML Request
	 * @return HttpServletRequest
	 */
	public HttpServletRequest getRequest() {
		return (HttpServletRequest)getFacesContext().getExternalContext().getRequest();
	}
	
	/**
	 * 由JSF FacesContext取得HTML Response
	 * @return HttpServletResponse
	 */
	public HttpServletResponse getResponse() {
		return (HttpServletResponse)getFacesContext().getExternalContext().getResponse();
	}
	
	/**
	 * 取得頁面Request parameter
	 * @return Map包含頁面參數key : value
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getParameterMap() {
	    Enumeration<String> enums = getRequest().getParameterNames();
	    Map<String, String> map = new HashMap<String, String>();
	    while(enums.hasMoreElements()) {
	        String key = enums.nextElement();
	        String value = new String(getRequest().getParameter(key));
	        map.put(key, value);
	    }
	    return map;
	}
	
	/**
	 * 取得頁面Request value
	 * @return Request value
	 */
	public String getParameter(String key) {
		return getRequest().getParameter(key);
	}
	
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
	public void saveSysLog(boolean isException, Exception e, String logType , String subject, String messageBody){
    	SysLog syslog = new SysLog();
    	syslog.setCreateDate(new Date());
    	syslog.setIsException(isException);
    	syslog.setLogType("logType");
    	if(e != null)
    		messageBody = e.getMessage() + "|" + messageBody;
    	syslog.setMessageBody(messageBody);
    	syslog.setSubject(subject );
    	HibernateSessionFactory.getSession().save(syslog);	
    }
	
	public List<SelectItem> getSortItems() {
		List<SelectItem> sortItems = new ArrayList<SelectItem>();
		for(int n = 0; n <= 999; n++)
			sortItems.add(new SelectItem(String.valueOf(n), String.valueOf(n)));
		return sortItems;
	}
}
