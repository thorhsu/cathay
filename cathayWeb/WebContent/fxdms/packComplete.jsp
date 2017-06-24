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
    var focusDecider = '<h:outputText escape="false" value="#{packCompleteBean.focusDecider}" />';
    var result = '<h:outputText escape="false" value="#{packCompleteBean.result}" />';
    var tp2 = <h:outputText  value="#{packCompleteBean.tp2}" />;
    var mailReceipt = <h:outputText  value="#{packCompleteBean.mailReceipt}" />;
	var fileName = "";
	$(function(){
		if(tp2 && mailReceipt){
		   $("#packNo").text("保單右上角號碼");		   		   
		}
		if(mailReceipt){
			$("#mailReceipt").show();
		}
		$("#logisticId").focus();
		if(focusDecider === "logisticId"){
			$("#packId").val("");
			$("#vendorId").val("");
			$("#scanVendorId").val("");
			$("#weight").val("");
		}
		$("#" + focusDecider).select();
		if(result.indexOf("無此") >= 0 || result === "回執聯號碼不正確"){
			alert(result);
		}		
		$('#logisticId').keypress( function(event) {
			$.ajaxSettings.async = false;
		    if (event.keyCode === 13) {
		    	var returnBoolean = false;
		    	$.getJSON(contextPath + "/secure/preview/pdfQueryServlet.serx?query=getVendorId", 
		    	        {logisticId: $('#logisticId').val()}).always(function(ret) {
			    	        	
		    	        	    var result = ret.responseText;
		    	        	    if(result === undefined){
		    	        	    	result = ret;
		    	        	    }
			    	        	if (result === "NON_EXIST") {
			    	        		$("#vendorId").val("");
			    	        		$('#weight').val("");
			    	        		$("#vendorId").select();
			    	        		returnBoolean = false;
			    	        	}else {			    	        		
			    	        		$('#vendorId').val(result);
			    	        		$('#weight').val("");
			    			    	$("#packId").select();
			    			    	returnBoolean = true;
			    	           }
			    	});
		    }else if(event.keyCode === 13 && $('#vendorId').val() !== ""){
		    	returnBoolean = true;
		    }
		    return returnBoolean;
		});
		
		$('#packId').keypress( function() {		
		    if (event.keyCode === 13 && mailReceipt) {
		    	$("#scanVendorId").select();
		    	return false;
		    }
		    
		});
		$("#scanVendorId").keypress( function() {		
		    if (event.keyCode === 13 && mailReceipt) {
		    	if($("#scanVendorId").val() !== $('#vendorId').val()){
		    		alert("回執聯號碼和掛號號碼不同，請重新輸入");
		    		return false;	
		    	}else{
		    		return true;
		    	}
		    	
		    }
		    
		});
		
	});
	var clearColumn = function(){
		$("#mailReceipt").val("");
		$("#packId").val("");
		$("#vendorId").val("");
		$("#logisticId").val("");
		$("#weight").val("");
		$("#scanVendorId").val("");		
		$("#logisticId").select();
	}
	
	function selectOne(obj){
		var thisId = $(obj).attr('id');		
		var hidden = document.getElementById(thisId + 'Hidden');
		var thisValue = hidden.value;
		return thisValue;
	}
	
    function checkselct(){    	
    	if($('#dataList').find("input:checkbox:checked").length == 0){
			alert("請先勾選裝箱清單再送出");
			return false;
		}else{
			if(!window.confirm('確認送出?')){
				return false;
			}
		}
    	var logisticIds = "";		
		var i = 0;
		
    	$('#dataList').find("input:checkbox:checked").each(function(){
    		if( $(this).attr('id') != 'cbSelAll'){
			   var logisticId = selectOne(this);
			   logisticIds = logisticIds  + logisticId + ",";
			   i++;
    		}
		});
		if(logisticIds != "")
			logisticIds = logisticIds.substring(0, logisticIds.length - 1);        				
		$("#logisticIds").val(logisticIds);		
		return true;
	}
  
