package com.fxdms.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Calendar;

import com.fxdms.cathy.bo.Properties;

public class FileFilterImpl {
	private static Integer expiredDay = Properties.getFilesKeepDays();
	
	public static FileFilter getDirectoryFilter(){
		return directoryFilter;
	}
	public static FileFilter getExpiredImgFile(){
		return expiredPolicyImgFilesFilter;
	}
	
	public static FileFilter getFileFilter(){
		return fileFilter;
	}
	
	public static FileFilter getTifFileFilter(){
		return tifFileFilter;
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
	public static FileFilter getPdfFileFilter(){
		return pdfFileFilter;
	}
	
	public static FileFilter getImgFileFilter(){
		return imgFileFilter;
	}
	public static FileFilter getCommonPdfFileFilter(){
		return commonPdfFileFilter;
	}
	
	public static FileFilter getImgZipFilesFilter(){
		return imgZipFilesFilter;
	}
	public static FileFilter getImgOkFilesFilter(){
		return imgOkFilesFilter;
	}
	public static FileFilter getTestImgOkFilesFilter(){
		return testImgOkFilesFilter;
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
	public static FileFilter getAfpFileFilter(){
		return afpFileFilter;
	}
	public static FileFilter getReturnFileFilter(){
		return returnFileFilter;
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
	
	private static FileFilter expiredPolicyImgFilesFilter = new FileFilter() {
		
		public boolean accept(File file) {
			long keepDays = 7L;
			if(expiredDay != null){
				keepDays = expiredDay;
			}
			if (file != null && file.isFile() && 
					(file.getName().toLowerCase().endsWith(".n.tif") || file.getName().toLowerCase().endsWith(".m.tif"))&& LockFile.checkFileIsReady(file)){
				Calendar cal = Calendar.getInstance();
				if(file.lastModified() + (keepDays * 24L * 60L * 60L * 1000L) < cal.getTimeInMillis())
				   return true;
				else
				   return false;
			}else{
				return false;
			}
			
		}
	};
	
	private static FileFilter imgZipFilesFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isFile() && 
					file.getName().toLowerCase().endsWith(".zip") && LockFile.checkFileIsReady(file)){
				return true;
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
	
	private static FileFilter testImgOkFilesFilter = new FileFilter() {
		public boolean accept(File file) {			
			if (file != null && file.isFile() && 
					file.getName().toUpperCase().equals("TEST_IMAGE.OK") && LockFile.checkFileIsReady(file))
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
			if (file != null && file.isDirectory() && file.getName().split("_").length == 2 ){
				return true;
			}else{
				return false;
			}
		}
	};
	
	private static FileFilter alteredAreaIdDirFilter = new FileFilter() {		
		public boolean accept(File file) {			
			if (file != null && file.isDirectory() && file.getName().split("_").length == 3){
				return true;
			}else{
				return false;
			}
		}
	};
	
	private static FileFilter afpFileFilter = new FileFilter() {		
		public boolean accept(File file) {			
			//結尾是.afp且開頭不是caaa(迴歸件)
			if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".afp") 
					&& !file.getName().toLowerCase().startsWith("caaab") && !file.getName().toLowerCase().startsWith("sgaab")){
				return true;
			}else{
				return false;
			}
		}
	};
	
	private static FileFilter returnFileFilter = new FileFilter() {		
		public boolean accept(File file) {			
			//結尾是.afp且開頭是caaab(迴歸件)或sgaab
			if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".afp") 
					&& (file.getName().toLowerCase().startsWith("caaab") || file.getName().toLowerCase().startsWith("sgaab"))){
				return true; 				
			}else{
				return false;
			}
		}
	};
	
	private static FileFilter commonPdfFileFilter = new FileFilter() {		
		public boolean accept(File file) {			
			//結尾是.pdf且開頭不是caaa(迴歸件)，也不是CA00T或SG00T(測試件)
			if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".pdf")					
					 && LockFile.checkFileIsReady(file)){
				return true;
			}else{
				return false;
			}
		}
	};
	
	private static FileFilter pdfFileFilter = new FileFilter() {		
		public boolean accept(File file) {			
			//結尾是.pdf且開頭不是caaa(迴歸件)，也不是CA00T或SG00T(測試件)
			if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".pdf")
					&& (!file.getName().toUpperCase().startsWith("CA00T") && !file.getName().toUpperCase().startsWith("SG00T") 
							&& !file.getName().toUpperCase().startsWith("CAAAB") && !file.getName().toUpperCase().startsWith("SGAAB")
							&& !file.getName().toUpperCase().startsWith("GG09T") && !file.getName().toUpperCase().startsWith("GA09T")) 
					&& file.getName().length() == 21 && LockFile.checkFileIsReady(file)){
				return true;
			}else{
				return false;
			}
		}
	};
	
	private static FileFilter tifFileFilter = new FileFilter() {		
		public boolean accept(File file) {			
			//結尾是.tif且開頭不是n.tif或m.tif
			if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".tif") && !file.getName().toLowerCase().endsWith("n.tif") && !file.getName().toLowerCase().endsWith("m.tif") ){
				return true;
			}else{
				return false;
			}
		}
	};
	
	private static FileFilter imgFileFilter = new FileFilter() {		
		public boolean accept(File file) {			
			//結尾是.tif且開頭不是n.tif或m.tif
			if (file != null && file.isFile() 
					&& (file.getName().toLowerCase().endsWith(".tif") || file.getName().toLowerCase().endsWith(".pdf") || file.getName().toLowerCase().endsWith(".tiff")) 
					&& !file.getName().toLowerCase().endsWith("n.tif") 
					&& !file.getName().toLowerCase().endsWith("m.tif") ){
				return true;
			}else{
				return false;
			}
		}
	};
	
}
