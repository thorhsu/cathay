package com.salmat.pas.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;

import com.enterprisedt.util.debug.Logger;
import com.salmat.pas.bo.PropertiesService;
import com.salmat.pas.conf.Constant;
import com.salmat.pas.filter.ServletContextGetter;
import com.salmat.pas.vo.AdminUser;
import com.salmat.pas.vo.ApplyData;
import com.salmat.pas.vo.Properties;
import com.salmat.util.FtpClientUtil;
import com.salmat.util.HibernateSessionFactory;
import com.salmat.util.PdfFileUtil;


public class PdfViewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(PdfViewServlet.class);
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PdfViewServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected synchronized void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		AdminUser adminUser = (AdminUser)request.getSession().getAttribute("loginUser");
		if(adminUser == null){
		   return;	
		}
		Properties props = PropertiesService.getProperties();
		String oldBatchName = request.getParameter("oldBatchName");
		Session session = null;
		try{
			session = HibernateSessionFactory.getSession();
			ApplyData applyData = (ApplyData) session.get(ApplyData.class, oldBatchName);
			//如果不是許可的轄區，則取不到
			if(adminUser.getCenter() != null && !adminUser.getCenter().trim().equals("") && !adminUser.getCenter().trim().equals(applyData.getCenter())){
				return;
			}
			
			String pdfNm = applyData.getPolicyPDF();
			File localDownload = new File(ServletContextGetter.getRealPath("pdf"), pdfNm + ".pdf");
			//如果不存在就從file server取得
			if(!localDownload.exists()){
			   //取得file server的ftp參數			   
			   FtpClientUtil fcu = new FtpClientUtil(props.getFileServerIp(), props.getFileServerUser(), props.getFileServerPwd());
			   // 從file server download file
			   fcu.downloadFile("", pdfNm + ".pdf", localDownload, false);
			}
			
			File decryptedFile = new File(ServletContextGetter.getRealPath("pdf"), UUID.randomUUID() + ".pdf");
			//解密
			PdfFileUtil.decryptPdf(localDownload.getAbsolutePath(), decryptedFile, Constant.getPdfpwd());
			
			InputStream is = null;
			BufferedInputStream bis = null;
			BufferedOutputStream bos = null;
			OutputStream os = null;
			try {
				response.setContentType("application/pdf");
				response.addHeader("Content-disposition", "attachment; filename=\""
						+ decryptedFile.getName() + "\"");
				is = ServletContextGetter.getInputStream("/pdf/" + decryptedFile.getName());
				bis = new BufferedInputStream(is);
	            os = response.getOutputStream();
	            bos = new BufferedOutputStream(os);
				int bytesRead = 0;
				byte [] buffer = new byte[1024];
				while((bytesRead = bis.read(buffer)) != -1){
					bos.write(buffer, 0, bytesRead);
				}
				bos.flush();
				os.flush();

			} catch (Exception e) {
				logger.error("", e);
				e.printStackTrace();
			} finally {
				try {
					if (bis != null)
						bis.close();
					if (is != null)
						is.close();
					if(bos != null)
						bos.close();
					if(os != null)
						os.close();
				} catch (IOException e) {
					logger.error("", e);
					e.printStackTrace();
				}
				bis = null;
				is = null;
				bos = null;
				os = null;
				//全部做完後刪除解密的檔案
				if(decryptedFile.exists())
				   FileUtils.forceDelete(decryptedFile);
			}
			
		}catch(Exception e){
			logger.error("", e);
			e.printStackTrace();
		}finally{
			if(session != null)
				session.close();
		}
	} 
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
