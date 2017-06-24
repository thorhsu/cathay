package com.fxdms.cathy.task;

import java.awt.image.RenderedImage;
import java.io.*;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;

import jcifs.smb.SmbFile;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;




import org.springframework.beans.BeansException;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FileFilterImpl;
import com.fxdms.util.FilesUtils;
import thor.util.JaiTiffImgProcess;
import com.fxdms.util.TiffChecker;
import com.fxdms.util.ZipUtils;
import com.itextpdf.text.Image;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.ImgFile;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

public class ImgUpdater {
	private static Date fileDate = null;

	static Logger logger = Logger.getLogger(ImgUpdater.class);	
	private static boolean running = false;
	private static boolean afterReqPolicyProcessed;

	public static boolean isAfterReqPolicyProcessed() {
		return afterReqPolicyProcessed;
	}

	public static void setAfterReqPolicyProcessed(boolean afterReqPolicyProcessed) {
		ImgUpdater.afterReqPolicyProcessed = afterReqPolicyProcessed;
	}

	public static boolean isRunning() {
		return running;
	}

	public static void startToRun() throws BeansException, RemoteException {
		Date today = new Date();
		File zipFolder = new File(Properties.getZipTmpPath());
		if(!zipFolder.exists())
			zipFolder.mkdirs();
		File unZipFolder = new File(zipFolder.getParent(), "unZip");
		if(!unZipFolder.exists())
			unZipFolder.mkdirs();
		logger.info("imgUpdater start to run");
		if (running) {
			logger.info("imgUpdater stop");
			return;
		}		
		running = true;
		
		
		//國壽人員下保單影像檔的目錄
		File imgFolder = new File(Properties.getLocalImgPath());
		logger.info(Properties.getLocalImgPath());
		if (!imgFolder.exists())
			imgFolder.mkdirs();
		File testImgFolder = new File(Properties.getLocalTestImgPath());
		logger.info(Properties.getLocalTestImgPath());
		if (!testImgFolder.exists())
			testImgFolder.mkdirs();

		File[] imgOkFile = new File(Properties.getLocalOKPath())
				.listFiles(FileFilterImpl.getImgOkFilesFilter());
		File[] testImgOkFile = new File(Properties.getLocalOKPath())
		      .listFiles(FileFilterImpl.getTestImgOkFilesFilter());
		// File [] lawOkFile = new
		// File(Properties.getLocalOKPath()).listFiles(FileFilterImpl.getLawOkFilesFilter());
		try {
			File[] centerFolders = imgFolder.listFiles(FileFilterImpl
					.getDirectoryFilter());
			logger.info("check img zip director");
			File[] imgFileZip1 = imgFolder.listFiles(FileFilterImpl
					.getImgZipFilesFilter());
			logger.info("check img zip files");

			// 如果當初定義的d:/dataIn/image/ 下頭還有目錄，就從裡面搬出來
			// 這是因為我對國壽人員的回答不信任的關係
			File centerF = null;
			if (centerFolders != null && imgOkFile != null && imgOkFile.length > 0) {				
				logger.info("check center folders:" + Constant.yyyy_MM_dd.format(InputdateParser.getInputDate()));				
				for (File centerFolder : centerFolders) {
                    if(centerFolder.listFiles() != null && centerFolder.listFiles().length > 0){
                       centerF = centerFolder;
                       InputdateParser.forceReadFile();	
                       File backupFolder = new File(Properties.getBackupFolder(),  "IMAGE/" + Constant.yyyy_MM_dd.format(InputdateParser.getInputDate()));
                       if(!backupFolder.exists()){
                    	   backupFolder.mkdirs();
                       }
                       if(new File(backupFolder, centerFolder.getName()).exists()){
                    	   FilesUtils.deleteDirectory(new File(backupFolder, centerFolder.getName()));
                       }
                       FilesUtils.copyDirectoryToDirectory(centerFolder, backupFolder);
                       File backupCenter = new File(backupFolder, centerFolder.getName());
                       if(backupCenter.exists()){                    	   
                    	   File[] files = backupCenter.listFiles(FileFilterImpl.getFileFilter());
                    	   if(files != null){
                    		   for(File file : files){
                    			   //如果是zip就解開
                    			   if(file.getName().toLowerCase().endsWith(".zip")){
                    				   ZipUtils.unzipFile(file, backupCenter, true);
                    				   File[] folders = backupCenter.listFiles(FileFilterImpl.getDirectoryFilter());
                    				   if(folders != null)
                    					   for(File folder : folders){
                    						   //如果是20140601或2014-06-01就把裡面的東西都copy出來
                    						   if(folder.getName().matches("2[0-1][0-9][0-9][0-1][0-9][0-3][0-9]") 
                    								   || folder.getName().matches("2[0-1][0-9][0-9]\\-[0-1][0-9]\\-[0-3][0-9]")){
                    							   File[] subFiles = folder.listFiles();
                    							   for(File subFile : subFiles){
                    							      FilesUtils.moveFileToDirectory(subFile, backupCenter, true);   
                    							   }
                    							   try{
                    							      FilesUtils.forceDelete(folder);
                    							   }catch(Exception e){
                    								   logger.error("", e);
                    							   }
                    						   }else if(folder.getName().toUpperCase().equals("META-INF")){
                    							   try{
                     							      FilesUtils.forceDelete(folder);
                     							   }catch(Exception e){
                     								   logger.error("", e);
                     							   }
                    						   }
                    					   }
                    				   try{
                    				      FileUtils.forceDelete(file);
                    				   }catch(Exception e){
                    					   logger.error("", e);
                    					   e.printStackTrace();
                    				   }
                    			   }
                    		   }
                    	   }
                       }

					   File zipFile = new File(imgFolder, centerFolder.getName() + ".zip");
					   if(zipFile.exists())
						   FileUtils.forceDelete(zipFile);					
					   ZipUtils.makeZip(centerFolder, zipFile);
					   File[] files = centerFolder.listFiles();
					   if(files != null){
						   for(File file : files){
							  FileUtils.forceDelete(file);		
						   }
					   }					
					   break;
                    }					
				}
			}
			logger.info("check img zip file");
			imgFileZip1 = imgFolder.listFiles(FileFilterImpl
					.getImgZipFilesFilter());
			Thread.sleep(5000);
			File[] imgFileZip2 = imgFolder.listFiles(FileFilterImpl
					.getImgZipFilesFilter());
			logger.info("check img zip files " + imgFileZip2.length);
			boolean normalImg = false;			
			if (imgFileZip1 != null && imgFileZip2 != null
					&& imgFileZip2.length > 0
					&& imgFileZip1[0].length() == imgFileZip2[0].length()) {
				normalImg = true;
			}
			
			//沒有正式檔時，檢查測試影像檔			
			if((imgOkFile == null  || imgOkFile.length == 0 ) && !normalImg){
			   logger.info("check test img zip file");
			   imgFileZip1 = testImgFolder.listFiles(FileFilterImpl
					.getImgZipFilesFilter());
			   Thread.sleep(5000);
			   imgFileZip2 = testImgFolder.listFiles(FileFilterImpl
					.getImgZipFilesFilter());
			   logger.info("check test img zip files " + imgFileZip2.length);
			}						
			boolean testImg = false;
			if (!normalImg && imgFileZip1 != null && imgFileZip2 != null
					&& imgFileZip2.length > 0
					&& imgFileZip1[0].length() == imgFileZip2[0].length() && testImgOkFile.length > 0) {
				testImg = true;				
			}
			if(testImgOkFile.length > 0 || imgOkFile.length > 0)
				fileDate = new Date();
			

			logger.info("check img zip file2");
			// 過了十秒，檔案數都相同，且檔案大小相同就開始進行
			if (((imgOkFile != null && imgOkFile.length > 0) || (testImgOkFile != null && testImgOkFile.length > 0)) 
					&& (normalImg || testImg)) {
				fileDate = new Date(imgOkFile[0].lastModified());
				logger.info("into zip folder");	
				// 01.zip ~ 06.zip
				// 解壓縮，不含目錄，直接解壓縮在imgFolder中
				if (normalImg)
					ZipUtils.unzipFile(imgFileZip2[0], unZipFolder, false);
				
				// 解壓後刪除				
				//FilesUtils.moveFileToDirectory(imgFileZip2[0], new File(
					//		Properties.getBackupFolder() + "/IMAGE/" + Constant.yyyyMMdd.format(today) ), true);
				FilesUtils.forceDelete(imgFileZip2[0]);
				logger.info("check img zip file3");

				File[] files = unZipFolder.listFiles(FileFilterImpl.getFileFilter());
				Date start = new Date();
            	ErrorReport eReport = new ErrorReport();
            	eReport.setErrHappenTime(new Date());
            	eReport.setErrorType("TifProcess");
            	eReport.setOldBatchName(null);
            	eReport.setReported(true);
				if(centerF != null)
					eReport.setMessageBody("處理" + centerF.getName() + "要保書影像檔:共 " + files.length + "個，約:" + (files.length * 7) + "秒可完成");
				else
					eReport.setMessageBody("處理" + imgFileZip2[0].getName() + "要保書影像檔:共 " + files.length + "個，約:" + (files.length * 7) + "秒可完成");
				eReport.setTitle("處理要保書影像檔");
				((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);

				for (File tifFile : files) {
					
					//如果tifFile只有 1K大，就直接copy到不處理pres的image path					
					if(tifFile.getName().toLowerCase().endsWith("end")){
						FileUtils.forceDelete(tifFile);
						continue;
					}else if(tifFile.getName().toLowerCase().endsWith("zip")){
						FilesUtils.moveFileToDirectory(tifFile, imgFolder, true);
						continue;
					}else if(!tifFile.getName().toLowerCase().endsWith(".n") && !tifFile.getName().toLowerCase().endsWith(".m")){
						FileUtils.forceDelete(tifFile);
						continue;
					}else if(tifFile.length() < 2048){                        					
						File destFile = new File(new File(Properties.getImgPostProcessedPath(), "image"), tifFile.getName() + ".tif");
						if(destFile.exists())
							FileUtils.forceDelete(destFile);
						
						FilesUtils.moveFile(tifFile, destFile);
						continue;
					}
						
					logger.info("into unzip files :" + tifFile.getName());
					String fileNm = tifFile.getName().toLowerCase() + ".tif";
					FileSeekableStream ss = null;
					try {
						ss = new FileSeekableStream(tifFile);
						ImageDecoder dec = ImageCodec.createImageDecoder(
									"tiff", ss, null);
						int count = dec.getNumPages();
						RenderedImage[] images = new RenderedImage[count];
						for (int i = 0; i < count; i++) {
							PlanarImage page = new RenderedImageAdapter(
									dec.decodeAsRenderedImage(i));
							page = JaiTiffImgProcess.cutImage(page, 10, 5);
							float width = page.getWidth();
							float height = page.getHeight();
							page = JaiTiffImgProcess
									.shrinkImage(
											(JaiTiffImgProcess.shrinkWidth / width),
											(JaiTiffImgProcess.shrinkHeight / height),
											page);
							int borderWidth = ((int) JaiTiffImgProcess.fixWidth - (int) JaiTiffImgProcess.shrinkWidth) / 2;
							int borderHeight = ((int) JaiTiffImgProcess.fixHeight - (int) JaiTiffImgProcess.shrinkHeight) / 2;
							page = JaiTiffImgProcess.doBorder(page,
									borderWidth, borderWidth, borderHeight,
									borderHeight, null);
							images[i] = page;
						}
						// 此處進來的放在pres影像的image目錄下
						File destFile = null;
						
						destFile = new File(new File(Properties.getImgPostProcessedPath(), "image"), tifFile.getName() + ".tif");
						// 如果存在就先刪除
						if (destFile.exists())
							FileUtils.forceDelete(destFile);
						// 存成多頁式600 dpi
						JaiTiffImgProcess.saveAsMultipageTIFF(images,
								destFile.getAbsolutePath());

						// 寫入資料庫
						List<ImgFile> imgFiles = null;
						/*
						imgFiles = session
									.getNamedQuery("ImgFile.findByFileNm")
									.setString(0, "/image/" + fileNm)
									.list();
						*/
						imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).getImgFilesByNm("/image/" + fileNm);
						ImgFile imgFile = null;
						if (imgFiles != null && imgFiles.size() > 0) {
							imgFile = imgFiles.get(0);
							imgFile.setNewCopy(false);
						} else {
							imgFile = new ImgFile();
							imgFile.setInsertDate(today);
							imgFile.setNewCopy(true);
						}
						
						imgFile.setFileNm("/image/" + fileNm);						
						imgFile.setReqPolicy(true);
						imgFile.setImage(false);
						imgFile.setLaw(false);
						imgFile.setCopyDate(today);
						imgFile.setCopySuccess(true);
						imgFile.setDpiX(JaiTiffImgProcess.DPI_X);
						imgFile.setDpiY(JaiTiffImgProcess.DPI_Y);
						imgFile.setErrorImage(false);
						imgFile.setExist(true);
						imgFile.setFileDate(new Date(tifFile.lastModified()));
						imgFile.setHeight(JaiTiffImgProcess.fixHeight + "");
						imgFile.setPath(tifFile.getAbsolutePath());
						imgFile.setLock(false);
						imgFile.setPostProcessedPath(destFile
								.getAbsolutePath());
						imgFile.setUpdateDate(today);
						imgFile.setWidth(JaiTiffImgProcess.fixWidth + "");
						if(imgFile.getNewCopy())
							((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
						else
							((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
						
						try {
							if (ss != null)
								ss.close();
						} catch (Exception e) {
							logger.error("", e);
						} finally {
							ss = null;
						}
						//FileUtils.forceDelete(tifFile);
						FilesUtils.forceDelete(tifFile);
						
					} catch (Exception e) {
						logger.error("", e);
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("tiffError");
						er.setOldBatchName(null);
						er.setReported(false);
						er.setException(true);
						er.setMessageBody("影像無法處理：" + tifFile.getName());
						er.setTitle("process img error");
						((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
						// 處理錯誤時更新資料庫
						List<ImgFile> imgFiles = null;
						
						imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).getImgFilesByNm("/image/" + fileNm);
						
						ImgFile imgFile = null;
						if (imgFiles != null && imgFiles.size() > 0) {
							imgFile = imgFiles.get(0);
							imgFile.setNewCopy(false);
						} else {
							imgFile = new ImgFile();
							imgFile.setInsertDate(today);
							imgFile.setNewCopy(true);
						}
						
						imgFile.setFileNm("/image/" + fileNm);						
						imgFile.setReqPolicy(true);
						imgFile.setImage(false);
						imgFile.setLaw(false);
						imgFile.setCopyDate(null);
						imgFile.setCopySuccess(null);
						imgFile.setDpiX(null);
						imgFile.setDpiY(null);
						imgFile.setErrorImage(true);
						imgFile.setExist(true);
						imgFile.setFileDate(new Date(tifFile.lastModified()));
						imgFile.setHeight(null);
						imgFile.setPath(tifFile.getAbsolutePath());
						imgFile.setLock(false);
						imgFile.setPostProcessedPath(null);
						imgFile.setUpdateDate(today);
						imgFile.setWidth(null);
						if(imgFile.getNewCopy())
							((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
						else
							((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
						try {
							if (ss != null )
								ss.close();
						} catch (Exception e2) {
							logger.error("", e);
						} finally {
							ss = null;
						}
						//FileUtils.forceDelete(tifFile);
						FilesUtils.moveFileToDirectory(tifFile, new File(
								Properties.getBackupFolder(), "IMAGE/" + Constant.yyyy_MM_dd.format(InputdateParser.getInputDate()) + "/error/" ), true);
						continue;
					} finally {
						
					}
				}
				Date end = new Date();
				eReport = new ErrorReport();
            	eReport.setErrHappenTime(new Date());
            	eReport.setErrorType("TifProcess");
            	eReport.setOldBatchName(null);
            	eReport.setReported(true);
				if(centerF != null)
					eReport.setMessageBody("處理" + centerF.getName() + "要保書影像檔:共 " + files.length + "個，已處理完成，耗時 " + ((end.getTime() - start.getTime()) / 1000) + "秒");
				else
					eReport.setMessageBody("處理" + imgFileZip2[0].getName() + "要保書影像檔:共 " + files.length + "個，已處理完成，耗時 " + ((end.getTime() - start.getTime()) / 1000) + "秒");
				eReport.setTitle("處理要保書影像檔");
				((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
				
				logger.info("end check img zip file");
				if(!testImg ){				   
				   boolean filesZero = false;
				   boolean folderZero = true;
				   if(imgFolder.listFiles(FileFilterImpl.getFileFilter()) == null || imgFolder.listFiles(FileFilterImpl.getFileFilter()).length == 0){
					   filesZero = true;
				   }
				   if(imgFolder.listFiles(FileFilterImpl.getDirectoryFilter()) == null || imgFolder.listFiles(FileFilterImpl.getDirectoryFilter()).length == 0){
					   
				   }else{
					   for(File folder : imgFolder.listFiles(FileFilterImpl.getDirectoryFilter())){
						   if(folder.list() != null && folder.list().length > 0){
							   folderZero = false;
							   break;
						   }
					   }
				   }
				   if(filesZero && folderZero)
				      afterReqPolicyProcessed = true;
				   /*
				    * 國壽說法一變再變，刪除image.ok目前改由FileDispatcher去進行
				    */
				   //if(filesZero && folderZero)
				     // FileUtils.forceDelete(new File(Properties.getLocalOKPath(),
						//      "IMAGE.OK")); // 全部做完後把IMAGE.OK刪除
				}else if(testImg && testImgFolder.list() != null && testImgFolder.list().length == 0){
					 boolean filesZero = false;
					 boolean folderZero = true;
					 if(testImgFolder.listFiles(FileFilterImpl.getFileFilter()) == null || testImgFolder.listFiles(FileFilterImpl.getFileFilter()).length == 0){
					   filesZero = true;
					 }
					 if(testImgFolder.listFiles(FileFilterImpl.getDirectoryFilter()) == null || testImgFolder.listFiles(FileFilterImpl.getDirectoryFilter()).length == 0){
					   
					 }else{
					   for(File folder : testImgFolder.listFiles(FileFilterImpl.getDirectoryFilter())){
						   if(folder.list() != null && folder.list().length > 0){
							   folderZero = false;
							   break;
						   }
					   }
					 }
					//if(filesZero && folderZero)	
					//FileUtils.forceDelete(new File(Properties.getLocalOKPath(),							
							   //"TEST_IMAGE.OK")); // 全部做完後把IMAGE.OK刪除
				}
			}// end of if(imgOkFile != null && imgOkFile.length > 0 &&
			
			
			
			//pres在進行中時不進行遠端影像檔更新			
			File presFolder = new File(Properties.getPresPath());
			File afpFolder = new File(Properties.getAfpPath());
			File[] txtFiles = presFolder.listFiles(FileFilterImpl.getFileFilter());
			File[] afpFiles = afpFolder.listFiles(FileFilterImpl.getAfpFileFilter());
			if(txtFiles != null && afpFiles != null && (txtFiles.length > 0 || afpFiles.length > 0)){			
				logger.info("imgUpdater stopped because of PRES running ");
				running = false;
				return;
			}

			logger.info("get smbFiles get");						
			//先檢查一般影像檔
			SmbFile[] remoteFiles = FilesUtils
					.getFileList(null, Properties.getImgServer(),
							Properties.getImgServerUser(),
							Properties.getImgServerPwd(),
							Properties.getImgPath(), null);
			
			if(remoteFiles != null)
			   logger.info("remoteFiles:" + remoteFiles.length);
			else
			   logger.info("can't find folder" );
			
			if(remoteFiles == null)
				remoteFiles = new SmbFile[0];
			Map<SmbFile, ImgFile> imgs4Transfer = new HashMap<SmbFile, ImgFile>();
			List<ImgFile> imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).findNormImage();
			List<ImgFile> lawFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).findNormLaw();

			// 檢查image file是不是已被copy過來
			for (SmbFile smbFile : remoteFiles) {
				String name = smbFile.getName().toLowerCase(); // 轉成小寫方便比對
				ImgFile imgFile = null;
				//檢查資料庫是不是有這筆資料
				if (imgFiles != null) {
					for (ImgFile imgfile : imgFiles) {
						if (imgfile.getFileNm().toLowerCase().equals("/image/" + name)) {
							imgFile = imgfile;
							break;
						}
					}
				}
				if (imgFile != null) {
					// 時間或檔案長度不同，也要update一份過來
					if (imgFile.getExist() == null || !imgFile.getExist() || (imgFile.getErrorImage() != null && imgFile.getErrorImage())  
							|| Math.abs(smbFile.lastModified() - imgFile.getFileDate().getTime()) > 5000
							|| smbFile.length() != imgFile.getLength()) {
						if(imgFile.getFileDate() != null)
						   logger.info("file :" + smbFile.lastModified() + " | DB: " + imgFile.getFileDate().getTime());
						if(imgFile.getLength() != null)
						   logger.info("smbFil length :" + smbFile.length() + " | DB: " + imgFile.getLength());
						imgFile.setNewCopy(false);
						imgFile.setCopyDate(new Date());
						imgFile.setCopySuccess(true);
						imgFile.setErrorImage(false);
						imgFile.setExist(true);
						imgFile.setFileDate(new Date(smbFile.lastModified()));
						imgFile.setLength(smbFile.length());
						imgFile.setImage(true);
						imgFile.setLaw(false);
						imgFile.setLock(false);
						imgFile.setPath(Properties.getImgServer() + "/image/"
								+ smbFile.getName());
						imgFile.setPostProcessedPath(Properties
								.getImgPostProcessedPath()
								+ "image/"
								+ smbFile.getName());
						imgFile.setReqPolicy(false);
						imgFile.setUpdateDate(new Date());
						imgs4Transfer.put(smbFile, imgFile);
					}

				} else {
					imgFile = new ImgFile();
					imgFile.setCopyDate(new Date());
					imgFile.setCopySuccess(true);
					imgFile.setErrorImage(false);
					imgFile.setExist(true);
					imgFile.setFileDate(new Date(smbFile.lastModified()));
					imgFile.setFileNm("/image/" + smbFile.getName());
					imgFile.setLength(smbFile.length());
					imgFile.setImage(true);
					imgFile.setInsertDate(new Date());
					imgFile.setLaw(false);
					imgFile.setReqPolicy(false);
					imgFile.setLock(false);
					imgFile.setNewCopy(true);
					imgFile.setPath(Properties.getImgServer() + "/image/"
							+ smbFile.getName());
					imgFile.setPostProcessedPath(Properties
							.getImgPostProcessedPath()
							+ "image/"
							+ smbFile.getName());
					imgFile.setReqPolicy(false);
					imgFile.setUpdateDate(new Date());
					imgs4Transfer.put(smbFile, imgFile);
				}
				File imageFolder = new File(Properties.getImgPostProcessedPath(), "Image");
				if(imgs4Transfer.get(smbFile) == null 
						&& !new File(imageFolder, smbFile.getName()).exists()){
					logger.info(smbFile.getName() + " file not exist. Copy from remote site.");
					imgs4Transfer.put(smbFile, imgFile);
				}

			}// for(SmbFile smbFile : remoteFiles){
			
			remoteFiles = FilesUtils
					.getFileList(null, Properties.getImgServer(),
							Properties.getImgServerUser(),
							Properties.getImgServerPwd(),
							Properties.getLawPath(), null);
			if(remoteFiles != null)
			   logger.info("remoteFiles:" + remoteFiles.length);
			else
			   logger.info("can't find folder" );
			
			if(remoteFiles == null)
				remoteFiles = new SmbFile[0];
			// 檢查law file是不是已被copy過來
			for (SmbFile smbFile : remoteFiles) {
				String name = smbFile.getName().toLowerCase(); // 轉成小寫方便比對
				ImgFile imgFile = null;
				if (lawFiles != null) {
					for (ImgFile imgfile : lawFiles) {
						if (imgfile.getFileNm().toLowerCase().equals("/law/" + name)) {
							imgFile = imgfile;
							break;
						}
					}
				}
				if (imgFile != null) {
					// 時間或檔案長度不同，也要copy一份過來
					if (imgFile.getExist() == null || !imgFile.getExist() || (imgFile.getErrorImage() != null && imgFile.getErrorImage())  
							|| Math.abs(smbFile.lastModified() - imgFile.getFileDate().getTime()) > 5000
							|| smbFile.length() != imgFile.getLength()) {
						if(imgFile.getFileDate() != null)
						   logger.info("file :" + smbFile.lastModified() + " | DB: " + imgFile.getFileDate().getTime());
						if(imgFile.getLength() != null)
						   logger.info("smbFil length :" + smbFile.length() + " | DB: " + imgFile.getLength());
						imgFile.setNewCopy(false);
						imgFile.setCopyDate(new Date());
						imgFile.setCopySuccess(true);
						imgFile.setErrorImage(false);
						imgFile.setExist(true);
						imgFile.setFileDate(new Date(smbFile.lastModified()));
						imgFile.setFileNm("/law/" + smbFile.getName());
						imgFile.setLength(smbFile.length());
						imgFile.setImage(false);
						imgFile.setLaw(true);
						imgFile.setReqPolicy(false);
						imgFile.setLock(false);
						imgFile.setPath(Properties.getImgServer() + "/law/"
								+ smbFile.getName());
						imgFile.setPostProcessedPath(Properties
								.getImgPostProcessedPath()
								+ "law/"
								+ smbFile.getName());
						imgFile.setReqPolicy(false);
						imgFile.setUpdateDate(new Date());
						imgs4Transfer.put(smbFile, imgFile);
					}

				} else {
					//如果沒有，新增一份
					imgFile = new ImgFile();
					imgFile.setCopyDate(new Date());
					imgFile.setCopySuccess(true);
					imgFile.setErrorImage(false);
					imgFile.setExist(true);
					imgFile.setFileDate(new Date(smbFile.lastModified()));
					imgFile.setFileNm("/law/" + smbFile.getName());
					imgFile.setLength(smbFile.length());
					imgFile.setImage(false);
					imgFile.setInsertDate(new Date());
					imgFile.setLaw(true);
					imgFile.setReqPolicy(false);
					imgFile.setLock(false);
					imgFile.setNewCopy(true);
					imgFile.setPath( Properties.getImgServer() + "/law/"
							+ smbFile.getName());
					imgFile.setPostProcessedPath(Properties
							.getImgPostProcessedPath()
							+ "law/"
							+ smbFile.getName());
					imgFile.setReqPolicy(false);
					imgFile.setUpdateDate(new Date());
					imgs4Transfer.put(smbFile, imgFile);
				}
				File lawFolder = new File(Properties.getImgPostProcessedPath(), "Law");
				if(imgs4Transfer.get(smbFile) == null 
						&& !new File(lawFolder, smbFile.getName()).exists()){
					logger.info(smbFile.getName() + " file not exist. Copy from remote site.");
					imgs4Transfer.put(smbFile, imgFile);
				}

			}// for(SmbFile smbFile : remoteFiles){

			if (imgs4Transfer.size() > 0) {
				Set<SmbFile> forTranFiles = imgs4Transfer.keySet();
				for (SmbFile tifFile : forTranFiles) {
					//每完成一筆commit一次
					String fileNm = tifFile.getName().toLowerCase();
					if (!fileNm.endsWith(".tif") && !fileNm.endsWith(".tiff") ) {
						//如果是pdf就直接copy過去
						if(fileNm.endsWith(".pdf")){
							ImgFile imgFile = null;
							try {
								imgFile = imgs4Transfer.get(tifFile);							
								logger.info(imgFile.getFileNm() + ":" + imgFile.getNewCopy());
								String subDir = null;
								if(imgFile.getImage() != null && imgFile.getImage())
									subDir = "image";
								else if(imgFile.getLaw() != null && imgFile.getLaw())
									subDir = "law";
	                            File subFolder = new File(Properties.getImgPostProcessedPath(), subDir);
								if(!subFolder.exists())
									subFolder.mkdirs();
								
								FilesUtils.copySmbFileToDirectory(tifFile, subFolder);																
								if(imgFile.getNewCopy() != null && imgFile.getNewCopy()){
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
									
								}else{
									((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
									
								}

							} catch (Exception e) {
								logger.error("", e);
								ErrorReport er = new ErrorReport();
								er.setErrHappenTime(new Date());
								er.setErrorType("tiffError");
								er.setOldBatchName(null);
								er.setReported(false);
								er.setException(true);
								er.setMessageBody("影像無法搬移：" + imgFile.getPath());
								er.setTitle("process img error");
								((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);

								// 處理錯誤時更新資料庫																												
								imgFile.setCopyDate(null);
								imgFile.setPostProcessedPath(null);
								imgFile.setErrorImage(true);
								imgFile.setUpdateDate(new Date());
								if(imgFile.getNewCopy() != null && imgFile.getNewCopy())
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
								else
									((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
								continue;
							} finally {

							}
						}
						continue;
					} else {
						ImgFile imgFile = null;
						try {
							// 條款檔不處理，直接搬過去
							imgFile = imgs4Transfer.get(tifFile);							
							logger.info(imgFile.getFileNm() + ":" + imgFile.getNewCopy());
							String subDir = null;
							if(imgFile.getImage() != null && imgFile.getImage())
								subDir = "image";
							else if(imgFile.getLaw() != null && imgFile.getLaw())
								subDir = "law";
                            File subFolder = new File(Properties.getImgPostProcessedPath(), subDir);
							if(!subFolder.exists())
								subFolder.mkdirs();
							
							FilesUtils.copySmbFileToDirectory(tifFile, subFolder);
							// 影像檔改為600DPI
							if (imgFile.getImage() != null
									&& imgFile.getImage()) {
								File oriTiffFile = new File(subFolder,
										tifFile.getName());
								long lastModified = 0;
								if(oriTiffFile.exists())
									oriTiffFile.lastModified();
								File tmpTiffFile = null;
								try {
									Image image = TiffChecker
											.getImage(oriTiffFile
													.getAbsolutePath());
									// 如果是300dpi，改存為600dpi
									if (image.getDpiX() == 300
											|| image.getDpiY() == 300) {
										image = null;
										
										tmpTiffFile = new File(subFolder,
												tifFile.getName() + ".tmp");
										JaiTiffImgProcess.processImg(
												oriTiffFile, tmpTiffFile);

										if (oriTiffFile.exists())
											FileUtils.forceDelete(oriTiffFile);
										tmpTiffFile.renameTo(oriTiffFile);
										if(lastModified != 0)
										   oriTiffFile.setLastModified(lastModified);
									}
								} catch (Exception e) {
									logger.error("", e);
									e.printStackTrace();
									logger.error("", e);
									ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("tiffError");
									er.setOldBatchName(null);
									er.setReported(false);
									er.setException(true);
									er.setMessageBody("影像無法搬移：" + imgFile.getPath());
									er.setTitle("process img error");
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
									
								}finally{
									if(tmpTiffFile != null && tmpTiffFile.exists())
										   FileUtils.forceDelete(tmpTiffFile);
								}
							}
							
							
							if(imgFile.getNewCopy() != null && imgFile.getNewCopy()){
								((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
								
							}else{
								((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
								
							}

						} catch (Exception e) {
							logger.error("", e);
							ErrorReport er = new ErrorReport();
							er.setErrHappenTime(new Date());
							er.setErrorType("tiffError");
							er.setOldBatchName(null);
							er.setReported(false);
							er.setException(true);
							er.setMessageBody("影像無法搬移：" + imgFile.getPath());
							er.setTitle("process img error");
							((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);

							// 處理錯誤時更新資料庫																												
							imgFile.setCopyDate(null);
							imgFile.setPostProcessedPath(null);
							imgFile.setErrorImage(true);
							imgFile.setUpdateDate(new Date());
							if(imgFile.getNewCopy() != null && imgFile.getNewCopy())
								((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
							else
								((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
							continue;
						} finally {

						}
					}
				}				
			}
			
			/*
			 * 以下為測試影像檔部分
			 */
			imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).findTestImage();
			lawFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).findTestLaw();		
			
			remoteFiles = FilesUtils.getFileList(null, Properties.getImgServer(),
							Properties.getImgServerUser(),
							Properties.getImgServerPwd(),
							Properties.getTestImgPath(), null);
			
			if(remoteFiles == null)
				remoteFiles = new SmbFile[0];
			imgs4Transfer = new HashMap<SmbFile, ImgFile>();
			

			// 檢查image file是不是已被copy過來
			for (SmbFile smbFile : remoteFiles) {
				String name = smbFile.getName().toLowerCase(); // 轉成小寫方便比對
				ImgFile imgFile = null;
				//檢查資料庫是不是有這筆資料
				if (imgFiles != null) {
					for (ImgFile imgfile : imgFiles) {
						if (imgfile.getFileNm().toLowerCase().equals("/test/image/" + name)) {
							imgFile = imgfile;
							break;
						}
					}
				}
				if (imgFile != null) {
					// 時間或檔案長度不同，也要update一份過來
					if (Math.abs(smbFile.lastModified() - imgFile.getFileDate().getTime()) > 5000
							|| smbFile.length() != imgFile.getLength()) {
						logger.info("file :" + smbFile.lastModified() + " | DB: " + imgFile.getFileDate().getTime());
						logger.info("smbFil length :" + smbFile.length() + " | DB: " + imgFile.getLength());
						imgFile.setNewCopy(false);
						imgFile.setCopyDate(new Date());
						imgFile.setCopySuccess(true);
						imgFile.setErrorImage(false);
						imgFile.setExist(true);
						imgFile.setFileDate(new Date(smbFile.lastModified()));
						imgFile.setLength(smbFile.length());
						imgFile.setImage(true);
						imgFile.setLaw(false);
						imgFile.setLock(false);
						imgFile.setPath(Properties.getImgServer() + "/test/image/"
								+ smbFile.getName());
						imgFile.setPostProcessedPath(Properties
								.getImgPostProcessedPath()
								+ "/test/image/"
								+ smbFile.getName());
						imgFile.setReqPolicy(false);
						imgFile.setUpdateDate(new Date());
						imgs4Transfer.put(smbFile, imgFile);
					}

				} else {
					imgFile = new ImgFile();
					imgFile.setCopyDate(new Date());
					imgFile.setCopySuccess(true);
					imgFile.setErrorImage(false);
					imgFile.setExist(true);
					imgFile.setFileDate(new Date(smbFile.lastModified()));
					imgFile.setFileNm("/test/image/" + smbFile.getName());
					imgFile.setLength(smbFile.length());
					imgFile.setImage(true);
					imgFile.setInsertDate(new Date());
					imgFile.setLaw(false);
					imgFile.setReqPolicy(false);
					imgFile.setLock(false);
					imgFile.setNewCopy(true);
					imgFile.setPath(Properties.getImgServer() + "/test/image/"
							+ smbFile.getName());
					imgFile.setPostProcessedPath(Properties
							.getImgPostProcessedPath()
							+ "/test/image/"
							+ smbFile.getName());
					imgFile.setReqPolicy(false);
					imgFile.setUpdateDate(new Date());
					imgs4Transfer.put(smbFile, imgFile);
				}

			}// for(SmbFile smbFile : remoteFiles){
			
			remoteFiles = FilesUtils
					.getFileList(null, Properties.getImgServer(),
							Properties.getImgServerUser(),
							Properties.getImgServerPwd(),
							Properties.getTestLawPath(), null);
			if(remoteFiles == null)
				remoteFiles = new SmbFile[0];
			// 檢查law file是不是已被copy過來
			for (SmbFile smbFile : remoteFiles) {
				String name = smbFile.getName().toLowerCase(); // 轉成小寫方便比對
				ImgFile imgFile = null;
				if (lawFiles != null) {
					for (ImgFile imgfile : lawFiles) {
						if (imgfile.getFileNm().toLowerCase().equals("/test/law/" + name)) {
							imgFile = imgfile;
							break;
						}
					}
				}
				if (imgFile != null) {
					// 時間或檔案長度不同，也要copy一份過來
					if (Math.abs(smbFile.lastModified() - imgFile.getFileDate().getTime()) > 5000
							|| smbFile.length() != imgFile.getLength()) {
						logger.info("file :" + smbFile.lastModified() + " | DB: " + imgFile.getFileDate().getTime());
						logger.info("smbFil length :" + smbFile.length() + " | DB: " + imgFile.getLength());
						imgFile.setNewCopy(false);
						imgFile.setCopyDate(new Date());
						imgFile.setCopySuccess(true);
						imgFile.setErrorImage(false);
						imgFile.setExist(true);
						imgFile.setFileDate(new Date(smbFile.lastModified()));
						imgFile.setFileNm("/test/law/" + smbFile.getName());
						imgFile.setLength(smbFile.length());
						imgFile.setImage(false);
						imgFile.setLaw(true);
						imgFile.setReqPolicy(false);
						imgFile.setLock(false);
						imgFile.setPath(Properties.getImgServer() + "/test/law/"
								+ smbFile.getName());
						imgFile.setPostProcessedPath(Properties
								.getImgPostProcessedPath()
								+ "/test/law/"
								+ smbFile.getName());
						imgFile.setReqPolicy(false);
						imgFile.setUpdateDate(new Date());
						imgs4Transfer.put(smbFile, imgFile);
					}

				} else {
					//如果沒有，新增一份
					imgFile = new ImgFile();
					imgFile.setCopyDate(new Date());
					imgFile.setCopySuccess(true);
					imgFile.setErrorImage(false);
					imgFile.setExist(true);
					imgFile.setFileDate(new Date(smbFile.lastModified()));
					imgFile.setFileNm("/test/law/" + smbFile.getName());
					imgFile.setLength(smbFile.length());
					imgFile.setImage(false);
					imgFile.setInsertDate(new Date());
					imgFile.setLaw(true);
					imgFile.setReqPolicy(false);
					imgFile.setLock(false);
					imgFile.setNewCopy(true);
					imgFile.setPath( Properties.getImgServer() + "/test/law/"
							+ smbFile.getName());
					imgFile.setPostProcessedPath(Properties
							.getImgPostProcessedPath()
							+ "/test/law/"
							+ smbFile.getName());
					imgFile.setReqPolicy(false);
					imgFile.setUpdateDate(new Date());
					imgs4Transfer.put(smbFile, imgFile);
				}

			}// for(SmbFile smbFile : remoteFiles){

			if (imgs4Transfer.size() > 0) {
				Set<SmbFile> forTranFiles = imgs4Transfer.keySet();
				for (SmbFile tifFile : forTranFiles) {
					//每完成一筆commit一次
					String fileNm = tifFile.getName().toLowerCase();
					if (!fileNm.endsWith(".tif") && !fileNm.endsWith(".tiff") ) {
						//如果是pdf就直接copy過去
						if(fileNm.endsWith(".pdf")){
							ImgFile imgFile = null;
							try {
								// 條款檔不處理，直接搬過去
								imgFile = imgs4Transfer.get(tifFile);
								logger.info(imgFile.getFileNm() + ":" + imgFile.getNewCopy());
								String subDir = null;
								if(imgFile.getImage() != null && imgFile.getImage())
									subDir = "/test/image";
								else if(imgFile.getLaw() != null && imgFile.getLaw())
									subDir = "/test/law";
	                            File subFolder = new File(Properties.getImgPostProcessedPath(), subDir);
								if(!subFolder.exists())
									subFolder.mkdirs();
								
								FilesUtils.copySmbFileToDirectory(tifFile, subFolder);
								if(imgFile.getNewCopy() != null && imgFile.getNewCopy()){
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
									
								}else{
									((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
									
								}

							} catch (Exception e) {
								logger.error("", e);
								ErrorReport er = new ErrorReport();
								er.setErrHappenTime(new Date());
								er.setErrorType("tiffError");
								er.setOldBatchName(null);
								er.setReported(false);
								er.setException(true);
								er.setMessageBody("影像無法搬移：" + imgFile.getPath());
								er.setTitle("process img error");
								((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);

								// 處理錯誤時更新資料庫																												
								imgFile.setCopyDate(null);
								imgFile.setCopySuccess(false);
								imgFile.setPostProcessedPath(null);
								imgFile.setErrorImage(true);
								imgFile.setUpdateDate(new Date());
								if(imgFile.getNewCopy() != null && imgFile.getNewCopy())
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
								else
									((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
								continue;
							} finally {

							}
						}
						continue;
					} else {
						ImgFile imgFile = null;
						try {
							// 條款檔不處理，直接搬過去
							imgFile = imgs4Transfer.get(tifFile);
							logger.info(imgFile.getFileNm() + ":" + imgFile.getNewCopy());
							String subDir = null;
							if(imgFile.getImage() != null && imgFile.getImage())
								subDir = "/test/image";
							else if(imgFile.getLaw() != null && imgFile.getLaw())
								subDir = "/test/law";
                            File subFolder = new File(Properties.getImgPostProcessedPath(), subDir);
							if(!subFolder.exists())
								subFolder.mkdirs();
							
							FilesUtils.copySmbFileToDirectory(tifFile, subFolder);
							if(imgFile.getNewCopy() != null && imgFile.getNewCopy()){
								((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
								
							}else{
								((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
								
							}

						} catch (Exception e) {
							logger.error("", e);
							ErrorReport er = new ErrorReport();
							er.setErrHappenTime(new Date());
							er.setErrorType("tiffError");
							er.setOldBatchName(null);
							er.setReported(false);
							er.setException(true);
							er.setMessageBody("影像無法搬移：" + imgFile.getPath());
							er.setTitle("process img error");
							((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);

							// 處理錯誤時更新資料庫																												
							imgFile.setCopyDate(null);
							imgFile.setCopySuccess(false);
							imgFile.setPostProcessedPath(null);
							imgFile.setErrorImage(true);
							imgFile.setUpdateDate(new Date());
							if(imgFile.getNewCopy() != null && imgFile.getNewCopy())
								((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
							else
								((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
							continue;
						} finally {

						}
					}
				}				
			} 
			
			


		} catch (Exception e) {
			logger.error("", e);
			ErrorReport er = new ErrorReport();
			er.setErrHappenTime(new Date());
			er.setErrorType("exception");
			er.setOldBatchName(null);
			er.setReported(false);
			er.setException(true);
			er.setMessageBody("exception happen:" + e.getMessage());
			er.setTitle("exception happened");
			
			((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
			
		} finally {
			running = false;			

		}
		running = false;
		logger.info("imgUpdater stop.");
	}

	public static Date getFileDate() {
		return fileDate;
	}
	
}
