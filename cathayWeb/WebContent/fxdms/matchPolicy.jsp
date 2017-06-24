<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<f:view>
<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
	var groupObj = <h:outputText escape="false" value="#{matchPolicyBean.groupObjStr}" />;
	var policyNoObjObj = <h:outputText escape="false" value="#{matchPolicyBean.policyNoObjStr}" />;
	var cardPagesObj = <h:outputText escape="false" value="#{matchPolicyBean.cardPagesStr}" />;
	var isGroup = <h:outputText escape="false" value="#{matchPolicyBean.group}" />;
	var matchMap = <h:outputText escape="false" value="#{matchPolicyBean.matchMapStr}" />;
	var alertStr = '<h:outputText escape="false" value="#{matchPolicyBean.alertStr}" />';
	var dialog;
	var bankReceiptId = "";		    		
	var bankReceiptId2 = "";
	var bankReceiptId3 = "";
	var bankReceiptId4 = "";		    		
	var bankReceiptId5 = "";
	var bankReceiptId6 = "";
	var bankReceiptId7 = "";		    		
	var bankReceiptId8 = "";
	var bankReceiptId9 = "";
	$().ready(function(){			
		dialog = $( "#dialog-form" ).dialog({
		      autoOpen: false,
		      height: 700,
		      width: 600,	
		      modal: true     	          	      
		});
		$(".returnEvn").text("");		
		$(".returnEvn[class^='TC']").text("中");
		$(".returnEvn[class^='TQ']").text("北");
		$(".returnEvn[class^='TX']").text("北");
		$(".returnEvn[class^='TN']").text("高");
		
		if(!isGroup){
			$("#groupBlock").hide();
			$("#taipeiNo2").show();	
    		$("#cardPages").val("");
    		$("#groupPolicyNo").val("");    		    		
		}else{
			$("#groupBlock").show();
			$("#taipeiNo2").hide();	
			$("#humidProof").prop( "checked", false );
			$("#cd").prop( "checked", false );
			$("#returnEnv").val("");
			$("#bankReceiptId").val("");						
			$("#bankReceiptId2").val("");
			$("#bankReceiptId3").val("");						
			$("#bankReceiptId4").val("");						
			$("#bankReceiptId5").val("");
			$("#bankReceiptId6").val("");
			$("#bankReceiptId7").val("");						
			$("#bankReceiptId8").val("");
			$("#bankReceiptId9").val("");
		}
		var withCard = false;		
		if($('#groupPolicyNo').val() === '' && $('#cardPages').val() === ''){			
	 		$("#groupBlock").hide();
	   }else{
		   $("#groupBlock").show();
	   }
	   if($('#policyNos').val() === ''){			
	 		$(".policyNos").hide();
	   }else{
		   $(".policyNos").show();
	   }
	   if($('#returnEnv').val() === ''){			
	 		$(".returnEnv").hide();
	   }else{
		   $(".returnEnv").show();
	   }
	   if($('#bankReceiptId').val() === ''){			
	 		$(".bankReceiptId").hide();
	   }else{
		   $(".bankReceiptId").show();
	   }
	   if($('#bankReceiptId2').val() === ''){			
	 		$("#bankReceiptId2").hide();
	   }else{
		   $("#bankReceiptId2").show();
	   }
	   if($('#bankReceiptId3').val() === ''){			
	 		$("#bankReceiptId3").hide();
	   }else{
		   $("#bankReceiptId3").show();
	   }
	   if($('#bankReceiptId4').val() === ''){			
	 		$("#bankReceiptId4").hide();
	   }else{
		   $("#bankReceiptId4").show();
	   }
	   if($('#bankReceiptId5').val() === ''){			
	 		$("#bankReceiptId5").hide();
	   }else{
		   $("#bankReceiptId5").show();
	   }
	   if($('#bankReceiptId6').val() === ''){			
	 		$("#bankReceiptId6").hide();
	   }else{
		   $("#bankReceiptId6").show();
	   }
	   if($('#bankReceiptId7').val() === ''){			
	 		$("#bankReceiptId7").hide();
	   }else{
		   $("#bankReceiptId7").show();
	   }
	   if($('#bankReceiptId8').val() === ''){			
	 		$("#bankReceiptId8").hide();
	   }else{
		   $("#bankReceiptId8").show();
	   }
	   if($('#bankReceiptId9').val() === ''){			
	 		$("#bankReceiptId9").hide();
	   }else{
		   $("#bankReceiptId9").show();
	   }
	   if($("#humidProof").prop("checked")){			
	 		$(".humidProof").show();
	   }else{
		   $(".humidProof").hide();
	   }
	   if($("#cd").prop("checked")){			
	 		$(".cd").show();
	   }else{
		   $(".cd").hide();
	   }
	   
	   
	   if(alertStr !== "")
		   alert(alertStr);
	   var recPolicyNo = "";
       var returnEnv = "";	   
	   var cd = "";
	   var recApplyPages = "";
	   var humidProof = "0";
	   
	   $('#uniqueNo').keypress( function(event) {		    		   
		    var uniqueNo = $('#uniqueNo').val(); 
		    if (event.keyCode === 13) {		    	
		    	var matchResult = matchMap[uniqueNo];
		    	if(matchResult === undefined || matchResult === null || matchResult === ""){
		    		alert("輸入的保單右上角號碼不屬於" + $("#jobBagNo").val() + "此工單");
		    		return false;
		    	}
		    	recPolicyNo = matchResult.recPolicyNo;
		    	returnEnv = matchResult.returnEnv;
	    		bankReceiptId = matchResult.bankReceiptId;
	    		bankReceiptId2 = matchResult.bankReceiptId2;
	    		bankReceiptId3 = matchResult.bankReceiptId3;
	    		bankReceiptId4 = matchResult.bankReceiptId4;
	    		bankReceiptId5 = matchResult.bankReceiptId5;
	    		bankReceiptId6 = matchResult.bankReceiptId6;
	    		bankReceiptId7 = matchResult.bankReceiptId7;
	    		bankReceiptId8 = matchResult.bankReceiptId8;
	    		bankReceiptId9 = matchResult.bankReceiptId9;
	    		
	    		recApplyPages = matchResult.recApplyPages;
	    		
	    		cd = matchResult.cd;
	    		humidProof = matchResult.humidProof;
		    	if(!isGroup){		    				    		
		    		if(returnEnv !== ""){
		    			$(".returnEnv").show();
		    			$("#returnEnv").val(returnEnv);
		    			$("#returnEnv").attr("placeholder", returnEnv);
		    		}else{
		    			$(".returnEnv").hide();
		    			$("#returnEnv").val("");
		    		}
		    		if(bankReceiptId !== ""){
		    			$(".bankReceiptId").show();
		    			$("#bankReceiptId").val(bankReceiptId);
		    			$("#bankReceiptId").attr("placeholder", bankReceiptId);
		    			if(bankReceiptId.indexOf("未收到") >= 0){
		    				//$("#bankReceiptId").attr("readonly", "readonly");
		    				$("#bankReceiptId").attr("size", "25");
		    				var receiptStr = "";
		    				if(recPolicyNo !== ""){
		    					receiptStr = "與簽收單"
		    				}
		    				alert("注意：" + bankReceiptId + "。\r\n如已收到，請到送金單接收功能設定接收此送金單，並於驗單功能中將此保單" + receiptStr + "設定為驗單完成");
			    		}
		    			for(var i = 2 ; i < 10 ; i++){
		    				if(matchResult["bankReceiptId" + i] !== ""){
		    					$("#bankReceiptId" + i ).show();
		    					$("#bankReceiptId" + i ).val(matchResult["bankReceiptId" + i]);
		    					$("#bankReceiptId" + i).attr("placeholder", matchResult["bankReceiptId" + i]);
		    					if(matchResult["bankReceiptId" + i].indexOf("未收到") >= 0){
				    				//$("#bankReceiptId").attr("readonly", "readonly");
				    				$("#bankReceiptId" + i).attr("size", "25");
				    				var receiptStr = "";
				    				if(recPolicyNo !== ""){
				    					receiptStr = "與簽收單"
				    				}
				    				alert("注意：" + matchResult["bankReceiptId" + i] + "。\r\n如已收到，請到送金單接收功能設定接收此送金單，並於驗單功能中將此保單" + receiptStr + "設定為驗單完成");
					    		}
		    				}else{
		    					$("#bankReceiptId" + i ).hide();
		    					$("#bankReceiptId" + i).val("");
		    				}
		    			}
		    			
		    			
		    		}else{
		    			$(".bankReceiptId").hide();
		    			$("#bankReceiptId").val("");
		    		}
		    		if(cd !== ""){
		    			$(".cd").show();
		    			$("#cd").prop( "checked", true );
		    		}else{
		    			$(".cd").hide();
		    			$("#cd").prop( "checked", false );
		    		}
		    		if(humidProof !== ""){
		    			$(".humidProof").show();
		    			$("#humidProof").prop( "checked", true );
		    		}else{
		    			$(".humidProof").hide();
		    			$("#humidProof").prop( "checked", false );
		    		}
		    	}else{
		    	   withCard = groupObj[uniqueNo];
		    	   //withCard = true;
		    	   if(withCard){
		    		   $("#groupBlock").show();
		    		   var policyNo = policyNoObjObj[uniqueNo];
		    		   var cardPages = cardPagesObj[uniqueNo];
		    		   $("#cardPages").attr("placeholder", cardPages);
		    		   $("#cardPages").val(cardPages);
		    		   $("#groupPolicyNo").val(policyNo);		    		
		    		   $("#groupPolicyNo").attr("placeholder", policyNo);
		    	   }else{
		    		   $("#groupBlock").hide();
		    		   $("#cardPages").val("");
		    		   $("#groupPolicyNo").val("");
		    	   }
                }
		    	if(recPolicyNo !== ""){
	    			$(".policyNos").show();
	    			$("#policyNos").val(recPolicyNo);
	    			$("#policyNos").attr("placeholder", recPolicyNo);
	    			$("#policyNos").select();
	    			if(recApplyPages !== "" && recApplyPages !== "1")
	    			   $("#recApplyPages").text("注意：簽收單" + recApplyPages + "張");
	    			else
	    			   $("#recApplyPages").text("");
	    			return false;
	    		}else{
	    			$(".policyNos").hide();
	    			$("#policyNos").val("");
	    			return focusTo("uniqueNo", recPolicyNo, returnEnv, bankReceiptId, cd, humidProof);
	    		}
		    	
		    }
		});
	  $('#policyNos').keypress( function(event) {		     
		    if (event.keyCode === 13) {		  
		    	var uniqueNo = $('#uniqueNo').val();		    	
			    if (event.keyCode === 13) {			    	
			    	withCard = groupObj[uniqueNo];
			    }
			    
		    	if(isGroup && withCard){		    		
		    		$("#groupPolicyNo").select();
		    		return false;
		    	}else if(!isGroup){
		    		return focusTo("policyNos", recPolicyNo, returnEnv, bankReceiptId, cd, humidProof);
		    	}		    	
		    }
		});
	  $('#returnEnv').keypress( function(event) {		     
		    if (event.keyCode === 13) {		  		    	
		    	return focusTo("returnEnv", recPolicyNo, returnEnv, bankReceiptId, cd, humidProof);		    			    	
		    }
		});
	  $('.bankReceiptId > input[id^="bankReceiptId"]').keypress( function(event) {		     
		    if (event.keyCode === 13) {		  				    	
		    	//alert($(this).attr("id"));
		    	return focusTo($(this).attr("id"), recPolicyNo, returnEnv, bankReceiptId, cd, humidProof);		    			    	
		    }
		});
	  $('#cd').keypress( function(event) {		     
		    if (event.keyCode === 13) {		  		    	
		    	return focusTo("cd", recPolicyNo, returnEnv, bankReceiptId, cd, humidProof);		    			    	
		    }
		});
	  $('#humidProof').keypress( function(event) {		     
		    if (event.keyCode === 13) {		  		    	
		    	return focusTo("humidProof", recPolicyNo, returnEnv, bankReceiptId, cd, humidProof);		    			    	
		    }
		});
	  var pageCounter = 0;
	  var preUniqueNo = "";
	  $('#groupPolicyNo').keypress( function(event) {			    		    
		    if (event.keyCode === 13) {		
		    	var uniqueNo = $('#uniqueNo').val();
		    	if(uniqueNo !== preUniqueNo){
		    		pageCounter = 0;
		    		preUniqueNo = uniqueNo
		    	}
		    	var cardPages = cardPagesObj[uniqueNo];
		    	var policyNo = policyNoObjObj[uniqueNo];
		    	var inputNo = $('#groupPolicyNo').val();
			    if (event.keyCode === 13) {
			    	withCard = groupObj[uniqueNo];
			    }
		    	if(withCard){
		    		//條碼不同，只考慮前十碼相同
		    		if(policyNo.substring(0, 10) === inputNo.substring(0, 10)){
		    			$('#groupPolicyNo').val(policyNo);
		    			$("#cardPages").val(++pageCounter);
		    		}
		    		if(pageCounter >= parseInt(cardPages)){	
		    		   $("#cardPages").select();
		    		   if(pageCounter > parseInt(cardPages)){
		    	    	   alert(pageCounter + "超過應有頁數" + cardPages);
		    	       }
		    	    }else{		    	       
		    		   $("#groupPolicyNo").select();
		    		   
		            }
		    		return false;
		    	}		    	
		    }
		});
	  
		
		var jobBagNo = $("#jobBagNo").val();
		if(jobBagNo === ""){
			$("#jobBagNo").focus();
		}else{
			$("#uniqueNo").select();
		}		
	});
	
	function clearColumn(){
		$("#jobBagNo").val("");
		$("#uniqueNo").val("");
		$("#policyNos").val("");		
		$(".bankReceiptId > input").val("");
		$("input[type='checkbox']").prop( "checked", false );
		$("#returnEnv").val("");
		$("#groupPolicyNo").val("");
		$("#cardPages").val("");
		$("#jobBagNo").focus();
		
	}
	var opBankReceipt = function(){
		dialog.dialog( "open" );		
		
		return false;
	}
	
	function noBankReceipt(){
		$("#jobBagNo").val("noBankReceiptPolicy");		 
	}	
	
	function validCheck(recPolicyNo, returnEnv, bankReceiptId, cd, humidProof){
		if($("#jobBagNo").val().length < 15 && "noBankReceiptPolicy" !== $("#jobBagNo").val()){
			alert("工單號碼長度小於15，非FX工單，請重新輸入");
			return false;
		}
		if("noBankReceiptPolicy" !== $("#jobBagNo").val())
		   $("#jobBagNo").val($("#jobBagNo").val().substring(0, 15));
		if(recPolicyNo === null && returnEnv === null && bankReceiptId === null && cd === null && humidProof === null){
			var uniqueNo = $('#uniqueNo').val();
			if(uniqueNo === "")
				return true;
			var matchResult = matchMap[uniqueNo];
	    	if(matchResult === undefined || matchResult === null || matchResult === ""){
	    		alert("輸入的保單右上角號碼不屬於" + $("#jobBagNo").val() + "此工單");
	    		return false;
	    	}
	    	recPolicyNo = matchResult.recPolicyNo;
	    	returnEnv = matchResult.returnEnv;
    		bankReceiptId = matchResult.bankReceiptId;		    		
    		cd = matchResult.cd;
    		humidProof = matchResult.humidProof;			
		}
		if($("#policyNos").val() !== recPolicyNo){
			alert("簽收單號碼不正確 ");
			return false;
		}
		if($("#returnEnv").val() !== returnEnv){
			alert("回郵信封必須是" + returnEnv);
			return false;
		}
		if($("#bankReceiptId").val() !== bankReceiptId){
			alert("此保單搭配的送金單號碼為:" + bankReceiptId + "，請重新檢查");
			return false;
		}
		if($("#bankReceiptId2").val() !== bankReceiptId2){
			alert("此保單搭配的第二送金單號碼為:" + bankReceiptId2 + "，請重新檢查");
			return false;
		}
		if($("#bankReceiptId3").val() !== bankReceiptId3){
			alert("此保單搭配的第三送金單號碼為:" + bankReceiptId3 + "，請重新檢查");
			return false;
		}
		if($("#bankReceiptId4").val() !== bankReceiptId4){
			alert("此保單搭配的第四送金單號碼為:" + bankReceiptId4 + "，請重新檢查");
			return false;
		}
		if($("#bankReceiptId5").val() !== bankReceiptId5){
			alert("此保單搭配的第五送金單號碼為:" + bankReceiptId5 + "，請重新檢查");
			return false;
		}
		
		if($("#cd").prop("checked") !== (cd === "true")){
			alert("必須有CD");
			return false;
		}
		if($("#humidProof").prop("checked") !== (humidProof === "true")){
			alert("必須有夾鏈袋");
			return false;
		}
		return true;
	}
	function focusTo(id, recPolicyNo, returnEnv, bankReceiptId, cd, humidProof){		
		if(id !== "bankReceiptId" && id !== "bankReceiptId2" && id !== "bankReceiptId3" 
		    && id !== "bankReceiptId4" && id !== "bankReceiptId5" && id !== "bankReceiptId6"
		    && id !== "bankReceiptId7" && id !== "bankReceiptId8" && id !== "bankReceiptId9"
			&&	id !== "returnEnv" && id !== "cd" && id !== "humidProof" 
			&& bankReceiptId != null && bankReceiptId !== ""){
			$("#bankReceiptId").select();
			return false;
		}else{
			if(id !== "bankReceiptId2" && id !== "bankReceiptId3" 
			    && id !== "bankReceiptId4" && id !== "bankReceiptId5" && id !== "bankReceiptId6"
			    && id !== "bankReceiptId7" && id !== "bankReceiptId8" && id !== "bankReceiptId9"
				&&	id !== "returnEnv" && id !== "cd" && id !== "humidProof" 
				&& bankReceiptId2 != null && bankReceiptId2 !== ""){
				$("#bankReceiptId2").select();
				return false;
			}else{
				if(id !== "bankReceiptId3" 
				    && id !== "bankReceiptId4" && id !== "bankReceiptId5" && id !== "bankReceiptId6"
				    && id !== "bankReceiptId7" && id !== "bankReceiptId8" && id !== "bankReceiptId9"
					&&	id !== "returnEnv" && id !== "cd" && id !== "humidProof" 
					&& bankReceiptId3 != null && bankReceiptId3 !== ""){
					$("#bankReceiptId3").select();
					return false;
				}else{
					if(id !== "bankReceiptId4" && id !== "bankReceiptId5" && id !== "bankReceiptId6"
					    && id !== "bankReceiptId7" && id !== "bankReceiptId8" && id !== "bankReceiptId9"
						&&	id !== "returnEnv" && id !== "cd" && id !== "humidProof" 
						&& bankReceiptId4 != null && bankReceiptId4 !== ""){
						$("#bankReceiptId4").select();
						return false;
					}else{
						if(id !== "bankReceiptId5" && id !== "bankReceiptId6"
						    && id !== "bankReceiptId7" && id !== "bankReceiptId8" && id !== "bankReceiptId9"
							&&	id !== "returnEnv" && id !== "cd" && id !== "humidProof" 
							&& bankReceiptId5 != null && bankReceiptId5 !== ""){
							$("#bankReceiptId5").select();
							return false;
						}else{	
			               if(id !== "returnEnv" && id !== "cd" && id !== "humidProof" 
				               && returnEnv !== null && returnEnv !== ""){			   
			                   $("#returnEnv").select();
			                   return false;
			               }else{
				               if(id !== "cd" && id !== "humidProof" && cd != null && cd !== ""){
					               $("#cd").focus();
					               return false;
				               }else{
					               if(id !== "humidProof" && humidProof !== null && humidProof !== ""){
						               $("#humidProof").focus();
						               return false;
					              }
				               }
			               }
						}
				    }
			   }
		   }
	    }
		return validCheck(recPolicyNo, returnEnv, bankReceiptId, cd, humidProof);
		
	}
	
	
	
  
