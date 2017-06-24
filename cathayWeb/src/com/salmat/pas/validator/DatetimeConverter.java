package com.salmat.pas.validator;

import java.sql.Timestamp;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import com.salmat.pas.vo.Constants;
import com.salmat.util.Tools;

public class DatetimeConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component,
			String str) throws ConverterException {
		return "";
	}

	/**
	 * 忽略日光節約時間，表示確定的日期與時間
	 */
	public String getAsString(FacesContext context, UIComponent component,
			Object obj) throws ConverterException {
		String result = "";
		try {	
			result = Tools.getDateFormat((Timestamp)obj, Constants.DATETIME_PATTERN);
		} catch (Exception e) {
			// 轉換錯誤，簡單的丟出例外
			throw new ConverterException();
		}
		return result;
	}
}