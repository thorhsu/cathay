package com.fxdms.cathy.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.salmat.pas.vo.ErrorReport;
import com.salmat.pas.vo.Properties;

public class StartMain {
	static String systemStartFile = "D:/cathayTransfer/systemStrarted.fle";
	private static Properties properties = null;
	static Logger logger = Logger.getLogger(StartMain.class);

	public static void main(String[] args) {
		FileWriter fw = null;
		try {
			File systemStart = new File(systemStartFile);
			if(!systemStart.getParentFile().exists())
				systemStart.getParentFile().mkdirs();
			if (systemStart.exists()) {
				try {
					FileUtils.forceDelete(systemStart);
					System.out.println("delete finished");
				} catch (IOException e) {
                   logger.error("已有別的傳檔AP啟動中，請先關閉再啟動此程序");
                   return;
				}

			}
			
			fw = new FileWriter(systemStart);
			fw.write("start");				
			Constant.setContext(new ClassPathXmlApplicationContext(
					"/com/fxdms/cathy/conf/applicationContexCathay.xml"));
			logger.info("started up cathay policy book file transfer system");
			InetAddress address = InetAddress.getLocalHost();			
			
	        properties = ((VoService) Constant.getContext().getBean("voServiceProxy")).getProperties();
	        //properties.setProcessDone(false);
	        ((VoService) Constant.getContext().getBean("voServiceProxy")).update(properties);
	        BeanUtils.copyProperties(properties, new com.fxdms.cathy.bo.Properties());
	        com.fxdms.cathy.bo.Properties.setInitialnized(true);
	        
			System.out.println("start up system.");
			ErrorReport er = new ErrorReport();
			er.setErrHappenTime(new Date());
			er.setErrorType("startUp");
			er.setOldBatchName("");
			er.setReported(true);
			er.setMessageBody("國壽保單傳檔系統啟動成功");
			er.setTitle("系統啟動");
			((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
			Thread.sleep(Long.MAX_VALUE);
		} catch (Exception e) {
			ErrorReport er = new ErrorReport();
			er.setErrHappenTime(new Date());
			er.setErrorType("startUp");
			er.setOldBatchName("");
			er.setReported(true);
			er.setMessageBody("國壽保單傳檔系統啟動失敗:" + e.getMessage());
			er.setTitle("系統啟動");
			((VoService) Constant.getContext().getBean("voServiceProxy")).save(er);
			logger.error("", e);
			e.printStackTrace();
		}finally{
			System.out.println("into finish process");
			if(fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			System.exit(0);
		}

	}

	public String println(String string) {
		System.out.println(string);
		return null;
	}
}