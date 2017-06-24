package com.salmat.genericdao.finder.impl;

import com.salmat.genericdao.finder.FinderExecutor;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.IntroductionInterceptor;

/**
 * Connects the Spring AOP magic with the Hibernate DAO magic For any method
 * beginning with "find" this interceptor will use the FinderExecutor to call a
 * Hibernate named query
 */
public class FinderIntroductionInterceptor implements IntroductionInterceptor {

	@SuppressWarnings("unchecked")
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {

		FinderExecutor executor = (FinderExecutor) methodInvocation.getThis();

		String methodName = methodInvocation.getMethod().getName();
		if (methodName.startsWith("find") || methodName.startsWith("list")) {
			Object[] arguments = methodInvocation.getArguments();
			return executor.executeFinder(methodInvocation.getMethod(),
					arguments);
		} else if (methodName.startsWith("rowFind")) { //撈出指定筆數範圍資料
			Object[] argumentsOld = methodInvocation.getArguments();
			int startRownum = Integer.parseInt(argumentsOld[0].toString());
			int resultSize = Integer.parseInt(argumentsOld[1].toString());
			//刪除前兩參數，分別為開始rownum, 結束rownum
			Object[] argumentsNew = new Object[argumentsOld.length - 2];
			if (argumentsOld.length > 2) {
				for(int n = 0; n < argumentsNew.length; n++)
					argumentsNew[n] = argumentsOld[n + 2];
			}
			return executor.executeFinder(methodInvocation.getMethod(),
					argumentsNew, startRownum, resultSize);
		} else if (methodName.startsWith("iterate")) {
			Object[] arguments = methodInvocation.getArguments();
			return executor.iterateFinder(methodInvocation.getMethod(),
					arguments);
		}
		// else if(methodName.startsWith("scroll"))
		// {
		// Object[] arguments = methodInvocation.getArguments();
		// return executor.scrollFinder(methodInvocation.getMethod(),
		// arguments);
		// }
		else {
			return methodInvocation.proceed();
		}
	}

	@SuppressWarnings("unchecked")
	public boolean implementsInterface(Class intf) {
		return intf.isInterface()
				&& FinderExecutor.class.isAssignableFrom(intf);
	}
}
