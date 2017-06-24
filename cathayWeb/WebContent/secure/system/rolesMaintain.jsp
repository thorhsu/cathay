<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<script type="text/javascript" src="../../objects/prog/js/lib/jquery.datepick.min.js"></script>
<script type="text/javascript" src="../../objects/prog/js/sbDate.js"></script>
<link href="../../objects/prog/css/jquery.datepick.css" rel="stylesheet" type="text/css" />

<title>[系統管理&gt;權限角色管理]</title>
<f:view>
<h:form id="sbForm">
<table width="825" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td align="center" valign="top">
		<table width="100%" border="0" cellspacing="0" cellpadding="0"
			id="table_criteria">
			<tr>
				<th class="title">權限角色維護查詢</th>
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
								<td align="center" bgcolor="#DFF4DD">角色</td>
								<td width="300" bgcolor="#F4FAF3" align="left">
									<t:inputText 
										id="userRole" 
										forceId="true"
										value="#{authorityMaintainBean.userRole}" 
										size="20" 
										maxlength="20" />
								</td>
								<td align="center" bgcolor="#DFF4DD">是否可見</td>
								<td valign="middle" bgcolor="#F4FAF3">
									<t:selectOneMenu 
										id="isShow" 
										forceId="true"
										value="#{authorityMaintainBean.isShow}">
										<f:selectItem itemValue="" itemLabel="全部" />
										<f:selectItem itemValue="1" itemLabel="是" />
										<f:selectItem itemValue="0" itemLabel="否" />
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
				<td colspan="4" align="center">
					<br/>
					<t:commandButton 
						value="查詢" 
						id="btnQuery"
						forceId="true"
						type="submit" 
						action="#{authorityMaintainBean.query}" 
						actionListener="#{authorityMaintainBean.resetDataScroller}"/>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr><td height="30"><hr align="center" width="90%" style="border: 1px; border: 1px solid #DFF4DD;"/></td></tr>
	<tr><td align="center">
		<t:outputText id="dataResult" forceId="true" style="color: red" value="#{authorityMaintainBean.result}"/>
	</td></tr>	
<tr>
	<td>
		<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" id="table_show">
		  	<tr>
		    	<td>
		    		<table width="100%" border="0" cellpadding="0" cellspacing="0" class="outline">
		      			<tr>
		        			<td class="title" colspan="2">查詢結果</td>
		      			</tr>
		    
					    <tr>
					    	<td align="left" height="30" valign="top">
					    	<t:commandButton value="新增" id="btnAdd" forceId="true" type="submit" 
						              action="#{authorityMaintainBean.toAdd}" />
					    	
								<!-- input type="submit"
					    			title="新增"
					    			value="新增" 
									id="btnAdd" 
									onclick="openUrl('roleAdd.jspx'); return false"/ -->	
								<t:commandButton
										id="btnDel" 
										forceId="true"
										onclick="return window.confirm('確認刪除資料？')"
										action="#{authorityMaintainBean.del}"
										value="刪除勾選資料"
										title="刪除勾選資料">
								</t:commandButton>
					    	</td>
					    </tr>
					    <tr>
					        <td>
								<t:dataTable value="#{authorityMaintainBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="adminRole" 
										rows="20"
										binding="#{authorityMaintainBean.dataTable}"
										styleClass="content"
										columnClasses="col_center,,col_center,col_center,col_center"
										rowClasses="odd_row,even_row" 
										cellspacing="0" 
										cellpadding="0"
										border="0">
									<t:column>
										<f:facet name="header">
											<f:verbatim>
												<input type="checkbox" id="cbSelAll" name="cbSelAll" onclick="selAllCb(this, '#table_show');" />
											</f:verbatim>
										</f:facet>
										<t:selectBooleanCheckbox id="cbSelOne" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="角色代號"></t:outputText>
										</f:facet>
										<h:commandLink action="#{authorityMaintainBean.toEdit}">
											<h:outputText value="#{adminRole.userRole}" />
											<f:param name="adminUserRole" value="#{adminRole.userRole}"/>
										</h:commandLink>
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="角色名稱"></t:outputText>
										</f:facet>
										<t:outputText value="#{adminRole.userRoleName}">
										</t:outputText>
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="角色描述"></t:outputText>
										</f:facet>

										<t:outputText value="#{adminRole.userRoleDesc}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="狀態"></t:outputText>
										</f:facet>
										<t:outputText value='#{adminRole.isShow == "1" ? "可見" : "不可見"}' />
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
		    	</td>
		  	</tr>
		</table>
	</td>
</tr>
</table>
</h:form>
</f:view>
