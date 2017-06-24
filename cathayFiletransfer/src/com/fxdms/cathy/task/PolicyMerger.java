package com.fxdms.cathy.task;

import java.io.*;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

public class PolicyMerger {

	static Logger logger = Logger.getLogger(PolicyMerger.class);
	static int packNo = 100;
	private static File tmpFolder = new File(Properties.getPresPath(), "tmp");
	private static boolean receipt = false;
    private static String checkedOkPath = null;
	private static int[] batchSerialNo = new int[Properties.getMaxCenter()];
	private static int[] onlineSerialNo = new int[Properties.getMaxCenter()];
	private static int testSerialNo = 0;
	
	private static DecimalFormat df = new DecimalFormat("0000");
	private static SimpleDateFormat sdf = Constant.yyyyMMdd;
	private static String cycleDateStr = sdf.format(InputdateParser.getInputDate());
	private static boolean running = false;
	private static boolean mergeDone = false;

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
	// 依batch test online center及日期定好serial no.
	public static void initSerialNo() throws BeansException, RemoteException {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(InputdateParser.getInputDate());
		List<Integer> serialNo = ((VoService) Constant.getContext().getBean("voServiceProxy")).findAfpMaxSerialNo(cal, "00", "T");
		
		
		if (serialNo != null && serialNo.size() > 0) {
			testSerialNo = (serialNo.get(0) == null ? 0 : serialNo.get(0));
		}
		//logger.info("new batch no is:" + newBatchNo);
		for (int i = 1; i <= Properties.getMaxCenter(); i++) {
			String center;
			if (i < 10)
				center = "0" + i;
			else
				center = i + "";
			//serialNo = session.getNamedQuery("AfpFile.findMaxSerialNo")
				//	.setDate(0, cal.getTime()).setString(1, center)
					//.setString(2, "B").list();
			serialNo = ((VoService) Constant.getContext().getBean("voServiceProxy")).findAfpMaxSerialNo(cal, center, "B");
			if (serialNo != null && serialNo.size() > 0)
				batchSerialNo[i - 1] = serialNo.get(0) == null ? 0 : serialNo
						.get(0);
			logger.info("batch serialNo " + (i + 1) + ":" + batchSerialNo[i - 1]);

			//serialNo = session.getNamedQuery("AfpFile.findMaxSerialNo")
				//	.setDate(0, cal.getTime()).setString(1, center)
					//.setString(2, "O").list();
			serialNo = ((VoService) Constant.getContext().getBean("voServiceProxy")).findAfpMaxSerialNo(cal, center, "O");
			if (serialNo != null && serialNo.size() > 0)
				onlineSerialNo[i - 1] = (serialNo.get(0) == null ? 0 : serialNo
						.get(0));
			logger.info("online serialNo " + (i + 1) + ":" + onlineSerialNo[i - 1]);
									
		}

	}

