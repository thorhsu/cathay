package com.salmat.pas.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.*;

@SuppressWarnings("serial")
@Entity
@Table(name = "properties")
public class Properties implements Serializable {	
	
	@Id 
	private Integer id;
	private String imgServer;
	private String imgServerUser;
	private String imgServerPwd;
	private String imgServerDomain;
	private String imgPath;
	private String lawPath;
	private String testLawPath;
	private String testImgPath;
	private String reqPolicyPath;
	private String policyTxtServer;
	private String policyTxtServerUser;
	private String policyTxtServerPwd;
	private String policyTxtServerDomain;
	private String policyTxtPath;
	private String okPath;
	private String localOKPath;
	private String localPolicyTxtPath;
	private String localTxtTmpPath;
	private String afpToPdfPath;
	private String fxdmsIP;
	private String fxdmsUser;
	private String fxdmsPwd;
	private String fxdmsUploadPath;
	private String fxdmsDownloadPath;
	private String localLawPath;
	private String localReqPolicyPath;
	private String localTestImgPath;
	private String localImgPath;
	private String feedbackFolder;
	private String imgPostProcessedPath;
	private String checkedOkPath;
	private String receiptOkPath;
	private String imgUncompletePath;
	private String imgSizeErrorPath;
	private String imgResolutionErrPath;
	private String difficultFontPath;
	private String backupFolder;
	private String errorFileNmPath;
	private String presPath;
	private String afpPath;
	private String zipTmpPath;
	private String gpgExePath;
	private Integer imgDpi;
	private Integer maxImgWidth;
	private Integer minImgWidth;
	private Integer maxImgHeight;
	private Integer minImgHeight;
	private Integer dbKeepDays;
	private String mailHost;
	private String mailUserNm;
	private String mailPwd;
	private String emails;
	private String returnEmails;
	private String fxdmsEmails;
	private String commonEmails;
	private Integer freeSpace;
	private Integer filesKeepDays;
	private String localPolicyOnlinePath;
	private String localTestPolicyPath;
	private String localReturnPath;
	private String localReturnPdf;
	private String localReturnForCheck;	
	private Integer fxFilesKeepDays;
	private String fileServerIp;
	private String fileServerPdfFolder;
	private String fileServerUser;
	private String fileServerPwd;
	private String fxSftpIp;
	private Integer dbModifiedDays;
	private String fsafpToPdfPath;
	private String pdfzipFileName;
	private boolean returnUnlock;
	private int applyNo;	
	private Integer maxCenter;
	private String returnPhones;
	private String commonPhones;
	private String errorPhones;
	private String dailyReportEmails;
	private boolean resetReturnTest;
	private boolean processDone;
	private String groupInFolder;
	private String groupOutFolder;	
	private String remoteGroupImgFolder;
	private String remoteGroupLawFolder;
	private String gpBackupFolder;
	private String gpImgServer;
	private String gpImgServerUser;
	private String gpImgServerPwd;
	private String gpImgServerDomain;
	private String tpe2Mail;
	private String tpe2DoneDate;
	private String holdFiles; //保留下來不進行作業的檔案名稱
	private String forceNormFiles; //強迫要轉為新契約的檔案名稱
	private Boolean groupSentToPres;
	private String backupFolders;
	private String backupFoldersKeepDays;
	private String returnTestFiles;
	private String returnTestTxt;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getImgServer() {
		return imgServer;
	}
	public void setImgServer(String imgServer) {
		this.imgServer = imgServer;
	}
	public String getImgServerUser() {
		return imgServerUser;
	}
	public void setImgServerUser(String imgServerUser) {
		this.imgServerUser = imgServerUser;
	}
	public String getImgServerPwd() {
		return imgServerPwd;
	}
	public void setImgServerPwd(String imgServerPwd) {
		this.imgServerPwd = imgServerPwd;
	}
	public String getImgServerDomain() {
		return imgServerDomain;
	}
	public void setImgServerDomain(String imgServerDomain) {
		this.imgServerDomain = imgServerDomain;
	}
	
