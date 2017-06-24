package com.salmat.genericdao.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.salmat.genericdao.GenericDao;
import com.salmat.genericdao.finder.FinderArgumentTypeFactory;
import com.salmat.genericdao.finder.FinderExecutor;
import com.salmat.genericdao.finder.FinderNamingStrategy;
import com.salmat.genericdao.finder.impl.SimpleFinderArgumentTypeFactory;
import com.salmat.genericdao.finder.impl.SimpleFinderNamingStrategy;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of GenericDao A typesafe implementation of CRUD and
 * finder methods based on Hibernate and Spring AOP The finders are implemented
 * through the executeFinder method. Normally called by the
 * FinderIntroductionInterceptor
 */
@SuppressWarnings("unchecked")
public class GenericDaoHibernateImpl<T, PK extends Serializable> extends
		HibernateDaoSupport implements GenericDao<T, PK>, FinderExecutor {
	// Default.Can override in config
	private FinderNamingStrategy namingStrategy = new SimpleFinderNamingStrategy();
	// Default.Can override in config
	private FinderArgumentTypeFactory argumentTypeFactory = new SimpleFinderArgumentTypeFactory(); 

	private Class<T> type;

	public GenericDaoHibernateImpl(Class<T> type) {
		this.type = type;
	}
	public Class<T> getClazz() {
			return type;
		}

	//	@Transactional(propagation = Propagation.REQUIRED)
	public PK create(T o) {
		PK pk = (PK) getHibernateTemplate().save(o);
		return pk;

	}

	public void saveOrUpdate(T o) {
		getHibernateTemplate().saveOrUpdate(o);
	}
	
	public T read(PK id) {
		return (T) getHibernateTemplate().get(type, id);
	}

	public void update(T o) {
		getHibernateTemplate().update(o);
	}

	public void delete(T o) {
		getHibernateTemplate().delete(o);
	}

	public void deleteAll(Collection <T> entities) {
		getHibernateTemplate().deleteAll(entities);;
	}
	
	public List<T> executeFinder(Method method, final Object[] queryArgs,
			int startRownum, int resultSize) {
		final Query namedQuery = prepareQuery(method, queryArgs);
		namedQuery.setFirstResult(startRownum);
		namedQuery.setMaxResults(resultSize);
		return (List<T>) namedQuery.list();
	}
	
	
	public List<T> executeFinder(Method method, final Object[] queryArgs) {
		final Query namedQuery = prepareQuery(method, queryArgs);
		return (List<T>) namedQuery.list();
	}

	public Iterator<T> iterateFinder(Method method, final Object[] queryArgs) {
		final Query namedQuery = prepareQuery(method, queryArgs);
		return (Iterator<T>) namedQuery.iterate();
	}

	private Query prepareQuery(Method method, Object[] queryArgs) {
		final String queryName = getNamingStrategy().queryNameFromMethod(type,
				method);
		final Query namedQuery = getSession().getNamedQuery(queryName);
		String[] namedParameters = namedQuery.getNamedParameters();

		if (namedParameters.length == 0) {
			setPositionalParams(queryArgs, namedQuery);
		} else {
			setNamedParams(namedParameters, queryArgs, namedQuery);
		}
		return namedQuery;
	}

	private void setPositionalParams(Object[] queryArgs, Query namedQuery) {
		// Set parameter. Use custom Hibernate Type if necessary
		if (queryArgs != null) {
			for (int i = 0; i < queryArgs.length; i++) {
				Object arg = queryArgs[i];
				Type argType = getArgumentTypeFactory().getArgumentType(arg);
				if (argType != null) {
					namedQuery.setParameter(i, arg, argType);
				} else {
					namedQuery.setParameter(i, arg);
				}
			}
		}
	}

	private void setNamedParams(String[] namedParameters, Object[] queryArgs,
			Query namedQuery) {
		// Set parameter. Use custom Hibernate Type if necessary
		if (queryArgs != null) {
			for (int i = 0; i < queryArgs.length; i++) {
				Object arg = queryArgs[i];
				Type argType = getArgumentTypeFactory().getArgumentType(arg);
				if (argType != null) {
					namedQuery.setParameter(namedParameters[i], arg, argType);
				} else {
					if (arg instanceof Collection) {
						namedQuery.setParameterList(namedParameters[i],
								(Collection) arg);
					} else {
						namedQuery.setParameter(namedParameters[i], arg);
					}
				}
			}
		}
	}

	public FinderNamingStrategy getNamingStrategy() {
		return namingStrategy;
	}

	public void setNamingStrategy(FinderNamingStrategy namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	public FinderArgumentTypeFactory getArgumentTypeFactory() {
		return argumentTypeFactory;
	}

	public void setArgumentTypeFactory(
			FinderArgumentTypeFactory argumentTypeFactory) {
		this.argumentTypeFactory = argumentTypeFactory;
	}
	
//	/**
//	 * Use this inside subclasses as a convenience method.
//	 */
//	@SuppressWarnings("unchecked")
//	public List<T> readByCriteria(Criterion... criterion) {
//		Criteria crit = getSession().createCriteria(getType());
//		for (Criterion c : criterion) {
//			crit.add(c);
//		}
//		return crit.list();
//	}
	
	public Criteria getCriteria() {
		return getSession().createCriteria(getClazz());
	}
	public HibernateTemplate getHiberTemplate() {   //Thor修改，實作取得HibernateTemplate的方法
		// TODO Auto-generated method stub
		return getHibernateTemplate();
	}
}
