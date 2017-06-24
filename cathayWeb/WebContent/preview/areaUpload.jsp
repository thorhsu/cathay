<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<link href="../../objects/prog/css/jquery.datepick.css" rel="stylesheet" type="text/css" />
<title>[國壽人員功能&gt;服務中心資料上傳]</title>
<f:view>
<h:form id="sbForm" enctype="multipart/form-data">

<table width="825" border="0" cellspacing="0" cellpadding="0">
	<tr><td align="center"><t:outputText id="dataResult" forceId="true" style="color: red" value="#{areaBean.result}"/></td></tr>
	<tr>
		<td align="center" valign="top">
		<table width="825" border="0" align="center" cellpadding="0"
			cellspacing="0" id="table_criteria">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellpadding="0" cellspacing="0"
					class="outline">
					<tr>
						<td class="title">服務中心excel檔上傳</td>
					</tr>
					<tr>
						<td>
							<div id="divCriteria">
								<table width="100%" border="0" cellpadding="0" cellspacing="1"
									class="content" id="tbCriteria">
									<tr>
										<th>選擇上傳excel檔</th>
										<td>
											<t:inputFileUpload 
												id="uploadXls"
												value="#{areaBean.uploadFile}" 
												storage="file"
												maxlength="200000">
												<f:validator validatorId="validator.fileNameValidator"/>
											</t:inputFileUpload>
											<t:message for="uploadXls" style="color:red" />
										</td>
									</tr>
									<tr>
										<th>範例excel檔下載</th>
										<td align="left">											
										    <t:commandLink value="下載全部" action="#{areaBean.downLoadExcel}"  >
										    </t:commandLink>
										    &nbsp;&nbsp;
          	                                                                                       開啟 : (<a href="<h:outputText escape="false" value='#{facesContext.externalContext.request.contextPath}' />/preview/unitAddress.xls" target="new">開啟國壽最初提供Excel</a>)		                                    
										</td>
									</tr>
								</table>
							</div>
						</td>
					</tr>
					<tr>
						<td colspan="4" align="center">
							<br/>
							<t:commandButton value="確定上傳" 
								id="btnModify"
								forceId="true"
								type="submit" 
								action="#{areaBean.upload}"
								onclick="return(window.confirm('確認上傳？'))" />
							&nbsp;&nbsp;
							<t:commandButton value="所有服務中心" id="btnQuery" forceId="true"
							   type="submit" action="#{areaBean.doQuery}" />
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
			    <td align="center">
			       <t:dataTable value="#{areaBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="area" 
										binding="#{areaBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="odd_row,even_row" 
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<t:outputText value="中心代碼 "></t:outputText>
										</f:facet>
										<t:outputText value="#{area.areaId}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="中心名稱"></t:outputText>
										</f:facet>
										<t:outputText value="#{area.areaName}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="郵遞區號"></t:outputText>
										</f:facet>
										<t:outputText value="#{area.zipCode}">
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="住址"></t:outputText>
										</f:facet>
										<t:outputText value="#{area.address}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="電話"></t:outputText>
										</f:facet>
										<t:outputText value="#{area.tel}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="服務中心"></t:outputText>
										</f:facet>
										<t:outputText value="#{area.serviceCenter}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="獨立科"></t:outputText>
										</f:facet>
										<t:outputText value="#{area.independent}" />
									</t:column>
							</t:dataTable>
			    </td>
			</tr>
		</table>
		</td>
	</tr>
	
</table>
</h:form>
</f:view>
