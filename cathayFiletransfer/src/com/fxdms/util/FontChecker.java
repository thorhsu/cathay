package com.fxdms.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import com.fxdms.cathy.conf.Constant;

public class FontChecker {

	private static String fontCheckerFolder = "D:\\fubonTransferData\\fontChecker\\";
	static Logger logger = Logger.getLogger(FontChecker.class);
	private static byte[] imageForCheck = null;
	private static Constant constant = new Constant();

	//載入圈圈圖
	static {
		try {			
			InputStream is = new ClassPathResource("/com/fxdms/util/difficultFont.jpeg")
					.getInputStream();
			imageForCheck = new byte[is.available()];
			is.read(imageForCheck);
            is.close();
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
		}
	}

	public static boolean imagesEq(BufferedImage image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			//image 轉成byte array1
			ImageIO.write(image, "jpeg", baos);
			baos.flush();
			byte[] imageAsRawBytes1 = baos.toByteArray();
			baos.close();
			
			return byteArrayEquals(imageAsRawBytes1, imageForCheck);
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
			return false;
		}
	}

	public static boolean byteArrayEquals(byte[] b1, byte[] b2) {
		boolean equals = true;
		if (b1.length != b2.length) {
			equals = false;
		} else {
			for (int i = 0; i < b1.length; i++) {
				if (b1[i] != b2[i]) {
					equals = false;
					break;
				}
			}
		}
		return equals;
	}

	public static boolean checkDiffcultTxt(File file) {
		boolean checkOver = false;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis, "ms950");
			br = new BufferedReader(isr);
			String line = null;
			int lineIndex = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("S0001") || line.startsWith("S0002")
						|| line.startsWith("A0009")) {
					line = line.replaceAll("[,.:_()|& /@~\\-a-zA-Z0-9 ]+", "");
					line = StringUtils.remove(line, " ");
					line = StringUtils.remove(line, "*");
                    System.out.println(line);
					if (!line.equals("") && checkDifficultString(line.toCharArray())) {
						//if (!line.equals("") && checkDifficultString(line)) {	
						checkOver = true;
						break;
					}
				}
			}

		} catch (Exception e) {
			checkOver = true;
			logger.error("", e);
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			if (isr != null)
				try {
					isr.close();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			fis = null;
			isr = null;
			br = null;
		}
		return checkOver;
	}
	public static void main(String[] args) {

		// fontCheckerFolder = "D:\\fubonTransferData\\fontChecker\\";
		String test = "蘇　秋　月||涂　　";
		if(args != null && args.length > 0){
			File file = new File(args[0]);
			System.out.println(checkDiffcultTxt(file));
		}else{
		   //System.out
				//.println(checkDifficultString(test.toCharArray()));
			System.out.println(checkDifficultString(test.toCharArray()));
		}

	}

	                                   	
	public synchronized static boolean checkDifficultString(char[] charArr) {
		boolean difficultFont = false;
		File fontFile = new File(constant.getFontFilePath());
		//原本的字型
		Font fontOri = new Font("serif", Font.TRUETYPE_FONT, 24);

		Font fontDifficult = null;
		/*
		try {			
			fontDifficult = Font.createFont(Font.TRUETYPE_FONT, fontFile);
			fontDifficult = fontDifficult.deriveFont(Font.TRUETYPE_FONT, 24);			
		} catch (FontFormatException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
		}
		*/
		// create the FontRenderContext object which helps us to measure the
		// text
		FontRenderContext frc = new FontRenderContext(null, true, true);

		for (char character : charArr) {
			if(Integer.toHexString(character).toLowerCase().equals("fffd"))
				continue;
			//如果原本有的字型就有，就跳到下個字元 
			//沒有的話，進行圈圈檢查
			//擴充與原本字型都找不到時，回傳true
			//原本字型找不到，但擴充字找得到時，做圈圈檢查
			if (fontOri.canDisplay(character)) {			
			}else if ( fontDifficult != null && !fontDifficult.canDisplay(character)) {
				return true;
			}else if( fontDifficult != null){
				// get the height and width of the text
				String displayStr = character + "";
				Rectangle2D bounds = fontDifficult.getStringBounds(displayStr, frc);
				int w = (int) bounds.getWidth();
				int h = (int) bounds.getHeight();
				BufferedImage image = new BufferedImage(w, h,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g = image.createGraphics();
		        g.setColor(Color.WHITE);
		        g.fillRect(0, 0, w, h);
		        g.setColor(Color.BLACK);
		        g.setFont(fontDifficult);
		        
		        g.drawString(displayStr, (float) bounds.getX(), (float) -bounds.getY());
		        //releasing resources
		        g.dispose();
				
				difficultFont = imagesEq(image);
				if(difficultFont)
					return difficultFont;
			}else if(!fontOri.canDisplay(character) ){
				return true;
			}
		}
		return false;
	}

	public synchronized static boolean checkDifficultString(String txt) {
		// Process p =
		// Runtime.getRuntime().exec("./webcontent/web-inf/streamEDP/bin/StreamEDP.bat F:\\XXTest.AFP");
		String execCommand = fontCheckerFolder + "pCheckEudc.exe " + txt;
		logger.info(execCommand);
		int exitValue = 0;
		try {
			Process p = Runtime.getRuntime().exec(execCommand);
			BufferedWriter buffOut = new BufferedWriter(new OutputStreamWriter(
					p.getOutputStream()));
			BufferedReader buffIn = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			buffOut.flush();

			String output = null;

			
			while ((output = buffIn.readLine()) != null) {

			}

			buffOut.close();
			buffIn.close();

			buffOut.close();
			buffIn.close();
			exitValue = p.exitValue();
			logger.info("finally:" + exitValue);

		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
		}
		if (exitValue == 1)
			return false;
		else
			return true;
	}
}
