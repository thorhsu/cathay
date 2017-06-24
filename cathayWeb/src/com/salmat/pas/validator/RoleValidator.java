package com.salmat.pas.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;

public class RoleValidator implements Validator {
	public void validate(FacesContext context, UIComponent component, Object obj)
			throws ValidatorException {
		String text = (String) obj;
		if(!text.startsWith("ROLE_")){
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "必須以ROLE_開頭", "*必須以ROLE_開頭");
			throw new ValidatorException(message);
		}
		text = StringUtils.remove(text, "_");
		
		if (!CommonDataValidator.isEngAndNum(text, true)) {
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "請輸入英數字與底線", "*請輸入英數字與底線");
			throw new ValidatorException(message);
		}
	}
}