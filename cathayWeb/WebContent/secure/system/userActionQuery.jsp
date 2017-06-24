<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
	<script type="text/javascript"
		src="../../objects/prog/js/lib/jquery.datepick.min.js"></script>
	<script type="text/javascript" src="../../objects/prog/js/sbDate.js"></script>

	<script src="../../objects/prog/js/lib/i18n/grid.locale-en.js"
		type="text/javascript"></script>
	<script src="../../objects/prog/js/lib/jquery.jqGrid.min.js"
		type="text/javascript"></script>
	<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
    $(function(){  
	jQuery("#list4").jqGrid({
	            datatype: "local",
	            height: 350,
	            colNames: ['id', '操作人員ID', '人員姓名', '操作行為', '操作時間'],
	            colModel: [{name: 'id', index: 'id', sorttype: "int",hidden: true},
	                       {name: 'userId', index: 'userId', width: 80},
	                       {name: 'userName', index: 'userName', width: 130},	                        
	       	               {name: 'action', index: 'action', width: 130 }, 
	       	               {name: 'actionDateTime', index: 'actionDateTime', sorttype: "date", width: 200}],
                onPaging: function(){
                	
				     var startDate = jQuery("#startDateHidden").val();;
					 var endDate = jQuery("#endDateHidden").val();;
					 var action = jQuery("#actionHidden").val();
					 var userId = jQuery("#userIdHidden").val();
					 jQuery("#list4").jqGrid().setGridParam({url : contextPath + "/secure/system/actionQueryServlet.serx?startDate=" + startDate + "&endDate=" + endDate + "&action=" + action + "&userId=" + userId });      
                },                 
                pager: "#pager1",
	            caption: "查詢結果"
	       }).setGridParam({lastpage:<h:outputText value="#{actionQueryBean.totalPage}" />});;
	       var mydata = <h:outputText escape="false" value="#{actionQueryBean.jsonResult}" />;
	       if(mydata != ''){
	    	   jQuery("#jqGridDiv").show();
	           for (var i = 0; i <= mydata.length; i++)
 	             jQuery("#list4").addRowData(i + 1, mydata[i]);
	       }else{
	    	   jQuery("#jqGridDiv").hide();
		   }

	       jQuery("#list4").jqGrid().setGridParam({datatype : 'json'});
	});

    function clearColumn(){    
        document.getElementById("sbForm:dtSelect1").value = "";
        document.getElementById("sbForm:dtSelect2").value = "";
        jQuery("#action").val("");
		jQuery("#userId").val("");
    }
  
</script>
	<link rel="stylesheet" type="text/css" media="screen"
		href="../../objects/prog/css/themes/redmond/jquery-ui-1.7.1.custom.css" />
	<link rel="stylesheet" type="text/css" media="screen"
		href="../../objects/prog/css/themes/ui.jqgrid.css" />

	<link href="../../objects/prog/css/jquery.datepick.css"
		rel="stylesheet" type="text/css" />

	<title>[系統管理&gt;操作歷程查詢]</title>

	<h:form id="sbForm">
		<table width="815" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">使用者操作歷程查詢</th>
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
										<td align="center" bgcolor="#DFF4DD">查詢日期區間</td>
										<td colspan="3" width="300" bgcolor="#F4FAF3" align="left">
										    <t:inputText 
											  id="dtSelect1" value="#{actionQueryBean.startDate}"
											  size="12" maxlength="10">
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect1" style="color:red" /> ~ 
										   <t:inputText 
											   id="dtSelect2" value="#{actionQueryBean.endDate}" size="12"
											   maxlength="10">
											   <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											   <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect2" style="color:red" />
										</td>
									</tr>
									<tr>
										<td align="center" bgcolor="#DFF4DD">操作行為</td>
										<td width="120" bgcolor="#F4FAF3" align="left">
										     <t:selectOneMenu id="action" forceId="true" value="#{actionQueryBean.action}" >
										          <f:selectItems  value = "#{actionQueryBean.allPages}" />
                                              </t:selectOneMenu> 
										</td>
										<td align="center" bgcolor="#DFF4DD">操作人員</td>
										<td width="120" bgcolor="#F4FAF3" align="left" >
										<t:selectOneMenu id="userId" forceId="true" value="#{actionQueryBean.userId}" >
											<f:selectItems  value = "#{actionQueryBean.allUserIds }" />
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
							    type="submit" action="#{actionQueryBean.doQuery}" />
							&nbsp;&nbsp;&nbsp;
						   <input type="button" value="清除" onclick="clearColumn()"/>
							
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
					style="color: red" value="#{actionQueryBean.result}" /></td>
			</tr>
			<tr>
				<td>
				<table width="95%" border="0" align="center" cellpadding="0"
					cellspacing="0" id="table_show">
					<tr>
						<td>
						<table width="100%" border="0" cellpadding="0" cellspacing="0" class="outline">
							<tr>
								<td class="title">查詢結果</td>
							</tr>
						</table>
						</td>
					</tr>
				</table>
				</td>
			</tr>
            <tr>
				<td>
				<table width="95%" border="0" align="center" cellpadding="0"
					cellspacing="0" id="table_show">
					<tr>
						<td align="center">
                          <div id="jqGridDiv">
		                     <table id="list4" class="scroll" align="left"></table>
		                     <div id="pager1" align="left"></div>
		                  </div>
						</td>
					</tr>
				</table>
				</td>
			</tr>			
		</table>
		<input type="hidden" id="startDateHidden"  value='<h:outputText escape="false" converter="converter.Date" value="#{actionQueryBean.startDate}" />' />
		<input type="hidden" id="endDateHidden"  value='<h:outputText escape="false" converter="converter.Date" value="#{actionQueryBean.endDate}" />' />
		<input type="hidden" id="actionHidden"  value='<h:outputText  value="#{actionQueryBean.action}" />' />
		<input type="hidden" id="userIdHidden" value="<h:outputText value='#{actionQueryBean.userId}' />" />
		
		
	</h:form>
</f:view>
