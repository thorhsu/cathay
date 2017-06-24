<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<script type="text/javascript" src="../../objects/prog/js/lib/jquery.datepick.min.js"></script>

<script type="text/javascript" src="../../objects/prog/js/sbDate.js"></script>
<link href="../../objects/prog/css/jquery.datepick.css" rel="stylesheet" type="text/css" />
<style type="text/css">
    #categoryBnms {
       margin-left:2px;
       margin-right:auto;
    }
</style>
<title>[FX人員功能&gt;修改收件單位與地址]</title>
<f:view>
<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
	$().ready(function(){
		var allAreaJson = <t:outputText value="#{fxPackQueryBean.areaJson}" escape="false" />;
		//var allAreaJson = {"address":"高雄市鳳山區光復路６５號８樓","areaId":"SE10000","areaName":"專招鳳揚二","independent":false,"serviceCenter":"鳳山服務一","serviceCenterNm":"鳳山服務一","subAreaId":"SE10","tel":"07-7478535","version":0,"zipCode":"830"}
		var allSubArea = <t:outputText value="#{fxPackQueryBean.allAuditArea}" escape="false" />;		
		$( "#subAreaId" ).autocomplete({
	        source: allSubArea,
	        close: function(){
	           	if($("#subAreaId").val().indexOf("__") > 1){
	           		$("#subAreaId").val($("#subAreaId").val().substring(1, $("#subAreaId").val().indexOf("__")));
	           		var inputId = $( "#subAreaId" ).val();
	    			inputId = $.trim(inputId);
	    			allAreaJson.forEach(function(element){			  			  
	    				  if(inputId == element.subAreaId){
	    					  var areaName = element.areaName;
	    					  var address = element.address;
	    					  var tel = element.tel;
	    					  //alert(areaName + ":" + address);
	    					  $("#subAreaName").val(areaName);
	    					  $("#areaAddress").val(address);
	    					  $("#subAreaTel").val(tel);					  
	    					  return;
	    				  }			  
	    			  });		  	
	           	}
	        }
	     });
		/*
		$("#subAreaId").keypress( function(event) {
		    if (event.keyCode === 13) {
		    	$("#subAreaName").focus();
		    	
		    }
		});
		$( "#subAreaId" ).blur(function(){
			var inputId = $( "#subAreaId" ).val();
			inputId = $.trim(inputId);
			allAreaJson.forEach(function(element){			  			  
				  if(inputId == element.subAreaId){
					  var areaName = element.areaName;
					  var address = element.address;
					  var tel = element.tel;
					  
					  $("#subAreaName").val(areaName);
					  $("#areaAddress").val(address);
					  $("#subAreaTel").val(tel);					  
					  return;
				  }			  
			  });		  						
		});
		*/
	});
</script>

