package com.fxdms.util;

import javax.mail.internet.*;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;


import java.io.File;
import java.util.List;

public class MailSender {
    private String[] mailTo, mailCc, mailBcc;
    private String host, mailFrom, sub, msg, fileName, userName, password;
	private JavaMailSenderImpl senderImpl;
	Logger logger = Logger.getLogger(MailSender.class);
    
	public String[] getMailCc() {
		return mailCc;
	}

	public void setMailCc(String[] mailCc) {
		this.mailCc = mailCc;
	}

	public String[] getMailBcc() {
		return mailBcc;
	}

	public void setMailBcc(String[] mailBcc) {
		this.mailBcc = mailBcc;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setMailTo(String[] mailTo) {
		this.mailTo = mailTo;
	}
    
    
    public MailSender() {
    }
    
    /**
     * Constructor of class SendMail
     * @param host
     * @param mailFrom
     * @param mailTo
     * @param mailCc
     * @param mailBcc
     * @param sub
     * @param msg
     * @param fileName
     */
    public MailSender(
    		String host,
    		String mailFrom,
    		String[] mailTo,
    		String[] mailCc,
    		String[] mailBcc,
    		String sub,
    		String msg,
    		String fileName) {
		setHost(host);
		setMailFrom(mailFrom);
		setMailTo(mailTo);
		setMailCc(mailCc);
		setMailBcc(mailBcc);
        setSub(sub);
        setMsg(msg);
        setFileName(fileName);
    }
    
    /**
     * Constructor of class SendMail
     * @param host
     * @param mailFrom
     * @param mailTo
     * @param sub
     * @param msg
     */
    public MailSender(
    		String host,
    		String mailFrom,
    		String[] mailTo,
    		String sub,
    		String msg) {
    	this(host, mailFrom, mailTo, null, null, sub, msg, null);
    }
    
    public MailSender(
    		String host,
    		String mailFrom,
    		String[] mailTo,
    		String sub,
    		String msg,
    		String fileName) {
    	this(host, mailFrom, mailTo, null, null, sub, msg, fileName);
    }
    
    public MailSender(
    		String host,
    		String mailFrom,
    		List<String> mailTo,
    		String sub,
    		String msg,
    		String fileName) 
    {
    	this();
    	String [] mailAdds = null;
    	if(mailTo != null){
     	    mailAdds = new String [mailTo.size()];
     	    for(int i = 0 ; i < mailAdds.length ; i++){
     	    	mailAdds[i] = mailTo.get(i);
     	    }
    	}
    	this.setHost(host);
    	this.setMailFrom(mailFrom);
    	this.setMailTo(mailAdds);
    	this.setSub(sub);
    	this.setMsg(msg);
    	this.setFileName(fileName);
    }
    /**
     * For changing subject and content.
     * @param sub Mail subject
     * @param msg Mail content
     */
	public void setMailContent(String sub, String msg, String fileName){
		setSub(sub);
		setMsg(msg);
		setFileName(fileName);
	}
    
    /**
     * Send the mail.
     * @return strResult Result after the mail was sent
     */
    public String send() {
    	String result = " success";
    	try {
	    	if (senderImpl == null)
	    		senderImpl = new JavaMailSenderImpl();
	        // 設定 Mail Server
	    	senderImpl.setHost(host); 
	    	if(userName != null && !userName.trim().equals(""))
	    		senderImpl.setUsername(userName);
	    	if(password != null && !password.trim().equals(""))
	    		senderImpl.setPassword(password);
	    	
	        // 建立郵件訊息
	        MimeMessage mailMessage = senderImpl.createMimeMessage();
	
	        //true代表html mail
	        MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "UTF-8");
	                
	        // 設定寄件人、收件人、主題與內文
	        messageHelper.setFrom(mailFrom);
	        for(String mailAdd : mailTo){
	        	System.out.println(mailAdd);
	        }
	        if(mailTo != null) messageHelper.setTo(mailTo);
	        if(mailCc != null) messageHelper.setCc(mailCc);
	        if(mailBcc != null) messageHelper.setBcc(mailBcc);
	        messageHelper.setSubject(sub);
	        messageHelper.setText(msg, false);
	        if(fileName != null && !fileName.equals("")) {
		        sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
		        FileSystemResource file = new FileSystemResource(new File(fileName));
		        messageHelper.addAttachment("=?BIG5?B?" + enc.encode(file.getFilename().getBytes()) + "?=", file);
	        }
	        // 傳送郵件
	        senderImpl.send(mailMessage); 
    	} catch(Exception e) {
    		result = e.getMessage();
    		logger.error("", e);
    		
    	}
    	logger.info("send mail result:" + result);
        return result;
    }

    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
     * 
     * @param millis
     */
    public void waitFor(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args) throws Exception {
    	MailSender ms = new MailSender(
    			"service.piinet.net",
    			"kennylo@service.piinet.net",
    			new String[] {"kennylo@service.piinet.net"}, 
    			"Spring Test",
    			"Content",
    			"c:/abc.txt");
        
        System.out.println(ms.send());
        ms.setMailContent("中文來啦 sub", "中文來啦msg", "c:/檔案utf8.txt");
        System.out.println(ms.send());
        ms.setMailContent("中文來啦 sub", "中文來啦msg", "c:/檔案.doc");
        System.out.println(ms.send());
        ms.setMailContent("中文來啦 sub", "中文來啦msg", "c:/檔案Ansi.txt");
        System.out.println(ms.send());
        
    }


}