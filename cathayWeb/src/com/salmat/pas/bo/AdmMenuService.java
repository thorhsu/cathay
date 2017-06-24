package com.salmat.pas.bo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NamedQuery;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.salmat.pas.dao.AdmPageListDao;
import com.salmat.pas.dao.AdmPageUserAuthorityDao;
import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.vo.ActionHistory;
import com.salmat.pas.vo.AdmPageList;
import com.salmat.pas.vo.AdmPageUserAuthority;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AdminUserRole;

public class AdmMenuService {
	static AdmPageListDao<AdmPageList> admPageListDao;
	static AdmPageUserAuthorityDao<AdmPageUserAuthority> admPageUserAuthorityDao;
	
	boolean isIdAppend;
	String menuId;
	
	public AdmMenuService() {
	}
	
	public List<AdmPageList> findAllPage(){
		Session session = this.getAdmPageListDao().getHiberTemplate()
		   .getSessionFactory().openSession();
		Criteria criteria = session.createCriteria(AdmPageList.class);
		List allPage = criteria.list();
		session.close();
		return allPage;
		
	}
	
	public boolean findUserByeRole(String userRole){
		Session session = this.getAdmPageListDao().getHiberTemplate()
		   .getSessionFactory().openSession();
		Criteria criteria = session.createCriteria(AdminUser.class);	
		criteria.add(Restrictions.eq("userRole", userRole));
		List<AdminUser> list = criteria.list();
		
		if(list == null || list.size() == 0){
			if(session.isOpen())
				session.close();
			return false;
		}else{
			if(session.isOpen())
				session.close();
			return true;
		}	
	}
	
	public List<AdminUserRole> findByCritical(String userRole, String isShow){
		Session session = this.getAdmPageListDao().getHiberTemplate()
		   .getSessionFactory().openSession();
		Criteria criteria = session.createCriteria(AdminUserRole.class);
		if(userRole != null && !"".equals(userRole.trim())){
			
		    criteria.add(Restrictions.eq("userRole", userRole));
		}    
		if(isShow != null && !"".equals(isShow.trim())){
			
		    criteria.add(Restrictions.eq("isShow", isShow));
		}
		List<AdminUserRole> list = criteria.list();
		if(session.isOpen())
			session.close();
		return list;
	}
	
	public AdminUserRole findByUserRole(String userRole){
		List<AdminUserRole> list = findByCritical(userRole, null);
		if(list != null && list.size() > 0)
			return list.get(0);
		else
			return null;
		
	}
	
	public List<AdminUserRole> getUserRoles(List<String> userRoles){
		Session session = this.getAdmPageListDao().getHiberTemplate()
		   .getSessionFactory().openSession();
		Criteria criteria = session.createCriteria(AdminUserRole.class);
	    criteria.add(Restrictions.in("userRole", userRoles));
		List<AdminUserRole> list = criteria.list();
		if(session.isOpen())
			session.close();
		return list;
	}
	
	
	public void delUserRole(String userRole){
		Session session = this.getAdmPageListDao().getHiberTemplate()
		   .getSessionFactory().openSession();
		AdminUserRole adminUserRole = (AdminUserRole) session.load(AdminUserRole.class, userRole);

		if(adminUserRole != null){
			Transaction tx = session.beginTransaction();
			//殺掉這個角色
			session.delete(adminUserRole);
			
			//找出這個角色已經有那些權限
			List <AdmPageUserAuthority> pageAuthList = session.getNamedQuery("AdmPageUserAuthority.findByRole").setString(0, "%" + userRole + "%").list();
			
			for(AdmPageUserAuthority pageAuth : pageAuthList){
				//移除此角色權限
				pageAuth.removeRole(userRole);
				pageAuth.setUpdateDate(new Timestamp(new Date().getTime()));
				session.update(pageAuth);
			}

			tx.commit();
		
		}
		if(session.isOpen())
			session.close();
	}
	
