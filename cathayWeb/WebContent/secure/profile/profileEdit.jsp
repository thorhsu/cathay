<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<script type="text/javascript" src="../../objects/prog/js/lib/jquery.datepick.min.js"></script>
<link href="../../objects/prog/css/jquery.datepick.css" rel="stylesheet" type="text/css" />
<title>[系統管理&gt;密碼修改]</title>
<script type="text/javascript" src="<c:out value='${pageContext.request.contextPath}' />/objects/prog/js/pwdComplexCheck.js"></script>
<script type="text/javascript">
function testPwd(){
	var testResult = testPassword("password", "passwordConfirm"); 
	if(testResult){
		return window.confirm('確認修改資料？');
	}else{	
		return false;
	}
}
function checkPwd(column, pwd){
	var text = testComplexity(pwd);
	if(text != "OK"){
		text = "密碼強度不足";
	}else{
		text = "密碼強度OK";
	}
	if(column == 1)
	   $("#displayTxt1").html(text);
	else if(column == 2)
	   $("#displayTxt2").html(text);   
	
}

</script>
<f:view>
<h:form id="sbForm">
<%//初始化adminUser %>
<t:outputText id="initResult" forceId="true" value="#{profileEditBean.initResult}" style="display: none"/>
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
						<td class="title">密碼修改</td>
					</tr>
					<tr>
						<td>
							<div id="divCriteria">
								<table width="100%" border="0" cellpadding="0" cellspacing="1"
									class="content" id="tbCriteria">
									<tr>
										<th>帳號</th>
										<td>
											<t:outputText
												id="userId" 
												forceId="true" 
												value="#{profileEditBean.userId}"/>
										</td>
									</tr>
									<tr>
										<th>密碼</th>
										<td>
											<t:inputSecret
												id="password" 
												forceId="true" 
												size="20" 
												maxlength="20"
												value="#{profileEditBean.password}"
												redisplay="true"
												onkeydown="checkPwd(1, this.value)"
												required="true">
												<f:validateLength minimum="6" maximum="20" />
											</t:inputSecret> 6~20個字元。
											<div id="displayTxt1"></div>
											<t:message for="password" style="color:red" />
										</td>
									</tr>
									<tr>
										<th>確認密碼</th>
										<td>
											<t:inputSecret 
												id="passwordConfirm" 
												forceId="true" 
												size="20" 
												maxlength="20"
												value="#{profileEditBean.passwordConfirm}"
												redisplay="true"
												onkeydown="checkPwd(2, this.value)"
												required="true">
												<f:validateLength maximum="20" />
											</t:inputSecret> 請再輸入一次密碼。
											<div id="displayTxt2"></div>
											<t:message for="passwordConfirm" style="color:red" />
										</td>
									</tr>
									<tr>
										<th>姓名</th>
										<td>
											<t:inputText 
												id="userName"
												value="#{profileEditBean.userName}" 
												size="20" 
												maxlength="20">
												<f:validator validatorId="validator.ChineseAndEngAndSpace"/>
												<f:validateLength maximum="10" />
											</t:inputText>
											<t:message for="userName" style="color:red" />
										</td>
									</tr>
									<tr>
										<th>帳號權限</th>
										<td>
											<t:outputText
												id="userRoleName" 
												forceId="true" 
												value="#{profileEditBean.userRoleName}"/>
										</td>
									</tr>
									<tr>
										<th>帳號權限說明</th>
										<td>
											<t:outputText
												id="userRoleDesc" 
												forceId="true" 
												value="#{profileEditBean.userRoleDesc}"/>
										</td>
									</tr>
									<tr>
										<th>帳號起訖日期</th>
										<td>
											<t:outputText 
												id="dtSelect1"
												value="#{profileEditBean.enableStart}" 
												forceId="true" />
											 ~ 
											<t:outputText 
												id="dtSelect2" 
												value="#{profileEditBean.enableEnd}" 
												forceId="true" />
										</td>
									</tr>
									<tr>
										<th>建立者</th>
										<td>
											<t:outputText 
												id="insertUser" 
												forceId="true" 
												value="#{profileEditBean.insertUser}" />
										</td>
									</tr>
									<tr>
										<th>建立時間</th>
										<td>
											<t:outputText 
												id="insertDate" 
												forceId="true" 
												value="#{profileEditBean.insertDate}" >
												<f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd HH:mm:ss"/>
											</t:outputText>
										</td>
									</tr>
									<tr>
										<th>最後異動者</th>
										<td>
											<t:outputText id="updateUser" forceId="true" 
												value="#{profileEditBean.updateUser}" />
										</td>
									</tr>
									<tr>
										<th>最後異動時間</th>
										<td>
											<t:outputText id="updateDate" forceId="true" 
												value="#{profileEditBean.updateDate}" >
												<f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd HH:mm:ss"/>
											</t:outputText>
										</td>
									</tr>
								</table>
							</div>
							
						</td>
					</tr>
					<tr>
						<td colspan="4" align="center">
							<br/>
							<t:commandButton value="確定修改" 
								id="btnModify"
								forceId="true"
								type="submit" 
								action="#{profileEditBean.modify}"
								onclick="return(testPwd())" />&nbsp;&nbsp;
							<t:commandButton 
								value="取消" 
								id="btnCancel"
								forceId="true"
								onclick="if(window.confirm('確認取消修改資料？')){openUrl('../index.jspx', false)};return false"
								type="submit" />
						</td>
					</tr>
				</table>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr><td align="center"><t:outputText id="dataResult" forceId="true" style="color: red" value="#{profileEditBean.result}"/></td></tr>
</table>
</h:form>
</f:view>
