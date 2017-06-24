<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
	$().ready(function(){
		
	});
	
	function clearColumn(){
	}
	
	
	
  
</script>
	
	<title>[國壽人員功能&gt;迴歸測試結果]</title>

	<h:form id="sbForm">
		<table width="70%" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">迴歸測試結果</th>
					</tr>

					<tr>
						<td align="center" height="10"></td>
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
					style="color: red" value="#{unlockBean.result}" /></td>
			</tr>
			    
			<tr>				
			    <td align="center">			       
			       <t:dataTable value="#{unlockBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="errorReport" 
										binding="#{unlockBean.dataTable}"
										columnClasses="col_center,col_center,col_center" 																				
										rowClasses="odd_row,even_row" 
										width="100%" renderedIfEmpty="false">
									
									<t:column >
										<f:facet name="header">
											<t:outputText value="發生時間"></t:outputText>
										</f:facet>
										<t:outputText value="#{errorReport.errHappenTime}" >
										   <f:convertDateTime pattern="yyyy/MM/dd HH:mm" timeZone="GMT+8" />
										</t:outputText>
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="內容"></t:outputText>
										</f:facet>
										<t:outputText value="#{errorReport.messageBody}">
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="已email通報"></t:outputText>
										</f:facet>
										<t:outputText value="#{errorReport.reported}" />
									</t:column>									
							</t:dataTable>
			    </td>
			</tr>	
			<tr>
			    <td colspan="4" align="center">
						<br/>
						<t:commandButton value="刪除記錄 " 
							type="submit" 
							action="#{unlockBean.deleteRecord}"
							onclick="return(window.confirm('刪除記錄？'))" />
						&nbsp;&nbsp;&nbsp;&nbsp;
						<t:commandButton value="確定解鎖 " 
							type="submit" 
							action="#{unlockBean.unlock}"
							onclick="return(window.confirm('確認解鎖並刪除記錄？'))" />
						&nbsp;&nbsp;&nbsp;&nbsp;
						<t:commandButton value="重置迴歸測試" 
							type="submit" 
							action="#{unlockBean.resetUnlock}"
							onclick="return(window.confirm('此動作將重新產生迴歸測試比對檔，確認進行？'))" />
					</td>
			</tr>			
			
		</table>
		
	</h:form>
</f:view>
