package com.fxdms.cathy.task;

import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;

import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FilesUtils;
import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Area;
import com.salmat.pas.vo.ImgFile;

public class AddressProcesser {

	static Logger logger = Logger.getLogger(AddressProcesser.class);	
	private static boolean running = false;
	public static Map<String, String> serviceCenterMap = null;
	private static String cycleDateStr = null;
	private static List<ApplyData> applyDatas = null;
	private static Area taipeiNo2;

	
	public static Map<String, String> readFile(File[] files){
		//先看看有沒有最近的檔案
		File txtFolder = new File(Properties.getLocalPolicyTxtPath()); //一般batch件folder D:/dataIn/DATA/
		File addrFolder = new File(txtFolder.getParent(), "ADDR");
		if(!addrFolder.exists())
			addrFolder.mkdirs();		
		File[] recentFiles = addrFolder.listFiles();
		//有的話讀最新的檔案，沒有的話讀傳入的檔案
		if(recentFiles != null && recentFiles.length > 0){
			files = recentFiles;
		}
		Map<String, String> serviceCenterMap = new HashMap<String, String>();
		for(File file : files){
			String fileNm = file.getName().toUpperCase();
			if(fileNm.startsWith("DIV_NO_INFO") || fileNm.startsWith("DIV_NO_RVK_INFO") ){
				FileInputStream fis = null;
				InputStreamReader isr = null;
				BufferedReader br = null;
				String line = null;
				try {
					fis = new FileInputStream(file);
					isr = new InputStreamReader(fis, "ms950");
					br = new BufferedReader(isr);					
					while((line = br.readLine()) != null) {
						line = line.trim();
						if(!line.startsWith("DIV_NO")){
						   String [] lineSplit = line.split(",");
						   if(lineSplit.length == 0){
						   }else if(lineSplit.length == 1){
							   serviceCenterMap.put(lineSplit[0], null);
						   }else if(lineSplit.length >= 2){
							   if(lineSplit[1] != null && !lineSplit[1].toLowerCase().equals("null") && lineSplit[1].trim().length() == 7)
							       serviceCenterMap.put(lineSplit[0].trim(), lineSplit[1].trim());
							   else
								   serviceCenterMap.put(lineSplit[0].trim(), null);
						   }
						}		
					}
				}catch(Exception e){
					logger.error("", e);
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
				
			}			
		}
		return serviceCenterMap;
	}
	
	public static void startToRun() {
		//時間設定更新影像檔的程式要早一點進行，如果更新還在進行中，先暫停此thread，等下一次再進行
		if(running )
			return;		
		running = true;
		
		logger.info("addr processer start to run");
		
		File txtFolder = new File(Properties.getLocalPolicyTxtPath()); //一般batch件folder D:/dataIn/DATA/
		File addrFolder = new File(txtFolder.getParent(), "ADDR");
		if(!addrFolder.exists())
			addrFolder.mkdirs();
		
		File[] files = addrFolder.listFiles();
		serviceCenterMap = readFile(files);
		if(serviceCenterMap.size() > 0){
			//((VoService) Constant.getContext().getBean("voServiceProxy")).updateAreaCenter(serviceCenterMap);
			//有和團險地址檔衝突的可能，以後再說
		}
		for(File file : files)
		   try {
			   InputdateParser.forceReadFile();
			   cycleDateStr = Constant.yyyy_MM_dd.format(InputdateParser.getInputDate());
			   String fileNm = file.getName();
			   if(fileNm.startsWith("DIV_NO_INFO") || fileNm.startsWith("DIV_NO_RVK_INFO") ){
			      FilesUtils.moveFileToDirectory(file, new File(Properties.getBackupFolder(), "addr/" + cycleDateStr), true);
			   }
			   
		   } catch (IOException e) {
		       logger.error("", e);
			   e.printStackTrace();
		   }
		
		
		
		logger.info("addr processer  stop");
		running = false;
	}		
	
	public static List<ApplyData> getApplyDatas(){
		return applyDatas;
	}

	public static List<ApplyData> parseFile(File file) {
		applyDatas = new ArrayList<ApplyData>();
		taipeiNo2 = ((VoService) Constant.getContext().getBean("voServiceProxy")).getArea("9D00000");
		//taipeiNo2 =  new Area();

		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		String line = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis, "ms950");
			br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				try {
					line = line.trim();
					String[] lineSplit = line.split("\\|");
					if (lineSplit.length >= 14) {
						ApplyData applyData = new ApplyData();
						Set<String> policyNoSet = new HashSet<String>();
						String applyNo = lineSplit[0].toUpperCase();
						// System.out.println(applyNo);
						String policyNo = lineSplit[1].toUpperCase();
						policyNoSet.add(policyNo);
						boolean merge = lineSplit[2].equals("0") ? false : true;
						Integer reprint = 0;
						try {
							reprint = new Integer(lineSplit[3]);
						} catch (Exception e) {
							logger.info("", e);
						}
						String areaId = lineSplit[4];
						String agentNm = lineSplit[5];
						String deliverType = lineSplit[6];
						String zipCode = lineSplit[7].replaceAll(" ", "");
						String address = lineSplit[8].replaceAll(" ", "");
						String receiver = lineSplit[9];
						String recBank = null;
						if (receiver.split(" ").length == 2) {
							recBank = receiver.split(" ")[0];
						}
						//編碼問題，名字有難字時可能會造成截斷錯誤
						if("正常".equals(lineSplit[12]) || (lineSplit[12] != null && lineSplit[12].indexOf("查詢地址有誤") >= 0)){
							try{
							   for(int i = lineSplit.length - 1 ; i >= 12 ; i--){
								  lineSplit[i] = lineSplit[i - 1] ;
							   }	
							}catch(Exception e){
							   logger.error("", e);	
							}
						}
						String bankReceiptId = lineSplit[12];
						Boolean parseNorm = lineSplit[13].trim().equals("正常") ? true
								: false;
						String pareseString = lineSplit[13].trim();

						applyData.setApplyNo(applyNo);
						applyData.setPolicyNoSet(policyNoSet);
						applyData.setReprint(0);
						applyData.setAreaId(areaId);
						// 有錯時送回北二
						if (parseNorm)
							applyData.setReceiver(receiver);
						else
							applyData.setReceiver(taipeiNo2.getAreaName());

						applyData.setDeliverType(deliverType);
						if (parseNorm && !applyData.getDeliverType().equals("B"))
							applyData.setAddress(zipCode + " " + address);
						else
							applyData.setAddress(taipeiNo2.getZipCode() + " "
									+ taipeiNo2.getAddress());
						if (parseNorm && !applyData.getDeliverType().equals("B"))
							applyData.setZip(zipCode);
						else
							applyData.setZip(taipeiNo2.getZipCode());
						applyData.setAgentNm(agentNm);
						applyData.setReceiverBank(recBank);
						applyData.setBankReceiptId(bankReceiptId);
						applyData.setParseNorm(parseNorm);
						applyData.setPareseString(pareseString);
						if (lineSplit.length >= 16) {
							String mailType = lineSplit[14] == null? "" : lineSplit[14].trim();
							String mailReceiptInd = lineSplit[15] == null? "" : lineSplit[15].trim();
							applyData.setMailReceiptIndex(mailReceiptInd);
							applyData.setMailType(mailType);
						}
						applyDatas.add(applyData);
					}
					
				} catch (Exception e) {
					logger.error("", e);
				}
			}			
			return applyDatas;
		} catch (Exception e) {
			logger.error("", e);
			applyDatas = null;
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
				logger.error("", e);
			}
			br = null;
			isr = null;
			fis = null;
			try {
				FilesUtils.moveFileToDirectory(file,
						new File(Properties.getBackupFolder(), "addr"), true);
			} catch (IOException e) {
				logger.error("", e);
				e.printStackTrace();
			}
		}

	}	
		
	public static void main(String args[]){
		List<ApplyData> applyDatas = parseFile(new File("D:/tmp/2016-02-26_06_ADDRESSvsRECIPTNO.txt"));
		for(ApplyData applyData : applyDatas){
			System.out.println(applyData.getPolicyNos() + "|" + applyData.getApplyNo() + "|" + applyData.getReceiverBank() + "|" + applyData.getAgentNm() + "|" + applyData.getAreaId() + "|" + applyData.getAddress() + "|" + applyData.getReceiver() + "|" + applyData.getBankReceiptId() + "|" + applyData.getZip() + "|" + applyData.getParseNorm());
		}
	}

	public static Area getTaipeiNo2() {
		return taipeiNo2;
	}

	public static void setTaipeiNo2(Area taipeiNo2) {
		AddressProcesser.taipeiNo2 = taipeiNo2;
	}
	
	
	
}
