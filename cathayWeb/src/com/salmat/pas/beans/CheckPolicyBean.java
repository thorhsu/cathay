package com.salmat.pas.beans;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;
import javax.persistence.NamedQuery;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.ibm.icu.util.Calendar;
import com.salmat.pas.conf.Constant;
import com.salmat.pas.report.GenerateReport;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.BankReceipt;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.JobBag;
import com.salmat.pas.vo.LogisticStatus;
import com.salmat.pas.vo.PackStatus;
import com.salmat.util.HibernateSessionFactory;

public class CheckPolicyBean extends BaseBean {
	
	private String newBatchName;
	private String inputNo;
	private String success = "false";
	private boolean verify = false;
	private String verifyResult;
	private UIData dataTable;
	private SortableModel dataModel;
	private boolean substract;
	private String substractModifiderName;

	private String result;
	private Date cycleDate;
	private String center;
	private String jobBagNo;
	private AdminUser user;
	private List<Area> auditCenters = null;
	private boolean forceSubmit;
	List<ApplyData> queryResult = null;

	Logger logger = Logger.getLogger(CheckPolicyBean.class);
	
	/**
	 * 寫入資料庫前的資料驗證
	 * 
	 * @return 錯誤訊息
	 */
	public String validData() {
		return "";
	}	
	//之前會用保單驗單失敗，簽收單同時設定失敗
	//之後不再使用這個檢查
	//以後只能用保單進行驗單，邏輯會簡單一點
	public String getVerifyResults(){
		List<String> verifyResults = null;
		verifyResults = (List<String>) this.getSession(true).getAttribute("verifyResults");
		Session session = null;

		if(verifyResults == null)
		   try {			
			   session = HibernateSessionFactory.getSession();
			   verifyResults = session.getNamedQuery("ApplyData.distinctResult").list();
			   this.getSession(true).setAttribute("verifyResults", verifyResults);
			   this.getSession(true).setAttribute("verifyResultsStr", null);
		   } catch (Exception e) {
			   logger.error("", e);
		   } finally {
			   if (session != null)
				   session.close();
		   }
		String returnStr = this.getSession(true).getAttribute("verifyResultsStr") == null? "" : (String)this.getSession(true).getAttribute("verifyResultsStr");
		if(!returnStr.equals("")){
			return "[" + returnStr + "]";
		}else if(verifyResults != null && verifyResults.size() > 0){
			for(String result: verifyResults){
				returnStr += "' " + result + "',";
			}
			this.getSession(true).setAttribute("verifyResultsStr", returnStr);
			return "[" + returnStr + "]";
		}else{
			return "null";
		}
	} 
	
