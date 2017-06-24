package com.fxdms.cathy.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import org.apache.log4j.Logger;
import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;

public class GroupInputdateParser {
	private static boolean running = false;
	private static File okFolder = new File(Properties.getGroupInFolder(), "OK");
		
	private static File gpPrevFile = null;
	private static long gpLastModified = 0;
	private static long gpLastFileLength = 0;
	private static File gpOkFolder = new File(Properties.getGroupInFolder(), "OK");
	private static Date gpInputDate; 	
	static Logger logger = Logger.getLogger(GroupInputdateParser.class);
	private static boolean forceRead = false;
	public static void startToRun() {
		if(running)
			return;
		
		running = true;
		logger.info("input date parser runnnig");
		FileReader fir = null;
		BufferedReader br = null;
		try{
			if(!okFolder.exists())
				okFolder.mkdirs();
			if(!gpOkFolder.exists())
				gpOkFolder.mkdirs();
			File gpInputdateFile = new File(gpOkFolder, "INPUTDATE.TXT");
			//如果檔案不同時才進入
			if(gpInputdateFile.exists() &&  (gpPrevFile == null || gpLastModified != gpInputdateFile.lastModified() || gpLastFileLength != gpInputdateFile.length() || forceRead)){
				gpPrevFile = gpInputdateFile;
				gpLastModified = gpPrevFile.lastModified();
				gpLastFileLength = gpPrevFile.length();
				
				fir = new FileReader(gpInputdateFile);
				br = new BufferedReader(fir);
				String line = null;
				while((line = br.readLine()) != null && line.length() == 8){
					gpInputDate = Constant.yyyyMMdd.parse(line);					
				}
				if(forceRead)
				    forceRead = false;
			}else if(gpInputDate == null){
				gpInputDate = new Date();
			}
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			gpInputDate = new Date();
		}finally{
			try{
			   if(br != null)
			       br.close();
			   if(fir != null)
			       fir.close();
			}catch(Exception e){
				logger.error("", e);
			}
			br = null;
			fir = null;
			running = false;
		}
		logger.info("group input date is " + Constant.yyyy_MM_dd.format(gpInputDate));
		running = false;
	}
	
	public static void forceReadFile(){
	   forceRead = true;	
	}
	public static Date getGpInputDate(){
		if(gpInputDate == null || forceRead){
			gpPrevFile = null;
			running = false;
			startToRun();
		}
		return gpInputDate;
	}

}
