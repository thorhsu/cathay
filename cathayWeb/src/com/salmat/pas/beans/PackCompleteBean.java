package com.salmat.pas.beans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.component.UIData;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.ibm.icu.util.Calendar;
import com.salmat.pas.conf.Constant;
import com.salmat.pas.report.GenerateReport;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.BankReceipt;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.LogisticStatus;
import com.salmat.pas.vo.PackStatus;
import com.salmat.util.HibernateSessionFactory;


/*
 * Thor新增
 */
public class PackCompleteBean extends BaseBean { 
	static Logger logger = Logger.getLogger(PackCompleteBean.class);
	private UIData dataTable;
	private SortableModel dataModel;
	private SortableModel policyDataModel;
	private UIData packDataTable;
	private SortableModel packDataModel;
	private UIData applyDataTable;
	private SortableModel applyDataModel;
	private String result;   //後端處理結果
	private String focusDecider;   //後端處理結果
	private String logisticId;
	private String packId;
	private Date cycleDate;
	private String center;
	private boolean tp2 = false;
	private String vendorId;
	private LogisticStatus ls = null;
	private Date cycleDateBegin;
	private Date cycleDateEnd;	
	private String subAreaName;
	private String subAreaId;
	private String name;
	private String address;    
	private Double weight;
	private String tel;
	private Integer year;
	private Integer month;
	private Integer date;
	private Integer hours;
	private Integer minutes;
	private boolean group;
	private boolean mailReceipt = false;
	private String scanVendorId;
	private Set<SelectItem> centers;
	private String logisticIds ;
	private String [][] resultString = new String[3][4];
	private boolean showPolicy = false;
    
	
	public String getCenter() {
		return center;
	}



	public void setCenter(String center) {
		this.center = center;
	}



	public Date getCycleDateBegin() {
		return cycleDateBegin;
	}



	public void setCycleDateBegin(Date cycleDateBegin) {
		this.cycleDateBegin = cycleDateBegin;
	}



	public Date getCycleDateEnd() {
		return cycleDateEnd;
	}



	public void setCycleDateEnd(Date cycleDateEnd) {
		this.cycleDateEnd = cycleDateEnd;
	}



	public String getLogisticId() {
		return logisticId;
	}



	public void setLogisticId(String logisticId) {
		this.logisticId = logisticId;
	}



	public String getPackId() {
		return packId;
	}



	public void setPackId(String packId) {
		this.packId = packId;
	}
	
	public String batchPackComplete(){
		AdminUser user = (AdminUser)this.getSession(true).getAttribute("loginUser");
		Date today = new Date();
		setResult("");
		Session session = null;
		Transaction tx = null;
		try {
		   session = HibernateSessionFactory.getSession();
		   tx = session.beginTransaction();
		   String [] logisticIds = this.logisticIds.split(",");
		   
		   session.createQuery("update LogisticStatus set packDone = true, sentTime = :sentTime, scanDate = :scanDate where logisticId in (:logisticIds)")
		            .setParameter("sentTime", today).setParameter("scanDate", today).setParameterList("logisticIds", logisticIds).executeUpdate();
		   session.createQuery("update PackStatus set status = 45, statusNm='等待貨運', packCompleted = true, updateDate = :updateDate "
		   		+ " where logisticId in (:logisticIds)")
		            .setParameter("updateDate", today).setParameterList("logisticIds", logisticIds).executeUpdate();
		   
		   List<ApplyData> applyDatas = session.createQuery("from ApplyData where packId in (select packId from PackStatus where logisticId in (:logisticIds))")
				   .setParameterList("logisticIds", logisticIds).list();
		   Set<String> newBatchNames = new HashSet<String>();
		   for(ApplyData applyData : applyDatas){
			   Set<BankReceipt> brs = applyData.getBankReceipts();
			   if(brs != null && brs.size() > 0){
				   for(BankReceipt br : brs){
					   Date receiveDate = br.getReceiveDate();
					   Calendar cal = Calendar.getInstance();
					   cal.setTime(receiveDate);
					   cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
					   cal.set(Calendar.MILLISECOND, 0);
					   Date recieveDateBegin = cal.getTime();

					   cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
					   cal.set(Calendar.MILLISECOND, 998);
					   Date recieveDateEnd = cal.getTime();
					   Integer dateSerialNo = br.getDateSerialNo();
					   Integer dateCenterSerialNo = br.getDateCenterSerialNo();
					   String bcenter = br.getCenter();
					
					   br.setPackDate(today);
					   br.setDateSerialNo(null);
					   br.setDateCenterSerialNo(null);
					   br.setPackUser((user.getUserName() == null || "".equals(user.getUserName()))? user.getUserId() : user.getUserName());
					   br.setStatus("交寄完成");
					   session.update(br);
					   
					   if(dateSerialNo != null){
					      String updateQuery = "update BankReceipt set dateSerialNo = (dateSerialNo - 1) where receiveDate between ? and ? and dateSerialNo > ?";					   
					      session.createQuery(updateQuery).setParameter(0, recieveDateBegin).setParameter(1, recieveDateEnd).setInteger(2, dateSerialNo).executeUpdate();
					   }
					   if(dateCenterSerialNo != null && bcenter != null){
						  String updateQuery = "update BankReceipt set dateCenterSerialNo = (dateCenterSerialNo - 1) where receiveDate between ? and ? and dateCenterSerialNo > ? and center = ?";					   
						  session.createQuery(updateQuery).setParameter(0, recieveDateBegin).setParameter(1, recieveDateEnd).setInteger(2, dateCenterSerialNo).setString(3, bcenter).executeUpdate();
					   }
					   
			      }
			   }
			   applyData.setUpdateDate(today);
			   applyData.setPackTime(today);
			   //applyData.setDeliverTime(today);
			   if(applyData.getCycleDate() == null  || today.getTime() >= applyData.getCycleDate().getTime())
				   applyData.setDeliverTime(today);
			   else
				   applyData.setDeliverTime(applyData.getCycleDate());
			   if((applyData.getSubstract() != null && applyData.getSubstract()) || (applyData.getExceptionStatus() != null && !applyData.getExceptionStatus().equals("")) && !"100".equals(applyData.getPolicyStatus()))
				  applyData.setPolicyStatus("98");
			   else if(!"100".equals(applyData.getPolicyStatus()))
			      applyData.setPolicyStatus("97");
			   session.update(applyData);
			   newBatchNames.add(applyData.getNewBatchName());
		   }
		   List<AfpFile> afps = session.createQuery("from AfpFile where newBatchName in (:newBatchNames)")
				   .setParameterList("newBatchNames", newBatchNames).list();
		   for(AfpFile afpFile : afps){
			   List<ApplyData> ads = session.createQuery("from ApplyData where newBatchName = '" + afpFile.getNewBatchName() + "' and policyStatus <> '100' and policyStatus <> '98' and policyStatus <> '97'").list();
			   if(ads != null && ads.size() == 0){
				  afpFile.setUpdateDate(today);
				  if(afpFile.getPackTime() == null)
				     afpFile.setPackTime(today);
				  afpFile.setStatus("裝箱完成");
			      session.update(afpFile);
			   }else{
				   afpFile.setUpdateDate(today);				   
				   if(afpFile.getPackTime() == null)
					    afpFile.setPackTime(today);
				   afpFile.setStatus("裝箱中");
				   session.update(afpFile);
			   }					   
		   }
		   tx.commit();
		   
		   if(logisticId != null && logisticId.toLowerCase().endsWith("l"))
			   logisticId = logisticId.substring(0, logisticId.length() - 1);
		   ls = (LogisticStatus) session.get(LogisticStatus.class, logisticId);
           center = ls.getCenter();
           cycleDate = ls.getCycleDate();		   
		   this.retreveData();	
		   String result = "";
		   int i = 0;
		   for(String logisticId: logisticIds){
			   i++;
			   result += logisticId + "，"; 			   
		   }
			setResult(result + "成功設定交寄完成");
		   
		}catch(Exception e){
			setResult(e.getMessage());
			if(tx != null)
				tx.rollback();
			logger.error("", e);
		}finally{
			if(session != null && session.isOpen())
				session.close();
			if(session != null && session.isConnected())
				session.disconnect();
		}

		
		return null;
	}
	
