package com.fxdms.cathy.task;

import java.io.*;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FileFilterImpl;
import com.fxdms.util.FilesUtils;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.ErrorReport;

public class GpPolicyMerger {

	static Logger logger = Logger.getLogger(GpPolicyMerger.class);
	static int packNo = 100;
	private static File tmpFolder = new File(Properties.getPresPath(), "tmp");
	private static boolean receipt = false;
    private static String checkedOkPath = null;
    private static String receiptOkPath = null;

	private static DecimalFormat df = new DecimalFormat("0000");
	private static SimpleDateFormat sdf = Constant.yyyyMMdd;
	private static String cycleDateStr = sdf.format(new Date());
	private static boolean running = false;
	private static int serialNo = 0;
    private static boolean jobFinished = false;
	static {
		if(!tmpFolder.exists())
			tmpFolder.mkdirs();
		try {
			initSerialNo();
		} catch (BeansException | RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 初始化serialNo，如果程式重新啟動，不會弄錯
	// 依batch test online center及日期定好servial no
	public static void initSerialNo() throws BeansException, RemoteException {
		Date cycleDate = GroupInputdateParser.getGpInputDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(cycleDate);		
		List<Integer> serialNos = ((VoService) Constant.getContext().getBean("voServiceProxy")).findAfpMaxSerialNo(cal, "06", "G");
		if(serialNos != null && serialNos.size() > 0)
			serialNo = serialNos.get(0) == null ? 0 : serialNos.get(0);
	}

	// 融合的檔案名為GA + 轄區 + (B|O|T|R) + yyyyMMdd + df.format(serialNo)
	// FileUtils.moveFileToDirectory(file, new
	// File(Properties.getCheckedOkPath() + fromWhere + "/" + center + "/" +
	// applyData.getAreaId()), true);
	// 改名為檔案數(五位數) + areaId(七位)，listfiles會有自然排序
	// Properties.getCheckedOkPath() + fromWhere + "/" + center +
	// "/moveCompleted.ok"
	public synchronized static void startToRun() throws BeansException, RemoteException {
		if (running)
		   return;

		running = true;
		
		logger.info("applyData merger start to work.");				
		String cycleDateStr = sdf.format(GroupInputdateParser.getGpInputDate());
		
		// 跨日的話serialNo 重編
		if (!cycleDateStr.equals(GpPolicyMerger.cycleDateStr)) {
			serialNo = 0;
			GpPolicyMerger.cycleDateStr = cycleDateStr;
		}
		File checkedOk = new File(checkedOkPath);
		if (!checkedOk.exists())
			checkedOk.mkdirs();
		File[] fromWheres = checkedOk.listFiles(FileFilterImpl
				.getDirectoryFilter());

		if (fromWheres != null) {
			File[] areaIds = null;
			File processingFolder = null;
			try {
				for (File fromWhereDir : fromWheres) { //從G目錄開始
					String fromWhere = fromWhereDir.getName();
					logger.info("processing " + fromWhere);
					boolean mergeBegin = false;
					String center = null;
					File centerFolder = null;
					if (fromWhere.equals("G")) {
						File[] centers = fromWhereDir.listFiles(FileFilterImpl
								.getDirectoryFilter());
						logger.info(fromWhere + " centers length :" + centers.length);
						for (File centerDir : centers) {
							if (!receipt && new File(centerDir, "moveCompleted.ok")
									.exists()) {
								mergeBegin = true;
								center = centerDir.getName();
								centerFolder = centerDir;
								areaIds = centerFolder.listFiles(FileFilterImpl
										.getAlteredAreaIdDirFilter());
								logger.info(fromWhere + " " + center + " " + areaIds.length);
								processingFolder = centerDir;
								break;
							}else if(receipt && new File(centerDir, "moveCompleted.ok")
							        .exists() && new File(centerDir, "CADone.ok").exists()){
								mergeBegin = true;
								center = centerDir.getName();
								centerFolder = centerDir;
								areaIds = centerFolder.listFiles(FileFilterImpl
										.getAlteredAreaIdDirFilter());
								logger.info(fromWhere + " " + center + " " + areaIds.length);
								processingFolder = centerDir;
								break;
							}
						}
						logger.info("merge begin :" + mergeBegin);
					}
					if (mergeBegin) {
						
						com.salmat.pas.vo.Properties props = ((VoService) Constant
									.getContext().getBean(
											"voServiceProxy"))
									.getProperties();
						props.setProcessDone(false);
						((VoService) Constant.getContext().getBean("voServiceProxy"))
									.update(props);
						
						jobFinished = false;
						String newBatchName = null;
						int currentPacksSize = 0; //計算目前的合併檔案含有幾個applyData
						String fileSerialNo = null;
						//初始化serialNo;
						if(!receipt){						   
						    fileSerialNo = df.format(++serialNo);						   
						}
						for (File areaIdFolder : areaIds) {
							String folderNm = areaIdFolder.getName();
							//此folder下有幾個檔案，檔案規則2340001_00001_areaId
							String[] folderNmSplit = folderNm.split("_");
							Integer filesAmt = new Integer(folderNmSplit[1]);
							String areaId = folderNmSplit[2];
						    if(!receipt ){
							    newBatchName = "GA09B" + GpPolicyMerger.cycleDateStr + fileSerialNo;
							}else{
								newBatchName = null;
							}
							//計算數字，加入下個areaId的檔案時超過100時換一個名稱
							
							if(!receipt && currentPacksSize != 0 && (currentPacksSize + filesAmt) > packNo){
								//把前面的.tmp改成.DAT
								
								if(new File(tmpFolder, newBatchName + ".tmp").exists()){									
									new File(tmpFolder, newBatchName + ".tmp").renameTo(new File(Properties.getPresPath(), newBatchName + ".DAT"));
								}
								currentPacksSize = 0;
							    fileSerialNo = df.format(++serialNo);
								newBatchName = "GA09B" + GpPolicyMerger.cycleDateStr + fileSerialNo;								

							}
							AfpFile afpFile = null;
							if(newBatchName != null)
							   afpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(newBatchName);
							boolean update = true;
							if(afpFile == null){
								afpFile = new AfpFile();								
								afpFile.setInsertDate(new Date());
								update = false;
							}
							afpFile.setReceipt(receipt);
							afpFile.setNewBatchName(newBatchName);
							afpFile.setBatchOrOnline(fromWhere);
							afpFile.setCenter(center);
							String areaIdSub = areaId.replaceAll("&", "*");
							if(areaIdSub.length() > 7)
								areaIdSub = areaIdSub.substring(0, 7);
							afpFile.setAreaId(areaIdSub);
							long newBatchNo;
							List<Long> maxBatchNos = ((VoService) Constant.getContext().getBean("voServiceProxy")).findMaxBatNo();
							if (maxBatchNos != null && maxBatchNos.size() > 0) {
								newBatchNo = (maxBatchNos.get(0) == null) ? 0 : maxBatchNos.get(0);
							} else {
								newBatchNo = 0;
							}
							afpFile.setNewBatchNo(++newBatchNo);
							afpFile.setSerialNo(newBatchNo);							
							afpFile.setCenterSerialNo(serialNo);
							afpFile.setGpged(false);
							afpFile.setTransfered(false);
							afpFile.setUnziped(false);
							afpFile.setUpdateDate(new Date());
							afpFile.setZiped(false);
							afpFile.setFeedback(true); //不產生回饋檔，故設定true
							afpFile.setStatus("轉檔中");
							if((!receipt && newBatchName != null) 
									|| (receipt && newBatchName != null)){
							   if(update)
							       ((VoService) Constant.getContext().getBean("voServiceProxy")).update(afpFile);
							   else
								   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(afpFile);
							}
							Set<ApplyData> appDataSet = afpFile.getApplyDatas();
							File[] applyDatas = areaIdFolder
									.listFiles(FileFilterImpl.getFileFilter());
							if(applyDatas != null){		
							   Date cycleDate = null;
							   for (File applyData : applyDatas) {
								   ApplyData applyDataDb = ((VoService) Constant.getContext().getBean("voServiceProxy")).getApplyData(applyData.getName());
								 //如果是簽收單時，要找出共軛的afpfilenm
								   if(receipt){									   									   
									   List<ApplyData> policyApps = null;									   
										for(String policyNo : applyDataDb.getPolicyNoSet()){
										   policyApps = ((VoService) Constant.getContext().getBean("voServiceProxy")).findByApplyNoAndPolicyNoAndCenerCycleReprint(applyDataDb.getCycleDate(), applyDataDb.getApplyNo(), policyNo, center, false, applyDataDb.getReprint());
										   if(policyApps != null && policyApps.size() > 0)
											   break;
										}									   
									   //如果找不到共軛的簽收單
									   if(policyApps == null || policyApps.size() == 0){										   
											applyDataDb.setPolicyStatus("16");
											applyDataDb.setExceptionStatus("16");
											applyDataDb.setVerifyResult("此簽收單找不到對應保單，不進行轉檔");
											FilesUtils.moveToDirectory(applyData, new File(Properties.getImgUncompletePath()), true);
											ErrorReport er = new ErrorReport();
											er.setTitle("Not_Found_Policy");
											er.setErrHappenTime(new Date());
											er.setErrorType("errFileNm");
											er.setOldBatchName(applyData.getName());
											er.setReported(false);
											er.setMessageBody(applyData.getName() + "找不到對應的保單，可能發單錯誤");
											((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
											continue;
									   }else{
										   String policyNewBatchName = policyApps.get(0).getNewBatchName();
										   if(policyNewBatchName == null || policyNewBatchName.trim().equals("")){
											   applyDataDb.setPolicyStatus("16");
											   applyDataDb.setExceptionStatus("16");
											   applyDataDb.setVerifyResult("此簽收單對應保單有錯誤發生，故不進行轉檔");
											   FilesUtils.moveToDirectory(applyData, new File(Properties.getImgUncompletePath()), true);
										   }else{	
											   
											   AfpFile conjugatedAfp = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(policyNewBatchName); 
											   //CA替換成SG
											   newBatchName = policyNewBatchName.replaceFirst("GA", "GG");
											   AfpFile recAfpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(newBatchName);	
											   afpFile.setNewBatchName(newBatchName);
											   
											   applyDataDb.setNewBatchName(newBatchName);
											   applyDataDb.setNewBatchNo(policyApps.get(0).getNewBatchNo());
											   if(recAfpFile != null){
												   //如果已經有了，就什麼事都不做
											   }else{
												   //找出最大serialNo
												   maxBatchNos = ((VoService) Constant.getContext().getBean("voServiceProxy")).findMaxBatNo();
												   if (maxBatchNos != null && maxBatchNos.size() > 0) {
														newBatchNo = (maxBatchNos.get(0) == null) ? 0 : maxBatchNos.get(0);
												   } else {
														newBatchNo = 0;
												   }
												   newBatchNo++;
												   //batchSerialNo[new Integer(center) - 1] = (++centerSerialNo);
												   if(conjugatedAfp != null)
												      afpFile.setCenterSerialNo(conjugatedAfp.getCenterSerialNo());
												   
												   afpFile.setNewBatchNo(newBatchNo);
												   afpFile.setSerialNo(newBatchNo);
												   cycleDate = applyDataDb.getCycleDate();
												   if(afpFile.getCycleDate() == null){
													   afpFile.setCycleDate(cycleDate);
												   }
											      ((VoService) Constant.getContext().getBean("voServiceProxy")).save(afpFile);
											   }
											   
										   }
									   }
								   }else if(receipt && fromWhere.equals("T")){
									   
								   } 
								   int newSerialNo = ++currentPacksSize;
								   logger.info(applyData.getAbsolutePath() + applyData.getName());
								   
								   
								   //設定cycle date
								   cycleDate = applyDataDb.getCycleDate();
								   if((!receipt || fromWhere.equals("T")) && afpFile.getCycleDate() == null){
									   afpFile.setCycleDate(cycleDate);
									   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(afpFile);
								   }
								   //如果單一資料夾就超過100，那就必須拆成兩個以上的batch
								   if(!receipt && currentPacksSize > packNo){
									   if(new File(tmpFolder, newBatchName + ".tmp").exists())
											new File(tmpFolder, newBatchName + ".tmp").renameTo(new File(Properties.getPresPath(), newBatchName + ".DAT"));
									   
									   //重新計算 
									   currentPacksSize = 1;
									   newSerialNo = 1;
									   newBatchNo++;
									   if(!receipt){											
										  fileSerialNo = df.format(++serialNo);											
									   }

								       newBatchName = "GA09B" + GpPolicyMerger.cycleDateStr + fileSerialNo;									   
									   afpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(newBatchName);
									   update = true; 
									   if(afpFile == null){
											afpFile = new AfpFile();
											afpFile.setInsertDate(new Date());
											update = false;
										}
										afpFile.setNewBatchName(newBatchName);
										afpFile.setBatchOrOnline(fromWhere);
										afpFile.setCenter(center);
										afpFile.setNewBatchNo(newBatchNo);
										afpFile.setSerialNo(newBatchNo);
										afpFile.setCycleDate(cycleDate);
										afpFile.setPresTime(new Date());
										afpFile.setGpged(false);
										afpFile.setTransfered(false);
										afpFile.setUnziped(false);
										afpFile.setUpdateDate(new Date());
										afpFile.setZiped(false);
										if(update)
										   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(afpFile);
										else
										   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(afpFile);
								   }
								   if(newBatchName != null && !receipt ){
								      applyDataDb.setNewBatchName(newBatchName);
								   }
								   applyDataDb.setNewBatchNo(newBatchNo);
								   applyDataDb.setNewSerialNo(newSerialNo);
								   applyDataDb.setPresTime(new Date());
								   boolean substract = applyDataDb.getSubstract() == null ? false : applyDataDb.getSubstract();
								   if(!substract)
									   applyDataDb.setPolicyStatus("15");
								   else
									   applyDataDb.setPolicyStatus("18");										
								   applyDataDb.setUpdateDate(new Date());										
								   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyDataDb);
								   
								   //非抽件才合併
								   if(!substract){
									   FileInputStream fis = null;
									   BufferedInputStream bis = null;
									   BufferedOutputStream bos = null;
									   FileOutputStream fos = null;
									   //開始進行合併保單
									   try {
										   byte[] bufferedB = new byte[2048];
										   fos = new FileOutputStream(new File(tmpFolder, newBatchName + ".tmp"), true);
										   bos = new BufferedOutputStream(fos);
										   fis = new FileInputStream(applyData);
										   bis = new BufferedInputStream(fis);	
										   int readLen;
										   while ((readLen = bis.read(bufferedB)) > 0) {
											   bos.write(bufferedB, 0, readLen);
										   }										
										   bos.flush();
										   fos.flush();
									   }catch(Exception e){
											logger.error("", e);
											ErrorReport er = new ErrorReport();
											er.setErrHappenTime(new Date());
											er.setErrorType("exception");
											er.setOldBatchName("");
											er.setReported(false);
											er.setException(true);
											er.setMessageBody("exception happen:"  + e.getMessage());
											er.setTitle("exception happened");
											((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);     
										}finally{
											if(bos != null)
												bos.close();
											if(fos != null)
												fos.close();
											if(bis != null)
												bis.close();
											if(fis != null)
												fis.close();
										}										  
									}														   
								   //做完一個就移到備份目錄								   								   
								   FilesUtils.moveFileToDirectory(applyData, new File(Properties.getGpBackupFolder() +  "DATA/" + Constant.yyyy_MM_dd.format(GroupInputdateParser.getGpInputDate()) + "/applyData/"), true);

							   } //for (File applyData : applyDatas)							   							   
							}							
							//做完就刪除
                            FileUtils.deleteDirectory(areaIdFolder);
						}
						
						File[] files = tmpFolder.listFiles();
						for(File file : files){
						   if(file.isFile() && file.getName().endsWith(".tmp") 
								   && (file.getName().toUpperCase().startsWith("GA09B") || file.getName().toUpperCase().startsWith("GG09B")))
							   file.renameTo(new File(Properties.getPresPath(), file.getName().substring(0, file.getName().length() - 4) + ".DAT"));
						}
												
						
						if(new File(centerFolder, "moveCompleted.ok").exists()){
							FileUtils.forceDelete(new File(centerFolder, "moveCompleted.ok"));						
							File conjugateFolder = new File(Properties.getReceiptOkPath() + fromWhere + "/" + center);
							ErrorReport eReport = new ErrorReport();
			            	eReport.setErrHappenTime(new Date());
			            	eReport.setErrorType("PolicyMerge");
			            	eReport.setOldBatchName(null);
			            	eReport.setReported(true);		
							if(!receipt && conjugateFolder.list() != null && conjugateFolder.list().length > 0){														
								eReport.setMessageBody("處理團險保單資料完成，送往PRES轉檔中");								
								FileWriter fw = null;
								if(center != null){								   
								    fw = new FileWriter(Properties.getReceiptOkPath() + fromWhere + "/" + center  + "/CADone.ok" );
								    fw.write("ok");
								    fw.flush();
								    fw.close();
								}								   
							}else if(receipt){
								eReport.setMessageBody("處理團險簽收單回條資料完成，送往PRES轉檔中");
								mergeBegin = false;
								jobFinished = true;
								//設定好通知pdf merger可以開始工作
								
								com.salmat.pas.vo.Properties properties = ((VoService) Constant.getContext().getBean(
									  "voServiceProxy")).getProperties(); 
								properties.setGroupSentToPres(true);
								((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);
								
								if(new File(centerFolder, "CADone.ok").exists())
								   FileUtils.forceDelete(new File(centerFolder, "CADone.ok"));
							}
							eReport.setTitle("PRES轉檔中");
							((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
						}

					}
				}// for(File fromWhereDir :fromWheres)
			} catch (Exception e) {
				logger.error("", e);
				ErrorReport er = new ErrorReport();
				er.setErrHappenTime(new Date());
				er.setErrorType("exception");
				er.setOldBatchName("");
				er.setReported(false);
				er.setException(true);
				er.setMessageBody("exception happen:"  + e.getMessage());
				er.setTitle("exception happened");
				try{
				   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
				}catch(Exception ex){
					logger.error("", e);
				}
			} finally {
				running = false;
			}

		}		

		logger.info("applyData merger stop.");
		running = false;
	}

	public static String getCheckedOkPath() {
		return checkedOkPath;
	}

	public static void setCheckedOkPath(String checkedOkPath) {
		GpPolicyMerger.checkedOkPath = checkedOkPath;
	}

	public static boolean isReceipt() {
		return receipt;
	}

	public static void setReceipt(boolean receipt) {
		GpPolicyMerger.receipt = receipt;
	}
	public static boolean isRunning(){
		return running;
	}

	public static boolean isJobFinished() {
		return jobFinished;
	}

	public static void setJobFinished(boolean jobFinished) {
		GpPolicyMerger.jobFinished = jobFinished;
	}
	
	
}
