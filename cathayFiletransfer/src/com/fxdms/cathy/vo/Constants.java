package com.fxdms.cathy.vo;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class Constants {
	public static final String NEWS = "news";
	public static final String MONTH = "month";
	public static final String REGION_ALL = "ALL";
	public static final String FUBON_URL = "fubonUrl";
	public static final String DEFAULT_INSERT_USER = "System";
	public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";
	public static final String ROLE_USER = "ROLE_USER";
	public static final String ROLE_OLDUSER = "ROLE_OLDUSER";
	public static final String ROLE_SUPERVISOR = "ROLE_SUPERVISOR";
	public static final String DATE_PATTERN = "yyyy/MM/dd";
	public static final String DATETIME_PATTERN = "yyyy/MM/dd HH:mm:ss";
	public static final String MALE = "M";
	public static final String FEMALE = "F";
	public static final String YES = "Y";
	public static final String NO = "N";
	public static final String TRUE = "1";
	public static final String FALSE = "0";
	public static final String USER_STATUS_UNAUTHORIZED = "0";
	public static final String USER_STATUS_AUTHORIZED = "1";
	public static final String USER_STATUS_DISABLED = "9";
	public static final String USER_DATA_SEPARATION = "`";
	public static final String EPAPER_AUTHORIZED = "1";
	public static final String EPAPER_UNAUTHORIZED = "0";
	public static final String PT = "PT";
	public static final String FT = "FT";
	public static final Long ROOT_PAGE_ID = 0L;
	private static  String DBPWD;
	

	public String toString() {
//		return (new ReflectionToStringBuilder(this) {
//			protected boolean accept(Field f) {
//				return super.accept(f) && !f.getName().equals("userPassword");
//	        }
//	    }).toString();
		return ReflectionToStringBuilder.toString(this);
	}
		
	public static String getDBPWD() {
		return DBPWD;
	}

	public static void setDBPWD(String dBPWD) {
		DBPWD = dBPWD;
	}

}
