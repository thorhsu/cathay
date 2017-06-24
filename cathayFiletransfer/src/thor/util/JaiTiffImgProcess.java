package thor.util;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.media.jai.BorderExtender;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.RenderedOp;



import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codec.TIFFField;

public class JaiTiffImgProcess {

	public static final BorderExtender zeroBorder = BorderExtender.createInstance(BorderExtender.BORDER_ZERO);
	public static final float shrinkHeight = 6566; //影像高 pixel
	public static final float shrinkWidth = 4560;  //影像寬 pixel
	public static final float fixHeight = 7016;  //加邊框後的高 pixel
	public static final float fixWidth = 4960;  //加邊框後的寬 pixel
	public static int DPI_X = 600;
    public static int DPI_Y = 600;
	
    public static void processImg(File inputFile, File outputFile) throws IOException{
    	FileSeekableStream ss = new FileSeekableStream(inputFile);
		ImageDecoder dec = ImageCodec.createImageDecoder("tiff", ss, null);		
		int count = dec.getNumPages();				
		RenderedImage[] images = new RenderedImage[count];
		
		for (int i = 0; i < count; i++) {
			PlanarImage page = new RenderedImageAdapter(dec.decodeAsRenderedImage(i));				        			
			page = shrinkImage(2, 2, page);			
			images[i] = page;
		}
		
		saveAsMultipageTIFFCompression(images, outputFile.getAbsolutePath());
		
        ss.close();
        ss = null;
    }
    
	public static void main(String[] args) throws IOException {
		/*
		FileOutputStream fos = new FileOutputStream("D:/tmp/myFile.tif"); 

        RenderedOp src = JAI.create("fileload", "D:/tmp/電子行銷.tif");

        BufferedImage buf = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        buf.getGraphics().drawImage(src.getAsBufferedImage(), 0, 0, null);
        RenderedImage ri = (RenderedImage) buf;

        TIFFEncodeParam encodeParam = new TIFFEncodeParam ();
//        encodeParam.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);

        ImageEncoder enc = ImageCodec.createImageEncoder("TIFF", fos, encodeParam);
        enc.encode(ri);
		*/
		processImg(new File("D:/tmp/G500002137104.tif"), new File ("D:/tmp/G500002137104_1.tif"));
		/*
		args = new String[2];
		args[0] = "D:\\tmp\\AA76155658.9099336487.00.N";
		args[1] = "D:\\tmp\\AA76155658.9099336487.00.N.tif";
		FileSeekableStream ss = null;
		if(args != null && args.length == 2){			
		    ss = new FileSeekableStream(args[0]);
		}else{
			System.out.println("輸入輸出的檔案名稱必須輸入");
			System.exit(0);
		}
		ImageDecoder dec = ImageCodec.createImageDecoder("tiff", ss, null);
		int count = dec.getNumPages();		
		//System.out.println("This TIF has " + count + " image(s)");		
		RenderedImage[] images = new RenderedImage[count];
		for (int i = 0; i < count; i++) {
			PlanarImage page = new RenderedImageAdapter(dec.decodeAsRenderedImage(i));				        
			
			page = cutImage(page, 13, 12);
			float width = page.getWidth();
			float height = page.getHeight();
			page = shrinkImage((shrinkWidth / width), (shrinkHeight / height), page);
			int borderWidth = ((int)fixWidth - (int)shrinkWidth ) / 2 ;
			int borderHeight = ((int)fixHeight - (int)shrinkHeight ) / 2 ;
			page = doBorder(page, borderWidth, borderWidth, borderHeight, borderHeight, null);
			
			images[i] = page;
		}
		saveAsMultipageTIFF(images, args[1]);
		*/
		/*
		Image image = TiffChecker.getImage(args[1]);
		image.setDpi(600, 600);
		*/
		/*
		FileSeekableStream ss = null;
		if(args != null && args.length == 2){			
		    ss = new FileSeekableStream(args[0]);
		}else{
            
		}
		//System.out.println("This TIF has " + count + " image(s)");
		RenderedImage[] images = new RenderedImage[6];
		for(int i = 0 ; i < 6 ; i++){
			ss = new FileSeekableStream("c:\\tmp\\130511610160_00" + (i + 1) + ".tif");
		   ImageDecoder dec = ImageCodec.createImageDecoder("tiff", ss, null);
		   int count = dec.getNumPages();
		   PlanarImage page = new RenderedImageAdapter(dec.decodeAsRenderedImage(0));
		   images[i] = page;
		}
		

		saveAsMultipageTIFF(images, "C:\\tmp\\130511610160.tif");
		*/
	}
	

	
	
	
	
	
	//裁切影像，左右上下各切多少pixel
	public static PlanarImage cutImage(RenderedImage page, int cutWidth, int cutHeight){
		ParameterBlock paramsCrop = new ParameterBlock();
        paramsCrop.addSource(page);
        paramsCrop.add((float)cutWidth); // The x origin for each band
        paramsCrop.add((float)cutHeight); // The y origin for each band
        paramsCrop.add((float)(page.getWidth() - 2 * cutWidth)); // The width for each band
        paramsCrop.add((float)(page.getHeight() - 2 * cutHeight)); // The height for each band
        PlanarImage out = JAI.create("crop", paramsCrop, null);        		
		return out;
	}
	
