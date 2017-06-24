package com.salmat.pas.conf;

import java.io.File;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Map;

import com.salmat.util.FileEncFactory;
import com.salmat.util.PwdDES;


public class Constant {
	    //
        private static final String pdfPwd = "cathayFxdmsPreview";
		//需刪除的冗餘字串
		private static String redundantWordList;		
		private static String[] redundantList;

		private static String bufferedPath;  		//local buffered path

		private static String backupFolder; 		//local backup file folder

		private static String packFilesNo = "100"; 		//numbers of file to afp
        private static String privateKey;
        private static String publicKey;
        private static PublicKey pubKey;
        private static PrivateKey priKey;
        
        public static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");        
        public static SimpleDateFormat yyMMdd = new SimpleDateFormat("yyMMdd");
        public static SimpleDateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
        public static SimpleDateFormat slashedyyyyMMdd = new SimpleDateFormat("yyyy/MM/dd");
        public static SimpleDateFormat yyyyMMddHHmm = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        private static Map<String, Double> weightMap = null;
        public static final int parcelWeight = 2000; 
        
                
        private static String uatOrProd;        
		

		public File getErrorTxtFile(){
        	File errorTxt  = new File(bufferedPath + "errorTxt/");
        	if(!errorTxt.exists())
        		errorTxt.mkdirs();
        	return errorTxt;
        }
        

		//從遠端移過來的保單文字檔儲放的地方
		public String getBufferedTxtPath(){
			return bufferedPath + "bufferdTxt/";
		}
		
		//保單文字檔裡影像檔不全的保單文字檔儲放的目錄
		public String getUnCompletedTxtPath(){
			return bufferedPath + "unCompletedTxt/";
		}
		
		//保單文字檔裡影像檔錯誤的保單文字檔儲放的目錄
		public String getErrImgTxtPath(){
			String errImgTxt = bufferedPath + "errImgTxt/";
			File errImgTxtFolder = new File(errImgTxt);
			if(!errImgTxtFolder.exists()){
				errImgTxtFolder.mkdirs();
			}
			return bufferedPath + "errImgTxt/";
		}

		
		//保單文字檔Batch件儲放的地方
		public String getBatchTxtPath(){
			return bufferedPath + "batchTxt/";
		}
		
		//保單文字檔Online件儲放的地方
		public String getOnlineTxtPath(){
			return bufferedPath + "onlineTxt/";
		}
		
		//用來傳檔的folder
		public String getForTransferFolder(){
			File forTransfer = new File(bufferedPath + "forTransfer/");
			if(!forTransfer.exists() ){
				forTransfer.mkdirs();
			}
			
			return bufferedPath + "forTransfer/";
		}
		
		public  String getRedundantWordList() {
			return redundantWordList;
		}
		
		public static String[] getRedundantList() {
			return redundantList;
		}

		public  void setRedundantWordList(String redundantWordList) {
			if (redundantWordList != null) {
				String[] stringArr = redundantWordList.split(",");
				if (stringArr != null && stringArr.length > 0)
					this.redundantList = stringArr;
			}
			this.redundantWordList = redundantWordList;

		}
		public String getLastRnbFile(){
			return bufferedPath + "lastRnb.txt";
		}
		
		public  String getBufferedPath() {
			return bufferedPath;
		}
		public  void setBufferedPath(String bufferedPath) {
			Constant.bufferedPath = bufferedPath;
		}
		public  String getBackupFolder() {
			return backupFolder;
		}
		public  void setBackupFolder(String backupFolder) {
			Constant.backupFolder = backupFolder;
		}
		public String getPackFilesNo() {
			return packFilesNo;
		}
		public void setPackFilesNo(String packFilesNo) {
			this.packFilesNo = packFilesNo;
		}
		public Integer getPackNo(){
			if(packFilesNo != null)
				return new Integer(packFilesNo);
			else
				return null;
		}
		
		
		public String getUnZipFolder() {
			File unZipFolder = new File(bufferedPath + "unZip/");
			if(!unZipFolder.exists()){
				unZipFolder.mkdirs();
			}
			return bufferedPath + "unZip/";
		}
		

		public String getListFolder() {
			File listFolder = new File(bufferedPath + "List/");
			if(!listFolder.exists()){
				listFolder.mkdirs();
			}
			return bufferedPath + "List/";
		}

        public File getDifficultFontFoler(){
        	File testFolder = new File(bufferedPath + "DifficultFont/");
			if(!testFolder.exists()){
				testFolder.mkdirs();
			}
			return testFolder;
        }
        
        public String getComboFilesPath(){
        	File testFolder = new File(bufferedPath + "comboFiles/");
			if(!testFolder.exists()){
				testFolder.mkdirs();
			}
        	return bufferedPath + "comboFiles/";
        }
		
		
		public static String getSystemIp(){
			String ip = null;
			try{
				  InetAddress ownIP = InetAddress.getLocalHost();
				  ip = ownIP.getHostAddress();
		    }catch (Exception e){
				  System.out.println("Exception caught ="+e.getMessage());
			}
		    return ip;
		}
		
		public String getUatOrProd() {
			return uatOrProd;
		}

		public void setUatOrProd(String uatOrProd) {
			Constant.uatOrProd = uatOrProd;
		}

		public static PrivateKey getPrivateKey() {
			if(priKey == null)
				priKey = FileEncFactory.loadPrivateKey(privateKey);
			return priKey;
		}

		public void setPrivateKey(String privateKey) {
			Constant.privateKey = privateKey;
		}

		public static PublicKey getPublicKey() {
			if(pubKey == null)
				pubKey = FileEncFactory.loadPublicKey(publicKey);
			return pubKey; 
		}

		public void setPublicKey(String publicKey) {
			Constant.publicKey = publicKey;
		}

		public static String getPdfpwd() {
			return pdfPwd;
		}

		public static Map<String, Double> getWeightMap() {
			return weightMap;
		}

		public static void setWeightMap(Map<String, Double> weightMap) {
			Constant.weightMap = weightMap;
		}
		
		

}
