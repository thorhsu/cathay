package com.fxdms.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;


public class FtpClientUtil {
	static Logger logger = Logger.getLogger(FtpClientUtil.class);
	private String serverAddress;
	private String userId;
	private String password;

	public FtpClientUtil(String serverAddress, String userId, String password) {
		this.serverAddress = serverAddress;
		try {
			this.userId = URLEncoder.encode(userId, "utf-8");
			this.password = URLEncoder.encode(password, "utf-8");;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public FtpClientUtil(){
		
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		try {
			this.userId = URLEncoder.encode(userId, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {		
		try {
			this.password = URLEncoder.encode(password, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args){
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			//fcu.downloadFile("", "01_2014-05-19_EH01093296_9097089407.pdf", new File("d:/tmp", "01_2014-05-19_EH01093296_9097089407.pdf"), false);
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
			
		} finally {
		}
	}
	
	public boolean delete(String remoteDirectory, String remoteFileNm){
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			// Initializes the file manager
			manager.init();
			if(remoteDirectory.startsWith("/"))
				remoteDirectory = remoteDirectory.substring(1);
			if(!remoteDirectory.endsWith("/"))
				remoteDirectory = remoteDirectory + "/";

			// Setup our ftp configuration
			FileSystemOptions opts = new FileSystemOptions();
			FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts,
					true);			
			FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);

			// Create the ftp URI using the host name, userid, password, remote
			// path and file name
			String ftpUri = "ftp://" + userId + ":" + password + "@"
					+ serverAddress + "/" + remoteDirectory + remoteFileNm ;
			// Create remote file object
			FileObject remoteFile = manager.resolveFile(ftpUri, opts);
			remoteFile.delete();
			return true;
						
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
			return false;
			
		} finally {
			manager.close();
		}
	}

	
	public boolean upload(String remoteDirectory, File file) {
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			if (!file.exists())
				throw new RuntimeException("Error. Local file not found");

			if(remoteDirectory.startsWith("/"))
				remoteDirectory = remoteDirectory.substring(1);
			if(!remoteDirectory.endsWith("/"))
				remoteDirectory = remoteDirectory + "/";
			
			// Initializes the file manager
			manager.init();

			// Setup our ftp configuration
			FileSystemOptions opts = new FileSystemOptions();
			FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts,
					true);			
			FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);


			// Create the ftp URI using the host name, userid, password, remote
			// path and file name
			String ftpUri = "ftp://" + userId + ":" + password + "@"
					+ serverAddress + "/" + remoteDirectory + file.getName();
			// Create local file object
			FileObject localFile = manager.resolveFile(file.getAbsolutePath());
			// Create remote file object
			FileObject remoteFile = manager.resolveFile(ftpUri, opts);

			// Copy local file to ftp server
			remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
			return false;
		} finally {
			manager.close();
		}
		return true;
	}

	public boolean downloadFile(String remoteDirectory, String fileToDownload,
			File localDownload, boolean delete) throws Exception {
		StandardFileSystemManager manager = new StandardFileSystemManager();

		try {
			if(remoteDirectory == null || remoteDirectory.trim().equals("")){
			}else{
				if(remoteDirectory.startsWith("/"))		
				   remoteDirectory = remoteDirectory.substring(1);
			    if(!remoteDirectory.endsWith("/"))
				   remoteDirectory = remoteDirectory + "/";
			}
			// Initializes the file manager
			manager.init();

			// Setup our ftp configuration
			FileSystemOptions opts = new FileSystemOptions();
			FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts,
					true);			
			FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);

			// Create the ftp URI using the host name, userid, password, remote
			// path and file name
			String ftpUri = "ftp://" + userId + ":" + password + "@"
					+ serverAddress + "/" + remoteDirectory + fileToDownload;

			FileObject localFile = manager.resolveFile(localDownload
					.getAbsolutePath());

			// Create remote file object
			FileObject remoteFile = manager.resolveFile(ftpUri, opts);
			if(remoteFile.exists()){
			   // Copy local file to ftp server
			   localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
			   logger.info("download completed:" + fileToDownload);
			   if(delete)
				   remoteFile.delete();
			}

		} catch (Exception ex) {
			logger.error("", ex);
			ex.printStackTrace();
			throw ex;
		} finally {
			manager.close();
		}

		return true;
	}
}
