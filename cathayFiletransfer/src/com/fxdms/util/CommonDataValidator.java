package com.fxdms.util;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.fxdms.util.Tools;

public class CommonDataValidator {
	
	/**
	 * Verify that the given string is a valid email address. "Validity" in this
	 * context only means that the address conforms to the correct syntax (not
	 * if the address actually exists).
	 * 
	 * @param email The email address to verify.
	 * @return a boolean indicating whether the email address is correctly
	 *         formatted.
	 */
	public static boolean isEmail(String email) {
		if (email == null)
			return false;
		if (email.indexOf('@') < 1)
			return false;
		try {
			new InternetAddress(email);
			return true;
		} catch (AddressException e) {
			return false;
		}
	}
	
	/**
	 * 檢查是否為整數
	 * @param value
	 * @param isPositive 是否為正整數
	 * @return
	 */
	public static boolean isInteger(String value, boolean isPositive) {
		try {
			if(isPositive) {
				if(Integer.parseInt(value) < 0) return false;
				else return true;
			}
		} catch (NumberFormatException e)  {
			return false;
		}
		return true;
	}
	
	/**
	 * 檢查是否為可轉換的數字
	 * @param value
	 * @return
	 */
	public static boolean isDouble(String value) {
		try {
			Double.parseDouble(value);
		} catch (NumberFormatException e)  {
			return false;
		}
		return true;
	}
	
	/**
	 * 檢查是否為0~9數字
	 * @param value
	 * @param isSpaceAllow
	 * @return
	 */
	public static boolean isNumber(String value, boolean isSpaceAllow) {
		if(isSpaceAllow) {
			if (!value.matches("[0-9 ]+"))
				return false;
		} else if (!value.matches("[0-9]+"))
			return false;
		return true;
	}
	
	/**
	 * 判斷是否全為中文字
	 * @param value
	 * @param isSpaceAllow 是否允許半形空白
	 * @return
	 */
	public static boolean isChinese(String value, boolean isSpaceAllow) { 
		if(isSpaceAllow) {
			if (!value.matches("[\u4e00-\u9fa5 ]+"))
				return false;
		} else if (!value.matches("[\u4e00-\u9fa5]+"))
			return false;
		return true;
	}
	
	/**
	 * 判斷是否為英數字
	 * @param value
	 * @param isSpaceAllow 是否允許半形空白
	 * @return
	 */
	public static boolean isEnglish(String value, boolean isSpaceAllow) {
		if(isSpaceAllow) {
			if (!value.matches("[a-zA-Z ]+"))
				return false;
		} else if (!value.matches("[a-zA-Z]+"))
			return false;
		return true;
	}
	
	/**
	 * 判斷是否為英數字
	 * @param value
	 * @param isSpaceAllow 是否允許半形空白
	 * @return
	 */
	public static boolean isEngAndNum(String value, boolean isSpaceAllow) {
		if(isSpaceAllow) {
			if (!value.matches("[a-zA-Z0-9 ]+"))
				return false;
		} else if (!value.matches("[a-zA-Z0-9]+"))
			return false;
		return true;
	}
	
	/**
	 * 判斷是否為英數字，允許部分標點符號
	 * @param value
	 * @param isSpaceAllow 是否允許半形空白
	 * @return
	 */
	public static boolean isEngAndNumAndPun(String value, boolean isSpaceAllow) {
		if(isSpaceAllow) {
			if (!value.matches("[,.:_()&@~\\-a-zA-Z0-9 ]+"))
				return false;
		} else if (!value.matches("[,.:_()&@~\\-a-zA-Z0-9]+"))
			return false;
		return true;
	}
	
	/**
	 * 判斷是否為中英數字
	 * @param value
	 * @param isSpaceAllow 是否允許半形空白
	 * @return
	 */
	public static boolean isChineseAndEngAndNum(String value, boolean isSpaceAllow) {
		if(isSpaceAllow) {
			if (!value.matches("[a-zA-Z0-9\u4e00-\u9fa5 ]+"))
				return false;
		} else if (!value.matches("[a-zA-Z0-9\u4e00-\u9fa5]+"))
			return false;
		return true;
	}
	
	/**
	 * 判斷是否為中英數字，允許部分標點符號
	 * @param value
	 * @param isSpaceAllow 是否允許半形空白
	 * @return
	 */
	public static boolean isChineseAndEngAndNumAndPun(String value, boolean isSpaceAllow) {
		if(isSpaceAllow) {
			if (!value.matches("[,.:_()&@~\\-a-zA-Z0-9\u4e00-\u9fa5\u2027\uff0c\u3001-\u3030\uff1a-\uff1f ]+"))
				return false;
		} else if (!value.matches("[,.:_()&@~\\-a-zA-Z0-9\u4e00-\u9fa5\u2027\uff0c\u3001-\u3030\uff1a-\uff1f]+"))
			return false;
		return true;
	}
	
	/**
	 * 判斷是否為中英文字
	 * @param value
	 * @param isSpaceAllow 是否允許半形空白
	 * @return
	 */
	public static boolean isChineseAndEnglish(String value, boolean isSpaceAllow) {
		if(isSpaceAllow) {
			if (!value.matches("[a-zA-Z\u4e00-\u9fa5 ]+"))
				return false;
		} else if (!value.matches("[a-zA-Z\u4e00-\u9fa5]+"))
			return false;
		return true;
	}
	
	/**
	 * 判斷是否為指定格式日期
	 * @param value
	 * @param pattern 
	 * @return
	 */
	public static boolean isDate(String value, String pattern) {
		if (value.trim().equals("") || Tools.getTimestampByString(value, pattern) == null)
			return false;
		return true;
	}
	
	/**
	 * 判斷是否為URL
	 * @param value
	 * @param isSpaceAllow 是否允許半形空白
	 * @return
	 */
	public static boolean isUrl(String value) {
		if (!value.matches("[,.:_/=?()&@~\\+\\-a-zA-Z0-9\u4e00-\u9fa5\u2027\uff0c\u3001-\u3030\uff1a-\uff1f ]+"))
			return false;
		return true;
	}
	
	/**
	 * 判斷是否為TextArea允許的字串，只擋特殊字元
	 * @param value
	 * @return
	 */
	public static boolean isTextArea(String value) {
		if(value.indexOf("<") > -1 || value.indexOf(">") > -1)
			return false;
		return true;
	}
	
	public static void main(String[] s) {
//		System.out.println(CommonDataValidator.isTextArea("http://www.g\r\noogle.c<om.tw/search?q=localhost+home+script+alert&dsf=asdf 我我【】"));
	}
}