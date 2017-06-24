package com.fxdms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;


public class PdfCompare {
	static Logger logger = Logger.getLogger(PdfCompare.class);
    private static String differPdfPath = "C:/Program Files (x86)/DiffPDFc"; //寫死的，如果位置換，這裡也要改
	public static String getDifferPdfPath() {
		return differPdfPath;
	}

	public static void setDifferPdfPath(String differPdfPath) {
		PdfCompare.differPdfPath = differPdfPath;
	}

	public static boolean comparePdf(File pdf, File comparedPdf, File resultPdf) throws IOException, InterruptedException{
		
		//String execCommand = "\"" + differPdfPath + "/diffpdfc.exe\" -Hc -r " + resultPdf.getAbsolutePath() + " -s 200" + " " + pdf.getAbsolutePath() + " " + comparedPdf.getAbsolutePath();
		String execCommand = "\"" + differPdfPath + "/diffpdfc.exe\" -a -r " + resultPdf.getAbsolutePath() + " -s 200 " + pdf.getAbsolutePath() + " " + comparedPdf.getAbsolutePath();
		System.out.println(execCommand);
		logger.info(execCommand);
		Process process = Runtime.getRuntime().exec(execCommand);
		//normal messge reader
		BufferedReader stdInput = new BufferedReader(new 
	             InputStreamReader(process.getInputStream()));
		//error messge reader
	    BufferedReader stdError = new BufferedReader(new 
	             InputStreamReader(process.getErrorStream()));
	    String s;
	    while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
	    
	    // 讀取正常 message
        boolean noDifferent = false;
	    while ((s = stdInput.readLine()) != null) {
            System.out.println(new String(s.getBytes(),"ms950"));
            if(s.toLowerCase().indexOf("same") >= 0){
            	noDifferent = true;
            }
        }
        System.out.println("wait for exit");
		process.waitFor();
        process.destroy();		
        return noDifferent;		
	}
	
	public static void main(String [] args) throws IOException, InterruptedException{
		File oriPdf = new File("c:/tmp/quick.pdf");
		File comparedPdf = new File("c:/tmp/quick5T.pdf");
		File resultPdf = new File("c:/tmp/report.pdf");
		comparePdf(oriPdf, comparedPdf, resultPdf);
	}
}
