package com.salmat.pas.vo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;


@SuppressWarnings("serial")
@Entity
@Table(name = "applyData")
@NamedQueries({
	@NamedQuery(name="ApplyData.findByFileNm", query="from ApplyData where oldBatchName = ? order by id desc"),
	@NamedQuery(name="ApplyData.findByApplyNoAndPolicyNoAndCenter", query="from ApplyData where applyNo = ? and policyNos like ? and center = ? and receipt = ?"),
	@NamedQuery(name="ApplyData.findByPolicyNoAndNewBatchName", query="from ApplyData where policyNos like ? and newBatchName = ?"),
	@NamedQuery(name="ApplyData.findByApplyNoCenterCycleDate", query="from ApplyData where applyNo = ? and center = ? and cycleDate = ? and receipt = ?"),
	@NamedQuery(name="ApplyData.findByNewBatchName", query="from ApplyData where newBatchName = ? order by areaId"),
	@NamedQuery(name="ApplyData.updateNewBatchNo", query="update ApplyData set newBatchNo = ?, vipModifierId = ?, vipModifierName = ? , vipModifierTime = ?, updateDate = ? where newBatchName = ? "),
	@NamedQuery(name="ApplyData.distinctResult", query="select distinct verifyResult from ApplyData where verifyResult is not null and verifyResult <> ''"),	
	@NamedQuery(name="ApplyData.deletByDate", query="delete from ApplyData where insertDate < ?")
})
public class ApplyData implements Serializable {
	
	@Transient	
	private static Map<String, String> sourceMap = new HashMap<String, String>();
    @Transient
    private static Map<String, String> packMap = new HashMap<String, String>();
    @Transient
    private static Map<String, String> centerMap = new HashMap<String, String>();
    @Transient
    private static Map<String, String> policyStatusMap = new HashMap<String, String>();
    static{
    	/*
    	sourceMap.put("NORM", "新契約─有簽收回條");
    	sourceMap.put("REPT", "新契約補單（補單）─有簽收回條");
    	sourceMap.put("CONV", "契約轉換（契轉）");
    	sourceMap.put("REIS", "REIS保全補發（保補）");
    	*/
    	sourceMap.put("NORM", "新契約");
    	sourceMap.put("REPT", "補單");
    	sourceMap.put("CONV", "契轉");
    	sourceMap.put("REIS", "保補");
    	
    	centerMap.put("00", "測試");
    	centerMap.put("01", "北一");
    	centerMap.put("02", "台中");
    	centerMap.put("03", "高雄");
    	centerMap.put("04", "台南");
    	centerMap.put("05", "桃竹");
    	centerMap.put("06", "北二");
    	 
    	packMap.put("01", "服務中心(預設值)");
    	packMap.put("02", "(保經)保代業務件");
    	packMap.put("03", "(雙掛號)直效行銷件");
    	
    	policyStatusMap.put("00", "初始作業");
    	policyStatusMap.put("10", "難字檢查");
    	policyStatusMap.put("11", "難字異常");
    	policyStatusMap.put("12", "影像檢查");
    	policyStatusMap.put("13", "影像異常");
    	
    	policyStatusMap.put("14", "無影像");    	
    	policyStatusMap.put("15", "轉檔中");
    	policyStatusMap.put("16", "轉檔失敗");
    	policyStatusMap.put("17", "轉檔成功");
    	policyStatusMap.put("18", "轉檔前抽件");
    	policyStatusMap.put("20", "回傳中");
    	policyStatusMap.put("25", "待印中");
    	policyStatusMap.put("28", "免印製");
    	policyStatusMap.put("30", "列印中");
    	policyStatusMap.put("35", "膠裝中");
    	policyStatusMap.put("36", "膠裝完畢");
    	policyStatusMap.put("40", "驗單中");
    	policyStatusMap.put("41", "驗單失敗");
    	policyStatusMap.put("42", "驗單完成");
    	policyStatusMap.put("50", "配表中");
    	policyStatusMap.put("55", "配表完成");
    	policyStatusMap.put("60", "裝箱中");    	
    	policyStatusMap.put("61", "簽收單錯誤，保單設定退回");
    	policyStatusMap.put("65", "退件裝箱中");
    	policyStatusMap.put("95", "裝箱完成");
    	policyStatusMap.put("96", "退件裝箱完成");
    	policyStatusMap.put("97", "等待貨運");
    	policyStatusMap.put("98", "退件等待貨運");
    	
    	policyStatusMap.put("100", "已交寄");
    }
    
	
	@Id 
	private String oldBatchName; //原始檔名 +applyNo
	//info|9070924428|00|AA43860304|H226771031|NORM||HD41C60|展楊梅一Ｃ|1|05|10279.0000||
	// policyNo |reprint|applyNo   |insureId  |	
	
