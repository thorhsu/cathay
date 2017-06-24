package com.salmat.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import net.sourceforge.barbecue.output.OutputException;

public class BarCodeImgUtil {

	public static void main(String[] args) {
		try {
			//save(outputtingBarcode("9108998021", 0.5, 0.3, "*****98021"), "d:/tmp/policyNo2.png");
			save(outputtingBarcode("9108998021", 0.5, 0.17, null, false), "d:/tmp/policyNo.png");
			//save(outBarcode("9108998021", 0, 1, false), "d:/tmp/policyNo.png");
		} catch (BarcodeException | IOException | OutputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean save(BufferedImage image, String path) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			//image 轉成byte array1
			ImageIO.write(image, "png", new File(path));
			baos.flush();
			return true;
		} catch (IOException e) {			
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	public static BufferedImage rotate90ToRight( BufferedImage inputImage ){
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		BufferedImage returnImage = new BufferedImage( height, width , inputImage.getType()  );

		for( int x = 0; x < width; x++ ) {
			for( int y = 0; y < height; y++ ) {
				returnImage.setRGB(height - y - 1, x, inputImage.getRGB( x, y  )  );
	            //Again check the Picture for better understanding
			}
		}
		return returnImage;
	}

	public static BufferedImage outputtingBarcode(String barcodeStr, double wscale, double hscale, String displayStr) throws BarcodeException,
			IOException, OutputException {
		return outputtingBarcode(barcodeStr,
				wscale, hscale, displayStr, true);		
	}

	public static BufferedImage outputtingBarcode(String barcodeStr,
			double wscale, double hscale, String displayStr, boolean labelDisplay)
			throws BarcodeException, IOException, OutputException {
		Barcode barcode;
		barcode = BarcodeFactory.createCode128(barcodeStr);
		barcode.setLabel(displayStr);
		barcode.setDrawingText(labelDisplay);
		barcode.setResolution(600);
		BufferedImage bi = BarcodeImageHandler.getImage(barcode);
		BufferedImage scaledBi = new BufferedImage(
				(int) (bi.getWidth() * wscale),
				(int) (bi.getHeight() * hscale), bi.getType());
		Graphics2D g2 = scaledBi.createGraphics();

		g2.drawImage(bi, 0, 0, scaledBi.getWidth(), scaledBi.getHeight(), null);
		scaledBi = rotate90ToRight(scaledBi);
		// save(scaledBi, path);
		g2.dispose();
		return scaledBi;

	}	
	
	
	

}
