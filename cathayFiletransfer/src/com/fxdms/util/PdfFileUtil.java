package com.fxdms.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.Splitter;
import org.ghost4j.Ghostscript;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.SimpleRenderer;

import thor.util.JaiTiffImgProcess;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfIndirectObject;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.PdfWriter;


public class PdfFileUtil {

	private static final Logger logger = Logger.getLogger(PdfFileUtil.class);
    
	//把PDF依頁數切開的方法
	public static boolean partitionPdfDocument( int intPage,
			int endPage, String outputPath, PdfReader inputReader) {
		if (intPage == 0 && endPage == 0) {
			logger.info("起始頁和終止頁都是0，不需要轉換");
			return true;
		}
		Document document = null;
		PdfCopy copy = null;		
		boolean returnFlag = false;
		try {
			
			int n = inputReader.getNumberOfPages();
			if (n < endPage)
				return returnFlag;
			document = new Document(inputReader.getPageSize(1));			
			copy = new PdfCopy(document, new FileOutputStream(outputPath));
			document.open();
			copy.open();
			for (int i = intPage; i <= endPage; i++) {
				document.newPage();
				PdfImportedPage page = copy.getImportedPage(inputReader, i);
				copy.addPage(page);
			}
			returnFlag = true;
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (DocumentException e) {
			logger.error("", e);
			e.printStackTrace();
		} finally {
			if (copy != null)
				copy.close();
			if (document != null)
				document.close();			
			document = null;
			copy = null;
		}
		return returnFlag;
	}	
	
	public static void addPagesNumber(File file, File outfile) throws IOException, COSVisitorException{
		PDDocument doc = null;
		File rasFile = null;
		RandomAccessFile ras = null;
		try
		{
			rasFile = new File( UUID.randomUUID() + "");
			ras = new RandomAccessFile(rasFile, "rw");
			doc = PDDocument.load(file, ras);

		    List allPages = doc.getDocumentCatalog().getAllPages();
		    PDFont font = PDType1Font.HELVETICA;
		    float fontSize = 7.0f;

		    for( int i=0; i < allPages.size(); i++ )
		    {
		        PDPage page = (PDPage)allPages.get( i );
		        PDRectangle pageSize = page.findMediaBox();
		        
		        float stringWidth = font.getStringWidth( file.getName() + "_" + (i + 1) ) * fontSize / 1000f;
		        if(file.getName().toLowerCase().endsWith(".pdf"))
		        	stringWidth = font.getStringWidth( file.getName().substring(0, file.getName().length() - 4) + "_" + (i + 1) ) * fontSize / 1000f;
		        // calculate to center of the page
		        int rotation = page.findRotation(); 		        
		        float pageWidth =  pageSize.getWidth();
		        float pageHeight =  pageSize.getHeight();
		        double centeredXPosition =   10;
		        double centeredYPosition =   10;
		        // append the content to the existing stream
		        PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true,true);
		        contentStream.beginText();
		        // set font and font size
		        contentStream.setFont( font, fontSize );
		        // set text color to red
		        contentStream.setNonStrokingColor(Color.GRAY);
	            contentStream.setTextTranslation(centeredXPosition, centeredYPosition);
		        
		        if(file.getName().toLowerCase().endsWith(".tmp") || file.getName().toLowerCase().endsWith(".pdf"))
		        	contentStream.drawString( file.getName().substring(0, file.getName().length() - 4)  + "_" + (i + 1));
		        else
		        	contentStream.drawString( file.getName() + "_" + (i + 1));
		        contentStream.endText();
		        contentStream.close();
		    }

		    doc.save( outfile );
		}
		finally
		{   if(ras != null){
			   ras.close();
		    }
		    if( doc != null ){
		        doc.close();
		    }
		    if(rasFile != null)
		    	rasFile.delete();
		}
	}

