package com.fxdms.cathy.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FileFilterImpl;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;

public class PolicyBookCounter {

	static Logger logger = Logger.getLogger(PolicyBookCounter.class);
	private static boolean running = false;

	public synchronized static void startToRun() throws BeansException,
			IOException {
		running = false;
		if (running)
			return;
		File backupData = new File(Properties.getBackupFolder(), "DATA/");
		// 日期目錄
		File[] subDirs = backupData.listFiles(FileFilterImpl
				.getDirectoryFilter());
		for (File dateFolder : subDirs) {
			if (dateFolder.getName().length() == 10) {
				// 行政中心目錄
				File[] centerFolders = dateFolder.listFiles(FileFilterImpl
						.getDirectoryFilter());
				for (File centerFolder : centerFolders) {
					File[] files = centerFolder.listFiles(FileFilterImpl
							.getFileFilter());
					FileWriter fw = null;
					File outputFile = new File(centerFolder,
							dateFolder.getName() + "_" + centerFolder.getName()
									+ ".txt");
					if (!outputFile.exists() && files != null && files.length > 1){
						fw = new FileWriter(outputFile);
						for (File file : files) {
							// 2014-07-16_2014-07-16_04_REIS_04_POSPRINT_合併件保單
							// 2014-07-16_2014-07-16_04_NORM_MI401494_非合併件保單
							/*
							 * String line = center + "," + sourceCode + "," +
							 * Constant.yyyyMMdd.format(InputdateParser
							 * .getInputDate()) + "," + oldBatchNo + "," + merge
							 * + "," + policyNos + "," + pages + "," + applyNos
							 * + "," + vendor + "\r\n";
							 */
							String fileNm = file.getName();
							if (fileNm.indexOf("保單") >= 0) {
								String merge = "1";
								if (fileNm.indexOf("非合併") > 0)
									merge = "0";
								String[] fileNmSplit = fileNm.split("_");
								FileReader fr = null;
								BufferedReader br = null;
								try {
									fr = new FileReader(file);
									br = new BufferedReader(fr);
									String line = null;
									int counter = 0;
									int policyCounter = 0;
									while((line = br.readLine()) != null){
										if(line.trim().toLowerCase().startsWith("info|")){
											policyCounter++;
										}
										if(line.trim().equals("%%eoj")){
											counter++;
										}
									}
									String oldbatchNo = fileNmSplit[4];
									if(fileNmSplit.length == 7)
										oldbatchNo = fileNmSplit[5];
									String writeLine = fileNmSplit[2] + "," + fileNmSplit[3] + "," + dateFolder.getName() + "," + oldbatchNo + "," + merge + "," + policyCounter + ",," + counter + ",FXDMS\r\n";
									fw.write(writeLine);

								} catch (Exception e) {
									logger.error("", e);
									e.printStackTrace();
								} finally {
									try {
										if (br != null)
											br.close();
										if (fr != null)
											fr.close();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

							}
						}
						fw.close();
					}
				}
			}

		}

		logger.info("finished count");
		running = false;

	}
}