	public String getMenuData(String role) {
		StringBuffer sb = new StringBuffer("");
		Session session = admPageListDao.getHiberTemplate().getSessionFactory().openSession();
		List<AdmPageList> pageList = session.getNamedQuery("AdmPageList.findByAuthority").list();
		
		//List<AdmPageList> pageList = admPageListDao.findByAuthority();
		//List<AdmPageUserAuthority> excludePageList = admPageUserAuthorityDao.findByRoleNotAccess("%" + role + "%");
		List<AdmPageUserAuthority> excludePageList = session.getNamedQuery("AdmPageUserAuthority.findByRoleNotAccess").setString(0, "%" + role + "%").list();
		//使用clone List以免刪除後loop出錯，權限命名請勿使用like可比對到的名稱，以免搜尋比對時誤判
		List<AdmPageList> clonePageList = new ArrayList<AdmPageList>(pageList);
		for(AdmPageList page : clonePageList) {
			for(AdmPageUserAuthority excludePage : excludePageList) {
				
				if(excludePage.getPageId() != null && 
						page.getId().intValue() == excludePage.getPageId().intValue()) {			
					pageList.remove(page);
				}
			}
		}
		int nodeId = 0;
		isIdAppend = false;
		getHtmlMenu(sb, pageList, nodeId);
		if(session.isOpen())
			session.close();
		
		return sb.toString();
	}
	
	public String getMenuFirst() {
		StringBuffer sb = new StringBuffer("");
		Session session = admPageListDao.getHiberTemplate().getSessionFactory().openSession();
		List<AdmPageList> pageList = session.getNamedQuery("AdmPageList.findByAuthority").list();
		//List<AdmPageList> pageList = admPageListDao.findByAuthority();
		
		List<AdmPageList> clonePageList = new ArrayList<AdmPageList>(pageList);
        //第一次登入或密碼過期時，只留下這兩個功能寫死在程式中，反正也不會改
		for(AdmPageList page : clonePageList) {
				if(!"登出".equals(page.getName()) && !"密碼修改".equals(page.getName())) {
					pageList.remove(page);
				}
		}
		int nodeId = 0;
		isIdAppend = false;
		getHtmlMenu(sb, pageList, nodeId);
		if(session.isOpen())
			session.close();
		
		return sb.toString();
	}
	
	
	//利用角色名稱取得所有可accessed的url
	public List<String> getAccessUrls(String userRole){
		//AdmPageUserAuthority.findByRole
		Session session = admPageListDao.getHiberTemplate().getSessionFactory().openSession();

		List<AdmPageUserAuthority> pageList = session.getNamedQuery("AdmPageUserAuthority.findByRole").setString(0, "%" + userRole + "%").list();
		//List<AdmPageList> pageList = admPageListDao.findByAuthority();
		
		List<AdmPageUserAuthority> clonePageList = new ArrayList<AdmPageUserAuthority>(pageList);
		List<String> returnList = new ArrayList<String> ();

		for(AdmPageUserAuthority page : clonePageList) {
             returnList.add(page.getUrl());              				
		}
		if(session.isOpen())
			session.close();
		return returnList;
		
	}
	
