package com.salmat.pas.servlet;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import com.salmat.pas.bo.AdmMenuService;
import com.salmat.pas.bo.AdminUserService;
import com.salmat.pas.vo.ActionHistory;
import com.salmat.util.ApplicationContextFactory;
import com.salmat.util.ToJqGridString;

public class ActionQueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private int rowNum = 20;
	private AdmMenuService admMenuService;
	private AdmMenuService getMenuService() {
		if (admMenuService == null)
			admMenuService = (AdmMenuService) ApplicationContextFactory
					.getApplicationContext().getBean("admMenuService");
		return admMenuService;
	}
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActionQueryServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected synchronized void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		getMenuService();
		response.setContentType("text/json;charset=UTF-8");
		String startDateParam = request.getParameter("startDate");
		String endDateParam = request.getParameter("endDate");
		String action = request.getParameter("action");
		String userId = request.getParameter("userId");
		String page = request.getParameter("page"); //查第幾頁
		Date startDate = null;
		Date endDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		
		
		if(startDateParam != null && !startDateParam.trim().equals("")){
			try {
				startDate = sdf.parse(startDateParam);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(endDateParam != null && !endDateParam.trim().equals("")){
			try {
				endDate = sdf.parse(endDateParam);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int startRow = 0;
		
		if(page != null && !page.trim().equals("")){
			int startPage = 1;	
			try{
			   startPage = Integer.parseInt(page.trim()) - 1 ;
			   if(startPage < 0)
				   startPage = 1;
			}catch(Exception e){
				e.printStackTrace();
			}
			startRow = startPage * rowNum;
		}
		
		
		List<ActionHistory> list = null;
		list = admMenuService.getActionHistoryByCriteria(startDate, endDate, //依查詢條件查詢
					action, userId, startRow, rowNum); 
		
		int count = admMenuService.getActionHistoryCount(startDate, endDate, //依查詢條件查詢
				action, userId); 
		
		//rowNum = count;   //目前不分頁，全部只有一頁。如果有需要修改時再改
	
		int totalPage = count/rowNum ;   //總頁數計算
		
		if(rowNum == 0 )
			totalPage = 0;
		if(rowNum != 0 && count % rowNum != 0)
			    totalPage = totalPage + 1;
		
		List<Object []> jqList = new ArrayList();
		List<ActionHistory> jqGridBeans = new ArrayList();
		  
		for (ActionHistory actionHistory : list) {
			
			jqGridBeans.add(actionHistory);
			//id', '操作人員ID', '人員姓名', '操作行為', '操作時間'
			Object [] objArray = new Object[7];
			objArray[0] = actionHistory.getId();
			objArray[1] = actionHistory.getUserId();
			objArray[2] = actionHistory.getUserName();
			objArray[3] = actionHistory.getAction();
			objArray[4] = actionHistory.getActionDateTime();
			jqList.add(objArray);
		}
		
		String returnJsonString = "{\"page\":\"" + page + "\",\"total\":" + totalPage + ",\"records\":\"" + list.size() + "\",\"rows\":" + ToJqGridString.toJqGridString(jqList)
				+ "}";
		/*
		returnJsonString = "{ total: \"" + totalPage + "\", page: \"" + page + "\", records: \""
			+ list.size()
			+ "\", rows : "
			+ ToJqGridString.toJqGridString(jqList) + "}";   //生成jqGrid所需JSON字串
		*/

	    response.getWriter().write(returnJsonString);
		//setJsonResult();  //把資料轉成jQGrid所需的JSON格式後設到本class的property

		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
