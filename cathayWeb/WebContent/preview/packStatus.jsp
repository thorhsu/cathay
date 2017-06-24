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
       left: 50%;
    
       margin-left: -360px;
       width: 750px;
       height: 600px;
    
       background-color: #EEE;
       color: #333;
       border: 1px solid black;
       padding: 12px;
       overflow: scroll;
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
    var focusDecider = '<h:outputText escape="false" value="#{packStatusBean.focusDecider}" />';
    var result = '<h:outputText escape="false" value="#{packStatusBean.result}" />';
	var fileName = "";
	var dialog;
	var form;
	$().ready(function(){
		form = $( "form" );
		$('#dialog-form').jqm();
		
        var jqmShow = <h:outputText escape="false" value="#{packStatusBean.showPolicy}" />;
        if(jqmShow)
           $('#dialog-form').jqmShow();
        else
		   $('#dialog-form').jqmHide();
		$('#showJqm').val("false");
	});
  
</script>	
	<link rel="stylesheet" type="text/css" media="screen"
		href="../objects/prog/css/themes/ui.jqgrid.css" />	

	<title>[國壽人員功能&gt;裝箱記錄查詢]</title>

	<h:form id="sbForm">
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">裝箱寄送記錄</th>
					</tr>

					<tr>
						<td align="center">
						<table width="100%" border="0" cellspacing="1" cellpadding="1"
							 bgcolor="#006F3F">
							<tr>
								<td align="center">
								<table id="input_criteria" width="1000" border="0" cellspacing="1" cellpadding="6"
									bgcolor="#4AAE66" >
									<tr>
										<td width="90" align="center" bgcolor="#DFF4DD">*Cycle Date區間</td>
										<td   bgcolor="#F4FAF3" align="left">
										    <t:inputText 
											  id="dtSelect1" value="#{packStatusBean.cycleDateBegin}"
											  size="7" maxlength="10" required="true">
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect1" style="color:red" /> 
										   ~
										   <t:inputText 
											  id="dtSelect2" value="#{packStatusBean.cycleDateEnd}"
											  size="7" maxlength="10" required="true">
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect2" style="color:red" />
										</td>
										<td width="90" align="center" bgcolor="#DFF4DD">行政中心</td>
										<td bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="center" forceId="true" value="#{packStatusBean.center}" >
                                                  <f:selectItems  value="#{packStatusBean.centers}" />                                                  
                                             </t:selectOneMenu>										   
										</td>
										<td width="90" align="center" bgcolor="#DFF4DD">個險／團險</td>
										<td bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="group" forceId="true" value="#{packStatusBean.group}" >
										          <f:selectItem   itemLabel="個險" itemValue="#{false}" />
                                                  <f:selectItem   itemLabel="團險" itemValue="#{true}" />                                                                                                   
                                             </t:selectOneMenu>										   
										</td>
										
									</tr>
									<tr>
									    <td width="90" align="center" bgcolor="#DFF4DD">服務中心</td>
										<td bgcolor="#F4FAF3" align="left" >
										   <t:inputText 
											  id="name" value="#{packStatusBean.name}"
											  size="10" >											  
										   </t:inputText>
										</td>
										<td width="90" align="center" bgcolor="#DFF4DD">服務中心電話</td>
										<td  bgcolor="#F4FAF3" align="left" >
										   <t:inputText 
											  id="tel" value="#{packStatusBean.tel}"
											  size="12" >											  
										   </t:inputText>
										</td>
										<td width="90" align="center" bgcolor="#DFF4DD">寄送地址</td>
										<td bgcolor="#F4FAF3" align="left">										           
										    <t:inputText 
											  id="address" value="#{packStatusBean.address}"
											  size="50" >											  
										   </t:inputText> 
										   <h:message for="address" style="color:red" /> 
										</td>
									</tr>
									<tr>
										<td width="90" align="center" bgcolor="#DFF4DD">超峰/掛號號碼</td>
										<td  bgcolor="#F4FAF3" align="left" >
										   <t:inputText 
											  id="vendorId" value="#{packStatusBean.vendorId}"
											  size="12" >											  
										   </t:inputText>
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
						   <t:commandButton value="送出" id="doQuery" forceId="true"
							   type="submit" action="#{packStatusBean.doQuery}" />
						   &nbsp;&nbsp;&nbsp;
							   
						   <t:commandButton value="產出貨運清單" id="produceLogistic" forceId="true"
							   type="submit" action="#{packStatusBean.exportLogistic}" />
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
					style="color: red" value="#{packStatusBean.result}" /></td>
			</tr>
			<tr>
			   <td></td>
			</tr>
			<tr>
			   
			</tr>
			<tr>			    
			    <td align="center">
			       <t:dataTable value="#{packStatusBean.resultString}"
										id="dataList1" 
										forceId="true" 
										var="packSummary" 										
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="even_row,odd_row"
										rendered = "#{packStatusBean.dataModel != null}" 
										width="90%" renderedIfEmpty="false">
							<t:column >
								<f:facet name="header">
									 <t:outputText value="類別"></t:outputText>
								</f:facet>
								<t:outputText value="#{packSummary[0]}" />
							</t:column>
							<t:column >
								<f:facet name="header">
									 <t:outputText value="總印製（保單本數）"></t:outputText>
								</f:facet>
								<t:commandLink value="#{packSummary[1]}" action="#{packStatusBean.policyBooks}"  >								    
									<f:param name="cycleDateBegin" value="#{packStatusBean.cycleDateBegin}" />									   
									<f:param name="cycleDateEnd" value="#{packStatusBean.cycleDateEnd}" />
									<f:param name="center" value="#{packStatusBean.center}" />
									<f:param name="group" value="#{packStatusBean.group}" />
									<f:param name="sourceCode" value="#{packSummary[0]}" />
									<f:param name="delivery" value="all" />																											
								</t:commandLink>																	
							</t:column>
							<t:column >
								<f:facet name="header">
									 <t:outputText value="郵寄／貨運（保單本數）"></t:outputText>
								</f:facet>
								<t:commandLink value="#{packSummary[2]}" action="#{packStatusBean.policyBooks}"  >								    
									<f:param name="cycleDateBegin" value="#{packStatusBean.cycleDateBegin}" />									   
									<f:param name="cycleDateEnd" value="#{packStatusBean.cycleDateEnd}" />
									<f:param name="center" value="#{packStatusBean.center}" />
									<f:param name="group" value="#{packStatusBean.group}" />
									<f:param name="sourceCode" value="#{packSummary[0]}" />
									<f:param name="delivery" value="yes" />																											
								</t:commandLink>
							</t:column>
							<t:column >
								<f:facet name="header">
									 <t:outputText value="未交寄（保單本數）"></t:outputText>
								</f:facet>
								<t:commandLink value="#{packSummary[3]}" action="#{packStatusBean.policyBooks}"  >								    
									<f:param name="cycleDateBegin" value="#{packStatusBean.cycleDateBegin}" />									   
									<f:param name="cycleDateEnd" value="#{packStatusBean.cycleDateEnd}" />
									<f:param name="center" value="#{packStatusBean.center}" />
									<f:param name="group" value="#{packStatusBean.group}" />
									<f:param name="sourceCode" value="#{packSummary[0]}" />																											
									<f:param name="delivery" value="no" />
								</t:commandLink>
							</t:column>
				   </t:dataTable>
			    
			       <t:dataTable value="#{packStatusBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="logisticStatus" 
										binding="#{packStatusBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="even_row,odd_row" 
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<t:outputText value="裝箱代碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.logisticId}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="#{packStatusBean.center == '06'? '掛號號碼' : '超峰號碼'}" ></t:outputText>
											
										</f:facet>
										<t:outputText value="#{logisticStatus.vendorId}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="完成交寄"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.packDone ? '是' : '否'}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="#{(packStatusBean.center == '06' && !packStatusBean.group)? '收件人' : '服務中心'}"></t:outputText>
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
									<t:column rendered="#{packStatusBean.center != '06' || packStatusBean.group}">
										<f:facet name="header">
											<t:outputText value="單位數"></t:outputText>
										</f:facet>
										<t:outputText value="#{logisticStatus.packs}" />
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
		<div id="dialog-form" class="jqmWindow" title="保單資訊">          
             <table id="input_criteria" >
                <tr>
			       <td align="center">
                     <t:dataTable value="#{packStatusBean.policyDataModel}"
										id="dataList3" 
										forceId="true" 
										var="applyData" 
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="even_row,odd_row" 
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<t:outputText value="受理號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.applyNo}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="保單號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.policyNos}">
										</t:outputText>
									</t:column>									
									<t:column>
										<f:facet name="header">
											<t:outputText value="要保人"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.recName}">
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="行政中心"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.centerName}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="來源類別"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.sourceName}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.policyStatusName}" />
									</t:column>									
									<t:column>
										<f:facet name="header">
											<t:outputText value="Cycel Date"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.cycleDate}">
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />	
										</t:outputText>
									</t:column>																										

							</t:dataTable>
					  </td>
				 </tr>
             </table>          
        </div>
        <t:inputHidden value="#{packStatusBean.showPolicy}" id='showJqm' forceId="true" />
	</h:form>
</f:view>
