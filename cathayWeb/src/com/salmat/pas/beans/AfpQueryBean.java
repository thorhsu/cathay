package com.salmat.pas.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;

import com.salmat.pas.conf.Constant;
import com.salmat.pas.report.GenerateReport;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.JqgridAfpFile;
import com.salmat.util.HibernateSessionFactory;
import com.salmat.util.ToJqGridString;


/*
 * Thor新增
 */
public class AfpQueryBean extends BaseBean {
	static Logger logger = Logger.getLogger(AfpQueryBean.class);
	private Date cycleDate;  
	private Date cycleDateEnd;
    private Integer totalPage;  //共幾頁，分頁使用
    private String center = "06";
	private String result;   //後端處理結果
	private Set<SelectItem> myCenters;
	private AdminUser adminUser = null;
	private List<JqgridAfpFile> queryResult; 
	private Integer totalFiles;
	private Integer totalBooks;
	private Integer totalReceipts;
	private Integer totalPages;
	private Integer totalSheets;
	private Integer totalCards;
	private Integer totalVolumns;
	private String receipt;
	private String groupInsure;
	
	//新契約保單數</th>
	private int norms;
    //新契約保單張數</th>
    private int normSheets;		                                       
    //保補契轉保單數</th>
    private int convs;
    //保補契轉保單張數</th>
    private int convSheets;                             		                                       		                                       		                                      
    //個險印製簽收回條數</th>
    private int printedReceipts;
    //個險免印製簽收回條數</th>
    private int notPrinteds; 
    //團險保單數</th>
    private int groups;
    //團險保單張數</th>
    private int groupSheets;		                                       
    //團險簽收回條數</th>
    private int groupReceipts;
    //團險證張數</th>
    private int insureCards;
	
	
	
	
	private String jsonResult;  //前端jQGrid要使用的result
    
    

	public Integer getTotalPage() {
		if(totalPage == null)
			return 0;
		else
		    return totalPage;
	}

	public AfpQueryBean() {
		if(adminUser == null)
	       adminUser = (AdminUser)this.getSession(true).getAttribute("loginUser");
	}

	
	public String validData() {
		return "";
	}
	
	public String exportExcel(){
		if(queryResult == null || queryResult.size() == 0){
			setResult("無資料可供輸出報表，請先查詢後再輸出報表");
			return null;
		}
		try {
			String targetName = GenerateReport.generateAfpExcel(queryResult, totalFiles, totalBooks, totalReceipts, totalPages, totalSheets, cycleDate, cycleDateEnd, center);
			this.getRequest().setAttribute("reportNameForDownload", targetName);
		} catch (Exception e) {
			setResult(e.getMessage());
			e.printStackTrace();
			return null;
		}
		return "download";
	}

