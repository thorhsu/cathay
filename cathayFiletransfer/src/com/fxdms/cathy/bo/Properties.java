package com.fxdms.cathy.bo;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Properties implements Serializable {	
	
	private static Integer id;
	private static String imgServer;
	private static String imgServerUser;
	private static String imgServerPwd;
	private static String imgServerDomain;
	private static String imgPath;
	private static String lawPath;
	private static String testLawPath;
	private static String testImgPath;
	private static String reqPolicyPath;
	private static String policyTxtServer;
	private static String policyTxtServerUser;
	private static String policyTxtServerPwd;
	private static String policyTxtServerDomain;
	private static String policyTxtPath;
	
	private static String okPath;
	private static String localOKPath;
	private static String localPolicyTxtPath;
	private static String localTxtTmpPath;
	private static String afpToPdfPath;
	private static String fxdmsIP;
	private static String fxdmsUser;
	private static String fxdmsPwd;
	private static String fxdmsUploadPath;
	private static String fxdmsDownloadPath;
	private static String localLawPath;
	private static String localReqPolicyPath;
	private static String localPolicyOnlinePath;
	private static String localTestPolicyPath;
	private static String localReturnPath;
	private static String localReturnPdf;
	private static String localReturnForCheck;
	private static String localTestImgPath;
	private static String localImgPath;
	private static String feedbackFolder;
	private static String imgPostProcessedPath;
	private static String checkedOkPath;
	private static String receiptOkPath;	
	private static String imgUncompletePath;
	private static String imgSizeErrorPath;
	private static String imgResolutionErrPath;
	private static String difficultFontPath;
	private static String backupFolder;
	private static String errorFileNmPath;
	private static String presPath;
	private static String afpPath;
	private static String zipTmpPath;
	private static String gpgExePath;
	private static Integer imgDpi;
	private static Integer maxImgWidth;
	private static Integer minImgWidth;
	private static Integer maxImgHeight;
	private static Integer minImgHeight;
	private static Integer dbKeepDays;
	private static String mailHost;
	private static String mailUserNm;
	private static String mailPwd;
	private static String emails;
	private static Integer freeSpace;
	private static Integer filesKeepDays;
	private static Integer dbModifiedDays;
	private static String fsafpToPdfPath;
	private static Integer maxCenter;
	private static boolean initialnized;
	private static String groupInFolder;
	private static String groupOutFolder;
	private static String remoteGroupImgFolder;
	private static String remoteGroupLawFolder;
	private static String gpBackupFolder;
	private static String gpImgServer;
	private static String gpImgServerUser;
	private static String gpImgServerPwd;
	private static String gpImgServerDomain;
	private static String holdFiles; //保留下來不進行作業的檔案名稱
	private static String forceNormFiles; //強迫要轉為新契約的檔案名稱
	private static String backupFolders;
	private static String backupFoldersKeepDays;
	
	public static String getFsafpToPdfPath() {
		return fsafpToPdfPath;
	}
	public void setFsafpToPdfPath(String fsafpToPdfPath) {
		Properties.fsafpToPdfPath = fsafpToPdfPath;
	}
	public static Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		Properties.id = id;
	}
	public static String getImgServer() {
		return imgServer;
	}
	public void setImgServer(String imgServer) {
		Properties.imgServer = imgServer;
	}
	public static String getImgServerUser() {
		return imgServerUser;
	}
	public void setImgServerUser(String imgServerUser) {
		Properties.imgServerUser = imgServerUser;
	}
	public  static String getImgServerPwd() {
		return imgServerPwd;
	}
	public void setImgServerPwd(String imgServerPwd) {
		Properties.imgServerPwd = imgServerPwd;
	}
	public static String getImgServerDomain() {
		return imgServerDomain;
	}
	public void setImgServerDomain(String imgServerDomain) {
		Properties.imgServerDomain = imgServerDomain;
	}
	public static String getImgPath() {
		return imgPath;
	}
	public void setImgPath(String imgPath) {
		Properties.imgPath = imgPath;
	}
	public static String getLawPath() {
		return lawPath;
	}
	public void setLawPath(String lawPath) {
		Properties.lawPath = lawPath;
	}
	public static String getReqPolicyPath() {
		return reqPolicyPath;
	}
	public void setReqPolicyPath(String reqPolicyPath) {
		Properties.reqPolicyPath = reqPolicyPath;
	}
	public static String getPolicyTxtServer() {
		return policyTxtServer;
	}
	public void setPolicyTxtServer(String policyTxtServer) {
		Properties.policyTxtServer = policyTxtServer;
	}
	public static String getPolicyTxtServerUser() {
		return policyTxtServerUser;
	}
	public void setPolicyTxtServerUser(String policyTxtServerUser) {
		Properties.policyTxtServerUser = policyTxtServerUser;
	}
	public static String getPolicyTxtServerPwd() {
		return policyTxtServerPwd;
	}
	public void setPolicyTxtServerPwd(String policyTxtServerPwd) {
		Properties.policyTxtServerPwd = policyTxtServerPwd;
	}
	public static String getPolicyTxtServerDomain() {
		return policyTxtServerDomain;
	}
	public void setPolicyTxtServerDomain(String policyTxtServerDomain) {
		Properties.policyTxtServerDomain = policyTxtServerDomain;
	}
	public static String getPolicyTxtPath() {
		return policyTxtPath;
	}
	public void setPolicyTxtPath(String policyTxtPath) {
		Properties.policyTxtPath = policyTxtPath;
	}
	public static String getOkPath() {
		return okPath;
	}
	public void setOkPath(String okPath) {
		Properties.okPath = okPath;
	}
	public static String getLocalOKPath() {
		return localOKPath;
	}
	public void setLocalOKPath(String localOKPath) {
		Properties.localOKPath = localOKPath;
	}
	public static String getLocalPolicyTxtPath() {
		return localPolicyTxtPath;
	}
	public void setLocalPolicyTxtPath(String localPolicyTxtPath) {
		Properties.localPolicyTxtPath = localPolicyTxtPath;
	}
	public static String getFxdmsIP() {
		return fxdmsIP;
	}
	public void setFxdmsIP(String fxdmsIP) {
		Properties.fxdmsIP = fxdmsIP;
	}
	public static String getFxdmsUser() {
		return fxdmsUser;
	}
	public void setFxdmsUser(String fxdmsUser) {
		Properties.fxdmsUser = fxdmsUser;
	}
	public static String getFxdmsPwd() {
		return fxdmsPwd;
	}
	public void setFxdmsPwd(String fxdmsPwd) {
		Properties.fxdmsPwd = fxdmsPwd;
	}
	public static String getFxdmsUploadPath() {
		return fxdmsUploadPath;
	}
	public void setFxdmsUploadPath(String fxdmsUploadPath) {
		Properties.fxdmsUploadPath = fxdmsUploadPath;
	}
	public static String getFxdmsDownloadPath() {
		return fxdmsDownloadPath;
	}
	public void setFxdmsDownloadPath(String fxdmsDownloadPath) {
		Properties.fxdmsDownloadPath = fxdmsDownloadPath;
	}
	public static String getLocalLawPath() {
		return localLawPath;
	}
	public void setLocalLawPath(String localLawPath) {
		Properties.localLawPath = localLawPath;
	}
	public static String getLocalReqPolicyPath() {
		return localReqPolicyPath;
	}
	public void setLocalReqPolicyPath(String localReqPolicyPath) {
		Properties.localReqPolicyPath = localReqPolicyPath;
	}
	public static String getLocalTestImgPath() {
		return localTestImgPath;
	}
	public void setLocalTestImgPath(String localTestImgPath) {
		Properties.localTestImgPath = localTestImgPath;
	}
	public static String getLocalImgPath() {
		return localImgPath;
	}
	public void setLocalImgPath(String localImgPath) {
		Properties.localImgPath = localImgPath;
	}
	public static String getImgPostProcessedPath() {
		return imgPostProcessedPath;
	}
	public void setImgPostProcessedPath(String imgPostProcessedPath) {
		Properties.imgPostProcessedPath = imgPostProcessedPath;
	}
	public static String getCheckedOkPath() {
		return checkedOkPath;
	}
	public void setCheckedOkPath(String checkedOkPath) {
		Properties.checkedOkPath = checkedOkPath;
	}
	public static String getReceiptOkPath() {
		return receiptOkPath;
	}
	public void setReceiptOkPath(String receiptOkPath) {
		Properties.receiptOkPath = receiptOkPath;
	}
	public static String getImgUncompletePath() {
		return imgUncompletePath;
	}
	public void setImgUncompletePath(String imgUncompletePath) {
		Properties.imgUncompletePath = imgUncompletePath;
	}
	public static String getImgSizeErrorPath() {
		return imgSizeErrorPath;
	}
	public void setImgSizeErrorPath(String imgSizeErrorPath) {
		Properties.imgSizeErrorPath = imgSizeErrorPath;
	}
	public static String getImgResolutionErrPath() {
		return imgResolutionErrPath;
	}
	public void setImgResolutionErrPath(String imgResolutionErrPath) {
		Properties.imgResolutionErrPath = imgResolutionErrPath;
	}
	public static String getDifficultFontPath() {
		return difficultFontPath;
	}
	public void setDifficultFontPath(String difficultFontPath) {
		Properties.difficultFontPath = difficultFontPath;
	}
	
	
	public static String getBackupFolder() {
		return backupFolder;
	}
	public void setBackupFolder(String backupFolder) {
		Properties.backupFolder = backupFolder;
	}
	public static String getErrorFileNmPath() {
		return errorFileNmPath;
	}
	public void setErrorFileNmPath(String errorFileNmPath) {
		Properties.errorFileNmPath = errorFileNmPath;
	}
	public static String getPresPath() {
		return presPath;
	}
	public void setPresPath(String presPath) {
		Properties.presPath = presPath;
	}
	public static String getAfpPath() {
		return afpPath;
	}
	public void setAfpPath(String afpPath) {
		Properties.afpPath = afpPath;
	}
	public static String getZipTmpPath() {
		return zipTmpPath;
	}
	public void setZipTmpPath(String zipTmpPath) {
		Properties.zipTmpPath = zipTmpPath;
	}
	public static String getGpgExePath() {
		return gpgExePath;
	}
	public void setGpgExePath(String gpgExePath) {
		Properties.gpgExePath = gpgExePath;
	}
	public static Integer getImgDpi() {
		return imgDpi;
	}
	public void setImgDpi(Integer imgDpi) {
		Properties.imgDpi = imgDpi;
	}
	public static Integer getMaxImgWidth() {
		return maxImgWidth;
	}
	public void setMaxImgWidth(Integer maxImgWidth) {
		Properties.maxImgWidth = maxImgWidth;
	}
	public static Integer getMinImgWidth() {
		return minImgWidth;
	}
	public void setMinImgWidth(Integer minImgWidth) {
		Properties.minImgWidth = minImgWidth;
	}
	public static Integer getMaxImgHeight() {
		return maxImgHeight;
	}
	public void setMaxImgHeight(Integer maxImgHeight) {
		Properties.maxImgHeight = maxImgHeight;
	}
	public static Integer getMinImgHeight() {
		return minImgHeight;
	}
	public void setMinImgHeight(Integer minImgHeight) {
		Properties.minImgHeight = minImgHeight;
	}
	public static Integer getDbKeepDays() {
		return dbKeepDays;
	}
	public void setDbKeepDays(Integer dbKeepDays) {
		Properties.dbKeepDays = dbKeepDays;
	}
	public static String getMailHost() {
		return mailHost;
	}
	public void setMailHost(String mailHost) {
		Properties.mailHost = mailHost;
	}
	public static String getMailUserNm() {
		return mailUserNm;
	}
	public void setMailUserNm(String mailUserNm) {
		Properties.mailUserNm = mailUserNm;
	}
	public static String getMailPwd() {
		return mailPwd;
	}
	public void setMailPwd(String mailPwd) {
		Properties.mailPwd = mailPwd;
	}
	public static String getEmails() {
		return emails;
	}
	public void setEmails(String emails) {
		Properties.emails = emails;
	}
	public static Integer getFreeSpace() {
		return freeSpace;
	}
	public void setFreeSpace(Integer freeSpace) {
		Properties.freeSpace = freeSpace;
	}
	public static Integer getFilesKeepDays() {
		return filesKeepDays;
	}
	public void setFilesKeepDays(Integer filesKeepDays) {
		Properties.filesKeepDays = filesKeepDays;
	}
	
	public static List<String> getEmailList(){
		if(Properties.emails != null){
			String[] emails = Properties.emails.split(";");
			return Arrays.asList(emails);
		}else{
			return null;
		}
	}
	public static String getLocalTxtTmpPath() {
		return localTxtTmpPath;
	}	
	public void setLocalTxtTmpPath(String localTxtTmpPath) {
		Properties.localTxtTmpPath = localTxtTmpPath;
	}
	
	
	public static String getLocalPolicyOnlinePath() {
		return localPolicyOnlinePath;
	}	
	public void setLocalPolicyOnlinePath(String localPolicyOnlinePath) {
		Properties.localPolicyOnlinePath = localPolicyOnlinePath;
	}
	
	
	public static String getLocalTestPolicyPath() {
		return localTestPolicyPath;
	}	
	public void setLocalTestPolicyPath(String localTestPolicyPath) {
		Properties.localTestPolicyPath = localTestPolicyPath;
	}
	
	
	public static String getLocalReturnPath() {
		return localReturnPath;
	}
	public void setLocalReturnPath(String localReturnPath) {
		Properties.localReturnPath = localReturnPath;
	}
	
	public static String getLocalReturnPdf() {
		return localReturnPdf;
	}
	public void setLocalReturnPdf(String localReturnPdf) {
		Properties.localReturnPdf = localReturnPdf;
	}
	public static String getLocalReturnForCheck() {
		return localReturnForCheck;
	}
	public void setLocalReturnForCheck(String localReturnForCheck) {
		Properties.localReturnForCheck = localReturnForCheck;
	}
	public static Integer getMaxCenter() {
		return maxCenter;
	}
	public void setMaxCenter(Integer maxCenter) {
		Properties.maxCenter = maxCenter;
	}
	public static String getFeedbackFolder() {
		return feedbackFolder;
	}
	public void setFeedbackFolder(String feedbackFolder) {
		Properties.feedbackFolder = feedbackFolder;
	}
	public static String getTestLawPath() {
		return testLawPath;
	}
	public void setTestLawPath(String testLawPath) {
		Properties.testLawPath = testLawPath;
	}
	public static String getTestImgPath() {
		return testImgPath;
	}
	public void setTestImgPath(String testImgPath) {
		Properties.testImgPath = testImgPath;
	}
	public static String getAfpToPdfPath() {
		return afpToPdfPath;
	}
	public void setAfpToPdfPath(String afpToPdfPath) {
		Properties.afpToPdfPath = afpToPdfPath;
	}
	public static Integer getDbModifiedDays() {
		return dbModifiedDays;
	}
	public void setDbModifiedDays(Integer dbModifiedDays) {
		Properties.dbModifiedDays = dbModifiedDays;
	}
	public static boolean isInitialnized() {
		return initialnized;
	}
	public static void setInitialnized(boolean initialnized) {
		Properties.initialnized = initialnized;
	}
	public static String getGroupInFolder() {
		return groupInFolder;
	}
	public void setGroupInFolder(String groupInFolder) {
		Properties.groupInFolder = groupInFolder;
	}
	public static String getGroupOutFolder() {
		return groupOutFolder;
	}
	public void setGroupOutFolder(String groupOutFolder) {
		Properties.groupOutFolder = groupOutFolder;
	}
	public static String getRemoteGroupImgFolder() {
		return remoteGroupImgFolder;
	}
	public void setRemoteGroupImgFolder(String remoteGroupImgFolder) {
		Properties.remoteGroupImgFolder = remoteGroupImgFolder;
	}
	public static String getRemoteGroupLawFolder() {
		return remoteGroupLawFolder;
	}
	public void setRemoteGroupLawFolder(String remoteGroupLawFolder) {
		Properties.remoteGroupLawFolder = remoteGroupLawFolder;
	}
	public static String getGpBackupFolder() {
		return gpBackupFolder;
	}
	public void setGpBackupFolder(String gpBackupFolder) {
		Properties.gpBackupFolder = gpBackupFolder;
	}
	public static String getGpImgServer() {
		return gpImgServer;
	}
	public void setGpImgServer(String gpImgServer) {
		Properties.gpImgServer = gpImgServer;
	}
	public static String getGpImgServerUser() {
		return gpImgServerUser;
	}
	public void setGpImgServerUser(String gpImgServerUser) {
		Properties.gpImgServerUser = gpImgServerUser;
	}
	public static String getGpImgServerPwd() {
		return gpImgServerPwd;
	}
	public void setGpImgServerPwd(String gpImgServerPwd) {
		Properties.gpImgServerPwd = gpImgServerPwd;
	}
	public static String getGpImgServerDomain() {
		return gpImgServerDomain;
	}
	public void setGpImgServerDomain(String gpImgServerDomain) {
		Properties.gpImgServerDomain = gpImgServerDomain;
	}
	public static String getHoldFiles() {
		return holdFiles;
	}
	public static String[] getHoldFileNms(){
		if(holdFiles != null && !holdFiles.trim().equals(""))
			return holdFiles.split(",");
		else 
			return null;
	}
	public void setHoldFiles(String holdFiles) {
		Properties.holdFiles = holdFiles;
	}
	public static String getForceNormFiles() {
		return forceNormFiles;
	}
	public void setForceNormFiles(String forceNormFiles) {
		Properties.forceNormFiles = forceNormFiles;
	}
	public static String[] getForceNormFileNms(){
		if(forceNormFiles != null && !forceNormFiles.trim().equals(""))
			return forceNormFiles.split(",");
		else 
			return null;
	}
	
	public static String getBackupFolders() {
		return backupFolders;
	}
	public void setBackupFolders(String backupFolders) {
		Properties.backupFolders = backupFolders;
	}
	public static String getBackupFoldersKeepDays() {
		return backupFoldersKeepDays;
	}
	public void setBackupFoldersKeepDays(String backupFoldersKeepDays) {
		Properties.backupFoldersKeepDays = backupFoldersKeepDays;
	}
	public static List<String> getBackupFoldersList(){
		List<String> folderList = new ArrayList<String>();
		if(getBackupFolders() != null){
			String[] backupFolders = getBackupFolders().split(",");
			for(String backupFolder : backupFolders){
				folderList.add(backupFolder);
			}
		}
		folderList.add("");
		return folderList;				
	}
	public static List<String> getBackupKeepDaysList(){
		List<String> keepDaysList = new ArrayList<String>();
		if(getBackupFolders() != null){
			String[] keepDays = getBackupFoldersKeepDays().split(",");
			for(String keepDay : keepDays){
				keepDaysList.add(keepDay);
			}
		}
		keepDaysList.add("");
		return keepDaysList;				
	}
	
}
