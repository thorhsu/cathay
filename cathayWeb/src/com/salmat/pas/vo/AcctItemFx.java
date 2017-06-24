package com.salmat.pas.vo;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "acct_item_fx")
@NamedQueries({
	@NamedQuery(name="AcctItemFx.findByCustNoAnd", query="from AcctItemFx where custNo = ?"),
	@NamedQuery(name="AcctItemFx.findCathayMaterial", query="from AcctItemFx where custNo = 'UD' and isMaterial = true")	
})
public class AcctItemFx implements java.io.Serializable {

	// Fields
	@Id
    @Column(name="id")
	private Integer id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="cost")
	private Double cost;
	
	@Column(name="acct_price")
	private Double acctPrice;	
	
	@Column(name="printNo")
	private String printNo;
	
	@Column(name="custNo")
	private String custNo;
	
	@Column(name="isMaterial")
	private boolean isMaterial;
	
	@Column(name="unitName")
	private String unitName;
	
	@Column(name="unitCode")
	private Integer unitCode;
		
	@Column(name="idf_charge_item")
	private Integer chargeItemNo;
	
	@Column(name="category_code")
	private Integer categoryCode;
	
	@Column(name="category_name")
	private String categoryName;
	
	@Column(name="color")
	private String color;
	
	@Column(name="pound")
	private Double pound;
	
	@Column(name="length")
	private Double length;
	
	@Column(name="width")
	private Double width;
	
	@Column(name="length_unit")
	private Integer lengthUnit;
	
	@Column(name="length_unitNm")
	private String lengthUnitNm;
	
	@Column(name="weight")
	private Double weight;
	
	@Column(name="weight_unit")
	private Integer weightUnit;
	
	@Column(name="weight_unitNm")
	private String weightUnitNm;
	
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}

	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getCost() {
		return cost;
	}
	public void setCost(Double cost) {
		this.cost = cost;
	}
	public Double getAcctPrice() {
		return acctPrice;
	}
	public void setAcctPrice(Double acctPrice) {
		this.acctPrice = acctPrice;
	}
	public String getPrintNo() {
		return printNo;
	}
	public void setPrintNo(String printNo) {
		this.printNo = printNo;
	}
	public String getCustNo() {
		return custNo;
	}
	public void setCustNo(String custNo) {
		this.custNo = custNo;
	}
	public boolean getIsMaterial() {
		return isMaterial;
	}
	public void setIsMaterial(boolean isMaterial) {
		this.isMaterial = isMaterial;
	}
	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	public Integer getUnitCode() {
		return unitCode;
	}
	public void setUnitCode(Integer unitCode) {
		this.unitCode = unitCode;
	}
	public Integer getCategoryCode() {
		return categoryCode;
	}
	public void setCategoryCode(Integer categoryCode) {
		this.categoryCode = categoryCode;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public Double getPound() {
		return pound;
	}
	public void setPound(Double pound) {
		this.pound = pound;
	}
	public Double getLength() {
		return length;
	}
	public void setLength(Double length) {
		this.length = length;
	}
	public Double getWidth() {
		return width;
	}
	public void setWidth(Double width) {
		this.width = width;
	}
	public Integer getLengthUnit() {
		return lengthUnit;
	}
	public void setLengthUnit(Integer lengthUnit) {
		this.lengthUnit = lengthUnit;
	}
	public String getLengthUnitNm() {
		return lengthUnitNm;
	}
	public void setLengthUnitNm(String lengthUnitNm) {
		this.lengthUnitNm = lengthUnitNm;
	}
	public Integer getWeightUnit() {
		return weightUnit;
	}
	public void setWeightUnit(Integer weightUnit) {
		this.weightUnit = weightUnit;
	}
	public String getWeightUnitNm() {
		return weightUnitNm;
	}
	public void setWeightUnitNm(String weightUnitNm) {
		this.weightUnitNm = weightUnitNm;
	}
	public Integer getChargeItemNo() {
		return chargeItemNo;
	}
	public void setChargeItemNo(Integer chargeItemNo) {
		this.chargeItemNo = chargeItemNo;
	}		
		
}