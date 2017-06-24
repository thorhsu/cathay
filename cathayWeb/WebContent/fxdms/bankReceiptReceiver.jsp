<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
<script src="../objects/prog/js/plugins/jquery.browser.js"
		type="text/javascript"></script>    	
	<script type="text/javascript" src="../objects/prog/js/sbDate.js"></script>
<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
	var dialog;
	var form;
	$().ready(function(){
		form = $( "form" );
		dialog = $( "#dialog-form" ).dialog({
  	      autoOpen: false,
  	      height: 330,
  	      width: 720,	
  	      modal: true,     	          	      
  	    });
		
		$("#inputBankReceipt").click(function(){
			dialog.dialog( "open" );	
			$("#bankReceiptId1").select();
		});
		
		
		$('#clearColumn').click( function(event) {
			clearInputColumn();
		});
		$('.bankReceipt').keypress( function(event) {	
		    if (event.keyCode === 13) {		
		    	var thisId = $(this).attr("id");
		    	var num = thisId.substring(13, thisId.length);
		    	if(num == '30'){
		    		$("#submitBR").focus();
		    	}else{
		    		num = (parseInt(num) + 1);
		    		$("#bankReceiptId" + num).select();
		    	}
		    }
		});
	});
	function clearInputColumn(){
		$(".bankReceipt").val("");
		$(".formInput").val("");
		$("input[id$='Hid']").val("");
	}
	var returnBr = function(){
		var submitOrNot = true;
		$(".bankReceipt").each(function(index1){
			var outterVal = $(this).val();
			$(".bankReceipt").each(function(index2){
				if(index2 > index1){
				   var innerVal = $(this).val();
				   if(outterVal !== "" && innerVal === outterVal && index1 !== index2){
					   alert((index1 + 1) + ".和 " + (index2 + 1) + ".的送金單號碼相同，" + "均為" + innerVal + "。請檢查後重新輸入");
					   submitOrNot = false;
				   }
				}
			});								
		});
		if(submitOrNot && window.confirm("確認退回國壽？")){		   
			   $(".formInput").val("");
			   $("input[id$='Hid']").val("");		   
			   $(".bankReceipt").each(function(index){
				   var hiddenId = this.id + "Hid";			   
				   $("#" + hiddenId).val($(this).val());
			   });		   			   			   
			   $("#cancelBankReceiver").click();
			   dialog.dialog( "close" );		   		   
			}
			return submitOrNot;
	}
	var submitForm = function(){		
		var submitOrNot = true;
		$(".bankReceipt").each(function(index1){
			var outterVal = $(this).val();
			$(".bankReceipt").each(function(index2){
				if(index2 > index1){
				   var innerVal = $(this).val();
				   if(outterVal !== "" && innerVal === outterVal && index1 !== index2){
					   alert((index1 + 1) + ".和 " + (index2 + 1) + ".的送金單號碼相同，" + "均為" + innerVal + "。請檢查後重新輸入");
					   submitOrNot = false;
				   }
				}
			});								
		});
		if(submitOrNot && window.confirm("確認送出")){		   
		   $(".formInput").val("");
		   $("#centerHidden").val($("#center").val());
		   $("input[id$='Hid']").val("");		   
		   $(".bankReceipt").each(function(index){
			   var hiddenId = this.id + "Hid";			   
			   $("#" + hiddenId).val($(this).val());
		   });		   
		   var today = new Date();
		   var todayStr = today.getFullYear() + "/" + (today.getMonth() + 1) + "/" + today.getDate();
		   
		   $(document.getElementById("sbForm:dtSelect3")).val(todayStr);
		   $(document.getElementById("sbForm:dtSelect4")).val(todayStr);
		   			   
		   $("#submitBankReceiver").click();
		   dialog.dialog( "close" );		   		   
		}
		return submitOrNot;		
	}
  
