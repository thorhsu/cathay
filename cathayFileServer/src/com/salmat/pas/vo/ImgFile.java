package com.salmat.pas.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.itextpdf.text.Image;

@SuppressWarnings("serial")
@Entity
@Table(name = "imgFile")
@NamedQueries({
	@NamedQuery(name="ImgFile.deletByDate", query="delete from ImgFile where copyDate <= ?"),
	@NamedQuery(name="ImgFile.findByFileNm", query="from ImgFile where fileNm = ?"),
	@NamedQuery(name="ImgFile.findByImage", query="from ImgFile where image = true"),
	@NamedQuery(name="ImgFile.findByLaw", query="from ImgFile where law = true")
})
public class ImgFile implements Serializable {
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long imgId;
	private Boolean image;  //是否為評註檔 // 
	private Boolean law;  //是否為條款檔
	private Boolean reqPolicy; //要保書影像檔
	private String postProcessedPath; //處理後的影像檔路徑
	private Date fileDate; //檔案日期
	private Boolean copySuccess;  //從sip server copy成功
	private Date copyDate; //解壓後copy成功時間
	private Boolean newCopy; //新影像檔
	private Long length; //新影像檔
	private Boolean errorImage;  //不符合規定的tiff檔
	private Boolean exist; //是否存在
	private String path;  //原始目錄
	private String fileNm;  //檔案名稱
	private Boolean lock;  //是否被別的程式鎖定而無法更名
	private Integer dpiX;
	private Integer dpiY;
	private String width;
	private String height;

	private Date insertDate;
	private Date updateDate;
	
	
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "imgFiles")
	private Set<ApplyData> applyDatas;
	
	public Long getImgId() {
		return imgId;
	}
	public void setImgId(Long imgId) {
		this.imgId = imgId;
	}
	
	public Boolean getImage() {
		return image;
	}
	public void setImage(Boolean image) {
		this.image = image;
	}
	public Boolean getLaw() {
		return law;
	}
	public void setLaw(Boolean law) {
		this.law = law;
	}
	public Boolean getReqPolicy() {
		return reqPolicy;
	}
	public void setReqPolicy(Boolean reqPolicy) {
		this.reqPolicy = reqPolicy;
	}
	public String getPostProcessedPath() {
		return postProcessedPath;
	}
	public void setPostProcessedPath(String postProcessedPath) {
		this.postProcessedPath = postProcessedPath;
	}
	public Date getFileDate() {
		return fileDate;
	}
	public void setFileDate(Date fileDate) {
		this.fileDate = fileDate;
	}
	
	public Long getLength() {
		return length;
	}
	public void setLength(Long length) {
		this.length = length;
	}
	public Boolean getCopySuccess() {
		return copySuccess;
	}
	public void setCopySuccess(Boolean copySuccess) {
		this.copySuccess = copySuccess;
	}
	public Date getCopyDate() {
		return copyDate;
	}
	public void setCopyDate(Date copyDate) {
		this.copyDate = copyDate;
	}
	public Boolean getNewCopy() {
		return newCopy;
	}
	public void setNewCopy(Boolean newCopy) {
		this.newCopy = newCopy;
	}
	public Boolean getErrorImage() {
		return errorImage;
	}
	public void setErrorImage(Boolean errorImage) {
		this.errorImage = errorImage;
	}
	public Boolean getExist() {
		return exist;
	}
	public void setExist(Boolean exist) {
		this.exist = exist;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getFileNm() {
		return fileNm;
	}
	public void setFileNm(String fileNm) {
		this.fileNm = fileNm;
	}
	public Boolean getLock() {
		return lock;
	}
	public void setLock(Boolean lock) {
		this.lock = lock;
	}
	public Integer getDpiX() {
		return dpiX;
	}
	public void setDpiX(Integer dpiX) {
		this.dpiX = dpiX;
	}
	public Integer getDpiY() {
		return dpiY;
	}
	public void setDpiY(Integer dpiY) {
		this.dpiY = dpiY;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public Date getInsertDate() {
		return insertDate;
	}
	public void setInsertDate(Date insertDate) {
		this.insertDate = insertDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public Set<ApplyData> getApplyDatas() {
		return applyDatas;
	}
	public void setApplyDatas(Set<ApplyData> applyDatas) {
		this.applyDatas = applyDatas;
	}

}
