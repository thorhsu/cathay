package com.fxdms.cathy.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
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
import org.springframework.beans.BeansException;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FilesUtils;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;

public class FeedbackTimer {

	static Logger logger = Logger.getLogger(FeedbackTimer.class);
	private static boolean running = false;
	private static File folder692 = new File(Properties.getBackupFolder(),
			"DTATA692");
	private static File folder689 = new File(Properties.getBackupFolder(),
			"DTATA689");

	public synchronized static void startToRun() throws BeansException,
			RemoteException {
		com.salmat.pas.vo.Properties properties = ((VoService) Constant
				.getContext().getBean("voServiceProxy")).getProperties();
		if (properties.isProcessDone()) {
			Date today = new Date();
			String yyyyMMddHHmmSS = (new SimpleDateFormat("yyyyMMdd-HHmmSS"))
					.format(today);
			int[] reptAcct = new int[6];
			int[] normAcct = new int[6];
			if (running)
				return;
			running = true;
			if (!folder692.exists())
				folder692.mkdirs();
			if (!folder689.exists())
				folder689.mkdirs();
			File dtata692 = new File(folder692, "DTATA692_FXDMS");
			File dtata689 = new File(folder689, "DTATA689_FXDMS");
			//File dtata692Test = new File(Properties.getFeedbackFolder(), "DTATA692_FXDMS_TEST.txt");
			HashMap<String, List<ApplyData>> sourceMap = new HashMap<String, List<ApplyData>>();

			List<AfpFile> afpFiles = null;
			Set<String> newBatchNames = new HashSet<String>();

			afpFiles = ((VoService) Constant.getContext().getBean(
					"voServiceProxy")).findNotFeedBack();

			if (afpFiles != null && afpFiles.size() > 0) {
				for (AfpFile afpFile : afpFiles) {
					if (afpFile.getNewBatchName().toUpperCase()
							.startsWith("CA")) {

						Date cycleDate = afpFile.getCycleDate();
						if (cycleDate != null) {
							newBatchNames.add(afpFile.getNewBatchName());
							Set<ApplyData> applyDatas = ((VoService) Constant
									.getContext().getBean("voServiceProxy"))
									.getApplyDataNewBatchNm(afpFile
											.getNewBatchName());
							// 先寫692
							FileOutputStream fos692 = null;
							OutputStreamWriter osw692 = null;
							BufferedWriter bw692 = null;
							/*
							FileOutputStream fos692Test = null;
							OutputStreamWriter osw692Test = null;
							BufferedWriter bw692Test = null;
							*/
							try {
								fos692 = new FileOutputStream(dtata692, true);
								osw692 = new OutputStreamWriter(fos692, "ms950");
								bw692 = new BufferedWriter(osw692);
								/*
								fos692Test = new FileOutputStream(dtata692Test, true);
								osw692Test = new OutputStreamWriter(fos692Test, "ms950");
								bw692Test = new BufferedWriter(osw692Test);
								*/
								for (ApplyData applyData : applyDatas) {
									// 測試檔不回饋
									if (applyData.getCenter() == "00")
										continue;
									/*
									 * 把applyData依sourceCode及merge分類
									 */
									String merge = applyData.getMerger() ? "1"
											: "0";
									String sourceCode = applyData
											.getSourceCode();
									String oldBatchNo = applyData
											.getOldBatchNo();
									if (sourceCode.toUpperCase().equals("REPT")) {
										oldBatchNo = "RePrint";
									} else if (oldBatchNo == null
											|| oldBatchNo.trim().equals("")) {
										String fileNm = applyData
												.getOldBatchName();
										String[] nameSplits = fileNm.split("_");
										if (fileNm.split("_").length >= 8) {
											oldBatchNo = nameSplits[5];
										}
									}
									String cycleDateStr = null;
									if (applyData.getCycleDate() != null)
										cycleDateStr = Constant.yyyyMMdd
												.format(applyData
														.getCycleDate());
									String key = sourceCode + "," + merge + ","
											+ oldBatchNo + ","
											+ applyData.getCenter() + ","
											+ cycleDateStr;
									List<ApplyData> applyDataList;
									if (sourceMap.get(key) != null) {
										applyDataList = sourceMap.get(key);
									} else {
										applyDataList = new ArrayList<ApplyData>();
									}
									applyDataList.add(applyData);
									sourceMap.put(key, applyDataList);

									Set<String> policyNos = applyData
											.getPolicyNoSet();
									String policyNo = "";
									if (policyNos != null) {
										for (String poNo : policyNos) {
											policyNo = poNo;
											String receipt = (applyData
													.getSourceCode().equals(
															"NORM") || applyData
													.getSourceCode().equals(
															"REPT")) ? "Y"
													: "N";
											if (applyData.getSourceCode()
													.equals("REPT")) {
												oldBatchNo = "RePrint";
											}
											String policyStatus = applyData.getPolicyStatus() == null? "00" : applyData.getPolicyStatus();
											Integer exceptionStatus = applyData.getExceptionStatus() == null || "".equals(applyData.getExceptionStatus())? 0 : new Integer(applyData.getExceptionStatus());
											
											String printOut = "Y"; 
											if(policyStatus.equals("100")){
												printOut = "Y";
											}else{
												if(policyStatus.compareTo("17") < 0){
													printOut = "N";
												}else{
													printOut = "Y";
												}													
											}
											if(exceptionStatus > 0 && exceptionStatus < 41){
												printOut = "N";
											}
											String line = // Constant.yyyyMMdd.format(cycleDate)											
											Constant.yyyyMMdd.format(applyData
													.getCycleDate())
													+ ","
													+ applyData.getCenter()
													+ ","
													+ policyNo
													+ ","
													+ StringUtils
															.leftPad(
																	applyData
																			.getReprint()
																			+ "",
																	2, '0')
													+ ","
													+ applyData.getApplyNo()
													+ ","
													+ applyData.getSourceCode()
													+ ",Y,Y,Y,"
													+ oldBatchNo
													+ "," + printOut + ","
													+ receipt
													+ ",,"
													+ (applyData.getTotalPage() == null ? 0
															: applyData
																	.getTotalPage())
													+ ","
													+ applyData.getAreaId()
													+ ","
													+ (applyData.getMerger() ? "1"
															: "0")
													+ ","													
													+ "FXDMS,P\r\n";
											/*
											String lineTest = // Constant.yyyyMMdd.format(cycleDate)
													Constant.yyyyMMdd.format(applyData
															.getCycleDate())
															+ ","
															+ applyData.getCenter()
															+ ","
															+ policyNo
															+ ","
															+ StringUtils
																	.leftPad(
																			applyData
																					.getReprint()
																					+ "",
																			2, '0')
															+ ","
															+ applyData.getApplyNo()
															+ ","
															+ applyData.getSourceCode()
															+ ",Y,Y,Y,"
															+ oldBatchNo
															+ ",Y,"
															+ receipt
															+ ",,"
															+ (applyData.getTotalPage() == null ? 0
																	: applyData
																			.getTotalPage())
															+ ","
															+ applyData.getAreaId()
															+ ","
															+ (applyData.getMerger() ? "1"
																	: "0")
															+ ","													
															+ "FXDMS,P\r\n";
										    */
											bw692.write(line);
											//bw692Test.write(lineTest);
										}
									}
								}
								//bw692Test.flush();
								osw692.flush();
								fos692.flush();

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
										//bw692Test.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									bw692 = null;
								}
								if (osw692 != null) {
									try {
										osw692.close();
										//osw692Test.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									osw692 = null;
								}
								if (fos692 != null) {
									try {
										fos692.close();
										//fos692Test.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									fos692 = null;
								}
							}
						}
					} else {
						Set<ApplyData> applyDatas = ((VoService) Constant
								.getContext().getBean("voServiceProxy"))
								.getApplyDataByNewBatchNm(afpFile
										.getNewBatchName());
						for (ApplyData applyData : applyDatas) {
							if (applyData.getReceipt() != null
									&& applyData.getReceipt()) {
								if (applyData.getSourceCode().toUpperCase()
										.equals("REPT")) {
									try {
										Integer center = new Integer(
												applyData.getCenter()) - 1;
										reptAcct[center]++;
									} catch (Exception e) {
										logger.error("", e);
									}

								} else if (applyData.getSourceCode()
										.toUpperCase().equals("NORM")) {
									try {
										Integer center = new Integer(
												applyData.getCenter()) - 1;
										normAcct[center]++;
									} catch (Exception e) {
										logger.error("", e);
									}
								}
							}
						}
					}
					// 把它 設成已回饋
					afpFile.setFeedback(true);
					((VoService) Constant.getContext()
							.getBean("voServiceProxy")).update(afpFile);
				}

				FileOutputStream fos689 = null;
				OutputStreamWriter osw689 = null;
				BufferedWriter bw689 = null;
				try {
					fos689 = new FileOutputStream(dtata689, true);
					osw689 = new OutputStreamWriter(fos689, "ms950");
					bw689 = new BufferedWriter(osw689);
					Set<String> keySet = sourceMap.keySet();
					for (String key : keySet) {
						String[] keySplit = key.split(",");
						String sourceCode = keySplit[0];
						String merge = keySplit[1];
						String oldBatchNo = keySplit[2];
						String center = keySplit[3];
						String cycleDateStr = keySplit[4];

						int policyNos = 0;
						int pages = 0;
						int applyNos = 0;
						String vendor = "FXDMS";
						List<ApplyData> applyDataList = sourceMap.get(key);
						for (ApplyData applyData : applyDataList) {
							applyNos++;
							policyNos += applyData.getPolicyNoSet().size();
							if (applyData.getAfpBeginPage() != null
									&& applyData.getAfpEndPage() != null) {
								pages += (applyData.getAfpEndPage()
										- applyData.getAfpBeginPage() + 1);
							}
						}
						String line = center + "," + sourceCode + ","
								+ cycleDateStr + "," + oldBatchNo + "," + merge
								+ "," + policyNos + "," + pages + ","
								+ applyNos + "," + vendor + "\r\n";
						bw689.write(line);
					}
					// 寫入簽收回條數
					for (int i = 0; i < 6; i++) {
						if (normAcct[i] != 0) {
							bw689.write("0"
									+ (i + 1)
									+ ",NORM,"
									+ Constant.yyyyMMdd.format(InputdateParser
											.getInputDate()) + ",回條,8,1,"
									+ normAcct[i] + ",1,FXDMS\r\n");
						}
						if (reptAcct[i] != 0) {
							bw689.write("0"
									+ (i + 1)
									+ ",REPT,"
									+ Constant.yyyyMMdd.format(InputdateParser
											.getInputDate()) + ",回條,8,1,"
									+ reptAcct[i] + ",1,FXDMS\r\n");
						}
					}
					bw689.flush();
					osw689.flush();
					fos689.flush();

				} catch (Exception e) {
					e.printStackTrace();
					logger.error("", e);

				} finally {
					if (bw689 != null) {
						try {
							bw689.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						bw689 = null;
					}
					if (osw689 != null) {
						try {
							osw689.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						osw689 = null;
					}
					if (fos689 != null) {
						try {
							fos689.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						fos689 = null;
					}
				}
			}
			if (dtata692.exists() && afpFiles != null && afpFiles.size() > 0) {
				try {
					FileUtils.copyFile(dtata692,
							new File(Properties.getFeedbackFolder(),
									"DTATA692_FXDMS.TXT"));
					dtata692.renameTo(new File(folder692, "DTATA692_FXDMS_"
							+ yyyyMMddHHmmSS));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (dtata689.exists() && afpFiles != null && afpFiles.size() > 0) {
				try {
					FileUtils.copyFile(dtata689,
							new File(Properties.getFeedbackFolder(),
									"DTATA689_FXDMS.TXT"));
					dtata689.renameTo(new File(folder689, "DTATA689_FXDMS_"
							+ yyyyMMddHHmmSS));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			// 產生report
			FileOutputStream fos689 = null;
			OutputStreamWriter osw689 = null;
			BufferedWriter bw689 = null;
			if (afpFiles != null && afpFiles.size() > 0) {
				try {
					fos689 = new FileOutputStream(new File(
							Properties.getFeedbackFolder(),
							"DTATA689_FXDMS.TXT"), true);
					osw689 = new OutputStreamWriter(fos689, "ms950");
					bw689 = new BufferedWriter(osw689);

					Map<String, Integer> reportMap = ((VoService) Constant
							.getContext().getBean("voServiceProxy"))
							.generateReport(newBatchNames);
					// 寫入回檔的report
					Set<String> keys = reportMap.keySet();
					for (String key : keys) {
						Integer number = reportMap.get(key);
						if (number != null && number != 0) {
							String[] keySplit = key.split("_");
							bw689.write(keySplit[1]
									+ ","
									+ keySplit[0]
									+ ","
									+ Constant.yyyyMMdd.format(InputdateParser
											.getInputDate()) + ",REPORT,9,1,"
									+ number + ",1,FXDMS\r\n");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("", e);
				} finally {
					if (bw689 != null) {
						try {
							bw689.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						bw689 = null;
					}
					if (osw689 != null) {
						try {
							osw689.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						osw689 = null;
					}
					if (fos689 != null) {
						try {
							fos689.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						fos689 = null;
					}
				}
				try {
					if (new File(folder689, "DTATA689_FXDMS_" + yyyyMMddHHmmSS)
							.exists()) {
						FilesUtils.forceDelete(new File(folder689,
								"DTATA689_FXDMS_" + yyyyMMddHHmmSS));
					}
					FileUtils.copyFile(new File(Properties.getFeedbackFolder(),
							"DTATA689_FXDMS.TXT"), new File(folder689,
							"DTATA689_FXDMS_" + yyyyMMddHHmmSS));
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}

			}

			running = false;
			logger.info("feeback file this time  stop");
			properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
			properties.setProcessDone(false);
			((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);
		}
	}

}