	private Date cycleDate; //cycleDate  V
	private Date processedDate;
	private String applyNo; //受理编號V
	private String policyNos; //保單號碼，以,,區分。例：,9888866835,  V
	private String insureId; //被保險人ID V
	/*
	 * 來源類別 NORM 新契約→有簽收回條
       REPT 新契約補單（補單）→有簽收回條
       CONV 契約轉換（契轉）
       REIS保全補發（保補）V
    */
	private String sourceCode;
	private String areaId; // 前三碼為單位；後四碼為轄區 V
	private String subAreaId;
	private String areaAddress;
	private String areaName; // V
	private String center;  // 行政中心：01 北一；02 台中；03 高雄；04台南；05 桃竹；06 北二 V
	private Integer reprint; //補印次數 V
	private Boolean merger; //合併列印 V
	private Boolean imageOk; //影像狀態
	private String fileNm;
	private Boolean docOk; //文字狀態
	private Boolean megerOK; //合併狀態
	private Boolean metaOk; //保單列印檔狀態，FXDMS收到AFP檔確認無誤後寫出LOG檔供更新
	private Boolean signOk; //簽收回條狀態 FXDMS收到AFP檔確認無誤後寫出LOG檔供更新
	private Date printDate; //印製日期  V
	private Integer totalPage; //保單頁數
	private Integer firstPage; //封面頁數
	private Boolean receipt; //是否為簽收回條
	private Integer a4Page; //A4頁數 
	private Integer dmPage; //dm頁數
	private Integer lastPage; //封底頁數
	private Integer s5Page;	 //特殊紙5頁數
	private Integer s6Page;  //特殊紙6頁數
	private Integer s7Page;  //特殊紙7頁數
	private Integer s8Page; //特殊紙8頁數
	private Integer afpBeginPage; //afp中的起始頁數
	private Integer afpEndPage; //afp中的結束頁數
	private String convertId; //轉檔廠商
	private Boolean vip;  //VIP優先 V
	private Boolean substract; //抽件動作 V 
	private String action2; //保留動作2
	private String action3; //保留動作3
	private String action4; //保留動作4
	
	private String oldBatchNo;  //原始批號 V
	private Integer oldSerialNo;  //原始列印順序 (檔案名稱排序) V
	private String newBatchName; //新批號檔名(AFP的主檔名)
	private Long newBatchNo; //新批號，可依VIP設定調整順序
	private Integer newSerialNo;//新列印順序，這是pack裡的順序，換一個pack時從0重新編起 
	private Date presTime; //pres轉檔時間
	private Date printTime; //列印時間
	private Date bindTime;  //膠裝時間
	private Date verifyTime; //驗單時間
	private Date packTime; //裝箱時間
	private Date deliverTime; //交寄時間
	private String policyPDF; //保單PDF檔名加路徑
	private String singPDF; //簽收PDF檔名加路徑
	private String exceptionStatus; 
	private String policyStatus; //保單狀態
	private String verifyResult; //驗單結果
	private String nonExistImgs; //不存在的影像檔，以逗號隔開
	private String productType; //保單環境 batch online sample
	private String recName; // 收件人姓名  要保人或理專姓名
	private String zip;// 郵遞區號		
	private String address; //地址
	private String channelID;//通路代碼
	private String channelName; //通路名稱
	private String deliverType; //保單交寄方式 S：直接寄給客戶 P：寄給業務
	private String uniqueNo; //右上角編號
	/*
	 * 保單交寄方式為S，且轄區代號為TQ或TC或TN開頭時，設定為：單掛號
   	保單交寄方式為P，且轄區代號為CU5，設定為單掛
	      其餘皆設定為雙掛號

	 */
	private String mailType; //郵寄方式 
	
