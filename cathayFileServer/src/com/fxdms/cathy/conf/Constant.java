package com.fxdms.cathy.conf;

import java.io.File;

import org.springframework.context.ApplicationContext;

import com.fxdms.rmi.service.VoService;

public class Constant {
	    
    private static String afpFolder;
    private static String pdfPwd;
    private static ApplicationContext context;
    private static VoService voService = null;    

	public static VoService getVoService() {
		return voService;
	}

	public void setVoService(VoService voService) {
		Constant.voService = voService;
	}

	public static String getAfpFolder() {
		return afpFolder;
	}

	public void setAfpFolder(String afpFolder) {
		Constant.afpFolder = afpFolder;
	}
          
	public static File getAfpDir(){
		if(afpFolder != null && !afpFolder.equals("")){
			return new File(afpFolder);
		}
		return null;
	}

	public static ApplicationContext getContext() {
		return context;
	}

	public static void setContext(ApplicationContext context) {
		Constant.context = context;
	}

	public static String getPdfPwd() {
		return pdfPwd;
	}

	public static void setPdfPwd(String pdfPwd) {
		Constant.pdfPwd = pdfPwd;
	}
		


}
