package com.salmat.pas.bo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import com.salmat.pas.dao.AdminUserDao;
import com.salmat.pas.dao.AdminUserDeleteDao;
import com.salmat.pas.dao.AdminUserRoleDao;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AdminUserDelete;
import com.salmat.pas.vo.AdminUserHistory;
import com.salmat.pas.vo.AdminUserRole;

/**
 * 
 * 讀寫會員相關資訊
 *
 */
public class AdminUserService {
	private static AdminUserDao<AdminUser> adminUserDao;
	private static AdminUserDeleteDao<AdminUserDelete> adminUserDeleteDao;
	private static AdminUserRoleDao<AdminUserRole> adminUserRoleDao;
	public AdminUserService() {
	}
	
	@SuppressWarnings("unchecked")
	public List<AdminUser> findByCritrial(
			String userId,
			String status) {
		Session session = adminUserDeleteDao.getHiberTemplate().getSessionFactory().openSession();
		Criteria crit = session.createCriteria(AdminUser.class);
		//Criteria crit = adminUserDao.getCriteria();
		if(!StringUtils.trimToEmpty(userId).equals(""))
			crit.add(Restrictions.eq("userId", userId.trim()));
		if(!StringUtils.trimToEmpty(status).equals(""))
			crit.add(Restrictions.eq("status", status.trim()));
		//排除管理者帳號不可於UI修改
		//crit.add(Restrictions.ne("userRole", Constants.ROLE_SUPERVISOR));
		
		crit.addOrder(Order.desc("status"));
		crit.addOrder(Order.desc("enableEnd"));
		crit.addOrder(Order.asc("userId"));
		
		List<AdminUser> result = crit.list();
		if(session.isOpen())
			session.close();
		return result;
	}
	
	public static void updateErrorCounter(String userId){
		Session session = adminUserDao.getHiberTemplate().getSessionFactory().openSession();
		List<AdminUser> list = session.getNamedQuery("AdminUser.findByUserId").setString(0, userId).list();
		AdminUser user = null;
		if(list != null && list.size() > 0){
			user = list.get(0);
		    int counter = (user.getErrorCounter()== null)? 0 : user.getErrorCounter();
		    counter++;
		    user.setErrorCounter(counter);
		    Transaction tx = session.beginTransaction();
		    session.update(user);
		    tx.commit();
		}
	    if(session.isOpen())
		   session.close();
	}
	
	public static void updateCounterZero(String userId){
		Session session = adminUserDao.getHiberTemplate().getSessionFactory().openSession();
		List<AdminUser> list = session.getNamedQuery("AdminUser.findByUserId").setString(0, userId).list();
		AdminUser user = null;
		if(list != null && list.size() > 0){
			user = list.get(0);
		    user.setErrorCounter(0);
		    Transaction tx = session.beginTransaction();
		    session.update(user);
		    tx.commit();
		}
		if(session.isOpen())
		   session.close();
	}
	public List<AdminUser> findAllUser(){
		Session session = adminUserDao.getHiberTemplate().getSessionFactory().openSession();
		List<AdminUser> list = session.getNamedQuery("AdminUser.findAllUser").list();		
		if(session.isOpen())
		   session.close();
		return list;
	}
	
	
	public void delete(AdminUser adminUser, String deleteUser) {
		//紀錄刪除履歷
		AdminUserDelete adminUserDel = new AdminUserDelete();
		adminUserDel.setDeleteDate(new Timestamp(System.currentTimeMillis()));
		adminUserDel.setDeleteUser(deleteUser);
		adminUserDel.setEmail(adminUser.getEmail());
		adminUserDel.setEnableEnd(adminUser.getEnableEnd());
		adminUserDel.setEnableStart(adminUser.getEnableStart());
		adminUserDel.setInsertDate(adminUser.getInsertDate());
		adminUserDel.setInsertUser(adminUser.getInsertUser());
		adminUserDel.setPassword(adminUser.getPassword());
		adminUserDel.setStatus(adminUser.getStatus());
		adminUserDel.setUpdateDate(adminUser.getUpdateDate());
		adminUserDel.setUpdateUser(adminUser.getUpdateUser());
		adminUserDel.setUserId(adminUser.getUserId());
		adminUserDel.setUserName(adminUser.getUserName());
		adminUserDel.setUserRole(adminUser.getUserRole());
		adminUserDel.setVersion(adminUser.getVersion());
		//adminUserDeleteDao.create(adminUserDel);
        Session session = adminUserDeleteDao.getHiberTemplate().getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Query historyQuery = session.getNamedQuery("AdminUser.deletById");
		Query userQuery = session.getNamedQuery("AdminUserHistory.deletByUserId");
		historyQuery.setLong(0, adminUser.getId()).executeUpdate();
		userQuery.setString(0, adminUser.getUserId()).executeUpdate();
		session.save(adminUserDel);
		tx.commit();
		session.close();
	}
	
