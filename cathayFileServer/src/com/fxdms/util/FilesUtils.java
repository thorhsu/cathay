package com.fxdms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.fxdms.cathy.conf.Constant;

public class FilesUtils extends FileUtils {
	static Logger logger = Logger.getLogger(FilesUtils.class);
	private int filesNo = 0;
	private static Constant constant = new Constant();

	public static void main(String args[]) throws IOException {
		// listDirectoryInformation("C:\\temp\\fubonTestData\\buffered",
		// "C:\\temp\\fubonTestData\\buffered\\mylog.log");
		/*
		 * try { File file = new File("C:\\temp\\testMy.pdf"); copyFile(new
		 * File("C:\\temp\\test.pdf"), file); file.setLastModified(new
		 * Date().getTime()); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */
		FileUtils.moveDirectory(new File("C:\\Temp\\tmp"), new File(
				"C:\\Temp\\temp"));
	}
	
	public static void copySmbFileToDirectory(SmbFile srcfile, File dtDirectory) throws IOException {
		System.out.println("copy file : " + srcfile.getName() + dtDirectory.getName());
		InputStream in = null;
		OutputStream out = null;
		if(dtDirectory.exists() && dtDirectory.isFile()){
			FileUtils.forceDelete(dtDirectory);
			dtDirectory.mkdirs();
		}else if(!dtDirectory.exists()){
			dtDirectory.mkdirs();
		}
		
		File dtFile = new File(dtDirectory, srcfile.getName());
		try {			
            if(srcfile.isDirectory()){                
                dtFile.mkdirs();
                SmbFile[] smbFiles = srcfile.listFiles();                
                for(SmbFile smbFile : smbFiles){
                	copySmbFileToDirectory(smbFile, dtFile);
                }
            }else{               
			   in = new SmbFileInputStream(srcfile);
			   out = new FileOutputStream(dtFile);
			   byte[] buf = new byte[4096];
			   int len;
			   while ((len = in.read(buf)) > 0) {
				   out.write(buf, 0, len);
			   }
            }
		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
			logger.error("無法在" + srcfile.getURL() + "中找到:" + srcfile.getName(), ex);
			throw ex;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			logger.error("檔案讀取錯誤:" + srcfile.getName(), e);
			throw e;
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			in = null;
			out = null;
			if (dtFile.exists())
				dtFile.setLastModified(srcfile.getDate());
		}
	}
	
	public static SmbFile[] getFileList(String smbDomain, String smbServer,
			String smbUser, String smbPwd, String smbFolder,
			SmbFileFilter filter) {
		String user = null;
		String pwd = null;
		String sipUrl = null;
		try {
			if(smbDomain == null)
				smbDomain = "";
			user = URLEncoder.encode(smbUser, "utf-8");
			pwd = URLEncoder.encode(smbPwd, "utf-8");
			sipUrl = "smb://" + smbDomain + ";" + user + ":" + pwd + "@"
					+ smbServer + "/" + smbFolder;
			sipUrl = sipUrl.replaceAll("\\\\", "/");

		} catch (UnsupportedEncodingException e1) {
			logger.error("", e1);
			e1.printStackTrace();
		}
		try {
			SmbFile folder = new SmbFile(sipUrl);
			if(folder.isDirectory()){
			   folder.lastModified();
			   folder.getDate();
			   if (filter != null)
				   return folder.listFiles(filter);
			   else
				   return folder.listFiles();
			}
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
		}
		return null;
	}

	public static List<File> checkFolder(String path) {
		File myFolder = new File(path);
		logger.info("this folder exist:" + myFolder.exists());
		File[] files = null;
		if (myFolder.exists())
			files = myFolder.listFiles();
		if (files != null && files.length > 0) {
			Vector<File> allFiles = new Vector<File>();
			// 去除目錄裡的目錄，把檔案加到files此物件變數中
			for (File file : files)
				if (file.isFile())
					allFiles.add(file);

			if (allFiles.size() > 0) {
				logger.info("this folder contained " + allFiles.size() + "個檔案");
				return allFiles;
			} else {
				return null;
			}

		} else {
			return null;
		}
	}

	public static void moveFileToDirectory(File src, File destDir,
			boolean createDestDir) throws IOException {
		File destFile = new File(destDir, src.getName());
		boolean destEmpty = true;
		if (src.exists() && destFile.exists())
			try {
				FileUtils.forceDelete(destFile);
			} catch (IOException e1) {
				logger.error("delete failed", e1);
				destEmpty = false;
				e1.printStackTrace();
			}
		if (src.exists() && destEmpty)
			try {
				FileUtils.moveToDirectory(src, destDir, createDestDir);
				logger.info("moved file " + src.getName() + " to "
						+ destDir.getPath() + " success.");
			} catch (IOException e) {
				logger.error("moved failed", e);
				e.printStackTrace();
				throw e;
			}
	}
	public static void moveToDirectory(File src, File destDir,
			boolean createDestDir) throws IOException {
		File destFile = new File(destDir, src.getName());
		boolean destEmpty = true;
		if (src.exists() && destFile.exists())
			try {
				FileUtils.forceDelete(destFile);
			} catch (IOException e1) {
				logger.error("delete failed", e1);
				destEmpty = false;
				e1.printStackTrace();
			}
		if (src.exists() && destEmpty)
			try {
				FileUtils.moveToDirectory(src, destDir, createDestDir);
				logger.info("moved file " + src.getName() + " to "
						+ destDir.getPath() + " success.");
			} catch (IOException e) {
				logger.error("moved failed", e);
				e.printStackTrace();
				throw e;
			}
	}

