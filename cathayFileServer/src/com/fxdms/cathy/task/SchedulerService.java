package com.fxdms.cathy.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.fxdms.cathy.bo.Properties;
import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.fxdms.util.AfpToPdfUtil;
import com.fxdms.util.FileFilterImpl;
import com.fxdms.util.HibernateSessionFactory;
import com.fxdms.util.PdfFileUtil;
import com.fxdms.util.ZipUtils;
import com.ibm.icu.util.Calendar;
import com.itextpdf.text.DocumentException;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.ErrorReport;

public class SchedulerService {

	/**
	 * Log4j instance.
	 */
	private final static Logger logger = Logger.getLogger(SchedulerService.class);
	private static Constant constant;
	private static boolean running = false;
	private static boolean pdfCleanRunning = false;
	private static String todayStr = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd");

	
	/* Constants */
	private static SchedulerService instance = new SchedulerService();


	private SchedulerService() {

	}

	public static SchedulerService getInstance() {
		if(instance == null)
			instance = new SchedulerService();
		return instance;
	}
	
	//刪除pdf
	public static void deleteFileAndZip(){
		if(pdfCleanRunning)
			return;
		pdfCleanRunning = true;
		
		File afpFolder = new File(Properties.getFileServerPdfFolder());
	   	if(!afpFolder.exists()){
	   		afpFolder.mkdirs();
	   	}
	   	Calendar cal = Calendar.getInstance();
	   	cal.add(Calendar.DATE, Properties.getFxFilesKeepDays() * -1);
	   	logger.info("files will be delete before :" + sdf2.format(cal.getTime()));
	   	FileFilterImpl.setForCheckTime(cal.getTimeInMillis());
	   	File [] pdfFiles = afpFolder.listFiles(FileFilterImpl.getFileFilter());
	   	
	   	if(pdfFiles != null && pdfFiles.length > 0){
	   		Session session = null;
	   		Transaction tx = null;
	   		try{
	   		   //ZipUtils.packFile(pdfFiles, new File(afpFolder, sdf.format(new Date()) + ".zip"));
	   		   session = HibernateSessionFactory.getSession();	   		   
	   		   Query query = session.createQuery("from ApplyData where policyPDF = ?");
	   		   
	   		   for(File file : pdfFiles){
	   			  if(file.lastModified() < cal.getTimeInMillis()){
	   			     tx = session.beginTransaction();
	   			     String policyPDF = file.getName().substring(0, file.getName().length() - 4);
	   			     query.setString(0, policyPDF);
	   			     List<ApplyData> list = query.list();
	   			     if(list != null)
	   			         for(ApplyData applyData : list){
	   			           applyData.setPolicyPDF(null);
	   			           session.update(applyData);
	   			         } 
	   			     FileUtils.forceDelete(file);
	   				 logger.info("deleted " + file.getName() + "success.");
	   				 tx.commit();
	   			  }
	   		  }
	   		   
	   		}catch(Exception e){
	   		   if(tx != null)
	   			   tx.rollback();
	   		   logger.error("", e);
	   		   e.printStackTrace();
	   		}finally{
	   			if(tx != null && !tx.wasCommitted())
	   				tx.commit();
	   			if(session != null && session.isOpen())
	   				session.close();
	   			session = null;
	   		}
	   	}
		pdfCleanRunning = false;	
	}
	