	public String query(boolean cleanResult){
		queryResult = new ArrayList<JqgridAfpFile>();
    	if(cleanResult)
    	   setResult("");
		String errMsg = validData();
		
		List<AfpFile> list = null;
		Session session = null;
		int rowNum = 0;
		try{
			session = HibernateSessionFactory.getSession();
			if((this.cycleDateEnd.getTime() - this.cycleDate.getTime()) > 90L * 24 * 60 * 60 * 1000){
				this.setResult("查詢區間請勿超過90天");
				return null;
			}
			if(adminUser.getCenter() != null && !"".equals(adminUser.getCenter())){
				center = adminUser.getCenter();
			}
			//String queryStr = "select distinct afp from AfpFile afp join afp.applyDatas as applyData where applyData.cycleDate between ? and ?";
			String queryStr = "from AfpFile where cycleDate between ? and ?";
			
			if(center != null && !center.equals("")){
				queryStr += " and center = '" + center + "'";
			}if(receipt != null && !receipt.equals("")){
				if(receipt.equals("CA"))
				   queryStr += " and (newBatchName like '" + receipt + "%' or newBatchName like 'GA%')";
				else if(receipt.equals("SG"))
				   queryStr += " and (newBatchName like '" + receipt + "%' or newBatchName like 'GG%')";
				else
				   queryStr += " and newBatchName like '" + receipt + "%' ";
			}
			if("personal".equals(this.getGroupInsure())){
				queryStr += " and batchOrOnline = 'B' ";
			}else if("group".equals(this.getGroupInsure())){
				queryStr += " and batchOrOnline = 'G' ";
			}
			String queryStr2 = queryStr + " order by serialNo";
			
			queryStr += " order by cycleDate, newBatchNo";			
			Query query = session.createQuery(queryStr);
			query.setDate(0, cycleDate);
			query.setDate(1, cycleDateEnd);
			list = query.list();
			
		    query = session.createQuery(queryStr2);
		    query.setDate(0, cycleDate);
		    query.setDate(1, cycleDateEnd);
		    //list2 = query.list();

            HashSet<String> newBatchNames = new HashSet<String>();
			if(list != null){				
			   rowNum = list.size();
			   totalFiles = rowNum;
			   //新列印順序
			   /*                            		                                       		                                       		                                      
               //個險印製簽收回條數</th>
               private int printedReceipts;
               //個險免印製簽收回條數</th>
               private int notPrinteds; 
               //團險保單張數</th>
               private int groupSheets;		                                       
               //團險簽收回條數</th>
               private int groupReceipts;
               //團險證張數</th>
               private int insureCards;
			    */
			   
			   printedReceipts = 0;
               notPrinteds = 0; 
               groupSheets = 0;		                                       
               groupReceipts = 0;
               insureCards = 0;
               totalReceipts = 0;
			   int i = 0; 
			   for(AfpFile afpFile : list ){
                   String newBatchName = afpFile.getNewBatchName();				   
				   newBatchNames.add(newBatchName);
				   if(newBatchName.startsWith("SG") && newBatchName.endsWith("9999")){
					   notPrinteds += afpFile.getPages();
					   totalReceipts += afpFile.getPages();
				   }else if(newBatchName.startsWith("SG") && !newBatchName.endsWith("9999")){
					   printedReceipts += afpFile.getPages();
					   totalReceipts += afpFile.getPages();
				   }else if(newBatchName.startsWith("GA")){
					   groupSheets += afpFile.getPages() / 2;
					   if(afpFile.getPages() % 2 == 1)
						   groupSheets++;
				   }else if(newBatchName.startsWith("GG")){
					   groupReceipts += afpFile.getPages();	
					   totalReceipts += afpFile.getPages();
				   }else if(newBatchName.startsWith("PD")){
					   insureCards += afpFile.getPages();					   
				   }
				   
				   i++;
				   JqgridAfpFile jqAfp = new JqgridAfpFile();
				   BeanUtils.copyProperties(afpFile, jqAfp);
				   if(afpFile.getCycleDate() != null)
				      jqAfp.setCycleDateStr(Constant.slashedyyyyMMdd.format(afpFile.getCycleDate()));
				   jqAfp.setNewBatchNo((long)i);				   
				   							   
				   //計算總頁數
				   totalPages += afpFile.getPages() == null ? 0 : afpFile.getPages();
				   totalSheets += afpFile.getSheets() == null ? 0 : afpFile.getSheets();
				   if(newBatchName.endsWith("9999")){
					   jqAfp.setVolumns(afpFile.getPages());
				   }
				   queryResult.add(jqAfp);
				   
			   }
			   Query query1 = session.createQuery("select count(a), sum(totalPage), sourceCode, receipt, groupInsure, newBatchName from ApplyData as a where newBatchName in (:newBatchNames) and newBatchName not like '%9999' group by sourceCode, receipt, groupInsure, newBatchName");
               query1.setParameterList("newBatchNames", newBatchNames);
               List<Object[]> groupResults = query1.list();
               
               norms = 0;
               //新契約保單張數</th>
               normSheets = 0;		                                       
               //保補契轉保單數</th>
               convs = 0;
               //保補契轉保單張數</th>
               convSheets = 0;  
               //團險保單數</th>
               groups = 0;
               totalBooks = 0;
               for(Object[] row : groupResults){
            	   String sourceCode = (String)row[2];
            	   Boolean receipt = (Boolean)row[3];
            	   Boolean insureCard = (Boolean)row[4];
            	   if(sourceCode.equals("GROUP") && receipt != null && !receipt && (insureCard == null || !insureCard)){
            		   groups += (Long)row[0];
            		   totalBooks += ((Long)row[0]).intValue();
            	   }else if((sourceCode.equals("NORM") || sourceCode.equals("REPT")) && receipt != null && !receipt && (insureCard == null || !insureCard)){
            		   norms += (Long)row[0];
            		   totalBooks += ((Long)row[0]).intValue();
            		   normSheets += ((Long)row[1]) / 2;
            		   if(((Long)row[1]) % 2 == 1)
            		      normSheets++;
            	   }else if((sourceCode.equals("CONV") || sourceCode.equals("REIS")) && receipt != null && !receipt && (insureCard == null || !insureCard)){
            		   convs += (Long)row[0];
            		   totalBooks += ((Long)row[0]).intValue();
            		   convSheets += ((Long)row[1]) / 2;
            		   if(((Long)row[1]) % 2 == 1)
            		      convSheets++;
            	   }
            	   
               }               
               for(JqgridAfpFile jqAfp : queryResult){
            	   String newBatchName = jqAfp.getNewBatchName();
            	   String firstTwo = newBatchName.substring(0, 2);
            	   switch(firstTwo){
            	       case "SG":
            	    	   jqAfp.setDescription("個險簽收回條");
            	    	   break;
            	       case "GG":
            	    	   jqAfp.setDescription("團險簽收回條");
            	    	   break;
            	       case "PD":
            	    	   jqAfp.setDescription("團險證");
            	    	   break;
            	       case "GA":
            	    	   jqAfp.setDescription("團險保單");
            	    	   break;
            	       case "CA":
            	    	   jqAfp.setDescription("個險保單");
            	    	   break;
            	   
            	   }
            	   for(Object[] row : groupResults){
            		   if(row[5].toString().equals(newBatchName)){
            			   int volumn = ((Long)row[0]).intValue();            			   
                           jqAfp.setVolumns(jqAfp.getVolumns() + volumn);            			   
            		   } 
            		   
            	   }
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
		
		setJsonResult(ToJqGridString.beansToJqGridLocalData(queryResult));  //把資料轉成jQGrid所需的JSON格式後設到本class的property            
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
		totalFiles = 0;
		totalBooks = 0;
		totalPages = 0;
		totalSheets = 0;
		totalCards = 0;
        totalVolumns = 0;
		totalReceipts = 0;
	    return query(true);
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

	public Date getCycleDateEnd() {
		return cycleDateEnd;
	}

	public void setCycleDateEnd(Date cycleDateEnd) {
		this.cycleDateEnd = cycleDateEnd;
	}

	public Integer getTotalVolumns() {
		return totalVolumns;
	}

	public void setTotalVolumns(Integer totalVolumns) {
		this.totalVolumns = totalVolumns;
	}

	public Integer getTotalCards() {
		return totalCards;
	}

	public void setTotalCards(Integer totalCards) {
		this.totalCards = totalCards;
	}

	public String getGroupInsure() {
		return groupInsure;
	}

	public void setGroupInsure(String groupInsure) {
		this.groupInsure = groupInsure;
	}

	public int getNorms() {
		return norms;
	}

	public void setNorms(int norms) {
		this.norms = norms;
	}

	public int getNormSheets() {
		return normSheets;
	}

	public void setNormSheets(int normSheets) {
		this.normSheets = normSheets;
	}

	public int getConvs() {
		return convs;
	}

	public void setConvs(int convs) {
		this.convs = convs;
	}

	public int getConvSheets() {
		return convSheets;
	}

	public void setConvSheets(int convSheets) {
		this.convSheets = convSheets;
	}

	public int getPrintedReceipts() {
		return printedReceipts;
	}

	public void setPrintedReceipts(int printedReceipts) {
		this.printedReceipts = printedReceipts;
	}

	public int getNotPrinteds() {
		return notPrinteds;
	}

	public void setNotPrinteds(int notPrinteds) {
		this.notPrinteds = notPrinteds;
	}

	public int getGroups() {
		return groups;
	}

	public void setGroups(int groups) {
		this.groups = groups;
	}

	public int getGroupSheets() {
		return groupSheets;
	}

	public void setGroupSheets(int groupSheets) {
		this.groupSheets = groupSheets;
	}

	public int getGroupReceipts() {
		return groupReceipts;
	}

	public void setGroupReceipts(int groupReceipts) {
		this.groupReceipts = groupReceipts;
	}

	public int getInsureCards() {
		return insureCards;
	}

	public void setInsureCards(int insureCards) {
		this.insureCards = insureCards;
	}
	

}
