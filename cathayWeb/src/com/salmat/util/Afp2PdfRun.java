package com.salmat.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class Afp2PdfRun implements Runnable{

	private static Logger logger = Logger.getLogger(Afp2PdfRun.class); 
	private String afpToPdfDir = null;
	private String afpFile;
	private String detFile;
	private boolean finishProcess;	
	private Process process = null;

	public void run() {
		finishProcess = false;
		String execCommand = afpToPdfDir + "afp2pdf.exe -o " + detFile + " "
		            + afpFile;
        System.out.println(execCommand);
        logger.info(execCommand);
        try {
			process = Runtime.getRuntime().exec(execCommand);
			//normal messge reader
			BufferedReader stdInput = new BufferedReader(new 
		             InputStreamReader(process.getInputStream()));
			//error messge reader
		    BufferedReader stdError = new BufferedReader(new 
		             InputStreamReader(process.getErrorStream()));
		    String s;
		    
		    logger.info("run time error message:");
		    System.out.println("run time error message");
	        // 讀取error message	        
	        while ((s = stdError.readLine()) != null) {
	        	logger.error("afp2pdf converting process error message:" + s );
	            System.out.println(s);
	        }
		    
		    // 讀取正常 message
	        logger.info("run time message:");
	        System.out.println("run time message");
		    while ((s = stdInput.readLine()) != null) {
		    	logger.info("afp2pdf converting process message:" +s);
	            System.out.println(s);
	        }
	        logger.info("wait for exit");
	        System.out.println("wait for exit");
			process.waitFor();
	        process.destroy();
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("", e);
			e.printStackTrace();
		}
		finishProcess = true;
	}
	public Process getProcess() {
		return process;
	}
	public void setAfpToPdfDir(String afpToPdfDir) {
		this.afpToPdfDir = afpToPdfDir;
	}
	public void setAfpFile(String afpFile) {
		this.afpFile = afpFile;
	}
	public void setDetFile(String detFile) {
		this.detFile = detFile;
	}
	public boolean isFinishProcess() {
		return finishProcess;
	}
}
