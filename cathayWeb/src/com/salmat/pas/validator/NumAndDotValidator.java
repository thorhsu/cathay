package com.salmat.pas.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class NumAndDotValidator implements Validator {
	public void validate(FacesContext context, UIComponent component, Object obj)
			throws ValidatorException {
		String text = null;
		if(obj != null)
		   text = obj.toString();
		if(obj == null)
			return;
		if (!CommonDataValidator.isNumberAndDot(text)) {
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "請輸入數字", "*請輸入數字");
			throw new ValidatorException(message);
		}
	}
}