<h:form id="sbForm">
<table width="825" border="0" cellspacing="0" cellpadding="0">
    <tr><td align="center"><t:outputText id="dataResult" forceId="true" style="color: red" value="#{fxPackQueryBean.result}"/></td></tr>
	<tr>
		<td align="center" valign="top">
		<table width="825" border="0" align="center" cellpadding="0"
			cellspacing="0" id="table_criteria">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellpadding="0" cellspacing="0"
					class="outline">
					<tr>
						<td colspan="2" class="title">修改收件單位與地址</td>
					</tr>
					<tr>
						<td colspan="2">
							<div id="divCriteria">
								<table width="100%" border="0" cellpadding="0" cellspacing="1"
									class="content" id="tbCriteria">
									<tr>
										<th width="20%">打包編號</th>
										<td>											
											<h:outputText  value="#{fxPackQueryBean.pack.packId}"/>
                                            <t:inputHidden value="#{fxPackQueryBean.pack.packId}" />
										</td>
									</tr>
									<tr>
										<th>Cycle Date</th>
										<td>
											<h:outputText  value="#{fxPackQueryBean.pack.cycleDate}">
											   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
											</h:outputText>
                                            <t:inputHidden value="#{fxPackQueryBean.pack.cycleDate}" >
                                                <f:convertDateTime pattern="yyyy/MM/dd HH:mm" timeZone="GMT+8" />
                                            </t:inputHidden>
										</td>
									</tr>
									<tr>
										<th>收件單位／人 </th>
										<td>
											<t:inputText value="#{fxPackQueryBean.pack.subAreaId}" id="subAreaId" forceId="true"/> &nbsp;&nbsp;&nbsp;<t:inputText value="#{fxPackQueryBean.pack.subAreaName}" id="subAreaName" forceId="true"/>											 
										</td>
									</tr>
									<tr>
										<th>收件地址</th>
										<td >
										    <t:inputText value="#{fxPackQueryBean.pack.areaAddress}" id="areaAddress" forceId="true" size="50"/>
										</td>
									</tr>
									<tr>
										<th>收件單位電話</th>
										<td >										    										    
                                            <t:inputText value="#{fxPackQueryBean.pack.subAreaTel}" id="subAreaTel" forceId="true" size="30"/>

										</td>
									</tr>
									<tr>
										<th>退回件/正常件</th>
										<td>
											<h:outputText value="#{fxPackQueryBean.pack.back? '退回件' : '正常件'}"/>
											<t:inputHidden value="#{fxPackQueryBean.pack.back}" />
										</td>
									</tr>
									
									<tr>
										<th>狀態</th>
										<td>
											<h:outputText value="#{fxPackQueryBean.pack.statusNm}"/>
											<t:inputHidden value="#{fxPackQueryBean.pack.statusNm}" /><t:inputHidden value="#{fxPackQueryBean.pack.status}" />
										</td>
									</tr>									
									<tr>
										<th>轄區</th>
										<td valign="middle" bgcolor="#F4FAF3">
                                            <h:outputText value="#{fxPackQueryBean.pack.center}"/>
											<t:inputHidden value="#{fxPackQueryBean.pack.center}" />											 										    
										</td>
									</tr>
									<tr>
										<th>保單數／簽收單數</th>
										<td valign="middle" bgcolor="#F4FAF3">
								            <h:outputText value="#{fxPackQueryBean.pack.books}"/> ／ <h:outputText value="#{fxPackQueryBean.pack.receipts}"/>
											<t:inputHidden value="#{fxPackQueryBean.pack.receipts}" /><t:inputHidden value="#{fxPackQueryBean.pack.books}" />
										</td>
									</tr>

									<tr>
										<th>label掃描時間／人員</th>
										<td valign="middle" bgcolor="#F4FAF3">
											<h:outputText  value="#{fxPackQueryBean.pack.labelScanDate}">
											   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
											</h:outputText> ／ <h:outputText  value="#{fxPackQueryBean.pack.labelScanUser}" />
                                            
										</td>
									</tr>
									<tr>
										<th>簽收單掃描時間／人員</th>
										<td valign="middle" bgcolor="#F4FAF3">
											<h:outputText  value="#{fxPackQueryBean.pack.receiptScanDate}">
											   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
											</h:outputText> ／ <h:outputText  value="#{fxPackQueryBean.pack.receiptScanUser}" />
                                            
										</td>
									</tr>
									<tr>
										<th>保單掃描時間／人員</th>
										<td valign="middle" bgcolor="#F4FAF3">
											<h:outputText  value="#{fxPackQueryBean.pack.policyScanDate}">
											   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
											</h:outputText> ／ <h:outputText  value="#{fxPackQueryBean.pack.policyScanUser}" />
                                            
										</td>
									</tr>
									
								</table>
							</div>
						</td>
					</tr>
					<tr>
						<td colspan="4" align="center">
							<br/>
							<t:commandButton 
								value="確定送出"
								title="確定送出" 
								id="btnSubmit"
								forceId="true"
								type="submit"
								onclick="return(window.confirm('確認送出資料？'))"
								action="#{fxPackQueryBean.updateSave}" />&nbsp;&nbsp;
							<t:commandButton 
								value="取消" 
								title="取消"
								id="btnCancel"
								forceId="true"
								onclick="openUrl('packPrepare.jspx', false);return false;"
								type="submit" />	
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