	/**
	 * Recursive method產生html menu
	 * sample;
	 * 
		<ul id='mymenu'>
			<li><a href='member/submember/member_forget.jspx'>會員資訊</a>
			<ul>
				<li><a href='/member/submember/member_join.jspx'>加入會員</a></li>
				<li><a href='/member/submember/member_login.jspx'>登入會員</a></li>
				<li><a href='/member/submember/member_edit.jspx'>會員資料修改</a></li>
				<li>忘記帳號密碼</li>
			</ul>
			<li>下載專區
				<ul>
					<li><a href='/member/download/member_download1.jspx'>月曆/桌布</a></li>
					<li><a href='/member/download/member_download2.jspx'>螢幕保護程式</a></li>
					<li><a href='/member/download/member_download3.jspx'>eCard</a></li>
				</ul>
				<li>藝文活動</li>
				<li><a href='/member/epaper/member_epaper_admin.jspx'>訂閱電子報</a></li>
		</ul>
	 * 
	 * @param sb 儲存回傳文字的StringBuffer
	 * @param pageList 來源資料列表
	 * @param nodeId 起始的menu id
	 * @return menu StringBuffer for jquery treeview
	 */
	private StringBuffer getHtmlMenu(StringBuffer sb,
			List<AdmPageList> pageList, int nodeId) {
		List<AdmPageList> clonePageList = new ArrayList<AdmPageList>(pageList);
		boolean isChildNodeExist = chkChildNodeExist(clonePageList, nodeId);
		if(isChildNodeExist) { //檢查目前node是否為parent node，有則新增<ul>
			if (isIdAppend) {
				sb.append("<ul>");
			} else {
				//第一次進入時才給定menu id(for jquery套效果)
				isIdAppend = true;
				sb.append("<ul id='");
				sb.append(menuId);
				sb.append("'>");
			}
		} 
		for (AdmPageList page : clonePageList) {
			if (page.getParentId() == nodeId) {
				int newNodeId = page.getId().intValue();
				sb.append("<li>");
				if (page.getUrl() != null && !page.getUrl().equals("") && (page.getUrl().endsWith(".jspx") || page.getUrl().endsWith(".jsp") || page.getUrl().endsWith(".htm") || page.getUrl().endsWith(".html"))) {
					sb.append("<a href='");
					sb.append(ServletContextGetter.getServletContextPath() + page.getUrl());
					sb.append("'>");
					sb.append(page.getName());
					sb.append("</a>");
				} else {
					sb.append(page.getName());
				}
				//若目前node無子節點時，才關閉li
//				if(!chkChildNodeExist(clonePageList, page.getId().intValue()))
//					sb.append("</li>");
				pageList.remove(page);
				//Recursive
				getHtmlMenu(sb, pageList, newNodeId);
				sb.append("</li>");
			}
		}
		//檢查目前node是否為parent node，是則結束<ul>後才回傳sb
		if(isChildNodeExist)
			sb.append("</ul>");
		return sb;
	}
	
	/**
	 * 檢查該node是否有child node
	 * @param pageList
	 * @param nodeId
	 * @return
	 */
	private boolean chkChildNodeExist(List<AdmPageList> pageList, int nodeId) {
		for (AdmPageList page : pageList) {
			if(page.getParentId().intValue() == nodeId)
				return true;
		}
		return false;
	}
	
	public AdmPageListDao<AdmPageList> getAdmPageListDao() {
		return admPageListDao;
	}

	public void setAdmPageListDao(AdmPageListDao<AdmPageList> admPageListDao) {
		this.admPageListDao = admPageListDao;
	}


	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public AdmPageUserAuthorityDao<AdmPageUserAuthority> getAdmPageUserAuthorityDao() {
		return admPageUserAuthorityDao;
	}

	public void setAdmPageUserAuthorityDao(
			AdmPageUserAuthorityDao<AdmPageUserAuthority> admPageUserAuthorityDao) {
		this.admPageUserAuthorityDao = admPageUserAuthorityDao;
	}

