<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
    <script src="../objects/prog/js/plugins/jquery.browser.js"
		type="text/javascript"></script>    	
	<script type="text/javascript" src="../objects/prog/js/sbDate.js"></script>
	<script src="../objects/prog/js/lib/i18n/grid.locale-en.js"
		type="text/javascript"></script>	
	<script src="../objects/prog/js/lib/jquery.jqGrid.min.js"
		type="text/javascript"></script>
		
	<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
	var hyperLink = '';
	var fileName = "";
	var newBatchName = "";
    $(function(){    	
    	var lastsel;	    	
	    jQuery("#list4").jqGrid({
	            datatype: "local",
	            height: 690,
	            rowNum: 30,
	            colNames: ['cycle date', '上傳檔名', '來源類別', '受理編號','要保人', '被保險人ID','保單號碼','行政中心','轄區代碼','服務中心','目前狀態','簽收單', '頁數','補印','合併','VIP','抽件', '預覽pdf', '檔案名', 'newBatchName', '驗單結果'],
	            colModel: [{name: 'cycleDateStr', index: 'cycleDateStr', sorttype: "date", width: 80, align: "center"},
	                       {name: 'oldBatchName', index: 'oldBatchName', hidden: true,editable: true},	                        
	                       {name: 'sourceCode', index: 'sourceCode', width: 80, align: "center"},
	       	               {name: 'applyNo', index: 'applyNo', width: 80 , align: "center"}, 
	       	               {name: 'recName', index: 'recName', width: 70 , align: "center"},	       	            
	       	               {name: 'insureId', index: 'insureId', width: 100 , align: "center", hidden:true},
	       	               {name: 'policyNos', index: 'policyNos', width: 100 , align: "center"},
	       	               {name: 'center', index: 'center', width: 50 , align: "center"},
	       	               {name: 'areaId', index: 'areaId', width: 80 , align: "center"},
	       	               {name: 'areaName', index: 'areaName', width: 100 , align: "center"},	       	               
	       	               {name: 'policyStatus', index: 'policyStatus', width: 60, align: "center"},	       	            
	       	               {name: 'receiptStr', index: 'receiptStr', width: 50, align: "center"},
	       	               {name: 'totalPage', index: 'totalPage', width: 50, align: "right"},
	       	               {name: 'reprint', index: 'reprint', width: 30, align: "right"},
	       	               {name: 'mergeStr', index: 'mergeStr', width: 30, align: "center"},
	       	               {name: 'vipStr', index: 'vipStr', width: 30, align: "center", editable: true,edittype:"checkbox",editoptions: {value:"V:"}},
	       	               {name: 'substractStr', index: 'substractStr', width: 30, align: "center",editable: true,edittype:"checkbox",editoptions: {value:"V:"}},
	       	               {name: 'nmWithTag', index: 'nmWithTag', width: 150, align: "center"},
	       	               {name: 'policyPDF', index: 'policyPDF',hidden: true},
	       	               {name: 'newBatchName', index: 'newBatchName',hidden: true},
	       	               {name: 'verifyResult', index: 'verifyResult',hidden: true}],
	       	    onCellSelect: function(rowId, iCol){	
	       	    	
	       	    	  if(16 === iCol || 17 === iCol){
	       	    		if(rowId && rowId!==lastsel){
	       	 			    jQuery('#list4').jqGrid('restoreRow',lastsel);
	       	 		        jQuery("#list4").jqGrid('editRow', rowId + "");
	       	 		        newBatchName = jQuery("#list4").getRowData(rowId).newBatchName;
	       	 			    lastsel = rowId + "";
	       	 			    $("#editMode").show();
	       	 		     }
	       	    		  
	       	    	  }
   	                  if(18 === iCol){   	                	
                    	   getSelectedPdfId(rowId);
   	                  }
                },
                onSortCol: function(){
                	//排序 
                	 jQuery("#list4").jqGrid().setGridParam({datatype : 'json'});
				     var startDate = jQuery("#startDateHidden").val();
					 var endDate = jQuery("#endDateHidden").val();
					 var center = jQuery("#centerHidden").val();
					 var applyNo = jQuery("#applyNoHidden").val();
					 var policyNo = jQuery("#policyNoHidden").val();
					 var insureId = jQuery("#insureIdHidden").val();
					 var areaId = jQuery("#areaIdHidden").val();
					 var policyStatus = jQuery("#policyStatusHidden").val();
					 var recName = jQuery("#recNameHidden").val();
					 var sourceCode = jQuery("#sourceCodeHidden").val();
					 var receipt = jQuery("#receiptHidden").val();
					 var exception = jQuery("#exceptionHidden").val();
					 var groupInsure = jQuery("#groupInsureHidden").val();
					 
					 jQuery("#list4").jqGrid().setGridParam({url : contextPath + "/secure/preview/pdfQueryServlet.serx?query=pageSplit&startDate=" + startDate + "&endDate=" + endDate + "&center=" + center + "&applyNo=" + applyNo + "&policyNo=" + policyNo +"&insureId=" + insureId + "&areaId=" + areaId + "&policyStatus=" + policyStatus + "&recName=" + recName + "&sourceCode=" + sourceCode + "&receipt=" + receipt + "&exception=" + exception + "&groupInsure=" + groupInsure });
                },
                afterInsertRow: function(rowid){                    
                	var verifyResult = jQuery("#list4").getRowData(rowid).verifyResult;
                	jQuery("#list4").jqGrid().setCell(rowid,'policyStatus','','',{'title':verifyResult});                	
                }, 
                subGrid: true, 
                subGridUrl: contextPath + "/secure/preview/pdfQueryServlet.serx?query=subGrid", 
                subGridModel: [{ 
                                 name : ['保單文字檔檔名','列印檔','異常','最近更新時間','轉檔時間','列印時間','膠裝時間','驗單時間', '裝箱時間', '交寄時間', 'vip設定人','抽件設定人'], 
                                 width : [  350, 95, 60, 60,60   ,    60   ,60      ,60       ,60        ,  60, 60, 60      ] ,
                                 align : ['left','left','left','left','left','left','left','left','left','left','left' ] ,  
                                 params: ['cycleDateStr','center','applyNo','areaId','oldBatchName'] 
                                }],   
                loadError: function(xhr, status, error){ alert(error);alert(JSON.stringify(xhr)); },                                
                onPaging: function(){
				     var startDate = jQuery("#startDateHidden").val();
					 var endDate = jQuery("#endDateHidden").val();
					 var center = jQuery("#centerHidden").val();
					 var applyNo = jQuery("#applyNoHidden").val();
					 var policyNo = jQuery("#policyNoHidden").val();
					 var insureId = jQuery("#insureIdHidden").val();
					 var areaId = jQuery("#areaIdHidden").val();
					 var policyStatus = jQuery("#policyStatusHidden").val();
					 var recName = jQuery("#recNameHidden").val();
					 var sourceCode = jQuery("#sourceCodeHidden").val();
					 var receipt = jQuery("#receiptHidden").val();
					 var exception = jQuery("#exceptionHidden").val();
					 var groupInsure = jQuery("#groupInsureHidden").val();
					 jQuery("#list4").jqGrid().setGridParam({url : contextPath + "/secure/preview/pdfQueryServlet.serx?query=pageSplit&startDate=" + startDate + "&endDate=" + endDate + "&center=" + center + "&applyNo=" + applyNo + "&policyNo=" + policyNo +"&insureId=" + insureId + "&areaId=" + areaId + "&policyStatus=" + policyStatus + "&recName=" + recName + "&sourceCode=" + sourceCode + "&receipt=" + receipt + "&exception=" + exception + "&groupInsure=" + groupInsure });      
                },                          
                pager: "#pager1",
	            caption: "查詢結果"
	       }).setGridParam({lastpage:<h:outputText value="#{pdfViewQueryBean.totalPage}" />});
	       
	       var mydata = <h:outputText escape="false" value="#{pdfViewQueryBean.jsonResult}" />;
	       if(mydata != ''){
	    	  jQuery("#jqGridDiv").show();
	          for (var i = 0; i <= mydata.length; i++)
	               jQuery("#list4").addRowData(i + 1, mydata[i]);
	       }else{
	    	   jQuery("#jqGridDiv").hide();
		   }	       
	       jQuery("#list4").jqGrid().setGridParam({datatype : 'json'});
	       jQuery("#cancelSave").click( function() {
	    		jQuery("#list4").jqGrid('restoreRow',lastsel);
	    		lastsel = "";
	    		$("#editMode").hide();
	    	});
	       
	});
    function getSelectedPdfId(rowId){
    	var oldBatchName = jQuery("#list4").getRowData(rowId).oldBatchName;
    	var policyStatus = jQuery("#list4").getRowData(rowId).policyStatus;
    	var policyPDF = jQuery("#list4").getRowData(rowId).policyPDF;
    	
    	if(policyPDF !== ""){
    		if(policyStatus !== "轉檔成功" && policyStatus !== "回傳中")
    	        window.open(contextPath + "/secure/preview/pdfViewServlet.serx?oldBatchName=" + oldBatchName , "");
    		else
    			alert("本保單FXDMS尚未開始列印，無法預覽");
    	}
    }
    function clearColumn(){
        jQuery("#input_criteria input").val("");
    }
    function checkValid(){
    	return true;

    	if(newBatchName === null || newBatchName === ""){
    		alert("未產生列印檔前無法調整順序與抽件");
    		return false
    	}else{
    		return true;
    	}
    }
  