</script>
	
	<title>FXDMS人員功能&gt;送金單維護]</title>

	<h:form id="sbForm">
	    <div id="dialog-form" title="請輸入送金單號碼">
          <fieldset>
             <label for="registerNo">送金單號碼</label>
                <table id="input_criteria" >
                   <tr>
                      <td align="center" colspan='5'>
                                                                   寄件單位：
                         <select id="center" >
                             <option value="台北">台北</option>
                             <option value="桃園">桃園</option>
                             <option value="台中">台中</option>
                             <option value="台南">台南</option>
                             <option value="高雄">高雄</option>
                         </select>
                      </td>
                   </tr>
                   <tr>
                      <td align="right">1.<input type='text' class="bankReceipt" id="bankReceiptId1" size="15"></td>
                      <td align="right">2.<input type='text' class="bankReceipt" id="bankReceiptId2" size="15"></td>
                      <td align="right">3.<input type='text' class="bankReceipt" id="bankReceiptId3" size="15"></td>
                      <td align="right">4.<input type='text' class="bankReceipt" id="bankReceiptId4" size="15"></td>
                      <td align="right">5.<input type='text' class="bankReceipt" id="bankReceiptId5" size="15"></td>
                   </tr>
                   <tr>
                      <td align="right">6.<input type='text' class="bankReceipt" id="bankReceiptId6" size="15"></td>
                      <td align="right">7.<input type='text' class="bankReceipt" id="bankReceiptId7" size="15"></td>
                      <td align="right">8.<input type='text' class="bankReceipt" id="bankReceiptId8" size="15"></td>
                      <td align="right">9.<input type='text' class="bankReceipt" id="bankReceiptId9" size="15"></td>
                      <td align="right">10.<input type='text' class="bankReceipt" id="bankReceiptId10" size="15"></td>
                   </tr>
                   <tr>
                      <td align="right">11.<input type='text' class="bankReceipt" id="bankReceiptId11" size="15"></td>
                      <td align="right">12.<input type='text' class="bankReceipt" id="bankReceiptId12" size="15"></td>
                      <td align="right">13.<input type='text' class="bankReceipt" id="bankReceiptId13" size="15"></td>
                      <td align="right">14.<input type='text' class="bankReceipt" id="bankReceiptId14" size="15"></td>
                      <td align="right">15.<input type='text' class="bankReceipt" id="bankReceiptId15" size="15"></td>
                   </tr>
                   <tr>
                      <td align="right">16.<input type='text' class="bankReceipt" id="bankReceiptId16" size="15"></td>
                      <td align="right">17.<input type='text' class="bankReceipt" id="bankReceiptId17" size="15"></td>
                      <td align="right">18.<input type='text' class="bankReceipt" id="bankReceiptId18" size="15"></td>
                      <td align="right">19.<input type='text' class="bankReceipt" id="bankReceiptId19" size="15"></td>
                      <td align="right">20.<input type='text' class="bankReceipt" id="bankReceiptId20" size="15"></td>
                   </tr>
                   <tr>
                      <td align="right">21.<input type='text' class="bankReceipt" id="bankReceiptId21" size="15"></td>
                      <td align="right">22.<input type='text' class="bankReceipt" id="bankReceiptId22" size="15"></td>
                      <td align="right">23.<input type='text' class="bankReceipt" id="bankReceiptId23" size="15"></td>
                      <td align="right">24.<input type='text' class="bankReceipt" id="bankReceiptId24" size="15"></td>
                      <td align="right">25.<input type='text' class="bankReceipt" id="bankReceiptId25" size="15"></td>
                   </tr>
                   <tr>
                      <td align="right">26.<input type='text' class="bankReceipt" id="bankReceiptId26" size="15"></td>
                      <td align="right">27.<input type='text' class="bankReceipt" id="bankReceiptId27" size="15"></td>
                      <td align="right">28.<input type='text' class="bankReceipt" id="bankReceiptId28" size="15"></td>
                      <td align="right">29.<input type='text' class="bankReceipt" id="bankReceiptId29" size="15"></td>
                      <td align="right">30.<input type='text' class="bankReceipt" id="bankReceiptId30" size="15"></td>
                   </tr>
			    </table>
			    <div align="center">
			       <input type="button" value="確認接收" id="submitBR" onclick="submitForm()"/>
			           &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			       <input type="button" value="退回國壽" id="returnBR" onclick="returnBr()"/>
			    </div>					  
          </fieldset>
        </div>
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">送金單接收登錄與查詢</th>
					</tr>

					<tr>
						<td align="center">
						<table width="100%" border="0" cellspacing="0" cellpadding="0"
							align="center" bgcolor="#006F3F">
							<tr>
								<td>
								<table id="input_criteria" width="800" border="0" cellspacing="1" cellpadding="6"
									bgcolor="#4AAE66" align="center">
									<tr>
									    <td width="100" align="center" bgcolor="#DFF4DD">FX接收日</td>
										<td  width="200" bgcolor="#F4FAF3" align="left" >
										    <t:inputText styleClass="formInput" 
											  id="dtSelect3" value="#{bankReceiptReceiverBean.receiveDateBegin}"
											  size="8" maxlength="10" >
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect3" style="color:red" /> ~
										   <t:inputText styleClass="formInput" 
											  id="dtSelect4" value="#{bankReceiptReceiverBean.receiveDateEnd}"
											  size="8" maxlength="10" >
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect4" style="color:red" />
										   
										</td>
										<td width="100" align="center"  bgcolor="#DFF4DD">國壽送交日</td>
										<td bgcolor="#F4FAF3" align="left">
										    <t:inputText styleClass="formInput"
											  id="dtSelect1" value="#{bankReceiptReceiverBean.startDate}"
											  size="8" maxlength="10" >
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect1" style="color:red" /> ~ 
										   <t:inputText styleClass="formInput"
											   id="dtSelect2" value="#{bankReceiptReceiverBean.endDate}" size="8"
											   maxlength="10" >
											   <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											   <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect2" style="color:red" />
										</td>
										
									</tr>
									<tr>
										<td align="center" bgcolor="#DFF4DD">送金單號碼</td>
										<td  bgcolor="#F4FAF3" align="left">
										   <t:inputText id="bankReceiptId" forceId="true" styleClass="formInput"
											   value="#{bankReceiptReceiverBean.bankReceiptId}" size="10" maxlength="10" >								            
										   </t:inputText> 										   
										</td>
										<td align="center" bgcolor="#DFF4DD">保單號碼</td>
										<td  bgcolor="#F4FAF3" align="left" >
										   <t:inputText id="policyNo" forceId="true" styleClass="formInput"
											   value="#{bankReceiptReceiverBean.policyNo}" size="10" maxlength="12" >								            
										   </t:inputText> 
										   
										</td>
									</tr>																	
									<tr>										
										<td align="center" bgcolor="#DFF4DD">收件人</td>
										<td width="100" bgcolor="#F4FAF3" align="left" >
										     <t:inputText id="areaId" forceId="true" styleClass="formInput"
											   value="#{bankReceiptReceiverBean.receiver}" maxlength="15" />
										</td>
										<td width="100" align="center"  bgcolor="#DFF4DD">Cycle Date</td>
										<td bgcolor="#F4FAF3" align="left">
										    <t:inputText styleClass="formInput"
											  id="dtSelect5" value="#{bankReceiptReceiverBean.cycleDateBegin}"
											  size="8" maxlength="10" >
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect5" style="color:red" /> ~ 
										   <t:inputText styleClass="formInput"
											   id="dtSelect6" value="#{bankReceiptReceiverBean.cycleDateEnd}" size="8"
											   maxlength="10" >
											   <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											   <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect6" style="color:red" />
										</td>
									</tr>
									<tr>										
										<td align="center" bgcolor="#DFF4DD">寄件單位</td>
										<td width="100" bgcolor="#F4FAF3" align="left" >
										     <t:selectOneMenu id="centerHidden" forceId="true" value="#{bankReceiptReceiverBean.centerHidden}" >
										          <f:selectItem  itemValue="" itemLabel="全部"/>
                                                  <f:selectItem  itemValue="台北" itemLabel="台北"/>
                                                  <f:selectItem  itemValue="桃園" itemLabel="桃園"/>
                                                  <f:selectItem  itemValue="台中" itemLabel="台中"/>
                                                  <f:selectItem  itemValue="台南" itemLabel="台南"/>
                                                  <f:selectItem  itemValue="高雄" itemLabel="高雄"/>                                                 
                                             </t:selectOneMenu>
										</td>
										
									</tr>
								</table>
								</td>
							</tr>
						</table>
						</td>
					</tr>
					<t:inputHidden id="bankReceiptId1Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId1}" />
					<t:inputHidden id="bankReceiptId2Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId2}" />
					<t:inputHidden id="bankReceiptId3Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId3}" />
					<t:inputHidden id="bankReceiptId4Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId4}" />
					<t:inputHidden id="bankReceiptId5Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId5}" />
					<t:inputHidden id="bankReceiptId6Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId6}" />
					<t:inputHidden id="bankReceiptId7Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId7}" />
					<t:inputHidden id="bankReceiptId8Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId8}" />
					<t:inputHidden id="bankReceiptId9Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId9}" />
					<t:inputHidden id="bankReceiptId10Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId10}" />
					<t:inputHidden id="bankReceiptId11Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId11}" />
					<t:inputHidden id="bankReceiptId12Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId12}" />
					<t:inputHidden id="bankReceiptId13Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId13}" />
					<t:inputHidden id="bankReceiptId14Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId14}" />
					<t:inputHidden id="bankReceiptId15Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId15}" />
					<t:inputHidden id="bankReceiptId16Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId16}" />
					<t:inputHidden id="bankReceiptId17Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId17}" />
					<t:inputHidden id="bankReceiptId18Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId18}" />
					<t:inputHidden id="bankReceiptId19Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId19}" />
					<t:inputHidden id="bankReceiptId20Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId20}" />
					<t:inputHidden id="bankReceiptId21Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId21}" />
					<t:inputHidden id="bankReceiptId22Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId22}" />
					<t:inputHidden id="bankReceiptId23Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId23}" />
					<t:inputHidden id="bankReceiptId24Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId24}" />
					<t:inputHidden id="bankReceiptId25Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId25}" />
					<t:inputHidden id="bankReceiptId26Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId26}" />
					<t:inputHidden id="bankReceiptId27Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId27}" />
					<t:inputHidden id="bankReceiptId28Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId28}" />
					<t:inputHidden id="bankReceiptId29Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId29}" />
					<t:inputHidden id="bankReceiptId30Hid" forceId="true" value="#{bankReceiptReceiverBean.bankReceiptId30}" />					
					<tr>
						<td align="center" height="10"></td>
					</tr>
					<tr>
						<td colspan="4" align="center"><br />						   
						   <t:commandButton value="一般查詢" id="btnQuery" forceId="true"
							   type="submit" action="#{bankReceiptReceiverBean.queryResult}" />
						   <t:commandButton value="查詢未配保單送金單" 
							   type="submit" action="#{bankReceiptReceiverBean.queryNoBr}" />
							   &nbsp;&nbsp;|&nbsp;&nbsp;
						   <t:commandButton value="送出" id="submitBankReceiver" forceId="true" style="display:none" 
							   type="submit" action="#{bankReceiptReceiverBean.submitBankReceiver}" onclick="return true"/>						   
						   <t:commandButton value="取消" id="cancelBankReceiver" forceId="true" style="display:none" 
							   type="submit" action="#{bankReceiptReceiverBean.cancelBankReceiver}" onclick="return true"/>
						   <input type="button" id="inputBankReceipt" value="輸入送金單號碼" />
						   <t:commandButton value="刪除勾選送金單" id="delete" forceId="true"  
							   type="submit" action="#{bankReceiptReceiverBean.delete}" onclick="return window.confirm('確認刪除？');"/>						   
						   &nbsp;&nbsp;|&nbsp;&nbsp;
						   <input type="button" id="clearColumn" value="清除" onclick="clearColumn()"/>
						   	
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
					style="color: red" value="#{bankReceiptReceiverBean.result}" /></td>
			</tr>
			<tr>
			    <td align="center">
			       <t:dataTable value="#{bankReceiptReceiverBean.dataModel}"
										id="dataList" 
										forceId="true" 
										rows="30"
										var="bankReceipt" 
										binding="#{bankReceiptReceiverBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="even_row,odd_row" 
										width="100%" renderedIfEmpty="false">
                                    <t:column>
										<f:facet name="header">
											<f:verbatim>
												<input type="checkbox" id="cbSelAll" name="cbSelAll" onclick="selAllCb(this, '#dataList');" />
											</f:verbatim>
										</f:facet>
										<t:selectBooleanCheckbox  id="cbSelOne" rendered="#{bankReceipt.issueDate == null && bankReceipt.matchDate == null && bankReceipt.packDate == null}"/>
									</t:column>											
									<t:column >
										<f:facet name="header">
											<t:outputText value="送金單號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.bankReceiptId}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="保單號碼"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.applyData.policyNos}">
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="Cycle Date"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.applyData.cycleDate}" >
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
										</t:outputText>
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="收件人"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.applyData.receiver}">
										</t:outputText>
									</t:column>
									
									<t:column >
										<f:facet name="header">
											<t:outputText value="保單狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.applyData.policyStatusName}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="國壽送交日"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.issueDate}" >
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="登錄人員"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.issueUser}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="FX接收日"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.receiveDate}" >
										   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="送交單位"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.center}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="接收順序"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.dateCenterSerialNo}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="FX接收人員"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.receiveUser}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="寄送日期"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.packDate}" >
											   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />										
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="FX寄送人員"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.packUser}" />
									</t:column>								
									<t:column >
										<f:facet name="header">
											<t:outputText value="送金單狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.status}" />
									</t:column>
							</t:dataTable>
			    </td>
			</tr>
            <tr>
			    	<td align="center">
								<br />
								<t:dataScroller id="info_scroller" for="dataList"
									rowsCountVar="rowsCount"
									displayedRowsCountVar="displayedRowsCountVar"
									firstRowIndexVar="firstRowIndex" lastRowIndexVar="lastRowIndex"
									pageCountVar="pageCount" pageIndexVar="pageIndex">
									<h:outputFormat value="顯示第{0}~{1}筆資料，共{2}筆，{3}/{4}頁">
										<f:param value="#{firstRowIndex}" />
										<f:param value="#{lastRowIndex}" />
										<f:param value="#{rowsCount}" />										
										<f:param value="#{pageIndex}" />
										<f:param value="#{pageCount}" />										
									</h:outputFormat>
									<%
										//For css style判斷
									%>
									<t:outputText id="currPageIndex" forceId="true"
										value="#{pageIndex}" style="display: none" />
									<t:outputText id="lastPageIndex" forceId="true"
										value="#{pageCount}" style="display: none" />
								</t:dataScroller>
							</td>
						</tr>
						<tr>
							<td align="center">
								<br/>
								<t:dataScroller id="data_scroller"
									for="dataList" 
									fastStep="10" 
									paginator="true"
									displayedRowsCountVar="displayedRowsCountVar"
									paginatorMaxPages="9"
									paginatorTableStyle="text-align:center; margin-left:auto; margin-right:auto;"
									paginatorActiveColumnStyle="font-weight:bold;font-size:13px;color: #333333;"
									renderFacetsIfSinglePage="false">
									<f:facet name="first">
										<t:outputText id="firstPage" forceId="true" value="第一頁" />
									</f:facet>
									<f:facet name="last">
										<t:outputText id="lastPage" forceId="true" value="最末頁" />
									</f:facet>
									<f:facet name="previous">
										<t:outputText id="previousPage" forceId="true" value="上一頁" />
									</f:facet>
									<f:facet name="next">
										<t:outputText id="nextPage" forceId="true" value="下一頁" />
									</f:facet>
								</t:dataScroller>
					    	</td>
					    </tr>
				
		</table>
		
	</h:form>
</f:view>
