package com.fxdms.cathy.task;

import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FileFilterImpl;
import com.fxdms.util.FilesUtils;
import com.fxdms.util.PdfFileUtil;
import com.fxdms.util.ZipUtils;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.ErrorReport;

public class GpPdfSorter {
	private static Date fileDate = null;

	static Logger logger = Logger.getLogger(GpPdfSorter.class);	
	private static boolean running = false;
	private static File cardOkFile = new File(new File(com.fxdms.cathy.bo.Properties.getGroupInFolder(), "OK"), "CARD.ok");
	private static File backupFolder = new File(com.fxdms.cathy.bo.Properties.getGpBackupFolder(), "INSUR_CARD_PDF");
	public static void startToRun() throws BeansException, RemoteException {		
		Date today = new Date();		
		if(!cardOkFile.exists())
			return;
		//本身在執行時或afpListener或ZipListener在執行時，自己是不能執行的
		//因為此程式會更新保單的DB，兩邊都同時在更新時，有可能造成資料不一致的狀況
		if(running || AfpListener.isRunning() || ZipListener.isRunning())
			return;		
		try{			
			running = true;
			logger.info("pdf sorter start.");
			com.salmat.pas.vo.Properties properties = ((VoService) Constant.getContext().getBean(
					"voServiceProxy")).getProperties(); 
			//如果保單融合完成才開始進行
			//if(properties.getGroupSentToPres() != null && properties.getGroupSentToPres()){
			//不要從資料庫判斷，如果下午有另一批時會有問題，要增加太多判斷式
			if(GpPolicyMerger.isJobFinished()){
				Date cycleDate = GroupInputdateParser.getGpInputDate();
				List<ApplyData> applyDatas = ((VoService) Constant.getContext().getBean("voServiceProxy")).findForPdf(cycleDate);
				//目前只處理06的
				File taipei2 = new File(new File(Properties.getGroupInFolder(), "INSUR_CARD"), "06");
				if(!taipei2.exists())
					taipei2.mkdirs();
				File[] centerFolders = new File(Properties.getGroupInFolder(), "INSUR_CARD").listFiles(FileFilterImpl.getDirectoryFilter());
				//如果有其它的，都搬去06
				for(File centerFolder : centerFolders){					
					File[] files = centerFolder.listFiles(FileFilterImpl.getCommonPdfFileFilter());
					for(File file : files){
					   FileUtils.copyFileToDirectory(file, new File(backupFolder, Constant.yyyy_MM_dd.format(cycleDate) + "/" + centerFolder.getName()), true);
					   if(!centerFolder.getName().equals("06"))
					      FilesUtils.moveFileToDirectory(file, new File(new File(centerFolder.getParent()), "06"), true);
					}										
				}
				File[] pdfFiles = taipei2.listFiles(FileFilterImpl.getCommonPdfFileFilter());
				//processSingleFile(pdfFiles);
				
				
				pdfFiles = taipei2.listFiles(FileFilterImpl.getCommonPdfFileFilter());
				File forPdfSort = new File(new File(Properties.getCheckedOkPath()).getParentFile(), "pdfSort");
				if(!forPdfSort.exists())
					forPdfSort.mkdirs();
				for(File pdfFile : pdfFiles){
					String fileNm = pdfFile.getName(); 
					if(pdfFile.getName().toUpperCase().endsWith(".PDF")){
						fileNm = fileNm.substring(0, fileNm.length() - 4);
					}
					String[] name = fileNm.split("_");
					if(name.length >= 1){		
					   for(ApplyData applyData : applyDatas){
						  String policyNo = null;
						  for(String policyNumber : applyData.getPolicyNoSet())
						       policyNo = policyNumber;
						  //logger.info(policyNos.toUpperCase() + "|" + "," + name[0] + "|" + "," + (name[0]).indexOf(policyNos.toUpperCase()));
						  //如果保單號碼相同，就移過去pdfSort目錄去整理
						  String filePolicyNo = "";
						  //06_SI_032_G500008914.pdf 目前設定的檔名
						  if(name.length >= 4)
							  filePolicyNo = name[3].toUpperCase();
					      else
						      filePolicyNo = name[name.length - 1].toUpperCase();
						  
						  //只取前十個字元
						  if(filePolicyNo.length() > 10 )
						      filePolicyNo = filePolicyNo.substring(0, 10);
						  
						  if( policyNo.toUpperCase().indexOf(filePolicyNo) >= 0 ){
							  logger.info(applyData.getOldBatchName() + ":" + "has insurance card.");
							  applyData.setHaveInsureCard(true); //把對應保單設為true
							  applyData.setUpdateDate(today);
							  //update原來的applyData，告知有保險證
							  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
							  //檔名規則同目錄名:GA09B201410170001_001_G000003133103
							  String folderNm = applyData.getNewBatchName() + "_" + StringUtils.leftPad(applyData.getNewSerialNo() + "", 3, '0') + "_" + policyNo;							  
							  File destFolder = new File(forPdfSort, folderNm);
							  File[] anotherFiles = null;
							  if(destFolder.exists()){
								  if(destFolder.listFiles(FileFilterImpl.getCommonPdfFileFilter()).length > 0){
									  anotherFiles =  destFolder.listFiles(FileFilterImpl.getCommonPdfFileFilter());
								  }
							  }
							  FilesUtils.moveFileToDirectory(pdfFile, destFolder, true);
							  String oldBatchName = Constant.yyyy_MM_dd.format(cycleDate) + "_" + pdfFile.getName();
							  //如果有其它的檔，先融合成一個
							  if(anotherFiles != null && anotherFiles.length > 0){
								  File pdfFileIn = new File(destFolder, anotherFiles[0].getName());
								  File pdfFileTmp = new File(destFolder, anotherFiles[0].getName() + ".tmp");
								  
								  anotherFiles =  destFolder.listFiles(FileFilterImpl.getCommonPdfFileFilter());
								  if(anotherFiles != null && anotherFiles.length > 1){
								     List<File> forMerge = new ArrayList<File>();
								     for(File file : anotherFiles){
									     forMerge.add(file);
								     }
								  
								     PdfFileUtil.doMergeBigPdf(forMerge, pdfFileTmp.getAbsolutePath());
								     for(File file : anotherFiles){
									     FileUtils.forceDelete(file);
								     }
								     pdfFileTmp.renameTo(pdfFileIn);		
								     oldBatchName = Constant.yyyy_MM_dd.format(cycleDate) + "_" + pdfFileIn.getName();
								  }
							  }
							  
							  
							  if(oldBatchName.toUpperCase().endsWith(".PDF")){
								  oldBatchName = oldBatchName.substring(0, oldBatchName.length() - 4); 
							  }
							  
							  ApplyData pdfData = ((VoService) Constant.getContext().getBean("voServiceProxy")).getApplyData(oldBatchName);
							  if(pdfData != null && pdfData.getNewBatchName() != null){
								  //如果有newBatchName ，代表是之前做過的，先刪除
								  ((VoService) Constant.getContext().getBean("voServiceProxy")).deleteApplyData(pdfData);
								  pdfData = null;
							  }
							  if(pdfData == null){
							     pdfData = new ApplyData();
							     BeanUtils.copyProperties(applyData, pdfData);
							     pdfData.setOldBatchName(oldBatchName);
							     pdfData.setHaveInsureCard(false);
							     pdfData.setNewBatchName(null);
							     pdfData.setPolicyPDF(oldBatchName);
							     pdfData.setFileNm(pdfFile.getName());
							     pdfData.setGroupInsure(true);
							     pdfData.setPresTime(null);
							     pdfData.setPrintTime(null);
							     pdfData.setBindTime(null);
							     pdfData.setVerifyTime(null);
							     pdfData.setReceipt(null);
							     pdfData.setPackTime(null);
							     pdfData.setBankReceiptId(null);
							     pdfData.setBankReceipts(null);
							     pdfData.setDeliverTime(null);							     
							     pdfData.setPolicyStatus("20");
							     pdfData.setVerifyResult(null);
							     pdfData.setUniqueNo(null);
							     pdfData.setInsertDate(new Date());
							     pdfData.setUpdateDate(new Date());
							     ((VoService) Constant.getContext().getBean("voServiceProxy")).save(pdfData);
							  }							  
							  break;
						  }
					   }
					   
					}		
					if(pdfFile.exists()){
						logger.info("couldn't find match policyNo: " + pdfFile.getName());
						FilesUtils.forceDelete(pdfFile);
						
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("InsureCard");
						er.setOldBatchName(pdfFile.getName());
						er.setReported(false);
						er.setException(true);
						er.setMessageBody("團險證：" + pdfFile.getName() + "。找不到對映的保單");
						er.setTitle("Insure Card Error");				
						((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
						
					}
				}
				
				File[] allFolders = forPdfSort.listFiles(FileFilterImpl.getDirectoryFilter());
				Map<String, List<File>>  zipFileMap = new HashMap<String, List<File>>();
				//String oldBatchName = applyData.getNewBatchName() + "_" + StringUtils.leftPad(applyData.getNewSerialNo() + "", 3, '0') + "_" + name[0];
				for(File folder : allFolders){
					//d:\cathayTransfer\pdfSort\GA09B201507220001_002_G30819-027104
					String pdfName = folder.getName();
					//zip的名字，GA改成PD 
					String pdfZipNm = "PD" + pdfName.split("_")[0].substring(2);					
					List<File> pdfList = zipFileMap.get(pdfZipNm);
					if(pdfList == null){
						pdfList = new ArrayList<File>();
					}					
					File[] allPdfFiles = folder.listFiles(FileFilterImpl.getCommonPdfFileFilter());
					for(File file : allPdfFiles){
						pdfList.add(file);
					}
					zipFileMap.put(pdfZipNm, pdfList);
				}
				
				Set<String> keySet = zipFileMap.keySet();
				//把所有的檔案融合成一個
				for(String key : keySet){
					boolean update = false;
					AfpFile afpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(key);
					AfpFile oriAfpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(key.replaceFirst("PD", "GA"));
					if(afpFile != null){
						update = true;
						afpFile = new AfpFile();
					}					
					long newBatchNo = 0;
					List<Long> maxBatchNos = ((VoService) Constant.getContext().getBean("voServiceProxy")).findMaxBatNo();					
					if (maxBatchNos != null && maxBatchNos.size() > 0) {
						newBatchNo = (maxBatchNos.get(0) == null) ? 0 : maxBatchNos.get(0);
					} else {
						newBatchNo = 0;
					}
					afpFile = new AfpFile();
					afpFile.setNewBatchName(key);
					afpFile.setAfpFileNm(key + ".pdf");
					afpFile.setBatchOrOnline("G");
					afpFile.setCenter("06");
					if(oriAfpFile != null)
						afpFile.setCenterSerialNo(oriAfpFile.getCenterSerialNo());						
					afpFile.setCycleDate(cycleDate);
					afpFile.setFeedback(true);
					afpFile.setInsertDate(today);
					afpFile.setUpdateDate(today);
					afpFile.setPresTime(today);
					afpFile.setNewBatchNo(++newBatchNo);
					afpFile.setSerialNo(afpFile.getNewBatchNo());
					afpFile.setZiped(true);
					if(!update)
					   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(afpFile);
				    else
					   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(afpFile);
					File pdfTmp = new File(forPdfSort, key + ".tmp");
					File pdfFile = new File(forPdfSort, key + ".pdf");
					List<File> files = zipFileMap.get(key);
					int pageCounter = 0;
					for(File file : files){
						int pages = PdfFileUtil.getPDFPageCount(file.getAbsolutePath());
						pageCounter += pages;
						String oldBatchName = Constant.yyyy_MM_dd.format(cycleDate) + "_" +	file.getName();
						if(oldBatchName.toUpperCase().endsWith(".PDF")){
						   oldBatchName = oldBatchName.substring(0, oldBatchName.length() - 4); 
						}
						ApplyData applyData = ((VoService) Constant.getContext().getBean("voServiceProxy")).getApplyData(oldBatchName);
						if(applyData != null){
						   applyData.setA4Page(pages);
						   applyData.setTotalPage(pages);
						   applyData.setAfpBeginPage(pageCounter - pages + 1);
						   applyData.setAfpEndPage(pageCounter);
						   applyData.setPolicyStatus("20");
						   applyData.setNewBatchName(key);
						   applyData.setUpdateDate(new Date());
						   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);						   
						}						
					}
					
					PdfFileUtil.doMergeBigPdf(files, pdfTmp.getAbsolutePath());
					//加入頁數 
					PdfFileUtil.addPagesNumber(pdfTmp, pdfFile);
					FilesUtils.forceDelete(pdfTmp);
					afpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(key);
					afpFile.setPages(pageCounter);
					if(pdfFile.exists())
					   afpFile.setFileDate(new Date(pdfFile.lastModified()));
					afpFile.setSheets(pageCounter);
					afpFile.setStatus("回傳中");
				   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(afpFile);
										
					File tmpZipFile = new File(Properties.getZipTmpPath(),
							key + ".tmp");
					File destZipFile = new File(Properties.getZipTmpPath(),
							key + ".zip");
					
				    File[] forPack = new File[1];
				    forPack[0] = pdfFile;
					ZipUtils.packFile(forPack, tmpZipFile);					
					tmpZipFile.renameTo(destZipFile);
					try{
					   FileUtils.forceDelete(pdfFile);
					}catch(Exception e){
						logger.error("", e);
					}
					
				}
				//merge完後全部folder都刪除
				for(File folder : allFolders){
					FilesUtils.deleteDirectory(folder);
				}
				//處理完後刪除OK檔案
				FileUtils.forceDelete(cardOkFile);
				
				properties = ((VoService) Constant.getContext().getBean(
						"voServiceProxy")).getProperties(); 
				properties.setGroupSentToPres(false);
				((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);
				GpPolicyMerger.setJobFinished(false);
				
				ErrorReport er = new ErrorReport();
				er.setErrHappenTime(new Date());
				er.setErrorType("InsureCard");
				er.setOldBatchName(null);
				er.setReported(true);
				er.setMessageBody("團險證處理完畢");
				er.setTitle("Insure Card processed done.");				
				((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
				
				
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
			try{
			   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
			}catch(Exception ex){
			    logger.error("", e);
			}
			
		} finally {
			
			running = false;			

		}

		logger.info("pdf sorter stop.");
	}

	private static void processSingleFile(File[] pdfFiles) {
		// TODO Auto-generated method stub
		for(File pdfFile : pdfFiles){
			/*
			 * 這段是用來處理如果不分檔，只有一個pdf的狀況時
			 * 將會把這個pdfFile依保單名拆成數個小檔再處理
			PDDocument pdfDoc = null;
			File rasFile = null;
			RandomAccessFile ras = null;
			try {			
				rasFile = new File( UUID.randomUUID() + "");
				ras = new RandomAccessFile(rasFile, "rw");
				pdfDoc = PDDocument.load(pdfFile, ras);						
			} catch (IOException e) {
				logger.error("", e);
				e.printStackTrace();
						  
			} 
			String extractTxt = PdfFileUtil.extractTxt(pdfDoc);
			String[] lines = extractTxt.split(System.getProperty("line.separator"));
			Map<String, Integer[]> policyPage = new HashMap<String, Integer[]>();
			String prevPolicyNo = null;
			
			for(String line : lines){
				
			}
			*/
		}
	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		GpPdfSorter.running = running;
	}

	public static Date getFileDate() {
		return fileDate;
	}

	public static File getCardOkFile() {
		return cardOkFile;
	}

	public static void setCardOkFile(File cardOkFile) {
		GpPdfSorter.cardOkFile = cardOkFile;
	}
	
	
}
