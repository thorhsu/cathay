package com.fxdms.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.cathy.task.InputdateParser;
import com.fxdms.rmi.service.VoService;
import com.salmat.pas.vo.ImgFile;


public class DeleteDBAndFile {

	static Logger logger = Logger.getLogger(DeleteDBAndFile.class);

	public static void deleteFile() {
		int keepDays = 7;
		
		if (Properties.getFilesKeepDays() != null)
			keepDays = Properties.getFilesKeepDays();
		List<String> specialFolders = Properties.getBackupFoldersList();
		List<String> foldersKeepDays = Properties.getBackupKeepDaysList();		
		Map<File, Integer> keepDaysMap = new HashMap<File, Integer>();
		for(int i = 0 ; i < specialFolders.size() ; i++){
			if(!specialFolders.get(i).equals("")){
			   File file = new File(specialFolders.get(i));
			   Integer keepDay = keepDays;
			   try{
			      keepDay = new Integer(foldersKeepDays.get(i));
			   }catch(Exception e){
				  logger.error("", e); 
				  e.printStackTrace();
			   }
			   keepDaysMap.put(file, keepDay);
			}
		}
		
		Calendar daysBeforeCal = Calendar.getInstance();
		daysBeforeCal.set(daysBeforeCal.get(Calendar.YEAR), daysBeforeCal.get(Calendar.MONTH), daysBeforeCal.get(Calendar.DATE) - keepDays, 0, 0, 0);
		Date daysBefore = daysBeforeCal.getTime();
		
		logger.info("keep date before:" + daysBefore);
		logger.info("today is:" + new Date());

		File[] backupFolders = {new File(Properties.getBackupFolder()), new File(Properties.getGpBackupFolder())};
		Set<File> spFolders = keepDaysMap.keySet();
	    
		for(File backupFolder : backupFolders){
		   if (backupFolder != null && backupFolder.exists()) {
			   try {				   
			       logger.info("開始刪除" + keepDays + "天以前的備份檔");
 			       // 取所有目錄
			       File[] folders = backupFolder.listFiles(FileFilterImpl.getDirectoryFilter());
			       for (File folder : folders) {
			           if(!spFolders.contains(folder))
					      recursiveDeleteFolder(folder, daysBefore, spFolders);				      
			       }
			   } catch (Exception e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   }			
		   }
	    }
		//刪除特殊設定的備份檔
		for(File folder : spFolders){
			Integer keepDay = keepDaysMap.get(folder);			
			logger.info("today is:" + new Date());
			logger.info("特殊設定:" + folder.getAbsolutePath() + "。保留天數:" + keepDay);
			Date myKeepDay = null;
			if(keepDay >= 0){
			   Calendar startDate = Calendar.getInstance();
			   startDate.add(Calendar.DATE, -1 * keepDay);
			   myKeepDay = startDate.getTime();
			   recursiveDeleteFolder(folder, myKeepDay, spFolders);
			}else{
			   //如果是負，代表以cycleDate去刪除
			   Date cycleDate = InputdateParser.getInputDate();
			   if(new Date().getTime() < cycleDate.getTime()){
				   //如果今天比cycle date小，就什麼事都不做
			   }else{
				   //否則就刪除
				   recursiveDeleteFolder(folder, new Date(), spFolders);
			   }
			}
			 
			
		}

		//刪除要保書影像檔
		File imgFolder = new File(Properties.getImgPostProcessedPath(), "image");
		if (imgFolder != null && imgFolder.exists()) {
			logger.info("開始刪除" + keepDays + "天以前的要保書影像檔");
			File[] files = imgFolder.listFiles(FileFilterImpl.getExpiredImgFile());
			if(files != null ){
				for(File file : files){
					try {
						FilesUtils.forceDelete(file);
						((VoService) Constant.getContext().getBean("voServiceProxy")).deleteImg("/image/" + file.getName());						
						logger.info("刪除要保書檔案" + file.getName() + "成功");
					} catch (IOException e) {
						logger.error("刪除要保書檔案失敗", e);
						e.printStackTrace();
					}
				}
			}
		}
		imgFolder = new File(Properties.getImgPostProcessedPath(), "group/image");
		if (imgFolder != null && imgFolder.exists()) {
			logger.info("開始刪除" + keepDays + "天以前的團險要保書影像檔");
			File[] files = imgFolder.listFiles(FileFilterImpl.getFileFilter());
			if(files != null ){
				for(File file : files){
					try {
						Calendar cal = Calendar.getInstance();						
						if(file.getName().length() >= 15 && file.getName().toLowerCase().endsWith("tif") &&
								file.lastModified() + (keepDays * 24L * 60L * 60L * 1000L) < cal.getTimeInMillis()){							
							List<ImgFile> imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).getImgFilesByNm("/group/image/" + file.getName());
							//檢查是不是要保書影像檔，免得誤刪
							boolean requestPolicy = false;
							if(imgFiles != null && imgFiles.size() > 0 ){
								for(ImgFile imgFile : imgFiles){
									if(imgFile.getReqPolicy() != null && imgFile.getReqPolicy()){
									    requestPolicy = true;
								       ((VoService) Constant.getContext().getBean("voServiceProxy")).deleteImg("/group/image/" + file.getName());
								       logger.info("刪除資料庫要保書檔案" + file.getName() + "成功");
									}
								}
								if(requestPolicy){
								   FilesUtils.forceDelete(file);
								   logger.info("刪除團險要保書檔案" + file.getName() + "成功");
								}
							}else{
								FilesUtils.forceDelete(file);
								logger.info("刪除團險檔案" + file.getName() + "成功");
							}
						}
						
					} catch (IOException e) {
						logger.error("刪除團險要保書檔案失敗", e);
						e.printStackTrace();
					}
				}
			}
		}		
		File pdfFolder = new File(Properties.getAfpPath(), "pdf");
		File[] tmpFiles = pdfFolder.listFiles(FileFilterImpl.getFileFilter());
		for(File tmpFile : tmpFiles){
			if(tmpFile.getName().toLowerCase().endsWith(".tmp")){
				try {
					FilesUtils.forceDelete(tmpFile);
				}catch(Exception e){
					logger.error("", e);
				}
			}
		}
		
