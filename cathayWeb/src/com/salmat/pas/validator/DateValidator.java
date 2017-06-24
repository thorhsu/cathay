package com.salmat.pas.validator;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.salmat.pas.vo.Constants;

public class DateValidator implements Validator {
	public void validate(FacesContext context, UIComponent component, Object obj)
			throws ValidatorException {
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_PATTERN);
		String text = null;
		if(obj instanceof String)
		    text = (String) obj;
		else if(obj instanceof Date)
			text = sdf.format((Date)obj);
		if(text.equals("")) return;
		if (!CommonDataValidator.isDate(text, Constants.DATE_PATTERN)) {
			String errMsg = "日期格式需為" + Constants.DATE_PATTERN;
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, errMsg, "*" + errMsg);
			throw new ValidatorException(message);
		}
	}
}