	/*
	 * 01：服務中心(預設值)
       02：(保經)保代業務件
       03：(雙掛號)直效行銷件
                 說明：若於保單收件table查得到即為北二區的件時，當保單交寄方式DeliverType
                 為S時，設定為03：直效行銷件
                 為P時，設定為02保代業務件
	 */	
	private String packType; //打包環境 
	private Date insertDate;
	private Date updateDate;
	private String vipModifierId;
	private String vipModifierName;
	private Date vipModifierTime;
	
	private String substractModifiderId;
	private String substractModifiderName;
	private Date substractModifiderTime;
	private String processedStaff;
	private Date processedTime; 
	private String packId;
	private String reSendStaff;
	private Date reSendTime;
	private String serviceCenter;
	private String serviceCenterNm;
	private Double weight;
	
	@ManyToOne()
	@JoinColumn(name = "newBatchName", insertable = false, updatable = false)
	private AfpFile afpFile;
	
	@ManyToOne()
	@JoinColumn(name = "packId", insertable = false, updatable = false)
	private PackStatus packSatus;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "imgMetaTable", joinColumns = { 
			@JoinColumn(name = "oldBatchName", nullable = false, updatable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "imgId", 
					nullable = false, updatable = false) })
	private Set<ImgFile> imgFiles;
	
	@Transient
	private ApplyData receiptData;

	public Date getCycleDate() {
		return cycleDate;
	}

	public void setCycleDate(Date cycleDate) {
		this.cycleDate = cycleDate;
	}	

	public Date getProcessedDate() {
		return processedDate;
	}

	public void setProcessedDate(Date processedDate) {
		this.processedDate = processedDate;
	}

	public String getApplyNo() {
		return applyNo;
	}

	public void setApplyNo(String applyNo) {
		this.applyNo = applyNo;
	}

	public String getPolicyNos() {
		return policyNos;
	}

	public void setPolicyNos(String policyNos) {
		this.policyNos = policyNos;
	}
	
	public Set<String> getPolicyNoSet(){
		if(policyNos == null)
			return new HashSet<String>();
		else{
			String [] policyNoArr = policyNos.split(",");
			
			if(policyNoArr == null || policyNoArr.length == 0){
				return new HashSet<String>();
			}else{
				LinkedHashSet<String> set = new LinkedHashSet<String>();
				for(String policyNo : policyNoArr){
					if(policyNo != null && !policyNo.trim().equals(""))
					set.add(policyNo.trim());
				}
			    return set;
			}
		}
	}
	public void setPolicyNoSet(Set<String> policyList) {
		if(policyList == null || policyList.size() == 0)
			this.setPolicyNos(null);
		else{
		   String policyNos = ",";
		   for(String policyNo: policyList){
			   if(policyNo != null && !policyNo.trim().equals(""))
			      policyNos += ( policyNo + ",");
		   }
		   this.setPolicyNos(policyNos);
		}		
	}
	

	public String getInsureId() {
		return insureId;
	}

	public void setInsureId(String insureId) {
		this.insureId = insureId;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getCenter() {
		return center;
	}
	
	public String getCenterName() {
		return centerMap.get(center);
	}

	public void setCenter(String center) {
		this.center = center;
	}

	public Integer getReprint() {
		return reprint;
	}

	public void setReprint(Integer reprint) {
		this.reprint = reprint;
	}

	public Boolean getMerger() {
		return merger;
	}

	public void setMerger(Boolean merger) {
		this.merger = merger;
	}

	public Boolean getImageOk() {
		return imageOk;
	}

	public void setImageOk(Boolean imageOk) {
		this.imageOk = imageOk;
	}

	public Boolean getDocOk() {
		return docOk;
	}

	public void setDocOk(Boolean docOk) {
		this.docOk = docOk;
	}

	public Boolean getMegerOK() {
		return megerOK;
	}

	public void setMegerOK(Boolean megerOK) {
		this.megerOK = megerOK;
	}

	public Boolean getMetaOk() {
		return metaOk;
	}

	public void setMetaOk(Boolean metaOk) {
		this.metaOk = metaOk;
	}

	public Boolean getSignOk() {
		return signOk;
	}

	public void setSignOk(Boolean signOk) {
		this.signOk = signOk;
	}

	public Date getPrintDate() {
		return printDate;
	}

	public void setPrintDate(Date printDate) {
		this.printDate = printDate;
	}

	public Integer getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	public Integer getFirstPage() {
		return firstPage;
	}

	public void setFirstPage(Integer firstPage) {
		this.firstPage = firstPage;
	}

	public Boolean getReceipt() {
		return receipt;
	}

	public void setReceipt(Boolean receipt) {
		this.receipt = receipt;
	}

	public Integer getA4Page() {
		return a4Page;
	}

	public void setA4Page(Integer a4Page) {
		this.a4Page = a4Page;
	}

	public Integer getDmPage() {
		return dmPage;
	}

	public void setDmPage(Integer dmPage) {
		this.dmPage = dmPage;
	}

	public Integer getLastPage() {
		return lastPage;
	}

	public void setLastPage(Integer lastPage) {
		this.lastPage = lastPage;
	}

	public Integer getS5Page() {
		return s5Page;
	}

	public void setS5Page(Integer s5Page) {
		this.s5Page = s5Page;
	}

	public Integer getS6Page() {
		return s6Page;
	}

	public void setS6Page(Integer s6Page) {
		this.s6Page = s6Page;
	}

	public Integer getS7Page() {
		return s7Page;
	}

	public void setS7Page(Integer s7Page) {
		this.s7Page = s7Page;
	}

	public Integer getS8Page() {
		return s8Page;
	}

	public void setS8Page(Integer s8Page) {
		this.s8Page = s8Page;
	}
	
	public Integer getAfpBeginPage() {
		return afpBeginPage;
	}

	public void setAfpBeginPage(Integer afpBeginPage) {
		this.afpBeginPage = afpBeginPage;
	}

	public Integer getAfpEndPage() {
		return afpEndPage;
	}

	public void setAfpEndPage(Integer afpEndPage) {
		this.afpEndPage = afpEndPage;
	}

	public String getConvertId() {
		return convertId;
	}

	public void setConvertId(String convertId) {
		this.convertId = convertId;
	}

	public Boolean getVip() {
		return vip;
	}

	public void setVip(Boolean vip) {
		this.vip = vip;
	}

	public Boolean getSubstract() {
		return substract;
	}

	public void setSubstract(Boolean substract) {
		this.substract = substract;
	}

	public String getAction2() {
		return action2;
	}

	public void setAction2(String action2) {
		this.action2 = action2;
	}

	public String getAction3() {
		return action3;
	}

	public void setAction3(String action3) {
		this.action3 = action3;
	}

	public String getAction4() {
		return action4;
	}

	public void setAction4(String action4) {
		this.action4 = action4;
	}

	public String getOldBatchName() {
		return oldBatchName;
	}

	public void setOldBatchName(String oldBatchName) {
		this.oldBatchName = oldBatchName;
	}

	public String getOldBatchNo() {
		return oldBatchNo;
	}

	public void setOldBatchNo(String oldBatchNo) {
		this.oldBatchNo = oldBatchNo;
	}

	
	public String getNewBatchName() {
		return newBatchName;
	}

	public void setNewBatchName(String newBatchName) {
		this.newBatchName = newBatchName;
	}

	public Long getNewBatchNo() {
		return newBatchNo;
	}

	public void setNewBatchNo(Long newBatchNo) {
		this.newBatchNo = newBatchNo;
	}
	public Integer getOldSerialNo() {
		return oldSerialNo;
	}

	public void setOldSerialNo(Integer oldSerialNo) {
		this.oldSerialNo = oldSerialNo;
	}

	public Integer getNewSerialNo() {
		return newSerialNo;
	}

	public void setNewSerialNo(Integer newSerialNo) {
		this.newSerialNo = newSerialNo;
	}

	public Date getPresTime() {
		return presTime;
	}

	public void setPresTime(Date presTime) {
		this.presTime = presTime;
	}

	public Date getPrintTime() {
		return printTime;
	}

	public void setPrintTime(Date printTime) {
		this.printTime = printTime;
	}

	public Date getBindTime() {
		return bindTime;
	}

	public void setBindTime(Date bindTime) {
		this.bindTime = bindTime;
	}

	public Date getVerifyTime() {
		return verifyTime;
	}

	public void setVerifyTime(Date verifyTime) {
		this.verifyTime = verifyTime;
	}

	public Date getPackTime() {
		return packTime;
	}

	public void setPackTime(Date packTime) {
		this.packTime = packTime;
	}

	public Date getDeliverTime() {
		return deliverTime;
	}

	public void setDeliverTime(Date deliverTime) {
		this.deliverTime = deliverTime;
	}

	public String getPolicyPDF() {
		return policyPDF;
	}

	public void setPolicyPDF(String policyPDF) {
		this.policyPDF = policyPDF;
	}

	public String getSingPDF() {
		return singPDF;
	}

	public void setSingPDF(String singPDF) {
		this.singPDF = singPDF;
	}

	public String getPolicyStatus() {
		return policyStatus;
	}
	public String getPolicyStatusName() {
		return policyStatusMap.get(policyStatus);
	}

	public void setPolicyStatus(String policyStatus) {
		if("11".equals(policyStatus) || "13".equals(policyStatus) || "14".equals(policyStatus) || "16".equals(policyStatus) || "41".equals(policyStatus)  || "61".equals(policyStatus)){
    	    setExceptionStatus(policyStatus);	
    	}
		this.policyStatus = policyStatus;
	}
	public void setPolicyStatusUnBack(String policyStatus) {
		String currStatus = this.getPolicyStatus();
		try{
			if(currStatus == null){
				this.policyStatus = policyStatus;
			}else{
			    if(policyStatus != null && new Integer(policyStatus) > new Integer(currStatus)){
			    	this.policyStatus = policyStatus;
			    }	
			}				
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public String getVerifyResult() {
		return verifyResult;
	}

	public void setVerifyResult(String verifyResult) {
		this.verifyResult = verifyResult;
	}

	public String getNonExistImgs() {
		return nonExistImgs;
	}

	public void setNonExistImgs(String nonExistImgs) {
		this.nonExistImgs = nonExistImgs;
	}
	public List<String> getNonExistImgList() {
		if(nonExistImgs == null){
		   return null;
		}else{
			String [] imgArr = nonExistImgs.split(",");
			return Arrays.asList(imgArr);		   			
		}
	}

	public void setNonExistImgList(List<String> nonExistImgList) {
		if(nonExistImgList == null){
			this.setNonExistImgs(null);
		}else{
		   String nonExistImgs = "";
		   for(String img: nonExistImgList){
			   nonExistImgs += ("," + img + ",");
		   }
		   this.setNonExistImgs(nonExistImgs);
		}	
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

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getRecName() {
		return recName;
	}

	public void setRecName(String recName) {
		this.recName = recName;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getChannelID() {
		return channelID;
	}

	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getDeliverType() {
		return deliverType;
	}

	public void setDeliverType(String deliverType) {
		this.deliverType = deliverType;
	}

	public String getMailType() {
		return mailType;
	}

	public void setMailType(String mailType) {
		this.mailType = mailType;
	}

	public String getPackType() {
		return packType;
	}
	public String getPackTypeName() {
		return packMap.get(packType);
	}

	public void setPackType(String packType) {
		this.packType = packType;
	}

	public String getSourceCode() {
		return sourceCode;
	}
	public String getSourceName(){
		return sourceMap.get(sourceCode);
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public AfpFile getAfpFile() {
		return afpFile;
	}

	public void setAfpFile(AfpFile afpFile) {
		this.afpFile = afpFile;
	}

	public Set<ImgFile> getImgFiles() {
		return imgFiles;
	}

	public void setImgFiles(Set<ImgFile> imgFiles) {
		this.imgFiles = imgFiles;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
	public boolean equal(ApplyData applyData){
		if(applyData == null)
			return false;
		if(applyData.toString().equals(this.toString()))
			return true;
		else
			return false;
	}

	public static Map<String, String> getSourceMap() {
		return sourceMap;
	}

	public static void setSourceMap(Map<String, String> sourceMap) {
		ApplyData.sourceMap = sourceMap;
	}

	public static Map<String, String> getPackMap() {
		return packMap;
	}

	public static void setPackMap(Map<String, String> packMap) {
		ApplyData.packMap = packMap;
	}

	public static Map<String, String> getCenterMap() {
		return centerMap;
	}

	public static void setCenterMap(Map<String, String> centerMap) {
		ApplyData.centerMap = centerMap;
	}

	public static Map<String, String> getPolicyStatusMap() {
		return policyStatusMap;
	}

	public static void setPolicyStatusMap(Map<String, String> policyStatusMap) {
		ApplyData.policyStatusMap = policyStatusMap;
	}

	public String getVipModifierId() {
		return vipModifierId;
	}

	public void setVipModifierId(String vipModifierId) {
		this.vipModifierId = vipModifierId;
	}

	public String getVipModifierName() {
		return vipModifierName;
	}

	public void setVipModifierName(String vipModifierName) {
		this.vipModifierName = vipModifierName;
	}

	public String getSubstractModifiderId() {
		return substractModifiderId;
	}

	public void setSubstractModifiderId(String substractModifiderId) {
		this.substractModifiderId = substractModifiderId;
	}

	public String getSubstractModifiderName() {
		return substractModifiderName;
	}

	public void setSubstractModifiderName(String substractModifiderName) {
		this.substractModifiderName = substractModifiderName;
	}

	public Date getVipModifierTime() {
		return vipModifierTime;
	}

	public void setVipModifierTime(Date vipModifierTime) {
		this.vipModifierTime = vipModifierTime;
	}

	public Date getSubstractModifiderTime() {
		return substractModifiderTime;
	}

	public void setSubstractModifiderTime(Date substractModifiderTime) {
		this.substractModifiderTime = substractModifiderTime;
	}

	public String getUniqueNo() {
		return uniqueNo;
	}

	public void setUniqueNo(String uniqueNo) {
		this.uniqueNo = uniqueNo;
	}

	public ApplyData getReceiptData() {
		return receiptData;
	}

	public void setReceiptData(ApplyData receiptData) {
		this.receiptData = receiptData;
	}

	public String getPackId() {
		return packId;
	}

	public void setPackId(String packId) {
		this.packId = packId;
	}

	public PackStatus getPackSatus() {
		return packSatus;
	}

	public void setPackSatus(PackStatus packSatus) {
		this.packSatus = packSatus;
	}

	public String getSubAreaId() {
		return subAreaId;
	}

	public void setSubAreaId(String subAreaId) {
		this.subAreaId = subAreaId;
	}

	public String getAreaAddress() {
		return areaAddress;
	}

	public void setAreaAddress(String areaAddress) {
		this.areaAddress = areaAddress;
	}

	public String getExceptionStatus() {
		return exceptionStatus;
	}
	public String getExceptionStatusName() {
		return policyStatusMap.get(exceptionStatus);
	}

	public void setExceptionStatus(String exceptionStatus) {
		this.exceptionStatus = exceptionStatus;
	}

	public String getProcessedStaff() {
		return processedStaff;
	}

	public void setProcessedStaff(String processedStaff) {
		this.processedStaff = processedStaff;
	}

	public Date getProcessedTime() {
		return processedTime;
	}

	public String getFileNm() {
		return fileNm;
	}

	public void setFileNm(String fileNm) {
		this.fileNm = fileNm;
	}

	public void setProcessedTime(Date processedTime) {
		this.processedTime = processedTime;
	}

	public String getReSendStaff() {
		return reSendStaff;
	}

	public void setReSendStaff(String reSendStaff) {
		this.reSendStaff = reSendStaff;
	}

	public Date getReSendTime() {
		return reSendTime;
	}

	public void setReSendTime(Date reSendTime) {
		this.reSendTime = reSendTime;
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

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}
	

}
