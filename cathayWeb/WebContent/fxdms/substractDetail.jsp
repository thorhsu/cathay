<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
	$().ready(function(){
		$("#uniqueNo").select();
		
	});	
	
	
  
</script>
	
	<title>[FX人員功能&gt;未送回國壽抽件列表]</title>

	<h:form id="sbForm">
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				   <table width="100%" border="0" cellspacing="0" cellpadding="0"
					   id="table_criteria">
					   <tr>
						   <th class="title">未送回國壽抽件列表</th>
					   </tr>
					   <tr>
						   <td align="center">
						      <table width="100%" border="0" cellspacing="1" cellpadding="1"
							    align="center" bgcolor="#006F3F">
					              <tr>
				                    <td align="center" bgcolor="#DFF4DD">保單右上角號碼</td>
				                    <td bgcolor="#F4FAF3" align="left">
				                        <t:inputText forceId="true" id="uniqueNo" required="true"
						                   value="#{substractBean.uniqueNo}" size="20" maxlength="20" />
						                <h:message for="uniqueNo" style="color:red" /> 
				                    </td>
				                  </tr>
				                  <tr>
				                    <td align="center" bgcolor="#DFF4DD">抽件要求人姓名</td>
				                    <td bgcolor="#F4FAF3" align="left">
				                        <t:inputText forceId="true" id="substractModifiderName" required="true"
						                   value="#{substractBean.substractModifiderName}" size="20" maxlength="20" />
						                <h:message for="substractModifiderName" style="color:red" /> 
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
				    <t:commandButton value="抽件"  
					    type="submit" action="#{substractBean.doSubmit}" />							
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
					style="color: red" value="#{substractBean.result}" /></td>
			</tr>
			<tr>
			    <td align="center">
			       <t:dataTable value="#{substractBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="applyData" 
										binding="#{substractBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="#{substractBean.rowClass}" 
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
									<t:column >
										<f:facet name="header">
											<t:outputText value="右上角编號 "></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.uniqueNo}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="要保人"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.recName}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="來源類別"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.sourceName}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="抽件設定人員 "></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.substractModifiderName}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="抽件設定時間"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.substractModifiderTime}" >
										   <f:convertDateTime pattern="yyyy/MM/dd HH:mm" timeZone="GMT+8" />
										</t:outputText>										
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="目前狀態"></t:outputText>
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
		
	</h:form>
</f:view>
