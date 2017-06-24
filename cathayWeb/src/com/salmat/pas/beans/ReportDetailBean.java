package com.salmat.pas.beans;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.model.ListDataModel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.ibm.icu.util.Calendar;
import com.salmat.pas.bo.PropertiesService;
import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.Properties;
import com.salmat.util.HibernateSessionFactory;
import com.salmat.util.SftpClientUtil;

public class ReportDetailBean extends BaseBean {	
	private static Logger logger = Logger
			.getLogger(ReportDetailBean.class);
	private UIData dataTable;
	private SortableModel dataModel;
	private Date startDate;
	private Date endDate;
	
	public UIData getDataTable() {
		return dataTable;
	}

	public void setDataTable(UIData dataTable) {
		this.dataTable = dataTable;
	}

	public SortableModel getDataModel() {
		Session session = null;
		
		try{
		   if(this.dataModel == null){
		      Calendar calStart = Calendar.getInstance();
		      calStart.set(calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DATE), 0, 0, 0);
		      calStart.add(Calendar.HOUR, -5);
		      Calendar calEnd = Calendar.getInstance();
		      calEnd.add(Calendar.HOUR, 1);
		      session = HibernateSessionFactory.getSession();
	          List<ErrorReport> list  = session.createCriteria(ErrorReport.class).add(Restrictions.between("errHappenTime", calStart.getTime(), calEnd.getTime())).addOrder(Order.asc("id")).list();	       
	          setDataModel(new SortableModel(new ListDataModel(list)));
		   }
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
	
	public ReportDetailBean() {
		
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
	
	public String query(){
        Session session = null;		
		try{
	       session = HibernateSessionFactory.getSession();
           List<ErrorReport> list  = null;
           Criteria criteria = session.createCriteria(ErrorReport.class);
           if(startDate != null){
        	   criteria.add(Restrictions.ge("errHappenTime", startDate));
           }else{
        	   Calendar calStart = Calendar.getInstance();
 		       calStart.set(calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DATE), 0, 0, 0);
 		      calStart.add(Calendar.HOUR, -5);
 		       criteria.add(Restrictions.ge("errHappenTime", calStart.getTime()));
           }	   
           
           if(endDate != null){
        	   Calendar cal = Calendar.getInstance();
        	   cal.setTime(endDate);
        	   cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
        	   criteria.add(Restrictions.le("errHappenTime", cal.getTime()));
           }
           list = criteria.addOrder(Order.asc("id")).list();	       
           setDataModel(new SortableModel(new ListDataModel(list)));		   
		}catch(Exception e){
			logger.error("", e);			
			
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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	

}
