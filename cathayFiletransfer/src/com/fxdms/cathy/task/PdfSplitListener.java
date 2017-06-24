package com.fxdms.cathy.task;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FileFilterImpl;
import com.fxdms.util.PdfFileUtil;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.ErrorReport;

public class PdfSplitListener {

	private static Logger logger = Logger.getLogger(PdfSplitListener.class);
	private static boolean running = false;
	private static File pdfFolder = new File(Properties.getAfpPath(), "pdf");
	private static File feedBackFolder = new File(
			Properties.getFeedbackFolder());
	private static File gpFeedBackFolder = new File(
			Properties.getGroupOutFolder());
	
	private static File backupFolder = new File(Properties.getBackupFolder());
	private static File gpBackupFolder = new File(Properties.getGpBackupFolder());
	

	public synchronized static void startToRun() {
		com.salmat.pas.vo.Properties properties = ((VoService) Constant
				.getContext().getBean("voServiceProxy")).getProperties();
		//要FeedbackTimer都處理完，且AFPLister也處理完，才會進行
		if (properties.isProcessDone()) {
			if (!pdfFolder.exists()) {
				pdfFolder.mkdirs();
			}

			if (!feedBackFolder.exists())
				feedBackFolder.mkdirs();			

			if (running)
				return;
			running = true;
			
			File signbackPdfFolder =  null;	   
			File policyPdfFolder = null;
			logger.info("pdf splitter listener start to work");
			Date today = new Date();
			File[] pdfFiles = pdfFolder.listFiles(FileFilterImpl
					.getPdfFileFilter());
			File[] afpFiles = pdfFolder.listFiles(FileFilterImpl.getAfpFileFilter());
			if (pdfFiles != null && pdfFiles.length > 0 
					&& (afpFiles == null || afpFiles.length == 0) ) {
				for (File pdfFile : pdfFiles) {
					boolean receipt = false;
					boolean group = false;
					//SG或GG開頭的是簽收單
					if (pdfFile.getName().startsWith("SG"))
						receipt = true;
					else if(pdfFile.getName().startsWith("GG"))
						receipt = true;
					//GG或GA開頭的是團險
					if(pdfFile.getName().startsWith("GG") || pdfFile.getName().startsWith("GA"))
                        group = true;
					logger.info("processing " + pdfFile.getName());
					String mainNm = pdfFile.getName().substring(0,
							pdfFile.getName().length() - 4); // 截斷.afp的部分
					AfpFile afp = ((VoService) Constant.getContext().getBean(
							"voServiceProxy")).getAfp(mainNm);
					logger.info(pdfFile.getName() + " exist. ready to split");
					PDDocument pdfDoc = null;
					File rasFile = null;
					RandomAccessFile ras = null;
					try {
						rasFile = new File(pdfFolder, UUID.randomUUID() + "");
						ras = new RandomAccessFile(rasFile, "rw");
						pdfDoc = PDDocument.load(pdfFile, ras);
						Set<ApplyData> applyDatas = ((VoService) Constant
								.getContext().getBean("voServiceProxy"))
								.getApplyDataByNewBatchNm(mainNm);
						for (ApplyData applyData : applyDatas) {
							if (applyData.getTotalPage() != null
									&& applyData.getTotalPage() > 0) {

								int startPage = applyData.getAfpBeginPage();
								int endPage = applyData.getAfpEndPage();
								// pdfName規則，保單：保單號碼 + 補印次數
								// 簽收單:保單號碼
								String fileName = "null";
								if (applyData.getPolicyNoSet() != null
										&& applyData.getPolicyNoSet().size() > 0) {
									for (String policyNo : applyData
											.getPolicyNoSet()) {
										// fileName = policyNo;
										// break;
										if ("null".equals(fileName)
												|| fileName == null
												|| fileName.compareTo(policyNo) > 0) {
											fileName = policyNo;
										}
									}
								}
								String pdfName = null;
								if (receipt)
									pdfName = fileName;
								else
									pdfName = fileName
											+ StringUtils
													.leftPad(
															applyData
																	.getReprint()
																	+ "", 2,
															'0');
								logger.info("splitting pdf " + pdfName + ".PDF");
								if (endPage < startPage) {
								} else {
									if(!group){
									   signbackPdfFolder = new File(feedBackFolder.getParent(),
											"SIGNBACK_PDF");
									   policyPdfFolder = new File(feedBackFolder.getParent(), "POLICY_PDF");
									}else{
									   signbackPdfFolder = new File(gpFeedBackFolder,
												"SIGNBACK_PDF");
									   policyPdfFolder = new File(gpFeedBackFolder, "POLICY_PDF");
									}
									File policyPdf = new File(policyPdfFolder,
											pdfName + ".PDF");
									
									File policyFolder = null;
									if (receipt) {
										policyPdf = new File(signbackPdfFolder,
												pdfName + ".PDF");
										if(!group){
										    policyFolder = new File(
												backupFolder,
												"SIGNBACK_PDF/"
														+ Constant.yyyy_MM_dd
																.format(InputdateParser
																		.getInputDate()));
										}else{
											policyFolder = new File(
													gpBackupFolder,
													"SIGNBACK_PDF/"
															+ Constant.yyyy_MM_dd
																	.format(GroupInputdateParser
																			.getGpInputDate()));
										}
									}else{
										if(!group){
										   policyFolder = new File(
												backupFolder,
												"POLICY_PDF/"
														+ Constant.yyyy_MM_dd
																.format(InputdateParser
																		.getInputDate()));
										}else{
											policyFolder = new File(
													gpBackupFolder,
													"POLICY_PDF/"
															+ Constant.yyyy_MM_dd
																	.format(GroupInputdateParser
																			.getGpInputDate()));
										}
									}
									PdfFileUtil
											.splitDocument(
													startPage,
													endPage,
													policyPdf.getAbsolutePath(),
													pdfDoc);
									FileUtils.copyFileToDirectory(policyPdf,
											policyFolder, true);

								}
							}
						}

					} catch (Exception e) {
						logger.error("", e);
					} finally {
						if (ras != null && pdfDoc != null)
							try {
								ras.close();
								pdfDoc.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						ras = null;
						pdfDoc = null;

					}
					try {
						FileUtils.forceDelete(rasFile);
						FileUtils.forceDelete(pdfFile);
					} catch (IOException e) {
						logger.error("", e);
						e.printStackTrace();
					}

				}
			}

			running = false;
			logger.info("pdf splitter this time  stop");
			properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
			properties.setProcessDone(false);
			((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);
		}
	}

}
