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
public class ImageFileNameValidator implements Validator {
	public void validate(FacesContext context, UIComponent component, Object obj)
	throws ValidatorException {
        UploadedFile upLoadFile = (UploadedFile)obj;
        String uploadFileName = upLoadFile.getName();
        if (!(uploadFileName.endsWith(".jpg") || uploadFileName.endsWith(".jpeg") 
        		|| uploadFileName.endsWith(".bmp") || uploadFileName.endsWith(".png") || uploadFileName.endsWith(".gif") 
        		|| uploadFileName.endsWith(".JPG") || uploadFileName.endsWith(".JPEG") 
        		|| uploadFileName.endsWith(".BMP") || uploadFileName.endsWith(".PNG") || uploadFileName.endsWith(".GIF"))) {
	        String errMsg = "輸入的檔名為不可接受之格式";
	        FacesMessage message = new FacesMessage(
			        FacesMessage.SEVERITY_ERROR, errMsg, "*" + errMsg);
	        throw new ValidatorException(message);
        }
    }
}