	    logger.info("finish delete process");
		
	}
	
	private static void recursiveDeleteFolder(File folder, Date deleteDate, Set<File> notDeleteFolders) {
		if (folder != null && folder.exists() && folder.isDirectory()) {
			File[] subFiles = folder.listFiles();
			if (subFiles != null)
				for (File subFile : subFiles) {
					if(notDeleteFolders != null && subFile.isDirectory() 
							&& notDeleteFolders.contains(subFile)){
						logger.info(subFile.getAbsoluteFile() + " is disignated not deleted.");
						continue;
						//如果標明不刪除的目錄，就什麼事都不做
					}else if(subFile.isDirectory() && subFile.listFiles().length > 0) {
						// 如果不是空目錄就往下繼續
						recursiveDeleteFolder(subFile, deleteDate, notDeleteFolders);
					} else if (subFile.isDirectory()
							&& subFile.listFiles().length == 0) {
						// 如果是空目錄就砍掉
						
						try {
							FilesUtils.deleteDirectory(subFile);
							logger.info("刪除目錄" + subFile.getAbsolutePath()
									+ "成功");
						} catch (IOException e) {
							logger.error("刪除目錄" + subFile.getAbsolutePath()
									+ "失敗", e);
							e.printStackTrace();
						}
						
					} else if (subFile.isFile()) {
						if (subFile.lastModified() < deleteDate.getTime()) {
							
							logger.info("檔案:"
									+ subFile.getName()
									+ "。檔案日期"
									+ Constant.yyyyMMdd.format(new Date(subFile
											.lastModified())) + "。小於保留日期"
									+ Constant.yyyyMMdd.format(deleteDate));
									
							try {
								FileUtils.forceDelete(subFile);
								logger.info("刪除檔案" + subFile.getAbsolutePath()
										+ "成功");
							} catch (Exception e) {
								e.printStackTrace();
								logger.error("刪除檔案" + subFile.getAbsolutePath()
										+ "失敗", e);
							}
						}
					}
				}
		}
	}

	public static void main(String[] args) {
		File workDisk = new File("c:/");
		System.out.println("before free space is: " + workDisk.getFreeSpace()/1024/1024/1024);
		File file = new File("C:\\photo\\路邊亂照\\");
		Calendar cal = Calendar.getInstance();
		cal.set(2011, 3, 31);
		cal.getTime();
		//repeatToDeleteFile(file, cal.getTime());
		System.out.println("after free space is: " + workDisk.getFreeSpace()/Math.pow(1024, 3));
	}

	

}
