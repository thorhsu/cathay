<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
    <script src="../objects/prog/js/plugins/jquery.browser.js"
		type="text/javascript"></script>    	
	<script type="text/javascript" src="../objects/prog/js/sbDate.js"></script>
	<script src="../objects/prog/js/lib/i18n/grid.locale-en.js"
		type="text/javascript"></script>	
		
	<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
    var focusDecider = '<h:outputText escape="false" value="#{packCompleteBean.focusDecider}" />';
    var result = '<h:outputText escape="false" value="#{packCompleteBean.result}" />';
	var fileName = "";  
</script>	
	<link rel="stylesheet" type="text/css" media="screen"
		href="../objects/prog/css/themes/ui.jqgrid.css" />	

	<title>[FX人員功能&gt;超峰取件]</title>

	<h:form id="sbForm">
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">超峰取件</th>
					</tr>

					<tr>
						<td align="center" height="10"></td>
					</tr>
					<tr>
					   <td  align="center">
						  <t:inputText required="true" size="2" value="#{packCompleteBean.year}" />年
						  <t:inputText required="true" size="1" value="#{packCompleteBean.month}" />月
						  <t:inputText required="true" size="1" value="#{packCompleteBean.date}" />日
						  <t:inputText required="true" size="1" value="#{packCompleteBean.hours}" />時
						  <t:inputText required="true" size="1" value="#{packCompleteBean.minutes}" />分
					   </td>
					</tr>
					<tr>
						<td  align="center"><br />
						   <t:commandButton value="記錄超峰取件時間" id="logisticFinshed" forceId="true"
							   type="submit" action="#{packCompleteBean.finishedLogistic}" />
						   
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
				<td align="center"><t:outputText id="dataResult" forceId="true"
					style="color: red" value="#{packCompleteBean.result}" /></td>
			</tr>
		</table>
	</h:form>
</f:view>