	public String finishedLogistic(){
		setResult("");
		Session session = null;
		Transaction tx = null;
		try {
		   session = HibernateSessionFactory.getSession();
		   tx = session.beginTransaction();
		   //北二都是送郵局交寄
		   List<PackStatus> packs = session.createQuery("from PackStatus p where p.logisticId in (select logisticId from LogisticStatus l where l.packDone = true and l.sentTime is null and l.center is not null and l.center <> '06')").list();
		   long policyCounter = 0;
		   long backCounter = 0;
		   for(PackStatus ps : packs){
			   if(!ps.isBack()){
			      List<Long> list = session.createQuery("select count(applyData) from ApplyData applyData where applyData.packId = '" + ps.getPackId() + "' and receipt = false").list();
			      if(list != null && list.size() > 0)
			         policyCounter += list.get(0);
			   }else{
				  List<Long> list = session.createQuery("select count(applyData) from ApplyData applyData where applyData.packId = '" + ps.getPackId() + "' and receipt = false").list();
				  if(list != null && list.size() > 0)
				     backCounter += list.get(0);
			   }			   
		   }
		   if(backCounter != 0 || policyCounter != 0){
			  Calendar cal = Calendar.getInstance();
			  cal.set(year, month - 1, date, hours, minutes);
			   //北二都是送郵局交寄
		      session.createQuery("update LogisticStatus set sentTime = ? where packDone = true and sentTime is null and center is not null and center <> '06'").setParameter(0, cal.getTime()).executeUpdate();
		      ErrorReport er = new ErrorReport();
		      er.setErrHappenTime(new Date());
		      er.setErrorType("PBSent");
		      er.setOldBatchName("");
		      er.setReported(false);
		      er.setMessageBody("超峰收件時間:" + Constant.yyyyMMddHHmm.format(new Date()) + "\r\n-----------" 
				      + "交寄保單:" + policyCounter + "本\r\n-----------");
		      if(backCounter != 0){
		    	  er.setMessageBody(er.getMessageBody() + 
		    			  "退回審查科保單:" + backCounter + "本\r\n-----------");		    	  
		      }
		      er.setTitle("PolicyBook Sent");
		      session.save(er);
		   }
		   tx.commit();
		   setResult("完成記錄");
		   
		}catch(Exception e){
			setResult(e.getMessage());
			if(tx != null)
				tx.rollback();
			logger.error("", e);
		}finally{
			if(session != null && session.isOpen())
				session.close();
			if(session != null && session.isConnected())
				session.disconnect();
		}

		
		return null;
	}

