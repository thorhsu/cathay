package com.salmat.genericdao;

import java.io.Serializable;
import java.util.Collection;

import org.hibernate.Criteria;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * The basic GenericDao interface with CRUD methods
 * Finders are added with interface inheritance and AOP introductions for concrete implementations
 *
 * Extended interfaces may declare methods starting with find... list... iterate... or scroll...
 * They will execute a preconfigured query that is looked up based on the rest of the method name
 */
public interface GenericDao<T, PK extends Serializable>
{
    public HibernateTemplate getHiberTemplate(); //Thor加入，讓繼承的介面可以取得 Spring的HibernateTemplate
	
    PK create(T newInstance);

    T read(PK id);

    void update(T transientObject);

	void saveOrUpdate(T o);
	
    void delete(T persistentObject);
    
	void deleteAll(Collection <T> entities);
    
    Criteria getCriteria();
}
