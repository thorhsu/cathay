package com.salmat.pas.beans;

import java.util.ArrayList;

import java.util.List;


import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.apache.myfaces.component.html.ext.HtmlSelectBooleanCheckbox;
import org.apache.myfaces.component.html.ext.SortableModel;

import com.salmat.pas.bo.AdmMenuService;
import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.vo.AdmPageList;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AdminUserRole;



public class AuthorityMaintainBean extends BaseBean { 
	private String userRole;
	private String isShow;
	private AdmMenuService admMenuService;
	private AdminUserRole adminPageUserRole;
	//此權限可存取的第一層選單
	private List<String> accessPageList1;
	//此權限可存取的第二層選單
	private List<String> accessPageList2;
	private UIData dataTable;
	private SortableModel dataModel;
	private String result;
	//第一層選單
	private static List<SelectItem> secondPages;
	//第二層選單
	private static List<SelectItem> firstPages;
	private List<String> accessedList = null;

	public AuthorityMaintainBean() {
	}

	/**
	 * 寫入資料庫前的資料驗證
	 * 
	 * @return 錯誤訊息
	 */
	public String validData() {
		return "";
	}

	
	// method to determine the selected items
	private List<AdminUserRole> getSelectedList() {
		List<AdminUserRole> selected = new ArrayList<AdminUserRole>();
		int first = dataTable.getFirst();
		int rows = dataTable.getRows();
		for (int i = first; i < (first + rows); i++) {
			dataTable.setRowIndex(i);
			List <UIComponent> columnList = dataTable.getChildren();
			for(UIComponent column : columnList) {
				List<UIComponent> columnChildren = column.getChildren();
				for (int n = 0; n < columnChildren.size(); n++) {
					if (columnChildren.get(n) instanceof HtmlSelectBooleanCheckbox) {
						HtmlSelectBooleanCheckbox tmpCb = (HtmlSelectBooleanCheckbox) columnChildren
								.get(n);
						if (tmpCb.getId().equals("cbSelOne")
								&& (Boolean) (tmpCb.getValue() == Boolean.TRUE)) {
							selected.add((AdminUserRole) dataTable.getRowData());
						}
					}
				}
			}
		}
		return selected;
	}

	public String add() {
		String user = ((AdminUser)this.getSession(true).getAttribute("loginUser")).getUserId();
		int first = (accessPageList1 == null)? 0 : accessPageList1.size();
		int second = (accessPageList2 == null)? 0 : accessPageList2.size();
		String[] urls = new String[first + second ];
		for(int i = 0 ; i < first ; i++){
			urls[i] = accessPageList1.get(i);
		}
		for(int i = 0 ; i < second ; i++){
			urls[i + first] = accessPageList2.get(i);
		}
		try{
		    boolean exist = admMenuService.addNewAuthority(adminPageUserRole.getUserRole(), urls, user, adminPageUserRole.getIsShow(), adminPageUserRole.getUserRoleName(), adminPageUserRole.getUserRoleDesc(), adminPageUserRole.getCenterOnly());
		    if(exist){
			   setResult("儲存失敗，此角色名已存在或有類似的角色名，請另選角色名");
			   return "failure";
		    }
		    retrieveData();
		    resetDataScroller(null);
		    setResult("新增成功");
		    return "success";
		}catch(Exception e){
			setResult(e.getMessage());
			return "failure";
		}
	}
	
	public String edit() {
		String user = ((AdminUser)this.getSession(true).getAttribute("loginUser")).getUserId();
		int first = (accessPageList1 == null)? 0 : accessPageList1.size();
		int second = (accessPageList2 == null)? 0 : accessPageList2.size();
		String[] urls = new String[first + second ];
		for(int i = 0 ; i < first ; i++){
			urls[i] = accessPageList1.get(i);
		}
		for(int i = 0 ; i < second ; i++){
			urls[i + first] = accessPageList2.get(i);
		}
		try{
			System.out.println( adminPageUserRole.getCenterOnly());
		    admMenuService.editAuthority(adminPageUserRole.getUserRole(), urls, user, adminPageUserRole.getIsShow(), adminPageUserRole.getUserRoleName(), adminPageUserRole.getUserRoleDesc(), adminPageUserRole.getCenterOnly());
		    retrieveData();
		    resetDataScroller(null);
		    setResult("修改成功");
		    return "success";
		}catch(Exception e){
			setResult(e.getMessage());
			return "failure";
		}
	}
	
	
	public String toAdd(){
		setResult("");
		this.setAdminPageUserRole(null);
		this.accessedList = null;
		init();
		return "toAdd";
	}
	
	public String toEdit(){
		String admUserRole = this.getParameter("adminUserRole");
		setResult("");
		this.setAdminPageUserRole(admMenuService.findByUserRole(admUserRole));
		this.accessedList = admMenuService.getAccessUrls(admUserRole);
		init();
		return "toEdit";
	}
	
	

