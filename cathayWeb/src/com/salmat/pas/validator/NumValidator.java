package com.salmat.pas.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class NumValidator implements Validator {
	public void validate(FacesContext context, UIComponent component, Object obj)
			throws ValidatorException {
		String text = null;
		if(obj == null)
			text = "";
		else
		    text = obj + "";
		if(text.equals("")) return;
		if (!CommonDataValidator.isNumber(text, false)) {
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "請輸入不含空白的數字", "*請輸入不含空白的數字");
			throw new ValidatorException(message);
		}
	}
}