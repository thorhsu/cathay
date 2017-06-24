package com.salmat.pas.beans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;

import com.salmat.pas.bo.ApplyDataService;
import com.salmat.pas.report.GenerateReport;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.ApplyData;
import com.salmat.util.HibernateSessionFactory;
import com.salmat.util.ToJqGridString;


/*
 * Thor新增
 */
public class PdfViewQueryBean extends BaseBean {
	static Logger logger = Logger.getLogger(PdfViewQueryBean.class);
	private Date startDate;  
	private Date endDate;    
	private UIData dataTable;
	private SortableModel dataModel;
   
    private Integer totalPage;  //共幾頁，分頁使用
    private String center;
	private String result;   //後端處理結果
	private Set<SelectItem> myCenters;
	private Set<SelectItem> sourceCodes;
	private String applyNo;
	private String exception;
	private String policyNo;
	private String oldBatchName;
	private String policyStatus;
	private Set<SelectItem> policyStatuses;
	private String insureId;
	private String areaId;
	private String recName;
	private String receipt = "false";
	private String sourceCode;
	private Boolean groupInsure;
	private AdminUser adminUser = null;
	private String policyOrReceipt;
	private int pageRows = 30; 
	private List<ApplyData> list = null;
	
	
	private String jsonResult;  //前端jQGrid要使用的result
    public static final int rowNum = 30;  //一頁幾列，分頁使用
    

	public Integer getTotalPage() {
		if(totalPage == null)
			return 0;
		else
		    return totalPage;
	}

	public PdfViewQueryBean() {
	    adminUser = (AdminUser)this.getSession(true).getAttribute("loginUser");
	}

	
	public String validData() {
		return "";
	}