	@SuppressWarnings("unchecked")
	public String del() {
		setResult("");
		List<AdminUserRole> dataList = null;
		if(dataModel == null) {
			setResult("請重新查詢資料！");
			return "success";
		} else {
			dataList = (List<AdminUserRole>)((ListDataModel)dataModel.getWrappedData()).getWrappedData();
		}
		List<AdminUserRole> selList = getSelectedList();
		if(selList.isEmpty()) {
			setResult("請勾選要刪除的資料！");
		} else {
			//檢查勾選的刪除資料中是否包含目前登入的帳號，若有則不允許刪除
			for(AdminUserRole data : selList) {
				String userRole = data.getUserRole();
				if(admMenuService.findUserByeRole(userRole)){
					setResult("刪除資料失敗：此角色目前已有使用者套用中！");
					return "failure";
				}
				
			}
			for(AdminUserRole data : selList) {
				//logger.info("Delete admin user: " + data.getId());
				dataList.remove(data);
				dataModel.setWrappedData(new ListDataModel(dataList));
				resetDataScroller(null);
				try{
					admMenuService.delUserRole(data.getUserRole());
				} catch(Exception ex) { //樂觀鎖定例外
					logger.error("", ex);
					setResult("刪除資料失敗：" + ex.getMessage());
					return "failure";
				}
				logger.info("Web Page is deleted successfully!");
			}
			setResult("刪除資料完成！");
		}
		return "success";
	}
	
	/**
	 * 取得與設定頁面資料，包含Data model與url map
	 * @param tmpPid
	 */
	public void retrieveData() {
		setResult("");
		List<AdminUserRole> queryResult = getAdmMenuService().findByCritical(
				getUserRole(), getIsShow()) ;
		setDataModel(new SortableModel(new ListDataModel(queryResult)));
	}
	
	/**
	 * 
	 * @return
	 */
	public String query() {
		
		setResult("");
		String errMsg = validData();
		if (errMsg.equals("")) {
			retrieveData();
			resetDataScroller(null);
			return "success";
		} else {
			//錯誤訊息寫入sbForm:errMsg
			setResult(errMsg);
			return "failure";
		}
	}
	
	/**
	 * For頁面查詢事件後呼叫
	 * @param e
	 */
	public void resetDataScroller(ActionEvent e) {
		if(dataTable != null) dataTable.setFirst(0);
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

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getIsShow() {
		return isShow;
	}

	public void setIsShow(String isShow) {
		this.isShow = isShow;
	}

	public AdmMenuService getAdmMenuService() {
		return admMenuService;
	}

	public void setAdmMenuService(AdmMenuService admMenuService) {
		this.admMenuService = admMenuService;
	}

	public AdminUserRole getAdminPageUserRole() {
		if(this.adminPageUserRole == null)
			this.adminPageUserRole = new AdminUserRole();
		return adminPageUserRole;
	}

	public void setAdminPageUserRole(AdminUserRole adminPageUserRole) {
		this.adminPageUserRole = adminPageUserRole;
	}
	public List<String> getAccessPageList1() {
		/*
		if(accessedList == null){
		   init();	
		}
		*/
		return accessPageList1;
	}

	public void setAccessPageList1(List<String> accessPageList1) {
		this.accessPageList1 = accessPageList1;
	}

	
	public List<String> getAccessPageList2() {
		/*
		if(accessedList == null){
		   init();	
		}
		*/		
		return accessPageList2;
	}

	public void setAccessPageList2(List<String> accessPageList2) {
		this.accessPageList2 = accessPageList2;
	}
	
	public List<SelectItem> getSecondPages(){
		if(secondPages == null){
			init();
		}
		return secondPages;	
	}
	
	public List<SelectItem> getFirstPages(){
		if(firstPages == null){
			init();
		}
		return firstPages;
		
	}
	
	public void init(){
		
		List<SelectItem> secondItems = new ArrayList<SelectItem>();
		List<SelectItem> firstItems = new ArrayList<SelectItem>();
		List<AdmPageList> allPageList = admMenuService.findByAuthority();
		accessPageList2 = new ArrayList();
	    accessPageList1 = new ArrayList();
		for(AdmPageList admPage : allPageList) {
			
			if(admPage.getParentId() != null && admPage.getParentId() != 0){
			   secondItems.add(new SelectItem(admPage.getUrl() , admPage.getName()));
               if(accessedList != null && accessedList.contains(admPage.getUrl().replaceAll(ServletContextGetter.getServletContextPath(), ""))){
            	   accessPageList2.add(admPage.getUrl());
               }
			}else if((admPage.getParentId() == null || admPage.getParentId() == 0) && !admPage.getName().equals("登出") && !admPage.getName().equals("密碼修改")){
			   firstItems.add(new SelectItem(admPage.getUrl() , admPage.getName()));
			   if(accessedList != null && accessedList.contains(admPage.getUrl())){
            	   accessPageList1.add(admPage.getUrl() );
               }
			}else if(admPage.getName().equals("登出") || admPage.getName().equals("密碼修改")){
				firstItems.add(new SelectItem(admPage.getUrl() , admPage.getName(), "", true));			
            	accessPageList1.add(admPage.getUrl() );
	               
			}
		}
		secondPages = secondItems;
		firstPages = firstItems;
		
		
	}

}
