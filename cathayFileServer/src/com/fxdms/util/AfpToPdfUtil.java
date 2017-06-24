package com.fxdms.util;


import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.fxdms.cathy.bo.Properties;


public class AfpToPdfUtil {
	private static String streamEDPDir =  "D:/streamEDP/StreamEDP/"; //要改 
	private static String afpToPdfDir  = Properties.getFsafpToPdfPath();
	private static long waitConvertTime = 15 * 1000; //轉pdf等待時間，設為15秒
	//private static String afpToPdfDir = "D:/AFPtoPDF/";
	private static Logger logger = Logger.getLogger(AfpToPdfUtil.class);
	public static void main(String[] args) {
		try {
			convertToPdf("D:/scbEdd/backup/20120720/ESC0706.AFP",  "D:/scbEdd/backup/20120720/ESC0706.pdf", false);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("Error creating process.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {

		}
	}

	public static boolean convertToPdf(String afpFile,
			String detFolder, boolean isStreamEdp) throws IOException,
			InterruptedException {
		if (isStreamEdp)
			return streamEdpToPdf(afpFile, detFolder);
		else
			return afpToPdf(afpFile, detFolder);

	}

	public static boolean streamEdpToPdf(String afpFile,
			String detFile) throws IOException, InterruptedException {		
		return streamEdpToPdf(streamEDPDir, afpFile, detFile);
	}
	
	public static boolean afpToPdf( String afpFile, String detFile)
	                 throws IOException, InterruptedException {
		return afpToPdf( afpToPdfDir, afpFile, detFile);
	}

	
	public static boolean streamEdpToPdf(String streamEDPDir, String afpFile,
			String detFile) throws IOException, InterruptedException {
		/*
		String execCommand = streamEDPDir + "bin/StreamEDPProject.bat" + " "
				+ afpFile + " " + detFile;
		System.out.println(execCommand);
		Process p = Runtime.getRuntime().exec(execCommand);
		p.waitFor();
		p.destroy();
		*/
		StreamEDPRun streamEdp = new StreamEDPRun();
		streamEdp.setStreamEDPDir(streamEDPDir);
		streamEdp.setAfpFile(afpFile);
		streamEdp.setDetFile(detFile);
		//另起thread執行
		new Thread(streamEdp).start();
		long beginProcess = new Date().getTime();
		while(!streamEdp.isFinishProcess()){
			long nowTime = new Date().getTime();
			logger.info("waiting streamEdp convert " + afpFile + "  to " + detFile + ". Waiting time " + (nowTime - beginProcess) / 1000 + " seconds.");
			//如果超過等待時間，就強迫跳出
			if((nowTime - beginProcess) > waitConvertTime){
				logger.warn(streamEDPDir + "bin/StreamEDPProject.bat" + " "
						+ afpFile + " " + detFile + " wait finish over " + (waitConvertTime / 1000) + " seconds.");
				logger.warn("forced to finish streamEDP convertion job");
                streamEdp.getProcess().destroy();				
				return false;				
			} 
			Thread.sleep(5000);
		}
		return true;
	}

	
	public static boolean afpToPdf(String afpToPdfDir, String afpFile, String detFolder)
			throws IOException, InterruptedException {
		/*
		String execCommand = afpToPdfDir + "afp2pdf.exe -o " + detFile + " "
				+ afpFile;
		System.out.println(execCommand);
		Runtime runTime = Runtime.getRuntime();
		
		Process p = runTime.exec(execCommand);
		p.waitFor();
		p.destroy();
        */
		Afp2PdfRun afp2PdfRun = new Afp2PdfRun();
		afp2PdfRun.setAfpToPdfDir(afpToPdfDir);
		afp2PdfRun.setAfpFile(afpFile);		
		afp2PdfRun.setDetFile(detFolder);
		//另起thread執行
		new Thread(afp2PdfRun).start();
		logger.info("wait to finish");
		long beginProcess = new Date().getTime();
		while(!afp2PdfRun.isFinishProcess()){
			
			long nowTime = new Date().getTime();
			logger.info("waiting Afp2Pdf convert " + afpFile + "  to " + detFolder + ". Waiting time " + (nowTime - beginProcess) / 1000 + " seconds.");
			//如果超過等待時間，就強迫跳出
			if((nowTime - beginProcess) >  waitConvertTime){
				logger.warn("\"" + afpToPdfDir + "AFP2PDFC\" " +  "\"/I:" + afpFile + "\" /A:baB+Um1Cda/YE7q2eoPqhQ \"/O:" + detFolder + " "
						+ afpFile + " wait finish over " + (waitConvertTime / 1000) + " seconds.");
				logger.warn("forced to finish afp2pdf convertion job");
				afp2PdfRun.getProcess().destroy();				
				return false;				
			} 
			Thread.sleep(5000);
		}
		return true;
	}

}
