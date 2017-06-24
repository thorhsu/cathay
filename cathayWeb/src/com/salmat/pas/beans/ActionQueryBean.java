package com.salmat.pas.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import javax.faces.model.SelectItem;


import com.salmat.pas.bo.AdmMenuService;
import com.salmat.pas.bo.AdminUserService;
import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.vo.ActionHistory;
import com.salmat.pas.vo.AdmPageList;
import com.salmat.pas.vo.AdminUser;
import com.salmat.util.ToJqGridString;

/*
 * Thor新增
 */
public class ActionQueryBean extends BaseBean {
	private Date startDate;  
	private Date endDate;    
	private String action;
	private String userId = "";
	
    private Integer totalPage;  //共幾頁，分頁使用
	private String result;   //後端處理結果
	private String jsonResult;  //前端jQGrid要使用的result
    public static final int rowNum = 16;  //一頁幾列，分頁使用
    private List<SelectItem> allUserIds = null;
    private List<SelectItem> allPages = null;
    private AdminUserService admUserService = null;
    private AdmMenuService admMenuService = null;
    private String contextPath = ServletContextGetter.getServletContextPath();
    
   


	public List<SelectItem> getAllUserIds() {
		if(allUserIds == null){
			List<SelectItem> sortItems = new ArrayList<SelectItem>();
			List<AdminUser> allUserList = admUserService.findAllUser();
			sortItems.add(new SelectItem("", "全部"));
			for(AdminUser adminUser : allUserList) {
				sortItems.add(new SelectItem(adminUser.getUserId(), adminUser.getUserId() + " " + adminUser.getUserName()));
			}
			allUserIds = sortItems;
		}
		return allUserIds;
	}
	
	
	public List<SelectItem> getAllPages(){
		if(allPages == null){
			List<SelectItem> sortItems = new ArrayList<SelectItem>();			
			List<AdmPageList> allPageList = admMenuService.findAllPage();
			sortItems.add(new SelectItem("", "全部"));
			for(AdmPageList admPage : allPageList) {
				if(admPage.getUrl() != null && !"".equals(admPage.getUrl()) && ! (ServletContextGetter.getServletContextPath() + "/login.jsp").equals(admPage.getUrl())&& (admPage.getUrl().endsWith(".jsp") || admPage.getUrl().endsWith(".jspx") || admPage.getUrl().endsWith(".html") || admPage.getUrl().endsWith(".htm")) ){
				   sortItems.add(new SelectItem(admPage.getUrl(), admPage.getName()));
				   if(admPage.getUrl().equals( ServletContextGetter.getServletContextPath() +  "/secure/system/adminUserEdit.jspx")){
					   sortItems.add(new SelectItem(ServletContextGetter.getServletContextPath() +  "/secure/system/adminUserMaintain.jspx/delteUser", "帳號刪除"));
				   }
				}
			}
			allPages = sortItems;
		}
		return allPages;
		
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}


	public void setAllUserIds(List<SelectItem> allUserIds) {
		this.allUserIds = allUserIds;
	}

	public static int getRownum() {
		return rowNum;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	public Integer getTotalPage() {
		if(totalPage == null)
			return 0;
		else
		    return totalPage;
	}

	public ActionQueryBean() {
	}

	
	public String validData() {
		return "";
	}

	//delete方法，由forumMaintain.jsp呼叫

	//查詢
	public String doQuery() {
		
			setResult("");
			String errMsg = validData();
			
			List<ActionHistory> list = null;
			list = admMenuService.getActionHistoryByCriteria(startDate, endDate, //依查詢條件查詢
						action, userId, 0, rowNum); 
			
			int count = admMenuService.getActionHistoryCount(startDate, endDate, //依查詢條件查詢
					action, userId); 
		
			if(rowNum == 0 )
				this.totalPage = 0;
			else
			    this.totalPage = count/rowNum ;   //總頁數計算
			
			if(rowNum != 0 && count % rowNum != 0)
				     this.totalPage = totalPage + 1;
			
			setJsonResult(ToJqGridString.beansToJqGridLocalData(list));  //把資料轉成jQGrid所需的JSON格式後設到本class的property

			if (errMsg.equals("")) {
				return "success";
			} else {
				// 錯誤訊息寫入sbForm:errMsg
				setResult("查詢失敗");
				return "failure";
			}
		 

	}


	public String getJsonResult() {
		if(jsonResult == null)
			return "''";
		else
		    return jsonResult;
	}

	public void setJsonResult(String jsonResult) {
		this.jsonResult = jsonResult;
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
	public AdminUserService getAdmUserService() {
		return admUserService;
	}

	public void setAdmUserService(AdminUserService admUserService) {
		this.admUserService = admUserService;
	}
	
	public AdmMenuService getAdmMenuService() {
		return admMenuService;
	}

	public void setAdmMenuService(AdmMenuService admMenuService) {
		this.admMenuService = admMenuService;
	}

}
