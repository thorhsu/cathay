package com.salmat.pas.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import jxl.CellView;
import jxl.SheetSettings;
import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.write.Label;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.springframework.core.io.ClassPathResource;

import com.salmat.pas.conf.Constant;
import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.BankReceipt;
import com.salmat.pas.vo.JqgridAfpFile;
import com.salmat.pas.vo.LogisticStatus;
import com.salmat.pas.vo.PackStatus;
import com.salmat.util.BarCodeImgUtil;


public class GenerateReport {
	private static Logger logger = Logger.getLogger(GenerateReport.class);
	private static SimpleDateFormat rocSdf = new SimpleDateFormat("yyyMMdd");
	
	public static void main(String[] args){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "d:\\tmp\\Voucher Image" + time + ".xls";
		String targetfile =  targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;		
		try {
			FileInputStream fis = new FileInputStream("d:\\tmp\\Voucher Image.xls");
			workbook = jxl.Workbook.getWorkbook(fis);
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			ws = wwb.getSheet(0);
			ArrayList<String> applyNos = new ArrayList<String>();
			for(int i = 5 ; i < 667 ; i++){
				String content = ws.getCell(3, i).getContents();
				//appNo:1040710002002TSVC009 job date: 1040710 was deleted from DB
				int beginInd = content.indexOf("appNo:") + 6;
				int endInd = content.indexOf(" job date:");
				String applyNo = content.substring(beginInd, endInd);
				System.out.println(applyNo);
				applyNos.add(applyNo);
			}
			ws = wwb.getSheet(2);
			CellFormat cf = ws.getCell(3, 5).getCellFormat();
			int lineCounter = 5;
			for(String applyNo : applyNos){
				int ran = (int) (Math.random() * 3) + 1;
				for(int j = ran ; j > 0 ; j--){
					ws.addCell(new Label(0, lineCounter, "2015/07/27 16:03", cf));
					ws.addCell(new Label(1, lineCounter, "DEL_XML_FILE", cf));
					ws.addCell(new Label(2, lineCounter, "刪除Voucher XML檔案", cf));
					String append = "2";
					if(j == 1)
						append = "4";
					if(j == 2)
						append = "8";
					String tmp = "appNo:" + applyNo + " image: " + applyNo + append + ".xml was deleted ";
					ws.addCell(new Label(3, lineCounter, tmp, cf));
					ws.addCell(new Label(4, lineCounter, "false", cf));
					lineCounter++;
				} 				
			}
			wwb.write();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;
		}
	}
	
	public static String generateCheckResult(List<ApplyData> applyDatas){
		String serverPath = ServletContextGetter.getRealPath("");
		ClassPathResource xlsResource = new ClassPathResource(
				"checkResult.xls");
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "checkResult_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;		
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			ws = wwb.getSheet(0);
			int lineCounter = 1;
			CellView rowView = ws.getRowView(1);
			CellFormat cf = ws.getCell(0, 1).getCellFormat();
			CellFormat cyclecf = ws.getCell(7, 1).getCellFormat();
			
		    for(ApplyData ad : applyDatas){
		    	String policyNo = (ad.getPolicyNos() == null || ad.getPolicyNos().length() < 2)? "" : ad.getPolicyNos().substring(1,  ad.getPolicyNos().length() - 1);
		    	String applyNo = ad.getApplyNo() == null? "" : ad.getApplyNo();
		    	String recName = ad.getRecName() == null? "" : ad.getRecName();
		    	String center = ad.getCenterName() == null? "" : ad.getCenterName();
		    	String status = ad.getPolicyStatusName() == null? "" : ad.getPolicyStatusName();
		    	String verifyResult = ad.getVerifyResult() == null? "" : ad.getVerifyResult();
		    	String backed = ad.getBackToVerify() && ad.getPackId() != null? "已退回" : "";
		    	Date cycleDate = ad.getCycleDate();
		    	String type = "團險證";
		        if(ad.getGroupInsure() != null && ad.getGroupInsure()){
		        	type = "團險證";
		        }else if(ad.getReceipt() != null && ad.getReceipt()){
		        	type = "簽收單";
		        }else if(ad.getReceipt() == null || !ad.getReceipt()){
		        	type = "保單";
		        }
				ws.addCell(new Label(0, lineCounter, applyNo, cf));
		    	ws.addCell(new Label(1, lineCounter, policyNo, cf));
		    	ws.addCell(new Label(2, lineCounter, recName, cf));
		    	ws.addCell(new Label(3, lineCounter, center, cf));
				ws.addCell(new Label(4, lineCounter, status, cf));
				ws.addCell(new Label(5, lineCounter, verifyResult, cf));
				ws.addCell(new Label(6, lineCounter, backed, cf));
				ws.addCell(new jxl.write.DateTime(7, lineCounter, cycleDate, cyclecf));
				ws.addCell(new Label(8, lineCounter, type, cf));
		    	ws.setRowView(lineCounter, rowView);
		    	lineCounter ++;
		    }
		    
			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
		return null;
	}
	
	public static String generateMailType(List<ApplyData> applyDatas){
		String serverPath = ServletContextGetter.getRealPath("");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		ClassPathResource xlsResource = new ClassPathResource(
				"mailType.xls");
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "mailType_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;		
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			ws = wwb.getSheet(0);
			int lineCounter = 1;
			CellView rowView = ws.getRowView(1);
			
		    for(ApplyData ad : applyDatas){
		    	String policyNo = ad.getPolicyNos();
		    	if(policyNo.length() > 2)
		    		policyNo = policyNo.substring(1, policyNo.length() - 1);
		    	
		    	String deliverType = "";
		    	
		    	if("S".equals(ad.getDeliverType())){
		    		deliverType = "客戶";
		    	
		    	}else if("P".equals(ad.getDeliverType())){
		    		deliverType = "理專";
		    	}
		    	boolean mailReceipt = true;
		    	String channelId = ad.getChannelID() == null? "" : ad.getChannelID();
		    	if(channelId.toUpperCase().equals("G"))
					mailReceipt = false;
				//如果是寄要保人，就要有回執聯
				if("S".equals(deliverType))
					mailReceipt = true;
				else if("B".equals(deliverType))
					mailReceipt = false;
				
				if(ad.getReceiver().equals("北二行政中心") 
						   || ad.getReceiver().equals("北二審查科")){
					mailReceipt = false;
				}
				ws.addCell(new Label(0, lineCounter, policyNo, ws.getCell(0, 1).getCellFormat()));
		    	ws.addCell(new Label(1, lineCounter, ad.getUniqueNo() == null? "" : ad.getUniqueNo(), ws.getCell(0, 1).getCellFormat()));
		    	ws.addCell(new Label(2, lineCounter, ad.getReceiver() == null? "" : ad.getReceiver(), ws.getCell(0, 1).getCellFormat()));
		    	ws.addCell(new Label(3, lineCounter, ad.getAddress() == null? "" : ad.getAddress(), ws.getCell(0, 1).getCellFormat()));
				ws.addCell(new Label(4, lineCounter, deliverType, ws.getCell(0, 1).getCellFormat()));
				ws.addCell(new Label(5, lineCounter, mailReceipt ? "雙掛" : "單掛", ws.getCell(0, 1).getCellFormat()));
		    	ws.setRowView(lineCounter, rowView);
		    	lineCounter ++;
		    }
		    
			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
		return null;
	}
	
	public static String generateAreaExcel(List<Area> areas){
		String serverPath = ServletContextGetter.getRealPath("");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		ClassPathResource xlsResource = new ClassPathResource(
				"unitAddress.xls");
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "unitAddress_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;		
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			ws = wwb.getSheet(0);
			int lineCounter = 1;
		    for(Area area : areas){
		    	ws.addCell(new Label(0, lineCounter, area.getAreaId(), ws.getCell(0, 1).getCellFormat()));
		    	ws.addCell(new Label(1, lineCounter, area.getAreaName() == null? "" : area.getAreaName(), ws.getCell(1, 1).getCellFormat()));
		    	ws.addCell(new Label(2, lineCounter, area.getZipCode() == null? "" : area.getZipCode(), ws.getCell(2, 1).getCellFormat()));
		    	ws.addCell(new Label(3, lineCounter, area.getAddress() == null? "" : area.getAddress(), ws.getCell(3, 1).getCellFormat()));
		    	ws.addCell(new Label(4, lineCounter, area.getTel() == null? "" :area.getTel(), ws.getCell(4, 1).getCellFormat()));
		    	ws.addCell(new Label(5, lineCounter, area.getServiceCenter() == null? "" : area.getServiceCenter(), ws.getCell(5, 1).getCellFormat()));
		    	ws.addCell(new Label(6, lineCounter, area.getServiceCenterNm() == null? "" : area.getServiceCenterNm(), ws.getCell(6, 1).getCellFormat()));
		    	ws.addCell(new jxl.write.Number(7, lineCounter, (area.getIndependent() == null || !area.getIndependent())? 0 : 1, ws.getCell(7, 1).getCellFormat()));
		    	lineCounter++;
		    }
		    
			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
		return null;
	}
	
