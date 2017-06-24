package com.fxdms.cathy.conf;

import java.io.File;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;

import org.springframework.context.ApplicationContext;

import com.fxdms.util.FileEncFactory;
import com.fxdms.util.PwdDES;

public class Constant {
	    
	    
		//需刪除的冗餘字串
		private static String redundantWordList;
		private static String[] redundantList;

		private static String bufferedPath;  		//local buffered path

		private static String backupFolder; 		//local backup file folder

		private static String packFilesNo = "100"; 		//numbers of file to afp
		private static String waitMinutes;	    //zip wait time

		private static String ftpServerIP; 		//combo ftp server IP
		private static String ftpUser; 		//combo ftp user
		private static String ftpPassword;		//combo ftp password
		private static String ftpRnb3580; //combo ftp server放RNB3580的目錄
		
		
		//放gpg執行檔的gpg dir
		private static String gpgDir;
		//PReS產生出來的afp的路徑
		private static String afpPath;
		//StreamEdp目錄
		private static String streamEdpDir;
		//temp Pdf Dir
		private static String tempPdfDir;
		private static String zipFolder;
		private static String outPdfPath;

		private static String sipUser;
		private static String sipPwd;
		private static String sipServer;
		private static String sipSharedFolder;  //sipserver登入後的根目錄
		private static String totalListFolder;
		private static String encDBPwd;  //加密後的密碼
		private static String dbUser;  //加密後的密碼
		private static String driverUrl;  //加密後的密碼
		private static String dbPwd;
		
		private static String prefix1;  // 批註檔
		private static String prefix2; // 條款影像檔
		private static String imgPath;  //Pres放本地端影像的地方
		private static String txtPath;   //Pres放保單文字檔的地方
		private static String rnb3580Folder; //rnb3580檔案放的地方  
		
		private static String imgServerUser = "fubonadmin"; //寰影影像檔伺服器ftp user
		private static String imgServerPwd = "p@ssw0rd"; //寰影影像檔伺服器 ftp pwd
		private static String imgServer = "10.113.139.69"; //寰影影像檔伺服器 
		private static String txtFileFolder; //寰影影像檔放置保單文字檔的相對目錄
		private static String rootPath = "/";   //寰影伺服器ftp登入後的根目錄
		private static String comboFilesPath;
		

        private static String mailHost;    //smtp server host
        private static String mailUserNm;  //smtp server user name
        private static String mailPwd;    //smtp server password
        
        private static String noTxtWaitTime; //收到3580後，多久沒收到保單文字檔時要通知(小時)
        private static String hyperLink; //預覽pdf所儲放目錄的超連結
        private static String privateKey;
        private static String publicKey;
        private static PublicKey pubKey;
        private static PrivateKey priKey;
        
        private static String fontFilePath = "C:/Temp/fonts/FUBONLIFEKAIU.TTF";
        public static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");        
        public static SimpleDateFormat yyyyMMdd_HHmmSS = new SimpleDateFormat("yyyyMMdd_HHmmSS");
        public static SimpleDateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
        public static SimpleDateFormat yyyy_MM_ddHHMM = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        public static SimpleDateFormat slashyyyyMMdd = new SimpleDateFormat("yyyy/MM/dd");
        private static ApplicationContext context = null;
        
                
        public static ApplicationContext getContext() {
			return context;
		}

		public static void setContext(ApplicationContext context) {
			Constant.context = context;
		}

		private static String uatOrProd;        
		
		public String getFontFilePath() {
			return fontFilePath;
		}

		public void setFontFilePath(String fontFilePath) {
			Constant.fontFilePath = fontFilePath;
		}

		public File getErrorTxtFile(){
        	File errorTxt  = new File(bufferedPath + "errorTxt/");
        	if(!errorTxt.exists())
        		errorTxt.mkdirs();
        	return errorTxt;
        }
        
		public  String getFtpRnb3580() {
			return ftpRnb3580;
		}

		public  void setFtpRnb3580(String ftpRnb3580) {
			Constant.ftpRnb3580 = ftpRnb3580;
		}

		public String getMailHost() {
			return mailHost;
		}

		public void setMailHost(String mailHost) {
			Constant.mailHost = mailHost;
		}

		public String getMailUserNm() {
			return mailUserNm;
		}