	public String getRowClasses(){
		String classes = "";
		
		for(int i = 0; i < packDataModel.getRowCount(); i++){		   		
		   packDataModel.setRowIndex(i);		
		   PackStatus packStatus = (PackStatus)packDataModel.getRowData();
		   if((packStatus.getPackCompleted() != null && packStatus.getPackCompleted())){
			   classes += "exception_row,";
		   }else{
			   if(i % 2 == 0)
				   classes += "even_row,";
			   else
				   classes += "odd_row,";
		   }
	    }
		if(classes.length() > 0 && classes.endsWith(","))
		   return classes.substring(0, classes.length() - 1);
		else
		   return "";		
	}
	
	public String getApplyRowClasses(){
		String classes = "";
		
		for(int i = 0; i < applyDataModel.getRowCount(); i++){		   		
		   applyDataModel.setRowIndex(i);		
		   ApplyData applyData = (ApplyData)applyDataModel.getRowData();
		   if((applyData.getPolicyStatus() != null && (applyData.getPolicyStatus().equals("100") || applyData.getPolicyStatus().equals("98") || applyData.getPolicyStatus().equals("97")))){
			   classes += "exception_row,";
		   }else{
			   if(i % 2 == 0)
				   classes += "even_row,";
			   else
				   classes += "odd_row,";
		   }
	    }
		if(classes.length() > 0 && classes.endsWith(","))
		   return classes.substring(0, classes.length() - 1);
		else
		   return "";		
	}
	
	public String getLogisticRowClasses(){
        String classes = "";		
		for(int i = 0; i < dataModel.getRowCount(); i++){		   		
		   dataModel.setRowIndex(i);		
		   LogisticStatus logisticStatus = (LogisticStatus)dataModel.getRowData();
		   if((logisticStatus.getLogisticId().equals(logisticId))){
			   classes += "processing,";
		   }else if(logisticStatus.isPackDone()){
			   classes += "exception_row,";
		   }else{
			   if(i % 2 == 0)
				   classes += "even_row,";
			   else
				   classes += "odd_row,";
		   }
	    }
		if(classes.length() > 0 && classes.endsWith(","))
		   return classes.substring(0, classes.length() - 1);
		else
		   return "";
	}

	private void retreveData(){
		Session session = null;
		
		try {
		   if(ls == null){
			   setResult("");
			   setResult("無此裝箱編號");
			   this.setFocusDecider("logisticId");			   
		   }
		   
		   session = HibernateSessionFactory.getSession();
		   Query query = session.createQuery("from LogisticStatus where cycleDate = ? and center = ? and batchOrOnline = ?  order by packDone, firstUniqueNo");
		   query.setParameter(0, cycleDate);
		   query.setString(1, center);
		   query.setString(2, ls.getBatchOrOnline());
		   List<LogisticStatus> list = query.list();
		   if(ls != null){			   
			   if(tp2 && mailReceipt){

				   List<ApplyData> applyDataList = session.getNamedQuery("ApplyData.findByPackId").setString(0, ls.getLogisticId()).list();				   			   
				   this.setApplyDataModel(new SortableModel(new ListDataModel(applyDataList)));
			   }else{
				   List<PackStatus> packsList = new ArrayList<PackStatus>();
				   packsList.addAll(ls.getPackStatuses());			   
				   setPackDataModel(new SortableModel(new ListDataModel(packsList)));				   
			   }
		   }
		   
		   boolean allFinished = true; 
		   for(LogisticStatus ls : list){
			   if(!ls.isPackDone()){
				   allFinished = false;
			       break;
			   }
		   }
		   if(allFinished){
			   setResult("所有紙袋都已裝箱完成，請送件");
		   }
		   setDataModel(new SortableModel(new ListDataModel(list)));
		}catch(Exception e){
		   logger.error("", e);
		}finally{
			if(session != null)
				session.close();
		}
	}
	
	private List<LogisticStatus>  retrieveData(Session session){

		try {
		    Criteria criteria = session.createCriteria(LogisticStatus.class, "logisticStatus");
		                    //.createAlias("logisticStatus.packStatuses", "packStatus", Criteria.INNER_JOIN);
		    criteria.add(Restrictions.between("logisticStatus.cycleDate", cycleDateBegin, cycleDateEnd));
		    if(center != null && !"".equals(center) ){
		    	criteria.add(Restrictions.eq("logisticStatus.center", center));
		    }
		    /*
		    if(subAreaName != null && !subAreaName.equals("")){
		    	criteria.add(Restrictions.like("packStatus.subAreaName", "%" + subAreaName + "%"));
		    }
		    if(subAreaId != null && !subAreaId.equals("")){
		    	String subAreaId = null;
		    	if(this.subAreaId.length() > 4)
		    		subAreaId = this.subAreaId.substring(0, 4);
		    	criteria.add(Restrictions.like("packStatus.subAreaId", "%" + subAreaId + "%"));
		    }
		    */
		    if(name != null && !"".equals(name) ){
		    	criteria.add(Restrictions.like("logisticStatus.name", "%" + name + "%"));
		    }
		    if(address != null && !"".equals(address) ){
		    	criteria.add(Restrictions.like("logisticStatus.address", "%" + address + "%"));
		    }
		    if(tel != null && !"".equals(tel) ){
		    	criteria.add(Restrictions.like("logisticStatus.tel", "%" + tel + "%"));
		    }
		    if(logisticId != null && !"".equals(logisticId) ){
		    	criteria.add(Restrictions.like("logisticStatus.logisticId", "%" + logisticId + "%"));
		    }
		    if(vendorId != null && !"".equals(vendorId) ){
		    	criteria.add(Restrictions.like("logisticStatus.vendorId", "%" + vendorId + "%"));
		    }
		    if(group){
		    	criteria.add(Restrictions.eq("logisticStatus.batchOrOnline", "G"));
		    }else{
		    	criteria.add(Restrictions.ne("logisticStatus.batchOrOnline", "G"));
		    }
		    
		    criteria.addOrder(Order.asc("center"));
		    criteria.addOrder(Order.asc("firstUniqueNo"));
		    
		    //criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
            List<LogisticStatus> list = criteria.list();
            
            return list;
		} catch (Exception e) {
			   logger.error("", e);
			   setResult(e.getMessage());
			   return null;
		} 	}
	
