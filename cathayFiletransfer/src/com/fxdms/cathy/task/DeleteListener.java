package com.fxdms.cathy.task;


import java.util.Calendar;

import org.apache.log4j.Logger;

import com.fxdms.util.DeleteDBAndFile;

public class DeleteListener {

	static Logger logger = Logger.getLogger(DeleteListener.class);
    
	
	public static synchronized void startToRun() {
		Calendar cal = Calendar.getInstance();
		logger.info("now hour:" + Calendar.HOUR + ". ready to delete file and DB");
		
		DeleteDBAndFile.deleteFile();
		//DeleteDBAndFile.deleteDB(); 
		
	}


}