	//取得pdf頁數
	public static int getPDFPageCount(String path) {		  
		PDDocument pdfDoc = null;
		File rasFile = null;
		RandomAccessFile ras = null;
		try {			
			rasFile = new File( UUID.randomUUID() + "");
			ras = new RandomAccessFile(rasFile, "rw");
			pdfDoc = PDDocument.load(path, ras);
			int ret = pdfDoc.getNumberOfPages();
			return ret;
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
			return 0;		  
		} finally {
			try {
			   if(ras != null)
			       ras.close();	
			   if(pdfDoc != null)
				   pdfDoc.close();
		    } catch (IOException e) {
			 	   logger.error("", e);
				   e.printStackTrace();
		    }
		    ras = null;
		    pdfDoc = null;
			if(rasFile != null)
				rasFile.delete();
		}
	}
		
	
	//把PDF依頁數切開的方法
	public static boolean partitionPdfFile(String filepath, int intPage,
			int endPage, String outputPath) {
		if (intPage == 0 && endPage == 0) {
			logger.info("起始頁和終止頁都是0，不需要轉換");
			return true;
		}
		Document document = null;
		PdfCopy copy = null;
		PdfReader reader = null;
		boolean returnFlag = false;
		try {
			reader = new PdfReader(filepath);
			int n = reader.getNumberOfPages();
			if (n < endPage)
				return returnFlag;
			document = new Document(reader.getPageSize(1));
			copy = new PdfCopy(document, new FileOutputStream(outputPath));
			document.open();
			for (int i = intPage; i <= endPage; i++) {
				document.newPage();
				PdfImportedPage page = copy.getImportedPage(reader, i);
				copy.addPage(page);
			}
			returnFlag = true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				reader.close();
			if (document != null)
				document.close();
			if (copy != null)
				copy.close();
		}
		return returnFlag;
	}

