package com.fxdms.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;



public class ZipUtils {
	static Logger logger = Logger.getLogger(ZipUtils.class);
	public static void main(String[] args) {
		/*
		File srcFile = new File("C:/Temp/temp/test");
		File targetZip = new File("C:/temp/123.zip");
		File extractDir = new File("C:\\servlet2\\");

		try {
			// 壓縮
			new ZipUtils().makeZip(srcFile, targetZip);
			// 解壓縮
			// new ZipUtils().unzipFile(targetZip, extractDir);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		//C:\Temp\setup.exe
		//C:\Temp\ev_client_log_20110427092747.txt
		/*
		File [] zipFiles = new File[2];
		zipFiles[0] = new File("C:/Temp/setup.exe");
		zipFiles[1] = new File("C:/Temp/ev_client_log_20110427092747.txt"); 
		
			new ZipUtils().packFile(zipFiles,  new File("c:/Temp/myTest123.zip"));
		
		*/
		//ZipUtils.unzipFile(new File("C:\\Temp\\RNB3582_PRIT3A_140_66_20_15-0602-225645-12316060.txt_2011-06-02-3448626759717610.zip"), new File("C:/photo/"), false);
		/*
		File[] files = new File[2];
		files[0] = new File("c:/temp/jars/proxool-0.9.1-fix.jar");
		files[1] = new File("c:/temp/jars/mail.jar");
		ZipUtils.packFile(files, new File("c:/temp/test.zip"));
		*/
		
		ZipUtils.unzipFile(new File("C:/java/jars/comboFtp/activation.jar"), new File("c:/temp/testUnzip"), false);
		File testUnzip = new File("c:/temp/testUnzip");
		File [] files = testUnzip.listFiles();
		for(File file: files){
			try {
				FilesUtils.forceDelete(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 解壓縮
	 * 
	 * @param zipfile
	 *            zip檔位置
	 * @param extractDir
	 *            解壓縮資料夾
	 * @return
	 */
	public static boolean unzipFile(File zipfile, File extractDir) {

		try {
			unZip(zipfile, extractDir.getAbsolutePath(), true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public static boolean unzipFile(File zipfile, File extractDir, boolean withDirectory) {

		try {
			unZip(zipfile, extractDir.getAbsolutePath(), withDirectory);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * 建立資料夾
	 * 
	 * @param directory
	 * @param subDirectory
	 */
	private static void createDirectory(String directory, String subDirectory) {
		String dir[];
		File fl = new File(directory);
		try {
			if (subDirectory == "" && fl.exists() != true)
				fl.mkdir();
			else if (subDirectory != "") {
				dir = subDirectory.replace('\\', '/').split("/");
				for (int i = 0; i < dir.length; i++) {
					File subFile = new File(directory + File.separator + dir[i]);
					System.out.println("outputDir is: " + directory + File.separator + dir[i]);
					if (subFile.exists() == false)
						subFile.mkdir();
					directory += File.separator + dir[i];
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
		}
	}

	
	
	
	
	
	/**
	 * 解壓縮主程式
	 * 
	 * @param zipFileName
	 * @param outputDirectory
	 * @throws Exception
	 */
	private static void unZip(File ZIPFile, String outputDirectory, boolean withDirectory)
			throws Exception {
		org.apache.tools.zip.ZipFile zipFile = null;
		InputStream in = null;
		FileOutputStream out = null;
		try {
			zipFile = new org.apache.tools.zip.ZipFile(
					ZIPFile);
			java.util.Enumeration e = zipFile.getEntries();
			org.apache.tools.zip.ZipEntry zipEntry = null;
			createDirectory(outputDirectory, "");
			// if(!outputDirectory.exists()) outputDirectory.mkdirs();

			while (e.hasMoreElements()) {
				zipEntry = (org.apache.tools.zip.ZipEntry) e.nextElement();
				if (zipEntry.isDirectory()) {
					String name = zipEntry.getName();
					name = name.substring(0, name.length() - 1);
					File f = new File(outputDirectory + File.separator + name);
					if(withDirectory){
					   f.mkdir();
					   logger.info("創建立目錄：" + outputDirectory
							   + File.separator + name);
					}
					
				} else {
					
					String fileName = zipEntry.getName();

					fileName = fileName.replace('\\', '/');
					fileName = fileName.replaceAll(":", "_");
					
					if (fileName.indexOf("/") != -1) {
						if(withDirectory)
						   createDirectory(outputDirectory, fileName.substring(0,
								   fileName.lastIndexOf("/")));
						
						fileName = fileName.substring(
								fileName.lastIndexOf("/") + 1,
								fileName.length());
					}
					
					
					File f = null;
					if(withDirectory)
					   f = new File(outputDirectory + File.separator
							+ zipEntry.getName().replaceAll(":", "_"));
					else
					   f = new File(outputDirectory +  File.separator +  fileName);
					
					f.createNewFile();
					
					in = zipFile.getInputStream(zipEntry);
					out = new FileOutputStream(f);
					

					byte[] by = new byte[1024];
					int c;
					while ((c = in.read(by)) != -1) {
						out.write(by, 0, c);
					}
					f.setLastModified(zipEntry.getTime());
					try{
					   out.close();
					   in.close();	
					}catch(Exception e1){
						logger.error("", e1);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("", ex);
			ex.printStackTrace();
		}finally{
			try{
			   if(out != null)
				   out.close();
			   if(in != null)
				   in.close();
			   if(zipFile != null)
				   zipFile.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			out = null;
			in = null;
			zipFile = null;
		}

	}

	/**
	 * 建立 zip 檔
	 * 
	 * @param srcFile
	 *            想要壓縮的資料夾
	 * @param targetZip
	 *            壓縮zip檔
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void makeZip(File srcFile, File targetZip)
			throws IOException, FileNotFoundException {
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
				targetZip));
		String dir = "";
		recurseFiles(srcFile, zos, dir);
		zos.close();
	}
	
	

	public static void packFile(File[] files, File file){
		ZipOutputStream zos = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			zos = new ZipOutputStream(fos);
			packFile(files, zos);
		} catch (FileNotFoundException e) {
			logger.error("", e);
			e.printStackTrace();
		}finally{
			if(zos != null)
				try {
					zos.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
		    if(fos != null)
			   try {
	 			  fos.close();
				} catch (IOException e) {
     				
				}	
			zos = null;
			fos = null;
		}
		
	}
	public static void packFile(File[] files, ZipOutputStream zos){
		//String[] filenames = new String[]{"filename1", "filename2"};

		// Create a buffer for reading the files
		byte[] buf = new byte[1024];
		FileInputStream in = null;
		try {
		    // Create the ZIP file
		    //String outFilename = "outfile.zip";
		    //ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
		    // Compress the files
		    for (int i=0; i<files.length; i++) {
		        in = new FileInputStream(files[i]);

		        // Add ZIP entry to output stream.
		        ZipEntry zipEntry = new ZipEntry(files[i].getName());
		        zipEntry.setTime(files[i].lastModified());
		        zos.putNextEntry(zipEntry);
		        

		        // Transfer bytes from the file to the ZIP file
		        int len;
		        while ((len = in.read(buf)) > 0) {
		            zos.write(buf, 0, len);
		        }
		        try{
		           in.close();
		        }catch(Exception e){
		        	logger.error("", e);
		        }
		        in = null;
		    }
	        // Complete the entry
	        try{
	        	try{
		           if(in != null)
		        	  in.close();
		        }catch(Exception e){
		        		
		        }	        	
	           zos.closeEntry();
	        }catch(Exception e){
	        	logger.error("", e);
	        }finally{
	        	in = null;
	        	zos = null;
	        }

		} catch (IOException e) {
			logger.error("", e);
		}finally{
			try{
	        	if(in != null)
	        	   in.close();
	        }catch(Exception e){
                logger.error("", e);
	        }
	        try{
	        	if(zos != null)
	        	   zos.close();
	        }catch(Exception e){
	        	logger.error("", e);
	        }
	        in = null;
	        zos = null;
		}
	}

	/**
	 * 壓縮 主程式
	 * 
	 * @param file
	 * @param zos
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void recurseFiles(File file, ZipOutputStream zos, String dir)
			throws IOException, FileNotFoundException {
		// 目錄
		if (file.isDirectory()) {
			System.out.println("找到資料夾:" + file.getName());
			dir += file.getName() + File.separator;
			String[] fileNames = file.list();
			if (fileNames != null) {
				for (int i = 0; i < fileNames.length; i++) {
					recurseFiles(new File(file, fileNames[i]), zos, dir);
				}
			}
		}
		// 如果是檔案才壓縮
		else {
			System.out.println("壓縮檔案:" + file.getName());

			byte[] buf = new byte[1024];
			int len;

			dir = dir.substring(dir.indexOf(File.separator) + 1);
			ZipEntry zipEntry = new ZipEntry(dir + file.getName());
			zipEntry.setTime(file.lastModified());
			FileInputStream fin = new FileInputStream(file);
			BufferedInputStream in = new BufferedInputStream(fin);
			zos.putNextEntry(zipEntry);
			// Read bytes from the file and write into the Zip archive.

			while ((len = in.read(buf)) >= 0) {
				zos.write(buf, 0, len);
			}

			in.close();

			zos.closeEntry();
		}
	}
}