	//update vip及抽件方法
	public String update() throws ParseException{
		setResult("");
		HttpServletRequest request = this.getRequest();
		String oldBatchName = request.getParameter("oldBatchName");
		logger.info("oldBatchName:" + oldBatchName);
		String vipStr = request.getParameter("vipStr");
		String substractStr = request.getParameter("substractStr");
		boolean vip;
		boolean substract;
        vip = "V".equals(vipStr)? true : false;
        substract = "V".equals(substractStr)? true : false;
		if(vipStr != null && substractStr != null && vipStr.equals(substractStr)){
			this.setResult("不可同時抽件和VIP");
			return "failure";
		}
		
		String status = ApplyDataService.updateVip(oldBatchName, vip, substract, adminUser);
		if(status == null){
			this.setResult("資料庫找無此筆資料，請重新搜尋一次");
			return "failure";
		}else if(status.equals("更新成功")){
			this.setResult(status);
			query(false);
		} else {
			this.setResult(status);
			return "failure";
		}
		return null;
	}
	public String exportExcel(){
		try{
			if(this.list == null || this.list.size() == 0){
				setResult("查無資料，無法輸出報表。請重新查詢");
				return null;
			}
			String targetName = GenerateReport.generateCheckResult(this.list);				
			this.getRequest().setAttribute("reportNameForDownload", targetName);
			return "download";	
		}catch(Exception e){
			logger.error("", e);
			this.setResult("發生異常：" + e.getMessage());
			return null;
		}
		
		
	}
	public String queryCheckResult(){
		if(dataTable != null) 
			dataTable.setFirst(0);
		this.setPageRows(30);
		setResult("");
		String errMsg = validData();
		int count = 0;		
		if(adminUser == null)
		    adminUser = (AdminUser)this.getSession(true).getAttribute("loginUser");
		String adminUserCenter = adminUser.getCenter();
		if(adminUserCenter != null && !"".equals(adminUserCenter) && !adminUserCenter.equals(center)){
			setResult("不得查詢非自己服務中心的保單");
			return "failure";
		}			
		String beforeStatus = null;
		String afterStatus = null;
		Boolean exceptionQuery = null;
		Boolean receipt = null;
        if(!"null".equals(exception) && !"true".equals(exception) && !"false".equals(exception)){
        	exceptionQuery = false;
        	beforeStatus = exception;
        }else if("null".equals(exception)){
        	exceptionQuery = null;
        	afterStatus = null;
        }else if("true".equals(exception)){
        	exceptionQuery = true;
        }else if("false".equals(exception)){
        	exceptionQuery = false;
        	afterStatus = "42";
        }
        String policyStatus = null;
        if("28".equals(exception)){
        	policyStatus = "28";
        }
		Boolean groupInsureCard = null;
		String policyOrReceipt = null;
		if("policy".equals(this.policyOrReceipt)){
			groupInsureCard = false;
			policyOrReceipt = "false";
		}else if("receipt".equals(this.policyOrReceipt)){
			groupInsureCard = false;
			policyOrReceipt = "true";
		}else if("card".equals(this.policyOrReceipt)){
			groupInsureCard = true;
			policyOrReceipt = "null";
		}
		String[] orderColumns = new String[3];
		orderColumns[0] = "cycleDate";
		orderColumns[1] = "applyNo";
		orderColumns[2] = "policyNos";
		list = ApplyDataService.getApplyData(startDate, endDate, center, applyNo, policyNo, oldBatchName, 
					policyStatus, insureId, recName, areaId, 0, 300000, orderColumns, "asc", exceptionQuery, beforeStatus, null, policyOrReceipt, afterStatus, groupInsureCard);
		if("40".equals(exception)){
			List<ApplyData> newList = new ArrayList<ApplyData>();
        	for(ApplyData ad : list){
        		if(!"28".equals(ad.getPolicyStatus())){
        			newList.add(ad);
        		}
        	}
        	list = newList;
        }else if("true".equals(exception)){
        	List<ApplyData> newList = new ArrayList<ApplyData>();
        	for(ApplyData ad : list){
        		if(ad.getSubstract() != null && ad.getSubstract() && ad.getReceipt()  != null && !ad.getReceipt()){
        			ad.setVerifyResult("審查科" + (ad.getSubstractModifiderName() == null? "" : ad.getSubstractModifiderName()) + "通知抽件");
        		}
        		newList.add(ad);
        	}
        	list = newList;
        }
		setDataModel(new SortableModel(new ListDataModel(list)));
		resetDataScroller(null);
		return null;

	}
    public String query(boolean cleanResult){
    	if(cleanResult)
    	   setResult("");
		String errMsg = validData();
		int count = 0;
		List<ApplyData> list = null;
		if(adminUser == null)
		    adminUser = (AdminUser)this.getSession(true).getAttribute("loginUser");
		String adminUserCenter = adminUser.getCenter();
		if(adminUserCenter != null && !"".equals(adminUserCenter) && !adminUserCenter.equals(center)){
			setResult("不得查詢非自己服務中心的保單");
			return "failure";
		}
		Boolean exceptionQuery = null;
		String beforeStatus = null;
		String afterStatus = null; 
		
        if(!"null".equals(exception) && !"true".equals(exception) && !"false".equals(exception)){
        	exceptionQuery = false;
        	beforeStatus = exception;
        }else if("null".equals(exception)){
        	exceptionQuery = null;
        	afterStatus = null;
        }else if("true".equals(exception)){
        	exceptionQuery = true;
        }else if("false".equals(exception)){
        	exceptionQuery = false;
        	afterStatus = "42";
        }        
        
		count = ApplyDataService.getApplyDataCount(startDate, endDate, center, applyNo, policyNo, oldBatchName, 
					policyStatus, insureId, recName, areaId, exceptionQuery, beforeStatus, sourceCode, this.receipt, afterStatus, null);		
		list = ApplyDataService.getApplyData(startDate, endDate, center, applyNo, policyNo, oldBatchName, 
					policyStatus, insureId, recName, areaId, 0, rowNum, null, null, exceptionQuery, beforeStatus, sourceCode, this.receipt, afterStatus, null);
        						            					
		if(rowNum == 0 )
			this.totalPage = 0;
		else
		    this.totalPage = count/rowNum ;   //總頁數計算
		
		if(rowNum != 0 && count % rowNum != 0)
			this.totalPage = totalPage + 1;
		
		List<PdfViewQueryJqGridBean> jqGridBeans = new ArrayList<PdfViewQueryJqGridBean>();
		for (ApplyData applyData : list) {
			String sourceCode = applyData.getSourceCode();
			PdfViewQueryJqGridBean bean = new PdfViewQueryJqGridBean();   //用來轉成jQgrid所需資料的bean
			BeanUtils.copyProperties(applyData, bean);
			bean.setSourceCode(ApplyData.getSourceMap().get(bean.getSourceCode()));
			String recName = applyData.getRecName() == null ? "" : applyData.getRecName().trim();
			if(!"GROUP".equals(sourceCode)){
			   switch (recName.length()){
		         case 0:
		            break;
		         case 1:
		    	    recName = "＊";
		    	    break;
		         case 2:
		    	    recName = recName.substring(0, 1) + "＊";
		    	    break;
		         default:				    	 
		    	    String begin = recName.substring(0, 1);
		    	    String end = recName.substring(recName.length() - 1);
		    	    String middle = "";
		    	    for(int i = 1 ; i < recName.length() - 1 ; i++){
		    		    String single = recName.substring(i, i+1);
		    		    if(!single.equals(" ") && !single.equals("　"))
		    		       middle += "＊";
		    		    else
		    			   middle += single;
		    	    }
		    	    recName = begin + middle + end;				    	 
		       }
		    }
			bean.setRecName(recName);
			bean.setPolicyStatus(applyData.getPolicyStatusName());
			//bean.setSourceCode(applyData.getSourceName());
			bean.setCenter(applyData.getCenterName());
			bean.setPackType(applyData.getPackTypeName());
			
			jqGridBeans.add(bean);
		}
		setJsonResult(ToJqGridString.beansToJqGridLocalData(jqGridBeans));  //把資料轉成jQGrid所需的JSON格式後設到本class的property            
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
	   return query(true);
	}
	public void resetDataScroller(ActionEvent e) {
		if(dataTable != null) dataTable.setFirst(0);
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
	
	public Set<SelectItem> getSourceCodes() {
		if(sourceCodes == null){
			sourceCodes = new LinkedHashSet<SelectItem>();			
			Map<String, String> centerMap = ApplyData.getSourceMap();							
			Set<String> keySet = new TreeSet<String>(centerMap.keySet());
			SelectItem si = new SelectItem();
			si.setLabel("全部");
			si.setValue("");
			sourceCodes.add(si);
			for(String key : keySet){
				SelectItem selectItem = new SelectItem();
				selectItem.setValue(key);
				selectItem.setLabel(centerMap.get(key));
				sourceCodes.add(selectItem);
			}				
			
		}
		return sourceCodes;
	}

	public void setMyCenters(Set<SelectItem> myCenters) {
		this.myCenters = myCenters;
	}

	public String getApplyNo() {
		return applyNo;
	}

	public void setApplyNo(String applyNo) {
		this.applyNo = applyNo;
	}

	public String getPolicyNo() {
		return policyNo;
	}

	public void setPolicyNo(String policyNo) {
		this.policyNo = policyNo;
	}

	public String getOldBatchName() {
		return oldBatchName;
	}

	public void setOldBatchName(String oldBatchName) {
		this.oldBatchName = oldBatchName;
	}

	public String getPolicyStatus() {
		return policyStatus;
	}

	public void setPolicyStatus(String policyStatus) {
		this.policyStatus = policyStatus;
	}

	public Set<SelectItem> getPolicyStatuses() {
		if(policyStatuses == null){
			policyStatuses = new LinkedHashSet<SelectItem>();
			
			Map<String, String> policyStatusMap = ApplyData.getPolicyStatusMap();
		    TreeSet<String> keySet = new TreeSet<String>(policyStatusMap.keySet());
			SelectItem si = new SelectItem();
			si.setLabel("全部");
			si.setValue("");
			policyStatuses.add(si);
			for(String key : keySet){
				//100是已交寄，是最後一項，要放到最後面
				if(!"100".equals(key)){
				   SelectItem selectItem = new SelectItem();
				   selectItem.setValue(key);
				   selectItem.setLabel(policyStatusMap.get(key));
				   policyStatuses.add(selectItem);
				}
			}				
			SelectItem selectItem = new SelectItem();
			selectItem.setValue("100");
			selectItem.setLabel(policyStatusMap.get("100"));
			policyStatuses.add(selectItem);
			
		}
		return policyStatuses;
	}

	public void setPolicyStatuses(Set<SelectItem> policyStatuses) {
		this.policyStatuses = policyStatuses;
	}

	public String getInsureId() {
		return insureId;
	}

	public void setInsureId(String insureId) {
		this.insureId = insureId;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String getJsonResult() {
		return jsonResult;
	}

	public void setJsonResult(String jsonResult) {
		this.jsonResult = jsonResult;
	}

	public static int getRownum() {
		return rowNum;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	public String getRecName() {
		return recName;
	}

	public void setRecName(String recName) {
		this.recName = recName;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
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

	public String getReceipt() {
		return receipt;
	}

	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public Boolean getGroupInsure() {
		return groupInsure;
	}

	public void setGroupInsure(Boolean groupInsure) {
		this.groupInsure = groupInsure;
	}

	public int getPageRows() {
		return pageRows;
	}

	public void setPageRows(int pageRows) {
		this.pageRows = pageRows;
	}

	public String getPolicyOrReceipt() {
		return policyOrReceipt;
	}

	public void setPolicyOrReceipt(String policyOrReceipt) {
		this.policyOrReceipt = policyOrReceipt;
	}


}
