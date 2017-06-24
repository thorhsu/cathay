package com.salmat.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class SmsHttpClient {
	private static CloseableHttpClient httpclient;
	static Logger logger = Logger.getLogger(SmsHttpClient.class);

	public static void main(String[] args) {
		String[] phones = new String[1];
		//phones[1] = "0934007255";
		phones[0] = "0926845237";
		try {
			sendMessageGet(phones, "2014-08-15台南結果：新契約583件，保補契轉42件。作業正常\r\n2014-08-15北二結果：新契約285件，保補契轉5件。作業正常");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean sendMessageGet(String[] phoneNums, String msg)
			throws IOException {		
		try {
			// http://smexpress.mitake.com.tw:9600/SmSendGet.asp?username=03374707&password=03374707&dstaddr=0926845237&destName=Thor&dlvtime=20060811093000&vldtime=6000&smbody=thisisatest
			for (String phoneNum : phoneNums) {
				httpclient = HttpClients.createDefault();
				String str1 = "http://smexpress.mitake.com.tw:9600/SmSendGet.asp?username=03374707&password=03374707&dstaddr=";
				String str2 = "&dlvtime=20060811093000&vldtime=6000&encoding=UTF8&smbody=";
				String url = str1 + phoneNum + str2
						+ URLEncoder.encode(msg, "UTF-8");
				HttpGet httpGet = new HttpGet(url);
				CloseableHttpResponse response1 = httpclient.execute(httpGet);
				// The underlying HTTP connection is still held by the response
				// object
				// to allow the response content to be streamed directly from
				// the network socket.
				// In order to ensure correct deallocation of system resources
				// the user MUST call CloseableHttpResponse#close() from a
				// finally clause.
				// Please note that if response content is not fully consumed
				// the underlying
				// connection cannot be safely re-used and will be shut down and
				// discarded
				// by the connection manager.
				try {
					logger.info(phoneNum + ":" + response1.getStatusLine());
					HttpEntity entity1 = response1.getEntity();
					// do something useful with the response body
					// and ensure it is fully consumed
					EntityUtils.consume(entity1);
				} catch (Exception e) {
					logger.error("", e);
					e.printStackTrace();
				} finally {
					response1.close();
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (httpclient != null)
				httpclient.close();
		}

	}
	
	//post無用號，送不出簡訊
	public static boolean sendMessagePost(String[] phoneNums, String msg)
			throws IOException {
		httpclient = null;
		try {
			httpclient = HttpClients.createDefault();
			// http://smexpress.mitake.com.tw:9600/SmSendGet.asp?username=03374707&password=03374707&dstaddr=0926845237&destName=Thor&dlvtime=20060811093000&vldtime=6000&smbody=thisisatest
            HttpPost httpPost = new HttpPost("http://smexpress.mitake.com.tw.:9600/SmSendPost.asp?username=03374707&password=03374707&encoding=UTF8");
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("username", "03374707"));
            nvps.add(new BasicNameValuePair("password", "03374707"));
            nvps.add(new BasicNameValuePair("encoding", "UTF8"));
            for(String phoneNum : phoneNums){
               nvps.add(new BasicNameValuePair("dstaddr", phoneNum));
            }
            nvps.add(new BasicNameValuePair("dlvtime", "20060811093000"));
            nvps.add(new BasicNameValuePair("vldtime", "6000"));            
            nvps.add(new BasicNameValuePair("smbody", msg));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response2 = httpclient.execute(httpPost);

            try {
                System.out.println(response2.getStatusLine());
                HttpEntity entity2 = response2.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(entity2);
            } finally {
                response2.close();
            }

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (httpclient != null)
				httpclient.close();
		}

	}

}
