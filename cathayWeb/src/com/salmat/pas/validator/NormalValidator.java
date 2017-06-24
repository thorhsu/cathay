package com.salmat.pas.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class NormalValidator implements Validator {
	public void validate(FacesContext context, UIComponent component, Object obj)
			throws ValidatorException {
		String text = (String) obj;
		if(text.equals("")) return;
		if (!CommonDataValidator.isChineseAndEngAndNumAndPun(text, true)) {
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "此欄位不可輸入特殊符號", "*此欄位不可輸入特殊符號");
			throw new ValidatorException(message);
		}
	}
}