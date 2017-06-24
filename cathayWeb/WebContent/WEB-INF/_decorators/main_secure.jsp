<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="org.acegisecurity.ui.webapp.AuthenticationProcessingFilter"%>
<%@ page import="java.util.Date"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html >
<head>
<title>Fujixerox 保單管理系統 <decorator:title default="TENSION CORP." /></title>
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate" />
<meta http-equiv="Expires" content="Mon, 1 Jan 1990 00:00:00 GMT" />
<meta http-equiv="Last-Modified" content="0" />
<!-- script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/lib/jquery-1.3.2.min.js"></script -->

<script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/jquery.js"></script>
<script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/selectivizr-min.js"  ></script>
<link rel="stylesheet" href="<c:out value="${pageContext.request.contextPath}" />/objects/prog/css/jquery.treeview.css" />
<script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/jquery-ui-1.10.3.custom.js"></script>
<script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/i18n/jquery.ui.datepicker-zh-TW.min.js"></script>
<script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/lib/jquery.treeview.js"></script>
<script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/lib/jquery.selectboxes.min.js"></script>
<script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/common.js"></script>
<script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/header.js"></script>
<script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/dataMaintain.js"></script>
<link href="<c:out value="${pageContext.request.contextPath}" />/objects/common_backstage.css" rel="stylesheet" type="text/css" />
<link href="<c:out value="${pageContext.request.contextPath}" />/objects/backstage_table.css" rel="stylesheet" type="text/css" />
<link href="<c:out value="${pageContext.request.contextPath}" />/objects/prog/css/backstage_search_table.css" rel="stylesheet" type="text/css" />
<link href="<c:out value="${pageContext.request.contextPath}" />/objects/prog/css/ui-lightness/jquery-ui-1.10.3.custom.css" rel="stylesheet" type="text/css"/>
 
<script type="text/javascript">
        var windowLocation = window.location + "";
        var contextPath = '<c:out value="${pageContext.request.contextPath}" />';
        var index = windowLocation.indexOf(contextPath);
        var realRoot = windowLocation.substring(0, index);
jQuery(window).load(
		function(){
			<%
	           org.acegisecurity.context.SecurityContext auth = (org.acegisecurity.context.SecurityContext)session.getAttribute("ACEGI_SECURITY_CONTEXT");
			   boolean auth_valid = false;
	           if(auth != null)
	               auth_valid = auth.getAuthentication().isAuthenticated();
	           if(auth_valid)
	        	   com.salmat.pas.bo.AdminUserService.updateCounterZero((String)session.getAttribute(AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY));
			%>
		   var auth_valid = <%= auth_valid %>;
		   var loginMsg = '<c:out value="${loginMsg}" />';
		   if(auth_valid == false){			      
			   window.location = realRoot + contextPath + "/logout.jspx";			   
		   }else if(loginMsg != ''){
			   alert(loginMsg);
		   }

//		   alert(window.location);
           /*
			$("#menuList").treeview({
				collapsed: false,
				//animated: "medium",
				//control:"#sidetreecontrol",
				//persist: "location",
				unique: false
			});
           */
		}
);
function waitToFinished(){
	   $('#main').hide();	   
	   $('#tilesWait').show();
}
//submit時攔截並顯示等待畫面
$().ready(function() {
	      $("form").submit(
	    		 function(){
	    			 var thisAction = $(this).attr("action");	    			 
    			     waitToFinished();
	    		 }
	       );
	      
	      
	  });

</script>


</head>

<body>

<table width="100%" border="0" cellpadding="0" cellspacing="0" align="center">
	<tr>
		<td width="100%"  style="padding: 1px" >
		     <div align="center">
		       <span style="color: #26550B;float:left;width=240px">登入帳號：<%=session.getAttribute(AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY)%></span>
		       <span style="float:right"><img src="<c:out value="${pageContext.request.contextPath}" />/objects/images/cathay.jpg"/></span>
		     </div>
		</td>
	</tr>
	<tr style="margin-top: 0px">
	   <td width="100%" >
	        <div id="topbanner" style="margin-left:auto;margin-right: auto">
	       	   <span id="menuSpan" style="display:inline-block">
		   	      <c:out value="${menuHtml}" escapeXml="false"/>
	    	   </span>
	    	</div>
	   </td>
	</tr>	
	
	<tr>
		<td>		  
			<div id="main">			    			     			    
				<decorator:body />								
			</div>			
		</td>
	</tr>
	
	<tr>
		<td valign="top" >
		    <div align="left" id="tilesMainFrame"></div>
		    <div align="center" align="center" id="tilesWait" style="display:none"><img src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/images/wait_65.gif" /></div>
		</td>
	</tr>
	
	<tr>
	   <td>
	      		<div id="copyright">
					<hr align="center" width="98%" size="1" noshade="noshade" />
					富士施樂文件管理公司版權所有 © copyright Fuji Xerox DMS Co.All Rights Reserved
			</div>
	   </td>
	</tr>
</table>
</body>
</html>
