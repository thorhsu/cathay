package com.salmat.pas.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import com.salmat.pas.vo.Constants;
import com.salmat.util.Tools;

public class DateConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component,
			String str) throws ConverterException {
		return "";
	}

	/**
	 * 忽略日光節約時間，表示確定的日期
	 */
	public String getAsString(FacesContext context, UIComponent component,
			Object obj) throws ConverterException {
		String result = "";
		if(obj != null)
		   try {	
			   result = Tools.getDateFormat((java.util.Date)obj, Constants.DATE_PATTERN);
		   } catch (Exception e) {
			   // 轉換錯誤，簡單的丟出例外
			   throw new ConverterException();
		   }
		return result;
	}
}