	//影像放大縮小
	public static PlanarImage shrinkImage(float xScaleFactor, float yScaleFactor, RenderedImage image){
		ParameterBlock params = new ParameterBlock();
        params.addSource(image);        
        params.add(xScaleFactor); // x scale factor
        params.add(yScaleFactor); // y scale factor
        params.add(0.0F); // x translate
        params.add(0.0F); // y translate

        params.add(new InterpolationNearest());

        /* Create an operator to scale image1. */
        RenderedOp image2 = JAI.create("scale", params, null);
		return image2;
	}
	
	//將多頁的tiff變成單頁的tiff
	public static void multipagesTiffToSinglePage(String tiffFileName, String splitName) throws IOException{
		FileSeekableStream ss = new FileSeekableStream(tiffFileName);
		ImageDecoder dec = ImageCodec.createImageDecoder("tiff", ss, null);
		int count = dec.getNumPages();
		TIFFEncodeParam param = new TIFFEncodeParam();
		param.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);
		param.setLittleEndian(false); // Intel
		ParameterBlock pb = new ParameterBlock();

		for (int i = 0; i < count; i++) {
			RenderedImage page = dec.decodeAsRenderedImage(i);
			
			File file = new File(splitName + "_" + i + ".tif");
			System.out.println("Saving " + file.getCanonicalPath());
			pb.addSource(page);
			pb.add(file.toString());
			pb.add("tiff");
			pb.add(param);
			RenderedOp r = JAI.create("filestore", pb);
			r.dispose();
			
		}
	}
	
	//影像加邊框，boder 為null時是zeroBorder
	public static PlanarImage doBorder(RenderedImage imageToBorder, int leftPad, int rightPad, int topPad, int bottomPad, BorderExtender border) {
        ParameterBlock borderParam = new ParameterBlock();
        borderParam.addSource( imageToBorder );
        borderParam.add( leftPad );
        borderParam.add( rightPad );
        borderParam.add( topPad );
        borderParam.add( bottomPad );
        
        // options : zeroBorder, reflectBorder, wrapBorder, copyBorder, constantBorder
        if(border == null)
           borderParam.add(zeroBorder);
        else
           borderParam.add(border);
        // translate image to show all effect
        PlanarImage imageToTranslate = JAI.create( "border", borderParam  ); 
        ParameterBlock translateParam = new ParameterBlock();
        float xTrans = 2.0f * rightPad;
        float yTrans = 2.0f * bottomPad;
        translateParam.addSource(imageToTranslate);
        translateParam.add(xTrans);
        translateParam.add(yTrans);
        translateParam.add(new InterpolationNearest());
        // Create the output image by translating itself.
        return JAI.create("translate", translateParam );
        
    }

	//儲存成多頁式的tiff檔
	public static void saveAsMultipageTIFF(RenderedImage[] image,
			String filename) throws java.io.IOException {
				
		OutputStream out = new FileOutputStream(filename);
		TIFFEncodeParam param = new TIFFEncodeParam();
		param.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);
		param.setLittleEndian(true);
		param.setWriteTiled(false);

		TIFFField[] fields = new TIFFField[7];
		// PhotometricInterpretation
		TIFFField fieldPhotoInter = new TIFFField(262, TIFFField.TIFF_SHORT, 1,
				(Object) new char[] { 0 });
		fields[0] = fieldPhotoInter;

		// ResolutionUnit
		TIFFField fieldResUnit = new TIFFField(296, TIFFField.TIFF_SHORT, 1,
				(Object) new char[] { 2 });
		fields[1] = fieldResUnit;

		// XResolution
		TIFFField fieldXRes = new TIFFField(282, TIFFField.TIFF_RATIONAL, 1,
				(Object) new long[][] { { DPI_X, 1 } });
		fields[2] = fieldXRes;

		// YResolution
		TIFFField fieldYRes = new TIFFField(283, TIFFField.TIFF_RATIONAL, 1,
				(Object) new long[][] { { DPI_Y, 1 } });
		fields[3] = fieldYRes;

		// BitsPerSample
		TIFFField fieldBitSample = new TIFFField(258, TIFFField.TIFF_SHORT, 1,
			(Object) new char[] { 1 });
		fields[4] = fieldBitSample;

		// FilOrder
		TIFFField fieldFillOrder = new TIFFField(266, TIFFField.TIFF_SHORT, 1,
				(Object) new char[] { 1 });
		fields[5] = fieldFillOrder;

		// RowsPerStrip
		TIFFField fieldRowsStrip = new TIFFField(278, TIFFField.TIFF_LONG, 1,
				(Object) new long[] { 2200 });
		fields[6] = fieldRowsStrip;
		
		
		param.setExtraFields(fields);
		ImageEncoder encoder = ImageCodec
				.createImageEncoder("TIFF", out, param);
		Vector vector = new Vector();
		for (int i = 1; i < image.length; i++) {
			vector.add(image[i]);
		}
		param.setExtraImages(vector.iterator());
		encoder.encode(image[0]);
		out.close();
	}
	
	//儲存成多頁式的tiff檔，壓縮方法改
	public static void saveAsMultipageTIFFCompression(RenderedImage[] image,
			String filename) throws java.io.IOException {
				
		OutputStream out = new FileOutputStream(filename);
		TIFFEncodeParam param = new TIFFEncodeParam();
		param.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);
		param.setLittleEndian(true);
		param.setWriteTiled(false);

		TIFFField[] fields = new TIFFField[7];
		// PhotometricInterpretation
		TIFFField fieldPhotoInter = new TIFFField(262, TIFFField.TIFF_SHORT, 1,
				(Object) new char[] { 0 });
		fields[0] = fieldPhotoInter;

		// ResolutionUnit
		TIFFField fieldResUnit = new TIFFField(296, TIFFField.TIFF_SHORT, 1,
				(Object) new char[] { 2 });
		fields[1] = fieldResUnit;

		// XResolution
		TIFFField fieldXRes = new TIFFField(282, TIFFField.TIFF_RATIONAL, 1,
				(Object) new long[][] { { DPI_X, 1 } });
		fields[2] = fieldXRes;

		// YResolution
		TIFFField fieldYRes = new TIFFField(283, TIFFField.TIFF_RATIONAL, 1,
				(Object) new long[][] { { DPI_Y, 1 } });
		fields[3] = fieldYRes;

		// BitsPerSample
		TIFFField fieldBitSample = new TIFFField(258, TIFFField.TIFF_SHORT, 1,
			(Object) new char[] { 1 });
		fields[4] = fieldBitSample;

		// FilOrder
		TIFFField fieldFillOrder = new TIFFField(266, TIFFField.TIFF_SHORT, 1,
				(Object) new char[] { 1 });
		fields[5] = fieldFillOrder;

		// RowsPerStrip
		TIFFField fieldRowsStrip = new TIFFField(278, TIFFField.TIFF_LONG, 1,
				(Object) new long[] { 2200 });
		fields[6] = fieldRowsStrip;
		
		
		param.setExtraFields(fields);
		ImageEncoder encoder = ImageCodec
				.createImageEncoder("TIFF", out, param);
		Vector vector = new Vector();
		for (int i = 1; i < image.length; i++) {
			vector.add(image[i]);
		}
		param.setExtraImages(vector.iterator());
		encoder.encode(image[0]);
		out.close();
	}

}
