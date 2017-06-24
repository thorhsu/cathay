package com.fxdms.cathy.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FileFilterImpl;
import com.fxdms.util.FilesUtils;
import com.fxdms.util.SftpClientUtil;
import com.fxdms.util.ZipUtils;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.Properties;

public class SftpListener {

	static Logger logger = Logger.getLogger(SftpListener.class);
	private static boolean running = false;

	public synchronized static void startToRun() throws BeansException, RemoteException {
		
		if (running)
			return;
		
		try {
			
			Properties properties = ((VoService) Constant.getContext().getBean(
					"voServiceProxy")).getProperties();			
			BeanUtils.copyProperties(properties, new com.fxdms.cathy.bo.Properties());	
			File returnFolder = new File(properties.getLocalReturnPath()); // 迴歸檢查Folder件
			File okFolder = new File(properties.getLocalOKPath());
			if (!okFolder.exists())
				okFolder.mkdirs();
			File d2oOK = new File(okFolder, "D2O.ok");
			File d2oing = new File(okFolder, "D2O.ing");
			logger.info("sftp listener start to work");
			running = true;
			//如果是重置的話，就全部刪掉後把backup裡最新的check_data裡的檔案丟過去，重新產生比對檔
			if (properties.isResetReturnTest() && !ReturnDispatcher.isRunning()) {
				File returnCheck = new File(properties.getLocalReturnForCheck()); //比對pdf 的folder D:\CheckOUT\PDF_ERROR
				File localReturnPdf = new File(properties.getLocalReturnPdf());   //回歸測試用來比較的測試 pdf D:\CheckOUT\PDF\original
				if(!localReturnPdf.exists())
					localReturnPdf.mkdirs();
				if (!returnCheck.exists())
					returnCheck.mkdirs();
				
				File[] files = returnCheck.listFiles(FileFilterImpl.getCommonPdfFileFilter());
				for(File file : files){
					if(file.isFile() && file.getName().toLowerCase().endsWith(".pdf"))
					   try{
						   FileUtils.forceDelete(file);
					   }catch(Exception e){
						   logger.error("", e);
					   }
				}
				files = localReturnPdf.listFiles();
				for(File file : files){
					try{
						FileUtils.forceDelete(file);
					}catch(Exception e){
						logger.error("", e);
					}
				}
				File myReturnFolder = new File(new File(properties.getCheckedOkPath()).getParentFile(), "returnTest"); //用來儲存原始檔的目錄，包括分割後的原始文字檔及影像檔
				files = myReturnFolder.listFiles();
				for(File file : files){
					try{
						if(file.isFile()){
						   FileUtils.forceDelete(file);
						}else{
						   if(!file.getName().equals("overlay") 
								   && !file.getName().equals("pgm")){
							   for(File insideFile : file.listFiles()){
								   FileUtils.forceDelete(insideFile);
							   }
							   
						   }
						}
							
					}catch(Exception e){
						logger.error("", e);
					}
				}
				//batch.lock都刪除
				File batchLock = new File(okFolder, "batch.lock");
				File keepLock = new File(okFolder, "batch.keeplock");
				if (batchLock.exists())
					FileUtils.forceDelete(batchLock);
				if (keepLock.exists())
					FileUtils.forceDelete(keepLock);
				properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
				properties.setResetReturnTest(false);
				((VoService) Constant.getContext().getBean(
						"voServiceProxy")).update(properties);
				properties = ((VoService) Constant.getContext().getBean(
						"voServiceProxy")).getProperties();
				
				
				File returnBackupFolder = new File(properties.getBackupFolder(),  "CHECK_DATA");
				File[] returnDatas = returnBackupFolder.listFiles(FileFilterImpl.getDirectoryFilter());
				
				
				
				File [] returnCheckFiles = returnFolder.listFiles(FileFilterImpl.getFileFilter());
				if(returnCheckFiles != null && returnCheckFiles.length > 0){
					for(File file : returnCheckFiles)
						try{
						   FileUtils.forceDelete(file);
						}catch(Exception e){
							e.printStackTrace();
							logger.error("", e);
						}
				}
				if(returnDatas != null && returnDatas.length > 0){
					//如果backup裡有檔案的話，把它copy過來重新產生迴歸測試比對檔
					File folder = returnDatas[returnDatas.length - 1]; //取最新的
					File[] returnFiles = folder.listFiles(FileFilterImpl.getFileFilter());
					
					for(File returnFile : returnFiles){
						FileUtils.copyFileToDirectory(returnFile, returnFolder);
					}
					File checkOk = new File(okFolder, "CHECK_DATA.OK");
					FileWriter fw = new FileWriter(checkOk);
					fw.write("");
					fw.flush();
					fw.close();
					fw = null;
				}
			}
			
			// 如果returnlock，就解開			
			if (properties.isReturnUnlock() || (d2oOK.exists() && !ReturnDispatcher.isRunning())) {
				
				try{
				   File pdfTmp = new File(new File(properties.getCheckedOkPath()).getParentFile(), "pdfTmp");
				   if(pdfTmp.exists()){
					   FilesUtils.forceDelete(pdfTmp);
				   }
				   pdfTmp.mkdirs();
				   File[] returnLocalFiles = ReturnDispatcher.localReturnPdfFolder.listFiles(FileFilterImpl.getFileFilter());
				   if(d2oOK.exists())
				      FileUtils.forceDelete(d2oOK);
				   FileWriter fw = new FileWriter(d2oing);
				   fw.write("d2oing");
				   fw.flush();
				   fw.close();
				   File returnCheck = new File(properties.getLocalReturnForCheck());
				   if (!returnCheck.exists())
					   returnCheck.mkdirs();
				   File[] files = returnCheck.listFiles(FileFilterImpl.getCommonPdfFileFilter());
				   if (files != null && files.length > 0) {
					   Date cycleDate = InputdateParser.getInputDate();
					   File returnBackFolder = new File(returnCheck, Constant.yyyy_MM_dd.format(cycleDate) );
					   if(!returnBackFolder.exists())
						   returnBackFolder.mkdirs();
					   // old.pdf刪除
					   // 4check.pdf刪除
 					   // 把新的pdf搬過去
					   for (File file : files) {
						   if (file.getName().toLowerCase().endsWith(".old.pdf")){
							  FilesUtils.copyFileToDirectory(file, returnBackFolder);
							  FileUtils.forceDelete(file);
						   }else if (file.getName().toLowerCase().endsWith("4check.pdf")){
							  FilesUtils.copyFileToDirectory(file, returnBackFolder);
							  FileUtils.forceDelete(file);
						   }else if (file.getName().toLowerCase().endsWith(".pdf")){
							  FilesUtils.copyFileToDirectory(file, returnBackFolder);
							  FilesUtils.moveFileToDirectory(file, new File(properties.getLocalReturnPdf()), true);
						   }
					   }
				   }
	    		   //待檢查的，準備置換的檔案
	    		   File myReturnFolder = new File(new File(properties.getCheckedOkPath()).getParentFile(), "returnTest");
	    		   File forCheckImg = new File(myReturnFolder, "forCheckImg");
	    		   File forCheckLaw = new File(myReturnFolder, "forCheckLaw");
	    		   File forCheckPdf2TifImg = new File(forCheckImg, "pdf2tif");
	    		   if(!forCheckPdf2TifImg.exists())
	    			   forCheckPdf2TifImg.mkdirs();
	    		   File forCheckPdf2TifLaw = new File(forCheckLaw, "pdf2tif");
	    		   if(!forCheckPdf2TifLaw.exists())
	    			   forCheckPdf2TifLaw.mkdirs();
	    		   File forCheckTxt = new File(myReturnFolder, "forCheckTxt");
	    		   File forCheckPgm = new File(myReturnFolder, "forCheckPgm");
	    		   File forCheckOverlay = new File(myReturnFolder, "forCheckOverlay");
	    		   File returnImage = new File(myReturnFolder, "image");
	    		   File returnLaw = new File(myReturnFolder, "law");
	    		   File returnPdf2TifImage = new File(returnImage, "pdf2tif");
	    		   File returnPdf2TifLaw = new File(returnLaw, "pdf2tif");
	    		   File returnTxt = new File(myReturnFolder, "txt");
	    		   File returnPgm = new File(myReturnFolder, "pgm");
	    		   File returnOverlay = new File(myReturnFolder, "overlay");
	    		   
	    		
	    		   if(!forCheckImg.exists())
	    			   forCheckImg.mkdirs();
	    		   if(!forCheckLaw.exists())
	    			   forCheckLaw.mkdirs();
	    		   if(!forCheckTxt.exists())
	    			   forCheckTxt.mkdirs();
	    		   if(!forCheckPgm.exists())
	    			   forCheckPgm.mkdirs();
	    		   if(!returnImage.exists())
	    			   returnImage.mkdirs();
	    		   if(!returnLaw.exists())
	    			   returnLaw.mkdirs();
	    		   if(!returnTxt.exists())
	    			   returnTxt.mkdirs();
	    		   if(!returnPgm.exists())
	    			   returnPgm.mkdirs();
				   //System.out.println("froCheckImg path:" + forCheckImg.getAbsolutePath());
				   for(File file : forCheckImg.listFiles(FileFilterImpl.getFileFilter())){
				      FilesUtils.moveFileToDirectory(file, returnImage, true);
				   }
				   for(File file : forCheckPdf2TifImg.listFiles(FileFilterImpl.getFileFilter())){
					  FilesUtils.moveFileToDirectory(file, returnPdf2TifImage, true);
				   }
				   for(File file : forCheckPgm.listFiles()){
				      FilesUtils.moveFileToDirectory(file, returnPgm, true);
				   }
				   for(File file : forCheckOverlay.listFiles()){
					 FilesUtils.moveFileToDirectory(file, returnOverlay, true);
				   }
		    	   for(File file : forCheckLaw.listFiles(FileFilterImpl.getFileFilter())){
				      FilesUtils.moveFileToDirectory(file, returnLaw, true);
				   }
		    	   for(File file : forCheckPdf2TifLaw.listFiles(FileFilterImpl.getFileFilter())){
					  FilesUtils.moveFileToDirectory(file, returnPdf2TifLaw, true);
				   }
		    	   for(File file : forCheckTxt.listFiles()){
		    		   //把原先要用來比較的檔案刪除
		    	      if(returnTxt.listFiles(FileFilterImpl.getFileFilter()) != null)
		    	         for(File oriReturnTxt : returnTxt.listFiles(FileFilterImpl.getFileFilter())){
		    		        if(oriReturnTxt.getName().length() >= 10 
		    		    		 && file.getName().length() >= 10 
		    		    		 && file.getName().substring(10).equals(oriReturnTxt.getName().substring(10))){
		    		    	    try{
		    		    		    FilesUtils.forceDelete(oriReturnTxt); 
		    		    	    }catch(Exception e){
		    		    		    logger.error("", e);
		    		    	    }
		    		        }
		    	         }
				      FilesUtils.moveFileToDirectory(file, returnTxt, true);
				   }
		    	   File dailyData = new File(new File(properties.getLocalReturnForCheck()).getParentFile(), "DATA");
		    	   if(dailyData.exists() && dailyData.isDirectory()){
		    		   FileUtils.deleteDirectory(dailyData);
		    	   }
		    	   if(!dailyData.exists())
		    		   dailyData.mkdirs();
		    	   File[] forCheckFiles = returnTxt.listFiles(FileFilterImpl.getFileFilter());
		    	   for(File eachFile : forCheckFiles){
		    		   FileUtils.copyFileToDirectory(eachFile, dailyData);
		    	   }
		    		
                   //File checkDataOk = new File(okFolder, "check_data.ok");
				   File batchLock = new File(okFolder, "batch.lock");
				   File keepLock = new File(okFolder, "batch.keeplock");
				   if (batchLock.exists())
					   FileUtils.forceDelete(batchLock);
				   if (keepLock.exists())
					   FileUtils.forceDelete(keepLock);
				   //if (checkDataOk.exists())
					  // FileUtils.forceDelete(checkDataOk);
				   if(d2oing.exists())
					   FileUtils.forceDelete(d2oing);
				   properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
				   properties.setReturnUnlock(false);
				   ((VoService) Constant.getContext().getBean(
						   "voServiceProxy")).update(properties);				   
				   
				   for(File srcFile : returnLocalFiles){				    	
				    	try {
							FileUtils.copyFileToDirectory(srcFile, pdfTmp);
						} catch (IOException e) {
							logger.error("", e);
							e.printStackTrace();
						}
				    }
				   //如果checkFileTmp裡有檔案，把它move到returnFolder中，並寫入check_data.ok
				   File checkFileTmp = new File(myReturnFolder.getParent(), "checkDataTmp");
				   File[] moveBackFiles = checkFileTmp.listFiles(FileFilterImpl.getFileFilter());
				   if(moveBackFiles != null && moveBackFiles.length > 0){
					   for(File file : moveBackFiles){						  
					      FilesUtils.moveFileToDirectory(file, returnFolder, true);					      
					   }
					   File checkOk = new File(okFolder, "CHECK_DATA.OK");
					   fw = new FileWriter(checkOk);
					   fw.write("");
					   fw.flush();
					   fw.close();
					   fw = null;
				   }
			    }catch(Exception e){
				   logger.error("", e);
				   ErrorReport err = new ErrorReport();
				   err.setErrHappenTime(new Date());
				   err.setErrorType("return check");
				   err.setMessageBody("錯誤發生" + e.getLocalizedMessage());
				   err.setReported(false);
				   err.setException(true);
				   err.setTitle("D2O unlock error");											    		
				   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);
			   }
			}
			//如果發現已經上傳了，那就把它抓下來
			if(properties.getPdfzipFileName() != null && !"".equals(properties.getPdfzipFileName())){
				File localDownload = new File(properties.getBackupFolder(), properties.getPdfzipFileName());
				File localBackup = new File(properties.getBackupFolder(), "POLICY_PDF");
				if(!localBackup.exists())
					localBackup.mkdirs();
				SftpClientUtil sftpC = new SftpClientUtil(properties.getFxdmsIP(), properties.getFxdmsUser(), properties.getFxdmsPwd());
				boolean success = sftpC.downloadFile(properties.getFxdmsDownloadPath(), properties.getPdfzipFileName(), localDownload);
				if(success){
					sftpC.delete(properties.getFxdmsDownloadPath(), properties.getPdfzipFileName());
					ZipUtils.unzipFile(localDownload, localBackup, false);
					//抓完並解壓縮後，把properties更新
					properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
					properties.setPdfzipFileName("");
					((VoService) Constant.getContext().getBean(
							"voServiceProxy")).update(properties);
				}
			}

		} catch (Exception e) {
			logger.error("", e);
		} finally {
			running = false;
		}
		logger.info("sftp listener this time  stop");

	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		SftpListener.running = running;
	}
	
	

}
