package com.fxdms.cathy.task;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;

public class SchedulerService {

	/**
	 * Log4j instance.
	 */
	private final static Logger logger = Logger.getLogger(SchedulerService.class);
	private static Constant constant;

	
	/* Constants */
	private static SchedulerService instance = new SchedulerService();


	private SchedulerService() {

	} 

	public static SchedulerService getInstance() {
		if(instance == null)
			instance = new SchedulerService();
		return instance;
	}
	
	public static void afpListener(){
		if(Properties.isInitialnized())
		   AfpListener.startToRun();
	}
	public static void returnAfpListener(){
		if(Properties.isInitialnized())
		   ReturnAfpListener.startToRun();
	}
	
	public static void addressProcesser(){
		if(Properties.isInitialnized())
		   AddressProcesser.startToRun();
	}
	
	public static void fileDispatcher() throws BeansException, RemoteException{
		if(Properties.isInitialnized())
		   FileDispatcher.startToRun();
	}
	
	public static void notPrintReceipt() throws BeansException, RemoteException{
		if(Properties.isInitialnized())
			NotPrintMerger.startToRun();
	}
	
	public static void gpFileDispatcher() throws BeansException, RemoteException{
		if(Properties.isInitialnized())
		   GpFileDispatcher.startToRun();
	}
	
	public static void gpTestDispatcher() throws BeansException, RemoteException{
		if(Properties.isInitialnized())
		   GpTestPolicy.startToRun();
	}
	
	public static void policyMerger() throws BeansException, RemoteException{
		if(Properties.isInitialnized() && !PolicyMerger.isRunning()){
		   PolicyMerger.setCheckedOkPath(Properties.getCheckedOkPath());
		   logger.info("checking: " + Properties.getCheckedOkPath());
		   PolicyMerger.setReceipt(false);
		   PolicyMerger.startToRun();
		}
	}
	public static void receiptMerger() throws BeansException, RemoteException{
		if(Properties.isInitialnized() && !PolicyMerger.isRunning()){
		   PolicyMerger.setCheckedOkPath(Properties.getReceiptOkPath());
		   logger.info("checking: " + Properties.getReceiptOkPath());
		   PolicyMerger.setReceipt(true);
		   PolicyMerger.startToRun();
		}
	}
	
	public static void gpPolicyMerger() throws BeansException, RemoteException{
		if(Properties.isInitialnized() && !GpPolicyMerger.isRunning()){
		   GpPolicyMerger.setCheckedOkPath(Properties.getCheckedOkPath());
		   logger.info("group checking: " + Properties.getCheckedOkPath());
		   GpPolicyMerger.setReceipt(false);
		   GpPolicyMerger.startToRun();
		}
	}
	public static void gpPdfSorter() throws BeansException, RemoteException{
		if(Properties.isInitialnized()){
           GpPdfSorter.startToRun();
		}
	}
	
	public static void gpReceiptMerger() throws BeansException, RemoteException{
		if(Properties.isInitialnized() && !GpPolicyMerger.isRunning()){
		   GpPolicyMerger.setCheckedOkPath(Properties.getReceiptOkPath());
		   logger.info("checking: " + Properties.getReceiptOkPath());
		   GpPolicyMerger.setReceipt(true);
		   GpPolicyMerger.startToRun();
		}
	}
	
	
	public static void returnDispatcher() throws BeansException, RemoteException{
		if(Properties.isInitialnized() && !ReturnDispatcher.isRunning()){		   
			ReturnDispatcher.startToRun();
		}
	}
	public static void sftpListener() throws BeansException, RemoteException{		
		if(Properties.isInitialnized())
		   SftpListener.startToRun();
		
	}
	
	public static void zipListener(){
		if(Properties.isInitialnized())
		   ZipListener.startToRun();
	} 
	
			
	public static void deleteListener(){
		if(Properties.isInitialnized())
		   DeleteListener.startToRun();
		
	}
	

	public static void imgUpdater() throws BeansException, RemoteException{
		if(Properties.isInitialnized())
		   ImgUpdater.startToRun();		
	}
	
	public static void gpImgUpdater() throws BeansException, RemoteException{
		if(Properties.isInitialnized())
		   GpImgUpdater.startToRun();		
	}
	
	public Constant getConstant() {
		return constant;
	}

	public void setConstant(Constant constant) {
		SchedulerService.constant = constant;
	}
	
	public static void feedbackTimer() throws BeansException, RemoteException{
		if(Properties.isInitialnized())
		   FeedbackTimer.startToRun();
	}
	public static void pdfSplitter() throws BeansException, RemoteException{
		if(Properties.isInitialnized())
		   PdfSplitListener.startToRun();
	}
	public static void inputDateParser(){
		if(Properties.isInitialnized())
		   InputdateParser.startToRun();
	}
	public static void gpInputDateParser(){
		if(Properties.isInitialnized())
		   GroupInputdateParser.startToRun();
	}
	
	public static void updateAreas(){
		if(Properties.isInitialnized())
		   AreaUpdater.startToRun();
	}
	
	public static void policyBookCounter(){
		
		try {
			if(Properties.isInitialnized())
			   PolicyBookCounter.startToRun();
		} catch (BeansException e) {			
			e.printStackTrace();
		} catch (IOException e) {		    
			e.printStackTrace();
		}
	}
	public static void pdfToTifConverter(){
		PdfToTifConverter.startToRun();		
	}

}
