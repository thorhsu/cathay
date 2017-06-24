package com.salmat.pas.bo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.ibm.icu.util.Calendar;
import com.salmat.pas.conf.Constant;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.BankReceipt;
import com.salmat.pas.vo.LogisticStatus;
import com.salmat.pas.vo.PackStatus;
import com.salmat.util.HibernateSessionFactory;

/**
 * 
 * ApplyData servevice
 *
 */
public class ApplyDataService {
	static Logger logger = Logger.getLogger(ApplyDataService.class);
	
	public static ApplyData findByPK(String oldBatchName){
		Session session = null;
		try{			
			session = HibernateSessionFactory.getSession();
			return (ApplyData) session.get(ApplyData.class, oldBatchName);
		}catch(Exception e){
			logger.error("", e);
			return null;
		}finally{
			if(session != null)
				session.close();
		}		
	}
	
	public static ApplyData findByCriteria(String cycleDateStr, String center, String applyNo, String areaId){
		Session session = null;
		try{			
			Date cycleDate = Constant.slashedyyyyMMdd.parse(cycleDateStr);			
			session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(ApplyData.class);
			criteria.add(Restrictions.eq("cycleDate", cycleDate));
			//criteria.add(Restrictions.eq("center", center));
			criteria.add(Restrictions.eq("applyNo", applyNo));
			criteria.add(Restrictions.eq("areaId", areaId));
			List<ApplyData> list = criteria.list();
			if(list != null && list.size() > 0)
				return list.get(0);
			else
				return null;
		}catch(Exception e){
			logger.error("", e);
			return null;
		}finally{
			if(session != null)
				session.close();
		}		
	}
	
	public static List<ApplyData> findByNewBatchName(String newBatchName){
		Session session = null;		
		try{			
			session = HibernateSessionFactory.getSession();
			return session.getNamedQuery("ApplyData.findByNewBatchName").setString(0, newBatchName).list();
		}catch(Exception e){
			logger.error("", e);
			return null;
		}finally{
			if(session != null)
				session.close();
		}				
	}
	