</script>	
	<link rel="stylesheet" type="text/css" media="screen"
		href="../objects/prog/css/themes/ui.jqgrid.css" />	

	<title>[FX人員功能&gt;裝箱記錄完成]</title>

	<h:form id="sbForm">
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">裝箱記錄</th>
					</tr>

					<tr>
						<td align="center">
						<table width="100%" border="0" cellspacing="1" cellpadding="1"
							align="center" bgcolor="#006F3F">
							<tr >
								<td align="center">
								<table width="100%" border="0" cellspacing="1" cellpadding="6"
									bgcolor="#4AAE66" align="left">
									<tr>
										<td width="15%" align="center" bgcolor="#DFF4DD">裝箱號碼</td>
										<td  bgcolor="#F4FAF3" align="left">
										    <t:inputText forceId="true" 
											  id="logisticId" value="#{packCompleteBean.logisticId}"
											  size="50" maxlength="15" required="true" />
										   <h:message for="logisticId" style="color:red" /> 
										   &nbsp;&nbsp;
										        重量：
										   <t:inputText forceId="true" 
											  id="weight" value="#{packCompleteBean.weight}"
											  size="3" >
											  <f:validator validatorId="validator.numAndDotValidator" />											  
										   </t:inputText>
										        克
										   <h:message for="weight" style="color:red" />
										</td>
									</tr>
									<tr>
										<td width="15%" align="center" bgcolor="#DFF4DD">掛號/超峰代號</td>
										<td  bgcolor="#F4FAF3" align="left">
										    <t:inputText forceId="true" required="true"
											  id="vendorId" value="#{packCompleteBean.vendorId}"
											  size="50" maxlength="17"  />
										   <h:message for="vendorId" style="color:red" /> 
										</td>
									</tr>
									<tr>
										<td width="15%" id="packNo" align="center" bgcolor="#DFF4DD">打包號碼</td>
										<td  bgcolor="#F4FAF3" align="left">
										    <t:inputText forceId="true" 
											  id="packId" value="#{packCompleteBean.packId}"
											  size="20" maxlength="17"  />
										   <h:message for="packId" style="color:red" /> 
										</td>
									</tr>
									<tr id="mailReceipt" style="display:none">
										<td width="15%" align="center" bgcolor="#DFF4DD">回執聯</td>
										<td  bgcolor="#F4FAF3" align="left">
										    <t:inputText forceId="true" 
											  id="scanVendorId" value="#{packCompleteBean.scanVendorId}"
											  size="20" maxlength="15"  />
										   <h:message for="scanVendorId" style="color:red" /> 
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
							   type="submit" action="#{packCompleteBean.scanPack}" />
						   <t:commandButton value="修改掛號號碼與重量" id="modifyRegisterNo" 
							   type="submit" action="#{packCompleteBean.modifyRegisterNo}" onclick="return confirm('確認修改？')"/>   
                           <t:commandButton value="批次設定交寄完成" id="batchPackComplete" forceId="true"
							   type="submit" action="#{packCompleteBean.batchPackComplete}" onclick="return checkselct();"/>							   
						   &nbsp;&nbsp;&nbsp;
						   <input type="button" value="清除" onclick="clearColumn();"/>
						   <t:inputHidden id="logisticIds" forceId="true" value="#{packCompleteBean.logisticIds}" />						   						   
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
					style="color: red" value="#{packCompleteBean.result}" /></td>
			</tr>
			<tr>
			   <th><h:outputText  value="打包清單" rendered="#{!packCompleteBean.tp2}"/><h:outputText  value="保單清單" rendered="#{packCompleteBean.tp2}"/></th>
			</tr>
			<tr>
			    
			    <td align="center">
			       <t:dataTable value="#{packCompleteBean.applyDataModel}"
										id="dataList3" 
										forceId="true" 
										var="applyData" 
										binding="#{packCompleteBean.applyDataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="#{packCompleteBean.applyRowClasses}" 
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<t:outputText value="保單右上角號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.uniqueNo}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="已裝箱"></t:outputText>
										</f:facet>
										<t:outputText value="#{(applyData.policyStatus eq '100' or applyData.policyStatus eq '98' or applyData.policyStatus eq '97') ? '是' : '否'}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="寄送住址"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.address}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="cycleDate"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.cycleDate}" >
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="要保人"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.recName}" />
									</t:column>									
									<t:column>
										<f:facet name="header">
											<t:outputText value="收件人"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.receiver}" />
									</t:column>
																										
									<t:column>
										<f:facet name="header">
											<t:outputText value="最新更新時間"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.updateDate}">
										   <f:convertDateTime pattern="yyyy/MM/dd HH:mm" timeZone="GMT+8" />
										</t:outputText>										
									</t:column>
							</t:dataTable>
			    
			    
			       <t:dataTable value="#{packCompleteBean.packDataModel}"
										id="dataList1" 
										forceId="true" 
										var="packStatus" 
										binding="#{packCompleteBean.packDataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="#{packCompleteBean.rowClasses}" 
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<t:outputText value="打包代碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{packStatus.packId}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="已裝箱"></t:outputText>
										</f:facet>
										<t:outputText value="#{packStatus.packCompleted ? '是' : '否'}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="寄送住址"></t:outputText>
										</f:facet>
										<t:outputText value="#{packStatus.areaAddress}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="cycleDate"></t:outputText>
										</f:facet>
										<t:outputText value="#{packStatus.cycleDate}" >
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="保單數"></t:outputText>
										</f:facet>
										<t:outputText value="#{packStatus.books}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="名稱"></t:outputText>
										</f:facet>
										<t:outputText value="#{packStatus.subAreaName}" />
									</t:column>									
									<t:column>
										<f:facet name="header">
											<t:outputText value="電話"></t:outputText>
										</f:facet>
										<t:outputText value="#{packStatus.subAreaTel}" />
									</t:column>
																										
									<t:column>
										<f:facet name="header">
											<t:outputText value="最新更新時間"></t:outputText>
										</f:facet>
										<t:outputText value="#{packStatus.updateDate}">
										   <f:convertDateTime pattern="yyyy/MM/dd HH:mm" timeZone="GMT+8" />
										</t:outputText>										
									</t:column>
							</t:dataTable>
			    </td>
			</tr>
			<tr>
			   <td></td>
			</tr>
			<tr>
			   <th>所有裝箱編號</th>
			</tr>
			<tr>			    
			    <td align="center">
			       <t:dataTable value="#{packCompleteBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="logisticStatus" 
										binding="#{packCompleteBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="#{packCompleteBean.logisticRowClasses}" 
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<f:verbatim>
												<input type="checkbox" id="cbSelAll" name="cbSelAll" onclick="selAllCb(this, '#dataList');" value="all"/>
											</f:verbatim>
										</f:facet>
										<t:selectBooleanCheckbox id="cbSelOne"  />
										<t:inputHidden id="cbSelOneHidden" value="#{logisticStatus.logisticId}" />										
									</t:column>		
									<t:column >
										<f:facet name="header">
											<t:outputText value="裝箱代碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.logisticId}" />
									</t:column>
									<t:column rendered="#{packCompleteBean.center eq '06'}">
										<f:facet name="header">
											<t:outputText value="掛號號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.vendorId}" />
									</t:column>
									<t:column rendered="#{packCompleteBean.center ne '06'}">
										<f:facet name="header">
											<t:outputText value="超峰號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.vendorId}" />
									</t:column>
									
									<t:column >
										<f:facet name="header">
											<t:outputText value="完成裝箱"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.packDone ? '是' : '否'}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="收件人"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.name}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="寄送住址"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.address}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="cycleDate"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.cycleDate}" >
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="保單總數"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.books}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText id="unitNum" forceId="true" value="內含紙袋" ></t:outputText>											
										</f:facet>
										<t:outputText value="#{logisticStatus.packs}" />
									</t:column>
                                    <t:column rendered="#{packCompleteBean.tp2}">
										<f:facet name="header">
											<t:outputText styleClass="mailReceipt" value="回執聯"></t:outputText>
										</f:facet>
										<t:outputText styleClass="mailReceipt" value="#{logisticStatus.mailReceipt? '有' : ''}" />
									</t:column>												
																						
									<t:column>
										<f:facet name="header">
											<t:outputText value="最新更新時間"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.scanDate}">
										   <f:convertDateTime pattern="yyyy/MM/dd HH:mm" timeZone="GMT+8" />
										</t:outputText>										
									</t:column>
							</t:dataTable>
			    </td>
			</tr>			
		</table>
	</h:form>
</f:view>
