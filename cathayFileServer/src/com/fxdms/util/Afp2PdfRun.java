package com.fxdms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.fxdms.cathy.bo.Properties;

public class Afp2PdfRun implements Runnable{

	private static Logger logger = Logger.getLogger(Afp2PdfRun.class); 
	private String afpToPdfDir = Properties.getFsafpToPdfPath(); //結尾必須是/
	private String afpFile;
	private String detFolder;
	private boolean finishProcess;	
	private Process process = null;

	public void run() {
		finishProcess = false;
		//"C:\Users\Administrator\AppData\Local\Apps\2.0\969EHPQE.ZYA\G0HBTWQ2.JW6\afp2..tion_0000000000000000_0002.0000_cc8f835bc34fbf98\AFP2PDFC" "/I:D:\kotai\preview\afpfiles\CA03B201403300001.AFP" /A:baDDVG1nC+PYEyW0emQwwQ "/O:D:\kotai\preview\afpfiles" "/N:test.pdf"
		//"C:\Users\Administrator\AppData\Local\Apps\2.0\969EHPQE.ZYA\G0HBTWQ2.JW6\afp2..tion_0000000000000000_0002.0000_cc8f835bc34fbf98\AFP2PDFC" "/I:                   D:\tmp\CA03B201403300001.pdf" /A:baDDVG1nC+PYEyW0emQwwQ "/O:D:\tmp
		String execCommand = "\"" + afpToPdfDir + "AFP2PDFC.exe\" " +  "\"/I:" + afpFile + "\" /A:baB+Um1Cda/YE7q2eoPqhQ \"/O:" + detFolder + "\"";
		
		  
        System.out.println(execCommand);
        logger.info(execCommand);
        try {
			process = Runtime.getRuntime().exec(execCommand);
			/*
			//normal messge reader
			BufferedReader stdInput = new BufferedReader(new 
		             InputStreamReader(process.getInputStream()));
			//error messge reader
		    BufferedReader stdError = new BufferedReader(new 
		             InputStreamReader(process.getErrorStream()));
		    String s;
		    
		    
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
	        */
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
	public void setDetFile(String detFolder) {
		if(detFolder != null && detFolder.length() > 0 && (detFolder.endsWith("/") || detFolder.endsWith("\\"))){
			detFolder = detFolder.substring(0, detFolder.length() - 1);
		}
		this.detFolder = detFolder;
	}
	public boolean isFinishProcess() {
		return finishProcess;
	}
	
	public static void main(String args[]){
		
		File afptopdfDir = new File("C:\\Users\\Administrator\\AppData\\Local\\Apps\\2.0\\969EHPQE.ZYA\\G0HBTWQ2.JW6\\afp2..tion_0000000000000000_0002.0000_cc8f835bc34fbf98\\");
		File afpFile = new File("D:\\tmp\\CA03B201403300001.pdf");
		File detFolder = new File("D:\\tmp\\");
		
		String execCommand = "\"" + afptopdfDir + "AFP2PDFC\" " +  "\"/I:" + afpFile + "\" /A:baDDVG1nC+PYEyW0emQwwQ \"/O:" + detFolder;
		System.out.println(execCommand);
	            
		
	}
}
