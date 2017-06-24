package com.salmat.pas.beans;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import net.sf.json.JSONArray;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;

import com.ibm.icu.util.Calendar;
import com.salmat.pas.bo.ApplyDataService;
import com.salmat.pas.conf.Constant;
import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.report.GenerateReport;
import com.salmat.pas.vo.ActionHistory;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.BankReceipt;
import com.salmat.pas.vo.JqgridAfpFile;
import com.salmat.pas.vo.LogisticStatus;
import com.salmat.pas.vo.PackStatus;
import com.salmat.util.HibernateSessionFactory;
import com.salmat.util.ToJqGridString;


/*
 * Thor新增
 */
public class FxAfpQueryBean extends BaseBean {
	private static int counter = 1;
	private static String todayStr = null; 
	static Logger logger = Logger.getLogger(FxAfpQueryBean.class);
	private Date cycleDate;  
	private Date cycleDateEnd;
    private Integer totalPage;  //共幾頁，分頁使用
    private String center;
	private String result;   //後端處理結果
	private Set<SelectItem> myCenters;
	private Set<SelectItem> centers;
	private AdminUser adminUser = null;
	private List<JqgridAfpFile> queryResult;
	private UIData dataTable;
	private SortableModel dataModel;
	private UIData adDataTable;
	private SortableModel adDataModel;
	private HashMap<PackStatus, Boolean> checked = new HashMap<PackStatus, Boolean>();
	private Integer totalFiles;
	private Integer totalBooks;
	private Integer totalReceipts;
	private Integer totalPages;
	private Integer totalSheets;
	private String receipt;
	private String uniqueNo;
	private String packIds;
	private String packId;
	private PackStatus pack;
	private HashMap<String, Area> auditAreaMap = null;
	private String returnStr = "";
	private String areaJson;
	private String inputNo;
	private boolean group = false;
	private String registerNo;
	private String parcelNo;
	private String batchOrOnline;
	private boolean autoOpen = false;
	
	
	private String jsonResult;  //前端jQGrid要使用的result
    
	public PackStatus getPack() {
		return pack;
	}

	public void setPack(PackStatus pack) {		
		this.pack = pack;
	}

	public String getAreaJson() {
		if(areaJson == null)
			getAllAuditArea();
		return areaJson;
	}
	public String test(){
		System.out.println("ABCEDFGS");
		return null;
	}

	public void setAreaJson(String areaJson) {
		this.areaJson = areaJson;
	}

	public Integer getTotalPage() {
		if(totalPage == null)
			return 0;
		else
		    return totalPage; 
	}
	
public String exportWeightExcel(){
		
		List<AfpFile> list = null;
		Session session = null;

		try{
			if(this.center == null || "".equals(this.center)){
				this.setResult("請先選擇轄區，再輸出報表");
				return null;
			}
			if(this.cycleDate == null ){
				this.setResult("請先選擇Cycle Date，再輸出報表");
				return null;
			}
			session = HibernateSessionFactory.getSession();
			List<Area> areas = session.getNamedQuery("Area.findByPk").setString(0, "9D00000").list();
			String tpeNo2Add = "";
			for(Area area : areas){				
				tpeNo2Add = area.getAddress();				
			}
			//排除寄回北二
			//排除找不到地址的
			String queryStr = "select weight, batchOrOnline, mailReceipt, count(*) "
					+ "from ("
					+ "select case "
					+ "when weight is null then 'no weight' "
					+ "when weight between 0 and 20.00 then '< 20' "
					+ "when weight between 20.01 and 50.00 then '20-50' "
					+ "when weight between 50.01 and 100.00 then '50-100' "
					+ "when weight between 100.01 and 250.00 then '100-250' "
					+ "when weight between 250.01 and 500.00 then '250-500' "
					+ "when weight between 500.01 and 1000.00 then '500-1000' "
					+ "when weight between 1000.01 and 2000.00 then '1000-2000' "
					+ "when weight between 2000.01 and 5000.00 then '2000-5000' "
					+ "when weight > 5000.00 then '> 5000' "
					+ "end as weight, batchOrOnline, mailReceipt "                                              
					+ "from LogisticStatus where sentTime between ? and ? and center = ? and address not like '%" + tpeNo2Add + "' and address not like '%無地址%' and address not like '%無法由%找到寄送地址') l "
					+ "group by l.weight, l.batchOrOnline, l.mailReceipt order by batchOrOnline desc, mailReceipt desc";
			Calendar cal = Calendar.getInstance();
			cal.setTime(cycleDate);
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
			
			SQLQuery query = session.createSQLQuery(queryStr);			
			query.setParameter(0, cycleDate);
			query.setParameter(1, cal.getTime());
			query.setString(2, this.center);
			List<Object[]> lss = query.list(); 
			
			if(lss == null || lss.size() < 0){
			   this.setResult("您選擇的範圍查無資料，請重新選擇。謝謝");
			   return null;
			}
			String targetName = GenerateReport.generateWeightReport(lss, this.cycleDate);
				
			this.getRequest().setAttribute("reportNameForDownload", targetName);
			return "download";
			
			
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			this.setResult("發生例外：" + e.getMessage());
			return "failure";
		}finally{
			if(session != null)
				session.close();
		}
	} 
	
