package com.salmat.pas.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.myfaces.custom.fileupload.UploadedFile;

/*
 * Thor新增，利用endWith()方法判斷副檔名合不合格式的簡單validator
 */
public class TxtFileNameValidator implements Validator {
	public void validate(FacesContext context, UIComponent component, Object obj)
	throws ValidatorException {
        UploadedFile upLoadFile = (UploadedFile)obj;
        String uploadFileName = upLoadFile.getName();
        String errMsg = "輸入的檔名必須以.xls結尾 ";
        FacesMessage message = new FacesMessage(
		        FacesMessage.SEVERITY_ERROR, errMsg, "*" + errMsg);
        if(uploadFileName != null && uploadFileName.length() > 4){
        	 String appendix = uploadFileName.substring(uploadFileName.length() - 4);
        	 if(!appendix.equalsIgnoreCase(".xls"))
        		 throw new ValidatorException(message);
        }else{
	        throw new ValidatorException(message);
        }
    }
}