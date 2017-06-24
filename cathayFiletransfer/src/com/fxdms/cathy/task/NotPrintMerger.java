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

public class NotPrintMerger {

	static Logger logger = Logger.getLogger(NotPrintMerger.class);

	private static File tmpFolder = new File(Properties.getPresPath(), "tmp");
	private static SimpleDateFormat sdf = Constant.yyyyMMdd;
	private static boolean running = false;
	private static final File notPrintReceipts = new File(new File(
			Properties.getCheckedOkPath()).getParentFile(), "notPrint");

	static {
		if (!tmpFolder.exists())
			tmpFolder.mkdirs();
	}

	public synchronized static void startToRun() throws BeansException,
			RemoteException {
		try {
			//本身在執行或FileDispatcher或policyMerger還沒執行完畢時不執行
			if (!PolicyMerger.isMergeDone() || running || FileDispatcher.getRunning())
				return;
			running = true;
			if (!notPrintReceipts.exists()) {
				notPrintReceipts.mkdirs();
			}
			File[] folders = notPrintReceipts.listFiles(FileFilterImpl
					.getDirectoryFilter());
			if (folders != null && folders.length > 0) {
				for (File cycleDateFolder : folders) {
					Date cycleDate = null;
					String newBatchName = null;
					try {
						cycleDate = Constant.yyyyMMdd.parse(cycleDateFolder
								.getName());
						newBatchName = "SG06B" + cycleDateFolder.getName()
								+ "9999";
						AfpFile afpFile = null;						
						afpFile = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(newBatchName);
						boolean update = true;
						if(afpFile == null){
							afpFile = new AfpFile();
							update = false;
						}
						afpFile.setNewBatchName(newBatchName);
						afpFile.setAreaId("noPri");
						afpFile.setBatchOrOnline("B");
						afpFile.setCenter("06");
						afpFile.setCenterSerialNo(0);
						afpFile.setCycleDate(cycleDate);
						afpFile.setFeedback(false);
						afpFile.setGpged(false);
						afpFile.setInsertDate(new Date());
						afpFile.setReceipt(true);
						afpFile.setStatus("轉檔中");
						afpFile.setUpdateDate(new Date());						
						afpFile.setUnziped(false);
						afpFile.setZiped(false);
						if(update)
						   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(afpFile);
						else
						   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(afpFile);
					} catch (Exception e) {
						FileUtils.deleteDirectory(cycleDateFolder);
						continue;
					}

					try {
						File[] applyDatas = cycleDateFolder
								.listFiles(FileFilterImpl.getFileFilter());
						for (File applyData : applyDatas) {
							ApplyData applyDataDb = ((VoService) Constant
									.getContext().getBean("voServiceProxy"))
									.getApplyData(applyData.getName());
							//找出對應的保單
							ApplyData policyAd = ((VoService) Constant
									.getContext().getBean("voServiceProxy"))
									.getApplyData(applyData.getName().replaceAll("簽收回條", "保單"));
							applyDataDb.setNewBatchNo(null);
							applyDataDb.setNewSerialNo(null);
							applyDataDb.setPresTime(new Date());
							applyDataDb.setNewBatchName(newBatchName);
							if(policyAd != null){
								applyDataDb.setRecName(policyAd.getRecName());
								applyDataDb.setPolicyNos(policyAd.getPolicyNos());
							}
							boolean substract = applyDataDb.getSubstract() == null ? false
									: applyDataDb.getSubstract();
							if (!substract)
								applyDataDb.setPolicyStatus("15");
							else
								applyDataDb.setPolicyStatus("18");
							applyDataDb.setUpdateDate(new Date());
							((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyDataDb);

							// 非抽件才合併
							if (!substract) {
								FileInputStream fis = null;
								BufferedInputStream bis = null;
								BufferedOutputStream bos = null;
								FileOutputStream fos = null;
								// 開始進行合併保單
								try {
									byte[] bufferedB = new byte[2048];
									fos = new FileOutputStream(new File(
											tmpFolder, newBatchName + ".tmp"),
											true);
									bos = new BufferedOutputStream(fos);
									fis = new FileInputStream(applyData);
									bis = new BufferedInputStream(fis);
									int readLen;
									while ((readLen = bis.read(bufferedB)) > 0) {
										bos.write(bufferedB, 0, readLen);
									}
									bos.flush();
									fos.flush();
								} catch (Exception e) {
									logger.error("", e);
									ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("exception");
									er.setOldBatchName("");
									er.setReported(false);
									er.setException(true);
									er.setMessageBody("exception happen:"
											+ e.getMessage());
									er.setTitle("exception happened");
									((VoService) Constant.getContext().getBean(
											"voServiceProxy")).save(er);
								} finally {
									if (bos != null)
										bos.close();
									if (fos != null)
										fos.close();
									if (bis != null)
										bis.close();
									if (fis != null)
										fis.close();
								}
							}
							FilesUtils.moveFileToDirectory(applyData,
									new File(Properties.getBackupFolder()
											+ "DATA/"
											+ Constant.yyyyMMdd
													.format(InputdateParser
															.getInputDate())
											+ "/applyData/notPrint/"), true);
						} // for (File applyData : applyDatas)						
						if(new File(tmpFolder, newBatchName + ".tmp").exists()){									
							new File(tmpFolder, newBatchName + ".tmp").renameTo(new File(Properties.getPresPath(), newBatchName + ".DAT"));
						}						
						if(applyDatas != null && applyDatas.length > 0){
							ErrorReport er = new ErrorReport();
							er.setErrHappenTime(new Date());
							er.setErrorType("NOT_PRINT_FILES");
							er.setOldBatchName("");
							er.setReported(true);
							er.setException(false);
						    er.setMessageBody(newBatchName + "簽收單處理完畢:共" + applyDatas.length + "張");
							er.setTitle("Not Print_Files");
							((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
						}
						FileUtils.deleteDirectory(cycleDateFolder);
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}

		} catch (Exception e) {
			logger.error("", e);
		} finally {
			logger.info("not print receipt stop.");
			running = false;
		}

	}

}
