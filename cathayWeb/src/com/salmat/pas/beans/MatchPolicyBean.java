package com.salmat.pas.beans;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;
import javax.persistence.NamedQuery;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;

import com.salmat.pas.conf.Constant;
import com.salmat.pas.report.GenerateReport;
import com.salmat.pas.vo.AcctItemFx;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.AfpFileDisplay;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.BankReceipt;
import com.salmat.pas.vo.JobBag;
import com.salmat.pas.vo.LogisticStatus;
import com.salmat.pas.vo.PackStatus;
import com.salmat.util.HibernateSessionFactory;

public class MatchPolicyBean extends BaseBean {
	private String newBatchName;
	private String uniqueNo;
	private String policyNos; ///簽收回條
	private boolean success = true;
	private String verifyResult;
	private UIData dataTable;
	private SortableModel dataModel;
	private String groupPolicyNo; 
	private UIData dataTable1;
	private SortableModel dataModel1;
	private UIData brDataTable;
	private SortableModel brDataModel;
	private boolean setByAnother = false;
	private String result;
	private Date cycleDate;
	private String center;
	private String jobBagNo;
	private String lastJobBagNo;
	private String groupObjStr = "{}"; //用來看是不是有保險證
	private String policyNoObjStr = "{}"; //對應的保單號碼
	private String cardPagesStr = "{}";
	private String matchMapStr = "{}";
	private Integer cardPages;
	boolean group = false;	
	private String returnEnv; //回郵信封
	private String bankReceiptId; //送金單
	private String bankReceiptId2; //送金單2
	private String bankReceiptId3; //送金單3
	private String bankReceiptId4; //送金單4
	private String bankReceiptId5; //送金單5
	private String bankReceiptId6; //送金單6
	private String bankReceiptId7; //送金單7
	private String bankReceiptId8; //送金單8
	private String bankReceiptId9; //送金單9	
	private Boolean cd;
	private Boolean humidProof;
	private String alertStr;
	private boolean haveBankReceipt;
	private JobBag jobBag;
	//團險map
	private HashMap<String, Boolean> withCardMap = null;			
	private HashMap<String, String> policyNoCardMap = null;
	private HashMap<String, String> cardPagesMap = null;
	
	
	//個險
	private Map<String, Map<String, String>> matchMap = null;
	private Map<String, String> insideMap;
	
	private AfpFile afpFile;
	private List<ApplyData> insureCards = null;
	private List<ApplyData> receiptDatas = null;
	
	
	
	private List<ApplyData> queryResult = null;
	private List<AcctItemFx> acctItemFxs = null;
	private Map<String, Double> weightMap = null;
	private List<AfpFile> afpFiles;
	private List<String> newBatchNames = new ArrayList<String>();

	Logger logger = Logger.getLogger(MatchPolicyBean.class);
	

	/**
	 * 寫入資料庫前的資料驗證
	 * 
	 * @return 錯誤訊息
	 */
	public String validData() {
		return "";
	}
	
	public String printLabel(){
		Session session = null;
		
		Session session2 = null;		
		try{
			this.setResult("");
			this.setAlertStr("");
			session2 = HibernateSessionFactory.getHibernateTemplate2().getSessionFactory().openSession();
			session = HibernateSessionFactory.getSession();
			boolean noBankReceipt = false;
			if(!"noBankReceiptPolicy".equals(jobBagNo)){
			    JobBag jobBag = (JobBag) session2.get(JobBag.class, jobBagNo);			
			    if(jobBag == null){
				   this.setAlertStr("查無資料，無法列印");
				   this.setResult("查無資料，無法列印");
				   return null;
			   }
			    String newBatchName = jobBag.getAfpName().toUpperCase().trim();
				if(newBatchName.endsWith(".AFP"))
					newBatchName = newBatchName.substring(0, newBatchName.length() - 4);			
				if(newBatchName.startsWith("SG") || newBatchName.startsWith("GG") || newBatchName.startsWith("PD")){
					this.setAlertStr("非保單工單，請輸入國泰保單工單號碼");
					this.setResult("非保單工單，請輸入國泰保單工單號碼");
					return null;
				}
				this.newBatchName = newBatchName;
				String query = "from ApplyData where newBatchName = ? order by newBatchName, newSerialNo";
				queryResult = session.createQuery(query).setString(0, newBatchName).list();
			}else{
				noBankReceipt = true;
			    Criteria criteria = session.createCriteria(ApplyData.class, "applyData")
                    .createAlias("applyData.bankReceipts", "bankReceipts", Criteria.INNER_JOIN);
			    criteria.add(Restrictions.isNull("packId"));
			    criteria.add(Restrictions.or(Restrictions.isNull("applyData.bkReceiptMatched"), Restrictions.eq("applyData.bkReceiptMatched", true)));
			    
			    criteria.add(Restrictions.gt("applyData.cycleDate", Constant.yyyyMMdd.parse("20150112")));  //這天是正式上線日				
			    criteria.addOrder(Order.asc("policyStatus")).addOrder(Order.asc("cycleDate")).addOrder(Order.asc("newSerialNo"));			
			    criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			    queryResult = criteria.list();			    
			}
			
			if(queryResult == null || queryResult.size() == 0){			
				this.setAlertStr(newBatchName + "列印檔查無資料");
				this.setResult(newBatchName + "列印檔查無資料");				
			}else{
                 String targetName = GenerateReport.generateMatchLabels(queryResult, session, noBankReceipt);
				
				this.getRequest().setAttribute("reportNameForDownload", targetName);
				return "download";
			}
		}catch(Exception e){
			logger.error("", e);
			
		}finally{
			if(session != null && session.isOpen())
				session.close();
			if(session2 != null && session2.isOpen())
				session2.close();
		}		
		
		return null;
	}
	
	public boolean isGroup(){		
		return group;
	}

