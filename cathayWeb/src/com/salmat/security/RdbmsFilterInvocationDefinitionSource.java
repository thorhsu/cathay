package com.salmat.security;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.SecurityConfig;
import org.acegisecurity.intercept.AbstractSecurityInterceptor;
import org.acegisecurity.intercept.web.FilterInvocation;
import org.acegisecurity.intercept.web.FilterInvocationDefinitionSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

/**
 * 
 * FilterInvocationDefinitionSource實做類別
 * 
 */
public class RdbmsFilterInvocationDefinitionSource extends JdbcDaoSupport
		implements FilterInvocationDefinitionSource {

//	protected static final Log logger = LogFactory.getLog(RdbmsFilterInvocationDefinitionSource.class);
	public static final String ACEGI_RDBMS_SECURED_SQL = "SELECT url, roles FROM webresdb ORDER BY id";
	
	//預設使用ACEGI_RDBMS_SECURED_SQL字串，亦可注入更改
    private String rolesUrlMappingQuery;
	
	private MappingSqlQuery rolesUrlMapping;
	private boolean cacheActive = false;
	

	private PathMatcher pathMatcher = new AntPathMatcher();
	
	private Ehcache webresdbCache;
	
	public RdbmsFilterInvocationDefinitionSource() {
		rolesUrlMappingQuery = ACEGI_RDBMS_SECURED_SQL;
	}
	
	public String getRolesUrlMappingQuery() {
		return rolesUrlMappingQuery;
	}

	public void setRolesUrlMappingQuery(String rolesUrlMappingQuery) {
		this.rolesUrlMappingQuery = rolesUrlMappingQuery;
	}

	/**
	 * 實現ObjectDefinitionSource介面的方法
	 * 最核心的方法, 幾乎可以認為RdbmsFilterInvocationDefinitionSource的其他大部分方法都是為這一方法服務的
	 * 
     * Accesses the <code>ConfigAttributeDefinition</code> that applies to a given secure object.<P>Returns
     * <code>null</code> if no <code>ConfigAttribiteDefinition</code> applies.</p>
     *
     * @param object the object being secured
     *
     * @return the <code>ConfigAttributeDefinition</code> that applies to the passed object
     * @返回 適用於傳入物件的ConfigAttributeDefinition(角色集合)
     *
     * @throws IllegalArgumentException if the passed object is not of a type supported by the
     *         <code>ObjectDefinitionSource</code> implementation
     */
	public ConfigAttributeDefinition getAttributes(Object object)
			throws IllegalArgumentException {
		if ((object == null) || !this.supports(object.getClass())) {
			throw new IllegalArgumentException("抱歉，目標對象不是FilterInvocation類型");
		}

		// 抽取出待請求的URL
		String url = ((FilterInvocation) object).getRequestUrl();
//		System.out.println("待請示的URL: " + url);
		
		// 獲取所有RdbmsEntryHolder列表(url與角色集合對應列表)
		List list = this.getRdbmsEntryHolderList();
		if (list == null || list.size() == 0)
			return null;

		// 去除url參數
		int firstQuestionMarkIndex = url.indexOf("?");
		if (firstQuestionMarkIndex != -1) {
			url = url.substring(0, firstQuestionMarkIndex);
		}

		Iterator iter = list.iterator();
		// 判斷是否有權限進入目前url，有則回傳ConfigAttributeDefinition(角色集合)
		while (iter.hasNext()) {
			RdbmsEntryHolder entryHolder = (RdbmsEntryHolder) iter.next();
			// 判斷目前欲進入的url是否符合entryHolder.getUrl()模式，即判斷是否有權限進入目前url
			// 如url="/secure/index.jsp"，entryHolder.getUrl()="/secure/**"，則有權限進入
			
			boolean matched = pathMatcher.match(entryHolder.getUrl(), url);
			//System.out.println("entryHolder url: " + entryHolder.getUrl() + " + | url:" + url + " | matched:" + matched);
//			if (logger.isDebugEnabled()) {
//				System.out.println("比對到如下URL： '" + url + "；模式為 "
//						+ entryHolder.getUrl() + "；比對是否相符：" + matched);
//			}

			// 如果在用戶所有被授權的URL中能找到相符的，則回傳ConfigAttributeDefinition(角色集合)
			if (matched) {
				return entryHolder.getCad();
			}
		}
		return null;
		/*
        if(url.indexOf("secure/system") < 0 && url.indexOf("scbHc") < 0 && url.indexOf("/report/") < 0)
		   return null;
        else
           throw new IllegalArgumentException("抱歉，目標對象不是許可的URL");
        */
	}

	/**
	 * 實現介面方法
	 * 
     * If available, all of the <code>ConfigAttributeDefinition</code>s defined by the implementing class.<P>This
     * is used by the {@link AbstractSecurityInterceptor} to perform startup time validation of each
     * <code>ConfigAttribute</code> configured against it.</p>
     *
     * @return an iterator over all the <code>ConfigAttributeDefinition</code>s or <code>null</code> if unsupported
     * @返回 ConfigAttributeDefinition迭代集合(Iterator)
     */
	public Iterator getConfigAttributeDefinitions() {
        Set set = new HashSet();
        Iterator iter = this.getRdbmsEntryHolderList().iterator();

        while (iter.hasNext()) {
        	RdbmsEntryHolder entryHolder = (RdbmsEntryHolder) iter.next();
            set.add(entryHolder.getCad());
        }

        return set.iterator();
	}

	/**
	 * 實現介面方法, 檢驗傳入的安全物件是否是與FilterInvocation類相同類型, 或是它的子類
	 * getAttributes(Object object)方法會調用這個方法
	 * 保證String url = ((FilterInvocation) object).getRequestUrl();的正確性
	 * 
     * Indicates whether the <code>ObjectDefinitionSource</code> implementation is able to provide
     * <code>ConfigAttributeDefinition</code>s for the indicated secure object type.
     *
     * @param clazz the class that is being queried
     *
     * @return true if the implementation can process the indicated class
     */
	public boolean supports(Class clazz) {
		if (FilterInvocation.class.isAssignableFrom(clazz)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 覆寫JdbcDaoSupport的方法, 用於將資料源傳入RdbmsSecuredUrlDefinition中
	 * JdbcDaoSupport實現了InitializingBean介面, 該介面中的afterPropertiesSet()方法
	 * 用於在所有spring bean屬性設置完畢後做一些初始化操作, BeanFactory會負責調用它
	 * 而在JdbcDaoSupport的實現中, afterPropertiesSet()方法調用了initDao()方法, 故我們
	 * 借此做一些初始化操作. 
	 * 在此用於將資料源傳入RdbmsSecuredUrlDefinition中
	 */
	protected void initDao() throws Exception {
//		System.out.println("第一個執行的方法: initDao()");
		this.rolesUrlMapping = 
				new RolesUrlMapping(this.getDataSource()); // 傳入Data source，Data source由Spring注入
		if (this.webresdbCache == null)
			throw new IllegalArgumentException("必須為RdbmsFilterInvocationDefinitionSource配置一EhCache cache");
	}

	/**
	 * 獲取所有RdbmsEntryHolder列表(url與角色集合對應列表)
	 * 
	 * @return
	 */
	private List getRdbmsEntryHolderList(){
		List list = null;
		//Element element = this.webresdbCache.get("webres");  fubon was7ehcache有問題，把cache關掉
		Element element = null;
		if(this.cacheActive)
			element = this.webresdbCache.get("webres");
		
		if (element != null){ // 如果緩存中存在RdbmsEntryHolder列表, 則直接獲取返回
			list = (List) element.getValue();
		} else { // 如果緩存中不存在RdbmsEntryHolder列表, 則重新查詢, 並放到緩存中
			list = this.rolesUrlMapping.execute();
			Element elem = new Element("webres", list);
			this.webresdbCache.put(elem);  
			this.cacheActive = true;
		}
		//list = this.rdbmsInvocationDefinition.execute();
		return list;
	}
	
	/**
	 * 用於Spring注入
	 * 
	 * @param webresdbCache
	 */
	public void setWebresdbCache(Ehcache webresdbCache) {
		this.webresdbCache = webresdbCache;
	}
	
	//Inner class for roles url mapping query
	protected class RolesUrlMapping extends MappingSqlQuery{

//		protected static final Log logger = LogFactory.getLog(RdbmsSecuredUrlDefinition.class);
		
	    protected RolesUrlMapping(DataSource ds) {
	        super(ds, rolesUrlMappingQuery);
//	        System.out.println("進入RdbmsInvocationDefinition建構子.........");
	        compile();
	    }

	    /**
	     * convert each row of the ResultSet into an object of the result type.
	     */
	    protected Object mapRow(ResultSet rs, int rownum)
	        throws SQLException {
//	    	System.out.println("撈url role mapping中的資料.........");
	    	
	    	RdbmsEntryHolder rsh = new RdbmsEntryHolder();
	    	// URL固定放第1欄，由SQL決定
	    	rsh.setUrl(rs.getString(1).trim());
	    	
	        ConfigAttributeDefinition cad = new ConfigAttributeDefinition();
	        // Roles固定放第2欄，由SQL決定
	        String rolesStr = rs.getString(2).trim();
	        // commaDelimitedListToStringArray:Convert a CSV list into an array of Strings
	        // 以逗號分割字串
	        String[] tokens = 
	        		StringUtils.commaDelimitedListToStringArray(rolesStr); // 角色陣列
	        // 建構角色集合
	        for(int i = 0; i < tokens.length;++i)
	        	cad.addConfigAttribute(new SecurityConfig(tokens[i]));
	        
	        // 配置角色集合
	        rsh.setCad(cad);
	    	
	        return rsh;
	    }

	}
	
	
}