	//北二打包及裝箱清單產生
	public static synchronized List<PackStatus> prepareTaipeiNo2Pack(Session session, List<AfpFile> afpFiles, Date cycleDate, String registerNo, AdminUser user, String parcelNo) {
		double commonBagWg = 0;			
		double bigBagWg = 0;
		double mailReceiptWg = 0;
		if(Constant.getWeightMap() != null){
			if(Constant.getWeightMap().get("牛皮紙袋（平面袋）") != null){
				commonBagWg = Constant.getWeightMap().get("牛皮紙袋（平面袋）");
			}
			if(Constant.getWeightMap().get("牛皮紙袋（立體袋）") != null){
				bigBagWg = Constant.getWeightMap().get("牛皮紙袋（立體袋）");
			}
			if(Constant.getWeightMap().get("雙掛號回執聯") != null){
				mailReceiptWg = Constant.getWeightMap().get("雙掛號回執聯");
			}
		}
		
		Date today = new Date();
		String cycleStr = Constant.yyMMdd.format(cycleDate);
		List<String> newBatchNames = new ArrayList<String>();
		List<String> receiptBatchNames = new ArrayList<String>();		
		List<String> allBatchNames = new ArrayList<String>();
		for(AfpFile afpFile : afpFiles){
			newBatchNames.add(afpFile.getNewBatchName());
			if(afpFile.getNewBatchName().startsWith("CA")){
				receiptBatchNames.add(afpFile.getNewBatchName().replaceFirst("CA", "SG")); 
			}
		}		
		allBatchNames.addAll(newBatchNames);
		allBatchNames.addAll(receiptBatchNames);
		//非退件 
		String queryStr = "select count(*), receiver, address from ApplyData where ((policyStatus <> '100' and policyStatus <> '98' and policyStatus <> '97') or policyStatus is null) and newBatchName in (:newBatchNames) and (substract is null or substract = false) and (exceptionStatus is null or exceptionStatus = '') group by receiver, address";		
		String orderStr = "from ApplyData where newBatchName in (:newBatchNames) and ((policyStatus <> '100' and policyStatus <> '98' and policyStatus <> '97') or policyStatus is null) and (substract is null or substract = false) and (exceptionStatus is null or exceptionStatus = '') order by uniqueNo";
		List<Object[]> rows = session.createQuery(queryStr).setParameterList("newBatchNames", newBatchNames).list();
		List<ApplyData> orderResults = session.createQuery(orderStr).setParameterList("newBatchNames", allBatchNames).list();
		List<Object[]> results = new ArrayList<Object[]>();
		//把它排序一下(這段是後來加的，不想改程式，所以很笨)
		for(ApplyData applyData : orderResults){
			for(Object[] addressObj : rows){
				if(applyData.getReceiver() != null && addressObj[1] != null && applyData.getReceiver().equals((String)addressObj[1]) 
						&& applyData.getAddress() != null && addressObj[2] != null && applyData.getAddress().equals((String)addressObj[2])){
					if(!results.contains(addressObj)){
					   results.add(addressObj);
					   //加入後跳出去
					   break;
					}else{
					   //如果已經有包含就跳出去
					   break;
					}
				}
			}
		}
		int counter = 0;	
		List<String> maxString = session.createQuery("select max(logisticId) from LogisticStatus where cycleDate = ? and batchOrOnline = 'B' and center = '06'").setParameter(0, cycleDate).list();
		if(maxString != null && maxString.size() > 0 && maxString.get(0) != null){					
			String packId = maxString.get(0);
			counter = (new Integer(packId.substring(packId.length() - 4)));
		}
		
		List<PackStatus> packStatuses = new ArrayList<PackStatus>();		
		List<PackStatus> resortPacks = new ArrayList<PackStatus>();
		List<PackStatus> tpeNo2Packs = new ArrayList<PackStatus>();		
		List<LogisticStatus> resortLss = new ArrayList<LogisticStatus>();
		List<LogisticStatus> mailReceiptLss = new ArrayList<LogisticStatus>();
		
		for(int i = 0 ; i < results.size(); i++){
			boolean mailReceipt = true;
			Object[] result = results.get(i);
			Integer adCount = 0;
			String receiver = (String)result[1];
			String address = (String)result[2];
			//查出所有的保單
			int receipts = 0;
			//List<ApplyData> applyDatas = applyQuery.setParameterList("newBatchNames", allBatchNames).setString("address", address).setString("receiver", receiver).list();
			List<ApplyData> applyDatas = new ArrayList<ApplyData>();
			//int arrayCounter1 = -1;
			//int arrayCounter2 = -1;
			int arrayCounter = 0;
			List<Integer> packCounters = new ArrayList<Integer>();

			for(ApplyData applyData : orderResults){
				mailReceipt = true;
				if((applyData.getReceipt() == null || !applyData.getReceipt()) 
						&& applyData.getReceiver() != null && receiver != null && applyData.getReceiver().equals(receiver) 
						&& applyData.getAddress() != null && address != null && applyData.getAddress().equals(address)
						){
					String channelId = applyData.getChannelID();
					String deliverType = applyData.getDeliverType();
					
					if(channelId.toUpperCase().equals("G"))
						mailReceipt = false;
					//如果是寄要保人，就要有回執聯
					if("S".equals(deliverType))
						mailReceipt = true;
					else if("B".equals(deliverType))
						mailReceipt = false;
					
					if(applyData.getReceiver().equals("北二行政中心") 
							   || applyData.getReceiver().equals("北二審查科")){
						mailReceipt = false;
					}

					//需要回執聯時，一個packStatus最多裝兩本保單
					if((packCounters.size() < 2)&& mailReceipt){
						applyDatas.add(applyData);
						//arrayCounter1 = arrayCounter;
						packCounters.add(arrayCounter);

					}else if(applyDatas.size() >= 2 && mailReceipt){
						//如果是雙掛號時，且超過兩個就離開						
						i--;
						break;
					}else if(!mailReceipt){
						packCounters.add(arrayCounter);
						applyDatas.add(applyData);						
					}
				}				
				arrayCounter++;
			}
			adCount = applyDatas.size();
			//再把簽收單裝進去
			ArrayList<ApplyData> receiptLists = new ArrayList<ApplyData>();
			
			int packSize = packCounters.size();		
			//已經被取走的就移開，避免重覆搜尋，加快速度
			for(int j = packSize - 1 ; j >= 0 ; j--){
				//不可以使用Wrapper class，要改成primitive type
				int id = packCounters.get(j).intValue(); 
				orderResults.remove(id);
			}
			int resultSize = orderResults.size();
			//已經被取走的就移開，避免重覆搜尋，加快速度
			for(int j = resultSize - 1 ; j >= 0 ; j--){
				ApplyData receiptData = orderResults.get(j);
				for(ApplyData applyData : applyDatas){
					if(receiptData.getReceipt() != null && receiptData.getReceipt() 
							&& receiptData.getPolicyNos().equals(applyData.getPolicyNos())
							&& receiptData.getApplyNo().equals(applyData.getApplyNo())
							&& receiptData.getReprint().equals(applyData.getReprint())
							&& receiptData.getCycleDate().equals(applyData.getCycleDate())){
						receiptLists.add(receiptData);
						orderResults.remove(j);
					}
				}
			}
			applyDatas.addAll(receiptLists);
			
			Set<String> newBatchNmSet = new HashSet<String>();
			String firstUniqueNo = null;
			String channelId = null;
			String channelNm = null;
			String zipCode = null;
			String deliverType = null;			
			boolean packDone = false;
			double packWeight = 0;
			for(ApplyData applyData : applyDatas){
				packWeight += applyData.getWeight() == null ? 0 : applyData.getWeight();
				newBatchNmSet.add(applyData.getNewBatchName());
				if(applyData.getUniqueNo() != null && applyData.getReceipt() != null 
						&& !applyData.getReceipt() && firstUniqueNo == null ){
					firstUniqueNo = applyData.getUniqueNo();					
				}
				if(applyData.getReceipt() != null && applyData.getReceipt()){
					receipts++;
				}
				
				channelId = applyData.getChannelID();
				channelNm = applyData.getChannelName();
				deliverType = applyData.getDeliverType();
				if(channelNm == null){
				   channelNm = applyData.getSourceMap().get(applyData.getSourceCode());
				}
				//如果是G，可能不需回執聯
				if(channelId.toUpperCase().equals("G"))
				   mailReceipt = false;
				//如果是寄要保人，就要有回執聯
				if("S".equals(deliverType))
				   mailReceipt = true;
				else if("B".equals(deliverType))
				   mailReceipt = false;
				
				if(applyData.getReceiver().equals("北二行政中心") 
					   || applyData.getReceiver().equals("北二審查科")){
				   mailReceipt = false;
				   packDone = true;
				}				
				zipCode = applyData.getZip();				
			}
			
			PackStatus ps = new PackStatus();
			//命名規則yyMMddTPEnnnn
			
			String packId = cycleStr + "TPE" + StringUtils.leftPad(++counter + "", 4, '0');
			LogisticStatus ls = new LogisticStatus();
			ls.setLogisticId(packId);
			ls.setAddress(address);
			ls.setBooks(adCount);
			ls.setCenter("06");
			ls.setCycleDate(cycleDate);
			ls.setFirstUniqueNo(firstUniqueNo);
			ls.setName(receiver);
			ls.setPacks(1);
			ls.setReceipts(receipts);
			ls.setMailReceipt(mailReceipt);
			if(mailReceipt){
			   if(adCount < 7)
			      ls.setWeight(packWeight + commonBagWg + mailReceiptWg);
			   else
				  ls.setWeight(packWeight + bigBagWg + mailReceiptWg);
			}
			ls.setBatchOrOnline("B");
			//String vendorId = StringUtils.leftPad(registerNum + "", registerNo.length(), '0');
			if(!packDone){
			   //ls.setVendorId(vendorId);			   
			   //registerNum++;
			}else{
			   ls.setSentTime(cycleDate);
			   ls.setPackDone(packDone);
			   ls.setScanDate(today);
			}
			session.save(ls);
			
			
			ps.setPackId(packId);
			ps.setAreaAddress(address);
			
			if(adCount >= 7)
			   ps.setWeight(packWeight + bigBagWg);
			else
			   ps.setWeight(packWeight + commonBagWg);
			//雙掛號時加上回執聯重量
			if(mailReceipt){
				ps.setWeight(ps.getWeight() + mailReceiptWg);
			}
			ps.setBack(false);
			ps.setBatchOrOnline("B");
			ps.setBooks(adCount);
			ps.setCenter("06");
			ps.setCreateDate(today);
			ps.setCycleDate(cycleDate);
			ps.setFirstUniqueNo(firstUniqueNo);
			ps.setInusreCard(null);
			ps.setLogisticId(packId);
			ps.setNewBatchNmSet(newBatchNmSet);
			ps.setPackCompleted(false);
			ps.setReceipts(receipts);
			ps.setReported(false);
			ps.setStatus(0);
			ps.setStatusNm("裝箱準備");
			ps.setSubAreaId(channelNm);
			ps.setSubAreaName(receiver);
			ps.setUpdateDate(today);
			ps.setZipCode(zipCode);
			if(packDone){
				ps.setStatus(45);
				ps.setStatusNm("等待貨運");
				ps.setPackCompleted(true);
				ps.setPolicyScanDate(today);
				ps.setReceiptScanDate(today);
				ps.setLabelScanDate(today);
				ps.setPolicyScanUser("SYSTEM");
				ps.setReceiptScanUser("SYSTEM");
				ps.setLabelScanUser("SYSTEM");				
			}
			session.save(ps);
			for(ApplyData applyData : applyDatas){
				applyData.setPackId(packId);
				if(!packDone){
				  applyData.setPolicyStatus("60");
				}else{			
				   if((applyData.getSubstract() != null && applyData.getSubstract()) || (applyData.getExceptionStatus() != null && !applyData.getExceptionStatus().trim().equals("")))
					  applyData.setPolicyStatus("98");
				   else
				      applyData.setPolicyStatus("97");
				   applyData.setUpdateDate(today);
				   //applyData.setDeliverTime(today);
				   if(applyData.getCycleDate() == null  || today.getTime() >= applyData.getCycleDate().getTime())
					   applyData.setDeliverTime(today);
				   else
					   applyData.setDeliverTime(applyData.getCycleDate());
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
				}
				session.update(applyData);
			}
			packStatuses.add(ps);
			//單掛，非退回，且非寄回北二的加入此List			
			if(!ps.isBack() && !mailReceipt && ps.getSubAreaName() != null && !ps.getSubAreaName().equals("北二行政中心")){
				resortPacks.add(ps);
				resortLss.add(ls);
			}else if(!ps.isBack() && !mailReceipt && ps.getSubAreaName() != null && ps.getSubAreaName().equals("北二行政中心")){
				tpeNo2Packs.add(ps);				
			}else if(!ps.isBack() && mailReceipt){
				mailReceiptLss.add(ls);
			}
		}
		if(tpeNo2Packs.size() > 0){
			double weight = 0;
			String logisticId = null;
			for(PackStatus ps : tpeNo2Packs){
				double psWg = ps.getWeight() == null? 0 : ps.getWeight();
				weight += psWg;
				logisticId = ps.getLogisticId();
			}
			if(logisticId != null){
				session.createQuery("update LogisticStatus set weight = " + weight + " where logisticId = '" + logisticId + "'").executeUpdate();
			}
		}
		
		//退件，不包含沒收到送金單的
		String applyQueryStr = "from ApplyData where newBatchName in (:newBatchNames) and (policyStatus <> '100' or policyStatus is null) "
				+ " and (packId is null or packId = '')"
				+ " and ((exceptionStatus is not null and exceptionStatus <> '' and verifyResult not like '%尚未接收到送金單%' )"
				+ " or substract = true )  order by uniqueNo";
		Query applyQuery = session.createQuery(applyQueryStr);		
		List<ApplyData> applyDatas = applyQuery.setParameterList("newBatchNames", allBatchNames).list();
		
		//全部裝成一包送回北二審查科
		if (applyDatas != null && applyDatas.size() > 0) {
			List<PackStatus> errorPackStatuses = session.createQuery("from PackStatus where cycleDate = ? and back = true and batchOrOnline = 'B' and center = '06'").setParameter(0, cycleDate).list();
			int books = 0;
			int receipts = 0;
			Set<String> newBatchNmSet = new HashSet<String>();
			double totalWeight = 0;

			for(ApplyData applyData : applyDatas){
				if(applyData.getReceipt() != null && applyData.getReceipt())
					receipts++;
				else if(applyData.getReceipt() != null && !applyData.getReceipt())
					books++;
				totalWeight += applyData.getWeight() == null? 0 : applyData.getWeight();
				newBatchNmSet.add(applyData.getNewBatchName());
			}
			if(applyDatas.size() >= 7)
				totalWeight += bigBagWg;
			else
				totalWeight += commonBagWg;
			List<Area> auditCenters = session.getNamedQuery("Area.findHaveAddressAndAudit").list();			
			Area auditArea = null;
			for(Area audit : auditCenters){
			    if(audit.getAreaName() != null && audit.getAreaName().indexOf("北二審查科") >= 0){
				     auditArea = audit;
				}
			}			
			PackStatus ps = new PackStatus();
			LogisticStatus ls = new LogisticStatus();
			// 命名規則yyMMddTPEnnnn
			String packId = cycleStr + "TPE"
					+ StringUtils.leftPad(++counter + "", 4, '0');
			if(errorPackStatuses != null && errorPackStatuses.size() > 0){
				ps = errorPackStatuses.get(0);
				packId = ps.getPackId();
				ls = (LogisticStatus) session.get(LogisticStatus.class, packId);
				books += ps.getBooks();
				receipts += ps.getReceipts();
			}

			
			
			ls.setLogisticId(packId);
			ls.setAddress(auditArea.getZipCode() + " " + auditArea.getAddress());
			ls.setBooks(books);
			ls.setCenter("06");
			ls.setBatchOrOnline("B");
			ls.setCycleDate(cycleDate);
			ls.setFirstUniqueNo(null);
			ls.setTel(auditArea.getTel());
			ls.setName("北二審查科");
			ls.setPacks(1);
			ls.setReceipts(receipts);
			ls.setWeight(totalWeight);
			//String vendorId = StringUtils.leftPad(registerNum + "",
					//registerNo.length(), '0');
		    //ls.setVendorId(vendorId);
		    ls.setSentTime(cycleDate);
			ls.setPackDone(true);
			ls.setScanDate(today);
			session.saveOrUpdate(ls);
			//registerNum++;

			ps.setPackId(packId);
			ps.setWeight(totalWeight);
			ps.setAreaAddress(auditArea.getZipCode() + " " + auditArea.getAddress());
			ps.setBack(true);
			ps.setBatchOrOnline("B");
			ps.setBooks(books);
			ps.setCenter("06");
			ps.setCreateDate(today);
			ps.setCycleDate(cycleDate);
			ps.setFirstUniqueNo(null);
			ps.setInusreCard(null);
			ps.setLogisticId(packId);
			ps.setNewBatchNmSet(newBatchNmSet);
			ps.setPackCompleted(true);
			ps.setReceipts(receipts);
			ps.setReported(false);			
			ps.setSubAreaTel(auditArea.getTel());
			ps.setSubAreaId(auditArea.getAreaId());
			ps.setSubAreaName("北二審查科");
			ps.setUpdateDate(today);
			ps.setZipCode(auditArea.getZipCode());
			ps.setStatus(45);
			ps.setStatusNm("等待貨運");
			ps.setPolicyScanDate(today);
			ps.setReceiptScanDate(today);
			ps.setLabelScanDate(today);
			ps.setPolicyScanUser("SYSTEM");
			ps.setReceiptScanUser("SYSTEM");
			ps.setLabelScanUser("SYSTEM");
			session.saveOrUpdate(ps);
			for (ApplyData applyData : applyDatas) {
				applyData.setPackId(packId);
				if((applyData.getSubstract() != null && applyData.getSubstract()) || (applyData.getExceptionStatus() != null && !applyData.getExceptionStatus().trim().equals("")))
				   applyData.setPolicyStatus("98");
				else
				   applyData.setPolicyStatus("97");
				
				applyData.setUpdateDate(today);
				//applyData.setDeliverTime(today);
				if(applyData.getCycleDate() == null  || today.getTime() >= applyData.getCycleDate().getTime())
				   applyData.setDeliverTime(today);
				else
				   applyData.setDeliverTime(applyData.getCycleDate());
				session.update(applyData);
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
			}
			packStatuses.add(ps);
		}
		
		//2015/06/26新增，將送國銀分行的再集中為一包
				
		HashMap<String, LogisticStatus> lsMap = new HashMap<String, LogisticStatus>();
		for(LogisticStatus ls : resortLss){
			String address = ls.getAddress();
			
			LogisticStatus firstLs = null;
			//如果地址對映的LogisticStatus是空的話，放入此map
			//之後所有的packStatus將對映到此LogisticStatus
			if((firstLs = lsMap.get(address)) == null){
				String receiver = ls.getName();
				String[] receivers = receiver.split(" ");
				if(receivers.length >= 2)
					ls.setName(receivers[0]);
				lsMap.put(address, ls);
			}
			//如果地址相同，但收件分行不同的話，分行要附加進去
			if(firstLs != null){ 
				String receiver = ls.getName();
				String[] receivers = receiver.split(" ");
				
				if(firstLs.getName() != null && ls.getName() != null 
						&& firstLs.getName().indexOf(receivers[0]) < 0){
					firstLs.setName(firstLs.getName() + "," + ls.getName());
					lsMap.put(address, firstLs);
				}
			}
		}		
		//接下來把packStatus對映的logisctId改變
		for(PackStatus ps : resortPacks){
			String address = ps.getAreaAddress();
			LogisticStatus ls = lsMap.get(address);
			// 如果對映的logisticId和PackStatus的logisticId不同，就改變它
			if (ls != null && !ls.getLogisticId().equals(ps.getLogisticId())) {
				int packs = ls.getPacks() == null ? 0 : ls.getPacks();
				packs++;
				ps.setLogisticId(ls.getLogisticId());
				// 重新計算books和receipts的數量
				ls.setBooks((ls.getBooks() == null ? 0 : ls.getBooks())
						+ ps.getBooks());
				ls.setReceipts((ls.getReceipts() == null ? 0 : ls.getReceipts())
						+ ps.getReceipts());
				ls.setPacks(packs);
				double totalWeight = (ls.getWeight() == null? 0 : ls.getWeight());
				totalWeight += (ps.getWeight() == null? 0 : ps.getWeight());				
				ls.setWeight(totalWeight);
				lsMap.put(address, ls);
				session.update(ps);
			}else if (ls != null && ls.getLogisticId().equals(ps.getLogisticId())) {
				double totalWeight = (ls.getWeight() == null? 0 : ls.getWeight());
				totalWeight += (ps.getWeight() == null? 0 : ps.getWeight());				
				ls.setWeight(totalWeight);
				lsMap.put(address, ls);
			}
			 
		}
		//刪除其它的LogisticStatus，並加入掛號號碼
		int registerNum = new Integer(registerNo);
		int parcelNum = new Integer(parcelNo);
		for(LogisticStatus oldLs : resortLss){
			LogisticStatus ls = lsMap.get(oldLs.getAddress());
			int packsCount = ls.getPacks() == null ? 0 : ls.getPacks(); 
			//如果是同一個時就update
			if(ls.getLogisticId().equals(oldLs.getLogisticId())){
				
				double weight = ls.getWeight() == null? 0 : ls.getWeight();
				ls.setWeight(weight);
				//計算重量
				if( packsCount == 0 ){
					ls.setWeight(0d);
				}else if(packsCount > 1){
					//如果含有多個袋子，要再用大袋子裝
					ls.setWeight(weight + bigBagWg);
				}
				//小於兩公斤是一般掛號，大於兩公斤是包裹
				if(ls.getWeight().doubleValue() < Constant.parcelWeight){
				   String vendorId = StringUtils.leftPad(registerNum + "", registerNo.length(), '0');
				   ls.setVendorId(vendorId);
				   registerNum++;
				}else{
				   String vendorId = StringUtils.leftPad(parcelNum + "", parcelNo.length(), '0');
				   ls.setVendorId(vendorId);
				   parcelNum++;
				}
				session.update(ls);
			}else{
				//不是同一個時就刪除
				session.delete(oldLs);
			}
		}
		//將雙掛號的掛號號碼也放進去，雙掛號最多兩本，不可能寄包裹		
		for(LogisticStatus ls : mailReceiptLss){
			String vendorId = StringUtils.leftPad(registerNum + "", registerNo.length(), '0');
			ls.setVendorId(vendorId);
			registerNum++;
			session.update(ls);
		}		
		// end of 2015/06/26修改
		
		Query query = session.getNamedQuery("ApplyData.findPackIdByNewBatchName");
		for(String newBatchName : allBatchNames){
			AfpFile afpFile = (AfpFile) session.get(AfpFile.class, newBatchName);
			List<String> packIds = query.setString(0, newBatchName).list();
			if(afpFile != null && packIds != null){
				afpFile.setPackIdSet(null);
				Set<String> packIdSet = afpFile.getPackIdSet();
				for(String packId: packIds){
				    packIdSet.add(packId);
				}
				afpFile.setStatus("裝箱中");
				afpFile.setUpdateDate(today);	
				afpFile.setPackTime(today);
				afpFile.setPackIdSet(packIdSet);
				session.update(afpFile);
			}
		}
		
		

		return packStatuses;
	}
	
	
	public static synchronized List<PackStatus> preparePack(Session session, List<AfpFile> afpFiles, Date cycleDate, HashMap<String, Area> areaMap, List<ApplyData> ads, List<ApplyData> errs) throws Exception{
		double paperWg = 0;					
		if(Constant.getWeightMap() != null){
			if(Constant.getWeightMap().get("內頁紙張") != null){
				paperWg = Constant.getWeightMap().get("內頁紙張");
			}
		}
		List<String> newBatchNames = new ArrayList<String>();
		List<String> receiptBatchNames = new ArrayList<String>();
		List<String> cardBatchNames = new ArrayList<String>();
		String center = null;
		String batchOrOnline = null;
		for(AfpFile afpFile : afpFiles){
			center = afpFile.getCenter();
			batchOrOnline = afpFile.getBatchOrOnline();
			newBatchNames.add(afpFile.getNewBatchName());
			if(afpFile.getNewBatchName().startsWith("CA")){
				receiptBatchNames.add(afpFile.getNewBatchName().replaceFirst("CA", "SG"));
			}else if(afpFile.getNewBatchName().startsWith("GA")){
				receiptBatchNames.add(afpFile.getNewBatchName().replaceFirst("GA", "GG"));
				cardBatchNames.add(afpFile.getNewBatchName().replaceFirst("GA", "PD"));
			}
		}
		Date today = new Date();
		String cycleStr = Constant.yyMMdd.format(cycleDate);
		
		//服務中心的地址對照表
		HashMap<String, Area> centerMap = new HashMap<String, Area>();
		List<Area> serviceCenters = session.createQuery("from Area where areaId in ( select distinct serviceCenter from Area ) and address is not null and address <> ''").list();
		if(serviceCenters != null){
			for(Area area : serviceCenters){
				centerMap.put(area.getAreaId(), area);
			}
		}
		if(areaMap == null){
		   //找出 area的address
		   List<Area> normalAreas =  session.getNamedQuery("Area.findHaveAddress").list();		   
		   areaMap = new HashMap<String, Area>();				
		   for(Area area : normalAreas){
			    areaMap.put(area.getAreaId(), area);
				Area mapArea = areaMap.get(area.getSubAreaId());
			    String address = null;
			    // subAreaId有重覆，只放入第一個抓到的
			    if(mapArea == null ){
			    	areaMap.put(area.getSubAreaId(), area);
			    	address = area.getAddress();
			    }else{
			    	address = mapArea.getAddress();
			    }	
			    //如果放入的地址是空的，再放一次
			    if(address == null || address.trim().equals("") || address.indexOf("無地址") >= 0){
			    	areaMap.put(area.getSubAreaId(), area);
			    }
			}
		    areaMap.putAll(centerMap);
		} 		
		/*
		 * 寫死的審查科對映table，如果有更改就必須hard code更動
		 */
		List<Area> auditCenters = session.getNamedQuery("Area.findHaveAddressAndAudit").list();
		
		HashMap<String, Area> auditMap = new HashMap<String, Area>();
		for(Area audit : auditCenters){
			if(audit.getAreaName() != null && audit.getAreaName().indexOf("高、審查科") >= 0){
				auditMap.put("03", audit);
			}else if(audit.getAreaName() != null && audit.getAreaName().indexOf("中、審查科") >= 0){
				auditMap.put("02", audit);
			}else if(audit.getAreaName() != null && audit.getAreaName().indexOf("南、審查科") >= 0){
				auditMap.put("04", audit);
			}else if(audit.getAreaName() != null && audit.getAreaName().indexOf("北一審查科") >= 0){
				auditMap.put("01", audit);
			}else if(audit.getAreaName() != null && audit.getAreaName().indexOf("桃、審查科") >= 0){
				auditMap.put("05", audit);
			}else if(audit.getAreaName() != null && audit.getAreaName().indexOf("北二審查科") >= 0){
				auditMap.put("06", audit);
			}
		}
		
		Set<String> newBatchNameSet = new HashSet<String>(); 
		
		//找出正常完成的件，只挑出驗單完成或配表完成的，而且沒有錯誤，沒有抽件，還沒被分配到packId的保單
		
		List<Object[]> list = null;		
		
		list = session.getNamedQuery("ApplyData.findUnpack2").setParameterList("newBatchNames", newBatchNames).list();
		
		List<PackStatus> retList = new ArrayList<PackStatus>();
		//以下開始處理正常件
		//在傳入的ads和errs都是null時，或ads不是null時進入
		if(list != null && list.size() > 0 
				&& ((ads == null && errs == null) || ads != null )){
			//找出subAreaIds的集合
			Set<String> subAreaIds = new HashSet<String>();
			int size = list.size();
			for(int i = size -1 ; i >= 0 ; i--){
				Object[] result = list.get(i);
				String subAreaId = (String)result[0];
				if(ads == null){
				   subAreaIds.add(subAreaId);
				}else{
				   //如果有傳入applyData，要產生特定的packStatus時，把不符的結果移除
				   boolean matched = false;
				   for(ApplyData ad : ads){
					   if(ad.getSubAreaId() != null && ad.getSubAreaId().equals(subAreaId)){
						   subAreaIds.add(subAreaId);
						   matched = true;
					   }
				   }	
				   if(!matched)
					   list.remove(i);
				}
			}
			//查出每個subAreaId裡的保單				
			List<ApplyData> applyDatas = new ArrayList<ApplyData>();
			if(errs == null && ads == null)
			   applyDatas = session.getNamedQuery("ApplyData.findForPack").setParameterList("newBatchNames", newBatchNames).list();
			else if(ads != null)
			   applyDatas = ads;
			//查出簽收單 
			//3.查出所有的簽收單
			List<ApplyData> receipts2 = session.getNamedQuery("ApplyData.findInNewBatchName").setParameterList("newBatchNames", receiptBatchNames).list();
			List<ApplyData> cards = session.getNamedQuery("ApplyData.findInNewBatchName").setParameterList("newBatchNames", cardBatchNames).list();
			List<ApplyData> receipts = new ArrayList<ApplyData>();
			for(ApplyData applyData : applyDatas){
				for(ApplyData receipt : receipts2){
					if(applyData.getApplyNo().equals(receipt.getApplyNo()) 
							&& applyData.getPolicyNos().equals(receipt.getPolicyNos()) && 
							applyData.getReprint() != null && receipt.getReprint() != null 
							&& applyData.getReprint().intValue() == receipt.getReprint().intValue()){
						receipts.add(receipt);
						break;
					}
				}
			}
			
			
			// packid規則 yyMMddSubAreaId00n
			for(Object[] result : list){				
				String subAreaId = (String)result[0];
				Integer count = new Integer(result[1] == null ? "0" : result[1].toString());
				String packId = cycleStr + subAreaId.toUpperCase();
				Criteria criteria = session.createCriteria(PackStatus.class);
				criteria.add(Restrictions.like("packId", packId + "%")).addOrder(Order.desc("packId"));
				List<PackStatus> packs = session.getNamedQuery("PackStatus.findPkLike").setString(0, packId + "%").list();
				
				
				PackStatus pack = new PackStatus();
				// 決定PK
				if(packId.length() == 13){
					//長度如果是13時，代表本身就是服務中心(cycleDate length 6 + areaId 7)
					if(packs != null && packs.size() > 0){
					    packId = packId + (packs.size() + 1);
					}else{
					    packId = packId + "1";
					}
				}else if(packs != null && packs.size() > 0){
				    packId = packId + StringUtils.leftPad((packs.size() + 1) + "", 3, '0');
				}else{
				    packId = packId + "001";
				}
				
				pack.setPackId(packId);	
				pack.setBatchOrOnline(batchOrOnline);
				pack.setBack(false);
				pack.setCenter(center);
				pack.setCreateDate(today);
				pack.setUpdateDate(today);
				pack.setCycleDate(cycleDate);
				pack.setStatus(0);
				pack.setStatusNm("裝箱準備");
				pack.setSubAreaId(subAreaId);
				if(areaMap.get(subAreaId) != null){
				   pack.setSubAreaName(areaMap.get(subAreaId).getAreaName());
				}else{
				   Criteria cri = session.createCriteria(Area.class);
				   cri.add(Restrictions.eq("subAreaId", subAreaId ));
				   List<Area> areas = cri.list();
				   if(areas != null && areas.size() > 0){
					   pack.setSubAreaName(areas.get(0).getAreaName());
					   areaMap.put(subAreaId, areas.get(0)); //把它塞回去areaMap
					   if(areaMap.get(subAreaId) != null)
					      pack.setSubAreaName(areaMap.get(subAreaId).getAreaName());
				   }else{
					   cri = session.createCriteria(Area.class);
					   cri.add(Restrictions.like("areaId", subAreaId + "%"));
					   areas = cri.list();					   
					   if(areas != null && areas.size() > 0){
						   pack.setSubAreaName(areas.get(0).getAreaName());
						   areaMap.put(subAreaId, areas.get(0)); //把它塞回去areaMap
						   if(areaMap.get(subAreaId) != null)
						      pack.setSubAreaName(areaMap.get(subAreaId).getAreaName());
					   }else{
						   areaMap.put(subAreaId, null); //把它塞回去areaMap
						   pack.setSubAreaName("Table中找不到此服務中心");
					   }   
				   }
				   
				}
					
				
				//如果有服務中心，地址設為服務中心
				//沒有，則設為此areaId的地址
		        String serviceCenter = null;
		        if(areaMap.get(subAreaId) != null)
		        	serviceCenter = areaMap.get(subAreaId).getServiceCenter();
		        String address = "無地址";
		        String zipCode = "";
		        
		      //如果有服務中心，就設定服務中心地址
		        String serviceCenterNm = null;
		        if(serviceCenter != null && !serviceCenter.trim().equals("") && centerMap.get(serviceCenter) != null){
		        	address = centerMap.get(serviceCenter).getAddress();
		        	zipCode = centerMap.get(serviceCenter).getZipCode();
		        	serviceCenterNm = (centerMap.get(serviceCenter).getAreaName() == null || centerMap.get(serviceCenter).getAreaName().trim().equals(""))?  serviceCenter : centerMap.get(serviceCenter).getAreaName();
		        }
		        
		        
		        //找不到服務中心時，把自己設為服務中心
		        if(serviceCenterNm == null){		        	
		        	serviceCenterNm = (subAreaId == null || centerMap.get(subAreaId) == null || centerMap.get(subAreaId).getAreaName() == null || centerMap.get(subAreaId).getAreaName().trim().equals(""))?  null : centerMap.get(subAreaId).getAreaName();
		        	if(serviceCenterNm == null)
		        	   serviceCenterNm = subAreaId;		        	
		        }
		         
		        if(address == null || "".equals(address.trim()) || address.indexOf("無地址") >= 0){
		        	logger.info("get address directly");
		        	//如果找不到地址，試看看從area拿
		        	if(areaMap.get(subAreaId) != null){
			        	address = areaMap.get(subAreaId).getAddress();
			        	zipCode = areaMap.get(subAreaId).getZipCode();
		        	}	
		        	
		        	//如果還是沒有，最後手段，那就直接query
		        	if(address == null || address.indexOf("無地址") >= 0 || "".equals(address.trim())){
		        		if(subAreaId != null && subAreaId.length() >= 4 ){
				        	//有可能是更新時間不同，造成兩邊不一致的狀態
				        	List<Area> areas = session.createQuery("from Area where areaId like '" + subAreaId + "%' order by areaId ").list();
				        	if(areas != null && areas.size() > 0){
				        	    Area area = areas.get(0);
				        	    serviceCenter = area.getServiceCenter();
				        	    if(subAreaId.length() == 7){
				        	    	//subAreaId長度為7時代表是從自己拿地址
				        	    	address = area.getAddress();
					        	    zipCode = area.getZipCode();
						        	serviceCenterNm = (area.getAreaName() == null || area.getAreaName().equals(""))? null : area.getAreaName();
						        	//如果找不到時，看看是不是有serviceCenter可以找
						        	if(address == null || address.indexOf("無地址") >= 0 || "".equals(address.trim()) 
						        			&& serviceCenter != null && centerMap.get(serviceCenter) != null){
						        		address = centerMap.get(serviceCenter).getAddress();
						        	    zipCode = centerMap.get(serviceCenter).getZipCode();
					        	    }
				        	    }else if(serviceCenter != null && !"".equals(serviceCenter) && centerMap.get(serviceCenter) != null){
				        	    	//如果長度不為7時，且有serviceCenter，先找serviceCenter
				        	    	address = centerMap.get(serviceCenter).getAddress();
					        	    zipCode = centerMap.get(serviceCenter).getZipCode();					        	    
					        	    serviceCenterNm = (centerMap.get(serviceCenter).getAreaName() == null || centerMap.get(serviceCenter).getAreaName().trim().equals(""))?  serviceCenter : centerMap.get(serviceCenter).getAreaName();
				        	    }else{
				        	    	//如果長度不為7時，且沒有serviceCenter時，找自己
				        	    	address = area.getAddress();
					        	    zipCode = area.getZipCode();
				        	    }
				        	    
				        	    //如果經歷這些還是找不到，就是真的找不到
				        	    if(address == null || address.indexOf("無地址") >= 0 || "".equals(address.trim())){
				        	    	address = "無法由" + subAreaId + "找到寄送地址";
				        	    }
				        	}else{
				        		//如果無法直接由subAreaId查到Area，，絕對是錯誤狀態
				        		address = "無法由" + subAreaId + "找到寄送地址";
				        	}
				        }else{
				        	//subAreaId 小於4 ，或subAreaId == null,應該不可能發生，絕對是錯誤狀態
		        		    address = "無法由" + subAreaId + "找到寄送地址";
				        }
		        	}
		        }
		        /*
		        if(serviceCenter == null || serviceCenter.trim().equals("")){
		        	if(areaMap.get(subAreaId) != null)
		        	   address = areaMap.get(subAreaId).getAddress();
		        	
		        }else{		        	
		        	address = centerMap.get(serviceCenter);
		        }
		        */
                pack.setServiceCenterNm(serviceCenterNm);                
				pack.setAreaAddress(address);	
				pack.setZipCode(zipCode);
				if(areaMap.get(subAreaId) != null)
				   pack.setSubAreaTel(areaMap.get(subAreaId).getTel());
				int packCount = pack.getBooks();
				pack.setBooks(count + packCount);
			    session.save(pack);

				if(applyDatas != null && list.size() > 0){
					//和此pack有關的列印檔
					Set<String> printFiles = new LinkedHashSet<String>();
					int receiptsCounter = 0;
					int cardCounter = 0;
					TreeSet<String> ts = new TreeSet<String>();
					double totalWeight = 0;
					for (ApplyData applyData : applyDatas) {
						if (subAreaId != null
								&& subAreaId.equals(applyData.getSubAreaId())) {

							if (applyData.getUniqueNo() != null
									&& !applyData.getUniqueNo().trim()
											.equals("")) {
								ts.add(applyData.getUniqueNo());
							}
							totalWeight += applyData.getWeight() == null? 0 : applyData.getWeight();
							applyData.setUpdateDate(today);
							applyData.setPackId(packId);
							String newBatchNm = applyData.getNewBatchName();
							// 找出對應的簽收單
							for (ApplyData receipt : receipts) {
								if (receipt.getSubAreaId() != null
										&& subAreaId.equals(receipt
												.getSubAreaId())) {
									if (applyData.getPolicyNos().trim().equals(receipt.getPolicyNos().trim())) {
										receiptsCounter++;
										totalWeight += receipt.getWeight() == null? 0 : receipt.getWeight();
										receipt.setUpdateDate(today);
										receipt.setPackId(packId);
										receipt.setPolicyStatus("60"); // 設定成裝箱中
										printFiles.add(receipt
												.getNewBatchName());
										session.update(receipt);
									}
								}
							}
							//團險證 
							for (ApplyData card : cards) {
								if (card.getSubAreaId() != null
										&& subAreaId.equals(card
												.getSubAreaId())) {
									if (applyData.getPolicyNos().trim().equals(card.getPolicyNos().trim())) {
										cardCounter ++;
										totalWeight += card.getWeight() == null? 0 : card.getWeight();
										card.setUpdateDate(today);
										card.setPackId(packId);
										card.setPolicyStatus("60"); // 設定成裝箱中
										printFiles.add(card
												.getNewBatchName());
										session.update(card);
									}
								}
							}
							
							if (newBatchNm != null) {
								printFiles.add(newBatchNm);
								applyData.setPolicyStatus("60"); // 設定成裝箱中
							}
							session.update(applyData);
						}
					}
					pack.setWeight(totalWeight + paperWg);
					pack.setReceipts(receiptsCounter);
					pack.setInusreCard(cardCounter);
					Set<String> allSet = pack.getNewBatchNmSet();
					allSet.addAll(printFiles);
					if(ts.size() > 0)
						pack.setFirstUniqueNo(ts.first());
					pack.setNewBatchNmSet(allSet);
					newBatchNameSet.addAll(allSet);
					session.update(pack);
				}
				retList.add(pack);
			}						
		}//正常件處理結束
		
		
		//開始處理異常件及抽件
		//找出此轄區所有錯誤件，頁數必須大於0，代表pres有送出，不分保單簽收單一次全查出來
		List<ApplyData> errors = null;		
		List<String> allNewBatchNames = new ArrayList<String>();
		allNewBatchNames.addAll(newBatchNames);
		allNewBatchNames.addAll(receiptBatchNames);
		allNewBatchNames.addAll(cardBatchNames);
		if(errs == null && ads == null)
		   errors = session.getNamedQuery("ApplyData.findErrorForPack").setParameterList("newBatchNames", allNewBatchNames).list();
		else if(errs != null)
		   errors = errs;
		
		if(errors != null && errors.size() > 0){
			//退件的名稱為yyyyMMdd轄區ERR00n
			String packId = cycleStr + center + "ERR";
			Criteria criteria = session.createCriteria(PackStatus.class);
			criteria.add(Restrictions.like("packId", packId + "%")).addOrder(Order.desc("packId"));
			List<PackStatus> packs = criteria.list();
			PackStatus pack = new PackStatus();
			LogisticStatus ls = null;
			
			// 決定PK
			if(packs == null || packs.size() == 0){
				packId = packId + "001";
				pack.setPackId(packId);				
			}else{				
				//如果此pack已交寄就不合併，另起新的
				if( pack.getStatus() == 50){					
					int serialNo = new Integer(pack.getPackId().substring(pack.getPackId().length() - 3)) + 1;
					pack = new PackStatus();
					packId = packId + StringUtils.leftPad(serialNo + "", 3, '0');
					pack.setPackId(packId);
				}else{
					pack = packs.get(0); 
					packId = pack.getPackId(); 
				}					
			}
			if(packId != null){
				ls = (LogisticStatus) session.get(LogisticStatus.class, packId);
			}
			
			if(ls == null){
				ls = new LogisticStatus();
			}
			
			
			pack.setBack(true);
			pack.setCenter(center);
			
			Area auditArea = auditMap.get(center);
			
			pack.setSubAreaId(auditArea.getSubAreaId());
			pack.setSubAreaName(auditArea.getAreaName());
			
			pack.setAreaAddress(auditArea.getAddress());
			pack.setSubAreaTel(auditArea.getTel());
			pack.setBatchOrOnline(batchOrOnline);
			pack.setServiceCenterNm(auditArea.getAreaName());
			
			pack.setCreateDate(today);
			pack.setUpdateDate(today);
			pack.setLabelScanDate(today);
			pack.setLabelScanUser("SYSTEM");
			pack.setPackCompleted(true);
			pack.setReceiptScanDate(today);
			pack.setReceiptScanUser("SYSTEM");
			pack.setPolicyScanDate(today);
			pack.setPolicyScanUser("SYSTEM");
			pack.setCycleDate(cycleDate);
			pack.setLogisticId(packId);
			pack.setStatus(45);
			pack.setStatusNm("等待貨運");			
			session.saveOrUpdate(pack); //儲存好packstatus
			Set<String> printFiles = new LinkedHashSet<String>();
			int counter = 0;
			int receiptsCounter = 0;
			int insureCardsCounter = 0;
			double totalWeight = 0;
			for(ApplyData errData : errors){
				//計算保單數
				if((errData.getReceipt() == null || !errData.getReceipt())  
						&& (errData.getGroupInsure() == null || !errData.getGroupInsure()))
					counter++;
				else if(errData.getReceipt() != null && errData.getReceipt())
					receiptsCounter++;
				if(errData.getGroupInsure() != null && errData.getGroupInsure())
					insureCardsCounter++;
				printFiles.add(errData.getNewBatchName().trim());
				errData.setPackId(packId); //放在那一箱中				
				errData.setPolicyStatus("98"); //退件立刻設定為交寄完成
				errData.setUpdateDate(today);
				totalWeight += errData.getWeight() == null? 0 : errData.getWeight();
				session.update(errData);				
			}
            pack.setWeight(totalWeight + paperWg);
			pack.setBooks(counter);
			pack.setReceipts(receiptsCounter);
			pack.setInusreCard(insureCardsCounter);
			Set<String> allSet = pack.getNewBatchNmSet();
			allSet.addAll(printFiles);
			pack.setNewBatchNmSet(allSet); //設定相關的設定檔
			newBatchNameSet.addAll(allSet);			
			session.update(pack);
			
			Set<PackStatus> pss = new HashSet<PackStatus>();
			pss.add(pack);
			
			ls.setWeight(pack.getWeight());
			ls.setLogisticId(packId);
			ls.setAddress(auditArea.getAddress());
			ls.setBatchOrOnline(batchOrOnline);
			ls.setBooks(counter);
			ls.setCenter(center);
			ls.setCycleDate(cycleDate);
			ls.setMailReceipt(false);
			ls.setName(auditArea.getAreaName());
			ls.setPackDone(true);
			ls.setPackStatuses(pss);
			ls.setPacks(1);
			ls.setReceipts(receiptsCounter);
			ls.setScanDate(today);
			ls.setSentTime(cycleDate);
			ls.setTel(auditArea.getTel());
			session.saveOrUpdate(ls);			
			
			
			retList.add(pack);			
		}						
		if(cardBatchNames.size() > 0)
		   newBatchNameSet.addAll(cardBatchNames);
        Query query = session.getNamedQuery("ApplyData.findPackIdByNewBatchName");
		//更新AfpFile的狀態
		for(String newBatchName : newBatchNameSet){
			AfpFile afpFile = (AfpFile) session.get(AfpFile.class, newBatchName);
			List<String> packIds = query.setString(0, newBatchName).list();
			if(afpFile != null && packIds != null){
				afpFile.setPackIdSet(null);
				Set<String> packIdSet = afpFile.getPackIdSet();
				for(String packId: packIds){
				    packIdSet.add(packId);
				}
				afpFile.setStatus("裝箱中");
				afpFile.setUpdateDate(today);				
				afpFile.setPackIdSet(packIdSet);
				session.update(afpFile);
			}
		}
		
		 

		return retList;
	}
	
