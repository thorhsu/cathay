package com.fxdms.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fxdms.cathy.conf.Constant;
import com.fxdms.rmi.service.VoService;
import com.salmat.pas.vo.ErrorReport;

public class Test1 {

	@BeforeTest
	// 標記指定所有測試進行前呼叫此 method
	public void beforeAllTest() {
		System.out.println("Before All Test");
	}

	@BeforeMethod
	// 標記指定每個測試進行前呼叫此 method
	public void setUp() {
		System.out.println("Before Each Test Method");
	}

	@Test(groups = { "group1" })
	// 標記為測試程式, 並為分組 group1
	public void group1Test() {
		System.out.println("Group 1");
	}

	@Test(groups = { "group2" })
	// 標記為測試程式, 並為分組 group2
	public void group2Test() {
		System.out.println("Group 2");
	}

	@AfterMethod
	// 標記指定每個測試進行後呼叫此 method
	public void tearDown() {
		System.out.println("After Each Test Method");
	}

	@AfterTest
	// 標記指定所有測試進行後呼叫此 method
	public void afterAllTest() {
		System.out.println("After All Test");
	}

	public static void main(String[] args) {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		// 開始進行合併保單
		File[] files = new File("d:/tmp/tmp").listFiles();
		for (File file : files) {

			try {
				byte[] bufferedB = new byte[2048];
				fos = new FileOutputStream(
						new File(
								"d:/tmp/2014-07-17_2014-07-17_04_NORM_MI401499_非合併件簽收回條"),
						true);
				bos = new BufferedOutputStream(fos);
				fis = new FileInputStream(file);
				bis = new BufferedInputStream(fis);
				int readLen;
				while ((readLen = bis.read(bufferedB)) > 0) {
					bos.write(bufferedB, 0, readLen);
				}
				bos.flush();
				fos.flush();
			} catch (Exception e) {
				ErrorReport er = new ErrorReport();
				er.setErrHappenTime(new Date());
				er.setErrorType("exception");
				er.setOldBatchName("");
				er.setReported(false);
				er.setMessageBody("exception happen:" + e.getMessage());
				er.setTitle("exception happened");
				((VoService) Constant.getContext().getBean("voServiceProxy"))
						.save(er);
			} finally {
				try {
					if (bos != null)
						bos.close();
					if (fos != null)
						fos.close();
					if (bis != null)
						bis.close();
					if (fis != null)
						fis.close();
				} catch (Exception e) {
                    e.printStackTrace();
				}
			}
		}
	}
}