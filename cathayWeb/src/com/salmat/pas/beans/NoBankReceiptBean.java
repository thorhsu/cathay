package com.salmat.pas.beans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.springframework.beans.BeanUtils;

import com.ibm.icu.util.Calendar;
import com.salmat.pas.bo.ApplyDataService;
import com.salmat.pas.conf.Constant;
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

public class NoBankReceiptBean extends BaseBean {
	private String newBatchName;
	private String inputNo;
	private boolean success = false;
	private String verifyResult;
	private UIData dataTable;
	private SortableModel dataModel;
	private boolean setByAnother = false;
	private String result;
	private Date cycleDate;
	private String center;
	private String jobBagNo;
	private String substractModifiderName;
	private AdminUser user;
	private Map<String, BankReceipt> bankMap;
	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
	private HashMap<String, AdminUser> userMap = null;
	private String oldBatchNames;

	Logger logger = Logger.getLogger(NoBankReceiptBean.class);

	/**
	 * 寫入資料庫前的資料驗證
	 * 
	 * @return 錯誤訊息
	 */
	public String validData() {
		return "";
	}
	
	public String backCathay(){
		Date today = new Date();
		setResult("");
		
		Session session = null;
		Transaction tx = null;
		String[] oldBatchNameArr = this.oldBatchNames.split(",");
		try {
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			user = (AdminUser) this.getSession(true).getAttribute("loginUser");
			if("".equals(substractModifiderName))
				substractModifiderName = null;
			ApplyDataService.delegatedBack(today, session, tx, oldBatchNameArr, "B", "06", null, user, substractModifiderName);			
			setResult("送回國壽設定完成");
			tx.commit();
		}catch (Exception e){
			// TODO Auto-generated catch block
			if(tx != null)
				tx.rollback();
			e.printStackTrace();
			setResult("例外發生:" + e.getMessage());
		}finally{
			if(session != null)
				session.close();
		}
		
		
		
		return null;
	}


	public String getVerifyResults() {
		List<String> verifyResult = null;
		Session session = null;
		String returnStr = "";
		try {
			session = HibernateSessionFactory.getSession();
			verifyResult = session.getNamedQuery("ApplyData.distinctResult")
					.list();

		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (session != null)
				session.close();
		}
		if (verifyResult != null && verifyResult.size() > 0) {
			for (String result : verifyResult) {
				returnStr += "' " + result + "',";
			}
			return "[" + returnStr + "]";
		} else {
			return "null";
		}
	}

