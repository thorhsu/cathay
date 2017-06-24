package com.fxdms.cathy.task;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FileEncFactory;
import com.fxdms.util.FileFilterImpl;
import com.fxdms.util.FilesUtils;
import com.fxdms.util.SftpClientUtil;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.ErrorReport;

public class ZipListener {

	static Logger logger = Logger.getLogger(ZipListener.class);
    static boolean running = false;
	public synchronized static void startToRun() {
		if(running || GpPdfSorter.isRunning())
			return;
		
		logger.info("zip listener start to work");
		FileEncFactory fef = new FileEncFactory();
		File zipFolder = new File(Properties.getZipTmpPath());
		try {
			running = true;
		    if (!zipFolder.exists())
			    zipFolder.mkdirs();
		    File[] filesList = zipFolder.listFiles(FileFilterImpl
				   .getZipFilesFilter());
		
			if (zipFolder.isDirectory() && filesList != null
					&& filesList.length > 0) {

				SftpClientUtil sftpC = new SftpClientUtil();
				sftpC.setServerAddress(Properties.getFxdmsIP());
				sftpC.setUserId(Properties.getFxdmsUser());
				sftpC.setPassword(Properties.getFxdmsPwd());
				for (File uploadFile : filesList) {
					Date start = new Date();
					String newBatchName = uploadFile.getName().substring(0,
							uploadFile.getName().length() - 4);
					AfpFile afpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(newBatchName);			
					Set<ApplyData> applyDatas = null;

					if(afpFile != null){
						applyDatas = ((VoService) Constant.getContext().getBean("voServiceProxy")).getApplyDataByNewBatchNm(newBatchName);  //取得applyData
					}
					File encFile = new File(uploadFile.getParent(), newBatchName + ".zip.gpg"); 
					if(encFile.exists())
						FileUtils.forceDelete(encFile);
					fef.encodeFile(uploadFile, encFile); //檔案加密gpg
					
					if(afpFile != null){						   
					   Date now = new Date();
					   if(!"已交寄".equals(afpFile.getStatus())){
						  afpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(newBatchName);
					      afpFile.setGpged(true);
					      afpFile.setStatus("回傳中");
					      afpFile.setUpdateDate(new Date());  
					      afpFile.setBeginTransferTime(now);
					      ((VoService) Constant.getContext().getBean("voServiceProxy")).update(afpFile);
					   }
					   if(applyDatas != null)
					      for(ApplyData applyData : applyDatas){
					    	  if(!"100".equals(applyData.getPolicyStatus()) && applyData.getExceptionStatus() != null 
					    			  && !"".equals(applyData.getExceptionStatus()) && applyData.getTotalPage() != null && applyData.getTotalPage() > 0){
						         applyData.setPolicyStatus("20");
						         applyData.setBeginTransferTime(now);
						         applyData.setUpdateDate(new Date());
						         ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
					    	  }
					      }
					}
					
					//上傳SFTP server
					boolean success = sftpC.upload(	Properties.getFxdmsUploadPath(), encFile);
					if(success){
						FileWriter fow = new FileWriter(new File(zipFolder, newBatchName + ".ok") );
						fow.write("ok");
						fow.flush();
						fow.close();
						success = sftpC.upload(Properties.getFxdmsUploadPath(), new File(zipFolder, newBatchName + ".ok") );
						FileUtils.forceDelete(new File(zipFolder, newBatchName + ".ok") );
						//成功後zip檔移到備份資料夾						
						if(afpFile != null){						   
						   if(success){
							  Date end = new Date();
							  ErrorReport eReport = new ErrorReport();
				              eReport.setErrHappenTime(new Date());
				              eReport.setErrorType("FtpProcess");
				              eReport.setOldBatchName(null);
				              eReport.setReported(true);		
				              eReport.setMessageBody("傳輸" + afpFile.getNewBatchName() + ".zip 成功，耗時" + ((end.getTime() - start.getTime()) / 1000) + "秒");
				              eReport.setTitle("檔案傳送至FXDMS");
							  ((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport); 
							  if(!"已交寄".equals(afpFile.getStatus())){
								 afpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(newBatchName);
						         afpFile.setTransfered(true);
						         afpFile.setStatus("待印中");
						         afpFile.setEndTransferTime(end);
						         afpFile.setUpdateDate(new Date());
						         ((VoService) Constant.getContext().getBean("voServiceProxy")).update(afpFile);
							  }
						      if(applyDatas != null)
						         for(ApplyData applyData : applyDatas){
						        	 if(!"100".equals(applyData.getPolicyStatus()) 
						        			 &&(applyData.getExceptionStatus() == null || "".equals(applyData.getExceptionStatus())) 
						        			 && applyData.getTotalPage() != null && applyData.getTotalPage() > 0){
							            applyData.setPolicyStatus("25"); //更新applyData的狀態
							            applyData.setEndTransferTime(end);
							            applyData.setUpdateDate(new Date());
							            ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);							            
						        	 }						        	 
						         }
						      //update applyData set haveInsureCard = 1 where policyNos in (select policyNos from applyData where groupInsure = 1 and newBatchName = 'PD09B201508270001') and newBatchName = 'GA09B201508270001' and haveInsureCard = 0 and groupInsure = 0 and receipt = 0
						      
						      
						   }
						}
						if(success){
						   FilesUtils.moveFileToDirectory(uploadFile, new File(Properties.getBackupFolder() + 
								   "ZIP/" + Constant.yyyy_MM_dd.format(InputdateParser.getInputDate()) ), true);
						}
						
					}else{
						//傳檔失敗不作任何處理，也不發訊息，下次再傳
					}
					if(encFile.exists())
					   FilesUtils.forceDelete(encFile); //加密檔傳完後刪除
					
				}
			}
		} catch (Exception e) {			
			logger.error("", e);
			ErrorReport eReport = new ErrorReport();
            eReport.setErrHappenTime(new Date());
            eReport.setErrorType("exception");
            eReport.setOldBatchName(null);
            eReport.setReported(false);		
            eReport.setException(true);
            eReport.setMessageBody("傳輸失敗" + e.getMessage() + "。五分鐘後將再重試");
            eReport.setTitle("SFTP傳檔失敗");
			  ((VoService) Constant.getContext().getBean("voServiceProxy")).save(eReport);
		}finally{
			running = false;
			logger.info("ziplistener this time  stop");
		}
		
		
	}
	public static boolean isRunning() {
		return running;
	}
	public static void setRunning(boolean running) {
		ZipListener.running = running;
	}

}