	//rowClasses="odd_row,even_row"
	public String getRowClass(){
		String classes = "";
		
		for(int i = 0; i < dataModel.getRowCount(); i++){		   		
		   dataModel.setRowIndex(i);		
		   ApplyData applyData = (ApplyData)dataModel.getRowData();
		   if((applyData.getExceptionStatus() != null && !"".equals(applyData.getExceptionStatus())) 
				   || (applyData.getSubstract() != null && applyData.getSubstract())){
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

	/**
	 * 取得與設定頁面資料，包含Data model與url map
	 * 
	 * @param tmpPid
	 */
	public boolean retrieveData() {
		if(user == null)
		   user = (AdminUser) this.getSession(true).getAttribute("loginUser");
		Session session = null;
		Transaction tx = null;
		Session session2 = null;
		Date today = new Date();
		try{
			session2 = HibernateSessionFactory.getHibernateTemplate2().getSessionFactory().openSession();
			session = HibernateSessionFactory.getSession();
			String jobBagNo = this.jobBagNo.trim().substring(0, 15);
			JobBag jobBag = (JobBag) session2.get(JobBag.class, jobBagNo);			
			if(jobBag == null)
				return false;
			
			String newBatchName = jobBag.getAfpName().toUpperCase().trim();
			if(newBatchName.endsWith(".AFP"))
				newBatchName = newBatchName.substring(0, newBatchName.length() - 4);

			this.newBatchName = newBatchName;
			if(newBatchName.startsWith("SG") || newBatchName.startsWith("GG") || newBatchName.startsWith("PD"))
				return false;
			
			String conjugateName = null;
			if(newBatchName.toUpperCase().startsWith("CA"))
				conjugateName = newBatchName.replaceFirst("CA", "SG");
			else if(newBatchName.toUpperCase().startsWith("SG"))
				conjugateName = newBatchName.replaceFirst("SG", "CA");
			else if(newBatchName.toUpperCase().startsWith("GA"))
				conjugateName = newBatchName.replaceFirst("GA", "GG");
			else if(newBatchName.toUpperCase().startsWith("GG"))
				conjugateName = newBatchName.replaceFirst("GG", "GA");

			
			tx = session.beginTransaction();
			AfpFile afpFile = (AfpFile) session.get(AfpFile.class, newBatchName);
			queryResult =  new ArrayList<ApplyData>();
			Criteria cri = session.createCriteria(ApplyData.class);
			cri.add(Restrictions.eq("newBatchName", afpFile.getNewBatchName()));
			cri.addOrder(Order.desc("exceptionStatus"));
			cri.addOrder(Order.asc("uniqueNo"));
			queryResult.addAll(cri.list());
			
			cri = session.createCriteria(ApplyData.class);
			cri.add(Restrictions.eq("newBatchName", conjugateName));
			cri.addOrder(Order.desc("exceptionStatus"));
			List<ApplyData> conjugatApplyDatas = cri.list();
			
			Query query = session.createQuery("from BankReceipt where bankReceiptId in (:bankReceiptIds)");
			if(queryResult != null && queryResult.size() > 0){
				
				//找出所有id
				List<String> bankReceiptIds = new ArrayList<String>();
				for(ApplyData applyData : queryResult){
					if(applyData.getBankReceiptId() != null && !"".equals(applyData.getBankReceiptId())){
						Set<String> bankReceiptSet = applyData.getBankReceiptIdSet();
						bankReceiptIds.addAll(bankReceiptSet);
					}
				}
				List<BankReceipt> bankReceipts = new ArrayList<BankReceipt>();
				if(bankReceiptIds.size() > 0){
					bankReceipts = query.setParameterList("bankReceiptIds", bankReceiptIds).list();
				}
				HashMap<String, BankReceipt> bankMap = new HashMap<String, BankReceipt>();
				if(bankReceipts != null){
				   for (BankReceipt bankReceipt : bankReceipts) {
					   bankMap.put(bankReceipt.getBankReceiptId(), bankReceipt);
				   }
				}
				cycleDate = queryResult.get(0).getCycleDate();
				center = queryResult.get(0).getCenter();
				boolean allFinish = true;
				for(ApplyData applyData : queryResult){
					//非抽件時檢查相對的簽收單或保單是不是有錯，有錯時把自己也設成錯誤
					String exceptionStatus = applyData.getExceptionStatus();
					if(applyData.getSubstract() != null && applyData.getSubstract()){
						applyData.setVerifyResult("抽件");
					}else{
						String policyNo = null;
						if(applyData.getPolicyNos() != null && applyData.getPolicyNoSet().size() > 0){
							for(String str : applyData.getPolicyNoSet()){
								policyNo = str;
								break;
							}
						}						 						
						if(applyData.getBankReceiptId() != null && !"".equals(applyData.getBankReceiptId())){
							boolean allMatch = true;
							Set<String> bks = applyData.getBankReceiptIdSet();
							for(String bankReceiptId : bks){
							   BankReceipt br = bankMap.get(bankReceiptId);
							   //如果
							   //1.無此送金單
							   //2.未送到FXDMS
							   //3.已寄出時
							   if(br == null || br.getReceiveDate() == null 
									   || (br.getPackDate() != null 
									       && br.getOldBatchName() != null 
									       && !br.getOldBatchName().equals(applyData.getOldBatchName()))){
								   String tmp = "尚未接收到送金單" + bankReceiptId;
								   if(br != null && br.getPackDate() != null ){									   
									   tmp = "送金單" + bankReceiptId + "於" + Constant.slashedyyyyMMdd.format(br.getPackDate()) + "連保單寄出" ;									   
								   }
								   applyData.setBkReceiptMatched(false);
								   allMatch = false;
								   applyData.setPolicyStatus("41");
								   applyData.setExceptionStatus("41");
								   String verifyResult = applyData.getVerifyResult() == null? "" : applyData.getVerifyResult().trim();
								   
								   if(verifyResult.indexOf(tmp) < 0){
									   if(!verifyResult.equals("") && !verifyResult.endsWith(","))
										    verifyResult += ",";
									   verifyResult += tmp;
								   }														   
								   applyData.setVerifyResult(verifyResult);
								   List<String> verifyResults = (List<String>) this.getSession(true).getAttribute("verifyResults"); 
								   if(verifyResults != null && verifyResult.indexOf("抽件") < 0 && verifyResult.indexOf("尚未接收到送金單") < 0
										   && !verifyResults.contains(verifyResult)){
									   verifyResults.add(verifyResult);
									   this.getSession(true).setAttribute("verifyResults", verifyResults);
									   this.getSession(true).setAttribute("verifyResultsStr", null);
								   }
								   applyData.setUpdateDate(today);                                   
								   session.update(applyData);								   
								   if(conjugatApplyDatas != null && conjugatApplyDatas.size() > 0){									   
									   for(ApplyData conjugatApplyData : conjugatApplyDatas){
										   if(conjugatApplyData.getApplyNo() != null && conjugatApplyData.getApplyNo().equals(applyData.getApplyNo())
												   && conjugatApplyData.getPolicyNos() != null && conjugatApplyData.getPolicyNos().equals(applyData.getPolicyNos())
												   && conjugatApplyData.getReprint() != null && applyData.getReprint() != null && conjugatApplyData.getReprint().intValue() == applyData.getReprint().intValue()){
											   conjugatApplyData.setPolicyStatus("41");
											   conjugatApplyData.setExceptionStatus("41");											   
											   conjugatApplyData.setVerifyResult(verifyResult);
											    
											   if(verifyResults != null && verifyResult.indexOf("抽件") < 0 && verifyResult.indexOf("尚未接收到送金單") < 0
													   && !verifyResults.contains(verifyResult)){
												   verifyResults.add(verifyResult);
												   this.getSession(true).setAttribute("verifyResults", verifyResults);
												   this.getSession(true).setAttribute("verifyResultsStr", null);
											   }
											   conjugatApplyData.setUpdateDate(today);
											   session.update(conjugatApplyData);
											   break;
										   }
									   }
								   }
							   }
							   if(br != null && br.getReceiveDate() == null && br.getPackDate() == null){
								   //如果只是尚未接收，確認它沒有關聯到任何的ApplyData
								   br.setOldBatchName(null);
								   br.setApplyData(null);
							       session.update(br);
							   }else if(br != null && br.getReceiveDate() != null && br.getPackDate() == null){
								   if(applyData.getReceipt() != null && !applyData.getReceipt() 
										   && !applyData.getPolicyStatus().equals("100") && !applyData.getPolicyStatus().equals("97") && !applyData.getPolicyStatus().equals("98")){
								      br.setApplyData(applyData);
								      br.setOldBatchName(applyData.getOldBatchName());
								      br.setMatchDate(today);
								      if(user == null)
										   user = (AdminUser) this.getSession(true).getAttribute("loginUser");
								      
								      br.setMatchUser((user.getUserName() == null || "".equals(user.getUserName()))? user.getUserId() : user.getUserName());
								      br.setStatus("配表完成");
								      session.update(br);
								      //改為一對多
								      Set<BankReceipt> bset = new HashSet<BankReceipt>();
								      if(applyData.getBankReceipts() != null){
								    	  bset = applyData.getBankReceipts();
								      }
								      bset.add(br);
							          applyData.setBankReceipts(bset);
								      session.update(applyData);
								   }
							   }
						   }								   
						   applyData.setBkReceiptMatched(allMatch);
						   session.update(applyData);
						   
					   }
						/*
					   Criteria criteria = session.createCriteria(ApplyData.class);
					   criteria.add(Restrictions.eq("cycleDate", applyData.getCycleDate()));
					   criteria.add(Restrictions.eq("applyNo", applyData.getApplyNo()));
					   criteria.add(Restrictions.like("policyNos", "%," + policyNo + ",%"));
					   if(applyData.getReceipt() != null)
					      criteria.add(Restrictions.eq("receipt", !applyData.getReceipt()));
					   criteria.add(Restrictions.eq("newBatchName", conjugateName));
					   criteria.addOrder(Order.asc("newBatchName"));
					   criteria.addOrder(Order.asc("newSerialNo"));
					   List<ApplyData> list = criteria.list();
					   
					   ApplyData conjugatApplyData = null;
					   for(ApplyData ad : conjugatApplyDatas){
						   if(ad.getPolicyNos() != null && ad.getPolicyNos().equals(applyData.getPolicyNos())  
								   && ad.getApplyNo() != null && ad.getApplyNo().equals(applyData.getApplyNo())
								   && ad.getReprint() != null && ad.getReprint().equals(applyData.getReprint())){
							   conjugatApplyData = ad;
							   break;
						   }							   
					   }
					   if(simUpdate && conjugatApplyData != null ){						   
						   if(conjugatApplyData.getExceptionStatus() != null && !"".equals(conjugatApplyData.getExceptionStatus().trim())){
							   applyData.setPolicyStatus("41");
							   String addStr = "簽收單";
							   //本身是簽收單時，設為保單
							   if(applyData.getReceipt())
								   addStr = "保單";
							   if(applyData.getVerifyResult() != null && !applyData.getVerifyResult().trim().equals(""))
							      applyData.setVerifyResult("因" + addStr + "發生" + conjugatApplyData.getPolicyStatusMap().get(conjugatApplyData.getPolicyStatus()) + "，故設定失敗");
							   
							   List<String> verifyResults = (List<String>) this.getSession(true).getAttribute("verifyResults"); 
							   if(this.verifyResult != null && !this.verifyResult.equals("")  
									   && verifyResults != null && this.verifyResult.indexOf("抽件") < 0 && this.verifyResult.indexOf("尚未接收到送金單") < 0
									   && !verifyResults.contains(applyData.getVerifyResult())){
								   verifyResults.add(applyData.getVerifyResult());
								   this.getSession(true).setAttribute("verifyResults", verifyResults);
								   this.getSession(true).setAttribute("verifyResultsStr", null);
							   }
							   
							   applyData.setPolicyStatus("41");
							   applyData.setExceptionStatus("41");							
							   session.update(applyData);
						   }						
					   }
					   */
					}

					
					Integer status = new Integer(applyData.getPolicyStatus() == null ? "0" : applyData.getPolicyStatus());
					/*
					 * policyStatusMap.put("11", "難字異常");
    	               policyStatusMap.put("12", "影像檢查");
    	               policyStatusMap.put("13", "影像異常");
    	
    	               policyStatusMap.put("14", "無影像");
    	               policyStatusMap.put("15", "轉檔中");
    	               policyStatusMap.put("16", "轉檔失敗");
    	               policyStatusMap.put("17", "轉檔成功");
					 */
					//如果在驗單中之前的話就改為驗單中
					//驗單失敗的話不更動狀況，即使送金單已送達，而原來的錯誤是因為未配送金單導致的驗單錯誤也不更動狀況
					//未配送金單的保單下午才進行，因為驗單會一直反覆的做，整批時如果一直更動送金單配對狀況會導致混亂
					if(status <= 40 && status != 11 && status != 13 && status != 14 && status != 16){
					   applyData.setPolicyStatus("40");
						//如果原有的exception狀況，
						if(exceptionStatus != null && !"".equals(exceptionStatus)){
						   applyData.setExceptionStatus("41");
						   applyData.setExceptionStatus(exceptionStatus);					   
						}
					   applyData.setUpdateDate(today);
					   session.update(applyData);
					}
                    
					Integer statusStr = new Integer(applyData.getPolicyStatus());
					if(statusStr == 40){
						allFinish = false;
					}					
				}
				/*
		    	policyStatusMap.put("50", "配表中");
		    	policyStatusMap.put("55", "配表完成");
		    	policyStatusMap.put("60", "裝箱中");    	
		    	policyStatusMap.put("65", "退件裝箱中");
		    	policyStatusMap.put("95", "裝箱完成");
		    	policyStatusMap.put("96", "退件裝箱完成");
		    	policyStatusMap.put("97", "等待貨運");
		    	policyStatusMap.put("98", "退件等待貨運");
		    	
		    	policyStatusMap.put("100", "已交寄");
		    	*/
				if(afpFile.getStatus().indexOf("配表") < 0 && afpFile.getStatus().indexOf("裝箱") < 0 
						&& afpFile.getStatus().indexOf("貨運") < 0 && afpFile.getStatus().indexOf("交寄") < 0 ){
				   if(allFinish)
					   afpFile.setStatus("驗單完成");
				   else
					   afpFile.setStatus("驗單中");

				   afpFile.setUpdateDate(today);
				   session.update(afpFile);
				}
			}
			tx.commit();
			setDataModel(new SortableModel(new ListDataModel(queryResult)));
			return true;
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			return false;
		}finally{
			if(session != null && session.isOpen())
				session.close();
			if(session2 != null && session2.isOpen())
				session2.close();
		}		
		
	}

	public String getMailType(){
		Session session = null;
		Session session2 = null;
		try{
			session2 = HibernateSessionFactory.getHibernateTemplate2().getSessionFactory().openSession();
			session = HibernateSessionFactory.getSession();
			String jobBagNo = this.jobBagNo.trim().substring(0, 15);
			JobBag jobBag = (JobBag) session2.get(JobBag.class, jobBagNo);			
			if(jobBag == null){
				setResult("查無此工單");
				return null;
			}
			String newBatchName = jobBag.getAfpName().toUpperCase().trim();
			if(newBatchName.endsWith(".AFP"))
				newBatchName = newBatchName.substring(0, newBatchName.length() - 4);
			this.newBatchName = newBatchName;
			List<ApplyData> applyDatas = session.createQuery("from ApplyData where newBatchName = ? and receipt = false order by uniqueNo").setString(0, newBatchName).list();
            String fileNm = GenerateReport.generateMailType(applyDatas);
            this.getRequest().setAttribute("reportNameForDownload", fileNm);
			return "download";	
		}catch(Exception e){
			
			setResult(e.getMessage());
			logger.error("", e);
			return null;
		}finally{
			if(session != null && session.isOpen())
				session.close();
			if(session2 != null && session2.isOpen())
				session2.close();
		}	
	}
	



	/**
	 * 
	 * @return
	 */
	public String doSubmit() {					
		getAuditCenters();
		setResult("");
		String errMsg = validData();
		if (errMsg.equals("")) {
			String updateStr = null;
			if(inputNo != null && !inputNo.trim().equals("")){
				try {
					updateStr = updateApplyData();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					setResult("Exception Happened:" + e.getMessage());
					return null;
				}
			}			
			boolean success = retrieveData();
			resetDataScroller(null);
			if(success && (inputNo == null || inputNo.equals(""))){
			   setResult("查詢成功，此批次共" + queryResult.size() + "筆");
			}else if(!success && (inputNo == null || inputNo.equals(""))){
				if(this.getNewBatchName().startsWith("SG") || this.getNewBatchName().startsWith("GG") || this.getNewBatchName().startsWith("PD"))
					setResult("查詢失敗，此工單非保單工單，請改用保單工單查詢");
				else
				    setResult("查詢失敗，請確定此工單是否相關於國泰保單");
			}else{
				setResult(updateStr);
			}
			return null;
		} else {
			// 錯誤訊息寫入sbForm:errMsg
			setResult(errMsg);
			return null;
		}
	}
	
	public String doSubmitSuccess(){
		setResult("");
		Session session = null;
		Transaction tx = null;
		Date today = new Date();
		try{
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			AfpFile afpFile = (AfpFile) session.get(AfpFile.class, newBatchName);
			List<ApplyData> applyDatas = session.getNamedQuery("ApplyData.findByNewBatchName").setString(0,  afpFile.getNewBatchName()).list();
			if(afpFile == null || applyDatas == null || applyDatas.size() == 0){
				setResult(newBatchName + ":查無資料");
				return null;
			}
			queryResult =  new ArrayList<ApplyData>();			
			queryResult.addAll(applyDatas);
			if(queryResult != null && queryResult.size() > 0){
				cycleDate = queryResult.get(0).getCycleDate();
				center = queryResult.get(0).getCenter();
				for(ApplyData applyData : queryResult){
					Integer status = new Integer(applyData.getPolicyStatus() == null ? "0" : applyData.getPolicyStatus());
					/*
					 * policyStatusMap.put("11", "難字異常");
    	               policyStatusMap.put("12", "影像檢查");
    	               policyStatusMap.put("13", "影像異常");
    	
    	               policyStatusMap.put("14", "無影像");
    	               policyStatusMap.put("15", "轉檔中");
    	               policyStatusMap.put("16", "轉檔失敗");
    	               policyStatusMap.put("17", "轉檔成功");
					 */
					if(status < 42 && status != 41 &&  status != 11 && status != 13 && status != 14 && status != 16 
							&& (applyData.getExceptionStatus() == null || "".equals(applyData.getExceptionStatus())) ){
					   applyData.setPolicyStatus("42");
					   applyData.setVerifyTime(today);
					   applyData.setUpdateDate(today);
					   session.update(applyData);
					}else if(status < 42 && status != 41 && applyData.getExceptionStatus() != null && !"".equals(applyData.getExceptionStatus())){
					   applyData.setPolicyStatus(applyData.getExceptionStatus());
					   applyData.setVerifyTime(today);
					   applyData.setUpdateDate(today);
					   session.update(applyData);
					}
				}
				/*
				policyStatusMap.put("50", "配表中");
		    	policyStatusMap.put("55", "配表完成");
		    	policyStatusMap.put("60", "裝箱中");    	
		    	policyStatusMap.put("95", "等待貨運");
		    	policyStatusMap.put("99", "部分交寄");
		    	policyStatusMap.put("100", "已交寄");
		    	*/
				if( !"配表中".equals(afpFile.getStatus()) && !"配表完成".equals(afpFile.getStatus())&& !"裝箱中".equals(afpFile.getStatus())&& !"等待貨運".equals(afpFile.getStatus())&& !"部分交寄".equals(afpFile.getStatus())&& !"已交寄".equals(afpFile.getStatus())){
				   afpFile.setStatus("驗單完成");
				   afpFile.setVerifyTime(today);
				   afpFile.setUpdateDate(today);
				   session.update(afpFile);
				}
			}
			tx.commit();
			
			queryResult =  new ArrayList<ApplyData>();			
			Criteria cri = session.createCriteria(ApplyData.class);
			cri.add(Restrictions.eq("newBatchName", afpFile.getNewBatchName()));
			cri.addOrder(Order.desc("exceptionStatus"));
			cri.addOrder(Order.asc("uniqueNo"));
			queryResult.addAll(cri.list());
			setDataModel(new SortableModel(new ListDataModel(queryResult)));
			
			
        }catch(Exception e){			
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			e.printStackTrace();			
			
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		
		return null;
	}

	private String updateApplyData() throws Exception{		
		Session session = null;
		Transaction tx = null;
		Date today = new Date();
		try{
			String updateStr = "";
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ApplyData.class);
			ApplyData applyData = null;
			ApplyData receiptAppData = null;
			boolean substract = false; //是不是抽件
			double bookWeight = 0;
			if(inputNo.length() == 17 || inputNo.length() == 16){
				criteria.add(Restrictions.eq("uniqueNo", inputNo));
				criteria.add(Restrictions.eq("newBatchName", newBatchName));
				List<ApplyData> applyDatas = criteria.list();
				if(applyDatas != null && applyDatas.size() == 1){					
					applyData = applyDatas.get(0);
					bookWeight = applyData.getWeight() == null? 0 : applyData.getWeight();
					Integer status = new Integer(applyData.getPolicyStatus());

					if(status >= 95 && !this.isForceSubmit())
						return "已裝箱完成，不能再設定驗單成功或失敗";
					this.setForceSubmit(false);
					
					if(applyData.getSubstract() != null && applyData.getSubstract())
						substract = true;
					
					//03B1403300001001
					//CA01B201404050002
					BankReceipt br = null;
					if(applyData.getBankReceiptId() != null && !"".equals(applyData.getBankReceiptId())){
						for(String bankReceiptId : applyData.getBankReceiptIdSet()){
						   br = (BankReceipt) session.get(BankReceipt.class, bankReceiptId);
						   //如果送金單未收到，就不能設定成功
						   if(br == null || br.getReceiveDate() == null
								   || (br.getPackDate() != null 
							       && br.getOldBatchName() != null 
							       && !br.getOldBatchName().equals(applyData.getOldBatchName()))){
							   if(verify)
							      updateStr = applyData.getPolicyNos() + "請先完成送金單接收後，再設定驗單成功。";
							   String tmp = "尚未接收到送金單" + bankReceiptId;
							   if(br != null && br.getPackDate() != null ){									   
								   tmp = "送金單" + bankReceiptId + "於" + Constant.slashedyyyyMMdd.format(br.getPackDate()) + "連保單寄出" ;									   
							   }
							   applyData.setVerifyResult(tmp);
							   applyData.setExceptionStatus("41");
							   verify = false;
							   applyData.setBkReceiptMatched(false);
						   }else{
							   if(user == null)
							      user = (AdminUser) this.getSession(true).getAttribute("loginUser");
							   if(applyData.getReceipt() != null && !applyData.getReceipt()){
							      br.setApplyData(applyData);
							      br.setOldBatchName(applyData.getOldBatchName());
							      br.setMatchDate(today);								   
							      br.setMatchUser((user.getUserName() == null || "".equals(user.getUserName()))? user.getUserId() : user.getUserName());
							      br.setStatus("配表完成");
							      session.update(br);
							   
							      //改為一對多
							      Set<BankReceipt> bset = new HashSet<BankReceipt>();
							      if(applyData.getBankReceipts() != null){
							   	     bset = applyData.getBankReceipts();
							      }
							      bset.add(br);
						          applyData.setBankReceipts(bset);
							      //applyData.setBankReceipt(br);
							   }
						   }
						}
					}
					String packId = applyData.getPackId();
					PackStatus packStatus = null;	
					LogisticStatus ls = null;
					if(packId != null){
						packStatus = (PackStatus) session.get(PackStatus.class, packId);
						ls = packStatus.getLogisticStatus();
					}
					String origVerifyResult = applyData.getVerifyResult() == null ? "" : applyData.getVerifyResult();
					if(verify){										 
						applyData.setVerifyResult(null);
						if(origVerifyResult.indexOf("抽件") >= 0)
						   applyData.setSubstract(false);
						updateStr = "保單" + inputNo + "驗單成功";
						if(applyData.getExceptionStatus() != null && applyData.getExceptionStatus().equals("41")){
						    updateStr += "保單" + inputNo + "重新設定為驗單成功";
						}
					    if(applyData.getPolicyStatus() == null || (applyData.getPolicyStatus() != null && 
				    		new Integer(applyData.getPolicyStatus()) <= 42)){
					        applyData.setPolicyStatus("42");
					        applyData.setExceptionStatus(null);;					    
					    }else{
						    applyData.setExceptionStatus(null);						    
						    applyData.setPackId(null);
						    applyData.setPackSatus(null);
						    if(packStatus != null){
						       packStatus.setBooks(packStatus.getBooks() - 1);
						       double psWeight = packStatus.getWeight() == null? 0 : packStatus.getWeight();
						       if((psWeight - bookWeight) > 0){
						    	   packStatus.setWeight(psWeight - bookWeight);
						       }else{
						    	   packStatus.setWeight(0D);
						       }
						    }
						    if(ls != null){
							   ls.setBooks(ls.getBooks() - 1);							   
						       double lsWeight = ls.getWeight() == null? 0 : ls.getWeight();
						       if((lsWeight - bookWeight) > 0){
						    	   ls.setWeight(lsWeight - bookWeight);
						       }else{
						    	   ls.setWeight(0D);
						       }
						    }
						    
						    if(applyData.getPolicyStatus() != null && 
						    		new Integer(applyData.getPolicyStatus()) >= 60){
						    	applyData.setPolicyStatus("55");
						    }
						}
					    
					}else{
						//錯誤時設定狀況外，還要設定理由
						if(this.getVerifyResult() != null && !this.getVerifyResult().trim().equals("")){
						   if(this.getSuccess().equals("substract") && (this.substractModifiderName == null || this.substractModifiderName.trim().equals("")))
							   return "錯誤！！！\r\n抽件時必須填入設定抽件者姓名";
						   applyData.setVerifyResult(verifyResult.trim());
						   List<String> verifyResults = (List<String>) this.getSession(true).getAttribute("verifyResults");
						   if(this.verifyResult != null && !this.verifyResult.equals("")  
								   && verifyResults != null && this.verifyResult.indexOf("抽件") < 0 && this.verifyResult.indexOf("尚未接收到送金單") < 0
								   && !verifyResults.contains(applyData.getVerifyResult())){
							   verifyResults.add(applyData.getVerifyResult());
							   this.getSession(true).setAttribute("verifyResults", verifyResults);
							   this.getSession(true).setAttribute("verifyResultsStr", null);
						   }
						   
						   
						   if(applyData.getVerifyResult().indexOf("抽件") >= 0){
							  applyData.setSubstract(true);
							  if(user == null)
								   user = (AdminUser) this.getSession(true).getAttribute("loginUser");
						      applyData.setSubstractModifiderId(user.getUserId());							  
							  applyData.setSubstractModifiderName(this.getSubstractModifiderName());							  
							  applyData.setSubstractModifiderTime(new Date());
						   }
						}else{
							return "錯誤！！！\r\n驗單錯誤時必須填入理由";
						}
						
				        if(applyData.getPolicyStatus() == null || (applyData.getPolicyStatus() != null && 
				   		    new Integer(applyData.getPolicyStatus()) < 97)){
				            applyData.setPolicyStatus("41");
				        }
				        applyData.setExceptionStatus("41");				        				        				        
				        updateStr += "保單" + inputNo + "驗單失敗";
				        
				        //如果是已經產生裝箱清單，就轉往審查科

					}
				    applyData.setVerifyTime(today);
				    applyData.setUpdateDate(today);
				    //System.out.println(applyData.getSubstract() + ":" + applyData.getVerifyResult() + ":" + applyData.getExceptionStatus());
		            session.update(applyData);
					
						
					
					//取出applyNo ，找出對應的簽收單
					String applyNo = applyData.getApplyNo();
					String policyNo = null;
					for(String policyNoStr : applyData.getPolicyNoSet()){
						policyNo = policyNoStr;
						break;
					}
					if(applyNo != null && !"".equals(applyNo)){
						//@NamedQuery(name="ApplyData.findByApplyNoAndPolicyNoAndCenerCycle", query="from ApplyData where applyNo = ? and policyNos like ? and center = ? and receipt = ? and cycleDate = ?"),
						List<ApplyData> result = session.getNamedQuery("ApplyData.findByApplyNoAndPolicyNoAndCenerCycleReprint")
								.setString(0, applyNo).setString(1, "%," + policyNo + ",%")
								.setString(2, center).setBoolean(3, true)
								.setDate(4, applyData.getCycleDate()).setInteger(5, applyData.getReprint()).list();

						if(result != null && result.size() >= 1){
							receiptAppData = result.get(0);														
							if(receiptAppData.getNewBatchName() != null 
									&& !receiptAppData.getNewBatchName().endsWith("9999")  && verify){
								//成功時，把簽收單設為驗單成功
								if("100".equals(receiptAppData.getPolicyStatus()) || "97".equals(receiptAppData.getPolicyStatus())){
									
								}else if(receiptAppData.getPolicyStatus() != null && 
							    		new Integer(receiptAppData.getPolicyStatus()) >= 60 ){
									receiptAppData.setPolicyStatus("55");
								}else{
									receiptAppData.setPolicyStatus("42");
								}
							    receiptAppData.setPackId(null); 
							    receiptAppData.setPackSatus(null);
							    if(packStatus != null)
								    packStatus.setReceipts(packStatus.getReceipts() - 1);
							    if(ls != null)
									ls.setReceipts(ls.getReceipts() - 1);
								receiptAppData.setVerifyResult(null);
								if(origVerifyResult.indexOf("抽件") >= 0)
									receiptAppData.setSubstract(false);								
								receiptAppData.setExceptionStatus(null);
								receiptAppData.setUpdateDate(today);
							    session.update(receiptAppData);
						    }else if(!"41".equals(receiptAppData.getPolicyStatus()) 
									&& receiptAppData.getNewBatchName() != null 
									&& !receiptAppData.getNewBatchName().endsWith("9999")  && !verify){
						    	//如果失敗時要把簽收單也設定成失敗
								receiptAppData.setPolicyStatus("41");
								receiptAppData.setVerifyResult("保單" + this.verifyResult);
								receiptAppData.setUpdateDate(today);
								if(applyData.getVerifyResult().indexOf("抽件") >= 0){
									receiptAppData.setSubstract(true);
									if(user == null)
									   user = (AdminUser) this.getSession(true).getAttribute("loginUser");
									receiptAppData.setSubstractModifiderId(user.getUserId());
									receiptAppData.setSubstractModifiderName(user.getUserName());
									receiptAppData.setSubstractModifiderTime(new Date());
								}
							    session.update(receiptAppData);
								updateStr += "簽收單同時設定失敗 ";
							}
                            if(receiptAppData.getNewBatchName() == null || receiptAppData.getNewBatchName().endsWith("9999")){
								receiptAppData = null;
							}
						}							
						if(!verify && applyData.getHaveInsureCard() != null && applyData.getHaveInsureCard()){
							String updateQuery = "update ApplyData set policyStatus = '41', exceptionStatus = '41',  verifyResult = '保單" + this.getVerifyResult() + "', updateDate = ? "
									+ "where groupInsure = true and center = ? and cycleDate = ? and policyNos = ? and reprint = ?";
							session.createQuery(updateQuery).setParameter(0, today)
							       .setString(1, applyData.getCenter())
							       .setParameter(2, applyData.getCycleDate())
							       .setString(3, applyData.getPolicyNos())
							       .setInteger(4, applyData.getReprint()).executeUpdate();
						}else if(verify && applyData.getHaveInsureCard() != null && applyData.getHaveInsureCard()){							
							String updateQuery = "update ApplyData set packId= null, policyStatus = '42', verifyResult = null, exceptionStatus = null, updateDate = ? "
									+ "where groupInsure = true and center = ? and cycleDate = ? and policyNos = ? and reprint = ?";
							if(origVerifyResult.indexOf("抽件") >= 0)
								updateQuery = "update ApplyData set packId= null, policyStatus = '42', substract = false, verifyResult = null, exceptionStatus = null, updateDate = ? "
										+ "where groupInsure = true and center = ? and cycleDate = ? and policyNos = ? and reprint = ?";
							if(applyData.getPolicyStatus() != null && (applyData.getPolicyStatus().compareTo("42") > 0 || "100".equals(applyData.getPolicyStatus())
									|| "97".equals(applyData.getPolicyStatus()) || "98".equals(applyData.getPolicyStatus())) ){
								if(origVerifyResult.indexOf("抽件") >= 0)
								   updateQuery = "update ApplyData set packId= null, substract = false,  PolicyStatus = '55', verifyResult = null, exceptionStatus = null, updateDate = ? "
										+ "where groupInsure = true and center = ? and cycleDate = ? and policyNos = ? and reprint = ?";								
								else
								   updateQuery = "update ApplyData set packId= null,  PolicyStatus = '55', verifyResult = null, exceptionStatus = null, updateDate = ? "
											+ "where groupInsure = true and center = ? and cycleDate = ? and policyNos = ? and reprint = ?";
							}
							int cardCounter = session.createQuery(updateQuery).setParameter(0, today)
						       .setString(1, applyData.getCenter())
						       .setParameter(2, applyData.getCycleDate())
						       .setString(3, applyData.getPolicyNos())
						       .setInteger(4, applyData.getReprint()).executeUpdate();
							packStatus.setInusreCard(packStatus.getInusreCard() == null ? 0 : packStatus.getInusreCard() - cardCounter);
							
						}
						if(!verify){
						   ErrorReport er = new ErrorReport();
						   er.setErrHappenTime(new Date());
						   er.setErrorType("verifyError");
						   er.setOldBatchName(applyData.getOldBatchName());
						   er.setReported(false);
						
						   er.setMessageBody("受理編號:" + applyData.getApplyNo() + " | 保單號碼:" + policyNo + "，驗單失敗。失敗理由:" + applyData.getVerifyResult() );
						   er.setTitle("verify error");
						   session.save(er);
						}else if(verify && packId != null && !"".equals(packId) && packStatus != null){														
							if(packStatus.getBooks() <= 0)
								session.delete(packStatus);
							else
								session.update(packStatus);
							if(ls != null){
								if(ls.getBooks() == null || ls.getBooks().intValue() <= 0)
									session.delete(ls);
								else
									session.update(ls);
							}
							
						}
					}
				}else if(applyDatas != null && applyDatas.size() > 1){
					return "保單右上角號碼重複";
				}else{
					return "保單中查無此號碼";
				}				
				
			}else{
				return "您輸入的" + inputNo + "不是保單右上角號碼，請輸入保單右上角號碼";
			}
			String packId = null;
			String group = "B";
			group = applyData.getSourceCode().equals("GROUP")? "G" : "B";
			List<PackStatus> pss = null;
			PackStatus ps = new PackStatus();			
			LogisticStatus ls = new LogisticStatus();
			ls.setCycleDate(today);
			boolean resetToBack = true;
			if(applyData != null){
				packId = applyData.getPackId();			
				if(!verify){
			       pss =  session.createQuery("from PackStatus where back = true and cycleDate = ? and center = ? and batchOrOnline = ? " )
					      .setParameter(0, applyData.getCycleDate()).setString(1, applyData.getCenter()).setString(2, group).list();
			       if(pss != null && pss.size() > 0){
			    	   for(PackStatus pack : pss){
			    		   ps = pack;
			    		   //如果本來就是退回審查科，就不用重算
			    		   if(pack.getPackId().equals(packId)){
			    			   resetToBack = false;
			    			   break;
			    		   }
			    	   }    				   
    				   if(ps.getLogisticStatus() != null)
    				      ls = ps.getLogisticStatus();
    			   }else{
    				   ps.setCreateDate(today);
    			   }	        
				}
			}
			
			//如果是失敗時，且已經產生交寄清單的做法
	        if(!verify && packId != null && !"".equals(packId.trim()) && resetToBack){
	        	//只有06時才進入，其它行政中心的作法再議
	        	if("06".equals(applyData.getCenter())){
	        		Area auditArea = null;
	        		for(Area audit : auditCenters){
	    			    if(audit.getAreaName() != null && audit.getAreaName().indexOf("北二審查科") >= 0){
	    				     auditArea = audit;
	    				}
	    			}
	        		//此保單設為已交寄，並放到退件清單中
	        		//if(!"GROUP".equals(applyData.getSourceCode())){
	        		if(true){
	        			PackStatus oldPs = null;
	        			if(packId != null)
	        			   oldPs = (PackStatus) session.get(PackStatus.class, packId);
	        			LogisticStatus oldLs = null;
	        			if(oldPs != null && oldPs.getLogisticId() != null){
	        			   oldLs = (LogisticStatus) session.get(LogisticStatus.class, oldPs.getLogisticId());
	        			}
	        			String center = applyData.getCenter();
	        			        
	        			int adCount = ps.getBooks();
	        			int oldAdCount = 0;
	        			int insureCard = (applyData.getHaveInsureCard() == null || !applyData.getHaveInsureCard())? 0 : 1;
	        			if(oldPs == null || !oldPs.getPackId().equals(ps.getPackId())){
	        				adCount++;
	        				oldAdCount = oldPs.getBooks() - 1;
	        				ps.setInusreCard((ps.getInusreCard() == null? 0 + + insureCard : ps.getInusreCard()) + insureCard);	        				
	        			}
	        			
	        			
	        			 
	        			ps.setBooks(adCount);
	        			ps.setCenter(center);
	        			ps.setCycleDate(applyData.getCycleDate());
	        			String firstUniqueNo = applyData.getUniqueNo();
	        			
	        			if(firstUniqueNo != null && ps.getFirstUniqueNo() != null && firstUniqueNo.compareTo(ps.getFirstUniqueNo()) > 0)
	        				firstUniqueNo = ps.getFirstUniqueNo();

	        			ps.setFirstUniqueNo(firstUniqueNo);	        				        			 
	        				        			
	        			
	        			Set<String> newBatchNmSet = ps.getNewBatchNmSet();
	        			newBatchNmSet.add(applyData.getNewBatchName());
	        			ps.setNewBatchNmSet(newBatchNmSet);
	        			int receipts = ps.getReceipts();
	        			int oldReceipts = 0;
	        			if(receiptAppData != null 
	        					&& (oldPs == null || !oldPs.getPackId().equals(ps.getPackId()) )){
        				    receipts ++;
	        				if(oldPs != null && receiptAppData != null) 
	        				   oldReceipts = oldPs.getReceipts() - 1;
	        			}
	        			if(oldAdCount < 0 || oldReceipts < 0){	        				
	        				       					        				
	        			}else{
	        				if(oldPs != null && !oldPs.getPackId().equals(ps.getPackId())){
	        				   oldPs.setBooks(oldAdCount);
	        				   oldPs.setReceipts(oldReceipts);
	        				   oldPs.setInusreCard((oldPs.getInusreCard() == null? 0 : oldPs.getInusreCard()) - insureCard);
	        				   double oldPsWg = oldPs.getWeight() == null? 0 : oldPs.getWeight();
		        				if((oldPsWg - bookWeight) > 0)
		        					oldPs.setWeight(oldPsWg - bookWeight);
		        				else
		        					oldPs.setWeight(0d);
		        				
	        				   session.update(oldPs);	        				   
	        				}
	        			    if(oldLs != null && !oldLs.getLogisticId().equals(ls.getLogisticId())){
	        			    	double oldLsWg = oldLs.getWeight() == null? 0 : oldLs.getWeight();
		        				if((oldLsWg - bookWeight) > 0)
		        					oldLs.setWeight(oldLsWg - bookWeight);
		        				else
		        					oldLs.setWeight(0d);
	        			    	if(oldLs.getBooks() != null && oldLs.getBooks().intValue() > 0 )
		        			        oldLs.setBooks(oldLs.getBooks() - 1);
	        			    	if(oldLs.getReceipts() != null && oldLs.getReceipts().intValue() > 0 && receiptAppData != null)
		        			        oldLs.setReceipts(oldLs.getReceipts() - 1);
	        			    	if(oldAdCount <= 0 && oldLs.getPacks() != null && oldLs.getPacks().intValue() > 0)
	        			    	    oldLs.setPacks(oldLs.getPacks() - 1);
	        			    	
		        			    session.update(oldLs);
		        			}
	        			}
	        			//舊的packId不同時要增加重量
	        			if(oldPs == null || !oldPs.getPackId().equals(ps.getPackId())){
	        			   double weight = ps.getWeight() == null? 0 : ps.getWeight();
	        			   ps.setWeight(weight + bookWeight);
	        			}
	        			if(oldLs == null || !oldLs.getLogisticId().equals(ls.getLogisticId())){
	        			   double weight = ls.getWeight() == null? 0 : ls.getWeight();
	        			   ls.setWeight(weight + bookWeight);
	        			}
	        			
	        			ps.setReceipts(receipts);
	        			ps.setReported(false);
	        			ps.setSubAreaId(auditArea.getSubAreaId());
	        			ps.setSubAreaName(auditArea.getAreaName());
	        			ps.setUpdateDate(today);
	        			ps.setZipCode(auditArea.getZipCode());
	        			ps.setBack(true);
	        			ps.setBatchOrOnline(group);
	        			ps.setStatus(45);
	        			ps.setStatusNm("待交寄");
	        			ps.setPackCompleted(true);
	        			ps.setAreaAddress(auditArea.getZipCode() + " " + auditArea.getAddress());
						ps.setPolicyScanDate(today);
						ps.setReceiptScanDate(today);
						ps.setLabelScanDate(today);
						ps.setPolicyScanUser("SYSTEM");
						ps.setReceiptScanUser("SYSTEM");
						ps.setLabelScanUser("SYSTEM");
						
						
						ls.setTel(auditArea.getTel());
						ls.setBatchOrOnline(group);
						ls.setBooks(adCount);
						ls.setCenter(center);
						ls.setCycleDate(applyData.getCycleDate());
						ls.setFirstUniqueNo(firstUniqueNo);
						ls.setName(auditArea.getAreaName());
						ls.setAddress(auditArea.getZipCode() + " " + auditArea.getAddress());												 
						ls.setSentTime(cycleDate);
						ls.setPackDone(true);
						ls.setPacks(1);
						ls.setVendorId(null);
						ls.setReceipts(receipts);
						ls.setScanDate(today);
						ls.setMailReceipt(false);						
						 
						if(ps.getPackId() == null || ps.getPackId().trim().equals("")){
							String newPackId = null;
							if(group.equals("B")){
							   Query query = session.createQuery("select max(packId) from PackStatus where cycleDate = ? and batchOrOnline = 'B' and center = '06'");
							   List<String> maxString =  query.setParameter(0, cycleDate).list();
							   if(maxString != null && maxString.size() > 0 && maxString.get(0) != null){					
								   newPackId = maxString.get(0);
								   String suffix = StringUtils.leftPad((new Integer(newPackId.substring(newPackId.length() - 4)) + 1) + "", 4 , '0');
								   newPackId = newPackId.substring(0, newPackId.length() - 4) + suffix;
							   }else{						
								   newPackId = Constant.yyMMdd.format(cycleDate) + "TPE" + StringUtils.leftPad("1", 4, '0');
							   }
							}else{
								String cycleStr = Constant.yyMMdd.format(applyData.getCycleDate());
								newPackId = cycleStr + center + "ERR" + "001";														
								// 決定PK
							}
							ps.setPackId(newPackId);
							ps.setLogisticId(newPackId);
							ls.setLogisticId(newPackId);	
							ps.setLogisticStatus(ls);
						}
						if(ls.getLogisticId() == null && ps.getLogisticId() != null)
							ls.setLogisticId(ps.getLogisticId());
						else if(ls.getLogisticId() == null)
							ls.setLogisticId(ps.getPackId());
						
						session.saveOrUpdate(ls);
						session.saveOrUpdate(ps);
						
						applyData.setPackSatus(ps);
						applyData.setPackId(ps.getPackId());
						applyData.setPolicyStatus("98");
						applyData.setUpdateDate(today);
						if(applyData.getCycleDate() == null  || today.getTime() >= applyData.getCycleDate().getTime())
						   applyData.setDeliverTime(today);
						else
						   applyData.setDeliverTime(applyData.getCycleDate());
						session.update(applyData);
						
						//團險證也要搬去新的packStatus
						if(applyData.getHaveInsureCard() != null && applyData.getHaveInsureCard() && oldPs != null){
							List<ApplyData> insureCardL = session.createCriteria(ApplyData.class).add(Restrictions.eq("packId", oldPs.getPackId()))
							   .add(Restrictions.eq("groupInsure", true)).add(Restrictions.eq("policyNos", applyData.getPolicyNos()))
							   .add(Restrictions.eq("reprint", applyData.getReprint())).add(Restrictions.isNull("receipt"))
							   .add(Restrictions.eq("cycleDate", applyData.getCycleDate())).list();
							if(insureCardL != null && insureCardL.size() > 0){
								ApplyData card = insureCardL.get(0);
								card.setPackSatus(ps);
								card.setPackId(ps.getPackId());
								card.setPolicyStatus("98");
								card.setUpdateDate(today);
								if(card.getCycleDate() == null  || today.getTime() >= card.getCycleDate().getTime())
								   card.setDeliverTime(today);
								else
								   card.setDeliverTime(card.getCycleDate());
								session.update(card);								
							}
						}
																		
						if(receiptAppData != null){
							receiptAppData.setPackSatus(ps);
							receiptAppData.setPackId(ps.getPackId());
							receiptAppData.setPolicyStatus("98");
							receiptAppData.setUpdateDate(today);
							receiptAppData.setDeliverTime(today);
							if(receiptAppData.getCycleDate() == null  || today.getTime() >= receiptAppData.getCycleDate().getTime())
							   receiptAppData.setDeliverTime(today);
						    else
							   receiptAppData.setDeliverTime(receiptAppData.getCycleDate());
							session.update(receiptAppData);
						}
						
						Set<BankReceipt> brs = applyData.getBankReceipts();
						if(brs != null && brs.size() > 0){
							if(user == null)
								user = (AdminUser) this.getSession(true).getAttribute("loginUser");
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
					   if(oldPs != null){
        				   //看看是不是都沒有保單被包含在打包清單中
        				   List<ApplyData> applyDatas = session.getNamedQuery("ApplyData.findByPackId")
        						   .setString(0, oldPs.getPackId()).list();
        				   
        				   if(applyDatas == null || applyDatas.size() == 0){
        					   if(oldLs != null){
        						   List<ApplyData> lsAds = session.createQuery("from ApplyData where packId in (select packId from PackStatus where logisticId = ?) ")
                						   .setString(0, oldLs.getLogisticId()).list();
        						   if(lsAds == null || lsAds.size() == 0)
   	        					      session.delete(oldLs);
        					   }
        					   session.delete(oldPs);        					   
        				   }
        			   }	 						
	        		}
	        		
	        	}else{
	        		//其它中心的做法之後再說
	        	}
	        }
			
			
			if(substract){
				updateStr = "此件為抽件  " + updateStr;
			}			
			tx.commit();
			
			return updateStr;
		}catch(Exception e){
			
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			e.printStackTrace();			
			throw e;
			
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
	}
	
	/**
	 * For頁面查詢事件後呼叫
	 * 
	 * @param e
	 */
	public void resetDataScroller(ActionEvent e) {
		if (dataTable != null)
			dataTable.setFirst(0);
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
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

	public String getNewBatchName() {
		return newBatchName;
	}

	public void setNewBatchName(String newBatchName) {
		this.newBatchName = newBatchName;
	}

	public String getInputNo() {
		return inputNo;
	}

	public void setInputNo(String inputNo) {
		this.inputNo = inputNo;
	}

	public boolean isVerify() {
		return verify;
	}

	public void setVerify(boolean verify) {
		this.verify = verify;
	}

	public String getVerifyResult() {
		return verifyResult;
	}

	public void setVerifyResult(String verifyResult) {		
		if(verifyResult != null)
		    this.verifyResult = verifyResult.trim();
		else
			this.verifyResult = verifyResult;
	}

	public Date getCycleDate() {
		return cycleDate;
	}

	public void setCycleDate(Date cycleDate) {
		this.cycleDate = cycleDate;
	}

	public String getCenter() {
		return center;
	}

	public void setCenter(String center) {
		this.center = center;
	}

	public String getJobBagNo() {
		return jobBagNo;
	}

	public void setJobBagNo(String jobBagNo) {
		this.jobBagNo = jobBagNo;
	}

	public boolean isForceSubmit() {
		return forceSubmit;
	}

	public void setForceSubmit(boolean forceSubmit) {
		this.forceSubmit = forceSubmit;
	}

	public List<Area> getAuditCenters() {
		if(auditCenters == null){
			auditCenters = (List<Area>) this.getSession(true).getAttribute("auditCenters");
			if(auditCenters == null){
				Session session = HibernateSessionFactory.getSession();
				List<Area> auditCenters = session.getNamedQuery("Area.findHaveAddressAndAudit").list();											
				this.getSession(true).setAttribute("auditCenters", auditCenters);
				session.close();
			}
		}
		return auditCenters;
	}

	public void setAuditCenters(List<Area> auditCenters) {
		this.auditCenters = auditCenters;
	}

	public String getSuccess() {		
		return success;
	}

	public void setSuccess(String success) {
		this.substract = false;
		if("substract".equals(success)){
			this.substract = true;
			this.verify = false;
		}else if("false".equals(success)){
			this.verify = false;
		}else if("true".equals(success)){
			this.verify = true;
		}else{
			this.verify = false;
		}
		this.success = success;
	}
	public String getSubstractModifiderName() {
		return substractModifiderName;
	}
	public void setSubstractModifiderName(String substractModifiderName) {
		this.substractModifiderName = substractModifiderName;
	}
	
        	
	
}