	public static String updateVip(String oldBatchName, boolean vip, boolean substract, AdminUser user) throws ParseException{
		String updateStatus = null;
		Session session = null;
		Transaction tx = null;
		
		Date today = new Date();
		try{			
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			//ApplyData.updateNewBatchNo, query="update ApplyData set newBatchNo = ?, vipModifierId = ?, vipModifierName = ? , vipModifierTime = ? where newBatchName = ? "
			Query updateBatchNo = session.getNamedQuery("ApplyData.updateNewBatchNo");			
			ApplyData applyData = (ApplyData) session.get(ApplyData.class, oldBatchName);
			String conjugateNewBatchNm = applyData.getNewBatchName();
			String groupInsureNm = null;
			if(conjugateNewBatchNm.toUpperCase().startsWith("GA")){
				conjugateNewBatchNm = "GG" + conjugateNewBatchNm.substring(2);
				groupInsureNm = "PD" + conjugateNewBatchNm.substring(2);
			}else if(conjugateNewBatchNm.toUpperCase().startsWith("CA")){
				conjugateNewBatchNm = "SG" + conjugateNewBatchNm.substring(2);
			}else if(conjugateNewBatchNm.toUpperCase().startsWith("GG")){
				conjugateNewBatchNm = "GA" + conjugateNewBatchNm.substring(2);
				groupInsureNm = "PD" + conjugateNewBatchNm.substring(2);
			}else if(conjugateNewBatchNm.toUpperCase().startsWith("SG")){
				conjugateNewBatchNm = "CA" + conjugateNewBatchNm.substring(2);				
			}
			Integer policyStatus = applyData.getPolicyStatus() == null? 0 : new Integer(applyData.getPolicyStatus()); 
			if(policyStatus >= 40 && vip){
				return "已進入驗單或之後的階段，無法設定VIP，請直接與客服人員聯絡";
			}
			if(policyStatus >= 60 ){
				return "已裝箱，無法設定抽件，請直接與客服人員聯絡";
			}
			if(policyStatus == 28 || (applyData.getNewBatchName() != null && applyData.getNewBatchName().endsWith("9999"))){
				return "免印製無法設定抽件或VIP";
			}
			if(applyData == null)
				return null;	
			//更新applyData
			applyData.setVip(vip);
			applyData.setSubstract(substract);
			if(substract){
				applyData.setVerifyResult(user.getUserName() + "設定抽件");
			}
			if(vip){
			   applyData.setVipModifierId(user.getUserId());
			   applyData.setVipModifierName(user.getUserName());
			   applyData.setVipModifierTime(today);
			}else{
			   applyData.setVipModifierId("");
			   applyData.setVipModifierName("");
			   applyData.setVipModifierTime(null);
			}
			
			//更新afpFile的順序
			AfpFile afpFile = applyData.getAfpFile();
			Long newBatchNo = 0L;
			if(afpFile != null){			   
			   if(vip){
			      afpFile.setVipModifierId(user.getUserId());
			      afpFile.setVipModifierName(user.getUserName());
			      afpFile.setVipSetTime(today);
			   }else{
				  newBatchNo = afpFile.getSerialNo();
			      afpFile.setVipModifierId("");
			      afpFile.setVipModifierName("");
			      afpFile.setVipSetTime(null);				  
			   }
			   afpFile.setNewBatchNo(newBatchNo);
			   afpFile.setUpdateDate(today);
			   session.update(afpFile);
			}else if(vip){
				return "尚未產生列印檔，無法調整順序";
			}
			if(substract){
			   applyData.setSubstractModifiderId(user.getUserId());
			   applyData.setSubstractModifiderName(user.getUserName());
			   applyData.setSubstractModifiderTime(today);
			   applyData.setExceptionStatus("41");
			   applyData.setVerifyResult("抽件");
			}else{				
			   applyData.setSubstractModifiderId("");
			   applyData.setSubstractModifiderName("");
			   applyData.setSubstractModifiderTime(null);
			   if("41".equals(applyData.getExceptionStatus()) && "抽件".equals(applyData.getVerifyResult())){
				   applyData.setExceptionStatus(null);
				   applyData.setVerifyResult(null);
			   }
			}
			applyData.setUpdateDate(today);
			session.update(applyData);
			
			//全部相關的都要updat回去
			if(afpFile != null){
			   updateBatchNo.setLong(0, newBatchNo);
			   if(vip){
			      updateBatchNo.setString(1, user.getUserId());
			      updateBatchNo.setString(2, user.getUserName());
			      updateBatchNo.setParameter(3, today);
			   }else{
				  updateBatchNo.setString(1, "");
				  updateBatchNo.setString(2, "");
				  updateBatchNo.setDate(3, null);
			   }			   
			   updateBatchNo.setParameter(4, today);
			   updateBatchNo.setString(5, afpFile.getNewBatchName());
			   updateBatchNo.executeUpdate(); //更新所有和afpFile相關的ApplyData
			}
			
			String applyNo = applyData.getApplyNo();	
			
			String policyNo = null;
			if(applyData.getPolicyNos() != null && applyData.getPolicyNoSet().size() > 0){
				for(String str : applyData.getPolicyNoSet()){
					policyNo = str;
					break;
				}
			}
			  
			//同時更新簽收回條及團險證
			if(applyNo != null && !applyNo.equals("")){
				Criteria criteria = session.createCriteria(ApplyData.class);
			    criteria.add(Restrictions.eq("applyNo", applyNo ));
			    criteria.add(Restrictions.like("policyNos", "%," + policyNo + ",%"));
			    criteria.add(Restrictions.eq("cycleDate", applyData.getCycleDate()));
			    criteria.add(Restrictions.eq("reprint", applyData.getReprint()));
			    if(groupInsureNm == null){
			    	criteria.add(Restrictions.eq("newBatchName", conjugateNewBatchNm));
			    }else{
			    	criteria.add(Restrictions.or(Restrictions.eq("newBatchName", conjugateNewBatchNm), Restrictions.eq("newBatchName", groupInsureNm)));
			    }
			    criteria.add(Restrictions.ne("policyStatus", "98"));
			    criteria.add(Restrictions.ne("policyStatus", "97"));
			    criteria.add(Restrictions.and(Restrictions.ne("policyStatus", "100"), Restrictions.ne("policyStatus", "28"))); //已交寄或免印製不更新
			    List<ApplyData> list = criteria.list();

			    if(list != null){
			    	for(ApplyData receiptData: list){
			    		receiptData.setVip(vip);
						receiptData.setSubstract(substract);
					    if(vip){
						   receiptData.setVipModifierId(user.getUserId());
						   receiptData.setVipModifierName(user.getUserName());
						   receiptData.setVipModifierTime(today);
					    }else{
					       receiptData.setVipModifierId("");
						   receiptData.setVipModifierName("");
						   receiptData.setVipModifierTime(null);
					    }						
						AfpFile receiptAfpFile = receiptData.getAfpFile();
						newBatchNo = 0L;
						if(receiptAfpFile != null){
							//非vip時回復原本的順序
							if(vip){
							   receiptAfpFile.setVipModifierId(user.getUserId());
							   receiptAfpFile.setVipModifierName(user.getUserName());
							   receiptAfpFile.setVipSetTime(today);
							}else{
							   newBatchNo = receiptAfpFile.getSerialNo() == null? 1 : receiptAfpFile.getSerialNo();
							   receiptAfpFile.setVipModifierId("");
							   receiptAfpFile.setVipModifierName("");
							   receiptAfpFile.setVipSetTime(null);
							}
							receiptAfpFile.setNewBatchNo(newBatchNo);							
							receiptAfpFile.setUpdateDate(today);
							session.update(receiptAfpFile);
						}					
						if(substract){
						   receiptData.setSubstractModifiderId(user.getUserId());
						   receiptData.setSubstractModifiderName(user.getUserName());
						   receiptData.setSubstractModifiderTime(today);
						   receiptData.setExceptionStatus("41");
						   receiptData.setVerifyResult("抽件");
						}else{
						   receiptData.setSubstractModifiderId("");
						   receiptData.setSubstractModifiderName("");
						   receiptData.setSubstractModifiderTime(null);
						   if("41".equals(receiptData.getExceptionStatus()) && "抽件".equals(receiptData.getVerifyResult())){
							   receiptData.setExceptionStatus(null);
							   receiptData.setVerifyResult(null);
						   }
						}
						receiptData.setUpdateDate(today);
						session.update(receiptData);
						if(receiptAfpFile != null){
						   updateBatchNo.setLong(0, newBatchNo);
						   if(vip){
						      updateBatchNo.setString(1, user.getUserId());
						      updateBatchNo.setString(2, user.getUserName());
						      updateBatchNo.setParameter(3, today);
						   }else{
							  updateBatchNo.setString(1, "");
							  updateBatchNo.setString(2, "");
							  updateBatchNo.setDate(3, null);
						   }
						   updateBatchNo.setParameter(4, today);
						   updateBatchNo.setString(5, receiptAfpFile.getNewBatchName());
						   updateBatchNo.executeUpdate(); //更新所有和afpFile相關的ApplyData
						}
			    	}
			    }
			}
			tx.commit();
			updateStatus = "更新成功";
		}catch(Exception e){
			if(tx != null )
				tx.rollback();
			updateStatus = "儲存失敗:" + e.getMessage(); 
			logger.error("", e);
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		
		return updateStatus;		
	}
	
	public static List<ApplyData> getApplyData(Date startDate, Date endDate, 
			String center, String applyNo, String policyNo, String oldBatchName, 
			String policyStatus, String insureId, String recName, String areaId, Integer beginPage, Integer rowNum, String[] orderColumns, String asc, 
			Boolean exception, String statusBefore, String sourceCode, String receipt, String statusAfter, Boolean groupInsure){
		List<ApplyData> list = null;
		Session session = null;
		try{			
			session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(ApplyData.class);
			if(startDate != null)
				criteria.add(Restrictions.ge("cycleDate", startDate));
			if(endDate != null)
				criteria.add(Restrictions.le("cycleDate", endDate));
			if(center != null && !center.equals(""))
				criteria.add(Restrictions.eq("center", center));
			if(applyNo != null && !"".equals(applyNo))
				criteria.add(Restrictions.like("applyNo", "%" + applyNo + "%"));
			if(policyNo != null && !"".equals(policyNo))
				criteria.add(Restrictions.like("policyNos", ",%" + policyNo + "%,"));
			if(oldBatchName != null && !"".equals(oldBatchName))
				criteria.add(Restrictions.like("oldBatchName", "%" + oldBatchName + "%"));
			if(policyStatus != null && !"".equals(policyStatus))
				criteria.add(Restrictions.eq("policyStatus", policyStatus));
			if(insureId != null && !"".equals(insureId))
				criteria.add(Restrictions.like("insureId", "%" + insureId + "%"));
			if(recName != null && !"".equals(recName))
				criteria.add(Restrictions.like("recName", "%" + recName + "%"));

			if(areaId != null && !"".equals(areaId))
				criteria.add(Restrictions.like("areaId", "%" + areaId + "%"));
			if(exception != null && exception){
				criteria.add(Restrictions.and(Restrictions.isNotNull("exceptionStatus"), Restrictions.ne("exceptionStatus", "")));
			}else if(exception != null && !exception) {
				criteria.add(Restrictions.or(Restrictions.isNull("exceptionStatus"), Restrictions.eq("exceptionStatus", "")));
			}
			if(statusBefore != null && !statusBefore.equals("")){
				criteria.add(Restrictions.le("policyStatus", statusBefore));
				criteria.add(Restrictions.ne("policyStatus", "100"));
			}
			if(groupInsure != null){
				criteria.add(Restrictions.eq("groupInsure", groupInsure));	
			}
			if(statusAfter != null && !statusAfter.equals("")){
				criteria.add(Restrictions.or(Restrictions.ge("policyStatus", statusAfter), Restrictions.eq("policyStatus", "100")));				
			}
			if(sourceCode != null && !"".equals(sourceCode)){
				criteria.add(Restrictions.eq("sourceCode", sourceCode));
			}
			if(receipt != null && (receipt.equals("true") || receipt.equals("false"))){
				criteria.add(Restrictions.eq("receipt", receipt.equals("true")));
			}else if(receipt != null && receipt.equals("null")){
				criteria.add(Restrictions.isNull("receipt"));
			}
			if(orderColumns == null || orderColumns.length == 0){				
			    criteria.addOrder(Order.asc("newBatchNo")).addOrder(Order.asc("vipModifierTime")).addOrder(Order.asc("newSerialNo"));
			}else{
				if(asc != null && asc.equals("asc")){
				     for(String orderColumn : orderColumns){
				    	 if(orderColumn != null)
				    	    criteria.addOrder(Order.asc(orderColumn));	 
				     }	
				     criteria.addOrder(Order.asc("newBatchNo")).addOrder(Order.asc("vipModifierTime")).addOrder(Order.asc("newSerialNo"));
				}else{
					for(String orderColumn : orderColumns){
						 if(orderColumn != null)
				    	    criteria.addOrder(Order.desc(orderColumn));
				     }	
				     criteria.addOrder(Order.asc("newBatchNo")).addOrder(Order.asc("vipModifierTime")).addOrder(Order.asc("newSerialNo"));
				}
					
			}
			
			criteria.setFirstResult(beginPage);
			criteria.setMaxResults(rowNum);
			list = criteria.list();
			
		}catch(Exception e){
			logger.error("", e);
		}finally{
			if(session != null)
				session.close();
		}
		return list;

	}
	
	public static int getApplyDataCount(Date startDate, Date endDate, 
			String center, String applyNo, String policyNo, String oldBatchName, 
			String policyStatus, String insureId,String recName, String areaId, Boolean exception, 
			String statusBefore, String sourceCode, String receipt, String statusAfter, Boolean groupInsure){
		List<ApplyData> list = null;
		Session session = null;
		try{			
			
			session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(ApplyData.class);
			if(startDate != null)
				criteria.add(Restrictions.ge("cycleDate", startDate));
			if(endDate != null)
				criteria.add(Restrictions.le("cycleDate", endDate));
			if(center != null && !center.equals(""))
				criteria.add(Restrictions.eq("center", center));
			if(applyNo != null && !"".equals(applyNo))
				criteria.add(Restrictions.like("applyNo", "%" + applyNo + "%"));
			if(policyNo != null && !"".equals(policyNo))
				criteria.add(Restrictions.like("policyNos", ",%" + policyNo + "%,"));
			if(oldBatchName != null && !"".equals(oldBatchName))
				criteria.add(Restrictions.like("oldBatchName", "%" + oldBatchName + "%"));
			if(policyStatus != null && !"".equals(policyStatus))
				criteria.add(Restrictions.eq("policyStatus", policyStatus));
			if(insureId != null && !"".equals(insureId))
				criteria.add(Restrictions.like("insureId", "%" + insureId + "%"));
			if(recName != null && !"".equals(recName))
				criteria.add(Restrictions.like("recName", "%" + recName + "%"));
			if(areaId != null && !"".equals(areaId))
				criteria.add(Restrictions.like("areaId", "%" + areaId + "%"));
			if(exception != null && exception){
				criteria.add(Restrictions.and(Restrictions.isNotNull("exceptionStatus"), Restrictions.ne("exceptionStatus", "")));
			}else if(exception != null && !exception) {
				criteria.add(Restrictions.or(Restrictions.isNull("exceptionStatus"), Restrictions.eq("exceptionStatus", "")));
			}
			if(groupInsure != null){
				criteria.add(Restrictions.eq("groupInsure", groupInsure));
			}
			if(statusBefore != null){
				criteria.add(Restrictions.le("policyStatus", statusBefore));
			}
			if(statusAfter != null && !statusAfter.equals("")){
				criteria.add(Restrictions.or(Restrictions.ge("policyStatus", statusAfter), Restrictions.eq("policyStatus", "100")));				
			}
			if(sourceCode != null && !"".equals(sourceCode)){
				criteria.add(Restrictions.eq("sourceCode", sourceCode));
			}
			if(receipt != null && (receipt.equals("true") || receipt.equals("false"))){
				criteria.add(Restrictions.eq("receipt", receipt.equals("true")));
			}else if(receipt != null && receipt.equals("null")){
				criteria.add(Restrictions.isNull("receipt"));
			}
			int totalResult = ((Number)criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();	
			return totalResult;	
		}catch(Exception e){
			logger.error("", e);
		}finally{
			if(session != null)
				session.close();
		}
		return 0;

	}
	
	//目前只有北二能用
	public static void delegatedBack(Date today, Session session, Transaction tx,
			String[] oldBatchNameArr, String batchOrOnline, String center, Area auditArea, AdminUser user, String substractModifiderName) throws Exception {
			List<String> receiptNames = new ArrayList<String>();
			for(String policyName : oldBatchNameArr){
				if(policyName.toUpperCase().indexOf("_GROUP_") < 0){
					receiptNames.add(policyName.replaceAll("保單", "簽收回條"));
				}else if(policyName.toUpperCase().startsWith("GA")){
					String oldBatchName = policyName.replaceAll("_policy_", "_sign_");				    	
					oldBatchName = oldBatchName.replaceAll("_POLICY_", "_SIGN_");
					oldBatchName = oldBatchName.replaceAll("_pl_", "_si_");
					oldBatchName = oldBatchName.replaceAll("_PL_", "_SI_");
					receiptNames.add(oldBatchName);
				}
				
			}
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			
			if(auditArea == null){
				List<Area> auditCenters = session.getNamedQuery("Area.findHaveAddressAndAudit").list();	
    		   for(Area audit : auditCenters){
			       if(audit.getAreaName() != null && audit.getAreaName().indexOf("北二審查科") >= 0){
				        auditArea = audit;
				   }
			   }
			}
			Query query = session.createQuery("from ApplyData where oldBatchName in (:oldBatchNames) and newBatchName is not null and newBatchName not like '%9999' ");
			List<ApplyData> applyDatas = query.setParameterList("oldBatchNames", oldBatchNameArr).list();
			List<ApplyData> receiptApps = query.setParameterList("oldBatchNames", receiptNames).list();			 
			if(applyDatas != null && applyDatas.size() > 0){
				for(ApplyData applyData : applyDatas){
					
					
					String newBatchName = applyData.getNewBatchName();
                    String firstTwoLetters = newBatchName.substring(0, 2).toUpperCase();
                    String remainedLetters = newBatchName.substring(2).toUpperCase();
                    String cardNewName = null;
                    String receiptNewName = null;
                    switch(firstTwoLetters){
                        case "CA":
                        	receiptNewName = "SG" + remainedLetters;
                        	break;
                        case "GA":
                        	receiptNewName = "GG" + remainedLetters;
                        	cardNewName = "PD" + remainedLetters;
                        	break;
                    
                    }
                    ApplyData cardAppData = null;
                    if(cardNewName != null){
                       Query query1 = session.getNamedQuery("ApplyData.findByPolicyNoAndNewBatchName");                    
                       List<ApplyData> cards = query1.setString(0, applyData.getPolicyNos()).setString(1, cardNewName).list();
                    
                       if(cards != null && cards.size() > 0)
                    	   cardAppData = cards.get(0);
                    }
                    
        			List<PackStatus> pss =  session.createQuery("from PackStatus where back = true and batchOrOnline = '" + batchOrOnline + "' and cycleDate = ? and center = '" + center + "'" )
        					.setParameter(0, applyData.getCycleDate()).list();
        			PackStatus ps = new PackStatus();
        			ps.setCreateDate(today);
        			LogisticStatus ls = new LogisticStatus();
        			if(pss != null && pss.size() > 0){
        				ps = pss.get(0);
        				if(ps.getLogisticStatus() != null)
        				   ls = ps.getLogisticStatus();
        			}	                        
        			int adCount = ps.getBooks() + 1;
        			
        			ps.setBooks(adCount); //設定
        			ps.setCenter(center);
        			String firstUniqueNo = applyData.getUniqueNo();
        			if(firstUniqueNo != null && ps.getFirstUniqueNo() != null && firstUniqueNo.compareTo(ps.getFirstUniqueNo()) > 0)
        				firstUniqueNo = ps.getFirstUniqueNo();

        			ps.setFirstUniqueNo(firstUniqueNo);
        			ps.setInusreCard(null);
        			Set<String> newBatchNmSet = ps.getNewBatchNmSet();
        			newBatchNmSet.add(applyData.getNewBatchName());
        			ps.setNewBatchNmSet(newBatchNmSet);
        			int receipts = ps.getReceipts();
        			ApplyData receiptAppData = null;
        			if(receiptApps != null){
        				for(ApplyData receiptApp : receiptApps){
        					String oldBatchNm = receiptApp.getOldBatchName().replaceAll("簽收回條", "保單");
        					if(receiptApp.getSourceCode().equals("GROUP")){
        						oldBatchNm = receiptApp.getOldBatchName();
        						oldBatchNm = oldBatchNm.replaceAll("_sign_", "_policy_");				    	
        						oldBatchNm = oldBatchNm.replaceAll("_SIGN_", "_POLICY_");
        						oldBatchNm = oldBatchNm.replaceAll("_si_", "_pl_");
        						oldBatchNm = oldBatchNm.replaceAll("_SI_", "_PL_");
        					}
        					if(oldBatchNm.equals(applyData.getOldBatchName())){
        						receiptAppData = receiptApp;
        						break;
        					}
        				}
        			}
        			if(receiptAppData != null){
        				receipts += 1;        				
        			}
        			int cardsCount = ps.getInusreCard() == null? 0 : ps.getInusreCard();
        			if(cardAppData != null)
        				cardsCount += 1;
        			PackStatus oldPs = null;
        			LogisticStatus oldLs = null;
        			//舊的PackStatus和LogisticStatus調整數量
        			if(applyData.getPackId() != null && !"".equals(applyData.getPackId())){
						oldPs = (PackStatus) session.get(PackStatus.class, applyData.getPackId());
						oldLs = oldPs.getLogisticStatus();
						if(oldPs != null){
							oldPs.setBooks(oldPs.getBooks() - 1);
							if(receiptAppData != null)
								oldPs.setReceipts(oldPs.getReceipts() - 1);
							if(cardAppData != null){
								int oldCardsCount = oldPs.getInusreCard() == null? 0 : oldPs.getInusreCard();
								if(oldCardsCount != 0)
								   oldPs.setInusreCard(oldCardsCount - 1);
							}
							oldPs.setUpdateDate(today);
							session.update(oldPs);
						}
						if(oldLs != null){
							int books = oldLs.getBooks() == null? 0 : oldLs.getBooks();
							int recs = oldLs.getReceipts() == null? 0 : oldLs.getReceipts();
							if(books != 0)
							   oldLs.setBooks(books - 1);
							if(receiptAppData != null && recs != 0 )
								oldLs.setReceipts(recs - 1);
							session.update(oldLs);
						}
						
					}
        			
                    
        			ps.setReceipts(receipts);
        			ps.setReported(false);
        			ps.setSubAreaId(auditArea.getSubAreaId());
        			ps.setSubAreaName(auditArea.getAreaName());
        			ps.setUpdateDate(today);
        			ps.setZipCode(auditArea.getZipCode());
        			ps.setBack(true);
        			ps.setBatchOrOnline(batchOrOnline);
        			ps.setStatus(45);
        			ps.setCycleDate(applyData.getCycleDate());
        			ps.setStatusNm("等待貨運");
        			ps.setPackCompleted(true);
        			ps.setAreaAddress(auditArea.getZipCode() + " " + auditArea.getAddress());
					ps.setPolicyScanDate(today);
					ps.setReceiptScanDate(today);
					ps.setLabelScanDate(today);
					ps.setPolicyScanUser("SYSTEM");
					ps.setReceiptScanUser("SYSTEM");
					ps.setLabelScanUser("SYSTEM");
					ps.setInusreCard(cardsCount);
					
					ls.setTel(auditArea.getTel());
					ls.setBatchOrOnline(batchOrOnline);
					ls.setBooks(adCount);
					ls.setCenter(center);
					ls.setCycleDate(applyData.getCycleDate());
					ls.setFirstUniqueNo(firstUniqueNo);
					ls.setName(auditArea.getAreaName());
					ls.setAddress(auditArea.getZipCode() + " " + auditArea.getAddress());						
					ls.setSentTime(applyData.getCycleDate());
					ls.setPackDone(true);
					ls.setPacks(1);
					ls.setVendorId(null);
					ls.setReceipts(receipts);
					
					ls.setScanDate(today);
					ls.setMailReceipt(false);
					if(ps.getPackId() == null){
						String newPackId = null;
						if("06".equals(center) && "B".equals(batchOrOnline)){
						   query = session.createQuery("select max(packId) from PackStatus where cycleDate = ? and batchOrOnline = 'B' and center = '06'");
						   List<String> maxString =  query.setParameter(0, applyData.getCycleDate()).list();
						   if(maxString != null && maxString.size() > 0 && maxString.get(0) != null){					
							   newPackId = maxString.get(0);
							   String suffix = StringUtils.leftPad((new Integer(newPackId.substring(newPackId.length() - 4)) + 1) + "", 4 , '0');
							   newPackId = newPackId.substring(0, newPackId.length() - 4) + suffix;

						   }else{						
							   newPackId = Constant.yyMMdd.format(applyData.getCycleDate()) + "TPE" + StringUtils.leftPad("1", 4, '0');
						   }
						   ps.setPackId(newPackId);
						   ps.setLogisticId(newPackId);
						   ls.setLogisticId(newPackId);						
						}else {
						   String cycleStr = Constant.yyMMdd.format(applyData.getCycleDate());
						   newPackId = cycleStr + center + "ERR" + "001";
						   ps.setPackId(newPackId);
						   ps.setLogisticId(newPackId);
						   ls.setLogisticId(newPackId);
						}
					}
					session.saveOrUpdate(ps);
					session.saveOrUpdate(ls);
					applyData.setVerifyResult("抽件");
					applyData.setSubstract(true);
					if(applyData.getSubstractModifiderId() == null)
					   applyData.setSubstractModifiderId(user.getUserId());
					if(applyData.getSubstractModifiderName() == null && (substractModifiderName == null || substractModifiderName.trim().equals("")))
					   applyData.setSubstractModifiderName(user.getUserName());
					else if(substractModifiderName != null && !substractModifiderName.trim().equals(""))
						applyData.setSubstractModifiderName(substractModifiderName);
					if(applyData.getSubstractModifiderTime() == null)
					   applyData.setSubstractModifiderTime(today);
					applyData.setExceptionStatus("41");
					applyData.setPackId(ps.getPackId());
					
					applyData.setPolicyStatus("98");
					applyData.setUpdateDate(today);
					//applyData.setDeliverTime(today);
					if(applyData.getCycleDate() == null  || today.getTime() >= applyData.getCycleDate().getTime())
					   applyData.setDeliverTime(today);
					else
					   applyData.setDeliverTime(applyData.getCycleDate());
					session.update(applyData);						
					if(receiptAppData != null){
						receiptAppData.setPackId(ps.getPackId());
						receiptAppData.setPolicyStatus("98");
						receiptAppData.setSubstract(true);
						receiptAppData.setVerifyResult("保單抽件");
						receiptAppData.setExceptionStatus("41");
						receiptAppData.setUpdateDate(today);
					    receiptAppData.setSubstractModifiderId(applyData.getSubstractModifiderId());
					    receiptAppData.setSubstractModifiderName(applyData.getSubstractModifiderName());
						if(receiptAppData.getSubstractModifiderTime() == null)
						   receiptAppData.setSubstractModifiderTime(today);
						//receiptAppData.setDeliverTime(today);
						if(receiptAppData.getCycleDate() == null  || today.getTime() >= receiptAppData.getCycleDate().getTime())
						   receiptAppData.setDeliverTime(today);
						else
						   receiptAppData.setDeliverTime(receiptAppData.getCycleDate());
						session.update(receiptAppData);
					}
					if(cardAppData != null){
						cardAppData.setPackId(ps.getPackId());
						cardAppData.setPolicyStatus("98");
						cardAppData.setSubstract(true);
						cardAppData.setVerifyResult("保單抽件");
						cardAppData.setExceptionStatus("41");
						cardAppData.setUpdateDate(today);						
						cardAppData.setSubstractModifiderId(applyData.getSubstractModifiderId());						
						cardAppData.setSubstractModifiderName(applyData.getSubstractModifiderName());
						if(cardAppData.getSubstractModifiderTime() == null)
						   cardAppData.setSubstractModifiderTime(today);
						//cardAppData.setDeliverTime(today);
						if(cardAppData.getCycleDate() == null  || today.getTime() >= cardAppData.getCycleDate().getTime())
						   cardAppData.setDeliverTime(today);
						else
						   cardAppData.setDeliverTime(cardAppData.getCycleDate());
						session.update(cardAppData);
					}
					
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
				   if(oldPs != null){
					   List<ApplyData> list = session.createQuery("from ApplyData where packId = ?").setString(0, oldPs.getPackId()).list();
					   if(list == null || list.size() == 0)
						   session.delete(oldPs);					   
				   }
				   if(oldLs != null){
					   List<PackStatus> list = session.createQuery("from PackStatus where logisticId = ?").setString(0, oldLs.getLogisticId()).list();
					   if(list == null || list.size() == 0)
						   session.delete(oldLs);			
				   }
				   
				}
			}

	}

}