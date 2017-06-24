package com.salmat.pas.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.salmat.pas.filter.ServletContextGetter;





/**
 * Servlet implementation class DownloadServlet
 */
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger log = Logger.getLogger(DownloadServlet.class);   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DownloadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String fileName = request.getParameter("fileName");
		String contentType = null;
		String application = request.getParameter("application");
		String charset = request.getParameter("charset") == null? "" : ";charset=" + request.getParameter("charset"); 
		//如果是空的，就是force-download，否則就是呼叫應用程式
		contentType = (application == null || application.trim().equals(""))? "force-download" : application + charset; 
		
		InputStream is = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		OutputStream os = null;
		try {
			response.setContentType("application/" + contentType );
			response.addHeader("Content-disposition", "attachment; filename=\""
					+ fileName + "\"");
			is = ServletContextGetter.getInputStream("/pdf/" + fileName);
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
			log.error("", e);
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
				log.error("", e);
				e.printStackTrace();
			}
			bis = null;
			is = null;
			bos = null;
			os = null;
		}		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
