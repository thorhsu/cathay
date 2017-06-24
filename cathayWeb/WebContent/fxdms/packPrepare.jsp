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
	var autoOpenDialog = <h:outputText value="#{fxPackQueryBean.autoOpen}" />;
    var dialog;
    var form;
	var fileName = "";
    $(function(){
    	form = $( "form" );
    	dialog = $( "#dialog-form" ).dialog({
    	      autoOpen: autoOpenDialog,
    	      height: 200,
    	      width: 350,	
    	      modal: true,     	          	      
    	  });
    	
         $(".statusNm").each(function(){
        	 var status = $(this).text();
        	 //裝箱預備，預設勾選
        	 if(status === '裝箱準備'){
        		 var tr = $(this).parent().parent();
        		 tr.find("input:checkbox").attr("checked", true);
        	 }
         });	
         
         $("#uniqueNo").keypress( function(event) {
 			$.ajaxSettings.async = false;
 		    if (event.keyCode === 13) {
 		    	var returnBoolean = false;
 		    	$.getJSON(contextPath + "/secure/preview/pdfQueryServlet.serx?query=getPack", 
 		    	        {uniqueNo: $("#uniqueNo").val()}).always(function(ret) { 			    	        	
 		    	        	    var result = ret.responseText;
 		    	        	    if(result === undefined){
 		    	        	    	result = ret;
 		    	        	    }
 			    	        	if (result === "NON_EXIST") {
                                    alert("無此保單，無法找到對應的打包內容");
 			    	        		returnBoolean = false;
 			    	        	}else if(result === "NON_EXIST_PACK"){
 			    	        		alert("此保單尚未產生打包清單");
 			    			    	returnBoolean = false;
 			    	           }else {
 			    	        	  var resultSplit = result.split("_"); 
 			    	        	  document.getElementById("sbForm:dtSelect1").value = resultSplit[0];
 			    	        	  $("#center").val(resultSplit[2]); 			    	        	  
 			    	              $("#batchOrOnline").val(resultSplit[1]);
 			    			      returnBoolean = true;
 			    	           }
 			    	});
 		    	return returnBoolean;
 		    }
 		});
    	
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
	       }).setGridParam({lastpage:<h:outputText value="#{fxPackQueryBean.totalPage}" />});
	       
	       var mydata  <h:outputText escape="false" value="#{fxPackQueryBean.jsonResult}" />;

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
    
    function checkNoBrCenter(){
    	
       var objDt = document.getElementById("sbForm:dtSelect1");
       var d = new Date();
       var month = "0" + (parseInt(d.getMonth()) + 1);
       month = month.substring(month.length - 2);
       $(objDt).val(d.getFullYear() + "/" + month + "/" + d.getDate());
       $("#center").val("06");
       $("#batchOrOnline").val("B");
       $("#noBr").val("true");
       $("#uniqueNo").val("");
       if( !dialog.dialog( "isOpen" )){
          dialog.dialog( "open" );
          $("#autoOpen").val("true");
          return false;
       }else{
    	  return true;
       }
       
       
    }
    
    function checkCenter(){    	
    	$("#noBr").val("false");
    	var center = $("#center").val();
    	var batchOrOnline = $("#batchOrOnline").val();    
    	if(center === "06" && batchOrOnline === "B" && !dialog.dialog( "isOpen" )){    		
    		dialog.dialog( "open" );
    		$("#autoOpen").val("true");
    		return false;
    	}else {
    		var uniqueNo = $("#uniqueNo").val(); 
    		if(uniqueNo === "")
    		   return window.confirm('注意！本Cycle Date如已產生打包清單及裝箱清單，此動作將會全部刪除，是否繼續？');
    		else
    		   return window.confirm('將單獨產生' + uniqueNo + "此保單的打包清單，請問是否繼續？");
    	}    	  
    }    
    
    function selectOne(obj){
		var thisId = $(obj).attr('id');		
		var hidden = document.getElementById(thisId + 'Hidden');
		var thisValue = hidden.value;
		return thisValue;
	}
    function checkVal( str ) {
	    if (/^[0-9]+$/.test(str))
	        return true;
	    else
	        return false;
	}
    
    function submitForm(){
    	//alert(checkVal($("#registerNo").val()));
    	if($("#registerNo").val() === "" || !checkVal($("#registerNo").val())){
    		alert("掛號號碼請輸入純數字");
    		return false;
    	}
    	if($("#parcelNo").val() === "" || !checkVal($("#parcelNo").val())){
    		alert("包裹號碼請輸入純數字");
    		return false;
    	}
    	$("#registerNoHidden").val($("#registerNo").val());
    	$("#parcelNoHidden").val($("#parcelNo").val());
    	if($("#noBr").val() !== "true"){
    	   $("#producePacks").click();
    	}else{    		
    	   $("#produceNoBrPacks").click();
        }
    	dialog.dialog( "close" );
    }
    
    function checkselct(){
    	if($('#dataList').find("input:checkbox:checked").length == 0){
			alert("請先勾選裝箱清單再送出");
			return false;
		}
    	$("#uniqueNo").val("");
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
		return true;
	}
  
