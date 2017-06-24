<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<script type="text/javascript" src="../../objects/prog/js/lib/jquery.datepick.min.js"></script>

<script type="text/javascript" src="../../objects/prog/js/sbDate.js"></script>
<link href="../../objects/prog/css/jquery.datepick.css" rel="stylesheet" type="text/css" />
<title>[系統管理&gt;帳號新增]</title>
<f:view>
<script lang="text/javascript">
    var allRoles = ${adminUserEditBean.roleListJson};
    function changeRoles() {
  	  //變更客戶聯絡人
	      $("#center > option" ).remove();	      
	      allRoles.forEach(function(element){			  			  
	    	  // 北一；02 台中；03 高雄；04台南；05 桃竹；06 北二 
		     if($( "#selUserRole" ).val() === element.userRole && element.centerOnly === "1"){				  
			     $("#center").append($("<option ></option>").attr("value", "01").text("北一"));				
			     $("#center").append($("<option ></option>").attr("value", "02").text("台中"));
			     $("#center").append($("<option ></option>").attr("value", "03").text("高雄"));				
			     $("#center").append($("<option ></option>").attr("value", "04").text("台南"));
			     $("#center").append($("<option ></option>").attr("value", "05").text("桃竹"));				
			     $("#center").append($("<option ></option>").attr("value", "06").text("北二"));
		      }else if($( "#selUserRole" ).val() === element.userRole ){				  
				 $("#center").append($("<option ></option>").attr("value", "").text("無"));								     
			  }
	      });		 	      
    }
    $().ready(function() {
    	$( "#selUserRole" )
	     .change(function(){
	    	 changeRoles();
	     }).change();
    });
</script>
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
						<td colspan="2" class="title">帳號新增</td>
					</tr>
					<tr>
						<td colspan="2">
							<div id="divCriteria">
								<table width="100%" border="0" cellpadding="0" cellspacing="1"
									class="content" id="tbCriteria">
									<tr>
										<th>帳號</th>
										<td>
											<t:inputText 
												id="userId" 
												forceId="true" 
												size="20" 
												maxlength="20"
												value="#{adminUserAddBean.userId}"
												required="true">
												<f:validateLength minimum="4" maximum="20" />
												<f:validator validatorId="validator.EngAndNum"/>
											</t:inputText> 4~20個英數字。
											<t:message for="userId" style="color:red" />
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
												value="#{adminUserAddBean.password}"
												required="true"
												redisplay="true">
												<f:validateLength minimum="6" maximum="20" />
											</t:inputSecret>6~20個字元。
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
												value="#{adminUserAddBean.passwordConfirm}"
												required="true"
												redisplay="true">
												<f:validateLength maximum="20" />
											</t:inputSecret> 請再輸入一次密碼。
											<t:message for="passwordConfirm" style="color:red" />
										</td>
									</tr>
									<tr>
										<th>姓名</th>
										<td>
											<t:inputText 
												id="userName"
												value="#{adminUserAddBean.userName}" 
												size="20" 
												maxlength="20">
												<f:validator validatorId="validator.ChineseAndEngAndSpace"/>
												<f:validateLength maximum="10" />
											</t:inputText>
											<t:message for="userName" style="color:red" />
										</td>
									</tr>
									<tr>
										<th>權限</th>
										<td valign="middle" bgcolor="#F4FAF3">
											<t:selectOneMenu 
												id="selUserRole" 
												forceId="true"
												value="#{adminUserAddBean.userRole}">
												<f:selectItems value="#{adminUserAddBean.userRoleList}" />
											</t:selectOneMenu>
										</td>
									</tr>
									<tr>
										<th>起訖日期</th>
										<td>
											<t:inputText 
												id="dtSelect1"
												value="#{adminUserAddBean.enableStart}" 
												size="12" 
												maxlength="10" 
												required="true">
												<f:validator validatorId="validator.Date"/>
											</t:inputText>
											<t:message for="dtSelect1" style="color:red" />
											 ~ 
											<t:inputText 
												id="dtSelect2" 
												value="#{adminUserAddBean.enableEnd}" 
												size="12" 
												maxlength="10" 
												required="true">
												<f:validator validatorId="validator.Date"/>

											</t:inputText>
											<t:message for="dtSelect2" style="color:red" />
										</td>
									</tr>
									<tr>
										<th>狀態</th>
										<td>
											<t:selectOneMenu 
												id="selStatus"
												value="#{adminUserAddBean.status}"
												style="width: 75px">
												<f:selectItem itemValue="1" itemLabel="啟用" />
												<f:selectItem itemValue="0" itemLabel="關閉" />
											</t:selectOneMenu>
										</td>
									</tr>
									<tr>
										<th>所屬行政中心</th>
										<td>
											<t:selectOneMenu 
												id="center"
												forceId="true"
												value="#{adminUserAddBean.center}"
												style="width: 75px">
												<f:selectItem itemValue="" itemLabel="無" />
												<f:selectItem itemValue="01" itemLabel="01" />
												<f:selectItem itemValue="02" itemLabel="02" />
												<f:selectItem itemValue="03" itemLabel="03" />
												<f:selectItem itemValue="04" itemLabel="04" />
												<f:selectItem itemValue="05" itemLabel="05" />
												<f:selectItem itemValue="06" itemLabel="06" />
												<f:selectItem itemValue="07" itemLabel="07" />
											</t:selectOneMenu>
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
								onclick="return(window.confirm('確認新增資料？'))"
								action="#{adminUserAddBean.add}" />&nbsp;&nbsp;
							<t:commandButton 
								value="取消" 
								title="取消"
								id="btnCancel"
								forceId="true"
								onclick="openUrl('adminUserMaintain.jspx', false);return false;"
								type="submit" />	
						</td>
					</tr>
				</table>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr><td align="center"><t:outputText id="dataResult" forceId="true" style="color: red" value="#{adminUserAddBean.result}"/></td></tr>
</table>
</h:form>
</f:view>
