package com.salmat.pas.bo;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteRef;

import org.springframework.remoting.rmi.RmiServiceExporter;

import sun.rmi.server.Util;
import sun.rmi.registry.RegistryImpl;
import sun.rmi.server.UnicastRef;
import sun.rmi.server.UnicastRef2;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;

public class MyRmiExporter extends RmiServiceExporter {
	protected Registry getRegistry(String registryHost, int registryPort,
			RMIClientSocketFactory clientSocketFactory,
			RMIServerSocketFactory serverSocketFactory) throws RemoteException {

		if (registryHost != null) {
			// Host explictly specified: only lookup possible.
			if (logger.isInfoEnabled()) {
				logger.info("Looking for RMI registry at port '" + registryPort
						+ "' of host [" + registryHost + "]");
			}
			try {
				Registry reg = getRegistry(registryHost, registryPort,
						clientSocketFactory);
				System.out.println("myRmi1:" + registryHost + "|"
						+ registryPort);
				testRegistry(reg);
				return reg;
			} catch (RemoteException ex) {
				logger.debug("RMI registry access threw exception", ex);
				logger.warn("Could not detect RMI registry - creating new one");
				// Assume no registry found -> create new one.
				LocateRegistry.createRegistry(registryPort);
				Registry reg = getRegistry("172.16.16.109", registryPort,
						clientSocketFactory);
				System.out.println("myRmi2:" + registryHost + "|"
						+ registryPort);
				testRegistry(reg);
				return reg;
			}
		}

		else {
			System.out.println("myRmi3:" + registryHost + "|" + registryPort);
			return getRegistry(registryPort, clientSocketFactory,
					serverSocketFactory);
		}
	}

	public static Registry getRegistry(String host, int port,
			RMIClientSocketFactory csf) throws RemoteException {
		Registry registry = null;

		if (port <= 0)
			port = Registry.REGISTRY_PORT;

		if (host == null || host.length() == 0) {
			// If host is blank (as returned by "file:" URL in 1.0.2 used in
			// java.rmi.Naming), try to convert to real local host name so
			// that the RegistryImpl's checkAccess will not fail.
			try {
				host = java.net.InetAddress.getLocalHost().getHostAddress();
			} catch (Exception e) {
				// If that failed, at least try "" (localhost) anyway...
				host = "";
			}
		}

		/*
		 * Create a proxy for the registry with the given host, port, and client
		 * socket factory. If the supplied client socket factory is null, then
		 * the ref type is a UnicastRef, otherwise the ref type is a
		 * UnicastRef2. If the property java.rmi.server.ignoreStubClasses is
		 * true, then the proxy returned is an instance of a dynamic proxy class
		 * that implements the Registry interface; otherwise the proxy returned
		 * is an instance of the pregenerated stub class for RegistryImpl.
		 */
		LiveRef liveRef = new LiveRef(new ObjID(ObjID.REGISTRY_ID),
				new TCPEndpoint(host, port, csf, null), false);
		RemoteRef ref = (csf == null) ? new UnicastRef(liveRef)
				: new UnicastRef2(liveRef);

		return (Registry) Util.createProxy(RegistryImpl.class, ref, false);
	}

}