</script>	
	<link rel="stylesheet" type="text/css" media="screen"
		href="../objects/prog/css/themes/ui.jqgrid.css" />	

	<title>[FX人員功能&gt;打包前作業]</title>

	<h:form id="sbForm">
	<div id="dialog-form" title="請輸入掛號號碼起始號">
          <fieldset>
            <p>
                <label for="registerNo">掛號號碼</label>
                <t:inputText  id="registerNo" value="#{fxPackQueryBean.registerNo}" forceId="true"
								  size="12" maxlength="10" />
			</p>
		    <p>                
                <label for="registerNo">包裹號碼</label>
                <t:inputText id="parcelNo" value="#{fxPackQueryBean.parcelNo}" forceId="true"
								  size="12" maxlength="10" />								  
			    <input type="button" value="送出" onclick="submitForm()"/>
			 </p>					  
          </fieldset>
  
     </div>
	
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">打包前作業</th>
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
										<td align="center" bgcolor="#DFF4DD">Cycle Date</td>
										<td  width="300" bgcolor="#F4FAF3" align="left">
										    <t:inputText 
											  id="dtSelect1" value="#{fxPackQueryBean.cycleDate}"
											  size="12" maxlength="10" required="true">
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect1" style="color:red" /> 
										</td>
										<td align="center" bgcolor="#DFF4DD">轄區</td>
										<td width="300" bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="center" forceId="true" value="#{fxPackQueryBean.center}" >
                                                  <f:selectItems  value="#{fxPackQueryBean.centers}" />                                                  
                                             </t:selectOneMenu>
										   
										</td>
									</tr>
									<tr>
										<td align="center" bgcolor="#DFF4DD">團險/個險</td>
										<td  width="300" bgcolor="#F4FAF3" align="left">
										      <t:selectOneMenu id="batchOrOnline" forceId="true" value="#{fxPackQueryBean.batchOrOnline}" >
                                                  <f:selectItem   itemLabel="個險" itemValue="B"/>
                                                  <f:selectItem   itemLabel="團險" itemValue="G"/>                                                                                                    
                                             </t:selectOneMenu>     
										</td>
										<td align="center" bgcolor="#DFF4DD">保單右上角號碼</td>
										<td width="300" bgcolor="#F4FAF3" align="left" >
										    <t:inputText id="uniqueNo" value="#{fxPackQueryBean.uniqueNo}" forceId="true"
											  size="20" />
										</td>										
									</tr>
									<t:inputHidden id="packIds" forceId="true" value="#{fxPackQueryBean.packIds}" />
									<t:inputHidden id="autoOpen" forceId="true" value="#{fxPackQueryBean.autoOpen}" />
									<t:inputHidden id="registerNoHidden" forceId="true" value="#{fxPackQueryBean.registerNo}" />
									<t:inputHidden id="parcelNoHidden" forceId="true" value="#{fxPackQueryBean.parcelNo}" />
									<input type="hidden" id="noBr" value="false" />
																
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
							   type="submit" action="#{fxPackQueryBean.doQuery}" />
						   &nbsp;&nbsp;&nbsp;
						   <t:commandButton value="產生打包清單" id="producePacks" forceId="true"
							   type="submit" action="#{fxPackQueryBean.producePacks}" onclick="return checkCenter();"/>                            							 
						   <t:commandButton value="合併裝箱清單" 
							   type="submit" action="#{fxPackQueryBean.combinePacks}" onclick="return checkselct();" style="display:none"/>
						   <t:commandButton value="列印打包清單"  
							   type="submit" action="#{fxPackQueryBean.exportPackExcel}" onclick="return checkselct();"/>
						   <t:commandButton value="產生等待送金單打包清單"  id="produceNoBrPacks" forceId="true"
							   type="submit" action="#{fxPackQueryBean.produceNoBrPacks}" onclick="return checkNoBrCenter();"/>
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
					style="color: red" value="#{fxPackQueryBean.result}" /></td>
			</tr>
			
			<tr>
			    <td align="center">
			       <t:dataTable value="#{fxPackQueryBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="pack" 
										binding="#{fxPackQueryBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				 
										rowClasses="#{fxPackQueryBean.rowClases}"										
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
										<t:commandLink value="#{pack.packId}" action="#{fxPackQueryBean.editAddress}"  >
											<f:param name="packId" value="#{pack.packId}" />
										</t:commandLink>										
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
									<t:column rendered="#{fxPackQueryBean.batchOrOnline == 'G'}" >
										<f:facet name="header">
											<t:outputText value="保險證數"></t:outputText>
										</f:facet>
										<t:outputText value="#{pack.inusreCard}" />
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
		                                       <td align="right">${fxPackQueryBean.totalFiles}</td>
		                                       <td></td>
		                                       <td></td>
		                                       <th>保單數</th>
		                                       <td align="right">${fxPackQueryBean.totalBooks}</td>
		                                       <td></td>
		                                       <td></td>
		                                       <th>簽收回條數</th>
		                                       <td align="right">${fxPackQueryBean.totalReceipts}</td>
		                                       
		                                    </tr>
		                                    <tr>
		                                       <th>總頁數</th>
		                                       <td align="right">${fxPackQueryBean.totalPages}</td>
		                                       <td></td>
		                                       <td></td>		                                       
		                                       <th>總張數</th>
		                                       <td align="right">${fxPackQueryBean.totalSheets}</td>		                                       
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
	</h:form>
</f:view>
