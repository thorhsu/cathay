package com.fxdms.rmi.service;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

import org.apache.log4j.Logger;



public class MyRMISocket extends RMISocketFactory implements Serializable {
	/**  
     *   
     */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(MyRMISocket.class);

	public Socket createSocket(String host, int port) throws IOException {
		System.out.println("my host:" + host + "| port:" + port);
		logger.info("my host:" + host + "| port:" + port);
		return new Socket(host, port);
	}

	public ServerSocket createServerSocket(int port) throws IOException {
		System.out.println("RMI transfert port =" + port);
		if (port == 0)
			port = 1099;

		System.out.println("RMI transfert port =" + port);
		logger.info("RMI transfer port =" + port);
		return new ServerSocket(port);
	}
}