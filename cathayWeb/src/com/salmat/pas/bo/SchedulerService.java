package com.salmat.pas.bo;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.ibm.icu.util.Calendar;
import com.moyosoft.exchange.Exchange;
import com.salmat.pas.beans.DailyReportBean;
import com.salmat.pas.conf.Constant;
import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.report.GenerateReport;
import com.salmat.pas.vo.AcctItemFx;
import com.salmat.pas.vo.AfpFile;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.ImgFile;
import com.salmat.pas.vo.JobBag;
import com.salmat.pas.vo.JobBagSplit;
import com.salmat.pas.vo.LogisticStatus;
import com.salmat.pas.vo.Properties;
import com.salmat.pas.vo.Sms;
import com.salmat.util.AfpToPdfUtil;
import com.salmat.util.FileEncFactory;
import com.salmat.util.FtpClientUtil;
import com.salmat.util.HibernateSessionFactory;
import com.salmat.util.MailSender;
import com.salmat.util.PdfFileUtil;
import com.salmat.util.SftpClientUtil;
import com.salmat.util.ZipUtils;


public class SchedulerService {

	/**
	 * Log4j instance.
	 */
	private final static Logger logger = Logger.getLogger(SchedulerService.class);
    private static boolean returnRptRunning = false;
    private static boolean errRptRunning = false;
    private static boolean commonRptRunning = false;
    private static boolean jobBagListenerRunnig = false;
    private static boolean gpgListenerRunning = false;
    private static boolean dailyReportRunnig = false;
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	/* Constants */
	private static SchedulerService instance = new SchedulerService();
    static{
    	sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }

	public SchedulerService() {
		//本機測試時這段要封起來        	
		/*
		System.setProperty("com.sun.management.jmxremote.port", "1099");
		System.setProperty("com.sun.management.jmxremote.rmi.port", "1099");		
        System.setProperty("java.rmi.server.hostname", "172.16.16.109");
          */     
	}

	public static SchedulerService getInstance() {
		if(instance == null)
			instance = new SchedulerService();
		return instance;
	}
	
