package com.fxdms.cathy.task;

import java.awt.image.RenderedImage;
import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class GpImgUpdater {
	private static Date fileDate = null;
    private static boolean tifProcessed = false;
	static Logger logger = Logger.getLogger(GpImgUpdater.class);	
	private static boolean running = false;
	public static boolean isRunning() {
		return running;
	}

	public static void startToRun() throws BeansException, RemoteException {
		Date today = new Date();
		
		logger.info("Group ImgUpdater start to run");
		if (running) {
			logger.info("Another thread running, this Group imgUpdater stopped");
			return;
		}		
		
		
		
		//國壽人員下團險保單影像檔的目錄D:\group_dataIN\IMAGE		
		File imgFolder = new File(Properties.getGroupInFolder(), "IMAGE");
		if (!imgFolder.exists())
			imgFolder.mkdirs();
		//國壽人員下團險測試保單影像檔的目錄D:\group_dataIN\TEST_IMAGE
		File testImgFolder = new File(Properties.getGroupInFolder(), "TEST_IMAGE");		
		if (!testImgFolder.exists())
			testImgFolder.mkdirs();

		File imgOkFile = new File(new File(Properties.getGroupInFolder(), "OK"), "IMAGE.ok");
		//File testImgOkFile = new File(new File(Properties.getGroupInFolder(), "OK"), "TEST_IMAGE.ok");
		
		try {
			running = true;
			File[] centerFolders = imgFolder.listFiles(FileFilterImpl
					.getDirectoryFilter());
			logger.info("check group img director");

			boolean beginProcessed = false;
			if (centerFolders != null && imgOkFile != null && imgOkFile.exists()) {
				beginProcessed = true;
			}
			if(beginProcessed){
				List<File> tiffFiles = new ArrayList<File>();
				GroupInputdateParser.forceReadFile();
				logger.info("check center folders:" + Constant.yyyy_MM_dd.format(GroupInputdateParser.getGpInputDate()));				
				for (File centerFolder : centerFolders) {
                    if(centerFolder.listFiles() != null && centerFolder.listFiles().length > 0){
                       
                       GroupInputdateParser.forceReadFile();	
                       File backupFolder = new File(Properties.getGpBackupFolder(),  "IMAGE/" + Constant.yyyy_MM_dd.format(GroupInputdateParser.getGpInputDate()));
                       if(!backupFolder.exists()){
                    	   backupFolder.mkdirs();
                       }
                       FilesUtils.copyDirectory(centerFolder, backupFolder, true);
                       File[] tifFiles = centerFolder.listFiles(FileFilterImpl.getTiffFilesFilter());
                       List<File> files = Arrays.asList(tifFiles);
                       tiffFiles.addAll(files);
                    }					
				}
				Date start = new Date();
            	ErrorReport eReport = new ErrorReport();
            	eReport.setErrHappenTime(new Date());
            	eReport.setErrorType("TifProcess");
            	eReport.setOldBatchName(null);
            	eReport.setReported(true);			
				eReport.setMessageBody("處理團險要保書影像檔:共 " + tiffFiles.size() + "個，約:" + (tiffFiles.size() * 7) + "秒可完成");						
				eReport.setTitle("處理要保書影像檔");
				((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
				logger.info("begin processing "+ tiffFiles.size() + "tiffFiles");
				File postProcessedFolder = new File(Properties.getImgPostProcessedPath(), "group");
				postProcessedFolder = new File(postProcessedFolder, "image");
				//D:/kotai/group/2014-10-04/
				if(!postProcessedFolder.exists())
					postProcessedFolder.mkdirs();
				for(File tifFile : tiffFiles){
					String fileNm = tifFile.getName().toLowerCase();
					//如果tifFile只有 1K大，就直接copy到不處理pres的image path
					File destFile = new File(postProcessedFolder, fileNm);
					String dbFileNm = destFile.getAbsolutePath();
					int index = dbFileNm.indexOf("group");
					dbFileNm = "/" + (dbFileNm.substring(index).replaceAll("\\\\", "/"));					
					if(tifFile.length() < 2048){                        											
						if(destFile.exists())
							FileUtils.forceDelete(destFile);
						
						List<ImgFile> imgFiles = null;
						imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).getImgFilesByNm(dbFileNm);
						ImgFile imgFile = null;
						if (imgFiles != null && imgFiles.size() > 0) {
							imgFile = imgFiles.get(0);
							imgFile.setNewCopy(false);
						} else {
							imgFile = new ImgFile();
							imgFile.setInsertDate(today);
							imgFile.setNewCopy(true);
						}
						
						imgFile.setFileNm(dbFileNm.toLowerCase());						
						imgFile.setReqPolicy(true);
						imgFile.setImage(false);
						imgFile.setLaw(false);
						imgFile.setCopyDate(today);
						imgFile.setCopySuccess(true);
						imgFile.setDpiX(0);
						imgFile.setDpiY(0);
						imgFile.setErrorImage(true);
						imgFile.setExist(true);
						imgFile.setFileDate(new Date(tifFile.lastModified()));
						imgFile.setHeight(0 + "");
						imgFile.setPath(tifFile.getAbsolutePath());
						imgFile.setLock(false);
						imgFile.setPostProcessedPath(destFile.getAbsolutePath());
						imgFile.setUpdateDate(today);
						imgFile.setWidth(0 + "");
						if(imgFile.getNewCopy())
							((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
						else
							((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
						
						FilesUtils.moveFile(tifFile, destFile);
						continue;
					}
						
					logger.info("processing :" + tifFile.getName());					
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
						
						
						// 如果存在就先刪除
						if (destFile.exists())
							FileUtils.forceDelete(destFile);
						// 存成多頁式600 dpi
						JaiTiffImgProcess.saveAsMultipageTIFF(images,
								destFile.getAbsolutePath());

						// 寫入資料庫
						List<ImgFile> imgFiles = null;
						imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).getImgFilesByNm(dbFileNm);
						ImgFile imgFile = null;
						if (imgFiles != null && imgFiles.size() > 0) {
							imgFile = imgFiles.get(0);
							imgFile.setNewCopy(false);
						} else {
							imgFile = new ImgFile();
							imgFile.setInsertDate(today);
							imgFile.setNewCopy(true);
						}
						
						imgFile.setFileNm(dbFileNm.toLowerCase());						
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
						imgFile.setPostProcessedPath(destFile.getAbsolutePath());
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
						
						imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).getImgFilesByNm(dbFileNm);
						
						ImgFile imgFile = null;
						if (imgFiles != null && imgFiles.size() > 0) {
							imgFile = imgFiles.get(0);
							imgFile.setNewCopy(false);
						} else {
							imgFile = new ImgFile();
							imgFile.setInsertDate(today);
							imgFile.setNewCopy(true);
						}
						
						imgFile.setFileNm(dbFileNm.toLowerCase());						
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
								Properties.getGpBackupFolder(), "IMAGE/" + Constant.yyyy_MM_dd.format(GroupInputdateParser.getGpInputDate()) + "/error/" ), true);
						continue;
					} finally {
						
					}
				}
				tifProcessed = true;
				eReport = new ErrorReport();
            	eReport.setErrHappenTime(new Date());
            	eReport.setErrorType("TifProcess");
            	eReport.setOldBatchName(null);
            	eReport.setReported(true);			
				eReport.setMessageBody("處理團險要保書影像檔:共 " + tiffFiles.size() + "個，使用" + (new Date().getTime() - start.getTime()) / 1000 + "秒完成");						
				eReport.setTitle("處理要保書影像檔");
				((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
				try{
				   if(imgOkFile.exists())
					  FileUtils.forceDelete(imgOkFile);
				   
				}catch(Exception e){
					logger.error("", e);
				}
			}// end of if (centerFolders != null && imgOkFile != null && imgOkFile.exists()) 
			
			/*
			 * 暫時性的作法，移到影像server後就要關閉
			 */
			replaceimgFile();
			
			//pres在進行中時不進行遠端影像檔更新			
			File presFolder = new File(Properties.getPresPath());
			File afpFolder = new File(Properties.getAfpPath());
			File[] txtFiles = presFolder.listFiles(FileFilterImpl.getFileFilter());
			File[] afpFiles = afpFolder.listFiles(FileFilterImpl.getAfpFileFilter());
			if(txtFiles != null && afpFiles != null && (txtFiles.length > 0 || afpFiles.length > 0)){			
				logger.info("GruupImgUpdater stopped because of PRES running ");
				running = false;
				return;
			}

			logger.info("get smbFiles get");						
			//先檢查一般影像檔
			SmbFile[] remoteFiles = FilesUtils
					.getFileList(Properties.getGpImgServerDomain(), Properties.getGpImgServer(),
							Properties.getGpImgServerUser(),
							Properties.getGpImgServerPwd(),
							Properties.getRemoteGroupImgFolder(), null);
			
			if(remoteFiles != null)
			   logger.info("remoteFiles:" + remoteFiles.length);
			else
			   logger.info("can't find folder" );
			
			if(remoteFiles == null)
				remoteFiles = new SmbFile[0];
			Map<SmbFile, ImgFile> imgs4Transfer = new HashMap<SmbFile, ImgFile>();
			List<ImgFile> imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).findGroupImage();
			List<ImgFile> lawFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).findGroupLaw();
			
			File postProcessedFolder = new File(Properties.getImgPostProcessedPath(), "group");
			postProcessedFolder = new File(postProcessedFolder, "image");
			if(!postProcessedFolder.exists())
				postProcessedFolder.mkdirs();

			// 檢查image file是不是已被copy過來
			for (SmbFile smbFile : remoteFiles) {
				String name = smbFile.getName().toLowerCase(); // 轉成小寫方便比對
				ImgFile imgFile = null;
				//檢查資料庫是不是有這筆資料
				if (imgFiles != null) {
					for (ImgFile imgfile : imgFiles) {
						if (imgfile.getFileNm().toLowerCase().equals("/group/image/" + name)) {
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
						imgFile.setInsertDate(new Date());
						imgFile.setLaw(false);
						imgFile.setLock(false);
						imgFile.setPath(Properties.getGpImgServer() + Properties.getRemoteGroupImgFolder()
								+ smbFile.getName());
						imgFile.setPostProcessedPath(new File(postProcessedFolder, smbFile.getName()).getAbsolutePath());
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
					imgFile.setFileNm("/group/image/" + smbFile.getName().toLowerCase());
					imgFile.setLength(smbFile.length());
					imgFile.setImage(true);
					imgFile.setInsertDate(new Date());
					imgFile.setLaw(false);
					imgFile.setReqPolicy(false);
					imgFile.setLock(false);
					imgFile.setNewCopy(true);
					imgFile.setPath(Properties.getGpImgServer() + Properties.getRemoteGroupImgFolder()
							+ smbFile.getName());
					imgFile.setPostProcessedPath(new File(postProcessedFolder, smbFile.getName()).getAbsolutePath());
					imgFile.setReqPolicy(false);
					imgFile.setUpdateDate(new Date());
					imgs4Transfer.put(smbFile, imgFile);
				}
				File imageFolder = postProcessedFolder;
				if(imgs4Transfer.get(smbFile) == null 
						&& !new File(imageFolder, smbFile.getName()).exists()){
					logger.info(smbFile.getName() + " file not exist. Copy from remote site.");
					imgs4Transfer.put(smbFile, imgFile);
				}

			}// for(SmbFile smbFile : remoteFiles){
			
			remoteFiles = FilesUtils
					.getFileList(Properties.getGpImgServerDomain(), Properties.getGpImgServer(),
							Properties.getGpImgServerUser(),
							Properties.getGpImgServerPwd(),
							Properties.getRemoteGroupLawFolder(), null);
			if(remoteFiles != null)
			   logger.info("remoteFiles:" + remoteFiles.length);
			else
			   logger.info("can't find folder" );
			
			postProcessedFolder = new File(Properties.getImgPostProcessedPath(), "group");
			postProcessedFolder = new File(postProcessedFolder, "law");
			if(!postProcessedFolder.exists())
				postProcessedFolder.mkdirs();
			
			if(remoteFiles == null)
				remoteFiles = new SmbFile[0];
			// 檢查law file是不是已被copy過來
			for (SmbFile smbFile : remoteFiles) {
				String name = smbFile.getName().toLowerCase(); // 轉成小寫方便比對
				ImgFile imgFile = null;
				if (lawFiles != null) {
					for (ImgFile imgfile : lawFiles) {
						if (imgfile.getFileNm().toLowerCase().equals("/group/law/" + name)) {
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
						imgFile.setFileNm("/group/law/" + smbFile.getName().toLowerCase());
						imgFile.setLength(smbFile.length());
						imgFile.setImage(false);
						imgFile.setInsertDate(new Date());
						imgFile.setLaw(true);
						imgFile.setReqPolicy(false);
						imgFile.setLock(false);
						imgFile.setPath(Properties.getGpImgServer() + Properties.getRemoteGroupLawFolder()
								+ smbFile.getName());
						imgFile.setPostProcessedPath(new File(postProcessedFolder, smbFile.getName()).getAbsolutePath());
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
					imgFile.setFileNm("/group/law/" + smbFile.getName());
					imgFile.setLength(smbFile.length());
					imgFile.setImage(false);
					imgFile.setInsertDate(new Date());
					imgFile.setLaw(true);
					imgFile.setReqPolicy(false);
					imgFile.setLock(false);
					imgFile.setNewCopy(true);
					imgFile.setPath( Properties.getGpImgServer() + Properties.getRemoteGroupLawFolder()
							+ smbFile.getName());
					imgFile.setPostProcessedPath(new File(postProcessedFolder, smbFile.getName()).getAbsolutePath());
					imgFile.setReqPolicy(false);
					imgFile.setUpdateDate(new Date());
					imgs4Transfer.put(smbFile, imgFile);
				}
				File lawFolder = postProcessedFolder;
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
						continue;
					} else {
						ImgFile imgFile = null;
						try {
							// 條款檔不處理，直接搬過去
							imgFile = imgs4Transfer.get(tifFile);							
							logger.info(imgFile.getFileNm() + ":" + imgFile.getNewCopy());
							String subDir = null;
							if(imgFile.getImage() != null && imgFile.getImage())
								subDir = "/group/image";
							else if(imgFile.getLaw() != null && imgFile.getLaw())
								subDir = "/group/law";
                            File subFolder = new File(Properties.getImgPostProcessedPath(), subDir);
							if(!subFolder.exists())
								subFolder.mkdirs();
							
							FilesUtils.copySmbFileToDirectory(tifFile, subFolder);
							
							//如果是影像檔，轉成600DPI
							if (imgFile.getImage() != null
									&& imgFile.getImage()) {
								File oriTiffFile = new File(subFolder,
										tifFile.getName());
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
									}
								} catch (Exception e) {
									e.printStackTrace();									
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

		logger.info("Group ImgUpdater stop.");
	}

	public static Date getFileDate() {
		return fileDate;
	}
	//暫時性的作法
	private static void replaceimgFile(){
		logger.info("tmp process:");
		//D:\group_CaseNew	
		//D:\groupImgTest
		File imgOriF = new File("D:\\group_CaseNew\\Image"); 
		File lawOriF = new File("D:\\group_CaseNew\\Law");
		File imgDestF = new File("D:\\groupImgTest\\Image"); 
		File lawDestF = new File("D:\\groupImgTest\\Law");
		
		File[] imgsOri = imgOriF.listFiles(); 
		File[] lawsOri = lawOriF.listFiles();
		File[] imgsDest = imgDestF.listFiles(); 
		File[] lawsDest = lawDestF.listFiles();
		
		for(File imgOri : imgsOri){
			String fileNm = imgOri.getName();
			boolean copyTo = true;
			for(File imgDest : imgsDest){
				if(fileNm.toLowerCase().equals(imgDest.getName().toLowerCase())){
					//如果檔名相同時，檔案日期比較舊或相同就不要copy
					if(imgOri.lastModified() <= imgDest.lastModified()){
						copyTo = false;
					}
					break;
				}
			}
			if(copyTo){
				logger.info("copy File from " + imgOri + " to " + imgDestF);
				if(new File(imgDestF, imgOri.getName()).exists() ){
					try {
						FileUtils.forceDelete(new File(imgDestF, imgOri.getName())) ;					
					} catch (IOException e) {
						logger.error("", e);
						e.printStackTrace();
					}
				}
				try {
					FileUtils.copyFileToDirectory(imgOri, imgDestF);
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			}
			
		}
		for(File lawOri : lawsOri){
			String fileNm = lawOri.getName();
			boolean copyTo = true;
			for(File lawDest : lawsDest){
				if(fileNm.toLowerCase().equals(lawDest.getName().toLowerCase())){
					//如果檔名相同時，檔案日期比較舊或相同就不要copy
					if(lawOri.lastModified() <= lawDest.lastModified()){
						copyTo = false;
					}
					break;
				}
			}
			if(copyTo){
				logger.info("copy File from " + lawOri + " to " + lawDestF);
				if(new File(lawDestF, lawOri.getName()).exists() ){
					try {
						FileUtils.forceDelete(new File(lawDestF , lawOri.getName())) ;
					} catch (IOException e) {
						logger.error("", e);
						e.printStackTrace();
					}
				}
				try {
					FileUtils.copyFileToDirectory(lawOri, lawDestF);
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			}
			
		}
	}

	public static boolean isTifProcessed() {
		return GpImgUpdater.tifProcessed;
	}

	public static void setTifProcessed(boolean tifProcessed) {
		GpImgUpdater.tifProcessed = tifProcessed;
	}
	
}
