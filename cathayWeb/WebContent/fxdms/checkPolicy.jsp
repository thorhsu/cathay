<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
	var center = "<t:outputText value="#{checkPolicyBean.center}" />";
	$().ready(function(){	
		var result = "<t:outputText value="#{checkPolicyBean.result}" />";
		$("#substractModifiderName").attr("placeholder", "抽件人姓名");
		if(result.indexOf("抽件") > 0){
			alert("請注意，此件為抽件");
		}
		var verifyResults = <t:outputText value="#{checkPolicyBean.verifyResults}" escape="false"/>;		
		$( "#verifyResult" ).autocomplete({
	        source: verifyResults
	     });
		var jobBagNo = $("#jobBagNo").val();
		if(jobBagNo === ""){
			$("#jobBagNo").focus();
		}else{
			$("#inputNo").select();
		}
		$("#success").change(function(){			
			if(this.value === "true"){
				$("#verifyResult").val("");
				$("#verifyResult").attr("disabled", "diabled");								
				$("#verifyResult").hide();
				$("#substractModifiderName").hide();
				$("#substractModifiderName").val("");
				$("#substractModifiderNm").hide();
			}else if(this.value === "substract"){
				$("#verifyResult").removeAttr("disabled");
				$("#verifyResult").hide();
				$("#verifyResult").val("抽件");
				$("#substractModifiderName").show();
				$("#substractModifiderNm").show();
				if($('#inputNo').val() !== "")
				   $("#substractModifiderName").select();
				else
				   $('#inputNo').select();
			}else{
				$("#verifyResult").removeAttr("disabled");
				if($("#verifyResult").val("") === "抽件")
					$("#verifyResult").val("");
				$("#verifyResult").show();
				$("#substractModifiderName").hide();
				$("#substractModifiderName").val("");
				$("#substractModifiderNm").hide();
			}
		}).change();
		$('#inputNo').keypress( function(event) {
		    if (event.keyCode === 13 && $("#success").val() === "false") {
		    	$("#verifyResult").focus();
		    	return false;
		    }else if (event.keyCode === 13 && $("#success").val() === "substract") {
		    	$("#substractModifiderName").focus();
		    	return false;
		    }
		});
		
		function forceSubmitTest(){
			$.ajaxSettings.async = false;
 		    
 		    var returnBoolean = false;
 		    if($("#inputNo").val() !== ""){
 		       $.getJSON(contextPath + "/secure/preview/pdfQueryServlet.serx?query=forceSubmit", 
 		            {inputNo: $("#inputNo").val()}).always(function(ret) { 			    	        	
 		            	    var result = ret.responseText;
 		            	    if(result === undefined){
 		            	    	result = ret;
 		            	    }
 			   	        	if (result === "NON_EXIST") {
                                alert("無此保單");
 			 	        		returnBoolean = false;
 			   	        		return returnBoolean;
 			   	        	}else if(center === "06" && result === "NOT_POLICY_BOOK"){
 			   	        		alert("非保單右上角號碼，請重新輸入");
 			   	        		$("#forceSubmit").val("false");
		    		    		$("#inputNo").val("");
		    		    		$("#inputNo").select();
 			   	        		returnBoolean = false;
 			   	        		return returnBoolean; 			    	        		
 			   	        	}else if(result >= 60){
 			   	        		if($("#success").val() === "false" || $("#success").val() === "substract" ){
  			   	        		   returnBoolean = window.confirm("此保單已裝箱完成或寄出，改為失敗件或抽件將轉送審查科。\r\n請問是否繼續？"); 
  			   			    	   if(returnBoolean){
  			   			    		   $("#forceSubmit").val("true");
  			   			    	   }else{
  			   			    		   $("#forceSubmit").val("false");
  			   			    		   $("#inputNo").val("");
  			   			    	   }
  			   			    	   return returnBoolean;
  			   	        		}else{
  			   	        			returnBoolean = window.confirm("此保單已裝箱完成或寄出，改驗單正確需要重新裝箱。\r\n請問是否繼續？");
  			   	        			if(returnBoolean){  			   	        			
   			   			    		   $("#forceSubmit").val("true");
   			   			    	   }else{
   			   			    		   $("#forceSubmit").val("false");
   			   			    		   $("#inputNo").val("");
   			   			    	   }
   			   			    	   return returnBoolean;
  			   	        		}
 			   	           }else {
 			   	               $("#forceSubmit").val("true");
 			   			       returnBoolean = true;
 			   	           }
 			   	   });
 		       return returnBoolean;
 		    }else{
 		       return true;
 		    }
		}

		$("#btnQuery").click( function() {
			if($("#forceSubmit").val() === "false"  && !forceSubmitTest()){
				return false;
			}
			
			if($("#inputNo").val() !== ""){				
				if($("#substractModifiderName").val() === "" && $("#success").val() === "substract"){
					alert("抽件時，必須填入抽件人姓名");
					return false;
				}else if($("#verifyResult").val() === "" && $("#success").val() === "false"){					
					alert("驗單失敗時，必須填入理由");
					return false;
				}else if($("#verifyResult").indexOf("抽件") >= 0 && $("#success").val() === "false"){					
					alert("保單抽件時請選擇抽件，並填入抽件人姓名");
					return false;
				} 		       
			}else if($("#substractModifiderName").val() === "" && $("#inputNo").val() !== "" && $("#success").val() === "substract"){
				alert("抽件時，必須填入抽件人姓名");
				return false;
			}else if($("#verifyResult").val() === "" && $("#inputNo").val() !== "" && $("#success").val() === "false"){
				alert("驗單失敗時，必須填入理由");
				return false;
			}else{
				return true;
			}
		});
		

	});
	
	
	
	
	function clearColumn(){
		$("#jobBagNo").val("");
		$("#inputNo").val("");
		$("#verifyResult").val("");
		$("#success").val("false");
		$("#verifyResult").removeAttr("disabled");
		$("#verifyResult").show();
		$("#jobBagNo").focus();
	}
	
  
