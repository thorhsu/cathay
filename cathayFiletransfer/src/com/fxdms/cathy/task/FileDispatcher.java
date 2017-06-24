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
import com.ibm.icu.util.Calendar;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.BankReceipt;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.ImgFile;

public class FileDispatcher {
    
	static Logger logger = Logger.getLogger(FileDispatcher.class);
	static Area taipeiNo2 = null;	
	private static boolean running = false;
	private static boolean thirdStage = true; //第三階段時打開 

	private static Map<String, ImgFile> imgFilesOK = null;
	private static Map<String, ImgFile> imgFilesNotExist = null;
	private static Map<String, ImgFile> imgFilesError = null;	
	private static Map<String, Area> areaMap = null;	
	private static File folder692 = new File(Properties.getBackupFolder(), "DTATA692");
	private static File folder689 = new File(Properties.getBackupFolder(), "DTATA689");
	private static File reCheckFolder = new File(new File(Properties.getImgUncompletePath()).getParentFile(), "reCheck"); //保補件要等兩天
	
	private static DecimalFormat df = new DecimalFormat("00000");
	private static char fromWhere = 0 ;
	private static Map<String, String> addressMap = new HashMap<String, String>();	
	private static Map<String, String> mailAddMap = new HashMap<String, String>();
	private static Map<String, String> mailMap = new HashMap<String, String>();
	private static Map<String, String> normMap = new HashMap<String, String>();
	private static Map<String, String> reptMap = new HashMap<String, String>();
	private static File imgFolder = new File(Properties.getLocalImgPath());
	private static File testImgFolder = new File(Properties.getLocalTestImgPath());
	private static File myRecheckFolder = null;
	private static int imageErrorCounter = 0;
	private static int noReceiptCounter = 0;
	private static int noPolicyCounter = 0;
	private static Map<String, String> mappingServiceCenter; //國壽發的發單單位和服務中心對照
	private static int normCounter = 0;
	private static int reptCounter = 0;
	private static int convCounter = 0;
	private static int reisCounter = 0;
	private static Date cycleDate = InputdateParser.getInputDate();	
	 
	public char getFromWhere() {
		return fromWhere;
	}