</script>	
	<link rel="stylesheet" type="text/css" media="screen"
		href="../objects/prog/css/themes/ui.jqgrid.css" />	

	<title>[國壽人員功能&gt;保單查詢]</title>

	<h:form id="sbForm">
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">保單查詢</th>
					</tr>

					<tr>
						<td align="center">
						<table width="100%" border="0" cellspacing="0" cellpadding="0"
							align="center" bgcolor="#006F3F">
							<tr>
								<td>
								<table id="input_criteria" width="1000" border="0" cellspacing="1" cellpadding="6"
									bgcolor="#4AAE66" align="center">
									<tr>
										<td width="90" align="center" bgcolor="#DFF4DD">Cycle Date區間</td>
										<td bgcolor="#F4FAF3" align="left">
										    <t:inputText 
											  id="dtSelect1" value="#{pdfViewQueryBean.startDate}"
											  size="6" maxlength="10">
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect1" style="color:red" /> ~ 
										   <t:inputText 
											   id="dtSelect2" value="#{pdfViewQueryBean.endDate}" size="6"
											   maxlength="10">
											   <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											   <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect2" style="color:red" />
										</td>
										<td width="90" align="center" bgcolor="#DFF4DD">行政中心</td>
										<td  bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="center" forceId="true" value="#{pdfViewQueryBean.center}" >
                                                  <f:selectItems  value="#{pdfViewQueryBean.myCenters}" />                                                  
                                             </t:selectOneMenu>
										   
										</td>
										<td width="90" align="center" bgcolor="#DFF4DD">來源類別</td>
										<td  bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="sourceCode" forceId="true" value="#{pdfViewQueryBean.sourceCode}" >
										          <f:selectItems  value="#{pdfViewQueryBean.sourceCodes}" />
                                             </t:selectOneMenu>										   
										</td>
									</tr>
									<tr>
										<td align="center" bgcolor="#DFF4DD">受理編號</td>
										<td  bgcolor="#F4FAF3" align="left">
										   <t:inputText id="applyNo" forceId="true" size="15"
											   value="#{pdfViewQueryBean.applyNo}"  maxlength="10" >								            
										   </t:inputText> 										   
										</td>
										<td align="center" bgcolor="#DFF4DD">保單號碼</td>
										<td  bgcolor="#F4FAF3" align="left" >
										   <t:inputText id="policyNo" forceId="true"
											   value="#{pdfViewQueryBean.policyNo}" size="15" maxlength="12" >								            
										   </t:inputText> 
										   
										</td>
										<td width="95" align="center" bgcolor="#DFF4DD">保單/簽收單/團險證</td>
										<td  bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="receipt" forceId="true" value="#{pdfViewQueryBean.receipt}" >
										          <f:selectItem   itemLabel="全部" itemValue=''/>
                                                  <f:selectItem   itemLabel="保單" itemValue='false'/>
                                                  <f:selectItem   itemLabel="簽收單" itemValue='true'/>
                                                  <f:selectItem   itemLabel="團險證" itemValue='null'/>
                                             </t:selectOneMenu>
										   
										</td>
									</tr>																	
									<tr>
										<td align="center" bgcolor="#DFF4DD">被保人ID</td>
										<td width="120" bgcolor="#F4FAF3" align="left" >
										   <t:inputText id="insureId" forceId="true"
											  value="#{pdfViewQueryBean.insureId}" size="15" maxlength="15" >
										   </t:inputText> 										   
										</td>
										<td align="center" bgcolor="#DFF4DD">轄區代碼</td>
										<td width="120" bgcolor="#F4FAF3" align="left" >
										     <t:inputText id="areaId" forceId="true"
											   value="#{pdfViewQueryBean.areaId}" maxlength="7" />
										</td>
										<td align="center" bgcolor="#DFF4DD">要保人</td>
										<td width="120" bgcolor="#F4FAF3" align="left" >
										     <t:inputText id="recName" forceId="true"
											   value="#{pdfViewQueryBean.recName}" maxlength="15" />
										</td>
									</tr>
									<tr>
										<td align="center" bgcolor="#DFF4DD">處理狀態</td>
										<td width="120" bgcolor="#F4FAF3" align="left" >
										     <t:selectOneMenu id="status" forceId="true" value="#{pdfViewQueryBean.policyStatus}" >
                                                  <f:selectItems  value="#{pdfViewQueryBean.policyStatuses}"/>                                                  
                                              </t:selectOneMenu>										    
										</td>
										<td width="95" align="center" bgcolor="#DFF4DD">驗單狀態</td>
										<td  bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="exception" forceId="true" value="#{pdfViewQueryBean.exception}" >
										          <f:selectItem   itemLabel="全部" itemValue='null'/>
                                                  <f:selectItem   itemLabel="驗單通過" itemValue='false'/>
                                                  <f:selectItem   itemLabel="驗單失敗" itemValue='true'/>                                                  
                                                  <f:selectItem   itemLabel="尚未驗單" itemValue='40'/>
                                             </t:selectOneMenu>
										   
										</td>										
										<td width="95" align="center" bgcolor="#DFF4DD"></td>
										<td  bgcolor="#F4FAF3" align="left" >
										</td>
									</tr>
								</table>
								</td>
							</tr>
						</table>
						</td>
					</tr>
					<tr>
						<td align="center" height="10"></td>
					</tr>
					<tr>
						<td colspan="4" align="center"><br />						   
						   <t:commandButton value="查詢" id="btnQuery" forceId="true"
							   type="submit" action="#{pdfViewQueryBean.doQuery}" />
						   <input type="button" value="清除" onclick="clearColumn()"/>
						   <span id="editMode" style="display:none">
						      &nbsp;&nbsp;|&nbsp;&nbsp;
						      <t:commandButton value="儲存" id="btnUpdate" forceId="true" 
							      type="submit" action="#{pdfViewQueryBean.update}" onclick="return checkValid();"/>
							   <input type="button" value="取消 儲存" id="cancelSave" />
							</span>							
						</td>
					</tr>
				</table>
				</td>
			</tr>
			<tr>
				<td height="5">
				<hr align="center" width="90%"
					style="border: 1px; border: 1px solid #DFF4DD;" />
				</td>
			</tr>
			<tr>
				<td align="center"><t:outputText id="dataResult" forceId="true"
					style="color: red" value="#{pdfViewQueryBean.result}" /></td>
			</tr>
			
            <tr>
				<td>
				<table width="98%" border="0" align="center" cellpadding="0"
					cellspacing="0" id="table_show">
					<tr>
						<td>
						<table width="100%" border="0" cellpadding="0" cellspacing="0" class="outline">
							<tr>
								<td  class="title" align="center">
								      <div align="center" id="jqGridDiv">
		                                 <table id="list4" class="scroll" align="left"></table>
		                                 <div id="pager1" align="left"></div>
		                              </div>    
								</td>
								
							</tr>
						</table>
						</td>
					</tr>
				</table>
				</td>
			</tr>			
		</table>
		
		
		<input type="hidden" id="startDateHidden"  value='<h:outputText escape="false" converter="converter.Date" value="#{pdfViewQueryBean.startDate}" />' />
		<input type="hidden" id="endDateHidden"  value='<h:outputText escape="false" converter="converter.Date" value="#{pdfViewQueryBean.endDate}" />' />
		<input type="hidden" id="centerHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.center}" />' />
		<input type="hidden" id="applyNoHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.applyNo}" />' />
		<input type="hidden" id="policyNoHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.policyNo}" />' />
		<input type="hidden" id="insureIdHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.insureId}" />' />
		<input type="hidden" id="areaIdHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.areaId}" />' />
		<input type="hidden" id="policyStatusHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.policyStatus}" />' />		
		<input type="hidden" id="sourceCodeHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.sourceCode}" />' />
		<input type="hidden" id="receiptHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.receipt}" />' />
		<input type="hidden" id="exceptionHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.exception}" />' />
		<input type="hidden" id="recNameHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.recName}" />' />
		<input type="hidden" id="groupInsureHidden"  value='<h:outputText escape="false"  value="#{pdfViewQueryBean.groupInsure}" />' />
	</h:form>
</f:view>
