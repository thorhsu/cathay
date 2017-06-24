package com.fxdms.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * Configures and provides access to Hibernate sessions, tied to the current
 * thread of execution. Follows the Thread Local Session pattern, see
 * {@link http://hibernate.org/42.html }.
 */
public class HibernateSessionFactory {

    /**
     * Location of hibernate.cfg.xml file. Location should be on the classpath
     * as Hibernate uses #resourceAsStream style lookup for its configuration
     * file. The default classpath location of the hibernate config file is in
     * the default package. Use #setConfigFile() to update the location of the
     * configuration file for the current session.
     */
    private static String CONFIG_FILE_LOCATION = "/hibernate.cfg.xml"; //目前不使用，直接設定在applicationContext.xml中

    private static final ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();
    
    private static HibernateTemplate hibernateTemplate = null;
    
    private static HibernateTemplate hibernateTemplate2 = null;
    
    private static HibernateTemplate hibernateTemplate3 = null;

    public static final ThreadLocal<Transaction> tLocaltx = new ThreadLocal<Transaction>();

    private static Configuration configuration = new Configuration();

    private static org.hibernate.SessionFactory sessionFactory;

    private static String configFile = CONFIG_FILE_LOCATION;

    private HibernateSessionFactory() {
    }

    /**
     * Returns the ThreadLocal Session instance. Lazy initialize the
     * <code>SessionFactory</code> if needed.
     * 
     * @return Session
     * @throws HibernateException
     */
    public static Session getSession() throws HibernateException {
        Session session = (Session) threadLocal.get();
        boolean isConnect = true;
        if (sessionFactory == null) {
            rebuildSessionFactory();
        }
        /*
        if(session != null && session.isOpen()){
           try {
        	   Connection connection = session.connection();
			   if( connection == null || connection.isClosed()){
				   isConnect = false;
			   }
		   } catch (Exception e) {
			   isConnect = false;
			   Log.error("", e);
			   e.printStackTrace();
		   }
        }
        */
        if (session == null || !session.isOpen() || !session.isConnected() || !isConnect) {            
            session = (sessionFactory != null) ? sessionFactory.openSession()
                    : null;
            threadLocal.set(session);
        }

        return session;
    }

    /**
     * Rebuild hibernate session factory
     * 
     */
    public static void rebuildSessionFactory() {
        try {
            //configuration.configure(configFile);
            //sessionFactory = configuration.buildSessionFactory();
        	sessionFactory = hibernateTemplate.getSessionFactory();            
        } catch (Exception e) {

            System.err.println("%%%% Error Creating SessionFactory %%%%");
            e.printStackTrace();
        }
    }

    /**
     * Close the single hibernate session instance.
     * 
     * @throws HibernateException
     */
    public static void closeSession() throws HibernateException {
        Session session = (Session) threadLocal.get();
        threadLocal.set(null);

        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    /**
     * return session factory
     * 
     */
    public static org.hibernate.SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * return session factory
     * 
     * session factory will be rebuilded in the next call
     */
    public static void setConfigFile(String configFile) {
        HibernateSessionFactory.configFile = configFile;
        sessionFactory = null;
    } 

    /**
     * return hibernate configuration
     * 
     */
    public static Configuration getConfiguration() {
        return configuration;
    }

    /*
     * begin the transaction
     */
    public static void beginTransaction() {
        Transaction tx = (Transaction) tLocaltx.get();
        try {
            if (tx == null) {
                tx = getSession().beginTransaction();
                tLocaltx.set(tx);
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }

    /*
     * close the transaction
     */
    public static void commitTransaction() {
        Transaction tx = (Transaction) tLocaltx.get();
        try {
            if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack())
                tx.commit();
            tLocaltx.set(null);
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }

    /*
     * for rollbacking
     */
    public static void rollbackTransaction() {
        Transaction tx = (Transaction) tLocaltx.get();
        try {
            tLocaltx.set(null);
            if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                tx.rollback();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }

	public static HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		HibernateSessionFactory.hibernateTemplate = hibernateTemplate;
	}
	
	public static HibernateTemplate getHibernateTemplate2() {
		return hibernateTemplate2;
	}

	public void setHibernateTemplate2(HibernateTemplate hibernateTemplate) {
		HibernateSessionFactory.hibernateTemplate2 = hibernateTemplate;
	}

	public static HibernateTemplate getHibernateTemplate3() {
		return hibernateTemplate3;
	}

	public void setHibernateTemplate3(HibernateTemplate hibernateTemplate) {
		HibernateSessionFactory.hibernateTemplate3 = hibernateTemplate;
	}
}