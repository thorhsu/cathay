package com.fxdms.cathy.task;

import java.io.*;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FileFilterImpl;
import com.fxdms.util.FilesUtils;
import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.bo.SplitFile;
import com.fxdms.cathy.conf.Constant;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.ImgFile;

public class GpFileDispatcher {

	static Logger logger = Logger.getLogger(GpFileDispatcher.class);
	
	private static boolean running = false;

	private static Map<String, ImgFile> imgFilesOK = null;
	private static Map<String, ImgFile> imgFilesNotExist = null;
	private static Map<String, ImgFile> imgFilesError = null;	
	private static Map<String, Area> areaMap = null;
	private static DecimalFormat df = new DecimalFormat("00000");
	private static Map<String, String> addressMap = new HashMap<String, String>();	
	private static int imageErrorCounter = 0;
	private static int noReceiptCounter = 0;
	private static int noPolicyCounter = 0;
	private static Map<String, String> mappingServiceCenter; //國壽發的發單單位和服務中心對照表
	private static int normCounter = 0;
	private static Date cycleDate = GroupInputdateParser.getGpInputDate();
	private static File imgOkFile = new File(new File(Properties.getGroupInFolder(), "OK"), "IMAGE.ok");
	


	public static void startToRun() throws BeansException, RemoteException {		
		
		//時間設定更新影像檔的程式要早一點進行，如果更新還在進行中，先暫停此thread，等下一次再進行
		//如果有其它Thread正在run，也是跳出去
		
		if(running || GpImgUpdater.isRunning() || AfpListener.isRunning() || FileDispatcher.getRunning())			
			return;
							
		logger.info("group file dispatcher start to run");
		imageErrorCounter = 0;
		noReceiptCounter = 0;
		noPolicyCounter = 0;				
		normCounter = 0;
		AreaUpdater.startToRun(); //先進行服務中心更新

		try{
			running = true;
			File txtFolder = new File(Properties.getGroupInFolder(), "DATA"); //一般batch件			
			if(!txtFolder.exists())
				txtFolder.mkdirs();

			File txtTestFolder = new File(Properties.getGroupInFolder(), "TEST_DATA"); //測試件
			if(!txtTestFolder.exists())
				txtTestFolder.mkdirs();
			
			
			File okFolder = new File(Properties.getGroupInFolder(), "OK");
			if(!okFolder.exists())
				okFolder.mkdirs();
			File okFile = new File(okFolder , "DATA.ok");
			File testOkFile = new File(okFolder , "TEST_DATA.ok");
			boolean processBegin = false;
			//只有在GpImgUpdater處理結束，okFile存在，imgOkFile不存在時，才會進行
			if(okFile.exists() && GpImgUpdater.isTifProcessed() && !imgOkFile.exists()){
				processBegin = true;
			}
			
			
			String center = "06"; //行政中心
			if(processBegin){				
			   for(int i = 1 ; i <= Properties.getMaxCenter() ; i ++){			      	
			      File centerFolder = null;
			      if(i < 10)
				      centerFolder = new File(txtFolder, "0" + i);
			      else
				      centerFolder = new File(txtFolder, i + "");
			      //06之外的都copy進來
			      if(centerFolder.exists() && i != 6){
				     File[] files = centerFolder.listFiles(FileFilterImpl.getFileFilter());
				     for(File file: files)
				    	 FilesUtils.moveFileToDirectory(file, new File(txtFolder, "06"), true);
			         
			      }
		    }
			logger.info("group process begin" );
			//如果可以開始進行，開始搬到tmp folder，寫入DB，並依轄區歸類到資料夾
			if(processBegin){
				addressMap = new HashMap<String, String>();
				SplitFile.setAreaMap(new HashMap<String, String>());
				GroupInputdateParser.forceReadFile();
				cycleDate = GroupInputdateParser.getGpInputDate();
			    ErrorReport er = new ErrorReport();
				er.setErrHappenTime(new Date());
				er.setErrorType("BeginProcess");
				er.setOldBatchName("");
				er.setReported(true);
				er.setMessageBody("開始處理團險發單資料"  );
				er.setTitle("Begin Process");
				((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
				//"Area.findHaveAddress"
				List<Area> normalAreas =  ((VoService) Constant.getContext().getBean("voServiceProxy")).getAreaList();	
				logger.info("got areas for checks");
				areaMap = new HashMap<String, Area>();	 
				for(Area area : normalAreas){
					//completeAreas.put(area.getAreaId(), area);
					areaMap.put(area.getAreaId(), area);
				    Area mapArea = areaMap.get(area.getSubAreaId());
				    String address = null;
				    // subAreaId有重覆，只放入第一個抓到的
				    if(mapArea == null ){
				    	areaMap.put(area.getSubAreaId(), area);
				    	address = area.getAddress();
				    }else{
				    	address = mapArea.getAddress();
				    }	
				    //如果放入的地址是空的，再放一次
				    if(address == null || address.trim().equals("") || address.indexOf("無地址") >= 0){
				    	areaMap.put(area.getSubAreaId(), area);
				    }
				}				
				List<ImgFile> imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).getImgFiles();
				//把它放在不同的Map中
				imgFilesOK = new HashMap<String, ImgFile>();
				imgFilesNotExist = new HashMap<String, ImgFile>();
				imgFilesError = new HashMap<String, ImgFile>();
				for(ImgFile imgFile : imgFiles){
					//存在且是非error image的
					if(imgFile.getExist() != null && imgFile.getExist() && imgFile.getErrorImage() != null && !imgFile.getErrorImage()){
					    imgFilesOK.put(imgFile.getFileNm().toLowerCase(), imgFile);
					}else if(imgFile.getExist() != null && imgFile.getExist() && imgFile.getErrorImage() != null && imgFile.getErrorImage()){
						imgFilesError.put(imgFile.getFileNm().toLowerCase(), imgFile);
				    }else{
					    imgFilesNotExist.put(imgFile.getFileNm().toLowerCase(), imgFile);
					}
				}
				
				
				int oldSerialNo = 0;
				File centerFolder = new File(txtFolder, "06"); 
				File [] files = centerFolder.listFiles(FileFilterImpl.getFileFilter());
				//先把所有檔案拆開
				//if(!reisRecheck){
				for(File file : files){
				   String[] fileNmSplit = file.getName().split("_");
				   //如果檔案名稱不對時，通知發生錯誤，並移到備份目錄
				   if(fileNmSplit.length < 6 || !fileNmSplit[3].toUpperCase().equals("GROUP")){
					   ErrorReport err = new ErrorReport();
					   err.setErrHappenTime(new Date());
					   err.setErrorType("errFileNm");
					   err.setOldBatchName(file.getName());
					   err.setReported(false);
					   err.setException(true);
					   err.setMessageBody("團險目錄中放入錯誤的檔案名稱" + file.getName());
					   err.setTitle("apply data wrong format ");
					   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);
					   FilesUtils.moveFileToDirectory(file, new File(Properties.getGpBackupFolder(), "DATA/" + Constant.yyyy_MM_dd.format(cycleDate) + "/" + file.getParentFile().getName()), true);
					   continue;
				   }
				   SplitFile.groupSplit(file, centerFolder);
				   FilesUtils.moveFileToDirectory(file, new File(Properties.getGpBackupFolder(), "DATA/" + Constant.yyyy_MM_dd.format(cycleDate) + "/" + file.getParentFile().getName()), true);
				   
				}
				
                File[] allFiles = centerFolder.listFiles();
                List<File> receiptFiles = new ArrayList<File>();
                List<File> policyFiles = new ArrayList<File>();

				for(File file : allFiles){
					String oldBatchName = file.getName();
					if(oldBatchName.toLowerCase().indexOf("sign") > 0){	
						receiptFiles.add(file);
					}else{
						policyFiles.add(file);
					}
				}
				//不是測試時才進行檢查
			    for (int i = policyFiles.size() - 1; i >= 0; i--) {
					File file = policyFiles.get(i);
					String fileNm = file.getName().toLowerCase();
					String fileNm4Search = fileNm.replaceAll("_policy_", "_sign_");
					fileNm4Search = fileNm4Search.replaceAll("_pl_", "_si_");
					// 先檢查是不是有簽收單
					//如果沒找到，寫入通知，將此筆保單移出
					if (!new File(file.getParent(), fileNm4Search).exists()) {
						noReceiptCounter++;
						ErrorReport err = new ErrorReport();
						err.setErrHappenTime(new Date());
						err.setErrorType("errFileNm");
						err.setOldBatchName(fileNm);
						err.setReported(false);
						err.setMessageBody("處理團險時，" + fileNm + "找不到對應的簽收回條， 此保單不轉檔" );
						err.setException(true);
						err.setTitle("apply data wrong format ");
						((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);								   
						FilesUtils.moveToDirectory(file, new File(Properties.getGpBackupFolder() + "DATA/" + Constant.yyyy_MM_dd.format(cycleDate) + "/applyData"), true);
						policyFiles.remove(i);								
					}						
				}
				for (int i = receiptFiles.size() - 1; i >= 0; i--) {
					File file = receiptFiles.get(i);
					String fileNm = file.getName().toLowerCase();
					String fileNm4Search = fileNm.replaceAll("_sign_", "_policy_");
					fileNm4Search = fileNm4Search.replaceAll("_si_", "_pl_");
					// 先檢查是不是有保單單
					//如果沒找到，寫入通知，將此筆簽收單移出
					if (!new File(file.getParent(), fileNm4Search).exists()) {
						noReceiptCounter++;
						ErrorReport err = new ErrorReport();
						err.setErrHappenTime(new Date());
						err.setErrorType("errFileNm");
						err.setOldBatchName(fileNm);
						err.setReported(false);
						err.setMessageBody("處理團險目錄時，" + fileNm + "找不到對應的保單， 此簽收單不轉檔" );
						err.setException(true);
						err.setTitle("apply data wrong format ");
						((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);								   
						FilesUtils.moveToDirectory(file, new File(Properties.getGpBackupFolder() + "DATA/" + Constant.yyyy_MM_dd.format(cycleDate) + "/applyData"), true);
						receiptFiles.remove(i);								
					}							//如果沒找到，寫入通知，將此筆保單移出
				}
			
				if((noPolicyCounter > 50 || noReceiptCounter> 50) ){
					er = new ErrorReport();
					er.setErrHappenTime(new Date());
					er.setErrorType("tooManyError");
					er.setOldBatchName("");
					er.setReported(false);
					er.setMessageBody("處理團險時。有保單無簽收回條的保單有" + noReceiptCounter + "件。"  + "有簽收回條無保單的簽收回條有" + noPolicyCounter + "件。故暫停作業，請立刻處理此異常狀況" );
					er.setTitle("apply data wrong format ");
					er.setException(true);
					((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
					allFiles = centerFolder.listFiles();
					for(File file : allFiles){
					    FilesUtils.moveToDirectory(file, new File(Properties.getGpBackupFolder() + "DATA/" + Constant.yyyy_MM_dd.format(cycleDate) + "/applyData"), true);
				    }
					if(okFile.exists())
					   FileUtils.forceDelete(new File(okFolder, "DATA.OK"));
				    logger.info("group fileDispatcher too many errors stop");
					running = false;
					return;
				}
				//到備份目錄去找國壽給的服務中心對映表
				if(!new File(Properties.getBackupFolder(), "addr").exists())
					new File(Properties.getBackupFolder(), "addr").mkdirs();
				File [] backupFolders = new File(Properties.getBackupFolder(), "addr").listFiles(FileFilterImpl.getDirectoryFilter());
				File backupFolder = null;
				File [] addressFiles = null;
				if(backupFolders != null && backupFolders.length > 0){
					backupFolder = backupFolders[backupFolders.length - 1];
				    addressFiles = backupFolder.listFiles(FileFilterImpl.getFileFilter());
				}
				mappingServiceCenter = new HashMap<String, String>();
				if(AddressProcesser.serviceCenterMap != null && AddressProcesser.serviceCenterMap.size() > 0)
					mappingServiceCenter = AddressProcesser.serviceCenterMap;
				else if(addressFiles != null && addressFiles.length > 0){					
					mappingServiceCenter = AddressProcesser.readFile(addressFiles);
				}
				
				oldSerialNo = verifyFiles(policyFiles, oldSerialNo, center, false);
				
				oldSerialNo = verifyFiles(receiptFiles, oldSerialNo, center, true);
				
				if((noPolicyCounter > 50 || imageErrorCounter > 50 || noReceiptCounter> 50) ){
					er = new ErrorReport();
					er.setErrHappenTime(new Date());
					er.setErrorType("tooManyError");
					er.setOldBatchName("");
					er.setReported(false);
					er.setMessageBody("處理團險保單時。無影像或影像錯誤的有" + imageErrorCounter + "件。故暫停作業，請立刻處理此異常狀況" );
					er.setTitle("apply data wrong format ");
					er.setException(true);
					((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
					allFiles = centerFolder.listFiles();
					for(File file : allFiles){
					    FilesUtils.moveToDirectory(file, new File(Properties.getGpBackupFolder() + "DATA/" + Constant.yyyy_MM_dd + "/applyData"), true);
				    }
					if(okFile.exists())
					   FileUtils.forceDelete(new File(okFolder, "DATA.OK"));
				    logger.info("group fileDispatcher too many errors stop");
					running = false;
					return;
				}
                
				
				if(processBegin){
					File directory = null;
					File receiptDir = null;
					if(center != null){
					   directory = new File(Properties.getCheckedOkPath(), "G" + "/" + center  + "/"  );
					   if(!directory.exists())
						   directory.mkdirs();
					   receiptDir = new File(Properties.getReceiptOkPath(), "G" + "/" + center  + "/"  );
					   if(!receiptDir.exists())
						   receiptDir.mkdirs();
					}
					
					
					File [] subdirs = directory.listFiles(FileFilterImpl.getAreaIdFolderFilter()); 					
					File [] recSubdirs = receiptDir.listFiles(FileFilterImpl.getAreaIdFolderFilter()); 
					//如果開始進行轉檔，寫入一筆資料
					if(subdirs != null && subdirs.length > 0){
						er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("BeginProcess");
						er.setOldBatchName("");
						er.setReported(false);
						er.setMessageBody(Constant.yyyy_MM_ddHHMM.format(new Date()) + "，行政中心:" + ApplyData.getCenterMap().get(center) + "開始進行轉檔作業處理\r\n "
								+ "團險:" + normCounter + "件\r\n ");
						er.setTitle("Begin Process");
						((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
					}
					
					//一般保單
					for(File subdir : subdirs){																		
						String [] fileNm = subdir.list();
						String filesLen = df.format(fileNm.length);
						String[] dirName = subdir.getName().split("_");						
						//改名為地址_檔案數(五位數)_areaId(七位)，listfiles會有自然排序						
						subdir.renameTo(new File(subdir.getParent(), dirName[0] + "_" + filesLen + "_" +  dirName[1]));						   
					}
					
					subdirs = directory.listFiles(FileFilterImpl.getAlteredAreaIdDirFilter());
					//簽收回條
					for(File subdir : recSubdirs){
						String[] dirName = subdir.getName().split("_");		
						boolean theSame = false;
						//把資料夾名稱改成和保單相同，這樣會有相同的排序 
						for(File policySubDir : subdirs){				
							String[] policyDirName = policySubDir.getName().split("_");
							if(policyDirName[0].equals(dirName[0]) && policyDirName[2].equals(dirName[1])){
								subdir.renameTo(new File(subdir.getParent(), policySubDir.getName()));
								theSame = true;
								break;
							}
						}
						//如果都沒有找到時，還是要更名，防止錯誤
						if(!theSame){
							String [] fileNm = subdir.list();
							String filesLen = df.format(fileNm.length);
							subdir.renameTo(new File(subdir.getParent(), dirName[0] + "_" + filesLen + "_" + dirName[1]));
						}
						   
					}
					
					//寫入moveCompleted.ok，通知完成
					FileWriter fw = null;
					if(center != null){
					   if(subdirs.length > 0){
					      fw = new FileWriter(new File(Properties.getCheckedOkPath(), "G" + "/" + center  + "/moveCompleted.ok" ));
					      fw.write("ok");
					      fw.flush();
						  fw.close();
					   }
					   if(recSubdirs.length > 0){
					      fw = new FileWriter(new File(Properties.getReceiptOkPath(), "G" + "/" + center  + "/moveCompleted.ok" ));
					      fw.write("ok");
					      fw.flush();
						  fw.close();
					   }					   
					}
				}
				//全部作完後把data.ok和image.ok幹掉
				
				if(processBegin){
				   if(okFile.exists())
					   FileUtils.forceDelete(okFile);
				}
				GpImgUpdater.setTifProcessed(false);
			} // end processBegin 
		}		
		logger.info("group fileDispatcher  stop");
		
	   }catch(Exception e){
		   logger.error("", e);
	   }finally{
		   running = false;   
	   }
	}
	
	
	
	private synchronized static int verifyFiles(List<File> allFiles, int oldSerialNo, String center, boolean receipt) throws BeansException, RemoteException {		
		
		Map<String, Area> centerMap = ((VoService) Constant.getContext().getBean("voServiceProxy")).getCenterAreaMap();		
		
		for(File file : allFiles){
			boolean update ;			
			oldSerialNo++;
			//檔案名稱為PK
			String oldBatchName = file.getName();
			ApplyData applyData = ((VoService) Constant.getContext().getBean("voServiceProxy")).getApplyData(oldBatchName);
			if(applyData == null){
				applyData = new ApplyData();
				applyData.setGroupInsure(false);
				applyData.setHaveInsureCard(false);
				applyData.setOldBatchName(oldBatchName);
				update = false;				
			}else{
				//如果之前有發單過，就刪除
			    Date start = new Date();
			    boolean continueNext = false;
				while(!((VoService) Constant.getContext().getBean("voServiceProxy")).deleteApplyData(applyData)){
					logger.info("group couldn't delete applyData :" + applyData.getOldBatchName());
					try {
						Thread.sleep(5000);
						Date now = new Date();
						//如果一直刪不掉，等60秒後跳下一個，這支檔案移到wrong_file_name
						if((now.getTime() - start.getTime()) > (1000 * 60)){
							continueNext = true;
							ErrorReport er = new ErrorReport();
							er.setErrHappenTime(new Date());
							er.setErrorType("deleteFailed");
							er.setOldBatchName(oldBatchName);
							er.setReported(false);
							er.setMessageBody("無法刪除:" + oldBatchName);
							er.setTitle("apply data could not be deleted");
							er.setException(true);
							((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);							
							break;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						logger.error("", e);
						e.printStackTrace();
					}
				}
				//如果已交寄，刪除後把舊的applyData改另一個PK
				if("100".equals(applyData.getPolicyStatus())){				   
				   try{
					   ApplyData updateAd = new ApplyData();
					   BeanUtils.copyProperties(applyData, updateAd);
					   updateAd.setOldBatchName(oldBatchName + "_" + Constant.yyyy_MM_dd.format(applyData.getCycleDate()));
					   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(updateAd);
				   }catch(Exception e){
					   logger.error("", e);
					   ErrorReport er = new ErrorReport();
					   er.setErrHappenTime(new Date());
					   er.setErrorType("insert Failed");
					   er.setOldBatchName(applyData.getOldBatchName());
					   er.setReported(false);
					   er.setMessageBody("無法更新資料庫:" + applyData.getOldBatchName());
					   er.setTitle("apply data could not be inserted ");
					   er.setException(true);
					   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
				   }
				}
				if(continueNext){
					//換下一支檔案
					try {
						FilesUtils.moveToDirectory(file, new File(Properties.getErrorFileNmPath()), true);
					} catch (IOException e) {
						logger.error("", e);
						e.printStackTrace();
					}					
					continue;
				}
				applyData = new ApplyData();
				applyData.setGroupInsure(false);
				applyData.setHaveInsureCard(false);
				applyData.setOldBatchName(oldBatchName);
				update = false;
			}
			
			
			applyData.setExceptionStatus(null);
		    applyData.setVerifyResult(null);
		    applyData.setVerifyTime(null);
			logger.info("group processing:" + oldBatchName);
				String [] fileNmSplit = oldBatchName.split("_");
			//不是Test時，檢查檔案名稱格式，如果格式錯誤送出訊息
			if( fileNmSplit.length < 7){
				ErrorReport er = new ErrorReport();
				er.setErrHappenTime(new Date());
				er.setErrorType("errFileNm");
				er.setOldBatchName(oldBatchName);
				er.setReported(false);
				er.setMessageBody("錯誤的檔案名稱" + oldBatchName);
				er.setTitle("apply data wrong format ");
				((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
				try{
				   FilesUtils.moveToDirectory(file, new File(Properties.getErrorFileNmPath()), true);
				}catch(Exception e){
				   logger.error("", e);
				}
				continue;
			}else {
				Date today = new Date();
				//batch,test和online時進入此處
				int lastIndex = oldBatchName.lastIndexOf("_");						
				applyData.setFileNm(oldBatchName.substring(0, lastIndex));							
				applyData.setInsertDate(new Date());
				
				applyData.setUpdateDate(today);
				applyData.setReceipt(receipt);
				applyData.setExceptionStatus(null);
				

				try{
				   // 檔名解析後回壓							
			       applyData.setCycleDate(cycleDate); 
			       applyData.setProcessedDate(Constant.yyyy_MM_dd.parse(fileNmSplit[2]));
			       applyData.setCenter("06"); //寫死06
				   applyData.setSourceCode(fileNmSplit[3].toUpperCase());
				   applyData.setOldBatchNo(fileNmSplit[4]);
				   applyData.setPackType("01"); //不是北二時設為服務中心
				   applyData.setOldSerialNo(oldSerialNo);
				   applyData.setImageOk(false);						   
				   applyData.setDocOk(false);
				   applyData.setMegerOK(false);
				   applyData.setMetaOk(false);
				   applyData.setSignOk(false);
				   applyData.setPolicyStatus("00");
				   applyData.setVip(false);
				   applyData.setSubstract(false);
				   applyData.setVerifyResult(null);
				   applyData.setProductType("G"); //那一種件 O? B? T?
				   FileInputStream fis = null;
				   InputStreamReader isr = null;
				   BufferedReader br = null;
				   String line = null;
				   try {
					    logger.info("group read file");
						fis = new FileInputStream(file);
						isr = new InputStreamReader(fis, "ms950");
						br = new BufferedReader(isr);					
						Set<String> policyNos = applyData.getPolicyNoSet();
						Set<ImgFile> imgFileSet = new HashSet<ImgFile>();
						boolean continueNext = false;
						
						while((line = br.readLine()) != null) {
							line = line .trim();
							//info|G500003467103|00|XXXXXXXXXX||||FO41400||0|06|0|
							//info|G300053505103|00|
							if(line.startsWith("info") ){
								String [] lineSplit = line.split("\\|");
								if(lineSplit.length < 3){
									ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("errParsingFile");
									er.setOldBatchName(oldBatchName);
									er.setReported(false);
									er.setMessageBody(oldBatchName + "檔案info行格式錯誤" + "|" + line);
									er.setTitle("error on:" + line);											
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
									if(new File(Properties.getErrorFileNmPath(), file.getName()).exists()){
										FileUtils.forceDelete(new File(Properties.getErrorFileNmPath(), file.getName()));
									}
									try{
									   if(br != null)
										  br.close();
									   if(isr != null)
									      isr.close();
									   if(fis != null)
										  fis.close();
									}catch(Exception e){
										logger.error("", e);
									}
									br = null;
									isr = null;
									fis = null;
									FilesUtils.moveToDirectory(file, new File(Properties.getErrorFileNmPath()), true);
									continueNext = true;
									break;
								}
								if(lineSplit.length <= 7 && file.getName().toLowerCase().indexOf("sign") > 0){
								   String forSearch = oldBatchName.replaceAll("_SI_", "_PL_");
								   forSearch = forSearch.replaceAll("_sign_", "_policy_");
								   ApplyData policyAp = ((VoService) Constant.getContext().getBean("voServiceProxy")).getApplyData(forSearch);
								   applyData.setAreaId(policyAp.getAreaId());	   
								   applyData.setAreaName(policyAp.getAreaName());
								   applyData.setAreaAddress(policyAp.getAreaAddress());
								   applyData.setZip(policyAp.getZip());
								   applyData.setServiceCenter(policyAp.getServiceCenter());
								   applyData.setServiceCenterNm(policyAp.getServiceCenterNm());
								   applyData.setApplyNo(policyAp.getApplyNo());
								   applyData.setInsureId(policyAp.getInsureId());
								   applyData.setRecName(policyAp.getRecName());
								}
								
								policyNos.add(lineSplit[1].trim());
								applyData.setPolicyNoSet(policyNos);
								applyData.setReprint(Integer.parseInt(lineSplit[2].trim()));
								
								
								if(lineSplit.length > 3)
								   applyData.setApplyNo(lineSplit[3].trim());
								if(lineSplit.length > 4)
								   applyData.setInsureId(lineSplit[4].trim());
								if(lineSplit.length > 7)
								   applyData.setAreaId(lineSplit[7].trim());
								
								
								Area area = null;
								boolean selfCenter = false;
								//先看看自己是不是就是服務中心
								if(area == null && applyData.getAreaId().length() == 7){
									area = centerMap.get(applyData.getAreaId());
									if(area != null)
									   selfCenter = true;
								}			
								//先用完整的areaId去抓
								if(area == null){
									area = areaMap.get(applyData.getAreaId());
								}
								//如果抓不到改用前五碼抓獨立課
								if(area == null && applyData.getAreaId().length() >= 5){
									area = areaMap.get(applyData.getAreaId().substring(0, 5));
								}
								//抓不到獨立課，再抓一般課
								if(area == null && applyData.getAreaId().length() >= 4){
									area = areaMap.get(applyData.getAreaId().substring(0, 4));									
								}
								
								
								//設定subareaId 和地址																
								if(area != null){
									applyData.setSubAreaId(area.getSubAreaId());
									String address = null;
									String serviceCenter = null;
									String zipCode = null;
									if(!selfCenter && area.getServiceCenter() != null && !area.getServiceCenter().trim().equals("")){
						        	   serviceCenter = area.getServiceCenter();
									}else{ 
									   //找不到時用完整的areaId代替subareaId
									   serviceCenter = area.getAreaId();
									   //如果自己就是服務中心，subAreaId是完整的7碼
									   applyData.setSubAreaId(serviceCenter);
									}
						        	//如果有服務中心，就設定服務中心地址
							        if(serviceCenter != null && !serviceCenter.trim().equals("") && centerMap.get(serviceCenter) != null){
							        	address = centerMap.get(serviceCenter).getAddress();
							        	zipCode = centerMap.get(serviceCenter).getZipCode();							        		
							        	applyData.setServiceCenter(serviceCenter);
							        	applyData.setServiceCenterNm(centerMap.get(serviceCenter).getAreaName());							        	
							        }else if(serviceCenter != null && !serviceCenter.trim().equals("") && areaMap.get(serviceCenter) != null){
							        	address = areaMap.get(serviceCenter).getAddress();
							        	zipCode = areaMap.get(serviceCenter).getZipCode();							        		
							        	applyData.setServiceCenter(serviceCenter);
							        	applyData.setServiceCenterNm(areaMap.get(serviceCenter).getAreaName());							        	
							        }else{
							        	//都找不到時用國壽發的單位─服務中心對照表去抓
							        	//但因為此對照表和團險單位發的單位地址對照表有時會不同，所以是在完全找不到時才使用
							        	serviceCenter = mappingServiceCenter.get(area.getAreaId());
							        	if(serviceCenter != null && !serviceCenter.equals("") 
							        			&& centerMap.get(serviceCenter) != null){
							        	   address = centerMap.get(serviceCenter).getAddress();
							        	   zipCode = centerMap.get(serviceCenter).getZipCode();
							        	   applyData.setServiceCenter(serviceCenter);
							        	   applyData.setServiceCenterNm(centerMap.get(serviceCenter).getAreaName());
							        	   area.setServiceCenter(serviceCenter);
							        	   area.setServiceCenterNm(centerMap.get(serviceCenter).getAreaName());
							        	   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(area);
							        	}
							        }
							        //如果找不到地址，試看看從area拿 
							        if(address == null || "".equals(address.trim()) || address.indexOf("無地址") >= 0){
							        	logger.info("get area address");
							        	address = area.getAddress();
							        	zipCode = area.getZipCode();
							        	if(address == null || address.indexOf("無地址") >= 0 || "".equals(address.trim())){
							        		address = "無法由" + area.getAreaId() + "找到寄送地址";
							        	}
							        }
							        logger.info("group service center's address" + address );
							        applyData.setAreaAddress(address);
							        applyData.setZip(zipCode);
						        	
							        if((applyData.getAreaName() == null || "".equals(applyData.getAreaName().trim()))){
							        	Area detailArea = null;
							        	String areaId = applyData.getAreaId();
							        	if(areaId != null && areaMap.get(areaId) != null)
							        		detailArea = areaMap.get(areaId);
							        	if(detailArea != null){
							        		String areaName = null;
							        		if(SplitFile.getAreaMap() != null)
							        		   areaName = SplitFile.getAreaMap().get(detailArea.getAreaId());
							        		if(areaName != null && !areaName.trim().equals("") 
							        				&& !areaName.equals(detailArea.getAreaName())){
							        			logger.info("old areaName: " + detailArea.getAreaName() + " replaced by new areaName: " + areaName);
							        			detailArea.setAreaName(areaName);
							        			((VoService) Constant.getContext().getBean("voServiceProxy")).update(detailArea);
							        		}							        		
							        	    applyData.setAreaName(detailArea.getAreaName());
							        	}else{
							        		String areaName = null;
						        			if(SplitFile.getAreaMap() != null)
						        			   areaName = SplitFile.getAreaMap().get(areaId);
						        			if(areaName != null && !areaName.trim().equals("")){						        				
						        				applyData.setAreaName(areaName);
						        			}else{
							        	        applyData.setAreaName(area.getAreaName());
						        			}
							        	}
							        	
							        }
									
								}else{								
									boolean areaUpdate = true;
									Area noAddressArea = ((VoService) Constant.getContext().getBean("voServiceProxy")).getArea(applyData.getAreaId());
									//如果是全新的，就去新增一筆
									if(noAddressArea == null){
									   applyData.setSubAreaId(applyData.getAreaId().substring(0, 4));
									   noAddressArea = new Area();
									   noAddressArea.setAreaId(applyData.getAreaId());
									   noAddressArea.setAreaName("");
									   noAddressArea.setSubAreaId(applyData.getAreaId().substring(0, 4));
									   noAddressArea.setIndependent(false);
									   areaUpdate = false;
									}else{										
									   applyData.setSubAreaId(noAddressArea.getSubAreaId());
									}		
									//看看能不能由保單資料找到areaName
									if(noAddressArea.getAreaName() == null || noAddressArea.getAreaName().trim().equals("")){
										String areaName = null;
					        			if(SplitFile.getAreaMap() != null)
					        			   areaName = SplitFile.getAreaMap().get(noAddressArea.getAreaId());
										if(areaName != null && !areaName.trim().equals(""))
											noAddressArea.setAreaName(areaName);
									
									}
									applyData.setAreaName(noAddressArea.getAreaName());
									applyData.setAreaAddress("無地址");
									//noAddressArea.setAreaName(lineSplit[8].trim());									
									
									//從國壽的資料檔找service center
									String serviceCenter = mappingServiceCenter.get(noAddressArea.getAreaId());
									//如果找得到service center
									if(serviceCenter != null && !serviceCenter.trim().equals("")){
									   String address = null; 
									   String zipCode = null; 
									   noAddressArea.setServiceCenter(serviceCenter);
									   if(centerMap.get(serviceCenter) != null){
									      noAddressArea.setServiceCenterNm(centerMap.get(serviceCenter).getAreaName());
									      applyData.setServiceCenterNm(centerMap.get(serviceCenter).getAreaName());
									      address = centerMap.get(serviceCenter).getAddress();
									      zipCode = centerMap.get(serviceCenter).getZipCode();
									   }else if(areaMap.get(serviceCenter) != null){
										  noAddressArea.setServiceCenterNm(areaMap.get(serviceCenter).getAreaName());
										  applyData.setServiceCenterNm(areaMap.get(serviceCenter).getAreaName());
										  address = areaMap.get(serviceCenter).getAddress();
									      zipCode = areaMap.get(serviceCenter).getZipCode();
									   }									   
									   applyData.setServiceCenter(serviceCenter);								       	   
									   //看看此service center找不找到得到地址									   
										if(address != null && !address.trim().equals("")){										   
									       applyData.setAreaAddress(address);
									       applyData.setZip(zipCode);
										}
									}
									if(!areaUpdate)
									   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(noAddressArea);
									else
									   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(noAddressArea);
								}
								
								if(applyData.getAreaAddress() != null){
									Set<String> keys = addressMap.keySet();
									String biggestAddress = "0000";
																		
									//找出目前最大的地址代號
									for(String key : keys){
										String value = addressMap.get(key);
										if(value.compareTo(biggestAddress) > 0){
											biggestAddress = value;
										}
									}
									if(addressMap.get(applyData.getServiceCenter() + applyData.getAreaAddress()) != null){
										
									}else{	
									   Integer biggest = new Integer(biggestAddress) + 1;
								       addressMap.put(applyData.getServiceCenter() + applyData.getAreaAddress(), StringUtils.leftPad(biggest + "", 4, '0'));
								       logger.info("group put address:" + applyData.getAreaAddress() + "into map :" + biggest);
									}
								}else{
									logger.info("group not set area map");
								}								
								applyData.setMerger(false);
								
							}else if(line.indexOf("要保單位：") > 0){
								int seperateInd = line.lastIndexOf("|") < 0 ? line.indexOf("要保單位：") + 6 : line.lastIndexOf("|") + 1;
								String recName = line.substring(seperateInd).trim();
								applyData.setRecName(recName);
								
							}else{
							}
							/*
							 * 檢查難字
							 */
							int index = 0;
							line = line.toLowerCase(); //全部轉小寫，方便檢查
							line = line.replaceAll("\\\\", "/"); //把反斜線改正斜線
							//如果是簽收回條時，檢查B31(姓名)，F12(地址)；非簽收回條時，檢查B20
							if((receipt && ((index = line.indexOf("|b31|")) > 0 || (index = line.indexOf("|f12|")) > 0 )) || 
									(!receipt && (index = line.indexOf("|b20|")) > 0)){
							    String forCheck = line.substring(index + 5);
							    forCheck = forCheck.replaceAll("\\|", ""); //去除|
							    forCheck = forCheck.replaceAll("　", ""); //去除全形空白
							    
							    
							   /*
							    * 難字檢查先封起來
							    */
							    //if(FontChecker.checkDifficultString(forCheck.toCharArray())){ 
							    if(false){
							    	//回饋檔中寫進一筆
							    	applyData.setDocOk(false);
							    	applyData.setPolicyStatus("11");
							    	ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("errDifficultFont");
									er.setOldBatchName(oldBatchName);
									er.setReported(false);
									er.setMessageBody("檔案有難字:" + oldBatchName + "|" + forCheck);
									er.setTitle("error on:" + line);
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
									if(new File(Properties.getDifficultFontPath() + center + "/" , file.getName()).exists()){
										FileUtils.forceDelete(new File(Properties.getDifficultFontPath(), "/" + center + "/" + file.getName()));
									}
									try{
									   if(br != null)
										  br.close();
									   if(isr != null)
									      isr.close();
									   if(fis != null)
										  fis.close();
									}catch(Exception e){
										logger.error("", e);
									}
									br = null;
									isr = null;
									fis = null;											
									
									FilesUtils.moveToDirectory(file, new File(Properties.getDifficultFontPath() + center + "/" ), true);
									if(update)
										((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
									else
										((VoService) Constant.getContext().getBean("voServiceProxy")).save(applyData);
									continueNext = true;
									break;
							    }else{
							    	applyData.setDocOk(true);
							    }
							}
							
							String requestImage = null;
							//如果是有tif檔或是要保書影像檔，開始進行檢查
							if(((index = line.indexOf(".tif")) > 0) ){
								//先截到.tif為止，免得後面還有導致出錯
								String forCheck = null;
								if(line.indexOf(".tiff") > 0)
								   forCheck = line.substring(0, index + 5);
								else
								   forCheck = line.substring(0, index + 4);
								String fileNm = null;
								int beginIndex = 0;
								// 從最後一個..之後截斷
								if((beginIndex = forCheck.lastIndexOf("..")) > 0){
									fileNm = forCheck.substring(beginIndex + 2).trim();

								}		

								ImgFile imgFile = imgFilesOK.get(fileNm);
								if(imgFile != null){ //如果圖像存在且是好的影像檔，就放入set中											
									applyData.setImageOk(true);
									imgFileSet.add(imgFile);
								}else{
									applyData.setImageOk(false);
									imgFile = imgFilesError.get(fileNm);
									
									ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("errImg");
									er.setOldBatchName(oldBatchName);
									er.setReported(false);											
									er.setTitle("error on:" + line);
									if(imgFile != null){
										er.setMessageBody("影像檔有誤，非合格的tiff檔:" + imgFile.getFileNm() + "此保單無法進行作業");
										applyData.setPolicyStatus("13");
									}else{
									   //如果圖像不存在，從不存在的影像map中取得，或新增一個
									   imgFile = imgFilesNotExist.get(fileNm);
									   if(imgFile == null){																							
										   imgFile = new ImgFile();
										   imgFile.setExist(false);
										   imgFile.setFileNm(fileNm);
                                           if(fileNm.indexOf("image/") >= 0){
											   imgFile.setImage(true);
											   imgFile.setReqPolicy(false);
											   imgFile.setLaw(false);
										   }else if(fileNm.indexOf("law/") >= 0){
											   imgFile.setImage(false);
											   imgFile.setReqPolicy(false);
											   imgFile.setLaw(true);
										   }else{
											   imgFile.setImage(false);
											   imgFile.setReqPolicy(true);
											   imgFile.setLaw(false);
										   }
										   imgFile.setErrorImage(true);
										   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
										   imgFilesNotExist.put(imgFile.getFileNm(), imgFile);
									   }
									   applyData.setPolicyStatus("14");			
									   applyData.setVerifyResult("影像檔不存在:" + imgFile.getFileNm() );
									   er.setMessageBody("影像檔不存在:" + imgFile.getFileNm() );
									}
									imgFileSet.add(imgFile);
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
									if(update)
										((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
									else
										((VoService) Constant.getContext().getBean("voServiceProxy")).save(applyData);
									if(new File(Properties.getImgUncompletePath() + center , file.getName()).exists()){
										FileUtils.forceDelete(new File(Properties.getImgUncompletePath(), "/" + center + "/" + file.getName()));
									}		
									try{
									   if(br != null)
										  br.close();
									   if(isr != null)
									      isr.close();
									   if(fis != null)
										  fis.close();
									}catch(Exception e){
										logger.error("", e);
									}
									br = null;
									isr = null;
									fis = null;
									FilesUtils.moveToDirectory(file, new File(Properties.getImgUncompletePath() + center + "/"), true);
									imageErrorCounter++;
									continueNext = true;
									break;
								}							
							}								
						}
						if(continueNext){
							logger.info("group continue next file");
							continue;
						}

						applyData.setImgFiles(imgFileSet);
						applyData.setImageOk(true);
						applyData.setDocOk(true);
						applyData.setPolicyStatus("15");
					}catch(Exception e){
						logger.error("", e);
						if(new File(Properties.getErrorFileNmPath(), file.getName()).exists()){
							FileUtils.forceDelete(new File(Properties.getErrorFileNmPath(), file.getName()));
						}
						try{
							if(br != null)
							   br.close();
							if(isr != null)
							   isr.close();
							if(fis != null)
							   fis.close();
						}catch(Exception err){
							logger.error("", err);
						}
						br = null;
						isr = null;
						fis = null;
						if(file.exists())
						   FilesUtils.moveToDirectory(file, new File(Properties.getErrorFileNmPath()), true);
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("errParsingFile");
						er.setOldBatchName(oldBatchName);
						er.setReported(false);
						er.setMessageBody("檔案無法解析" + oldBatchName + "|" + e.getMessage());
						er.setTitle("error on:" + line);
						((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
						
						if(update)
							((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
						else
							((VoService) Constant.getContext().getBean("voServiceProxy")).save(applyData);
						continue;
					}finally{
						try{
						   if(br != null)
							  br.close();
						   if(isr != null)
						      isr.close();
						   if(fis != null)
							  fis.close();
						}catch(Exception e){
							logger.error("", e);
						}
						br = null;
						isr = null;
						fis = null;
					}
				   
				}catch(Exception e){
					//發生不可知的錯誤時的處理
					logger.error("", e);
					ErrorReport er = new ErrorReport();
					er.setErrHappenTime(new Date());
					er.setErrorType("exception");
					er.setOldBatchName(oldBatchName);
					er.setReported(false);
					er.setException(true);
					er.setMessageBody("exception happen:" + oldBatchName + "|" + e.getMessage());
					er.setTitle("exception happened");
					((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
					if(update)
						((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					else
						((VoService) Constant.getContext().getBean("voServiceProxy")).save(applyData);
                    
					try {
						if(file.exists())
						   FilesUtils.moveToDirectory(file, new File(Properties.getErrorFileNmPath()), true);
					} catch (IOException e1) {
					    logger.error("", e);
						e1.printStackTrace();
					}
					continue;
				}		
				
				boolean conjugateTrue = true;
				
				//檢查共軛的保單是不是正確，如果共軛保單錯誤，本單同時設定錯誤
				String applyNo = applyData.getApplyNo();
				
				List<ApplyData> appDatas = null;
				for(String policyNo : applyData.getPolicyNoSet()){
				   try {
					  appDatas = ((VoService) Constant.getContext().getBean("voServiceProxy")).findByApplyNoAndPolicyNoAndCenerCycleReprint( cycleDate, applyNo, policyNo, center, !receipt, applyData.getReprint());
				   } catch (Exception e) {
					  logger.error("", e);
					  e.printStackTrace();
				   }
				   if(appDatas != null && appDatas.size() > 0)
					   break;
				}
				if(appDatas != null && appDatas.size() > 0){
					ApplyData appData = appDatas.get(0); //共軛保單或簽收單
					if(appData.getExceptionStatus() != null && !appData.getExceptionStatus().equals("")){
						conjugateTrue = false;
						applyData.setPolicyStatus(appData.getPolicyStatus());
						applyData.setExceptionStatus(appData.getExceptionStatus());
						String conjugateNm = "因保單";
						String selfNm = "簽收單";
						if(!receipt){
							conjugateNm = "因簽收單";
							selfNm = "保單";
						}
						applyData.setVerifyResult(conjugateNm + "檢核錯誤，故同時設定錯誤");
					}
				}
				//檢查共軛的保單結束
				
				//毫無錯誤完成處理的apply data儲存起來
				if(update)
					((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
				else
					((VoService) Constant.getContext().getBean("voServiceProxy")).save(applyData);                
                try{
                   String subAreaId = applyData.getSubAreaId().replaceAll("\\*", "&");
                   subAreaId = StringUtils.rightPad(subAreaId, 7, '0');
                   
                   String addressCode = addressMap.get(applyData.getServiceCenter() + applyData.getAreaAddress());
                   String zipCode = (applyData.getZip() == null || applyData.getZip().length() < 3) ? "00000" : StringUtils.rightPad(applyData.getZip(), 5, '0');
				   if(center != null && !receipt && conjugateTrue ){		
				      FilesUtils.moveFileToDirectory(file, new File(Properties.getCheckedOkPath() + "G" + "/" + center + "/" + zipCode + addressCode + "_" + subAreaId), true);
					  normCounter++;
				   }else if(center != null && receipt && conjugateTrue ){
				      FilesUtils.moveFileToDirectory(file, new File(Properties.getReceiptOkPath() + "G" + "/" + center + "/" + zipCode + addressCode + "_" + subAreaId), true);
				   }else if(!conjugateTrue){
					   if(applyData.getExceptionStatus().equals("13") || applyData.getExceptionStatus().equals("14"))
					       FilesUtils.moveToDirectory(file, new File(Properties.getImgUncompletePath() + center + "/" ), true);
					   else
						   FilesUtils.moveToDirectory(file, new File(Properties.getDifficultFontPath() + center + "/" ), true);
					
				   }
                }catch(Exception e){
                	logger.error("", e);
                	e.printStackTrace();
                }
				
			} // else if(fromWhere == 'B' || fromWhere == 'T' || fromWhere == 'O') // 檔案名稱正確的檔案處理完畢
			
			
		} //end for(File file : txtFile2nd) 所有檔案處理完畢。如果之前有進行過作業，寫入檔案，通知另一個thread已處理完畢
	    //回傳順序 
		if(oldSerialNo == 0)
			return 0;
		else
		    return oldSerialNo - 1;		
	}
	
	public static boolean isRunning(){
		return running;
	}
}
