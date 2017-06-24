package com.fxdms.cathy.task;

import java.awt.image.RenderedImage;
import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;

import thor.util.JaiTiffImgProcess;

import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FileFilterImpl;
import com.fxdms.util.FilesUtils;
import com.fxdms.util.TiffChecker;
import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.bo.SplitFile;
import com.fxdms.cathy.conf.Constant;
import com.itextpdf.text.Image;
import com.salmat.pas.vo.ErrorReport;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

public class GpTestPolicy {

	static Logger logger = Logger.getLogger(GpTestPolicy.class);

	private static boolean running = false;

	private static File imgOkFile = new File(new File(
			Properties.getGroupInFolder(), "OK"), "TEST_IMAGE.ok");
	private static File tmpFolder = new File(Properties.getPresPath(), "tmp");

	public static void startToRun() throws BeansException, RemoteException {

		// 時間設定更新影像檔的程式要早一點進行，如果更新還在進行中，先暫停此thread，等下一次再進行
		// 如果有其它Thread正在run，也是跳出去

		if (running || GpImgUpdater.isRunning())
			return;

		logger.info("group test dispatcher start to run");

		try {
			String todayStr = Constant.yyyyMMdd.format(new Date());
			running = true;
			File txtTestFolder = new File(Properties.getGroupInFolder(),
					"TEST_DATA"); // 測試件
			if (!txtTestFolder.exists())
				txtTestFolder.mkdirs();

			File okFolder = new File(Properties.getGroupInFolder(), "OK");
			File testImgFolder = new File(Properties.getGroupInFolder(),
					"TEST_IMAGE");
			if (!okFolder.exists())
				okFolder.mkdirs();
			if (!testImgFolder.exists())
				testImgFolder.mkdirs();
			File testOkFile = new File(okFolder, "TEST_DATA.ok");
			boolean processBegin = false;

			if (imgOkFile.exists()) {
				File[] imgFiles = testImgFolder.listFiles();
				File subFolder = new File(Properties.getPresPath())
						.getParentFile();
				subFolder = new File(subFolder, "group/image/");
				if (imgFiles != null && imgFiles.length > 0) {
					for (File oriTiffFile : imgFiles) {
						File destFile = null;
						if (oriTiffFile.getName().toLowerCase()
								.endsWith(".tif")
								|| oriTiffFile.getName().toLowerCase()
										.endsWith(".tiff")) {
							try {
								// 改存為600dpi
								FileSeekableStream ss = null;
								try {
									ss = new FileSeekableStream(oriTiffFile);
									ImageDecoder dec = ImageCodec
											.createImageDecoder("tiff", ss,
													null);
									int count = dec.getNumPages();
									RenderedImage[] images = new RenderedImage[count];
									for (int i = 0; i < count; i++) {
										PlanarImage page = new RenderedImageAdapter(
												dec.decodeAsRenderedImage(i));
										page = JaiTiffImgProcess.cutImage(page,
												10, 5);
										float width = page.getWidth();
										float height = page.getHeight();
										page = JaiTiffImgProcess
												.shrinkImage(
														(JaiTiffImgProcess.shrinkWidth / width),
														(JaiTiffImgProcess.shrinkHeight / height),
														page);
										int borderWidth = ((int) JaiTiffImgProcess.fixWidth - (int) JaiTiffImgProcess.shrinkWidth) / 2;
										int borderHeight = ((int) JaiTiffImgProcess.fixHeight - (int) JaiTiffImgProcess.shrinkHeight) / 2;
										page = JaiTiffImgProcess.doBorder(page,
												borderWidth, borderWidth,
												borderHeight, borderHeight,
												null);
										images[i] = page;
									}
									destFile = new File(subFolder,
											oriTiffFile.getName());

									// 如果存在就先刪除
									if (destFile.exists())
										FileUtils.forceDelete(destFile);
									// 存成多頁式600 dpi
									JaiTiffImgProcess
											.saveAsMultipageTIFFCompression(
													images,
													destFile.getAbsolutePath());
								} catch (Exception e) {
									logger.error("", e);
									e.printStackTrace();
									ErrorReport err = new ErrorReport();
									err.setErrHappenTime(new Date());
									err.setErrorType("exception");
									err.setOldBatchName(oriTiffFile.getName());
									err.setReported(false);
									err.setException(true);
									err.setMessageBody("無法處理" + oriTiffFile.getName());
									err.setTitle("IMG_PROCESS_ERR");
									((VoService) Constant.getContext().getBean(
											"voServiceProxy")).save(err);
								} finally {
                                    if(ss != null)
                                    	ss.close();
								}
							} catch (Exception e) {
								e.printStackTrace();
								logger.error("", e);
								ErrorReport err = new ErrorReport();
								err.setErrHappenTime(new Date());
								err.setErrorType("exception");
								err.setOldBatchName(oriTiffFile.getName());
								err.setReported(false);
								err.setException(true);
								err.setMessageBody("無法處理" + oriTiffFile.getName());
								err.setTitle("IMG_PROCESS_ERR");
								((VoService) Constant.getContext().getBean(
										"voServiceProxy")).save(err);
							} finally {

							}
						}
						if (oriTiffFile.exists())
							try{
							   FilesUtils.forceDelete(oriTiffFile);
							}catch(Exception e){
								e.printStackTrace();
								logger.error("", e);
								ErrorReport err = new ErrorReport();
								err.setErrHappenTime(new Date());
								err.setErrorType("exception");
								err.setOldBatchName(oriTiffFile.getName());
								err.setReported(false);
								err.setException(true);
								err.setMessageBody("無法刪除" + oriTiffFile.getName());
								err.setTitle("IMG_PROCESS_ERR");
								((VoService) Constant.getContext().getBean(
										"voServiceProxy")).save(err);
							}
					}					
				}
				if (imgOkFile.exists())
					FilesUtils.forceDelete(imgOkFile);
			}

			if (testOkFile.exists())
				processBegin = true;

			logger.info("group process begin");
			// 如果可以開始進行，開始搬到tmp folder，寫入DB，並依轄區歸類到資料夾
			if (processBegin) {
				
				File[] files = txtTestFolder.listFiles(FileFilterImpl
						.getFileFilter());
				// 先把所有檔案拆開
				// if(!reisRecheck){
				for (File file : files) {					
					String[] fileNmSplit = file.getName().split("_");
					// 如果檔案名稱不對時，通知發生錯誤，並移到備份目錄
					if (fileNmSplit.length < 6
							|| !fileNmSplit[3].toUpperCase().equals("GROUP")) {
						ErrorReport err = new ErrorReport();
						err.setErrHappenTime(new Date());
						err.setErrorType("errFileNm");
						err.setOldBatchName(file.getName());
						err.setReported(false);
						err.setException(true);
						err.setMessageBody("測試團險目錄中放入錯誤的檔案名稱" + file.getName());
						err.setTitle("apply data wrong format ");
						((VoService) Constant.getContext().getBean(
								"voServiceProxy")).save(err);
						FilesUtils.moveFileToDirectory(file, new File(
								"D:\\group_dataOUT\\CHECK_ERR_DATA"), true);
						continue;
					}
					ErrorReport err1 = new ErrorReport();
					err1.setErrHappenTime(new Date());
					err1.setErrorType("TestGroupPolicy");
					err1.setOldBatchName(file.getName());
					err1.setReported(true);
					err1.setException(false);
					err1.setMessageBody("團險測試開始進行" + file.getName());
					err1.setTitle("Test Group Policy");
					((VoService) Constant.getContext().getBean(
							"voServiceProxy")).save(err1);
					// 切開檔案並偷改tif檔路徑
					SplitFile.groupSplit(file, txtTestFolder);
					try {
						// 刪除原始檔案
						File testBackupFolder = new File(Properties.getGpBackupFolder(), "TEST_DATA");
						FilesUtils.moveFileToDirectory(file, testBackupFolder , true);
					} catch (Exception e) {
						ErrorReport err = new ErrorReport();
						err.setErrHappenTime(new Date());
						err.setErrorType("exception");
						err.setOldBatchName(file.getName());
						err.setReported(false);
						err.setException(true);
						err.setMessageBody("無法刪除" + file.getName());
						err.setTitle("delete error");
						((VoService) Constant.getContext().getBean(
								"voServiceProxy")).save(err);
					}

				}

				File[] allFiles = txtTestFolder.listFiles();
				List<File> receiptFiles = new ArrayList<File>();
				List<File> policyFiles = new ArrayList<File>();

				for (File file : allFiles) {
					String oldBatchName = file.getName();
					if (oldBatchName.toLowerCase().indexOf("sign") > 0) {
						receiptFiles.add(file);
					} else {
						policyFiles.add(file);
					}
				}
				if (processBegin) {
					File presGpDir = new File("D:\\kotai\\group");
					String policyName = "GA09T" + todayStr + "9990.DAT";
					String receiptName = "GG09T" + todayStr + "9990.DAT";
					if (policyFiles.size() > 0) {
						boolean written = mergeFile(policyFiles, presGpDir, policyName);
						if(written)
						   new File(tmpFolder, policyName).renameTo(new File(
								Properties.getPresPath(), policyName));
						else if(new File(tmpFolder, policyName).exists())
							FileUtils.forceDelete(new File(tmpFolder, policyName));
					}
					if (receiptFiles.size() > 0) {
						boolean written = mergeFile(receiptFiles, presGpDir, receiptName);
						if(written)
						   new File(tmpFolder, receiptName).renameTo(new File(
								Properties.getPresPath(), receiptName));
						else if(new File(tmpFolder, receiptName).exists())
							FileUtils.forceDelete(new File(tmpFolder, receiptName));
					}
				}
				// 全部作完後把data.ok和image.ok幹掉
				if (testOkFile.exists())
					FilesUtils.forceDelete(testOkFile);				
			} // end processBegin
		} catch (Exception e) {
			logger.error("", e);

		}finally{
			running = false;
		}
	}

