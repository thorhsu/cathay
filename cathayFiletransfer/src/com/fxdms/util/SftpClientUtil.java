package com.fxdms.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

public class SftpClientUtil {
	static Logger logger = Logger.getLogger(SftpClientUtil.class);
	private String serverAddress;
	private String userId;
	private String password;

	public SftpClientUtil(String serverAddress, String userId, String password) {
		this.serverAddress = serverAddress;
		try {
			this.userId = URLEncoder.encode(userId, "utf-8");
			this.password = URLEncoder.encode(password, "utf-8");;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public SftpClientUtil(){
		
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

	public void setUserId(String userId) throws UnsupportedEncodingException {
		this.userId = URLEncoder.encode(userId, "utf-8");
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) throws UnsupportedEncodingException {
		this.password = URLEncoder.encode(password, "utf-8");;
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

			// Setup our SFTP configuration
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
					opts, "no");
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts,
					false);
			SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 30000);

			// Create the SFTP URI using the host name, userid, password, remote
			// path and file name
			String sftpUri = "sftp://" + userId + ":" + password + "@"
					+ serverAddress + "/" + remoteDirectory + remoteFileNm ;
			// Create remote file object
			FileObject remoteFile = manager.resolveFile(sftpUri, opts);
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
	 

	//取得待傳的gpg檔
	public String[] getFile(String remoteDirectory) throws Exception{
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			// Initializes the file manager
			manager.init();

			// Setup our SFTP configuration
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
					opts, "no");
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts,
					false);
			SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 30000);

			// Create the SFTP URI using the host name, userid, password, remote
			// path and file name
			String sftpUri = "sftp://" + userId + ":" + password + "@"
					+ serverAddress + "/" + remoteDirectory ;
			// Create remote file object
			FileObject remoteFile = manager.resolveFile(sftpUri, opts);
			FileObject[] files = remoteFile.getChildren();
			String[] fileNm = new String[files.length];
			for(int i = 0 ; i < fileNm.length ; i++){
				fileNm[i] = files[i].getName().getBaseName();
			}
			return fileNm;
			
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
			throw e;
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

			// Setup our SFTP configuration
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
					opts, "no");
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts,
					false);
			SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 30000);

			// Create the SFTP URI using the host name, userid, password, remote
			// path and file name
			String sftpUri = "sftp://" + userId + ":" + password + "@"
					+ serverAddress + "/" + remoteDirectory + file.getName();
			// Create local file object
			FileObject localFile = manager.resolveFile(file.getAbsolutePath());
			// Create remote file object
			FileObject remoteFile = manager.resolveFile(sftpUri, opts);

			// Copy local file to sftp server
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
			File localDownload) {
		StandardFileSystemManager manager = new StandardFileSystemManager();

		try {
			if(remoteDirectory.startsWith("/"))
				remoteDirectory = remoteDirectory.substring(1);
			if(!remoteDirectory.endsWith("/"))
				remoteDirectory = remoteDirectory + "/";
			// Initializes the file manager
			manager.init();

			// Setup our SFTP configuration
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
					opts, "no");
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts,
					false);
			SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 30000);

			// Create the SFTP URI using the host name, userid, password, remote
			// path and file name
			String sftpUri = "sftp://" + userId + ":" + password + "@"
					+ serverAddress + "/" + remoteDirectory + fileToDownload;

			FileObject localFile = manager.resolveFile(localDownload
					.getAbsolutePath());

			// Create remote file object
			FileObject remoteFile = manager.resolveFile(sftpUri, opts);

			// Copy local file to sftp server
			localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
			System.out.println("File download successful");

		} catch (Exception ex) {
			logger.error("", ex);
			ex.printStackTrace();
			return false;
		} finally {
			manager.close();
		}

		return true;
	}
}
