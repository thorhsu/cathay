package com.fxdms.cathy.task;

import java.awt.image.RenderedImage;
import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;

import jcifs.smb.SmbFile;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;




import org.springframework.beans.BeansException;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.FilesUtils;
import com.fxdms.util.LockFile;
import com.fxdms.util.PdfFileUtil;

import thor.util.JaiTiffImgProcess;

import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.ImgFile;

public class PdfToTifConverter {
	static Logger logger = Logger.getLogger(PdfToTifConverter.class);
	private static File presPath = new File(Properties.getPresPath()).getParentFile();
	private static File imgFolder = new File(presPath, "Image");
	private static File lawFolder = new File(presPath, "Law");
	private static File imgPdfToTif = new File(imgFolder, "pdf2tif");
	private static File lawPdfToTif = new File(lawFolder, "pdf2tif");

	private static boolean afterReqPolicyProcessed;

	public static boolean isAfterReqPolicyProcessed() {
		return afterReqPolicyProcessed;
	}

	public static void setAfterReqPolicyProcessed(boolean afterReqPolicyProcessed) {
		PdfToTifConverter.afterReqPolicyProcessed = afterReqPolicyProcessed;
	}


	public static void startToRun() {
		if(!imgPdfToTif.exists())
			imgPdfToTif.mkdirs();
		if(!lawPdfToTif.exists())
			lawPdfToTif.mkdirs();
		Date today = new Date();
		logger.info("pdf to tiff converter start to run");
		try{
			File[] imgFolderPdfFiles = imgFolder.listFiles(pdfFilter); //原來在imgFolder裡的pdf檔
			File[] imgTifFolderFiles = imgPdfToTif.listFiles(pdfFilter); //在pdf2tif裡的pdf檔
			List<File> imgToConvertFiles = new ArrayList<File>();
			
			File[] lawFolderPdfFiles = lawFolder.listFiles(pdfFilter); //原來在lawFolder裡的pdf檔
			File[] lawTifFolderFiles = lawPdfToTif.listFiles(pdfFilter); //在pdf2tif裡的pdf檔
			List<File> lawToConvertFiles = new ArrayList<File>();
			
			for(File file : imgFolderPdfFiles){
				boolean needToConvert = true;
				if(!LockFile.checkFileIsReady(file)){
					needToConvert = false;
					continue;
				} 
				File tifFile = new File(file.getParent(), file.getName() + ".tif");
				for(File forConvertFile : imgTifFolderFiles){					
					if( forConvertFile.getName().equals(file.getName()) 
							&& file.lastModified() == forConvertFile.lastModified()
							&& file.length() == forConvertFile.length()
									&& tifFile != null && tifFile.exists()){
						needToConvert = false; //檔案大小相同，名字相同，時間相同。就代表已轉過，不用再轉
						break;
					}										
				}
				if(needToConvert && !imgToConvertFiles.contains(file))
					imgToConvertFiles.add(file);						
			}
			for(File file : lawFolderPdfFiles){
				boolean needToConvert = true;
				if(!LockFile.checkFileIsReady(file)){
					needToConvert = false;
					continue;
				} 
				File tifFile = new File(file.getParent(), file.getName() + ".tif");
				for(File forConvertFile : lawTifFolderFiles){
					if(forConvertFile.getName().equals(file.getName()) 
							&& file.lastModified() == forConvertFile.lastModified()
							&& file.length() == forConvertFile.length()
							&& tifFile != null && tifFile.exists()){
						needToConvert = false;
						break;
					}					
				}
				if(needToConvert && !lawToConvertFiles.contains(file))
					lawToConvertFiles.add(file);						
				
			}
			for(File file : imgToConvertFiles){
				String tifName = file.getName() + ".tif";
				File tifFile = new File(imgFolder, tifName);  
				try{
				    startConvert(today, file, tifFile, false, true); //轉檔案並儲存資料庫				    
				}catch(Exception e){
					logger.error("", e);
					e.printStackTrace();
				}
			}
			for(File file : lawToConvertFiles){
				String tifName = file.getName() + ".tif";
				File tifFile = new File(lawFolder, tifName);  
				try{
					startConvert(today, file, tifFile, true, false); //轉檔案並儲存資料庫
				}catch(Exception e){
					logger.error("", e);
					e.printStackTrace();
				}
			}
			
			logger.info("pdf to tiff converter stop to run");
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
			ErrorReport er = new ErrorReport();
			er.setErrHappenTime(new Date());
			er.setErrorType("ConvertPdf");
			er.setOldBatchName("");
			er.setReported(false);
			er.setException(true);
			er.setMessageBody("PDF轉換發生例外狀況：" + e.getMessage() + "。請查閱系統log" );
			er.setTitle("Convert Pdf");
			((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
		}finally{
			
		}
		logger.info("PDF2Tiff converter stop.");
	}

	private static void startConvert(Date today, File pdfFile,
			File tifFile, boolean law, boolean image) throws IOException, Exception {		
		File destFolder = new File(pdfFile.getParentFile(), "pdf2tif/");
		if(new File(destFolder, pdfFile.getName()).exists()){
			FilesUtils.forceDelete(new File(destFolder, pdfFile.getName()));
		}
		int pages = PdfFileUtil.getPDFPageCount(pdfFile.getAbsolutePath());
		if(pages == 0){
			ErrorReport er = new ErrorReport();
			er.setErrHappenTime(new Date());
			er.setErrorType("ConvertPdf");
			er.setOldBatchName("");
			er.setReported(false);
			er.setException(true);
			er.setMessageBody(pdfFile.getAbsolutePath() + "頁數為0，請檢查此pdf狀態" );
			er.setTitle("Convert Pdf");
			((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
			return;
		}
		logger.info("convert pdf :" + pdfFile.getAbsolutePath());
		boolean convertError = false;
		try{
		   PdfFileUtil.pdfToTiffCommand2(pdfFile, tifFile.getAbsolutePath()); //pdf轉tiff
		}catch(Exception e){
		   logger.error("", e);		 
		   convertError = true;
		}
		
		FilesUtils.copyFileToDirectory(pdfFile, destFolder, true); //把pdf也copy過去′作為參考
		
		List<ImgFile> imgFiles = null;

		//以下存入資料庫
		String dbName = "";		
		if(image && !law){
			dbName = "/image/" + tifFile.getName();			
		}else if(!image && law){
			dbName = "/law/" + tifFile.getName();
		}
		imgFiles = ((VoService) Constant.getContext().getBean("voServiceProxy")).getImgFilesByNm(dbName);
		ImgFile imgFile = null;
		if (imgFiles != null && imgFiles.size() > 0) {
			imgFile = imgFiles.get(0);
			imgFile.setNewCopy(false);
		} else {
			imgFile = new ImgFile();
			imgFile.setInsertDate(today);
			imgFile.setNewCopy(true);
		}
		imgFile.setLength(tifFile.length());
		imgFile.setFileNm(dbName);						
		imgFile.setReqPolicy(false);
		imgFile.setImage(image);
		imgFile.setLaw(law);
		imgFile.setCopyDate(today);
		imgFile.setCopySuccess(true);
		imgFile.setDpiX(JaiTiffImgProcess.DPI_X);
		imgFile.setDpiY(JaiTiffImgProcess.DPI_Y);
		imgFile.setErrorImage(convertError);
		imgFile.setExist(!convertError);
		imgFile.setFileDate(new Date(tifFile.lastModified()));
		imgFile.setHeight(null);
		imgFile.setPath(pdfFile.getAbsolutePath());
		imgFile.setLock(false);
		imgFile.setPostProcessedPath(tifFile.getAbsolutePath());
		imgFile.setUpdateDate(today);
		imgFile.setWidth(null);
		if(imgFile.getNewCopy() && !convertError)
			((VoService) Constant.getContext().getBean("voServiceProxy")).save(imgFile);
		else if(!imgFile.getNewCopy())
			((VoService) Constant.getContext().getBean("voServiceProxy")).update(imgFile);
		
		if(convertError){
			if(tifFile.exists())
			   FilesUtils.forceDelete(tifFile);
		}
		

	}
	
	private static FileFilter afpFilter = new FileFilter() {		
		public boolean accept(File file) {			
			//結尾是.afp且開頭不是caaa(迴歸件)
			if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".afp")){
				return true;
			}else{
				return false;
			}
		}
	};

	private static FileFilter pdfFilter = new FileFilter() {		
		public boolean accept(File file) {			
			//結尾是.afp且開頭不是caaa(迴歸件)
			if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".pdf")){
				return true;
			}else{
				return false;
			}
		}
	};
	
}
