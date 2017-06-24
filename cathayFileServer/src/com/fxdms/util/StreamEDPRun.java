package com.fxdms.util;

import java.io.IOException;

import com.enterprisedt.util.debug.Logger;

public class StreamEDPRun implements Runnable{

	private static Logger logger = Logger.getLogger(StreamEDPRun.class); 
	private String streamEDPDir = null;
	private String afpFile;
	private String detFile;
	private boolean finishProcess;	
	private Process process = null;

	public void run() {
		finishProcess = false;
		String execCommand = streamEDPDir + "bin/StreamEDPProject.bat" + " "
		    + afpFile + " " + detFile;
        System.out.println(execCommand);
        logger.info(execCommand);
        try {
			process = Runtime.getRuntime().exec(execCommand);
			process.waitFor();
	        process.destroy();
		} catch (IOException e) {
			logger.error("", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("", e);
			e.printStackTrace();
		}
		finishProcess = true;
	}
	public Process getProcess() {
		return process;
	}
	public void setStreamEDPDir(String streamEDPDir) {
		this.streamEDPDir = streamEDPDir;
	}
	public void setAfpFile(String afpFile) {
		this.afpFile = afpFile;
	}
	public void setDetFile(String detFile) {
		this.detFile = detFile;
	}
	public boolean isFinishProcess() {
		return finishProcess;
	}
}
