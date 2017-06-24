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

public class AfpListener {

	static Logger logger = Logger.getLogger(AfpListener.class);
	static int packFilesNo;
	private static File feedback = new File(Properties.getFeedbackFolder());
	private static boolean running = false;
	private static int errorCounter = 0;

	public static void startToRun() {
		logger.info("AfpListener start to run");
		if (!feedback.exists())
			feedback.mkdirs();
        //這幾個在進行時，本thread不進行
		if (running || GpPdfSorter.isRunning() || FileDispatcher.getRunning() || GpFileDispatcher.isRunning() ) 
			return;
		running = true;

		// 發現afp檔案
		String afpFolder = Properties.getAfpPath();

		File zipFolder = new File(Properties.getZipTmpPath());
		File afpPath = new File(afpFolder);
		File[] afpFiles = null;
		if (afpPath.exists()) {
			afpFiles = afpPath.listFiles(FileFilterImpl.getAfpFileFilter());

		} else {
			logger.error("afpPath exist :" + afpPath.exists());
		}
		if (!zipFolder.exists())
			zipFolder.mkdirs();

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
					((VoService) Constant.getContext()
							.getBean("voServiceProxy")).save(er);
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
						while ( fileLen1 != fileLen2 || fileLen2 == 0) {
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
								er.setMessageBody("CSV超過五分鐘，產生遲緩:" + logFile.getName());
								er.setTitle("CSV File Error");
								((VoService) Constant.getContext().getBean(
										"voServiceProxy")).save(er);
								break;
							}
						}
						AfpFile afpFileDb = ((VoService) Constant.getContext()
								.getBean("voServiceProxy"))
								.getAfp(newBatchName);
						//如果查不到，有可能是團險測試件
						if (afpFileDb == null) {
							// 如果不是團險，也不是測試件，發出通知
							if (!((newBatchName.toUpperCase().startsWith("GA09T") 
									|| newBatchName.toUpperCase().startsWith("GG09T")) 
									&& newBatchName.toLowerCase().endsWith("9990"))) {
								/*
								FilesUtils.moveFileToDirectory(afpFile,
										new File(Properties.getBackupFolder()),
										true);
								FilesUtils.moveFileToDirectory(logFile,
										new File(Properties.getBackupFolder()),
										true);
								*/
								ErrorReport er = new ErrorReport();
								er.setErrHappenTime(new Date());
								er.setErrorType("NOT_NORMAL_FILE");
								er.setOldBatchName(newBatchName);
								er.setReported(false);
								er.setException(true);
								er.setMessageBody("非測試與正常件:"
										+ logFile.getName());
								er.setTitle("not a normal file");
								((VoService) Constant.getContext().getBean(
										"voServiceProxy")).save(er);
								continue;
							} else {
                                //如果是GA09T或GG09T開頭，且9990結尾的，就會進入這段
								List<ApplyData> applyDatas = parseLog(logFile,
										false);
								File pdfFolder = new File(afpFolder, "pdf");
								File tmpFile = new File(pdfFolder,
										UUID.randomUUID() + ".tmp");
								FilesUtils.copyFile(afpFile, tmpFile, true);
								tmpFile.renameTo(new File(pdfFolder, afpFile
										.getName()));
								String pdfFileNm = afpFile.getName().substring(
										0, afpFile.getName().length() - 4)
										+ ".pdf";
								File pdfFile = new File(pdfFolder, pdfFileNm);
								long length1 = 0;
								if (pdfFile.exists())
									length1 = pdfFile.length();
								Thread.sleep(5000);
								long length2 = 0;								
								boolean convertSuccess = true;
								beginTime = new Date().getTime();
								while (!pdfFile.exists() || length1 != length2
										|| length2 == 0) {
									if (pdfFile.exists())
										length1 = pdfFile.length();
									Thread.sleep(10000);
									if (pdfFile.exists())
										length2 = pdfFile.length();
									long endTime = new Date().getTime();
									// 如果超過50分鐘，就跳出來
									if ((endTime - beginTime) > (1000L * 60 * 50)) {
										convertSuccess = false;
										break;
									}
								}
								if (!convertSuccess){
									running = false;
									return;
								}
									
								PDDocument pdfDoc = null;
								File rasFile = null;
								RandomAccessFile ras = null;

								File testFolder = new File(new File(
										Properties.getGroupOutFolder()),
										"PROD_TEST");
								if (!testFolder.exists())
									testFolder.mkdirs();
								try {
									rasFile = new File(pdfFolder,
											UUID.randomUUID() + "");
									ras = new RandomAccessFile(rasFile, "rw");
									pdfDoc = PDDocument.load(pdfFile, ras);
									for (ApplyData applyData : applyDatas) {
										int startPage = applyData
												.getAfpBeginPage();
										int endPage = applyData.getAfpEndPage();
										String pdfName = null;
										if (applyData.getPolicyNoSet() != null
												&& applyData.getPolicyNoSet()
														.size() > 0) {
											for (String policyNo : applyData
													.getPolicyNoSet()) {
												if (pdfName == null
														|| pdfName
																.compareTo(policyNo) > 0) {
													pdfName = policyNo;
												}
											}
										}
										if (pdfName != null
												&& newBatchName.toUpperCase()
														.startsWith("GA")) {
											pdfName += "_"
													+ StringUtils
															.leftPad(
																	applyData
																			.getReprint()
																			+ "",
																	2, '0');
										}
										if (endPage >= startPage) {
											PdfFileUtil.splitDocument(
													startPage, endPage,
													new File(testFolder,
															pdfName + ".pdf")
															.getAbsolutePath(),
													pdfDoc);
										} else {
											ErrorReport er = new ErrorReport();
											er.setErrHappenTime(new Date());
											er.setErrorType("presLogError");
											er.setOldBatchName("");
											er.setReported(false);
											er.setException(true);
											er.setMessageBody("團險測試轉檔錯誤"
													+ logFile.getName());
											er.setTitle("convert error");
											((VoService) Constant.getContext()
													.getBean("voServiceProxy"))
													.save(er);
										}
									}
								} catch (Exception e) {
									logger.error("", e);
								} finally {
									try {
										if (ras != null)
											ras.close();
										if (pdfDoc != null)
											pdfDoc.close();
									} catch (IOException e) {
										logger.error("", e);
										e.printStackTrace();
									}
									ras = null;
									pdfDoc = null;
									if (rasFile != null)
										rasFile.delete();
									if (pdfFile.exists())
										FilesUtils.forceDelete(pdfFile);
								}
								if (logFile.exists())
									FileUtils.forceDelete(logFile);
								if (afpFile.exists())
									FileUtils.forceDelete(afpFile);
								ErrorReport er = new ErrorReport();
								er.setErrHappenTime(new Date());
								er.setErrorType("GROUP_TEST");
								er.setOldBatchName(newBatchName);
								er.setReported(true);
								er.setException(false);
								er.setMessageBody("團險測試件處理完畢");
								er.setTitle("Group Test Finished");
								((VoService) Constant.getContext().getBean(
										"voServiceProxy")).save(er);
							}
						} else if (afpFileDb != null) {
							logger.info("got data from db." + newBatchName);

							List<ApplyData> applyDatas = parseLog(logFile);

							if (applyDatas == null || applyDatas.size() == 0) {
								ErrorReport er = new ErrorReport();
								er.setErrHappenTime(new Date());
								er.setErrorType("presLogError");
								er.setOldBatchName("");
								er.setReported(false);
								er.setException(true);
								er.setMessageBody("解析log檔失敗:"
										+ logFile.getName());
								er.setTitle("format error");
								((VoService) Constant.getContext().getBean(
										"voServiceProxy")).save(er);
								continue;
							}
							int totalPages = 0;
							boolean allSuccess = true;

							for (ApplyData applyData : applyDatas) {
								// 計算總頁數
								if (applyData.getAfpBeginPage() != null
										&& applyData.getAfpEndPage() != null)
									totalPages += (applyData.getAfpEndPage()
											- applyData.getAfpBeginPage() + 1);
								if (!"17".equals(applyData.getPolicyStatus())) {
									allSuccess = false;
								}
							}
							// 如果是test，就把它切開後，放到D:/dataOUT/PROD_TEST
							File pdfFile = new File(new File(afpFolder, "pdf"),
									newBatchName + ".pdf");
							if (!afpFile.getName().toLowerCase()
									.startsWith("ca00t")
									&& !afpFile.getName().toLowerCase()
											.startsWith("sg00t")
									&& !afpFile.getName().toLowerCase()
											.startsWith("ga09t")
									&& !afpFile.getName().toLowerCase()
											.startsWith("gg09t")) {
								// 表示成功解析內容，準備打包
								File[] files = new File[2];
								files[0] = afpFile;
								files[1] = logFile;
								// 把它copy到pdf folder，然後另外的thread會切pdf
								// FilesUtils.copyFileToDirectory(afpFile, new
								// File(afpFolder, "pdf"), true);
								File pdfFolder = new File(afpFolder, "pdf");
								File tmpFile = new File(pdfFolder,
										UUID.randomUUID() + ".tmp");
								FilesUtils.copyFile(afpFile, tmpFile, true);
								tmpFile.renameTo(new File(pdfFolder, afpFile
										.getName()));

								File tmpZipFile = new File(
										Properties.getZipTmpPath(),
										newBatchName + ".tmp");
								File destZipFile = new File(
										Properties.getZipTmpPath(),
										newBatchName + ".zip");
								logger.info("ready to pack:" + newBatchName);
								// 如果存在的話都先刪除
								if (destZipFile.exists())
									FileUtils.forceDelete(destZipFile);
								if (tmpZipFile.exists())
									FileUtils.forceDelete(tmpZipFile);

								ZipUtils.packFile(files, tmpZipFile);
								tmpZipFile.renameTo(destZipFile);
							} else if (afpFile.getName().toLowerCase()
									.startsWith("ca00t")
									|| afpFile.getName().toLowerCase()
											.startsWith("sg00t")) {
								File pdfFolder = new File(afpFolder, "pdf");
								File tmpFile = new File(pdfFolder,
										UUID.randomUUID() + ".tmp");
								FilesUtils.copyFile(afpFile, tmpFile, true);
								tmpFile.renameTo(new File(pdfFolder, afpFile
										.getName()));

								long length1 = 0;
								if (pdfFile.exists())
									length1 = pdfFile.length();
								Thread.sleep(5000);
								long length2 = 0;
								beginTime = new Date().getTime();
								boolean convertSuccess = true;
								while (!pdfFile.exists() || length1 != length2
										|| length2 == 0) {
									if (pdfFile.exists())
										length1 = pdfFile.length();
									Thread.sleep(10000);
									if (pdfFile.exists())
										length2 = pdfFile.length();
									long endTime = new Date().getTime();
									// 如果超過20分鐘，就跳出來
									if ((endTime - beginTime) > (1000L * 60 * 20)) {
										convertSuccess = false;
										break;
									}
								}
								if (!convertSuccess)
									return;
								PDDocument pdfDoc = null;
								File rasFile = null;
								RandomAccessFile ras = null;
								File testFolder = new File(
										"D:/dataOUT/PROD_TEST");
								if (!testFolder.exists())
									testFolder.mkdirs();
								try {
									rasFile = new File(pdfFolder,
											UUID.randomUUID() + "");
									ras = new RandomAccessFile(rasFile, "rw");
									pdfDoc = PDDocument.load(pdfFile, ras);
									for (ApplyData applyData : applyDatas) {
										int startPage = applyData
												.getAfpBeginPage();
										int endPage = applyData.getAfpEndPage();
										String pdfName = null;
										if (applyData.getPolicyNoSet() != null
												&& applyData.getPolicyNoSet()
														.size() > 0) {
											for (String policyNo : applyData
													.getPolicyNoSet()) {
												if (pdfName == null
														|| pdfName
																.compareTo(policyNo) > 0) {
													pdfName = policyNo;
												}
											}
										}
										if (pdfName != null) {
											pdfName += StringUtils
													.leftPad(
															applyData
																	.getReprint()
																	+ "", 2,
															'0');
											if (applyData.getReceipt() != null
													&& applyData.getReceipt())
												pdfName += "_SIGN";
										} else {
											pdfName = applyData.getPolicyPDF();
										}
										PdfFileUtil.splitDocument(startPage,
												endPage, new File(testFolder,
														pdfName + ".pdf")
														.getAbsolutePath(),
												pdfDoc);
									}
								} catch (Exception e) {
									logger.error("", e);
								} finally {
									try {
										if (ras != null)
											ras.close();
										if (pdfDoc != null)
											pdfDoc.close();
									} catch (IOException e) {
										logger.error("", e);
										e.printStackTrace();
									}
									ras = null;
									pdfDoc = null;
									if (rasFile != null)
										rasFile.delete();
									if (pdfFile.exists())
										FilesUtils.forceDelete(pdfFile);
								}

							}

							afpFileDb.setAfpFileNm(afpFileNm);
							afpFileDb.setPages(totalPages);
							afpFileDb.setFileDate(new Date(afpFile
									.lastModified()));
							afpFileDb.setUpdateDate(new Date());
							afpFileDb.setPresTime(new Date());
							afpFileDb.setZiped(true);
							if (allSuccess)
								afpFileDb.setStatus("轉檔成功");
							else
								afpFileDb.setStatus("部分失敗，請查閱詳情");

							afpFileDb.setUpdateDate(new Date());
							((VoService) Constant.getContext().getBean(
									"voServiceProxy")).update(afpFileDb);

							// FilesUtils.moveFileToDirectory(logFile, new
							// File(Properties.getBackupFolder(),
							// Constant.yyyyMMdd.format(new Date())), true);
							// 成功後刪除，不用備份，備份zip檔就好
							File pdfFolder = new File(afpFile.getParent(),
									"pdf");
							String tmpName = UUID.randomUUID() + ".tmp";
							File afpTmp = new File(pdfFolder, tmpName);
							File movedAfp = new File(pdfFolder,
									afpFile.getName());
							// 如果不是測試件，就移過去pdf folder
							if (!afpFile.getName().toLowerCase()
									.startsWith("ca00t")
									&& !afpFile.getName().toLowerCase()
											.startsWith("sg00t")) {
								FilesUtils.copyFile(afpFile, afpTmp, true);
								afpTmp.renameTo(movedAfp);
							}
							if (afpFile.exists())
								FileUtils.forceDelete(afpFile);
							if (logFile.exists())
								FileUtils.forceDelete(logFile);

							ErrorReport eReport = new ErrorReport();
							eReport.setErrHappenTime(new Date());
							eReport.setErrorType("AfpProcess");
							eReport.setOldBatchName(null);
							eReport.setReported(true);
							eReport.setMessageBody("壓縮" + afpFile.getName()
									+ "完成，準備回傳");
							eReport.setTitle("準備回傳Afp");
							((VoService) Constant.getContext().getBean(
									"voServiceProxy")).save(eReport);
                            File[] datas = new File(Properties.getPresPath())
							           .listFiles(FileFilterImpl.getFileFilter());
                            File[] afps = afpPath.listFiles(FileFilterImpl
									.getAfpFileFilter());
                            boolean datasEmpty = true;
                            boolean afpsEmpty = true;
                            if (datas == null || datas.length == 0){
                            	
                            }else{
                            	for(File data : datas)
                            		if(!data.getName().toUpperCase().startsWith("CAAA") && !data.getName().toUpperCase().startsWith("SGAA")){
                            			datasEmpty = false;
                            		}
                            }
                            if (afps == null || afps.length == 0){
                            	
                            }else{
                            	for(File afp : afps)
                            		if(!afp.getName().toUpperCase().startsWith("CAAA") && !afp.getName().toUpperCase().startsWith("SGAA")){
                            			afpsEmpty = false;
                            		}
                            }
							// 如果data都空了，代表轉檔結束
							if (datasEmpty && afpsEmpty) {
								Date cycleDate = InputdateParser.getInputDate();
								if(cycleDate == null || (GroupInputdateParser.getGpInputDate() != null && GroupInputdateParser.getGpInputDate().compareTo(cycleDate) > 0)){
									cycleDate = GroupInputdateParser.getGpInputDate();									
								}
								List<AfpFile> allAfps = null;
								if(cycleDate != null)
								   allAfps = ((VoService) Constant.getContext().getBean("voServiceProxy")).getCycleDateAfpfiles(cycleDate);
								boolean allProcessed = true;
								if(allAfps != null && allAfps.size() > 0){
									for(AfpFile afp : allAfps){
										if("轉檔中".equals(afp.getStatus()) ){
											allProcessed = false;
										}
									}
								}else{
									allProcessed = false;
								}
								if(allProcessed){
								   ErrorReport er = new ErrorReport();
								   er.setErrHappenTime(new Date());
								   er.setErrorType("EndProcess");
								   er.setOldBatchName("");
								   er.setReported(false);
								   if (errorCounter > 0)
									   er.setMessageBody("轉檔錯誤：" + errorCounter + "件");
								   else
									   er.setMessageBody(null);
								   er.setTitle("End Process");
								   ((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);								   								   
								
								   if (!afpFile.getName().toLowerCase()
										   .startsWith("ca00t")
										   && !afpFile.getName().toLowerCase()
												   .startsWith("sg00t")) {
									   com.salmat.pas.vo.Properties properties = ((VoService) Constant
											   .getContext().getBean("voServiceProxy")).getProperties();
									   properties.setProcessDone(true);
									   ((VoService) Constant.getContext().getBean(
											   "voServiceProxy")).update(properties);
								   }
								   errorCounter = 0;
								   ((VoService) Constant.getContext().getBean("voServiceProxy")).commonReport(); //寄出轉檔通知信
								}								
							}
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
		running = false;
	}

	// parse csv檔
	private static List<ApplyData> parseLog(File logFile)
			throws BeansException, RemoteException {

		Date today = new Date();
		String mainName = logFile.getName().substring(0, 17).toUpperCase();
		String conjugateName = mainName.substring(2);
		if (mainName.startsWith("CA"))
			conjugateName = "SG" + conjugateName;
		else if (mainName.startsWith("SG"))
			conjugateName = "CA" + conjugateName;
		else if (mainName.startsWith("GA"))
			conjugateName = "GG" + conjugateName;
		else if (mainName.startsWith("GG"))
			conjugateName = "GA" + conjugateName;
		Set<ApplyData> applySet = ((VoService) Constant.getContext().getBean(
				"voServiceProxy")).getApplyDataByNewBatchNm(mainName);
		Set<ApplyData> conjugetSet = ((VoService) Constant.getContext()
				.getBean("voServiceProxy"))
				.getApplyDataByNewBatchNm(conjugateName);
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		List<ApplyData> applyDatas = new ArrayList<ApplyData>();
		try {
			fis = new FileInputStream(logFile);
			isr = new InputStreamReader(fis, "ms950");
			br = new BufferedReader(isr);
			String line = null;
			int lineCounter = 0;
			int applyDataCounter = 0;
			while ((line = br.readLine()) != null) {
				if (lineCounter == 0 || line.indexOf("處理日") >= 0) {
					// 第一行不用讀
					lineCounter++;
					continue;
				}
				lineCounter++;
				applyDataCounter++;
				line = org.apache.commons.lang.StringUtils.trimToEmpty(line);
				String[] lineSplit = line.split(",");
				if ("".equals(line) || (lineSplit.length < 25)) {
					if(line.equals("")){
						continue;
					}else{
					// 格式錯誤，送出錯誤訊息
					   ErrorReport er = new ErrorReport();
					   er.setErrHappenTime(new Date());
					   er.setErrorType("presLogError");
					   er.setOldBatchName("");
					   er.setReported(false);
					   er.setMessageBody("log file:" + logFile.getName()
							   + " format error on parsing " + line);
					   er.setTitle("format error");
					   ((VoService) Constant.getContext()
							   .getBean("voServiceProxy")).save(er);
					   continue;
					}
				} else {
					// 處理日,行政中心別,保單號碼 ,補印次數,受理編號
					// ,明細類別,要保書影像處理狀態,文字檔處理狀態,合併處理狀態,掃描批次號碼,保單列印檔處理狀態,簽收回條處理狀態,轉檔日,保單頁數,轉檔失敗訊息,列印失敗訊息,封面頁數,A4頁數,DM頁數,封底頁數,錯誤代碼,AFP中起始頁數,AFP終止頁數,PDF檔名,address,
					// uniqueNo
					// 處理日,行政中心別,保單號碼,補印次數,受理編號,明細類別,要保書影像處理狀態,文字檔處理狀態,合併處理狀態,掃描批次號碼,保單列印檔處理狀態,簽收回條處理狀態,轉檔日,保單頁數,轉檔失敗訊息,列印失敗訊息,封面頁數,A4頁數,DM頁數,封底頁數,錯誤代碼,AFP中起始頁數,AFP終止頁數,PDF檔名,要保人地址,
					// 0 ,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8 ,9 ,10 ,11 ,12 ,13 ,14 ,15 , 16
					// , 17 , 18 ,19 ,20 ,21 , 22 ,23 , 24
					// 2014-05, 01, 075198, 00,5498943,NORM , , , , , ,
					// ,-05-16,1 ,
					// ,,0,1,0,0,,1,1,01_2014-05-16_MC15498943_9097075198,台北市士林區重慶北路四段４９巷１５弄２２號３樓,
					Date printTime = Constant.yyyy_MM_dd.parse(lineSplit[0]);
					String center = lineSplit[1].trim();
					String policyNo = lineSplit[2].trim();
					Integer reprint = lineSplit[3].trim().equals("") ? 0
							: new Integer(lineSplit[3].trim());
					String applyNo = lineSplit[4];
					Integer totalPage = lineSplit[13].trim().equals("") ? null
							: new Integer(lineSplit[13].trim());

					Integer firstPage = lineSplit[16].trim().equals("") ? null
							: new Integer(lineSplit[16].trim());
					Integer a4Page = lineSplit[17].trim().equals("") ? null
							: new Integer(lineSplit[17].trim());
					Integer dmPage = lineSplit[18].trim().equals("") ? null
							: new Integer(lineSplit[18].trim());
					Integer lastPage = lineSplit[19].trim().equals("") ? null
							: new Integer(lineSplit[19].trim());
					String policyStatus = lineSplit[20].trim();
					Integer afpBeginPage = lineSplit[21].trim().equals("") ? null
							: new Integer(lineSplit[21].trim());
					Integer afpEndPage = lineSplit[22].trim().equals("") ? null
							: new Integer(lineSplit[22].trim());
					String pdfName = lineSplit[23].trim();
					String address = null;
					String uniqueNo = null;
					if (lineSplit.length >= 25) {
						address = lineSplit[24].trim();
					}
					if (lineSplit.length >= 26) {
						uniqueNo = lineSplit[25];
						//如果是CA或GA時，pdf名改為uniqueNo
						if(mainName.toUpperCase().startsWith("CA") || mainName.toUpperCase().startsWith("GA")){
							pdfName = uniqueNo;
						}
					}

					/*
					 * List<ApplyData> list = session .getNamedQuery(
					 * "ApplyData.findByApplyNoAndPolicyNoAndCenter")
					 * .setString(0, applyNo) .setString(1, "%," + policyNo +
					 * ",%") .setString(2, center).list();
					 */
					ApplyData applyData = null;
					if (applySet != null && applySet.size() > 0) {
						for (ApplyData ad : applySet) {
							if (ad.getPolicyNos().indexOf("," + policyNo + ",") >= 0
									&& ad.getReprint() != null
									&& ad.getReprint().intValue() == reprint
											.intValue() 
									&& (ad.getUniqueNo() == null || "".equals(ad.getUniqueNo().trim()) || ad.getUniqueNo().equals(uniqueNo))) {
								logger.info(mainName + ".cvs line" + lineCounter + "|" +  " matched to " + ad.getPolicyNos());
								applyData = ad;
								break;
							}
						}
					}
					if (applyData == null) {
						// 找不到data，格式錯誤，送出錯誤訊息
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("presLogError");
						er.setOldBatchName("");
						er.setReported(false);
						er.setException(true);
						er.setMessageBody("無法由 '" + logFile.getName() + "的"
								+ line + "'找到唯一值");
						er.setTitle("error");
						((VoService) Constant.getContext().getBean(
								"voServiceProxy")).save(er);
						logger.error("parse log file error:" + line);
						continue;
					}
					applyData.setPresTime(today);
					applyData.setUpdateDate(today);
					applyData.setMegerOK(applyData.getMerger());
					applyData.setTotalPage(totalPage);
					if (!policyStatus.equals("0")
							&& (policyStatus.equals("14") || policyStatus
									.equals("16"))) {
						if(!mainName.toUpperCase().startsWith("CAAAB") && !mainName.endsWith("9990"))
						   errorCounter++;
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("presConvertErr");
						er.setOldBatchName(applyData.getOldBatchName());
						er.setReported(false);
						er.setMessageBody("log file:" + logFile.getName()
								+ " format error on parsing " + line);
						er.setTitle("format error");
						// 把applyData從列印檔中移除
						((VoService) Constant.getContext().getBean(
								"voServiceProxy")).save(er);

						applyData.setPolicyStatus("16");
						applyData.setTotalPage(0);
						// 把applyData從列印檔中移除
						applyData.setNewBatchName(null);
						applyDataCounter--;
						// 不是簽收回條時要寫入回饋檔
						if ((applyData.getReceipt() == null || !applyData
								.getReceipt())
								&& !applyData.getOldBatchName().toUpperCase()
										.startsWith("GG")
								&& !applyData.getOldBatchName().toUpperCase()
										.startsWith("GA")) {
							FileOutputStream fos692 = null;
							OutputStreamWriter osw692 = null;
							BufferedWriter bw692 = null;
							File dtata692 = new File(new File(
									Properties.getBackupFolder(), "DTATA692"),
									"DTATA692_FXDMS");
							try {
								fos692 = new FileOutputStream(dtata692, true);
								osw692 = new OutputStreamWriter(fos692, "ms950");
								bw692 = new BufferedWriter(osw692);
								Set<String> policyNoset = applyData
										.getPolicyNoSet();

								if (policyNoset != null) {
									for (String poNo : policyNoset) {
										policyNo = poNo;
										String cycleStr = applyData
												.getCycleDate() == null ? ""
												: Constant.yyyyMMdd
														.format(InputdateParser
																.getInputDate());
										String presTime = applyData
												.getPresTime() == null ? ""
												: Constant.yyyyMMdd
														.format(applyData
																.getPresTime());
										String writeLine = cycleStr
												+ ","
												+ center
												+ ","
												+ policyNo
												+ ","
												+ StringUtils.leftPad(
														applyData.getReprint()
																+ "", 2, '0')
												+ ","
												+ applyData.getApplyNo()
												+ ","
												+ applyData.getSourceCode()
												+ ",N,Y,Y,"
												+ applyData.getOldBatchNo()
												+ ",N,"
												+ (applyData.getReceipt() ? "Y"
														: "N")
												+ ",,"
												+ (applyData.getTotalPage())
												+ ","
												+ applyData.getAreaId()
												+ ","
												+ (applyData.getMerger() ? "1"
														: "0") + ","
												+ "FXDMS,P\r\n";
										bw692.write(writeLine);
									}
								}

								bw692.flush();
								osw692.flush();
								fos692.flush();
							} catch (Exception e) {
								e.printStackTrace();
								logger.error("", e);
							} finally {
								if (bw692 != null) {
									try {
										bw692.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									bw692 = null;
								}
								if (osw692 != null) {
									try {
										osw692.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									osw692 = null;
								}
								if (fos692 != null) {
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
					} else if (!policyStatus.equals("0")) {
						if(!mainName.toUpperCase().startsWith("CAAAB") && !mainName.endsWith("9990"))
						   errorCounter++;
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("presConvertErr");
						er.setOldBatchName(applyData.getOldBatchName());
						er.setReported(false);
						er.setMessageBody("log file:" + logFile.getName()
								+ " format error on parsing " + line);
						er.setTitle("format error");
						// 把applyData從列印檔中移除
						((VoService) Constant.getContext().getBean(
								"voServiceProxy")).save(er);

						applyData.setPolicyStatus("16");
						applyData.setTotalPage(0);
						// 把applyData從列印檔中移除
						applyData.setNewBatchName(null);
						applyDataCounter--;
						if ((applyData.getReceipt() == null || !applyData
								.getReceipt())
								&& !applyData.getOldBatchName().toUpperCase()
										.startsWith("GG")
								&& !applyData.getOldBatchName().toUpperCase()
										.startsWith("GA")) {
							FileOutputStream fos692 = null;
							OutputStreamWriter osw692 = null;
							BufferedWriter bw692 = null;
							File dtata692 = new File(new File(
									Properties.getBackupFolder(), "DTATA692"),
									"DTATA692_FXDMS");
							try {
								fos692 = new FileOutputStream(dtata692, true);
								osw692 = new OutputStreamWriter(fos692, "ms950");
								bw692 = new BufferedWriter(osw692);
								Set<String> policyNoset = applyData
										.getPolicyNoSet();

								if (policyNoset != null) {
									for (String poNo : policyNoset) {
										policyNo = poNo;
										String cycleStr = applyData
												.getCycleDate() == null ? ""
												: Constant.yyyyMMdd
														.format(InputdateParser
																.getInputDate());
										String presTime = applyData
												.getPresTime() == null ? ""
												: Constant.yyyyMMdd
														.format(applyData
																.getPresTime());
										String writeLine = cycleStr
												+ ","
												+ center
												+ ","
												+ policyNo
												+ ","
												+ StringUtils.leftPad(
														applyData.getReprint()
																+ "", 2, '0')
												+ ","
												+ applyData.getApplyNo()
												+ ","
												+ applyData.getSourceCode()
												+ ",N,Y,Y,"
												+ applyData.getOldBatchNo()
												+ ",N,"
												+ (applyData.getReceipt() ? "Y"
														: "N")
												+ ",,"
												+ (applyData.getTotalPage())
												+ ","
												+ applyData.getAreaId()
												+ ","
												+ (applyData.getMerger() ? "1"
														: "0") + ","
												+ "FXDMS,P\r\n";
										bw692.write(writeLine);
									}
								}

								bw692.flush();
								osw692.flush();
								fos692.flush();
							} catch (Exception e) {
								e.printStackTrace();
								logger.error("", e);
							} finally {
								if (bw692 != null) {
									try {
										bw692.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									bw692 = null;
								}
								if (osw692 != null) {
									try {
										osw692.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									osw692 = null;
								}
								if (fos692 != null) {
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

					} else {
						if(applyData.getPolicyStatus() == null 
								|| (!"100".equals(applyData.getPolicyStatus()) 
										&& applyData.getPolicyStatus().compareTo("17") <= 0))
						   applyData.setPolicyStatus("17");
					}
					applyData.setFirstPage(firstPage);
					applyData.setA4Page(a4Page);
					applyData.setDmPage(dmPage);
					applyData.setReprint(reprint);
					applyData.setLastPage(lastPage);
					applyData.setAfpBeginPage(afpBeginPage);
					applyData.setAfpEndPage(afpEndPage);
					// applyData.setAddress(address);
					ApplyData conjugateAd = null;
					for (ApplyData ap : conjugetSet) {
						for (String conjugateNo : ap.getPolicyNoSet()) {
							// 保單號碼相同，且列印次數相同
							if (applyData.getPolicyNoSet()
									.contains(conjugateNo)
									&& applyData.getReprint() != null
									&& ap.getReprint() != null
									&& applyData.getReprint().intValue() == ap
											.getReprint().intValue()) {
								conjugateAd = ap;
								break;
							}
						}
						if (conjugateAd != null)
							break;
					}

					// 如果是北二直效通路，先幫op檢查寄件地址，簽收單有地址，看看是不是相同
					if (logFile.getName().toUpperCase().startsWith("SG06")) {

						// 如果是channelId是A或B時，先幫op檢查地址
						// 沒有用的功能，因為channel A的簽收單不轉檔
						/*
						 * if("A".equals(applyData.getChannelID().toUpperCase())
						 * ||
						 * "B".equals(applyData.getChannelID().toUpperCase())){
						 * //如果是正常讀取到地址，而且地址和簽收單裡的是一樣的，就設為true
						 * //這裡仍然有個漏洞，就是如果寄送地址打成戶籍地址，因為簽收單是戶籍地址，仍然會認不出來
						 * if(applyData.getParseNorm() != null &&
						 * applyData.getParseNorm() && applyData.getAddress() !=
						 * null && address != null &&
						 * applyData.getAddress().indexOf(address) >= 0){
						 * applyData.setAddressEq(true); }else{
						 * applyData.setAddressEq(false); } }else{
						 * applyData.setAddressEq(true); } if(conjugateAd !=
						 * null){
						 * conjugateAd.setAddressEq(applyData.getAddressEq());
						 * ((VoService)
						 * Constant.getContext().getBean("voServiceProxy"
						 * )).update(conjugateAd); }
						 */
					} else {
						applyData.setAddressEq(true);
					}
					applyData.setPolicyPDF(pdfName);
					applyData.setUniqueNo(uniqueNo);
					// 如果是保單就直接設csv回的名稱，不然就是設成保單號碼
					/*
					 * if(applyData.getReceipt() != null &&
					 * !applyData.getReceipt()){
					 * applyData.setUniqueNo(uniqueNo); }else
					 * if(applyData.getReceipt() != null &&
					 * applyData.getReceipt() && applyData.getPolicyNos() !=
					 * null ){ String policyNos = applyData.getPolicyNos();
					 * if(policyNos != null && policyNos.startsWith(",") ){
					 * policyNos = policyNos.substring(1); } if(policyNos !=
					 * null && policyNos.endsWith(",") ){ policyNos =
					 * policyNos.substring(0, policyNos.length() - 1); }
					 * if(policyNos.length() > 50){ policyNos =
					 * policyNos.substring(0, 50); }
					 * applyData.setUniqueNo(policyNos); }
					 */
					if ((applyData.getPolicyStatus() != null 
							&& (applyData.getPolicyStatus().equals("100") || applyData.getPolicyStatus().compareTo("17") >= 0))
							&& (afpEndPage == null
									|| afpBeginPage == null
									|| (afpEndPage - afpBeginPage + 1) != totalPage || afpEndPage < afpBeginPage)) {
						// 格式錯誤，送出錯誤訊息
						//未發生過的狀況：1.前頁比後頁大  2.總頁數不等於後頁減前頁+1
						//這是預防發生所做的額外判斷
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("presLogError");
						er.setOldBatchName(applyData.getOldBatchName());
						er.setReported(false);
						er.setMessageBody("log file:" + logFile.getName()
								+ " format error on parsing " + line);
						er.setTitle("format error");
						// 把applyData從列印檔中移除
						applyData.setNewBatchName(null);
						applyDataCounter--;
						((VoService) Constant.getContext().getBean(
								"voServiceProxy")).save(er);
						applyData.setTotalPage(0);
						applyData.setPolicyStatus("16");
					}
					((VoService) Constant.getContext()
							.getBean("voServiceProxy")).update(applyData);
					if (applyData.getPolicyStatus() != null 
							&& (applyData.getPolicyStatus().equals("100") || applyData.getPolicyStatus().compareTo("17") >= 0)) {
						applyDatas.add(applyData);
					} else {
						// 轉檔錯誤時，同步把共軛保單或簽收單設定為錯誤
						if (conjugateAd != null) {
							conjugateAd.setExceptionStatus("16");
							// conjugateAd.setPolicyStatus("41");;
							String result = "簽收回條轉檔錯誤，保單設為驗單失敗";
							if(conjugateAd.getNewBatchName() != null 
									&& (conjugateAd.getNewBatchName().toUpperCase().startsWith("SG") || conjugateAd.getNewBatchName().toUpperCase().startsWith("GG")))
								result = "保單轉檔錯誤，簽收回條單設為驗單失敗";
							conjugateAd.setVerifyResult(result);
							conjugateAd.setVerifyTime(today);
							conjugateAd.setUpdateDate(today);
							((VoService) Constant.getContext().getBean(
									"voServiceProxy")).update(applyData);
						}
					}
				}
			}
			
			applySet = ((VoService) Constant.getContext().getBean(
					"voServiceProxy")).getApplyDataByNewBatchNm(mainName);			
			
			return applyDatas;
			
		} catch (Exception e) {
			logger.error("", e);
			ErrorReport er = new ErrorReport();
			er.setErrHappenTime(new Date());
			er.setErrorType("exception");
			er.setOldBatchName("");
			er.setReported(false);
			er.setException(true);
			er.setMessageBody("exception happen:" + e.getMessage());
			er.setTitle("exception happened");
			((VoService) Constant.getContext().getBean("voServiceProxy"))
					.save(er);
			return null;

		} finally {
			try {
				if (br != null)
					br.close();
				if (isr != null)
					isr.close();
				if (fis != null)
					fis.close();
			} catch (Exception e) {

			}
			br = null;
			isr = null;
			fis = null;
		}
	}
	
	// parse csv檔
	private static List<ApplyData> parseLog(File logFile, boolean readDb)
			throws BeansException, RemoteException {
        if(readDb)
        	return parseLog(logFile);
		Date today = new Date();
		String mainName = logFile.getName().substring(0, 17).toUpperCase();
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		List<ApplyData> applyDatas = new ArrayList<ApplyData>();
		try {
			fis = new FileInputStream(logFile);
			isr = new InputStreamReader(fis, "ms950");
			br = new BufferedReader(isr);
			String line = null;
			int lineCounter = 0;
			while ((line = br.readLine()) != null) {
				if (lineCounter == 0 || line.indexOf("處理日") >= 0) {
					// 第一行不用讀
					lineCounter++;
					continue;
				}
				lineCounter++;
				line = org.apache.commons.lang.StringUtils.trimToEmpty(line);
				String[] lineSplit = line.split(",");
				if ("".equals(line) || (lineSplit.length < 25)) {
					if(line.equals("")){
						continue;
					}else{
					// 格式錯誤，送出錯誤訊息
					   ErrorReport er = new ErrorReport();
					   er.setErrHappenTime(new Date());
					   er.setErrorType("presLogError");
					   er.setOldBatchName("");
					   er.setReported(false);
					   er.setMessageBody("log file:" + logFile.getName()
							   + " format error on parsing " + line);
					   er.setTitle("format error");
					   ((VoService) Constant.getContext()
							   .getBean("voServiceProxy")).save(er);
					   continue;
					}
				} else {
					// 處理日,行政中心別,保單號碼 ,補印次數,受理編號
					// ,明細類別,要保書影像處理狀態,文字檔處理狀態,合併處理狀態,掃描批次號碼,保單列印檔處理狀態,簽收回條處理狀態,轉檔日,保單頁數,轉檔失敗訊息,列印失敗訊息,封面頁數,A4頁數,DM頁數,封底頁數,錯誤代碼,AFP中起始頁數,AFP終止頁數,PDF檔名,address,
					// uniqueNo
					// 處理日,行政中心別,保單號碼,補印次數,受理編號,明細類別,要保書影像處理狀態,文字檔處理狀態,合併處理狀態,掃描批次號碼,保單列印檔處理狀態,簽收回條處理狀態,轉檔日,保單頁數,轉檔失敗訊息,列印失敗訊息,封面頁數,A4頁數,DM頁數,封底頁數,錯誤代碼,AFP中起始頁數,AFP終止頁數,PDF檔名,要保人地址,
					// 0 ,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8 ,9 ,10 ,11 ,12 ,13 ,14 ,15 , 16
					// , 17 , 18 ,19 ,20 ,21 , 22 ,23 , 24
					// 2014-05, 01, 075198, 00,5498943,NORM , , , , , ,
					// ,-05-16,1 ,
					// ,,0,1,0,0,,1,1,01_2014-05-16_MC15498943_9097075198,台北市士林區重慶北路四段４９巷１５弄２２號３樓,
					Date printTime = Constant.yyyy_MM_dd.parse(lineSplit[0]);
					String center = lineSplit[1].trim();
					String policyNo = lineSplit[2].trim();
					Integer reprint = lineSplit[3].trim().equals("") ? 0
							: new Integer(lineSplit[3].trim());
					String applyNo = lineSplit[4];
					Integer totalPage = lineSplit[13].trim().equals("") ? null
							: new Integer(lineSplit[13].trim());

					Integer firstPage = lineSplit[16].trim().equals("") ? null
							: new Integer(lineSplit[16].trim());
					Integer a4Page = lineSplit[17].trim().equals("") ? null
							: new Integer(lineSplit[17].trim());
					Integer dmPage = lineSplit[18].trim().equals("") ? null
							: new Integer(lineSplit[18].trim());
					Integer lastPage = lineSplit[19].trim().equals("") ? null
							: new Integer(lineSplit[19].trim());
					String policyStatus = lineSplit[20].trim();
					Integer afpBeginPage = lineSplit[21].trim().equals("") ? null
							: new Integer(lineSplit[21].trim());
					Integer afpEndPage = lineSplit[22].trim().equals("") ? null
							: new Integer(lineSplit[22].trim());
					String pdfName = lineSplit[23].trim();
					String address = null;
					String uniqueNo = null;
					if (lineSplit.length >= 25) {
						address = lineSplit[24].trim();
					}
					if (lineSplit.length >= 26) {
						uniqueNo = lineSplit[25];
						//如果是CA或GA時，pdf名改為uniqueNo
						if(mainName.toUpperCase().startsWith("CA") || mainName.toUpperCase().startsWith("GA")){
							pdfName = uniqueNo;
						}
					}

					/*
					 * List<ApplyData> list = session .getNamedQuery(
					 * "ApplyData.findByApplyNoAndPolicyNoAndCenter")
					 * .setString(0, applyNo) .setString(1, "%," + policyNo +
					 * ",%") .setString(2, center).list();
					 */
					ApplyData applyData = new ApplyData();
					applyData.setPolicyNos("," + policyNo + ",");
					applyData.setPresTime(today);
					applyData.setUpdateDate(today);
					applyData.setMegerOK(applyData.getMerger());
					applyData.setTotalPage(totalPage);
					if (!policyStatus.equals("0")) {
						if(!mainName.toUpperCase().startsWith("CAAAB") && !mainName.endsWith("9990"))
						   errorCounter++;
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("presConvertErr");
						er.setOldBatchName(applyData.getOldBatchName());
						er.setReported(false);
						er.setMessageBody("log file:" + logFile.getName()
								+ " format error on parsing " + line);
						er.setTitle("format error");

						applyData.setPolicyStatus("16");
						applyData.setTotalPage(0);
						// 把applyData從列印檔中移除
						applyData.setNewBatchName(null);						
					} else {
						applyData.setPolicyStatus("17");
					}
					applyData.setFirstPage(firstPage);
					applyData.setA4Page(a4Page);
					applyData.setDmPage(dmPage);
					applyData.setReprint(reprint);
					applyData.setLastPage(lastPage);
					applyData.setAfpBeginPage(afpBeginPage);
					applyData.setAfpEndPage(afpEndPage);
					// applyData.setAddress(address);
					applyData.setPolicyPDF(pdfName);
					applyData.setUniqueNo(uniqueNo);
					if (applyData.getPolicyStatus().equals("17")
							&& (afpEndPage == null
									|| afpBeginPage == null
									|| (afpEndPage - afpBeginPage + 1) != totalPage || afpEndPage < afpBeginPage)) {
						// 格式錯誤，送出錯誤訊息
						ErrorReport er = new ErrorReport();
						er.setErrHappenTime(new Date());
						er.setErrorType("presLogError");
						er.setOldBatchName(applyData.getOldBatchName());
						er.setReported(false);
						er.setMessageBody("log file:" + logFile.getName()
								+ " format error on parsing " + line);
						er.setTitle("format error");
						// 把applyData從列印檔中移除
						applyData.setNewBatchName(null);
						((VoService) Constant.getContext().getBean(
								"voServiceProxy")).save(er);
						applyData.setTotalPage(0);
						applyData.setPolicyStatus("16");
					}
					applyDatas.add(applyData);

				}
			}
			return applyDatas;
		} catch (Exception e) {
			logger.error("", e);
			ErrorReport er = new ErrorReport();
			er.setErrHappenTime(new Date());
			er.setErrorType("exception");
			er.setOldBatchName("");
			er.setReported(false);
			er.setException(true);
			er.setMessageBody("exception happen:" + e.getMessage());
			er.setTitle("exception happened");
			((VoService) Constant.getContext().getBean("voServiceProxy"))
					.save(er);
			return null;

		} finally {
			try {
				if (br != null)
					br.close();
				if (isr != null)
					isr.close();
				if (fis != null)
					fis.close();
			} catch (Exception e) {

			}
			br = null;
			isr = null;
			fis = null;
		}
	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		AfpListener.running = running;
	}

	
}
