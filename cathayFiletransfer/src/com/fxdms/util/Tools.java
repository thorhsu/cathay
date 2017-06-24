package com.fxdms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Utility class for sword project, providing all public and static function.
 * 
 * @author KennyLo
 */
public class Tools {
	public static final int MILLISECONDS_PER_DAY = 86400000;
	public static final int MILLISECONDS_PER_HOUR = 3600000;
	public static final int MILLISECONDS_PER_MINUTE = 60000;
	public static final int MILLISECONDS_PER_SECOND = 1000;
	
	
	public static Date getDateFormat(String sDate) {
		java.util.Date dt = null;
		String sSplitChar;
		if (sDate.indexOf("-") != -1)
			sSplitChar = "-";
		else if (sDate.indexOf("/") != -1)
			sSplitChar = "/";
		else
			sSplitChar = "";

		try {
			DateFormat df = new SimpleDateFormat("yyyy" + sSplitChar + "MM"
					+ sSplitChar + "dd");
			dt = df.parse(sDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dt;
	}

	/**
	 * Gets Date object by specified pattern
	 * 
	 * @param dateTime
	 *            datetime in string format
	 * @param pattern
	 *            like "yyyy/MM/dd" or "MM-dd-yyyy"
	 * @return Date object
	 */
	public static Date getDateByString (String dateTime, String pattern) {
		java.util.Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			date = sdf.parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * Gets Timestamp object by specified pattern
	 * 
	 * @param dateTime
	 *            datetime in string format
	 * @param pattern
	 *            like "yyyy/MM/dd" or "MM-dd-yyyy"
	 * @return Timestamp object
	 */
	public static java.sql.Timestamp getTimestampByString(String dateTime,
			String pattern)  {
		java.sql.Timestamp ts = null;
		Date dt = getDateByString(dateTime, pattern);
		if (dt != null)
			ts = new java.sql.Timestamp(dt.getTime());
		return ts;
	}

	/**
	 * Leads the given amount of zero into the specified String.
	 * 
	 * @param sNum
	 *            the original String
	 * @param nDigit
	 *            how many amount of zero to lead the String
	 * @return A String led by the given amount of zero
	 */
	public static String leadingZero(String sNum, int nDigit) {
		String strRET;

		if (sNum.trim().length() < nDigit) {
			strRET = sNum.trim();
			for (int i = 1; i <= nDigit - sNum.trim().length(); i++) {
				strRET = "0" + strRET;
			}
		} else {
			strRET = sNum.trim();
		}
		return strRET;
	}

	public static String leadingSpace(Object obj, int nDigit) {
		String s = obj.toString();
		int nObjLength = s.length();
		if (nObjLength < nDigit) {
			for (int i = 0; i < nDigit - nObjLength; i++) {
				s = " " + s;
			}
		}
		return s;
	}

	public static String addSpace(Object obj, int nDigit) {
		String s = obj.toString();
		int nObjLength = s.length();
		if (nObjLength < nDigit) {
			for (int i = 0; i < nDigit - nObjLength; i++) {
				s += " ";
			}
		}
		return s;
	}

	/**
	 * Transforms null into empty String.
	 * 
	 * @param obj
	 *            Any Object
	 * @return A String if the given Object is an instance of String<BR>
	 *         A Empty String if the given Object is null
	 */
	public static String nullToEmptyString(Object obj) {
		if (obj instanceof String || obj != null)
			return String.valueOf(obj).trim();
		else
			return "";
	}

	/**
	 * Converts date from/to "yyyyMMdd" to/from "yyyy-MM-dd"
	 * 
	 * @param obj
	 *            the date-time value to be formatted into a date-time string.
	 * @return A date in String format
	 */
	public static String convertDate(Object obj) {
		if (obj == null || obj.toString().equals(""))
			return "";
		String ret = null;
		SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMdd");
		try {
			if (obj instanceof java.util.Date) {
				ret = df2.format(obj);
			} else if (obj instanceof String) {
				java.util.Date dt = df2.parse((String) obj);
				ret = df1.format(dt);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Converts month from/to "yyyyMMdd" to/from "yyyy-MM-dd"
	 * 
	 * @param obj
	 *            the month value to be formatted into a month string.
	 * @return A month in String format
	 */
	public static String convertMonth(Object obj) {
		if (obj == null || obj.toString().equals(""))
			return "";
		String ret = null;
		SimpleDateFormat df1 = new SimpleDateFormat("yyyyMM");
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM");
		try {
			if (obj instanceof java.util.Date) {
				ret = df2.format(obj);
			} else if (obj instanceof String) {
				java.util.Date dt = df2.parse((String) obj);
				ret = df1.format(dt);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Splits the specified string from right by allocated regex
	 * 
	 * @param source
	 * @param sRegex
	 * @return result
	 */
	public static String splitFromRight(String source, String sRegex) {
		String[] s = source.split(sRegex);
		String sResult = s[0];

		for (int i = 1; i < s.length - 1; i++) {
			sResult += sRegex + s[i];
		}
		return sResult;
	}

	/**
	 * Gets current date time formatted by the given format.
	 * 
	 * @param sPattern
	 *            the pattern describing the date and time format.<br>
	 *            ex. "MM-dd-yyyy hh:mm:ss", "yyyyMMddhhmmss"
	 * @return the formatted time string
	 */
	public static String getCurrDate(String sPattern) {
		Date dt = new Date(System.currentTimeMillis());
		SimpleDateFormat df;
		if (sPattern == null)
			sPattern = "MM-dd-yyyy hh:mm:ss";
		try {
			df = new SimpleDateFormat(sPattern);
		} catch (IllegalArgumentException iae) {
			df = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
		}
		return df.format(dt);
	}

	public static String getDateFormat(Date dt, String sPattern) {
		SimpleDateFormat df;
		if (sPattern == null)
			sPattern = "MM-dd-yyyy hh:mm:ss";
		try {
			df = new SimpleDateFormat(sPattern);
		} catch (IllegalArgumentException iae) {
			df = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
		}
		return df.format(dt);
	}

	/**
	 * Adds the specified amount of days to the given date.
	 * 
	 * @param oldDate
	 *            the original date
	 * @param nAddDays
	 *            the amount of days to be added
	 * @return a new date
	 */
	public static Date addDay2Calendar(Date oldDate, int nAddDays) {
		Calendar c = Calendar.getInstance();
		c.setTime(oldDate);
		c.add(Calendar.DATE, nAddDays);
		return c.getTime();
	}

	public static Timestamp addDay2Calendar(Timestamp oldDate, int nAddDays) {
		Calendar c = Calendar.getInstance();
		c.setTime(oldDate);
		c.add(Calendar.DATE, nAddDays);
		return new Timestamp(c.getTimeInMillis());
	}
	
	/**
	 * Adds the specified amount of months to the given date month.
	 * 
	 * @param oldMonth
	 *            the original month
	 * @param nAddMonths
	 *            the amount of months to be added
	 * @return a new month
	 */
	public static Date addMonth2Calendar(Date oldMonth, int nAddMonths) {
		Calendar c = Calendar.getInstance();
		c.setTime(oldMonth);
		c.add(Calendar.MONTH, nAddMonths);
		return c.getTime();
	}

	/**
	 * Get String from the specified number, MaximumFractionDigits and grouping.
	 * 
	 * @param dbValue
	 *            the original number
	 * @param nFraction
	 *            the fraction to be set
	 * @param bGrouping
	 *            grouping is used or not
	 * @return the new formatted value in String format
	 */
	public static String numFormat(double dbValue, int nFraction,
			boolean bGrouping) {
		NumberFormat numberFormat = NumberFormat.getInstance();
		// numberFormat.setMinimumIntegerDigits(3);
		// numberFormat.setMinimumFractionDigits(2);
		numberFormat.setGroupingUsed(bGrouping);
		numberFormat.setMaximumFractionDigits(nFraction);
		return numberFormat.format(dbValue);
	}

	/**
	 * Get String from the specified number, MaximumFractionDigits and grouping.
	 * 
	 * @param dbValue
	 *            the original number
	 * @param nFraction
	 *            the fraction to be set
	 * @param bGrouping
	 *            grouping is used or not
	 * @return the new formatted value in String format
	 */
	public static String numFormat(double dbValue, int nMinFraction,
			int nMaxFraction, boolean bGrouping) {
		NumberFormat numberFormat = NumberFormat.getInstance();
		// numberFormat.setMinimumIntegerDigits(3);
		numberFormat.setMinimumFractionDigits(nMinFraction);
		numberFormat.setGroupingUsed(bGrouping);
		numberFormat.setMaximumFractionDigits(nMaxFraction);
		return numberFormat.format(dbValue);
	}

	public static String replaceAllString(String s, String regex,
			String replacement) {
		int n;
		while ((n = s.indexOf(regex)) != -1) {
			s = s.substring(0, n) + replacement
					+ s.substring(n + 1, s.length());
		}
		return s;
	}

	public static String decordPassword(String encodePassword, int rule) {
		char[] charArray = encodePassword.toCharArray();
		StringBuffer sb = new StringBuffer("");
		for (char c : charArray) {
			sb.append((char) (16 ^ (int) c));
		}
		return sb.toString();
	}

	public static String escape(String src) {
		int i;
		char j;
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length() * 6);

		for (i = 0; i < src.length(); i++) {

			j = src.charAt(i);

			if (Character.isDigit(j) || Character.isLowerCase(j)
					|| Character.isUpperCase(j))
				tmp.append(j);
			else if (j < 256) {
				tmp.append("%");
				if (j < 16)
					tmp.append("0");
				tmp.append(Integer.toString(j, 16));
			} else {
				tmp.append("%u");
				tmp.append(Integer.toString(j, 16));
			}
		}
		return tmp.toString();
	}

	public static String unescape(String src) {
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length());
		int lastPos = 0, pos = 0;
		char ch;
		while (lastPos < src.length()) {
			pos = src.indexOf("%", lastPos);
			if (pos == lastPos) {
				if (src.charAt(pos + 1) == 'u') {
					ch = (char) Integer.parseInt(src
							.substring(pos + 2, pos + 6), 16);
					tmp.append(ch);
					lastPos = pos + 6;
				} else {
					ch = (char) Integer.parseInt(src
							.substring(pos + 1, pos + 3), 16);
					tmp.append(ch);
					lastPos = pos + 3;
				}
			} else {
				if (pos == -1) {
					tmp.append(src.substring(lastPos));
					lastPos = src.length();
				} else {
					tmp.append(src.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return tmp.toString();
	}

	public static void refreshWeb(String rUrl) {
		try {
			(new java.net.URL(rUrl)).getContent();
		} catch (MalformedURLException me) {
			System.out.println(me.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	} 
	
	/**
	 * 日期相減已取得毫秒
	 * @param dt1
	 * @param dt2
	 * @return
	 */
	public static long dateSub(Date dtBegin, Date dtEnd) {
        long dateRange = 0;
        if(dtBegin == null || dtEnd == null) return dateRange;
        dateRange = dtEnd.getTime() - dtBegin.getTime();
        return dateRange;
	}
	
	public static void main(String[] test) throws Exception {

		// System.out.println(String.format("測試%s測試%sHello world%s","字一","\n","字三"));
		// java.net.URL url = new java.net.URL("http://192.168.11.2");
		// url.getContent();
		//		
		// url.openConnection();
		// System.out.println(url.getContent());
		// String s = "dsad dd";
		// System.out.println("-" + Tools.addSpace(s, 15) + "-");
		// String s = "26074620-21_SAP12";
		// System.out.println(s.indexOf("SAP"));
		//    	
		// System.out.println(s.substring(0,
		// s.indexOf(s.substring(s.indexOf("SAP"), s.length())) - 1));
		//    	
		// Calendar c = Calendar.getInstance();
		// c.add(Calendar.DATE, 3);
		// System.out.println(c);
		// System.out.println(c.get(Calendar.DAY_OF_WEEK));
		// System.out.println("UB589890423A-WR6635B-FC4A_E".substring(26, 27));
		//    	
		// Random random = new Random();
		// System.out.println(random.nextInt(5));

        String testtest = "-)ak;lfj";
        System.out.println(testtest.replaceAll("-\\)", ")"));
		
	}
}