	public static void listDirectoryInformation(String path, String fileName) {

		OutputStreamWriter ow = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			ow = new OutputStreamWriter(fos, "ms950");

			File sourceDir = new File(path);
			FilesUtils fu = new FilesUtils();
			fu.setFilesNo(0);
			int allFiles = fu.listDirectory(sourceDir, ow);
			// = new FilesUtils().countAllFilesNo(sourceDir);
			ow.write("共 " + fu.getFilesNo() + " 個檔案");
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
		} finally {
			if (ow != null)
				try {
					ow.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static int countFilsNo(File sourceDir) {
		int filesNo = 0;
		File[] files = sourceDir.listFiles();
		for (File file : files)
			if (file.isFile())
				filesNo++;
		return filesNo;
	}

	public int countAllFilesNo(File sourceDir) {
		filesNo += countFilsNo(sourceDir);
		File[] files = sourceDir.listFiles();
		for (File file : files)
			if (file.isDirectory())
				countAllFilesNo(file);
		return filesNo;
	}

	public int listDirectory(File sourceDir, OutputStreamWriter fileWriter)
			throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd   HH:mm");
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumIntegerDigits(10);
		String outLine = sourceDir.getPath() + " 的目錄 \n";
		fileWriter.write(outLine);
		fileWriter.write("\n");
		fileWriter.write(sdf.format(sourceDir.lastModified())
				+ "    <DIR>            .\n");
		fileWriter.write(sdf.format(sourceDir.getParentFile().lastModified())
				+ "    <DIR>            ..\n");
		File[] dirList = sourceDir.listFiles();
		int myFiles = 0;
		for (int i = 0; i < dirList.length; i++) {
			long lastModified = dirList[i].lastModified();
			outLine = sdf.format(new Date(lastModified));
			if (dirList[i].isDirectory()) { // a directory
				outLine += "    <DIR>             " + dirList[i].getName()
						+ "\n";
			} else {
				myFiles++;
				outLine += "           "
						+ StringUtils.leftPad(nf.format(dirList[i].length()),
								10) + " " + dirList[i].getName() + "\n";
			}
			fileWriter.write(outLine);
		}
		this.filesNo += myFiles;
		fileWriter.write("                " + myFiles + " 個檔案           "
				+ nf.format(sourceDir.length()) + " 位元組\n");
		fileWriter.write("\n");

		for (int i = 0; i < dirList.length; i++)
			if (dirList[i].isDirectory())
				listDirectory(dirList[i], fileWriter);

		return myFiles;

	}

	public static void copyFile(SmbFile srcfile, File dtFile)
			throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			File f2 = dtFile;
			File parent = f2.getParentFile();
			if (!parent.exists())
				parent.mkdirs();			

			in = new SmbFileInputStream(srcfile);
			out = new FileOutputStream(f2);

			byte[] buf = new byte[4096];

			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
			logger.error("無法在sip server中找到:" + srcfile.getName(), ex);
			throw ex;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			logger.error("檔案讀取錯誤:" + srcfile.getName(), e);
			throw e;
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			in = null;
			out = null;
			if (dtFile.exists())
				dtFile.setLastModified(srcfile.getDate());
		}
	}

	public static void copyFile(SmbFile srcfile, SmbFile detFile)
			throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			String parent = detFile.getParent();
			logger.info("unCompleted path is " + parent);
			// if (!parent.exists())
			// parent.mkdirs();

			in = new SmbFileInputStream(srcfile);
			out = new SmbFileOutputStream(detFile);

			byte[] buf = new byte[4096];

			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
			logger.error("無法在sip server中找到:" + srcfile.getName(), ex);
			throw ex;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			logger.error("檔案讀取錯誤:" + srcfile.getName(), e);
			throw e;
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			in = null;
			out = null;
			if (detFile.exists())
				detFile.setLastModified(srcfile.getDate());
		}
	}

	public static void copyFile(String srFile, String dtFile)
			throws IOException {
		File srcFile = new File(srFile);
		File destFile = new File(dtFile);
		FilesUtils.copyFile(srcFile, destFile);
	}

	public static void moveFile(String srFile, String dtFile)
			throws IOException {
		File srcFile = new File(srFile);
		File destFile = new File(dtFile);
		FilesUtils.moveDirectory(srcFile, destFile);
	}

	public static String packFiles(List<String> fileNames, String rootPath,
			String zipFileName) {
		String targetFileNm = rootPath + zipFileName;
		for (String filename : fileNames) {

		}
		return null;
	}

	public int getFilesNo() {
		return filesNo;
	}

	public void setFilesNo(int filesNo) {
		this.filesNo = filesNo;
	}


	public static boolean checkFileIsReady(String pathFile) {
		File file = null;
		FileChannel channel = null;
		FileLock lock = null;
		try {
			file = new File(pathFile);
			channel = new RandomAccessFile(file, "rw").getChannel();
			lock = channel.tryLock();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (lock != null)
					lock.release();
				if (channel != null)
					channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static boolean checkFileIsReady(File file) {
		FileChannel channel = null;
		FileLock lock = null;
		try {
			channel = new RandomAccessFile(file, "rw").getChannel();
			lock = channel.tryLock();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (lock != null)
					lock.release();
				if (channel != null)
					channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
