package com.salmat.pas.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;




import org.apache.myfaces.component.html.ext.HtmlSelectBooleanCheckbox;
import org.apache.myfaces.component.html.ext.SortableModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.salmat.pas.bo.AdminUserService;
import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.vo.ActionHistory;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.AdminUserHistory;
import com.salmat.util.HibernateSessionFactory;

public class AdminUserMaintainBean extends BaseBean {
	private String userId;
	private String status;
	private AdminUserService adminUserService;
	
	private UIData dataTable;
	private SortableModel dataModel;
	private String result;

	public AdminUserMaintainBean() {
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
	private List<AdminUser> getSelectedList() {
		List<AdminUser> selected = new ArrayList<AdminUser>();
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
							selected.add((AdminUser) dataTable.getRowData());
						}
					}
				}
			}
		}
		return selected;
	}

	@SuppressWarnings("unchecked")
	public String del() {
		setResult("");
		List<AdminUser> dataList = null;
		if(dataModel == null) {
			setResult("請重新查詢資料！");
			return "success";
		} else {
			dataList = (List<AdminUser>)((ListDataModel)dataModel.getWrappedData()).getWrappedData();
		}
		List<AdminUser> selList = getSelectedList();
		if(selList.isEmpty()) {
			setResult("請勾選要刪除的資料！");
		} else {
			//檢查勾選的刪除資料中是否包含目前登入的帳號，若有則不允許刪除
			for(AdminUser data : selList) {
				if(data.getUserId().equals(getAcegiUser().getUsername())) {
					setResult("刪除資料失敗：無法刪除目前登入帳號！");
					return "failure";
				}
			}
			AdminUser loginUser = (AdminUser)this.getSession(true).getAttribute("loginUser");
			for(AdminUser data : selList) {
				logger.info("Delete admin user: " + data.getId());
				dataList.remove(data);
				dataModel.setWrappedData(new ListDataModel(dataList));
				resetDataScroller(null);
				try{
					/*
					Set<AdminUserHistory> set = data.getAdminUserHistory();
					for(AdminUserHistory userHistory : set){
						adminUserService.getAdminUserDao().getHiberTemplate().delete(userHistory);
					}
					*/
					adminUserService.delete(data, getAcegiUser().getUsername());
					
					
					Session session = HibernateSessionFactory.getSession();
					Transaction tx = session.beginTransaction();
					//因為只靠url判斷不出來
			        ActionHistory actionHistory = new ActionHistory();
			        actionHistory.setAction("刪除使用者" + data.getUserId() + ":" + data.getUserName());
			        actionHistory.setActionPageUrl(ServletContextGetter.getServletContextPath() + "/secure/system/adminUserMaintain.jspx/delteUser");
			        Date now = new Date();
			        actionHistory.setInsertDate(now);
			        actionHistory.setActionTime(now);
			        actionHistory.setUpdateDate(now);
			        if(loginUser != null){
			           actionHistory.setUserId(loginUser.getUserId());
			           actionHistory.setUserName(loginUser.getUserName());
			        }
                    session.save(actionHistory);
                    tx.commit();
                    session.close();

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
		List<AdminUser> queryResult = getAdminUserService().findByCritrial(
				getUserId(), getStatus()) ;
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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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


	public AdminUserService getAdminUserService() {
		return adminUserService;
	}

	public void setAdminUserService(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

}
