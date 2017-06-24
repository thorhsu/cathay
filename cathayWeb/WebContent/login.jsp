<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="org.acegisecurity.ui.AbstractProcessingFilter" %>
<%@ page import="org.acegisecurity.AuthenticationException" %>
<%@ page import="org.acegisecurity.ui.webapp.AuthenticationProcessingFilter"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Fuji Xerox 國壽保單管理系統</title>
<script type="text/javascript" src="<c:out value="${pageContext.request.contextPath}" />/objects/prog/js/lib/jquery-1.3.2.min.js"></script>

<script type="text/javascript">
	$(document).ready(function(){
		document.getElementById("j_username").focus();
		$(document).keydown(function(){	if(event.keyCode==13){$("#btnLogin").click();}
		});

	});
	function submitForm(){
		var sbForm = document.getElementById("sbForm");
		sbForm.submit();
	}
</script>

<link href="objects/common_backstage.css" rel="stylesheet" type="text/css" />
</head>

<body class="login">

<div id="login_main"> 
<f:view>
<form action="<c:out value="${pageContext.request.contextPath}" />/j_acegi_security_check" method="post" id="sbForm">
  	<table align="center">
  	  <tr>
  	     <td colspan="2"></td>  	                                                                                  
  	  </tr>
  	  <tr>
  	     <td colspan="2" height="25"></td>  	                                                                                  
  	  </tr>
  	  <tr >
  		<td colspan="2" class="loginTxt" align="center">Fuji Xerox國壽保單管理系統登入</td>
  	  </tr>
  	  <tr >
  		<td colspan="2" class="loginTxt" height="10"></td>
  	  </tr>
      <tr>
  		<td align="right">帳號：</td>
    	<td align="left">
 			<t:inputText 
 					id='j_username' 
 					forceId="true" 
 					style="color: #6C6C6C; font-family: Verdana, Arial, Helvetica, sans-serif;"
 				 	size="20" 
 				 	maxlength="20"></t:inputText>
    	</td>
      </tr>
      <tr>
        <td align="right">密碼：</td>
    	<td align="left">
			<t:inputSecret 
					id="j_password" 
					forceId="true" 
					style="color: #6C6C6C; font-family: Verdana, Arial, Helvetica, sans-serif;"
				 	size="20" 
				 	maxlength="20" 
				 	redisplay="true"></t:inputSecret>
		</td>
      </tr>
      <tr>
      	<td colspan="2">
      		<br/>
                 <a href="#" onclick="submitForm();" id="btnLogin" class="login_btn">登入</a>
      	</td>
      </tr>
    </table>
    <c:if test="${not empty param.login_error}">
	    <font color="red">
			嘗試登入失敗，請重新嘗試。<br />
	    	原因: <c:out value="${userLocked}" /><br />
            <%
	           org.acegisecurity.context.SecurityContext auth = (org.acegisecurity.context.SecurityContext)session.getAttribute("ACEGI_SECURITY_CONTEXT");
			   boolean auth_valid = false;
	           if(auth != null)
	               auth_valid = auth.getAuthentication().isAuthenticated();
	           if(!auth_valid){
	        	   com.salmat.pas.bo.AdminUserService.updateErrorCounter((String)session.getAttribute(AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY));
	           }
			%>

    	</font>
 	</c:if>
  	<h:messages id="messages" layout="table" globalOnly="true" showSummary="true" showDetail="false"/>
  <div id="copyright">
    <hr align="center" width="90%" size="1" color="#CCCCCC" noshade="noshade" />
  </div>
<form>
</f:view>
</div>

</body>
</html>