	public static void taipei2logisticReport(){
		String serverPath = ServletContextGetter.getRealPath("");
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE) - 30);
		Date before = cal.getTime();
		cal.add(Calendar.DATE, 23);
		Date end = cal.getTime();
		
		Properties properties = PropertiesService.getProperties();
	    if(properties != null && properties.getTpe2Mail() != null ){
	    	String[] mailTo = properties.getTpe2Mail().split(",");	    		    	
	    	Session session = null;
	    	Transaction tx = null;
	    	try{
	    		// 查出未完成的，30天前到7天前的
	    		session = HibernateSessionFactory.getSession();
	    		Query dateListQuery = session.createQuery("select distinct cycleDate from ApplyData "
	    				+ " where sourceCode <> 'GROUP' and cycleDate > '2015-01-12' "
	    				+ " and policyStatus <> '11' and policyStatus <> '13' and policyStatus <> '14' and policyStatus <> '16' and policyStatus <> '18'"
	    				+ " and cycleDate between ? and ?   "
	    				+ " and packId is null and newBatchName is not null and newBatchName not like '%9999'"
	    				+ " and center = '06'");
	    		List<Date> dateList = dateListQuery.setDate(0, before).setDate(1, end).list();
	    		
	    		Query query = session.createQuery("from LogisticStatus where batchOrOnline = 'B' and cycleDate in "
	    				+ "(:cycleDates)  order by cycleDate, firstUniqueNo");
	    		List<LogisticStatus> logisticStatuses = null;
	    		if(dateList != null && dateList.size() > 0){
	    			logisticStatuses = query.setParameterList("cycleDates", dateList).list();
	    		}
	    		if(logisticStatuses != null && logisticStatuses.size() > 0){
	    			Map<Date, List<LogisticStatus>> dateMap = new HashMap<Date, List<LogisticStatus>>();
	    			for(LogisticStatus ls : logisticStatuses){
	    				List<LogisticStatus> lss = dateMap.get(ls.getCycleDate());
	    				if( lss == null){
	    					lss = new ArrayList<LogisticStatus>(); 
	    				}
	    				lss.add(ls);
	    				dateMap.put(ls.getCycleDate(), lss);
	    			}
	    			Set<Date> keySet = dateMap.keySet();
	    			for(Date cycleDate : keySet){
	    				List<LogisticStatus> lss = dateMap.get(cycleDate);
	    				String targetName = GenerateReport.generateLogisticReports(lss,  session, "06");
	    				File attachFile = new File(serverPath + "/pdf/", targetName);
	    				MailSender mail = new MailSender();	    				
	    				logger.info("sending report");
	    			    if(mailTo.length > 0){
	    				   mail.setSub("超過七日未完成交寄保單" + "_" + Constant.yyyy_MM_dd.format(cycleDate));
	    				   mail.setMailTo(mailTo);
	    				   mail.setMailFrom("dailyReport@fxdms.net");
	    				   mail.setHost(properties.getMailHost());
	    				   mail.setMsg("富士施樂-國泰人壽保單北二交寄作業\r\n內容請參考附件" + "\r\n\r\n" + "如需修改收件人，請聯絡壽一科申請\r\n本郵件為系統自動通知郵件，請勿直接回覆");
	    				   mail.setFileName(attachFile.getAbsolutePath());
	    				   mail.send();
	    				   logger.info("sent report:" + attachFile.getAbsolutePath());
	    			    }
	    			}
	    		}

	    		dateList = dateListQuery.setDate(0, before).setDate(1, new Date()).list();
	    		
	    		//查出30天內已完成，且未交寄過的	    		
	    		if(dateList != null && dateList.size() > 0){
	    		    logisticStatuses = session.createQuery("from LogisticStatus where center = '06' and cycleDate > :projBeginDate and cycleDate not in "
	    				+ "(:cycleDates) and batchOrOnline = 'B' and cycleDate between :beginDate and :endDate order by cycleDate, firstUniqueNo")
	    				.setDate("projBeginDate", Constant.yyyyMMdd.parse("20150112")).setParameterList("cycleDates", dateList)
	    				.setDate("beginDate", before).setDate("endDate", new Date()).list();
	    		}else{
	    			logisticStatuses = session.createQuery("from LogisticStatus where center = '06' and cycleDate > :projBeginDate "
	    					+ " and batchOrOnline = 'B' and cycleDate between :beginDate and :endDate order by cycleDate, firstUniqueNo")
		    				.setDate("projBeginDate", Constant.yyyyMMdd.parse("20150112"))
		    				.setDate("beginDate", before).setDate("endDate", new Date()).list();
	    		}
	    		Map<Date, List<LogisticStatus>> dateMap = new HashMap<Date, List<LogisticStatus>>();
    			for(LogisticStatus ls : logisticStatuses){
    				List<LogisticStatus> lss = dateMap.get(ls.getCycleDate());
    				if( lss == null){
    					lss = new ArrayList<LogisticStatus>(); 
    				}
    				lss.add(ls);
    				dateMap.put(ls.getCycleDate(), lss);
    			}
    			Set<Date> keySet = dateMap.keySet();
    			String doneDateStr = properties.getTpe2DoneDate();
    			String[] doneDates = null;
    			if(doneDateStr == null){
    				doneDates = new String [0];
    			}else{
    				doneDates = doneDateStr.split(",");
    			}    			
	    		for(Date cycleDate : keySet){
	    			String yyyyMMdd = Constant.yyyyMMdd.format(cycleDate);
	    			boolean doneBefore = false;
	    			for(String doneDate : doneDates){
	    				if(yyyyMMdd.equals(doneDate)){
	    					doneBefore = true;
	    					break;
	    				}
	    			}
	    			if(!doneBefore){
	    				doneDateStr += ("," +yyyyMMdd);
	    				if(doneDateStr.length() > 900){
	    					String [] doneDateStrs = doneDateStr.split(",");
	    					doneDateStr = "";
	    					for(int i = (doneDateStrs.length - 1) ; i >= 0 ; i--){
	    						doneDateStr = ("," + doneDateStrs[i]) + doneDateStr;
	    						if(doneDateStr.length() >= 801){
	    							break;
	    						}
	    					}	    					
	    				}
	    				tx = session.beginTransaction();
	    				properties.setTpe2DoneDate(doneDateStr);
	    				session.update(properties);
	    				tx.commit();
	    				List<LogisticStatus> lss = dateMap.get(cycleDate);
		    			String targetName = GenerateReport.generateLogisticReports(lss,  session, "06");
		    			File attachFile = new File(serverPath + "/pdf/", targetName);
		    			MailSender mail = new MailSender();	    				
		    			logger.info("sending report");
		    		    if(mailTo.length > 0){
		    			   mail.setSub("全部完成交寄保單" + "_" + Constant.yyyy_MM_dd.format(cycleDate));
		    			   mail.setMailTo(mailTo);
		    			   mail.setMailFrom("dailyReport@fxdms.net");
		    			   mail.setHost(properties.getMailHost());
		    			   mail.setMsg("富士施樂-國泰人壽保單北二交寄作業\r\n內容請參考附件" + "\r\n\r\n" + "如需修改收件人，請聯絡壽一科申請\r\n本郵件為系統自動通知郵件，請勿直接回覆");
		    			   mail.setFileName(attachFile.getAbsolutePath());
		    			   mail.send();
		    			   logger.info("sent report:" + attachFile.getAbsolutePath());
		    		    }

    				}	    			
    			}	    		
	    	}catch(Exception e){
	    		if(tx != null)
	    			tx.rollback();
	    		logger.error("", e);
	    	}finally{
	    		if(session != null)
	    			session.close();
	    	}
	    }
	}
	
	public static void dailyReport(){
		if(dailyReportRunnig)
			return;
		
		logger.info("daily report running");
		dailyReportRunnig = true;
		Date today = new Date();
		Properties properties = PropertiesService.getProperties();
		Session session = null;
		String queryCenter = "06";//以後要封起來
		  
		try {
			//key = center + "_" + 20140601
			//Map<String, int[]> centerMap, String name
			session = HibernateSessionFactory.getSession();
			Calendar dateBeg = Calendar.getInstance();
			Calendar dateEnd = Calendar.getInstance();
			dateBeg.set(dateBeg.get(Calendar.YEAR), dateBeg.get(Calendar.MONTH), 1, 0, 0, 0);
			dateBeg.set(Calendar.MILLISECOND, 0);
			dateEnd.set(dateEnd.get(Calendar.YEAR), dateEnd.get(Calendar.MONTH), 1, 0, 0, 0);
			int maxDate = dateEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
			dateEnd.set(dateEnd.get(Calendar.YEAR), dateEnd.get(Calendar.MONTH), maxDate, 0, 0, 0);
			
			HashMap<String, int[]> map = DailyReportBean.dailyReportQuery(session, dateBeg,
					dateEnd, queryCenter);
			

			String fileName = GenerateReport.generateDailyReport(map, dateBeg.get(Calendar.YEAR) + "-" + ( dateBeg.get(Calendar.MONTH) + 1));
			String serverPath = ServletContextGetter.getRealPath("");
			File attachFile = new File(serverPath + "/pdf/", fileName);
			MailSender mail = new MailSender();
			String[] mailTo = properties.getDailyReportEmails().split(",");
			logger.info("sending report");
		    if(mailTo.length > 0){
			   mail.setSub("富士施樂-國泰人壽保單Daily Report");
			   mail.setMailTo(mailTo);
			   mail.setMailFrom("dailyReport@fxdms.net");
			   mail.setHost(properties.getMailHost());
			   mail.setMsg("富士施樂-國泰人壽保單Daily Report\r\n內容請參考附件" + "\r\n\r\n" + "如需修改收件人，請聯絡壽一科申請\r\n本郵件為系統自動通知郵件，請勿直接回覆");
			   mail.setFileName(attachFile.getAbsolutePath());
			   mail.send();
			   logger.info("sent report:" + attachFile.getAbsolutePath());
		    }

			
		}catch(Exception e){
			logger.error("", e);
		}finally{
			dailyReportRunnig = false;
			if(session != null && session.isOpen())
				session.close();			
			logger.info("daily report stop");
		}

		
		
	}
	
	public static void returnReport(){
		if(returnRptRunning){
			return;
		}
		returnRptRunning = true;
		Date today = new Date();
		Properties properties = PropertiesService.getProperties();
		Exchange exchange = null;
		Session session = null;
		Transaction tx = null;
		  
		try {
			session = HibernateSessionFactory.getSession();
			MailSender mail = new MailSender();
			String[] mailTo = properties.getReturnEmails().split(",");
			mail.setSub("富士施樂-國泰人壽保單迴歸作業通知");
			mail.setMailTo(mailTo);
			mail.setMailFrom("returnReport@fxdms.net");
			mail.setHost(properties.getMailHost());
			
			Query query = session.createQuery("from ErrorReport  where reported = false and errorType = 'return check'");
			List<ErrorReport> reports = query.list();
			query = session.createQuery("from ErrorReport  where reported = false and errorType = 'return check' and title = 'return check end'");
			List<ErrorReport> list = query.list();

			tx = session.beginTransaction();
			String titleMsg = "主旨：富士施樂-國泰人壽保單迴歸作業通知\r\n";
			String message = "";
			Date beginDate = null;
			Date endDate = null;
			boolean haveError = false;
			String smsMsg = null;
			//Return Check not OK
			if(list != null && list.size() > 0){
			   for(ErrorReport report : reports){
				   if("return check end".equals(report.getTitle())){
					   smsMsg = report.getMessageBody();
				   }else if(beginDate == null || beginDate.getTime() > report.getErrHappenTime().getTime())
					   beginDate = report.getErrHappenTime();
				   if(endDate == null || endDate.getTime() < report.getErrHappenTime().getTime())
					   endDate = report.getErrHappenTime();
				   message += report.getMessageBody() == null? "" : report.getMessageBody() ;
				   message += "\r\n -------------------------------------- \r\n";
				   report.setReported(true);
				   report.setReportTime(today);
				   if("Return Check not OK".equals(report.getTitle())){
					   haveError = true;
				   }
				   session.update(report);
			   }			
			   if(beginDate != null){
				  titleMsg += "作業執行日期：" + Constant.yyyyMMddHHmm.format(beginDate) + "\r\n";
			      if(!haveError)
				     titleMsg += "作業執行結果：作業正常\r\n";
			      else
				     titleMsg += "作業執行結果：作業異常\r\n";
			      if(beginDate != null && endDate != null)
				      titleMsg += "作業處理時間：" + sdf.format(new Date(endDate.getTime() - beginDate.getTime())) + "\r\n";;
			      mail.setMsg(titleMsg + message + "\r\n" + "如需修改收件人，請聯絡壽一科申請\r\n本郵件為系統自動通知郵件，請勿直接回覆");
			      if(reports!= null && reports.size() > 0 && mailTo != null)
			         mail.send();
			   }
			}
			tx.commit();
			if(haveError){
				Session session2 = null;
			    Transaction tx2 = null;
			    try{
			    	session2 = HibernateSessionFactory.getHibernateTemplate2().getSessionFactory().openSession();
			    	tx2= session2.beginTransaction();
				    Sms sms = new Sms();
				    String msg = "本日比對結果：轉檔結果內容不一致，已暫停正式作業" ;
				    sms.setInsertDate(new Date());
				    sms.setMsg(msg);
				    sms.setPhoneNos(properties.getReturnPhones());
				    sms.setSent(false);
				    session2.save(sms);
				    tx2.commit();
			    }catch(Exception e){
			    	if(tx2 != null)
			    		tx2.rollback();			    	
			    }finally{
			    	if(session2 != null && session2.isOpen())
			    		session.close();
			    }
			}
			
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			e.printStackTrace();
		}finally{
			returnRptRunning = false;
			if(session != null && session.isOpen())
				session.close();
			if(exchange != null)
			   exchange.closeConnection();
				
		}
		
	}
	
	public static void commonReport(){
		logger.info("run common report");
		if(commonRptRunning)
			return;
		commonRptRunning = true;
		
		Date today = new Date();
		Properties properties = PropertiesService.getProperties();
		Session session = null;
		Transaction tx = null;
		  
		try {
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			
			
			String[] mailTo = properties.getCommonEmails().split(",");						
			MailSender mail = new MailSender();
			mail.setSub("富士施樂-國壽保單處理報告");
			mail.setMailTo(mailTo);
			mail.setMailFrom("report@fxdms.net");
			mail.setHost(properties.getMailHost());
			
			
            Query query = session.createQuery("from ErrorReport  where reported = false and  errorType = 'EndProcess'");			
			List<ErrorReport> list = query.list();			

			String titleMsg = "主旨：富士施樂-國壽保單處理報告\r\n";
			String messageBody = "";
			Date beginTime = null;
			/*
			Constant.yyyy_MM_ddHHMM.format(new Date()) + "，行政中心:" + center + "開始進行轉檔作業處理\r\n "
			+ "新契約:" + normCounter + "件\r\n "
		    + "補單：" + reptCounter + "件\r\n "
		    + "保補：" + reisCounter + "件\r\n "
		    + "契轉：" + convCounter + "件"
		    */
			String msg = "";
			boolean normal = false;
			if(list.size() > 0){
			   session.createQuery("update ErrorReport set reported = true, reportTime = ?  where reported = false and  errorType = 'EndProcess'").setParameter(0, today).executeUpdate();
			   ErrorReport rpt = list.get(list.size() - 1);
			   query = session.createQuery("select distinct title from ErrorReport  where reported = false and  errorType = 'BeginProcess' and id < " + rpt.getId());			
			   List<Object> titleList = query.list();

			   for(Object title: titleList){
				   if(title != null)
				      query = session.createQuery("from ErrorReport  where reported = false and title = '" + title + "' and  (errorType = 'BeginProcess' or errorType = 'EndProcess') and id <= " + rpt.getId());
				   else
					  query = session.createQuery("from ErrorReport  where reported = false and title is null and  (errorType = 'BeginProcess' or errorType = 'EndProcess') and id <= " + rpt.getId());
				   List<ErrorReport> reports = query.list();
				   messageBody += title.toString() + "\r\n ---------------------------------- \r\n";
				   for(ErrorReport report : reports){
					   
					   if(beginTime == null || beginTime.getTime() > report.getErrHappenTime().getTime()){
						   beginTime = report.getErrHappenTime();
					   }					   
					   String happenedTime = report.getErrHappenTime() == null? "" : Constant.yyyyMMddHHmm.format(report.getErrHappenTime());					   
					   String message = report.getMessageBody() == null? "" : report.getMessageBody();
					   if("BeginProcess".equals(report.getErrorType())){
						   int centerBegin = message.indexOf("，行政中心:"); 
						   int centerEnd = message.indexOf("開始進行轉檔作業處理");
						   int normBegin = message.indexOf("新契約:");
						   int normEnd = message.indexOf("件", normBegin + 4);
						   int repBegin = message.indexOf("補單：");
						   int repEnd = message.indexOf("件", repBegin + 3);
						   int reisBegin = message.indexOf("保補：");
						   int reisEnd = message.indexOf("件", reisBegin + 3);
						   int convBegin = message.indexOf("契轉：");
						   int convEnd = message.indexOf("件", convBegin + 3);
						   int groupBegin = message.indexOf("團險:");
						   int groupEnd = message.indexOf("件", groupBegin + 3);
						   String center = "";
						   int norm = 0;
						   int rept = 0;
						   int reis = 0;
						   int conv = 0;
						   int group = 0;
						   if(centerBegin >= 0 && centerEnd >= 0){
							   center = message.substring(centerBegin + 6, centerEnd);
						   }
						   if(normBegin >= 0 && normEnd >= 0){
							   norm = new Integer(message.substring(normBegin + 4, normEnd));
						   }
						   if(repBegin >= 0 && repEnd >= 0){
							   rept = new Integer(message.substring(repBegin + 3, repEnd));
						   }
						   if(reisBegin >= 0 && reisEnd >= 0){
							   reis = new Integer(message.substring(reisBegin + 3, reisEnd));
						   }
						   if(convBegin >= 0 && convEnd >= 0){
							   conv = new Integer(message.substring(convBegin + 3, convEnd));
						   }
						   if(groupBegin >= 0 && groupEnd >= 0){
							   group = new Integer(message.substring(groupBegin + 3, groupEnd));
						   }
						   msg += message.substring(0, 10) + center + "結果：";
						   //2014-08-15台南結果：新契約583件，保補契轉42件。作業正常
						   if((norm + rept) > 0 || (reis + conv) > 0)
						      msg += "新契約" + (norm + rept) + "件，" + "保補契轉：" + (reis + conv) + "件" ;
						   if(group > 0)
							  msg += "團險" + group + "件";
						   msg += "\r\n";
					   }
					   
					   String oldBatchName = report.getOldBatchName() == null? "" : report.getOldBatchName();
					   messageBody += "時間:" + happenedTime + " | 狀況：" + message ;
					   if(!oldBatchName.equals("")){
						   messageBody += " | 保單檔案:" + oldBatchName;
					   }
					   messageBody += "\r\n";
					   report.setReported(true);
					   report.setReportTime(today);
					   session.update(report);
				   }
				   messageBody += "\r\n";
			   }
			   if(beginTime != null)
			      titleMsg += "作業執行日期：" + Constant.yyyyMMddHHmm.format(beginTime) + "\r\n";			   
			   
			   
			   messageBody += rpt.getTitle() + "\r\n ---------------------------------- \r\n";
			   String happenedTime = rpt.getErrHappenTime() == null? "" : Constant.yyyyMMddHHmm.format(rpt.getErrHappenTime());				   
			   String message = rpt.getMessageBody() == null? "轉檔正常" : rpt.getMessageBody();
			   if("轉檔正常".equals(message)){
				   normal = true;
			   }
			   titleMsg += "作業執行結果：" + message + "\r\n";
			   if(rpt.getErrHappenTime() != null && beginTime != null)
			      titleMsg += "作業處理時間：" + sdf.format(new Date(rpt.getErrHappenTime().getTime() - beginTime.getTime())) + "\r\n";
			   messageBody += "時間:" + happenedTime + " | 狀況：" + message ;
			   messageBody += "\r\n";


			   //檢查送交超峰的訊息
			   if(titleList == null || titleList.size() == 0){
			      query = session.createQuery("from ErrorReport  where reported = false and  errorType = 'PBSent'");
			      list = query.list();
			      if(list != null && list.size() > 0){
			    	 titleList.add("");
			    	 messageBody = "";
			    	 titleMsg = "";
			    	 msg = "";
			         for(ErrorReport report : list){
					     messageBody += report.getTitle() + "\r\n ---------------------------------- \r\n";
					     String happenTime = report.getErrHappenTime() == null? "" : Constant.yyyyMMddHHmm.format(report.getErrHappenTime());				   
					     messageBody += report.getMessageBody() ;					   
					     titleMsg += "送交超峰：" + happenTime + "\r\n";					   
					     msg = "保單送交超峰：" + happenTime + "\r\n";
					     report.setReported(true);
					     report.setReportTime(today);
					     session.update(report);
				     }
			      }
			   }
			   mail.setMsg(titleMsg + messageBody + "\r\n如需修改收件人，請聯絡壽一科申請\r\n本郵件為系統自動通知郵件，請勿直接回覆");
			   if(titleList != null && titleList.size() > 0 && mailTo != null && !msg.equals(""))
			      mail.send();
			}
			tx.commit();
			
			if(list.size() > 0 && !msg.equals("")){
			   Session session2 = null;
			   Transaction tx2 = null;
			   try{
			      session2 = HibernateSessionFactory.getHibernateTemplate2().getSessionFactory().openSession();
			   	  tx2= session2.beginTransaction();
			      Sms sms = new Sms();
			      sms.setInsertDate(new Date());
	              if(normal)		    
			         sms.setMsg(msg + "。作業正常");
	              else
	            	 sms.setMsg(msg + "。作業異常");
			      sms.setPhoneNos(properties.getCommonPhones());
			      sms.setSent(false);
			      session2.save(sms);
			      tx2.commit();
			  }catch(Exception e){
			   	 if(tx2 != null)
			   	 	tx2.rollback();			    	
			  }finally{
			    if(session2 != null && session2.isOpen())
			    	session2.close();
			  }
			}
			
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			e.printStackTrace();
		}finally{
			commonRptRunning = false;
			if(session != null && session.isOpen())
				session.close();			
			
		}
		
		logger.info("stop common report");
	}
	
	
	public static void errorReport(){
		logger.info("run error report");
		if(errRptRunning)
			return;
		errRptRunning = true;
		
		Date today = new Date();
		
		
		Properties properties = PropertiesService.getProperties();
		Session session = null;
		Transaction tx = null;
		  
		try {
			session = HibernateSessionFactory.getSession();
			tx = session.beginTransaction();
			List<String> mailList = properties.getEmailList();
			
			String[] mailTo = null;
			if(mailList != null){
			   mailTo = new String[mailList.size()];
			   int i = 0;
			   for(String email: mailList){
				  mailTo[i] = email;
				  i++;
			   }
			}
			
			MailSender mail = new MailSender();
			mail.setSub("國壽保單異常處理報告");
			mail.setMailTo(mailTo);
			mail.setMailFrom("errorReport@fxdms.net");
			mail.setHost(properties.getMailHost());
			
			Query query = session.createQuery("select distinct title from ErrorReport  where reported = false and errorType <> 'return check' and errorType <> 'BeginProcess' and errorType <> 'EndProcess' and errorType <> 'PBSent'");
			
			List<Object> titleList = query.list();
			logger.info("total title: " + titleList.size());
			String messageBody = "";
			for(Object title: titleList){
				if(title != null)
				   query = session.createQuery("from ErrorReport  where reported = false and title = '" + title + "' and errorType <> 'return check' and errorType <> 'BeginProcess' and errorType <> 'EndProcess' and errorType <> 'PBSent'");
				else
				   query = session.createQuery("from ErrorReport  where reported = false and title is null and errorType <> 'return check' and errorType <> 'BeginProcess' and errorType <> 'EndProcess' and errorType <> 'PBSent'");
				List<ErrorReport> reports = query.list();
				messageBody += title + "\r\n ---------------------------------- \r\n";
				for(ErrorReport report : reports){										
					String happenedTime = report.getErrHappenTime() == null? "" : Constant.yyyyMMddHHmm.format(report.getErrHappenTime());
					String message = report.getMessageBody() == null? "" : report.getMessageBody();
					String oldBatchName = report.getOldBatchName() == null? "" : report.getOldBatchName();
					messageBody += "發生時間:" + happenedTime + " | 狀況：" + message ;
					if(!oldBatchName.equals("")){
						messageBody += " | 保單檔案:" + oldBatchName;
					}
					messageBody += "\r\n";
					report.setReported(true);
					report.setReportTime(today);
					session.update(report);
				}
				messageBody += "\r\n\r\n";
			}
			mail.setMsg(messageBody + "如需修改收件人，請聯絡壽一科申請\r\n本郵件為系統自動通知郵件，請勿直接回覆");
			if(titleList != null && titleList.size() > 0 && mailTo != null)
			   mail.send();
			tx.commit();
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			e.printStackTrace();
		}finally{
			errRptRunning = false;
			if(session != null && session.isOpen())
				session.close();							
		}
		
		logger.info("stop error report");
	}
	
	
	//刪除本地端的檔案
	public static void deleteListener(){
		int keepDays = 365;
		Properties properties = PropertiesService.getProperties();
		if (properties.getFxFilesKeepDays() != null)
			keepDays = properties.getFxFilesKeepDays();
		Date today = new Date();
		Calendar daysBeforeCal = Calendar.getInstance();
		daysBeforeCal.set(daysBeforeCal.get(Calendar.YEAR), daysBeforeCal.get(Calendar.MONTH), daysBeforeCal.get(Calendar.DATE) - keepDays, 0, 0, 0);		
		Date daysBefore = daysBeforeCal.getTime();
		logger.info("keep date before:" + daysBefore);
		logger.info("today is:" + new Date());

		//刪除檔案
		File backupFolder = new File(ServletContextGetter.getRealPath("/pdf"));
		Set<String> fileNmSet = new HashSet<String> ();
		if (backupFolder != null && backupFolder.exists()) {
			logger.info("開始刪除" + keepDays + "天以前的備份檔");
			File[] files = backupFolder.listFiles();
			for (File file : files) {
				Date fileDate = new Date(file.lastModified());
				if (fileDate.before(daysBefore)) {
					try {
						if (file.isFile()){
							fileNmSet.add(file.getName());
							FileUtils.forceDelete(file);							
						}else if (file.isDirectory()){
							FileUtils.deleteDirectory(file);
						}
						logger.info("刪除備份檔" + file.getName() + "成功");
					} catch (IOException e) {
						logger.error("刪除備份檔失敗", e);
						e.printStackTrace();
					}
				}

			}
		}
		Session session = null;
		Transaction tx = null;
		  
		try {           
			session = HibernateSessionFactory.getSession();
			int imgKeepDays = 7;
			if (properties.getFilesKeepDays() != null)
				imgKeepDays = properties.getFilesKeepDays() + imgKeepDays;
			
			Calendar keepcal = Calendar.getInstance();
			keepcal.add(Calendar.DATE, imgKeepDays * -1);
			
			String queryStr = "from ImgFile where (updateDate is null or updateDate < ?) and reqPolicy = true and image = false and law = false";
            List<ImgFile> list = session.createQuery(queryStr).setParameter(0, keepcal.getTime()).list();
            if(list != null && list.size() > 0){
            	for(ImgFile imgFile : list){
            		tx = session.beginTransaction();
            		//先刪中介table的資料
            		SQLQuery deleteQuery = (SQLQuery) session.createSQLQuery("delete from imgMetaTable where imgId = " + imgFile.getImgId());
            		deleteQuery.executeUpdate();
            		tx.commit();
            		tx = session.beginTransaction();
            		Query imgDelete = session.createQuery("delete from ImgFile where imgId = " + imgFile.getImgId());
            		imgDelete.executeUpdate();
            		tx.commit();
            		logger.info("delete image " + imgFile.getFileNm());
            	}
            }    
            //只留180天的資料
			Query query = session.createQuery("delete from ErrorReport where  errHappenTime < ?");
            tx = session.beginTransaction();
			query.setDate(0, new Date(today.getTime() - (180L * 60 * 60 * 24 * 1000))).executeUpdate();
			tx.commit();
			//只留365天的資料
			query = session.createQuery("delete from ActionHistory where  insertDate < ?");
            tx = session.beginTransaction();
			query.setDate(0, new Date(today.getTime() - (365L * 60 * 60 * 24 * 1000))).executeUpdate();
			tx.commit();
			
		}catch(Exception e){
			if(tx != null)
				tx.rollback();
			logger.error("", e);
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		
		
	}
	
	
	
	public static void jobBagListener(){
		
		if(jobBagListenerRunnig)
			return;
		jobBagListenerRunnig = true;
		Calendar today = Calendar.getInstance();
		Calendar tendaysAgo = Calendar.getInstance();
		tendaysAgo.add(Calendar.DATE, - 10);
		
	    Session session = null;
	    Session session2 = null;
	    try{
	    	session = HibernateSessionFactory.getSession();
	    	session2 = HibernateSessionFactory.getHibernateTemplate2().getSessionFactory().openSession();
	    	String queryStr = "from AfpFile where center <> '00' and cycleDate > ? and status <> '已交寄' and status <> '免印製' and (newBatchName like 'CA%' or newBatchName like 'GA%' "
	    			+ "or ((newBatchName like 'GG%' or newBatchName like 'SG%' or newBatchName like 'PD%') and (status like '列印%' or status like '膠裝%' )))";
	    	
	    	List<AfpFile> list = session.createQuery(queryStr).setDate(0, tendaysAgo.getTime()).list();
	    	
            
	    	
	    	if(list != null && list.size() > 0){
	    		for(AfpFile afpFile : list){	    			
	    			//找出國泰工單
	    			List<JobBag> jobBags = session2.createQuery("from JobBag where afpName like '%" + afpFile.getNewBatchName() + "%' and custNo = 'UD' and (isDamage is null or isDamage = false) and (isDeleted is null or isDeleted = false)"  ).list(); 
                    if(jobBags != null && jobBags.size() > 0){	    				
	    				JobBag jobBag = jobBags.get(0);
	    				int sheets = jobBag.getSheets() == null? 0 : jobBag.getSheets();
	    				int pages = jobBag.getPages() == null? 0 : jobBag.getPages();
	    				List<JobBagSplit> jbss = session2.createQuery("from JobBagSplit where jobBagNo = '" + jobBag.getJobBagNo() + "'").list();
	    				JobBagSplit jbs = jbss.get(0);
	    				
	    				afpFile.setSheets(sheets);	    				
	    				afpFile.setPages(pages);	    				
	    				afpFile.setPrintTime(jbs.getLpCompletedDateByUser());
	    			    afpFile.setBindTime(jbs.getMpCompletedDateByUser());
	    			    Date deliverTime = jbs.getLgCompletedDateByUser();
	    			    if(deliverTime == null)
	    			    	deliverTime = jbs.getLgCompletedDateByManager();
	    			    afpFile.setDeliverTime(deliverTime);
	    			    
	    				String statusStr = afpFile.getStatus() == null? "" : afpFile.getStatus();
	    				String status = jobBag.getJobBagStatus() == null ? "" : jobBag.getJobBagStatus().trim();
	    				int policyStatus = 0;
	    				if("COMPLETED_LG".equals(status) || status.startsWith("ACC")){
	    					policyStatus = 100; //膠裝中
	    					logger.info(afpFile.getNewBatchName() + " |jobBagStatus:" + status );
    						statusStr = "已交寄";
	    				}else if("COMPLETED_LP".equals(status)){
	    					policyStatus = 35; //膠裝中
	    					if(statusStr.indexOf("膠裝") >= 0 || statusStr.indexOf("驗單") >= 0 || statusStr.indexOf("配表") >= 0 || statusStr.indexOf("裝箱") >= 0 
	    							|| statusStr.indexOf("貨運") >= 0 || statusStr.indexOf("交寄") >= 0 ){
	    						
	    					}else{
	    						statusStr = "膠裝中";
	    					}
	    					
	    				}else if("COMPLETED_MP".equals(status)){
	    					policyStatus = 36; //膠裝完畢
	    					if("膠裝完畢".equals(statusStr) 
	    							|| statusStr.indexOf("驗單") >= 0 || statusStr.indexOf("配表") >= 0 || statusStr.indexOf("裝箱") >= 0 
	    							|| statusStr.indexOf("貨運") >= 0 || statusStr.indexOf("交寄") >= 0 ){
	    						
	    					}else{
	    						statusStr = "膠裝完畢";
	    					}
	    					
	    				}
	    				/*
	    				else if("COMPLETED_LG".equals(status) ){
	    					policyStatus = 100; //完成交寄	    					
	    					statusStr = "已交寄";	    					
	    				}	    				
	    				*/
	    				
	    				List<ApplyData> applyDatas = session.getNamedQuery("ApplyData.findByNewBatchName").setString(0,  afpFile.getNewBatchName()).list();
	    				boolean completedSent = true;
	    				
	    				AfpFile receiptsAfp = null;
	    				AfpFile cardsAfp = null;
	    				Set<ApplyData> cards = null;
	    				Set<ApplyData> receipts = null;
	    				String newBatchName = afpFile.getNewBatchName();
	    				String receiptName = null;
	    				String cardName = null;
	    				if(newBatchName.toUpperCase().startsWith("CA")){
	    					receiptName = "SG" + newBatchName.substring(2);
	    				}else if(newBatchName.toUpperCase().startsWith("GA")){
	    					receiptName = "GG" + newBatchName.substring(2);
	    					cardName = "PD" + newBatchName.substring(2);
	    					cardsAfp = (AfpFile) session.get(AfpFile.class, cardName);
	    					if(cardsAfp != null)
	    					   cards = cardsAfp.getApplyDatas();
	    				}
	    				if(receiptName != null)
	    				   receiptsAfp = (AfpFile) session.get(AfpFile.class, receiptName);
	    				if(receiptsAfp != null)
	    				   receipts = receiptsAfp.getApplyDatas();
	    				
	    				Set<String> packIds = new HashSet<String>();
	    				   
	    				Date latestDate = null;	    				
	    				for(ApplyData applyData : applyDatas){
	    					ApplyData receipt = null;
	    					ApplyData card = null;
	    					if(receipts != null){
	    						for(ApplyData ad : receipts){
	    							if(applyData.getPolicyNos() != null && applyData.getPolicyNos().equals(ad.getPolicyNos())
	    									&& applyData.getReprint() != null && applyData.getReprint().equals(ad.getReprint())){
	    								receipt = ad;
	    								break;
	    							}
	    						}
	    					}
	    					
	    					if(cards != null){
	    						for(ApplyData ad : cards){
	    							if(applyData.getPolicyNos() != null && applyData.getPolicyNos().equals(ad.getPolicyNos())
	    									&& applyData.getReprint() != null && applyData.getReprint().equals(ad.getReprint())){
	    								card = ad;
	    								break;
	    							}
	    						}
	    					}
	    					
	    					Transaction tx = session.beginTransaction();
	    					if(jbs.getLpCompletedDateByUser() != null && (applyData.getPrintTime() == null || applyData.getPrintTime().getTime() != jbs.getLpCompletedDateByUser().getTime())){
	    						applyData.setPrintTime(jbs.getLpCompletedDateByUser());
	    						applyData.setUpdateDate(today.getTime());
	    						session.update(applyData);
	    						if(receipt != null && receipt.getPrintTime() == null){
	    							receipt.setPrintTime(jbs.getLpCompletedDateByUser());
	    							receipt.setUpdateDate(today.getTime());
	    							session.update(receipt);
	    						}
	    						if(card != null && card.getPrintTime() == null){
	    							card.setPrintTime(jbs.getLpCompletedDateByUser());
	    							card.setUpdateDate(today.getTime());
	    							session.update(card);
	    						}	    						
	    					}
	    					if(jbs.getMpCompletedDateByUser() != null && (applyData.getBindTime() == null || applyData.getBindTime().getTime() != jbs.getMpCompletedDateByUser().getTime())){
	    						applyData.setBindTime(jbs.getMpCompletedDateByUser());
	    						applyData.setUpdateDate(today.getTime());
	    						session.update(applyData);
	    						if(receipt != null && receipt.getBindTime() == null){
	    							receipt.setBindTime(jbs.getMpCompletedDateByUser());
	    							receipt.setUpdateDate(today.getTime());
	    							session.update(receipt);
	    						}
	    						if(card != null && card.getBindTime() == null){
	    							card.setBindTime(jbs.getMpCompletedDateByUser());
	    							card.setUpdateDate(today.getTime());
	    							session.update(card);
	    						}
	    					}
	    					int compareStatus = new Integer(applyData.getPolicyStatus() == null ? "0" : applyData.getPolicyStatus()); 
	    					//不能倒退回去
	    					if(policyStatus != 100 && compareStatus < policyStatus && (applyData.getExceptionStatus() == null || applyData.getExceptionStatus().trim().equals(""))){
	    						applyData.setPolicyStatus(policyStatus + "");
	    						applyData.setUpdateDate(today.getTime());
	    						session.update(applyData);
	    						if(receipt != null){
	    							receipt.setPolicyStatus(policyStatus + "");
		    						receipt.setUpdateDate(today.getTime());
	    							session.update(receipt);
	    						}
	    						if(card != null){
	    							card.setPolicyStatus(policyStatus + "");
		    						card.setUpdateDate(today.getTime());
	    							session.update(card);
	    						}
	    					}else if(policyStatus == 100){
	    						//如果有保單還沒列印交寄清單的話，就是部分交寄
	    						if(applyData.getPolicyStatus() == null || applyData.getPackId() == null || applyData.getPackId().trim().equals("")){
	    							statusStr = "部分交寄";
	    						}else if(!"100".equals(applyData.getPolicyStatus()) && "97".compareTo(applyData.getPolicyStatus()) > 0){
	    							statusStr = "部分交寄";
	    						}else if(!"100".equals(applyData.getPolicyStatus())){
	    							
	    							applyData.setPolicyStatus("100");
	    							packIds.add(applyData.getPackId());
	    							
	    							if(applyData.getPackTime() != null && applyData.getCycleDate().getTime() > applyData.getPackTime().getTime() ){
	    							    //如果cycleDate比packTime大，以cycleDate加兩小時當做交寄時間
	    							    deliverTime = new Date(applyData.getCycleDate().getTime() + 1000 * 60 * 60 * 2);
	    							}else if(applyData.getPackTime() != null){
	    								//否則就是以打包時間加兩小時當作交寄時間
	    								deliverTime = new Date(applyData.getPackTime().getTime() + 1000 * 60 * 60 * 2);
	    							}	    						
	    							applyData.setUpdateDate(today.getTime());
	    							applyData.setDeliverTime(deliverTime);

	    							if(deliverTime != null && (afpFile.getDeliverTime() == null || deliverTime.compareTo(afpFile.getDeliverTime()) > 0))
	     							    afpFile.setDeliverTime(deliverTime);		    						
	    							if(receipt != null){
	    								if("100".equals(applyData.getPolicyStatus()))
	    									receipt.setPolicyStatus("100");
	    								
			    						receipt.setDeliverTime(applyData.getDeliverTime());
			    						receipt.setUpdateDate(applyData.getUpdateDate());
		    							session.update(receipt);
		    						}
		    						if(card != null){
		    							if("100".equals(applyData.getPolicyStatus()))
	    									card.setPolicyStatus("100");

			    						card.setDeliverTime(applyData.getDeliverTime());
			    						card.setUpdateDate(applyData.getUpdateDate());
		    							session.update(card);
		    						}
		    						session.createQuery("update BankReceipt set status = '交寄完成', dateSerialNo = null, dateCenterSerialNo = null, packDate = ?, packUser = matchUser where "
			    							   + "oldBatchName = '" + applyData.getOldBatchName() + "' and (packDate is null or status is null or status <> '交寄完成')").setParameter(0, deliverTime).executeUpdate();
			    					//session.createQuery("update BankReceipt set  packDate = ?, packUser = matchUser where "
			    						//	   + "oldBatchName = '" + applyData.getOldBatchName() + "' and (packDate is null or status is null or status <> '交寄完成')").setParameter(0, today.getTime()).executeUpdate();
	    							session.update(applyData);
	    						}  						
	    					}
	    					tx.commit();
	    					
	    					if(!applyData.getPolicyStatus().equals("100")){
	    						completedSent = false;
	    						if(latestDate == null || (applyData.getDeliverTime() != null && latestDate.compareTo(applyData.getDeliverTime()) < 0)){
	    							latestDate = applyData.getDeliverTime();
	    						}
	    					}else{
	    						/*
	    						session.createQuery("update BankReceipt set packDate = ?, packUser = matchUser where packDate is null and oldBatchName ='" + afpFile.getNewBatchName() + "'")
	    						   .setParameter(0, today.getTime()).executeUpdate();
	    						*/
	    					}	    					
	    				}
	    				if(packIds != null && packIds.size() > 0 ){
	    					Transaction tx = session.beginTransaction();	    					
    					    session.createQuery("update PackStatus set status = 50, statusNm = '交寄完成', updateDate = :updateDate where "
	    							   + " packId in (:packIds)").setParameterList("packIds", packIds).setParameter("updateDate", deliverTime).executeUpdate();
    					    session.createQuery("update LogisticStatus set sentTime = :sentTime where "
	    							   + " logisticId in (:packIds)").setParameterList("packIds", packIds).setParameter("sentTime", deliverTime).executeUpdate();
	    					tx.commit();
	    				}
	    				if(completedSent){
	    					logger.info("completed sent:" + afpFile.getNewBatchName());
	    					statusStr = "已交寄";
	    					//afpFile.setDeliverTime(latestDate);
	    					//if(afpFile.getCycleDate() == null  || today.getTime().getTime() >= afpFile.getCycleDate().getTime())
 							  // afpFile.setDeliverTime(today.getTime());
 							//else
 							   //afpFile.setDeliverTime(afpFile.getCycleDate());
	    					if(cardsAfp != null){
	    						if(cardsAfp.getBindTime() == null)
	    							cardsAfp.setBindTime(afpFile.getBindTime());
	    						if(cardsAfp.getPrintTime() == null)
	    							cardsAfp.setPrintTime(afpFile.getPrintTime());
	    						cardsAfp.setVerifyTime(afpFile.getVerifyTime());
								cardsAfp.setDeliverTime(afpFile.getDeliverTime());
	    					}
							if(receiptsAfp != null){
								if(receiptsAfp.getBindTime() == null)
									receiptsAfp.setBindTime(afpFile.getBindTime());
	    						if(receiptsAfp.getPrintTime() == null)
	    							receiptsAfp.setPrintTime(afpFile.getPrintTime());
								receiptsAfp.setVerifyTime(afpFile.getVerifyTime());
								receiptsAfp.setDeliverTime(afpFile.getDeliverTime());
							}
	    				}
                        
	    				//如果狀況有不同的，或頁數或紙張數有不同，就更新	    				
	    				if( afpFile.getSheets() == null 
    							|| !afpFile.getSheets().equals(sheets) 
    							|| afpFile.getPages() == null 
    							|| !afpFile.getPages().equals(pages)
	    						|| afpFile.getStatus() == null 
	    						|| !statusStr.equals(afpFile.getStatus()) ){
	    					
	    					
	    					Transaction tx = session.beginTransaction();
	    					logger.info(afpFile.getNewBatchName() + ":::" + statusStr);
	    					afpFile.setStatus(statusStr);
	    					afpFile.setUpdateDate(today.getTime());
	    					session.update(afpFile);
	    					tx.commit();
	    					tx = session.beginTransaction();
	    					if(cardsAfp != null){
								cardsAfp.setDeliverTime(afpFile.getDeliverTime());
								cardsAfp.setStatus(statusStr);
		    					cardsAfp.setUpdateDate(today.getTime());
		    					session.update(cardsAfp);
	    					}
							if(receiptsAfp != null){
								receiptsAfp.setDeliverTime(afpFile.getDeliverTime());
								receiptsAfp.setStatus(statusStr);
		    					receiptsAfp.setUpdateDate(today.getTime());
		    					session.update(receiptsAfp);
							}
	    					
	    					tx.commit();
	    				}	    					    				
	    			}
	    		}
	    	}	    	
	    }catch(Exception e){
	    	logger.error("", e);
	    	e.printStackTrace();
	    }finally{
	    	jobBagListenerRunnig = false;
	    	if(session != null && session.isOpen())
	    		session.close();
	    	if(session2 != null && session2.isOpen())
	    		session2.close();
	    	
	    }
	    
	} 
	
	/*
	 * 因為架構改變
	//這段程式目前不使用
	 * 
	 */
	public static void gpgListener(){
		if(gpgListenerRunning)
			return;
		
		gpgListenerRunning = true;		
		Date today = new Date();
		Properties properties = PropertiesService.getProperties();
		SftpClientUtil sftpC = new SftpClientUtil();
		sftpC.setServerAddress(properties.getFxSftpIp());
		
		
		sftpC.setUserId(properties.getFxdmsUser());
		sftpC.setPassword(properties.getFxdmsPwd());
		Session session = null;
		Transaction tx = null;
		try {
			// zip.gpg
			String forDownload = sftpC.getFile(properties.getFxdmsUploadPath()); //找出想要解開的gpg檔案
			File destFile = new File(ServletContextGetter.getRealPath("/pdf"), forDownload);
			String zipFileStr = forDownload.substring(0, forDownload.length() - 4); //解開後的zip檔
			File zipFile = new File(ServletContextGetter.getRealPath("/pdf"), zipFileStr);
			if(destFile.exists()){
				FileUtils.forceDelete(destFile);
			}
			if(zipFile.exists()){
				FileUtils.forceDelete(zipFile);
			}
			boolean success = sftpC.downloadFile(properties.getFxdmsUploadPath(), forDownload, destFile, true);
			//傳成功後的處理
			//1.把server上原來的檔案刪除  2.先解密  3.再解壓縮  4.再回傳回原來目錄  5.把不必要的檔案都刪除
			if(success){				
				sftpC.delete(properties.getFxdmsUploadPath(), forDownload);
				sftpC.delete(properties.getFxdmsUploadPath(), forDownload.substring(0, forDownload.length() - 8) + ".ok");
				PublicKey pk = FileEncFactory.loadPublicKey(FileEncFactory.publicKey);
				String mainName = zipFile.getName().substring(0, zipFile.getName().length() - 4);
				new FileEncFactory().decodeFile(pk, destFile, zipFile);
				ZipUtils.unzipFile(zipFile, new File(ServletContextGetter.getRealPath("/pdf")));
				
				
				File afpFile = new File(ServletContextGetter.getRealPath("/pdf"), mainName + ".afp");
				File logFile = new File(ServletContextGetter.getRealPath("/pdf"), mainName + "_summary.csv");
				if(afpFile.exists()){
					sftpC.upload(properties.getFxdmsUploadPath(), afpFile);
				}
				if(logFile.exists()){
					sftpC.upload(properties.getFxdmsUploadPath(), afpFile);
					FileUtils.forceDelete(logFile);
				}
				FileUtils.forceDelete(destFile);
				FileUtils.forceDelete(zipFile);
				
				//待續，轉pdf，切pdf，先用streamEdp，之後再改
				File pdfFile = new File(ServletContextGetter.getRealPath("/pdf"), mainName + ".pdf");
				AfpToPdfUtil.streamEdpToPdf(afpFile.getAbsolutePath(), pdfFile.getAbsolutePath());
				if(new File(ServletContextGetter.getRealPath("/pdf"), mainName + ".afp").exists()){
					FileUtils.forceDelete(afpFile);
					
					session = HibernateSessionFactory.getSession();
					tx = session.beginTransaction();
					AfpFile afp = (AfpFile) session.get(AfpFile.class, mainName);					
					PDDocument pdfDoc = null;
					File rasFile = null;
					RandomAccessFile ras = null;
					try {		
					   rasFile = new File(ServletContextGetter.getRealPath("/pdf"), UUID.randomUUID() + "");
					   ras = new RandomAccessFile(rasFile, "rw");
					   pdfDoc = PDDocument.load(pdfFile, ras);
					   
					   Set<ApplyData> applyDatas = afp.getApplyDatas();
					   for(ApplyData applyData : applyDatas){
						   if(applyData.getTotalPage() > 0){
						      int startPage = applyData.getAfpBeginPage();
						      int endPage = applyData.getAfpEndPage();
						      String pdfName = applyData.getPolicyPDF();						      
						      PdfFileUtil.splitDocument(startPage, endPage, new File(ServletContextGetter.getRealPath("/pdf"), pdfName).getAbsolutePath(), pdfDoc);
						      applyData.setPolicyStatus("30");
						      applyData.setPresTime(today);
						      applyData.setUpdateDate(today);
						      session.update(applyData);
						   }
					   }	
					} catch (IOException e) {
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
					afp.setStatus("列印中");
					afp.setUnziped(true);
					afp.setTransfered(true);
					afp.setUpdateDate(today);
					session.update(afp);
					if(tx != null)
						 tx.commit();
				}
			}			
		} catch (Exception e) {
			if(tx != null)
				tx.rollback();
			logger.error("", e);
			e.printStackTrace();
		}finally{
			if(session != null && session.isOpen())
				session.close();
		}
		gpgListenerRunning = false;
	}
	

}