	public static void startToRun() throws BeansException, RemoteException {
		cycleDate = InputdateParser.getInputDate();
		if(!folder692.exists())
			folder692.mkdirs();
		if(!folder689.exists())
			folder689.mkdirs();
		if(!reCheckFolder.exists())
			reCheckFolder.mkdirs();

		
		//時間設定更新影像檔的程式要早一點進行，如果更新還在進行中，先暫停此thread，等下一次再進行
		//如果有其它Thread正在run，也是跳出去
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
		
		
		if(running || ImgUpdater.isRunning() || AfpListener.isRunning() || GpFileDispatcher.isRunning())						
			return;
		
		
				
		logger.info("file dispatcher start to run");
		imageErrorCounter = 0;
		noReceiptCounter = 0;
		noPolicyCounter = 0;
		
		addressMap = new HashMap<String, String>();
		mailAddMap = new HashMap<String, String>();
		normMap = new HashMap<String, String>();
		reptMap = new HashMap<String, String>();
		mailMap = new HashMap<String, String>();
		
		normCounter = 0;
		reptCounter = 0;
		convCounter = 0;
		reisCounter = 0;
		try{
			running = true;
			File txtFolder = new File(Properties.getLocalPolicyTxtPath()); //一般batch件
			File addrFolder =  new File(txtFolder.getParent(), "ADDR");
			if(!txtFolder.exists())
				txtFolder.mkdirs();
			File onlineFolder = new File(Properties.getLocalPolicyOnlinePath()); //online件
			if(!onlineFolder.exists())
				onlineFolder.mkdirs();
			File txtTestFolder = new File(Properties.getLocalTestPolicyPath()); //測試件
			if(!txtTestFolder.exists())
				txtTestFolder.mkdirs();
			
			
			File okFolder = new File(Properties.getLocalOKPath());
			if(!okFolder.exists())
				okFolder.mkdirs();
			File [] okFiles = okFolder.listFiles();
			boolean processBegin = false;
			boolean dataOk = false;
			boolean imageOk = false;
			boolean onlineDataOk = false;
			boolean onlineImageOk = false;
			boolean testDataOk = false;
			boolean testImageOk = false;
			boolean batchLock = false;						
			/*
			 * DATA.OK存在，且batch.lock不存在時開始作業
			 * 目前不管Data.OK惹 
			 */
			for(File file: okFiles){
				if(file.getName().equalsIgnoreCase("batch.lock") || file.getName().equalsIgnoreCase("batch.keeplock")){
					batchLock = true;
				}
				
				if(file.getName().equalsIgnoreCase("DATA.OK") ){
					dataOk = true;
				}				
				if(file.getName().equalsIgnoreCase("ONLINE_DATA.OK")){
					onlineDataOk = true;
				}
				if(file.getName().equalsIgnoreCase("TEST_DATA.OK")){
					testDataOk = true;
				}
				if(file.getName().equalsIgnoreCase("IMAGE.OK") ){
					 imageOk = true;
					 //檢查image資料夾是不是全空了，全空了代表影像處理完畢
					 filesZero = false;
					 folderZero = true;
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
					 imageOk = imageOk & filesZero & folderZero;
					
				}				
				if(file.getName().equalsIgnoreCase("ONLINE_IMAGE.OK")){
					onlineImageOk = true;
				}
				if(file.getName().equalsIgnoreCase("TEST_IMAGE.OK")){
					testImageOk = true;
					//檢查testimage資料夾是不是全空了，全空了代表影像處理完畢
					boolean testfilesZero = false;
					boolean testfolderZero = true;
					if(testImgFolder.listFiles(FileFilterImpl.getFileFilter()) == null || testImgFolder.listFiles(FileFilterImpl.getFileFilter()).length == 0){
					   testfilesZero = true;
					}
					if(testImgFolder.listFiles(FileFilterImpl.getDirectoryFilter()) == null || testImgFolder.listFiles(FileFilterImpl.getDirectoryFilter()).length == 0){
					   
					}else{
					   for(File folder : testImgFolder.listFiles(FileFilterImpl.getDirectoryFilter())){
						   if(folder.list() != null && folder.list().length > 0){
							   testfolderZero = false;
							   break;
						   }
					   }
					}
					testImageOk = testImageOk & testfilesZero & testfolderZero;
				}
			}
			dataOk = imageOk & dataOk;
			onlineDataOk = onlineImageOk & onlineDataOk;
			testDataOk = testImageOk & testDataOk;
			
			
			String center = null; //行政中心
			File[] txtFile1st = null;
			File[] txtFile2nd = null;
			for(int i = 1 ; i <= Properties.getMaxCenter() ; i ++){
			   processBegin = (!batchLock & dataOk);	
			   File centerFolder = null;
			   if(i < 10)
				   centerFolder = new File(txtFolder, "0" + i);
			   else
				   centerFolder = new File(txtFolder, i + "");
			   if(centerFolder.exists()){
				  
			      txtFile1st = centerFolder.listFiles();
			      Thread.sleep(5000);
			      txtFile2nd = centerFolder.listFiles();
			      //等待 5秒後，如果兩次list出來的數字一樣多，開始作業
			      if(processBegin && txtFile1st != null && txtFile2nd != null 
			    		  && txtFile2nd.length > 0 
			    		  && txtFile2nd.length == txtFile1st.length ){
				     processBegin = true;
				     fromWhere = 'B';
				     if(i < 10)
				        center = "0" + i;
				     else
				    	center = i + "";
				     break;
			      }else{
				     processBegin = false;
			      }
			   }else{
				   centerFolder.mkdirs();
			   }
			}
			
			//檢查online，沒有batch件時才進行
			if(!processBegin){								
				processBegin = (!batchLock & onlineDataOk);
								
				for(int i = 1 ; i <= Properties.getMaxCenter() ; i ++){
				   File centerFolder = null;
				   if(i < 10)
					   centerFolder = new File(txtFolder, "0" + i);
				   else
					   centerFolder = new File(txtFolder, i + "");
				   if(centerFolder.exists()){
				      txtFile1st = centerFolder.listFiles();
				      Thread.sleep(5000);
				      txtFile2nd = centerFolder.listFiles();
				      //等待 5秒後，如果兩次list出來的數字一樣多，開始作業
				      if(processBegin && txtFile1st != null && txtFile2nd != null
						   && txtFile2nd.length > 0
						   && txtFile2nd.length == txtFile1st.length){
					      processBegin = true;
					      fromWhere = 'O';
					      if(i < 10)
						     center = "0" + i;
						  else
						  	 center = i + "";
					      break;
				      }else{
					      processBegin = false;
				      }
				   }
				}
			}
			//檢查test件，沒有batch和online件時才進行
			if(!processBegin){								
				processBegin = (!batchLock & testDataOk);
				txtFile1st = txtTestFolder.listFiles();
				Thread.sleep(5000);
				txtFile2nd = txtTestFolder.listFiles();
				//等待 5秒後，如果兩次list出來的數字一樣多，開始作業
				if(processBegin && txtFile1st != null && txtFile2nd != null
						&& txtFile2nd.length > 0
						&& txtFile2nd.length == txtFile1st.length){
					processBegin = true;
					fromWhere = 'T';
					center = "00";
				}else{
					processBegin = false;
				}
			}
			
			//如果還有其它的待處理檔案，就不處理
			if(processBegin){
				logger.info("checking folder" + new File(Properties.getCheckedOkPath() + fromWhere + "/" + center + "/").getAbsolutePath());
				if(!new File(Properties.getCheckedOkPath() + fromWhere + "/" + center + "/").exists())
					new File(Properties.getCheckedOkPath() + fromWhere + "/" + center + "/").mkdirs();
				if(new File(Properties.getCheckedOkPath() + fromWhere + "/" + center + "/").listFiles(FileFilterImpl.getAlteredAreaIdDirFilter()).length > 0)
					processBegin = false;
				logger.info("checking folder" + new File(Properties.getReceiptOkPath() + fromWhere + "/" + center + "/").getAbsolutePath());
				if(!new File(Properties.getReceiptOkPath() + fromWhere + "/" + center + "/").exists())
					new File(Properties.getReceiptOkPath() + fromWhere + "/" + center + "/").mkdirs();
				if(new File(Properties.getReceiptOkPath() + fromWhere + "/" + center + "/").listFiles(FileFilterImpl.getAlteredAreaIdDirFilter()).length > 0 )
					processBegin = false;
				
				//如果06的地址檔還沒有送過來，就不處理，繼續等待				
				//地址檔是cycleDate起頭
				String addrStr = Constant.yyyy_MM_dd.format(cycleDate) + "_06_ADDRESSvsRECIPTNO.txt";								
				File addrFile = new File(addrFolder, addrStr);				
				
				Calendar cal = Calendar.getInstance();
				//不一定是cycleDate，尚待確定
				if(center.equals("06") && !addrFile.exists() && cal.get(Calendar.HOUR_OF_DAY) > 20){
					//用當天日期去判斷
					addrStr = Constant.yyyy_MM_dd.format(cal.getTime()) + "_06_ADDRESSvsRECIPTNO.txt";
					addrFile = new File(addrFolder, addrStr);				
				}
				if(center.equals("06") && !addrFile.exists() && cal.get(Calendar.HOUR_OF_DAY) < 18){					
					//如果還是沒有，且現在時間 0~12點，用前一天找看看
					cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE) - 1);					   					
					addrStr = Constant.yyyy_MM_dd.format(cal.getTime()) + "_06_ADDRESSvsRECIPTNO.txt";
					addrFile = new File(addrFolder, addrStr);
					
				}
				
				if(thirdStage && center.equals("06") && !addrFile.exists()){
					//北二使用
					processBegin = false;
					logger.info("waiting addr file " + addrStr + " appear.");
					ErrorReport er = new ErrorReport();
					er.setErrHappenTime(new Date());
					er.setErrorType("WaitingAddrFile");
					er.setOldBatchName("");
					er.setReported(false);
					er.setException(true);
					er.setMessageBody("等待地址清單:" + addrStr );
					er.setTitle("Waiting Address File");
					((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
				}else if(addrFile.exists()){					
					AddressProcesser.parseFile(addrFile);
				}				
			}			
			
			logger.info("process begin:" + processBegin + "|" + fromWhere + "|" +  center );
			//如果可以開始進行，開始搬到tmp folder，寫入DB，並依轄區歸類到資料夾
			if(processBegin){	
				InputdateParser.forceReadFile();
				cycleDate = InputdateParser.getInputDate();
				//北二行政中心
				if(AddressProcesser.getTaipeiNo2() != null)
					taipeiNo2 = AddressProcesser.getTaipeiNo2();
				else
				    taipeiNo2 = ((VoService) Constant.getContext().getBean("voServiceProxy")).getArea("9D00000");
				
				ImgUpdater.setAfterReqPolicyProcessed(false);
				if(fromWhere != 'T'){				   
				   ErrorReport er = new ErrorReport();
				   er.setErrHappenTime(new Date());
				   er.setErrorType("BeginProcess");
				   er.setOldBatchName("");
				   er.setReported(true);
				   er.setMessageBody("開始處理發單資料:" + center );
				   er.setTitle("Begin Process");
				   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
				}
				//"Area.findHaveAddress"
				List<Area> normalAreas =  ((VoService) Constant.getContext().getBean("voServiceProxy")).getAreaList();	
				logger.info("got areas for checks");
				areaMap = new HashMap<String, Area>();

				for(Area area : normalAreas){	
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
				//放入apply data的目錄
				File parentFolder = txtFile2nd[0].getParentFile();				
				String [] holdFiles = Properties.getHoldFileNms();
				//先把所有檔案拆開
				for(File file : txtFile2nd){
					//檢查看看有那些檔案要hold住的
					if(holdFiles != null){
					   boolean continueNext = false;
					   for(String holdFile : holdFiles){
						   if(file.getName().indexOf(holdFile) >= 0){
							   continueNext = true;
							   File destDir = new File(Properties.getDifficultFontPath());
							   FilesUtils.copyFileToDirectory(file, destDir, true);
							   FilesUtils.moveFileToDirectory(file, new File(Properties.getBackupFolder(), "DATA/" + Constant.yyyy_MM_dd.format(cycleDate) + "/" + file.getParentFile().getName()), true);
							   ErrorReport err = new ErrorReport();
							   err.setErrHappenTime(new Date());
							   err.setErrorType("HoldFile");
							   err.setOldBatchName(file.getName());
							   err.setReported(false);
							   err.setMessageBody( file.getName() + "，因設定" + holdFile + "型態的檔案暫停作業，故先移到backup目錄中");
							   err.setTitle("暫停處理檔案");
							   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);
							   break;
						   }
					   }
					   if(continueNext)
						   continue;
					}
					
				   String[] fileNmSplit = file.getName().split("_");
				   //如果檔案名稱不對，或放入其它時，通知發生錯誤，並移到備份目錄
				   if(fileNmSplit.length < 6 || (!fileNmSplit[3].toUpperCase().equals("NORM") 
						   && !fileNmSplit[3].toUpperCase().equals("REPT")
						   && !fileNmSplit[3].toUpperCase().equals("REIS")
						   && !fileNmSplit[3].toUpperCase().equals("CONV"))){
					   ErrorReport err = new ErrorReport();
					   err.setErrHappenTime(new Date());
					   err.setErrorType("errFileNm");
					   err.setOldBatchName(file.getName());
					   err.setReported(false);
					   err.setException(true);
					   err.setMessageBody("個險目錄" + file.getParentFile().getAbsolutePath() + "中放入錯誤的檔案名稱" + file.getName());
					   err.setTitle("apply data wrong format ");
					   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(err);
					   FilesUtils.moveFileToDirectory(file, new File(Properties.getBackupFolder(), "DATA/" + Constant.yyyy_MM_dd.format(cycleDate) + "/" + file.getParentFile().getName()), true);
					   continue;
				   }
				   
				   //補發或正常件，尚未拆就拆檔
				   if(fileNmSplit.length == 6 && fromWhere != 'T'){
				      SplitFile.splitCathay(file, parentFolder);
				   }else if(fileNmSplit.length == 7 && fromWhere != 'T' && (fileNmSplit[3].toUpperCase().equals("CONV") || fileNmSplit[3].toUpperCase().equals("REIS"))){
					 //保補與契轉，尚未拆檔的話就拆檔					   
					  SplitFile.splitCathay(file, parentFolder);
				   }else if(fileNmSplit.length >= 6 && fromWhere == 'T'){
					  SplitFile.splitCathay(file, parentFolder);
				   }else{
					   continue;
				   }
				   //拆完後把原來的檔案搬走	   
				   if(fromWhere == 'B' || fromWhere == 'O')
				      FilesUtils.moveFileToDirectory(file, new File(Properties.getBackupFolder(), "DATA/" + Constant.yyyy_MM_dd.format(cycleDate) + "/" + file.getParentFile().getName()), true);
				   if(fromWhere == 'T')
					  FilesUtils.moveFileToDirectory(file, new File(Properties.getBackupFolder(), "TEST_DATA/" + Constant.yyyy_MM_dd.format(cycleDate)), true);
				}
				myRecheckFolder = new File(reCheckFolder, center);
				if(!myRecheckFolder.exists()){
					myRecheckFolder.mkdirs();
				}
				List<File> reisNconvs = new ArrayList<File>();
				//把切出來的檔案，如果是REIS和CONV的，先移去reCheckFolder
                File[] allFiles = parentFolder.listFiles(FileFilterImpl.getFileFilter());
				for(File file : allFiles){
					String[] fileNmSplit = file.getName().split("_");
					if(fileNmSplit.length > 7 && fromWhere != 'T' && (fileNmSplit[3].toUpperCase().equals("CONV") || fileNmSplit[3].toUpperCase().equals("REIS"))){
					    //保補與契轉，搬去recheck folder;
						reisNconvs.add(file);
						//FilesUtils.moveFileToDirectory(file, myRecheckFolder, true);
					}
				}				
				//}
				//把recheck的File move過來				
				if(myRecheckFolder.exists()){
					for(File file : myRecheckFolder.listFiles(FileFilterImpl.getFileFilter())){					   
					   logger.info("move" + file.getName() + " to recheck");
					   FilesUtils.moveFileToDirectory(file, parentFolder, true);
					}
					//把本日發送的保補契轉件都移過去recheck目錄
					for(File file : reisNconvs){
						FilesUtils.moveFileToDirectory(file, myRecheckFolder, true);
					}
				}
				
				//搬完後再list一次 
                allFiles = parentFolder.listFiles(FileFilterImpl.getFileFilter());
                List<File> receiptFiles = new ArrayList<File>();
                List<File> policyFiles = new ArrayList<File>();

				for(File file : allFiles){
					String oldBatchName = file.getName();
					if(oldBatchName.indexOf("回條") > 0){	
						receiptFiles.add(file);
						continue;
					}else{
						policyFiles.add(file);
					}
				}
				//不是測試時才進行檢查
				if (fromWhere != 'T') {
					for (int i = policyFiles.size() - 1; i >= 0; i--) {
						File file = policyFiles.get(i);
						String fileNm = file.getName();
						String[] fileNmSplit = fileNm.split("_");
						// 應該有簽收單時，先檢查是不是有簽收單
						if (fileNmSplit[3].toUpperCase().equals("NORM")
								|| fileNmSplit[3].toUpperCase().equals("REPT")) {
							String policyCompare = fileNmSplit[0]
									+ fileNmSplit[1] + fileNmSplit[2]
									+ fileNmSplit[3] + fileNmSplit[4]
									+ fileNmSplit[6];
							boolean receiptFound = false;
							for (int j = receiptFiles.size() - 1; j >= 0; j--) {
								String receiptFileNm = receiptFiles.get(j)
										.getName();
								String[] receiptFileNmSplit = receiptFileNm
										.split("_");
								String receiptCompare = receiptFileNmSplit[0]
										+ receiptFileNmSplit[1]
										+ receiptFileNmSplit[2]
										+ receiptFileNmSplit[3]
										+ receiptFileNmSplit[4]
										+ receiptFileNmSplit[6];
								if (receiptCompare.equals(policyCompare)) {
									receiptFound = true;
									break;
								}
							}
							//如果沒找到，寫入通知，將此筆保單移出
							if (!receiptFound) {
								noReceiptCounter++;
								ErrorReport er = new ErrorReport();
								er.setErrHappenTime(new Date());
								er.setErrorType("errFileNm");
								er.setOldBatchName(fileNm);
								er.setReported(false);
								er.setMessageBody("處理" + center + "目錄時，" + fileNm + "找不到對應的簽收回條， 此保單不轉檔" );
								er.setException(true);
								er.setTitle("apply data wrong format ");
								((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);								   
								FilesUtils.moveToDirectory(file, new File(Properties.getBackupFolder() + "DATA/" + Constant.yyyyMMdd.format(cycleDate) + "/applyData"), true);
								policyFiles.remove(i);
								
							}
						}
					}
					for (int i = receiptFiles.size() - 1; i >= 0; i--) {
						File file = receiptFiles.get(i);
						String fileNm = file.getName();
						String[] fileNmSplit = fileNm.split("_");
						// 應該有保單時，檢查是不是有保單
						if (fileNmSplit[3].toUpperCase().equals("NORM")
								|| fileNmSplit[3].toUpperCase().equals("REPT")) {
							String receiptCompare = fileNmSplit[0]
									+ fileNmSplit[1] + fileNmSplit[2]
									+ fileNmSplit[3] + fileNmSplit[4]
									+ fileNmSplit[6];
							boolean policyFound = false;
							for (int j = policyFiles.size() - 1; j >= 0; j--) {
								String policyFileNm = policyFiles.get(j)
										.getName();
								String[] policyFileNmSplit = policyFileNm
										.split("_");
								String policyCompare = policyFileNmSplit[0]
										+ policyFileNmSplit[1]
										+ policyFileNmSplit[2]
										+ policyFileNmSplit[3]
										+ policyFileNmSplit[4]
										+ policyFileNmSplit[6];
								if (receiptCompare.equals(policyCompare)) {
									policyFound = true;
									break;
								}
							}
							//如果沒找到，寫入通知，將此筆保單移出
							if (!policyFound) {
								noPolicyCounter++;
								ErrorReport er = new ErrorReport();
								er.setErrHappenTime(new Date());
								er.setErrorType("errFileNm");
								er.setOldBatchName(fileNm);
								er.setReported(false);
								er.setMessageBody("處理" + center + "目錄時，" + fileNm + "找不到對應的保單， 此簽收單不轉檔" );
								er.setException(true);
								er.setTitle("apply data wrong format ");
								((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);								   
								FilesUtils.moveToDirectory(file, new File(Properties.getBackupFolder() + "DATA/" + Constant.yyyyMMdd.format(cycleDate) + "/applyData"), true);
								receiptFiles.remove(i);								
							}
						}
					}
				}	
				if((noPolicyCounter > 50 || noReceiptCounter> 50) && fromWhere != 'T'){
					ErrorReport er = new ErrorReport();
					er.setErrHappenTime(new Date());
					er.setErrorType("tooManyError");
					er.setOldBatchName("");
					er.setReported(false);
					er.setMessageBody("處理" + center + "目錄時。有保單無簽收回條的保單有" + noReceiptCounter + "件。"  + "有簽收回條無保單的簽收回條有" + noPolicyCounter + "件。故暫停作業，請立刻處理此異常狀況" );
					er.setTitle("apply data wrong format ");
					er.setException(true);
					((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
					allFiles = parentFolder.listFiles();
					for(File file : allFiles){
					    FilesUtils.moveToDirectory(file, new File(Properties.getBackupFolder() + "DATA/" + Constant.yyyy_MM_dd.format(cycleDate) + "/applyData"), true);
				    }
					FileUtils.forceDelete(new File(okFolder, "DATA.OK"));
				    FileUtils.forceDelete(new File(okFolder, "IMAGE.OK"));
				    File directory = new File(Properties.getCheckedOkPath() + fromWhere + "/" + center  + "/"  );
				    File receiptDir = new File(Properties.getReceiptOkPath() + fromWhere + "/" + center  + "/"  );

				    if(directory.exists())
				    	FileUtils.deleteDirectory(directory);
				    if(receiptDir.exists())
				    	FileUtils.deleteDirectory(receiptDir);

				    
				    logger.info("fileDispatcher too many errors stop");
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
				if(AddressProcesser.serviceCenterMap != null && AddressProcesser.serviceCenterMap.size() > 0){
					mappingServiceCenter = AddressProcesser.serviceCenterMap;
				}else if(addressFiles != null && addressFiles.length > 0){					
					mappingServiceCenter = AddressProcesser.readFile(addressFiles);
				}
				//如果是北二的話，要先讀入地址檔
				List<ApplyData> applyDatas = null;
				if("06".equals(center)){
					 applyDatas = AddressProcesser.getApplyDatas();
					if(applyDatas != null)
					   logger.info("apply datas:" + applyDatas.size());
					else
					   logger.info("apply datas: null");
					
				}				
				oldSerialNo = verifyFiles(policyFiles, oldSerialNo, center, false, applyDatas);				
				oldSerialNo = verifyFiles(receiptFiles, oldSerialNo, center, true, applyDatas);
				
				if((noPolicyCounter > 50 || imageErrorCounter > 50 || noReceiptCounter> 50) && fromWhere != 'T'){
					ErrorReport er = new ErrorReport();
					er.setErrHappenTime(new Date());
					er.setErrorType("tooManyError");
					er.setOldBatchName("");
					er.setReported(false);
					er.setMessageBody("處理" + center + "目錄時。無影像或影像錯誤的有" + imageErrorCounter + "件。故暫停作業，請立刻處理此異常狀況" );
					er.setTitle("apply data wrong format ");
					er.setException(true);
					((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
					allFiles = parentFolder.listFiles();
					for(File file : allFiles){
					    FilesUtils.moveToDirectory(file, new File(Properties.getBackupFolder() + "DATA/" + Constant.yyyy_MM_dd + "/applyData"), true);
				    }
					FileUtils.forceDelete(new File(okFolder, "DATA.OK"));
				    FileUtils.forceDelete(new File(okFolder, "IMAGE.OK"));
				    File directory = new File(Properties.getCheckedOkPath() + fromWhere + "/" + center  + "/"  );
				    File receiptDir = new File(Properties.getReceiptOkPath() + fromWhere + "/" + center  + "/"  );
				    File reCheck = new File(Properties.getCheckedOkPath() + fromWhere + "/" + center  + "/"  );
				    if(directory.exists())
				    	FileUtils.deleteDirectory(directory);
				    if(receiptDir.exists())
				    	FileUtils.deleteDirectory(receiptDir);				    
				    if(reCheck.exists())
				    	FileUtils.deleteDirectory(reCheck);
				    logger.info("fileDispatcher too many errors stop");
					running = false;
					return;
				}
                
				
				if(processBegin){
					File directory = null;
					File receiptDir = null;
					if(center != null){
					   directory = new File(Properties.getCheckedOkPath() + fromWhere + "/" + center  + "/"  );
					   if(!directory.exists())
						   directory.mkdirs();
					   receiptDir = new File(Properties.getReceiptOkPath() + fromWhere + "/" + center  + "/"  );
					   if(!receiptDir.exists())
						   receiptDir.mkdirs();
					}
					
					
					File [] subdirs = directory.listFiles(FileFilterImpl.getAreaIdFolderFilter()); 					
					File [] recSubdirs = receiptDir.listFiles(FileFilterImpl.getAreaIdFolderFilter()); 
					//如果開始進行轉檔，寫入一筆資料
					if(subdirs != null && subdirs.length > 0 && fromWhere != 'T'){
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("BeginProcess");
						er.setOldBatchName("");
						er.setReported(false);
						er.setMessageBody(Constant.yyyy_MM_ddHHMM.format(new Date()) + "，行政中心:" + ApplyData.getCenterMap().get(center) + "開始進行轉檔作業處理\r\n "
								+ "新契約:" + normCounter + "件\r\n "
							    + "補單：" + reptCounter + "件\r\n "
							    + "保補：" + reisCounter + "件\r\n "
							    + "契轉：" + convCounter + "件");
						er.setTitle("Begin Process");
						((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
					}else if(subdirs != null && subdirs.length > 0 && fromWhere == 'T'){
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("BeginProcess");
						er.setOldBatchName("");
						er.setReported(false);
						er.setMessageBody(Constant.yyyy_MM_ddHHMM.format(new Date()) + "測試件開始進行轉檔作業處理\r\n "
								+ "新契約:" + normCounter + "件\r\n "
							    + "補單：" + reptCounter + "件\r\n "
							    + "保補：" + reisCounter + "件\r\n "
							    + "契轉：" + convCounter + "件");
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
					      fw = new FileWriter(Properties.getCheckedOkPath() + fromWhere + "/" + center  + "/moveCompleted.ok" );
					      fw.write("ok");
					      fw.flush();
						  fw.close();
					   }
					   if(recSubdirs.length > 0){
					      fw = new FileWriter(Properties.getReceiptOkPath() + fromWhere + "/" + center  + "/moveCompleted.ok" );
					      fw.write("ok");
					      fw.flush();
						  fw.close();
					   }					   
					}
				}
				//全部作完後把data.ok和image.ok幹掉
				
				if(processBegin && dataOk){
				   boolean deleteOrN = true; 
				   for(int i = 1 ; i <= Properties.getMaxCenter() ; i ++){
					   File centerFolder = null;
					   if(i < 10)
						   centerFolder = new File(txtFolder, "0" + i);
					   else
						   centerFolder = new File(txtFolder, i + "");
					   if(centerFolder.exists()){
						   //如果裡面還有檔案，代表還沒全部處理完，dataOK不清空
						   String [] fileNms = centerFolder.list();
						   if(fileNms != null && fileNms.length > 0){
							   deleteOrN = false;
						   }
					   }
				   }
				   //全部的資料夾都清空後刪除data.ok
			       if(deleteOrN){
			    	  if(new File(okFolder, "DATA.OK").exists()) 
				         FileUtils.forceDelete(new File(okFolder, "DATA.OK"));
			    	  if(new File(okFolder, "IMAGE.OK").exists())
			             FileUtils.forceDelete(new File(okFolder, "IMAGE.OK"));
			       }
				}
			    if(processBegin && onlineDataOk){
			       if(new File(okFolder, "ONLINE_DATA.OK").exists())
				      FileUtils.forceDelete(new File(okFolder, "ONLINE_DATA.OK"));
			       if(new File(okFolder, "ONLINE_IMAGE.OK").exists())
					  FileUtils.forceDelete(new File(okFolder, "ONLINE_IMAGE.OK"));
				}else if(processBegin && testDataOk){
				   if(new File(okFolder, "TEST_DATA.OK").exists())
				      FileUtils.forceDelete(new File(okFolder, "TEST_DATA.OK"));
				   if(new File(okFolder, "TEST_IMAGE.OK").exists())
				      FileUtils.forceDelete(new File(okFolder, "TEST_IMAGE.OK"));
				}
				
			} // end processBegin 
		}catch(Exception e){
			e.printStackTrace();
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
			running = false;
		}
		
		logger.info("fileDispatcher  stop");
		running = false;
	}
	
	
	private synchronized static int verifyFiles(List<File> allFiles, int oldSerialNo, String center, boolean receipt, List<ApplyData> applyDatas) throws BeansException, RemoteException {		
		
		Map<String, Area> centerMap = ((VoService) Constant.getContext().getBean("voServiceProxy")).getCenterAreaMap();		
		
		for(File file : allFiles){
			boolean forceNorm = false;
			boolean requestPolicyExist = false;
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
				applyData.setCd(false);				
				update = false;
				
			}else{
				//如果之前有發單過，就刪除
			    Date start = new Date();
			    boolean continueNext = false;
				while(!((VoService) Constant.getContext().getBean("voServiceProxy")).deleteApplyData(applyData)){
					logger.info("couldn't delete applyData :" + applyData.getOldBatchName());
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
					   er.setTitle("apply data could not be inserted");
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
				//applyData = new ApplyData();
				applyData.setGroupInsure(false);
				applyData.setHaveInsureCard(false);
				applyData.setOldBatchName(oldBatchName);
				applyData.setPolicyStatus(null);
				applyData.setExceptionStatus(null);
				applyData.setVerifyResult(null);
				applyData.setCd(false);				
				update = false;
			}
			applyData.setExceptionStatus(null);
		    applyData.setVerifyResult(null);
		    applyData.setVerifyTime(null);
		    applyData.setNewBatchName(null);
		    applyData.setPackId(null);
			logger.info("processing:" + oldBatchName);
			String [] fileNmSplit = oldBatchName.split("_");
			//不是Test時，檢查檔案名稱格式，如果格式錯誤送出訊息
			if(fromWhere != 'T'  && fileNmSplit.length != 7 && fileNmSplit.length != 8){
				ErrorReport er = new ErrorReport();
				er.setErrHappenTime(new Date());
				er.setErrorType("errFileNm");
				er.setOldBatchName(oldBatchName);
				er.setReported(false);
				er.setException(true);
				er.setMessageBody("錯誤的檔案名稱" + oldBatchName);
				er.setTitle("apply data wrong format ");
				((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
				try{
				   FilesUtils.moveToDirectory(file, new File(Properties.getErrorFileNmPath()), true);
				}catch(Exception e){
				   logger.error("", e);
				}
				continue;
			}else if(fromWhere == 'B' || fromWhere == 'T' || fromWhere == 'O'){
				Date today = new Date();
				//batch,test和online時進入此處
				int lastIndex = oldBatchName.lastIndexOf("_");						
				applyData.setFileNm(oldBatchName.substring(0, lastIndex));							
				applyData.setInsertDate(new Date());
				
				applyData.setUpdateDate(today);
				applyData.setReceipt(receipt);
				applyData.setExceptionStatus(null);
				

				try{
				   if(fromWhere == 'B' || fromWhere == 'O' || fromWhere == 'T'){
					   // 檔名解析後回壓							
				       applyData.setCycleDate(cycleDate); 
				       try{
				          applyData.setProcessedDate(Constant.yyyy_MM_dd.parse(fileNmSplit[1]));
				          if(fromWhere != 'T')
				             applyData.setCenter(fileNmSplit[2]);
				          else
				    	     applyData.setCenter("00");
				       }catch(Exception e){
				          logger.error("", e);   
				          fileNmSplit = oldBatchName.split("_");
				          try{
				             applyData.setProcessedDate(Constant.yyyy_MM_dd.parse(fileNmSplit[1]));
				             applyData.setCenter(fileNmSplit[2]);
				          }catch(Exception ex){
				        	  logger.error("", ex);
				        	  applyData.setProcessedDate(new Date());
					          applyData.setCenter("06");
				          }
				       }
				       String[] forceNorms = Properties.getForceNormFileNms();
				        
				       if(forceNorms != null){
				          for(String forceNm : forceNorms){
				    	      if(oldBatchName.indexOf(forceNm) >= 0){
				    		      forceNorm = true;
				    		      break;
				    	      }
				          }
				       }
				       //測試件不管forceNorm
				       if(fromWhere == 'T')
				    	   forceNorm = false;
				       if(!forceNorm)
				          applyData.setSourceCode(fileNmSplit[3].toUpperCase());
				       else
				    	  applyData.setSourceCode("NORM");
				       if(fileNmSplit.length == 7 || fromWhere == 'T'){				    	   
					      applyData.setOldBatchNo(fileNmSplit[4]);
					      if(fileNmSplit[4].length() > 8){
					    	  applyData.setOldBatchNo(fileNmSplit[4].substring(0, 8));
					      }
			           }else if(fileNmSplit.length == 8){
			        	  applyData.setOldBatchNo(fileNmSplit[5]);
			        	  if(fileNmSplit[4].length() > 8){
					    	  applyData.setOldBatchNo(fileNmSplit[4].substring(0, 8));
					      }
			           }
				       
				       if(!applyData.getCenter().equals("06")){
				    	   applyData.setPackType("01"); //不是北二時設為服務中心
				       }else{				    	   
				    	   /*
                            01：服務中心(預設值)
                            02：(保經)保代業務件
                            03：(雙掛號)直效行銷件
                                                                         說明：若於保單收件table查得到即為北二區的件時，當保單交寄方式DeliverType
                                                                         為S時，設定為03：直效行銷件
                                                                         為P時，設定為02保代業務件
                            */
				    	   if("NORM".equals(applyData.getSourceCode())){				    		   
				    		   // 如果是MI的話就是G類
				    		   applyData.setPackType("02");
				    		   if(applyData.getOldBatchNo() != null 
				    				   && applyData.getOldBatchNo().toUpperCase().startsWith("MI")){
				    			   applyData.setChannelID("G");
				    			   applyData.setChannelName("國泰金控");				    			   
				    			   applyData.setDeliverType("P");
				    		   }else if(applyData.getOldBatchNo() != null && applyData.getOldBatchNo().toUpperCase().startsWith("P")){	
				    			   applyData.setDeliverType("P");
				    		   }else if(applyData.getOldBatchNo() != null && applyData.getOldBatchNo().toUpperCase().startsWith("S")){
				    			   applyData.setPackType("03");	
				    			   applyData.setDeliverType("S");
				    		   }
				    		   
				    	   }else{
				    		   //新契約以外，都是送回北二
				    		   applyData.setPackType("01"); //服務中心
				    		   applyData.setChannelID("");
				    	   }
				    	    
				    	   
				       }

				       
				       if(!center.equals(applyData.getCenter())){
				    	   ErrorReport er = new ErrorReport();
						   er.setErrHappenTime(new Date());
						   er.setErrorType("errFileNm");
						   er.setOldBatchName(oldBatchName);
						   er.setReported(false);
						   er.setMessageBody("處理" + center + "目錄時，" + oldBatchName + "檔案放在錯誤的目錄" );
						   er.setTitle("apply data wrong format ");
						   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
						   
						   FilesUtils.moveToDirectory(file, new File(Properties.getErrorFileNmPath()), true);
						   continue;
				       }
				   }else{
					   if(applyData != null && !"00".equals(applyData.getCenter())){
						   logger.info("測試件:" + oldBatchName + "檔案為過去真實存在的檔案，行政中心為" + applyData.getCenter() + "，不可使用此名稱");
						   ErrorReport er = new ErrorReport();
						   er.setErrHappenTime(new Date());
						   er.setErrorType("errFileNm");
						   er.setOldBatchName(oldBatchName);
						   er.setReported(false);
						   er.setMessageBody("測試件:" + oldBatchName + "檔案為過去真實存在的檔案，行政中心為" + applyData.getCenter() + "，不可使用此名稱");
						   er.setTitle("apply data wrong format ");
						   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
						   continue;
					   }
					   //測試檔自己塞
					   applyData.setCycleDate(new Date()); 
				       applyData.setPrintDate(new Date());
				       applyData.setCenter(center); //test set to 00
				       applyData.setSourceCode("TEST");						       
				   }
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
				   applyData.setProductType(fromWhere + ""); //那一種件 O? B? T?
				   FileInputStream fis = null;
				   InputStreamReader isr = null;
				   BufferedReader br = null;
				   String line = null;
				   try {
					    logger.info("read file");
						fis = new FileInputStream(file);
						isr = new InputStreamReader(fis, "ms950");
						br = new BufferedReader(isr);					
						Set<String> policyNos = applyData.getPolicyNoSet();
						Set<ImgFile> imgFileSet = new HashSet<ImgFile>();
						List<String> forCheckList = new ArrayList<String>();
						boolean continueNext = false;
						boolean beginParsing = false;
						boolean afterParsed = false;
						while((line = br.readLine()) != null) {
							line = line .trim();
							//info|9070923600|00|CB01726316|Q120670235|NORM||QG42302|展朴子二３|0|04|
							//info|9070924428|00|AA43860304|H226771031|NORM||HD41C60|展楊梅一Ｃ|1|05|10279.0000||
							//info|9097058358|00|CC06704474|G221579483|NORM||A*14206|專巨鼎瑞晟|0|01|300000.0000||
							//|policyNo|reprint|applyNo|insureId|sourceCode||areaId |areaName|merger|行政中心|保費
							//9091080912|00|2|001A|B20|要 保 人：|游　○　蝶
							if(line.startsWith("info") ){
								beginParsing = true;;
								String [] lineSplit = line.split("\\|");
								if(lineSplit.length < 10){
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

								policyNos.add(lineSplit[1].trim());
								applyData.setPolicyNoSet(policyNos);
								applyData.setReprint(Integer.parseInt(lineSplit[2].trim()));
								applyData.setApplyNo(lineSplit[3].trim());								
								applyData.setInsureId(lineSplit[4].trim());
								if(!forceNorm)
								   applyData.setSourceCode(lineSplit[5].trim());
								else
								   applyData.setSourceCode("NORM");
								applyData.setAreaId(lineSplit[7].trim());
								if("06".equals(applyData.getCenter())){
									//北二時要設定chennelId
									/*
									 * packtype
									 01：服務中心(預設值)
			                            02：(保經)保代業務件
			                            03：(雙掛號)直效行銷件
			                        */
									if("02".equals(applyData.getPackType())){
										//保代業務件										
										//CT5 == D 中信保代
										//TF5 == E 台新保代
										//CY5 == E 群益保代
										//CF5 == E 中租保代
										//SP5 == F or SS5 永豐保代
										//CU5 == G 國泰金控
										//其它 == C 非金控保代通路
										if( applyData.getAreaId() != null &&  applyData.getAreaId().length() >= 3){
										    String judgeStr = applyData.getAreaId().substring(0, 3).toUpperCase();
										    if("CT5".equals(judgeStr)){
										    	applyData.setChannelID("D");
										    	applyData.setChannelName("中信保代");
										    }else if("TF5".equals(judgeStr)){
										    	applyData.setChannelID("E");
										    	applyData.setChannelName("台新保代");
										    }else if("CY5".equals(judgeStr)){
										    	applyData.setChannelID("E");
										    	applyData.setChannelName("群益保代");
										    }else if("CF5".equals(judgeStr)){
										    	applyData.setChannelID("E");
										    	applyData.setChannelName("中租保代");
										    }else if("SP5".equals(judgeStr) || "SS5".equals(judgeStr)){
										    	applyData.setChannelID("F");
										    	applyData.setChannelName("永豐保代");
										    }else if("CU5".equals(judgeStr)){
										    	applyData.setChannelID("G");
										    	applyData.setChannelName("國泰金控");
										    }else {
										    	applyData.setChannelID("C");
										    	applyData.setChannelName("非金控保代通路");
										    }
										}
										
									}else if("03".equals(applyData.getPackType())){
										if( applyData.getAreaId() != null &&  applyData.getAreaId().length() >= 2){
											String judgeStr = applyData.getAreaId().substring(0, 2).toUpperCase();
											//TQ,TC,TN,TX開頭為國壽直效代號
											//其它為保代直效通路 
										    if("TQ".equals(judgeStr) || "TC".equals(judgeStr) || "TN".equals(judgeStr) || "TX".equals(judgeStr)){
									    	   applyData.setChannelID("B");
									    	   applyData.setChannelName("國壽直效通路");
									        }else{
									           applyData.setChannelID("A");
										       applyData.setChannelName("保代直效通路");
									        }
										}
									}
									
								}
								
								Area area = null;
								boolean selfCenter = false;
								//先看看自己是不是就是服務中心
								if(area == null && applyData.getAreaId().length() == 7){
									area = centerMap.get(applyData.getAreaId());
									if(area != null)
									   selfCenter = true;
								}		
								//先用完整的7碼去抓
								if(area == null){
									area = areaMap.get(applyData.getAreaId());
								}
								//抓不到改抓五碼的獨立課
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
									if(!selfCenter && area.getServiceCenter() != null && !area.getServiceCenter().trim().equals("")){
							           serviceCenter = area.getServiceCenter();
									}else{ 
									   serviceCenter = area.getAreaId();
									   applyData.setSubAreaId(serviceCenter);
									}
						        	
						        	//如果有服務中心，就設定服務中心地址
									String zip = null;
							        if(serviceCenter != null && !serviceCenter.trim().equals("") ){
							        	if(centerMap.get(serviceCenter) != null){
							        	   address = centerMap.get(serviceCenter).getAddress();
							        	   zip = centerMap.get(serviceCenter).getZipCode();
							        	   applyData.setServiceCenterNm(centerMap.get(serviceCenter).getAreaName());
							        	}else if(areaMap.get(serviceCenter) != null){
							        		address = areaMap.get(serviceCenter).getAddress();
								        	zip = areaMap.get(serviceCenter).getZipCode();
								        	applyData.setServiceCenterNm(areaMap.get(serviceCenter).getAreaName());
							        	}
							        	applyData.setServiceCenter(serviceCenter);							        								        								        	
							        }else{
							        	serviceCenter = mappingServiceCenter.get(area.getAreaId());
							        	if(serviceCenter != null && !serviceCenter.equals("") 
							        			&& centerMap.get(serviceCenter) != null){
							        	   address = centerMap.get(serviceCenter).getAddress();
							        	   zip = centerMap.get(serviceCenter).getZipCode();
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
							        	zip = area.getZipCode();
							        	if(address == null || address.indexOf("無地址") >= 0 || "".equals(address.trim())){
							        		address = "無法由" + area.getAreaId() + "找到寄送地址";
							        	}
							        }
							        
							        logger.info("service center's address" + address );
							        applyData.setAreaAddress(address);
							        if(!"06".equals(center))
							            applyData.setZip(zip);
							        //如果是網路投保
							        if(applyData.getAreaId() != null && (applyData.getAreaId().indexOf("網路") >= 0 || applyData.getAreaId().toUpperCase().equals("ON88888"))){
										//網路投保件
							        	applyData.setSubAreaId("ON88");
										applyData.setAreaName("WEB");									
									    applyData.setAreaAddress("無地址");
									}									
								}else if(applyData.getAreaId() != null && (applyData.getAreaId().indexOf("網路") >= 0 || applyData.getAreaId().toUpperCase().equals("ON88888"))){
									//網路投保件
									applyData.setSubAreaId("ON88");
									applyData.setAreaName("WEB");
								    applyData.setAreaAddress("無地址");
								}else{
									
									boolean areaUpdate = true;
									Area noAddressArea = ((VoService) Constant.getContext().getBean("voServiceProxy")).getArea(applyData.getAreaId());
									//如果是全新的，就去新增一筆
									if(noAddressArea == null && applyData.getAreaId() != null && applyData.getAreaId().length() >= 4){
									   applyData.setSubAreaId(applyData.getAreaId().substring(0, 4));
									   noAddressArea = new Area();
									   noAddressArea.setAreaId(applyData.getAreaId());
									   noAddressArea.setSubAreaId(applyData.getAreaId().substring(0, 4));
									   noAddressArea.setIndependent(false);
									   areaUpdate = false;
									}else{										
									   applyData.setSubAreaId(noAddressArea.getSubAreaId());
									}		
									applyData.setAreaAddress("無地址");
									if(noAddressArea != null)
									   noAddressArea.setAreaName(lineSplit[8].trim());									
									
									//從國壽的資料檔找service center
									String serviceCenter = null;
									if(noAddressArea != null)
									   serviceCenter = mappingServiceCenter.get(noAddressArea.getAreaId());
									//如果找得到service center
									if(serviceCenter != null && !serviceCenter.trim().equals("")){
									   Area serviceCenterA = centerMap.get(serviceCenter);
									   if(serviceCenterA == null)
										   serviceCenterA = areaMap.get(serviceCenter);
									   noAddressArea.setServiceCenter(serviceCenter);		
									   if(serviceCenterA != null){
									       noAddressArea.setServiceCenterNm(serviceCenterA.getAreaName());
									       applyData.setServiceCenterNm(serviceCenterA.getAreaName());
									   }
									   applyData.setServiceCenter(serviceCenter);
									   //看看此service center找不找到得到地址
									    String address = null;
									    String zip = null;
									    if(serviceCenterA != null){
									       address = serviceCenterA.getAddress();
									       zip = serviceCenterA.getZipCode();
									    }
										if(address != null && !address.trim().equals("")){										   
									       applyData.setAreaAddress(address);
									       if(!"06".equals(center))
									          applyData.setZip(zip);
										}
									}
									if(!areaUpdate && noAddressArea != null)
									   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(noAddressArea);
									else if(areaUpdate && noAddressArea != null)
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
								       logger.info("put address:" + applyData.getAreaAddress() + "into map :" + biggest);
									}
								}else{
									logger.info("not set area map");
								}
								if(applyData.getOldBatchNo() != null && applyData.getSourceCode() != null){									
                                    if(applyData.getSourceCode().toUpperCase().equals("NORM")){
                                    	//取出影像批號的對映值
                                    	String first2AreaId = "";
                                    	String key = applyData.getOldBatchNo();
                                    	if(applyData.getAreaId() != null && applyData.getAreaId().length() >= 2){
                                    		first2AreaId = applyData.getAreaId().substring(0, 2).toUpperCase();
                                    		key = key + first2AreaId;
                                    	}                                    	
                                    	//如果無法取得排序順序，就新增一個
                                        if(normMap.get(key) == null){                                        	 
                                        	if(normMap.keySet() != null &&  
                                        			(!key.toUpperCase().startsWith("S") 
                                        					|| (key.toUpperCase().startsWith("S") 
                                        					&& !(first2AreaId.equals("TN") || first2AreaId.equals("TC") || first2AreaId.equals("TQ") || first2AreaId.equals("TX") )))){                                        		
                                        		normMap.put(key, "0" + StringUtils.leftPad((normMap.keySet().size() + 1) + "", 3, '0' ));
                                        	}else if(key.toUpperCase().startsWith("S") 
                                        			&& (first2AreaId.equals("TX") ||first2AreaId.equals("TN") || first2AreaId.equals("TC") || first2AreaId.equals("TQ"))){
                                        		if(normMap.keySet() == null || normMap.keySet().size() == 0){
                                        		   if(first2AreaId.equals("TC"))
                                        		      normMap.put(key, "1001");                                        		   
                                        		   else if(first2AreaId.equals("TN"))
                                         		      normMap.put(key, "2001");
                                        		   else if(first2AreaId.equals("TQ"))
                                          		      normMap.put(key, "3001");
                                        		   else if(first2AreaId.equals("TX"))
                                          		      normMap.put(key, "3001");
                                        		}else{
                                        			if(first2AreaId.equals("TC"))
                                        		       normMap.put(key, "1" + StringUtils.leftPad((normMap.keySet().size() + 1) + "", 3, '0' ));
                                        			else if(first2AreaId.equals("TN"))
                                        			   normMap.put(key, "2" + StringUtils.leftPad((normMap.keySet().size() + 1) + "", 3, '0' ));
                                          		    else if(first2AreaId.equals("TQ"))
                                          		       normMap.put(key, "3" + StringUtils.leftPad((normMap.keySet().size() + 1) + "", 3, '0' ));
                                          		  else if(first2AreaId.equals("TX"))
                                         		       normMap.put(key, "3" + StringUtils.leftPad((normMap.keySet().size() + 1) + "", 3, '0' ));
                                        		}
                                        	}else if(normMap.keySet() == null || normMap.keySet().size() == 0){
                                        		normMap.put(key, "0001" );
                                        	}
                                        }
                                    }else if(applyData.getSourceCode().toUpperCase().equals("REPT")){
                                    	if(reptMap.get(applyData.getOldBatchNo()) == null){                                        	
                                        	if(reptMap.keySet() != null){
                                        		reptMap.put(applyData.getOldBatchNo(), "6" + StringUtils.leftPad((reptMap.keySet().size() + 1) + "", 3, '0' ));
                                        	}else if(reptMap.keySet() == null){
                                        		reptMap.put(applyData.getOldBatchNo(), "6001");
                                        	}
                                        }
                                    }
								}
								//如果ON88888，就會已經設好areaName了
								if(!"WEB".equals(applyData.getAreaName()))
								    applyData.setAreaName(lineSplit[8].trim());
								applyData.setMerger(lineSplit[9].trim().equals("0")? false : true);
								//要保書影像檔檔名
								// AA74107761.9096883770.00.N
								char merge = 'N';
								if(applyData.getMerger()){
									merge = 'M';
								}
								String forCheck = (applyData.getApplyNo() + "." + lineSplit[1].trim() + "." + lineSplit[2].trim() + "." + merge + ".tif").toLowerCase();
								logger.info("for check policy image:" + forCheck);
								forCheckList.add(forCheck);								
									
								
							}else if(line.indexOf("要 保 人") > 0){
								int seperateInd = line.lastIndexOf("|") < 0 ? line.indexOf("要 保 人") + 5 : line.lastIndexOf("|") + 1;
								String recName = line.substring(seperateInd).trim();
								applyData.setRecName(recName);
								
							}else{
								//如果已經開始處理了，代表已經處理結束
								if(beginParsing){
									afterParsed = true;
								}
							}
							//保補契轉不需要送金單
							if(applyData.getSourceCode() != null && 
									applyData.getSourceCode().toUpperCase().equals("CONV") || applyData.getSourceCode().toUpperCase().equals("REIS")){
								applyData.setHavaBkReceipt(false);
							}
							// 如果有一天括弧改成全形時也適用
							if(applyData.getSourceCode() != null && 
									(applyData.getSourceCode().equals("NORM") || applyData.getSourceCode().equals("REPT")) && 
									line.toUpperCase().indexOf("|016A|X10|") > 0 && line.indexOf("(單)") < 0 && line.indexOf("（單）") < 0 ){
								//如果沒有單，就要夾送金單，新契約及補單時需判斷
								applyData.setHavaBkReceipt(true);
							}else if(applyData.getSourceCode() != null && 
									(applyData.getSourceCode().equals("NORM") || applyData.getSourceCode().equals("REPT"))  && 
									line.toUpperCase().indexOf("|016A|X10|") > 0 && (line.indexOf("(單)") >= 0 || line.indexOf("（單）") >= 0) ){
								//如果有單，就不要夾送金單，新契約及補單時需判斷
								applyData.setHavaBkReceipt(false);
							} 
							
							if(line.indexOf("附商品說明書光碟") >= 0){
								//有附商品說明書光碟的字時，設true
								applyData.setCd(true);
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
									File dtata692 = null;											
									Date cycleDate = applyData.getCycleDate();											
									
									dtata692 = new File(folder692, "DTATA692_FXDMS");											
									FileOutputStream fos692 = null;
									OutputStreamWriter osw692 = null;
									BufferedWriter bw692 = null;
									try{
									   fos692 = new FileOutputStream(dtata692, true);
									   osw692 = new OutputStreamWriter(fos692, "ms950");
									   bw692 = new BufferedWriter(osw692);
									   Set<String> policyNoset = applyData.getPolicyNoSet();
									   String policyNo = "";
									   if(policyNoset != null){
										   for(String poNo : policyNoset){
											   policyNo = poNo;
											   String cycleDateStr = applyData.getCycleDate() == null? "" : Constant.yyyyMMdd.format(cycleDate);
											   String presTime = applyData.getPresTime() == null ? "" : Constant.yyyyMMdd.format(applyData.getPresTime());
											   String writeLine = cycleDateStr + "," + applyData.getCenter() + 
													   "," + policyNo + "," + StringUtils.leftPad(applyData.getReprint() + "", 2, '0') + "," 
													   + applyData.getApplyNo() + "," + applyData.getSourceCode() + ",Y,N,Y," + applyData.getOldBatchNo() + ",N," 
													   + (applyData.getReceipt()? "Y" : "N") + ",," 
													   + (applyData.getTotalPage() == null? 0 : applyData.getTotalPage()) + "," + applyData.getAreaId() + ","   
											           + (applyData.getMerger()? "1" : "0") + "," + "FXDMS,P\r\n";
											   bw692.write(writeLine);											   
										   }
									   }
									   
									   
									   bw692.flush();
									   osw692.flush();
									   fos692.flush();
									}catch(Exception e){
										e.printStackTrace();
										logger.error("", e);
									}finally{
										if(bw692 != null){
											try {
												bw692.close();
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											bw692 = null;
										}
										if(osw692 != null){
											try {
												osw692.close();
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											osw692 = null;
										}
										if(fos692 != null){
											try {
												fos692.close();
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											fos692 = null;
										}
									}


							    	applyData.setDocOk(false);
							    	applyData.setPolicyStatus("11");
							    	ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("errDifficultFont");
									er.setOldBatchName(oldBatchName);
									er.setReported(false);
									er.setException(true);
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
							
							/*
							 * 檢查影像檔是不是存在
							 * 先檢查要保書影像檔
							 */
							boolean requestPolicy = false;
							String requestImage = null;
							String notExistFile = null;
							//保補件要檢查檔案是不是存在才能往下進行
							//必須在info行檢查完之後
							if(beginParsing && afterParsed && 
									(applyData.getSourceCode().toUpperCase().equals("REIS") || applyData.getSourceCode().toUpperCase().equals("CONV")) 
									&& !requestPolicyExist){
							   for(String forCheck : forCheckList){
								   logger.info(file.getName() + ": checking policy image");
								   // REIS或CONV時，必須要要保書影像檔存在時才進行能
								   if(applyData.getSourceCode().toUpperCase().equals("REIS") || applyData.getSourceCode().toUpperCase().equals("CONV")){
									   logger.info(new File(new File(Properties.getImgPostProcessedPath(), "image"), forCheck.toLowerCase()).getAbsoluteFile() + " exist:" + new File(new File(Properties.getImgPostProcessedPath(), "image"), forCheck.toLowerCase()).exists());
									   if(new File(new File(Properties.getImgPostProcessedPath(), "image"), forCheck.toLowerCase()).exists()){										   
										   requestPolicyExist = true;
										   break;
									   }else if(new File(new File(Properties.getImgPostProcessedPath(), "image"), forCheck.toUpperCase()).exists()){
										   requestPolicyExist = true;
										   break;
									   }else{
										   notExistFile = forCheck;
									   }
								   }
								
								   if(line.indexOf(forCheck) > 0){
									   requestImage = "/image/" + forCheck;
									   requestPolicy = true;
								   }
							   }
							
							   if(!requestPolicyExist && (applyData.getSourceCode().toUpperCase().equals("REIS") || applyData.getSourceCode().toUpperCase().equals("CONV")) ){
								   //如果存在時就往 下進行，不存在就移去影像錯誤目錄
									// 先close reader									
									try {
										if (br != null)
											br.close();
										if (isr != null)
											isr.close();
										if (fis != null)
											fis.close();
									} catch (Exception e) {
										logger.error("", e);
									}
									br = null;
									isr = null;
									fis = null;
									
									write692(cycleDate, folder692, applyData, center);
									applyData.setImageOk(false);
									applyData.setPolicyStatus("14");
									applyData.setExceptionStatus("14");
									applyData.setVerifyResult("保補件要保書影像檔不存在");
									if(update)
										((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
									else
										((VoService) Constant.getContext().getBean("voServiceProxy")).save(applyData);
									
									ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("errImg");
									er.setOldBatchName(oldBatchName);
									er.setException(true);
									er.setReported(false);											
									er.setTitle("error on:" + line);
									er.setMessageBody(oldBatchName + "保補件要保書影像檔" + notExistFile + "不存在");
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);

									continueNext = true; // 通知繼續下一個
									// 移到影像檔不完全目錄
									FilesUtils.moveToDirectory(file, new File(Properties.getImgUncompletePath() + center + "/"), true);

								    logger.info("move file "
												+ file.getAbsolutePath());

									break;
									// }
							   }
							}
							
							//如果是有tif檔或是要保書影像檔，開始進行檢查
							//本機測試時封起來
							//if(false)
							if((index = line.indexOf(".tif")) > 0 || (index = line.indexOf(".pdf")) > 0 || requestPolicy ){
								//先截到.tif為止，免得後面還有導致出錯
								String forCheck = null;
								if(line.indexOf(".tiff") > 0)
								   forCheck = line.substring(0, index + 5);
								else
								   forCheck = line.substring(0, index + 4);
								String fileNm = null;
								int beginIndex = 0;
								//如果是要保書影像檔，用要保書影像檔去檢查
								//如果是其它.. 從..之後截斷
								if(requestPolicy){
									fileNm = requestImage;
							    }else if((beginIndex = forCheck.lastIndexOf("..")) > 0){
									fileNm = forCheck.substring(beginIndex + 2);
								}else{
									beginIndex = forCheck.lastIndexOf("|");
									fileNm = forCheck.substring(beginIndex + 1);
									if(receipt){
										fileNm = "/image/" + fileNm;
									}
								}			
								ImgFile imgFile = imgFilesOK.get(fileNm);
								if(requestPolicy){
									if(imgFile != null)
									   logger.info("Db fileNm:" + imgFile.getFileNm());
									else
									   logger.info("Db fileNm: cannot be found");
								}
								

								if(imgFile != null){ //如果圖像存在且是好的影像檔，就放入set中											
									applyData.setImageOk(true);
									imgFileSet.add(imgFile);
								}else{
									//如果是要保書影像檔不全，而且是REIS(保補)或保補的話的話，搬去影像錯誤目錄
									//保補件的需要的要保書影像檔，其實有些並不是真正的要保書影像檔，只是做為一種開關
									//看起來和前面的程式是重覆了，先不刪除，之後再觀察
									if(requestPolicy && (applyData.getSourceCode().toUpperCase().equals("REIS") || applyData.getSourceCode().toUpperCase().equals("CONV") )){
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

										write692(cycleDate, folder692, applyData, center);
										applyData.setImageOk(false);
										applyData.setPolicyStatus("14");
										applyData.setExceptionStatus("14");
										applyData.setVerifyResult("保補件要保書影像檔不存在");
										if(update)
											((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
										else
											((VoService) Constant.getContext().getBean("voServiceProxy")).save(applyData);
										
										ErrorReport er = new ErrorReport();
										er.setErrHappenTime(new Date());
										er.setErrorType("errImg");
										er.setOldBatchName(oldBatchName);
										er.setException(true);
										er.setReported(false);											
										er.setTitle("error on:" + line);
										er.setMessageBody(oldBatchName + "保補件要保書影像檔" + fileNm + "不存在");

										continueNext = true; // 通知繼續下一個
										// 移到影像檔不完全目錄
										FilesUtils.moveToDirectory(file, new File(Properties.getImgUncompletePath() + center + "/"), true);

									    logger.info("move file "
													+ file.getAbsolutePath());
										break;
									}
									//回饋檔中寫進一筆
									write692(cycleDate, folder692, applyData, center);
									
									applyData.setImageOk(false);
									imgFile = imgFilesError.get(fileNm);
									
									ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("errImg");
									er.setOldBatchName(oldBatchName);
									er.setException(true);
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
										   if(requestPolicy){
											   imgFile.setImage(false);
											   imgFile.setReqPolicy(true);
											   imgFile.setLaw(false);
										   }else if(fileNm.indexOf("image/") >= 0){
											   imgFile.setImage(true);
											   imgFile.setReqPolicy(false);
											   imgFile.setLaw(false);
										   }else if(fileNm.indexOf("law/") >= 0){
											   imgFile.setImage(false);
											   imgFile.setReqPolicy(false);
											   imgFile.setLaw(true);
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
									//非REIS及CONV時才加計
									if((!"REIS".equalsIgnoreCase(applyData.getSourceCode()) && !"CONV".equalsIgnoreCase(applyData.getSourceCode())))
									   imageErrorCounter++;
									continueNext = true;
									break;
								}							
							}								
						}
						if(continueNext){
							logger.info("continue next file");
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
						er.setException(true);
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
				//如果保單號碼和受理编號相同時，就把地址檔的數據copy過來
                if(applyDatas != null && "06".equals(center) && 
                		(applyData.getSourceCode().toUpperCase().equals("NORM") || applyData.getSourceCode().toUpperCase().equals("REPT"))){
              	   boolean equals = false;
                   for(ApplyData addData : applyDatas){                	   
             	      if(applyData.getApplyNo().toUpperCase().equals(addData.getApplyNo().toUpperCase()) ){ // 受理编號相同時進入
             		      //保單號碼可能有多個
             	    	  for(String policyNo : addData.getPolicyNoSet()){
             			      if(applyData.getPolicyNoSet().contains(policyNo)){
             			    	  logger.info("find equals:" + applyData.getPolicyNos() + " | " + applyData.getApplyNo());
             			    	  equals = true; //如果保單號碼也相同，就代表是相同一本保單
             			    	  break;
             			      }
             		      }
             		      //如果保單號碼和受理编號相同時
             		      if(equals){
             		    	  applyData.setAgentNm(addData.getAgentNm());
             		    	  if(addData.getParseNorm() != null && addData.getParseNorm())
             		    	     applyData.setDeliverType(addData.getDeliverType());             		    	  
             		    	 
             		    	  applyData.setZip(addData.getZip());
             		    	  applyData.setAddress(addData.getAddress());
             		    	  applyData.setMailType(addData.getMailType());
             		    	  applyData.setMailReceiptIndex(addData.getMailReceiptIndex());
             		    	  
             		    	  applyData.setReceiver(addData.getReceiver());
             		    	  applyData.setReceiverBank(addData.getReceiverBank());
             		    	  //需送金單，且非簽收單時
             		    	  if(applyData.getHavaBkReceipt() != null && applyData.getHavaBkReceipt() && applyData.getReceipt() != null && !applyData.getReceipt()){
             		    	      applyData.setBankReceiptId(addData.getBankReceiptId());             		    	      
             		    	      //檢查送金單是不是已經寄出，如果已寄出，抽掉此送金單，並寄送通知
             		    	      try{
             		    	    	  Set<String> substractIds = new HashSet<String>();
             		    	    	  Set<String> bankReceiptIds = applyData.getBankReceiptIdSet();             		    	    	  
             		    	    	  for(String bankReceiptId : bankReceiptIds){             		    	    		 
             		    	    		  //如果送金單已寄出，就不寫入送金單號碼
             		    	    		 BankReceipt  bankReceipt = ((VoService) Constant.getContext().getBean("voServiceProxy")).findBk(bankReceiptId);
             		    	    		 //如果是不同保單時，有可能要抽出送金單
             		    	    		 if(bankReceipt != null && bankReceipt.getOldBatchName() != null
             		    	    				 && !"".equals(bankReceipt.getOldBatchName())
             		    	    				 && !applyData.getOldBatchName().equals(bankReceipt.getOldBatchName())){
             		    	    			 ApplyData ad = ((VoService) Constant.getContext().getBean("voServiceProxy")).getApplyData(bankReceipt.getOldBatchName());
             		    	    			 boolean allMatch = true;             		    	    			 
											 if (ad != null) {
												Set<String> policyNoSet = ad.getPolicyNoSet();
												for (String policyNo : policyNoSet) {
													if (!applyData.getPolicyNoSet().contains(policyNo)){
														allMatch = false;
														break;
													}
												}
												//如果保單號碼相同，代表此送金單在之前已配送走												
												if (allMatch) {
													substractIds.add(bankReceiptId);
													ErrorReport er = new ErrorReport();
													er.setErrHappenTime(new Date());
													er.setErrorType("Check_Bk");
													er.setOldBatchName("");
													er.setReported(false);
													er.setMessageBody("地址檔規定送金單"
																+ bankReceiptId
																+ "配"
																+ applyData.getOldBatchName()
																+ "此保單，但送金單已跟隨" + ad.getOldBatchName() + "寄出，此送金單號碼抽出");
													er.setTitle("Check Bank Receipt");
													((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
												} else {
													// 如果送金單號碼相同，但保單號碼不相同，代表有問題，直接歸類成驗單錯誤
													ErrorReport er = new ErrorReport();
													er.setErrHappenTime(new Date());
													er.setErrorType("Check_Bk");
													er.setOldBatchName("");
													er.setReported(false);
													er.setException(true);
													er.setMessageBody("地址檔規定送金單"
															+ bankReceiptId
															+ "配"
															+ applyData.getOldBatchName()
															+ "保單，但送金單已配送另一本保單"
															+ bankReceipt.getOldBatchName()
															+ "此兩本保單不是相同保單號碼。故轉檔後將直接列為驗單錯誤");
													er.setTitle("Check Bank Receipt");
													((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
													applyData.setExceptionStatus("41");
													String tmp = "送金單" + bankReceipt.getBankReceiptId() + "於"
																+ Constant.slashyyyyMMdd.format(bankReceipt.getPackDate())
																+ "連保單寄出";
													applyData.setVerifyResult(tmp);
												}
											}else{												
												ErrorReport er = new ErrorReport();
												er.setErrHappenTime(new Date());
												er.setErrorType("Check_Bk");
												er.setOldBatchName(bankReceipt.getOldBatchName());
												er.setReported(false);
												er.setException(true);
												er.setMessageBody("舊送金單指向的" + bankReceipt.getOldBatchName() + "不存在，造成錯誤");
												bankReceipt.setOldBatchName(null);
												er.setTitle("Check Bank Receipt");
												((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
												((VoService) Constant.getContext().getBean("voServiceProxy")).update(bankReceipt);
											}
             		    	    		 }
             		    	    	  }
             		    	    	  
             		    	    	  if(substractIds.size() > 0){             		    	    			 
             		    	    	     for(String bankReceiptId : substractIds){             		    	    	    	               		    	    	        	
             		    	    	        bankReceiptIds.remove(bankReceiptId);             		    	    	        	
             		    	    	     }
             		    	    	     applyData.setBankReceiptIdSet(bankReceiptIds);             		    	    		               		    	    	                   		    	    	      
             		    	    	  }
             		    	      }catch(Exception e){
             		    	    	  logger.error("", e);
             		    	    	 ErrorReport er = new ErrorReport();
             						 er.setErrHappenTime(new Date());
             						 er.setErrorType("Check_Bk");
             						 er.setOldBatchName("");
             						 er.setReported(false);
             						 er.setException(true);
             						 er.setMessageBody(e.getMessage());
             						 er.setTitle("Check Bank Receipt");
             						 ((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
             		    	      }
             		    	      if("".equals(addData.getBankReceiptId())){
             		    	    	  applyData.setHavaBkReceipt(false);
             		    	      }
             		    	  }
             		    	  applyData.setParseNorm(addData.getParseNorm());
             		    	  applyData.setPareseString(addData.getPareseString());
             		    	  break;
             		      }
             	      }
                   }
                   if("06".equals(center) && applyData.getSourceCode().toUpperCase().equals("REPT")){
                 	   //北二的件如果不是新契約，送北二行政中心
                 	   applyData.setReceiver(taipeiNo2.getAreaName());
                 	   applyData.setAddress(taipeiNo2.getZipCode() + " " + taipeiNo2.getAddress());
                 	   applyData.setZip(taipeiNo2.getZipCode());
                   }
      		       //找不到時送北二行政中心
                   if(!equals){
                	  applyData.setHavaBkReceipt(false);
                 	  applyData.setReceiver(taipeiNo2.getAreaName());
                 	  applyData.setAddress(taipeiNo2.getZipCode() + " " + taipeiNo2.getAddress());
                 	  applyData.setZip(taipeiNo2.getZipCode());
                   }      		      
                }else if("06".equals(center) && !applyData.getSourceCode().toUpperCase().equals("NORM")){
             	   //北二的件如果不是新契約，送北二行政中心
             	   applyData.setReceiver(taipeiNo2.getAreaName());
             	   applyData.setAddress(taipeiNo2.getZipCode() + " " + taipeiNo2.getAddress());
             	   applyData.setZip(taipeiNo2.getZipCode());             	   
                }
				
				if(update)
					((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
				else
					((VoService) Constant.getContext().getBean("voServiceProxy")).save(applyData);                
                try{
                   
                   String taipeiNo2key = null;
                   String mailAddKey = null;
                   //如果receiver和address都有值時
                   if(applyData.getReceiver() != null && applyData.getAddress() != null){
                	   //key值為收件人加地址
                       taipeiNo2key = applyData.getReceiver() + "_" + applyData.getAddress();
                       mailAddKey = applyData.getAddress().trim();
					   String value = mailMap.get(taipeiNo2key);
					   String mailAdd = mailAddMap.get(mailAddKey);
					   if(value == null){
						   mailMap.put(taipeiNo2key, StringUtils.leftPad((mailMap.size() + 1) + "", 4, '0'));						   
						   logger.info("put address:" + taipeiNo2key + "into map " +  (mailMap.size() + 1));						                                
					   }
					   if(mailAdd == null){
						   mailAddMap.put(mailAddKey, StringUtils.leftPad((mailAddMap.size() + 1) + "", 4, '0'));						   
						   logger.info("put address:" + mailAddKey + "into mailAddMap " +  (mailAddMap.size() + 1));						                                
					   }					   
                   }
                   String subAreaId = applyData.getSubAreaId().replaceAll("\\*", "&");
                   subAreaId = StringUtils.rightPad(subAreaId, 7, '0');
                   String areaId = applyData.getAreaId().replaceAll("\\*", "&");
                   areaId = StringUtils.rightPad(areaId, 7, '0');
                   String addressCode = addressMap.get(applyData.getServiceCenter() + applyData.getAreaAddress());
                   String zip = (applyData.getZip() == null || applyData.getZip().length() < 3)? "00000" : StringUtils.rightPad(applyData.getZip(), 5, '0'); 
				   if(center != null && !receipt && conjugateTrue && !center.equals("06")){		
				      FilesUtils.moveFileToDirectory(file, new File(Properties.getCheckedOkPath() + fromWhere + "/" + center + "/" + zip + addressCode + "_" + subAreaId), true);
				      if(applyData.getSourceCode().toUpperCase().equals("NORM")){
						  normCounter++;
					  }else if(applyData.getSourceCode().toUpperCase().equals("REPT")){
						  reptCounter++;
					  }else if(applyData.getSourceCode().toUpperCase().equals("REIS")){
						  reisCounter++;
					  }else if(applyData.getSourceCode().toUpperCase().equals("CONV")){
						  convCounter++;
					  }
				   }else if(center != null && receipt && conjugateTrue && !center.equals("06")){
				      FilesUtils.moveFileToDirectory(file, new File(Properties.getReceiptOkPath() + fromWhere + "/" + center + "/" + zip + addressCode + "_" + subAreaId), true);
				   }else if(!receipt && conjugateTrue && "06".equals(center)){
					   
					   String minPolicyNo = null;
					   for(String policyNo : applyData.getPolicyNoSet()){
				    	 if(minPolicyNo == null ||  minPolicyNo.compareTo(policyNo) > 0){
				    		 minPolicyNo = policyNo;
				         }					        		
				       }			
					   //06時用sourceCode去分類	
					    if(thirdStage){
					    	//依通路名分成12類
					    	switch(applyData.getSourceCode().toUpperCase() + (applyData.getChannelName() == null ? "" : applyData.getChannelName().trim())){
					    	   case "NORM國泰金控":
					    		   if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
					    			   addressCode = "0020";
					    	       }else if(applyData.getDeliverType().equals("P") || applyData.getDeliverType().equals("O")){
					    		      addressCode = "0001";
					    	       }else if("S".equals(applyData.getDeliverType())){ 
					    	    	  applyData.setChannelName("保代直效通路");
					    	    	  applyData.setChannelID("A");
					    	    	  applyData.setPackType("03");
					    	    	  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					    			  addressCode = "0013";
					    		   }
					    		   
					    	       break;
					    	   case "NORM永豐保代":
					    		   addressCode = "0003";					    		   
					    		   if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
					    			   addressCode = "0020";
					    		   }else if("S".equals(applyData.getDeliverType())){
					    			  applyData.setPackType("03");
						    		  addressCode = "0012";					    	
					    		   }else{
					    			   applyData.setPackType("03");
					    			   /*
					    			  applyData.setAddress("104 台北市中山區南京東路三段３６號１０樓");
					    			  applyData.setZip("104");
					    			  applyData.setReceiver("郭淑婷");
					    			  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					    			  */
					    		   } 
				    			   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);	   
					    		   break;
					    	   case "NORM中租保代":
					    		   addressCode = "0004";
					    		   if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
					    			   addressCode = "0020";
					    		   }else if("S".equals(applyData.getDeliverType())){
						    			  applyData.setPackType("03");
							    		  addressCode = "0012";					    	
						    	   }else{
						    		   applyData.setPackType("03");
					    			   /*
						    		  applyData.setAddress("114 台北市內湖區瑞光路３６２號２樓");
						    		  applyData.setZip("114");
						    		  applyData.setReceiver("巫月珍");
						    		  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
						    		  */
						    	   } 
					    		   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					    		   break;
					    	   case "NORM群益保代":
					    		   addressCode = "0005";
					    		   if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
					    			   addressCode = "0020";
					    		   }else if("S".equals(applyData.getDeliverType())){
						    		  applyData.setPackType("03");
							    	  addressCode = "0012";					    	
						    	   }else{
						    		  applyData.setPackType("03");
						    	   }
					    		   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					    		   break;
					    	   case "NORM台新保代":
					    		   addressCode = "0006";
					    		   if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
					    			   addressCode = "0020";
					    		   }else if("S".equals(applyData.getDeliverType())){
						    		   applyData.setPackType("03");
							    	   addressCode = "0012";					    	
						    	   }else{
						    		   applyData.setPackType("03");
						    	   }		
				    			   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					    		   break;
					    	   case "NORM中信保代":
					    		   addressCode = "0007";
					    		   if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
					    			   addressCode = "0020";
					    		   }else if("S".equals(applyData.getDeliverType())){
						    		   applyData.setPackType("03");
							    	   addressCode = "0012";					    	
						           }else{
						        	   applyData.setPackType("03"); 
						    		  /*
							    	  applyData.setAddress("110 台北市信義區永吉路１６號１３樓");
							    	  applyData.setZip("110");
							    	  applyData.setReceiver("中信保經 文件處理中心");
							    	  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
							    	  */
							       } 
				    			   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					    		   break;
					    	   case "NORM非金控保代通路":
					    		   addressCode = "0008";
					    		   if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
					    			   addressCode = "0020";
					    		   }else if("S".equals(applyData.getDeliverType())){
						    		   applyData.setPackType("03");
							    	   addressCode = "0012";					    	
						    	   }else{
						    		   applyData.setPackType("03");
						    	   }
				    			   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					    		   break;
					    	   case "NORM國壽直效通路":
					    		   if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType()))
					    			   addressCode = "0020";
					    		   else if(applyData.getAreaId().toUpperCase().startsWith("TQ"))
					    		       addressCode = "0009";
					    		   else if(applyData.getAreaId().toUpperCase().startsWith("TX"))
					    		       addressCode = "0009"; 
					    		   else if(applyData.getAreaId().toUpperCase().startsWith("TC"))
					    		       addressCode = "0010"; 
					    		   else if(applyData.getAreaId().toUpperCase().startsWith("TN"))
					    		       addressCode = "0011"; 					    		   
					    		   break;
					    	   case "NORM保代直效通路":
					    		   addressCode = "0013"; 	
					    		   if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType()))
					    			   addressCode = "0020";					    		   
					    		   break;
					    	   case "REPT":
					    		   addressCode = "0021";
					    		   break;
					    	   case "REIS":
					    		   addressCode = "0022";
					    		   break;
					    	   case "CONV":
					    		   addressCode = "0023";
					    		   break;
					    	}
					    	
					    	if(taipeiNo2key != null)
					    	   areaId = mailMap.get(taipeiNo2key); 
					    	if(mailAddKey != null)
					    	   areaId = mailAddMap.get(mailAddKey) + areaId;
					    	String verifyResult = null;
					    	if(addressCode != null && addressCode.compareTo("0020") < 0){
					    		//0001一定是單掛
					    		if("0001".equals(addressCode) && "雙".equals(applyData.getMailType())){
					    			ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("Mail Type");
									er.setOldBatchName(oldBatchName);
									er.setReported(false);
									er.setException(true);
									verifyResult = "金控類設定為雙掛";
									er.setMessageBody(oldBatchName + "|" + verifyResult);
									er.setTitle("mail type error");
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
									
									applyData.setExceptionStatus("41");									
									applyData.setVerifyResult(verifyResult);
									((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
									
					    		}else if(!"0001".equals(addressCode) && "單".equals(applyData.getMailType())){
					    			ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("Mail Type");
									er.setOldBatchName(oldBatchName);
									er.setReported(false);
									er.setException(true);
									verifyResult = "非金控類設定為單掛";
									er.setMessageBody(oldBatchName + "|" + verifyResult);
									er.setTitle("mail type error");
									((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
									
									applyData.setExceptionStatus("41");									
									applyData.setVerifyResult(verifyResult);
									((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
									
					    		}else if(!"0001".equals(addressCode) && "雙".equals(applyData.getMailType())){
					    			 String receiptInd = "(業)";
					    			 if(applyData.getAreaId().toUpperCase().equals("ON88888")){
							        	 receiptInd = "";
							         }else if(applyData.getChannelID() != null && applyData.getChannelID().toUpperCase().equals("A")){
								         receiptInd = "(要)";
							         }else if(applyData.getChannelID() != null && applyData.getChannelID().toUpperCase().equals("B")){
								         receiptInd = "(直)";
							         }else if(applyData.getChannelID() != null && applyData.getChannelID().toUpperCase().equals("G")){
							    	     receiptInd = "(要)";
							         }

					    			 if(!receiptInd.equals(applyData.getMailReceiptIndex())){
					    				 ErrorReport er = new ErrorReport();
										 er.setErrHappenTime(new Date());
										 er.setErrorType("Mail Type");
										 er.setOldBatchName(oldBatchName);
										 er.setReported(false);
										 er.setException(true);
										 verifyResult = "系統認定為" + receiptInd + "，但地址檔指定為" + applyData.getMailReceiptIndex(); 
										 er.setMessageBody(oldBatchName + "|" + verifyResult);
										 er.setTitle("pack type error");
										 ((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
										 
										 applyData.setPolicyStatus("41");
										 applyData.setVerifyResult(verifyResult);
										 ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);										 
					    			 }
					    		}
					    	}
					    	
				        }else if(applyData.getSourceCode().toUpperCase().equals("NORM")){
						   String first2AreaId = "";
                       	   String key = applyData.getOldBatchNo();
                       	   if(applyData.getAreaId() != null && applyData.getAreaId().length() >= 2){
                       		  first2AreaId = applyData.getAreaId().substring(0, 2).toUpperCase();
                       		  key = key + first2AreaId;
                       	   }                        
						   addressCode = normMap.get(key);
					   }else if(applyData.getSourceCode().toUpperCase().equals("REPT")){
						   addressCode = reptMap.get(applyData.getOldBatchNo());
					   }else if(applyData.getSourceCode().toUpperCase().equals("REIS")){
						   addressCode = "7000";
					   }else if(applyData.getSourceCode().toUpperCase().equals("CONV")){
						   addressCode = "8000";
					   }
					   String zipCode = (applyData.getZip() == null || applyData.getZip().length() < 3)? "00000" : StringUtils.rightPad(applyData.getZip(), 5, '0'); 
					   FilesUtils.moveFileToDirectory(file, new File(Properties.getCheckedOkPath() + fromWhere + "/" + center + "/" + addressCode + "_" + zipCode + areaId + minPolicyNo), true);
					   if(applyData.getSourceCode().toUpperCase().equals("NORM")){
						   normCounter++;
					   }else if(applyData.getSourceCode().toUpperCase().equals("REPT")){
						   reptCounter++;
					   }else if(applyData.getSourceCode().toUpperCase().equals("REIS")){
						   reisCounter++;
					   }else if(applyData.getSourceCode().toUpperCase().equals("CONV")){
						   convCounter++;
					   }
					   
				   }else if(receipt && conjugateTrue && "06".equals(center)){					   
					   String minPolicyNo = null;
					   for(String policyNo : applyData.getPolicyNoSet()){
				    	 if(minPolicyNo == null ||  minPolicyNo.compareTo(policyNo) > 0){
				    		 minPolicyNo = policyNo;
				         }					        		
				       }			
					   //06時用sourceCode去分類		
					   if(thirdStage){
					    	//依通路名分成12類
						   switch(applyData.getSourceCode().toUpperCase() + (applyData.getChannelName() == null ? "" : applyData.getChannelName().trim())){
				    	      case "NORM國泰金控":
				    		      if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
				    			      addressCode = "0020";
				    	          }else if(applyData.getDeliverType().equals("P") || applyData.getDeliverType().equals("O")){
				    		         addressCode = "0001";
				    	          }else if("S".equals(applyData.getDeliverType())){ 
				    	    	     applyData.setChannelName("保代直效通路");
				    	    	     applyData.setChannelID("A");
				    	    	     applyData.setPackType("03");
					    			 ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
				    			     addressCode = "0013";
				    		      }				    		   				    		  
				    	          break;
				    	      case "NORM永豐保代":
				    		      addressCode = "0003";
				    		      if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
				    			      addressCode = "0020";
				    		      }else if("S".equals(applyData.getDeliverType())){
					    			  applyData.setPackType("03");
						    		  addressCode = "0012";					    	
					    		  }else{
					    			  applyData.setPackType("03"); 
				    			      /*
				    			     applyData.setAddress("104 台北市中山區南京東路三段３６號１０樓");
				    			     applyData.setZip("104");
				    			     applyData.setReceiver("郭淑婷");
				    			     ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
				    			     */
				    		      } 
				    			  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
				    		      break;
				    	      case "NORM中租保代":
				    		      addressCode = "0004";
				    		      if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
				    			      addressCode = "0020";
				    		      }else if("S".equals(applyData.getDeliverType())){
					    			  applyData.setPackType("03");
						    		  addressCode = "0012";					    	
					    		  }else{
					    			  applyData.setPackType("03"); 
				    			      /*
					    		     applyData.setAddress("114 台北市內湖區瑞光路３６２號２樓");
					    		     applyData.setZip("114");
					    		     applyData.setReceiver("巫月珍");
					    		     ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					    		     */
					    	      }
				    			  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
				    		      break;
				    	      case "NORM群益保代":
				    		      addressCode = "0005";
				    		      if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
				    			      addressCode = "0020";
				    		      }else if("S".equals(applyData.getDeliverType())){
					    			 applyData.setPackType("03");
						    		 addressCode = "0012";					    	
					    		  }else{
					    			 applyData.setPackType("03");
					    		  }
				    			  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					    	      break;
				    	      case "NORM台新保代":
				    		      addressCode = "0006";
				    		      if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
				    			      addressCode = "0020";
				    		      }else if("S".equals(applyData.getDeliverType())){
					    			 applyData.setPackType("03");
						    		 addressCode = "0012";					    	
					    		  }else{
					    			 applyData.setPackType("03");
					    		  }
				    			  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
				    		      break;
				    	      case "NORM中信保代":
				    		      addressCode = "0007";
				    		      if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
				    			      addressCode = "0020";
				    		      }else if("S".equals(applyData.getDeliverType())){
					    			  applyData.setPackType("03");
						    		  addressCode = "0012";					    	
					    		  }else{
					    			  applyData.setPackType("03");
					    		      /*
							         applyData.setAddress("110 台北市信義區永吉路１６號１３樓");
							         applyData.setZip("110");
							         applyData.setReceiver("中信保經 文件處理中心");
							         ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
							         */
							      }
				    			  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
				    		      break;
				    	      case "NORM非金控保代通路":
				    		      addressCode = "0008";
				    		      if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType())){
				    			      addressCode = "0020";
				    		      }else if("S".equals(applyData.getDeliverType())){
					    			  applyData.setPackType("03");
						    		  addressCode = "0012";					    	
					    		  }else{
					    			  applyData.setPackType("03");
					    		  }
				    			  ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
				    		      break;
				    	      case "NORM國壽直效通路":		
				    		      if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType()))
				    			      addressCode = "0020";
				    		      else if(applyData.getAreaId().toUpperCase().startsWith("TQ"))
				    		          addressCode = "0009"; 
				    		      else if(applyData.getAreaId().toUpperCase().startsWith("TX"))
				    		          addressCode = "0009"; 
				    		      else if(applyData.getAreaId().toUpperCase().startsWith("TC"))
				    		          addressCode = "0010"; 
				    		      else if(applyData.getAreaId().toUpperCase().startsWith("TN"))
				    		          addressCode = "0011"; 				    		   
				    		      break;
				    	      case "NORM保代直效通路":
				    		      addressCode = "0013"; 	
				    		      if((applyData.getReceiver() != null && applyData.getReceiver().equals(taipeiNo2.getAreaName())) || "B".equals(applyData.getDeliverType()))
				    			      addressCode = "0020";				    		   
				    		      break;
				    	      case "REPT":
				    		      addressCode = "0021";
				    		      break;
				    	      case "REIS":
				    		      addressCode = "0022";
				    		   break;
				    	      case "CONV":
				    		      addressCode = "0023";
				    		      break;				    	    
				    		}										    	
					    	if(taipeiNo2key != null)
					    	   areaId = mailMap.get(taipeiNo2key);
					    	if(mailAddKey != null)
						       areaId = mailAddMap.get(mailAddKey) + areaId;
					    	
					    	
				        }else if(applyData.getSourceCode().toUpperCase().equals("NORM")){
						   String first2AreaId = "";
                       	   String key = applyData.getOldBatchNo();
                       	   if(applyData.getAreaId() != null && applyData.getAreaId().length() >= 2){
                       		  first2AreaId = applyData.getAreaId().substring(0, 2).toUpperCase();
                       		  key = key + first2AreaId;
                       	   }                        
						   addressCode = normMap.get(key);						   
					   }else if(applyData.getSourceCode().toUpperCase().equals("REPT")){
						   addressCode = reptMap.get(applyData.getOldBatchNo());
					   }else if(applyData.getSourceCode().toUpperCase().equals("REIS")){
						   addressCode = "7000";
					   }else if(applyData.getSourceCode().toUpperCase().equals("CONV")){
						   addressCode = "8000";
					   }
					   //北二正式上線時打開，不列印簽收回條的處理					   
					   if((fromWhere == 'B' || fromWhere == 'O') 
							   && (("A".equals(applyData.getChannelID()) && "06".equals(center) )
							      || "WEB".equals(applyData.getAreaName()) 
							      || "0012".equals(addressCode) 
							      || "0013".equals(addressCode))
						   ){ 
						   File notPrintReceipts = new File(new File(Properties.getCheckedOkPath()).getParentFile(), "notPrint");
						   if(!notPrintReceipts.exists())
							   notPrintReceipts.mkdirs();						   
						       //移去notPrintReceipts資料夾						   
							   FilesUtils.moveFileToDirectory(file, new File(notPrintReceipts,   Constant.yyyyMMdd.format(cycleDate)), true);
						   applyData.setPolicyStatus("00");
					      ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					   }else{
						  String zipCode = (applyData.getZip() == null || applyData.getZip().length() < 3)? "00000" : StringUtils.rightPad(applyData.getZip(), 5, '0');
					      FilesUtils.moveFileToDirectory(file, new File(Properties.getReceiptOkPath() + fromWhere + "/" + center + "/" + addressCode + "_" + zipCode + areaId + minPolicyNo), true);
					   }
					   
				   }else if(!conjugateTrue){
					   if(applyData.getExceptionStatus().equals("13") || applyData.getExceptionStatus().equals("14"))
					       FilesUtils.moveToDirectory(file, new File(Properties.getImgUncompletePath() + center + "/" ), true);
					   else
						   FilesUtils.moveToDirectory(file, new File(Properties.getDifficultFontPath() + center + "/" ), true);
					
				   }
                }catch(Exception e){
                	logger.error("", e);
                	e.printStackTrace();
					ErrorReport er = new ErrorReport();
					er.setErrHappenTime(new Date());
					er.setErrorType("exception");
					er.setOldBatchName(oldBatchName);
					er.setReported(false);
					er.setException(true);
					er.setMessageBody("exception happen:" + oldBatchName + "|" + e.getMessage());
					er.setTitle("exception happened");
					((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);                	
                }
                
			}//end
            //else if(fromWhere == 'B' || fromWhere == 'T' || fromWhere == 'O') 	
			
			//如果是強制設為新契約時，在這裡把它改回來
			if(forceNorm){
				if(applyData != null)
				   applyData.setSourceCode(fileNmSplit[3].toUpperCase());
				((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
			}
			
		} //end for(File file : allFiles) 
	    //回傳順序 
		if(oldSerialNo == 0)
			return 0;
		else
		    return oldSerialNo - 1;		
	}
	
	public static boolean getRunning(){
		return running;
	}
	
	private static void write692(Date cycleDate, File folder692, ApplyData applyData, String center){
		File dtata692 = null;											
		String cycleDateStr = Constant.yyyyMMdd.format(cycleDate);																				
		dtata692 = new File(folder692, "DTATA692_FXDMS");											
		FileOutputStream fos692 = null;
		OutputStreamWriter osw692 = null;
		BufferedWriter bw692 = null;
		try{
		   fos692 = new FileOutputStream(dtata692, true);
		   osw692 = new OutputStreamWriter(fos692, "ms950");
		   bw692 = new BufferedWriter(osw692);
		   Set<String> policyNoset = applyData.getPolicyNoSet();
		   String policyNo = "";
		   if(policyNoset != null){
			   for(String poNo : policyNoset){
				   policyNo = poNo;
				   String presTime = applyData.getPresTime() == null ? "" : Constant.yyyyMMdd.format(applyData.getPresTime());									   
				   String writeLine = cycleDateStr + "," + center + 
						   "," + policyNo + "," + StringUtils.leftPad(applyData.getReprint() + "", 2, '0') + "," 
						   + applyData.getApplyNo() + "," + applyData.getSourceCode() + ",N,Y,Y," + applyData.getOldBatchNo() + ",N," 
						   + (applyData.getReceipt()? "Y" : "N") + ",," 
						   + (applyData.getTotalPage() == null ? 0 : applyData.getTotalPage() == null) + "," + applyData.getAreaId() + ","   
				           + (applyData.getMerger()? "1" : "0") + "," + "FXDMS,P\r\n";
				   bw692.write(writeLine);											   
			   }
		   }									   									   
		   bw692.flush();
		   osw692.flush();
		   fos692.flush();
		}catch(Exception e){
			e.printStackTrace();
			logger.error("", e);
		}finally{
			if(bw692 != null){
				try {
					bw692.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bw692 = null;
			}
			if(osw692 != null){
				try {
					osw692.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				osw692 = null;
			}
			if(fos692 != null){
				try {
					fos692.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fos692 = null;
			}
		}
	} 
}