	public static void afpListener(){		
		logger.info("afpListener start to run");
		if(running)
			return;
		
		running = true;		
		Date today = new Date();
		
		if(todayStr == null || !todayStr.equals(sdf.format(today))){			
			File file = new File("D:/tmp", "caLogistic" + todayStr + ".CSV");
			try{			
			   if(file.exists())
			      FileUtils.forceDelete(file);
			   todayStr = sdf.format(today);			
			   file = new File("D:/tmp", "caLogistic" + todayStr + ".CSV");
			   FileWriter fw = new FileWriter(file);
			   BufferedWriter bw = new BufferedWriter(fw);
			   bw.write("ClientID,Filename,CycleDate,ProcessDate,Accounts,Pages,Feeder2,Feeder3,Feeder4,Feeder5,Tray1,Tray2,Tray3,Tray4\r\n");
			   bw.flush();
			   fw.flush();
			   bw.close();
			   fw.close();
			}catch(Exception e){
				logger.error("", e);
			}
		}
		File csvFile = new File("D:/tmp", "caLogistic" + todayStr + ".CSV");
		
		
	   	File afpFolder = new File(Properties.getFileServerPdfFolder());
	   	logger.info("checking " + afpFolder.getAbsolutePath());
	   	if(!afpFolder.exists()){
	   		afpFolder.mkdirs();
	   	}
	   	File [] pdfFiles = afpFolder.listFiles(FileFilterImpl.getPdfFileFilter());
	   	if((pdfFiles != null && pdfFiles.length > 0)){
	   		//保單及簽收單
	   		try{
	   	     for(File pdfFile : pdfFiles){
	   	    	 logger.info("processing " + pdfFile.getName());
	   	    	String mainNm = pdfFile.getName().substring(0, pdfFile.getName().length() - 4); //截斷.afp的部分
	   	    	AfpFile afp = ((VoService) Constant.getContext().getBean("voServiceProxy")).getAfp(mainNm);
	   	    	if(afp == null){
	   	    		logger.info(mainNm + " not exist in DB. Delete it.");
	   	    		FileUtils.forceDelete(pdfFile);
	   	    		continue;
	   	    	}
	   	    	String filePattern = "CA0n";	   	    	
	   	    	if(mainNm.toUpperCase().startsWith("SG"))
	   	    		filePattern = "SG0n";
	   	    	else if(mainNm.toUpperCase().startsWith("GA"))
   	    		    filePattern = "GA09";
	   	    	else if(mainNm.toUpperCase().startsWith("GG"))
   	    		    filePattern = "GG09";
	   	    	else if(mainNm.toUpperCase().startsWith("PD"))
   	    		    filePattern = "PD09";
	   	    	filePattern += (mainNm.substring(4, 5).toUpperCase() + "yyyymmddnnnn");
	   	    	int sheets = afp.getPages();
	   	    	if(mainNm.toUpperCase().startsWith("CA") || mainNm.toUpperCase().startsWith("GA") ){
	   	    	   sheets = afp.getPages() / 2;
	   	    	   if(afp.getPages() % 2 == 1)
	   	    		   sheets++;
	   	    	}
	   	    	//ClientID,Filename,CycleDate,ProcessDate,Accounts,Pages,Feeder2,Feeder3,Feeder4,Feeder5,Tray1,Tray2,Tray3,Tray4
	   	    	//HS,CMN_0520            ,14/05/20  ,14/05/21  ,       9,      22,       0,       0,       0,       0,      11,       0,       0,       0,CMN_mmdd            ,001,       0,       0,       0,       0,       0,       0,       0,
	   	    	
	   	    	if(pdfFile.exists()){
	   	    		logger.info(pdfFile.getName() + " exist. ready to split");
	   	    		PDDocument pdfDoc = null;
					File rasFile = null;
					RandomAccessFile ras = null;
					try {		
					   rasFile = new File(afpFolder, UUID.randomUUID() + "");
					   ras = new RandomAccessFile(rasFile, "rw");
					   pdfDoc = PDDocument.load(pdfFile, ras);
					   Set<ApplyData> applyDatas = ((VoService) Constant.getContext().getBean("voServiceProxy")).getApplyDataByNewBatchNm(mainNm);
					   int pages = 0;
					   //如果pages不同，重新計算
					   for(ApplyData applyData : applyDatas){
						   pages += applyData.getTotalPage() == null? 0 : applyData.getTotalPage();
						   if(afp.getPages() == null || pages != afp.getPages().intValue()){
							   afp.setPages(pages);
							   sheets = afp.getPages();
					   	       if(mainNm.toUpperCase().startsWith("CA") || mainNm.toUpperCase().startsWith("GA")){
					   	    	  sheets = afp.getPages() / 2;
					   	    	  if(afp.getPages() % 2 == 1)
					   	    	     sheets++;
					   	       }
						   }
						   
					   }
					   //上傳jbm，產生工單
					   String forappend = "UD," + StringUtils.rightPad(afp.getNewBatchName(), 20, " ") + "," + 
	                	   	  	StringUtils.rightPad(sdf2.format(afp.getCycleDate()), 10, ' ') + "," + StringUtils.rightPad(sdf2.format(today), 10, ' ')  + "," + 
		   	    			    StringUtils.leftPad(applyDatas.size() + "", 8, ' ') +  "," + StringUtils.leftPad(afp.getPages() + "", 8, ' ') + ",       0,       0,       0,       0," + 
		   	    			    StringUtils.leftPad( sheets + "", 8, ' ') + ",       0,       0,       0," + StringUtils.rightPad( filePattern, 20, ' ') + 
		   	    			    ",001,       0,       0,       0,       0,       0,       0,       0,\r\n" ;
					   //9999的檔案不用產生工單
					   if(!mainNm.endsWith("9999")){
					      FileWriter fw = new FileWriter(csvFile, true);
					      BufferedWriter bw = new BufferedWriter(fw);
					      bw.write(forappend);
					      bw.flush();
					      fw.flush();
					      bw.close();
					      fw.close();
					      com.fxdms.util.FtpClientUtil fcu = new com.fxdms.util.FtpClientUtil("10.113.137.23", "jbm", "1qazxsw2"); 
					      fcu.upload("logistic", csvFile);
					   }
					   
					   for(ApplyData applyData : applyDatas){
						   if(applyData.getTotalPage() != null && applyData.getTotalPage() > 0){
							   
						      int startPage = applyData.getAfpBeginPage();
						      int endPage = applyData.getAfpEndPage();
						      //如果沒有pdfName，硬塞一個
						      String pdfName = (applyData.getPolicyPDF() == null || applyData.getPolicyPDF().trim().equals(""))? UUID.randomUUID() + "" : applyData.getPolicyPDF();
						      logger.info("splitting pdf " + pdfName);
						      if(endPage < startPage){
						    	  ErrorReport er = new ErrorReport();
								  er.setErrHappenTime(new Date());
								  er.setErrorType("error pdf");
								  er.setOldBatchName(mainNm);
								  er.setReported(false);
								  er.setMessageBody("error on producing pdf because:" );
								  er.setTitle("format error");
								  ((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
						    	  applyData.setPolicyStatus("16");
						      }else{
						    	  File encPdfFile = new File(afpFolder, pdfName + ".pdf");
						    	  File tmpFile = new File(afpFolder, pdfName + ".tmp");
						    	  
						    	  if(encPdfFile.exists())
							           FileUtils.forceDelete(encPdfFile);
						    	  if(tmpFile.exists())
						    		  FileUtils.forceDelete(tmpFile);
						    	  logger.info("tmp path:" + tmpFile.getAbsolutePath() + ".start:" + startPage + "end:" + endPage);
						    	  
						          PdfFileUtil.splitDocument(startPage, endPage, tmpFile.getAbsolutePath(), pdfDoc);
						             
						          						            
						          if(tmpFile.exists()){
						              PdfFileUtil.encryptPdf(tmpFile.getAbsolutePath(), encPdfFile.getAbsolutePath(), Constant.getPdfPwd());
						          //加密後，把tmp pdf刪除						          
						              FileUtils.forceDelete(tmpFile);
						          }else{
						        	  logger.info(tmpFile.getName()+ " not exist weird!!!");
						          }
						          applyData.setPolicyPDF(pdfName);
						          if(!mainNm.endsWith("9999")){
						        	 if(applyData.getPolicyStatus() == null
						        			 || (!applyData.getPolicyStatus().equals("100") && applyData.getPolicyStatus().compareTo("30") < 0))
						                applyData.setPolicyStatus("30");
						          }else{
						           	 applyData.setPolicyStatus("28");
						          }						    	  
						      }
						      //applyData.setPresTime(today);
						      applyData.setUpdateDate(today);
						      ((VoService) Constant.getContext().getBean("voServiceProxy")).update(applyData);
						   }						   
					   }
					   if(!mainNm.endsWith("9999")){
						  if(!"已交寄".equals(afp.getStatus())){
					         afp.setUpdateDate(today);					   
					         afp.setStatus("列印中");
						  }
					   }else{
						  afp.setUpdateDate(today);					   
						  afp.setStatus("免印製");
					   }
					   //afp.setPresTime(today);
					   ((VoService) Constant.getContext().getBean("voServiceProxy")).update(afp);
					   logger.info("update afp status: " + afp.getNewBatchName() );
					   //刪除轉換完的pdf
					   FileUtils.forceDelete(pdfFile);
					} catch (IOException e) {
						logger.error("", e);
						e.printStackTrace();
					} catch (COSVisitorException e) {
						logger.error("", e);
						e.printStackTrace();
					} catch (DocumentException e) {
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
						if(rasFile != null && rasFile.exists())
							rasFile.delete();
					}
	   	    	 } 
	   	      } //end of for
	   	   }catch(Exception ex){
	   		  logger.error("", ex);
	   	   }finally{
	   		   running = false;
	   	   }	   		
	   	}
	   	logger.info("stop running");
		running = false;
	}

}