	public String exportLogistic(){
		setResult("");
		Session session = null;
		try {
			session = HibernateSessionFactory.getSession();
            List<LogisticStatus> list = retrieveData(session);
            if(list == null || list.size() == 0){
        	   setResult("查無貨運資料，請修改查詢條件重新查詢一次");
        	   return null;
            }
            String fileNm = GenerateReport.generateLogisticReports(list,  session, group);
            this.getRequest().setAttribute("reportNameForDownload", fileNm);
		    
		} catch (Exception e) {
			   logger.error("", e);
			   setResult(e.getMessage());
			   return null;
		} finally {
		   if (session != null && session.isOpen())
			   session.close();
		}
		return "download";	}


	public String doQuery(){
		setResult("");
		setDataModel(null);
		Session session = null;
		try {
			session = HibernateSessionFactory.getSession();
			List<LogisticStatus> list = retrieveData(session);			
			if(list == null || list.size() == 0){
				setResult("查無貨運資料，請修改查詢條件重新查詢一次");
			}else{
				Query query1 = session.createQuery("select count(a), a.sourceCode from ApplyData a where (a.groupInsure = false or a.groupInsure is null) and a.receipt = false and (a.packId is null or a.packId = '') and a.cycleDate between :cycleDateBegin and :cycleDateEnd and a.center = :center and a.sourceCode in (:sourceCodes) group by sourceCode ");
				Query query2 = session.createQuery("select count(a), a.sourceCode from ApplyData a where (a.groupInsure = false or a.groupInsure is null) and a.receipt = false and  a.packId <> '' and a.cycleDate between :cycleDateBegin and :cycleDateEnd and a.center = :center and a.sourceCode in (:sourceCodes) group by sourceCode ");
				List<String> sourceCodes = new ArrayList<String>();
				sourceCodes.add("GROUP");
				sourceCodes.add("NORM");
				sourceCodes.add("REPT");
				sourceCodes.add("CONV");
				sourceCodes.add("REIS");

				List<Object[]> list1 = query1.setDate("cycleDateBegin", cycleDateBegin).setDate("cycleDateEnd", cycleDateEnd).setString("center", center).setParameterList("sourceCodes", sourceCodes).list();
				List<Object[]> list2 = query2.setDate("cycleDateBegin", cycleDateBegin).setDate("cycleDateEnd", cycleDateEnd).setString("center", center).setParameterList("sourceCodes", sourceCodes).list();
				int [][] result = new int[3][3];
				for(Object[] row : list1){
					int count = ((Long)row[0]).intValue();
					String sourceCode = (String)row[1];
					if("REIS".equals(sourceCode) || "CONV".equals(sourceCode)){
						result[1][2] += count;
						result[1][0] += count;
					}else if("NORM".equals(sourceCode) || "REPT".equals(sourceCode)){
						result[0][2] += count;
						result[0][0] += count;
					}else if("GROUP".equals(sourceCode)){
						result[2][2] += count;
						result[2][0] += count;
					}					
				}
				for(Object[] row : list2){
					int count = ((Long)row[0]).intValue();
					String sourceCode = (String)row[1];
					if("REIS".equals(sourceCode) || "CONV".equals(sourceCode)){
						result[1][1] += count;
						result[1][0] += count;
					}else if("NORM".equals(sourceCode) || "REPT".equals(sourceCode)){
						result[0][1] += count;
						result[0][0] += count;
					}else if("GROUP".equals(sourceCode)){
						result[2][1] += count;
						result[2][0] += count;
					}					
				}
				
				resultString[0][0] = "壽險件";
				resultString[1][0] = "保全";
				resultString[2][0] = "團險件";
				for(int i = 0 ; i < 3 ; i ++){
					for(int j = 0 ; j < 3 ; j++){
						resultString[i][j + 1] = result[i][j] + "";
					}
				}
				
				
			    setDataModel(new SortableModel(new ListDataModel(list)));
				setResult("查詢成功");
			}
		} catch (Exception e) {
			   logger.error("", e);
			   setResult(e.getMessage());
			   return null;
		} finally {
		   if (session != null && session.isOpen())
			   session.close();
		}
        return null;
		
	}
	public String modifyRegisterNo(){
		setResult("");		
		Session session = null;
		Transaction tx = null;
		try {
		   session = HibernateSessionFactory.getSession();
		   tx = session.beginTransaction();
		   if(logisticId != null && logisticId.toLowerCase().endsWith("l"))
			   logisticId = logisticId.substring(0, logisticId.length() - 1);
		   ls = (LogisticStatus) session.get(LogisticStatus.class, logisticId);

		   if(ls == null){
			   setResult("無此裝箱編號");
			   this.setFocusDecider("logisticId");
			   return null;
		   }	
		   ls.setVendorId(vendorId);
		   if(this.weight != null && this.weight.doubleValue() != 0)
		      ls.setWeight(weight);
		   else
			  ls.setWeight(null);
		   session.update(ls);
		   cycleDate = ls.getCycleDate();
		   center = ls.getCenter();
		   tx.commit();
		   setResult(logisticId + "修改成功");
		   this.setFocusDecider("packId");
		}catch(Exception e){
			setResult("異常:" + e.getMessage());
			if(tx != null)
				tx.rollback();			
		}finally{
			if(session != null)
				session.close();
		}
		return null;
	}
	public String scanPack(){
		AdminUser user = (AdminUser)this.getSession(true).getAttribute("loginUser");
		setResult("");
		Date today = new Date();
		Session session = null;
		Transaction tx = null;
		try {
		   session = HibernateSessionFactory.getSession();
		   tx = session.beginTransaction();
		   if(logisticId != null && logisticId.toLowerCase().endsWith("l"))
			   logisticId = logisticId.substring(0, logisticId.length() - 1);
		   ls = (LogisticStatus) session.get(LogisticStatus.class, logisticId);
		   if(ls == null){
			   setResult("無此裝箱編號");
			   this.setFocusDecider("logisticId");
			   return null;
		   }		   
		   center = ls.getCenter();
		   this.weight = ls.getWeight();
		   Set<PackStatus> packStatuses = ls.getPackStatuses();		   
		   mailReceipt = false;
		   
		   Set<ApplyData> ads = new LinkedHashSet<ApplyData>();
		   if(ls.getBatchOrOnline() != null && ls.getBatchOrOnline().equals("B") && center.equals("06")){			   
			   tp2 = true;			   			   
			   mailReceipt = ls.getMailReceipt() == null? false : ls.getMailReceipt();
			   for(PackStatus pack : packStatuses){
				   ads.addAll(pack.getApplyDatas());
			   }
			   			   
		   }else{
			   tp2 = false;
		   }
		   boolean allFinish = true;
		   		   
		   if (!tp2) {			    
				cycleDate = ls.getCycleDate();
				PackStatus pack = null;				
				if (packId != null && !packId.trim().equals("")) {
					ls.setVendorId(vendorId);					
					if (packId.toLowerCase().endsWith("p")
							|| packId.toLowerCase().endsWith("l")) {
						packId = packId.substring(0, packId.length() - 1);
					}
					pack = (PackStatus) session.get(PackStatus.class, packId);
				} else {
					this.retreveData();
					this.setFocusDecider("packId");
					setResult("請逐本裝箱並掃描條碼");
					return null;
				}
				if (pack == null) {
					setResult("無此打包編號");
					this.setFocusDecider("packId");
					return null;
				} else if (ls.getAddress() != null
						&& pack.getAreaAddress() != null
						&& (ls.getAddress().indexOf(pack.getAreaAddress()) < 0 || !logisticId
								.equals(pack.getLogisticId()))) {
					setResult("寄件箱號不含此打包號碼");
					this.setFocusDecider("packId");
					return null;
				} else {
					setResult(packId + "已裝箱，請繼續");
					pack.setPackCompleted(true);
					pack.setUpdateDate(new Date());
					session.update(pack);
					ls.setScanDate(new Date());
					session.update(ls);
				}
		    }else{
		    	//北二掃描條碼
		    	/*
		    	 * PackStatus ps = null;
				   Set<ApplyData> ads = null;
		    	*/
				this.setVendorId(ls.getVendorId());				
				if(mailReceipt && packId != null && !packId.equals("") && (this.scanVendorId == null || this.scanVendorId.equals("") || !this.scanVendorId.equals(ls.getVendorId()))){
					setResult("回執聯號碼不正確");
					this.setFocusDecider("packId");
					return null;	
				}
				cycleDate = ls.getCycleDate();
				ApplyData applyData = null;
				if (packId != null && !packId.trim().equals("")) {										
					if (mailReceipt) {
						// 雙掛號或退回北二時是輸入保單的uniqueNo
						for (ApplyData ad : ads) {
							if (ad.getUniqueNo() != null
									&& ad.getUniqueNo().equals(packId)) {
								Set<BankReceipt> brs = null;
								brs = ad.getBankReceipts();
								if (brs != null && brs.size() > 0) {
									for (BankReceipt br : brs) {
										Date receiveDate = br.getReceiveDate();
										Calendar cal = Calendar.getInstance();
										cal.setTime(receiveDate);
										cal.set(cal.get(Calendar.YEAR),
												cal.get(Calendar.MONTH),
												cal.get(Calendar.DATE), 0, 0, 0);
										cal.set(Calendar.MILLISECOND, 0);
										Date recieveDateBegin = cal.getTime();

										cal.set(cal.get(Calendar.YEAR),
												cal.get(Calendar.MONTH),
												cal.get(Calendar.DATE), 23, 59,
												59);
										cal.set(Calendar.MILLISECOND, 998);
										Date recieveDateEnd = cal.getTime();
										Integer dateSerialNo = br
												.getDateSerialNo();
										Integer dateCenterSerialNo = br
												.getDateCenterSerialNo();
										String bcenter = br.getCenter();

										br.setPackDate(today);
										br.setDateSerialNo(null);
										br.setDateCenterSerialNo(null);
										br.setPackUser((user.getUserName() == null || ""
												.equals(user.getUserName())) ? user
												.getUserId() : user
												.getUserName());
										br.setStatus("交寄完成");
										session.update(br);
										if (dateSerialNo != null) {
											String updateQuery = "update BankReceipt set dateSerialNo = (dateSerialNo - 1) where receiveDate between ? and ? and dateSerialNo > ?";
											session.createQuery(updateQuery)
													.setParameter(0,
															recieveDateBegin)
													.setParameter(1,
															recieveDateEnd)
													.setInteger(2, dateSerialNo)
													.executeUpdate();
										}
										if (dateCenterSerialNo != null
												&& bcenter != null) {
											String updateQuery = "update BankReceipt set dateCenterSerialNo = (dateCenterSerialNo - 1) where receiveDate between ? and ? and dateCenterSerialNo > ? and center = ?";
											session.createQuery(updateQuery)
													.setParameter(0,
															recieveDateBegin)
													.setParameter(1,
															recieveDateEnd)
													.setInteger(2,
															dateCenterSerialNo)
													.setString(3, bcenter)
													.executeUpdate();
										}
									}
								}
								if((ad.getSubstract() != null && ad.getSubstract()) || (ad.getExceptionStatus() != null && !ad.getExceptionStatus().trim().equals("")) && !"100".equals(ad.getPolicyStatus()))
								   ad.setPolicyStatus("98");
								else if(!"100".equals(ad.getPolicyStatus()))
								   ad.setPolicyStatus("97");
								ad.setVerifyResult(null);
								ad.setPackTime(today);
								// ad.setDeliverTime(today);
								if (ad.getCycleDate() == null
										|| today.getTime() >= ad.getCycleDate()
												.getTime())
									ad.setDeliverTime(today);
								else
									ad.setDeliverTime(ad.getCycleDate());
								session.update(ad);
								applyData = ad;
							}
						}
						// 簽收單
						if (applyData != null) {
							Integer reprint = applyData.getReprint();
							String applyNo = applyData.getApplyNo();
							String policyNo = applyData.getPolicyNos();
							for (ApplyData ad : ads) {
								if (ad.getReceipt() != null
										&& ad.getReceipt()
										&& reprint != null
										&& ad.getReprint() != null
										&& ad.getReprint().intValue() == reprint
												.intValue()
										&& ad.getApplyNo() != null
										&& ad.getApplyNo().equals(applyNo)
										&& ad.getPolicyNos() != null
										&& ad.getPolicyNos().equals(policyNo)) {
									if((ad.getSubstract() != null && ad.getSubstract()) || (ad.getExceptionStatus() != null && !ad.getExceptionStatus().trim().equals("")) && !"100".equals(ad.getPolicyStatus()))
									   ad.setPolicyStatus("98");
								    else if(!"100".equals(ad.getPolicyStatus()))
									   ad.setPolicyStatus("97");
									
									ad.setVerifyResult(null);
									ad.setPackTime(today);
									// ad.setDeliverTime(today);
									if (ad.getCycleDate() == null
											|| today.getTime() >= ad
													.getCycleDate().getTime())
										ad.setDeliverTime(today);
									else
										ad.setDeliverTime(ad.getCycleDate());
									session.update(ad);
								}
								// 檢查看看是不是全部完成
								if (!ad.getPolicyStatus().equals("100") && !ad.getPolicyStatus().equals("97") && !ad.getPolicyStatus().equals("98")) {
									allFinish = false;
								}
							}
							// 如果全部完成
							if (allFinish) {
								for (PackStatus ps : packStatuses) {
									ps.setPackCompleted(true);
									ps.setUpdateDate(today);
									ps.setStatus(45);
									ps.setStatusNm("等待貨運");
									session.update(ps);
								}
							}
						}
					}else{
						cycleDate = ls.getCycleDate();
						PackStatus pack = null;
						if (packId != null && !packId.trim().equals("")) {
							if (packId.toLowerCase().endsWith("p")
									|| packId.toLowerCase().endsWith("l")) {
								packId = packId.substring(0, packId.length() - 1);
							}
							pack = (PackStatus) session.get(PackStatus.class, packId);
						} else {
							this.retreveData();
							this.setFocusDecider("packId");
							setResult("請逐本裝箱並掃描條碼");
							return null;
						}
						if (pack == null) {
							setResult("無此打包編號");
							this.setFocusDecider("packId");
							return null;
						} else if (ls.getAddress() != null
								&& pack.getAreaAddress() != null
								&& (ls.getAddress().indexOf(pack.getAreaAddress()) < 0 || !logisticId
										.equals(pack.getLogisticId()))) {
							setResult("寄件箱號不含此打包號碼");
							this.setFocusDecider("packId");
							return null;
						} else {
							setResult(packId + "已裝箱，請繼續");
							pack.setPackCompleted(true);
							pack.setUpdateDate(new Date());
							session.update(pack);
							ls.setScanDate(new Date());
							session.update(ls);
						}
					}
			
				} else {
					
					this.vendorId = ls.getVendorId();
					this.retreveData();
					this.setFocusDecider("packId");
					setResult("請逐本裝箱並掃描條碼");
					return null;
				}
				if (mailReceipt	&& applyData == null) {
					setResult("無此保單右上角號碼");
					this.setFocusDecider("packId");
					return null;
				}else {
					setResult(packId + "已裝箱，請繼續");
					ls.setScanDate(new Date());
					session.update(ls);
				}
		    }
			
		   //檢查pack是 不是全部完成		   
		   if(!tp2 || !mailReceipt ){
		      for(PackStatus packStatus : packStatuses){
			      if(packStatus.getPackCompleted() == null || !packStatus.getPackCompleted()){
				      allFinish = false;
				      break;
			      }
		      }
		   }
		   tx.commit();
		   
		   if(allFinish){
			   tx = session.beginTransaction();
			   setResult(logisticId + "已裝箱完成，請繼續下一箱");
			   Set<String> afpFileIds = new HashSet<String>();
			   ls.setPackDone(true);
			   //如果是北二就設定交寄時間為現在
			   if(ls.getCenter() != null && ls.getCenter().equals("06"))
			      ls.setSentTime(cycleDate);
			   session.update(ls);			   
               tx.commit();       
               
			   for(PackStatus packStatus : packStatuses){
				   tx = session.beginTransaction();
				   String packId = packStatus.getPackId();
				   Date now = new Date();				   
				   session.createQuery("update PackStatus set status = 45, statusNm = '等待貨運', updateDate = ? where packId =?").setParameter(0, new Date()).setString(1, packId).executeUpdate();
				   if(!packStatus.isBack())
				      session.createQuery("update ApplyData set policyStatus = '97', updateDate = ?, deliverTime = ? where packId =? and policyStatus <> '100'").setParameter(0, now)
				           .setParameter(1, today).setString(2, packId).executeUpdate();
				   else
					  session.createQuery("update ApplyData set policyStatus = '98', updateDate = ?, deliverTime = ? where packId =? and policyStatus <> '100'").setParameter(0, now)
			           .setParameter(1, today).setString(2, packId).executeUpdate();
				   List<AfpFile> afpFiles = session.createQuery("from AfpFile where packIds like '%," + packId + ",%'").list();
				   if(afpFiles != null)
				      for(AfpFile afpFile :afpFiles){
					      afpFileIds.add(afpFile.getNewBatchName());
				      }
				   tx.commit();
			   }			   			   
			   session.close();
			   session = HibernateSessionFactory.getSession();			   
			   session.clear();
			   tx = session.beginTransaction();
			   this.setFocusDecider("logisticId");
			   if(afpFileIds.size() > 0){				   				   
				   session.createQuery("update AfpFile set status = '裝箱中', updateDate = :updateDate, packTime = :packTime where newBatchName in (:newBatchNames) "
				   		+ " and status <> '已交寄' ").setParameterList("newBatchNames", afpFileIds).setParameter("updateDate", today)
				   		.setParameter("packTime", today).executeUpdate();
				  /*不弄這麼複雜了，影響效能，檢查是不是全部交寄完成改由背景執行
			      List<AfpFile> afpFiles = session.createQuery("from AfpFile where newBatchName in (:newBatchNames)").setParameterList("newBatchNames", afpFileIds).list();
			      for(AfpFile afpFile : afpFiles){
			    	  //List<ApplyData> applyDatas = session.getNamedQuery("ApplyData.findByNewBatchName").setString(0,  afpFile.getNewBatchName()).list();
				      Set<ApplyData> applyDatas = afpFile.getApplyDatas();
				      boolean adAllFinish = true;
				      for(ApplyData applyData : applyDatas){
					      if(!applyData.getPolicyStatus().equals("100")){
						      adAllFinish = false;
						      break;
					      }
				      }
				      if(!adAllFinish){
					      afpFile.setStatus("部分交寄");
				      }else{
					      afpFile.setStatus("已交寄");
				      }
				      afpFile.setUpdateDate(new Date());
				      afpFile.setDeliverTime(new Date());
				      session.update(afpFile);
			      }
			      */
			   }
			   tx.commit();
		   }else{
			   this.setFocusDecider("packId");			   
		   }
		   this.retreveData();
		   return null;
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
		   if(tx != null)
			   tx.rollback();
		   
			   
		   return null;
		} finally {
		   if (session != null && session.isOpen())
			   session.close();
		}
	}
	
	
	
