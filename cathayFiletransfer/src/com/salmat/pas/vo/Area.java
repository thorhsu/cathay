package com.salmat.pas.vo;

import java.io.Serializable;
import javax.persistence.*;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@SuppressWarnings("serial")
@Entity
@Table(name = "area")
@NamedQueries({
	@NamedQuery(name="Area.findByPk", query="from Area where areaId = ?"),
	@NamedQuery(name="Area.findBySubAreaId", query="from Area where subAreaId = ? and independent = ? order by areaId"),
	@NamedQuery(name="Area.findHaveAddress", query="from Area where ((address is not null and address <> '') or (serviceCenter is not null and serviceCenter <> '')) order by areaId asc, serviceCenter desc"),
	@NamedQuery(name="Area.findServiceCenter", query="from Area where (areaId in ( select distinct serviceCenter from Area ) or serviceCenter is null or serviceCenter = '') and address is not null and address <> ''"),
	@NamedQuery(name="Area.updateAllCenter", query="update Area set serviceCenter = ?, serviceCenterNm = ? where subAreaId = ?" ),
	@NamedQuery(name="Area.findHaveAddressAndAudit", query="from Area where address is not null and address <> '' and areaName like '%審查%' order by areaId")	
})
public class Area implements Serializable {	
	@Id
	private String areaId;
	private String subAreaId;
	private String areaName;
	private String zipCode;
	private String address;
	private String tel;
	private String serviceCenter;
	private String serviceCenterNm;
	private Boolean independent;		
	
	public Area() {}


	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}


	public String getAreaId() {
		return areaId;
	}


	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}


	public String getSubAreaId() {
		return subAreaId;
	}


	public void setSubAreaId(String subAreaId) {
		this.subAreaId = subAreaId;
	}


	public String getAreaName() {
		return areaName;
	}


	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}


	public String getZipCode() {
		return zipCode;
	}


	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getTel() {
		return tel;
	}


	public void setTel(String tel) {
		this.tel = tel;
	}


	public Boolean getIndependent() {
		return independent;
	}


	public void setIndependent(Boolean independent) {
		this.independent = independent;
	}

	public String getServiceCenter() {
		return serviceCenter;
	}


	public void setServiceCenter(String serviceCenter) {
		this.serviceCenter = serviceCenter;
	}


	public String getServiceCenterNm() {
		return serviceCenterNm;
	}


	public void setServiceCenterNm(String serviceCenterNm) {
		this.serviceCenterNm = serviceCenterNm;
	}
}
