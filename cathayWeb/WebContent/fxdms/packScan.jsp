<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
   <style>
      .jqmWindow {
         display: none;    
         position: fixed;
         top: 20%;
         left: 60%;
    
         margin-left: -360px;
         width: 350px;
         height: 200px;
    
         background-color: #EEE;
         color: #333;
         border: 1px solid black;
         padding: 12px;         
      }
      .jqmOverlay { background-color: #000; }
   </style>
    <script src="../objects/prog/js/plugins/jquery.browser.js"
		type="text/javascript"></script>    	
	<script type="text/javascript" src="../objects/prog/js/sbDate.js"></script>
	<script src="../objects/prog/js/lib/i18n/grid.locale-en.js"
		type="text/javascript"></script>	
	<script src="../objects/prog/js/lib/jquery.jqGrid.min.js"
		type="text/javascript"></script>
	<script src="../objects/prog/js/src/jqModal.js" type="text/javascript"></script>	
	<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
    var dataResult = '<h:outputText escape="false" value="#{fxAfpQueryBean.result}" />';
	var fileName = "";
	function clearColumn(){
		$("#inputNo").val("");
		$("#packId").val("");		
		$("#packId").focus();
	}
	
	var group = <h:outputText escape="false" value="#{fxAfpQueryBean.group}" />;
	var center = '<h:outputText escape="false" value="#{fxAfpQueryBean.center}" />';
    $(function(){
    	$('#dialog-form').jqm();
    	//$('#dialog-form').jqmShow();
    	$('#dialog-form').jqmHide();
    	
    	if(group && $("#registerNo").val() !== ""){
    		$("#registerNoArea").show();
    	}
    	if(dataResult !== "" && dataResult.indexOf("完成") < 0 ){
    		alert(dataResult);
    	}
         $(".statusNm").each(function(){
        	 var status = $(this).text();
        	 //預設勾選 裝箱完成
        	 if(status === '裝箱掃描完成'){
        		 var tr = $(this).parent().parent();
        		 tr.find("input:checkbox").attr("checked", true);
        	 }
         });	
         if(dataResult.indexOf("裝箱檢查完成") < 0){
            $("#packId").select();
            $("#inputNo").val("");
         }else{
        	$("#inputNo").select();
         }
    	
	     jQuery("#list4").jqGrid({
	            datatype: "local",
	            height: 500,
	            colNames: ['列印檔名', '處理順序','處理狀態','最近更新時間','VIP 設定人','轄區','頁數','Sheets', '本數'],
	            colModel: [{name: 'newBatchName', index: 'newBatchName', align: "right"},	                        	                       
	       	               {name: 'newBatchNo', index: 'newBatchNo', width: 100 , align: "right"},
	       	               {name: 'status', index: 'status', width: 150 , align: "center"},	       	            
	       	               {name: 'updateDateStr', index: 'updateDateStr', width: 160 , align: "center"},
	       	               {name: 'vipModifierName', index: 'policyNos', width: 100 , align: "center"},
	       	               {name: 'center', index: 'center', width: 70 , align: "center"},
	       	               {name: 'pages', index: 'pages', width: 80 , align: "right"},
	       	               {name: 'sheets', index: 'sheets', width: 80 , align: "right"},
	       	               {name: 'volumns', index: 'volumns', width: 100 , align: "right"}],
	       	    onCellSelect: function(rowId, iCol){
   	                  
                },
                /*
private Date presTime; //pres轉檔時間
	private Date printTime; //列印時間
	private Date bindTime;  //膠裝時間
	private Date verifyTime; //驗單時間
	private Date packTime; //裝箱時間
	private Date deliverTime; //交寄時間
                */
                subGrid : true, 
                subGridUrl: contextPath + "/secure/preview/pdfQueryServlet.serx?query=afpSubGrid&cycleDate=true", 
                subGridModel: [{ 
                                 name : [ '受理編號','收件人', 'cycleDate', '被保險人ID','保單號碼','中心代碼','服務中心','目前狀態','簽收單', '抽件','最近更新時間'], 
                                 width : [  100     , 70    ,'100',    80       ,120      ,70        ,120      ,  70     , 50    , 50   , 150     ] ,
                                 align : ['left','left','left','left','left','left','left','left','center','center','left' ] ,  
                                 params: ['newBatchName'] 
                                }],   
                loadError: function(xhr, status, error){ alert(error);alert(JSON.stringify(xhr)); },                                                                                 
	            caption: "尚未完成交寄的列印檔"
	       }).setGridParam({lastpage:<h:outputText value="#{fxAfpQueryBean.totalPage}" />});
	       
	       var mydata  <h:outputText escape="false" value="#{fxAfpQueryBean.jsonResult}" />;

	       if(mydata !== undefined && mydata !== null && mydata !== ''){
	    	  jQuery("#jqGridDiv").show();
	          for (var i = 0; i <= mydata.length; i++)
	               jQuery("#list4").addRowData(i + 1, mydata[i]);
	       }else{
	    	   jQuery("#jqGridDiv").hide();
		   }	       
	       jQuery("#list4").jqGrid().setGridParam({datatype : 'json'});
	       
	       $("#producePacks").click(function(){	    	
	    	   if($("#center").val() === ""){
	    		   alert("請先選轄區再產生打包檔案");
	    		   return false;
	    	   }
	       });
	       
	});
    function getSelectedPdfId(rowId){    	
    	var status = $("#list4").getRowData(rowId).status;
    }
    
    function selectOne(obj){
		var thisId = $(obj).attr('id');		
		var hidden = document.getElementById(thisId + 'Hidden');
		var thisValue = hidden.value;
		return thisValue;
	}
    
    function checkselct(obj){
    	if($('#dataList').find("input:checkbox:checked").length == 0){
			alert("請先勾選打包清單再送出");
			return false;
		}
    	
    	var packIds = "";		
		var i = 0;
    	$('#dataList').find("input:checkbox:checked").each(function(){
    		if( $(this).attr('id') != 'cbSelAll'){
			   var packId = selectOne(this);
			   packIds = packIds  + packId + ",";
			   i++;
    		}
		});
    	
		if(packIds != "")
			packIds = packIds.substring(0, packIds.length - 1);		
        				
		jQuery("#packIds").val(packIds);
		
		if(group && obj.id === "prodLogistic"){
			$("#dialog-form").jqmShow();
			$("#registerNoArea").show();
			return false;
		}else if(group && obj.id === "prodLogisticMail"){	
			if($("#registerNo").val() === "" ){
				alert("輸入掛號號碼起始號");
				return false;
			}
			if($("#parcelNo").val() === "" ){
				alert("輸入包裹號碼起始號");
				return false;
			}
			var patt = new RegExp("^[0-9]+$");			
		    var res = patt.test($("#registerNo").val());		    
			if(!res){
				alert("掛號號碼請輸入數字");
			    return false;
			}
			res = patt.test($("#parcelNo").val());
			if(!res){
				alert("包裹號碼請輸入數字");
			    return false;
			}
			return(window.confirm('確認產生？'));
			
		}else if(!group && center === '06'){
			$("#registerNoArea").hide();
			$("#registerNo").val("");
			alert("注意：北二個險寄件清單無法在此產生及列印，請在打包前作業的頁面產生或列印");
			return false;
		}else if(!group){
			$("#registerNoArea").hide();
			$("#registerNo").val("");
		}
		
		return true;
	}
    
  
</script>	
	<link rel="stylesheet" type="text/css" media="screen"
		href="../objects/prog/css/themes/ui.jqgrid.css" />	

	<title>[FX人員功能&gt;裝箱記錄]</title>

	<h:form id="sbForm">
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">打包記錄</th>
					</tr>

					<tr>
						<td align="center">
						<table width="100%" border="0" cellspacing="1" cellpadding="1"
							align="center" bgcolor="#006F3F">
							<tr>
								<td >
								<table width="100%" border="0" cellspacing="1" cellpadding="6"
									bgcolor="#4AAE66" align="left">
									<tr>
										<td width="20%" align="center" bgcolor="#DFF4DD">打包號碼</td>
										<td  bgcolor="#F4FAF3" align="left">
										    <t:inputText forceId="true" 
											  id="packId" value="#{fxAfpQueryBean.packId}"
											  size="50" maxlength="17" required="true" />
										   <h:message for="packId" style="color:red" />
										   <t:inputHidden id="packIds" forceId="true" value="#{fxAfpQueryBean.packIds}" /> 
										</td>
									</tr>
									<tr>
										<td width="20%" align="center" bgcolor="#DFF4DD">保單或簽收單號碼</td>
										<td  bgcolor="#F4FAF3" align="left">
										    <t:inputText forceId="true" 
											  id="inputNo" value="#{fxAfpQueryBean.inputNo}"
											  size="20" maxlength="20"  />
										   <h:message for="inputNo" style="color:red" /> 
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
						   <t:commandButton value="送出" id="scanPack" forceId="true"
							   type="submit" action="#{fxAfpQueryBean.scanPack}" />
						   &nbsp;&nbsp;&nbsp;
						   <t:commandButton value="產生寄件清單" id="prodLogistic" forceId="true"
							   type="submit" action="#{fxAfpQueryBean.prodLogistic}" onclick="return checkselct(this);"/>
							&nbsp;&nbsp;&nbsp;
							<t:commandButton value="列印已產生的寄件清單" id="getLogistic" forceId="true"
							   type="submit" action="#{fxAfpQueryBean.getLogisticFile}" onclick="return checkselct(this);"/>
							&nbsp;&nbsp;   
							<input type="button" value="清除" onclick="clearColumn();"/>   
						   
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
					style="color: red" value="#{fxAfpQueryBean.result}" /></td>
			</tr>
			
			<tr>
			   <th>打包內容</th>
			</tr>
			<tr>
			    <td align="center">
			       <t:dataTable value="#{fxAfpQueryBean.adDataModel}"
										id="dataList1" 
										forceId="true" 
										var="applyData" 
										binding="#{fxAfpQueryBean.adDataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="#{fxAfpQueryBean.adRowClasses}" 
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<t:outputText value="">保單/簽收單</t:outputText>
										</f:facet>
										<t:outputText  value="#{applyData.receipt? '簽收單' : '保單'}" rendered="#{!applyData.groupInsure}"/>
										<t:outputText  value="保險證" rendered="#{applyData.groupInsure}"/>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="已打包 "></t:outputText>
										</f:facet>
										<t:outputText value="#{(applyData.packTime == null)? '否' : '是 '}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="保單右上角號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.uniqueNo}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="保單號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.policyNos}" >
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="寄送地址"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.areaAddress}" />
									</t:column>									
									<t:column>
										<f:facet name="header">
											<t:outputText value="收件單位"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.areaName}" />
									</t:column>
																										
									<t:column>
										<f:facet name="header">
											<t:outputText value="打包時間"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.packTime}">
										   <f:convertDateTime pattern="yyyy/MM/dd HH:mm" timeZone="GMT+8" />
										</t:outputText>										
									</t:column>
							</t:dataTable>
			    </td>
			</tr>
			
			
			<tr>
			   <th>所有打包清單</th>
			</tr>
			<tr>
			    <td align="center">
			       <t:dataTable value="#{fxAfpQueryBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="pack" 
										binding="#{fxAfpQueryBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="#{fxAfpQueryBean.rowClasses}" 
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<f:verbatim>
												<input type="checkbox" id="cbSelAll" name="cbSelAll" onclick="selAllCb(this, '#dataList');" value="all"/>
											</f:verbatim>
										</f:facet>
										<t:selectBooleanCheckbox id="cbSelOne"  />
										<t:inputHidden id="cbSelOneHidden" value="#{pack.packId}" />										
									</t:column>		
									<t:column >
										<f:facet name="header">
											<t:outputText value="打包代碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{pack.packId}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="正常件"></t:outputText>
										</f:facet>
										<t:outputText value="#{!pack.back}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="收件單位"></t:outputText>
										</f:facet>
										<t:outputText value="#{pack.subAreaId}">
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="名稱"></t:outputText>
										</f:facet>
										<t:outputText value="#{pack.subAreaName}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="住址"></t:outputText>
										</f:facet>
										<t:outputText value="#{pack.areaAddress}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="電話"></t:outputText>
										</f:facet>
										<t:outputText value="#{pack.subAreaTel}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="保單總數"></t:outputText>
										</f:facet>
										<t:outputText value="#{pack.books}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="簽收單數"></t:outputText>
										</f:facet>
										<t:outputText value="#{pack.receipts}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{pack.statusNm}" styleClass="statusNm"/>
									</t:column>
																										
									<t:column>
										<f:facet name="header">
											<t:outputText value="最近更新時間"></t:outputText>
										</f:facet>
										<t:outputText value="#{pack.updateDate}">
										   <f:convertDateTime pattern="yyyy/MM/dd HH:mm" timeZone="GMT+8" />
										</t:outputText>										
									</t:column>
							</t:dataTable>
			    </td>
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
								      <div align="center" id="jqGridDiv" style="display:none">
		                                 <table id="list4" class="scroll" align="left"></table>
		                                 <table>
		                                    <tr>
		                                       <th>列印檔數</th>
		                                       <td align="right">${fxAfpQueryBean.totalFiles}</td>
		                                       <td></td>
		                                       <td></td>
		                                       <th>保單數</th>
		                                       <td align="right">${fxAfpQueryBean.totalBooks}</td>
		                                       <td></td>
		                                       <td></td>
		                                       <th>簽收回條數</th>
		                                       <td align="right">${fxAfpQueryBean.totalReceipts}</td>
		                                       
		                                    </tr>
		                                    <tr>
		                                       <th>總頁數</th>
		                                       <td align="right">${fxAfpQueryBean.totalPages}</td>
		                                       <td></td>
		                                       <td></td>		                                       
		                                       <th>總張數</th>
		                                       <td align="right">${fxAfpQueryBean.totalSheets}</td>		                                       
		                                       <td></td>
		                                       <td></td>
		                                       <th></th>
		                                       <td ></td>		                                       
		                                    </tr>
		                                 </table>		                          
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
		<div id="dialog-form" class="jqmWindow" title="保單資訊">          
             <table id="input_criteria" >
                <tr id="registerNoArea" align="center">
				   <td width="50%" align="right" bgcolor="#DFF4DD">輸入掛號號碼起始號</td>
				   <td  bgcolor="#F4FAF3" align="left">
					  <t:inputText forceId="true" 
						  id="registerNo" value="#{fxAfpQueryBean.registerNo}"
								  size="20" maxlength="20"  />
					  <h:message for="registerNo" style="color:red" />
					  <t:inputHidden value="#{fxAfpQueryBean.group}" /> 
					  <t:inputHidden value="#{fxAfpQueryBean.batchOrOnline}" />
					  <t:inputHidden value="#{fxAfpQueryBean.center}" />
				   </td>
				</tr>
                <tr id="parcelNoArea" >
				   <td width="50%" align="right" bgcolor="#DFF4DD">輸入包裹掛號號碼起始號</td>
				   <td  bgcolor="#F4FAF3" align="left">
					  <t:inputText forceId="true" 
						  id="parcelNo" value="#{fxAfpQueryBean.parcelNo}"
								  size="20" maxlength="20"  />
					  <h:message for="parcelNo" style="color:red" />
				   </td>
				</tr>
				<tr>
				    <td colspan="2">
				        <t:commandButton value="產生寄件清單" id="prodLogisticMail" forceId="true"
							   type="submit" action="#{fxAfpQueryBean.prodLogistic}" onclick="return checkselct(this);"/>
				    </td>
				</tr>
             </table>          
        </div>
	</h:form>
</f:view>
