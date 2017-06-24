package com.salmat.genericdao.finder;

import java.lang.reflect.Method;

/**
 * Used to locate a named query based on the called finder method
 */
public interface FinderNamingStrategy {
    @SuppressWarnings("unchecked")
	public String queryNameFromMethod(Class findTargetType, Method finderMethod);
}
