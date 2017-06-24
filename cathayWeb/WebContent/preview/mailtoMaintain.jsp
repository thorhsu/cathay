<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<link href="../../objects/prog/css/jquery.datepick.css" rel="stylesheet" type="text/css" />
<title>[國壽人員功能&gt;email通知人員修改]</title>
<f:view>
<script lang="text/javascript">
   var checkPwd = function(){
	   if(window.confirm('確認修改資料？')){
	      if($("#mailPwd1").val() !== $("#mailPwd2").val()){
		      alert("Mail Server Password兩個不相等");
		      return false;
	      }
	      
	      return true;
	   }else{
		   return false;
	   }
   }

</script>
<h:form id="sbForm">
<table width="1000" border="0" cellspacing="0" cellpadding="0">
	<tr><td align="center"><t:outputText id="dataResult" forceId="true" style="color: red" value="#{propertiesMaintainBean.result}"/></td></tr>
	<tr>
		<td align="center" valign="top">
		<table width="1000" border="0" align="center" cellpadding="0"
			cellspacing="0" id="table_criteria">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellpadding="0" cellspacing="0"
					class="outline">
					<tr>
						<td class="title">通知人員修改</td>
					</tr>
					<tr>
						<td>
							<div id="divCriteria">
								<table width="800" border="0" cellpadding="0" cellspacing="1"
									class="content" id="tbCriteria">
									<tr>
									   <th colspan="2">
									             email通知人員設定
									   </th>
									</tr>
									<tr>
									   <th width="20%">
									                  異常狀況通知人員─請用半形逗點(,)分開
									   </th>
									   <td width="80%">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.emails}" size="100"/>
									   </td>
									</tr>
                                    <tr>
									   <th width="20%">
									                   迴歸測試通知人員─請用半形逗點(,)分開
									   </th>
									   <td width="80%">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.returnEmails}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th width="20%">
									                 完成每日工作後通知國壽人員─請用半形逗點(,)分開
									   </th>
									   <td width="80%">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.commonEmails}" size="100"/>
									   </td>									   
									</tr>
									<tr>
									   <th width="20%">
									                日報表寄送人員─請用半形逗點(,)分開
									   </th>
									   <td width="80%">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.dailyReportEmails}" size="100"/>
									   </td>									   
									</tr>
									<tr>
									   <th width="20%">
									                       北二寄件狀況通知人員─請用半形逗點(,)分開
									   </th>
									   <td width="80%">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.tpe2Mail}" size="100"/>
									   </td>
									</tr>
									
									<tr><td colspan="2"></td></tr>
									<tr>
									   <th colspan="2">
									             簡訊通知人員設定
									   </th>
									</tr>
									<tr>
									   <th width="20%">
									               正常轉檔通知號碼─請用半形逗點(,)分開
									   </th>
									   <td width="80%">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.commonPhones}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th width="20%">
									                 異常轉檔通知號碼─請用半形逗點(,)分開
									   </th>
									   <td width="80%">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.errorPhones}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th width="20%">
									                 迴歸異常通知號碼─請用半形逗點(,)分開
									   </th>
									   <td width="80%">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.returnPhones}" size="100"/>
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
								action="#{propertiesMaintainBean.persist}"
								onclick="return checkPwd()" />
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
