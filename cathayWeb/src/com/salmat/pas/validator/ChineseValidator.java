package com.salmat.pas.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class ChineseValidator implements Validator {
	public void validate(FacesContext context, UIComponent component, Object obj)
			throws ValidatorException {
		String text = (String) obj;
		if(text.equals("")) return;
		if (!CommonDataValidator.isChinese(text, false)) {
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "請輸入不含空白的中文", "*請輸入不含空白的中文");
			throw new ValidatorException(message);
		}
	}
}