	public UIData getDataTable() {
		return dataTable;
	}



	public void setDataTable(UIData dataTable) {
		this.dataTable = dataTable;
	}



	public SortableModel getDataModel() {
		return dataModel;
	}



	public void setDataModel(SortableModel dataModel) {
		this.dataModel = dataModel;
	}



	public String getResult() {
		return result;
	}



	public void setResult(String result) {
		this.result = result;
	}

	public String validData() {
		return "";
	}



	public String getFocusDecider() {
		return focusDecider;
	}



	public void setFocusDecider(String focusDecider) {
		this.focusDecider = focusDecider;
	}



	public UIData getPackDataTable() {
		return packDataTable;
	}



	public void setPackDataTable(UIData packDataTable) {
		this.packDataTable = packDataTable;
	}



	public SortableModel getPackDataModel() {
		return packDataModel;
	}

	public void setPackDataModel(SortableModel packDataModel) {
		this.packDataModel = packDataModel;
	}

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}



	public String getSubAreaName() {
		return subAreaName;
	}



	public void setSubAreaName(String subAreaName) {
		this.subAreaName = subAreaName;
	}



	public String getSubAreaId() {
		return subAreaId;
	}



	public void setSubAreaId(String subAreaId) {
		this.subAreaId = subAreaId;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getAddress() {
		return address;
	}



	public void setAddress(String address) {
		this.address = address;
	}



	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}



	public int getYear() {
		if(this.year == null){
			Calendar cal = Calendar.getInstance();
			this.year = cal.get(Calendar.YEAR);
		}
		return year;
	}



	public void setYear(int year) {
		this.year = year;
	}



	public int getMonth() {
		if(this.month == null){
			Calendar cal = Calendar.getInstance();
			this.month = cal.get(Calendar.MONTH) + 1;
		}
		return month;
	}



	public void setMonth(int month) {
		this.month = month;
	}



	public int getDate() {
		if(this.date == null){
			Calendar cal = Calendar.getInstance();
			this.date = cal.get(Calendar.DATE);
		}
		return date;
	}



	public void setDate(int date) {
		this.date = date;
	}



	public int getHours() {
		if(this.hours == null){
			Calendar cal = Calendar.getInstance();
			this.hours = cal.get(Calendar.HOUR_OF_DAY);
		}
		return hours;
	}



	public void setHours(int hours) {
		this.hours = hours;
	}



	public int getMinutes() {
		if(this.minutes == null){
			Calendar cal = Calendar.getInstance();
			this.minutes = cal.get(Calendar.MINUTE);
		}
		return minutes;
	}



	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}



	public boolean isGroup() {
		return group;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}

	public boolean isTp2() {
		return tp2;
	}

	public void setTp2(boolean tp2) {
		this.tp2 = tp2;
	}



	public String getScanVendorId() {
		return scanVendorId;
	}


	public void setScanVendorId(String scanVendorId) {
		this.scanVendorId = scanVendorId;
	}



	public boolean isMailReceipt() {
		return mailReceipt;
	}

	public void setMailReceipt(boolean mailReceipt) {
		this.mailReceipt = mailReceipt;
	}

	public SortableModel getApplyDataModel() {
		return applyDataModel;
	}

	public void setApplyDataModel(SortableModel applyDataModel) {
		this.applyDataModel = applyDataModel;
	}



	public UIData getApplyDataTable() {
		return applyDataTable;
	}



	public void setApplyDataTable(UIData applyDataTable) {
		this.applyDataTable = applyDataTable;
	}

	public Set<SelectItem> getCenters() {
		if(centers == null){
			centers = new LinkedHashSet<SelectItem>();
			AdminUser adminUser = (AdminUser)this.getSession(true).getAttribute("loginUser");
			Map<String, String> centerMap = ApplyData.getCenterMap();
			if(adminUser.getCenter() == null || "".equals(adminUser.getCenter().trim())){				
				Set<String> keySet = new TreeSet<String>(centerMap.keySet());
				for(String key : keySet){
					SelectItem selectItem = new SelectItem();
					selectItem.setValue(key);
					selectItem.setLabel(centerMap.get(key));
					centers.add(selectItem);
				}				
			}else{
				SelectItem selectItem = new SelectItem();
				selectItem.setValue(adminUser.getCenter());
				selectItem.setLabel(centerMap.get(adminUser.getCenter()));
				centers.add(selectItem);
			}
		}
		return centers;
	}



	public void setCenters(Set<SelectItem> centers) {
		this.centers = centers;
	}



	public String getLogisticIds() {
		return logisticIds;
	}

	public void setLogisticIds(String logisticIds) {
		this.logisticIds = logisticIds;
	}



	public String[][] getResultString() {
		return resultString;
	}



	public void setResultString(String[][] resultString) {
		this.resultString = resultString;
	}



	public boolean isShowPolicy() {
		return showPolicy;
	}



	public void setShowPolicy(boolean showPolicy) {
		this.showPolicy = showPolicy;
	}
	
	public String policyBooks(){
		/*
		 * <f:param name="cycleDateBegin" value="#{packCompleteBean.cycleDateBegin}" />									   
									<f:param name="cycleDateEnd" value="#{packCompleteBean.cycleDateEnd}" />
									<f:param name="center" value="#{packCompleteBean.center}" />
									<f:param name="group" value="#{packCompleteBean.group}" />
									<f:param name="sourceCode" value="#{packSummary[0]}" />
									<f:param name="delivery" value="all" />
		 */
		SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
		Session session = null;

		setResult("");

		try {
			Date startDate = formatter.parse(this.getParameter("cycleDateBegin"));
			Date endDate = formatter.parse(this.getParameter("cycleDateEnd"));
			String center = this.getParameter("center");
			String sourceCode = this.getParameter("sourceCode");
			String delivery = this.getParameter("delivery");
			
			String queryStr = "from ApplyData where (groupInsure = false or groupInsure is null) and receipt = false and cycleDate between :cycleDateBegin and :cycleDateEnd and center = :center and sourceCode in (:sourceCodes) and packId is null or packId = '' ";
			if("yes".equals(delivery))
			    queryStr = "from ApplyData where (groupInsure = false or groupInsure is null) and receipt = false and cycleDate between :cycleDateBegin and :cycleDateEnd and center = :center and sourceCode in (:sourceCodes) and packId <> ''and packId is not null";
			if("all".equals(delivery))
				queryStr = "from ApplyData where (groupInsure = false or groupInsure is null) and receipt = false and cycleDate between :cycleDateBegin and :cycleDateEnd and center = :center and sourceCode in (:sourceCodes)";
			
			List<String> sourceCodes = new ArrayList<String>();
			if("壽險件".equals(sourceCode)){
				sourceCodes.add("NORM");
				sourceCodes.add("REPT");				
			}else if("保全".equals(sourceCode)){
				sourceCodes.add("REIS");
				sourceCodes.add("CONV");				
			}else if("團險件".equals(sourceCode)){
				sourceCodes.add("GROUP");				
			}
			session = HibernateSessionFactory.getSession();
			List<ApplyData> list = session.createQuery(queryStr).setDate("cycleDateBegin", startDate).setDate("cycleDateEnd", endDate).setString("center", center).setParameterList("sourceCodes", sourceCodes).list();
			if(list.size() > 0)
			   this.setShowPolicy(true);
			else
			   this.setShowPolicy(false);
			setPolicyDataModel(new SortableModel(new ListDataModel(list)));
			return null;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setResult(e.getMessage());
			return "pbDetail";
		}finally{
			if(session != null)
				session.close();
		}				
	}



	public SortableModel getPolicyDataModel() {
		return policyDataModel;
	}



	public void setPolicyDataModel(SortableModel policyDataModel) {
		this.policyDataModel = policyDataModel;
	}



	public Double getWeight() {
		return weight;
	}



	public void setWeight(Double weight) {
		this.weight = weight;
	}


}
