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
	var clearColumns = function(){
		document.getElementById("sbForm:dtSelect1").value = "";
		document.getElementById("sbForm:dtSelect2").value = "";
		document.getElementById("center").value = "";
		document.getElementById("applyNo").value = "";
		document.getElementById("policyNo").value = "";
		document.getElementById("insureId").value = "";
		document.getElementById("recName").value = "";
		document.getElementById("status").value = "true";
		document.getElementById("policyOrReceipt").value = "policy";		
	};
  
</script>
	
	<title>國壽人員功能&gt;抽件／驗單狀態查詢</title>

	<h:form id="sbForm">
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">抽件／驗單狀態查詢</th>
					</tr>

					<tr>
						<td align="center">
						<table width="100%" border="0" cellspacing="0" cellpadding="0"
							align="center" bgcolor="#006F3F">
							<tr>
								<td>
								<table id="input_criteria" width="800" border="0" cellspacing="1" cellpadding="6"
									bgcolor="#4AAE66" align="center">
									<tr>
										<td width="100" align="center" bgcolor="#DFF4DD">*Cycle Date區間</td>
										<td bgcolor="#F4FAF3" align="left">
										    <t:inputText 
											  id="dtSelect1" value="#{checkResultQueryBean.startDate}"
											  size="8" maxlength="10"  required="true">
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect1" style="color:red" /> ~ 
										   <t:inputText 
											   id="dtSelect2" value="#{checkResultQueryBean.endDate}" size="8"
											   maxlength="10" required="true">
											   <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											   <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect2" style="color:red" />
										</td>
										<td width="100" align="center" bgcolor="#DFF4DD">行政中心</td>
										<td  bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="center" forceId="true" value="#{checkResultQueryBean.center}" >
                                                  <f:selectItems  value="#{checkResultQueryBean.myCenters}" />                                                  
                                             </t:selectOneMenu>
										   
										</td>
									</tr>
									<tr>
										<td align="center" bgcolor="#DFF4DD">受理編號</td>
										<td  bgcolor="#F4FAF3" align="left">
										   <t:inputText id="applyNo" forceId="true"
											   value="#{checkResultQueryBean.applyNo}" size="40" maxlength="10" >								            
										   </t:inputText> 										   
										</td>
										<td align="center" bgcolor="#DFF4DD">保單號碼</td>
										<td  bgcolor="#F4FAF3" align="left" >
										   <t:inputText id="policyNo" forceId="true"
											   value="#{checkResultQueryBean.policyNo}" size="40" maxlength="12" >								            
										   </t:inputText> 
										   
										</td>
									</tr>																	
									<tr>
										<td align="center" bgcolor="#DFF4DD">被保人ID</td>
										<td width="120" bgcolor="#F4FAF3" align="left" >
										   <t:inputText id="insureId" forceId="true"
											  value="#{checkResultQueryBean.insureId}" size="40" maxlength="15" >
										   </t:inputText> 										   
										</td>
										<td align="center" bgcolor="#DFF4DD">要保人</td>
										<td width="120" bgcolor="#F4FAF3" align="left" >
										     <t:inputText id="recName" forceId="true"
											   value="#{checkResultQueryBean.recName}" maxlength="15" />
										</td>										
									</tr>
									<tr>
										<td align="center" bgcolor="#DFF4DD">處理狀態</td>
										<td width="120" bgcolor="#F4FAF3" align="left" >
										     <t:selectOneMenu id="status" forceId="true" value="#{checkResultQueryBean.exception}" >
										          <f:selectItem   itemLabel="驗單失敗" itemValue='true'/>                                                                                                    
                                                  <f:selectItem   itemLabel="驗單通過" itemValue='false'/>                                                                                                   
                                                  <f:selectItem   itemLabel="免印製" itemValue='28'/>
                                                  <f:selectItem   itemLabel="尚未驗單" itemValue='40'/>                                                  
                                              </t:selectOneMenu>										    
										</td>
										<td align="center" bgcolor="#DFF4DD">保單/簽收單</td>
										<td width="120" bgcolor="#F4FAF3" align="left" >
										     <t:selectOneMenu id="policyOrReceipt" forceId="true" value="#{checkResultQueryBean.policyOrReceipt}" >                                                                                                    
                                                  <f:selectItem   itemLabel="保單" itemValue='policy'/>
                                                  <f:selectItem   itemLabel="簽收單" itemValue='receipt'/>                                                  
                                                  <f:selectItem   itemLabel="團險證" itemValue='card'/>
                                                  <f:selectItem   itemLabel="全部" itemValue='all'/>                                                  
                                              </t:selectOneMenu>										    
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
							   type="submit" action="#{checkResultQueryBean.queryCheckResult}" />
						   <input type="button" value="清除" onclick="clearColumns()"/>
						   &nbsp;
						   <t:commandButton value="輸出excel" forceId="true"
							   type="submit" action="#{checkResultQueryBean.exportExcel}" />
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
					style="color: red" value="#{checkResultQueryBean.result}" /></td>
			</tr>
			<tr>
			    <td align="center">
			       <t:dataTable value="#{checkResultQueryBean.dataModel}"
										id="dataList" 
										forceId="true" 
										rows="#{checkResultQueryBean.pageRows}"
										var="applyData" 
										binding="#{checkResultQueryBean.dataTable}"
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
											<t:outputText value="ID"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.insureId}">
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
											<t:outputText value="狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.policyStatusName}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="驗單失敗理由"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.verifyResult}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="退回審查科"></t:outputText>
										</f:facet>
										<t:outputText value="#{(applyData.backToVerify and applyData.packId != null and applyData.policyStatus == '100')? '已退回': ''}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="Cycel Date"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.cycleDate}">
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />	
										</t:outputText>
									</t:column>
																										
									<t:column>
										<f:facet name="header">
											<t:outputText value="簽收單/保單"></t:outputText>
										</f:facet>
										<t:outputText value="#{(applyData.receipt)? '簽收單' : '保單'}" rendered="#{!applyData.groupInsure}"/>										
										<t:outputText value="保險證" rendered="#{applyData.groupInsure}"/>
									</t:column>																		
									
							</t:dataTable>
			    </td>
			</tr>
            <tr>
			    	<td align="center">
								<br />
								<t:dataScroller id="info_scroller" for="dataList"
									rowsCountVar="rowsCount"
									displayedRowsCountVar="displayedRowsCountVar"
									firstRowIndexVar="firstRowIndex" lastRowIndexVar="lastRowIndex"
									pageCountVar="pageCount" pageIndexVar="pageIndex">
									<h:outputFormat value="顯示第{0}~{1}筆資料，共{2}筆，{3}/{4}頁">
										<f:param value="#{firstRowIndex}" />
										<f:param value="#{lastRowIndex}" />
										<f:param value="#{rowsCount}" />										
										<f:param value="#{pageIndex}" />
										<f:param value="#{pageCount}" />										
									</h:outputFormat>
									<%
										//For css style判斷
									%>
									<t:outputText id="currPageIndex" forceId="true"
										value="#{pageIndex}" style="display: none" />
									<t:outputText id="lastPageIndex" forceId="true"
										value="#{pageCount}" style="display: none" />
								</t:dataScroller>
							</td>
						</tr>
						<tr>
							<td align="center">
								<br/>
								<t:dataScroller id="data_scroller"
									for="dataList" 
									fastStep="10" 
									paginator="true"
									displayedRowsCountVar="displayedRowsCountVar"
									paginatorMaxPages="9"
									paginatorTableStyle="text-align:center; margin-left:auto; margin-right:auto;"
									paginatorActiveColumnStyle="font-weight:bold;font-size:13px;color: #333333;"
									renderFacetsIfSinglePage="false">
									<f:facet name="first">
										<t:outputText id="firstPage" forceId="true" value="第一頁" />
									</f:facet>
									<f:facet name="last">
										<t:outputText id="lastPage" forceId="true" value="最末頁" />
									</f:facet>
									<f:facet name="previous">
										<t:outputText id="previousPage" forceId="true" value="上一頁" />
									</f:facet>
									<f:facet name="next">
										<t:outputText id="nextPage" forceId="true" value="下一頁" />
									</f:facet>
								</t:dataScroller>
					    	</td>
					    </tr>
				
		</table>
		
	</h:form>
</f:view>