	//結合PDF的方法
	public static void doMergePdf(List<File> files, String outputPath) {
		PDFMergerUtility ut = new PDFMergerUtility();
		for(File file: files){
			ut.addSource(file);
		}
		ut.setDestinationFileName(outputPath);
		try {
			ut.mergeDocuments();
		} catch (COSVisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	public static void doMergeBigPdf(List<File> files, String outputPath) {
		File forMergepdfFile = files.get(0);
		PDDocument forMerge = null;
		File forMergerasFile = null;
		RandomAccessFile forMergeras = null;
		try {			
			forMergerasFile = new File( UUID.randomUUID() + "");
			forMergeras = new RandomAccessFile(forMergerasFile, "rw");
			forMerge = PDDocument.load(forMergepdfFile, forMergeras);
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
					  
		}
		List<PDDocument> pdDocs = new ArrayList<PDDocument>();
		List<File> tmpFiles = new ArrayList<File>();
		for(int i = 1 ; i < files.size() ;i++){
			File pdfFile = files.get(i);					
			PDDocument pdfDoc = null;
			File rasFile = null;
			RandomAccessFile ras = null;
			try {			
				rasFile = new File( UUID.randomUUID() + "");
				tmpFiles.add(rasFile);
				ras = new RandomAccessFile(rasFile, "rw");
				pdfDoc = PDDocument.load(pdfFile, ras);
				pdDocs.add(pdfDoc);
			} catch (IOException e) {
				logger.error("", e);
				e.printStackTrace();						  
			} 
		}
		try {
			mergeDocuments(forMerge, pdDocs, outputPath);
		} catch (COSVisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				forMerge.close();
				forMergeras.close();
				FilesUtils.forceDelete(forMergerasFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
		int i = 0;
		for(PDDocument pdd : pdDocs){
		    try {
				pdd.close();
			    FileUtils.forceDelete(tmpFiles.get(i));	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}					
		    i++;
		}
	}

	//結合PDF同時加密碼
	public static void doMergePdfWithPassword(List<File> list, File outputFile,
			String password) throws DocumentException, IOException {
		String tmpFileNm = outputFile.getAbsolutePath() + ".tmp";
		File tmpFile = new File(tmpFileNm);
		doMergePdf(list, tmpFile.getAbsolutePath());
		encryptPdf(tmpFileNm, outputFile.getAbsolutePath(), password);
		if(tmpFile.exists())
			tmpFile.delete();
	}

	//pdf加密
	public static void encryptPdf(String src, String dest, String password)
			throws IOException, DocumentException {
		PdfReader reader = new PdfReader(src);
		reader.removeUsageRights();
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
		stamper.setEncryption(password.getBytes(), password.getBytes(),
				PdfWriter.ALLOW_PRINTING, PdfWriter.STANDARD_ENCRYPTION_128);
		stamper.close();
	}

	//把pdf依照頁數切開，一頁就是一個pdf
	public static List<PDDocument> splitByPages(String path){
		PDDocument pddDocument = null;
		try{
		   pddDocument=PDDocument.load(new File(path));
		   Splitter splitter = new Splitter();
		   splitter.setSplitAtPage(1);
		   return splitter.split(pddDocument);
		}catch(Exception ex){
		   ex.printStackTrace();
		   return null;
		}finally{
			if(pddDocument != null)
				try {
					pddDocument.close();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
		}		
	} 
	
	//**此方法會把pdf全讀進記憶體中，所以會有heap memory的限制，讀入過大的pdf會有outofmemory的問題
    //大檔案時pdDocument必須使用PDDocument.load(filePath, RandomAccessFile)所產生的PDDocument才行，RandomAccessFile作為暫存檔
	public static void splitDocument(int startPage, int endPage, String outPath, PDDocument pdDocument) throws IOException, COSVisitorException {
           PDDocument document = splitDocument(startPage, endPage, pdDocument);
		   if(document != null){
           	   document.save(outPath);
        	   document.close();   
          }            	   
	}
	
	//把pdf裡的文字取出的方法
	public static String extractTxt(PDDocument pddDocument){		
		try{
		   PDFTextStripper textStripper=new PDFTextStripper();
		   return textStripper.getText(pddDocument);
		    
		}catch(Exception ex){
		   ex.printStackTrace();
		   return null;
		}
	}
	//有分頁時，splitter切出的有可能會分成一個以上的PDDocument，所以要再merger起來
	public static PDDocument splitDocument(int startPage, int endPage, PDDocument pdDocument) throws IOException, COSVisitorException {
		   Splitter splitter = new Splitter();
		   splitter.setStartPage(startPage);
		   splitter.setEndPage(endPage);
		   splitter.setSplitAtPage(endPage - startPage + 1);
		   List<PDDocument> documents = splitter.split(pdDocument);
		   PDFMergerUtility ut = new PDFMergerUtility();
		   PDDocument documentForMerge = null;
		   if(documents != null){
               for(PDDocument document : documents){
				   if(documentForMerge == null){
					   documentForMerge = document;
				   }else{
					   ut.appendDocument(documentForMerge, document);
					   document.close();
				   }
			   }
               return documentForMerge;
		   }
		   return null;
	}
	
	public static PDDocument mergeDocuments(PDDocument documentForMerge, List<PDDocument> documents4Merge) throws IOException, COSVisitorException{
        if(documents4Merge != null){
        	PDFMergerUtility ut = new PDFMergerUtility();
            for(PDDocument document : documents4Merge){
      	      ut.appendDocument(documentForMerge, document);
            }
     	    return documentForMerge;
        }		
        return null;
	}
    
	//大檔時使用的merge檔案方法，加密碼
	public static void mergeDocumentsWithpassword(PDDocument documentForMerge, List<PDDocument> documents4Merge, String outPath, String password) throws IOException, COSVisitorException, CryptographyException{
        if(documents4Merge != null){
        	documentForMerge = mergeDocuments(documentForMerge, documents4Merge);
        	documentForMerge.encrypt(password, password);
     	    documentForMerge.save(outPath);
        }		
	}
	//大檔時使用的merge檔案方法，不加密碼
	public static void mergeDocuments(PDDocument documentForMerge, List<PDDocument> documents4Merge, String outPath) throws IOException, COSVisitorException{
        if(documents4Merge != null){
        	documentForMerge = mergeDocuments(documentForMerge, documents4Merge);
            documentForMerge.save(outPath);                 
        }		
	}

	
	public static void main(String args[]) {
		try {
			//pdfToTiff(new File("D:\\tmp\\IU1040804.pdf"), "D:\\tmp\\IU1040804.tif");
			File[] files = new File("d:/tmp").listFiles(FileFilterImpl.getFileFilter());
			for(File  file : files)
			   pdfToTiffCommand2(file, new File(file.getParent(), file.getName() + ".g4.tif").getAbsolutePath());
			files = new File("d:/tmp/law").listFiles(FileFilterImpl.getFileFilter());
			//manipulatePdf("D:\\tmp\\health01A_1.pdf", "D:\\tmp\\health01A_test.pdf");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		String path = "D:/tmp/CAAAB201406010001_1/CAAAB201406010001_1.pdf";
		PDDocument pdfDoc = null;
		File rasFile = null;
		RandomAccessFile ras = null;
		try {			
			rasFile = new File( UUID.randomUUID() + "");
			ras = new RandomAccessFile(rasFile, "rw");
			pdfDoc = PDDocument.load(path, ras);
			splitDocument(1218, 1293, "D:/tmp/test20.pdf", pdfDoc);
			
		} catch (IOException | COSVisitorException e) {
			logger.error("", e);
			e.printStackTrace();
					  
		} finally {
			try {
			   if(ras != null)
			       ras.close();	
			   if(pdfDoc != null)
				   pdfDoc.close();
		    } catch (IOException e) {
			 	   logger.error("", e);
				   e.printStackTrace();
		    }
		    ras = null;
		    pdfDoc = null;
			if(rasFile != null)
				rasFile.delete();
		}
		
		/*
		try {
			addPagesNumber(new File("d:/tmp/test.pdf"), new File("D:/tmp/test2.pdf"));
		} catch (COSVisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<File> fileList = new ArrayList<File>();
		for(int i = 1 ; i <= 9 ; i++){
			File file = new File("D:/tmp", "test" + i + ".pdf");
			fileList.add(file);			
		}
		doMergeBigPdf(fileList, "D:/tmp/mergetest.pdf");
        */
         
	}
	
	
	//轉換字型為內篏字型
	public static void manipulatePdf(String src, String dest) throws IOException, DocumentException {
		
	    
		String FONT = "C:\\WINDOWS\\Fonts\\TIMES.TTF";
	    /** The name of the special font. */
	    String FONTNAME = "Times New Roman";
	    // the font file
	    java.io.RandomAccessFile raf = new java.io.RandomAccessFile(FONT, "r");
	    byte fontfile[] = new byte[(int)raf.length()];
	    raf.readFully(fontfile);
	    raf.close();
	    // create a new stream for the font file
	    PdfStream stream = new PdfStream(fontfile);
	    stream.flateCompress();
	    stream.put(PdfName.LENGTH1, new PdfNumber(fontfile.length));
	    // create a reader object
	    PdfReader reader = new PdfReader(src);
	    int n = reader.getXrefSize();
	    PdfObject object;
	    PdfDictionary font;
	    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
	    PdfName fontname = new PdfName(FONTNAME);
	    for (int i = 0; i < n; i++) {
	        object = reader.getPdfObject(i);
	        if (object == null || !object.isDictionary())
	            continue;
	        font = (PdfDictionary)object;
	        
	        if (PdfName.FONTDESCRIPTOR.equals(font.get(PdfName.TYPE))
	            && fontname.equals(font.get(PdfName.FONTNAME))) {
	            PdfIndirectObject objref = stamper.getWriter().addToBody(stream);
	            font.put(PdfName.FONTFILE2, objref.getIndirectReference());
	        }	        
	    }
	    stamper.close();
	    reader.close();
	}

	public static void pdfToTiffJna(File pdfFile, String outputTiffPath) throws Exception{
		Ghostscript gs = Ghostscript.getInstance();
		//-dQUIET  -dNOPAUSE -r600 -sDEVICE=tiffg4 -dBATCH -sOutputFile=D:\tmp\health01A.tif D:\tmp\health01A.pdf
		String[] args = new String[7];
		args[0] = "-dQUIET"; 
		args[1] = "-dNOPAUSE";
		args[2] = "-r600";
		//args[3] = "-sDEVICE=tiff48nc";
		args[3] = "-sDEVICE=tiffg4";
		//args[4] = "-sCompression=lzw";
		args[4] = "-dBATCH";
		args[5] = "-sOutputFile=" + outputTiffPath;
		args[6] =  pdfFile.getAbsolutePath();
		gs.initialize(args);
		gs.exit();		
		gs = null;
	}
	
	public static void pdfToTiffCommand(File pdfFile, String outputTiffPath) throws Exception{
		//gswin32c -dNOPAUSE -q -r600 -sDEVICE=tiffg4 -dBATCH -sOutputFile=D:\tmp\health01A.tif D:\tmp\health01A.pdf
		String cmd = "D:\\cathayTransfer\\ghostscript\\gs9.18\\bin\\gswin64c.exe -dNOPAUSE -q -r600 -sDEVICE=tiffgray -sCompression=pack -dBATCH -sOutputFile=" + outputTiffPath + " " +  pdfFile.getAbsolutePath();
		//String cmd = "C:\\Program Files\\gs\\gs9.18\\bin\\gswin32c.exe -dNOPAUSE -q -r600 -sDEVICE=tiffgray -sCompression=lzw -dBATCH -sOutputFile=" + outputTiffPath + " " +  pdfFile.getAbsolutePath();
		
		logger.info(cmd);
		int exitVal = 1;
		try {
	        exitVal = Runtime.getRuntime().exec(cmd).waitFor();
	        if (exitVal != 0) {
	        	logger.error("eror happened exitVal is :" + exitVal);
		        throw new Exception("GhostScript error!!!!");
		    }
	    } catch (Exception e) {
	        logger.error("", e);
	        throw e;
	    }
	}
	
	public static void pdfToTiffCommand2(File pdfFile, String outputTiffPath) throws Exception{
		//gswin32c -dNOPAUSE -q -r600 -sDEVICE=tiffg4 -dBATCH -sOutputFile=D:\tmp\health01A.tif D:\tmp\health01A.pdf
		String cmd = "D:\\cathayTransfer\\ghostscript\\gs9.18\\bin\\gswin64c.exe -dNOPAUSE -q -r600 -sDEVICE=tiffg4 -dBATCH -sOutputFile=" + outputTiffPath + " " +  pdfFile.getAbsolutePath();
		//String cmd = "C:\\Program Files\\gs\\gs9.18\\bin\\gswin32c.exe -dNOPAUSE -q -r600 -sDEVICE=tiffg4 -dBATCH -sOutputFile=" + outputTiffPath + " " +  pdfFile.getAbsolutePath();
		
		logger.info(cmd);
		int exitVal = 1;
		try {
	        exitVal = Runtime.getRuntime().exec(cmd).waitFor();
	        if (exitVal != 0) {
	        	logger.error("eror happened exitVal is :" + exitVal);
		        throw new Exception("GhostScript error!!!!");
		    }
	    } catch (Exception e) {
	        logger.error("", e);
	        throw e;
	    }
	    
		
	}
	
	public static void pdfToTiff(File pdfFile, String outputTiffPath) throws Exception{
		PDFDocument document = null;
		 try {
			 // load PDF document
	         document = new PDFDocument();
	         document.load(pdfFile);
	 
	         // create renderer
	         SimpleRenderer renderer = new SimpleRenderer();
	         // set resolution (in DPI)
	         renderer.setResolution(600);
	 
	         // render
	         List<Image> images = renderer.render(document);
	         RenderedImage[] renderedImages = new RenderedImage[images.size()];
	         for(int i = 0 ; i < images.size() ; i++)
	        	 renderedImages[i] = (RenderedImage) images.get(i);
	 
	         // write images to files to disk as PNG
	         try {
	           	JaiTiffImgProcess.saveAsMultipageTIFFCompression(renderedImages, outputTiffPath);
	         
	         } catch (IOException e) {
	             System.out.println("ERROR: " + e.getMessage());
	             e.printStackTrace();
	             throw e;
	         }
	           
	 
	       } catch (Exception e) {
	            System.out.println("ERROR: " + e.getMessage());
	            throw e;
	       }finally{
	    	   
	       }
	}

}