	public static String generateDailyReport(Map<String, int[]> centerMap, String name){
		String[] nameSplit = name.split("-");
		int year = new Integer(nameSplit[0]);
		int month = new Integer(nameSplit[1]) - 1;
		
		String serverPath = ServletContextGetter.getRealPath("");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		ClassPathResource xlsResource = new ClassPathResource(
				"dailyReport.xls");
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "dailyReport_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;		
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			Set<String> keys = centerMap.keySet();
			//排序
			TreeSet<String> treeKeys = new TreeSet<String>();
			treeKeys.addAll(keys);
			
			
			HashSet<Integer> notIncludeCenters = new HashSet<Integer>();
			for(int i = 1 ; i <= 6 ; i++){
				notIncludeCenters.add(i);
			}
		    ws = wwb.getSheet(0);
		    //設定工作表名稱
		    ws.setName(name);
		    int maxDate = 1;
		    CellFormat dateCf = ws.getCell(2, 0).getCellFormat();
		    int books = 0;
		    int pages = 0;
		    int reports = 0;
		    int produced = 0;
		    int sheets = 0;
		    int cards = 0;
		    int receipts = 0;
		    int unproduced = 0;
		    int totbooks = 0;
		    int totpages = 0;
		    int totSheets = 0;
		    int totCards = 0;
		    int totreports = 0;
		    int totReceipts = 0;
		    int totproduced = 0;
		    int totunproduced = 0;
			for(String key : treeKeys){
				//key = center + "_" + 20140601
				String [] strSplit = key.split("_");
				String centerStr = strSplit[0];
				Date inputDate = Constant.yyyyMMdd.parse(strSplit[1]);
				Calendar cal = Calendar.getInstance();
				cal.setTime(inputDate);
				//找出要輸入的日期，以此找出要輸入的column
				int date = cal.get(Calendar.DATE);
				if(date > maxDate)
					maxDate = date;
				Integer center = new Integer(centerStr);
				if(notIncludeCenters.contains(center))
					notIncludeCenters.remove(center);
				
 			    
 			    int startLine = 1 + (center - 1) * 18;
 			    int inputColumn = 3 + (date - 1);
 			    //輸入日期
 			    ws.addCell(new jxl.write.DateTime(inputColumn, 0, inputDate, ws.getCell(inputColumn, 0).getCellFormat()));
 			    int i = 0; 			    
                for(int number : centerMap.get(key)){
                	if(i == 0 || i == 1 || i == 2){                		
                		totbooks += number; //總保單數
                	}
                	if(i == 3 || i == 4 || i == 5){                		 
                		totpages += number; //總頁數
                	}    
                	if(i == 6 || i == 7 || i == 8){                		 
                		totSheets += number; //總頁數
                	}                	
                	if(i == 10 || i == 11 || i == 12){                		 
                		totreports += number; //總報表數
                	}
                	if(i == 13 || i == 15){
                		totReceipts += number; //總回條數
                	}
                	if(i == 14)
                		totCards += number; //總團險證
                	if(i == 16){                 		
                		totproduced += number; //總生產數
                	}
                	if(i == 17){                		 
                		totunproduced += number; //總未生產數
                	}	
                	if(number != 0){                	
                       ws.addCell(new jxl.write.Number(inputColumn, startLine, number, ws.getCell(inputColumn, startLine).getCellFormat()));
                	}
                	
                    startLine++;
                    i++;
                }
                if(center == 6){
                	for(int j = 0 ; j < 6 ; j++){
                		int books1 = (ws.getCell(inputColumn, j * 18 + 1).getContents() == null || ws.getCell(inputColumn, j * 18 + 1).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 1).getContents());  
                		int books2 = (ws.getCell(inputColumn, j * 18 + 2).getContents() == null || ws.getCell(inputColumn, j * 18 + 2).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 2).getContents());
                		int books3 = (ws.getCell(inputColumn, j * 18 + 3).getContents() == null || ws.getCell(inputColumn, j * 18 + 3).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 3).getContents());
                		books += (books1 + books2 + books3);
                		int pages1 = (ws.getCell(inputColumn, j * 18 + 4).getContents() == null || ws.getCell(inputColumn, j * 18 + 4).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 4).getContents());  
                		int pages2 = (ws.getCell(inputColumn, j * 18 + 5).getContents() == null || ws.getCell(inputColumn, j * 18 + 5).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 5).getContents());
                		int pages3 = (ws.getCell(inputColumn, j * 18 + 6).getContents() == null || ws.getCell(inputColumn, j * 18 + 6).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 6).getContents());
                		pages += (pages1 + pages2 + pages3);
                		int sheets1 = (ws.getCell(inputColumn, j * 18 + 10).getContents() == null || ws.getCell(inputColumn, j * 18 + 10).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 10).getContents());
                		sheets += sheets1;
                		int reports1 = (ws.getCell(inputColumn, j * 18 + 11).getContents() == null || ws.getCell(inputColumn, j * 18 + 11).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 11).getContents());  
                		int reports2 = (ws.getCell(inputColumn, j * 18 + 12).getContents() == null || ws.getCell(inputColumn, j * 18 + 12).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 12).getContents());
                		int reports3 = (ws.getCell(inputColumn, j * 18 + 13).getContents() == null || ws.getCell(inputColumn, j * 18 + 13).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 13).getContents());
                		reports += (reports1 + reports2 + reports3 );                		
                		int receipts1 = (ws.getCell(inputColumn, j * 18 + 14).getContents() == null || ws.getCell(inputColumn, j * 18 + 14).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 14).getContents());
                		int receipts2 = (ws.getCell(inputColumn, j * 18 + 16).getContents() == null || ws.getCell(inputColumn, j * 18 + 16).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 16).getContents());
                		receipts += (receipts1 + receipts2);
                		int cards1 = (ws.getCell(inputColumn, j * 18 + 15).getContents() == null || ws.getCell(inputColumn, j * 18 + 15).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 15).getContents());
                		cards += cards1;
                		int produced1 = (ws.getCell(inputColumn, j * 18 + 17).getContents() == null || ws.getCell(inputColumn, j * 18 + 17).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 17).getContents());
                		produced += produced1;
                		int unproduced1 = (ws.getCell(inputColumn, j * 18 + 18).getContents() == null || ws.getCell(inputColumn, j * 18 + 18).getContents().trim().equals(""))? 0 : new Integer(ws.getCell(inputColumn, j * 18 + 18).getContents());
                		unproduced += unproduced1;
                	} 
                	
                	ws.addCell(new jxl.write.Number(inputColumn, 109, books, ws.getCell(inputColumn, 109).getCellFormat()));
                	ws.addCell(new jxl.write.Number(inputColumn, 110, pages, ws.getCell(inputColumn, 110).getCellFormat()));
                	ws.addCell(new jxl.write.Number(inputColumn, 111, sheets, ws.getCell(inputColumn, 111).getCellFormat()));
                	ws.addCell(new jxl.write.Number(inputColumn, 112, reports, ws.getCell(inputColumn, 112).getCellFormat()));
                	ws.addCell(new jxl.write.Number(inputColumn, 113, receipts, ws.getCell(inputColumn, 113).getCellFormat()));
                	ws.addCell(new jxl.write.Number(inputColumn, 114, cards, ws.getCell(inputColumn, 114).getCellFormat()));
                	ws.addCell(new jxl.write.Number(inputColumn, 115, produced, ws.getCell(inputColumn, 115).getCellFormat()));
                	ws.addCell(new jxl.write.Number(inputColumn, 116, unproduced, ws.getCell(inputColumn, 116).getCellFormat()));
                	books = 0;
        		    pages = 0;
        		    reports = 0;
        		    sheets = 0;
        		    cards = 0;
        		    receipts = 0;
        		    produced = 0;
        		    unproduced = 0;
                }                
			}
			for(int i = 1 ; i <= maxDate ; i++){
				int inputColumn = 3 + (i - 1);
				Calendar cal = Calendar.getInstance();
				cal.set(year, month, i);
				ws.addCell(new jxl.write.DateTime(inputColumn, 0, new Date(cal.getTimeInMillis()), ws.getCell(inputColumn, 0).getCellFormat()));
			}
			ws.addCell(new jxl.write.Number(3, 117, totbooks, ws.getCell(3, 117).getCellFormat()));
        	ws.addCell(new jxl.write.Number(3, 118, totpages, ws.getCell(3, 118).getCellFormat()));
        	ws.addCell(new jxl.write.Number(3, 119, totSheets, ws.getCell(3, 119).getCellFormat()));
        	ws.addCell(new jxl.write.Number(3, 120, totreports, ws.getCell(3, 120).getCellFormat()));        	
        	ws.addCell(new jxl.write.Number(3, 121, totReceipts, ws.getCell(3, 121).getCellFormat()));
        	ws.addCell(new jxl.write.Number(3, 122, totCards, ws.getCell(3, 122).getCellFormat()));
        	ws.addCell(new jxl.write.Number(3, 123, totproduced, ws.getCell(3, 123).getCellFormat()));
        	ws.addCell(new jxl.write.Number(3, 124, totunproduced, ws.getCell(3, 124).getCellFormat()));
			
			//移去多餘的column
			for(int i = 33 ; i > maxDate + 2 ; i--){
				ws.removeColumn(i);
			}
			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
		return null;
	}
	
	public static String generate06Report(Map<String, List<ApplyData>> groupMap){
		String serverPath = ServletContextGetter.getRealPath("");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		ClassPathResource xlsResource = new ClassPathResource(
				"policyReport.xls");
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "policyReport_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;		
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			Set<String> keys = groupMap.keySet();
			int sheetCounter = 1; 
			int policyBookCounter = 0;
			int receiptCounter = 0;
			for(String key : keys){
  			    wwb.copySheet(1, key, ++sheetCounter);
  			    ws = wwb.getSheet(sheetCounter);
  			    CellFormat seqFormat = ws.getCell(0, 1).getCellFormat();
  			    CellFormat normFormat = ws.getCell(2, 1).getCellFormat();
  			  
                List<ApplyData> applyDatas = groupMap.get(key);
                int line = 0;
                for(ApplyData applyData : applyDatas){
                	String receipt = (applyData
							.getSourceCode().equals("NORM") || applyData
							.getSourceCode().equals("REPT")) ? "Y"
							: "N";
                    for(String policyNo : applyData.getPolicyNoSet()){
                	   ws.addCell(new Label(0, ++line, line + "", seqFormat));                	   
                	   ws.addCell(new Label(1, line, applyData.getUniqueNo() == null ? "" : applyData.getUniqueNo(), normFormat));
                	   ws.addCell(new Label(2, line, Constant.slashedyyyyMMdd.format(applyData.getCycleDate()), normFormat));
                	   ws.addCell(new Label(3, line, applyData.getCenter(), normFormat));
                	   ws.addCell(new Label(4, line, policyNo, normFormat));
                	   ws.addCell(new Label(5, line, StringUtils.leftPad(applyData.getReprint() +  "", 2, '0'), normFormat));
                	   ws.addCell(new Label(6, line, applyData.getApplyNo(), normFormat));
                	   ws.addCell(new Label(7, line, applyData.getSourceCode(), normFormat));
                	   ws.addCell(new Label(8, line, "Y", normFormat));
                	   ws.addCell(new Label(9, line, "Y", normFormat));
                	   ws.addCell(new Label(10, line, "Y", normFormat));
                	   if(!"REPT".equals(applyData.getSourceCode()))
                	       ws.addCell(new Label(11, line, applyData.getOldBatchNo(), normFormat));
                	   else
                		   ws.addCell(new Label(11, line, "RePrint", normFormat));
                	   ws.addCell(new Label(12, line, "Y", normFormat));
                	   ws.addCell(new Label(13, line, receipt, normFormat));
                	   ws.addCell(new Label(14, line, "", normFormat));
                	   ws.addCell(new Label(15, line, (applyData.getTotalPage() == null ? 0	: applyData.getTotalPage()) + "", normFormat));
                	   ws.addCell(new Label(16, line, applyData.getAreaId() + "", normFormat));
                	   ws.addCell(new Label(17, line, (applyData.getMerger() ? "1" : "0"), normFormat));
                	   ws.addCell(new Label(18, line, "FXDMS", normFormat));
                	   ws.addCell(new Label(19, line, key, normFormat));
                    }
                }
                ws = wwb.getSheet(0);
                if("MI".equals(key)){
                	policyBookCounter += applyDatas.size();
                    receiptCounter += applyDatas.size();
					ws.addCell(new jxl.write.Number(1, 5, applyDatas.size(), ws.getCell(1, 5).getCellFormat()));
					ws.addCell(new jxl.write.Number(2, 5, applyDatas.size(), ws.getCell(2, 5).getCellFormat()));
				}else if("P".equals(key)){
					policyBookCounter += applyDatas.size();
                    receiptCounter += applyDatas.size();
					ws.addCell(new jxl.write.Number(1, 6, applyDatas.size(), ws.getCell(1, 6).getCellFormat()));
					ws.addCell(new jxl.write.Number(2, 6, applyDatas.size(), ws.getCell(2, 6).getCellFormat()));
				}else if("S".equals(key)){
					policyBookCounter += applyDatas.size();
                    receiptCounter += applyDatas.size();
					ws.addCell(new jxl.write.Number(1, 7, applyDatas.size(), ws.getCell(1, 7).getCellFormat()));
					ws.addCell(new jxl.write.Number(2, 7, applyDatas.size(), ws.getCell(2, 7).getCellFormat()));
				}else if("POSPRINT".equals(key)){
					policyBookCounter += applyDatas.size();                    
					ws.addCell(new jxl.write.Number(1, 8, applyDatas.size(), ws.getCell(1, 8).getCellFormat()));					
				}else if("CONPRINT".equals(key)){
					policyBookCounter += applyDatas.size();
					ws.addCell(new jxl.write.Number(1, 9, applyDatas.size(), ws.getCell(1, 9).getCellFormat()));					
				}else if("RePrint".equals(key)){
					policyBookCounter += applyDatas.size();
                    receiptCounter += applyDatas.size();
					ws.addCell(new jxl.write.Number(1, 10, applyDatas.size(), ws.getCell(1, 10).getCellFormat()));
					ws.addCell(new jxl.write.Number(2, 10, applyDatas.size(), ws.getCell(2, 10).getCellFormat()));
				}
			}
			ws = wwb.getSheet(0);
			ws.addCell(new jxl.write.DateTime(1, 2, new Date(), ws.getCell(1, 2).getCellFormat()));
			wwb.removeSheet(1);
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
		return null;
		
		
	}
	
	public static String generateLabels(List<LogisticStatus> list, List<PackStatus> packs, String labelName) throws Exception{
		String serverPath = ServletContextGetter.getRealPath("");		
		if(labelName == null)
			labelName = "label2.xls";
		ClassPathResource xlsResource = new ClassPathResource(labelName);
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "label_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;
		Calendar cal = Calendar.getInstance();
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			WritableSheet policy = null;
			WritableSheet receipt = null;
			ws = wwb.getSheet(0); //label
			policy = wwb.getSheet(1); //保單明細
			receipt = wwb.getSheet(2); //簽收單明細
			
			//設定縮放及斷行
			SheetSettings wsSettings = ws.getSettings();
			SheetSettings policySettings = policy.getSettings();
			SheetSettings receiptSettings = receipt.getSettings();
			
			wsSettings.setFitToPages(false);
		  	wsSettings.setPageBreakPreviewMode(true);
			
			policySettings.setFitToPages(false);			
		  	policySettings.setPageBreakPreviewMode(true);
		  	
		  	receiptSettings.setFitToPages(false);
		  	receiptSettings.setPageBreakPreviewMode(true);
		  	
			
			
			CellFormat barcodeFormat = ws.getCell(0, 0).getCellFormat();
			
			CellFormat strFormat = ws.getCell(0, 1).getCellFormat();
			
			CellFormat textFormat = policy.getCell(0, 5).getCellFormat(); //清單列表
			CellFormat pageTitleFormat = policy.getCell(2, 0).getCellFormat(); //清單title
			CellFormat detailBarcodeFormat = policy.getCell(3, 0).getCellFormat(); //清單barcode
			CellFormat titleFormat = policy.getCell(0, 3).getCellFormat(); //清單title
			CellFormat subFormat = policy.getCell(0, 1).getCellFormat(); //一般說明
			
			
			
			int columnCounter = 0;
			int lineCounter = 0;
			int receiptLine = 0;
			int policyLine = 0;
			
			for(PackStatus pack : packs){
				//計算移到第幾個Column
				if(columnCounter > 4){
                    if((lineCounter + 1) % 24 == 0){
                    	ws.addRowPageBreak(lineCounter);
					}
					lineCounter += 3;
					columnCounter = 0;
					
				}
				String forWrite = "收件單位:" + pack.getSubAreaId() + "\r\n" + "收件地址:" + pack.getAreaAddress() + "\r\n" + "        保單本數:" + pack.getBooks();
				//先寫bar code
				ws.addCell(new Label(columnCounter, lineCounter, pack.getLogisticId() , barcodeFormat));
				ws.addCell(new Label(columnCounter, lineCounter + 1, forWrite, strFormat));
				columnCounter += 2;
				
				//保單明細部分
				policy.addCell(new Label(2, policyLine, "保單明細", pageTitleFormat));
				policy.mergeCells(3, policyLine, 4, policyLine);
				policy.addCell(new Label(3, policyLine, pack.getPackId(), detailBarcodeFormat));
				policyLine++;
				policy.addCell(new Label(0, policyLine, "單位：", subFormat));
				policy.addCell(new Label(1, policyLine, pack.getSubAreaId(), subFormat));				
				policyLine++;
				
				policy.addCell(new Label(0, policyLine, "明細種類：", subFormat));
				policy.addCell(new Label(1, policyLine, pack.isBack()? "退件" : "正常件", subFormat));				
				policyLine++;
				
				//簽收單部分
				receipt.addCell(new Label(2, receiptLine, "簽收單明細", pageTitleFormat));
				receipt.mergeCells(3, receiptLine, 4, receiptLine);
				receipt.addCell(new Label(3, receiptLine, pack.getPackId(), detailBarcodeFormat));
				receiptLine++;
				receipt.addCell(new Label(0, receiptLine, "單位：", subFormat));
				receipt.addCell(new Label(1, receiptLine, pack.getSubAreaId(), subFormat));
				receiptLine++;
				
				receipt.addCell(new Label(0, receiptLine, "明細種類：", subFormat));
				receipt.addCell(new Label(1, receiptLine, pack.isBack()? "退件" : "正常件", subFormat));				
				receiptLine++;
				
				Set<ApplyData> appSet = pack.getApplyDatas();
				int policyColumn = 0;
				int receiptColumn = 0;
				int line = 0;
				
				WritableSheet currentWs = null;				
				boolean receiptNotMergeFirstLine = true;
				boolean policyNotMergeFirstLine = true;
				boolean receiptMergeFirstLine = true;
				boolean policyMergeFirstLine = true;
				for(ApplyData applyData: appSet){					
					int column = 0;
					boolean merge = applyData.getMerger() == null ? false : applyData.getMerger();
					//判斷要使用那一個worksheet
					if(applyData.getReceipt() != null && applyData.getReceipt()){
						//簽收單						
						currentWs = receipt;
						column = receiptColumn;
						line = receiptLine;
						//如果是第一行，加入title
						if(merge && receiptMergeFirstLine){
							//如果之前已進入過notMerge的話，要加一行
							if(!receiptNotMergeFirstLine){
								line++;
							}
							currentWs.addCell(new Label(0, line, "受理編號（轄區）"  ,titleFormat));
							currentWs.mergeCells(1, line, 4, line);
							currentWs.addCell(new Label(1, line, "保 單 號 碼（轄區）"  ,titleFormat));
							line ++;
							receiptMergeFirstLine = false;
						}else if(!merge && receiptNotMergeFirstLine){
							currentWs.addCell(new Label(0, line, "受理編號（轄區）"  ,titleFormat));
							currentWs.addCell(new Label(1, line, "受理編號（轄區）"  ,titleFormat));
							currentWs.addCell(new Label(2, line, "受理編號（轄區）"  ,titleFormat));
							currentWs.addCell(new Label(3, line, "受理編號（轄區）"  ,titleFormat));
							currentWs.addCell(new Label(4, line, "受理編號（轄區）"  ,titleFormat));
							line ++;
							receiptNotMergeFirstLine = false;
						}
						
					}else{
						currentWs = policy;
						column = policyColumn;
						line = policyLine;
						//如果是第一行，加入title
						if(merge && policyMergeFirstLine){
							//如果之前已進入過notMerge的話，要加一行
							if(!policyNotMergeFirstLine){
								line++;
							}
							currentWs.addCell(new Label(0, line, "受理編號（轄區）"  ,titleFormat));
							currentWs.mergeCells(1, line, 4, line);
							currentWs.addCell(new Label(1, line, "保 單 號 碼（轄區）"  ,titleFormat));
							line ++;
							policyMergeFirstLine = false;							
						}else if(!merge && policyNotMergeFirstLine){
							currentWs.addCell(new Label(0, line, "受理編號（轄區）"  ,titleFormat));
							currentWs.addCell(new Label(1, line, "受理編號（轄區）"  ,titleFormat));
							currentWs.addCell(new Label(2, line, "受理編號（轄區）"  ,titleFormat));
							currentWs.addCell(new Label(3, line, "受理編號（轄區）"  ,titleFormat));
							currentWs.addCell(new Label(4, line, "受理編號（轄區）"  ,titleFormat));
							line ++;
							policyNotMergeFirstLine = false;							
						}
					}
					
					if(!merge){						
						if(column > 4){
							column = 0;
							line++;
						}
						currentWs.addCell(new Label(column++, line, applyData.getApplyNo() + "(" + pack.getSubAreaId() + ")" , textFormat));						
						
					}else{
						currentWs.addCell(new Label(0, line, applyData.getApplyNo(), textFormat));
						Set<String> policyNos = applyData.getPolicyNoSet();
						int mergeColumn = 1;
						for(String policyNo : policyNos){
							currentWs.addCell(new Label(mergeColumn++, line, policyNo + "(" + pack.getSubAreaId() + ")", textFormat));
						}
						line++;
					}
					

					if(applyData.getReceipt() != null && applyData.getReceipt()){
						receiptLine = line;
						receiptColumn = column;
					}else{
						policyLine = line;
						policyColumn = column;
					}
				}
				policyLine++;
				receiptLine++;
				//加入斷行記號，跳下一頁
				policy.addRowPageBreak(policyLine);
				receipt.addRowPageBreak(receiptLine);
			}
			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			throw e;
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}

	}
	
	public static String generateLabels(List<PackStatus> packs, Session session) throws Exception{
		String serverPath = ServletContextGetter.getRealPath("");				
		String labelName = "label.xls";
		ClassPathResource xlsResource = new ClassPathResource(labelName);
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "label_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;

			WritableSheet fxPolicy = null;
			WritableSheet fxReceipt = null;
			ws = wwb.getSheet(0); //label
			fxPolicy = wwb.getSheet(1);
			fxReceipt = wwb.getSheet(2);
			
			
			//設定縮放及斷行
			SheetSettings wsSettings = ws.getSettings();
			SheetSettings fxPolicySettings = fxPolicy.getSettings();
			SheetSettings fxReceiptSettings = fxReceipt.getSettings();
			
			wsSettings.setFitToPages(false);
		  	wsSettings.setPageBreakPreviewMode(true);
			
		  	
		  	
		  	fxPolicySettings.setFitToPages(false);			
		  	fxPolicySettings.setPageBreakPreviewMode(true);
		  	
		  	fxReceiptSettings.setFitToPages(false);
		  	fxReceiptSettings.setPageBreakPreviewMode(true);
			
			CellFormat barcodeFormat = ws.getCell(0, 0).getCellFormat();			
			CellFormat strFormat = ws.getCell(0, 1).getCellFormat();
			CellFormat splitFormat = ws.getCell(0, 2).getCellFormat();
			CellView barcodeView = ws.getRowView(0);
			CellView bodyView = ws.getRowView(1);
			CellView splitView = ws.getRowView(2);
			CellView lastBodyView = ws.getRowView(22);
			
			CellFormat fxTextFormat = fxPolicy.getCell(0, 4).getCellFormat(); //清單列表
			
			CellFormat pageTitleFormat = fxPolicy.getCell(2, 0).getCellFormat(); //清單title
			CellFormat detailBarcodeFormat = fxPolicy.getCell(3, 0).getCellFormat(); //清單barcode
			CellFormat titleFormat = fxPolicy.getCell(0, 3).getCellFormat(); //清單title
			CellFormat subFormat = fxPolicy.getCell(0, 1).getCellFormat(); //一般說明
			CellView rowView = fxPolicy.getRowView(4);

			
			
			
			int columnCounter = 0;
			int lineCounter = 0;
			int receiptLine = 0;
			int fxPolicyLine = 0;
			boolean hasBack = false;
			for(PackStatus pack : packs){
				//計算移到第幾個Column
				if(columnCounter > 4){
                    if((lineCounter + 1 + 2) % 24 == 0){
                    	ws.addRowPageBreak(lineCounter + 1 + 2);
					}
					lineCounter += 3;
					columnCounter = 0;
					
				}
				String backStr = "   ";
				if(pack.isBack()){
				   backStr = "退件";
				   hasBack = true;
				}
					
				String forWrite = "收件單位:" + pack.getSubAreaId() + "\r\n" + "收件地址:" + pack.getAreaAddress() + "\r\n" + "        保單本數:" + pack.getBooks();
				//先寫bar code
				ws.addCell(new Label(columnCounter, lineCounter, "*" + pack.getPackId() + "L*", barcodeFormat));
				ws.setRowView(lineCounter, barcodeView);
				ws.addCell(new Label(columnCounter, lineCounter + 1, forWrite, strFormat));
				
				ws.addCell(new Label(columnCounter, lineCounter + 2, " ", splitFormat));
				ws.setRowView(lineCounter + 2, splitView);
				
				columnCounter += 2;
				
				if((lineCounter + 3) % 24 == 0){
					ws.setRowView(lineCounter + 1, lastBodyView);					
				}else{
					ws.setRowView(lineCounter + 1, bodyView);
				}
				
				
				fxPolicy.addCell(new Label(2, fxPolicyLine, "保單明細", pageTitleFormat));				
				fxPolicy.mergeCells(3, fxPolicyLine, 5, fxPolicyLine);
				fxPolicy.addCell(new Label(3, fxPolicyLine, "*" + pack.getPackId() + "P*", detailBarcodeFormat));
				fxPolicyLine++;

				
				fxPolicy.addCell(new Label(0, fxPolicyLine, "單位：", subFormat));
				fxPolicy.addCell(new Label(1, fxPolicyLine, pack.getSubAreaId(), subFormat));
				fxPolicy.addCell(new Label(3, fxPolicyLine, "單位名稱：", subFormat));				
				String allSubAreaNames = pack.getSubAreaName();
				if(allSubAreaNames != null && !allSubAreaNames.trim().equals("")){
					String[] subAreaNames = allSubAreaNames.split(",");
					int beginLine = fxPolicyLine;
					int endLine = fxPolicyLine;
					for(String subAreaName : subAreaNames){
					   fxPolicy.mergeCells(4, fxPolicyLine, 5, fxPolicyLine);
					   fxPolicy.addCell(new Label(0, fxPolicyLine, "單位：", subFormat));
					   fxPolicy.addCell(new Label(1, fxPolicyLine, pack.getSubAreaId(), subFormat));
					   fxPolicy.addCell(new Label(2, fxPolicyLine, "", subFormat));
					   fxPolicy.addCell(new Label(3, fxPolicyLine, "單位名稱：", subFormat));
				       fxPolicy.addCell(new Label(4, fxPolicyLine, subAreaName, subFormat));
				       fxPolicy.addCell(new Label(5, fxPolicyLine, "", subFormat));
				       endLine = fxPolicyLine;
				       fxPolicyLine++;
					}
					if(beginLine != endLine){
						fxPolicy.mergeCells(0, beginLine, 0, endLine);
						fxPolicy.mergeCells(1, beginLine, 1, endLine);
						fxPolicy.mergeCells(3, beginLine, 3, endLine);
					}
				}else{
					fxPolicyLine++;
				}
				
				fxPolicy.addCell(new Label(0, fxPolicyLine, "明細種類：", subFormat));
				fxPolicy.addCell(new Label(1, fxPolicyLine, pack.isBack()? "退件" : "正常件", subFormat));
				
				String serviceCenterNm = null;
				if(pack.getServiceCenterNm() != null && !pack.getServiceCenterNm().trim().equals("")){
					serviceCenterNm = pack.getServiceCenterNm(); 
					//如果全部是數字和字母，代表應該是找不到服務中心
					if(serviceCenterNm.matches("[a-zA-Z0-9]*"))
						serviceCenterNm = null;
				}
				if(serviceCenterNm == null){
				    List<Area> areaList = session.createQuery("from Area where address is not null and address <> '' and areaName is not null "
						+ "and areaName <> '' and areaId in (select distinct serviceCenter from Area where subAreaId = '" + pack.getSubAreaId() + "')").list();
				    if(areaList != null && areaList.size() > 0){					
					   Area serviceCenter = areaList.get(0);
					   serviceCenterNm = serviceCenter.getAreaName();
				    }
				}	
				if(serviceCenterNm != null && !serviceCenterNm.equals("")){
				   fxPolicy.addCell(new Label(3, fxPolicyLine, "服務中心：", subFormat));
				   fxPolicy.addCell(new Label(4, fxPolicyLine, serviceCenterNm, subFormat));								
				   fxPolicy.mergeCells(4, fxPolicyLine, 5, fxPolicyLine);
				}		
				ws.setRowView(fxPolicyLine++, bodyView);
				
				//簽收單部分				
				if(pack.isBack()){
				   fxReceipt.addCell(new Label(2, receiptLine, "簽收單明細", pageTitleFormat));
				   fxReceipt.mergeCells(3, receiptLine, 4, receiptLine);
				   fxReceipt.addCell(new Label(3, receiptLine, "*" + pack.getPackId() + "P*", detailBarcodeFormat));
				}
				receiptLine++;				
				if(pack.isBack()){
				   fxReceipt.addCell(new Label(0, receiptLine, "單位：", subFormat));
				   fxReceipt.addCell(new Label(1, receiptLine, pack.getSubAreaId(), subFormat));
				}
				receiptLine++;
				
				if(pack.isBack()){
				   fxReceipt.addCell(new Label(0, receiptLine, "明細種類：", subFormat));
				   fxReceipt.addCell(new Label(1, receiptLine, pack.isBack()? "退件" : "正常件", subFormat));
				}
				receiptLine++;
				
				Criteria criteria = session.createCriteria(ApplyData.class);
				criteria.add(Restrictions.eq("packId", pack.getPackId()));
				criteria.addOrder(Order.asc("uniqueNo"));
				List<ApplyData> appSet = criteria.list();
				int policyColumn = 0;
				int receiptColumn = 0;
				boolean receiptFirstLine = true;
				boolean policyFirstLine = true;
				for(ApplyData applyData: appSet){
					WritableSheet currentWs2 = null;
					int column = 0;
					//判斷要使用那一個worksheet
					if(applyData.getReceipt() != null && applyData.getReceipt()){						
						//簽收單						
						if(pack.isBack())
						    currentWs2 = fxReceipt;
						column = receiptColumn;
						
						//如果是第一行，加入title
						if(receiptFirstLine){
							if(currentWs2 != null){
							   currentWs2.addCell(new Label(0, receiptLine, "簽收單號碼"  ,titleFormat));
							   currentWs2.addCell(new Label(1, receiptLine, "簽收單號碼"  ,titleFormat));							
							   currentWs2.addCell(new Label(2, receiptLine, "簽收單號碼"  ,titleFormat));
							   currentWs2.addCell(new Label(3, receiptLine, "簽收單號碼"  ,titleFormat));
							   currentWs2.addCell(new Label(4, receiptLine, "要保人"  ,titleFormat));
							}
							receiptLine ++;
							receiptFirstLine = false;
						}
						
						if(currentWs2 != null){
							int i = 0;
							for(String policyNo : applyData.getPolicyNoSet()){
								currentWs2.addCell(new Label(i, receiptLine, policyNo  ,fxTextFormat));
								i++;
								if (i > 3)
									break;
							}
							currentWs2.addCell(new Label(4, receiptLine, applyData.getRecName()  ,fxTextFormat));
							currentWs2.setRowView(receiptLine, rowView);
							receiptLine++;
						}
						
					}else if(applyData.getReceipt() != null && !applyData.getReceipt()){
						
						currentWs2 = fxPolicy;
						column = policyColumn;						
						//如果是第一行，加入title						
						if(policyFirstLine){
						   currentWs2.addCell(new Label(0, fxPolicyLine, "保單流水號"  ,titleFormat));
						   currentWs2.addCell(new Label(1, fxPolicyLine, "保單號碼"  ,titleFormat));
						   currentWs2.addCell(new Label(2, fxPolicyLine, "保單號碼"  ,titleFormat));
						   currentWs2.addCell(new Label(3, fxPolicyLine, "保單號碼"  ,titleFormat));
						   currentWs2.addCell(new Label(4, fxPolicyLine, applyData.getSourceCode().toUpperCase().equals("GROUP")? "要保單位" : "要保人" ,titleFormat));
						   currentWs2.addCell(new Label(5, fxPolicyLine, ""  ,titleFormat));
						   policyFirstLine = false;
						   fxPolicyLine++;
						}
						
						currentWs2.addCell(new Label(0, fxPolicyLine, applyData.getUniqueNo(), fxTextFormat));
						currentWs2.addCell(new Label(4, fxPolicyLine, applyData.getRecName()  ,fxTextFormat));
						if(applyData.getSourceCode().equals("CONV") || applyData.getSourceCode().equals("REIS")){
							currentWs2.addCell(new Label(5, fxPolicyLine, "無簽收單"  ,titleFormat));
					    }else{
					    	int i = 1;
					    	for(String policyNo : applyData.getPolicyNoSet()){                                    					    			
					    	   currentWs2.addCell(new Label(i, fxPolicyLine, policyNo, fxTextFormat));					    	   
						       i++;
						       if(i > 3)
						    	   break;	
                            }
					    	//如果是因為簽收單轉檔失敗導致，這本保單不會有簽收回條
					    	if(pack.isBack()){
					    		//ApplyData.findByApplyNoAndPolicyNoAndCenerCycle", query="from ApplyData where applyNo = ? and policyNos like ? and center = ? and receipt = ? and cycleDate = ?"),
					    		String policyNo = null;
					    		if(applyData.getPolicyNoSet() != null)
					    		   for(String policyNoStr : applyData.getPolicyNoSet()){
					    		      policyNo = policyNoStr;
					    		      break;
					    		   }
					    	    List<ApplyData> list = session.getNamedQuery("ApplyData.findByApplyNoAndPolicyNoAndCenerCycleReprint").setString(0, applyData.getApplyNo())
					    	                .setString(1, "%," + policyNo + ",%").setString(2, applyData.getCenter())
					    	                .setBoolean(3, true).setDate(4, applyData.getCycleDate()).setInteger(5, applyData.getReprint()).list();
					    	    if(list != null && list.size() > 0){
					    	    	ApplyData receiptApp = list.get(0);
					    	    	if(new Integer(receiptApp.getPolicyStatus()) < 20 ){
					    	    		currentWs2.mergeCells(2, fxPolicyLine, 3, fxPolicyLine);
					    	    		currentWs2.addCell(new Label(2, fxPolicyLine, "無簽收單(因轉檔錯誤)"  ,titleFormat));
					    	    	}
					    	    }
					    	}else{
					    		fxPolicy.mergeCells(4, fxPolicyLine, 5, fxPolicyLine);
					    	}
					    }
						currentWs2.setRowView(fxPolicyLine, rowView);
						fxPolicyLine++;
					}										
				}
				receiptLine++;
				fxPolicyLine++;
				fxPolicy.addCell(new Label(0, fxPolicyLine, "共" + pack.getBooks() + "本", titleFormat));
				fxPolicyLine++;
				//加入斷行記號，跳下一頁
				fxPolicy.addRowPageBreak(fxPolicyLine);
				fxReceipt.addRowPageBreak(receiptLine);
			}
			wwb.removeSheet(0);
			if(!hasBack){
				wwb.removeSheet(2);
			}
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			throw e;
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
	}
	public static String generateGroupLogisticReports(List<LogisticStatus> lss, Session session) throws Exception{
		List<String> logisticIds = new ArrayList<String>();
		for(LogisticStatus ls : lss){
			logisticIds.add(ls.getLogisticId());
		}
		String serverPath = ServletContextGetter.getRealPath("");				
		String labelName = "groupLogistList.xls";
		ClassPathResource xlsResource = new ClassPathResource(labelName);
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "logisticDetail_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;
		Calendar cal = Calendar.getInstance();
		
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄			
			WritableSheet packDetail = null;
			packDetail = wwb.getSheet(0);
			CellFormat txtFormat = packDetail.getCell(0, 1).getCellFormat(); //一般文字
			CellFormat dateFormat = packDetail.getCell(6, 1).getCellFormat(); //日期
			
			String sql = "select distinct l.vendorId, a.serviceCenter, p.serviceCenterNm, a.areaName,"
					+ " substring(a.policyNos, 2, length(a.policyNos) - 2) as myPolicyNo , a.recName, a.deliverTime, a.updateDate,"
					+ " a.policyNos, p.back, a.substract, a.substractModifiderName "
					+ " from ApplyData a inner join a.packSatus p  inner join p.logisticStatus l " 
					+ " where l.logisticId in(:logisticIds) "
					+ " and (a.receipt = false or a.receipt is null) and (a.groupInsure = false or a.groupInsure is null) order by vendorId, serviceCenter, areaName, policyNos";

			List<Object[]> list = session.createQuery(sql).setParameterList("logisticIds", logisticIds).list();
			String prevVendorId = null;
			String prevServiceCenter = null;
			String prevAreaName = null;
			int line = 1;
			for(Object[] objArr : list){
				//System.out.println(objArr[0] + "|" + objArr[1] + "|" + objArr[2] + "|" + objArr[3] + "|" + objArr[4] + "|" + objArr[5] + "|" + objArr[6] + "|" + objArr[7] );
				String vendorId = objArr[0] == null ? "" : (String)objArr[0]; 
				String serviceCenter = objArr[1] == null ? "" : (String)objArr[1];
				String serviceCenterNm = objArr[2] == null ? "" : (String)objArr[2];
				String areaName = objArr[3] == null ? "" : (String)objArr[3];
				String policyNo = objArr[4] == null ? "" : (String)objArr[4];
				String recName = objArr[5] == null ? "" : (String)objArr[5];				
				Date deliverTime = (Date)objArr[6];				
				Boolean back = (Boolean) objArr[9];
				Boolean substract = objArr[10] == null? false : (Boolean)objArr[10];
				String substractNm = objArr[11] == null ? "" : (String)objArr[11];
				if(vendorId.equals("") && back)
					vendorId = "送快遞";
				if(!vendorId.equals(prevVendorId)){
					packDetail.addCell(new Label(0, line, vendorId, txtFormat));
					prevVendorId = vendorId;
				}
				if(!serviceCenterNm.equals(prevServiceCenter)){
					if(!serviceCenterNm.equals(""))
					   packDetail.addCell(new Label(1, line, serviceCenterNm, txtFormat));
					else
					   packDetail.addCell(new Label(1, line, serviceCenter, txtFormat));
					prevServiceCenter = serviceCenterNm;
				}
				if(!areaName.equals(prevAreaName)){					
					packDetail.addCell(new Label(2, line, areaName, txtFormat));
					prevAreaName = areaName;
				}
				packDetail.addCell(new Label(3, line, policyNo, txtFormat));
				packDetail.addCell(new Label(4, line, recName, txtFormat));
				packDetail.mergeCells(4, line, 5, line);
				if(deliverTime != null)
				   packDetail.addCell(new jxl.write.DateTime(6, line, deliverTime, dateFormat));
				else
				  packDetail.addCell(new Label(6, line, "未交寄", txtFormat));
				line++;
			}
			
			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			throw e;
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
		
	}
	
	public static String generateWeightReport(List<Object[]> lss, Date cycleDate){
		String serverPath = ServletContextGetter.getRealPath("");		
		ClassPathResource xlsResource = new ClassPathResource(
				"weight.xls");
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "weight.xls_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		
		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;
		Calendar cal = Calendar.getInstance();
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			ws = wwb.getSheet(0);

            CellFormat numberFormat = ws.getCell(2, 2).getCellFormat();
            ws.addCell(new jxl.write.DateTime(7, 0, cycleDate, ws.getCell(7, 0).getCellFormat()));
            HashMap<Integer, Integer> lineTotals = new HashMap<Integer, Integer>();
            lineTotals.put(2, 0);
            lineTotals.put(3, 0);
            lineTotals.put(4, 0);
			for(Object[] row : lss){
				//select weight, batchOrOnline, mailReceipt, count(l)
				String weightRange = (String)row[0];
				String batchOrOnline = row[1] + "";
				Boolean mailReceipt = (Boolean)row[2]; 
				Integer count = (Integer) row[3];
				if(count == null)
					count = 0;
				Integer line = 4;
				int column = 2;
				if("G".equals(batchOrOnline)){
					line = 2;
				}else if(mailReceipt != null && mailReceipt){
				    line = 3;	
				}
				switch (weightRange){
				    case "no weight":
				        column = -1 ;
				        break;
				    case "< 20":
				        column = 2 ;
				        break;
				    case "20-50":
				        column = 3 ;
				        break;
				    case "50-100":
				        column = 4 ;
				        break;
				    case "100-250":
				        column = 5 ;
				        break;
				    case "250-500":
				        column = 6 ;
				        break;
				    case "500-1000":
				        column = 7 ;
				        break;
				    case "1000-2000":
				        column = 8 ;
				        break;
				    case "2000-5000":
				        column = 9 ;
				        break;
				    case "> 5000":
				        column = 10 ;
				        break;
				    default:
				    	column = -1 ;
				}
				if(column > 0)
				   ws.addCell(new jxl.write.Number(column, line, count, numberFormat));
				Integer lineTotal = lineTotals.get(line) + count;
				lineTotals.put(line, lineTotal);
			    
			}
			ws.addCell(new jxl.write.Number(11, 2, lineTotals.get(2), numberFormat));
			ws.addCell(new jxl.write.Number(11, 3, lineTotals.get(3), numberFormat));
			ws.addCell(new jxl.write.Number(11, 4, lineTotals.get(4), numberFormat));
			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			return null;
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
	}
	
	
	
	public static String generateLogisticReports(List<LogisticStatus> lss, Session session, String center) throws Exception{
		List<String> logisticIds = new ArrayList<String>();
		Date beginDate = null;
		Date endDate = null;
		
		for(LogisticStatus ls : lss){
			if(beginDate == null || beginDate.compareTo(ls.getCycleDate()) > 0){
				beginDate = ls.getCycleDate();
			}
			if(endDate == null || endDate.compareTo(ls.getCycleDate()) < 0){
				endDate = ls.getCycleDate();
			}
			logisticIds.add(ls.getLogisticId());
		}
		List<ApplyData> applyDatas = session.createQuery("from ApplyData where cycleDate between ? and ? and packId is null and center = ? and sourceCode <> 'GROUP' "
				+ " and newBatchName not like '%9999' and receipt = 0 order by uniqueNo")
		   .setParameter(0, beginDate).setParameter(1, endDate).setString(2, center).list();
		String serverPath = ServletContextGetter.getRealPath("");				
		String labelName = "tpe2LogistList.xls";
		ClassPathResource xlsResource = new ClassPathResource(labelName);
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "logisticDetail_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;
		//Calendar cal = Calendar.getInstance();
		WritableSheet packDetail = null;
		WritableSheet noPack = null;
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄			
			wwb.copySheet(0, "等待打包保單", 1);
			packDetail = wwb.getSheet(0);
			noPack = wwb.getSheet(1);
			packDetail.setName("已打包清單");
			packDetail.getSettings().setFitToPages(false);
			CellFormat txtFormat = packDetail.getCell(0, 1).getCellFormat(); //一般文字
			//CellFormat dateFormat = packDetail.getCell(7, 1).getCellFormat(); //日期
			CellView rowView = packDetail.getRowView(1);
			
			String sql = "select p.subAreaId, p.firstUniqueNo, p.subAreaName, l.address,  substring(a.policyNos, 2, length(a.policyNos) - 2) as myPolicyNo "
					+ ", a.recName, a.deliverTime, a.updateDate, a.policyNos, a.sourceCode, a.deliverType , p.back, a.verifyResult, a.cycleDate, l.vendorId, l.mailReceipt, a.substract, a.substractModifiderName "
					+ " from ApplyData a inner join a.packSatus p  inner join p.logisticStatus l " 
					+ " where l.logisticId in(:logisticIds) and (a.receipt = false or a.receipt is null) order by p.back, p.firstUniqueNo, p.subAreaId, l.name, a.policyNos, a.sourceCode";

			List<Object[]> list = session.createQuery(sql).setParameterList("logisticIds", logisticIds).list();
			String prevFirstNo = null;
			String prevChannelNm = null;
			String prevReceiver = null;
			String prevVendorId = null;
			
			int line = 1;
			
			for(Object[] objArr : list){
				//System.out.println(objArr[0] + "|" + objArr[1] + "|" + objArr[2] + "|" + objArr[3] + "|" + objArr[4] + "|" + objArr[5] + "|" + objArr[6] + "|" + objArr[7] );
				String channelNm = objArr[0] == null ? "" : (String)objArr[0];
				//其實不是掛號號碼，因為是cop
				String firstUniqueNo = objArr[1] == null ? "" : (String)objArr[1]; 				
				String receiver = objArr[2] == null ? "" : (String)objArr[2];
				String sourceCode = (String)objArr[9];
				String deliverType = (String)objArr[10];
				boolean back = (Boolean)objArr[11];
				String verifyResult = objArr[12] == null ? "" : (String)objArr[12];
				boolean expressDeliver = false;
				if(receiver != null && receiver.equals("北二行政中心")){
					if("REPT".equals(sourceCode) || "CONV".equals(sourceCode) || "REIS".equals(sourceCode)){
						channelNm = "補單回送";
					}else if("B".equals(deliverType)){
						channelNm = "系統指定回送";
						receiver = "北二行政中心 ";
					}else{
						channelNm = "系統查無地址";
						receiver = "北二行政中心  ";
					}
					expressDeliver = true;
					firstUniqueNo = "";
				}else if(receiver != null && receiver.equals("北二審查科") && back){
					channelNm = "驗單錯誤";
					firstUniqueNo = "";
					expressDeliver = true;
				}
				
				String[] receiverArr = receiver.split(" ");
				String bank = "";
				if(receiverArr.length > 1){
					receiver = "";
					for(int i = 1 ; i < receiverArr.length ; i++){
						if(!receiverArr[i].trim().equals(" ")){
						   receiver += receiverArr[i];
						   if(i != receiverArr.length - 1)
							   receiver += "\r\n";
						}
					}
					bank = receiverArr[0];
				}
				String address = objArr[3] == null ? "" : (String)objArr[3];
				String[] addressArr = address.split(" ");
				String zip = "";
				if(addressArr.length > 1){
					zip = addressArr[0];
					address = "";
					for(int i = 1 ; i < addressArr.length ; i++){
						if(!addressArr[i].trim().equals(" ")){
						   address += addressArr[i];						
						}
					}
				}				
				String policyNo = objArr[4] == null ? "" : (String)objArr[4];
				String recName = objArr[5] == null ? "" : (String)objArr[5];
				recName = recName.replaceAll("　", "");
				Date deliverTime = (Date)objArr[6];				
				Date cycleDate = (Date)objArr[13];
				String registerNo = objArr[14] == null ? "" : (String)objArr[14];
				boolean substract = objArr[16] == null ? false : (Boolean)objArr[16];
				String substractNm = objArr[17] == null ? "" : (String)objArr[17];
				//String registerType = (objArr[15] == null || !(Boolean)objArr[15]) ? "單" : "雙";
				//if(notShowTime)
					//registerType = "";
				//registerNo = registerType + "\r\n" + registerNo;
				if(!channelNm.equals(prevChannelNm)){					
					packDetail.addCell(new Label(0, line, channelNm, txtFormat));					
					prevChannelNm = channelNm;
				}
				packDetail.addCell(new Label(1, line, policyNo, txtFormat));
				if(firstUniqueNo != null && !firstUniqueNo.equals(prevFirstNo)){					
					prevFirstNo = firstUniqueNo;
				}
				packDetail.addCell(new Label(2, line, bank, txtFormat));
				
				if(!receiver.equals(prevReceiver)){					
					prevReceiver = receiver;
				}
				//packDetail.addCell(new Label(3, line, registerNo, txtFormat));
				packDetail.addCell(new Label(3, line, receiver, txtFormat));
				packDetail.addCell(new Label(4, line, zip, txtFormat));
				packDetail.addCell(new Label(5, line, address, txtFormat));
				
				packDetail.addCell(new Label(6, line, recName, txtFormat));
				if(cycleDate != null){
					//cal = Calendar.getInstance();
					Calendar cal = Calendar.getInstance();
					cal.setTime(cycleDate);
					cal.add(Calendar.YEAR, - 1911);
					String dateStr = rocSdf.format(cal.getTime());
					packDetail.addCell(new Label(7, line, dateStr, txtFormat));
					//packDetail.addCell(new jxl.write.DateTime(7, line, cycleDate, dateFormat));
				}
				if(!expressDeliver)
				   packDetail.addCell(new Label(8, line, registerNo, txtFormat));
				else
				   packDetail.addCell(new Label(8, line, "送快遞", txtFormat));
				if(deliverTime != null ){
					Calendar cal = Calendar.getInstance();
					cal.setTime(deliverTime);
					cal.add(Calendar.YEAR, - 1911);
					String dateStr = rocSdf.format(cal.getTime());
					packDetail.addCell(new Label(9, line, dateStr, txtFormat));					
				   //packDetail.addCell(new jxl.write.DateTime(8, line, deliverTime, dateFormat));
				}else{
				   packDetail.addCell(new Label(9, line, "未交寄", txtFormat));
				}
				packDetail.setRowView(line, rowView);
				if(channelNm == "驗單錯誤"){
					line++;
					packDetail.mergeCells(2, line, 9, line);
					packDetail.addCell(new Label(1, line, "理由：", txtFormat));
					packDetail.addCell(new Label(2, line, verifyResult, txtFormat));
					if(substract){
						packDetail.addCell(new Label(2, line, "審查科" + substractNm + "指定抽件", txtFormat));
					}
				}
				line++;
			}
			line = 1;
						
			if(applyDatas == null || applyDatas.size() == 0){
				noPack.setName("無待送保單");
				noPack.addCell(new Label(0, 1, "", noPack.getCell(0, 1).getCellFormat()));
				noPack.addCell(new Label(1, 1, "", noPack.getCell(0, 1).getCellFormat()));
				noPack.addCell(new Label(2, 1, "", noPack.getCell(0, 1).getCellFormat()));
				noPack.addCell(new Label(3, 1, "", noPack.getCell(0, 1).getCellFormat()));
				noPack.addCell(new Label(4, 1, "", noPack.getCell(0, 1).getCellFormat()));
				noPack.addCell(new Label(5, 1, "", noPack.getCell(0, 1).getCellFormat()));
				noPack.addCell(new Label(6, 1, "", noPack.getCell(0, 1).getCellFormat()));
				noPack.addCell(new Label(7, 1, "", noPack.getCell(0, 1).getCellFormat()));
				noPack.addCell(new Label(8, 1, "", noPack.getCell(0, 1).getCellFormat()));
				noPack.addCell(new Label(9, 1, "", noPack.getCell(0, 1).getCellFormat()));
			}
			for(ApplyData applyData : applyDatas){
				//System.out.println(objArr[0] + "|" + objArr[1] + "|" + objArr[2] + "|" + objArr[3] + "|" + objArr[4] + "|" + objArr[5] + "|" + objArr[6] + "|" + objArr[7] );
				String channelNm = applyData.getChannelName() == null? "" : applyData.getChannelName();				
				String vendorId = ""; 				
				String receiver = applyData.getReceiver() == null? "" : applyData.getReceiver();
				String sourceCode = applyData.getSourceCode() == null? "" : applyData.getSourceCode();
				String deliverType = applyData.getDeliverType() == null? "" : applyData.getDeliverType();
				boolean notShowTime = false;
				if(receiver != null && receiver.equals("北二行政中心")){
					if("REPT".equals(sourceCode) || "CONV".equals(sourceCode) || "REIS".equals(sourceCode)){
						channelNm = "補單回送";
					}else if("B".equals(deliverType)){
						channelNm = "系統指定回送";
						receiver = "北二行政中心 ";
					}else{
						channelNm = "系統查無地址";
						receiver = "北二行政中心  ";
					}
					notShowTime = true;
					vendorId = "";
				}else if(receiver != null && receiver.equals("北二審查科")){
					channelNm = "驗單錯誤";
					vendorId = "";
					notShowTime = true;
				}
				
				String[] receiverArr = receiver.split(" ");
				String bank = "";
				if(receiverArr.length > 1){
					receiver = "";
					for(int i = 1 ; i < receiverArr.length ; i++){
						if(!receiverArr[i].trim().equals(" ")){
						   receiver += receiverArr[i];
						   if(i != receiverArr.length - 1)
							   receiver += "\r\n";
						}
					}
					bank = receiverArr[0];
				}
				String address = applyData.getAddress()  == null? "" : applyData.getAddress();
				String[] addressArr = address.split(" ");
				String zip = "";
				if(addressArr.length > 1){
					zip = addressArr[0];
					address = "";
					for(int i = 1 ; i < addressArr.length ; i++){
						if(!addressArr[i].trim().equals(" ")){
						   address += addressArr[i];
						}
					}
				}
				
				String policyNo = applyData.getPolicyNos() != null && applyData.getPolicyNos().length() >= 2? applyData.getPolicyNos().substring(1, applyData.getPolicyNos().length() -1) : "";
				String recName = applyData.getRecName();
				recName = recName.replaceAll("　", "");
				Date cycleDate = applyData.getCycleDate();				
				if(!channelNm.equals(prevChannelNm)){					
					noPack.addCell(new Label(0, line, channelNm, txtFormat));					
					prevChannelNm = channelNm;
				}
				noPack.addCell(new Label(1, line, policyNo, txtFormat));
				noPack.addCell(new Label(2, line, bank, txtFormat));
				prevVendorId = vendorId;
								
				if(!receiver.equals(prevReceiver)){					
					prevReceiver = receiver;
				}
				noPack.addCell(new Label(3, line, receiver, txtFormat));
				noPack.addCell(new Label(4, line, zip, txtFormat));
				noPack.addCell(new Label(5, line, address, txtFormat));				
				noPack.addCell(new Label(6, line, recName, txtFormat));
				Calendar cal = Calendar.getInstance();
				if(cycleDate != null){
				   cal.setTime(cycleDate);
				   cal.add(Calendar.YEAR, - 1911);
				   String dateStr = rocSdf.format(cal.getTime());
				   noPack.addCell(new Label(7, line, dateStr, txtFormat));
				   //noPack.addCell(new jxl.write.DateTime(7, line, date, dateFormat));
				}
				
				noPack.setRowView(line, rowView);
				line++;
			}
			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			throw e;
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
	}
	public static String generateLogisticReports(List<LogisticStatus> lss, Session session, boolean group) throws Exception{
		if(group)
			return generateGroupLogisticReports(lss, session);
		else{
			if(lss != null && lss.size() > 0 && lss.get(0).getCenter().equals("06")){
				return generateLogisticReports(lss, session, "06");
			}
		}
		String serverPath = ServletContextGetter.getRealPath("");				
		String labelName = "logisticDetail.xls";
		ClassPathResource xlsResource = new ClassPathResource(labelName);
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "logisticDetail.xls_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;
		Calendar cal = Calendar.getInstance();
		
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			
			WritableSheet packDetail = null;
			packDetail = wwb.getSheet(0);
			WritableSheet applyDetail = null;
			applyDetail = wwb.getSheet(1);
			
			
			
			//設定縮放及斷行
			SheetSettings packDetailSettings = packDetail.getSettings();
			SheetSettings applyDetailSettings = packDetail.getSettings();

		  	packDetailSettings.setFitToPages(false);			
		  	packDetailSettings.setPageBreakPreviewMode(true);
		  	applyDetailSettings.setFitToPages(false);			
		  	applyDetailSettings.setPageBreakPreviewMode(true);
			
			CellFormat textFormat = packDetail.getCell(0, 6).getCellFormat();
			CellFormat pageTitleFormat = packDetail.getCell(2, 0).getCellFormat(); //清單title
			CellFormat codeFormat = packDetail.getCell(1, 1).getCellFormat(); //清單編號
			CellFormat titleFormat = packDetail.getCell(0, 1).getCellFormat(); //清單title
			CellFormat subTitleFormat = packDetail.getCell(0, 5).getCellFormat(); //清單title
			
			int packDetailLine = 0;
			List<PackStatus> packs = new ArrayList<PackStatus>();
			for(LogisticStatus ls : lss){
				packs.addAll(ls.getPackStatuses());
				
				
				packDetail.addCell(new Label(2, packDetailLine, "裝箱明細", pageTitleFormat));								
				packDetailLine++;
				
				packDetail.addCell(new Label(0, packDetailLine, "富士施樂編號", titleFormat));								
				packDetail.addCell(new Label(1, packDetailLine, ls.getLogisticId(), codeFormat));
				packDetailLine++;
				
				packDetail.addCell(new Label(0, packDetailLine, group? "掛號號碼" : "超峰號碼", titleFormat));								
				packDetail.addCell(new Label(1, packDetailLine, ls.getVendorId(), codeFormat));
				packDetailLine++;

				
				packDetail.addCell(new Label(0, packDetailLine, "服務中心：", titleFormat));
				packDetail.addCell(new Label(1, packDetailLine, ls.getName(), codeFormat));
				packDetail.addCell(new Label(2, packDetailLine, "電話：", titleFormat));
				packDetail.addCell(new Label(3, packDetailLine, ls.getTel(), codeFormat));
				packDetailLine++;
				
				packDetail.addCell(new Label(0, packDetailLine, "地址：", titleFormat));
				packDetail.addCell(new Label(1, packDetailLine, ls.getAddress(), codeFormat));
				packDetailLine++;
				
				packDetail.addCell(new Label(0, packDetailLine, "打包編號：", subTitleFormat));
				packDetail.addCell(new Label(1, packDetailLine, "單位名", subTitleFormat));
				packDetail.addCell(new Label(2, packDetailLine, "單位编號", subTitleFormat));
				packDetail.addCell(new Label(3, packDetailLine, "單位電話", subTitleFormat));
				packDetailLine++;
				
				Set<PackStatus> packSet = ls.getPackStatuses();				
				for(PackStatus ps: packSet){
					packDetail.addCell(new Label(0, packDetailLine, ps.getPackId() ,textFormat));
					packDetail.addCell(new Label(1, packDetailLine, ps.getSubAreaName() ,textFormat));
					packDetail.addCell(new Label(2, packDetailLine, ps.getSubAreaId() ,textFormat));
					packDetail.addCell(new Label(3, packDetailLine, ps.getSubAreaTel() ,textFormat));
					packDetailLine++;
				}				

				packDetailLine++;
				packDetail.addCell(new Label(0, packDetailLine, "共" + ls.getPacks() + "袋", titleFormat));
				packDetailLine++;
				//加入斷行記號，跳下一頁
				packDetail.addRowPageBreak(packDetailLine);				
			}
			
			textFormat = applyDetail.getCell(0, 5).getCellFormat(); //清單列表
			pageTitleFormat = applyDetail.getCell(2, 0).getCellFormat(); //清單title
			CellFormat detailBarcodeFormat = applyDetail.getCell(3, 0).getCellFormat(); //清單barcode
			titleFormat = applyDetail.getCell(0, 3).getCellFormat(); //清單title
			CellFormat subFormat = applyDetail.getCell(0, 1).getCellFormat(); //一般說明

			
			int policyLine = 0;
			for(PackStatus pack : packs){								
				//保單明細部分				
				applyDetail.addCell(new Label(2, policyLine, "保單明細", pageTitleFormat));
				applyDetail.mergeCells(3, policyLine, 4, policyLine);
				applyDetail.addCell(new Label(3, policyLine, pack.getPackId(), detailBarcodeFormat));				
				policyLine++;				
				
				applyDetail.addCell(new Label(0, policyLine, "轄區名稱", subFormat));
				applyDetail.addCell(new Label(1, policyLine, pack.getSubAreaName(), subFormat));
				List<Area> areaList = session.createQuery("from Area where address is not null and address <> '' and areaName is not null "
						+ "and areaName <> '' and areaId in (select distinct serviceCenter from Area where subAreaId = '" + pack.getSubAreaId() + "')").list();
				if(areaList != null && areaList.size() > 0){					
				   Area serviceCenter = areaList.get(0);
				   applyDetail.addCell(new Label(3, policyLine, "服務中心", subFormat));
				   applyDetail.addCell(new Label(4, policyLine, serviceCenter.getAreaName(), subFormat));
				}								
				policyLine++;
				
				applyDetail.addCell(new Label(0, policyLine, group? "掛號號碼" : "超峰號碼", subFormat));
				applyDetail.addCell(new Label(1, policyLine, pack.getLogisticStatus().getVendorId(), subFormat));
				applyDetail.addCell(new Label(3, policyLine, "狀態", subFormat));
				applyDetail.addCell(new Label(4, policyLine, pack.getStatusNm(), subFormat));
				//applyDetail.addCell(new Label(4, policyLine, pack.isBack()? "退件" : "正常件", subFormat));												
				policyLine++;
				
				
				Set<ApplyData> appSet = null;
				if(group){
					List<ApplyData> applyDatas = session.createQuery("from ApplyData where packId = ? and receipt = false order by policyNos asc").setString(0, pack.getPackId()).list();
					appSet = new LinkedHashSet<ApplyData>();
					appSet.addAll(applyDatas);
					
				}else{
					appSet = pack.getApplyDatas();
				}
				
				int policyColumn = 0;

				int line = 0;
				boolean policyNotMergeFirstLine = true;
				boolean policyMergeFirstLine = true;
				if(group){
					applyDetail.addCell(new Label(0, policyLine, "序號"  ,titleFormat));					
					applyDetail.addCell(new Label(1, policyLine, "轄區代號"  ,titleFormat));
					applyDetail.addCell(new Label(2, policyLine, "保單號碼"  ,titleFormat));
					applyDetail.addCell(new Label(3, policyLine, "要保單位"  ,titleFormat));
					applyDetail.addCell(new Label(4, policyLine, ""  ,titleFormat));
					applyDetail.mergeCells(3, line, 4, policyLine);
					policyLine ++;
				}
				
				int serialNo = 0;
				for(ApplyData applyData: appSet){
					// 只取保單，簽收單和團險證不管
					if (applyData.getReceipt() != null
							&& !applyData.getReceipt()) {
						int column = 0;
						boolean merge = applyData.getMerger() == null ? false
								: applyData.getMerger();
						column = policyColumn;
						line = policyLine;
						// 如果是第一行，加入title
						if (merge && policyMergeFirstLine && !group) {
							// 如果之前已進入過notMerge的話，要加一行
							if (!policyNotMergeFirstLine) {
								line++;
							}
							applyDetail.addCell(new Label(0, line, "受理編號（轄區）",
									titleFormat));
							applyDetail.mergeCells(1, line, 4, line);
							applyDetail.addCell(new Label(1, line,
									"保 單 號 碼（轄區）", titleFormat));
							line++;
							policyMergeFirstLine = false;
						} else if (!merge && policyNotMergeFirstLine && !group) {
							applyDetail.addCell(new Label(0, line, "受理編號（轄區）",
									titleFormat));
							applyDetail.addCell(new Label(1, line, "受理編號（轄區）",
									titleFormat));
							applyDetail.addCell(new Label(2, line, "受理編號（轄區）",
									titleFormat));
							applyDetail.addCell(new Label(3, line, "受理編號（轄區）",
									titleFormat));
							applyDetail.addCell(new Label(4, line, "受理編號（轄區）",
									titleFormat));
							line++;
							policyNotMergeFirstLine = false;
						}

						if (!merge && !group) {
							if (column > 4) {
								column = 0;
								line++;
							}
							applyDetail.addCell(new Label(column++, line,
									applyData.getApplyNo() + "("
											+ pack.getSubAreaId() + ")",
									textFormat));

						} else if (merge && !group) {
							applyDetail.addCell(new Label(0, line, applyData
									.getApplyNo(), textFormat));
							Set<String> policyNos = applyData.getPolicyNoSet();
							int mergeColumn = 1;
							for (String policyNo : policyNos) {
								applyDetail.addCell(new Label(mergeColumn++,
										line, policyNo + "("
												+ pack.getSubAreaId() + ")",
										textFormat));
							}
							line++;
						} else if (group) {
							applyDetail.mergeCells(3, line, 4, line);
							applyDetail.addCell(new Label(0, line, ++serialNo
									+ "", textFormat));
							applyDetail.addCell(new Label(1, line, applyData
									.getAreaId(), textFormat));
							applyDetail.addCell(new Label(2, line, applyData
									.getPolicyNos().replaceAll(",", ""),
									textFormat));
							applyDetail.addCell(new Label(3, line, applyData
									.getRecName(), textFormat));
							line++;
						}
						policyLine = line;
						policyColumn = column;
					}
				}
				policyLine++;
				//加入斷行記號，跳下一頁
				applyDetail.addRowPageBreak(policyLine);
				
			}
			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			throw e;
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
	}
	
	public static String generateMatchLabels(List<ApplyData> applyDatas, Session session, boolean noBankReceipt) throws Exception{	
		
		
		String serverPath = ServletContextGetter.getRealPath("");				
		String labelName = "policyPack.xls";
		
		ClassPathResource xlsResource = new ClassPathResource(labelName);
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "policyPack_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;
		Calendar cal = Calendar.getInstance();
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			WritableSheet brSheet = null;
			
			ws = wwb.getSheet(0); //label
			brSheet = wwb.getSheet(1); //收金單清單
			//設定縮放及斷行
			SheetSettings wsSettings = ws.getSettings();
			SheetSettings brSettings = brSheet.getSettings();
			
			wsSettings.setFitToPages(false);
		  	wsSettings.setPageBreakPreviewMode(true);
		  	
		  	brSettings.setFitToPages(false);
		  	
		  	CellFormat brTitleFormat = brSheet.getCell(0, 1).getCellFormat();
		  	CellFormat brTxtFormat = brSheet.getCell(0, 2).getCellFormat();			
			CellFormat brDateFormat = brSheet.getCell(4, 2).getCellFormat();
			CellFormat brNumFormat = brSheet.getCell(5, 2).getCellFormat();
			
			CellFormat barcodeFormat = ws.getCell(0, 0).getCellFormat();			
			CellFormat strFormat = ws.getCell(0, 1).getCellFormat();
			CellFormat splitFormat = ws.getCell(0, 2).getCellFormat();
			CellView barcodeView = ws.getRowView(0);
			CellView bodyView = ws.getRowView(1);
			CellView splitView = ws.getRowView(2);
			CellView lastBodyView = ws.getRowView(22);
			CellView lastSplitView = ws.getRowView(23);
			
			int columnCounter = 0;
			int lineCounter = 0;
			
            String center = null;
            Date cycleDate = null;
            boolean group = false;
            List<String> oldBatchNames = new ArrayList<String>();
			for(ApplyData applyData : applyDatas){
				oldBatchNames.add(applyData.getOldBatchName());
				center = applyData.getCenter();
				cycleDate = applyData.getCycleDate();
				group = "GROUP".equals(applyData.getSourceCode())? true : false;
				//計算移到第幾個Column
				if(columnCounter > 4){
                    if((lineCounter + 1 + 2) % 24 == 0){
                    	ws.addRowPageBreak(lineCounter + 1 + 2);
					}
					lineCounter += 3;
					columnCounter = 0;
					
				}
				String prePolicyNo = null;
				for(String policyNo : applyData.getPolicyNoSet()){
					if(prePolicyNo == null && (policyNo != null || policyNo.compareTo(prePolicyNo) < 0)){
						prePolicyNo = policyNo;
					}
				}
					
				String forWrite = applyData.getRecName() + "\r\n" + "保單號碼:" + prePolicyNo;
				//先寫bar code
				ws.addCell(new Label(columnCounter, lineCounter, "*" + applyData.getUniqueNo() + "*", barcodeFormat));
				ws.setRowView(lineCounter, barcodeView);
				ws.addCell(new Label(columnCounter, lineCounter + 1, forWrite, strFormat));
				
				ws.addCell(new Label(columnCounter, lineCounter + 2, " ", splitFormat));
				ws.setRowView(lineCounter + 2, splitView);
				
				columnCounter += 2;
				
				if((lineCounter + 3) % 24 == 0){
					ws.setRowView(lineCounter + 1, lastBodyView);
					ws.setRowView(lineCounter + 2, lastSplitView);
				}else{
					ws.setRowView(lineCounter + 1, bodyView);
				}						
			}	
			Criteria criteria = session.createCriteria(BankReceipt.class, "bankReceipt")
                    .createAlias("bankReceipt.applyData", "applyData", Criteria.INNER_JOIN);
			
			
			if(!noBankReceipt){
			   criteria.add(Restrictions.eq("applyData.cycleDate", cycleDate));			   
			}else{
			   criteria.add(Restrictions.in("oldBatchName", oldBatchNames));
			}
			criteria.add(Restrictions.or(Restrictions.isNull("applyData.bkReceiptMatched"), Restrictions.eq("applyData.bkReceiptMatched", true)));
			if(group)
				criteria.add(Restrictions.eq("applyData.sourceCode", "GROUP"));
			else
				criteria.add(Restrictions.ne("applyData.sourceCode", "GROUP"));
			criteria.add(Restrictions.eq("applyData.center", center));
			criteria.add(Restrictions.eq("applyData.receipt", false));
			criteria.addOrder(Order.asc("applyData.newBatchName")).addOrder(Order.asc("bankReceipt.center"))
			     .addOrder(Order.asc("bankReceipt.receiveDate")).addOrder(Order.asc("bankReceipt.dateCenterSerialNo"))
			     .addOrder(Order.asc("bankReceipt.bankReceiptId")).addOrder(Order.asc("applyData.uniqueNo"));
			//criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			List<BankReceipt> bankReceipts = criteria.list();
			int brLineCounter = 2;
			if(bankReceipts != null && bankReceipts.size() > 0){
				brSheet.removeRow(2);
				Map<String, List<BankReceipt>> centerMap = new HashMap<String, List<BankReceipt>>();
				Map<String, Map<String, List<BankReceipt>>> newBatchNameMap = new HashMap<String, Map<String, List<BankReceipt>>>();
				Map<String, Map<Date, List<BankReceipt>>> centerDateMap = new HashMap<String, Map<Date, List<BankReceipt>>>();
				Set<Date> receiveDateSet = new TreeSet<Date>();
				for(BankReceipt bankReceipt : bankReceipts){
					
					String bCenter = bankReceipt.getCenter();
					String newBatchName = null;
					if(bankReceipt.getApplyData() != null)
					   newBatchName = bankReceipt.getApplyData().getNewBatchName();
					if(bCenter != null){
					   List<BankReceipt> list = centerMap.get(bCenter);
					   
					   Map<Date, List<BankReceipt>> dateMap = centerDateMap.get(bCenter);
					   if(list == null)
						   list = new ArrayList<BankReceipt>();
					   if(dateMap == null)
						   dateMap = new HashMap<Date, List<BankReceipt>>();
					   Date receiveDate = bankReceipt.getReceiveDate();
					   if(receiveDate != null){
						   List<BankReceipt> dateList = dateMap.get(receiveDate);
						   if(dateList == null){
							   dateList = new ArrayList<BankReceipt>();
						   }
						   dateList.add(bankReceipt);
						   dateMap.put(receiveDate, dateList);
						   centerDateMap.put(bCenter, dateMap);
					   }
					   list.add(bankReceipt);
					   centerMap.put(bCenter, list);
					}else{
						Map<Date, List<BankReceipt>> dateMap = centerDateMap
								.get("其它");

						if (dateMap == null)
							dateMap = new HashMap<Date, List<BankReceipt>>();
						Date receiveDate = bankReceipt.getReceiveDate();
						if (receiveDate != null) {
							List<BankReceipt> dateList = dateMap
									.get(receiveDate);
							if (dateList == null) {
								dateList = new ArrayList<BankReceipt>();
							}
							dateList.add(bankReceipt);
							dateMap.put(receiveDate, dateList);
							centerDateMap.put("其它", dateMap);
						}
					}
					if(newBatchName != null){
					   Map<String, List<BankReceipt>> insideMap = newBatchNameMap.get(newBatchName);					   
					   if(insideMap == null)
						   insideMap = new HashMap<String, List<BankReceipt>>();
					   
					   List<BankReceipt> list = null;
					   if(bCenter != null)
						   list = insideMap.get(bCenter);
					   if(list == null)
						   list = new ArrayList<BankReceipt>();
					   list.add(bankReceipt);
					   if(bCenter == null){
						   insideMap.put("其它", list);
					   }else{
						   insideMap.put(bCenter, list);
					   }					   
					   newBatchNameMap.put(newBatchName, insideMap);
					   
					   brSheet.addCell(new Label(0, brLineCounter, newBatchName, brTxtFormat));
					}					    
					if(bankReceipt.getCenter() != null)
					    brSheet.addCell(new Label(1, brLineCounter, bankReceipt.getCenter(), brTxtFormat));
					if(bankReceipt.getApplyData() != null && bankReceipt.getApplyData().getRecName() != null)
					    brSheet.addCell(new Label(2, brLineCounter, bankReceipt.getApplyData().getRecName(), brTxtFormat));
					brSheet.addCell(new Label(3, brLineCounter, bankReceipt.getBankReceiptId(), brTxtFormat));
					if(bankReceipt.getReceiveDate() != null){
						receiveDateSet.add(bankReceipt.getReceiveDate());
					    brSheet.addCell(new jxl.write.DateTime(4, brLineCounter, bankReceipt.getReceiveDate(), brDateFormat));
					}
					if(bankReceipt.getDateCenterSerialNo() != null)
						brSheet.addCell(new jxl.write.Number(5, brLineCounter, bankReceipt.getDateCenterSerialNo(), brNumFormat));					
					if(bankReceipt.getApplyData() != null && bankReceipt.getApplyData().getUniqueNo() != null )
						brSheet.addCell(new Label(6, brLineCounter, bankReceipt.getApplyData().getUniqueNo(), brTxtFormat));					
					brLineCounter++;
				}	
				brLineCounter = brLineCounter + 2;
				
				Set<String> keySet = new LinkedHashSet<String>();
				keySet.add("台北");
				keySet.add("桃園");
				keySet.add("台中");
				keySet.add("台南");
				keySet.add("高雄");
				brSheet.addCell(new Label(0, brLineCounter, "地區", brTitleFormat));
				int column = 1;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				for(Date receiveDate : receiveDateSet){
					brSheet.addCell(new Label(column++, brLineCounter, sdf.format(receiveDate), brTitleFormat));
				}
				brSheet.addCell(new Label(column, brLineCounter, "總計", brTitleFormat));
				brLineCounter++;
				Map<Date, Integer> dateSumMap = new HashMap<Date, Integer>();
				for(String key : keySet){
					Map<Date, List<BankReceipt>> insideMap = centerDateMap.get(key);
					if(insideMap != null && insideMap.size() > 0){
					   brSheet.addCell(new Label(0, brLineCounter, key, brTxtFormat));
					   int columnCount = 1;
					   int centerSum = 0;
					   for(Date receiveDate : receiveDateSet){
						   Integer dateSum = dateSumMap.get(receiveDate) == null? 0 : dateSumMap.get(receiveDate);
						   List<BankReceipt>insideList = insideMap.get(receiveDate);
						   if(insideList != null){
							   dateSum += insideList.size();
							   dateSumMap.put(receiveDate, dateSum);
							   brSheet.addCell(new jxl.write.Number(columnCount, brLineCounter, insideList.size(), brNumFormat));
							   centerSum += insideList.size();
						   }						
						   columnCount++;
					   }					
					   brSheet.addCell(new jxl.write.Number(columnCount, brLineCounter, centerSum, brNumFormat));
					   brLineCounter++;
					}
				}
				brSheet.addCell(new Label(0, brLineCounter, "總計", brTitleFormat));
				int total = 0;
				int columnCount = 1;				
				for(Date receiveDate : receiveDateSet){
				    Integer sum = dateSumMap.get(receiveDate) == null? 0 : dateSumMap.get(receiveDate);
				    total += sum;
				    brSheet.addCell(new jxl.write.Number(columnCount, brLineCounter, sum, brNumFormat));
					columnCount++;
				}					
				brSheet.addCell(new jxl.write.Number(columnCount, brLineCounter, total, brNumFormat));
				
				
				brLineCounter += 2;
				brSheet.addCell(new Label(0, brLineCounter, "列印檔", brTitleFormat));
				brSheet.addCell(new Label(1, brLineCounter, "台北", brTitleFormat));
				brSheet.addCell(new Label(2, brLineCounter, "桃園", brTitleFormat));
				brSheet.addCell(new Label(3, brLineCounter, "台中", brTitleFormat));
				brSheet.addCell(new Label(4, brLineCounter, "台南", brTitleFormat));
				brSheet.addCell(new Label(5, brLineCounter, "高雄", brTitleFormat));
				brSheet.addCell(new Label(6, brLineCounter, "總計", brTitleFormat));
				brLineCounter++;
				keySet = new TreeSet(newBatchNameMap.keySet());
				Map<String, Integer> centerTotal = new HashMap<String, Integer>();
				for(String key : keySet){
					Map<String, List<BankReceipt>> insideMap = newBatchNameMap.get(key);
					brSheet.addCell(new Label(0, brLineCounter, key, brTxtFormat));					
					int newBatchNameSum = 0;
					for(int i = 1 ; i <= 5 ; i++){
						String bcenter = "其它";
						switch(i){
						   case 1:
							   bcenter = "台北";
							   break;
						   case 2:
							   bcenter = "桃園";
							   break;
						   case 3:
							   bcenter = "台中";
							   break;
						   case 4:
							   bcenter = "台南";
							   break;
						   case 5:
							   bcenter = "高雄";
							   break;							   
						}
						    
						
						List<BankReceipt>insideList = insideMap.get(bcenter);
						Integer centerCount = centerTotal.get(bcenter) == null? 0 : centerTotal.get(bcenter);
						if(insideList != null){
							centerCount += insideList.size();
							brSheet.addCell(new jxl.write.Number(i, brLineCounter, insideList.size(), brNumFormat));
							newBatchNameSum += insideList.size();
						}				
						centerTotal.put(bcenter, centerCount);						
					}					
					brSheet.addCell(new jxl.write.Number(6, brLineCounter, newBatchNameSum, brNumFormat));
					brLineCounter++;
				}
				total = 0;
				brSheet.addCell(new Label(0, brLineCounter, "總計", brTitleFormat));
				
				Integer count = centerTotal.get("台北");
				total += count;
				brSheet.addCell(new jxl.write.Number(1, brLineCounter, count, brNumFormat));
				count = centerTotal.get("桃園");
				total += count;
				brSheet.addCell(new jxl.write.Number(2, brLineCounter, count, brNumFormat));
				count = centerTotal.get("台中");
				total += count;
				brSheet.addCell(new jxl.write.Number(3, brLineCounter, count, brNumFormat));
				count = centerTotal.get("台南");
				total += count;
				brSheet.addCell(new jxl.write.Number(4, brLineCounter, count, brNumFormat));
				count = centerTotal.get("高雄");
				total += count;
				brSheet.addCell(new jxl.write.Number(5, brLineCounter, count, brNumFormat));
				brSheet.addCell(new jxl.write.Number(6, brLineCounter, total, brNumFormat));
				
				keySet = centerMap.keySet();
				int sheetCounter = 2;
				WritableSheet centerSheet = null;
				for(String key : keySet){
					wwb.copySheet(2, key, ++sheetCounter);
					centerSheet = wwb.getSheet(sheetCounter); //收金單清單
					centerSheet.addCell(new Label(0, 0, "國泰人壽保險股份有限公司\r\n送金單領件清單_" + key, centerSheet.getCell(0, 0).getCellFormat()));
					brLineCounter = 2;
					bankReceipts = centerMap.get(key);
					int serialNo = 0;
					 
					for(BankReceipt bankReceipt : bankReceipts){
						centerSheet.addCell(new jxl.write.Number(0, brLineCounter, ++serialNo, centerSheet.getCell(0, 2).getCellFormat()));
						centerSheet.addCell(new Label(1, brLineCounter, bankReceipt.getBankReceiptId(), brTxtFormat));
						ApplyData applyData = bankReceipt.getApplyData();						
						if(applyData.getRecName() != null)
							centerSheet.addCell(new Label(2, brLineCounter, applyData.getRecName(), brTxtFormat));						
						if(applyData.getReceiverBank() != null)
							centerSheet.addCell(new Label(3, brLineCounter, applyData.getReceiverBank(), brTxtFormat));
						
						String receiveDate = "" ;
						if(bankReceipt.getReceiveDate() != null)
						   receiveDate = Constant.slashedyyyyMMdd.format(bankReceipt.getReceiveDate());
						if(bankReceipt.getDateCenterSerialNo() != null)
						   receiveDate += "_" + bankReceipt.getDateCenterSerialNo();						
					    centerSheet.addCell(new Label(4, brLineCounter, receiveDate, brTxtFormat));
						
						String policyNos = applyData.getPolicyNos();
						if(policyNos != null && policyNos.length() > 2){
							policyNos = policyNos.substring(1, policyNos.length() - 1);
						}
						if(applyData.getPolicyNos() != null)
							centerSheet.addCell(new Label(5, brLineCounter, policyNos, brTxtFormat));

						brLineCounter++;
					}	
					
				}
				
			}
			wwb.removeSheet(2);			
			wwb.write();
			if(bankReceipts == null || bankReceipts.size() == 0)
			   wwb.removeSheet(1);
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			throw e;
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
	}
	
	public static String generateLogisticLabels(List<LogisticStatus> lss, Session session, boolean taipeiNo2) throws Exception{		
		if(!taipeiNo2){			
			return generateLogisticLabels( lss,  session);
		}else{
			String serverPath = ServletContextGetter.getRealPath("");				
			String labelName = "label3_mail.xls";
			
			List<String> logisticIds = new ArrayList<String>();
			
			for(LogisticStatus ls : lss){				
				logisticIds.add(ls.getLogisticId());
			}
			HashMap<String, List<ApplyData>> logisticMap = new HashMap<String, List<ApplyData>>();
			List<ApplyData> applyDatas = session.createQuery("select a from ApplyData a inner join a.packSatus p where (a.groupInsure is null or a.groupInsure = false) "
					+ "and (a.receipt is null or a.receipt = false) "
					+ "and p.logisticId in (:logisticIds) order by a.packId")
					.setParameterList("logisticIds", logisticIds).list();
			List<PackStatus> packStatuses = session.createQuery("from PackStatus where logisticId in (:logisticIds) order by firstUniqueNo")
					.setParameterList("logisticIds", logisticIds).list();
			HashMap<String, LogisticStatus> packLogisticMap = new HashMap<String, LogisticStatus>();
			for(ApplyData applyData : applyDatas){
				for(PackStatus packStatus : packStatuses){					
					for(LogisticStatus ls : lss){
						if(ls.getLogisticId().equals(packStatus.getLogisticId())){
							packLogisticMap.put(packStatus.getPackId(), ls);
							break;
						}
					}
					if(packStatus.getPackId().equals(applyData.getPackId())){
						if(packStatus.getLogisticId() != null){
							List<ApplyData> appList = logisticMap.get(packStatus.getLogisticId()) == null? new ArrayList<ApplyData>() : logisticMap.get(packStatus.getLogisticId());
							appList.add(applyData);
							logisticMap.put(packStatus.getLogisticId(), appList);							
							break;
						}
					}
				}
			}
			
			ClassPathResource xlsResource = new ClassPathResource(labelName);
			long time = new Date().getTime();
			String targetfileName = "";
			targetfileName = "label3_mail" + time + ".xls";
			String targetfile = serverPath + "/pdf/" + targetfileName;
			if (new File(targetfile).exists()) {
				new File(targetfile).delete();
			}		
			jxl.Workbook workbook = null;
			WritableWorkbook wwb = null;
			Calendar cal = Calendar.getInstance();
			try {
				workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
				wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
				WritableSheet ws = null;
				WritableSheet mailDetail = null;

				WritableSheet mailDetail2 = null;
				WritableSheet mailReturn2 = null;
				WritableSheet packLabel = null;
				WritableSheet parcelDetail = null;
				//查出所有保單
				
				ws = wwb.getSheet(0); //label
				mailDetail = wwb.getSheet(1);				
				mailReturn2 = wwb.getSheet(3);
				wwb.copySheet(1, "裝箱清單2", 4);				
				mailDetail2 = wwb.getSheet(4);				
				parcelDetail = wwb.getSheet(2);	

				//設定縮放及斷行
				SheetSettings wsSettings = ws.getSettings();
				SheetSettings mailSettings = mailDetail.getSettings();
				SheetSettings mrSettings2 = mailReturn2.getSettings();
				SheetSettings mrDetailSetting = mailDetail2.getSettings();
				SheetSettings parcelSettings = parcelDetail.getSettings();
				
				wsSettings.setFitToPages(false);
			  	wsSettings.setPageBreakPreviewMode(true);
			  	
			  	mailSettings.setFitToPages(false);
			  	mailSettings.setPageBreakPreviewMode(true);
			  				  	
			  	mrSettings2.setFitToPages(false);
			  	mrSettings2.setPageBreakPreviewMode(true);
			  	
			  	mrDetailSetting.setFitToPages(false);
			  	mrDetailSetting.setPageBreakPreviewMode(true);
			  	
			  	parcelSettings.setFitToPages(false);
			  	parcelSettings.setPageBreakPreviewMode(true);
				
				CellFormat barcodeFormat = ws.getCell(0, 0).getCellFormat();			
				CellFormat strFormat = ws.getCell(0, 1).getCellFormat();
				CellFormat splitFormat = ws.getCell(0, 2).getCellFormat();
				CellView barcodeView = ws.getRowView(0);
				CellView bodyView = ws.getRowView(1);
				CellView splitView = ws.getRowView(2);
				CellView lastBodyView = ws.getRowView(22);
				CellView lastSplitView = ws.getRowView(23);
				
				
				CellView [] mailReturn2View = new CellView[29];
				for(int i = 0 ; i < 29 ; i++){
					mailReturn2View[i] = mailReturn2.getRowView(i);
				}
				

				
				CellView mailLineView = mailDetail.getRowView(5);
				int columnCounter = 0; 
				int lineCounter = 0;
				int mailDetailLine = 5;
				int parcelCounter = 0;
				int parcelDetailLine = 5;
				int parcelLineBreakCheck = 5;
				int counter = 0;				
				int mrCounter = 0;
				int mailDetailLine2 = 5;				
				int lineBreakCheck = 5;
				int line2BreakCheck = 5;
				mailDetail.addCell(new jxl.write.DateTime(0, 1, new Date(), mailDetail.getCell(0, 1).getCellFormat()));				
				mailDetail2.addCell(new jxl.write.DateTime(0, 1, new Date(), mailDetail.getCell(0, 1).getCellFormat()));
				parcelDetail.addCell(new jxl.write.DateTime(0, 1, new Date(), mailDetail.getCell(0, 1).getCellFormat()));
				String batchOrOnline = "G";
				int mailReturnLine = 0;
				logisticIds = new ArrayList<String>();
				
				for(LogisticStatus ls : lss){
					batchOrOnline = ls.getBatchOrOnline();
					String logisticId = ls.getLogisticId();
					
					//計算移到第幾個Column
									
					boolean mailReceipt = ls.getMailReceipt() == null? false : ls.getMailReceipt();
					String mailReceiptStr = "雙";
					if(!mailReceipt){
						logisticIds.add(ls.getLogisticId());  //用來查出寄送國銀分行的PackStatus
						if(columnCounter > 4){
		                    if((lineCounter + 1 + 2) % 24 == 0){
		                    	ws.addRowPageBreak(lineCounter + 1 + 2);
							}
							lineCounter += 3;
							columnCounter = 0;
							
						}	
						mailReceiptStr = "單";
						if(ls.getWeight() != null && ls.getWeight().doubleValue() >= Constant.parcelWeight){
							mailReceiptStr = "包";
						}
					    String forWrite = ls.getName() + "\r\n" + ls.getAddress() + "\r\n" + mailReceiptStr + "：" + (ls.getVendorId() == null? "" : ls.getVendorId()) + "  保單本數:" + ls.getBooks() ;
					    //先寫bar code					
					    ws.addCell(new Label(columnCounter, lineCounter, "*" + ls.getLogisticId() + "*", barcodeFormat));
					    ws.setRowView(lineCounter, barcodeView);
					    ws.addCell(new Label(columnCounter, lineCounter + 1, forWrite, strFormat));					
					    ws.addCell(new Label(columnCounter, lineCounter + 2, " ", splitFormat));
					    columnCounter += 2;
					    if((lineCounter + 3) % 24 == 0){
							ws.setRowView(lineCounter + 1, lastBodyView);
							ws.setRowView(lineCounter + 2, lastSplitView);
						}else{
							ws.setRowView(lineCounter + 1, bodyView);
							ws.setRowView(lineCounter + 2, splitView);
						}
					}
					
							
					
					if((ls.getMailReceipt() == null || !ls.getMailReceipt())
							&& (ls.getWeight() == null || ls.getWeight() < Constant.parcelWeight)
							&& (ls.getVendorId() != null && !"送快遞".equals(ls.getVendorId()) && !"".equals(ls.getVendorId()))){
					   counter++;
					   //有掛號號碼時，且不需回執聯的狀況
					   mailDetail.addCell(new Label(0, mailDetailLine, StringUtils.leftPad(counter + "", 3, '0'), mailDetail.getCell(0, 5).getCellFormat()));
					   mailDetail.addCell(new Label(1, mailDetailLine, ls.getVendorId(), mailDetail.getCell(1, 5).getCellFormat()));
					   String name = (ls.getName() == null || ls.getName().equals(""))? "國壽服務中心" : ls.getName();
					   String address = ls.getAddress() == null? "無寄送地址" : ls.getAddress();
					   address = name + "\r\n" + address;
					   mailDetail.addCell(new Label(3, mailDetailLine, address, mailDetail.getCell(2, 5).getCellFormat()));
					   
					   List<ApplyData> ads = logisticMap.get(ls.getLogisticId());
					   int begin = mailDetailLine;
					   int end = mailDetailLine - 1;
					   if(ads != null){
						  for(ApplyData ad : ads){					
							 end++;
							 mailDetailLine = end;
							 mailDetail.setRowView(mailDetailLine, mailLineView);
						     if(ad.getPolicyNos() != null && ad.getPolicyNos().startsWith(",") && ad.getPolicyNos().endsWith(",")){
					            mailDetail.addCell(new Label(2, mailDetailLine, ad.getPolicyNos().substring(1, ad.getPolicyNos().length() - 1), mailDetail.getCell(2, 5).getCellFormat()));
					            if(end > begin){
					            	mailDetail.addCell(new Label(0, mailDetailLine, "", mailDetail.getCell(0, 5).getCellFormat()));
					            	mailDetail.addCell(new Label(1, mailDetailLine, "", mailDetail.getCell(1, 5).getCellFormat()));
					            	mailDetail.addCell(new Label(3, mailDetailLine, "", mailDetail.getCell(2, 5).getCellFormat()));
					            }
						     }
							 lineBreakCheck++;
						  }
						  //不要讓融合的格子被從中切斷
						  if((lineBreakCheck - 1) >= 23){	
							  int page = (end - begin) / 23;
							  for(int i = 0 ; i <= page ; i++){
								  mailDetail.addRowPageBreak(begin + i * 23);
							  }
							  lineBreakCheck = (end - begin) % 23;
							  lineBreakCheck++;
						  }
						  if(begin < end){
							  mailDetail.mergeCells(0, begin, 0, end);
							  mailDetail.mergeCells(1, begin, 1, end);
							  mailDetail.mergeCells(3, begin, 3, end);
						  }
					   }					   
					   mailDetailLine++;
					}else if((ls.getMailReceipt() == null || !ls.getMailReceipt())
							&& (ls.getWeight() == null || ls.getWeight() >= Constant.parcelWeight)
							&& (ls.getVendorId() != null && !"送快遞".equals(ls.getVendorId()) && !"".equals(ls.getVendorId()))){
					   parcelCounter++;
					   //有掛號號碼時，且不需回執聯的狀況
					   parcelDetail.addCell(new Label(0, parcelDetailLine, StringUtils.leftPad(parcelCounter + "", 3, '0'), parcelDetail.getCell(0, 5).getCellFormat()));
					   parcelDetail.addCell(new Label(1, parcelDetailLine, ls.getVendorId(), parcelDetail.getCell(1, 5).getCellFormat()));
					   String name = (ls.getName() == null || ls.getName().equals(""))? "國壽服務中心" : ls.getName();
					   String address = ls.getAddress() == null? "無寄送地址" : ls.getAddress();
					   address = name + "\r\n" + address;
					   parcelDetail.addCell(new Label(3, parcelDetailLine, address, parcelDetail.getCell(2, 5).getCellFormat()));
					   
					   List<ApplyData> ads = logisticMap.get(ls.getLogisticId());
					   int begin = parcelDetailLine;
					   int end = parcelDetailLine - 1;
					   if(ads != null){
						  for(ApplyData ad : ads){					
							 end++;
							 parcelDetailLine = end;
							 parcelDetail.setRowView(parcelDetailLine, mailLineView);
						     if(ad.getPolicyNos() != null && ad.getPolicyNos().startsWith(",") && ad.getPolicyNos().endsWith(",")){
					            parcelDetail.addCell(new Label(2, parcelDetailLine, ad.getPolicyNos().substring(1, ad.getPolicyNos().length() - 1), parcelDetail.getCell(2, 5).getCellFormat()));
					            if(end > begin){
					            	parcelDetail.addCell(new Label(0, parcelDetailLine, "", parcelDetail.getCell(0, 5).getCellFormat()));
					            	parcelDetail.addCell(new Label(1, parcelDetailLine, "", parcelDetail.getCell(1, 5).getCellFormat()));
					            	parcelDetail.addCell(new Label(3, parcelDetailLine, "", parcelDetail.getCell(2, 5).getCellFormat()));
					            }
						     }
							 parcelLineBreakCheck++;
						  }
						  //不要讓融合的格子被從中切斷
						  if((parcelLineBreakCheck - 1) >= 23){	
							  int page = (end - begin) / 23;
							  for(int i = 0 ; i <= page ; i++){
								  parcelDetail.addRowPageBreak(begin + i * 23);
							  }
							  parcelLineBreakCheck = (end - begin) % 23;
							  parcelLineBreakCheck++;
						  }
						  if(begin < end){
							  parcelDetail.mergeCells(0, begin, 0, end);
							  parcelDetail.mergeCells(1, begin, 1, end);
							  parcelDetail.mergeCells(3, begin, 3, end);
						  }
					   }					   
					   parcelDetailLine++;
					}else if(ls.getVendorId() != null && !"送快遞".equals(ls.getVendorId())  && !"".equals(ls.getVendorId())){						
					   mrCounter++;
					   //有掛號號碼，且需要回執聯的狀況
					   mailDetail2.addCell(new Label(0, mailDetailLine2, StringUtils.leftPad(mrCounter + "", 3, '0'), mailDetail2.getCell(0, 5).getCellFormat()));
					   mailDetail2.addCell(new Label(1, mailDetailLine2, ls.getVendorId(), mailDetail2.getCell(1, 5).getCellFormat()));
					   String name = (ls.getName() == null || ls.getName().equals(""))? "國壽服務中心" : ls.getName();
					   String address = ls.getAddress() == null? "無寄送地址" : ls.getAddress();
					   address = name + "\r\n" + address;					   
					   mailDetail2.addCell(new Label(3, mailDetailLine2, address, mailDetail2.getCell(2, 5).getCellFormat()));
					   
					   List<ApplyData> ads = logisticMap.get(ls.getLogisticId());
					   int begin = mailDetailLine2;
					   int end = mailDetailLine2 - 1;
					   if(ads != null){
						  for(ApplyData ad : ads){					
							 end++;							 
							 mailDetailLine2 = end;		
							 mailDetail2.setRowView(mailDetailLine2, mailLineView);
						     if(ad.getPolicyNos() != null && ad.getPolicyNos().startsWith(",") && ad.getPolicyNos().endsWith(",")){
					            mailDetail2.addCell(new Label(2, mailDetailLine2, ad.getPolicyNos().substring(1, ad.getPolicyNos().length() - 1), mailDetail2.getCell(2, 5).getCellFormat()));
					            if(end > begin){
					            	mailDetail2.addCell(new Label(0, mailDetailLine2, "", mailDetail2.getCell(0, 5).getCellFormat()));
					            	mailDetail2.addCell(new Label(1, mailDetailLine2, "", mailDetail2.getCell(1, 5).getCellFormat()));
					            	mailDetail2.addCell(new Label(3, mailDetailLine2, "", mailDetail2.getCell(2, 5).getCellFormat()));
					            }
						     }
						     line2BreakCheck++;
						  }

						  //不要讓融合的格子被從中切斷
						  if((line2BreakCheck - 1) >= 23){
							  int page = (end - begin) / 23;
							  for(int i = 0 ; i <= page ; i++){
								  mailDetail2.addRowPageBreak(begin + i * 23);
							  }
							  line2BreakCheck = (end - begin) % 23;							  
							  line2BreakCheck++;
						  }
						  
						  if(begin < end){
							  mailDetail2.mergeCells(0, begin, 0, end);
							  mailDetail2.mergeCells(1, begin, 1, end);
							  mailDetail2.mergeCells(3, begin, 3, end);
						  }
					   }					   					   
					   mailDetailLine2++;
					}
					
					//如果是北二個險，要加入回執聯
	                if(batchOrOnline.equals("B")){
	                   //List<ApplyData> applyDatas = session.getNamedQuery("ApplyData.findByPackId").setString(0, logisticId).list();                   
					   String mrAddress = ls.getAddress();
					   String [] addressSplit = mrAddress.split(" ");
					   String zipCode = "";
					   String vendorId = ls.getVendorId() == null ? "" : ls.getVendorId();
					   if(addressSplit.length > 1){
						   zipCode = addressSplit[0];
						   mrAddress = "";
						   for(int i = 1 ; i < addressSplit.length ; i++){
							   mrAddress += addressSplit[i];
						   }
					   }
					   String zipTmp = "";
					   char[] characters = zipCode.toCharArray();
					   for(int i = 0 ; i < characters.length; i ++){						   
						   zipTmp += characters[i] ;
						   if(i != characters.length - 1)
							   zipTmp += " ";
						   if(i == 2 && characters.length > 3)
							   zipTmp += "  ";						   						   
					   }
					   //zipCode = zipTmp;
					   String policyNos = "";
					   List<String> policyNoList = new ArrayList<String>();
					   String receiptInd = "（業）";
					   if(logisticMap.get(logisticId) != null){
					      for(ApplyData applyData : logisticMap.get(logisticId)){
						      if(applyData.getReceipt() != null && !applyData.getReceipt()){							  
						         for(String policyNo : applyData.getPolicyNoSet()){
						    	     policyNos += "*" + policyNo + "*  ";					
						    	     policyNoList.add(policyNo);
								     break;
						         }
						         if(applyData.getAreaId().toUpperCase().equals("ON88888")){
						        	 receiptInd = "";
						         }else if(applyData.getChannelID() != null && applyData.getChannelID().toUpperCase().equals("A")){
							         receiptInd = "（要）";
						         }else if(applyData.getChannelID() != null && applyData.getChannelID().toUpperCase().equals("B")){
							         receiptInd = "（直）";
						         }else if(applyData.getChannelID() != null && applyData.getChannelID().toUpperCase().equals("G")){
						    	     receiptInd = "（要）";
						         }
						      }
					      }
					   }
					   if(policyNos.startsWith(","))
						   policyNos = policyNos.substring(1);
					   if(policyNos.endsWith(","))
						   policyNos = policyNos.substring(0, policyNos.length() - 1);
					   if(ls.getMailReceipt() != null && ls.getMailReceipt() ){						  
						   
					      //zipCode
					      mailReturn2.mergeCells(7, mailReturnLine + 7, 8, mailReturnLine + 7);					      
					      mailReturn2.addCell(new Label(7, mailReturnLine + 7, zipCode, mailReturn2.getCell(7, 7).getCellFormat()));
					      mailReturn2.addCell(new Label(13, mailReturnLine + 7, zipCode, mailReturn2.getCell(13, 7).getCellFormat()));
					      
					      mailReturn2.mergeCells(4, mailReturnLine + 7, 6, mailReturnLine + 7);
					      mailReturn2.addCell(new Label(4, mailReturnLine + 7, "保單號碼", mailReturn2.getCell(4, 7).getCellFormat()));
					      

					      //BufferedImage input = BarCodeImgUtil.outputtingBarcode(vendorId, 0.5, 0.39, null);
					      //ByteArrayOutputStream baos = new ByteArrayOutputStream();
					      //ImageIO.write(input, "PNG", baos);
					      //mailReturn2.addImage(new WritableImage(12, mailReturnLine + 7, 0.9 ,
					    		    //3.9,baos.toByteArray()));
					      mailReturn2.mergeCells(12, mailReturnLine + 7, 12, mailReturnLine + 12);
					      mailReturn2.addCell(new Label(12, mailReturnLine + 7, "*" + vendorId + "*", mailReturn2.getCell(11, 7).getCellFormat()));
					      
					      mailReturn2.mergeCells(3, mailReturnLine + 15, 3, mailReturnLine + 18);
					      mailReturn2.addCell(new Label(3, mailReturnLine + 15,   vendorId, mailReturn2.getCell(3, 15).getCellFormat()));
					      
					      
					      //input = BarCodeImgUtil.outputtingBarcode(ls.getLogisticId(), 0.5, 0.39, null);
					      //baos = new ByteArrayOutputStream();
					      //ImageIO.write(input, "PNG", baos);
					      
					      //mailReturn2.addImage(new WritableImage(11, mailReturnLine + 7, 0.9 ,
					    		    //9, baos.toByteArray()));
					      mailReturn2.mergeCells(11, mailReturnLine + 7, 11, mailReturnLine + 16);
					      mailReturn2.addCell(new Label(11, mailReturnLine + 7, "*" + ls.getLogisticId() + "*", mailReturn2.getCell(11, 7).getCellFormat()));					      
					      
					      mailReturn2.mergeCells(0, mailReturnLine + 3, 1, mailReturnLine + 3);
					      
					      mailReturn2.addCell(new Label(0, mailReturnLine + 3, receiptInd, mailReturn2.getCell(0, 3).getCellFormat()));
					      mailReturn2.mergeCells(6, mailReturnLine + 3, 8, mailReturnLine + 3);
					      mailReturn2.addCell(new jxl.write.DateTime(6, mailReturnLine + 3, new Date(), mailReturn2.getCell(6, 3).getCellFormat()));
					      
					      mailReturn2.mergeCells(13, mailReturnLine + 8, 13, mailReturnLine + 23);
					      mailReturn2.mergeCells(7, mailReturnLine + 8, 7, mailReturnLine + 19);
					      mailReturn2.addCell(new Label(13, mailReturnLine + 8, mrAddress, mailReturn2.getCell(13, 8).getCellFormat()));
					      mailReturn2.addCell(new Label(7, mailReturnLine + 8, mrAddress, mailReturn2.getCell(7, 8).getCellFormat()));
					      
					      mailReturn2.mergeCells(12, mailReturnLine + 13, 12, mailReturnLine + 23);
					      mailReturn2.mergeCells(8, mailReturnLine + 8, 8, mailReturnLine + 18);
					      String name = ls.getName().replaceAll("　", " ");
					      mailReturn2.addCell(new Label(12, mailReturnLine + 13, ls.getName() == null? "" : name.trim() + "　君收", mailReturn2.getCell(12, 18).getCellFormat()));
					      mailReturn2.addCell(new Label(8, mailReturnLine + 8, ls.getName(), mailReturn2.getCell(8, 8).getCellFormat()));					      
					      mailReturn2.addRowPageBreak(mailReturnLine + 29);
					      
					      int policyNoCounter = 0;
					      for(String policyNo :policyNoList){
					    	 if(policyNo.length() >= 5){
					    	    String displayStr = policyNo.substring(policyNo.length() - 5, policyNo.length());
					    	    for(int i = 0 ; i < policyNo.length() - 5 ; i++){
					    	    	displayStr = "*" + displayStr;
					    	    }
					            //BufferedImage input = BarCodeImgUtil.outputtingBarcode(vendorId, 0.5, 0.39, null);
							    //ByteArrayOutputStream baos = new ByteArrayOutputStream();
					            //ImageIO.write(input, "PNG", baos);
					            if(policyNoCounter == 0){
					               mailReturn2.mergeCells(6, mailReturnLine + 8, 6, mailReturnLine + 10);
					               mailReturn2.addCell(new Label(6, mailReturnLine + 8, policyNo, mailReturn2.getCell(6, 8).getCellFormat()));
					               
					               //mailReturn2.addImage(new WritableImage(6, mailReturnLine + 11, 1 ,
					    		            //6,baos.toByteArray()));
					               mailReturn2.mergeCells(6, mailReturnLine + 11, 6, mailReturnLine + 18);
								   mailReturn2.addCell(new Label(6, mailReturnLine + 11, "*" + policyNo + "*", mailReturn2.getCell(6, 11).getCellFormat()));
					            }else{
					               //mailReturn2.mergeCells(5 - policyNoCounter, mailReturnLine + 10, 5 - policyNoCounter, mailReturnLine + 12);
					               //mailReturn2.addCell(new Label(5 - policyNoCounter, mailReturnLine + 10, policyNo.substring(policyNo.length() - 5), mailReturn2.getCell(3, 15).getCellFormat()));
					               mailReturn2.mergeCells(5 - policyNoCounter, mailReturnLine + 8, 5 - policyNoCounter, mailReturnLine + 10);
					               mailReturn2.addCell(new Label(5 - policyNoCounter, mailReturnLine + 8, policyNo, mailReturn2.getCell(6, 8).getCellFormat()));
					               //mailReturn2.addImage(new WritableImage(5 - policyNoCounter, mailReturnLine + 11, 1 ,
					    		            //6,baos.toByteArray()));
					               mailReturn2.mergeCells(5 - policyNoCounter, mailReturnLine + 11, 5 - policyNoCounter, mailReturnLine + 18);
								   mailReturn2.addCell(new Label(5 - policyNoCounter, mailReturnLine + 11, "*" + policyNo + "*", mailReturn2.getCell(6, 11).getCellFormat()));
					            }
					    	 }
					    	 policyNoCounter++;
					      }
					      
					      if(mailReturnLine != 0){
					         for(int i = 0 ; i < 29; i++){
							    mailReturn2.setRowView(mailReturnLine + i, mailReturn2View[i]);
						     }
					      }
					      mailReturnLine = mailReturnLine + 29;
					   }
	                }
				}				
				mailDetail.addCell(new Label(0, mailDetailLine, "共" + counter + "袋", mailDetail.getCell(0, 5).getCellFormat()));
				parcelDetail.addCell(new Label(0, parcelDetailLine, "共" + parcelCounter + "袋", mailDetail.getCell(0, 5).getCellFormat()));
				mailDetail2.addCell(new Label(0, mailDetailLine2, "共" + mrCounter + "袋", mailDetail.getCell(0, 5).getCellFormat()));
				
				if(batchOrOnline.equals("B") && logisticIds.size() > 0){
				   wwb.copySheet(0, "單掛打包標籤", 5);
				   packLabel = wwb.getSheet(5);				   
				   int pColCounter = 0;
				   int pLineCounter = 0;
				   for(PackStatus ps : packStatuses){
					    boolean mailReceipt = packLogisticMap.get(ps.getPackId()).getMailReceipt() == null? false : packLogisticMap.get(ps.getPackId()).getMailReceipt();
					    
					    //單掛時要多印一個標籤
					    if(!mailReceipt && !ps.isBack() && !"北二行政中心".equals(ps.getSubAreaName())){
						   if(pColCounter > 4){
		                       if((pLineCounter + 1 + 2) % 24 == 0){
		                    	  ws.addRowPageBreak(pLineCounter + 1 + 2);
							   }
							   pLineCounter += 3;
							   pColCounter = 0;							
						   }	
					       String forWrite = ps.getSubAreaName() + "　君收\r\n" + "\r\n" + "   保單本數:" + ps.getBooks();
					       //先寫bar code					
					       packLabel.addCell(new Label(pColCounter, pLineCounter, "*" + ps.getPackId() + "P*", barcodeFormat));
					       packLabel.setRowView(pLineCounter, barcodeView);
					       packLabel.addCell(new Label(pColCounter, pLineCounter + 1, forWrite, strFormat));					
					       packLabel.addCell(new Label(pColCounter, pLineCounter + 2, " ", splitFormat));
					       pColCounter += 2;
					       if((pLineCounter + 3) % 24 == 0){
							   packLabel.setRowView(pLineCounter + 1, lastBodyView);
							   packLabel.setRowView(pLineCounter + 2, lastSplitView);
						   }else{
							   packLabel.setRowView(pLineCounter + 1, bodyView);
							   packLabel.setRowView(pLineCounter + 2, splitView);
						   }
					    }
				   }
				}
				
				if(batchOrOnline.equals("B")){
					mailDetail.addCell(new Label(0, 0, "國泰人壽保險股份有限公司\r\n個險保單正本─單掛號寄送", mailDetail.getCell(0, 0).getCellFormat()));
					if(mrCounter > 0 )
					   mailDetail2.addCell(new Label(0, 0, "國泰人壽保險股份有限公司\r\n個險保單正本─雙掛號寄送", mailDetail.getCell(0, 0).getCellFormat()));
				}
				if(mrCounter == 0){
					wwb.removeSheet(4);
					wwb.removeSheet(3);
				}
				if(parcelCounter == 0)
				    wwb.removeSheet(2);
				if(counter == 0)
					wwb.removeSheet(1);
				if(columnCounter == 0 && lineCounter == 0){
					wwb.removeSheet(0);
				}
				wwb.write();
				return targetfileName;
			}catch(Exception e){
				logger.error("", e);
				e.printStackTrace();
				throw e;
			}finally{
				if (workbook != null)
					workbook.close();
				if (wwb != null)
					try {
						wwb.close();
					} catch (WriteException e) {
						logger.error("", e);
						e.printStackTrace();
					} catch (IOException e) {
						logger.error("", e);
						e.printStackTrace();
					}
				workbook = null;
				wwb = null;	
			}
		}		
		
	}
	
	public static String generateLogisticLabels(List<LogisticStatus> lss, Session session) throws Exception{
		String serverPath = ServletContextGetter.getRealPath("");				
		String labelName = "label3.xls";
		
		ClassPathResource xlsResource = new ClassPathResource(labelName);
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "label3_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;
		Calendar cal = Calendar.getInstance();
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			WritableSheet packDetail = null;
			WritableSheet mailReturn = null;
			
			ws = wwb.getSheet(0); //label
			packDetail = wwb.getSheet(1);
			mailReturn = wwb.getSheet(2);
			
			
			//設定縮放及斷行
			SheetSettings wsSettings = ws.getSettings();
			SheetSettings packDetailSettings = packDetail.getSettings();

			
			wsSettings.setFitToPages(false);
		  	wsSettings.setPageBreakPreviewMode(true);
		  	packDetailSettings.setFitToPages(false);			
		  	packDetailSettings.setPageBreakPreviewMode(true);
		  	
			
			CellFormat barcodeFormat = ws.getCell(0, 0).getCellFormat();			
			CellFormat strFormat = ws.getCell(0, 1).getCellFormat();
			CellFormat splitFormat = ws.getCell(0, 2).getCellFormat();
			CellView barcodeView = ws.getRowView(0);
			CellView bodyView = ws.getRowView(1);
			CellView splitView = ws.getRowView(2);
			CellView lastBodyView = ws.getRowView(22);
			CellView lastSplitView = ws.getRowView(23);
			
			CellFormat textFormat = packDetail.getCell(0, 4).getCellFormat();
			CellFormat pageTitleFormat = packDetail.getCell(2, 0).getCellFormat(); //清單title
			CellFormat detailBarcodeFormat = packDetail.getCell(3, 0).getCellFormat(); //清單barcode
			CellFormat titleFormat = packDetail.getCell(0, 3).getCellFormat(); //清單title
			CellFormat subFormat = packDetail.getCell(0, 1).getCellFormat(); //一般說明			
			
			
			int columnCounter = 0;
			int lineCounter = 0;
			int packDetailLine = 0;
			int mrLine = 0;//回執聯行
			
			for(LogisticStatus ls : lss){
				String logisticId = ls.getLogisticId();
				String center = ls.getCenter();				
				//計算移到第幾個Column
				if(columnCounter > 4){
                    if((lineCounter + 1 + 2) % 24 == 0){
                    	ws.addRowPageBreak(lineCounter + 1 + 2);
					}
					lineCounter += 3;
					columnCounter = 0;
					
				}
				String backStr = "   ";
					
				String forWrite = ls.getName() + "\r\n" + ls.getName() + "\r\n" + ls.getAddress() + "\r\n" + "        保單本數:" + ls.getBooks();
				//先寫bar code
				ws.addCell(new Label(columnCounter, lineCounter, "*" + ls.getLogisticId() + "*", barcodeFormat));
				ws.setRowView(lineCounter, barcodeView);
				ws.addCell(new Label(columnCounter, lineCounter + 1, forWrite, strFormat));
				
				ws.addCell(new Label(columnCounter, lineCounter + 2, " ", splitFormat));
				ws.setRowView(lineCounter + 2, splitView);
				
				columnCounter += 2;
				
				if((lineCounter + 3) % 24 == 0){
					ws.setRowView(lineCounter + 1, lastBodyView);
					ws.setRowView(lineCounter + 2, lastSplitView);
				}else{
					ws.setRowView(lineCounter + 1, bodyView);
				}
				packDetail.addCell(new Label(2, packDetailLine, "裝箱明細", pageTitleFormat));				
				packDetail.addCell(new Label(3, packDetailLine, "*" + ls.getLogisticId() + "*", detailBarcodeFormat));
				packDetailLine++;

				
				packDetail.addCell(new Label(0, packDetailLine, "中心名：", subFormat));
				packDetail.addCell(new Label(1, packDetailLine, ls.getName(), subFormat));
				packDetail.addCell(new Label(2, packDetailLine, "電話：", subFormat));
				packDetail.addCell(new Label(3, packDetailLine, ls.getTel(), subFormat));
				packDetailLine++;
				
				packDetail.addCell(new Label(0, packDetailLine, "地址：", subFormat));
				packDetail.addCell(new Label(1, packDetailLine, ls.getAddress(), subFormat));
				packDetailLine++;
				
				packDetail.addCell(new Label(0, packDetailLine, "打包編號：", titleFormat));
				packDetail.addCell(new Label(1, packDetailLine, "單位名", titleFormat));
				packDetail.addCell(new Label(2, packDetailLine, "單位编號", titleFormat));
				packDetail.addCell(new Label(3, packDetailLine, "單位電話", titleFormat));
				packDetailLine++;
				
				Set<PackStatus> packSet = ls.getPackStatuses();
				int policyColumn = 0;
				int receiptColumn = 0;
				boolean receiptFirstLine = true;
				boolean policyFirstLine = true;
				for(PackStatus ps: packSet){
					packDetail.addCell(new Label(0, packDetailLine, ps.getPackId() ,textFormat));
					packDetail.addCell(new Label(1, packDetailLine, ps.getSubAreaName() ,textFormat));
					packDetail.addCell(new Label(2, packDetailLine, ps.getSubAreaId() ,textFormat));
					packDetail.addCell(new Label(3, packDetailLine, ps.getSubAreaTel() ,textFormat));
					packDetailLine++;
				}				

				packDetailLine++;
				packDetail.addCell(new Label(0, packDetailLine, "共" + ls.getPacks() + "袋", titleFormat));
				packDetailLine++;
				//加入斷行記號，跳下一頁
				packDetail.addRowPageBreak(packDetailLine);
				
			}
			
			wwb.removeSheet(2);			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			throw e;
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
	}
	
	public static String generateSubstractReport(List<ApplyData> queryResult){
		String serverPath = ServletContextGetter.getRealPath("");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		ClassPathResource xlsResource = new ClassPathResource(
				"substractReport.xls");
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "substractReport.xls_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		
		
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;
		Calendar cal = Calendar.getInstance();
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			ws = wwb.getSheet(0);

            int lineCounter = 3; //行數counter
            CellFormat numberFormat = ws.getCell(7, 3).getCellFormat();
            CellFormat textformat = ws.getCell(0, 3).getCellFormat();
            ws.addCell(new Label(1, 1, sdf.format(new Date()), ws.getCell(1, 1).getCellFormat()));            
			for(ApplyData applyData : queryResult){
				String policyNos = "";
				if(applyData.getPolicyNoSet() != null && applyData.getPolicyNoSet().size() > 0){
					int counter = 0;
					for(String policyNo : applyData.getPolicyNoSet()){
						counter ++;
						policyNos += policyNo ;
						if(counter != applyData.getPolicyNoSet().size())
							policyNos += "\r\n";
					}
					
				}
				ws.addCell(new Label(0, lineCounter, policyNos, textformat));
				ws.addCell(new Label(1, lineCounter, applyData.getCenterName(), textformat));
				ws.addCell(new Label(2, lineCounter, applyData.getApplyNo(), textformat));
				ws.addCell(new Label(3, lineCounter, applyData.getUniqueNo() == null ? "" : applyData.getUniqueNo(), textformat));
				String receiptStr = "保單";
				if(applyData.getReceipt() != null && applyData.getReceipt())
					receiptStr = "簽收單";
				ws.addCell(new Label(4, lineCounter, receiptStr, textformat));
				
				ws.addCell(new Label(5, lineCounter, applyData.getNewBatchName(), textformat));
				ws.addCell(new Label(6, lineCounter, applyData.getSubstractModifiderName(), textformat));
				if(applyData.getSubstractModifiderTime() != null)
				   ws.addCell(new Label(7, lineCounter, sdf.format(applyData.getSubstractModifiderTime()), textformat));				
				ws.addCell(new jxl.write.Number(8, lineCounter, applyData.getTotalPage() == null? 0 : applyData.getTotalPage(), numberFormat));
			    lineCounter ++;			    
			}

			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			return null;
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
	}
	
	public static String generateAfpExcel(List<JqgridAfpFile> queryResult, Integer totalFiles,Integer totalBooks, 
	                                      Integer totalReceipts, Integer totalPages , Integer totalSheets, Date queryDate, Date cycleDateEnd, String center) throws Exception{
		String serverPath = ServletContextGetter.getRealPath("");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		ClassPathResource xlsResource = new ClassPathResource(
				"afpReport.xls");
		long time = new Date().getTime();
		String targetfileName = "";
		targetfileName = "afpReport_" + time + ".xls";
		String targetfile = serverPath + "/pdf/" + targetfileName;
		if (new File(targetfile).exists()) {
			new File(targetfile).delete();
		}		
		
		Map<String, String> centerMap = ApplyData.getCenterMap();
		jxl.Workbook workbook = null;
		WritableWorkbook wwb = null;
		Calendar cal = Calendar.getInstance();
		try {
			workbook = jxl.Workbook.getWorkbook(xlsResource.getInputStream());
			wwb = Workbook.createWorkbook(new File(targetfile), workbook); // 創建可寫工作薄
			WritableSheet ws = null;
			ws = wwb.getSheet(0);

            int lineCounter = 5; //行數counter
            CellFormat numberFormat = ws.getCell(9, 5).getCellFormat();
            CellFormat textformat = ws.getCell(0, 5).getCellFormat();
            ws.addCell(new Label(1, 2, (queryDate == null ? "" : sdf.format(queryDate)) + "~" + (cycleDateEnd == null ? "" : sdf.format(cycleDateEnd)), ws.getCell(1, 2).getCellFormat()));
            if(center == null || "".equals(center)) 
            	center = "全部";
            else
            	center = centerMap.get(center);
            
           
            ws.addCell(new Label(4, 2, center, ws.getCell(4, 2).getCellFormat()));
            /*            
            [{name: 'cycleDateStr', index: 'cycleDate', sorttype: "date", width: 100, align: "center"},
             {name: 'newBatchName', index: 'newBatchName', align: "right"},	                        
             {name: 'serialNo', index: 'serialNo', width: 80, align: "right"},
	               {name: 'newBatchNo', index: 'newBatchNo', width: 100 , align: "right"},
	               {name: 'status', index: 'status', width: 150 , align: "center"},	       	            
	               {name: 'updateDateStr', index: 'updateDateStr', width: 100 , align: "center"},
	               {name: 'vipModifierName', index: 'policyNos', width: 100 , align: "center"},
	               {name: 'center', index: 'center', width: 70 , align: "center"},
	               {name: 'pages', index: 'pages', width: 80 , align: "right"},
	               {name: 'volumns', index: 'volumns', width: 100 , align: "right"}],
            */
			for(JqgridAfpFile jqGrid : queryResult){
				ws.addCell(new Label(0, lineCounter, jqGrid.getCycleDateStr(), textformat));
				ws.addCell(new Label(1, lineCounter, jqGrid.getNewBatchName(), textformat));
				if(jqGrid.getSerialNo() != null)
				   ws.addCell(new jxl.write.Number(2, lineCounter, jqGrid.getSerialNo(), numberFormat));
				if(jqGrid.getNewBatchNo() != null)
				   ws.addCell(new jxl.write.Number(3, lineCounter, jqGrid.getNewBatchNo(), numberFormat));
				ws.addCell(new Label(4, lineCounter, jqGrid.getStatus(), textformat));
				ws.addCell(new Label(5, lineCounter, jqGrid.getUpdateDateStr(), textformat));
				ws.addCell(new Label(6, lineCounter, jqGrid.getVipModifierName(), textformat));
				String centerStr = "";
				if(jqGrid.getCenter() == null || "".equals(jqGrid.getCenter()))
	            	centerStr = "";
	            else
	            	centerStr = centerMap.get(jqGrid.getCenter());
				ws.addCell(new Label(7, lineCounter, centerStr, textformat));
				ws.addCell(new jxl.write.Number(8, lineCounter, jqGrid.getPages() == null ? 0 : jqGrid.getPages(), numberFormat));
				ws.addCell(new jxl.write.Number(9, lineCounter, jqGrid.getVolumns(), numberFormat));
				ws.addCell(new Label(10, lineCounter, jqGrid.getInsertDateStr(), textformat));
				ws.addCell(new Label(11, lineCounter, jqGrid.getPresTimeStr(), textformat));
				ws.addCell(new Label(12, lineCounter, jqGrid.getBeginTransferTimeStr(), textformat));
				ws.addCell(new Label(13, lineCounter, jqGrid.getEndTransferTimeStr(), textformat));
				ws.addCell(new Label(14, lineCounter, jqGrid.getPrintTimeStr(), textformat));
				ws.addCell(new Label(15, lineCounter, jqGrid.getBindTimeStr(), textformat));
				ws.addCell(new Label(16, lineCounter, jqGrid.getVerifyTimeStr(), textformat));
				ws.addCell(new Label(17, lineCounter, jqGrid.getPackTimeStr(), textformat));
				ws.addCell(new Label(18, lineCounter, jqGrid.getDeliverTimeStr(), textformat));
			    lineCounter ++;			    
			}
			ws.addCell(new Label(0, (lineCounter), "列印檔數：", textformat));
			ws.addCell(new jxl.write.Number(1, (lineCounter), totalFiles, numberFormat));
			ws.addCell(new Label(0, (++lineCounter), "保單數：", textformat));
			ws.addCell(new jxl.write.Number(1, (lineCounter), totalBooks, numberFormat));
			ws.addCell(new Label(0, (++lineCounter), "簽收回條數：", textformat));
			ws.addCell(new jxl.write.Number(1, (lineCounter), totalReceipts, numberFormat));
			ws.addCell(new Label(0, (++lineCounter), "總頁數：", textformat));
			ws.addCell(new jxl.write.Number(1, (lineCounter), totalPages, numberFormat));
			ws.addCell(new Label(0, (++lineCounter), "總張數：", textformat));
			ws.addCell(new jxl.write.Number(1, (lineCounter), totalSheets, numberFormat));
			ws.addCell(new Label(0, (++lineCounter), "總本數(保單加簽收單)：", textformat));
			ws.addCell(new jxl.write.Number(1, (lineCounter), totalBooks + totalReceipts, numberFormat));
			
			wwb.write();
			return targetfileName;
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			throw e;
		}finally{
			if (workbook != null)
				workbook.close();
			if (wwb != null)
				try {
					wwb.close();
				} catch (WriteException e) {
					logger.error("", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			workbook = null;
			wwb = null;	
		}
		
	}
	
}