	/**
	 * 取得與設定頁面資料，包含Data model與url map
	 * 
	 * @param tmpPid
	 */
	@SuppressWarnings("unchecked")
	public boolean retrieveData() {
				
		Session session = null;
		Transaction tx = null;
		Session session2 = null;		
		try{
			session2 = HibernateSessionFactory.getHibernateTemplate2().getSessionFactory().openSession();
			session = HibernateSessionFactory.getSession();				
			//如果工單號碼換了，以下才需要改
			if(this.lastJobBagNo == null || this.lastJobBagNo.trim().equals("") || !this.lastJobBagNo.equals(jobBagNo)){
				queryResult = null;
		 		setDataModel1(null);
		 		setDataModel(null);
				afpFile = null;
				insureCards = null;
				receiptDatas = null;
				
				withCardMap = new HashMap<String, Boolean>();
				policyNoCardMap = new HashMap<String, String>();
				cardPagesMap = new HashMap<String, String>();
				matchMap = new HashMap<String, Map<String, String>>();
				
				
				
			    this.lastJobBagNo = jobBagNo;
			    jobBag = (JobBag) session2.get(JobBag.class, jobBagNo);			
			
			    if(jobBag == null){
				    return false;
			    }
			    String newBatchName = jobBag.getAfpName().toUpperCase().trim();
			    if(newBatchName.endsWith(".AFP"))
				    newBatchName = newBatchName.substring(0, newBatchName.length() - 4);
			
			    if(newBatchName.startsWith("SG") || newBatchName.startsWith("GG") || newBatchName.startsWith("PD")){
				    this.setAlertStr("非保單工單，請輸入國泰保單工單號碼");
				    this.setResult("非保單工單，請輸入國泰保單工單號碼");
				    return false;
			    }
			    if(!newBatchName.substring(2, 4).equals("06") && !newBatchName.substring(2, 4).equals("09") ){
				    this.setAlertStr("非北二工單，不需配表");
				    this.setResult("非北二工單，不需配表");
				    return false;
			    }
			    this.newBatchName = newBatchName;
			    if(newBatchName.startsWith("GA"))
				    group = true;
			    else
			        group = false;
			
			    tx = session.beginTransaction();
			    afpFile = (AfpFile) session.get(AfpFile.class, newBatchName);
			    if(afpFile == null){
				    this.setAlertStr(newBatchName + "列印檔查無資料");
				    this.setResult(newBatchName + "列印檔查無資料");
				    return false;
			    }
			    this.cycleDate = afpFile.getCycleDate();
			    String aStatus = afpFile.getStatus();
			    if(aStatus == null || (!"驗單完成".equals(aStatus) 
				 	    && aStatus.indexOf("配表") < 0 
					    && aStatus.indexOf("裝箱") < 0 
					    && aStatus.indexOf("貨運") < 0 
					    && aStatus.indexOf("交寄") < 0  )){
				    this.setAlertStr("必須為驗單完成或配表中的列印檔才能進行配表");
				    this.setResult("必須為驗單完成或配表中的列印檔才能進行配表");
				    return false;
			    }
			/*
			Criteria criteria = session.createCriteria(ApplyData.class, "applyData")
                    .createAlias("applyData.bankReceipts", "bankReceipts", Criteria.LEFT_JOIN);
			criteria.add(Restrictions.eq("newBatchName", newBatchName));
			criteria.addOrder(Order.asc("policyStatus")).addOrder(Order.asc("newSerialNo"));
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			*/		
				Query query = session
						.getNamedQuery("ApplyData.findByNewBatchNameOrderByStatus");
				
				queryResult = query.setString(0, newBatchName).list();
				// queryResult = criteria.list();
				this.haveBankReceipt = false;
				List<String> oldBatchNames = new ArrayList<String>();
				if (queryResult != null && queryResult.size() > 0) {
					String receiptsAfpNm = null;
					String cardsNm = null;
					List<String> queryNms = new ArrayList<String>();
					queryNms.add(newBatchName);
					if (newBatchName.startsWith("GA")) {						
						cardsNm = newBatchName.replaceFirst("GA", "PD");
						receiptsAfpNm = newBatchName.replaceFirst("GA", "GG");
						queryNms.add(cardsNm);
						queryNms.add(receiptsAfpNm);
						insureCards = query.setString(0, cardsNm).list();
						receiptDatas = query.setString(0, receiptsAfpNm).list();
					} else {
						receiptsAfpNm = newBatchName.replaceFirst("CA", "SG");
						queryNms.add(receiptsAfpNm);
						receiptDatas = query.setString(0, receiptsAfpNm).list();
					}
					if(afpFile.getStatus().indexOf("配表") < 0 && afpFile.getStatus().indexOf("裝箱") < 0
						     && afpFile.getStatus().indexOf("貨運") < 0
						     && afpFile.getStatus().indexOf("交寄") < 0
						     ){
					    afpFile.setStatus("配表中");
					    afpFile.setNewBatchName(newBatchName);
					    session.createQuery("update AfpFile set status = '配表中' where newBatchName in (:newBatchNames) and cycleDate = :cycleDate")
					        .setParameterList("newBatchNames", queryNms).setParameter("cycleDate", cycleDate).executeUpdate();
					    String updateStr = "update ApplyData set policyStatus = '50' where newBatchName in (:newBatchNames) and cycleDate = :cycleDate "
					    		+ "and policyStatus <> '100' and policyStatus < 50 "
					    		+ "and (bkReceiptMatched is null or bkReceiptMatched = true or substract = true)";
					    session.createQuery(updateStr).setParameterList("newBatchNames", queryNms)
					       .setParameter("cycleDate", cycleDate).executeUpdate();
					    
					}
					Map<String, ApplyData> receiptDataMap = new HashMap<String, ApplyData>();
					Map<String, ApplyData> insureCardMap = new HashMap<String, ApplyData>();
					for (ApplyData receipt : receiptDatas) {
						if (!newBatchName.startsWith("GA")) {
							receiptDataMap.put(receipt.getOldBatchName()
									.replaceAll("簽收回條", "保單"), receipt);
						} else {
							String fileNm4Search = receipt.getOldBatchName()
									.replaceAll("_sign_", "_policy_");
							fileNm4Search = fileNm4Search.replaceAll("_SIGN_",
									"_POLICY_");
							fileNm4Search = fileNm4Search.replaceAll("_si_",
									"_pl_");
							fileNm4Search = fileNm4Search.replaceAll("_SI_",
									"_PL_");

							receiptDataMap.put(fileNm4Search, receipt);
						}
					}
					if (insureCards != null) {
						for (ApplyData insureCard : insureCards) {
							insureCardMap.put(insureCard.getPolicyNos(),
									insureCard);

						}
					}
					cycleDate = queryResult.get(0).getCycleDate();
					center = queryResult.get(0).getCenter();
					boolean allFinish = true;
					
					//查出所有的afpFile
					String queryStr = "from AfpFile where cycleDate = ? and center = ? and (newBatchName like 'CA%' or newBatchName like 'SG%') and newBatchName not like '%9999'";
					if (group)
						queryStr = "from AfpFile where cycleDate = ? and center = ? and (newBatchName like 'GA%' or newBatchName like 'GG%' or newBatchName like 'PD%')";				
					afpFiles = session.createQuery(queryStr)
							.setDate(0, cycleDate).setString(1, center).list();
					for (AfpFile afp : afpFiles) {
						newBatchNames.add(afp.getNewBatchName());
					}

					Criteria criteria = session.createCriteria(
							BankReceipt.class, "bankReceipt").createAlias(
							"bankReceipt.applyData", "applyData",
							Criteria.INNER_JOIN);
					criteria.add(Restrictions.in("applyData", queryResult));
					criteria.addOrder(Order.asc("applyData.cycleDate"))
							.addOrder(Order.asc("applyData.uniqueNo"));
					// criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
					List<BankReceipt> bankReceipts = criteria.list();
					// setBrDataModel(new SortableModel(new
					// ListDataModel(bankReceipts)));

					for (ApplyData applyData : queryResult) {
						if (applyData.getUniqueNo() != null) {
							oldBatchNames.add(applyData.getOldBatchName());
							withCardMap.put(applyData.getUniqueNo(), applyData
									.getHaveInsureCard() == null ? false
									: applyData.getHaveInsureCard());
							if (applyData.getPolicyNoSet().size() > 0)
								policyNoCardMap.put(applyData.getUniqueNo(),
										applyData.getPolicyNos().split(",")[1]);
							if (insureCards != null
									&& withCardMap.get(applyData.getUniqueNo())) {
								for (ApplyData card : insureCards) {
									if (card.getPolicyNos() != null
											&& card.getPolicyNos().equals(
													applyData.getPolicyNos())) {
										cardPagesMap.put(
												applyData.getUniqueNo(),
												card.getTotalPage() + "");
										break;
									}
								}
							}
						}

						Integer status = new Integer(
								applyData.getPolicyStatus() == null ? "0"
										: applyData.getPolicyStatus());
						// 只有驗單完成的才設定成配表中
						if (status == 42
								|| (applyData.getGroupInsure() != null && applyData
										.getGroupInsure()))
							applyData.setPolicyStatus("50");

						Integer statusStr = new Integer(
								applyData.getPolicyStatus());
						// 如果還有在配表中，就不是全部完成
						if (statusStr == 50) {
							allFinish = false;
						}
						// 找出簽收單

						String policyNo = null;
						for (String str : applyData.getPolicyNoSet()) {
							policyNo = str;
							break;
						}
						// recPolicyNo: 簽收單保單號碼
						// insideMap內有policyNo : 簽收單保單號碼
						// returnEnv：回郵信封
						// bankReceiptId：送金單號碼
						// cd:附光碟
						insideMap = new HashMap<String, String>();
						insideMap.put("recPolicyNo", "");
						if (applyData.getChannelID() != null) {
							// &&
							// (applyData.getChannelID().toUpperCase().equals("B")
							// || applyData.getSourceCode().equals("REPT") ||
							// applyData.getSourceCode().equals("CONV"))){
							if (applyData.getAreaId().toUpperCase()
									.startsWith("TQ")
									|| applyData.getAreaId().toUpperCase()
											.startsWith("TX")) {
								insideMap.put("returnEnv", "北");
							} else if (applyData.getAreaId().toUpperCase()
									.startsWith("TC")) {
								insideMap.put("returnEnv", "中");
							} else if (applyData.getAreaId().toUpperCase()
									.startsWith("TN")) {
								insideMap.put("returnEnv", "高");
							} else {
								insideMap.put("returnEnv", "");
							}
						} else {
							insideMap.put("returnEnv", "");
						}
						insideMap.put("bankReceiptId", "");
						insideMap.put("bankReceiptId2", "");
						insideMap.put("bankReceiptId3", "");
						insideMap.put("bankReceiptId4", "");
						insideMap.put("bankReceiptId5", "");
						insideMap.put("bankReceiptId6", "");
						insideMap.put("bankReceiptId7", "");
						insideMap.put("bankReceiptId8", "");
						insideMap.put("bankReceiptId9", "");
						if (applyData.getHavaBkReceipt() != null
								&& applyData.getHavaBkReceipt()) {
							int counter = 0;
							// 跑兩重迴圈，檢查是不是所有的簽收單都已經收到
							for (String bankReceiptId : applyData
									.getBankReceiptIdSet()) {
								counter++;
								boolean match = false;
								for (BankReceipt bankReceipt : bankReceipts) {
									if (bankReceipt.getBankReceiptId().equals(
											bankReceiptId)) {
										match = true;
										break;
									}
								}
								if (match) {
									// 有收到時，在inside map添加送金單號碼
									this.haveBankReceipt = true;
									if (counter == 1) {
										insideMap.put("bankReceiptId",
												bankReceiptId);
									} else {
										insideMap.put(
												"bankReceiptId" + counter,
												bankReceiptId);
									}
								} else {
									if (counter == 1) {
										insideMap.put("bankReceiptId", "送金單"
												+ bankReceiptId + "未收到");
									} else {
										insideMap.put(
												"bankReceiptId" + counter,
												"送金單" + bankReceiptId + "未收到");
									}
								}
							}
							/*
							 * if(match1){ insideMap.put("bankReceiptId",
							 * applyData.getBankReceiptId());
							 * //bankReceipts.add(applyData.getBankReceipt());
							 * this.haveBankReceipt = true; }else{
							 * insideMap.put("bankReceiptId", "送金單" +
							 * applyData.getBankReceiptId() + "未收到"); }
							 */
						}
						if (applyData.getCd() != null && applyData.getCd())
							insideMap.put("cd", "true");
						else
							insideMap.put("cd", "");
						if (applyData.getSourceCode().equals("NORM")) {
							insideMap.put("humidProof", "true");
						} else {
							insideMap.put("humidProof", "");
						}
						if (receiptDatas != null) {
							ApplyData receiptData = receiptDataMap
									.get(applyData.getOldBatchName());
							if (receiptData != null) {
								oldBatchNames.add(receiptData.getOldBatchName());
								Integer recStatus = receiptData
										.getPolicyStatus() == null ? 0
										: new Integer(
												receiptData.getPolicyStatus());
								String recNo = null;
								for (String recPolicyNo : receiptData
										.getPolicyNoSet()) {
									if (recPolicyNo != null
											&& (recNo == null || recNo
													.compareTo(recPolicyNo) > 0)) {
										recNo = recPolicyNo;
									}
								}
								if (receiptData.getTotalPage() != null)
									insideMap.put("recApplyPages",
											receiptData.getTotalPage() + "");
								else
									insideMap.put("recApplyPages", "");
								insideMap.put("recPolicyNo", recNo);
								// 如果簽收單是在驗單完成，就更新
								if (recStatus <= 42 && recStatus != 41) {
									receiptData.setPolicyStatus("50");
								}
								applyData.setReceiptData(receiptData);
							}
						} else {
							insideMap.put("recApplyPages", "");
						}

						if (insureCards != null) {
							ApplyData insureCard = insureCardMap.get(applyData
									.getPolicyNos());
							if (insureCard != null) {
								oldBatchNames.add(insureCard.getOldBatchName());
								Integer recStatus = insureCard
										.getPolicyStatus() == null ? 0
										: new Integer(
												insureCard.getPolicyStatus());
								// 如果簽收單是在驗單完成，就更新
								if (recStatus <= 42 && recStatus != 41) {
									insureCard.setPolicyStatus("50");
									// session.update(insureCard);
								}
								applyData.setInsureCardData(insureCard);
							}
						}
						// session.update(applyData);
						if (applyData.getUniqueNo() != null)
							matchMap.put(applyData.getUniqueNo(), insideMap);
					}
					if (oldBatchNames.size() > 0)
						session.createQuery(
								"update ApplyData set policyStatus = '50', updateDate = :updateDate where oldBatchName in (:oldBatchNames) "
										+ "and policyStatus < '50' and policyStatus <> '41' and policyStatus <> '100' and policyStatus <> '97'and policyStatus <> '98' and newBatchName not like '%9999'")
								.setParameter("updateDate", new Date())
								.setParameterList("oldBatchNames",
										oldBatchNames).executeUpdate();

					if (matchMap.size() > 0) {
						JSONObject jo = JSONObject.fromObject(matchMap);
						matchMapStr = jo.toString();
					} else {
						matchMapStr = "{}";
					}
					if (withCardMap.size() > 0) {
						JSONObject jo = JSONObject.fromObject(withCardMap);
						groupObjStr = jo.toString();
					} else {
						groupObjStr = "{}";
					}

					if (policyNoCardMap.size() > 0) {
						JSONObject jo = JSONObject.fromObject(policyNoCardMap);
						policyNoObjStr = jo.toString();
					} else {
						policyNoObjStr = "{}";
					}

					if (cardPagesMap.size() > 0) {
						JSONObject jo = JSONObject.fromObject(cardPagesMap);
						cardPagesStr = jo.toString();
					} else {
						cardPagesStr = "{}";
					}
				}
		 		setDataModel1(new SortableModel(new ListDataModel(queryResult)));
				// 找出此轄區中此cycleDate所有的批次列印檔
                /*

				*/
				tx.commit();
			}
			
			List<AfpFileDisplay> displayResult = new ArrayList<AfpFileDisplay>(); // 真正要顯示的
			List<Object[]> listAll = new ArrayList<Object[]>();
			//@NamedQuery(name="ApplyData.findMatchedStatus", query="select a.policyStatus, count(a), a.newBatchName from ApplyData a where a.cycleDate = :cycleDate and a.newBatchName in (:newBatchNames) and a.newBatchName not like '%9999' group by a.newBatchName, a.policyStatus"),
			if (newBatchNames.size() > 0){                
				listAll = session.getNamedQuery("ApplyData.findMatchedStatus").setDate("cycleDate", this.cycleDate).setParameterList("newBatchNames", newBatchNames).list();                
			}
			Map<String, List<Object[]>> afpMap = new HashMap<String, List<Object[]>>();
			for (Object[] objArr : listAll) {
				List<Object[]> objList = afpMap.get((String) objArr[2]);
				if (objList == null) {
					objList = new ArrayList<Object[]>();
				}
				objList.add(objArr);
				afpMap.put((String) objArr[2], objList);
			}
			if (afpFiles != null && afpFiles.size() > 0) {
				int allNotMached = 0;
				for (AfpFile afp : afpFiles) {

					List<Object[]> list = afpMap.get(afp.getNewBatchName());
					if (list == null)
						list = new ArrayList<Object[]>();

					AfpFileDisplay afpDisplay = new AfpFileDisplay();
					// 用gooup by 查出各狀態的數量

					int matched = 0;
					int notMatched = 0;
					int errors = 0;
					int verifiedErrs = 0;
					int volumns = 0;
					for (Object[] result : list) {
						Integer policyStatus = new Integer(
								(String) result[0]);
						Integer counter = new Integer(result[1].toString());
						volumns += counter;
						if (policyStatus >= 55) {
							// 配表完成
							matched += counter;
						} else if (policyStatus < 55
								&& !(policyStatus == 11
										|| policyStatus == 13
										|| policyStatus == 14 || policyStatus == 16)
								&& policyStatus != 41) {
							// 未配表完成
							notMatched += counter;
							allNotMached += counter;
						} else if (policyStatus == 11 || policyStatus == 13
								|| policyStatus == 14 || policyStatus == 16) {
							// 異常
							errors += counter;
						} else if (policyStatus == 41) {
							// 驗單錯誤
							verifiedErrs += counter;
						}
					}
					if (notMatched == 0
							&& afp.getStatus().indexOf("裝箱") < 0
							&& afp.getStatus().indexOf("貨運") < 0
							&& afp.getStatus().indexOf("交寄") < 0) {
						afp.setStatus("配表完成");
						session.update(afp);
					} else if (notMatched > 0
							&& afp.getStatus().indexOf("裝箱") < 0
							&& afp.getStatus().indexOf("貨運") < 0
							&& afp.getStatus().indexOf("交寄") < 0
							&& afp.getStatus().indexOf("配表完成") < 0) {
						afp.setStatus("配表中");
						session.update(afp);
					}
					BeanUtils.copyProperties(afp, afpDisplay);
					if (allNotMached == 0) {
						this.setResult("全部保單均已配表完成，請繼續打包作業");
					}
					afpDisplay.setCenter(ApplyData.getCenterMap().get(
							afpDisplay.getCenter()));
					afpDisplay.setMatched(matched);
					afpDisplay.setNotMatched(notMatched);
					afpDisplay.setErrors(errors);
					afpDisplay.setVerifiedErrs(verifiedErrs);
					afpDisplay.setVolumns(volumns);

					displayResult.add(afpDisplay);
				}
			}
			setDataModel(new SortableModel(new ListDataModel(displayResult)));
			
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

	private String updateNoBrApplyData() throws Exception{
		
		Date today = new Date();
		Session session = null;
		Transaction tx = null;
		try{
			String updateStr = "";
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			//找出保單
			Criteria criteria = session.createCriteria(ApplyData.class, "applyData")
			     .createAlias("applyData.bankReceipts", "bankReceipts", Criteria.INNER_JOIN);
			criteria.add(Restrictions.eq("uniqueNo", uniqueNo));
			if(!"noBankReceiptPolicy".equals(this.jobBagNo))
			   criteria.add(Restrictions.eq("newBatchName", newBatchName));
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			List<ApplyData> list = criteria.list();
			ApplyData applyData = null;
			ApplyData receiptAppData = null;			
			List<String> oldBatchNames = new ArrayList<String>();
			if(list != null && list.size() > 0){
				applyData = list.get(0);
				//oldBatchNames.add(applyData.getOldBatchName());
				//找出對應的簽收單
				criteria = session.createCriteria(ApplyData.class);
				criteria.add(Restrictions.eq("applyNo", applyData.getApplyNo()));
				String policyNo = null;
				for(String str : applyData.getPolicyNoSet()){
					policyNo = str;
					break;
				}
				String sgOldName = null;
				if(applyData.getNewBatchName() != null && applyData.getNewBatchName().toUpperCase().startsWith("CA"))
					sgOldName = "SG" + applyData.getNewBatchName().substring(2);
				else if(applyData.getNewBatchName() != null && applyData.getNewBatchName().toUpperCase().startsWith("GA"))
					sgOldName = "GG" + applyData.getNewBatchName().substring(2);
				criteria.add(Restrictions.like("policyNos", "%," + policyNo + ",%"));
				criteria.add(Restrictions.eq("receipt", true));
				criteria.add(Restrictions.eq("cycleDate", applyData.getCycleDate()));
				criteria.add(Restrictions.eq("reprint", applyData.getReprint()));
				criteria.add(Restrictions.eq("newBatchName", sgOldName));
				criteria.add(Restrictions.eq("reprint", applyData.getReprint()));				

				list = criteria.list();
				if(list != null && list.size() > 0){
					receiptAppData = list.get(0);
					oldBatchNames.add(receiptAppData.getOldBatchName());
				}				
			}

			if (applyData.getPolicyStatus().equals("14")
					|| applyData.getPolicyStatus().equals("16")
					|| applyData.getPolicyStatus().equals("11")
					|| applyData.getPolicyStatus().equals("13")) {
				// 保單狀態異常
				updateStr = "此保單狀態為" + applyData.getPolicyStatusName()
						+ "，故無法配表更新狀態";
				this.setAlertStr(updateStr);
				this.setResult(updateStr);
				return updateStr;
			}
			if (receiptAppData != null
					&& (receiptAppData.getPolicyStatus().equals("14")
							|| receiptAppData.getPolicyStatus().equals("16")
							|| receiptAppData.getPolicyStatus().equals("11") || receiptAppData
							.getPolicyStatus().equals("13"))) {
				// 簽收單狀態異常
				updateStr = "此簽收單狀態為" + receiptAppData.getPolicyStatusName()
						+ "，故無法配表更新狀態";
				this.setAlertStr(updateStr);
				this.setResult(updateStr);
				return updateStr;
			}
			if (receiptAppData == null && policyNos != null
					&& !policyNos.equals("")) {
				// 有保單無簽收單時，輸入了簽收單號碼入
				updateStr = "此保單無簽收單，簽收單保單號碼不需輸入";
				this.setAlertStr(updateStr);
				this.setResult(updateStr);
				return updateStr;
			}

			if (receiptAppData != null && policyNos != null
					&& !policyNos.equals("")) {
				// 有保單有簽收單時，輸入了錯誤的簽收單保單號碼
				if (!receiptAppData.getPolicyNoSet().contains(policyNos)) {
					updateStr = "保單右上角號碼與簽收單保單號碼無法匹配";
					this.setAlertStr(updateStr);
					this.setResult(updateStr);
					return updateStr;
				}
			}
			if (receiptAppData != null
					&& (policyNos == null || policyNos.equals(""))) {
				// 有保單有簽收單時，沒輸入簽收單保單號碼
				updateStr = "請輸入簽收單保單號碼";
				this.setAlertStr(updateStr);
				this.setResult(updateStr);
				return updateStr;
			}
			applyData.setUpdateDate(today);
			if(!"100".equals(applyData.getPolicyStatus()) 
					&& "55".compareTo(applyData.getPolicyStatus()) > 0 )
			   applyData.setPolicyStatus("55");
			double weight = applyData.getWeight() == null? 0 : applyData.getWeight();
			String packId = applyData.getPackId();
			
			applyData = this.setWeight(applyData);
			session.update(applyData);
			


			String updateQuery = "update ApplyData set policyStatus = '55', updateDate = :updateDate, weight = 0  "
					+ "where oldBatchName in(:oldBatchNames) and policyStatus < '55' and policyStatus <> '100'";
			if(oldBatchNames.size() > 0)
			   session.createQuery(updateQuery).setParameter("updateDate", today).setParameterList("oldBatchNames", oldBatchNames).executeUpdate();
			//如果本來沒有沒有重量，而且已經有打包代號的話，代表已未配表前就已經裝箱完成，此時需要重量加回去
			if(packId != null && !packId.trim().equals("") && weight == 0){
				weight = applyData.getWeight() == null? 0 : applyData.getWeight();				
				PackStatus packStatus = (PackStatus) session.get(PackStatus.class, packId);
				if(packStatus != null){
					packStatus.setWeight((packStatus.getWeight() == null? 0 : packStatus.getWeight()) + weight);
					session.update(packStatus);
					LogisticStatus ls = packStatus.getLogisticStatus();
					if(ls != null){
					   ls.setWeight((ls.getWeight() == null? 0 : ls.getWeight()) + weight);
					   session.update(ls);
					}
				}
			}
			
			
			tx.commit();			
			this.setResult("配表完成，請繼續");
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
	 * 
	 * @return
	 */
	public String doSubmit() {
		Session session2 = null;
		if(this.acctItemFxs == null){
		   try{
		      session2 = HibernateSessionFactory.getHibernateTemplate2().getSessionFactory().openSession();
		      acctItemFxs = session2.getNamedQuery("AcctItemFx.findCathayMaterial").list();
		      if(acctItemFxs != null){
		    	  weightMap = new HashMap<String, Double>();
		    	  for(AcctItemFx ai : acctItemFxs){
		    		  weightMap.put(ai.getName(), ai.getWeight() == null? 0 : ai.getWeight());
		    	  }
		    	  Constant.setWeightMap(weightMap);
		      }
		   }catch(Exception e){
			  logger.error("", e);
		   }finally{
			  if(session2 != null)
				  session2.close();
		   }
		}
		setResult("");
		this.setAlertStr("");
		String errMsg = validData();
		if (errMsg.equals("")) {
			String updateStr = null;
			
			if( !"noBankReceiptPolicy".equals(this.jobBagNo) && (this.lastJobBagNo == null || this.lastJobBagNo.trim().equals("") 
					|| !this.jobBagNo.equals(this.lastJobBagNo)) ){
				this.uniqueNo = "";
				this.policyNos = "";
				this.groupPolicyNo = "";
				this.cardPages = null;
				this.bankReceiptId = null;
				this.bankReceiptId2 = null;
				this.bankReceiptId3 = null;
				this.returnEnv = null;
				this.cd = null;
				this.humidProof = null;
				this.cycleDate = null;
				this.center = null;
			}else if("noBankReceiptPolicy".equals(this.jobBagNo)){
			    this.lastJobBagNo = this.jobBagNo;
			    this.center = null;
			    this.cycleDate = null;
			    this.newBatchName = null;			    
			}
			if(uniqueNo != null && !uniqueNo.trim().equals("") ){
				try {
					if(!"noBankReceiptPolicy".equals(this.jobBagNo)){					   
					   updateStr = updateApplyData();
					}else{					   
					   updateStr = updateNoBrApplyData();
					}							
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					this.setAlertStr("Exception Happened:" + e.getMessage());
					setResult("Exception Happened:" + e.getMessage());
					return null;
				}
			}
			resetDataScroller(null);
			boolean success = false;
			if(!"noBankReceiptPolicy".equals(this.jobBagNo)){				
				success = retrieveData();	
			}else{				
				success = retrieveNoBankReceiptData();
			}
			if(success && updateStr == null ){
			   setResult("查詢成功");
			}else if(updateStr == null && !"noBankReceiptPolicy".equals(this.jobBagNo)){
				 this.setDataModel(null);
			     this.setDataModel1(null);
				 if(getResult() == null || "".equals(getResult())){
					this.setAlertStr("查詢失敗，請確定此工單是否為國泰保單工單");
				    setResult("查詢失敗，請確定此工單是否為國泰保單工單");
				 }
			}else if(updateStr == null && "noBankReceiptPolicy".equals(this.jobBagNo)){
				this.setDataModel(null);
			    this.setDataModel1(null);
				this.setAlertStr("無可供配表的保單");
			    setResult("無可供配表的保單");
			}
			return null;
		} else {
			// 錯誤訊息寫入sbForm:errMsg
			setResult(errMsg);
			return null;
		}
	}

	public String noBankReceipt(){
		return doSubmit();
	}
	private String updateApplyData() throws Exception{
		Date today = new Date();
		Session session = null;		
		Transaction tx = null;

		try{
			String updateStr = "";
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			//找出保單
			Query query = session.getNamedQuery("ApplyData.findByUniNoAndNewBatchName").setString(0, uniqueNo).setString(1, newBatchName);			
			List<ApplyData> list = query.list();
			ApplyData applyData = null;
			ApplyData receiptAppData = null;			
			ApplyData cardAppData = null;
			List<String> oldBatchNames = new ArrayList<String>();
			if(list != null && list.size() > 0){
				applyData = list.get(0);
				//oldBatchNames.add(applyData.getOldBatchName());
				//找出對應的簽收單
				if(receiptDatas != null && receiptDatas.size() > 0){
					for(ApplyData ad : receiptDatas){
						if(ad.getReceipt() != null && ad.getReceipt() && ad.getPolicyNos().equals(applyData.getPolicyNos())){
							receiptAppData = ad;
							oldBatchNames.add(receiptAppData.getOldBatchName());
							break;
						}
					}
					
				}				
			}
			//找出對應的保險證
			if(applyData != null && applyData.getHaveInsureCard() != null && applyData.getHaveInsureCard()){
				if(insureCards != null && insureCards.size() > 0){
					for(ApplyData ad : insureCards){
						if(ad.getPolicyNos().equals(applyData.getPolicyNos())){
							cardAppData = ad;
							oldBatchNames.add(cardAppData.getOldBatchName());
							break;
						}
					}
			    }
			}
			if (applyData.getPolicyStatus().equals("14")
					|| applyData.getPolicyStatus().equals("16")
					|| applyData.getPolicyStatus().equals("11")
					|| applyData.getPolicyStatus().equals("13")) {
				// 保單狀態異常
				updateStr = "此保單狀態為" + applyData.getPolicyStatusName()
						+ "，故無法配表更新狀態";
				this.setAlertStr(updateStr);
				this.setResult(updateStr);
				return updateStr;
			}
			if (receiptAppData != null
					&& (receiptAppData.getPolicyStatus().equals("14")
							|| receiptAppData.getPolicyStatus().equals("16")
							|| receiptAppData.getPolicyStatus().equals("11") || receiptAppData
							.getPolicyStatus().equals("13"))) {
				// 簽收單狀態異常
				updateStr = "此簽收單狀態為" + receiptAppData.getPolicyStatusName()
						+ "，故無法配表更新狀態";
				this.setAlertStr(updateStr);
				this.setResult(updateStr);
				return updateStr;
			}
			if (receiptAppData == null && policyNos != null
					&& !policyNos.equals("")) {
				// 有保單無簽收單時，輸入了簽收單號碼入
				updateStr = "此保單無簽收單，簽收單保單號碼不需輸入";
				this.setAlertStr(updateStr);
				this.setResult(updateStr);
				return updateStr;
			}

			if (receiptAppData != null && policyNos != null
					&& !policyNos.equals("")) {
				// 有保單有簽收單時，輸入了錯誤的簽收單保單號碼
				if (!receiptAppData.getPolicyNoSet().contains(policyNos)) {
					updateStr = "保單右上角號碼與簽收單保單號碼無法匹配";
					this.setAlertStr(updateStr);
					this.setResult(updateStr);
					return updateStr;
				}
			}
			if (receiptAppData != null
					&& (policyNos == null || policyNos.equals(""))) {
				// 有保單有簽收單時，沒輸入簽收單保單號碼
				updateStr = "請輸入簽收單保單號碼";
				this.setAlertStr(updateStr);
				this.setResult(updateStr);
				return updateStr;
			}
			if (applyData.getHaveInsureCard() != null
					&& applyData.getHaveInsureCard()) {
				if (cardAppData == null) {
					updateStr = "輸入的保險證號碼無法匹配保單";
					this.setAlertStr(updateStr);
					this.setResult(updateStr);
					return updateStr;
				} else if (cardPages != null
						&& cardAppData.getTotalPage().intValue() != cardPages
								.intValue()) {
					updateStr = "保險證張數不符，應為" + cardAppData.getTotalPage();
					this.setAlertStr(updateStr);
					this.setResult(updateStr);
					return updateStr;
				}else if(cardPages == null){
					updateStr = "請輸入保險證張數";
					this.setAlertStr(updateStr);
					this.setResult(updateStr);
					return updateStr;
				}
			}
			applyData.setUpdateDate(today);
			if(!"100".equals(applyData.getPolicyStatus()) 
					&& "55".compareTo(applyData.getPolicyStatus()) > 0 
					&& (!"41".equals(applyData.getExceptionStatus()) 
					|| ("41".equals(applyData.getExceptionStatus())  
					     &&  applyData.getVerifyResult().indexOf("尚未接收到送金單") < 0)))
			      applyData.setPolicyStatus("55");
			String packId = applyData.getPackId();
			double weight = applyData.getWeight() == null? 0 : applyData.getWeight();
			applyData = this.setWeight(applyData);
			session.update(applyData);

			String updateQuery = "update ApplyData set policyStatus = '55', updateDate = :updateDate, weight = 0  where oldBatchName in(:oldBatchNames) and policyStatus < '55' "
				  	   + " and policyStatus <> '100' ";
			if(oldBatchNames.size() > 0)
			    session.createQuery(updateQuery).setParameter("updateDate", today).setParameterList("oldBatchNames", oldBatchNames).executeUpdate();
			
			
			//如果本來沒有沒有重量，而且已經有打包代號的話，代表已未配表前就已經裝箱完成，此時需要重量加回去
			if(packId != null && !packId.trim().equals("") && weight == 0){				
				weight = applyData.getWeight() == null? 0 : applyData.getWeight();				
				PackStatus packStatus = (PackStatus) session.get(PackStatus.class, packId);
				if(packStatus != null){
					packStatus.setWeight((packStatus.getWeight() == null? 0 : packStatus.getWeight()) + weight);
					session.update(packStatus);
					LogisticStatus ls = packStatus.getLogisticStatus();
					if(ls != null){
					   ls.setWeight((ls.getWeight() == null? 0 : ls.getWeight()) + weight);
					   session.update(ls);
					}
				}
			}
			
			/*
			List<String> newBatchNames = new ArrayList();
			if(applyData != null){
				applyData.setUpdateDate(today);
				applyData.setPolicyStatus("55");
				session.update(applyData);
				newBatchNames.add(applyData.getNewBatchName());
			}
			if(receiptAppData != null){
				receiptAppData.setUpdateDate(today);
				receiptAppData.setPolicyStatus("55");
				session.update(receiptAppData);
				newBatchNames.add(receiptAppData.getNewBatchName());
			}
			if(cardAppData != null){
				cardAppData.setUpdateDate(today);
				cardAppData.setPolicyStatus("55");
				session.update(cardAppData);
				newBatchNames.add(cardAppData.getNewBatchName());
			}
			*/
			tx.commit();	
			if(queryResult != null){
			   ApplyData ad;
			   int listCounter = 0;
			   for(int i = 0 ; i < queryResult.size() ; i++){
				  ApplyData appD = queryResult.get(i);
				  if(appD.getUniqueNo().equals(applyData.getUniqueNo())){
					  ad = appD;
					  ApplyData receiptData = ad.getReceiptData();
					  ApplyData card = ad.getInsureCardData();
					  if(receiptData != null && !"100".equals(receiptData.getPolicyStatus()) 
								&& "55".compareTo(receiptData.getPolicyStatus()) > 0 
								&& (!"41".equals(receiptData.getExceptionStatus()) 
								|| ("41".equals(receiptData.getExceptionStatus())  
								     &&  receiptData.getVerifyResult().indexOf("尚未接收到送金單") < 0)))
						      receiptData.setPolicyStatus("55");
					  if(card != null && !"100".equals(card.getPolicyStatus()) 
								&& "55".compareTo(card.getPolicyStatus()) > 0 
								&& (!"41".equals(card.getExceptionStatus()) 
								|| ("41".equals(card.getExceptionStatus())  
								     &&  card.getVerifyResult().indexOf("尚未接收到送金單") < 0)))
						      card.setPolicyStatus("55");
					  applyData.setReceiptData(receiptData);
					  applyData.setInsureCardData(card);
					  listCounter = i;
					  break;
				  }
			   }
			   queryResult.remove(listCounter);
			   queryResult.add(applyData);
			   sortByStatus();
			}
			this.setResult("配表完成，請繼續");
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

	public UIData getDataTable1() {
		return dataTable1;
	}

	public void setDataTable1(UIData dataTable1) {
		this.dataTable1 = dataTable1;
	}

	public SortableModel getDataModel1() {
		return dataModel1;
	}

	public void setDataModel1(SortableModel dataModel1) {
		this.dataModel1 = dataModel1;
	}

	public String getUniqueNo() {
		return uniqueNo;
	}

	public void setUniqueNo(String uniqueNo) {
		this.uniqueNo = uniqueNo;
	}

	public String getPolicyNos() {
		return policyNos;
	}

	public void setPolicyNos(String policyNos) {
		this.policyNos = policyNos;
	}
	public String getCycleDateStr(){
		if(cycleDate != null)
		   return Constant.slashedyyyyMMdd.format(cycleDate);
		else 
		   return null;
	}

	public String getGroupPolicyNo() {
		return groupPolicyNo;
	}

	public void setGroupPolicyNo(String groupPolicyNo) {
		this.groupPolicyNo = groupPolicyNo;
	}



	public String getGroupObjStr() {
		return groupObjStr;
	}



	public void setGroupObjStr(String groupObjStr) {
		this.groupObjStr = groupObjStr;
	}



	public String getPolicyNoObjStr() {
		return policyNoObjStr;
	}



	public void setPolicyNoObjStr(String policyNoObjStr) {
		this.policyNoObjStr = policyNoObjStr;
	}



	public String getCardPagesStr() {
		return cardPagesStr;
	}



	public void setCardPagesStr(String cardPagesStr) {
		this.cardPagesStr = cardPagesStr;
	}



	public Integer getCardPages() {
		return cardPages;
	}

	public void setCardPages(Integer cardPages) {
		this.cardPages = cardPages;
	}
	public String getRowClases(){
		String classes = "";
		
		for(int i = 0; i < dataModel1.getRowCount(); i++){		   		
		   dataModel1.setRowIndex(i);		
		   ApplyData applyData = (ApplyData)dataModel1.getRowData();
		   if(applyData.getPolicyStatus() != null && new Integer(applyData.getPolicyStatus()) >= 55){
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
	public String getAfpRowClases(){
		String classes = "";		
		for(int i = 0; i < dataModel.getRowCount(); i++){		   		
		   dataModel.setRowIndex(i);		
		   AfpFileDisplay afpFile = (AfpFileDisplay)dataModel.getRowData();
		   if(afpFile.getStatus() != null && (afpFile.getStatus().equals("配表完成") 
				   || afpFile.getStatus().indexOf("貨運") >= 0 
				   || afpFile.getStatus().indexOf("裝箱") >= 0) 
				   || afpFile.getStatus().indexOf("交寄") >= 0){
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

	public String getReturnEnv() {
		return returnEnv;
	}

	public void setReturnEnv(String returnEnv) {
		this.returnEnv = returnEnv;
	}

	public String getBankReceiptId() {
		return bankReceiptId;
	}

	public void setBankReceiptId(String bankReceiptId) {
		this.bankReceiptId = bankReceiptId;
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

	public String getMatchMapStr() {
		return matchMapStr;
	}

	public void setMatchMapStr(String matchMapStr) {
		this.matchMapStr = matchMapStr;
	}

	public Boolean getCd() {
		return cd;
	}

	public void setCd(Boolean cd) {
		this.cd = cd;
	}

	public Boolean getHumidProof() {
		return humidProof;
	}

	public void setHumidProof(Boolean humidProof) {
		this.humidProof = humidProof;
	}

	public String getAlertStr() {
		return alertStr;
	}

	public void setAlertStr(String alertStr) {
		this.alertStr = alertStr;
	}

	public boolean isHaveBankReceipt() {
		return haveBankReceipt;
	}

	public void setHaveBankReceipt(boolean haveBankReceipt) {
		this.haveBankReceipt = haveBankReceipt;
	}

	public UIData getBrDataTable() {
		return brDataTable;
	}

	public void setBrDataTable(UIData brDataTable) {
		this.brDataTable = brDataTable;
	}

	public SortableModel getBrDataModel() {
		return brDataModel;
	}

	public void setBrDataModel(SortableModel brDataModel) {
		this.brDataModel = brDataModel;
	}
	
    public boolean retrieveNoBankReceiptData(){ 
		Date now = new Date();
		Session session = null;
		Transaction tx = null;
		
		try{
			setDataModel1(null);
	 		setDataModel(null);
			session = HibernateSessionFactory.getSession();
		    group = false;		    
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ApplyData.class, "applyData")
                    .createAlias("applyData.bankReceipts", "bankReceipts", Criteria.INNER_JOIN);
			criteria.add(Restrictions.isNull("applyData.packId"));
			//如果exceptionStatus有值，且驗單結果裡有"尚未收到送金單"的字樣的，不包含在內
			criteria.add(Restrictions.or(
					   Restrictions.or(Restrictions.isNull("exceptionStatus"), Restrictions.eq("exceptionStatus", "")),
					   Restrictions.and(Restrictions.ne("exceptionStatus", ""), Restrictions.not(Restrictions.like("verifyResult", "%尚未接收到送金單%")))
					   
					));
			criteria.add(Restrictions.gt("applyData.cycleDate", Constant.yyyyMMdd.parse("20150112")));  //這天是正式上線日
			criteria.addOrder(Order.asc("applyData.policyStatus")).addOrder(Order.asc("applyData.cycleDate")).addOrder(Order.asc("applyData.uniqueNo"));			
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			
			withCardMap = new HashMap<String, Boolean>();			
			policyNoCardMap = new HashMap<String, String>();
			cardPagesMap = new HashMap<String, String>();
            List<String> newBatchNames = new ArrayList<String>();
			queryResult = criteria.list();
			List<String> oldBatchNames = new ArrayList<String>();
			for(ApplyData applyData : queryResult){
				oldBatchNames.add(applyData.getOldBatchName().replaceAll("保單", "簽收回條"));
			}
			if(oldBatchNames.size() == 0)
				return false;
			
			Query query = session.createQuery("from ApplyData where receipt = true and  packId is null and sourceCode <> 'GROUP' and cycleDate > :beginDate "
					+ " and newBatchName is not null and newBatchName not like '%9999' and oldBatchName in (:oldBatchNames) order by uniqueNo")
					.setDate("beginDate", Constant.yyyyMMdd.parse("20150112"))
					.setParameterList("oldBatchNames", oldBatchNames);
			List<ApplyData> receiptDatas = query.list();
			Map<String, ApplyData> receiptDataMap = new HashMap<String, ApplyData>();
			for(ApplyData receipt : receiptDatas){
				receiptDataMap.put(receipt.getOldBatchName().replaceAll("簽收回條", "保單"), receipt);
			}

			
			criteria = session.createCriteria(BankReceipt.class, "bankReceipt")					
                    .createAlias("bankReceipt.applyData", "applyData", Criteria.INNER_JOIN);
			criteria.add(Restrictions.in("applyData", queryResult));				
			criteria.addOrder(Order.asc("applyData.cycleDate")).addOrder(Order.asc("applyData.uniqueNo"));			
			//criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			List<BankReceipt> bankReceipts = criteria.list();
			setBrDataModel(new SortableModel(new ListDataModel(bankReceipts)));
			
			this.haveBankReceipt = false;
			List<Date> cycleDates = new ArrayList<Date>();
			if(queryResult != null && queryResult.size() > 0){							    
				cycleDate = queryResult.get(0).getCycleDate();
				center = queryResult.get(0).getCenter();				
				boolean allFinish = true;
				matchMap = new HashMap<String, Map<String, String>>();
				oldBatchNames = new ArrayList<String>(); 
				for(ApplyData applyData : queryResult){
					if(!cycleDates.contains(applyData.getCycleDate())){
						cycleDates.add(applyData.getCycleDate());
					}
					oldBatchNames.add(applyData.getOldBatchName());
					newBatchNames.add(applyData.getNewBatchName());
					if(applyData.getUniqueNo() != null){
					   withCardMap.put(applyData.getUniqueNo(), applyData.getHaveInsureCard() == null ? false : applyData.getHaveInsureCard());
					   if(applyData.getPolicyNoSet().size() > 0)
					      policyNoCardMap.put(applyData.getUniqueNo(), applyData.getPolicyNos().split(",")[1]);					   
					}
					
					Integer status = new Integer(applyData.getPolicyStatus() == null ? "0" : applyData.getPolicyStatus());
					//只有驗單完成的才設定成配表中
					if((status <= 42 && status != 41) || (applyData.getGroupInsure() != null && applyData.getGroupInsure()))
					   applyData.setPolicyStatus("50");					
					
					Integer statusStr = new Integer(applyData.getPolicyStatus());
					//如果還有在配表中，就不是全部完成
					if(statusStr == 50){
						allFinish = false;
					}
					//找出簽收單
					
					String policyNo = null;
					for(String str : applyData.getPolicyNoSet()){
						policyNo = str;
						break;
					}
					//recPolicyNo: 簽收單保單號碼
					//insideMap內有policyNo : 簽收單保單號碼
					//returnEnv：回郵信封
					//bankReceiptId：送金單號碼
					//cd:附光碟
					insideMap = new HashMap<String, String>();
					insideMap.put("recPolicyNo", "");
					if(applyData.getChannelID() != null){					
						if(applyData.getAreaId().toUpperCase().startsWith("TQ")
								|| applyData.getAreaId().toUpperCase().startsWith("TX")){
							insideMap.put("returnEnv", "北");
						}else if(applyData.getAreaId().toUpperCase().startsWith("TC")){
							insideMap.put("returnEnv", "中");
						}else if(applyData.getAreaId().toUpperCase().startsWith("TN")){
							insideMap.put("returnEnv", "高");
						}else{
							insideMap.put("returnEnv", "");
						}
					}else{
						insideMap.put("returnEnv", "");
					}
					/*
					if(applyData.getHavaBkReceipt() != null && applyData.getHavaBkReceipt()){
						boolean match = false;
						for(BankReceipt bankReceipt : bankReceipts){
							if(applyData.getBankReceiptId().equals(bankReceipt.getBankReceiptId())){
								match = true;
								break;
							}
						}
						if(match){
						   insideMap.put("bankReceiptId", applyData.getBankReceiptId());
						   //bankReceipts.add(applyData.getBankReceipt());
						   this.haveBankReceipt = true;
						}else{
						   insideMap.put("bankReceiptId", "送金單" + applyData.getBankReceiptId() + "未收到");
						}
					}else{
						insideMap.put("bankReceiptId", "");
					}
					*/
					insideMap.put("bankReceiptId", "");
					insideMap.put("bankReceiptId2", "");
					insideMap.put("bankReceiptId3", "");
					insideMap.put("bankReceiptId4", "");
					insideMap.put("bankReceiptId5", "");
					insideMap.put("bankReceiptId6", "");
					insideMap.put("bankReceiptId7", "");
					insideMap.put("bankReceiptId8", "");
					insideMap.put("bankReceiptId9", "");
					if(applyData.getHavaBkReceipt() != null && applyData.getHavaBkReceipt()){						
						int counter = 0;			
						//跑兩重迴圈，檢查是不是所有的收金單都已經收到
						for(String bankReceiptId : applyData.getBankReceiptIdSet()){
						   counter++;	
						   boolean match = false;
						   for(BankReceipt bankReceipt : bankReceipts){
						      if(bankReceipt.getBankReceiptId().equals(bankReceiptId)){
						    	  match = true;
						    	  break;
						      }
						   }
						   if(match){
							   //有收到時，在inside map添加送金單號碼
							   this.haveBankReceipt = true;
							   if(counter == 1){
								   insideMap.put("bankReceiptId", bankReceiptId);								   
							   }else{
								   insideMap.put("bankReceiptId" + counter, bankReceiptId);
							   }
						   }else{
							   if(counter == 1){
								   insideMap.put("bankReceiptId", "送金單" + bankReceiptId + "未收到");								   
							   }else{
								   insideMap.put("bankReceiptId" + counter, "送金單" + bankReceiptId + "未收到");
							   }
						   }						   
						}
						/*
						if(match1){
						   insideMap.put("bankReceiptId", applyData.getBankReceiptId());
						   //bankReceipts.add(applyData.getBankReceipt());
						   this.haveBankReceipt = true;
						}else{
						   insideMap.put("bankReceiptId", "送金單" + applyData.getBankReceiptId() + "未收到");
						}
						*/
					}
					if(applyData.getCd() != null && applyData.getCd())
						insideMap.put("cd", "true");
					else
						insideMap.put("cd", "");
					if(applyData.getSourceCode().equals("NORM")){
						insideMap.put("humidProof", "true");
					}else{
						insideMap.put("humidProof", "");
					}
					

                    
                    ApplyData receiptData = null;
                    if(receiptDatas != null && receiptDatas.size() > 0){
                        receiptData = receiptDataMap.get(applyData.getOldBatchName());
                        if(receiptData != null)
                           newBatchNames.add(receiptData.getNewBatchName());
                         
                    }
                    if(receiptData != null){
                    	oldBatchNames.add(receiptData.getOldBatchName());
				        Integer recStatus = receiptData.getPolicyStatus() == null ? 0 :new Integer(receiptData.getPolicyStatus());
						String recNo = null;
						for(String recPolicyNo : receiptData.getPolicyNoSet()){
						    if(recPolicyNo != null && (recNo == null || recNo.compareTo(recPolicyNo) > 0)){
						       recNo = recPolicyNo;
						    }
						}
						if(receiptData.getTotalPage() != null)
						   insideMap.put("recApplyPages", receiptData.getTotalPage() + "");
						else
						   insideMap.put("recApplyPages", "");
						insideMap.put("recPolicyNo", recNo);
 						//如果簽收單是在驗單完成，就更新
						/*
						if(recStatus <= 42){
						   receiptData.setPolicyStatus("50");							
						   //session.update(receiptData);
						}
						*/
					    applyData.setReceiptData(receiptData);
                    }
					//session.update(applyData);
					if(applyData.getUniqueNo() != null)
					   matchMap.put(applyData.getUniqueNo(), insideMap);
				}
				if(oldBatchNames.size() > 0)
				   session.createQuery("update ApplyData set policyStatus = '50', updateDate = :updateDate where oldBatchName in (:oldBatchNames) "
				   		+ "and policyStatus < '50' and policyStatus <> '41'  and policyStatus <> '100' and policyStatus <> '97'and policyStatus <> '98' ")
				           .setParameter("updateDate", now).setParameterList("oldBatchNames", oldBatchNames).executeUpdate();
				
				if(matchMap.size() > 0){
				   JSONObject jo = JSONObject.fromObject(matchMap);
				   matchMapStr = jo.toString();
				}else{
				   matchMapStr = "{}";
				}
				if(withCardMap.size() > 0){
				   JSONObject jo = JSONObject.fromObject(withCardMap);
				   groupObjStr = jo.toString();
				}else{
				   groupObjStr = "{}";
				}
				
				if(policyNoCardMap.size() > 0){
					   JSONObject jo = JSONObject.fromObject(policyNoCardMap);
					   policyNoObjStr = jo.toString();
					}else{
						policyNoObjStr = "{}";
					}
				
				if(cardPagesMap.size() > 0){
					   JSONObject jo = JSONObject.fromObject(cardPagesMap);
					   cardPagesStr = jo.toString();
					}else{
						cardPagesStr = "{}";
					}				
			}		
							
 
			setDataModel1(new SortableModel(new ListDataModel(queryResult)));
			//找出此轄區中此cycleDate所有的批次列印檔
			
			String queryStr = "from AfpFile where newBatchName in(:newBatchNames)";
			List<AfpFile> afpFiles = null; 
			if(newBatchNames.size() > 0){
				afpFiles = session.createQuery(queryStr).setParameterList("newBatchNames", newBatchNames).list();
			}
			
			queryStr = "select a.policyStatus, count(a), a.newBatchName from ApplyData a where a.newBatchName in (:newBatchNames) and cycleDate in (:cycleDates) group by a.newBatchName, a.policyStatus";
			List<Object[]> listAll = session.createQuery(queryStr).setParameterList("newBatchNames", newBatchNames).setParameterList("cycleDates", cycleDates).list();
			Map<String, List<Object[]>> afpMap = new HashMap<String, List<Object[]>>();
			for(Object[] objArr : listAll){
				String newBatchName = (String)objArr[2];
				List<Object[]> objList = afpMap.get(newBatchName);
				if(objList == null){
					objList = new ArrayList<Object[]>();
				}
			    objList.add(objArr);					
			    afpMap.put(newBatchName, objList);
			}
		
			List<AfpFileDisplay> displayResult = new ArrayList<AfpFileDisplay>(); //真正要顯示的
			if(afpFiles != null && afpFiles.size() > 0){
				int allNotMached = 0; 				
				for(AfpFile afp : afpFiles){
					List<Object[]> list = afpMap.get(afp.getNewBatchName());					
					if(list == null)
						list = new ArrayList<Object[]>();
					AfpFileDisplay afpDisplay = new AfpFileDisplay();
																				
					// 用gooup by 查出各狀態的數量
					
					
					int matched = 0;
					int notMatched = 0;
					int errors = 0;
					int verifiedErrs = 0;
					int volumns = 0;
					for(Object [] result : list){
						Integer policyStatus = new Integer((String) result[0]);
						Integer counter = new Integer( result[1].toString());
						volumns += counter;
						if(policyStatus >= 55){
							//配表完成
							matched += counter;
						}else if(policyStatus < 55 
								&& !(policyStatus == 11 || policyStatus == 13 || policyStatus == 14 || policyStatus == 16) 
								&& policyStatus != 41){
							//未配表完成
							notMatched += counter;
							allNotMached += counter;
						}else if(policyStatus == 11 || policyStatus == 13 || policyStatus == 14 || policyStatus == 16){
							//異常
							errors += counter; 
						}else if( policyStatus == 41){
							//驗單錯誤
							verifiedErrs += counter;
						}
					}
					if(notMatched == 0 && afp.getStatus().indexOf("裝箱") < 0 
							&& afp.getStatus().indexOf("貨運") < 0 && afp.getStatus().indexOf("交寄") < 0 ){
						afp.setStatus("配表完成");
						session.update(afp);
					}else if(notMatched > 0 && afp.getStatus().indexOf("裝箱") < 0 
							&& afp.getStatus().indexOf("貨運") < 0 && afp.getStatus().indexOf("交寄") < 0 
							&& afp.getStatus().indexOf("配表完成") < 0){
						afp.setStatus("配表中");
						session.update(afp);
					} 
					BeanUtils.copyProperties(afp, afpDisplay);
					if(allNotMached == 0){
						this.setResult("全部保單均已配表完成，請繼續打包作業");
					}
					afpDisplay.setCenter(ApplyData.getCenterMap().get(afpDisplay.getCenter()));
					afpDisplay.setMatched(matched);
					afpDisplay.setNotMatched(notMatched);
					afpDisplay.setErrors(errors);
					afpDisplay.setVerifiedErrs(verifiedErrs);
					afpDisplay.setVolumns(volumns);
					
					displayResult.add(afpDisplay);
				}	
			}
			setDataModel(new SortableModel(new ListDataModel(displayResult)));
			tx.commit();
			
			return true;
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			return false;
		}finally{
			if(session != null && session.isOpen())
				session.close();
			
		}	
		
	}

    public String getLastJobBagNo() {
	   return lastJobBagNo;
    }

    public void setLastJobBagNo(String lastJobBagNo) {
	   this.lastJobBagNo = lastJobBagNo;
    }
    
    public ApplyData setWeight(ApplyData applyData){
    	boolean group = "GROUP".equals(applyData.getSourceCode())? true : false;
    	String uniqueNo = applyData.getUniqueNo();
    	
    	ApplyData insureCard = null;

    	if(insureCards != null)
     	   for(ApplyData ad : insureCards){
     		  if(ad.getPolicyNos().equals(applyData.getPolicyNos())){
     			 insureCard = ad;
     			  break;
     		  }
     	   }
    	double after_match_weight = 0;	    
	    
	    
	    double bookSheetsWg = weightMap.get("內頁紙張") == null? 0 : weightMap.get("內頁紙張");
	    int bookSheets = applyData.getTotalPage() == null? 0 : applyData.getTotalPage();
	    bookSheets = bookSheets % 2 == 1? (bookSheets + 1) / 2 : bookSheets / 2;
	    bookSheetsWg = bookSheetsWg * bookSheets; //內頁重量
	    after_match_weight += bookSheetsWg;
	    //System.out.println("bookSheetsWg:" + bookSheetsWg);
	    
	    double coversWg = weightMap.get("封底封面") == null? 0 : weightMap.get("封底封面");
	    coversWg = coversWg * 2; //封底 + 封面
	    after_match_weight += coversWg;
	    //System.out.println("coversWg:" + coversWg);
	    
	    double filmsWg = weightMap.get("膠片") == null? 0 : weightMap.get("膠片");//膠片
	    after_match_weight += filmsWg;
	    //System.out.println("filmsWg:" + filmsWg);
	    
	    double receiptWg = weightMap.get("內頁紙張") == null? 0 : weightMap.get("內頁紙張");	    
    	if(group){    		
    		double cardsWg = weightMap.get("保險證") == null? 0 : weightMap.get("保險證");
    	    int cardPages = insureCard == null ? 0 : (insureCard.getTotalPage() == null? 0 : insureCard.getTotalPage());
    	    cardsWg = cardsWg * cardPages; //保險證重量    	    
    	    after_match_weight += cardsWg;    	    
    	    after_match_weight += receiptWg;
    	    
    	}else{
    		Map<String, String> matchResult = matchMap.get(uniqueNo);
    		int recApplyPages = matchResult.get("recApplyPages") == null || matchResult.get("recApplyPages").equals("") ? 0 : new Integer(matchResult.get("recApplyPages"));		    
    	    receiptWg = receiptWg * recApplyPages; //簽收回條重量    	    
    	    after_match_weight += receiptWg;
    	    //System.out.println("receiptWg:" + receiptWg);
    		
    		if(matchResult != null){
    			//System.out.println("after_match_weight0:" + after_match_weight);
        		double returnEnvWg = weightMap.get("回郵信封") == null? 0 : weightMap.get("回郵信封");
    			boolean returnEnv = matchResult.get("returnEnv") == null || matchResult.get("returnEnv").equals("") ? false : true; //回郵
    			if(returnEnv)
    				after_match_weight += returnEnvWg;   
    			//System.out.println("returnEnvWg:" + returnEnvWg);
    			
    			//System.out.println("after_match_weight1:" + after_match_weight);
    			
    			double bankReceiptWg = weightMap.get("送金單") == null? 0 : weightMap.get("送金單");
    			boolean bankReceiptId = matchResult.get("bankReceiptId") == null || matchResult.get("bankReceiptId").equals("") ? false : true; //
    			if(bankReceiptId)
    				after_match_weight += bankReceiptWg;
    			boolean bankReceiptId2 = matchResult.get("bankReceiptId2") == null || matchResult.get("bankReceiptId2").equals("") ? false : true; //
    			if(bankReceiptId2)
    				after_match_weight += bankReceiptWg;
    			boolean bankReceiptId3 = matchResult.get("bankReceiptId3") == null || matchResult.get("bankReceiptId3").equals("") ? false : true; //
    			if(bankReceiptId3)
    				after_match_weight += bankReceiptWg;
    			boolean bankReceiptId4 = matchResult.get("bankReceiptId4") == null || matchResult.get("bankReceiptId4").equals("") ? false : true; //
    			if(bankReceiptId4)
    				after_match_weight += bankReceiptWg;
    			boolean bankReceiptId5 = matchResult.get("bankReceiptId5") == null || matchResult.get("bankReceiptId5").equals("") ? false : true; //
    			if(bankReceiptId5)
    				after_match_weight += bankReceiptWg;
    			//System.out.println("after_match_weight2:" + after_match_weight);
    			

    			double cdWg = weightMap.get("光碟") == null? 0 : weightMap.get("光碟");
    			boolean cd = matchResult.get("cd") == null || matchResult.get("cd").equals("") ? false : true; //cd
    			if(cd)
    				after_match_weight += cdWg;
    			//System.out.println("after_match_weight3:" + after_match_weight);
    			
    			
    			double humidproofBagWg = weightMap.get("夾鏈袋") == null? 0 : weightMap.get("夾鏈袋");
    			boolean humidProof = matchResult.get("humidProof") == null || matchResult.get("humidProof").equals("") ? false : true; //cd
    			if(humidProof)
    				after_match_weight += humidproofBagWg;
    			//System.out.println("after_match_weight4:" + after_match_weight);
    		}    		
    	}
    	if(after_match_weight > 10)
    	   applyData.setWeight(after_match_weight - 10);
    	
    	return applyData;
    }
    
    public void sortByStatus() {
    	if(queryResult != null){
 		    Collections.sort(queryResult, new Comparator<ApplyData>() {
 			   @Override
 			   public int compare(ApplyData o1, ApplyData o2) {
 				   Integer o1Status = o1.getPolicyStatus() == null? 0 : new Integer(o1.getPolicyStatus()); 
 				   Integer o2Status = o2.getPolicyStatus() == null? 0 : new Integer(o2.getPolicyStatus()); 				   
 				   int statusCompare = o1Status.compareTo(o2Status); 					
 				   if(statusCompare == 0){
 					   return o1.getUniqueNo().compareTo(o2.getUniqueNo());
 				   }else{
 					   return statusCompare;
 				   }
 			   }
 		    });
 		   setDataModel1(new SortableModel(new ListDataModel(queryResult)));
    	}else{    	   
    	   setDataModel1(null);
    	}
 	}
}