	// rowClasses="odd_row,even_row"
	public String getRowClass() {
		String classes = "";

		for (int i = 0; i < dataModel.getRowCount(); i++) {
			dataModel.setRowIndex(i);
			ApplyData applyData = (ApplyData) dataModel.getRowData();
			if ((applyData.getExceptionStatus() != null && !"已接收"
					.equals(applyData.getExceptionStatus()))) {
				classes += "exception_row " + applyData.getOldBatchName() + ",";
			} else {
				if (i % 2 == 0)
					classes += "even_row " + applyData.getOldBatchName() + ",";
				else
					classes += "odd_row " + applyData.getOldBatchName() + ",";
			}
		}
		if (classes.length() > 0 && classes.endsWith(","))
			return classes.substring(0, classes.length() - 1);
		else
			return "";
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

	public List<ApplyData> updateApplyData() {
		Session session = null;
		Transaction tx = null;

		Date today = new Date();
		try {
			session = HibernateSessionFactory.getSession();
			List<AdminUser> users = session.getNamedQuery(
					"AdminUser.findAllUser").list();
			userMap = new HashMap<String, AdminUser>();
			for (AdminUser adminUser : users) {
				userMap.put(adminUser.getUserId(), adminUser);
			}

			tx = session.beginTransaction();
			// 找出尚未收到送金單的保單
			List<ApplyData> queryResult = session
					.createQuery(
							"from ApplyData where receipt = false and newBatchName is not null "
									+ "and newBatchName not like '%9999' and policyStatus > '35' and sourceCode <> 'GROUP' "
									+ "and packId is null and bankReceiptId is not null and (bkReceiptMatched = false or verifyResult like '%尚未接收到送金單%' ) "
									+ "and bankReceiptId <> '' and cycleDate >= '2015-01-12' order by uniqueNo")
					.list();
			List<ApplyData> receipts = session
					.createQuery(
							"from ApplyData where receipt = true "
									+ " and newBatchName is not null and sourceCode <> 'GROUP' and policyStatus > '35' "
									+ " and newBatchName not like '%9999' and packId is null "
									+ " and cycleDate >= '2015-01-12'").list();
			Query query = session
					.createQuery("from BankReceipt where bankReceiptId in (:bankReceiptIds)");
			if (queryResult != null && queryResult.size() > 0) {
				// 找出所有id
				List<String> bankReceiptIds = new ArrayList<String>();
				for (ApplyData applyData : queryResult) {
					if (applyData.getBankReceiptId() != null
							&& !"".equals(applyData.getBankReceiptId())) {
						if (applyData.getBankReceiptId() != null
								&& !"".equals(applyData.getBankReceiptId())) {
							Set<String> bankReceiptSet = applyData
									.getBankReceiptIdSet();
							bankReceiptIds.addAll(bankReceiptSet);
						}
					}
				}
				List<BankReceipt> bankReceipts = new ArrayList<BankReceipt>();
				if (bankReceiptIds.size() > 0) {
					bankReceipts = query.setParameterList("bankReceiptIds",
							bankReceiptIds).list();
					bankMap = new HashMap<String, BankReceipt>();
					for (BankReceipt bankReceipt : bankReceipts) {
						bankMap.put(bankReceipt.getBankReceiptId(), bankReceipt);
					}
				}				
				//System.out.println("size:" + bankReceipts.size());
				for (ApplyData applyData : queryResult) {
					
					//抽件時也要檢查送金單狀態，此段封存態
					if (false && applyData.getSubstract() != null
							&& applyData.getSubstract()) {
						// 非抽件時檢查相對的簽收單或保單是不是有錯，有錯時把自己也設成錯誤
						applyData.setVerifyResult("抽件");
						if(applyData.getPolicyStatus() == null
								   && !applyData.getPolicyStatus().equals("100")
								   && applyData.getPolicyStatus().compareTo("42") < 0)
						   applyData.setPolicyStatus("42");
						applyData.setExceptionStatus("41");
						session.update(applyData);
					} else {
						String policyNo = null;
						if (applyData.getPolicyNos() != null
								&& applyData.getPolicyNoSet().size() > 0) {
							for (String str : applyData.getPolicyNoSet()) {
								policyNo = str;
								break;
							}
						}
						if (applyData.getBankReceiptId() != null
								&& !"".equals(applyData.getBankReceiptId())) {
							BankReceipt br = null;
							ApplyData conjugatApplyData = null;
							if (receipts != null && receipts.size() > 0) {
								for (ApplyData receipt : receipts) {
									if (receipt.getCycleDate() != null
											&& receipt.getCycleDate().getTime() == applyData
													.getCycleDate().getTime()
											&& receipt.getApplyNo() != null
											&& receipt.getApplyNo().equals(
													applyData.getApplyNo())
											&& receipt.getPolicyNos() != null
											&& receipt.getPolicyNos().equals(
													applyData.getPolicyNos())
											&& receipt.getReprint() != null
											&& receipt.getReprint().intValue() == applyData
													.getReprint().intValue()) {
										conjugatApplyData = receipt;
										break;
									}
								}

							}
							boolean allMatch = true;
							for (String bankReceiptId : applyData.getBankReceiptIdSet()) {
								if (bankReceipts != null) {
									br = bankMap.get(bankReceiptId);
								}
								
								if (br == null || br.getReceiveDate() == null
										|| (br.getPackDate() != null 
									       && br.getOldBatchName() != null 
									       && !br.getOldBatchName().equals(applyData.getOldBatchName()))) {									
									allMatch = false;
									applyData.setPolicyStatus("41");
									applyData.setExceptionStatus("41");
									String verifyResult = applyData.getVerifyResult() == null? "" : applyData.getVerifyResult().trim();
									String tmp = "尚未接收到送金單" + bankReceiptId;
									if(br != null && br.getPackDate() != null ){									   
									   tmp = "送金單" + bankReceiptId + "於" + Constant.slashedyyyyMMdd.format(br.getPackDate()) + "連保單寄出" ;									   
									}
									if(verifyResult.indexOf(tmp) < 0){
									   if(!verifyResult.equals("") && !verifyResult.endsWith(","))
										    verifyResult += ",";
									   verifyResult += tmp;
									}
									applyData.setVerifyResult(verifyResult);
									applyData.setUpdateDate(today);
									applyData.setBankReceipts(null);
									session.update(applyData);
									if (conjugatApplyData != null) {
										conjugatApplyData.setPolicyStatus("41");
										conjugatApplyData
												.setExceptionStatus("41");
										conjugatApplyData.setVerifyResult(verifyResult);
										conjugatApplyData.setUpdateDate(today);
										session.update(conjugatApplyData);
									}
								}								
								if (br != null && br.getReceiveDate() == null && br.getPackDate() == null) {
									// 如果只是尚未接收，確認它沒有關聯到任何的ApplyData
									br.setOldBatchName(null);
									br.setApplyData(null);
									// applyData.setBankReceipts(null);
									session.update(br);
								} else if (br != null && br.getReceiveDate() != null && br.getPackDate() == null) {
									if (user == null)
										user = (AdminUser) this
												.getSession(true).getAttribute(
														"loginUser");
									if (applyData.getReceipt() != null
											&& !applyData.getReceipt()) {
										br.setApplyData(applyData);
										br.setOldBatchName(applyData
												.getOldBatchName());
										br.setMatchDate(today);
										br.setMatchUser((user.getUserName() == null || ""
												.equals(user.getUserName())) ? user
												.getUserId() : user
												.getUserName());
										br.setStatus("配表完成");
										session.update(br);
                                        
										// 改為一對多
										Set<BankReceipt> bset = new HashSet<BankReceipt>();
										if (applyData.getBankReceipts() != null) {
											bset = applyData.getBankReceipts();
										}
										bset.add(br);
										applyData.setBankReceipts(bset);
										
										boolean matched = false;
										//跑兩個迴圈，看看記錄中的bankReceiptId是不是全部都match到了
										for(String bId : applyData.getBankReceiptIdSet()){
										   bId = bId.trim();
										   matched = false;
										   for(BankReceipt abr : applyData.getBankReceipts()){
										       if(abr.getBankReceiptId().equals(bId)){
										    	   matched = true;
										    	   break;
										       }
										   }
										   //如果記錄的bankReceiptId沒有match到，就跳出去
										   if(!matched){
											   break;
										   }
										}
										if(matched){
										   // applyData.setBankReceipt(br);
										   // applyData.setVerifyResult(null);
										   // 先不更新，免得會查不出來
										   int policyStatus = 0;
										   try{
										      policyStatus = Integer.parseInt(applyData.getPolicyStatus());
										   }catch(Exception e){
											   logger.error("", e);
										   }
										   if(policyStatus < 42)
										     applyData.setPolicyStatus("42");
										   if (applyData.getVerifyResult() != null
												&& applyData.getVerifyResult().indexOf("尚未接收到送金單") >= 0 ){
											   String[] verifyResults =  applyData.getVerifyResult().split(",");
											   if(verifyResults.length > 1){
												   //如果有逗號分開的話，代表是多個驗單錯誤狀態
												   String verifyResult = "";
												   for(String vr : verifyResults){
													   if(vr.indexOf("尚未接收到送金單") < 0 && vr.indexOf("送金單已退回") < 0){
														   verifyResult += (vr + ",");
													   }
												   }
												   if(verifyResult.lastIndexOf(",") >= 0){
													   verifyResult = verifyResult.substring(0, verifyResult.length() - 1);
												   }
												   if("".equals(verifyResult)){													
													   applyData.setExceptionStatus(null);													   
												   }
											   }else{
												   //如果只有一個，表示之前驗單失敗是因為送金單未送達的關係，所以此時要把驗單失敗消除							   
												   applyData.setExceptionStatus(null);
											   }
										   }
										   applyData.setUpdateDate(today);
										   if (conjugatApplyData != null) {
											   if(conjugatApplyData.getPolicyStatus() != null
													   && !conjugatApplyData.getPolicyStatus().equals("100")
													   && !conjugatApplyData.getPolicyStatus().equals("98")
													   && !conjugatApplyData.getPolicyStatus().equals("97")
													   && conjugatApplyData.getPolicyStatus().compareTo("42") < 0)
											   conjugatApplyData.setPolicyStatus("42");
											   conjugatApplyData.setUpdateDate(today);
											   
											   conjugatApplyData.setExceptionStatus(applyData.getExceptionStatus());
											   if(applyData.getSubstract() != null && applyData.getSubstract()){
											      conjugatApplyData.setVerifyResult("保單抽件");
											      conjugatApplyData.setExceptionStatus("41");;
											   }else{
											      conjugatApplyData.setVerifyResult(applyData.getVerifyResult());
											   }
											   
										      session.update(conjugatApplyData);
										   }										   
									    }
										session.update(applyData);
									}
								}
							}
							applyData.setBkReceiptMatched(allMatch);
							session.update(applyData);
						}
					}
				}
			}
			if (tx != null)
				tx.commit();
			return queryResult;
		} catch (Exception e) {
			logger.error("", e);
			if (tx != null)
				tx.rollback();
			return null;
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}
	}

	public SortableModel getDataModel() {
		List<ApplyData> applyDatas = updateApplyData();
		List<ApplyData> displays = new ArrayList<ApplyData>();
		for (ApplyData applyData : applyDatas) {
			Set<String> brSet = applyData.getBankReceiptIdSet();
			for (String bankReceiptId : brSet) {
				BankReceipt br = bankMap.get(bankReceiptId);
				ApplyData newAd = new ApplyData();
				BeanUtils.copyProperties(applyData, newAd);
				newAd.setBankReceiptId(bankReceiptId);
				if (br == null) {
					newAd.setCenter("");
					newAd.setExceptionStatus("國壽人員尚未發送");
				} else {
					String name = "";
					if (br.getIssueUser() != null) {
						AdminUser admUser = userMap.get(br.getIssueUser());
						if (admUser != null)
							name = admUser.getUserName();
					}
					if (name != null && !"".equals(name))
						newAd.setCenter(name);
					if (br.getIssueDate() != null
							&& br.getReceiveDate() == null) {
						newAd.setExceptionStatus(sdf.format(br
								.getIssueDate()) + "已發送，FXDMS未接收");
						if (br.getStatus() != null
								&& br.getStatus().indexOf("退回國壽") >= 0) {
							newAd.setExceptionStatus(br.getStatus());
						}
					} else if (br.getIssueDate() == null
							&& br.getReceiveDate() == null) {
						newAd.setExceptionStatus("未接收");
						if (br.getStatus() != null
								&& br.getStatus().indexOf("退回國壽") >= 0) {
							newAd.setExceptionStatus(br.getStatus());
						}
					} else {
						newAd.setExceptionStatus("已接收");
					}
				}
				displays.add(newAd);
			}
		}
		setDataModel(new SortableModel(new ListDataModel(displays)));
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

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getVerifyResult() {
		return verifyResult;
	}

	public void setVerifyResult(String verifyResult) {
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

	public boolean isSetByAnother() {
		return setByAnother;
	}

	public void setSetByAnother(boolean setByAnother) {
		this.setByAnother = setByAnother;
	}

	public String getOldBatchNames() {
		return oldBatchNames;
	}

	public void setOldBatchNames(String oldBatchNames) {
		this.oldBatchNames = oldBatchNames;
	}

	public String getSubstractModifiderName() {
		return substractModifiderName;
	}

	public void setSubstractModifiderName(String substractModifiderName) {
		this.substractModifiderName = substractModifiderName;
	}
	

}
