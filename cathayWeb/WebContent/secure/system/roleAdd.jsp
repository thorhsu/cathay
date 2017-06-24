<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<script type="text/javascript" src="../../objects/prog/js/lib/jquery.datepick.min.js"></script>
<script type="text/javascript">
var contextPath = '<c:out value="${pageContext.request.contextPath}" />';
$(document).ready(function(){
	$("input[name=firstPage]").each(
			function(){
		       checkSecond(this);
			}
	);
	
});

function checkSecond(obj){
	var checked_status = obj.checked;
	var checkValue = obj.value;
	var starIndex = checkValue.indexOf("*");
	if(starIndex > 0)
	   checkValue = checkValue.substring(0, starIndex);
	if(checked_status){
		$("input[name=secondPage]").each(function()
				{
			        if(this.value.indexOf(checkValue) == 0){
				       $(this).show();
					   this.checked = checked_status;
			        }					   
				});	
		
	}else{
		$("input[name=secondPage]").each(function()
				{
			        if(this.value.indexOf(checkValue) == 0){
			        	this.checked = checked_status;
			        	$(this).hide();
			        }
					   
				});
		
	}
}
function confirmCheck(){
	if(window.confirm('確認新增資料？')){
		$("input[name=firstPage]").each(function(){
			   this.disabled = false
			});	
		return true;
	}else{
        return false;
	}
}


</script>

<script type="text/javascript" src="../../objects/prog/js/sbDate.js"></script>
<link href="../../objects/prog/css/jquery.datepick.css" rel="stylesheet" type="text/css" />
<title>[系統管理&gt;角色新增]</title>
<f:view>
<h:form id="sbForm">
<table width="825" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td align="center" valign="top">
		<table width="825" border="0" align="center" cellpadding="0"
			cellspacing="0" id="table_criteria">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellpadding="0" cellspacing="0"
					class="outline">
					<tr>
						<td colspan="2" class="title">角色新增</td>
					</tr>
					<tr>
						<td colspan="2">
							<div id="divCriteria">
								<table width="100%" border="0" cellpadding="0" cellspacing="1"
									class="content" id="tbCriteria">
									<tr>
										<th>*權限角色</th>
										<td>
											<t:inputText 
												id="userRole" 
												forceId="true" 
												size="20" 
												maxlength="20"
												value="#{authorityMaintainBean.adminPageUserRole.userRole}"
												required="true">
												<f:validateLength minimum="4" maximum="20" />
												<f:validator validatorId="validator.userRole"/>
											</t:inputText>必須以ROLE_開頭，6~20個英數字
											<t:message for="userRole" style="color:red" />
										</td>
										<th>*角色名稱</th>
										<td>
											<t:inputText
												id="userRoleName" 
												forceId="true" 
												size="20" 
												maxlength="20"
												value="#{authorityMaintainBean.adminPageUserRole.userRoleName}"
												required="true"	>
			                                    <f:validator validatorId="validator.ChineseAndEngAndNum"/>
											</t:inputText>20個字元以下
											<t:message for="userRoleName" style="color:red" />
										</td>
									</tr>
									<tr>
										<th>角色描述</th>
										<td>
											<t:inputText 
												id="userRoleDesc" 
												forceId="true" 
												size="20" 
												maxlength="50"
												value="#{authorityMaintainBean.adminPageUserRole.userRoleDesc}" >
 											    <f:validator validatorId="validator.ChineseAndEngAndNum"/>												
											</t:inputText>50個字元以下
											<t:message for="userRoleDesc" style="color:red" />
										</td>
										<th>是否可見</th>
										<td>
											<t:selectOneMenu id="isShow" forceId="true" value="#{authorityMaintainBean.adminPageUserRole.isShow}" >
                                                  <f:selectItem  itemLabel="是" itemValue="1" />
                                                  <f:selectItem  itemLabel="否" itemValue="2" />
                                              </t:selectOneMenu>
										</td>
									</tr>
									<tr>
										<th>只能查詢自己行政中心</th>										
										<td>
											<t:selectOneMenu id="areaIdOnly" forceId="true" value="#{authorityMaintainBean.adminPageUserRole.centerOnly}" >
                                                  <f:selectItem  itemLabel="是" itemValue="1" />
                                                  <f:selectItem  itemLabel="否" itemValue="2" />
                                              </t:selectOneMenu>
										</td>
									</tr>
									<tr>
										<th>第一層功能</th>
										<td valign="top" bgcolor="#F4FAF3">
										     <t:selectManyCheckbox  value="#{authorityMaintainBean.accessPageList1}" id="firstPage" forceId="true" layout="pageDirection"  onclick="checkSecond(this);">
										        <f:selectItems  value="#{authorityMaintainBean.firstPages}" />
										     </t:selectManyCheckbox>										    										
										</td>
										<th>第二層功能</th>
										<td valign="top" bgcolor="#F4FAF3">
										     <div id="secondDiv" >
										        <t:selectManyCheckbox  value="#{authorityMaintainBean.accessPageList2}" id="secondPage" forceId="true" layout="pageDirection" >
										           <f:selectItems  value="#{authorityMaintainBean.secondPages}" />
										        </t:selectManyCheckbox>
										     </div>										    										
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
								onclick="return confirmCheck()"
								action="#{authorityMaintainBean.add}" />&nbsp;&nbsp;
							<t:commandButton 
								value="取消" 
								title="取消"
								id="btnCancel"
								forceId="true"
								onclick="openUrl('rolesMaintain.jspx', false);return false;"
								type="submit" />	
						</td>
					</tr>
				</table>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr><td align="center"><t:outputText id="dataResult" forceId="true" style="color: red" value="#{authorityMaintainBean.result}"/></td></tr>
</table>
</h:form>
</f:view>
