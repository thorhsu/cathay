package com.salmat.pas.beans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;
import org.apache.myfaces.component.html.ext.HtmlSelectBooleanCheckbox;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.ibm.icu.util.Calendar;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.BankReceipt;
import com.salmat.util.HibernateSessionFactory;

public class BankReceiptBean extends BaseBean {
	private UIData dataTable;
	private SortableModel dataModel;
	private String result;
	private Date startDate;
	private Date endDate;
	private Date packDateBegin;
	private Date packDateEnd;
	private Date receiveDateBegin;
	private Date receiveDateEnd;
	private Date cycleDateBegin;
	private Date cycleDateEnd;
	private String bankReceiptId;
	private String policyNo;
	private String receiver;
	private String recName;
	private String bankReceiptId1;
	private String bankReceiptId2;
	private String bankReceiptId3;
	private String bankReceiptId4;
	private String bankReceiptId5;
	private String bankReceiptId6;
	private String bankReceiptId7;
	private String bankReceiptId8;
	private String bankReceiptId9;
	private String bankReceiptId10;
	private String bankReceiptId11;
	private String bankReceiptId12;
	private String bankReceiptId13;
	private String bankReceiptId14;
	private String bankReceiptId15;
	private String bankReceiptId16;
	private String bankReceiptId17;
	private String bankReceiptId18;
	private String bankReceiptId19;
	private String bankReceiptId20;
	private String bankReceiptId21;
	private String bankReceiptId22;
	private String bankReceiptId23;
	private String bankReceiptId24;
	private String bankReceiptId25;
	private String bankReceiptId26;
	private String bankReceiptId27;
	private String bankReceiptId28;
	private String bankReceiptId29;
	private String bankReceiptId30;
	private String centerHidden;

	Logger logger = Logger.getLogger(BankReceiptBean.class); 
	 
