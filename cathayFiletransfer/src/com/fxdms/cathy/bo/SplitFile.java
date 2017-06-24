package com.fxdms.cathy.bo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.salmat.pas.vo.ErrorReport;

public class SplitFile {
	static Logger logger = Logger.getLogger(SplitFile.class);
	static String comparedate = null;
	public static HashMap<String, Integer> reprintMap = new HashMap<String, Integer>();
	private static HashMap<String, String> areaMap = new HashMap<String, String>();

	public static synchronized void splitCathay(File oriFile, File destFolder)
			throws IOException {
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		if(comparedate == null || !sdf.format(today).equals(comparedate)){
			comparedate = sdf.format(today);
		}
		HashSet<String> policyNos = new HashSet<String>();
		if (!destFolder.exists())
			destFolder.mkdirs();
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(oriFile);
			bis = new BufferedInputStream(fis);
			byte[] oneByte = new byte[1];
			List<Byte> fileByte = new ArrayList<Byte>();
			List<Byte> lineByte = new ArrayList<Byte>();
			char seperate1 = '\r'; //0D
			char seperate2 = '\n'; //0A
			String prevAppNo = null;
			FileOutputStream fos = null;
			String forReplace = null;
			String lineStr = null;
			while (bis.read(oneByte) > 0) {	
				if(fileByte.size() == 0 && lineByte.size() == 0  && (seperate2 == oneByte[0] || seperate1 == oneByte[0])){
					//不能以0D0A開頭
				}else{				
				    lineByte.add(oneByte[0]);
				}
				if ((seperate1 == oneByte[0]) || (seperate2 == oneByte[0])) {
					byte[] linebytes = new byte[lineByte.size()];
					int i = 0;
					for (Byte singleByte : lineByte) {
						linebytes[i] = singleByte;
						i++;
					}

					lineStr = new String(linebytes, "ms950").trim();
					//不是info
					if(lineStr.indexOf("info") < 0 
							&& (lineStr.toLowerCase().indexOf(".pdf") < 0  || lineStr.toLowerCase().indexOf(".pdf.tif") >= 0 || lineStr.toUpperCase().indexOf("|Z") < 0)){						
						//009A|z20...... c:\kotai\image\test|1 此行無法處理 
						int index = lineStr.toLowerCase().indexOf("c:\\kotai\\image\\test|1");
						String oriFilePath = oriFile.getAbsolutePath();
						//如果是來自測試資料夾的tif，要加上/test
						if(lineStr.toLowerCase().indexOf(".tif") >= 0 && lineStr.toLowerCase().indexOf("\\image") >= 0 
								&& oriFilePath.toLowerCase().indexOf(Properties.getLocalTestPolicyPath()) >= 0){
	                          lineStr = lineStr.replaceAll("\\\\image", "\\\\test\\\\image");
	                          for(Byte singleByte : lineStr.getBytes("ms950")){
								   if(singleByte != seperate1 && singleByte != seperate2)
							          fileByte.add(singleByte);
							  }						   
	                          fileByte.add(oneByte[0]);								  							
						}else if( !(lineStr.toLowerCase().indexOf("009a|z20|") >= 0 && lineStr.endsWith("|1") && index > 0) ){
						   fileByte.addAll(lineByte);
						}else{
						   String [] lineSplit = lineStr.split("\\|");
						   String policyNo = lineSplit[0];						
						   lineStr = lineStr.substring(0, index) + "..\\Image\\" + forReplace.replaceAll("tmppolicyNo", policyNo) + "|2";
						   for(Byte singleByte : lineStr.getBytes("ms950")){
							   if(singleByte != seperate1 && singleByte != seperate2)
						          fileByte.add(singleByte);
						   }						   
                           fileByte.add(oneByte[0]);						   						   
						}						      
					}
					if (lineStr.indexOf("info") >= 0) {
						//info|9097058358|00|CC06704474|G221579483|NORM||A*14206|專巨鼎瑞晟|0|01|300000.0000||						
						String[] splits = lineStr.split("\\|");
						String applyNo = splits[3];
						String policyNo = splits[1];
						policyNos.add(policyNo);
						String reprint = splits[2];
						String merger = splits[9].equals("0")? "N" : "M";
						String forCheckFile = applyNo + ".tmppolicyNo." + reprint + "." + merger + ".tif";
						forReplace = forCheckFile;
						
						//10個X時一定是非合併件
						if(applyNo.indexOf("XXXXXXXXXX") >= 0){
						}
						if(oriFile.getName().indexOf("非合併") > 0){
							applyNo = policyNo;
						}

						fileByte.addAll(lineByte);												 
						if (prevAppNo == null || !prevAppNo.equals(applyNo)) {
							prevAppNo = applyNo;
						}
					}else if (lineStr.toLowerCase().indexOf(".pdf") >= 0  && lineStr.toLowerCase().indexOf(".pdf.tif") < 0 && lineStr.toUpperCase().indexOf("|Z") >= 0){
						//把pdf偷改成tif
						int pdfInd = lineStr.toLowerCase().indexOf(".pdf");
						int beginPoint = lineStr.lastIndexOf("\\", pdfInd);
						if(beginPoint < 0){
							beginPoint = lineStr.lastIndexOf("|", pdfInd);
						}
						String pdfMainName = null;
						if(beginPoint >= 0){							
							pdfMainName = lineStr.substring(beginPoint + 1, pdfInd);							
						}
						if(pdfMainName != null){					       					    	 
					    	 lineStr = lineStr.replaceAll("(?i)" + pdfMainName + ".pdf",  pdfMainName + ".pdf.tif");
						}						
                        for(Byte singleByte : lineStr.getBytes("ms950")){
						   if(singleByte != seperate1 && singleByte != seperate2)
						         fileByte.add(singleByte);
						}						   
                        fileByte.add(oneByte[0]);
					    
					}else if (lineStr.indexOf("%%eoj") >= 0) {
						fos = new FileOutputStream(new File(destFolder,
								oriFile.getName() + "_" + prevAppNo), true);
						byte[] filebytes = new byte[fileByte.size()];
						int j = 0;
						for (Byte singleByte : fileByte) {
							filebytes[j] = singleByte;
							j++;
						}
						fos.write(filebytes);
						fos.write(seperate2);
						fos.flush();
						fos.close();
						forReplace = null;
						fileByte = new ArrayList<Byte>();
					}

					lineByte = new ArrayList<Byte>();
				}
				// System.out.println(oneByte[0] + "|" + (seperate1 ==
				// oneByte[0]) + "|" + (seperate2 == oneByte[0]) );
			}
			if (fileByte != null && fileByte.size() > 200) {				
				fos = new FileOutputStream(new File(destFolder,
						oriFile.getName() + "_" + prevAppNo), true);
				byte[] filebytes = new byte[fileByte.size()];
				int j = 0;
				for (Byte singleByte : fileByte) {
					filebytes[j] = singleByte;
					j++;
				}
				fos.write(filebytes);
				if(lineStr.indexOf("%%eoj") < 0){
					fos.write("%%eoj\r\n".getBytes());
				}
				fos.flush();
				fos.close();
			}
		} catch (IOException e) {
			logger.error("", e);
			throw e;
		} finally {
			if (bis != null)
				bis.close();
			if (fis != null)
				fis.close();
		}
	}
	
	public static synchronized void returnSplitCathay(File oriFile, File destFolder)	
			throws IOException {
	    String signOrpolicy = "sign";
	    if(oriFile.getName().toUpperCase().indexOf("SPLITPOLICY") > 0){
	    	signOrpolicy = "policy";
	    }
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		HashSet<String> policyNos = new HashSet<String>();
		if (!destFolder.exists())
			destFolder.mkdirs();
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(oriFile);
			bis = new BufferedInputStream(fis);
			byte[] oneByte = new byte[1];
			List<Byte> fileByte = new ArrayList<Byte>();
			List<Byte> lineByte = new ArrayList<Byte>();
			char seperate1 = '\r'; //0D
			char seperate2 = '\n'; //0A
			String preAppNo = null;
			String prePolicyNo = null;
			String preReprint = null;
			FileOutputStream fos = null;
			
			String lineStr = null;
			boolean replaceReprint = false;
			String newStr = null;
			String oldStr = null;
			String applyNo = null;
			String policyNo = null;
			String reprint = null;
			String applyNo1 = null;
			String policyNo1 = null;
			String reprint1 = null;
			String applyNo2 = null;
			String policyNo2 = null;
			String reprint2 = null;
			while (bis.read(oneByte) > 0) {	
				if(fileByte.size() == 0 && lineByte.size() == 0 && (seperate2 == oneByte[0] || seperate1 == oneByte[0])){
					//不能以0D0A開頭
				}else{				
				    lineByte.add(oneByte[0]);
				}
				if ((seperate1 == oneByte[0]) || (seperate2 == oneByte[0])) {
					byte[] linebytes = new byte[lineByte.size()];
					int i = 0;
					for (Byte singleByte : lineByte) {
						linebytes[i] = singleByte;
						i++;
					}

					lineStr = new String(linebytes, "ms950").trim();
					//不是info
					if(lineStr.indexOf("info") < 0 && (lineStr.toLowerCase().indexOf(".pdf") < 0 || lineStr.toUpperCase().indexOf("|Z") < 0) ){						
						//009A|z20...... c:\kotai\image\test|1 此行無法處理 
						int index = lineStr.toLowerCase().indexOf("c:\\kotai\\image\\test|1");
						//如果是來自測試資料夾的tif，要加上/test
						if(replaceReprint && oldStr != null && newStr != null){
							lineStr = lineStr.replaceAll(oldStr,  newStr);
							for(Byte singleByte : lineStr.getBytes("ms950")){
							   if(singleByte != seperate1 && singleByte != seperate2)
							      fileByte.add(singleByte);
							}						   
							fileByte.add(oneByte[0]);
						}else{
					        fileByte.addAll(lineByte);
						}
					}

					if (lineStr.indexOf("info") >= 0) {
						//info|9097058358|00|CC06704474|G221579483|NORM||A*14206|專巨鼎瑞晟|0|01|300000.0000||						
						String[] splits = lineStr.split("\\|");
						if(applyNo == null)
						   applyNo = splits[3];
						else if(applyNo1 == null)
						   applyNo1 = splits[3];
						else if(applyNo2 == null)
						   applyNo2 = splits[3];
						
						if(policyNo == null)
						   policyNo = splits[1];
						else if(policyNo1 == null)
						   policyNo1 = splits[1];
						else if(policyNo2 == null)
						   policyNo2 = splits[1];
						
						policyNos.add(policyNo);
						
						if(reprint == null)
						   reprint = splits[2];
						else if(reprint1 == null)
						   reprint1 = splits[2];
						else if(reprint2 == null)
						   reprint2 = splits[2];
						
						try{
							new Integer(reprint);
						}catch(Exception e){
							e.printStackTrace();
							reprint = "00";
						}						
						
						if (preAppNo == null || !preAppNo.equals(applyNo + "_" + policyNo + "_" + reprint)) {
							preAppNo = applyNo + "_" + policyNo + "_" + reprint;
							Integer appearTimes = reprintMap.get(preAppNo + signOrpolicy);
							if(appearTimes == null){
								reprintMap.put(preAppNo + signOrpolicy, new Integer(0));								
							}else{							
								logger.info("重覆的保單資料:" + oriFile.getName() );
								
								oldStr = policyNo + "\\|" + reprint + "\\|";
								appearTimes++;
								reprint = appearTimes + "1";																
								//9001941698|00|
								newStr = policyNo + "\\|" + reprint + "\\|";
								logger.info("字串替代:" + oldStr + "-->" + newStr ); 
								reprintMap.put(preAppNo + signOrpolicy, appearTimes);
								replaceReprint = true;
							}
						}
						if(replaceReprint && oldStr != null && newStr != null){
							lineStr = lineStr.replaceAll(oldStr,  newStr);
							for(Byte singleByte : lineStr.getBytes("ms950")){
							   if(singleByte != seperate1 && singleByte != seperate2)
							      fileByte.add(singleByte);
							}						   
							fileByte.add(oneByte[0]);
						}else
						    fileByte.addAll(lineByte);												 
						
					} else if (lineStr.toLowerCase().indexOf(".pdf") >= 0  && lineStr.toLowerCase().indexOf(".pdf.tif") < 0 && lineStr.toUpperCase().indexOf("|Z") >= 0){
						//把pdf偷改成tif
						int pdfInd = lineStr.toLowerCase().indexOf(".pdf");
						int beginPoint = lineStr.lastIndexOf("\\", pdfInd);
						if(beginPoint < 0){
							beginPoint = lineStr.lastIndexOf("|", pdfInd);
						}
						String pdfMainName = null;
						if(beginPoint >= 0){							
							pdfMainName = lineStr.substring(beginPoint + 1, pdfInd);							
						}
						if(pdfMainName != null){					       					    	 
					    	 lineStr = lineStr.replaceAll("(?i)" + pdfMainName + ".pdf",  pdfMainName + ".pdf.tif");
						}
						if(replaceReprint && oldStr != null && newStr != null){
							lineStr = lineStr.replaceAll(oldStr,  newStr);
						}
						
                        for(Byte singleByte : lineStr.getBytes("ms950")){
						   if(singleByte != seperate1 && singleByte != seperate2)
						         fileByte.add(singleByte);
						}						   
                        fileByte.add(oneByte[0]);
					    
					}else if (lineStr.indexOf("%%eoj") >= 0) {
						applyNo = null;
						policyNo = null;
						reprint = null;
						applyNo1 = null;
						policyNo1 = null;
						reprint1 = null;
						applyNo2 = null;
						policyNo2 = null;
						reprint2 = null;
						
						
						replaceReprint = false;
						oldStr = null;						
						newStr = null;
						fos = new FileOutputStream(new File(destFolder,
								oriFile.getName() + "_" + preAppNo), false);
						byte[] filebytes = new byte[fileByte.size()];
						int j = 0;
						for (Byte singleByte : fileByte) {
							filebytes[j] = singleByte;
							j++;
						}
						fos.write(filebytes);
						fos.write(seperate2);
						fos.flush();
						fos.close();
						fileByte = new ArrayList<Byte>();
					}

					lineByte = new ArrayList<Byte>();
				}
				// System.out.println(oneByte[0] + "|" + (seperate1 ==
				// oneByte[0]) + "|" + (seperate2 == oneByte[0]) );
			}
			if (fileByte != null && fileByte.size() > 200) {				
				fos = new FileOutputStream(new File(destFolder,
						oriFile.getName() + "_" + preAppNo), false);
				byte[] filebytes = new byte[fileByte.size()];
				int j = 0;
				for (Byte singleByte : fileByte) {
					filebytes[j] = singleByte;
					j++;
				}
				fos.write(filebytes);
				if(lineStr.indexOf("%%eoj") < 0){
					fos.write("%%eoj\r\n".getBytes());
				}
				fos.flush();
				fos.close();
			}
		} catch (IOException e) {
			logger.error("", e);
			throw e;
		} finally {
			if (bis != null)
				bis.close();
			if (fis != null)
				fis.close();

		}

	}
	
	public static synchronized void groupSplit(File oriFile, File destFolder)
			throws IOException {
		if (!destFolder.exists())
			destFolder.mkdirs();
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(oriFile);
			bis = new BufferedInputStream(fis);
			byte[] oneByte = new byte[1];
			List<Byte> fileByte = new ArrayList<Byte>();
			List<Byte> lineByte = new ArrayList<Byte>();
			char seperate1 = '\r'; //0D
			char seperate2 = '\n'; //0A
			String policyNo = null;
			String reprint = null;
			FileOutputStream fos = null;
			String lineStr = null;
			String reprintStr = null;
			String areaId = null;
			while (bis.read(oneByte) > 0) {	
				
				if(fileByte.size() == 0 && lineByte.size() == 0 && (seperate2 == oneByte[0] || seperate1 == oneByte[0])){
					//不能以0D0A開頭
				}else{				
				    lineByte.add(oneByte[0]);
				}
				if ((seperate1 == oneByte[0]) || (seperate2 == oneByte[0])) {
					byte[] linebytes = new byte[lineByte.size()];
					int i = 0;
					for (Byte singleByte : lineByte) {
						linebytes[i] = singleByte;
						i++;
					}

					lineStr = new String(linebytes, "ms950").trim();
					if(lineStr.startsWith("!$")){ //第一行
						String tempLine = lineStr.replaceAll("保單", "");
						String[] splitStr = tempLine.split("_");
						if(splitStr.length > 1)
							reprintStr = splitStr[1];
						else
							reprintStr = null;
						
					}
					//不是info
					if(lineStr.indexOf("info") < 0 && lineStr.toLowerCase().indexOf(".tif") < 0){						
						//009A|z20...... c:\kotai\image\test|1 此行無法處理 
						int index = lineStr.toLowerCase().indexOf("c:\\kotai\\image\\test|1");
						//如果是來自測試資料夾的tif，要加上/test
					    fileByte.addAll(lineByte);						      
					}
					if(lineStr.indexOf("|0|016A|X10|") > 0 && oriFile.getName().toLowerCase().indexOf("_policy") > 0){
						String areaNm = lineStr.substring(lineStr.indexOf("|0|016A|X10|") + "|0|016A|X10|".length()).trim();
						if(areaId != null)
						    areaMap.put(areaId, areaNm);
						//System.out.println(policyNo + "|" + areaId + "|" + areaNm);
					}
					if (lineStr.indexOf("info") >= 0) {
						//info|G300053505103|00| 簽收單 
						//info|G300053505103|00|XXXXXXXXXX||||BB47E01||0|06|0| 保單						
						String[] splits = lineStr.split("\\|");
						
						policyNo = splits[1];						 
						reprint = splits[2];
						if(oriFile.getName().toLowerCase().indexOf("_policy") > 0)
						   areaId = splits[7].trim();
						//第一行補印次數與info行不相同時，以第一行為主
						if(reprintStr != null && !reprintStr.equals(reprint)){
							reprint = reprintStr;
							lineStr = lineStr.substring(0, 5 + policyNo.length() + 1) + reprintStr + lineStr.substring(5 + policyNo.length() + 1 + reprint.length());
							for(Byte singleByte : lineStr.getBytes("ms950")){
							   if(singleByte != seperate1 && singleByte != seperate2)
						          fileByte.add(singleByte);
						   }						   						
						}else{
						    fileByte.addAll(lineByte);
						}
					}else if (lineStr.toLowerCase().indexOf(".tif") >= 0){
						//看見以下兩種格式
						//D:\ftpusr\BG\IMAGE_BK\2014-10-03\ 
						//..\IMAGE_BK\2014-10-03\
						lineStr = lineStr.replaceAll("(?i)\\\\image\\\\", "\\\\group\\\\image\\\\");
						lineStr = lineStr.replaceAll("(?i)\\\\law\\\\", "\\\\group\\\\law\\\\");
						lineStr = lineStr.replaceAll("(?i)D:\\\\ftpusr\\\\BG\\\\IMAGE_BK\\\\[2-9][0-2][0-9][0-9]\\-[0-1][0-9]\\-[0-3][0-9]", "..\\\\group\\\\image");
					    lineStr = lineStr.replaceAll("(?i)\\\\IMAGE_BK\\\\[2-9][0-2][0-9][0-9]\\-[0-1][0-9]\\-[0-3][0-9]", "..\\\\group\\\\image");
					    
					    
					    if(lineStr.toUpperCase().indexOf("|Z10|") > 0 && oriFile.getName().toLowerCase().indexOf("_sign") > 0){
					    	lineStr = lineStr.replaceAll("(?i)\\|Z10\\|", "\\|Z10\\|..\\\\group\\\\image\\\\");
					    }

                        for(Byte singleByte : lineStr.getBytes("ms950")){
						   if(singleByte != seperate1 && singleByte != seperate2)
						         fileByte.add(singleByte);
						}						   
                        fileByte.add(oneByte[0]);
					    
					}else if (lineStr.indexOf("%%eoj") >= 0) {
						//保單結尾符號
						areaId = null;
						String fileNm = oriFile.getName();
						if(fileNm.toLowerCase().endsWith(".txt"))
							fileNm = fileNm.substring(0, fileNm.length() - 4);
						fos = new FileOutputStream(new File(destFolder,
								fileNm + "_" + policyNo + "_" + reprint), true);
						byte[] filebytes = new byte[fileByte.size()];
						int j = 0;
						for (Byte singleByte : fileByte) {
							filebytes[j] = singleByte;
							j++;
						}
						fos.write(filebytes);
						fos.write(seperate2);
						fos.flush();
						fos.close();
						fileByte = new ArrayList<Byte>();
					}

					lineByte = new ArrayList<Byte>();
				}
				// System.out.println(oneByte[0] + "|" + (seperate1 ==
				// oneByte[0]) + "|" + (seperate2 == oneByte[0]) );
			}
			if (fileByte != null && fileByte.size() > 200) {				
				String fileNm = oriFile.getName();
				if(fileNm.toLowerCase().endsWith(".txt"))
					fileNm = fileNm.substring(0, fileNm.length() - 4);
				fos = new FileOutputStream(new File(destFolder,
						fileNm + "_" + policyNo + "_" + reprint), true);
				byte[] filebytes = new byte[fileByte.size()];
				int j = 0;
				for (Byte singleByte : fileByte) {
					filebytes[j] = singleByte;
					j++;
				}
				fos.write(filebytes);
				if(lineStr.indexOf("%%eoj") < 0){
					fos.write("%%eoj\r\n".getBytes());
				}
				fos.flush();
				fos.close();
			}
		} catch (IOException e) {
			logger.error("", e);
			throw e;
		} finally {
			if (bis != null)
				bis.close();
			if (fis != null)
				fis.close();

		}

	}

	public static void main(String[] args) throws IOException {
		File[] files = new File("D:\\tmp\\").listFiles();
		for(File file : files)
			if(file.isFile())
			   SplitFile.splitCathay(file, new File("d:\\tmp\\test\\"));
		//File [] files = new File("C:\\tmp\\test").listFiles();
		//for(File file : files){
			//System.out.println(file.getName());
		//}
		//mergeFile(new File("C:\\tmp\\test"), "test.txt", files);
        
		/*
		String test1 = "2014-05-19,01,9097036427,00,AA74922099,NORM,,,,                    ,Y,,2014-05-19,100,,,2,96,0,2,   0,1,100,01_20140519_AA74922099_9097042150,,01B1405190003001,";
		String split [] = test1.split(",");
		System.out.println(split.length);
		for(String sing : split){
			System.out.println(sing);
		}
		*/
	}

	public static void mergeFile(File tmpFolder, String newBatchName,
			File[] inputFiles) throws IOException {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		for (File inputFile : inputFiles){
			// 開始進行合併
			try {
				byte[] bufferedB = new byte[2048];
				fos = new FileOutputStream(new File(tmpFolder, newBatchName), true);
				bos = new BufferedOutputStream(fos);
				fis = new FileInputStream(inputFile);
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
				er.setMessageBody("exception happen:" + e.getMessage());
				er.setTitle("exception happened");
				((VoService) Constant.getContext().getBean("voServiceProxy"))
						.save(er);
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

	}

	public static HashMap<String, String> getAreaMap() {
		return areaMap;
	}

	public static void setAreaMap(HashMap<String, String> areaMap) {
		SplitFile.areaMap = areaMap;
	}
	

}