	public String getImgPath() {
		return imgPath;
	}
	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}
	public String getLawPath() {
		return lawPath;
	}
	public void setLawPath(String lawPath) {
		this.lawPath = lawPath;
	}
	public String getReqPolicyPath() {
		return reqPolicyPath;
	}
	public void setReqPolicyPath(String reqPolicyPath) {
		this.reqPolicyPath = reqPolicyPath;
	}
	public String getPolicyTxtServer() {
		return policyTxtServer;
	}
	public void setPolicyTxtServer(String policyTxtServer) {
		this.policyTxtServer = policyTxtServer;
	}
	public String getPolicyTxtServerUser() {
		return policyTxtServerUser;
	}
	public void setPolicyTxtServerUser(String policyTxtServerUser) {
		this.policyTxtServerUser = policyTxtServerUser;
	}
	public String getPolicyTxtServerPwd() {
		return policyTxtServerPwd;
	}
	public void setPolicyTxtServerPwd(String policyTxtServerPwd) {
		this.policyTxtServerPwd = policyTxtServerPwd;
	}
	public String getPolicyTxtServerDomain() {
		return policyTxtServerDomain;
	}
	public void setPolicyTxtServerDomain(String policyTxtServerDomain) {
		this.policyTxtServerDomain = policyTxtServerDomain;
	}
	public String getPolicyTxtPath() {
		return policyTxtPath;
	}
	public void setPolicyTxtPath(String policyTxtPath) {
		this.policyTxtPath = policyTxtPath;
	}
	public String getOkPath() {
		return okPath;
	}
	public void setOkPath(String okPath) {
		this.okPath = okPath;
	}
	public String getLocalOKPath() {
		return localOKPath;
	}
	public void setLocalOKPath(String localOKPath) {
		this.localOKPath = localOKPath;
	}
	public String getLocalPolicyTxtPath() {
		return localPolicyTxtPath;
	}
	public void setLocalPolicyTxtPath(String localPolicyTxtPath) {
		this.localPolicyTxtPath = localPolicyTxtPath;
	}
	public String getFxdmsIP() {
		return fxdmsIP;
	}
	public void setFxdmsIP(String fxdmsIP) {
		this.fxdmsIP = fxdmsIP;
	}
	public String getFxdmsUser() {
		return fxdmsUser;
	}
	public void setFxdmsUser(String fxdmsUser) {
		this.fxdmsUser = fxdmsUser;
	}
	public String getFxdmsPwd() {
		return fxdmsPwd;
	}
	public void setFxdmsPwd(String fxdmsPwd) {
		this.fxdmsPwd = fxdmsPwd;
	}
	public String getFxdmsUploadPath() {
		return fxdmsUploadPath;
	}
	public void setFxdmsUploadPath(String fxdmsUploadPath) {
		this.fxdmsUploadPath = fxdmsUploadPath;
	}
	public String getFxdmsDownloadPath() {
		return fxdmsDownloadPath;
	}
	public void setFxdmsDownloadPath(String fxdmsDownloadPath) {
		this.fxdmsDownloadPath = fxdmsDownloadPath;
	}
	public String getLocalLawPath() {
		return localLawPath;
	}
	public void setLocalLawPath(String localLawPath) {
		this.localLawPath = localLawPath;
	}
	public String getLocalReqPolicyPath() {
		return localReqPolicyPath;
	}
	public void setLocalReqPolicyPath(String localReqPolicyPath) {
		this.localReqPolicyPath = localReqPolicyPath;
	}
	public String getLocalTestImgPath() {
		return localTestImgPath;
	}
	public void setLocalTestImgPath(String localTestImgPath) {
		this.localTestImgPath = localTestImgPath;
	}
	public String getLocalImgPath() {
		return localImgPath;
	}
	public void setLocalImgPath(String localImgPath) {
		this.localImgPath = localImgPath;
	}
	public String getImgPostProcessedPath() {
		return imgPostProcessedPath;
	}
	public void setImgPostProcessedPath(String imgPostProcessedPath) {
		this.imgPostProcessedPath = imgPostProcessedPath;
	}
	public String getCheckedOkPath() {
		return checkedOkPath;
	}
	public void setCheckedOkPath(String checkedOkPath) {
		this.checkedOkPath = checkedOkPath;
	}	
	public String getReceiptOkPath() {
		return receiptOkPath;
	}
	public void setReceiptOkPath(String receiptOkPath) {
		this.receiptOkPath = receiptOkPath;
	}
	
	public String getImgUncompletePath() {
		return imgUncompletePath;
	}
	public void setImgUncompletePath(String imgUncompletePath) {
		this.imgUncompletePath = imgUncompletePath;
	}
	public String getImgSizeErrorPath() {
		return imgSizeErrorPath;
	}
	public void setImgSizeErrorPath(String imgSizeErrorPath) {
		this.imgSizeErrorPath = imgSizeErrorPath;
	}
	public String getImgResolutionErrPath() {
		return imgResolutionErrPath;
	}
	public void setImgResolutionErrPath(String imgResolutionErrPath) {
		this.imgResolutionErrPath = imgResolutionErrPath;
	}
	public String getDifficultFontPath() {
		return difficultFontPath;
	}
	public void setDifficultFontPath(String difficultFontPath) {
		this.difficultFontPath = difficultFontPath;
	}
	public String getBackupFolder() {
		return backupFolder;
	}
	public void setBackupFolder(String backupFolder) {
		this.backupFolder = backupFolder;
	}
	public String getErrorFileNmPath() {
		return errorFileNmPath;
	}
	public void setErrorFileNmPath(String errorFileNmPath) {
		this.errorFileNmPath = errorFileNmPath;
	}
	public String getPresPath() {
		return presPath;
	}
	public void setPresPath(String presPath) {
		this.presPath = presPath;
	}
	public String getAfpPath() {
		return afpPath;
	}
	public void setAfpPath(String afpPath) {
		this.afpPath = afpPath;
	}
	public String getZipTmpPath() {
		return zipTmpPath;
	}
	public void setZipTmpPath(String zipTmpPath) {
		this.zipTmpPath = zipTmpPath;
	}
	public String getGpgExePath() {
		return gpgExePath;
	}
	public void setGpgExePath(String gpgExePath) {
		this.gpgExePath = gpgExePath;
	}
	public Integer getImgDpi() {
		return imgDpi;
	}
	public void setImgDpi(Integer imgDpi) {
		this.imgDpi = imgDpi;
	}
	public Integer getMaxImgWidth() {
		return maxImgWidth;
	}
	public void setMaxImgWidth(Integer maxImgWidth) {
		this.maxImgWidth = maxImgWidth;
	}
	public Integer getMinImgWidth() {
		return minImgWidth;
	}
	public void setMinImgWidth(Integer minImgWidth) {
		this.minImgWidth = minImgWidth;
	}
	public Integer getMaxImgHeight() {
		return maxImgHeight;
	}
	public void setMaxImgHeight(Integer maxImgHeight) {
		this.maxImgHeight = maxImgHeight;
	}
	public Integer getMinImgHeight() {
		return minImgHeight;
	}
	public void setMinImgHeight(Integer minImgHeight) {
		this.minImgHeight = minImgHeight;
	}
	public Integer getDbKeepDays() {
		return dbKeepDays;
	}
	public void setDbKeepDays(Integer dbKeepDays) {
		this.dbKeepDays = dbKeepDays;
	}
	public String getMailHost() {
		return mailHost;
	}
	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}
	public String getMailUserNm() {
		return mailUserNm;
	}
	public void setMailUserNm(String mailUserNm) {
		this.mailUserNm = mailUserNm;
	}
	public String getMailPwd() {
		return mailPwd;
	}
	public void setMailPwd(String mailPwd) {
		this.mailPwd = mailPwd;
	}
	public String getEmails() {
		return emails;
	}
	public void setEmails(String emails) {
		this.emails = emails;
	}
	public Integer getFreeSpace() {
		return freeSpace;
	}
	public void setFreeSpace(Integer freeSpace) {
		this.freeSpace = freeSpace;
	}
	public Integer getFilesKeepDays() {
		return filesKeepDays;
	}
	public void setFilesKeepDays(Integer filesKeepDays) {
		this.filesKeepDays = filesKeepDays;
	}
	
	public List<String> getEmailList(){
		if(this.emails != null){
			String[] emails = this.emails.split(",");
			return Arrays.asList(emails);
		}else{
			return null;
		}
	}
	public String getLocalTxtTmpPath() {
		return localTxtTmpPath;
	}
	public void setLocalTxtTmpPath(String localTxtTmpPath) {
		this.localTxtTmpPath = localTxtTmpPath;
	}
	public String getLocalPolicyOnlinePath() {
		return localPolicyOnlinePath;
	}
	public void setLocalPolicyOnlinePath(String localPolicyOnlinePath) {
		this.localPolicyOnlinePath = localPolicyOnlinePath;
	}
	public String getLocalTestPolicyPath() {
		return localTestPolicyPath;
	}
	public void setLocalTestPolicyPath(String localTestPolicyPath) {
		this.localTestPolicyPath = localTestPolicyPath;
	}
	public String getLocalReturnPath() {
		return localReturnPath;
	}
	public void setLocalReturnPath(String localReturnPath) {
		this.localReturnPath = localReturnPath;
	}
	public Integer getMaxCenter() {
		return maxCenter;
	}
	public void setMaxCenter(Integer maxCenter) {
		this.maxCenter = maxCenter;
	}
	public Integer getFxFilesKeepDays() {
		return fxFilesKeepDays;
	}
	public void setFxFilesKeepDays(Integer fxFilesKeepDays) {
		this.fxFilesKeepDays = fxFilesKeepDays;
	}
	public String getFxSftpIp() {
		return fxSftpIp;
	}
	public void setFxSftpIp(String fxSftpIp) {
		this.fxSftpIp = fxSftpIp;
	}
	public Integer getDbModifiedDays() {
		return dbModifiedDays;
	}
	public void setDbModifiedDays(Integer dbModifiedDays) {
		this.dbModifiedDays = dbModifiedDays;
	}
	public String getLocalReturnPdf() {
		return localReturnPdf;
	}
	public void setLocalReturnPdf(String localReturnPdf) {
		this.localReturnPdf = localReturnPdf;
	}
	public String getLocalReturnForCheck() {
		return localReturnForCheck;
	}
	public void setLocalReturnForCheck(String localReturnForCheck) {
		this.localReturnForCheck = localReturnForCheck;
	}
	public String getFileServerIp() {
		return fileServerIp;
	}
	public void setFileServerIp(String fileServerIp) {
		this.fileServerIp = fileServerIp;
	}
	public String getFileServerPdfFolder() {
		return fileServerPdfFolder;
	}
	public void setFileServerPdfFolder(String fileServerPdfFolder) {
		this.fileServerPdfFolder = fileServerPdfFolder;
	}
	public String getFileServerUser() {
		return fileServerUser;
	}
	public void setFileServerUser(String fileServerUser) {
		this.fileServerUser = fileServerUser;
	}
	public String getFileServerPwd() {
		return fileServerPwd;
	}
	public void setFileServerPwd(String fileServerPwd) {
		this.fileServerPwd = fileServerPwd;
	}
	public String getFeedbackFolder() {
		return feedbackFolder;
	}
	public void setFeedbackFolder(String feedbackFolder) {
		this.feedbackFolder = feedbackFolder;
	}
	public boolean isReturnUnlock() {
		return returnUnlock;
	}
	public void setReturnUnlock(boolean returnUnlock) {
		this.returnUnlock = returnUnlock;
	}
	public String getTestLawPath() {
		return testLawPath;
	}
	public void setTestLawPath(String testLawPath) {
		this.testLawPath = testLawPath;
	}
	public String getTestImgPath() {
		return testImgPath;
	}
	public void setTestImgPath(String testImgPath) {
		this.testImgPath = testImgPath;
	}
	public String getAfpToPdfPath() {
		return afpToPdfPath;
	}
	public void setAfpToPdfPath(String afpToPdfPath) {
		this.afpToPdfPath = afpToPdfPath;
	}
	public String getFsafpToPdfPath() {
		return fsafpToPdfPath;
	}
	public void setFsafpToPdfPath(String fsafpToPdfPath) {
		this.fsafpToPdfPath = fsafpToPdfPath;
	}
	public String getPdfzipFileName() {
		return pdfzipFileName;
	}
	public void setPdfzipFileName(String pdfzipFileName) {
		this.pdfzipFileName = pdfzipFileName;
	}
	public int getApplyNo() {
		return applyNo;
	}
	public void setApplyNo(int applyNo) {
		this.applyNo = applyNo;
	}
	public String getReturnEmails() {
		return returnEmails;
	}
	public void setReturnEmails(String returnEmails) {
		this.returnEmails = returnEmails;
	}
	public String getFxdmsEmails() {
		return fxdmsEmails;
	}
	public void setFxdmsEmails(String fxdmsEmails) {
		this.fxdmsEmails = fxdmsEmails;
	}
	public String getCommonEmails() {
		return commonEmails;
	}
	public void setCommonEmails(String commonEmails) {
		this.commonEmails = commonEmails;
	}
	public String getReturnPhones() {
		return returnPhones;
	}
	public void setReturnPhones(String returnPhones) {
		this.returnPhones = returnPhones;
	}
	public String getCommonPhones() {
		return commonPhones;
	}
	public void setCommonPhones(String commonPhones) {
		this.commonPhones = commonPhones;
	}
	public String getErrorPhones() {
		return errorPhones;
	}
	public void setErrorPhones(String errorPhones) {
		this.errorPhones = errorPhones;
	}
	public String getDailyReportEmails() {
		return dailyReportEmails;
	}
	public void setDailyReportEmails(String dailyReportEmails) {
		this.dailyReportEmails = dailyReportEmails;
	}
	public boolean isResetReturnTest() {
		return resetReturnTest;
	}
	public void setResetReturnTest(boolean resetReturnTest) {
		this.resetReturnTest = resetReturnTest;
	}
	public boolean isProcessDone() {
		return processDone;
	}
	public void setProcessDone(boolean processDone) {
		this.processDone = processDone;
	}
	public String getGroupInFolder() {
		return groupInFolder;
	}
	public void setGroupInFolder(String groupInFolder) {
		this.groupInFolder = groupInFolder;
	}
	public String getGroupOutFolder() {
		return groupOutFolder;
	}
	public void setGroupOutFolder(String groupOutFolder) {
		this.groupOutFolder = groupOutFolder;
	}
	public String getRemoteGroupImgFolder() {
		return remoteGroupImgFolder;
	}
	public void setRemoteGroupImgFolder(String remoteGroupImgFolder) {
		this.remoteGroupImgFolder = remoteGroupImgFolder;
	}
	public String getRemoteGroupLawFolder() {
		return remoteGroupLawFolder;
	}
	public void setRemoteGroupLawFolder(String remoteGroupLawFolder) {
		this.remoteGroupLawFolder = remoteGroupLawFolder;
	}
	public String getGpBackupFolder() {
		return gpBackupFolder;
	}
	public void setGpBackupFolder(String gpBackupFolder) {
		this.gpBackupFolder = gpBackupFolder;
	}
	public String getGpImgServer() {
		return gpImgServer;
	}
	public void setGpImgServer(String gpImgServer) {
		this.gpImgServer = gpImgServer;
	}
	public String getGpImgServerUser() {
		return gpImgServerUser;
	}
	public void setGpImgServerUser(String gpImgServerUser) {
		this.gpImgServerUser = gpImgServerUser;
	}
	public String getGpImgServerPwd() {
		return gpImgServerPwd;
	}
	public void setGpImgServerPwd(String gpImgServerPwd) {
		this.gpImgServerPwd = gpImgServerPwd;
	}
	public String getGpImgServerDomain() {
		return gpImgServerDomain;
	}
	public void setGpImgServerDomain(String gpImgServerDomain) {
		this.gpImgServerDomain = gpImgServerDomain;
	}
	public String getTpe2Mail() {
		return tpe2Mail;
	}
	public void setTpe2Mail(String tpe2Mail) {
		this.tpe2Mail = tpe2Mail;
	}
	public String getTpe2DoneDate() {
		return tpe2DoneDate;
	}
	public void setTpe2DoneDate(String tpe2DoneDate) {
		this.tpe2DoneDate = tpe2DoneDate;
	}
	public String getHoldFiles() {
		return holdFiles;
	}
	public void setHoldFiles(String holdFiles) {
		this.holdFiles = holdFiles;
	}
	public String getForceNormFiles() {
		return forceNormFiles;
	}
	public void setForceNormFiles(String forceNormFiles) {
		this.forceNormFiles = forceNormFiles;
	}
	public Boolean getGroupSentToPres() {
		return groupSentToPres;
	}
	public void setGroupSentToPres(Boolean groupSentToPres) {
		this.groupSentToPres = groupSentToPres;
	}
	public String getBackupFolders() {
		return backupFolders;
	}
	public void setBackupFolders(String backupFolders) {
		this.backupFolders = backupFolders;
	}
	public String getBackupFoldersKeepDays() {
		return backupFoldersKeepDays;
	}
	public void setBackupFoldersKeepDays(String backupFoldersKeepDays) {
		this.backupFoldersKeepDays = backupFoldersKeepDays;
	}
	public List<String> getBackupFoldersList(){
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
	public List<String> getBackupKeepDaysList(){
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
	public String getReturnTestFiles() {
		return returnTestFiles;
	}
	public void setReturnTestFiles(String returnTestFiles) {
		this.returnTestFiles = returnTestFiles;
	}
	
	public String[] getReturnTestFileNms(){
		if(returnTestFiles == null)
			return null;
		else
			return returnTestFiles.split(",");
	}
	public String getReturnTestTxt() {
		return returnTestTxt;
	}
	public void setReturnTestTxt(String returnTestTxt) {
		this.returnTestTxt = returnTestTxt;
	}
	
	
	
}
