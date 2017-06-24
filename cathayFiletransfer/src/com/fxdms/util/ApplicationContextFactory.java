package com.fxdms.util;



//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * This class should be used by non-spring-wired classes if they need access to
 * the application context
 */
public class ApplicationContextFactory implements ApplicationContextAware {
	// private static Log log =
	// LogFactory.getLog(ApplicationContextFactory.class) ;

	private static Object initObj = null;
	private static int count = 0;
	private static ApplicationContext context;

	public static void init(Object obj) {
		if (count > 0) {
			// log.error("Can't initialize the application context twice: THIS SHOULD ONLY HAPPEN DURING TESTING");
		}
		initObj = obj;
		count++;
	}

	public static ApplicationContext getApplicationContext() {
		if (initObj == null) {
			throw new IllegalStateException(
					"Application context not initialized");
		} else if (initObj instanceof ApplicationContext) {
			ApplicationContext appContext = (ApplicationContext) initObj;
			return appContext;

		} else if (initObj instanceof String) {
			if (context == null) {
				String contextResourceLocation = (String) initObj;
				context = new ClassPathXmlApplicationContext(
						contextResourceLocation);
			}
			return context;
		} else {
			throw new IllegalStateException(
					"You must initialize the context with a String");
		}
	}



	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		init(context);
		// TODO Auto-generated method stub
		
	}

}
