package com.salmat.pas.beans;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.model.ListDataModel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.salmat.pas.bo.PropertiesService;
import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.Properties;
import com.salmat.util.HibernateSessionFactory;
import com.salmat.util.SftpClientUtil;

public class UnlockBean extends BaseBean {	
	private static Logger logger = Logger
			.getLogger(UnlockBean.class);
	private UIData dataTable;
	private SortableModel dataModel;
	
	
	
	public UIData getDataTable() {
		return dataTable;
	}

	public void setDataTable(UIData dataTable) {
		this.dataTable = dataTable;
	}

	public SortableModel getDataModel() {
		Session session = null;
		
		try{
		   session = HibernateSessionFactory.getSession();
	       List<ErrorReport> list  = session.createCriteria(ErrorReport.class).add(Restrictions.eq("errorType", "return check")).addOrder(Order.asc("id")).list();	       
	       setDataModel(new SortableModel(new ListDataModel(list)));
			
		}catch(Exception e){
			logger.error("", e);			
			
		}finally{
			if(session != null)
				session.close();
		}
		return dataModel;
	}

	public void setDataModel(SortableModel dataModel) {
		this.dataModel = dataModel;
	}

	private String result;
	
	public UnlockBean() {
		
	}

	/**
	 * 寫入資料庫前的資料驗證
	 * 
	 * @return 錯誤訊息
	 */
	public String validData() {
		String returnStr = "";
		return returnStr;
	}
	
	public String deleteRecord(){
		Session session = null;
		Transaction tx = null;
		
		try{
		   session = HibernateSessionFactory.getSession();
		   tx = session.beginTransaction();
		   List<ErrorReport> list  = session.createCriteria(ErrorReport.class).add(Restrictions.eq("errorType", "return check")).list();
		   if(list != null){
			   for(ErrorReport err : list)
				   session.delete(err);
		   }		   
		   this.setResult("刪除迴歸測試記錄");
		   tx.commit();
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			this.setResult(e.getMessage());			
		}finally{
		   if(session != null)
			   session.close();
		}
		return null;
		
	}
	
	public String unlock(){
		Session session = null;
		Transaction tx = null;
		
		try{
		   session = HibernateSessionFactory.getSession();
		   tx = session.beginTransaction();
		   Properties properties = (Properties) session.get(Properties.class, 1);
		   properties.setReturnUnlock(true);
		   session.update(properties);
		   List<ErrorReport> list  = session.createCriteria(ErrorReport.class).add(Restrictions.eq("errorType", "return check")).add(Restrictions.eq("reported", false)).list();
		   if(list != null){
			   for(ErrorReport err : list){
				   err.setReported(true);
				   session.update(err);
			   }
		   }		   
		   this.setResult("已解開迴歸測試鎖定");
		   tx.commit();
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			this.setResult(e.getMessage());			
		}finally{
		   if(session != null)
			   session.close();
		}
		return null;
	}
	public String resetUnlock(){
		Session session = null;
		Transaction tx = null;
		
		try{
		   session = HibernateSessionFactory.getSession();
		   tx = session.beginTransaction();
		   Properties properties = (Properties) session.get(Properties.class, 1);
		   properties.setResetReturnTest(true);;
		   session.update(properties);
		   List<ErrorReport> list  = session.createCriteria(ErrorReport.class).add(Restrictions.eq("errorType", "return check")).add(Restrictions.eq("reported", false)).list();
		   if(list != null){
			   for(ErrorReport err : list){
				   err.setReported(true);
				   session.update(err);
			   }
		   }	
		   this.setResult("已重置迴歸測試，約三小時後將產生全部的迴歸測試比對檔");
		   tx.commit();
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			this.setResult(e.getMessage());			
		}finally{
		   if(session != null)
			   session.close();
		}
		return null;
	}
	

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	

}
