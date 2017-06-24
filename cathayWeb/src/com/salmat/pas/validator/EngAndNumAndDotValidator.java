package com.salmat.pas.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class EngAndNumAndDotValidator implements Validator {
	public void validate(FacesContext context, UIComponent component, Object obj)
			throws ValidatorException {
		String text = (String) obj;
		if(text.equals("")) return;
		if (!CommonDataValidator.isEngAndNumAndDot(text)) {
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "請輸入英數字與.", "*請輸入英數字與.");
			throw new ValidatorException(message);
		}
	}
}