	public String getLogisticFile(){
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		String tab = "	";
		Session session = null;
		try{
			setResult("");
			String [] packIdArr = packIds.split(",");
			session = HibernateSessionFactory.getSession();
			List<LogisticStatus> logistics = session.createQuery("from LogisticStatus where logisticId in (select distinct logisticId from PackStatus where packId in(:packIds)) order by firstUniqueNo").setParameterList("packIds", packIdArr).list();
			if(logistics == null || logistics.size() == 0){
				setResult("你勾選的" + packIds + "尚未產生裝箱清單");
				return null;
			}else{
				if("06".equals(logistics.get(0).getCenter()) && "B".equals(logistics.get(0).getBatchOrOnline())){
					setResult("北二個險列印清單請到打包前作業列印");
					return null;
				}
			}
			String todayStr = Constant.yyyy_MM_dd.format(new Date());
			if(!todayStr.equals(FxAfpQueryBean.todayStr)){
				FxAfpQueryBean.todayStr = todayStr;
				counter = 1;
			}
			String targetName = todayStr + "_" + StringUtils.leftPad(counter + "", 3, "0") + ".txt" ;
			File wFil = new File(ServletContextGetter.getRealPath("/pdf"), targetName);
			fos = new FileOutputStream(wFil);
			osw = new OutputStreamWriter(fos, "ms950");
			bw = new BufferedWriter(osw);
			for(LogisticStatus ls : logistics){
				//出貨編號	電話	姓名	住址	件數
				String line = ls.getLogisticId() + tab + ls.getTel() + tab + 
						    ls.getName() + tab + 
							ls.getAddress().replaceAll(tab, "") + tab + "1" + tab + " \r\n";
				bw.write(line);				
			}

			bw.flush();
			osw.flush();
			fos.flush();
			String fileName = GenerateReport.generateLogisticLabels(logistics, session, group);
			this.getRequest().setAttribute("reportNameForDownload", targetName);
			this.getRequest().setAttribute("reportNameForDownload2", fileName);
			
			return "download2";
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			return null;
		}finally{
			if(session != null && session.isOpen())
				session.close();
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bw = null;
			}
			if(osw != null){
				try {
					osw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				osw = null;
			}
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fos = null;
			}			
		}
		
	}
     
	public synchronized String prodLogistic(){
		double commonBagWg = 0;			
		double bigBagWg = 0;
		if(Constant.getWeightMap() != null){
			if(Constant.getWeightMap().get("牛皮紙袋（平面袋）") != null){
				commonBagWg = Constant.getWeightMap().get("牛皮紙袋（平面袋）");
			}
			if(Constant.getWeightMap().get("牛皮紙袋（立體袋）") != null){
				bigBagWg = Constant.getWeightMap().get("牛皮紙袋（立體袋）");
			}
		}
		int registerNoLen = 0;
		int parcelNoLen = 0;
		Integer registerNo = null;
		Integer parcelNo = null;
		if(this.registerNo != null && !"".equals(this.registerNo.trim())){
			registerNoLen = this.registerNo.length();
			registerNo = new Integer(this.registerNo);
		}
		if(this.parcelNo != null && !"".equals(this.parcelNo.trim())){
			parcelNoLen = this.parcelNo.length();
			parcelNo = new Integer(this.parcelNo);
		}
		Date today = new Date();

		String todayStr = Constant.yyyy_MM_dd.format(today);
		if(!todayStr.equals(FxAfpQueryBean.todayStr)){
			FxAfpQueryBean.todayStr = todayStr;
			counter = 1;
		}
		setResult("");
		
		String [] packIdArr = packIds.split(",");

		Session session = null;
		Transaction tx = null;
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		
		
		try{
			session = HibernateSessionFactory.getSession();
			List<Area> areas = session.createCriteria(Area.class).add(Restrictions.eq("areaName", "北二行政中心")).list();
			String taipeiNo2Add = null;
			if(areas != null && areas.size() > 0)
				taipeiNo2Add = areas.get(0).getAddress();
			
			tx = session.beginTransaction();
			List<PackStatus> myPacks = session.createQuery("from PackStatus where packId in (:packIds) and (logisticId is null) order by firstUniqueNo").setParameterList("packIds", packIdArr).list();
			if(myPacks != null && myPacks.size() > 0){
				PackStatus ps = myPacks.get(0);
				if("B".equals(ps.getBatchOrOnline()) && "06".equals(ps.getCenter())){
					setResult("北二個險不需重新產生裝箱清單");
					return null;
				}
			}else{
				setResult("你所選擇的打包清單均已產生過寄件清單，不需重新產生");
				return null;
			}
			List<String> logisticIds = new ArrayList<String>();
	//		List<Area> areaNmsList = session.createQuery("from Area where areaId in( select distinct serviceCenter from ApplyData where packId in (:packIds)) order by areaId").setParameterList("packIds", packIdArr).list();
			LinkedHashSet<String> addresses = new LinkedHashSet<String>();
			Date cycleDate = null;
			String center = null;
			//找每個地址對應的打包項目
			Map<String, List<PackStatus>> addresspacks = new HashMap<String, List<PackStatus>>();	
			//地址及服務中心名稱都相同時，放在同一包
			for(PackStatus pack : myPacks){
				String key = pack.getAreaAddress() + "#_#" + pack.getServiceCenterNm();
				logisticIds.add(pack.getPackId());
				
				cycleDate = pack.getCycleDate();
				center = pack.getCenter();
				
				addresses.add(key);
				List<PackStatus> list = null;
				if(addresspacks.get(key) == null){
					list = new ArrayList<PackStatus>();
				}else{
					list = addresspacks.get(key);
				}
				list.add(pack);
				addresspacks.put(key, list);
			}
			//把之前產生的logisticStatus都刪除
			session.createQuery("delete from LogisticStatus where logisticId in (:logisticIds)").setParameterList("logisticIds", logisticIds).executeUpdate();
			tx.commit();
			tx = session.beginTransaction();
			
			String tab = "	";
			String targetName = todayStr + "_" + StringUtils.leftPad(counter + "", 3, "0") + ".txt" ;
			counter++;
			Set<AfpFile> afpfiles = new HashSet<AfpFile>();			
			for(String address: addresses){
				List<PackStatus> packs = addresspacks.get(address);
				String firstUniqueNo = null;
				int i = 0;
				String lsId = null;
				String batchOrGroup = null; 
				String zipWithAdd = null;
				int books = 0;
				int receipts = 0;
				String tel = null;
				String name = "";
				double totalWeight = 0;
				for(PackStatus pack : packs){					
					if(pack.getServiceCenterNm() != null 
							&& !pack.getServiceCenterNm().equals(pack.getSubAreaId())){
						//服務中心名和subAreaId相同時代表是沒有服務中心的
						name = pack.getServiceCenterNm();
					}else{
						name = pack.getSubAreaName();
						if(name == null || name.trim().equals(""))
							name = pack.getSubAreaId();
					}
					totalWeight += pack.getWeight() == null? 0 : pack.getWeight();
					tel = pack.getSubAreaTel();
					books += pack.getBooks();
					receipts += pack.getReceipts();
					if(i == 0){
						firstUniqueNo = pack.getFirstUniqueNo();
						lsId = pack.getPackId(); //logisticId 等於 打包清單中的第一個packId
						this.pack = pack;
						batchOrGroup = pack.getBatchOrOnline();
						center = pack.getCenter();						
					}
					zipWithAdd = pack.getAreaAddress();
					if(pack.getZipCode() != null && !pack.getZipCode().trim().equals("")){
						zipWithAdd = pack.getZipCode() + " " + zipWithAdd;  
					}
					if(pack.getFirstUniqueNo() != null && firstUniqueNo != null){
						if(pack.getFirstUniqueNo().compareTo(firstUniqueNo) < 0){
							firstUniqueNo = pack.getFirstUniqueNo();
							lsId = pack.getPackId();
							this.pack = pack;							
						}
					}
					i++;
				}
				LogisticStatus ls = new LogisticStatus();
				
				ls.setLogisticId(lsId);
				ls.setMailReceipt(false);
				ls.setBatchOrOnline(batchOrGroup);
				ls.setCycleDate(cycleDate);
				if(books >= 7)
					ls.setWeight(totalWeight + bigBagWg);
				else
					ls.setWeight(totalWeight + commonBagWg);
				double weight = ls.getWeight() == null? 0 : ls.getWeight();
				//非退回件要設掛號號碼		
				if(taipeiNo2Add == null || taipeiNo2Add.trim().equals("") || zipWithAdd == null 
						|| (zipWithAdd.indexOf(taipeiNo2Add) < 0 && zipWithAdd.indexOf("無地址") < 0 && zipWithAdd.indexOf("找到寄送地址") < 0)){					
				
				   if(registerNo != null && !pack.isBack() && weight < Constant.parcelWeight){
					   String registerNoStr = StringUtils.leftPad(registerNo + "", registerNoLen, '0');
					   ls.setVendorId(registerNoStr);
					   registerNo++;
				   }else if(parcelNo != null && !pack.isBack() && weight >= Constant.parcelWeight){
					   String parcelNoStr = StringUtils.leftPad(parcelNo + "", parcelNoLen, '0');
					   ls.setVendorId(parcelNoStr);
					   parcelNo++;
				   }
				}else{
				   ls.setVendorId("送快遞");
				}
				if(!pack.isBack())
				   ls.setPackDone(false);
				else
				   ls.setPackDone(true);				
				ls.setAddress(zipWithAdd);
				ls.setBooks(books );
				
				
				ls.setReceipts(receipts);
				if(packs != null)
				   ls.setPacks(packs.size());
				ls.setCenter(center);
				ls.setFirstUniqueNo(firstUniqueNo);
				ls.setTel((tel == null || tel.equals("null"))? "" : tel);				
				ls.setName(name);
				Set<PackStatus> packSet = new HashSet<PackStatus>();
				for(PackStatus pack : packs){
					packSet.add(pack);
					Set<ApplyData> set = pack.getApplyDatas();
					for(ApplyData applyData : set){
						afpfiles.add(applyData.getAfpFile());
						applyData.setUpdateDate(today);
						//退回件，直接設為交寄完成
						if(pack.isBack())
						   applyData.setPolicyStatus("98");
						else
						   applyData.setPolicyStatus("97");
						session.update(applyData);
					}			
					if(!pack.isBack()){
					   pack.setStatus(40);
					   pack.setStatusNm("列印交寄清單");
					}else{
					   pack.setPackCompleted(true);
					   pack.setStatus(45);
					   pack.setStatusNm("等待貨運");
					   pack.setPolicyScanDate(today);
					   pack.setReceiptScanDate(today);
					   pack.setLabelScanDate(today);
					   pack.setPolicyScanUser("SYSTEM");
					   pack.setReceiptScanUser("SYSTEM");
					   pack.setLabelScanUser("SYSTEM");
					}
				    pack.setLogisticId(lsId);
				    pack.setUpdateDate(today);

				    session.update(pack);				    
				}								
		       ls.setPackStatuses(packSet);
			   session.save(ls);
				
			}
			List<LogisticStatus> logistics = session.createQuery("from LogisticStatus where logisticId in (select distinct logisticId from PackStatus where packId in(:packIds)) order by firstUniqueNo").setParameterList("packIds", packIdArr).list();
			File wFil = new File(ServletContextGetter.getRealPath("/pdf"), targetName);
			fos = new FileOutputStream(wFil);
			osw = new OutputStreamWriter(fos, "ms950");
			bw = new BufferedWriter(osw);
			for(LogisticStatus ls : logistics){
				//出貨編號	電話	姓名	住址	件數
				String line = ls.getLogisticId() + tab + ls.getTel() + tab + 
						    ls.getName() + tab + 
							ls.getAddress().replaceAll(tab, "") + tab + "1" + tab + " \r\n";
				bw.write(line);				
			}

			bw.flush();
			osw.flush();
			fos.flush();
			String fileName = GenerateReport.generateLogisticLabels(logistics, session, group);
										
			for(AfpFile afpFile : afpfiles){
				//List<ApplyData> list = session.getNamedQuery("ApplyData.findByNewBatchName").setString(0,  afpFile.getNewBatchName()).list();
				Set<ApplyData> list = afpFile.getApplyDatas();
				if(list != null && list.size() > 0){
					boolean allFinish = true;
					for(ApplyData applyData : list){
						if(applyData.getPolicyStatus() != null &&  new Integer(applyData.getPolicyStatus()) < 95){
							allFinish = false;
							break;
						}
					}
					if(allFinish && !"已交寄".equals(afpFile.getStatus())){
						session.createQuery("update AfpFile set status = '等待貨運', updateDate = ? where newBatchName = '" + afpFile.getNewBatchName() + "'").setParameter(0, today).executeUpdate();
					}
				}
			}
				
		    this.getRequest().setAttribute("reportNameForDownload", targetName);
			this.getRequest().setAttribute("reportNameForDownload2", fileName);
			tx.commit();			
			return "download2";
			
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			e.printStackTrace();
			setResult("Exception happened:" + e.getMessage());
			return null;
		}finally{
			if(session != null && session.isOpen())
				session.close();
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bw = null;
			}
			if(osw != null){
				try {
					osw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				osw = null;
			}
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fos = null;
			}			
		}
	}
	
	public String scanPack(){
		setResult("");
		String userId = adminUser.getUserId();
		Date today = new Date();
		Session session = null;
		//Transaction tx = null;
		try {
		   session = HibernateSessionFactory.getSession();
		   
		  
		   if((packId.length() != 14 && packId.length() != 15 && packId.length() != 16) || (!packId.endsWith("R") && !packId.endsWith("P") && !packId.endsWith("L") )){
			   setResult("非裝箱代號，請重新輸入");
			   return null;
		   }
		   
		   String packId = this.packId.substring(0, this.packId.length() - 1); //前面的pk
		   String packType = this.packId.substring(this.packId.length() - 1);//最後一碼
		   pack = (PackStatus) session.get(PackStatus.class, packId);
		   if(pack == null){
			   setResult("不正確的裝箱代號，請重新輸入");
			   return null;
		   }
		   if(pack.getLogisticStatus() != null && pack.getLogisticStatus().getMailReceipt() != null &&  pack.getLogisticStatus().getMailReceipt()){
			   setResult("北二個險雙掛號件請直接進行裝箱記錄");
			   return null;
		   }
		   if("06".equals(pack.getCenter()) && "B".equals(pack.getBatchOrOnline()) && pack.isBack()){
			   setResult("北二個險退件請直接送快遞");
			   return null;
		   }

		   /*
		   if("06".equals(pack.getCenter()) && pack.getBatchOrOnline() != null && "B".equals(pack.getBatchOrOnline())){
			   setResult("北二個險請直接進行裝箱記錄");
			   return null;
		   }
           */
		   
		   center = pack.getCenter();
		   cycleDate = pack.getCycleDate();
		   
		   
		   if(packType.equals("R")){
			   //以後不掃簽收單了
			   
			   setResult(packId + "簽收單明細檢查完成");
			   //掃描完成簽收單
			   pack.setReceiptScanDate(today);
			   pack.setReceiptScanUser(userId);
			   			   
		   }else if(packType.equals("L")){
			   //掃描完成標籤單，目前不使用
			   setResult(packId + "寄送標籤掃描完成");
			   pack.setLabelScanDate(today);
			   pack.setLabelScanUser(userId);			   
		   }else if(packType.equals("P")){
			   //掃描完成保單
			   setResult(packId + "保單明細檢查完成");
			   pack.setPolicyScanDate(today);
			   pack.setPolicyScanUser(userId);
			   pack.setLabelScanDate(today);
			   pack.setLabelScanUser(userId);
		   }
		   List<ApplyData> apps = null;
		   
		    //@NamedQuery(name="ApplyData.findByPackId", query="from ApplyData where packId = ? order by packTime, receipt"),		   
		   Query queryApp = session.getNamedQuery("ApplyData.findByPackId");
	       queryApp.setString(0, pack.getPackId());
		   apps = queryApp.list();

		   if(apps != null && apps.size() > 0){
			   if("GROUP".equals(apps.get(0).getSourceCode())){
			   	  this.setGroup(true);
			   	  this.setBatchOrOnline("G");
			   }else{
			   	  this.setGroup(false);
			   	  this.setBatchOrOnline("B");
			   }
		   }
		   
		   //如果保單明細和標籤都掃描好了，就應該開始掃保單和簽收單
		   if(pack.getPolicyScanDate() != null && pack.getLabelScanDate() != null ){			     
			      // 檢查是不是此裝箱清單都已經裝箱完成      
			      //Set<ApplyData> apps = pack.getApplyDatas();
			      boolean allPackComplete = true;
			      boolean found = false;
			      boolean matchBefore = false;
			      
			      for(ApplyData applyData : apps){	
					   if(inputNo != null && (inputNo.length() == 16 || inputNo.length() == 17) && inputNo.equals(applyData.getUniqueNo()) 
							   && applyData.getReceipt() != null && !applyData.getReceipt()){						     
						     found = true;
						     if("GROUP".equals(applyData.getSourceCode()) ){
						    	 matchBefore = true;
						    	 this.setGroup(true);
						    	 this.setBatchOrOnline("G");
						     }else if("06".equals(pack.getCenter()) && "B".equals(pack.getBatchOrOnline())){
						    	 matchBefore = true;
						    	 this.setGroup(false);
						    	 this.setBatchOrOnline("B");
						     }else{
						    	 this.setGroup(false);
						    	 this.setBatchOrOnline("B");
						     }
						    //@NamedQuery(name="ApplyData.updateAppDataById", query="update ApplyData set policyStatus = ?, packTime = ?, updateDate = ? where oldBatchName = ? "),			     
						    Query query = session.getNamedQuery("ApplyData.updateAppDataById");
						    if(pack.isBack()){
					    	   query.setString(0, "96");
					    	   applyData.setPolicyStatus("96");					    	   
						    }else{
						       query.setString(0, "95");
						       applyData.setPolicyStatus("95");
						    }
						    query.setParameter(1, today);
						    applyData.setPackTime(today);
						    query.setParameter(2, today);
						    applyData.setUpdateDate(today);
						    query.setString(3, applyData.getOldBatchName());
						    //session.update(applyData);
						    query.executeUpdate();
						    if(matchBefore){
						    	//如果是團險，掃完保單就是掃完了，因為已經先配表了
						    	for(ApplyData reCheckData : apps){
						    		if(applyData.getPolicyNos().equals(reCheckData.getPolicyNos()) 
						    				&& applyData.getReprint().intValue() == reCheckData.getReprint().intValue() 
						    				&& !applyData.getOldBatchName().equals(reCheckData.getOldBatchName())){
						    			    if(pack.isBack()){
									    	   query.setString(0, "96");
									    	   reCheckData.setPolicyStatus("96");					    	   
										    }else{
										       query.setString(0, "95");
										       reCheckData.setPolicyStatus("95");
										    }
										    query.setParameter(1, today);
										    reCheckData.setPackTime(today);
										    query.setParameter(2, today);
										    reCheckData.setUpdateDate(today);
										    query.setString(3, reCheckData.getOldBatchName());
										    //session.update(applyData);
										    query.executeUpdate();
						    			
						    		}
						    	}
						    }
					   }else if(inputNo != null && !inputNo.trim().equals("") && applyData.getReceipt() != null && applyData.getReceipt() && applyData.getPolicyNoSet().contains(inputNo)){
						    found = true;
						    Query query = session.getNamedQuery("ApplyData.updateAppDataById");
						    if(pack.isBack()){
					    	   query.setString(0, "96");
					    	   applyData.setPolicyStatus("96");
						    }else{
						       query.setString(0, "95");
						       applyData.setPolicyStatus("95");
						    }
						    query.setParameter(1, today);
						    applyData.setPackTime(today);
						    query.setParameter(2, today);
						    applyData.setUpdateDate(today);
						    query.setString(3, applyData.getOldBatchName());
						    //session.update(applyData);
						    query.executeUpdate();						    
					   }
			    	  int policyStatus = applyData.getPolicyStatus() == null? 0 : new Integer(applyData.getPolicyStatus());
			    	  if(policyStatus < 95){
			    		  allPackComplete = false;
			    	  }
			      }			      
			      
			      if((inputNo == null || "".equals(inputNo.trim())) && pack != null){
			    	  found = true;
			      }
			      if(!found){
			    	  setResult( "輸入代號:" + inputNo + "，不屬於裝箱代號:" + packId + "。請重新檢查");
			    	  return null;
			      }
			      if(matchBefore){
			    	  Query query = session.createQuery("from ApplyData where policyStatus < '95' and packId =? ");
				      query.setString(0, pack.getPackId());
				      List result = query.list();
				      if(result != null && result.size() > 0){
				    	  allPackComplete = false;
				      }else{
				    	  allPackComplete = true;
				      }
			      }
                  
			      //如果全部完成，設定成receiptScanData也完成
			      if(allPackComplete){
					   pack.setReceiptScanDate(today);
					   pack.setReceiptScanUser(userId);
			      }
			      setResult("裝箱代號:" + packId + "   輸入號碼：" + inputNo + "裝箱檢查完成");			   
		   }
		   if(pack.getPolicyScanDate() != null && pack.getLabelScanDate() != null && pack.getReceiptScanDate() == null && (inputNo == null || "".equals(inputNo.trim()))){			
			   setResult("裝箱代號:" + packId + "準備進行逐件裝箱檢查完成");
		   }
		   
		   //檢查是不是三個都掃描完成，如果全都掃完，更新成裝箱完成
		   if(pack.getStatus() <= 30 && pack.getReceiptScanDate() != null && pack.getLabelScanDate() != null && pack.getPolicyScanDate() != null){
			   pack.setStatus(30);
			   pack.setStatusNm("裝箱掃描完成");
			   pack.setUpdateDate(today);
			   Transaction tx = session.beginTransaction();
			   session.update(pack);
			   tx.commit();
			   setResult(packId + "裝箱完成，準備貨運");
			   //Set<AfpFile> afpFiles = new HashSet<AfpFile>();
               Set<String> newBatchNames = new HashSet<String>();
			   //如果裝箱完成，更新applyData及afpFile
			   if(pack.getApplyDatas() != null){
				   for(ApplyData applyData : pack.getApplyDatas()){				      
				      newBatchNames.add(applyData.getNewBatchName());
				   }
			   }
			   //@NamedQuery(name="ApplyData.findInNewBatchName", query="from ApplyData where newBatchName in (:newBatchNames)  order by newBatchName"),
			   //@NamedQuery(name="AfpFile.findInNewBatchNames", query="from AfpFile where newBatchNames in (:newBatchNames)"),
			   List<AfpFile> afpFiles = session.getNamedQuery("AfpFile.findInNewBatchNames").setParameterList("newBatchNames", newBatchNames).list();
			   Query query = session.getNamedQuery("ApplyData.findInNewBatchName");
			   query.setParameterList("newBatchNames", newBatchNames);
			   List<ApplyData> list = query.list();
			   for(AfpFile afpFile : afpFiles){
				    //List<ApplyData> list = session.getNamedQuery("ApplyData.findByNewBatchName").setString(0,  afpFile.getNewBatchName()).list();
					//Set<ApplyData> list = afpFile.getApplyDatas();
					if(list != null && list.size() > 0){
						boolean allFinish = true;
						for(ApplyData applyData : list){
							if(afpFile.getNewBatchName().equals(applyData.getNewBatchName()) && applyData.getPolicyStatus() != null &&  new Integer(applyData.getPolicyStatus()) < 95){
								allFinish = false;
								break;
							}
						}
						if(allFinish && !"已交寄".equals(afpFile.getStatus())){
							tx = session.beginTransaction();
							session.createQuery("update AfpFile set status = '裝箱完成', packTime = ?, updateDate = ? where newBatchName = '" + afpFile.getNewBatchName() + "'").setParameter(0, today).setParameter(1, today).executeUpdate();
							tx.commit();
						}else{
							tx = session.beginTransaction();
							session.createQuery("update AfpFile set packTime = ?, updateDate = ? where newBatchName = '" + afpFile.getNewBatchName() + "'").setParameter(0, today).setParameter(1, today).executeUpdate();
							tx.commit();
						}
					}
				}
			   			   
		   }else if(pack.getStatus() < 30){			   
			   pack.setStatus(20);
			   pack.setStatusNm("裝箱中");			   
			   pack.setUpdateDate(today);
			   Transaction tx = session.beginTransaction();
			   session.update(pack);
			   tx.commit();
		   }
		   
		   //"PackStatus.findByCycleDateAndCenter", 
		   //query="from PackStatus where cycleDate = ? and center = ? and batchOrOnline = ? order by status"
		   List<PackStatus> packs = null;
		   if(!("06".equals(pack.getCenter()) && "B".equals(pack.getBatchOrOnline()))){
		      packs = session.getNamedQuery("PackStatus.findByCycleDateAndCenter").setDate(0, cycleDate).setString(1, center).setString(2, batchOrOnline).list();
		   }else{
			  packs = session.createQuery("select p from PackStatus p left join p.logisticStatus l where l.mailReceipt = false and p.subAreaName <> '北二行政中心' and p.subAreaName <> '北二審查科' and p.back = false and p.cycleDate = ? and p.batchOrOnline = 'B' order by p.status, p.firstUniqueNo").setDate(0, cycleDate).list();
			  
		   }
		   setDataModel(new SortableModel(new ListDataModel(packs)));
		   if(packId != null){
		      setAdDataModel(new SortableModel(new ListDataModel(apps)));
		   }
		   return null;
		} catch (Exception e) {
		   logger.error("", e);
		   setResult("exception happened:" + e.getMessage());
			   
		   return null;
		} finally {
		   if (session != null && session.isOpen()){
			   session.close();
		   }
		   session = null;
		}
	}
	
	public String getAllAuditArea(){
		if(auditAreaMap == null){
		   Session session = null;		   	      
		   try {
			   session = HibernateSessionFactory.getSession();
			   List<Area> normalAreas =  session.getNamedQuery("Area.findHaveAddressAndAudit").list(); 
			   
	           auditAreaMap = new HashMap<String, Area>();
	           
			   for(Area area : normalAreas){					
			       Area mapArea = auditAreaMap.get(area.getSubAreaId());
			       // subAreaId有重覆，只放入第一個抓到的
			       if(mapArea == null){
			    	   auditAreaMap.put(area.getSubAreaId(), area);
			       }				    
			   }
			   areaJson = JSONArray.fromObject(normalAreas).toString();
			   Set<String> keySet  = auditAreaMap.keySet();
				if(keySet != null && keySet.size() > 0){
					for(String result: keySet){
						returnStr += "' " + result + "__" + auditAreaMap.get(result).getAreaName() + "',";
					}
					returnStr = "[" + returnStr + "]";
					return  returnStr ;
				}else{
					return "null";
				}
				
		   } catch (Exception e) {
			   logger.error("", e);
			   
			   return null;
		   } finally {
			   if (session != null)
				   session.close();
		   }
		}else{
			return returnStr;
		}
		
	} 
	
	public void printIt(ActionEvent event){
		System.out.println(12345678);
		
	}

	public FxAfpQueryBean() {
		if(adminUser == null)
	       adminUser = (AdminUser)this.getSession(true).getAttribute("loginUser");
	}

	
	public String validData() {
		return "";
	}
	
	public String updateSave(){
		setResult("");
		Session session = null;
		Transaction tx = null;
		try{
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			session.update(pack);		
			
			
			//同步更新Area資料
			if (!pack.getCenter().equals("06")
					|| !pack.getBatchOrOnline().equals("B")) {
				Criteria criteria = session.createCriteria(Area.class);
				criteria.add(Restrictions.eq("subAreaId", pack.getSubAreaId()));
				criteria.addOrder(Order.asc("areaId"));
				List<Area> areas = criteria.list();
				// 找出是不是有服務中心
				LinkedHashSet<String> serviceCenters = new LinkedHashSet<String>();
				for (Area area : areas) {
					if (area.getServiceCenter() != null
							&& !area.getServiceCenter().trim().equals(""))
						serviceCenters.add(area.getServiceCenter());
				}
				// 如果沒有服務中心，更新Area的地址
				if (serviceCenters.size() == 0) {
					for (Area area : areas) {
						area.setAddress(pack.getAreaAddress());
						session.update(area);
					}
				} else {
					// 如果有服務中心，更新服務中心的地址
					String serviceCenter = null;
					// 只更新第一個，因為如果有多個服務中心時，也是取第一個
					for (String obj : serviceCenters) {
						serviceCenter = obj;
						break;
					}
					// 找出service center
					Area area = (Area) session.get(Area.class, serviceCenter);
					// 找不到時新增一個
					if (area == null) {
						area = new Area();
						area.setSubAreaId(serviceCenter.substring(0, 4));
						area.setAreaId(serviceCenter);
					}
					area.setAddress(pack.getAreaAddress());
					session.saveOrUpdate(area);
				}
			}else{
				LogisticStatus ls = (LogisticStatus) session.get(LogisticStatus.class, pack.getPackId());
				if(ls != null){
					ls.setName(pack.getSubAreaName());
					ls.setAddress(pack.getAreaAddress());
					ls.setTel(pack.getSubAreaTel());
					session.update(ls);
				}
			}
			
			tx.commit();
			query(false);
			setResult("更新成功");
			return "success";				
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			setResult("發生錯誤：" + e.getMessage());
			return "failure";
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		
	}
	
	public String editAddress(){
		
		String packId = this.getParameter("packId");
		if(packId != null && !"".equals(packId.trim())){
			Session session = null;
			try{
				session = HibernateSessionFactory.getSession();
				pack = (PackStatus)session.get(PackStatus.class, packId);
				setResult("");
				return "toEdit";				
			}catch(Exception e){
				logger.error("", e);
				setResult("發生錯誤：" + e.getMessage());
			}finally{
				if(session != null && session.isOpen())
					session.close();
			}
		}
		
		return "failure";
	}
	
	public String exportExcel(){
		
		List<AfpFile> list = null;
		Session session = null;
		int rowNum = 0;
		try{
			session = HibernateSessionFactory.getSession();
			String queryStr = "select a.newBatchName from AfpFile a inner join a.applyDatas ad where ad.cycleDate = ?  ";
			
			if(center != null && !center.equals("")){
				queryStr += " and a.center = '" + center + "'";
			}if(receipt != null && !receipt.equals("")){
				queryStr += " and a.newBatchName like '" + receipt + "%'";
			}
			Query query1 = session.createQuery(queryStr);
			if(cycleDate != null)
			   query1.setDate(0, cycleDate);
			
			Query query2 = session.createQuery("from ApplyData where newBatchName in (:newBatchNames) and substract = true order by center").setParameterList("newBatchNames", query1.list());
			List<ApplyData> applyDatas = query2.list();
			if(applyDatas == null || applyDatas.size() == 0){
				this.setResult("無抽件，不產生報表");
				return "success";
			}else{
				String targetName = GenerateReport.generateSubstractReport(applyDatas);
				
				this.getRequest().setAttribute("reportNameForDownload", targetName);
				return "download";
			}
			
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			return "failure";
		}finally{
			if(session != null)
				session.close();
		}
	}
	
    public String exportAdCsv(){
		
		List<AfpFile> list = null;
		Session session = null;
		if(center == null || !center.equals("06")){
			this.setResult("此報表為北二專用，請選擇北二後再輸出");
			return "success";
		}
		try{
			session = HibernateSessionFactory.getSession();
			Map<String, List<ApplyData>> groupMap = new HashMap<String, List<ApplyData>>(); 
			String queryStr2 = "from ApplyData where cycleDate = ? ";
			if(center != null && !center.equals("")){
				queryStr2 += " and center = '" + center + "' and receipt = false ";
			}			
			//norm
			for(int i = 0 ;i < 3 ; i++){				
				String likeStr = "";
				if(i == 0){
					likeStr = "MI";
				}else if(i == 1){
					likeStr = "P";
				}else if(i == 2){
					likeStr = "S";
				}					
				String executeQuery = queryStr2 + " and oldBatchNo like '" + likeStr + "%' and sourceCode = 'NORM' order by newBatchName, newSerialNo";
			    Query query = session.createQuery(executeQuery).setDate(0, cycleDate);
			    List<ApplyData> applyDatas = query.list();
			    if(applyDatas != null && applyDatas.size() > 0)
			    	groupMap.put(likeStr, applyDatas);			    
			}
			//rept
			String executeQuery = queryStr2 + " and sourceCode = 'REPT' order by newBatchName, newSerialNo";
		    Query query = session.createQuery(executeQuery).setDate(0, cycleDate);
		    List<ApplyData> applyDatas = query.list();
		    if(applyDatas != null && applyDatas.size() > 0)
		    	groupMap.put("RePrint", applyDatas);
			//reis
		    executeQuery = queryStr2 + " and sourceCode = 'REIS' order by newBatchName, newSerialNo";
		    query = session.createQuery(executeQuery).setDate(0, cycleDate);
		    applyDatas = query.list();
		    if(applyDatas != null && applyDatas.size() > 0)
		    	groupMap.put("POSPRINT", applyDatas);
		    //CONV
		    executeQuery = queryStr2 + " and sourceCode = 'CONV' order by newBatchName, newSerialNo";
		    query = session.createQuery(executeQuery).setDate(0, cycleDate);
		    applyDatas = query.list();
		    if(applyDatas != null && applyDatas.size() > 0)
		    	groupMap.put("CONPRINT", applyDatas);
		    
			
			//Query query2 = session.createQuery("from ApplyData where where cycleDate = ? ");
			if(groupMap == null || groupMap.size() == 0){
				this.setResult("查無資料，不產生報表");
				return "success";
			}else{
				String targetfileName = GenerateReport.generate06Report(groupMap);
				this.getRequest().setAttribute("reportNameForDownload", targetfileName);
				return "download";
			}
			
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			return "failure";
		}finally{
			if(session != null)
				session.close();
		}
	}
	
	public String exportPackExcel(){
		autoOpen = false;
		setResult("");
		String[] packs = packIds.split(",");
		Session session = null;
		try{
			session = HibernateSessionFactory.getSession();
			List<PackStatus> list = session.getNamedQuery("PackStatus.findByPksOrderUniqueNo").setParameterList("packIds", packs).list();
			String targetName = null;
			if(list != null && list.size() > 0){				
				if(list.get(0).getCenter().equals("06") && list.get(0).getBatchOrOnline().equals("B")){
					//北二且是個險時走這段 
					List<LogisticStatus> logistics = session.createQuery("from LogisticStatus where logisticId in (select distinct logisticId from PackStatus where packId in(:packIds)) order by firstUniqueNo").setParameterList("packIds", packs).list();
					
					targetName = GenerateReport.generateLogisticLabels(logistics, session, true);
					this.getRequest().setAttribute("reportNameForDownload", targetName);
					
				}else{
					targetName = GenerateReport.generateLabels(list, session);
				}
			}
			 			
			if(targetName != null){
				Transaction tx = session.beginTransaction();
				for(PackStatus pack : list){
					if(pack.getStatus() < 10){
					   pack.setStatus(10);
					   pack.setStatusNm("已列印標籤清單");
					   pack.setUpdateDate(new Date());
					   session.update(pack);
					}
				}
				tx.commit();
			}
				
			this.getRequest().setAttribute("reportNameForDownload", targetName);
			return "download";
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			setResult("Exception happened:" + e.getMessage());
			return null;
		}finally{
			if(session != null)
				session.close();
		}
	}
	
	public String exportPackExcel2(){
		setResult("");
		String[] packs = packIds.split(",");
		Session session = null;
		try{
			session = HibernateSessionFactory.getSession();
			List<PackStatus> list = session.getNamedQuery("PackStatus.findByPks").setParameterList("packIds", packs).list();
			//String targetName = GenerateReport.generateLabels(list, "label2.xls");
			String targetName = null;
			if(targetName != null){				
				Transaction tx = session.beginTransaction();
				for(PackStatus pack : list){
					if(pack.getStatus() < 10){
					   pack.setStatus(10);
					   pack.setStatusNm("已列印標籤清單");
					   pack.setUpdateDate(new Date());
					   session.update(pack);
					}
				}
				tx.commit();
			}
				
			this.getRequest().setAttribute("reportNameForDownload", targetName);
			return "download";
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			setResult("Exception happened:" + e.getMessage());
			return null;
		}finally{
			if(session != null)
				session.close();
		}
	}
	
	public String combinePacks(){
	   autoOpen = false;
	   Date today = new Date();
	   String[] packs = packIds.split(",");
	   if(packs.length == 0 || packs.length == 1){
		   setResult("請至少點選一個以上的裝箱清單進行合併");
		   return null;
	   }else{
		   String preSubAreaId = null;
		   String maxSerialNo = "000000000";
		   for(String packId : packs){
			   //packid規則 yyMMddSubAreaId00n
			   String subAreaId = packId.substring(6, packId.length() - 3);
			   String dateStr = packId.substring(0, 6);
			   String serialNo = dateStr + packId.substring(packId.length() - 3); //有可能是不同天的			   
			   if(preSubAreaId != null && !preSubAreaId.equals(subAreaId)){
				   setResult("只有相同收件單位才能合併");
				   return null;
			   }
			   if(maxSerialNo.compareTo(serialNo) < 0){
				   maxSerialNo = serialNo; 
			   }
			   preSubAreaId = subAreaId;
		   }
		   String destPackId = maxSerialNo.substring(0, 6) + preSubAreaId + maxSerialNo.substring(6); //要融合的目的地
		   Session session = null;
		   Transaction tx = null;
		   try{
			  session = HibernateSessionFactory.getSession();
			  tx = session.beginTransaction();
			  PackStatus destPack = (PackStatus) session.get(PackStatus.class, destPackId);
			  //回復到裝箱準備
			  destPack.setStatus(0);
			  destPack.setStatusNm("裝箱準備");
			  destPack.setPolicyScanDate(null);
			  destPack.setPolicyScanUser(null);
			  destPack.setReceiptScanDate(null);
			  destPack.setReceiptScanUser(null);
			  destPack.setLabelScanDate(null);
			  destPack.setLabelScanUser(null);
			  destPack.setUpdateDate(today);			  
			  Set<String> newBatchNms = destPack.getNewBatchNmSet();
		      for(String packId : packs){
		    	  PackStatus pack = (PackStatus) session.get(PackStatus.class, packId);
		    	  //如果不同時
		    	  if(!packId.equals(destPackId)){		    		  
		    	     Set<ApplyData> applyDatas = pack.getApplyDatas();		    	  
		    	     for(ApplyData applyData : applyDatas){
		    	    	 String oriPackId = applyData.getPackId();
		    	    	 //更新afpFile
		    	    	 AfpFile afpFile = applyData.getAfpFile();
		    	    	 newBatchNms.add(afpFile.getNewBatchName()); 
		    	    	 Set<String> packIds = afpFile.getPackIdSet();
		    	    	 boolean update = false;
		    	    	 //如果有原來的id在就移除
		    	    	 if(packIds.contains(oriPackId)){
		    	    		 packIds.remove(oriPackId);
		    	    		 update = true;
		    	    	 }
		    	    	 //如果不含有目標id就加入
		    	    	 if(!packIds.contains(destPackId)){
		    	    		 packIds.add(destPackId);
		    	    		 update = true;
		    	    	 }
		    	    	 if(update){
		    	    		afpFile.setPackIdSet(packIds);
		    	    	    afpFile.setUpdateDate(today);
		    	    	    afpFile.setStatus("裝箱中");
		    	    	    session.update(afpFile);
		    	    	    
		    	    	 }
		    	    	//更新afpFile結束 
		    	    	//更新applyData
		    	    	 applyData.setPackId(destPackId);
		    	    	 if(!destPack.isBack())
		    	    	    applyData.setPolicyStatus("60");
		    	    	 else
		    	    		applyData.setPolicyStatus("65");
		    	    	 applyData.setUpdateDate(today);
		    	    	 session.update(applyData);
		    	    	//更新applyData結束
		    	    	 if(applyData.getReceipt() != null && applyData.getReceipt()){
		    	    		 destPack.setReceipts((destPack.getReceipts() + 1));
		    	    	 }else{
		    	    		 destPack.setBooks((destPack.getBooks() + 1));
		    	    	 }
		    		 }
		    	     session.delete(pack);
		    	  }			   
		      }
		      destPack.setNewBatchNmSet(newBatchNms);
		      session.update(destPack);
		      tx.commit();
		      setResult("合併成功:所有的裝箱清單合併到" + destPackId + "中，狀態並設定為準備裝箱");
		      query(false);
		      return null;
		   }catch(Exception e){
			   if(tx != null)
				   tx.rollback();
			   logger.error("", e);
			   setResult("exception happened:" + e.getMessage());
			   e.printStackTrace();
			   return null;
		   }finally{
			   if(session != null && session.isOpen())
				   session.close();
			   
		   }
		   
	   }
	}

	public synchronized String produceNoBrPacks(){
		double commonBagWg = 0;			
		double bigBagWg = 0;
		double mailReceiptWg = 0;
		double paperWg = 0;
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
			if(Constant.getWeightMap().get("內頁紙張") != null){
				paperWg = Constant.getWeightMap().get("內頁紙張");
			}
		}
		
		int registerNo = 0;
		if(this.registerNo != null && !"".equals(this.registerNo.trim())){
			registerNo = new Integer(this.registerNo);
		}else{
			setResult("請輸入掛號號碼");
			return null;
		}
		autoOpen = false;
		Session session = null;
		Transaction tx = null;
		setResult("");
		try{
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();

			//只查出送金單關聯到的，且尚未產生packId的
			Criteria criteria = session.createCriteria(ApplyData.class, "applyData")
                    .createAlias("applyData.bankReceipts", "bankReceipts", Criteria.INNER_JOIN);
			criteria.add(Restrictions.isNull("applyData.packId"));
			criteria.add(Restrictions.ne("sourceCode", "GROUP"));
			criteria.add(Restrictions.or(Restrictions.eq("receipt", false), Restrictions.isNull("receipt")));
			criteria.add(Restrictions.or(Restrictions.ge("policyStatus", "55"), Restrictions.eq("policyStatus", "41")));
			//如果exceptionStatus有值，且驗單結果裡有"尚未收到送金單"的字樣的，不包含在內
			criteria.add(Restrictions.or(
					   Restrictions.or(Restrictions.isNull("exceptionStatus"), Restrictions.eq("exceptionStatus", "")), 					   
					   Restrictions.and(Restrictions.ne("exceptionStatus", ""), Restrictions.not(Restrictions.like("verifyResult", "%尚未接收到送金單%")))
					   
					));
			criteria.add(Restrictions.ne("policyStatus", "100"));
			criteria.add(Restrictions.gt("applyData.cycleDate", Constant.yyyyMMdd.parse("20150112"))); //這天是正式上線日			
			criteria.addOrder(Order.asc("applyData.policyStatus")).addOrder(Order.asc("applyData.cycleDate")).addOrder(Order.asc("applyData.uniqueNo"));			
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);						
			List<ApplyData> applyDatas = criteria.list();
			if(applyDatas == null || applyDatas.size() == 0){
				setResult("無等待送金單保單");
				return null;
			}
			List<PackStatus> packStatuses = pdTpePacks(session, applyDatas);			
			tx.commit();
			
			//設定重量
			tx = session.beginTransaction();
			Query query = session.createQuery("from ApplyData where receipt = false and packId = ? and (groupInsure = false or groupInsure is null)");
			List<String> logisticIds = new ArrayList<String>();	
			Set<String> backPackIds = new HashSet<String>();
			for(PackStatus ps : packStatuses){
				if(ps.isBack())
					backPackIds.add(ps.getPackId());
				if(!logisticIds.contains(ps.getLogisticId()) && ps.getLogisticId() != null)
					logisticIds.add(ps.getLogisticId());
				List<ApplyData> ads = query.setString(0, ps.getPackId()).list();
				double weight = 0;
				int booksCount = 0;
				for(ApplyData ad : ads){
					if(ad.getReceipt() != null && !ad.getReceipt())
						booksCount++;
					weight += ad.getWeight() == null? 0 : ad.getWeight();
				}
				if( booksCount >= 7){
					weight += bigBagWg;
				}else if(booksCount > 0){
					weight += commonBagWg;
				}
				ps.setWeight(weight);
				session.update(ps);
			}			
			tx.commit();
			
			tx = session.beginTransaction();
			query = session.createQuery("from PackStatus where logisticId = ?");
			int registerNum = 0;
			int parcelNum = 0;
			try{
			   registerNum = new Integer(this.registerNo);
			   parcelNum = new Integer(this.parcelNo);
			}catch(Exception e){
				
			}
			for(String logisticId : logisticIds){
				LogisticStatus ls = (LogisticStatus) session.get(LogisticStatus.class, logisticId);
				if(ls != null){
				   List<PackStatus> packs = query.setString(0, logisticId).list();
				   double weight = 0;
				   for(PackStatus pack : packs){
					   weight += pack.getWeight() == null? 0 : pack.getWeight(); 
				   }
				   if(packs.size() > 1){
					   weight += bigBagWg;
				   }
				   if(ls.getMailReceipt() != null && ls.getMailReceipt())
					   weight += mailReceiptWg;
				   else
					   weight += packs.size() * paperWg;
				   ls.setWeight(weight);
				   
				   if(!backPackIds.contains(logisticId)){
				      if(weight < Constant.parcelWeight){
					     String vendorId = StringUtils.leftPad(registerNum + "", this.registerNo.length(), '0');
				         ls.setVendorId(vendorId);
				         registerNum++;
				      }else {
					     String vendorId = StringUtils.leftPad(parcelNum + "", this.parcelNo.length(), '0');
					     ls.setVendorId(vendorId);
					     parcelNum++;
				      }
				   }
				   session.update(ls);
				}				
			}
			
			
			tx.commit();
			setDataModel(new SortableModel(new ListDataModel(packStatuses)));
			setAdDataModel(new SortableModel(new ListDataModel(applyDatas)));
			if(applyDatas != null && applyDatas.size() > 0)
			    setResult("產生裝箱清單成功");
			else
				setResult("無等待保單");
			return null;
		}catch(Exception e){			
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			setResult("發生例外狀況:" + e.getMessage());
			setDataModel(new SortableModel(new ListDataModel(new ArrayList<PackStatus>())));
			setAdDataModel(new SortableModel(new ListDataModel(new ArrayList<ApplyData>())));
			e.printStackTrace();
			return null;
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		
		
		
	}

	private List<PackStatus> pdTpePacks(Session session,
			List<ApplyData> applyDatas) {
		List<String> oldBatchNames = new ArrayList<String>();
		List<String> allPolicys = new ArrayList<String>();
		for(ApplyData applyData : applyDatas){
		    oldBatchNames.add(applyData.getOldBatchName().replaceAll("保單", "簽收回條"));
		    allPolicys.add(applyData.getOldBatchName());
		}
		//查出簽收回條
		List<ApplyData> receiptAds = session.createQuery("from ApplyData where receipt = true and oldBatchName in (:oldBatchNames) and newBatchName is not null "
				+ " and newBatchName not like '%9999' order by cycleDate, applyNo, policyNos desc").setParameterList("oldBatchNames", oldBatchNames).list();
		applyDatas.addAll(receiptAds); //簽收回條放後面
					
		Query query = session.createQuery("select max(packId) from PackStatus where cycleDate = ? and batchOrOnline = 'B' and center = '06'");
		List<LogisticStatus> logisticStatuses = new ArrayList<LogisticStatus>();
		List<PackStatus> packStatuses = new ArrayList<PackStatus>();
		List<PackStatus> resortPacks = new ArrayList<PackStatus>();
		List<LogisticStatus> resortLss = new ArrayList<LogisticStatus>();
		List<LogisticStatus> mailReceiptLss = new ArrayList<LogisticStatus>();
		Query query2 = session.createQuery("from PackStatus where cycleDate in "
				+ " (select distinct cycleDate from ApplyData where oldBatchName in (:oldBatchNames)) "
				+ " and back = true and batchOrOnline = 'B' and center = '06' order by cycleDate");
		List<PackStatus> errorPackStatuses = query2.setParameterList("oldBatchNames", allPolicys).list();
		
		query2 = session.createQuery("from LogisticStatus where logisticId in "
				+ " (select logisticId from PackStatus where cycleDate in (select distinct cycleDate from ApplyData where oldBatchName "
				+ " in (:oldBatchNames)) and back = true and batchOrOnline = 'B' and center = '06') order by cycleDate");
		List<LogisticStatus> errorLogisticStatuses = query2.setParameterList("oldBatchNames", allPolicys).list();
		
		List<Area> auditCenters = session.getNamedQuery("Area.findHaveAddressAndAudit").list();			
		Area auditArea = null;
		for(Area audit : auditCenters){
		    if(audit.getAreaName() != null && audit.getAreaName().indexOf("北二審查科") >= 0){
			     auditArea = audit;
			}
		}
		Date today = new Date();
		ArrayList<ApplyData> policys = new ArrayList<ApplyData>();
		
		for(ApplyData applyData : applyDatas){								
			PackStatus ps = new PackStatus();
			LogisticStatus ls = new LogisticStatus();
			boolean errorMatch = false;
			//如果是抽件或錯誤，併到之前的，或是重新產生
			if((applyData.getExceptionStatus() != null && !"".equals(applyData.getExceptionStatus())) 
					|| (applyData.getSubstract() != null && applyData.getSubstract())){
				errorMatch = true;
				for(PackStatus pks : errorPackStatuses){
					if(pks.getCycleDate() != null && applyData.getCycleDate() != null && 
							pks.getCycleDate().getTime() == applyData.getCycleDate().getTime()){
						ps = pks;							
						break;
					}
				}					
				for(LogisticStatus lgs : errorLogisticStatuses){
					if(lgs.getCycleDate() != null && applyData.getCycleDate() != null &&
							lgs.getCycleDate().getTime() == applyData.getCycleDate().getTime()){
						ls = lgs;
						break;
					}
				}
				ps.setBack(true);
				ps.setSubAreaTel(auditArea.getTel());
				ps.setSubAreaId(auditArea.getAreaId());
				ps.setSubAreaName(auditArea.getAreaName());
				ps.setZipCode(auditArea.getZipCode());
				ps.setStatus(45);
				ps.setStatusNm("等待貨運");
				ps.setAreaAddress(auditArea.getZipCode() + " " + auditArea.getAddress());
				ps.setPolicyScanDate(today);
				ps.setReceiptScanDate(today);
				ps.setLabelScanDate(today);
				ps.setPackCompleted(true);
				ps.setPolicyScanUser("SYSTEM");
				ps.setReceiptScanUser("SYSTEM");
				ps.setLabelScanUser("SYSTEM");
				
				ls.setTel(auditArea.getTel());
				ls.setName(auditArea.getAreaName());
				ls.setAddress(auditArea.getZipCode() + " " + auditArea.getAddress());
				ls.setSentTime(cycleDate);
				ls.setPackDone(true);
				ls.setScanDate(today);
			
			}else if(applyData.getReceipt() == null || !applyData.getReceipt()){
				boolean mailReceipt = false;
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
				if(mailReceipt){					
				   //如果是保單，且雙掛號時，裝箱清單如果少於兩本保單，就加入
			       for(PackStatus pks : packStatuses){
				      if(pks.getCycleDate().getTime() == applyData.getCycleDate().getTime() 
					       && pks.getSubAreaName().equals(applyData.getReceiver())
						   && pks.getAreaAddress().equals(applyData.getAddress())
						   && pks.getBooks() < 2 ){
					      ps = pks;
				 	      break;
				      }
			      }
			      for(LogisticStatus lgs : logisticStatuses){
				      if(lgs.getCycleDate().getTime() == applyData.getCycleDate().getTime() 
					   	   && lgs.getName().equals(applyData.getReceiver())
						   && lgs.getAddress().equals(applyData.getAddress())
						   && (lgs.getBooks() == null || lgs.getBooks().intValue() < 2)){
					       ls = lgs;
					      break;
				      }
			      }
			   }else{
				   //單掛號時就全部放在一起就行了
				   for(PackStatus pks : packStatuses){
					   if(pks.getCycleDate().getTime() == applyData.getCycleDate().getTime() 
					       && pks.getSubAreaName().equals(applyData.getReceiver())
						   && pks.getAreaAddress().equals(applyData.getAddress())){
					      ps = pks;
					      break;
					   }
				   }
				   for(LogisticStatus lgs : logisticStatuses){
					   if(lgs.getCycleDate().getTime() == applyData.getCycleDate().getTime() 
					   	   && lgs.getName().equals(applyData.getReceiver())
						   && lgs.getAddress().equals(applyData.getAddress())){
					      ls = lgs;
					      break;
					   }
				   } 
			   }
			}else if(applyData.getReceipt() != null && applyData.getReceipt()){
				//如果是簽收單時，要看保單有沒有產生裝箱清單過
				//如果保單有產生裝箱清單，就放在一起
				for(ApplyData policy : policys){
					if(policy.getCycleDate().equals(applyData.getCycleDate()) 
							&& policy.getPolicyNos().equals(applyData.getPolicyNos())
							&& policy.getReprint().equals(applyData.getReprint())
							&& policy.getApplyNo().equals(applyData.getApplyNo())){
						String packId = policy.getPackId();
						for(PackStatus pks : packStatuses){
						   if(packId != null && packId.equals(pks.getPackId())){
							   ps = pks;
							   break;   
						   }
						}
						for(LogisticStatus lgs : logisticStatuses){
						   if(packId != null && packId.equals(lgs.getLogisticId())){
							   ls = lgs;
							   break;
						   }
						}							
					}
				}
			}
			
			Date cycleDate = applyData.getCycleDate();
			
			String address = applyData.getAddress();
			int adCount = ls.getBooks() == null? 0 : ls.getBooks();
			if(applyData.getReceipt() != null && !applyData.getReceipt()){
				adCount++;
			}
			String firstUniqueNo = ls.getFirstUniqueNo();
			if(firstUniqueNo == null || firstUniqueNo.trim().equals("")){
				firstUniqueNo = applyData.getUniqueNo();
			}
			String receiver = applyData.getReceiver();								
			
			int receipts = ls.getReceipts() == null? 0 : ls.getReceipts();
			if(applyData.getReceipt() != null && applyData.getReceipt()){
				receipts++;
			}										
			
			String packId = null;
			//String vendorId = null;
			String channelId = applyData.getChannelID();

			
			String deliverType = applyData.getDeliverType();
			boolean mailReceipt = true;
			//G類可能沒有回執聯
			if(channelId.toUpperCase().equals("G"))
				mailReceipt = false;
			//但如果是寄要保人，就要有回執聯
			if("S".equals(deliverType))
				mailReceipt = true;
			else if("B".equals(deliverType))
				mailReceipt = false;
			boolean packDone = false;
			//如果packStatus是錯誤件或是本身就載明退回北二的狀況
			if(applyData.getReceiver().equals("北二行政中心") 
					   || applyData.getReceiver().equals("北二審查科") 
					   || errorMatch){
				mailReceipt = false;
				packDone = true;
			}else{
				packDone = false;
			}
			
			boolean update = false;
			if(ps.getPackId() == null){
				update = false;
				//if(!packDone)
				   //vendorId = StringUtils.leftPad( (++registerNo) + "", this.registerNo.length(), '0');
				List<String> maxString =  query.setParameter(0, cycleDate).list();
				if(maxString != null && maxString.size() > 0 && maxString.get(0) != null){					
					packId = maxString.get(0);
					String suffix = StringUtils.leftPad((new Integer(packId.substring(packId.length() - 4)) + 1) + "", 4 , '0');
					packId = packId.substring(0, packId.length() - 4) + suffix;

				}else{						
					packId = Constant.yyMMdd.format(cycleDate) + "TPE" + StringUtils.leftPad("1", 4, '0');
				}	
			}else{
				update = true;
				packId = ps.getPackId();
				//vendorId = ls.getVendorId();
			}
			
			ls.setLogisticId(packId);				
			ls.setBooks(adCount);
			ls.setCenter("06");
			ls.setCycleDate(cycleDate);
			ls.setFirstUniqueNo(firstUniqueNo);
			if(!errorMatch){
			   ls.setName(receiver);
			   ls.setAddress(address);
			}
			ls.setPacks(1);
			ls.setReceipts(receipts);
			ls.setMailReceipt(mailReceipt);
			ls.setBatchOrOnline("B");					
			if(!packDone){
			   //ls.setVendorId(vendorId);			   					   
			}else{
			   ls.setSentTime(cycleDate);
			   ls.setPackDone(packDone);
			   ls.setScanDate(today);
			}								
			
			Set<String> newBatchNmSet = ps.getNewBatchNmSet();
			newBatchNmSet.add(applyData.getNewBatchName());
			
			String channelNm = applyData.getChannelName();				
			if(channelNm == null){
				channelNm = applyData.getSourceMap().get(applyData.getSourceCode());
			}
			
			ps.setPackId(packId);				
			if(errorMatch){
			   ps.setBack(true);
			   ps.setPackCompleted(true);
			   ps.setStatus(45);
			   ps.setStatusNm("等待貨運");
			   ps.setSubAreaId(auditArea.getSubAreaId());
			}else{
			   ps.setBack(false);
			   ps.setPackCompleted(false);
			   ps.setSubAreaName(receiver);
			   ps.setZipCode(applyData.getZip());
			   ps.setAreaAddress(address);
			   ps.setStatus(0);
			   ps.setStatusNm("裝箱準備");
			   ps.setSubAreaId(channelNm);
			}
			
			ps.setBatchOrOnline("B");
			ps.setBooks(adCount);
			ps.setCenter("06");
			ps.setCreateDate(today);
			ps.setCycleDate(cycleDate);
			ps.setFirstUniqueNo(firstUniqueNo);
			ps.setInusreCard(null);
			ps.setLogisticId(packId);
			ps.setNewBatchNmSet(newBatchNmSet);				
			ps.setReceipts(receipts);
			ps.setReported(false);												
			ps.setUpdateDate(today);

			 
			if(packDone){
			   ps.setStatus(45);
			   ps.setStatusNm("等待貨運");
			   ps.setPolicyScanDate(today);
			   ps.setReceiptScanDate(today);
			   ps.setLabelScanDate(today);
			   ps.setPolicyScanUser("SYSTEM");
			   ps.setReceiptScanUser("SYSTEM");
			   ps.setLabelScanUser("SYSTEM");					
			}
			
			
			Set<ApplyData> ads = ps.getApplyDatas();
			if(ads == null)
				ads = new HashSet<ApplyData>();
			ads.add(applyData);
			ps.setApplyDatas(ads);
			if(update){
			   session.update(ls);
			   session.update(ps);
			}else{					
			   session.save(ls);
			   session.save(ps);
			}
			applyData.setPackId(packId);
			applyData.setPackSatus(ps);
			if(applyData.getBkReceiptMatched() != null && applyData.getBkReceiptMatched() && applyData.getVerifyResult() != null && applyData.getVerifyResult().indexOf("尚未接收到送金單") >= 0){
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
					   if(!"".equals(verifyResult))
					      applyData.setVerifyResult(verifyResult);
					   else
						  applyData.setVerifyResult(null);
					   
				   }else{
					   //如果只有一個，表示之前驗單失敗是因為送金單未送達的關係，所以此時要把驗單失敗消除							   
					   applyData.setVerifyResult(null);
				   }				   
			}
			if(applyData.getExceptionStatus()== null && applyData.getVerifyResult() != null 
					&& (applyData.getVerifyResult().indexOf("尚未接收到送金單") >= 0
					|| (applyData.getVerifyResult().startsWith("送金單") && applyData.getVerifyResult().indexOf("連保單寄出") > 0)))
			   applyData.setVerifyResult(null);
			applyData.setUpdateDate(new Date());
			if(!packDone){
			   applyData.setPolicyStatus("60");
			}else{			   
			   if(ps.isBack())
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
					   if(adminUser != null)
					      br.setPackUser((adminUser.getUserName() == null || "".equals(adminUser.getUserName()))? adminUser.getUserId() : adminUser.getUserName());
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
			//保單放入此容器中
			if(applyData.getReceipt() == null || !applyData.getReceipt()){
				policys.add(applyData);
			}
			if(!update){
			   logisticStatuses.add(ls);				   
			   packStatuses.add(ps);
			   if(!ps.isBack() && ps.getSubAreaName() != null && !ps.getSubAreaName().equals("北二行政中心") && !mailReceipt){
				   resortPacks.add(ps);
				   resortLss.add(ls);
			   }else if(!ps.isBack() && mailReceipt){
				   mailReceiptLss.add(ls);
			   }
			}
		}
		
		//2015/06/29新增，將送國銀分行的再集中為一包		
		HashMap<String, LogisticStatus> lsMap = new HashMap<String, LogisticStatus>();
		for(LogisticStatus ls : resortLss){
			String address = ls.getAddress();				
		    String cycleDateStr = "";
		    if(ls.getCycleDate() != null)
		        cycleDateStr = Constant.yyMMdd.format(ls.getCycleDate());
			LogisticStatus firstLs = null;
			//如果地址對映的LogisticStatus是空的話，放入此map
			//之後所有的packStatus將對映到此LogisticStatus
			if((firstLs = lsMap.get(cycleDateStr + address)) == null){
				String receiver = ls.getName();
				String[] receivers = receiver.split(" ");
				if(receivers.length >= 2)
					ls.setName(receivers[0]);
				lsMap.put(cycleDateStr + address, ls);
			}
			//如果地址相同，但收件分行不同的話，分行要附加進去
			if(firstLs != null){ 
				String receiver = ls.getName();
				String[] receivers = receiver.split(" ");
				
				if(firstLs.getName() != null && ls.getName() != null 
						&& firstLs.getName().indexOf(receivers[0]) < 0){
					firstLs.setName(firstLs.getName() + "," + ls.getName());
					lsMap.put(cycleDateStr + address, firstLs);
				}
			}
		}		
		//接下來把packStatus對映的logisctId改變
		for(PackStatus ps : resortPacks){
			String address = ps.getAreaAddress();
			String cycleDateStr = "";
			if (ps.getCycleDate() != null)
				cycleDateStr = Constant.yyMMdd.format(ps.getCycleDate());
			LogisticStatus ls = lsMap.get(cycleDateStr + address);
			// 如果對映的logisticId和PackStatus的logisticId不同，就改變它
			if (ls != null
					&& !ls.getLogisticId().equals(ps.getLogisticId())) {
				int packs = ls.getPacks() == null ? 0 : ls.getPacks();
				packs++;
				ps.setLogisticId(ls.getLogisticId());
				// 重新計算books和receipts的數量
				ls.setBooks((ls.getBooks() == null ? 0 : ls.getBooks())
						+ ps.getBooks());
				ls.setReceipts((ls.getReceipts() == null ? 0 : ls
						.getReceipts()) + ps.getReceipts());
				ls.setPacks(packs);
				lsMap.put(cycleDateStr + address, ls);
				session.update(ps);
			}
		}
		//刪除其它的LogisticStatus，並加入掛號號碼
		int registerNum = new Integer(this.registerNo);
		for(LogisticStatus oldLs : resortLss){
			String cycleDateStr = "";
		    if(oldLs.getCycleDate() != null)
		        cycleDateStr = Constant.yyMMdd.format(oldLs.getCycleDate());
			LogisticStatus ls = lsMap.get(cycleDateStr + oldLs.getAddress());
			//如果是同一個時就update
			if(ls.getLogisticId().equals(oldLs.getLogisticId())){
				//String vendorId = StringUtils.leftPad(registerNum + "", this.registerNo.length(), '0');
				//ls.setVendorId(vendorId);
				//registerNum++;
				session.update(ls);
			}else{
				//不是同一個時就刪除
				session.delete(oldLs);
			}
		}
		//將雙掛號的掛號號碼也放進去			
		for(LogisticStatus ls : mailReceiptLss){
			String vendorId = StringUtils.leftPad(registerNum + "", this.registerNo.length(), '0');
			ls.setVendorId(vendorId);
			registerNum++;
			session.update(ls);
		}		
		// end of 2015/06/26修改
		return packStatuses;
	}
	
	public synchronized String producePacks(){
		
		autoOpen = false;
		if(this.uniqueNo != null && !this.uniqueNo.equals("")){
			return produceSinglePack();
		}
		if(center == null || "".equals(center)){
			setResult("請先選轄區再輸出檔案");
			return null;
		}
		AdminUser loginUser = (AdminUser)this.getRequest().getSession().getAttribute("loginUser");
		setResult("");
		Session session = null;
		Transaction tx = null;
		try{
			session = HibernateSessionFactory.getSession();
			//List<AfpFile> afpFiles = session.createQuery("select distinct afp from AfpFile afp join afp.applyDatas as applyData where applyData.cycleDate = ? and afp.center = ? and afp.pages is not null  and afp.pages > 0").setDate(0, cycleDate).setString(1, center).list();
			List<AfpFile> afpFiles = null;
			String queryStr = "from AfpFile afp where  newBatchName not like '%9999' and afp.cycleDate = ? ";
			
			if(center != null && !center.equals("")){
				queryStr += " and afp.center = '" + center + "'";
			}
			if(receipt != null && !receipt.equals("")){
				queryStr += " and afp.newBatchName like '" + receipt + "%'";
			}
			if(batchOrOnline != null && !batchOrOnline.equals("")){
				queryStr += " and afp.batchOrOnline = '" + batchOrOnline + "'";
			}
			
			afpFiles = session.createQuery(queryStr).setDate(0, cycleDate).list();
			if(afpFiles == null || afpFiles.size() == 0){
				setResult("此Cycle Date無資料");
				return null;
			}else{
				List<AfpFile> setToProduce = new ArrayList<AfpFile>();
				List<String> newBatchNames = new ArrayList<String>();
				for(AfpFile afpFile : afpFiles){
					String newBatchName = afpFile.getNewBatchName().toUpperCase();
					String status = afpFile.getStatus();
					//檢查狀態，不符合的不能輸出檔案
					if( (newBatchName.startsWith("CA") || newBatchName.startsWith("GA")) 
							 && "06".equals(center) && "驗單完成".equals(status) ){
						setResult("北二轄區需要配表完成才能輸出檔案");
						return null;
					}else if( (newBatchName.startsWith("CA") || newBatchName.startsWith("GA")) 
							&& !"驗單完成".equals(status) && !"配表中".equals(status) && !"配表完成".equals(status) && !"裝箱中".equals(status) && !"等待貨運".equals(status) && !"部分交寄".equals(status) && !"已交寄".equals(status)){
						if("06".equals(center)){
							setResult("北二轄區需要配表完成才能輸出檔案");
						}else
							setResult("狀態必須在驗單完成後才能輸出檔案");
						return null;
					}else if("已交寄".equals(status)){
						//setResult("目前的列印檔已完成交寄，如欲重新產生打包清單，請洽IT人員");
						if(loginUser != null){
		                   ActionHistory actionHistory = new ActionHistory();
			               actionHistory.setAction(afpFile.getNewBatchName() + "完成交寄，不產生打包清單");
			               actionHistory.setActionPageUrl("/fxdms/packPrepare.jspx");
			               actionHistory.setInsertDate(new Date());
			               actionHistory.setActionTime(new Date());
			               actionHistory.setUpdateDate(new Date());
			               actionHistory.setUserId(loginUser.getUserId());
			               actionHistory.setUserName(loginUser.getUserName());
			               session.save(actionHistory);
						}
						//return null;
					}else if(afpFile.getNewBatchName().startsWith("CA") || afpFile.getNewBatchName().startsWith("GA")){
						//CA或GA的才加入，因為是用保單去反查簽收單
						setToProduce.add(afpFile);						
						newBatchNames.add(newBatchName);
					}else{
						newBatchNames.add(newBatchName);
					}										
				}
				
				if(setToProduce.size() == 0){
					setResult("查無保單，無法產生 ");
					return null;
				}else{
					
					setResult("");
				}
				//檢查成功後開始update資料，準備輸出檔案
				tx = session.beginTransaction();
				int count1 = 0;
				int count2 = 0;
				//先刪除同一個cycleDate的所有裝箱清單及打包清單，並把狀態還原
				String query = "delete from PackStatus where packId in (select distinct packId from ApplyData where ((policyStatus <> '100' and policyStatus <> '98' and policyStatus <> '97') or policyStatus is null) and newBatchName in (:newBatchNames))";
				session.createQuery(query).setParameterList("newBatchNames", newBatchNames).executeUpdate();
				query = "delete from LogisticStatus where logisticId in (select distinct packId from ApplyData where ((policyStatus <> '100' and policyStatus <> '98' and policyStatus <> '97') or policyStatus is null) and newBatchName in (:newBatchNames))";
				session.createQuery(query).setParameterList("newBatchNames", newBatchNames).executeUpdate();				
				

				if(!center.equals("06")){
				   count1 = session.createQuery("update ApplyData set packId = null, policyStatus = '42' where ((policyStatus <> '100' and policyStatus <> '98' and policyStatus <> '97') or policyStatus is null) and newBatchName not like '%9999' and newBatchName in (:newBatchNames) and (exceptionStatus is null or exceptionStatus = '')").setParameterList("newBatchNames", newBatchNames).executeUpdate();
				   count2 = session.createQuery("update ApplyData set packId = null, policyStatus = '41' where ((policyStatus <> '100' and policyStatus <> '98' and policyStatus <> '97') or policyStatus is null) and newBatchName not like '%9999' and newBatchName in (:newBatchNames) and exceptionStatus = '41'").setParameterList("newBatchNames", newBatchNames).executeUpdate();
				   session.createQuery("update AfpFile set packIds = null, status = '驗單完成' where newBatchName not like '%9999' and newBatchName in (:newBatchNames) ").setParameterList("newBatchNames", newBatchNames).executeUpdate();
				}else{				
				   count1 = session.createQuery("update ApplyData set packId = null, policyStatus = '55' where ((policyStatus <> '100' and policyStatus <> '98' and policyStatus <> '97') or policyStatus is null) and newBatchName not like '%9999' and newBatchName in (:newBatchNames) and (exceptionStatus is null or exceptionStatus = '') ").setParameterList("newBatchNames", newBatchNames).executeUpdate();
				   count2 = session.createQuery("update ApplyData set packId = null, policyStatus = '41' where ((policyStatus <> '100' and policyStatus <> '98' and policyStatus <> '97') or policyStatus is null) and newBatchName not like '%9999' and newBatchName in (:newBatchNames) and exceptionStatus = '41' ").setParameterList("newBatchNames", newBatchNames).executeUpdate();
				   session.createQuery("update AfpFile set packIds = null, status = '配表完成' where newBatchName not like '%9999' and newBatchName in (:newBatchNames) ").setParameterList("newBatchNames", newBatchNames).executeUpdate();				
				}
				if(count1+ count2 == 0){
					setResult("所有保單均完成，無法產生寄件清單");
					tx.rollback();
					return null;
				}
					
				if(loginUser != null){
                   ActionHistory actionHistory = new ActionHistory();
	               actionHistory.setAction("產生打包清單");
	               actionHistory.setActionPageUrl("/fxdms/packPrepare.jspx");
	               actionHistory.setInsertDate(new Date());
	               actionHistory.setActionTime(new Date());
	               actionHistory.setUpdateDate(new Date());
	               actionHistory.setUserId(loginUser.getUserId());
	               actionHistory.setUserName(loginUser.getUserName());
	               actionHistory.setMessaage("產生打包清單|" + batchOrOnline + "|" + cycleDate + "|" + center);
	               session.save(actionHistory);
				}
				tx.commit();
				
				tx = session.beginTransaction();
				
				List<PackStatus> result = null;
				//北二的Batch件需要進行特別的打包
				AdminUser user = (AdminUser)this.getSession(true).getAttribute("loginUser");
				if(center.equals("06") && this.batchOrOnline.equals("B"))
				   result = ApplyDataService.prepareTaipeiNo2Pack(session, setToProduce,cycleDate, registerNo, user, parcelNo);
				else
				   result = ApplyDataService.preparePack(session, setToProduce,cycleDate, null, null, null);
				tx.commit();
				
				//非北二個險時調整寄件單位名稱
				if(!center.equals("06") || !this.batchOrOnline.equals("B")){
					tx = session.beginTransaction();                                                                   
					Query myQuery = session.createQuery("select distinct a.areaName, a.packId from ApplyData a where a.packSatus in (:packStatuses) and a.areaName is not null and a.areaName <> '' and a.receipt = false and cycleDate = :cycleDate order by packId");
					
					myQuery.setParameter("cycleDate", cycleDate);
					myQuery.setParameterList("packStatuses", result);
					List<Object[]> list = myQuery.list();
					for(PackStatus packStatus : result){
						if(!packStatus.isBack() 
								&& !"Table中找不到此服務中心".equals(packStatus.getSubAreaName())){
							String subAreaName = "";
							int querySize = list.size();
							for(int i = querySize - 1 ; i >= 0 ; i--){
								Object[] row = list.get(i);
								String areaName = row[0] + "";
								String packId = row[1] + "";
								if(packId.equals(packStatus.getPackId()) && !areaName.equals("")){
									subAreaName += areaName + ",";
									list.remove(i);
								}
							}
							if(subAreaName.endsWith(","))
								subAreaName = subAreaName.substring(0, subAreaName.length() - 1);
							if(!subAreaName.equals("")){
								packStatus.setSubAreaName(subAreaName);
								session.update(packStatus);
							}
						}
						   
					}
					tx.commit();
				}
				
				if(result != null && result.size() > 0)
				   setResult("產生裝箱清單成功");
				else
				   setResult("目前所有的列印檔都已產生裝箱清單，故不產生裝箱清單");				 
				query(false);				
			}
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			setResult("發生例外狀況:" + e.getMessage());
			e.printStackTrace();
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		
		
		return null;
	}
	
	public String getRowClasses(){
		String classes = "";
		
		for(int i = 0; i < dataModel.getRowCount(); i++){		   		
			dataModel.setRowIndex(i);		
		   PackStatus packStatus = (PackStatus)dataModel.getRowData();
		   if(packStatus.getStatus() >= 30 ){
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
	

	private String produceSinglePack() {
		Session session = null;
		Transaction tx = null;

		setResult("");
		try{
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			List<ApplyData> applyDatas = session.getNamedQuery("ApplyData.findByUniqueNo").setString(0, this.uniqueNo).list();
			if(applyDatas == null || applyDatas.size() == 0){
				setResult("無此保單，無法產生");
				return null;
			}
			ApplyData applyData = applyDatas.get(0);
			this.cycleDate = applyData.getCycleDate() == null? this.cycleDate : applyData.getCycleDate();
			List<AfpFile> afpFiles = new ArrayList<AfpFile>();
			afpFiles.add(applyData.getAfpFile());
			PackStatus oldPs = applyData.getPackSatus();
			LogisticStatus oldLs = null;
			if(oldPs != null)
				oldLs = oldPs.getLogisticStatus();
			if(oldPs != null && oldLs != null){
				setResult("已產生打包清單，不需再產生");
				return null;
			}
			List<PackStatus> packs = null;
			if(applyData.getCenter().equals("06") && !applyData.getSourceCode().equals("GROUP")){
				packs = this.pdTpePacks(session, applyDatas);
			}else{
				if(applyData.getExceptionStatus() != null && !applyData.getExceptionStatus().equals(""))
				    packs = ApplyDataService.preparePack(session, afpFiles, cycleDate, null, null, applyDatas);
				else 
					packs = ApplyDataService.preparePack(session, afpFiles, cycleDate, null, applyDatas, null);
			}
			setDataModel(new SortableModel(new ListDataModel(packs)));			
			tx.commit();
			
			//以下是產生重量
			double commonBagWg = 0;			
			double bigBagWg = 0;
			double mailReceiptWg = 0;
			double paperWg = 0;
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
				if(Constant.getWeightMap().get("內頁紙張") != null){
					paperWg = Constant.getWeightMap().get("內頁紙張");
				}
			}			
			tx = session.beginTransaction();
			Query query = session.createQuery("from ApplyData where receipt = false and packId = ? and (groupInsure = false or groupInsure is null)");
			List<String> logisticIds = new ArrayList<String>();			
			for(PackStatus ps : packs){
				if(!logisticIds.contains(ps.getLogisticId()) && ps.getLogisticId() != null)
					logisticIds.add(ps.getLogisticId());
				List<ApplyData> ads = query.setString(0, ps.getPackId()).list();
				double weight = 0;
				int booksCount = 0;
				for(ApplyData ad : ads){
					if(ad.getReceipt() != null && !ad.getReceipt())
						booksCount++;
					weight += ad.getWeight() == null? 0 : ad.getWeight();
				}
				if( booksCount >= 7){
					weight += bigBagWg;
				}else if(booksCount > 0){
					weight += commonBagWg;
				}
				ps.setWeight(weight);
				session.update(ps);
			}			
			tx.commit();
			
			tx = session.beginTransaction();
			query = session.createQuery("from PackStatus where logisticId = ?");			
			for(String logisticId : logisticIds){
				LogisticStatus ls = (LogisticStatus) session.get(LogisticStatus.class, logisticId);
				if(ls != null){
				   List<PackStatus> pss = query.setString(0, logisticId).list();
				   double weight = 0;
				   for(PackStatus pack : pss){
					   weight += pack.getWeight() == null? 0 : pack.getWeight(); 
				   }
				   if(pss.size() > 1){
					   weight += bigBagWg;
				   }
				   if(ls.getMailReceipt() != null && ls.getMailReceipt())
					   weight += mailReceiptWg;
				   else
					   weight += pss.size() * paperWg;
				   ls.setWeight(weight);
				   session.update(ls);
				}				
			}						
			tx.commit();
			
			
			setResult("產生打包清單成功");
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			setResult(e.getMessage());
		}finally{
			if(session != null)
				session.close();
		}
		return null;
	}

	public String query(boolean cleanResult){
		totalFiles = 0;
		totalBooks = 0;
		totalPages = 0;
		totalSheets = 0;
		totalReceipts = 0;

		queryResult = new ArrayList<JqgridAfpFile>();
    	if(cleanResult)
    	   setResult("");
		String errMsg = validData();
		
		List<AfpFile> list = null;
		Session session = null;
		int rowNum = 0;
		try{
			session = HibernateSessionFactory.getSession();						
			String queryStr = "select distinct afp from AfpFile afp where  afp.cycleDate = ? ";
						
			if(center != null && !center.equals("")){
				queryStr += " and afp.center = '" + center + "'";
			}
			if(receipt != null && !receipt.equals("")){
				queryStr += " and afp.newBatchName like '" + receipt + "%'";
			}
			if(batchOrOnline != null && !batchOrOnline.equals("")){
				queryStr += " and afp.batchOrOnline = '" + batchOrOnline + "'";
			}
			
			queryStr += " order by afp.newBatchNo, afp.vipSetTime";			
			Query query = session.createQuery(queryStr);
			if(cycleDate != null)
			   query.setDate(0, cycleDate);
			list = query.list();

			if(list != null){
			   rowNum = list.size();
			   totalFiles = rowNum;
			   int i = 0; 
			   Date now = new Date();
			   for(AfpFile afpFile : list ){
				   i++;
				   JqgridAfpFile jqAfp = new JqgridAfpFile();
				   BeanUtils.copyProperties(afpFile, jqAfp);
				   if(cycleDate != null)
				      jqAfp.setCycleDateStr(Constant.slashedyyyyMMdd.format(cycleDate));
				   jqAfp.setNewBatchNo((long)i);
				   Set<ApplyData> applyDatas = afpFile.getApplyDatas();
				   int volumns = applyDatas == null ? 0 : applyDatas.size();
				   //計算總本數
				   if(afpFile.getNewBatchName().startsWith("CA"))
				      totalBooks += volumns;
				   else if(afpFile.getNewBatchName().startsWith("SG"))
					  totalReceipts += volumns;
				   jqAfp.setVolumns(volumns);
				   
				   //計算總頁數
				   totalPages += afpFile.getPages() == null ? 0 : afpFile.getPages();
				   totalSheets += afpFile.getSheets() == null ? 0 : afpFile.getSheets();
				   
				   queryResult.add(jqAfp);
				   
			   }
			   Criteria  criteria = session.createCriteria(PackStatus.class, "packStatus");
			   if(this.uniqueNo != null && !this.uniqueNo.equals(""))					   
				   criteria.createAlias("packStatus.applyDatas", "applyDatas", Criteria.INNER_JOIN);
			   criteria.add(Restrictions.eq("packStatus.cycleDate", cycleDate));
			   criteria.add(Restrictions.eq("packStatus.center", center));			   
			   criteria.add(Restrictions.eq("packStatus.batchOrOnline", batchOrOnline));
			   if(this.uniqueNo != null && !this.uniqueNo.equals("")){
				   criteria.add(Restrictions.eq("applyDatas.uniqueNo", uniqueNo));
			   }
			   criteria.addOrder(Order.asc("packStatus.status"));
			   criteria.addOrder(Order.asc("packStatus.firstUniqueNo"));
			   criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			   List<PackStatus> packs = criteria.list();
			   setDataModel(new SortableModel(new ListDataModel(packs)));
			   if(packId != null){				  
			      criteria = session.createCriteria(ApplyData.class);
			      if(packId.endsWith("P") || packId.endsWith("L") || packId.endsWith("R"))
			          criteria.add(Restrictions.eq("packId", packId.substring(0, packId.length() - 1)));
			      criteria.addOrder(Order.asc("packTime"));
			      criteria.addOrder(Order.asc("receipt"));
			      List<ApplyData> applyDatas = criteria.list();
			      setAdDataModel(new SortableModel(new ListDataModel(applyDatas)));
			   }
			}
			
		}catch(Exception e){
			logger.error("", e);
			setResult("發生錯誤：" + e.getMessage());
			this.saveSysLog(true, e, "AFP_QUERY", "列印檔查詢錯誤", cycleDate + ":" + center);
		}finally{
			if(session != null)
				session.close();
		}
		if(rowNum == 0 )
			this.totalPage = 0;
		else
			this.totalPage = 1;
		
		setJsonResult(" = " + ToJqGridString.beansToJqGridLocalData(queryResult));  //把資料轉成jQGrid所需的JSON格式後設到本class的property            
		if (errMsg.equals("")) {
			return "success";
		} else {
			// 錯誤訊息寫入sbForm:errMsg
			setResult("查詢失敗");
			return "failure";
		}
    	
    }
	//查詢
	public String doQuery() {
		setResult("");
		autoOpen = false;
	    return query(true);
	}
	
	public String afpQuery() {
		totalFiles = 0;
		totalBooks = 0;
		totalPages = 0;
		totalSheets = 0;
		totalReceipts = 0;

		queryResult = new ArrayList<JqgridAfpFile>();    	
    	setResult("");
		String errMsg = validData();
		
		List<AfpFile> list = null;
		Session session = null;
		int rowNum = 0;
		try{
			session = HibernateSessionFactory.getSession();						
			String queryStr = "select distinct afp from AfpFile afp join afp.applyDatas as applyData where applyData.cycleDate between ? and ? ";
			
			if(center != null && !center.equals("")){
				queryStr += " and afp.center = '" + center + "'";
			}if(receipt != null && !receipt.equals("")){
				queryStr += " and afp.newBatchName like '" + receipt + "%'";
			}
			String queryStr2 = queryStr + " order by afp.serialNo";
			
			queryStr += " order by afp.newBatchNo, afp.vipSetTime";			
			Query query = session.createQuery(queryStr);
			query.setDate(0, cycleDate);
			query.setDate(1, cycleDateEnd);
			
			list = query.list();

			if(list != null){
			   rowNum = list.size();
			   totalFiles = rowNum;
			   int i = 0; 
			   for(AfpFile afpFile : list ){
				   i++;
				   JqgridAfpFile jqAfp = new JqgridAfpFile();
				   BeanUtils.copyProperties(afpFile, jqAfp);
				   if(cycleDate != null)
				      jqAfp.setCycleDateStr(Constant.slashedyyyyMMdd.format(cycleDate));
				   jqAfp.setNewBatchNo((long)i);				
				   Set<ApplyData> applyDatas = afpFile.getApplyDatas();
				   int volumns = applyDatas == null ? 0 : applyDatas.size();
				   //計算總本數
				   if(afpFile.getNewBatchName().startsWith("CA"))
				      totalBooks += volumns;
				   else if(afpFile.getNewBatchName().startsWith("SG"))
					  totalReceipts += volumns;
				   jqAfp.setVolumns(volumns);
				   
				   //計算總頁數
				   totalPages += afpFile.getPages() == null ? 0 : afpFile.getPages();
				   totalSheets += afpFile.getSheets() == null ? 0 : afpFile.getSheets();
				   
				   queryResult.add(jqAfp);
				   
			   }
			   query = session.createQuery(queryStr2);
			   if(cycleDate != null)
			       query.setDate(0, cycleDate);
			   list = query.list();

			   i = 0;
			   for(AfpFile afpFile : list ){
				   i++;
				   for(JqgridAfpFile jqAfpFile : queryResult){
					   if(jqAfpFile.getNewBatchName().equals(afpFile.getNewBatchName())){
						   jqAfpFile.setSerialNo((long)i);

					   }
				   }
			   }
			   Criteria  criteria = session.createCriteria(PackStatus.class);
			   criteria.add(Restrictions.eq("cycleDate", cycleDate));
			   criteria.add(Restrictions.eq("center", center));
			   criteria.addOrder(Order.asc("status"));
			   List<PackStatus> packs = criteria.list();
			   setDataModel(new SortableModel(new ListDataModel(packs)));
			   if(packId != null){
				  
			      criteria = session.createCriteria(ApplyData.class);
			      if(packId.endsWith("P") || packId.endsWith("L") || packId.endsWith("R"))
			          criteria.add(Restrictions.eq("packId", packId.substring(0, packId.length() - 1)));
			      criteria.addOrder(Order.asc("packTime"));
			      criteria.addOrder(Order.asc("receipt"));
			      List<ApplyData> applyDatas = criteria.list();
			      setAdDataModel(new SortableModel(new ListDataModel(applyDatas)));
			   }
			}
			
		}catch(Exception e){
			logger.error("", e);
			this.saveSysLog(true, e, "AFP_QUERY", "列印檔查詢錯誤", cycleDate + ":" + center);
		}finally{
			if(session != null)
				session.close();
		}
		if(rowNum == 0 )
			this.totalPage = 0;
		else
			this.totalPage = 1;
		
		setJsonResult(" = " + ToJqGridString.beansToJqGridLocalData(queryResult));  //把資料轉成jQGrid所需的JSON格式後設到本class的property            
		if (errMsg.equals("")) {
			return "success";
		} else {
			// 錯誤訊息寫入sbForm:errMsg
			setResult("查詢失敗");
			return "failure";
		}

	}


	public String getCenter() {
		return center;
	}

	public void setCenter(String center) {
		this.center = center;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Set<SelectItem> getMyCenters() {
		if(myCenters == null){
			myCenters = new LinkedHashSet<SelectItem>();
			AdminUser adminUser = (AdminUser)this.getSession(true).getAttribute("loginUser");
			Map<String, String> centerMap = ApplyData.getCenterMap();
			if(adminUser.getCenter() == null || "".equals(adminUser.getCenter().trim())){				
				Set<String> keySet = new TreeSet<String>(centerMap.keySet());
				
				SelectItem si = new SelectItem();
				si.setLabel("全部");
				si.setValue("");
				myCenters.add(si);
				for(String key : keySet){
					SelectItem selectItem = new SelectItem();
					selectItem.setValue(key);
					selectItem.setLabel(centerMap.get(key));
					myCenters.add(selectItem);
				}				
			}else{
				SelectItem selectItem = new SelectItem();
				selectItem.setValue(adminUser.getCenter());
				selectItem.setLabel(centerMap.get(adminUser.getCenter()));
				myCenters.add(selectItem);
			}
		}
		return myCenters;
	}
	public Set<SelectItem> getCenters() {
		if(centers == null){
			centers = new LinkedHashSet<SelectItem>();
			AdminUser adminUser = (AdminUser)this.getSession(true).getAttribute("loginUser");
			Map<String, String> centerMap = ApplyData.getCenterMap();
			if(adminUser.getCenter() == null || "".equals(adminUser.getCenter().trim())){				
				Set<String> keySet = new TreeSet<String>(centerMap.keySet());
                ArrayList<String> keyList = new ArrayList<String>();
                keyList.addAll(keySet);
				for(int i = keyList.size() - 1 ; i >= 0 ; i-- ){
					String key = keyList.get(i);
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

	public void setMyCenters(Set<SelectItem> myCenters) {
		this.myCenters = myCenters;
	}


	public Date getCycleDate() {
		return cycleDate;
	}

	public void setCycleDate(Date cycleDate) {
		this.cycleDate = cycleDate;
	}

	public String getJsonResult() {
		return jsonResult;
	}

	public void setJsonResult(String jsonResult) {
		this.jsonResult = jsonResult;
	}


	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	public Integer getTotalFiles() {
		return totalFiles;
	}

	public void setTotalFiles(Integer totalFiles) {
		this.totalFiles = totalFiles;
	}

	public Integer getTotalBooks() {
		return totalBooks;
	}

	public void setTotalBooks(Integer totalBooks) {
		this.totalBooks = totalBooks;
	}

	public Integer getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(Integer totalPages) {
		this.totalPages = totalPages;
	}

	public Integer getTotalSheets() {
		return totalSheets;
	}

	public void setTotalSheets(Integer totalSheets) {
		this.totalSheets = totalSheets;
	}

	public Integer getTotalReceipts() {
		return totalReceipts;
	}

	public void setTotalReceipts(Integer totalReceipts) {
		this.totalReceipts = totalReceipts;
	}

	public String getReceipt() {
		return receipt;
	}

	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}

	public List<JqgridAfpFile> getQueryResult() {
		return queryResult;
	}

	public void setQueryResult(List<JqgridAfpFile> queryResult) {
		this.queryResult = queryResult;
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

	public HashMap<PackStatus, Boolean> getChecked() {
		return checked;
	}

	public void setChecked(HashMap<PackStatus, Boolean> checked) {
		this.checked = checked;
	}

	public String getPackIds() {
		return packIds;
	}

	public void setPackIds(String packIds) {
		this.packIds = packIds;
	}

	public String getPackId() {
		return packId;
	}

	public void setPackId(String packId) {
		this.packId = packId;
	}
	
	//rowClasses="odd_row,even_row"
	public String getRowClases(){
		String classes = "";
		
		for(int i = 0; i < dataModel.getRowCount(); i++){		   		
		   dataModel.setRowIndex(i);		
		   PackStatus packStatus = (PackStatus)dataModel.getRowData();
		   if(packStatus.isBack()){
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
	
	public String getAdRowClasses(){
		String classes = "";
		
		for(int i = 0; i < adDataModel.getRowCount(); i++){		   		
		   adDataModel.setRowIndex(i);		
		   ApplyData applyData = (ApplyData)adDataModel.getRowData();
		   if(new Integer(applyData.getPolicyStatus()) >= 95){
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

	public String getInputNo() {
		return inputNo;
	}

	public void setInputNo(String inputNo) {
		this.inputNo = inputNo;
	}

	public UIData getAdDataTable() {
		return adDataTable;
	}

	public void setAdDataTable(UIData adDataTable) {
		this.adDataTable = adDataTable;
	}

	public SortableModel getAdDataModel() {
		return adDataModel;
	}

	public void setAdDataModel(SortableModel adDataModel) {
		this.adDataModel = adDataModel;
	}
	public Date getCycleDateEnd() {
		return cycleDateEnd;
	}

	public void setCycleDateEnd(Date cycleDateEnd) {
		this.cycleDateEnd = cycleDateEnd;
	}

	public boolean isGroup() {
		return group;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}

	public String getRegisterNo() {
		return registerNo;
	}

	public void setRegisterNo(String registerNo) {
		this.registerNo = registerNo;
	}

	public String getBatchOrOnline() {
		return batchOrOnline;
	}

	public void setBatchOrOnline(String batchOrOnline) {
		this.batchOrOnline = batchOrOnline;
	}

	public boolean isAutoOpen() {
		return autoOpen;
	}

	public void setAutoOpen(boolean autoOpen) {
		this.autoOpen = autoOpen;
	}

	public String getUniqueNo() {
		return uniqueNo;
	}

	public void setUniqueNo(String uniqueNo) {
		this.uniqueNo = uniqueNo;
	}

	public String getParcelNo() {
		return parcelNo;
	}

	public void setParcelNo(String parcelNo) {
		this.parcelNo = parcelNo;
	}
	
}
