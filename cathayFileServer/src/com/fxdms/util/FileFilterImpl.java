package com.fxdms.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Calendar;

import com.fxdms.cathy.bo.Properties;

public class FileFilterImpl {
	
	private Calendar cal = null;
	private static long forCheckTime = 0;
	
	
	public static long getForCheckTime() {
		return forCheckTime;
	}

	public static void setForCheckTime(long forCheckTime) {
		FileFilterImpl.forCheckTime = forCheckTime;
	}

	public static FileFilter getDirectoryFilter(){
		return directoryFilter;
	}
	
	public static FileFilter getFileFilter(){
		return fileFilter;
	}
		
	public static FileFilter getTarFilesFilter(){
		return tarFilesFilter;
	}
	public static FileFilter getTiffFilesFilter(){
		return tiffFilesFilter;
	}
	
	public static FileFilter getZipFilesFilter(){
		return zipFilesFilter;
	}
	
	public static FileFilter getImgZipFilesFilter(){
		return imgZipFilesFilter;
	}
	public static FileFilter getImgOkFilesFilter(){
		return imgOkFilesFilter;
	}
	public static FileFilter getLawOkFilesFilter(){
		return lawOkFilesFilter;
	}	
	public static FileFilter getAreaIdFolderFilter(){
		return areaIdFolderFilter;
	}
	public static FileFilter getAlteredAreaIdDirFilter(){
		return alteredAreaIdDirFilter;
	}
	public static FileFilter getPdfFileFilter(){
		return pdfFileFilter;
	}
	public static FileFilter getGpPdfFileFilter(){
		return gPpdfFileFilter;
	}
	public static FileFilter getReturnFileFilter(){
		return returnFileFilter;
	}	 
	public static FileFilter getTimeBeforeFilesFilter(){
		return timeBeforeFilesFilter;
	}

	private static FileFilter directoryFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isDirectory() )
				return true;
			else
				return false;
		}
	};
	
	private static FileFilter fileFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isFile() )
				return true;
			else
				return false;
		}
	};
	
	private static FileFilter timeBeforeFilesFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isFile() && 
					file.getName().toLowerCase().endsWith(".pdf") && file.lastModified() < forCheckTime)
				return true;
			else
				return false;
		}
	};
	
	private static FileFilter tarFilesFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isFile() && 
					file.getName().toLowerCase().endsWith(".tar") && LockFile.checkFileIsReady(file))
				return true;
			else
				return false;
		}
	};
	
	private static FileFilter zipFilesFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isFile() && 
					file.getName().toLowerCase().endsWith(".zip") && LockFile.checkFileIsReady(file))
				return true;
			else
				return false;
		}
	};
	private static FileFilter tiffFilesFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isFile() && 
					(file.getName().toLowerCase().endsWith(".tif") || file.getName().toLowerCase().endsWith(".tiff")))
				return true;
			else
				return false;
		}
	};
	
	private static FileFilter imgZipFilesFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isFile() && 
					file.getName().toLowerCase().endsWith(".zip") && LockFile.checkFileIsReady(file)){
				String mainFileNm = file.getName().substring(0, 2);
			
				for(int i = 1 ; i <= Properties.getMaxCenter() ; i++){
					String center = null;
					if(i < 10)
						center = "0" + i;
					else
						center = i + "";
					if(mainFileNm.equals(center))
						return true;					
				}
				return false;
			}else{
				return false;
			}
			
		}
	};
	
	private static FileFilter imgOkFilesFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isFile() && 
					file.getName().toUpperCase().equals("IMAGE.OK") && LockFile.checkFileIsReady(file))
				return true;
			else
				return false;
		}
	};
	private static FileFilter lawOkFilesFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isFile() && 
					file.getName().toUpperCase().equals("LAW_IMAGE.OK") && LockFile.checkFileIsReady(file))
				return true;
			else
				return false;
		}
	};
	private static FileFilter areaIdFolderFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isDirectory() && file.getName().length() == 7){
				return true;
			}else{
				return false;
			}
		}
	};
	private static FileFilter alteredAreaIdDirFilter = new FileFilter() {		
		public boolean accept(File file) {			
			if (file != null && file.isDirectory() && file.getName().length() == 12){
				return true;
			}else{
				return false;
			}
		}
	};
	
	private static FileFilter pdfFileFilter = new FileFilter() {		
		public boolean accept(File file) {			
			if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".pdf")
					&& (file.getName().toUpperCase().startsWith("CA") || file.getName().toUpperCase().startsWith("SG") 
							|| file.getName().toUpperCase().startsWith("GA") || file.getName().toUpperCase().startsWith("GG")
							|| file.getName().toUpperCase().startsWith("PD")) 
					&& file.getName().length() == 21 && LockFile.checkFileIsReady(file)){
				return true;
			}else{
				return false;
			}
		}
	};
	private static FileFilter gPpdfFileFilter = new FileFilter() {		
		public boolean accept(File file) {			
			if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".pdf")
					&& file.getName().toUpperCase().startsWith("PD") 
					&& file.getName().length() != 21 && LockFile.checkFileIsReady(file)){
				return true;
			}else{
				return false;
			}
		}
	};
	
	private static FileFilter returnFileFilter = new FileFilter() {		
		public boolean accept(File file) {			
			//結尾是.afp且開頭不是caaa(迴歸件)
			if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".afp") && file.getName().toLowerCase().startsWith("caaat")){
				String afpFileNm = file.getName();
				String newBatchName = afpFileNm.substring(0, 17); // 17位長的newBatchName
				String logFileNm = newBatchName + "_summary.csv"; // 問一下pipi是不是長得如此
				if(new File(file.getParent(), logFileNm).exists())
				   return true;
				else
				   return false;
			}else{
				return false;
			}
		}
	};
	
}
