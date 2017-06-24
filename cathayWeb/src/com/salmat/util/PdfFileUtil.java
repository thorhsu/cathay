package com.salmat.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.Splitter;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
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
	
	/**
	 * pdf解密
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     */
	 public static void decryptPdf(String src, File dest, String password) throws DocumentException, IOException {
	        PdfReader reader = null ;
	        PdfStamper stamper = null;
	        try{
	           reader = new PdfReader(src, password.getBytes());
	           stamper = new PdfStamper(reader, new FileOutputStream(dest));
	        }catch(DocumentException e){
	        	e.printStackTrace();
	        	logger.error("", e);
	        	throw e;
	        }finally{
	           if(stamper != null)
	              stamper.close();
	           if(reader != null)
	              reader.close();
	        }
	 }

	//把pdf依照頁數切開，一頁就是一個pdf
	public static List<PDDocument> splitByPages(PDDocument pddDocument){		
		try{		
		   Splitter splitter = new Splitter();
		   splitter.setSplitAtPage(1);
		   return splitter.split(pddDocument);
		}catch(Exception ex){
		   ex.printStackTrace();
		   return null;
		}finally{
			/*
			if(pddDocument != null)
				try {
					pddDocument.close();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
			*/
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
	
	public static void mergeDocumentsWithpassword(PDDocument documentForMerge, List<PDDocument> documents4Merge, String outPath, String password) throws IOException, COSVisitorException, CryptographyException{
        if(documents4Merge != null){
        	documentForMerge = mergeDocuments(documentForMerge, documents4Merge);
        	documentForMerge.encrypt(password, password);
     	    documentForMerge.save(outPath);
        }		
	}
	
	public static void mergeDocuments(PDDocument documentForMerge, List<PDDocument> documents4Merge, String outPath) throws IOException, COSVisitorException{
        if(documents4Merge != null){
        	documentForMerge = mergeDocuments(documentForMerge, documents4Merge);
            documentForMerge.save(outPath);                 
        }		
	}

	
	public static void main(String args[]) {
		PDDocument pddDocument = null;
		try{
		   pddDocument=PDDocument.load(new File("D:\\tmp\\06_SI_2014-10-17_GROUP_FromBGB1_card1.pdf"));
		}catch(Exception e){
			e.printStackTrace();
		}
		List<PDDocument> list = splitByPages(pddDocument);
		String txt = extractTxt(list.get(80));
		String[] lines = txt.split(System.getProperty("line.separator"));		                                             
		for(String line : lines)
		   System.out.println(line);
		
		/*
		try {
			PdfFileUtil.decryptPdf("D:\\tmp\\scb14070438000.pdf", new File("D:\\tmp\\test1.pdf"), "P221265560");
		} catch (DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			PDDocument document = PDDocument.load("C:\\tmp\\0210000_Consolidate_2013-01-31.PDF");
			PDDocumentOutline root = document.getDocumentCatalog().getDocumentOutline();
			/*
			PDOutlineItem item = root.getFirstChild();
		    while( item != null )
		      {
		          System.out.println( "Item:" + item.getTitle() );
		          PDOutlineItem child = item.getFirstChild();
		          while( child != null )
		          {
		              System.out.println( "    Child:" + child.getTitle() );		              
		              child = child.getFirstChild();
		              
		          }
		          System.out.println( "finished" );
		          item = item.getNextSibling();
		      }
		    */
			/*
			PDFTextStripper textStripper=new PDFTextStripper();		
			
			//System.out.println(textStripper.getText(document));
			
			FileWriter fw = new FileWriter("c:\\tmp\\test2.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			//bw.write(textStripper.getText(document));
			textStripper.writeText(document, bw);
			bw.flush();
			bw.close();
			fw.close();
			/*
			document = PDDocument.load("C:\\tmp\\CVS_Consolidate_3240001_Jan25.pdf");
			textStripper=new PDFTextStripper();			 
			System.out.println(textStripper.getText(document));
			*/
		/*
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		//System.out.println(extractTxt("D:\\scbEdd\\backup\\20120726\\x1342172191023_11.417.mm.mm.out.pdf"));
//		   List<Integer> pages4accounts = new ArrayList<Integer>();
		/*
		   PdfReader document = null;
		   try {			   
			   document = new PdfReader("D:\\scbEdd\\tmpPdf\\ESC0824.pdf");
			   PdfFileUtil.partitionPdfDocument(1, 2, "D:\\scbEdd\\tmpPdf\\test1.pdf", document);
			   partitionPdfDocument(3, 4, "D:\\scbEdd\\tmpPdf\\test2.pdf", document);
			   partitionPdfDocument(7, 12, "D:\\scbEdd\\tmpPdf\\test3.pdf", document);
		   } catch (Exception e) {
			   // TODO Auto-generated catch block
			   e.printStackTrace();
		   }
		   */
		/*
		   PDDocument pdd = null;
		   try{
		      RandomAccessFile ras = new RandomAccessFile(new File("D:\\scbEdd\\tmpPdf\\pdftemp"), "rw");
		      PDDocument dm1 = PDDocument.load("D:\\scbEdd\\testDM\\edd_dm_1.pdf");
		      PDDocument dm2 = PDDocument.load("D:\\scbEdd\\testDM\\edd_dm_2.pdf");
		      PDDocument tac = PDDocument.load("D:\\scbEdd\\tacPdf\\tac_090721.pdf");
		      List<PDDocument> list = new ArrayList<PDDocument>();
		      list.add(dm1);
		      list.add(dm2);
		      list.add(tac);
		      pdd = PDDocument.load("D:\\scbEdd\\tmpPdf\\epl0906.pdf", ras);
		      PDDocument document = PdfFileUtil.splitDocument(81, 82,  pdd);
		      //同一個document切兩次並加密後，條碼會消失。如果沒加密就不會，原因不明，應該是pdf box的bug
		      PdfFileUtil.mergeDocumentsWithpassword(document, list, "D:\\scbEdd\\tmpPdf\\test0.pdf", "A226790888");		      
		      document = PdfFileUtil.splitDocument(3, 4,  pdd);
		      document.save("D:\\scbEdd\\tmpPdf\\test2.pdf");		      
		      PdfFileUtil.mergeDocumentsWithpassword(document, list, "D:\\scbEdd\\tmpPdf\\test1.pdf", "A226790888");		      		      
		      pdd.close();
		      ras.close();
		   }catch(Exception e){
			  e.printStackTrace(); 
		   }
		   /*
		   String extractTxt = extractTxt(pddocument);
		   //extractTxt = "客\r\n戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢客戶資料查詢";
		   try {
			    char [] chararr = extractTxt.toCharArray();
			   for(int i = 0 ; i < 50; i++){
				   Character character = new Character(chararr[i]);
				   System.out.println(character);
				   System.out.println(Character.isUnicodeIdentifierPart(character));
			   }
			   //String blah = URLEncoder.encode(extractTxt, "UTF-32");  
		       //System.out.println(URLDecoder.decode(blah, "UTF-8"));     
		   } catch (Exception e) {
			   // TODO Auto-generated catch block
			   e.printStackTrace();
		   }
		   
		/*
		try {
			//encryptPdf("C:/streamEDP/StreamEDP/bin/D276S15M.pdf",
				//	"C:/streamEDP/StreamEDP/bin/D276S15M.pdf", "aeolus");
			PdfFileUtil.partitionPdfFile("D:/scbEdd/backup/20120725/x1340183581316_6.7568.scb.scb.out.pdf", 1, 2, "D:/scbEdd/backup/20120725/test.pdf");
			PdfFileUtil.partitionPdfFile("D:/scbEdd/backup/20120725/x1340183581316_6.7568.scb.scb.out.pdf", 3, 4, "D:/scbEdd/backup/20120725/test2.pdf");
			ArrayList<File> list = new ArrayList<File>();
			list.add(new File("D:/scbEdd/backup/20120725/test.pdf"));
			list.add(new File("D:/scbEdd/backup/20120725/test2.pdf"));
			doMergePdfWithPassword(list, new File("D:/scbEdd/backup/20120725/test3.pdf"), "test");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		*/
	}

}
