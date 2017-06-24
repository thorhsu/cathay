package com.fxdms.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;

public class TiffChecker {
	static Logger logger = Logger.getLogger(TiffChecker.class);
	
	public static Image getImage(String file){
		    Image image = null;
			try {
				image = Image.getInstance(file);
			} catch (Exception e) {
				logger.error("", e);
				e.printStackTrace();
			} 
			return image;
	}
	
	
	
    public  static boolean checkTiff(Image image, Integer resolution){
    	boolean qualifiedImg = true;
    	try {
			int dpiX = image.getDpiX();
			int dpiY = image.getDpiY();
			logger.info("image DpiY is:" + dpiY + " and image DpiX is:" + dpiX);
			if(resolution != null && (dpiX < resolution || dpiY < resolution ))
				qualifiedImg = false;
		} catch (Exception e) {
			qualifiedImg = false;
			e.printStackTrace();
			logger.error("", e);
		}
    	
    	return qualifiedImg;
    }
    
    public  static boolean checkWidthAndHeight(Image image, Integer widthMax, Integer widthMin, Integer heightMax, Integer heightMin){
    	boolean qualifiedImg = true;
    	try {
			if(widthMax == null || widthMin == null || heightMax == null || heightMin == null)
				qualifiedImg = true;
			else if(widthMax == 0 && widthMin == 0 && heightMin == 0  &&  heightMax == 0)
				return true;
			else if(widthMax == 0 && widthMin == 0 && heightMin <= image.getHeight() &&image.getHeight() <= heightMax)
				return true;
			else if(heightMax == 0 && heightMin == 0 && widthMin <= image.getWidth() &&image.getWidth() <= widthMax)
				return true;				
			else if( widthMin <= image.getWidth() && image.getWidth() <= widthMax && heightMin <= image.getHeight() && image.getHeight() <= heightMax)
				qualifiedImg = true;
			else 
				qualifiedImg = false;
				
		} catch (Exception e) {
			qualifiedImg = false;
			e.printStackTrace();
			logger.error("", e);
		}
    	
    	return qualifiedImg;
    }
    
    public  static boolean checkTiff(Image image){
    	return checkTiff(image, 300);
    }
	public static void main(String [] args){
		   File [] files = new File[1];
           files[0] = new File("d:/fubon/imagebuffer/");
           FileWriter fw = null;
           try {
			   fw = new FileWriter("d:/fubonTransferData/errorImglog.log");
		   } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		   }
           for(File dir: files){
        	   File[] allFile = dir.listFiles();
        	   for(File file : allFile){
        		   if(file.isFile()){
        			   Image image = getImage(file.getPath());
        			   if(image != null){
        			   String line = "";
        			   if(image.getDpiX() < 300 || image.getDpiY() < 300){
                            line += file.getPath() + ":" + " dpiX is " + image.getDpiX() + " DPI and dpiY is " + image.getDpiY() + " DPI\r\n";        				   
        			   }
        			   if(!(2457 <= image.getWidth() && image.getWidth() <= 2504 )){
        				    line += file.getPath() + ":" + " width is " + image.getWidth() + " not between 2457 to 2504\r\n";
        			   }
        			   if(!(3484 <= image.getHeight() && image.getHeight() <= 3531 )){
        				   line += file.getPath() + ":" + " height is " + image.getHeight() + " not between 3484 to 3531\r\n";
        			   }
        			   if(!"".equals(line)){
						   try {
							  fw.write(line);
							  fw.write("\r\n");
						   } catch (IOException e) {
							  // TODO Auto-generated catch block
							  e.printStackTrace();
						  }
						   System.out.println(line);
        			   }
        			   }
        		   }if(file.isDirectory()){
        			   File [] subFiles = file.listFiles();
        			   for(File subFile : subFiles){
                		   if(subFile.isFile()){
                			   Image image = getImage(subFile.getPath());
                			   if(image != null){
                			   String line = "";
                			   if(image.getDpiX() < 300 || image.getDpiY() < 300){
                                    line += subFile.getPath() + ":" + " dpiX is " + image.getDpiX() + " DPI and dpiY is " + image.getDpiY() + " DPI\r\n";        				   
                			   }
                			   if(!(2457 <= image.getWidth() && image.getWidth() <= 2504 )){
                				    line += subFile.getPath() + ":" + " width is " + image.getWidth() + " not between 2457 to 2504\r\n";
                			   }
                			   if(!(3484 <= image.getHeight() && image.getHeight() <= 3531 )){
                				   line += subFile.getPath() + ":" + " height is " + image.getHeight() + " not between 3484 to 3531\r\n";
                			   }
                			   if(!"".equals(line)){
        						   try {
        							  fw.write(line);
        							  fw.write("\r\n");
        						   } catch (IOException e) {
        							  // TODO Auto-generated catch block
        							  e.printStackTrace();
        						  }
        						   System.out.println(line);
                			   }
                			   }
                		   }
                	   }
        		   }
        	   }
           }
           if(fw != null)
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	   
           
           
		
	}

}