	private static boolean mergeFile(List<File> policyFiles, File presGpDir,
			String fileName) throws IOException {
		boolean written = false;
		for (File policyFile : policyFiles) {
			FileInputStream fis = null;
			InputStreamReader isr = null;
			BufferedReader br = null;
			String line = null;
			boolean imgErr = false;
			try {
				logger.info("group test read file");
				fis = new FileInputStream(policyFile);
				isr = new InputStreamReader(fis, "ms950");
				br = new BufferedReader(isr);

				while ((line = br.readLine()) != null) {
					// 如果是有tif檔或是要保書影像檔，開始進行檢查
					int index = -1;
					if (((index = line.indexOf(".tif")) > 0)
							|| ((index = line.indexOf(".TIF")) > 0)) {
						// 先截到.tif為止，免得後面還有導致出錯
						String forCheck = null;
						forCheck = line.substring(0, index + 4).toLowerCase();

						String fileNm = null;
						int beginIndex = 0;
						// 從最後一個..之後截斷
						if ((beginIndex = forCheck.lastIndexOf("group")) > 0) {
							fileNm = forCheck.substring(beginIndex + 5).trim();
						}
						File tifFile = new File(presGpDir, fileNm);
						// 如果不存在就跳出去
						if (!tifFile.exists()) {
							imgErr = true;
							FilesUtils
									.copyFileToDirectory(
											policyFile,
											new File(
													"D:\\group_dataOUT\\CHECK_ERR_DATA"),
											true);

							ErrorReport err = new ErrorReport();
							err.setErrHappenTime(new Date());
							err.setErrorType("errImg");
							err.setOldBatchName(policyFile.getName());
							err.setReported(false);
							err.setException(true);
							err.setMessageBody("團險測試檔影像檔不存在:"
									+ tifFile.getAbsolutePath());
							err.setTitle("error on:" + line);
							((VoService) Constant.getContext().getBean(
									"voServiceProxy")).save(err);

							break;
						}
					}
				}
			} catch (Exception e) {
				logger.error("", e);
				ErrorReport err = new ErrorReport();
				err.setErrHappenTime(new Date());
				err.setErrorType("exception");
				err.setOldBatchName(policyFile.getName());
				err.setReported(false);
				err.setException(true);
				err.setMessageBody("測試團險讀檔錯誤" + e.getMessage());
				err.setTitle("error on:" + line);
				((VoService) Constant.getContext().getBean("voServiceProxy"))
						.save(err);
			} finally {
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
			}
			if (!imgErr) {
				written = true;
				fis = null;
				BufferedInputStream bis = null;
				BufferedOutputStream bos = null;
				FileOutputStream fos = null;
				// 開始進行合併保單
				try {
					byte[] bufferedB = new byte[2048];
					fos = new FileOutputStream(new File(tmpFolder, fileName),
							true);
					bos = new BufferedOutputStream(fos);
					fis = new FileInputStream(policyFile);
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
					er.setMessageBody("exception happen:" + e.getMessage());
					er.setTitle("exception happened");
					((VoService) Constant.getContext()
							.getBean("voServiceProxy")).save(er);
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
			// 作完後砍掉
			FilesUtils.forceDelete(policyFile);
		}
		return written;
	}

}
