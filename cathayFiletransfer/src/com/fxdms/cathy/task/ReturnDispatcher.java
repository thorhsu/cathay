package com.fxdms.cathy.task;

import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.BeansException;

import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FileFilterImpl;
import com.fxdms.util.FilesUtils;
import com.fxdms.util.LockFile;
import com.fxdms.util.PdfCompare;
import com.fxdms.util.PdfFileUtil;
import com.fxdms.cathy.bo.SplitFile;
import com.fxdms.cathy.conf.Constant;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Properties;
import com.salmat.pas.vo.ErrorReport;


public class ReturnDispatcher {
    
	
	static Logger logger = Logger.getLogger(ReturnDispatcher.class);
	static File localReturnPdfFolder = new File(com.fxdms.cathy.bo.Properties.getLocalReturnPdf());
	static File dailyFolder = new File(localReturnPdfFolder.getParentFile(), "Daily");
	//static File localReturnPdfFolder = null;
	//static File dailyFolder = null;
	
    
	private static boolean running = false;    
	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		ReturnDispatcher.running = running;
	}

	public static void startToRun() throws BeansException, RemoteException {
		// 時間設定更新影像檔的程式要早一點進行，如果更新還在進行中，先暫停此thread，等下一次再進行
		// 如果有其它Thread正在run，也是跳出去		
		if (SftpListener.isRunning())
			return;
	    					
		Properties properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();	
		File pdfPath = new File(properties.getAfpPath(), "pdf");
		int normCounter = 0;
		int reptCounter = 0;
		int reisCounter = 0;
		int convCounter = 0;
		logger.info("return dispatcher start to run");
		if(!new File(properties.getLocalReturnForCheck()).exists())
			new File(properties.getLocalReturnForCheck()).mkdirs();
		if(!localReturnPdfFolder.exists())
			localReturnPdfFolder.mkdirs();
		if(!dailyFolder.exists())
			dailyFolder.mkdirs();
		logger.info("return dispatcher start to run1");
		
		
		try {
			running = true;
			Date today = new Date();
			File returnFolder = new File(properties.getLocalReturnPath()); // 迴歸件
			if (!returnFolder.exists())
				returnFolder.mkdirs();
			File okFolder = new File(properties.getLocalOKPath());
			if (!okFolder.exists())
				okFolder.mkdirs();
			File batchLock = new File(okFolder, "batch.lock");
			File keepLock = new File(okFolder, "batch.keeplock");
			
			File[] okFiles = okFolder.listFiles();
			boolean checkDataOk = false;

			File[] files = returnFolder.listFiles(FileFilterImpl
					.getFileFilter());
			Map<String, String> dailyCheckFiles = new HashMap<String, String>();
			Map<String, String> originalCheckFiles = new HashMap<String, String>();
			String originalTxt = "";
			String originalDetailTxt = "";
			String dailyTxt = "";
			String dailyDetailTxt = "";
			
			File myReturnFolder = new File(new File(properties.getCheckedOkPath()).getParentFile(), "returnTest");
    		if(!myReturnFolder.exists())
    			myReturnFolder.mkdirs();
    		File dailyTxtFile = new File(myReturnFolder, "dailyTxt.txt");
			File forCheckImg = new File(myReturnFolder, "forCheckImg");    			    		
    		File forCheckLaw = new File(myReturnFolder, "forCheckLaw");    		
    		File forCheckTxt = new File(myReturnFolder, "forCheckTxt");
    		File forCheckPdf2TifImg = new File(forCheckImg, "pdf2tif");
    		File forCheckPdf2TifLaw = new File(forCheckLaw, "pdf2tif");
    		
    		if(!forCheckImg.exists())
    			forCheckImg.mkdirs();
    		if(!forCheckLaw.exists())
    			forCheckLaw.mkdirs();
    		if(!forCheckTxt.exists())
    			forCheckTxt.mkdirs();
    		if(!forCheckPdf2TifImg.exists())
    			forCheckPdf2TifImg.mkdirs();	    		    		
    		if(!forCheckPdf2TifLaw.exists())
    			forCheckPdf2TifLaw.mkdirs();
    		
    		
    		
    		
    		
    		File presImage = new File(properties.getImgPostProcessedPath(), "image");
    		File presLaw = new File(properties.getImgPostProcessedPath(), "law");
    		File presPdf2TifImage = new File(presImage, "pdf2tif");
    		File presPdf2TifLaw = new File(presLaw, "pdf2tif");
    		
    		File returnImage = new File(myReturnFolder, "image");
    		File returnLaw = new File(myReturnFolder, "law");
    		File returnPdf2TifImage = new File(returnImage, "pdf2tif");
    		File returnPdf2TifLaw = new File(returnLaw, "pdf2tif");
    		File returnTxt = new File(myReturnFolder, "txt");
    		
    		if(!returnImage.exists())
    			returnImage.mkdirs();
    		if(!returnLaw.exists())
    			returnLaw.mkdirs();
    		if(!returnTxt.exists())
    			returnTxt.mkdirs();
			
			/*
			 * DATA.OK存在，且batch.lock不存在時開始作業 目前不管Data.OK惹
			 */
			for (File file : okFiles) {
				if (file.getName().equalsIgnoreCase("Check_DATA.OK")) {
					checkDataOk = true;													    
					break;
				}
			}	
			// 如果迴歸資料都送完了
			if (checkDataOk && files != null && files.length > 0) {
				String filesDate = null;
				
				//先全部送去d:/cathayTransfer/checkDataTmp下
				for(File file : files){
					if(file.getName().length() > 10)
					   filesDate = file.getName().substring(0, 10);
					else
					   FilesUtils.forceDelete(file);
					try{
					    Constant.yyyy_MM_dd.parse(filesDate);
					    FilesUtils.moveFileToDirectory(file, new File(myReturnFolder.getParent(), "checkDataTmp"), true);
					}catch(Exception e){
						logger.error("", e);
						FilesUtils.forceDelete(file);
					}					
				}				
				
				File[] moveBackFiles = new File(myReturnFolder.getParent(), "checkDataTmp").listFiles(FileFilterImpl.getFileFilter());
				//再把送去d:/cathayTransfer/checkDataTmp下，開頭為同一天的送回來進行迴歸測試
				if(moveBackFiles != null && filesDate != null)
				   for(File file : moveBackFiles){
				      String fileNm = file.getName();
				      if(fileNm.startsWith(filesDate)){
				    	  FilesUtils.moveFileToDirectory(file, returnFolder, true);
				      }
				   }
				//送回來後再listFiles一次
				files = returnFolder.listFiles(FileFilterImpl.getFileFilter());
				
				SplitFile.reprintMap = new HashMap<String, Integer>();
				for(File file : forCheckImg.listFiles(FileFilterImpl.getFileFilter())){
					FileUtils.forceDelete(file);
				}    			    		
				for(File file : forCheckLaw.listFiles(FileFilterImpl.getFileFilter())){
					FileUtils.forceDelete(file);
				}    			    			    		    		
				for(File file : forCheckTxt.listFiles(FileFilterImpl.getFileFilter())){
					FileUtils.forceDelete(file);
				}    			    		
				for(File file : forCheckPdf2TifImg.listFiles(FileFilterImpl.getFileFilter())){
					FileUtils.forceDelete(file);
				}    			    		
				for(File file : forCheckPdf2TifLaw.listFiles(FileFilterImpl.getFileFilter())){
					FileUtils.forceDelete(file);
				}    			    		
				
				File outputText = new File(properties.getLocalReturnForCheck(), "Error_" + Constant.yyyyMMdd_HHmmSS.format(today) + ".txt");
				FileWriter outputTextWr = null;
				FileWriter dailyTextWr = null;
				outputTextWr = new FileWriter(outputText);
				
				dailyTextWr = new FileWriter(dailyTxtFile);

				
				
				ErrorReport errbeg = new ErrorReport();
	    		errbeg.setErrHappenTime(new Date());
	    		errbeg.setErrorType("return check");
	    		errbeg.setMessageBody(Constant.yyyy_MM_ddHHMM.format(new Date()) +" :迴歸測試開始進行");
	    		errbeg.setReported(false);
	    		errbeg.setTitle("return check");											    		
	    		((VoService) Constant.getContext().getBean("voServiceProxy")).save(errbeg);
	    		properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
	    		properties.setReturnTestTxt(null);
	    		properties.setReturnTestFiles(null);
	    		((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);
	    		
	    		for (File file : okFiles) {
					if (file.getName().equalsIgnoreCase("Check_DATA.OK")) {
						FilesUtils.forceDelete(file);
						break;
					}
				}	
	    		
	    		
				// 寫入file lock和file keep lock
				if (batchLock.exists()) {

				} else {
					FileWriter fw = new FileWriter(batchLock);
					fw.write("batch.lock");
					fw.flush();
					fw.close();
				}
				if (keepLock.exists()) {
					
				}else{
					FileWriter fw = new FileWriter(keepLock);
					fw.write("batch.keeplock");
					fw.flush();
					fw.close();
				}
				File[] returnLocalFiles = localReturnPdfFolder.listFiles(FileFilterImpl.getFileFilter());
				logger.info("LocalFiles:" + returnLocalFiles.length);
				File[] dailyPdfFiles = dailyFolder.listFiles();		
				logger.info("daily Files:" + dailyPdfFiles.length);
				File pdfTmp = new File(new File(properties.getCheckedOkPath()).getParentFile(), "pdfTmp");
				for(File dailyFile : dailyPdfFiles){
			    	try{
			    		FileUtils.forceDelete(dailyFile);
			    	}catch(Exception ex){
			    		logger.error("", ex);
			    		ex.printStackTrace();
			    	}
			    }
				if(pdfTmp.exists() && pdfTmp.listFiles(FileFilterImpl.getFileFilter()) != null 
						&& pdfTmp.listFiles(FileFilterImpl.getFileFilter()).length == returnLocalFiles.length){
					returnLocalFiles = pdfTmp.listFiles(FileFilterImpl.getFileFilter());
					try{
						for(File srcFile : returnLocalFiles){
							FilesUtils.moveFileToDirectory(srcFile, dailyFolder, true);
							if(new File(dailyFolder, srcFile.getName()).exists())
								new File(dailyFolder, srcFile.getName()).setLastModified(new Date().getTime());
					    }
			    		
			    	}catch(Exception ex){
			    		logger.error("", ex);
			    		ex.printStackTrace();
			    	}
				}else{
			        for(File srcFile : returnLocalFiles){
			    	    File destFile = new File(dailyFolder, srcFile.getName());
			    	    try {
						   FileUtils.copyFile(srcFile, destFile, false);
					    } catch (IOException e) {
						   logger.error("", e);
						   e.printStackTrace();
					    }
			        }
				}

				
	    		
	    		
	    		
	    		File forCheckPgm = new File(myReturnFolder, "forCheckPgm");
	    		if(!forCheckPgm.exists())
	    			forCheckPgm.mkdirs();	    		
	    		File returnPgm = new File(myReturnFolder, "pgm");
	    		File[] pgms = returnPgm.listFiles();
	    		File kotaiPgmsFolder = new File("D:\\kotai\\pgm\\");
	    		//檢查看看程式有沒有變
	    		if(pgms != null){
	    			for(File oriPgm : pgms){
	    			   String pgmNm = oriPgm.getName();
	    			   File kotaiPgm = new File(kotaiPgmsFolder, pgmNm);
	    			   //如果有時間或大小不同的話，copy到forCheckPgm
	    			   if((kotaiPgm.length() - oriPgm.length()) != 0 || Math.abs(kotaiPgm.lastModified() - oriPgm.lastModified()) > 3000){
	    				   FilesUtils.copyFileToDirectory(kotaiPgm, forCheckPgm);
	    			   }
	    			}
	    		}
	    		File[] forCheckPgms = forCheckPgm.listFiles();
	    		boolean policyChanged = false;
	    		boolean signChanged = false;
	    		if(forCheckPgms != null && forCheckPgms.length > 0){
	    			for(File checkPgm : forCheckPgms){
	    				if(checkPgm.getName().startsWith("PIP_Kotai_SIGN_1")){
	    					signChanged = true;
	    				}else if(checkPgm.getName().startsWith("PIP_Kotai_all")){
	    					policyChanged = true;
	    				}
	    			}
	    		}
	    		
	    		
	    		File forCheckOverlay = new File(myReturnFolder, "forCheckOverlay");
	    		if(!forCheckOverlay.exists())
	    			forCheckOverlay.mkdirs();	    		
	    		File returnOverlay = new File(myReturnFolder, "overlay");
	    		File[] overlays = returnOverlay.listFiles();
	    		File kotaiOverlaysFolder = new File("D:\\kotai\\overlay\\");
	    		//檢查看看程式有沒有變
	    		if(overlays != null){
	    			for(File oriOverlay : overlays){
	    			   String overlayNm = oriOverlay.getName();
	    			   File kotaiOverlay = new File(kotaiOverlaysFolder, overlayNm);
	    			   //如果有時間或大小不同的話，copy到forCheckOverlay
	    			   if((kotaiOverlay.length() - oriOverlay.length()) != 0 || Math.abs(kotaiOverlay.lastModified() - oriOverlay.lastModified()) > 3000){
	    				   FilesUtils.copyFileToDirectory(kotaiOverlay, forCheckOverlay);
	    			   }
	    			}
	    		}
	    		File[] forCheckOverlays = forCheckOverlay.listFiles();
	    		if(forCheckOverlays != null && forCheckOverlays.length > 0)
	    			signChanged = true;
	    		
	    		
	    		//待檢查的，準備置換的檔案
	    		
	    		
	    		HashSet<File> differentImages = new HashSet<File>();
	    		HashSet<File> differentLaws = new HashSet<File>();
	    		HashSet<File> differentPdf2TifImages = new HashSet<File>();
	    		HashSet<File> differentPdf2TifLaws = new HashSet<File>();
	    		HashMap<File, Integer> differentImagesMap = new HashMap<File, Integer>();
	    		HashMap<File, Integer> differentLawsMap = new HashMap<File, Integer>();
	    		
	    		//找出和上次迴歸測試不同的影像檔
	    		for(File tif : presImage.listFiles(FileFilterImpl.getImgFileFilter())){
	    			File compareTif = new File(returnImage, tif.getName());
	    			
	    			if(compareTif.exists()){
	    				if(compareTif.lastModified() != tif.lastModified() || compareTif.length() != tif.length()){
	    					differentImages.add(tif);
	    					logger.info(tif.getAbsoluteFile() + " was be added to different Set");
	    				}
	    			}else{
	    				differentImages.add(tif);
	    				logger.info(tif.getAbsoluteFile() + " was be added to different Set");
	    			}
	    		}
	    		for(File tif : presPdf2TifImage.listFiles(FileFilterImpl.getImgFileFilter())){
	    			File compareTif = new File(returnPdf2TifImage, tif.getName());
	    			
	    			if(compareTif.exists()){
	    				if(compareTif.lastModified() != tif.lastModified() || compareTif.length() != tif.length()){
	    					differentPdf2TifImages.add(tif);
	    					logger.info(tif.getAbsoluteFile() + " was be added to different Pdf2Tif Set");
	    				}
	    			}else{
	    				differentPdf2TifImages.add(tif);
	    				logger.info(tif.getAbsoluteFile() + " was be added to different Pdf2Tif Set");
	    			}
	    		}
	    		
	    		//找出和上次迴歸測試不同的影像檔
	    		for(File tif : presLaw.listFiles(FileFilterImpl.getImgFileFilter())){
	    			File compareTif = new File(returnLaw, tif.getName());
	    			if(compareTif.exists()){
	    				if(compareTif.lastModified() != tif.lastModified() || compareTif.length() != tif.length()){
	    					differentLaws.add(tif);
	    					logger.info(tif.getAbsoluteFile() + " was be added to different Set");
	    				}
	    			}else{
	    				differentLaws.add(tif);
	    				logger.info(tif.getAbsoluteFile() + " was be added to different Set");
	    			}
	    		}
	    		for(File tif : presPdf2TifLaw.listFiles(FileFilterImpl.getImgFileFilter())){
	    			File compareTif = new File(returnPdf2TifLaw, tif.getName());
	    			if(compareTif.exists()){
	    				if(compareTif.lastModified() != tif.lastModified() || compareTif.length() != tif.length()){
	    					differentPdf2TifLaws.add(tif);
	    					logger.info(tif.getAbsoluteFile() + " was be added to different Set");
	    				}
	    			}else{
	    				differentPdf2TifLaws.add(tif);
	    				logger.info(tif.getAbsoluteFile() + " was be added to different Set");
	    			}
	    		}
				
				if (files != null && files.length > 0) {
				    File tmpFolder = new File(properties.getPresPath(), "tmp");
					File outputFile = new File(tmpFolder,
							"CAAAB201406010001.tmp");
					File renameFile = new File(properties.getPresPath(),
							"CAAAB201406010001.DAT");															
					File sgOutputFile = new File(tmpFolder,
							"SGAAB201406010001.tmp");
					File sgRenameFile = new File(properties.getPresPath(),
							"SGAAB201406010001.DAT");
					
					if (outputFile.exists())
						FileUtils.forceDelete(outputFile);
					if(renameFile.exists())
						FileUtils.forceDelete(renameFile);
					if (sgOutputFile.exists())
						FileUtils.forceDelete(sgOutputFile);
					if(sgRenameFile.exists())
						FileUtils.forceDelete(sgRenameFile);
					
					if(new File(properties.getAfpPath(), "CAAAB201406010001.AFP").exists())
						FileUtils.forceDelete(new File(properties.getAfpPath(), "CAAAB201406010001.AFP"));
					if(new File(properties.getAfpPath(), "CAAAB201406010001.CSV").exists())
						FileUtils.forceDelete(new File(properties.getAfpPath(), "CAAAB201406010001.CSV"));
					if(new File(pdfPath, "CAAAB201406010001.pdf").exists())
						FileUtils.forceDelete(new File(pdfPath, "CAAAB201406010001.pdf"));
					
					if(new File(properties.getAfpPath(), "SGAAB201406010001.AFP").exists())
						FileUtils.forceDelete(new File(properties.getAfpPath(), "SGAAB201406010001.AFP"));
					if(new File(properties.getAfpPath(), "SGAAB201406010001.CSV").exists())
						FileUtils.forceDelete(new File(properties.getAfpPath(), "SGAAB201406010001.CSV"));
					if(new File(pdfPath, "SGAAB201406010001.pdf").exists())
						FileUtils.forceDelete(new File(pdfPath, "SGAAB201406010001.pdf"));
										
					// 開始進行合併保單
					boolean allFinished = true;

					int mergeFiles = 1;
					try {
						//先切檔案
						File returnBackupFolder = new File(properties.getBackupFolder(),  "CHECK_DATA");
						
						
						if(!returnBackupFolder.exists())
							returnBackupFolder.mkdirs();
						InputdateParser.forceReadFile();
						returnBackupFolder = new File(returnBackupFolder, Constant.yyyy_MM_dd.format(InputdateParser.getInputDate()));
						if(!returnBackupFolder.exists())
							returnBackupFolder.mkdirs();

						for(File file : files){
							SplitFile.returnSplitCathay(file, file.getParentFile());																						
							//移到備份目錄								   
							//FilesUtils.copyFileToDirectory(file, dailyData, true);
							FilesUtils.moveFileToDirectory(file, returnBackupFolder, true);
						}
						files = returnFolder.listFiles(FileFilterImpl
								.getFileFilter());
						if(files != null)
						   for(File dailyFile : files){
							   if(dailyFile.getName().length() > 10)
						          dailyCheckFiles.put(dailyFile.getName().substring(10), dailyFile.getName());
						   }
												
						//開始檢查保單文字檔，如果tif檔有不一樣，或是文字檔有變，就放到這個set
						ArrayList<File> returnFiles = new ArrayList<File>();
						for(File file : files){							
							String policyTxt1 = "";
							String fileNm = file.getName().substring(10);
							String[] fileNmSplit = file.getName().split("_");
							if(file.getName().toUpperCase().indexOf("SPLITPOLICY") > 0){
								if(fileNmSplit[3].toUpperCase().equals("NORM"))
								   normCounter++;
								else if(fileNmSplit[3].toUpperCase().equals("REPT"))
								   reptCounter++;
								else if(fileNmSplit[3].toUpperCase().equals("CONV"))
								   convCounter++;
								else if(fileNmSplit[3].toUpperCase().equals("REIS"))
								   reisCounter++;
							}
							
							//boolean withDifferent = false;
							FileInputStream testFis = new FileInputStream(file);
							InputStreamReader testIsr  = new InputStreamReader(testFis, "ms950");
							BufferedReader testBr = new BufferedReader(testIsr);
							String line = null;
							while ((line = testBr.readLine()) != null) {
								//System.out.println(line);
								policyTxt1 += (line.trim() + "\r\n");
								int index = 0;
								//查看看內含的tif是不是和之前的不同，不同，免得造成轉檔錯誤
								if((index = line.indexOf(".tif")) >= 0 || (index = line.indexOf(".pdf")) >= 0){
									String forCheck = null;
									if(line.indexOf(".tiff") > 0)
									   forCheck = line.substring(0, index + 5);
									else
									   forCheck = line.substring(0, index + 4);
									String tifFileNm = null;
									int beginIndex = 0;
                                    if((beginIndex = forCheck.lastIndexOf("..")) > 0){
										tifFileNm = forCheck.substring(beginIndex + 2);
									}else{
										beginIndex = forCheck.lastIndexOf("|");
										tifFileNm = forCheck.substring(beginIndex + 1);
										//如果都沒有的時候
										if(forCheck.toLowerCase().indexOf("\\law\\") < 0 && forCheck.toLowerCase().indexOf("\\image\\") < 0){
											tifFileNm = "\\Image\\" + tifFileNm;
										}										
									}
                                    File tifFile = new File(properties.getImgPostProcessedPath(), tifFileNm);
                                    
                                    if(differentImages.contains(tifFile) ){
                                    	Integer counter = (differentImagesMap.get(tifFile) == null)? 0 : differentImagesMap.get(tifFile);
                                    	counter++;
                                    	//超過30次就不轉檔，反正也比對不了那麼多
                                    	if(counter < 31 && !returnFiles.contains(file)){
                                    	   returnFiles.add(file);
                                    	   logger.info(tifFile.getAbsoluteFile() + " was different therefore be added to for check Set");
                                    	}else{
                                    		logger.info(tifFile.getAbsoluteFile() + " was checked more than 30 times");
                                    	}
                                    	differentImagesMap.put(tifFile, counter);                                    	
                                    	//withDifferent = true;
                                    	FilesUtils.copyFileToDirectory(tifFile, forCheckImg, true);		                                    	
                                    	FilesUtils.copyFileToDirectory(file, forCheckTxt, true);
                                    }else if(differentPdf2TifImages.contains(tifFile) ){
                                    	Integer counter = (differentImagesMap.get(tifFile) == null)? 0 : differentImagesMap.get(tifFile);
                                    	counter++;
                                    	//超過30次就不轉檔，反正也比對不了那麼多
                                    	if(counter < 31 && !returnFiles.contains(file)){
                                    	   returnFiles.add(file);
                                    	   logger.info(tifFile.getAbsoluteFile() + " was different therefore be added to for check Set");
                                    	}else{
                                    		logger.info(tifFile.getAbsoluteFile() + " was checked more than 30 times");
                                    	}
                                    	differentImagesMap.put(tifFile, counter);                                    	
                                    	//withDifferent = true;
                                    	FilesUtils.copyFileToDirectory(tifFile, forCheckPdf2TifImg, true);		                                    	
                                    	FilesUtils.copyFileToDirectory(file, forCheckTxt, true);
                                    }else if(differentLaws.contains(tifFile) ){
                                    	Integer counter = (differentLawsMap.get(tifFile) == null)? 0 : differentLawsMap.get(tifFile);
                                    	counter++;
                                    	//超過30次就不轉檔，反正也比對不了那麼多
                                    	if(counter < 31 && !returnFiles.contains(file)){
                                    	   returnFiles.add(file);
                                    	   logger.info(tifFile.getAbsoluteFile() + " was different therefore be added to for check Set");
                                    	}else{
                                    		logger.info(tifFile.getAbsoluteFile() + " was checked more than 30 times");
                                    	}
                                    	differentLawsMap.put(tifFile, counter);
                                    	
                                    	//withDifferent = true;
                                    	FilesUtils.copyFileToDirectory(tifFile, forCheckLaw, true);
                                    	FilesUtils.copyFileToDirectory(file, forCheckTxt, true);
                                    }else if(differentPdf2TifLaws.contains(tifFile) ){
                                    	Integer counter = (differentLawsMap.get(tifFile) == null)? 0 : differentLawsMap.get(tifFile);
                                    	counter++;
                                    	//超過30次就不轉檔，反正也比對不了那麼多
                                    	if(counter < 31 && !returnFiles.contains(file)){
                                    	   returnFiles.add(file);
                                    	   logger.info(tifFile.getAbsoluteFile() + " was different therefore be added to for check Set");
                                    	}else{
                                    		logger.info(tifFile.getAbsoluteFile() + " was checked more than 30 times");
                                    	}
                                    	differentLawsMap.put(tifFile, counter);
                                    	
                                    	//withDifferent = true;
                                    	FilesUtils.copyFileToDirectory(tifFile, forCheckPdf2TifLaw, true);
                                    	FilesUtils.copyFileToDirectory(file, forCheckTxt, true);
                                    }
								}
							}
							testBr.close();
							testIsr.close();
							testFis.close();
							boolean findSameFile = false;
							File[] returnTxtFiles = returnTxt.listFiles(FileFilterImpl.getFileFilter());
							if(returnTxtFiles != null){
								for(File returnTxtFile : returnTxtFiles){
									if(returnTxtFile.getName().length() > 10){
										originalCheckFiles.put(returnTxtFile.getName().substring(10), returnTxtFile.getName());
									}
								}
								
							}
							   
							
							for(File returnFile : returnTxtFiles){
								String forCheckNm = returnFile.getName().substring(10);
								if(fileNm.equals(forCheckNm)){
									findSameFile = true;
									String policyTxt2 = "";																		
									testFis = new FileInputStream(returnFile);
									testIsr  = new InputStreamReader(testFis, "ms950");
									testBr = new BufferedReader(testIsr);
									while ((line = testBr.readLine()) != null) {
										//System.out.println(line);
										policyTxt2 += (line.trim() + "\r\n");										
									}
									testBr.close();
									testIsr.close();
									testFis.close();
									//如果檔案內容不相同就放入 
									if(!policyTxt1.equals(policyTxt2) && !returnFiles.contains(file)){
										//withDifferent = true;
										returnFiles.add(file);										
                                    	logger.info(file.getName()+ " was be added to for check Set because of txt was changed");
										FilesUtils.copyFileToDirectory(file, forCheckTxt, true);
									}
								}
							}
							//如果沒找到，就加入returnFilies的set
							if(!findSameFile && !returnFiles.contains(file)){
								returnFiles.add(file);
								FilesUtils.copyFileToDirectory(file, forCheckTxt, true);
							}
							//如果程式有改變，而且是簽收單的話
							if(signChanged && !returnFiles.contains(file) && file.getName().indexOf("CHECK_SPLITSIGN") > 0){
								returnFiles.add(file);
								FilesUtils.copyFileToDirectory(file, forCheckTxt, true);
							}
							if(policyChanged && !returnFiles.contains(file) && file.getName().indexOf("CHECK_SPLITPOLICY") > 0){
								returnFiles.add(file);
								FilesUtils.copyFileToDirectory(file, forCheckTxt, true);
							}
							
							/*							
							if(withDifferent || !findSameFile){
								//returnFiles.add(file);
								FilesUtils.copyFileToDirectory(file, forCheckTxt, true);
							}
							*/
							//如果returnFiles含有此檔案，就不刪除
							if(!returnFiles.contains(file)){
								try{
								   FilesUtils.forceDelete(file);
								}catch(Exception e){
									logger.error("", e);
									e.printStackTrace();
								}
							}
						}	
						
						int originalCount = 0;
						Set<String> originalFiles = originalCheckFiles.keySet();
						Set<String> dailyFiles = dailyCheckFiles.keySet();
						for(String original : originalFiles){
							if(!dailyFiles.contains(original)){
								originalCount++;
								originalDetailTxt += "有原始無每日：" + originalCheckFiles.get(original) + "\r\n";
							}							
						}
						if(originalCount != 0){
							originalTxt = "有原始無每日檔案共：" + originalCount + "筆\r\n" ;
						}
												
						int dailyCount = 0;
						for(String daily : dailyFiles){
							if(!originalFiles.contains(daily)){
								dailyCount++;
								dailyDetailTxt += "有每日無原始：" + dailyCheckFiles.get(daily) + "\r\n";
							}							
						}
						if(dailyCount != 0){
							dailyTxt = "有每日無原始檔案共：" + dailyCount + "筆\r\n";
						}
						if((dailyTxt != null && !dailyTxt.equals("")) || (dailyDetailTxt != null && !dailyDetailTxt.equals(""))
								|| (originalTxt != null && !originalTxt.equals("")) || (originalDetailTxt != null && !originalDetailTxt.equals("")))
						outputTextWr.write(dailyTxt + dailyDetailTxt + originalTxt + originalDetailTxt);
						if(returnFiles.size() == 0){
							//dailyCheckFiles = new ArrayList<File>();
							//List<File> originalCheckFiles = new ArrayList<File>();
						    logger.info("迴歸測試結果無差別");
						    ErrorReport err = new ErrorReport();
						    err.setErrHappenTime(new Date());
						    err.setErrorType("return check");
						    err.setMessageBody(dailyTxt + originalTxt + Constant.yyyy_MM_ddHHMM.format(new Date()) + ":迴歸測試結果\r\n"
						    		+ "新契約：" + (normCounter + reptCounter) + "件\r\n"						    		
						    		+ "保補契轉：" + (reisCounter + convCounter) + "件\r\n"
						    		+ "無差別：作業正常");
						    err.setReported(false);
						    err.setTitle("return check end");											    		
						    ((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);
						    outputTextWr.write("迴歸測試無異常\r\n");
						    properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
						    properties.setReturnTestTxt(null);
						    properties.setReturnUnlock(true);
						    ((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);

							 running = false;
							 if(dailyTxtFile.exists()){
								 if(dailyTextWr != null){
									 dailyTextWr.close();
									 dailyTextWr = null;
								 }
								 FileUtils.forceDelete(dailyTxtFile);
							 }	 
							 ((VoService) Constant.getContext().getBean("voServiceProxy")).returnReport(); //寄出迴歸通知信
							 return;							
						}
						dailyTextWr.write(dailyTxt + originalTxt);
						FileInputStream fis = null;
						InputStreamReader isr  = null;
						BufferedReader br = null;
						
						FileOutputStream fos = null;
						OutputStreamWriter osw = null;
						BufferedWriter bw = null;
						int fileCounter = 0;
						
						File outFile = null;
						File policyOutFile = null;
						Set<String> outputFiles = new HashSet<String>();
						for (File file : returnFiles) {														
							if(file.getName().toUpperCase().indexOf("SPLITSIGN") > 0){

								outFile = sgOutputFile;
							}else{
								if(fileCounter >= 100){
									String name = outputFile.getName().substring(0, outputFile.getName().length() - 7) + StringUtils.leftPad(mergeFiles + "", 3, "0") + ".DAT";									
									policyOutFile.renameTo(new File(renameFile.getParentFile(), name));
									fileCounter = 0;
									mergeFiles++;
								}
								String name = outputFile.getName().substring(0, outputFile.getName().length() - 7) + StringUtils.leftPad(mergeFiles + "", 3, "0") + ".tmp";								
								outFile = new File(outputFile.getParentFile(), name);								
								policyOutFile = new File(outputFile.getParentFile(), name);
							}
							
							fos = new FileOutputStream(outFile, true);
							String fileNm = outFile.getName().substring(0, outFile.getName().length() - 4);
							outputFiles.add(fileNm);
							osw = new OutputStreamWriter(fos, "ms950");
							bw = new BufferedWriter(osw);
							
							fis = new FileInputStream(file);
							isr = new InputStreamReader(fis, "ms950");
							br = new BufferedReader(isr);
							String line = null;
							while ((line = br.readLine()) != null) {
								//c:\\kotai\\image\\test|1不寫入								
								int index = 0;
                                if(line.toLowerCase().indexOf("c:\\kotai\\image\\test|1") < 0){
									bw.write(line + "\r\n");
								}
							}
							br.close();
							isr.close();
							fis.close();
							
							bw.flush();
							osw.flush();
							fos.flush();
							bw.close();
							osw.close();
							fos.close();
							try{
							   FilesUtils.forceDelete(file);
							}catch(Exception e){
								logger.error("", e);
							}
							//簽收回條檔案小，全部集中成一個就可以
							if(file.getName().toUpperCase().indexOf("SPLITSIGN") < 0)
							   fileCounter++;
							
						}						
						if(bw != null)
							bw.close();
						if(isr != null)
							isr.close();
						if (fos != null)
							fos.close();
						
						if(policyOutFile != null && policyOutFile.exists()){
						   String name = policyOutFile.getName().replaceAll("tmp", "DAT");
						   policyOutFile.renameTo(new File(renameFile.getParentFile(), name));
					    }
						String returnTestFiles = "";
						for(String fileNm : outputFiles)
							returnTestFiles += fileNm + ",";
						if(!returnTestFiles.equals("")){
							//D:\CheckOUT\PDF_ERROR\Error_20151208_205206.txt
							properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
							properties.setReturnTestTxt(outputText.getAbsolutePath());
							properties.setReturnTestFiles(returnTestFiles);
							((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);
						}
						
					} catch (Exception e) {
						allFinished = false;
						logger.error("", e);
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("exception");
						er.setOldBatchName("");
						er.setReported(false);
						er.setException(true);
						er.setMessageBody("exception happen:" + e.getMessage());
						er.setTitle("exception happened");
						((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
					} finally {
						if(outputTextWr != null){
						   outputTextWr.flush();
						   outputTextWr.close();
						}
						outputTextWr = null;
						if(dailyTextWr != null){
						   dailyTextWr.flush();
						   dailyTextWr.close();
						}
						dailyTextWr = null;
						if(allFinished){
							if(sgOutputFile.exists())
							   sgOutputFile.renameTo(sgRenameFile);							
						}							
					}
					if(outputFile.exists() && outputFile.getName().toLowerCase().endsWith(".tmp"))
						FileUtils.forceDelete(outputFile);
				}	
				if(outputTextWr != null){
					outputTextWr.flush();
					outputTextWr.close();
				}					
				outputTextWr = null;
			}else if(checkDataOk && okFiles != null){
				for (File file : okFiles) {
					if (file.getName().equalsIgnoreCase("Check_DATA.OK")) {
						ErrorReport err = new ErrorReport();
						err.setErrHappenTime(new Date());
						err.setErrorType("return check");
						err.setMessageBody("迴歸測試有check_data.ok，但無迴歸測試檔可供測試");
						err.setReported(false);
						err.setException(true);
						err.setTitle("Return check passed but weird");
						((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);
						FilesUtils.forceDelete(file);
						break;
					}
				}	
				
			}
			
			String[] returnTestFileNms = properties.getReturnTestFileNms();
			String outputTxt = properties.getReturnTestTxt();
			if(returnTestFileNms != null && returnTestFileNms.length > 0 && outputTxt != null ){
				        if(dailyTxtFile.exists()){
					       FileReader fr = new FileReader(dailyTxtFile);				
					       BufferedReader br = new BufferedReader(fr);
					       String line = null;
					       dailyTxt = "";
					       originalTxt = "";
				           while((line = br.readLine()) != null ){
				    	      dailyTxt += line;
				           }
				           br.close();
				           fr.close();
				        }
				        FileWriter outputTextWr = null;
				        outputTextWr = new FileWriter(outputTxt, true);
						File returnBackupFolder = new File(properties.getBackupFolder(),  "CHECK_DATA");
						if(!returnBackupFolder.exists())
							returnBackupFolder.mkdirs();
						returnBackupFolder = new File(returnBackupFolder, Constant.yyyy_MM_dd.format(InputdateParser.getInputDate()));
						if(!returnBackupFolder.exists())
							returnBackupFolder.mkdirs();
						if(files != null && files.length > 0)
						    for (File file : files) {								
							   //做完一個就移到備份目錄	
						    	if(file.exists())
							      FilesUtils.moveFileToDirectory(file, returnBackupFolder, true);							
						   }
						//以下開始看afpFile是不是已經生成
						String afpFolder = properties.getAfpPath();
						
						File afpPath = new File(afpFolder);
						if (afpPath.exists()){
							String [] returnTestFiles = properties.getReturnTestFiles().split(",");
							List<File> pdfFiles = new ArrayList<File>();
							for(String returnTestFile : returnTestFiles){
								pdfFiles.add(new File(pdfPath, returnTestFile + ".pdf"));
							}
                            int i = pdfFiles.size();
                            long beginTime = new Date().getTime();
							while(pdfFiles.size() > 0){								
								i--;
								File pdfFile = pdfFiles.get(i);
								logger.info("waiting for " + pdfFile.getName() + " produced.");
								if(pdfFile != null && pdfFile.exists() && pdfFile.length() > 1024
										&& LockFile.checkFileIsReady(pdfFile)){
									logger.info("processing " + pdfFile.getName());
									boolean receipt = false;
									String pdfFileNm = pdfFile.getName();
									String newBatchName = pdfFileNm.substring(
											0, pdfFileNm.length() - 4); // 17位長的newBatchName
									String csvFileNm = newBatchName + ".csv"; //
									
									File csvFile = new File(pdfPath, csvFileNm);
									if (csvFile.getName().toUpperCase()
											.startsWith("SG")) {
										receipt = true;
									}

									logger.info("parsing csv file:"
											+ csvFile.getName());
									List<ApplyData> applyDatas = parseLog(csvFile);
									if (applyDatas != null
											&& applyDatas.size() > 0) {
										logger.info("start to split pdf. total "
												+ applyDatas.size() + " pdfs.");
										PDDocument pdfDoc = null;
										File rasFile = null;
										RandomAccessFile ras = null;
										try {
											rasFile = new File(
													properties.getZipTmpPath(),
													UUID.randomUUID() + "");
											ras = new RandomAccessFile(rasFile,
													"rw");
											pdfDoc = PDDocument.load(pdfFile,
													ras);

											// 開始進行比對前，先刪除裡面的所有檔案
											for (ApplyData applyData : applyDatas) {
												String pdfName = applyData.getCenter() + "_" + applyData.getApplyNo()
														+ "_"
														+ applyData.getPolicyNos()
														+ "_"
														+ applyData.getReprint()
														+ "_"
														+ applyData.getSourceCode()
														+ ".pdf";
												if (receipt)
													pdfName = "SG_" + pdfName;
												int startPage = applyData
														.getAfpBeginPage();
												int endPage = applyData
														.getAfpEndPage();
												logger.info("pdf name :"
														+ pdfName
														+ " | "
														+ "startPage:"
														+ startPage
														+ "| endPage:"
														+ endPage
														+ "| totalPage:"
														+ applyData
																.getTotalPage());
												if (endPage > 0
														&& startPage > 0
														&& endPage >= startPage) {

													String forCheckNm = pdfName;
													if (pdfName.toLowerCase()
															.endsWith(".pdf")) {
														forCheckNm = pdfName
																.substring(
																		0,
																		pdfName.length() - 4);
													}
													forCheckNm += "4check.pdf";
													logger.info("split pdf");
													PdfFileUtil.splitDocument(
																	startPage,
																	endPage,
																	new File(properties.getZipTmpPath(), pdfName)
																			.getAbsolutePath(),
																	pdfDoc);
													//搬到每日
													if(new File(properties.getZipTmpPath(), pdfName).exists()){
														if(new File(dailyFolder, pdfName).exists())
															try{
															   FileUtils.forceDelete(new File(dailyFolder, pdfName));
															}catch(Exception e){
																logger.error("", e);
															}
														FileUtils.copyFileToDirectory(new File(properties.getZipTmpPath(), pdfName), dailyFolder, true);
													}
													File oriFile = new File(localReturnPdfFolder, pdfName);
													if (oriFile.exists()) {
														File checkResultPdf = new File(properties.getLocalReturnForCheck(), forCheckNm);
														boolean noDifferent = PdfCompare
																.comparePdf(oriFile,
																		new File(properties.getZipTmpPath(), pdfName),
																		checkResultPdf);
														// 如果有不同，把結果，原來的pdf，新的pdf都copy到forcheck目錄
														if (checkResultPdf != null && checkResultPdf.exists()) {
															FilesUtils.moveFileToDirectory(
																			new File(properties.getZipTmpPath(), pdfName),
																			new File(properties.getLocalReturnForCheck()),
																			   true);
															FilesUtils.copyFile(oriFile,
																			new File(properties.getLocalReturnForCheck(), 
																					oriFile.getName()
																							.substring(0, oriFile.getName().length() - 4) + ".old.pdf"));
															// 記錄，以供發mail
															logger.info("迴歸測試"
																	+ pdfName
																	+ "和原本的檔案不同，請查看"
																	+ checkResultPdf.getAbsolutePath());
															ErrorReport err = new ErrorReport();
															err.setErrHappenTime(new Date());
															err.setErrorType("return check");
															err.setMessageBody("迴歸測試"
																	+ pdfName
																	+ "和原本的檔案不同，請查看"
																	+ checkResultPdf.getAbsolutePath());
															err.setReported(false);
															err.setException(true);
															err.setTitle("Return Check not OK");
															((VoService) Constant
																	.getContext()
																	.getBean(
																			"voServiceProxy"))
																	.save(err);
															outputTextWr.write("迴歸測試"
																	+ pdfName
																	+ "和原本的檔案不同，請查看"
																	+ checkResultPdf.getAbsolutePath() + "\r\n");
															outputTextWr.flush();
														} else {
															FileUtils
																	.forceDelete(new File(
																			properties
																					.getZipTmpPath(),
																			pdfName));
														}
													} else {
														FilesUtils
																.moveFileToDirectory(
																		new File(properties.getZipTmpPath(), pdfName),
																		localReturnPdfFolder,
																		true);
													}
												}
											}
										} catch (Exception e) {

											logger.error("", e);
											e.printStackTrace();
										} finally {
											if (pdfDoc != null)
												pdfDoc.close();
											if (rasFile != null
													&& rasFile.exists())
												FileUtils.forceDelete(rasFile);
											if (pdfFile != null
													&& pdfFile.exists())
												FilesUtils.moveFileToDirectory(pdfFile, new File(returnBackupFolder, "csv"), true);
												//FileUtils.forceDelete(pdfFile);
										}
									}
									FilesUtils.moveFileToDirectory(csvFile, new File(returnBackupFolder, "csv"), true);
									pdfFiles.remove(i);  //如果已經產生過就移除
									
									//更新DB
									returnTestFiles = properties.getReturnTestFiles().split(",");
									String returnTestFilesStr = "";
									for(String returnTestFile : returnTestFiles){
										if(!returnTestFile.toUpperCase().equals(newBatchName.toUpperCase())){
											returnTestFilesStr += returnTestFile + ",";
										}
									}
									properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
									if(!returnTestFilesStr.equals(""))
									   properties.setReturnTestFiles(returnTestFilesStr);
									else
									   properties.setReturnTestFiles(null);
									((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);
									
							   }//end of 
								//pdfFile != null && pdfFile.exists() && LockFile.checkFileIsReady(pdfFile)
								
								//到0時再重頭來一次
							    if(i == 0)
							       i = pdfFiles.size();
							    //每五秒檢查一次就行
							    Thread.sleep(5000);
							    
							   long now = new Date().getTime();
							   if(now - beginTime > 15L * 60 * 60 * 1000){
								   logger.info("超過十五小時未完成迴歸，跳出迴歸測試作業");
								   logger.info("迴歸測試結果有問題");
							       ErrorReport err = new ErrorReport();
							       err.setErrHappenTime(new Date());
							       err.setErrorType("return check");
							       err.setMessageBody(dailyTxt + originalTxt + Constant.yyyy_MM_ddHHMM.format(new Date()) + ":迴歸測試結果\r\n"
								    		+ "迴歸時間超過十五小時，請直接查閱log");
							       err.setReported(false);
							       err.setException(true);
							       err.setTitle("return check end");											    		
							       ((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);
							       ((VoService) Constant.getContext().getBean("voServiceProxy")).returnReport(); //寄出迴歸通知信
							       break;
							   }							   
							}
							   //如果都一樣，刪除batchlock及keepLock
							File [] compareFiles = new File(properties.getLocalReturnForCheck()).listFiles(FileFilterImpl.getCommonPdfFileFilter()); 
						    if( compareFiles == null || compareFiles.length == 0){
						       logger.info("迴歸測試結果無差別");
						       ErrorReport err = new ErrorReport();
						       err.setErrHappenTime(new Date());
						       err.setErrorType("return check");
						       err.setMessageBody(dailyTxt + originalTxt + Constant.yyyy_MM_ddHHMM.format(new Date()) + ":迴歸測試結果\r\n"
							    		+ "新契約：" + (normCounter + reptCounter) + "件\r\n"						    		
							    		+ "保補契轉：" + (reisCounter + convCounter) + "件\r\n"
							    		+ "無差別：作業正常");
						       err.setReported(false);
						       err.setTitle("return check end");											    		
						       ((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);
						       outputTextWr.write("迴歸比對無異常\r\n");
						       properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
						       properties.setReturnTestTxt(null);
						       properties.setReturnUnlock(true);
						       ((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);
						       ((VoService) Constant.getContext().getBean("voServiceProxy")).returnReport(); //寄出迴歸通知信						       					    	   
							   
							}else{
							   logger.info("迴歸測試結果有差別");
							   ErrorReport err = new ErrorReport();
							   err.setErrHappenTime(new Date());
							   err.setErrorType("return check");
							   err.setMessageBody(dailyTxt + originalTxt + Constant.yyyy_MM_ddHHMM.format(new Date()) + ":迴歸測試結果\r\n"
							    		+ "新契約：" + (normCounter + reptCounter) + "件\r\n"						    		
							    		+ "保補契轉：" + (reisCounter + convCounter) + "件\r\n"
							    		+ "比對結果：有差別，作業異常");
							   err.setReported(false);
							   err.setException(true);							   
							   err.setTitle("return check end");
							   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);							   
							   if(batchLock.exists())
							     FileUtils.forceDelete(keepLock);
							   FileWriter fw = new FileWriter(batchLock);
							   fw.write("keep.lock");
							   fw.flush();
							   fw.close();
							   ((VoService) Constant.getContext().getBean("voServiceProxy")).returnReport(); //寄出迴歸通知信
							}
						}
						if(outputTextWr != null){
							try {
								outputTextWr.flush();
								outputTextWr.close();
								properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
								properties.setReturnTestTxt(null);
								((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if(dailyTxtFile.exists()){
						   FileUtils.forceDelete(dailyTxtFile);
						}
					}else{
						
					}
			
				//}
			//}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
			ErrorReport er = new ErrorReport();
			er.setErrHappenTime(new Date());
			er.setErrorType("exception");
			er.setOldBatchName("");
			er.setReported(false);
			er.setException(true);
			er.setMessageBody("exception happen:" + e.getMessage());
			er.setTitle("exception happened");
			((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
		} finally {
			running = false;
		}

		logger.info("return fileDispatcher stop");
		
	}
	
	private static List<ApplyData> parseLog(File logFile) throws BeansException, RemoteException {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		List<ApplyData> applyDatas = new ArrayList<ApplyData>();
		String line = null;
		try {
			fis = new FileInputStream(logFile);
			isr = new InputStreamReader(fis, "ms950");
			br = new BufferedReader(isr);			
			int lineCounter = 0;
			while ((line = br.readLine()) != null) {
				if (lineCounter == 0) {
					// 第一行不用讀
					lineCounter++;
					continue;
				}
				lineCounter++;
				line = org.apache.commons.lang.StringUtils.trimToEmpty(line);
				String[] lineSplit = line.split(",");
				if ("".equals(line)
						|| (lineSplit.length < 25 )) {
					if(line.equals("")){
						continue;
					}else{
					// 格式錯誤，送出錯誤訊息
					   ErrorReport er = new ErrorReport();
					   er.setErrHappenTime(new Date());
					   er.setErrorType("presLogError");
					   er.setOldBatchName("");
					   er.setReported(false);
					   er.setMessageBody("log file:" + logFile.getName()
							   + " format error on parsing " + line);
					   er.setTitle("format error");
					   ((VoService) Constant.getContext()
							   .getBean("voServiceProxy")).save(er);
					   continue;
					}
				} else {
					// 處理日,行政中心別,保單號碼,補印次數,受理編號,明細類別,要保書影像處理狀態,文字檔處理狀態,合併處理狀態,掃描批次號碼,保單列印檔處理狀態,簽收回條處理狀態,轉檔日,保單頁數,轉檔失敗訊息,列印失敗訊息,封面頁數,A4頁數,DM頁數,封底頁數,錯誤代碼,AFP中起始頁數,AFP終止頁數,PDF檔名,
					// "
					// 0 ,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8 ,9 ,10 ,11 ,12 ,13 ,14 ,15 ,16, 17 , 18 ,19 ,20 ,21 , 22 ,23 , 24
					Date printTime = Constant.yyyy_MM_dd.parse(lineSplit[0]);
					
					String policyNo = lineSplit[2].trim();					
					Integer reprint = 0;
					try{
					   reprint = new Integer(lineSplit[3].trim());
					}catch(Exception e){
						
					}
					String applyNo = lineSplit[4].trim();
					String sourceCode = lineSplit[5].trim();
					String center = lineSplit[1];
					Integer totalPage = lineSplit[13].trim().equals("")? null :new Integer(lineSplit[13].trim());					
					Integer firstPage = lineSplit[16].trim().equals("")? null : new Integer(lineSplit[16].trim());
					Integer a4Page = lineSplit[17].trim().equals("")? null : new Integer(lineSplit[17].trim());
					Integer dmPage = lineSplit[18].trim().equals("")? null : new Integer(lineSplit[18].trim());
					Integer lastPage = lineSplit[19].trim().equals("")? null : new Integer(lineSplit[19].trim());
					String policyStatus = lineSplit[20].trim();
					Integer afpBeginPage = lineSplit[21].trim().equals("")? null : new Integer(lineSplit[21].trim());
					Integer afpEndPage = lineSplit[22].trim().equals("")? null : new Integer(lineSplit[22].trim());
					String pdfName = lineSplit[23].trim();
					String address = null;									
					String uniqueNo = null;
					if (lineSplit.length >= 25){
						address = lineSplit[24].trim();
					}else if(lineSplit.length >= 26){
						uniqueNo = lineSplit[25];
					}
					
                    ApplyData applyData = new ApplyData();
					applyData.setPrintTime(printTime);
					applyData.setCenter(center);
					applyData.setSourceCode(sourceCode);					
					applyData.setMegerOK(applyData.getMerger());
					applyData.setTotalPage(totalPage);
					if (!policyStatus.equals("0") && (policyStatus.equals("14") || policyStatus.equals("16")))
						applyData.setPolicyStatus(policyStatus);
					else if(!policyStatus.equals("0"))
						applyData.setPolicyStatus("16");
					else
						applyData.setPolicyStatus("17");
					applyData.setFirstPage(firstPage);
					applyData.setReprint(reprint);
					applyData.setApplyNo(applyNo);
					applyData.setPolicyNos(policyNo);
					applyData.setA4Page(a4Page);
					applyData.setDmPage(dmPage);
					applyData.setLastPage(lastPage);
					applyData.setAfpBeginPage(afpBeginPage);
					applyData.setAfpEndPage(afpEndPage);
					applyData.setAddress(address);
					applyData.setPolicyPDF(pdfName);
					applyData.setUniqueNo(uniqueNo);					
					applyDatas.add(applyData);
				}
			}

			return applyDatas;
		} catch (Exception e) {
			logger.error("", e);
			ErrorReport er = new ErrorReport();
			er.setErrHappenTime(new Date());
			er.setErrorType("exception");
			er.setOldBatchName("");
			er.setReported(false);
			er.setException(true);
			er.setMessageBody("exception happen at line:" + line + "\r\n error message:" + e.getMessage());
			er.setTitle("exception happened");
			((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
			return null;

		} finally {
			try {
				if (br != null)
					br.close();
				if (isr != null)
					isr.close();
				if (fis != null)
					fis.close();
			} catch (Exception e) {

			}
			br = null;
			isr = null;
			fis = null;
		}
	}
	
	public static void main(String [] args) throws BeansException, RemoteException{
        
			List<ApplyData> applyDatas = parseLog(new File("d:\\tmp\\SGAAB201406010001.csv"));
			System.out.println(applyDatas.size());
			if (applyDatas != null
					&& applyDatas.size() > 0) {
				logger.info("start to split pdf. total "
						+ applyDatas.size() + " pdfs.");
				PDDocument pdfDoc = null;
				File rasFile = null;
				RandomAccessFile ras = null;
				try {
					rasFile = new File(
							"D:/tmp/",
							UUID.randomUUID() + "");
					ras = new RandomAccessFile(rasFile,
							"rw");
					pdfDoc = PDDocument.load(new File("d:/tmp/SGAAB201406010001.pdf"),
							ras);

					// 開始進行比對前，先刪除裡面的所有檔案
					for (int i = applyDatas.size() - 1 ; i >= 0 ; i--) {
						ApplyData applyData = applyDatas.get(i);
						String pdfName = "SG" + applyData.getApplyNo()
								+ "_"
								+ applyData.getPolicyNos()
								+ "_"
								+ applyData.getReprint()
								+ ".pdf";
						
						int startPage = applyData
								.getAfpBeginPage();
						int endPage = applyData
								.getAfpEndPage();
						System.out.println("pdf name :"
								+ pdfName
								+ " | "
								+ "startPage:"
								+ startPage
								+ "| endPage:"
								+ endPage
								+ "| totalPage:"
								+ applyData
										.getTotalPage());
						if (endPage > 0
								&& startPage > 0
								&& endPage >= startPage) {
							
							PdfFileUtil.splitDocument(
											startPage,
											endPage,
											new File("d:/tmp/", pdfName).getAbsolutePath(),
											pdfDoc);
							
						}
					}
				
		    } catch (BeansException | COSVisitorException | IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
		   }
	   }
	}

}
