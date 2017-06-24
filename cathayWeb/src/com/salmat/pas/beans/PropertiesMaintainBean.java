package com.salmat.pas.beans;

import javax.faces.component.UIData;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

import com.salmat.pas.vo.Properties;
import com.salmat.util.HibernateSessionFactory;

public class PropertiesMaintainBean extends BaseBean {	
	private static Logger logger = Logger
			.getLogger(PropertiesMaintainBean.class);
	private UIData dataTable;
	private String result;

	private static com.salmat.pas.vo.Properties myProperties;

	
	public PropertiesMaintainBean() {
		Session session = HibernateSessionFactory.getSession();
		this.myProperties = (Properties) session.get(com.salmat.pas.vo.Properties.class, 1);
		session.close();
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
	
	public String persist(){
		Session session = null;
		Transaction tx = null;
		try{
		   //指定目錄設定
		   String [] backupFoldersStr = this.getRequest().getParameterValues("backupFolders");
		   String [] keepDaysStr = this.getRequest().getParameterValues("backupFoldersKeepDays");
		   String backupFolders = "";
		   String keepDays = "";
		   for(int i = 0 ; i < backupFoldersStr.length ; i++){			   
			  //不等於空白時才更動
			  if(!backupFoldersStr[i].trim().equals("")){
			      backupFolders += backupFoldersStr[i] + ",";
			      if(keepDaysStr[i].trim().equals(""))
			     	 keepDaysStr[i] = "0";			      
			      new Integer(keepDaysStr[i]);	//測試看看能不能轉得過		      
			      keepDays += keepDaysStr[i] + ",";
			  }
			   
		   }
		   myProperties.setBackupFolders(backupFolders);
		   myProperties.setBackupFoldersKeepDays(keepDays);
		   session = HibernateSessionFactory.getSession();
		   tx = session.beginTransaction();
		   session.update(myProperties);
		   this.setResult("修改成功");
		   tx.commit();
		}catch(StaleObjectStateException e){
			if(tx != null)
				tx.rollback();
			this.setResult("有其它人修改，請重新輸入再送出");
			this.myProperties = (Properties) session.get(com.salmat.pas.vo.Properties.class, 1);			
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			this.setResult("發生錯誤：" + e.getMessage());
			this.myProperties = (Properties) session.get(com.salmat.pas.vo.Properties.class, 1);
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

	public com.salmat.pas.vo.Properties getMyProperties() {
		return myProperties;
	}

	public void setMyProperties(com.salmat.pas.vo.Properties myProperties) {
		PropertiesMaintainBean.myProperties = myProperties;
	}

	

}
