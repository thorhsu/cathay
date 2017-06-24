package com.fxdms.cathy.bo;

import java.io.Serializable;
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
	private static String afpToPdfPath;
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
	private static Integer fxFilesKeepDays;
	private static String fileServerPdfFolder;
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
	private static String fsafpToPdfPath;
	private static Integer maxCenter;
	
	
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
	public static String getAfpToPdfPath() {
		return afpToPdfPath;
	}
	public void setAfpToPdfPath(String afpToPdfPath) {
		Properties.afpToPdfPath = afpToPdfPath;
	}
	public static String getFsafpToPdfPath() {
		return fsafpToPdfPath;
	}
	public void setFsafpToPdfPath(String fsafpToPdfPath) {
		Properties.fsafpToPdfPath = fsafpToPdfPath;
	}
	public static Integer getFxFilesKeepDays() {
		return fxFilesKeepDays;
	}
	public void setFxFilesKeepDays(Integer fxFilesKeepDays) {
		Properties.fxFilesKeepDays = fxFilesKeepDays;
	}
	public static String getFileServerPdfFolder() {
		return fileServerPdfFolder;
	}
	public void setFileServerPdfFolder(String fileServerPdfFolder) {
		Properties.fileServerPdfFolder = fileServerPdfFolder;
	}
	
}