	// 融合的檔案名為CA + 轄區 + (B|O|T|R) + yyyyMMdd + df.format(serialNo)
	// FileUtils.moveFileToDirectory(file, new
	// File(Properties.getCheckedOkPath() + fromWhere + "/" + center + "/" +
	// applyData.getAreaId()), true);
	// 改名為地址 + 檔案數(五位數) + areaId(七位)，listfiles會有自然排序
	// Properties.getCheckedOkPath() + fromWhere + "/" + center +
	// "/moveCompleted.ok"
	public synchronized static void startToRun() throws BeansException, RemoteException {
		if (running)
		   return;
		
		logger.info("applyData merger start to work.");
		
		String cycleDateStr = sdf.format(InputdateParser.getInputDate());
		
		// 跨日的話serialNo 重編
		if (!cycleDateStr.equals(PolicyMerger.cycleDateStr)) {
			for (int i = 1; i <= Properties.getMaxCenter(); i++) {
				batchSerialNo[i - 1] = 0;
				onlineSerialNo[i - 1] = 0;
			}
			testSerialNo = 0;
			PolicyMerger.cycleDateStr = cycleDateStr;
		}
		File checkedOk = new File(checkedOkPath);
		if (!checkedOk.exists())
			checkedOk.mkdirs();
		File[] fromWheres = checkedOk.listFiles(FileFilterImpl
				.getDirectoryFilter());

		if (fromWheres != null) {
			File[] areaIds = null;
			try {
				running = true;
				
				for (File fromWhereDir : fromWheres) { //從T B O R等目錄開始
					String fromWhere = fromWhereDir.getName();
					logger.info("processing " + fromWhere);
					boolean mergeBegin = false;
					String center = null;
					File centerFolder = null;
					if (fromWhere.equals("O") || fromWhere.equals("B")
							|| fromWhere.equals("T")) {
						// O和B時多一層center
						// T也是多一層，是00
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
								break;
							}else if(receipt && new File(centerDir, "moveCompleted.ok")
							        .exists() && new File(centerDir, "CADone.ok").exists() && !fromWhere.equals("T")){
								mergeBegin = true;
								center = centerDir.getName();
								centerFolder = centerDir;
								areaIds = centerFolder.listFiles(FileFilterImpl
										.getAlteredAreaIdDirFilter());
								logger.info(fromWhere + " " + center + " " + areaIds.length);								
								break;
							}else if(receipt && new File(centerDir, "moveCompleted.ok")
					             .exists() && fromWhere.equals("T")){
						        mergeBegin = true;
						        center = centerDir.getName();
						        centerFolder = centerDir;
						        areaIds = centerFolder.listFiles(FileFilterImpl
								   . getAlteredAreaIdDirFilter());
						        logger.info(fromWhere + " " + center + " " + areaIds.length);						        
						        break;
					        }
						}
						logger.info("merge begin :" + mergeBegin);
					}
					if (mergeBegin) {
						if(fromWhere.equals("B")){
							com.salmat.pas.vo.Properties properties = ((VoService) Constant
									.getContext().getBean(
											"voServiceProxy"))
									.getProperties();
							properties.setProcessDone(false);
							((VoService) Constant.getContext().getBean(
									"voServiceProxy"))
									.update(properties);
						}
						String newBatchName = null;
						int currentPacksSize = 0; //計算目前的合併檔案含有幾個applyData
						long newBatchNo = 0;
						List<Long> curMaxBatchNos = ((VoService) Constant.getContext().getBean("voServiceProxy")).findMaxBatNo();
						if (curMaxBatchNos != null && curMaxBatchNos.size() > 0) {
							newBatchNo = (curMaxBatchNos.get(0) == null) ? 0 : curMaxBatchNos.get(0);
						} else {
							newBatchNo = 0;
						}
						newBatchNo++; //會一直增加下去
						String fileSerialNo = null;
						//初始化serialNo;
						if(!receipt){
						   if(fromWhere.equals("B")){
						      fileSerialNo = df.format(++batchSerialNo[new Integer(center) - 1]);
						   }else if(fromWhere.equals("O")){
							  fileSerialNo = df.format(++onlineSerialNo[new Integer(center) - 1]);
						   }else if(fromWhere.equals("T")){
							  fileSerialNo = df.format(++testSerialNo);
						   }
						}else{
							if(fromWhere.equals("T")){
								fileSerialNo = df.format(++testSerialNo);
							}
						}
						boolean startRept = false;
						for (File areaIdFolder : areaIds) {
							String folderNm = areaIdFolder.getName();
							String [] folderNmSplit = folderNm.split("_");
							//此folder下有幾個檔案，檔案規則0001_00001_areaId
							Integer prefixNo = 0;
							try{
								prefixNo = new Integer(folderNmSplit[0].substring(folderNmSplit[0].length() - 4, folderNmSplit[0].length()));
								//prefixNo >= 20時是REPT及保補及查不到地址送北二 的
							}catch(Exception e){
								logger.error("", e);
							}
							Integer filesAmt = new Integer(folderNmSplit[1]);
							String areaId = folderNmSplit[2];
							/*
							if(filesAmt != areaIdFolder.list().length){
								filesAmt = areaIdFolder.list().length;
								ErrorReport er = new ErrorReport();
								er.setErrHappenTime(new Date());
								er.setErrorType("exception");
								er.setOldBatchName("");
								er.setReported(false);
								er.setMessageBody("在融合檔案時現超乎預期的的結果:實際檔案數" + areaIdFolder.list().length + "超過預想檔案數:" + filesAmt);
								er.setTitle("exception happened");
								((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);     	
							} 
							*/
						    if(!receipt ){
							    newBatchName = "CA" + center + fromWhere + PolicyMerger.cycleDateStr + fileSerialNo;
						    }else if(receipt && fromWhere.equals("T")){
						    	//如果是簽收單，而且是測試件
						    	newBatchName = "SG" + center + fromWhere + PolicyMerger.cycleDateStr + fileSerialNo;
							}else{
								newBatchName = null;
							}
							//計算數字，加入下個areaId的檔案時超過100時換一個名稱
						    //北二時的保補及補單，要獨立一包							
							if((!receipt && currentPacksSize != 0 && (currentPacksSize + filesAmt) > packNo) 
									|| (!startRept && "06".equals(center) && prefixNo >= 20)){
								//把前面的.tmp改成.DAT
								if(prefixNo >= 20)
									startRept = true;
								if(new File(tmpFolder, newBatchName + ".tmp").exists()){									
									new File(tmpFolder, newBatchName + ".tmp").renameTo(new File(Properties.getPresPath(), newBatchName + ".DAT"));
								}
								currentPacksSize = 0;
								newBatchNo++;
								
								if(fromWhere.equals("B")){
								   fileSerialNo = df.format(++batchSerialNo[new Integer(center) - 1]);
								}else if(fromWhere.equals("O")){
								   fileSerialNo = df.format(++onlineSerialNo[new Integer(center) - 1]);
								}else if(fromWhere.equals("T")){
								   fileSerialNo = df.format(++testSerialNo);
								}
								
								newBatchName = "CA" + center + fromWhere + PolicyMerger.cycleDateStr + fileSerialNo;								

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
							afpFile.setNewBatchNo(newBatchNo);
							afpFile.setSerialNo(newBatchNo);
							afpFile.setFeedback(false);
							if(fromWhere.equals("B")){
								afpFile.setCenterSerialNo(batchSerialNo[new Integer(center) - 1] );
							}else if(fromWhere.equals("O")){
								afpFile.setCenterSerialNo(onlineSerialNo[new Integer(center) - 1] );
							}if(fromWhere.equals("T")){
								afpFile.setCenterSerialNo(testSerialNo);
								afpFile.setFeedback(true);
							}
							
							afpFile.setGpged(false);
							afpFile.setTransfered(false);
							afpFile.setUnziped(false);
							afpFile.setUpdateDate(new Date());
							afpFile.setZiped(false);
							
							afpFile.setStatus("轉檔中");
							if((!receipt && newBatchName != null) 
									|| (receipt && fromWhere.equals("T") && newBatchName != null)){
							   if(update)
							       ((VoService) Constant.getContext().getBean("voServiceProxy")).update(afpFile);
							   else
								   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(afpFile);
							}

							File[] applyDatas = areaIdFolder
									.listFiles(FileFilterImpl.getFileFilter());
							if(applyDatas != null){		
							   Date cycleDate = null;
							   for (File applyData : applyDatas) {
								   ApplyData applyDataDb = ((VoService) Constant.getContext().getBean("voServiceProxy")).getApplyData(applyData.getName());
								 //如果是簽收單時，要找出共軛的afpfilenm
								   if(receipt && !fromWhere.equals("T")){									   									   
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
											er.setErrHappenTime(new Date());
											er.setTitle("Not_Found_Policy");
											er.setErrorType("errFileNm");
											er.setOldBatchName(applyData.getName());
											er.setReported(false);
											er.setMessageBody(applyData.getName() + "找不到對應的保單，可能發單錯誤");
											((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
											continue;
									   }else{
										   applyDataDb.setRecName(policyApps.get(0).getRecName());
										   if(policyApps.get(0).getPolicyNos() != null && !policyApps.get(0).getPolicyNos().equals(applyDataDb.getPolicyNos()))
										      applyDataDb.setPolicyNos(policyApps.get(0).getPolicyNos());
										   String policyNewBatchName = policyApps.get(0).getNewBatchName();
										   if(policyNewBatchName == null || policyNewBatchName.trim().equals("")){
											   applyDataDb.setPolicyStatus("16");
											   applyDataDb.setExceptionStatus("16");
											   applyDataDb.setVerifyResult("此簽收單對應保單有錯誤發生，故不進行轉檔");
											   FilesUtils.moveToDirectory(applyData, new File(Properties.getImgUncompletePath()), true);
										   }else{	
											   
											   AfpFile conjugatedAfp = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(policyNewBatchName); 
											   //CA替換成SG
											   newBatchName = policyNewBatchName.replaceFirst("CA", "SG");
											   AfpFile recAfpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(newBatchName);	
											   afpFile.setNewBatchName(newBatchName);
											   
											   applyDataDb.setNewBatchName(newBatchName);
											   applyDataDb.setNewBatchNo(policyApps.get(0).getNewBatchNo());
											   if(recAfpFile != null){
												   //如果已經有了，就什麼事都不做
											   }else{
												   //找出最大serialNo
												   List<Long> maxBatchNos = ((VoService) Constant.getContext().getBean("voServiceProxy")).findMaxBatNo();
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
											if(fromWhere.equals("B")){
											   fileSerialNo = df.format(++batchSerialNo[new Integer(center) - 1]);
											}else if(fromWhere.equals("O")){
											   fileSerialNo = df.format(++onlineSerialNo[new Integer(center) - 1]);
											}else if(fromWhere.equals("T")){
											   fileSerialNo = df.format(++testSerialNo);
											}
										}else{
										    if(fromWhere.equals("T")){
											   fileSerialNo = df.format(++testSerialNo);
											}
									    }

								       newBatchName = "CA" + center + fromWhere + PolicyMerger.cycleDateStr + fileSerialNo;									   
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
								   if(newBatchName != null && (!receipt || fromWhere.equals("T")) ){
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
								   if("B".equals(fromWhere) || "O".equals(fromWhere))
								      FilesUtils.moveFileToDirectory(applyData, new File(Properties.getBackupFolder() +  "DATA/" + Constant.yyyyMMdd.format(InputdateParser.getInputDate()) + "/applyData/"), true);
								   if("T".equals(fromWhere))
									  FilesUtils.moveFileToDirectory(applyData, new File(Properties.getBackupFolder() +  "TEST_DATA/" + PolicyMerger.cycleDateStr + "/applyData/"), true);
							   } //for (File applyData : applyDatas)							   							   
							   if(fromWhere.equals("T") && new File(tmpFolder, newBatchName + ".tmp").exists()){
								   FileInputStream fis = null;
								   BufferedInputStream bis = null;
								   BufferedOutputStream bos = null;
								   FileOutputStream fos = null;
								   //開始進行合併保單
								   try {
									   byte[] bufferedB = new byte[2048];
									   fos = new FileOutputStream(new File(tmpFolder, newBatchName + ".tmp2"), true);
									   bos = new BufferedOutputStream(fos);
									   fis = new FileInputStream(new File(tmpFolder, newBatchName + ".tmp"));
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
										//把.tmp刪除
										FileUtils.forceDelete(new File(tmpFolder, newBatchName + ".tmp"));
									}	
								   
								   
								   /*
								   FileInputStream fis = null;
								   InputStreamReader isr = null;
								   BufferedReader br = null;
								   FileOutputStream fos = null;
								   OutputStreamWriter osw = null;
								   BufferedWriter bw = null;
								   try{
									   fis = new FileInputStream(new File(tmpFolder, newBatchName + ".tmp"));
									   isr = new InputStreamReader(fis, "ms950");
									   br = new BufferedReader(isr);						
									   
									   
									   fos = new FileOutputStream(new File(tmpFolder, newBatchName + ".tmp2"), true);
									   osw = new OutputStreamWriter(fos, "ms950");
									   bw = new BufferedWriter(osw);
									   String line = null;
									   while ((line = br.readLine()) != null) {
										   String checkLine = line.toLowerCase();
										   if(checkLine.indexOf(".tiff") > 0 || checkLine.indexOf(".tif") > 0){
											   int addPoint = 0;
											   if(checkLine.indexOf("/image") > 0){
												   addPoint =  checkLine.indexOf("/image");												   
											   }else if(checkLine.indexOf("/law") > 0){
												   addPoint =  checkLine.indexOf("/law");
											   }
											   if(addPoint != 0){
												   String firstHalf = line.substring(0, addPoint); 
												   String secondHalf = line.substring(addPoint);
												   line = firstHalf + "/test" + secondHalf;
											   }											   
										   }
										   bw.write(line + "\r\n");										   
									   }
									   
									   bw.flush();
									   osw.flush();
									   fos.flush();
									   
									   br.close();
									   isr.close();
									   fis.close();
									   //把.tmp刪除
									   FileUtils.forceDelete(new File(tmpFolder, newBatchName + ".tmp"));
									   br = null;
									   isr = null;
									   fis = null;
								   }catch(Exception e){
									   e.printStackTrace();
									   logger.error("", e);
								   }finally{
									   if(br != null)
										   br.close();
									   if(isr != null)
										   isr.close();
									   if(fis != null)
										   fis.close();
									   
									   if(bw != null)
										   bw.close();
									   if(osw != null)
										   osw.close();
									   if(fos != null)
										   fos.close();
								   }
								   */
								   //如果.tmp2存在就改名
								   
							   }
							}							
							//做完就刪除
                            FileUtils.deleteDirectory(areaIdFolder);
						}
						
						File[] files = tmpFolder.listFiles();
						for(File file : files){
						   if(file.isFile() && file.getName().endsWith(".tmp")
								   && (file.getName().toUpperCase().startsWith("CA" + center) || file.getName().toUpperCase().startsWith("SG" + center)))
							   file.renameTo(new File(Properties.getPresPath(), file.getName().substring(0, file.getName().length() - 4) + ".DAT"));
						}
						
						if(new File(tmpFolder, newBatchName + ".tmp2").exists()){
							   new File(tmpFolder, newBatchName + ".tmp2").renameTo(new File(Properties.getPresPath(), newBatchName + ".DAT"));
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
								eReport.setMessageBody("處理" + centerFolder.getName() + "保單資料完成，送往PRES轉檔中");								
								FileWriter fw = null;
								if(center != null){								   
								    fw = new FileWriter(Properties.getReceiptOkPath() + fromWhere + "/" + center  + "/CADone.ok" );
								    fw.write("ok");
								    fw.flush();
								    fw.close();
								}								   
							}else if(receipt){
								eReport.setMessageBody("處理" + centerFolder.getName() + "簽收單回條資料完成，送往PRES轉檔中");
								if(new File(centerFolder, "CADone.ok").exists()){
								   setMergeDone(true);
								   FileUtils.forceDelete(new File(centerFolder, "CADone.ok"));
								}
							}
							eReport.setTitle("PRES轉檔中");
							((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
						}                        
					}
				}// for(File fromWhereDir :fromWheres)
			} catch (Exception e) {
				try{
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
				}catch(Exception ex){
					logger.error("", ex);
				}
			} finally {
				running = false;
			}
		}		

		logger.info("applyData merger stop.");
	}

	public static String getCheckedOkPath() {
		return checkedOkPath;
	}

	public static void setCheckedOkPath(String checkedOkPath) {
		PolicyMerger.checkedOkPath = checkedOkPath;
	}

	public static boolean isReceipt() {
		return receipt;
	}

	public static void setReceipt(boolean receipt) {
		PolicyMerger.receipt = receipt;
	}
	public static boolean isRunning(){
		return running;
	}

	public static boolean isMergeDone() {
		return mergeDone;
	}

	public static void setMergeDone(boolean mergeDone) {
		PolicyMerger.mergeDone = mergeDone;
	}
	
	
}
