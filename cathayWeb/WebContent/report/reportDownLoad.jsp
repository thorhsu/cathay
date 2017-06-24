<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
	<link rel="stylesheet" type="text/css" media="screen"
		href="../objects/prog/css/themes/redmond/jquery-ui-1.7.1.custom.css" />
	<link rel="stylesheet" type="text/css" media="screen"
		href="../objects/prog/css/themes/ui.jqgrid.css" />

	<link href="../objects/prog/css/jquery.datepick.css" 
		rel="stylesheet" type="text/css" />

	<title>[Report&gt;download報表]</title>

	<h:form id="sbForm">
	
	<table width="1000" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">download報表</th>
					</tr>
					<tr>
                        <td height="28" style="padding-top:10px">
          	                                         下載 : (<a href="<h:outputText escape="false" value='#{facesContext.externalContext.request.contextPath}' />/downloadServlet.serx?fileName=${reportNameForDownload}" target="new">下載</a>)
		                </td>
                    </tr>
					<tr>
                        <td height="28" style="padding-top:10px">
          	                                          開啟 : (<a href="<h:outputText escape="false" value='#{facesContext.externalContext.request.contextPath}' />/pdf/${reportNameForDownload}" target="new">開啟</a>)
		                </td>
                    </tr>   
				</table>
				</td>
		   </tr>
	</table>
	
	</h:form>
</f:view>
