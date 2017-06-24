package com.fxdms.cathy.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import org.apache.log4j.Logger;
import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;

public class InputdateParser {
	private static File prevFile = null;
	private static long lastModified = 0;
	private static long lastFileLength = 0;
	private static Date inputDate; 	
	private static boolean running = false;
	private static File okFolder = new File(Properties.getLocalOKPath());			
	
	static Logger logger = Logger.getLogger(InputdateParser.class);
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
			File inputdateFile = new File(okFolder, "INPUTDATE.TXT");
			//如果檔案不同時才進入
			if(inputdateFile != null && inputdateFile.exists() &&  (prevFile == null || lastModified != inputdateFile.lastModified() || lastFileLength != inputdateFile.length() || forceRead)){
				prevFile = inputdateFile;
				lastModified = prevFile.lastModified();
				lastFileLength = prevFile.length();
				
				fir = new FileReader(inputdateFile);
				br = new BufferedReader(fir);
				String line = null;
				while((line = br.readLine()) != null && line.length() == 8){
					inputDate = Constant.yyyyMMdd.parse(line);					
				}
				if(forceRead)
					forceRead = false;
			}else if(inputDate == null){
				inputDate = new Date();
			}
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			inputDate = new Date();
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
		logger.info("input date is " + Constant.yyyy_MM_dd.format(inputDate));

		running = false;
	}
	
	public static void forceReadFile(){
	   forceRead = true;	
	}
	public static Date getInputDate(){
		if(inputDate == null || forceRead){
			prevFile = null;
			running = false;
			startToRun();
		}
		return inputDate;
	}


}