	public static void main(String[] str) {
		ApplicationContext factory = new ClassPathXmlApplicationContext(
					"com/salmat/fubon/conf/applicationContext*");
		AdmPageListDao dao = (AdmPageListDao) factory.getBean("admPageListDao");
		AdmPageUserAuthorityDao admPageUserAuthorityDao = (AdmPageUserAuthorityDao) factory.getBean("admPageUserAuthorityDao");
		AdmMenuService s = new AdmMenuService();
		s.setMenuId("menuId");
		s.setAdmPageListDao(dao);
		s.setAdmPageUserAuthorityDao(admPageUserAuthorityDao);
	}
	
	
	public List<ActionHistory> getActionHistoryByCriteria(Date startDate, Date endDate, String action, String userId, int firstResult, int maxResult){
		Calendar cal = Calendar.getInstance();
		Session session = this.getAdmPageListDao().getHiberTemplate()
		   .getSessionFactory().openSession();
		Criteria criteria = session.createCriteria(ActionHistory.class);
		if (startDate != null) {
			cal.setTime(startDate);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DATE), 0, 0, 0);
			criteria.add(Restrictions.ge("actionTime", cal.getTime()));
		}
		if (endDate != null) {
			cal.setTime(endDate);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DATE), 23, 59, 59);
			criteria.add(Restrictions.le("actionTime", cal.getTime()));
		}
		if(action != null && !action.equals("")){
			criteria.add(Restrictions.eq("actionPageUrl", action));
		}
		if(userId != null && !userId.equals("")){
			criteria.add(Restrictions.eq("userId", userId));
		}
		criteria.addOrder(Order.desc("id"));
		criteria.setFirstResult(firstResult);
		criteria.setMaxResults(maxResult);
		List<ActionHistory> list = criteria.list();
		if(session.isOpen())
			session.close();
		if(list != null)
			return list;
		else
			return new ArrayList<ActionHistory>();		
		
	}
	
	public void editAuthority (String userRole, String [] urls, String user, String isShow, String userRoleName, String userRoleDesc, String centerOnly){
		Session session = this.getAdmPageListDao().getHiberTemplate()
		   .getSessionFactory().openSession();
       
		
		AdminUserRole admUserRole = (AdminUserRole) session.load(AdminUserRole.class, userRole);
        admUserRole.setUpdateDate(new Timestamp(new Date().getTime()));
        admUserRole.setUpdateUser(user);
		admUserRole.setIsShow(isShow);
		admUserRole.setUserRoleDesc(userRoleDesc);
		admUserRole.setUserRoleName(userRoleName);
		admUserRole.setCenterOnly(centerOnly);		
		Transaction tx = session.beginTransaction();
		session.update(admUserRole);
		
		//加入授權
		for(String url: urls){
			//AdmPageUserAuthority.findByUrl
			AdmPageUserAuthority admPageUserAuthority = new AdmPageUserAuthority();
			admPageUserAuthority.setInsertDate(new Timestamp(new Date().getTime()));
			admPageUserAuthority.setInsertUser(user);

			//把pageContext拿走，此處是寫死的
			String queryUrl = url.replaceAll(ServletContextGetter.getServletContextPath(), "");
			//取得之前授權的物件 
			List<AdmPageUserAuthority> userAuth = session.getNamedQuery("AdmPageUserAuthority.findByUrl").setString(0, queryUrl).list();
			if(userAuth != null && userAuth.size() > 0){
				admPageUserAuthority = userAuth.get(0);
				admPageUserAuthority.setUpdateDate(new Timestamp(new Date().getTime()));
				admPageUserAuthority.setUpdateUser(user);
			}
			//取得pageId
			List<AdmPageList> pageList = session.getNamedQuery("AdmPageList.findByUrl").setString(0, url).list();
			if(pageList != null && pageList.size() > 0)
				admPageUserAuthority.setPageId(pageList.get(0).getId().intValue());
			admPageUserAuthority.addRole(userRole);
			admPageUserAuthority.setUrl(queryUrl);
			session.saveOrUpdate(admPageUserAuthority);
		}
		
		//移除授權
		List<AdmPageUserAuthority> userAuths = session.getNamedQuery("AdmPageUserAuthority.findByAllEnable").list();
		for(AdmPageUserAuthority pageUserAuth: userAuths){
			boolean authResult = false;
			//如果勾選的url有找到 ，檢查結果仍為false，就不會移除
			for(String url: urls){
				String queryUrl = url.replaceAll(ServletContextGetter.getServletContextPath(), "");
				if(queryUrl.equals(pageUserAuth.getUrl()))
					authResult = true;
			}
			if(!authResult){
				pageUserAuth.removeRole(userRole);
				pageUserAuth.setUpdateDate(new Timestamp(new Date().getTime()));
				pageUserAuth.setUpdateUser(user);
				session.update(pageUserAuth);
			}
		}
	    
		tx.commit();
		if(session.isOpen())
			session.close();
	}

	//加入新的權限角色，urls是含有pageContext的url
	public boolean addNewAuthority(String userRole, String [] urls, String user, String isShow, String userRoleName, String userRoleDesc, String centerOnly){
		Session session = this.getAdmPageListDao().getHiberTemplate()
		   .getSessionFactory().openSession();
		List<AdminUserRole> userRoles = session.getNamedQuery("AdminUserRole.likeUserRole").setString(0, "%" + userRole + "%").list();
		
		//如果發現有類似的角色名，回傳false
		if(userRoles != null && userRoles.size() > 0)
			return true;
		AdminUserRole admUserRole = new AdminUserRole();
		admUserRole.setInsertDate(new Timestamp(new Date().getTime()));
		admUserRole.setInsertUser(user);
		admUserRole.setIsShow(isShow);
		admUserRole.setUserRole(userRole);
		admUserRole.setUserRoleDesc(userRoleDesc);
		admUserRole.setUserRoleName(userRoleName);
		admUserRole.setCenterOnly(centerOnly);
		
		Transaction tx = session.beginTransaction();
		session.save(admUserRole);
		for(String url: urls){
			//AdmPageUserAuthority.findByUrl
			AdmPageUserAuthority admPageUserAuthority = new AdmPageUserAuthority();
			admPageUserAuthority.setInsertDate(new Timestamp(new Date().getTime()));
			admPageUserAuthority.setInsertUser(user);

			//把pageContext拿走，此處是寫死的
			String queryUrl = url.replaceAll(ServletContextGetter.getServletContextPath(), "");
			//取得之前授權的物件 
			List<AdmPageUserAuthority> userAuth = session.getNamedQuery("AdmPageUserAuthority.findByUrl").setString(0, queryUrl).list();
			if(userAuth != null && userAuth.size() > 0){
				admPageUserAuthority = userAuth.get(0);
				admPageUserAuthority.setUpdateDate(new Timestamp(new Date().getTime()));
				admPageUserAuthority.setUpdateUser(user);
			}
			//取得pageId
			List<AdmPageList> pageList = session.getNamedQuery("AdmPageList.findByUrl").setString(0, url).list();
			if(pageList != null && pageList.size() > 0)
				admPageUserAuthority.setPageId(pageList.get(0).getId().intValue());
			admPageUserAuthority.addRole(userRole);
			admPageUserAuthority.setUrl(queryUrl);
			session.saveOrUpdate(admPageUserAuthority);
		}
	    
		tx.commit();
		if(session.isOpen())
			session.close();
		return false;
	}
	
	public int getActionHistoryCount(Date startDate, Date endDate, String action, String userId){
		Calendar cal = Calendar.getInstance();

		Session session = this.getAdmPageListDao().getHiberTemplate()
		   .getSessionFactory().openSession();
		Criteria criteria = session.createCriteria(ActionHistory.class);
		if (startDate != null) {
			cal.setTime(startDate);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DATE), 0, 0, 0);
			criteria.add(Restrictions.ge("actionTime", cal.getTime()));
		}
		if (endDate != null) {
			cal.setTime(endDate);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DATE), 23, 59, 59);
			criteria.add(Restrictions.le("actionTime", cal.getTime()));
		}
		if(action != null && !action.equals("")){
			criteria.add(Restrictions.eq("actionPageUrl", action));
		}
		if(userId != null && !userId.equals("")){
			criteria.add(Restrictions.eq("userId", userId));
		}
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.rowCount());
		criteria.setProjection(projectionList);
		Integer count = ((Integer)criteria.list().get(0));
		if(session.isOpen())
			session.close();

		if(count != null)
			return count;
		else
			return 0;
				
		
	}

	public List<AdmPageList> findByAuthority() {
		Session session = this.getAdmPageListDao().getHiberTemplate()
		   .getSessionFactory().openSession();
		Query query = session.getNamedQuery("AdmPageList.findByAuthority");		
		List<AdmPageList> retList = query.list();
		session.close();
		return retList;
	}
	
}