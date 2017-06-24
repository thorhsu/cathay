package com.salmat.pas.bo;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.salmat.pas.vo.Properties;
import com.salmat.util.HibernateSessionFactory;

/**
 * 
 * ApplyData servevice
 *
 */
public class PropertiesService {
	static Logger logger = Logger.getLogger(PropertiesService.class);
	
	public static Properties getProperties(){
		Session session = null;
		try{			
			session = HibernateSessionFactory.getSession();
			return (Properties) session.get(Properties.class, 1);
		}catch(Exception e){
			logger.error("", e);
			return null;
		}finally{
			if(session != null)
				session.close();
		}		
	}
	
	public static void updateProperties(Properties properties){
		Session session = null;
		Transaction tx = null;
		try{						
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			session.update(properties);
			tx.commit();
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			
		}finally{
			if(session != null)
				session.close();
		}		
	}
	
	
}