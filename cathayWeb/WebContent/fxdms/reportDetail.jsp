<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
<script src="../objects/prog/js/plugins/jquery.browser.js"
		type="text/javascript"></script>    	
<script type="text/javascript" src="../objects/prog/js/sbDate.js"></script>
<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
	$().ready(function(){
		
	});
	
</script>
	
	<title>[FXDMS人員功能&gt;系統進度記錄]</title>

	<h:form id="sbForm">
		<table width="70%" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">系統進度記錄</th>
					</tr>
					<tr>
						<td align="center">
						
								<table id="input_criteria" width="1000" border="0" cellspacing="1" cellpadding="6"
									bgcolor="#4AAE66" align="center">
									<tr>
										<td width="50%" align="right" bgcolor="#DFF4DD">查詢區間</td>
										<td bgcolor="#DFF4DD" align="left">
										    <t:inputText 
											  id="dtSelect1" value="#{reportDetailBean.startDate}"
											  size="6" maxlength="10">
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect1" style="color:red" /> ~ 
										   <t:inputText 
											   id="dtSelect2" value="#{reportDetailBean.endDate}" size="6"
											   maxlength="10">
											   <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											   <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect2" style="color:red" />
										</td>
							        </tr>
							   </table>
					           

					  </td>
					</tr>
					<tr>
						<td align="center" height="10"></td>
					</tr>
					<tr>
						<td  align="center"><br />						   
						   <t:commandButton value="查詢" id="btnQuery" forceId="true"
							   type="submit" action="#{reportDetailBean.query}" />							
						</td>
					</tr>
				</table>
				</td>
			</tr>
			<tr>
				<td height="30">
				<hr align="center" width="90%"
					style="border: 1px; border: 1px solid #DFF4DD;" />
				</td>
			</tr>
			<tr>
				<td align="center"><t:outputText id="dataResult" forceId="true"
					style="color: red" value="#{reportDetailBean.result}" /></td>
			</tr>
			    
			<tr>				
			    <td align="center">			       
			       <t:dataTable value="#{reportDetailBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="errorReport" 
										binding="#{reportDetailBean.dataTable}"
										columnClasses="col_center,col_center,col_center" 																				
										rowClasses="odd_row,even_row" 
										width="100%" renderedIfEmpty="false">
									
									<t:column >
										<f:facet name="header">
											<t:outputText value="時間"></t:outputText>
										</f:facet>
										<t:outputText value="#{errorReport.errHappenTime}" >
										   <f:convertDateTime pattern="yyyy/MM/dd HH:mm" timeZone="GMT+8" />
										</t:outputText>
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="記錄類別"></t:outputText>
										</f:facet>
										<t:outputText value="#{errorReport.errorType}">
										</t:outputText>
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="標題"></t:outputText>
										</f:facet>
										<t:outputText value="#{errorReport.title}">
										</t:outputText>
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="內容"></t:outputText>
										</f:facet>
										<t:outputText value="#{errorReport.messageBody}">
										</t:outputText>
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="異常"></t:outputText>
										</f:facet>
										<t:outputText value="#{errorReport.exception}">
										</t:outputText>
									</t:column>
							</t:dataTable>
			    </td>
			</tr>	
						
			
		</table>
		
	</h:form>
</f:view>