</script>
	
	<title>[FX人員功能&gt;配表]</title>

	<h:form id="sbForm">
	        <div id="dialog-form" title="送金單順序">
                    <fieldset>
                 		<t:dataTable value="#{matchPolicyBean.brDataModel}"
										id="dataList2" 
										forceId="true" 
										var="bankReceipt" 
										binding="#{matchPolicyBean.brDataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																														
										width="100%" >
								<t:column >
									<f:facet name="header">
										<t:outputText value="送金單號碼"></t:outputText>
									</f:facet>
									<t:outputText value="#{bankReceipt.bankReceiptId}" />
								</t:column>
								<t:column >
									<f:facet name="header">
										<t:outputText value="接收日期"></t:outputText>
									</f:facet>
									<t:outputText value="#{bankReceipt.receiveDate}" >
									   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
									</t:outputText>
								</t:column>
								<t:column >
									<f:facet name="header">
										<t:outputText value="寄送單位"></t:outputText>
									</f:facet>
									<t:outputText value="#{bankReceipt.center}" />
									   
								</t:column>
								<t:column >
									<f:facet name="header">
										<t:outputText value="寄送順序"></t:outputText>
									</f:facet>
									<t:outputText value="#{bankReceipt.dateCenterSerialNo}" />
									   
								</t:column>
						</t:dataTable>
                    </fieldset>
                 </div>
	
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">配表</th>
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
										<td align="center" bgcolor="#DFF4DD" width="15%">工單號碼</td>
										<td   bgcolor="#F4FAF3" align="left">
										    <t:inputText forceId="true" 
											  id="jobBagNo" value="#{matchPolicyBean.jobBagNo}"
											  size="20" maxlength="19" required="true">
											  <f:validateLength minimum="15" maximum="19" />
										    </t:inputText>
											<h:message for="jobBagNo" style="color:red" />
											<t:outputText value="列印檔：#{matchPolicyBean.newBatchName}" rendered="#{matchPolicyBean.newBatchName ne null && matchPolicyBean.newBatchName ne '' }"/>
											<t:inputHidden value="#{matchPolicyBean.newBatchName}" />											
											
										</td>																												
									</tr>
									<tr >
									    <td align="center" bgcolor="#DFF4DD" width="15%">保單 + 簽收單號碼</td>
										<td   bgcolor="#F4FAF3" align="left">
										        保單右上角編號： 
										    <t:inputText forceId="true"
											  id="uniqueNo" value="#{matchPolicyBean.uniqueNo}"
											  size="20" maxlength="20" />
										   <h:message for="uniqueNo" style="color:red" />   
										   &nbsp;
										   <span class="policyNos">
										               簽收單保單號碼： 
										       <t:inputText forceId="true"
											      id="policyNos" value="#{matchPolicyBean.policyNos}"
											     size="20" maxlength="20" />
											  <span id="recApplyPages" style="color:red">
											  </span>
										   </span>
											<span id="groupBlock" >
											   &nbsp;  
											        團險證： 
										       <t:inputText forceId="true"
											     id="groupPolicyNo" value="#{matchPolicyBean.groupPolicyNo}"
											     size="20" maxlength="20" />
											   <t:inputText forceId="true"
											     id="cardPages" value="#{matchPolicyBean.cardPages}"
											     size="2" maxlength="5" />
											         張
											</span>
											<div id="taipeiNo2" style='display:none' >											   
											   <span class="bankReceiptId">
											               送金單：
											      <t:inputText forceId="true"
											       id="bankReceiptId" value="#{matchPolicyBean.bankReceiptId}"
											       size="10" maxlength="12" />
											      <t:inputText forceId="true"
											         id="bankReceiptId2" value="#{matchPolicyBean.bankReceiptId2}"
											         size="10" maxlength="12" style="display:none"/>
											      <t:inputText forceId="true"
											         id="bankReceiptId3" value="#{matchPolicyBean.bankReceiptId3}"
											         size="10" maxlength="12" style="display:none"/>
											      <t:inputText forceId="true"
											         id="bankReceiptId4" value="#{matchPolicyBean.bankReceiptId4}"
											         size="10" maxlength="12" style="display:none"/>
											      <t:inputText forceId="true"
											         id="bankReceiptId5" value="#{matchPolicyBean.bankReceiptId5}"
											         size="10" maxlength="12" style="display:none"/>
											      <t:inputText forceId="true"
											         id="bankReceiptId6" value="#{matchPolicyBean.bankReceiptId6}"
											         size="10" maxlength="12" style="display:none"/>
											      <t:inputText forceId="true"
											         id="bankReceiptId7" value="#{matchPolicyBean.bankReceiptId7}"
											         size="10" maxlength="12" style="display:none"/>
											      <t:inputText forceId="true"
											         id="bankReceiptId8" value="#{matchPolicyBean.bankReceiptId8}"
											         size="10" maxlength="12" style="display:none"/>
											      <t:inputText forceId="true"
											         id="bankReceiptId9" value="#{matchPolicyBean.bankReceiptId9}"
											         size="10" maxlength="12" style="display:none"/>											      
											   </span>
											   &nbsp;
											   <span class="returnEnv">     
											                回郵信封：
											      <t:inputText forceId="true"
											         id="returnEnv" value="#{matchPolicyBean.returnEnv}"
											          size="1" maxlength="1" />
											   </span>
											   &nbsp;
											   <span class="cd">											              
											     <t:selectBooleanCheckbox id="cd" value="#{matchPolicyBean.cd}" forceId="true"/>
											             光碟
											   </span>
											   &nbsp;    
											   <span class="humidProof">											              
											     <t:selectBooleanCheckbox id="humidProof" value="#{matchPolicyBean.humidProof}" forceId="true"/>
											             夾鏈袋
											   </span>
											</div>  
											<br/>
											<h:outputText value="Cycle Date：#{matchPolicyBean.cycleDateStr}" rendered="#{matchPolicyBean.cycleDate ne null}" />
											    
											<t:inputHidden value="#{matchPolicyBean.cycleDate}" >
										        <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
										    </t:inputHidden>									
											&nbsp;
										    <t:outputText value="轄區：#{matchPolicyBean.center}" rendered="#{matchPolicyBean.center ne null && matchPolicyBean.center ne '' }"/>
										    <t:inputHidden value="#{matchPolicyBean.center}" />											                                              
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
						    <t:commandButton value="送出資料" id="btnQuery" onclick="return validCheck(null, null, null, null, null);" 
							    type="submit" action="#{matchPolicyBean.doSubmit}" />
							<input type="button" value="清除" onclick="clearColumn()"/>
							    &nbsp;&nbsp;							
							<t:commandButton value="送金單領件表與標籤" id="printLabel" 
							    type="submit" action="#{matchPolicyBean.printLabel}" />    						    						   						    
						    <% //<h:commandButton value="送金單順序" id="opBankReceipt" rendered="#{matchPolicyBean.haveBankReceipt}"
							    //type="submit" action="#{matchPolicyBean.printLabel}" onclick="return opBankReceipt()" / %>
							<t:commandButton value="等待送金單保單" id="noBankReceipt" 
							    type="submit" action="#{matchPolicyBean.noBankReceipt}" onclick="return noBankReceipt()"/>
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
					style="color: red" value="#{matchPolicyBean.result}" />
				</td>				   			    
			</tr>
			<tr>
			    <td align="center"><h:outputText value="#{matchPolicyBean.newBatchName}內容" rendered="#{matchPolicyBean.dataModel1 ne null}"/></td>
			</tr>			
			<tr>				
			    <td align="center">			       
			       <t:dataTable value="#{matchPolicyBean.dataModel1}"
										id="dataList" 
										forceId="true" 
										var="applyData" 
										binding="#{matchPolicyBean.dataTable1}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="#{matchPolicyBean.rowClases}" 
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<t:outputText value="保單右上角號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.uniqueNo}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="Cycle Date"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.cycleDate}" >
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="保單號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.policyNos}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="要保人"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.recName}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="保單狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.policyStatusName}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="簽收單狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{(applyData.receiptData eq null)? '無簽收單' : applyData.receiptData.policyStatusName}" />
									</t:column>
									<t:column rendered="#{!matchPolicyBean.group}">
										<f:facet name="header">
											<t:outputText value="回郵信封"></t:outputText>
										</f:facet>										
										<t:outputText value="#{applyData.areaId}" styleClass="#{applyData.areaId} returnEvn"/>
									</t:column>
									<t:column rendered="#{!matchPolicyBean.group}">
										<f:facet name="header">
											<t:outputText value="送金單"></t:outputText>
										</f:facet>										
										<t:outputText value="#{applyData.bankReceiptId}" />
									</t:column>
									<t:column rendered="#{!matchPolicyBean.group}">
										<f:facet name="header">
											<t:outputText value="光碟"></t:outputText>
										</f:facet>										
										<t:outputText value="#{applyData.cd ? '有' : ''}" />
									</t:column>
									<t:column rendered="#{matchPolicyBean.group}">
										<f:facet name="header">
											<t:outputText value="團險證狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{(applyData.insureCardData eq null)? '無團險證' : applyData.insureCardData.policyStatusName}" />
									</t:column>
									<t:column>
									    <f:facet name="header">
											<t:outputText value="最近更新時間" />
										</f:facet>
										<t:outputText value="#{applyData.updateDate}" >
										   <f:convertDateTime pattern="yyyy/MM/dd HH:mm" timeZone="GMT+8" />
										</t:outputText>
									</t:column>									
									
							</t:dataTable>
			    </td>
			</tr>
			<tr>
			    <td><br/></td>
			</tr>
			<tr>
			    <td align="center"><h:outputText value="#{matchPolicyBean.cycleDateStr}所有列印檔內容" rendered="#{matchPolicyBean.dataModel ne null}"/></td>
			</tr>
			<tr>
			    <td align="center">
			       <t:dataTable value="#{matchPolicyBean.dataModel}"
										id="dataList1" 
										forceId="true" 
										var="afpFileDisPlay" 
										binding="#{matchPolicyBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="#{matchPolicyBean.afpRowClases}"										
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<t:outputText value="批次號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{afpFileDisPlay.newBatchName}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="轄區"></t:outputText>
										</f:facet>
										<t:outputText value="#{afpFileDisPlay.center}">
										</t:outputText>
									</t:column>																
									<t:column >
										<f:facet name="header">
											<t:outputText value="列印檔狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{afpFileDisPlay.status}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="目前已配表"></t:outputText>
										</f:facet>
										<t:outputText value="#{afpFileDisPlay.matched}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="尚未配表"></t:outputText>
										</f:facet>
										<t:outputText value="#{afpFileDisPlay.notMatched}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="轉檔錯誤"></t:outputText>
										</f:facet>
										<t:outputText value="#{afpFileDisPlay.errors}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="驗單失敗"></t:outputText>
										</f:facet>
										<t:outputText value="#{afpFileDisPlay.verifiedErrs}" />
									</t:column>																																		
									<t:column>
									    <f:facet name="header">
											<t:outputText value="總數" />
										</f:facet>
										<t:outputText value="#{afpFileDisPlay.volumns}" />
									</t:column>									
									
							</t:dataTable>
			    </td>
			</tr>	
			
		</table>
		
	</h:form>
</f:view>
