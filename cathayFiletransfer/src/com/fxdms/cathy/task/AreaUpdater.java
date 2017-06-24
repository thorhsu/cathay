package com.fxdms.cathy.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FilesUtils;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.ErrorReport;

public class AreaUpdater {

	static Logger logger = Logger.getLogger(AreaUpdater.class);
	private static File addressFile = new File(new File(Properties.getGroupInFolder(), "OK"), "unitAddress.txt");
    static boolean running = false;
	public synchronized static void startToRun() {
		if(running)
			return;
		running = true;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		String line = null;
		
		try {	
			if(addressFile.exists()){
				ArrayList<Area> areas = new ArrayList<Area>();
				String cycleDateStr = Constant.yyyy_MM_dd.format(GroupInputdateParser.getGpInputDate());
				fis = new FileInputStream(addressFile);
				isr = new InputStreamReader(fis, "ms950");
				br = new BufferedReader(isr);
				int i = 0;
				while ((line = br.readLine()) != null) {	
					i++;
					if(i == 1) //第一行不讀
						continue;
					if(line.trim().equals(""))
						continue;
					/*
					 if(!"單位代號".equals(areaIdT) || !"單位名稱(簡稱)".equals(areaNameT) 
			     	    		|| !"郵遞區號".equals(zipCodeT) || !"單位地址".equals(addressT)
			     	    		|| !"單位電話1".equals(telT) || !"所屬服務中心代號".equals(serviceCenterT)
			     	    		|| !"所屬服務中心名稱(簡稱)".equals(serviceCenterNmT) || !"獨立課區判別碼".equals(independentT)){
				  */
					String [] lineSplit = line.split(",");
					if(lineSplit.length >= 4){
						String areaId = lineSplit[0].trim();
						if(areaId.length() != 7){
							ErrorReport eReport = new ErrorReport();
				            eReport.setErrHappenTime(new Date());
				            eReport.setErrorType("無法更新單位");
				            eReport.setOldBatchName(null);
				            eReport.setReported(false);		
				            eReport.setException(true);
				            eReport.setMessageBody("第" + i + "行:" + line + "有誤，單位代碼不為七位數");
				            eReport.setTitle("服務中心更新");
							  ((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
						}
						String areaNm = lineSplit[1].trim();
						String zipCode = lineSplit[2].trim();
						String address = lineSplit[3].trim();
						String tel = null;
						String serviceCenter = null;
						String serviceCenterNm = null;
						boolean independent = false;
						if(lineSplit.length >= 5){
							tel = lineSplit[4].trim();
						}
						if(lineSplit.length >= 6){
							serviceCenter = lineSplit[5].trim();
						}
						if(lineSplit.length >= 7){
							serviceCenterNm = lineSplit[6].trim();
						}
						if(lineSplit.length >= 8){
							independent = lineSplit[7].trim().equals("1");
						}
						Area area = new Area();
						area.setAddress(null);
						area.setAreaId(areaId);
						area.setAreaName(areaNm);
						area.setIndependent(independent);
						area.setServiceCenter(serviceCenter);
						area.setServiceCenterNm(serviceCenterNm);
						area.setTel(tel);
						area.setZipCode(null);
						//更新的地址如果有serviceCenter時，代表的其實是serviceCenter的地址
						
						area.setAddress(address);
						area.setTel(tel);
						area.setZipCode(zipCode);
						
						areas.add(area);
						
					}else{
						ErrorReport eReport = new ErrorReport();
			            eReport.setErrHappenTime(new Date());			            
			            eReport.setErrorType("UPDATE AREAS");
			            eReport.setOldBatchName(null);
			            eReport.setReported(false);		
			            eReport.setException(true);
			            eReport.setMessageBody("第" + i + "行:" + line + "無法解析");
			            eReport.setTitle("無法更新單位");
						  ((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
					}					
				}				
				String message = ((VoService) Constant.getContext().getBean("voServiceProxy")).updateArea(areas); 
				if(message == null){
					try {
						if (br != null)
							br.close();
						if (isr != null)
							isr.close();
						if (fis != null)
							fis.close();
					} catch (Exception e) {
						logger.error("", e);
					}finally{
					   br = null;
					   isr = null;
					   fis = null;
					}
				    FilesUtils.moveFileToDirectory(addressFile, new File(Properties.getGpBackupFolder(), "addr/" + cycleDateStr), true);
				    ErrorReport eReport = new ErrorReport();
			        eReport.setErrHappenTime(new Date());
			        eReport.setErrorType("UPDATE AREAS");
			        eReport.setOldBatchName(null);
			        eReport.setReported(true);		
			        eReport.setException(null);
			        eReport.setMessageBody("更新單位成功，共:" + areas.size() + "筆");
			        eReport.setTitle("更新單位");
					((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
			    }else{
			       ErrorReport eReport = new ErrorReport();
		           eReport.setErrHappenTime(new Date());
		           eReport.setErrorType("UPDATE AREAS");
		           eReport.setOldBatchName(null);
		           eReport.setReported(false);		
		           eReport.setException(true);
		           eReport.setMessageBody(message);
		           eReport.setTitle("無法更新單位");
				   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
			    }
			}
		} catch (Exception e) {			
			logger.error("", e);
			ErrorReport eReport = new ErrorReport();
            eReport.setErrHappenTime(new Date());
            eReport.setErrorType("UPDATE AREAS");
            eReport.setOldBatchName(null);
            eReport.setReported(false);		
            eReport.setException(true);
            eReport.setMessageBody("更新服務中心失敗" + e.getMessage() + "");
            eReport.setTitle("例外發生");
			  ((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
		}finally{
			running = false;
			logger.info("area updater stoped");
			try {
				if (br != null)
					br.close();
				if (isr != null)
					isr.close();
				if (fis != null)
					fis.close();
			} catch (Exception e) {
				logger.error("", e);
			}
			br = null;
			isr = null;
			fis = null;
		}
	}

}