	public List<BankReceipt>  retrieveData() throws Exception{		
		Session session = null;
		try {
			String fromWhere = this.getParameter("fromWhere");
			session = HibernateSessionFactory.getSession();
			List<AdminUser> users = session.getNamedQuery("AdminUser.findAllUser").list();
			HashMap<String, AdminUser> map = new HashMap<String, AdminUser>();
			for(AdminUser adminUser : users){
				map.put(adminUser.getUserId(), adminUser);
			}
			Criteria criteria = session.createCriteria(BankReceipt.class, "bankReceipt")
                    .createAlias("bankReceipt.applyData", "applyData", Criteria.LEFT_JOIN);
			Calendar cal = Calendar.getInstance();
			if(startDate != null)
				criteria.add(Restrictions.ge("bankReceipt.issueDate", startDate));
			if(startDate != null){
				//enddate設為23:59:59				
				cal.setTime(endDate);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);            
				this.endDate = cal.getTime();
				criteria.add(Restrictions.le("bankReceipt.issueDate", endDate));
			}
			//criteria.add(Restrictions.between("bankReceipt.issueDate", startDate, endDate));			

			if(receiveDateBegin != null)
				criteria.add(Restrictions.ge("bankReceipt.receiveDate", receiveDateBegin));
			
			if(receiveDateEnd != null){
				cal.setTime(receiveDateEnd);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
				this.receiveDateEnd = cal.getTime();
				criteria.add(Restrictions.le("bankReceipt.receiveDate", receiveDateEnd));
			}
			if(packDateBegin != null)
				criteria.add(Restrictions.ge("bankReceipt.packDate", packDateBegin));
			
			if(packDateEnd != null){
				cal.setTime(packDateEnd);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
				this.packDateEnd = cal.getTime();
				criteria.add(Restrictions.le("bankReceipt.packDate", packDateEnd));
			}
			if(cycleDateBegin != null)
				criteria.add(Restrictions.ge("applyData.cycleDate", cycleDateBegin));
			
			if(cycleDateEnd != null){
				cal.setTime(cycleDateEnd);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
				this.cycleDateEnd = cal.getTime();
				criteria.add(Restrictions.le("applyData.cycleDate", cycleDateEnd));
			}
			if(fromWhere != null && fromWhere.equals("fromCathay")){
				AdminUser user = (AdminUser) this.getSession(true).getAttribute("loginUser");
				criteria.add(Restrictions.or(Restrictions.eq("bankReceipt.issueUser", user.getUserId()), Restrictions.eq("bankReceipt.fxBackReceiver", user.getUserId())));				
			}
			
			if(bankReceiptId != null && !"".equals(this.bankReceiptId.trim()))
				criteria.add(Restrictions.like("bankReceipt.bankReceiptId", "%" + bankReceiptId + "%"));
			if(policyNo != null && !"".equals(this.policyNo.trim()))
				criteria.add(Restrictions.like("applyData.policyNos", ",%" + policyNo + "%,"));
			if(receiver != null && !"".equals(this.receiver.trim()))
				criteria.add(Restrictions.like("applyData.receiver", "%" + receiver + "%"));
			if(recName != null && !"".equals(this.recName.trim()))
				criteria.add(Restrictions.like("applyData.recName", "%" + recName + "%"));
			if(this.getCenterHidden() != null && !this.getCenterHidden().equals("")){
				criteria.add(Restrictions.eq("bankReceipt.center", this.getCenterHidden()));
			}
			criteria.addOrder(Order.asc("receiveDate")).addOrder(Order.asc("dateSerialNo"));
			List<BankReceipt> list = criteria.list();
			List<BankReceipt> retlist = new ArrayList<>();
			//把userId替換成name
			if(list != null && list.size() > 0){
				for(BankReceipt br : list){
					String name = null;
					if(br.getIssueUser() != null){
						AdminUser admUser = map.get(br.getIssueUser());
						if(admUser != null)
							name = admUser.getUserName();
					}
					if(name != null && !"".equals(name)){
						br.setIssueUser(name);
					}
					retlist.add(br);
				}
			}
			
			return retlist;
		} catch (Exception e) {
			   logger.error("", e);
			   setResult(e.getMessage());
			   throw e;
		} finally {
		   if (session != null && session.isOpen())
			   session.close();
		}	
	}
	private List<BankReceipt> getSelectedList() {
		List<BankReceipt> selected = new ArrayList<BankReceipt>();
		int first = dataTable.getFirst();
		int rows = dataTable.getRows();
		for (int i = first; i < (first + rows); i++) {
			dataTable.setRowIndex(i);
			List<UIComponent> columnList = dataTable.getChildren();
			for (UIComponent column : columnList) {
				List<UIComponent> columnChildren = column.getChildren();
				for (int n = 0; n < columnChildren.size(); n++) {
					if (columnChildren.get(n) instanceof HtmlSelectBooleanCheckbox) {
						HtmlSelectBooleanCheckbox tmpCb = (HtmlSelectBooleanCheckbox) columnChildren
								.get(n);
						if (tmpCb.getId().equals("cbSelOne")
								&& (Boolean) (tmpCb.getValue() == Boolean.TRUE)) {
							selected.add((BankReceipt) dataTable.getRowData());
						}
					}
				}
			}
		}
		return selected;
	}
	
	public String delete(){
		this.setResult("");
		List<BankReceipt> forDelete = getSelectedList();
		Session session = null;
        Transaction tx = null;        
		try {			
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			List<String> brIds = new ArrayList<String>();
		    for(BankReceipt br : forDelete){
		   	   brIds.add(br.getBankReceiptId());
		    }
		    //只有刪除接收日為null發送日不為null
		    //或
		    //只有刪除接收日不為null發送日為null
		    int deleted = session.createQuery("delete from BankReceipt where bankReceiptId in (:bankReceiptIds) "
		    		+ "and packDate is null and matchDate is null "
		    		+ "and ((receiveDate is null and issueDate is not null) or (receiveDate is not null and issueDate is null))")
		    		.setParameterList("bankReceiptIds", brIds).executeUpdate();
		    tx.commit();
		    this.setResult("刪除成功，共" + deleted + "筆");
		    List<BankReceipt> list = retrieveData();		    
			setDataModel(new SortableModel(new ListDataModel(list)));
		    
		} catch (Exception e) {
			if(tx != null && tx.isActive())
				tx.rollback();
			logger.error("", e);
			setResult(e.getMessage());
			return null;
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		return null;
	}
	
	public String submitBankReceiver(){		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		
		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();
		AdminUser user = (AdminUser) this.getSession(true).getAttribute("loginUser");
        Session session = null;
        Transaction tx = null;        
        
		try {
			
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
			cal.set(Calendar.MILLISECOND, 0);
			//找出目前最大的dateSerialNo;
			List<Integer> max = session.createQuery("select max(dateSerialNo) from BankReceipt where receiveTime >= ?").setDate(0, cal.getTime()).list();
			List<Integer> maxDateCenterSerialNo = session.createQuery("select max(dateCenterSerialNo) from BankReceipt where receiveTime >= ? and center = ?")
					.setDate(0, cal.getTime()).setString(1, this.getCenterHidden()).list();
			int dateSerialNo = 0;
			int dateCenterSerialNo = 0;
			if(max != null && max.size() > 0){
				dateSerialNo = max.get(0) == null? 0 : max.get(0);
			}
			if(maxDateCenterSerialNo != null && maxDateCenterSerialNo.size() > 0){
				dateCenterSerialNo = maxDateCenterSerialNo.get(0) == null? 0 : maxDateCenterSerialNo.get(0);
			}
			String result = "";
			int counter = 0;
			for(int i = 1 ; i <= 30 ; i++){
				//利用reflection取得方法，並取得值
				String bankReceiptId = (String)this.getClass().getMethod("getBankReceiptId" + i).invoke(this);
				if(bankReceiptId != null && !bankReceiptId.trim().equals("")){
					BankReceipt bankReceipt = (BankReceipt) session.get(BankReceipt.class, bankReceiptId);					
					if(bankReceipt == null){
						bankReceipt = new BankReceipt();
						bankReceipt.setInsertDate(today);
						bankReceipt.setBankReceiptId(bankReceiptId);
					}
					bankReceipt.setCenter(this.getCenterHidden());
					if(bankReceipt.getReceiveDate() == null){
						bankReceipt.setReceiveDate(cal.getTime());
						bankReceipt.setReceiveTime(today);
						bankReceipt.setReceiveUser((user.getUserName() == null || "".equals(user.getUserName()))? user.getUserId() : user.getUserName());
						if(bankReceipt.getMatchDate() == null && bankReceipt.getPackDate() == null)
						   bankReceipt.setStatus("FXDMS已接收");
						//if(bankReceipt.getDateSerialNo() == null) 
						bankReceipt.setDateSerialNo(++dateSerialNo); 
						//if(bankReceipt.getDateCenterSerialNo() == null) 
						bankReceipt.setDateCenterSerialNo(++dateCenterSerialNo); 
						counter ++;
					}else{
						result += bankReceiptId + "已於" + sdf.format(bankReceipt.getReceiveTime()) + "，由" + bankReceipt.getReceiveUser() + "登錄";
					}
					if(!result.equals(""))
						result += "，";
					session.saveOrUpdate(bankReceipt);
				}
			}
			result = "FXDMS接收送金單，登錄" + counter + "筆成功。" + result;
			if(result.endsWith("，"))
				result = result.substring(0, result.length() - 1);
			tx.commit();
		    List<BankReceipt> list = retrieveData();		    
			setDataModel(new SortableModel(new ListDataModel(list)));
			this.setResult(result);
			
		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			setResult(e.getMessage());
			return null;
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		return null;
	}
	
	public String submitBankReceipt(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date today = new Date();
		AdminUser user = (AdminUser) this.getSession(true).getAttribute("loginUser");
        Session session = null;
        Transaction tx = null;        
		try {
			
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			String result = "";
			int counter = 0;
			for(int i = 1 ; i <= 30 ; i++){
				String bankReceiptId = (String)BankReceiptBean.class.getMethod("getBankReceiptId" + i).invoke(this);
				if(bankReceiptId != null && !bankReceiptId.trim().equals("")){
					BankReceipt bankReceipt = (BankReceipt) session.get(BankReceipt.class, bankReceiptId);
					if(bankReceipt == null){
						bankReceipt = new BankReceipt();
						bankReceipt.setInsertDate(today);
						bankReceipt.setBankReceiptId(bankReceiptId);
					}
					if(bankReceipt.getStatus() != null && bankReceipt.getStatus().indexOf("退回國壽") >= 0){
						bankReceipt.setIssueDate(today);
						bankReceipt.setIssueUser(user.getUserId());
						bankReceipt.setFxBackReceiveDate(null);						
						bankReceipt.setStatus("國壽重新發送");
						counter ++;
					}else if(bankReceipt.getIssueDate() == null){
						bankReceipt.setIssueDate(today);
						bankReceipt.setIssueUser(user.getUserId());
						if(bankReceipt.getStatus() == null || "".equals(bankReceipt.getStatus().trim()))
						   bankReceipt.setStatus("國壽完成登錄");
						counter ++;
					}else{												
						result += bankReceiptId + "並非新單，也非國壽退件，無法發送，";
					}
					if(!result.equals(""))
						result += "，";
					session.saveOrUpdate(bankReceipt);
				}
			}
			result = "登錄送金單" + counter + "筆成功。" + result;
			if(result.endsWith("，"))
				result = result.substring(0, result.length() - 1);
			tx.commit();
			this.setBankReceiptId1(null);
			this.setBankReceiptId2(null);
			this.setBankReceiptId3(null);
			this.setBankReceiptId4(null);
			this.setBankReceiptId5(null);
			this.setBankReceiptId6(null);
			this.setBankReceiptId7(null);
			this.setBankReceiptId8(null);
			this.setBankReceiptId9(null);
			this.setBankReceiptId10(null);
			this.setBankReceiptId11(null);
			this.setBankReceiptId12(null);
			this.setBankReceiptId13(null);
			this.setBankReceiptId14(null);
			this.setBankReceiptId15(null);
			this.setBankReceiptId16(null);
			this.setBankReceiptId17(null);
			this.setBankReceiptId18(null);
			this.setBankReceiptId19(null);
			this.setBankReceiptId20(null);
			this.setBankReceiptId21(null);
			this.setBankReceiptId22(null);
			this.setBankReceiptId23(null);
			this.setBankReceiptId24(null);
			this.setBankReceiptId25(null);
			this.setBankReceiptId26(null);
			this.setBankReceiptId27(null);
			this.setBankReceiptId28(null);
			this.setBankReceiptId29(null);
			this.setBankReceiptId30(null);
		    List<BankReceipt> list = retrieveData();		    
			setDataModel(new SortableModel(new ListDataModel(list)));
			this.setResult(result);
			
		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			setResult(e.getMessage());
			return null;
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		return null;
	}
	
	public List<BankReceipt> retriveNoBr(){
		Session session = null;
		try {
			session = HibernateSessionFactory.getSession();
			List<AdminUser> users = session.getNamedQuery("AdminUser.findAllUser").list();
			HashMap<String, AdminUser> map = new HashMap<String, AdminUser>();
			for(AdminUser adminUser : users){
				map.put(adminUser.getUserId(), adminUser);
			}
			Criteria criteria = session.createCriteria(BankReceipt.class, "bankReceipt");
			criteria.add(Restrictions.isNull("bankReceipt.oldBatchName"));
			Calendar cal = Calendar.getInstance();
			if(startDate != null)
				criteria.add(Restrictions.ge("bankReceipt.issueDate", startDate));
			if(startDate != null){
				//enddate設為23:59:59				
				cal.setTime(endDate);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);            
				this.endDate = cal.getTime();
				criteria.add(Restrictions.le("bankReceipt.issueDate", endDate));
			}
			//criteria.add(Restrictions.between("bankReceipt.issueDate", startDate, endDate));			

			if(receiveDateBegin != null)
				criteria.add(Restrictions.ge("bankReceipt.receiveDate", receiveDateBegin));
			
			if(receiveDateEnd != null){
				cal.setTime(receiveDateEnd);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
				this.receiveDateEnd = cal.getTime();
				criteria.add(Restrictions.le("bankReceipt.receiveDate", receiveDateEnd));
			}
			if(packDateBegin != null)
				criteria.add(Restrictions.ge("bankReceipt.packDate", packDateBegin));
			
			if(packDateEnd != null){
				cal.setTime(packDateEnd);
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
				this.packDateEnd = cal.getTime();
				criteria.add(Restrictions.le("bankReceipt.packDate", packDateEnd));
			}
			
			if(bankReceiptId != null && !"".equals(this.bankReceiptId.trim()))
				criteria.add(Restrictions.like("bankReceipt.bankReceiptId", "%" + bankReceiptId + "%"));
			
			if(this.getCenterHidden() != null && !this.getCenterHidden().equals("")){
				criteria.add(Restrictions.eq("bankReceipt.center", this.getCenterHidden()));
			}
			criteria.addOrder(Order.asc("receiveDate")).addOrder(Order.asc("dateSerialNo"));
			List<BankReceipt> list = criteria.list();
			List<BankReceipt> retlist = new ArrayList<>();
			//把userId替換成name
			if(list != null && list.size() > 0){
				for(BankReceipt br : list){
					String name = null;
					if(br.getIssueUser() != null){
						AdminUser admUser = map.get(br.getIssueUser());
						if(admUser != null)
							name = admUser.getUserName();
					}
					if(name != null && !"".equals(name)){
						br.setIssueUser(name);
					}
					retlist.add(br);
				}
			}				
			return retlist;
		} catch (Exception e) {
			   logger.error("", e);

			   throw e;
		} finally {
		   if (session != null && session.isOpen())
			   session.close();
		}
	} 
	
	
	public String queryNoBr(){
        setResult("");
		
		try {
			if(dataTable != null) 
				dataTable.setFirst(0);
			List<BankReceipt> retlist = retriveNoBr();
			setDataModel(new SortableModel(new ListDataModel(retlist)));
			if(retlist != null && retlist.size() > 0)
		       setResult("查詢成功，共" + retlist.size() + "筆");
			else
				setResult("查無資料");
								     		    		
		} catch (Exception e) {
			   logger.error("", e);
			   setResult(e.getMessage());
			   return null;
		} 
		
		return null;
	}
	public String queryResult(){
		setResult("");
		
		try {
			if(dataTable != null) 
				dataTable.setFirst(0);			
		    List<BankReceipt> list = retrieveData();		    
			setDataModel(new SortableModel(new ListDataModel(list)));
			if(list != null)
		       setResult("查詢成功，共" + list.size() + "筆");
		} catch (Exception e) {
			   logger.error("", e);
			   setResult(e.getMessage());
			   return null;
		} 
		
		return null;
	}
	/**
	 * 寫入資料庫前的資料驗證
	 * 
	 * @return 錯誤訊息
	 */
	public String validData() {
		return "";
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


	public Date getPackDateBegin() {
		return packDateBegin;
	}


	public void setPackDateBegin(Date packDateBegin) {
		this.packDateBegin = packDateBegin;
	}


	public Date getPackDateEnd() {
		return packDateEnd;
	}


	public void setPackDateEnd(Date packDateEnd) {
		this.packDateEnd = packDateEnd;
	}


	public String getBankReceiptId() {
		return bankReceiptId;
	}


	public void setBankReceiptId(String bankReceiptId) {
		this.bankReceiptId = bankReceiptId;
	}


	public String getPolicyNo() {
		return policyNo;
	}


	public void setPolicyNo(String policyNo) {
		this.policyNo = policyNo;
	}


	public String getReceiver() {
		return receiver;
	}


	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}


	public String getRecName() {
		return recName;
	}


	public void setRecName(String recName) {
		this.recName = recName;
	}


	public Logger getLogger() {
		return logger;
	}


	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	public String getBankReceiptId1() {
		return bankReceiptId1;
	}
	public void setBankReceiptId1(String bankReceiptId1) {
		this.bankReceiptId1 = bankReceiptId1;
	}
	public String getBankReceiptId2() {
		return bankReceiptId2;
	}
	public void setBankReceiptId2(String bankReceiptId2) {
		this.bankReceiptId2 = bankReceiptId2;
	}
	public String getBankReceiptId3() {
		return bankReceiptId3;
	}
	public void setBankReceiptId3(String bankReceiptId3) {
		this.bankReceiptId3 = bankReceiptId3;
	}
	public String getBankReceiptId4() {
		return bankReceiptId4;
	}
	public void setBankReceiptId4(String bankReceiptId4) {
		this.bankReceiptId4 = bankReceiptId4;
	}
	public String getBankReceiptId5() {
		return bankReceiptId5;
	}
	public void setBankReceiptId5(String bankReceiptId5) {
		this.bankReceiptId5 = bankReceiptId5;
	}
	public String getBankReceiptId6() {
		return bankReceiptId6;
	}
	public void setBankReceiptId6(String bankReceiptId6) {
		this.bankReceiptId6 = bankReceiptId6;
	}
	public String getBankReceiptId7() {
		return bankReceiptId7;
	}
	public void setBankReceiptId7(String bankReceiptId7) {
		this.bankReceiptId7 = bankReceiptId7;
	}
	public String getBankReceiptId8() {
		return bankReceiptId8;
	}
	public void setBankReceiptId8(String bankReceiptId8) {
		this.bankReceiptId8 = bankReceiptId8;
	}
	public String getBankReceiptId9() {
		return bankReceiptId9;
	}
	public void setBankReceiptId9(String bankReceiptId9) {
		this.bankReceiptId9 = bankReceiptId9;
	}
	public String getBankReceiptId10() {
		return bankReceiptId10;
	}
	public void setBankReceiptId10(String bankReceiptId10) {
		this.bankReceiptId10 = bankReceiptId10;
	}
	public String getBankReceiptId11() {
		return bankReceiptId11;
	}
	public void setBankReceiptId11(String bankReceiptId11) {
		this.bankReceiptId11 = bankReceiptId11;
	}
	public String getBankReceiptId12() {
		return bankReceiptId12;
	}
	public void setBankReceiptId12(String bankReceiptId12) {
		this.bankReceiptId12 = bankReceiptId12;
	}
	public String getBankReceiptId13() {
		return bankReceiptId13;
	}
	public void setBankReceiptId13(String bankReceiptId13) {
		this.bankReceiptId13 = bankReceiptId13;
	}
	public String getBankReceiptId14() {
		return bankReceiptId14;
	}
	public void setBankReceiptId14(String bankReceiptId14) {
		this.bankReceiptId14 = bankReceiptId14;
	}
	public String getBankReceiptId15() {
		return bankReceiptId15;
	}
	public void setBankReceiptId15(String bankReceiptId15) {
		this.bankReceiptId15 = bankReceiptId15;
	}
	public String getBankReceiptId16() {
		return bankReceiptId16;
	}
	public void setBankReceiptId16(String bankReceiptId16) {
		this.bankReceiptId16 = bankReceiptId16;
	}
	public String getBankReceiptId17() {
		return bankReceiptId17;
	}
	public void setBankReceiptId17(String bankReceiptId17) {
		this.bankReceiptId17 = bankReceiptId17;
	}
	public String getBankReceiptId18() {
		return bankReceiptId18;
	}
	public void setBankReceiptId18(String bankReceiptId18) {
		this.bankReceiptId18 = bankReceiptId18;
	}
	public String getBankReceiptId19() {
		return bankReceiptId19;
	}
	public void setBankReceiptId19(String bankReceiptId19) {
		this.bankReceiptId19 = bankReceiptId19;
	}
	public String getBankReceiptId20() {
		return bankReceiptId20;
	}
	public void setBankReceiptId20(String bankReceiptId20) {
		this.bankReceiptId20 = bankReceiptId20;
	}
	public Date getReceiveDateBegin() {
		return receiveDateBegin;
	}
	public void setReceiveDateBegin(Date receiveDateBegin) {
		this.receiveDateBegin = receiveDateBegin;
	}
	public Date getReceiveDateEnd() {
		return receiveDateEnd;
	}
	public void setReceiveDateEnd(Date receiveDateEnd) {
		this.receiveDateEnd = receiveDateEnd;
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
	public String getBankReceiptId21() {
		return bankReceiptId21;
	}
	public void setBankReceiptId21(String bankReceiptId21) {
		this.bankReceiptId21 = bankReceiptId21;
	}
	public String getBankReceiptId22() {
		return bankReceiptId22;
	}
	public void setBankReceiptId22(String bankReceiptId22) {
		this.bankReceiptId22 = bankReceiptId22;
	}
	public String getBankReceiptId23() {
		return bankReceiptId23;
	}
	public void setBankReceiptId23(String bankReceiptId23) {
		this.bankReceiptId23 = bankReceiptId23;
	}
	public String getBankReceiptId24() {
		return bankReceiptId24;
	}
	public void setBankReceiptId24(String bankReceiptId24) {
		this.bankReceiptId24 = bankReceiptId24;
	}
	public String getBankReceiptId25() {
		return bankReceiptId25;
	}
	public void setBankReceiptId25(String bankReceiptId25) {
		this.bankReceiptId25 = bankReceiptId25;
	}
	public String getBankReceiptId26() {
		return bankReceiptId26;
	}
	public void setBankReceiptId26(String bankReceiptId26) {
		this.bankReceiptId26 = bankReceiptId26;
	}
	public String getBankReceiptId27() {
		return bankReceiptId27;
	}
	public void setBankReceiptId27(String bankReceiptId27) {
		this.bankReceiptId27 = bankReceiptId27;
	}
	public String getBankReceiptId28() {
		return bankReceiptId28;
	}
	public void setBankReceiptId28(String bankReceiptId28) {
		this.bankReceiptId28 = bankReceiptId28;
	}
	public String getBankReceiptId29() {
		return bankReceiptId29;
	}
	public void setBankReceiptId29(String bankReceiptId29) {
		this.bankReceiptId29 = bankReceiptId29;
	}
	public String getBankReceiptId30() {
		return bankReceiptId30;
	}
	public void setBankReceiptId30(String bankReceiptId30) {
		this.bankReceiptId30 = bankReceiptId30;
	}
	public String getCenterHidden() {
		return centerHidden;
	}
	public void setCenterHidden(String centerHidden) {
		this.centerHidden = centerHidden;
	}
	
	
	public String cancelBankReceiver(){
		setResult("");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date today = new Date();
		AdminUser user = (AdminUser) this.getSession(true).getAttribute("loginUser");
        Session session = null;
        Transaction tx = null;        
		try {
			String userNm = (user.getUserName() == null || "".equals(user.getUserName()))? user.getUserId() : user.getUserName();
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			String result = "";
			int counter = 0;
			for(int i = 1 ; i <= 30 ; i++){
				String bankReceiptId = (String)BankReceiptBean.class.getMethod("getBankReceiptId" + i).invoke(this);
				if(bankReceiptId != null && !bankReceiptId.trim().equals("")){
					BankReceipt bankReceipt = (BankReceipt) session.get(BankReceipt.class, bankReceiptId);
					if(bankReceipt == null){
						bankReceipt = new BankReceipt();
						bankReceipt.setInsertDate(today);
						bankReceipt.setBankReceiptId(bankReceiptId);
						bankReceipt.setReceiveTime(new Date());
						bankReceipt.setReceiveUser(userNm);
					}					
					if(bankReceipt.getOldBatchName() != null && !"".equals(bankReceipt.getOldBatchName())){
						result += bankReceiptId + "已和保單媒合，不可退回";
					}else if(bankReceipt.getReceiveDate() == null){
						result += bankReceiptId + "尚未接收或已退回，請先接收後再輸入";
					}else{						
						bankReceipt.setDateSerialNo(null);
						bankReceipt.setDateCenterSerialNo(null);
					    Integer dateSerialNo = bankReceipt.getDateSerialNo();
					    Integer dateCenterSerialNo = bankReceipt.getDateCenterSerialNo();
					    Date receiveDate = bankReceipt.getReceiveDate();
					    
						Calendar cal = Calendar.getInstance();
						cal.setTime(receiveDate);
						cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
						cal.set(Calendar.MILLISECOND, 0);
						Date recieveDateBegin = cal.getTime();

						cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
						cal.set(Calendar.MILLISECOND, 998);
						Date recieveDateEnd = cal.getTime();
						String bcenter = bankReceipt.getCenter();					    
					    
						if(dateSerialNo != null){
						    String updateQuery = "update BankReceipt set dateSerialNo = (dateSerialNo - 1) where receiveDate between ? and ? and dateSerialNo > ?";					   
						    session.createQuery(updateQuery).setParameter(0, recieveDateBegin).setParameter(1, recieveDateEnd).setInteger(2, dateSerialNo).executeUpdate();
						}
						if(dateCenterSerialNo != null && bcenter != null){
							String updateQuery = "update BankReceipt set dateCenterSerialNo = (dateCenterSerialNo - 1) where receiveDate between ? and ? and dateCenterSerialNo > ? and center = ?";					   
							session.createQuery(updateQuery).setParameter(0, recieveDateBegin).setParameter(1, recieveDateEnd).setInteger(2, dateCenterSerialNo).setString(3, bcenter).executeUpdate();
						}
						bankReceipt.setStatus(sdf.format(today) + " " + userNm + "退回國壽");
						bankReceipt.setReceiveDate(null);
						counter++;
					}
					
					if(!result.equals(""))
						result += "，";
					session.saveOrUpdate(bankReceipt);
				}
			}
			result = "退回送金單" + counter + "筆成功。" + result;
			if(result.endsWith("，"))
				result = result.substring(0, result.length() - 1);			
			tx.commit();
			this.setBankReceiptId1(null);
			this.setBankReceiptId2(null);
			this.setBankReceiptId3(null);
			this.setBankReceiptId4(null);
			this.setBankReceiptId5(null);
			this.setBankReceiptId6(null);
			this.setBankReceiptId7(null);
			this.setBankReceiptId8(null);
			this.setBankReceiptId9(null);
			this.setBankReceiptId10(null);
			this.setBankReceiptId11(null);
			this.setBankReceiptId12(null);
			this.setBankReceiptId13(null);
			this.setBankReceiptId14(null);
			this.setBankReceiptId15(null);
			this.setBankReceiptId16(null);
			this.setBankReceiptId17(null);
			this.setBankReceiptId18(null);
			this.setBankReceiptId19(null);
			this.setBankReceiptId20(null);
			this.setBankReceiptId21(null);
			this.setBankReceiptId22(null);
			this.setBankReceiptId23(null);
			this.setBankReceiptId24(null);
			this.setBankReceiptId25(null);
			this.setBankReceiptId26(null);
			this.setBankReceiptId27(null);
			this.setBankReceiptId28(null);
			this.setBankReceiptId29(null);
			this.setBankReceiptId30(null);
		    List<BankReceipt> list = retriveNoBr();		    
			setDataModel(new SortableModel(new ListDataModel(list)));
			this.setResult(result);			
		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			setResult(e.getMessage());
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		return null;
	}
	
	public String backBr(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date today = new Date();
		AdminUser user = (AdminUser) this.getSession(true).getAttribute("loginUser");
        Session session = null;
        Transaction tx = null;        
		try {
			
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			String result = "";
			int counter = 0;
			List<String> bankReceiptIds = new ArrayList<String>();
			List<BankReceipt> list = new ArrayList<BankReceipt>();
			for(int i = 1 ; i <= 30 ; i++){
				String bankReceiptId = (String)BankReceiptBean.class.getMethod("getBankReceiptId" + i).invoke(this);
				if(bankReceiptId != null && !bankReceiptId.trim().equals("")){
					BankReceipt bankReceipt = (BankReceipt) session.get(BankReceipt.class, bankReceiptId);
					//bankReceipt.setStatus(sdf.format(today) + " " + userNm + "退回國壽");
					if(bankReceipt == null){						
						result += bankReceiptId + "送金單尚未登錄無法記錄退回，";
						continue;
					}					
					String status = bankReceipt.getStatus();
					if(status == null || status.indexOf("退回國壽") < 0){
						bankReceiptIds.add(bankReceiptId);
						result += bankReceiptId + "送金單非FX退回件不記錄退回，";
						continue;
					}
					bankReceiptIds.add(bankReceiptId);
					bankReceipt.setFxBackReceiveDate(today);
					bankReceipt.setFxBackReceiver(user.getUserId());
					bankReceipt.setStatus("FXDMS退回送金單簽收");
					if(!result.equals(""))
						result += "，";
					session.update(bankReceipt);
					list.add(bankReceipt);
					counter++;
				}
			}
			result = "FX退回送金單簽收" + counter + "筆成功。" + result;
			if(result.endsWith("，"))
				result = result.substring(0, result.length() - 1);
			tx.commit();
			this.setBankReceiptId1(null);
			this.setBankReceiptId2(null);
			this.setBankReceiptId3(null);
			this.setBankReceiptId4(null);
			this.setBankReceiptId5(null);
			this.setBankReceiptId6(null);
			this.setBankReceiptId7(null);
			this.setBankReceiptId8(null);
			this.setBankReceiptId9(null);
			this.setBankReceiptId10(null);
			this.setBankReceiptId11(null);
			this.setBankReceiptId12(null);
			this.setBankReceiptId13(null);
			this.setBankReceiptId14(null);
			this.setBankReceiptId15(null);
			this.setBankReceiptId16(null);
			this.setBankReceiptId17(null);
			this.setBankReceiptId18(null);
			this.setBankReceiptId19(null);
			this.setBankReceiptId20(null);
			this.setBankReceiptId21(null);
			this.setBankReceiptId22(null);
			this.setBankReceiptId23(null);
			this.setBankReceiptId24(null);
			this.setBankReceiptId25(null);
			this.setBankReceiptId26(null);
			this.setBankReceiptId27(null);
			this.setBankReceiptId28(null);
			this.setBankReceiptId29(null);
			this.setBankReceiptId30(null);
			List<BankReceipt> retlist = new ArrayList<>();
			
			if (bankReceiptIds.size() > 0) {
				if (list != null && list.size() > 0) {
					List<AdminUser> users = session.getNamedQuery("AdminUser.findAllUser").list();
					HashMap<String, AdminUser> map = new HashMap<String, AdminUser>();
					for (AdminUser adminUser : users) {
						map.put(adminUser.getUserId(), adminUser);
					}

					for (BankReceipt br : list) {
						String name = null;
						if (br.getIssueUser() != null) {
							AdminUser admUser = map.get(br.getIssueUser());
							if (admUser != null)
								name = admUser.getUserName();
						}
						if (name != null && !"".equals(name)) {
							br.setIssueUser(name);
						}
						name = null;
						if (br.getFxBackReceiver() != null) {
							AdminUser admUser = map.get(br.getFxBackReceiver());
							if (admUser != null)
								name = admUser.getUserName();
						}
						if (name != null && !"".equals(name)) {
							br.setFxBackReceiver(name);
						}						
						retlist.add(br);
					}
				}
			}
		    
			setDataModel(new SortableModel(new ListDataModel(retlist)));
			this.setResult(result);
			
		} catch (Exception e) {			
			logger.error("", e);
			setResult(e.getMessage());
			if(tx != null)
				tx.rollback();
			return null;
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		return null;
	}
	
	
}
