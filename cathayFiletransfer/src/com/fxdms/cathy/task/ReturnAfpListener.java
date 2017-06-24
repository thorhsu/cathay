package com.fxdms.cathy.task;

import java.io.*;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
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

public class ReturnAfpListener {

	static Logger logger = Logger.getLogger(ReturnAfpListener.class);


	public static void startToRun() {
				// 發現afp檔案
		String afpFolder = Properties.getAfpPath();

		File afpPath = new File(afpFolder);
		File[] afpFiles = null;
		if (afpPath.exists()) {
			afpFiles = afpPath.listFiles(FileFilterImpl.getReturnFileFilter());
		} else {
			logger.error("afpPath exist :" + afpPath.exists());
		}


		if (afpFiles != null && afpFiles.length > 0) {
			for (File afpFile : afpFiles) {
					String afpFileNm = afpFile.getName();
					String newBatchName = afpFileNm.substring(0, 17); // 17位長的newBatchName
					String logFileNm = newBatchName + ".csv"; // 問一下pipi是不是長得如此
					File logFile = new File(afpPath, logFileNm);
					if (afpFile.length() < 2048) {
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("Afp Convert Err");
						er.setOldBatchName("");
						er.setReported(false);
						er.setException(true);
						er.setMessageBody("AFP檔案小於2K， 轉檔錯誤，請聯絡FXDMS人員到E:/backup/zip/error中查看轉檔記錄");
						er.setTitle("format error");
						((VoService) Constant.getContext().getBean(
								"voServiceProxy")).save(er);
						File errorZipFolder = new File(
								Properties.getBackupFolder(), "ZIP/error");
						if (!errorZipFolder.exists())
							errorZipFolder.mkdirs();
						File zipFile = new File(errorZipFolder, newBatchName
								+ ".zip");
						File[] files = null;
						try {
							if (logFile.exists()) {
								files = new File[2];
								files[0] = afpFile;
								files[1] = logFile;
								ZipUtils.packFile(files, zipFile);
								FileUtils.forceDelete(logFile);
							} else {
								files = new File[1];
								files[0] = afpFile;
								ZipUtils.packFile(files, zipFile);
							}
							FileUtils.forceDelete(afpFile);
						} catch (IOException e) {
							e.printStackTrace();
							logger.error("", e);
						}
						continue;
					}

					if (logFile.exists()) {
						try {
							long fileLen1 = logFile.length();
							Thread.sleep(5000);
							long fileLen2 = logFile.length();
							long beginTime = new Date().getTime();
							while (fileLen1 != fileLen2 || fileLen2 == 0) {
								fileLen1 = logFile.length();
								Thread.sleep(5000);
								fileLen2 = logFile.length();
								long endTime = new Date().getTime();
								// 如果超過5分鐘，就跳出來
								if ((endTime - beginTime) > (1000L * 60 * 5)) {
									ErrorReport er = new ErrorReport();
									er.setErrHappenTime(new Date());
									er.setErrorType("CSV_ERROR");
									er.setOldBatchName(newBatchName);
									er.setReported(false);
									er.setException(true);
									er.setMessageBody("CSV超過五分鐘，產生遲緩:"
											+ logFile.getName());
									er.setTitle("CSV File Error");
									((VoService) Constant.getContext().getBean(
											"voServiceProxy")).save(er);
									break;
								}
							}
							AfpFile afpFileDb = ((VoService) Constant
									.getContext().getBean("voServiceProxy"))
									.getAfp(newBatchName);
							// 如果查不到，有可能是團險測試件
							if (newBatchName.toUpperCase().startsWith("CAAAB")
									|| newBatchName.toUpperCase().startsWith(
											"SGAAB")) {
								// 迴歸的話就是把afp搬去pdf目錄
								File pdfFolder = new File(afpFolder, "pdf");
								FilesUtils.moveFileToDirectory(afpFile,
										pdfFolder, true);
								FilesUtils.moveFileToDirectory(logFile,
										pdfFolder, true);
								continue;
							}

						} catch (Exception e) {
							try {
								ErrorReport eReport = new ErrorReport();
								eReport.setErrHappenTime(new Date());
								eReport.setErrorType("Exception");
								eReport.setOldBatchName(null);
								eReport.setReported(false);
								eReport.setException(true);
								eReport.setMessageBody("處理" + afpFile.getName()
										+ "錯誤，" + e.getMessage());
								eReport.setTitle("異常");
								((VoService) Constant.getContext().getBean(
										"voServiceProxy")).save(eReport);
								logger.error("", e);
								e.printStackTrace();
							} catch (Exception ex) {
								logger.error("", ex);
							}

						} finally {

						}
					}				
		    }
		}
		logger.info("AfpListener stop");

	}


	
}
