package com.salmat.pas.beans;

import java.util.Date;

import com.salmat.pas.conf.Constant;


/*
 * Thor新增，本類別是用來做為jQGrid所需的local資料處理用
 */
public class PdfViewQueryJqGridBean {
	/*
	[{name: 'id', index: 'id', sorttype: "int",hidden: true},
     {name: 'policyDate', index: 'policyDate', sorttype: "date"},
     {name: 'sipName', index: 'sipName', width: 380},	                        
        {name: 'policyNo', index: 'policyNo' }, 
        {name: 'status', index: 'status', width: 50}, 
        {name: 'fileNm', index: 'fileNm'},],
    */
	private String oldBatchName; //原始檔名V
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
	private String areaName; // V
	private String center;  // 行政中心：01 北一；02 台中；03 高雄；04台南；05 桃竹；06 北二 V
	private Integer reprint; //補印次數 V
	private Boolean merger; //合併列印 V
	private Boolean imageOk; //影像狀態
	private Boolean docOk; //文字狀態
	private Boolean megerOK; //合併狀態
	private Boolean metaOk; //保單列印檔狀態，FXDMS收到AFP檔確認無誤後寫出LOG檔供更新
	private Boolean signOk; //簽收回條狀態 FXDMS收到AFP檔確認無誤後寫出LOG檔供更新
	private Date printDate; //印製日期  V
	private Integer totalPage; //保單頁數
	private Integer firstPage; //封面頁數
	private Integer a4Page; //A4頁數 
	private Integer dmPage; //dm頁數
	
	private Integer s5Page;	 //特殊紙5頁數
	private Integer s6Page;  //特殊紙6頁數
	private Integer s7Page;  //特殊紙7頁數
	private Integer s8Page; //特殊紙8頁數
	private Integer afpBeginPage; //afp中的起始頁數
	private Integer afpEndPage; //afp中的結束頁數
	private String convertId; //轉檔廠商
	private Boolean vip;  //VIP優先 V
	private Boolean receipt;
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
	
	
	public String getNmWithTag(){
		String url = "";		
		if(policyPDF != null && !"".equals(policyPDF)) 
		   url = "<div style='cursor: pointer;'>" + policyPDF +"</div>" ;		
		return url;
	}


	public String getOldBatchName() {
		return oldBatchName;
	}


	public void setOldBatchName(String oldBatchName) {
		this.oldBatchName = oldBatchName;
	}


	public Date getCycleDate() {
		return cycleDate;
	}

	public String getCycleDateStr(){
		if(cycleDate != null)
			return Constant.slashedyyyyMMdd.format(cycleDate);
		else 
			return "";
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
		if(policyNos != null && !policyNos.equals(""))
		    return policyNos.substring(1, policyNos.length() - 1);
		else
			return "";
	}


	public void setPolicyNos(String policyNos) {		
		this.policyNos = policyNos;
	}


	public String getInsureId() {
		return insureId;
	}


	public void setInsureId(String insureId) {
		this.insureId = insureId;
	}


	public String getSourceCode() {
		return sourceCode;
	}


	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
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

	public String getMergeStr(){
	     if(merger != null && merger)
	    	 return "V";
	     else
	    	 return "";
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
		if(totalPage != null)
		   return totalPage;
		else
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


	public String getVipStr(){
		if(getVip() != null && getVip())
			return "V";
		else
		    return "";
	}
	
	public Boolean getVip() {
		return vip;
	}


	public void setVip(Boolean vip) {
		this.vip = vip;
	}


	public Boolean getReceipt() {
		return receipt;
	}
	public String getReceiptStr() {
		if(receipt != null && receipt)
		   return "V";
		else
		   return "";
	}

	public void setReceipt(Boolean receipt) {
		this.receipt = receipt;
	}


	public Boolean getSubstract() {
		return substract;
	}
	public String getSubstractStr() {
		if(getSubstract() != null && getSubstract())
		   return "V";
		else
		   return "";
		
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


	public String getOldBatchNo() {
		return oldBatchNo;
	}


	public void setOldBatchNo(String oldBatchNo) {
		this.oldBatchNo = oldBatchNo;
	}


	public Integer getOldSerialNo() {
		return oldSerialNo;
	}


	public void setOldSerialNo(Integer oldSerialNo) {
		this.oldSerialNo = oldSerialNo;
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


	public void setPolicyStatus(String policyStatus) {
		this.policyStatus = policyStatus;
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


	public void setPackType(String packType) {
		this.packType = packType;
	}
	
		

}
