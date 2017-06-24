package com.salmat.pas.servlet;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;

import com.salmat.pas.beans.PdfViewQueryBean;
import com.salmat.pas.beans.PdfViewQueryJqGridBean;
import com.salmat.pas.bo.ApplyDataService;
import com.salmat.pas.conf.Constant;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.LogisticStatus;
import com.salmat.util.HibernateSessionFactory;
import com.salmat.util.ToJqGridString;

public class PdfQueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(PdfQueryServlet.class);
	private int rowNum = PdfViewQueryBean.rowNum;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PdfQueryServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected synchronized void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json;charset=UTF-8");
        
		String query = request.getParameter("query");
		logger.info("query method:" + query);
		if ("pageSplit".equals(query)) {			
			String startDateParam = request.getParameter("startDate");
			String endDateParam = request.getParameter("endDate");			
			String center = request.getParameter("center");
			String applyNo = request.getParameter("applyNo");
			String policyNo = request.getParameter("policyNo");
			String insureId = request.getParameter("insureId");			
			String areaId = request.getParameter("areaId");
			String policyStatus = request.getParameter("policyStatus");
			String recName = request.getParameter("recName");
			String sourceCode = request.getParameter("sourceCode");
			String receiptStr = request.getParameter("receipt");
			String exception = request.getParameter("exception");
			boolean groupInsure = request.getParameter("groupInsure").equals("true")? true : false;
			 
			Boolean exceptionQuery = null;
			String beforeStatus = null;
			String afterStatus = null;
			String receipt = null;
	        if(!"null".equals(exception) && !"true".equals(exception) && !"false".equals(exception)){
	        	exceptionQuery = false;
	        	beforeStatus = exception;
	        }else if("null".equals(exception)){
	        	exceptionQuery = null;
	        }else if("true".equals(exception)){
	        	exceptionQuery = true;
	        }else if("false".equals(exception)){
	        	exceptionQuery = false;
	        	afterStatus = "42";
	        }	        
	        receipt = receiptStr;
	        
			
			
			String sidx = request.getParameter("sidx");
			if(sidx.endsWith("Str"))
				sidx = sidx.substring(0, sidx.length() - 3);
			String sord = request.getParameter("sord");
			 
			String page = request.getParameter("page"); // 查第幾頁
			
			Date startDate = null;
			Date endDate = null;			

			if (startDateParam != null && !startDateParam.trim().equals("")) {
				try {
					startDate = sdf.parse(startDateParam);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (endDateParam != null && !endDateParam.trim().equals("")) {
				try {
					endDate = sdf.parse(endDateParam);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			int startRow = 0;

			if (page != null && !page.trim().equals("")) {
				int startPage = 1;
				try {
					startPage = Integer.parseInt(page.trim()) - 1;
					if (startPage < 0)
						startPage = 1;
				} catch (Exception e) {
					e.printStackTrace();
				}
				startRow = startPage * rowNum;
			}
            
			List<ApplyData> list = null;
			if(sidx != null && sidx.equals("merge"))
				sidx = "merger";
			if(sidx != null && sidx.equals("nmWithTag"))
				sidx = "policyPDF";
			String[] orderColumns = null;
			if(sidx != null && !sidx.trim().equals("")){
				orderColumns = new String[1];
				orderColumns[0] = sidx;
			}
				
			list = ApplyDataService.getApplyData(startDate, endDate, center, applyNo, policyNo, null, 
					policyStatus, insureId, recName, areaId, startRow, rowNum, orderColumns, sord, exceptionQuery, beforeStatus, sourceCode, receipt, afterStatus, null );
			
			if (list == null)
				list = new ArrayList<ApplyData>();

			int count = ApplyDataService.getApplyDataCount(startDate, endDate, center, applyNo, policyNo, null, 
					policyStatus, insureId, recName, areaId, exceptionQuery, beforeStatus, sourceCode, receipt, afterStatus, null );

			int totalPage = count / rowNum; // 總頁數計算

			if (rowNum == 0)
				totalPage = 0;
			if (rowNum != 0 && count % rowNum != 0)
				totalPage = totalPage + 1;

			List<Object[]> jqList = new ArrayList();

			List<PdfViewQueryJqGridBean> jqGridBeans = new ArrayList();

			for (ApplyData applyData : list) {

				PdfViewQueryJqGridBean bean = new PdfViewQueryJqGridBean(); // 用來轉成jQgrid所需資料的bean
				BeanUtils.copyProperties(applyData, bean);
				bean.setSourceCode(ApplyData.getSourceMap().get(bean.getSourceCode()));
				bean.setPolicyStatus(applyData.getPolicyStatusName());
				bean.setCenter(applyData.getCenterName());
				bean.setPackType(applyData.getPackTypeName());
				jqGridBeans.add(bean);
				/*         {name: 'cycleDateStr', index: 'cycleDateStr', sorttype: "date", width: 80, align: "center"},
	                       {name: 'oldBatchName', index: 'oldBatchName', hidden: true},	                        
	                       {name: 'sourceCode', index: 'sourceCode', width: 80, align: "center"},
	       	               {name: 'applyNo', index: 'applyNo', width: 100 , align: "center"}, 
	       	               {name: 'recName', index: 'recName', width: 70 , align: "center"},	       	            
	       	               {name: 'insureId', index: 'insureId', width: 100 , align: "center"},
	       	               {name: 'policyNos', index: 'policyNos', width: 100 , align: "center"},
	       	               {name: 'center', index: 'center', width: 50 , align: "center"},
	       	               {name: 'areaId', index: 'areaId', width: 80 , align: "center"},
	       	               {name: 'areaName', index: 'areaName', width: 100 , align: "center"},	       	               
	       	               {name: 'policyStatus', index: 'policyStatus', width: 60, align: "center"},	       	            
	       	               {name: 'receiptStr', index: 'receiptStr', width: 30, align: "center"},
	       	               {name: 'totalPage', index: 'totalPage', width: 50, align: "right"},
	       	               {name: 'reprint', index: 'reprint', width: 30, align: "right"},
	       	               {name: 'mergeStr', index: 'mergeStr', width: 30, align: "center"},
	       	               {name: 'vipStr', index: 'vipStr', width: 30, align: "center"},
	       	               {name: 'substractStr', index: 'substractStr', width: 30, align: "center"},
	       	               {name: 'nmWithTag', index: 'nmWithTag', width: 100, align: "center"},
	       	               {name: 'policyPDF', index: 'policyPDF',hidden: true},
	       	               {name: 'newBatchName', index: 'newBatchName',hidden: true},
	       	               {name: 'verifyResult', index: 'verifyResult',hidden: true}],
				 */
				Object[] objArray = new Object[21];
				objArray[0] = bean.getCycleDateStr();
				objArray[1] = bean.getOldBatchName();
				objArray[2] = bean.getSourceCode();
				objArray[3] = bean.getApplyNo();
				String recNameStr = bean.getRecName() == null ? "" : bean.getRecName().trim();
				if(!"GROUP".equals(applyData.getSourceCode())){
				   switch (recNameStr.length()){
			         case 0:
			            break;
			         case 1:
			    	     recNameStr = "＊";
			    	    break;
			         case 2:
			    	     recNameStr = recNameStr.substring(0, 1) + "＊";
			    	    break;
			         default:				    	 
			    	     String begin = recNameStr.substring(0, 1);
			    	     String end = recNameStr.substring(recNameStr.length() - 1);
			    	     String middle = "";
			    	     for(int i = 1 ; i < recNameStr.length() - 1 ; i++){
			    		     String single = recNameStr.substring(i, i+1);
			    		     if(!single.equals(" ") && !single.equals("　"))
			    		         middle += "＊";
			    		     else
			    			     middle += single;
			    	     }
			    	     recNameStr = begin + middle + end;				    	 
			       }
			    } 
				
				objArray[4] = recNameStr;
				objArray[5] = bean.getInsureId();
				objArray[6] = bean.getPolicyNos();
				objArray[7] = bean.getCenter();
				objArray[8] = bean.getAreaId();
				objArray[9] = bean.getAreaName();
				objArray[10] = bean.getPolicyStatus();
				objArray[11] = bean.getReceiptStr();
				objArray[12] = bean.getTotalPage();
				objArray[13] = bean.getReprint();
				objArray[14] = bean.getMergeStr();
				objArray[15] = bean.getVipStr();
				objArray[16] = bean.getSubstractStr();
				objArray[17] = bean.getNmWithTag();
				objArray[18] = bean.getPolicyPDF();
				objArray[19] = bean.getNewBatchName() == null? "" : bean.getNewBatchName();
				objArray[20] = bean.getVerifyResult() == null? "" : bean.getVerifyResult();
				jqList.add(objArray);
			}
			/*
			String returnJsonString = "{ 'total': " + totalPage
					+ ", 'page': '" + page + "', 'records': '" + list.size()
					+ "', 'rows' : " + ToJqGridString.toJqGridString(jqList)
					+ "}"; // 生成jqGrid所需JSON字串
			*/
			String returnJsonString = "{\"page\":\"" + page + "\",\"total\":" + totalPage + ",\"records\":\"" + list.size() + "\",\"rows\":" + ToJqGridString.toJqGridString(jqList)
					+ "}";            
			response.getWriter().write(returnJsonString);
		}else if("subGrid".equals(query)){
			querySubgrid(request, response);
		}else if("afpSubGrid".equals(query)){			
			queryAfpSubgrid(request, response);
		}else if("getVendorId".equals(query)){
			
			String logisticId = request.getParameter("logisticId");
			Session session = HibernateSessionFactory.getSession();
			LogisticStatus ls = (LogisticStatus) session.get(LogisticStatus.class, logisticId);
			session.close();
			String vendorId = "NON_EXIST";
			if(ls != null && ls.getVendorId() != null && !"".equals(ls.getVendorId()))
				vendorId = ls.getVendorId();			
			response.getWriter().write(vendorId);
		}else if("getPack".equals(query)){
			
			String uniqueNo = request.getParameter("uniqueNo");
			Session session = HibernateSessionFactory.getSession();
			List<ApplyData> applyDatas = session.createQuery("from ApplyData where uniqueNo = ? and receipt = false").setString(0, uniqueNo).list();			
			ApplyData applyData = null;
			if(applyDatas == null || applyDatas.size() == 0){
				response.getWriter().write("NON_EXIST");
				session.close();
			}else{
				applyData = applyDatas.get(0);
				session.close();
				if(applyData.getPackId() == null || "".equals(applyData.getPackId())){
				   response.getWriter().write("NON_EXIST_PACK");
			    }else{
			    	String group = applyData.getSourceCode().equals("GROUP")? "G" : "B";
			    	response.getWriter().write(Constant.slashedyyyyMMdd.format(applyData.getCycleDate()) + "_" +group + "_" + applyData.getCenter() );
			    }
			}
		}else if("forceSubmit".equals(query)){			
			String inputNo = request.getParameter("inputNo");			
			if(inputNo.trim().length() != 17){
				response.getWriter().write("NOT_POLICY_BOOK");
			}else{
				Session session = null;
				try{
				   session = HibernateSessionFactory.getSession();
				   List<ApplyData> applyDatas = session.createQuery("from ApplyData where uniqueNo = ? and receipt = false").setString(0, inputNo).list();
				   
				   
				   if(applyDatas == null || applyDatas.size() == 0){
					   response.getWriter().write("NON_EXIST");					   
				   }else{
					   response.getWriter().write(applyDatas.get(0).getPolicyStatus());
				   }
				}catch(Exception e){
					e.printStackTrace();
					logger.error("", e);
				}finally{
					if(session != null)
						session.close();
				}
				
				
			}
			
			
		}
		// setJsonResult(); //把資料轉成jQGrid所需的JSON格式後設到本class的property
	}
	
	
	private synchronized void queryAfpSubgrid(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String newBatchName = request.getParameter("newBatchName");
		boolean cycleDate = "true".equals(request.getParameter("cycleDate"))? true : false;
		List<ApplyData> list = ApplyDataService.findByNewBatchName(newBatchName);
		if(list != null){
			List<Object[]> jqList = new ArrayList();
			/*
			 * name : [ '受理編號','收件人', '被保險人ID','保單號碼','中心代碼','服務中心','目前狀態','回條', '抽件', '最近更新時間'],
			 * name : [ '受理編號','收件人', 'cycleDate', '被保險人ID','保單號碼','中心代碼','服務中心','目前狀態','簽收單', '抽件','最近更新時間'],
			 */
			if(!cycleDate){
			   int index = 0;
			   for(ApplyData applyData : list){
				   String policyNos = "";
				   if(applyData.getPolicyNos() != null && applyData.getPolicyNos().length() >= 2 && applyData.getPolicyNos().startsWith(",") && applyData.getPolicyNos().endsWith(",")){
					   policyNos =  applyData.getPolicyNos().substring(1, applyData.getPolicyNos().length() - 1);
				   }
				   Object [] objArray = new Object[11];
				   objArray[0] = ++index + "";
				   objArray[1] = applyData.getApplyNo();
				   String recNameStr = applyData.getRecName() == null ? "" : applyData.getRecName().trim();
					switch (recNameStr.length()){
				     case 0:
				        break;
				     case 1:
				    	 recNameStr = "＊";
				    	 break;
				     case 2:
				    	 recNameStr = recNameStr.substring(0, 1) + "＊";
				    	 break;
				     default:				    	 
				    	 String begin = recNameStr.substring(0, 1);
				    	 String end = recNameStr.substring(recNameStr.length() - 1);
				    	 String middle = "";
				    	 for(int i = 1 ; i < recNameStr.length() - 1 ; i++){
				    		 String single = recNameStr.substring(i, i+1);
				    		 if(!single.equals(" ") && !single.equals("　"))
				    		    middle += "＊";
				    		 else
				    			middle += single;
				    	 }
				    	 recNameStr = begin + middle + end;				    	 
				    }
				   
				   objArray[2] = recNameStr;
				   String insureId = applyData.getInsureId();
				   switch (insureId.length()){
				     case 0:
				        break;
				     case 1:
				    	 insureId = "*";
				    	 break;
				     case 2:
				    	 insureId = insureId.substring(0, 1) + "*";
				    	 break;
				     case 3:
				    	 insureId = insureId.substring(0, 1) + "*" + insureId.substring(2, 3);
				    	 break;
				     case 4:
				    	 insureId = insureId.substring(0, 1) + "**" + insureId.substring(3, 4);
				    	 break;				    	 
				     default:				    	 
				    	 int firstHalf = (insureId.length() - 3) / 2;
				    	 if(insureId.matches("[a-zA-Z].+")){
				    		 firstHalf ++;
				    	 }
				    	 int endHalf = insureId.length() - 3 - firstHalf;
				    	 insureId = insureId.substring(0, firstHalf) + "***" + insureId.substring(firstHalf + 3, firstHalf + 3 + endHalf);
				    }
				   
				   objArray[3] = insureId;
			       objArray[4] = policyNos;
			       objArray[5] = applyData.getAreaId();
			       objArray[6] = applyData.getAreaName();
			       objArray[7] = applyData.getPolicyStatusMap().get(applyData.getPolicyStatus());
			       objArray[8] = (applyData.getReceipt() == null || !applyData.getReceipt())? "" : "V";
			       objArray[9] = (applyData.getSubstract() == null || !applyData.getSubstract())? "" : "V";
			       objArray[10] = (applyData.getUpdateDate() == null)? "" : sdf2.format(applyData.getUpdateDate());
			       jqList.add(objArray);
			   }
			}else{
				for(ApplyData applyData : list){
					String policyNos = "";
					if(applyData.getPolicyNos() != null && applyData.getPolicyNos().length() >= 2 && applyData.getPolicyNos().startsWith(",") && applyData.getPolicyNos().endsWith(",")){
					   policyNos =  applyData.getPolicyNos().substring(1, applyData.getPolicyNos().length() - 1);
					}
				    Object [] objArray = new Object[11];
				    objArray[0] = applyData.getApplyNo();
				    String recNameStr = applyData.getRecName() == null ? "" : applyData.getRecName().trim();
					switch (recNameStr.length()){
				     case 0:
				        break;
				     case 1:
				    	 recNameStr = "＊";
				    	 break;
				     case 2:
				    	 recNameStr = recNameStr.substring(0, 1) + "＊";
				    	 break;
				     default:				    	 
				    	 String begin = recNameStr.substring(0, 1);
				    	 String end = recNameStr.substring(recNameStr.length() - 1);
				    	 String middle = "";
				    	 for(int i = 1 ; i < recNameStr.length() - 1 ; i++){
				    		 String single = recNameStr.substring(i, i+1);
				    		 if(!single.equals(" ") && !single.equals("　"))
				    		    middle += "＊";
				    		 else
				    			middle += single;
				    	 }
				    	 recNameStr = begin + middle + end;				    	 
				    }
				    objArray[1] = recNameStr;
				    objArray[2] = Constant.slashedyyyyMMdd.format(applyData.getCycleDate());
				    objArray[3] = applyData.getInsureId();
				    objArray[4] = policyNos;
				    objArray[5] = applyData.getAreaId();
				    objArray[6] = applyData.getAreaName();
				    objArray[7] = applyData.getPolicyStatusMap().get(applyData.getPolicyStatus());
				    objArray[8] = (applyData.getReceipt() == null || !applyData.getReceipt())? "" : "V";
				    objArray[9] = (applyData.getSubstract() == null || !applyData.getSubstract())? "" : "V";
				    objArray[10] = (applyData.getUpdateDate() == null)? "" : sdf2.format(applyData.getUpdateDate());
				    jqList.add(objArray);
				}
			}
			String returnJsonString =  ToJqGridString.toJqGridString(jqList); // 生成jqGrid所需JSON字串
			returnJsonString = "{\"rows\":" + returnJsonString + "}";
		    response.getWriter().write(returnJsonString);
		}
	} 
	
	private synchronized void querySubgrid(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//'cycleDateStr','center','applyNo','areaId'
		String oldBatchName = request.getParameter("oldBatchName");		
		String cycleDateStr = request.getParameter("cycleDateStr");		
		String center = request.getParameter("center");		
		String applyNo = request.getParameter("applyNo");		
		String areaId = request.getParameter("areaId");
		
		
		ApplyData applyData = ApplyDataService.findByPK(oldBatchName);
		try{
		   List<Object[]> jqList = new ArrayList();		  
		   //'保單文字檔檔名','列印檔','異常','最近更新時間','轉檔時間','列印時間','膠裝時間','驗單時間', '裝箱時間', '交寄時間', 'vip設定人','抽件設定人'
		   Object [] objArray = new Object[12];
		   objArray[0] = applyData.getOldBatchName();
		   objArray[1] = applyData.getNewBatchName();
		   objArray[2] = applyData.getExceptionStatusName() == null? "" : applyData.getExceptionStatusName();
		   objArray[3] = applyData.getUpdateDate() == null ? "" : sdf2.format(applyData.getUpdateDate());
	       objArray[4] = applyData.getPresTime() == null ? "" : sdf2.format(applyData.getPresTime());
	       objArray[5] = applyData.getPrintTime() == null ? "" : sdf2.format(applyData.getPrintTime());
	       objArray[6] = applyData.getBindTime() == null ? "" : sdf2.format(applyData.getBindTime());
	       objArray[7] = applyData.getVerifyTime() == null ? "" : sdf2.format(applyData.getVerifyTime());
	       objArray[8] = applyData.getPackTime() == null ? "" : sdf2.format(applyData.getPackTime());
	       objArray[9] = applyData.getDeliverTime() == null ? "" : sdf2.format(applyData.getDeliverTime());
	       objArray[10] = applyData.getVipModifierName() == null ? "" : applyData.getVipModifierName(); 
	       objArray[11] = applyData.getSubstractModifiderName() == null ? "" : applyData.getSubstractModifiderName();
	       
		   jqList.add(objArray);
		   String returnJsonString =  ToJqGridString.toJqGridString(jqList); // 生成jqGrid所需JSON字串
		   returnJsonString = "{\"rows\":" + returnJsonString + "}";
		   logger.info(returnJsonString);
	      response.getWriter().write(returnJsonString);
		   
		}catch(Exception e){
			e.printStackTrace();
			
		}
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
