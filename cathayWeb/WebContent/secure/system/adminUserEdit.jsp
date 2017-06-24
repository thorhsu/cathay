<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<script type="text/javascript" src="../../objects/prog/js/sbDate.js"></script>

<title>[系統管理&gt;帳號修改]</title>
<f:view>
<script lang="text/javascript">
    <%//初始化adminUser %>
    var initStatus = '<t:outputText id="initResult" forceId="true" value="#{adminUserEditBean.initResult}" />';
    var allRoles = <h:outputText value="#{adminUserEditBean.roleListJson}" escape="false" /> ;
    function changeRoles() {

  	  //變更客戶聯絡人
	      $("#center > option" ).remove();	      
	      allRoles.forEach(function(element){			  			  
	    	  // 北一；02 台中；03 高雄；04台南；05 桃竹；06 北二 V
		     if($( "#selUserRole" ).val() === element.userRole && element.centerOnly === "1"){
		    	 $("#center").append($("<option ></option>").attr("value", "00").text("測試"));
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
    	$("#center").val('<h:outputText value="#{adminUserEditBean.center}" />');
    });
</script>
<h:form id="sbForm">

<table width="825" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td align="center" valign="top">
		<table width="825" border="0" align="center" cellpadding="0"
			cellspacing="0" id="table_criteria">
			<tr><td align="center"><t:outputText id="dataResult" forceId="true" style="color: red" value="#{adminUserEditBean.result}"/></td></tr>
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellpadding="0" cellspacing="0"
					class="outline">					
					<tr>
						<td class="title">帳號修改</td>
					</tr>
					<tr>
						<td>
							<div id="divCriteria">
								<table width="100%" border="0" cellpadding="0" cellspacing="1"
									class="content" id="tbCriteria">
									<tr>
										<th>帳號</th>
										<td>
											<t:inputText 
												id="userId" 
												forceId="true" 
												value="#{adminUserEditBean.userId}"
												disabled="true">
											</t:inputText> 不可修改。
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
												value="#{adminUserEditBean.password}"
												redisplay="true">
												<f:validateLength minimum="6" maximum="20" />
											</t:inputSecret> 6~20個字元，若不修改則不需填寫。
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
												value="#{adminUserEditBean.passwordConfirm}"
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
												value="#{adminUserEditBean.userName}" 
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
												value="#{adminUserEditBean.userRole}">
												<f:selectItems value="#{adminUserEditBean.userRoleList}" />
											</t:selectOneMenu>
										</td>
									</tr>
									<tr>
										<th>起訖日期</th>
										<td>
											<t:inputText 
												id="dtSelect1"
												value="#{adminUserEditBean.enableStart}" 
												size="12" 
												maxlength="10" 
												required="true">
												<f:validator validatorId="validator.Date"/>

											</t:inputText>
											<t:message for="dtSelect1" style="color:red" />
											 ~ 
											<t:inputText 
												id="dtSelect2" 
												value="#{adminUserEditBean.enableEnd}" 
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
												value="#{adminUserEditBean.status}"
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
												value="#{adminUserEditBean.center}"
												style="width: 75px">
												<f:selectItem itemValue="" itemLabel="無" />
												<f:selectItem itemValue="00" itemLabel="00" />
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
									<tr>
										<th>建立者</th>
										<td>
											<t:outputText 
												id="insertUser" 
												forceId="true" 
												value="#{adminUserEditBean.insertUser}" />
										</td>
									</tr>
									<tr>
										<th>建立時間</th>
										<td>
											<t:outputText 
												id="insertDate" 
												forceId="true" 
												value="#{adminUserEditBean.insertDate}" >
												<f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd HH:mm:ss"/>
											</t:outputText>
										</td>
									</tr>
									<tr>
										<th>最後異動者</th>
										<td>
											<t:outputText id="updateUser" forceId="true" 
												value="#{adminUserEditBean.updateUser}" />
										</td>
									</tr>
									<tr>
										<th>最後異動時間</th>
										<td>
											<t:outputText id="updateDate" forceId="true" 
												value="#{adminUserEditBean.updateDate}" >
												<f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd HH:mm:ss"/>
											</t:outputText>
										</td>
									</tr>
								</table>
							</div>
							<div id="divPreview" style="display: none">
								<table  border="0" align="center" cellpadding="0"
									cellspacing="0" id="pstage">
									<tr>
										<td class="main">
											<div id="pheader1">
												<table width="100%" border="0" cellpadding="0" cellspacing="0"
													id="about">
													<tr>
														<td><img src="../../objects/preview/objects/images/about_newsrelease_03.jpg"
															title="新聞稿" width="671" height="36" /></td>
													</tr>
													<tr>
														<td align="right">
														<table width="100%" border="0" cellspacing="0" cellpadding="0">
															<tr>
																<td><img src="../../objects/preview/objects/images/about_newsrelease_show_05.jpg"
																	width="397" height="30" /></td>
																<td><img src="../../objects/preview/objects/images/about_newsrelease_show_06.jpg"
																	height="30" /></td>
																<td align="right"><img
																	src="../../objects/preview/objects/images/about_newsrelease_show_07.jpg" width="170"
																	height="30" /></td>
															</tr>
														</table>
														</td>
													</tr>
												</table>
											</div>
									
											<table width="671" border="0" cellspacing="0" cellpadding="0"
												id="about_newsrelease_main">
												<tr>
													<td valign="top">
													<table width="100%" border="0" cellpadding="0" cellspacing="0"
														class="tb_datatitle" id="tb_title">
														<tr>
															<td class="dot"><img src="../../objects/preview/objects/images/dot.gif"
																alt="*" width="11" height="11" /></td>
															<td><span id="spNewsTitle"></span></td>
														</tr>
													</table>
													<div id="data_show" style="color: #5D5D5D; font-size: 12px;">
													<p><span id="spNewsContent"></span></p>
													</div>
													<div id="data_date">發佈日期： <span id="spNewsDate"></span>
													</div>
													</td>
												</tr>
											</table>
								
								
											<div id="pbottom"><img src="../../objects/preview/objects/images/top_14.gif" alt="top" width="649"
												height="31" border="0" /></div>
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
								action="#{adminUserEditBean.modify}"
								onclick="return(window.confirm('確認修改資料？'))" />&nbsp;&nbsp;
							<t:commandButton 
								value="取消" 
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

</table>
</h:form>
</f:view>