	public List<AdminUserHistory> getByAid(Long aid, String user, int maxRows){
		Session session = adminUserDeleteDao.getHiberTemplate().getSessionFactory().openSession();
		Criteria criteria = session.createCriteria(AdminUserHistory.class).add(Restrictions.eq("aid", aid)).add(Restrictions.eq("insertUser", user));
		criteria.addOrder(Order.desc("id"));
		criteria.setMaxResults(maxRows);
		List<AdminUserHistory> al = new ArrayList<AdminUserHistory>();
		if(criteria.list() != null)
		   al = criteria.list();
		if(session.isOpen())
			session.close();
		   return al;
	}
	
	/**
	 * userId 為User table的key，不允許重複
	 * @param userId
	 * @return
	 */
	public AdminUser findUserByUserId(String userId) {
		Session session = adminUserDao.getHiberTemplate().getSessionFactory().openSession();
		List<AdminUser> userList = session.getNamedQuery("AdminUser.findByUserId").setString(0, userId).list();
		AdminUser user = null;
		if(userList != null && !userList.isEmpty()){
			user = userList.get(0);
			user.getEnableEnd();
		}
		if(session.isOpen())
			session.close();
		
		return user;
		/*
		List<AdminUser> userList = adminUserDao.findByUserId(userId);
		if(userList != null && !userList.isEmpty())
			return userList.get(0);
		else
			return null;
	    */
	}
	
	public AdminUser findUserById(Long id) {
		Session session = adminUserDao.getHiberTemplate().getSessionFactory().openSession();
		AdminUser adminUser = (AdminUser)session.load(AdminUser.class, id);
		adminUser.getId();
		//List<AdminUser> userList = adminUserDao.findById(id);
		if(session.isOpen())
			session.close();
		return adminUser;
	}
	
	public AdminUser findByUserIdAndPass(String userId, String userPassword) {
		Session session = adminUserDao.getHiberTemplate().getSessionFactory().openSession();
		List<AdminUser> userList = session.getNamedQuery("AdminUser.findByUserIdAndPass").setString(0, userId).setString(1, userPassword).list();
		//List<AdminUser> userList = adminUserDao.findByUserIdAndPass(userId, userPassword);
		AdminUser adminUser = null;
		if(userList != null && !userList.isEmpty())
			adminUser = userList.get(0);
		if(session.isOpen())
			session.close();
		
		return adminUser;
	}
	
	public AdminUser findByUserRole(String userRole) {
		Session session = adminUserDao.getHiberTemplate().getSessionFactory().openSession();
		List<AdminUser> userList = session.getNamedQuery("AdminUser.findByUserRole").setString(0, userRole).list();
		//List<AdminUser> userList = adminUserDao.findByUserRole(userRole);
		AdminUser adminUser = null;
		if(userList != null && !userList.isEmpty())
			adminUser = userList.get(0);
		if(session.isOpen())
			session.close();
		
		return adminUser;
	}
	
	public List<AdminUserRole> getUserRoleList() {
		return adminUserRoleDao.findByAll();
	}
	
	public AdminUserDao<AdminUser> getAdminUserDao() {
		return adminUserDao;
	}

	public void setAdminUserDao(AdminUserDao<AdminUser> adminUserDao) {
		this.adminUserDao = adminUserDao;
	}

	public AdminUserDeleteDao<AdminUserDelete> getAdminUserDeleteDao() {
		return adminUserDeleteDao;
	}

	public void setAdminUserDeleteDao(
			AdminUserDeleteDao<AdminUserDelete> adminUserDeleteDao) {
		this.adminUserDeleteDao = adminUserDeleteDao;
	}

	public AdminUserRoleDao<AdminUserRole> getAdminUserRoleDao() {
		return adminUserRoleDao;
	}

	public void setAdminUserRoleDao(AdminUserRoleDao<AdminUserRole> adminUserRoleDao) {
		this.adminUserRoleDao = adminUserRoleDao;
	}

}