</script>
	
	<title>[FX人員功能&gt;驗單／抽件維護]</title>

	<h:form id="sbForm">
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">驗單／抽件維護</th>
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
											  id="jobBagNo" value="#{checkPolicyBean.jobBagNo}"
											  size="20" maxlength="19" required="true">
											  <f:validateLength minimum="15" maximum="19" />
										    </t:inputText>
											<h:message for="jobBagNo" style="color:red" />
											<t:outputText value="列印檔：#{checkPolicyBean.newBatchName}" rendered="#{checkPolicyBean.newBatchName ne null && checkPolicyBean.newBatchName ne '' }"/>
											<t:inputHidden value="#{checkPolicyBean.newBatchName}" />
										</td>
									</tr>
									<tr>
										<td align="center" bgcolor="#DFF4DD">保單/簽收單號碼</td>
										<td   bgcolor="#F4FAF3" align="left">										    
										    <t:selectOneMenu id="success" forceId="true" value="#{checkPolicyBean.success}" >                                                                                                    
                                                  <f:selectItem  itemLabel="失敗" itemValue="false"/>
                                                  <f:selectItem  itemLabel="抽件" itemValue="substract" />
                                                  <f:selectItem  itemLabel="成功" itemValue="true"/>                                                                                                    
                                             </t:selectOneMenu> 
										    <t:inputText forceId="true"
											  id="inputNo" value="#{checkPolicyBean.inputNo}"
											  size="20" maxlength="20" />
											 <t:inputHidden id="forceSubmit" forceId="true" value="#{checkPolicyBean.forceSubmit}"/>											 
                                             &nbsp;&nbsp;
                                             <t:inputText forceId="true"
											  id="verifyResult" value="#{checkPolicyBean.verifyResult}"
											  size="50" maxlength="60" />
											 <span id="substractModifiderNm">抽件人姓名</span>
											 <t:inputText forceId="true"
											  id="substractModifiderName" value="#{checkPolicyBean.substractModifiderName}"
											  size="7" maxlength="20" />
										</td>
										<t:inputHidden value="#{checkPolicyBean.cycleDate}" >
										    <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
										</t:inputHidden>
										<t:inputHidden value="#{checkPolicyBean.center}" />
										
										
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
						    <t:commandButton value="送出資料" id="btnQuery" forceId="true" 
							    type="submit" action="#{checkPolicyBean.doSubmit}" />							
                           	<t:commandButton value="批次設定成功"  
							    type="submit" action="#{checkPolicyBean.doSubmitSuccess}" onclick="return(window.confirm('此功能將其它尚未設定的保單或簽收單全設定為驗單成功，是否送出？'))"/>						    
							<t:commandButton value="產出掛號類別清單"  
							    type="submit" action="#{checkPolicyBean.getMailType}" />
						    <input type="button" value="清除" onclick="clearColumn();"/>						   
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
					style="color: red" value="#{checkPolicyBean.result}" /></td>
			</tr>
			<tr>
			    <td align="center">
			       <t:dataTable value="#{checkPolicyBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="applyData" 
										binding="#{checkPolicyBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="#{checkPolicyBean.rowClass}" 
										width="100%" renderedIfEmpty="false">
									<t:column >
										<f:facet name="header">
											<t:outputText value="受理號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.applyNo}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="保單號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.policyNos}">
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="右上角编號 "></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.uniqueNo}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="轄區"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.centerName}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.policyStatusName}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="驗單失敗理由"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.verifyResult}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="Cycel Date"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.cycleDate}">
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />	
										</t:outputText>
									</t:column>
																										
									<t:column>
										<f:facet name="header">
											<t:outputText value="簽收單/保單"></t:outputText>
										</f:facet>
										<t:outputText value="#{(applyData.receipt == true)? '簽收單' : '保單'}" />										
									</t:column>
									<t:column>
									    <f:facet name="header">
											<t:outputText value="抽件" />
										</f:facet>
										<t:outputText value="#{applyData.substract}" />
									</t:column>									
									
							</t:dataTable>
			    </td>
			</tr>	
		</table>
		
	</h:form>
</f:view>