		public void setMailUserNm(String mailUserNm) {
			Constant.mailUserNm = mailUserNm;
		}

		public String getMailPwd() {
			return mailPwd;
		}

		public void setMailPwd(String mailPwd) {
			Constant.mailPwd = mailPwd;
		}

		public String getImgServerUser() {
			return imgServerUser;
		}

		public void setImgServerUser(String imgServerUser) {
			Constant.imgServerUser = imgServerUser;
		}

		public String getImgServerPwd() {
			return imgServerPwd;
		}

		public void setImgServerPwd(String imgServerPwd) {
			//Constant.imgServerPwd = imgServerPwd;
			Constant.imgServerPwd = PwdDES.getDecPwd(imgServerPwd);
		}

		public String getImgServer() {
			return imgServer;
		}

		public void setImgServer(String imgServer) {
			Constant.imgServer = imgServer;
		}

		public String getRnb3580Folder() {
			File localRnb3580 = new File(rnb3580Folder);
			if(!localRnb3580.exists())
				localRnb3580.mkdirs();
			return rnb3580Folder;
		}

		public void setRnb3580Folder(String rnb3580Folder) {
			Constant.rnb3580Folder = rnb3580Folder;
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
		
		//本地端相關的影像檔儲放的位置
		public String getImgPath(){
			return imgPath;
		}
		public void setImgPath(String imgPath){
			Constant.imgPath = imgPath;
		}
		
		//本地端merge後的保單文字檔儲放的位置
		public String getTxtPath(){
			return txtPath;
		}
		public void setTxtPath(String txtPath){
			Constant.txtPath = txtPath;
		}
		
		public  String getTxtFileFolder() {
			return txtFileFolder;
		}
		public  void setTxtFileFolder(String txtFileFolder) {
			Constant.txtFileFolder = txtFileFolder;
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
		public  String getRootPath() {
			return rootPath;
		}
		public  void setRootPath(String rootPath) {
			Constant.rootPath = rootPath;
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
		
		public String getWaitMinutes() {
			return waitMinutes;
		}
		public void setWaitMinutes(String waitMinutes) {
			this.waitMinutes = waitMinutes;
		}
		public String getFtpServerIP() {
			return ftpServerIP;
		}
		public void setFtpServerIP(String ftpServerIP) {
			this.ftpServerIP = ftpServerIP;
		}
		public String getFtpUser() {
			return ftpUser;
		}
		public void setFtpUser(String ftpUser) {
			this.ftpUser = ftpUser;
		}
		public String getFtpPassword() {
			return ftpPassword;
		}
		public void setFtpPassword(String ftpPassword) {
			
			//this.ftpPassword = ftpPassword;
			this.ftpPassword = PwdDES.getDecPwd(ftpPassword);
		}
		public String getGpgDir() {
			return gpgDir;
		}
		public void setGpgDir(String gpgDir) {
			this.gpgDir = gpgDir;
		}
		public String getSipUser() {
			return sipUser;
		}
		public void setSipUser(String sipUser) {
			this.sipUser = sipUser;
		}
		public String getSipPwd() {
			return sipPwd;
		}
		public void setSipPwd(String sipPwd) {
			//this.sipPwd = sipPwd;
			this.sipPwd = PwdDES.getDecPwd(sipPwd);
		}
		public String getSipServer() {
			return sipServer;
		}
		public void setSipServer(String sipServer) {
			this.sipServer = sipServer;
		}
		public String getSipSharedFolder() {
			return sipSharedFolder;
		}
		public void setSipSharedFolder(String sipSharedFolder) {
			this.sipSharedFolder = sipSharedFolder;
		}
		public String getTotalListFolder() {
			return totalListFolder;
		}
		public void setTotalListFolder(String totalListFolder) {
			this.totalListFolder = totalListFolder;
		}
		public String getEncDBPwd() {
			return encDBPwd;
		}
		public void setEncDBPwd(String encDBPwd) {
			this.encDBPwd = encDBPwd;
			this.dbPwd = PwdDES.getDecPwd(encDBPwd);
		}
		public static String getDbPwd() {
			return dbPwd;
		}
		public void setDbPwd(String dbPwd) {
			this.dbPwd = dbPwd;
			this.encDBPwd = PwdDES.getEncPwd(dbPwd);
		}
		public  String getPrefix1() {
			return prefix1;
		}

		public  String getPrefix2() {
			return prefix2;
		}

		public  void setPrefix1(String prefix1) {
			Constant.prefix1 = prefix1;
		}

		public  void setPrefix2(String prefix2) {
			Constant.prefix2 = prefix2;
		}
		
		public String getAfpPath() {
			return afpPath;
		}

		public void setAfpPath(String afpPath) {
			Constant.afpPath = afpPath;
		}
		public String getStreamEdpDir() {
			return streamEdpDir;
		}

		public  void setStreamEdpDir(String streamEdpDir) {
			Constant.streamEdpDir = streamEdpDir;
		}
		public String getTempPdfDir() {
			return tempPdfDir;
		}

		public void setTempPdfDir(String tempPdfDir) {
			Constant.tempPdfDir = tempPdfDir;
		}

		public String getOutPdfPath() {
			return outPdfPath;
		}

		public void setOutPdfPath(String outPdfPath) {
			Constant.outPdfPath = outPdfPath;
		}

		public String getZipFolder() {
			return zipFolder;
		}

		public void setZipFolder(String zipFolder) {
			Constant.zipFolder = zipFolder;
		}
		
		public String getUnZipFolder() {
			File unZipFolder = new File(bufferedPath + "unZip/");
			if(!unZipFolder.exists()){
				unZipFolder.mkdirs();
			}
			return bufferedPath + "unZip/";
		}
		
		public String getNoTxtWaitTime() {
			return noTxtWaitTime;
		}

		public void setNoTxtWaitTime(String noTxtWaitTime) {
			Constant.noTxtWaitTime = noTxtWaitTime;
		}
		
		public String getRemoteListFolder() {
			return rootPath + "List/";
		}

		public String getListFolder() {
			File listFolder = new File(bufferedPath + "List/");
			if(!listFolder.exists()){
				listFolder.mkdirs();
			}
			return bufferedPath + "List/";
		}
		/*
		-          D:\fubon\schlog                        : scheduler log (text format)

		-          D:\fubon\report                        :report files after pres process(text format)

		-          D:\fubon\work_tmp                  : data area for after pres process(text format)

		-          D:\fubon\imagebuffer              : images for policybook(TIF files)

		-          D:\fubon\preview\afpfiles        : AFP files for after PReS process(afp files & CSV format)
		*/
		
		public File getPresSchlogFolder(){
			File testFolder = new File("D:/fubon/schlog/");
			if(!testFolder.exists()){
				testFolder.mkdirs();
			}
			return testFolder;
		}
		public File getPresReportFolder(){
			File testFolder = new File("D:\\fubon\\eport\\");
			if(!testFolder.exists()){
				testFolder.mkdirs();
			}
			return testFolder;
		}
		public File getPresWork_tmpFolder(){
			File testFolder = new File("D:\\fubon\\work_tmp\\");
			if(!testFolder.exists()){
				testFolder.mkdirs();
			}
			return testFolder;
		}
		public File getPresImagebufferFolder(){
			File testFolder = new File("D:\\fubon\\imagebuffer\\");
			if(!testFolder.exists()){
				testFolder.mkdirs();
			}
			return testFolder;
		}
		public File getPresAfpFilesFolder(){
			File testFolder = new File("D:\\fubon\\preview\\afpfiles\\");
			if(!testFolder.exists()){
				testFolder.mkdirs();
			}
			return testFolder;
		}
		
		public String getDbUser() {
			return dbUser;
		}

		public void setDbUser(String dbUser) {
			Constant.dbUser = dbUser;
		}

		public String getDriverUrl() {
			return driverUrl;
		}

		public void setDriverUrl(String driverUrl) {
			Constant.driverUrl = driverUrl;
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
		
        //pdf檔放置的地方的根目錄
        public String getHyperLink() {
        	if(hyperLink == null){
    			try{
    				  InetAddress ownHost = InetAddress.getLocalHost();
    				  //http://10.42.71.89/docs/pdf/
    				  hyperLink = "http://" + ownHost.getCanonicalHostName() + "/docs/pdf/";
    		    }catch (Exception e){
    				  System.out.println("Exception caught ="+e.getMessage());
    			}	
        	}
			return hyperLink;
		}

		public void setHyperLink(String hyperLink) {
			Constant.hyperLink = hyperLink;
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
		
		


}
