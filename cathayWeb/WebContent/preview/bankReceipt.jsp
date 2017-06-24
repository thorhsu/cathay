<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
<style>
.jqmWindow {
    display: none;    
    position: fixed;
    top: 20%;
    left: 50%;
    
    margin-left: -360px;
    width: 750px;
    
    background-color: #EEE;
    color: #333;
    border: 1px solid black;
    padding: 12px;
}
.jqmOverlay { background-color: #000; }
</style>
<script src="../objects/prog/js/plugins/jquery.browser.js"
		type="text/javascript"></script>    	
<script type="text/javascript" src="../objects/prog/js/sbDate.js"></script>
<script src="../objects/prog/js/src/jqModal.js" type="text/javascript"></script>

<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
	var dialog;
	var form;
	$().ready(function(){
		form = $( "form" );
		$('#dialog-form').jqm();
		
		$("#inputBankReceipt").click(function(){
			$('#dialog-form').jqmShow();
			$("#bankReceiptId1").select();
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
		    	return false;
		    }
		});
		
		$('#clearColumn').click( function(event) {
			clearInputColumn();
		});
	});
	function clearInputColumn(){
		$(".bankReceipt").val("");
		$(".formInput").val("");
		$("input[id$='Hid']").val("");
	}
	var submitForm = function(fxBack){
		
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
		   var today = new Date();
		   var todayStr = today.getFullYear() + "/" + (today.getMonth() + 1) + "/" + today.getDate();
		   if(fxBack === null || fxBack === false){
		      $(document.getElementById("sbForm:dtSelect1")).val(todayStr);
		      $(document.getElementById("sbForm:dtSelect2")).val(todayStr);
		   }
		   submitOrNot = true;
		}else{
		   submitOrNot = false;
		   $('#dialog-form').jqmHide();
		}
		return submitOrNot;
		
	}
  
</script>
	
	<title>國壽人員功能&gt;送金單維護]</title>

	<h:form id="sbForm">        	    
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">送金單維護查詢</th>
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
										<td width="100" align="center"  bgcolor="#DFF4DD">國壽送交日</td>
										<td bgcolor="#F4FAF3" align="left" width="200">
										    <t:inputText styleClass="formInput"
											  id="dtSelect1" value="#{bankReceiptBean.startDate}"
											  size="8" maxlength="10"  >
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect1" style="color:red" /> ~ 
										   <t:inputText styleClass="formInput"
											   id="dtSelect2" value="#{bankReceiptBean.endDate}" size="8"
											   maxlength="10" >
											   <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											   <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect2" style="color:red" />
										</td>
										<td width="100" align="center" bgcolor="#DFF4DD">FX接收日</td>
										<td  bgcolor="#F4FAF3" align="left" >
										    <t:inputText styleClass="formInput"
											  id="dtSelect3" value="#{bankReceiptBean.receiveDateBegin}"
											  size="8" maxlength="10" >
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect3" style="color:red" /> ~
										   <t:inputText styleClass="formInput"
											  id="dtSelect4" value="#{bankReceiptBean.receiveDateEnd}"
											  size="8" maxlength="10" >
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect4" style="color:red" />
										   
										</td>
									</tr>
									<tr>
										<td align="center" bgcolor="#DFF4DD">送金單號碼</td>
										<td  bgcolor="#F4FAF3" align="left">
										   <t:inputText id="bankReceiptId" forceId="true" styleClass="formInput"
											   value="#{bankReceiptBean.bankReceiptId}" size="10" maxlength="10" >								            
										   </t:inputText> 										   
										</td>
										<td align="center" bgcolor="#DFF4DD">保單號碼</td>
										<td  bgcolor="#F4FAF3" align="left" >
										   <t:inputText id="policyNo" forceId="true" styleClass="formInput"
											   value="#{bankReceiptBean.policyNo}" size="10" maxlength="12" >								            
										   </t:inputText> 
										   
										</td>
									</tr>																	
									<tr>										
										<td align="center" bgcolor="#DFF4DD">收件人</td>
										<td width="100" bgcolor="#F4FAF3" align="left" >
										     <t:inputText id="areaId" forceId="true" styleClass="formInput"
											   value="#{bankReceiptBean.receiver}" maxlength="15" />
										</td>
										<td width="100" align="center"  bgcolor="#DFF4DD">Cycle Date</td>
										<td bgcolor="#F4FAF3" align="left">
										    <t:inputText styleClass="formInput"
											  id="dtSelect5" value="#{bankReceiptBean.cycleDateBegin}"
											  size="8" maxlength="10" >
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect5" style="color:red" /> ~ 
										   <t:inputText styleClass="formInput"
											   id="dtSelect6" value="#{bankReceiptBean.cycleDateEnd}" size="8"
											   maxlength="10" >
											   <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											   <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect6" style="color:red" />
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
						   <input type="hidden" name="fromWhere" value="fromCathay" />						   
						   <t:commandButton value="查詢" id="btnQuery" forceId="true"
							   type="submit" action="#{bankReceiptBean.queryResult}" />						   						   
						   <input type="button" id="inputBankReceipt" value="輸入送金單號碼" />
						   <t:commandButton value="刪除勾選送金單" id="delete" forceId="true"  
							   type="submit" action="#{bankReceiptBean.delete}" onclick="return window.confirm('確認刪除？');"/>
						   &nbsp;&nbsp;
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
					style="color: red" value="#{bankReceiptBean.result}" /></td>
			</tr>
			<tr>
			    <td align="center">
			       <t:dataTable value="#{bankReceiptBean.dataModel}"
										id="dataList" 
										forceId="true" 
										rows="30"
										var="bankReceipt" 
										binding="#{bankReceiptBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="even_row,odd_row" 
										width="100%" renderedIfEmpty="false">
									<t:column>
										<f:facet name="header">
											<f:verbatim>
												<input type="checkbox" id="cbSelAll" name="cbSelAll" onclick="selAllCb(this, '#dataList');" />
											</f:verbatim>
										</f:facet>
										<t:selectBooleanCheckbox id="cbSelOne" rendered="#{bankReceipt.receiveDate == null}"/>
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
											<t:outputText value="退回簽收日"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.fxBackReceiveDate}" >
											   <f:convertDateTime pattern="yyyy/MM/dd" timeZone="GMT+8" />										
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="退回簽收人員"></t:outputText>
										</f:facet>
										<t:outputText value="#{bankReceipt.fxBackReceiver}" />
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
		<div id="dialog-form" class="jqmWindow" title="請輸入送金單號碼">
          <fieldset>
             <label for="registerNo">送金單號碼</label>
                <table id="input_criteria" >
                   <tr>
                      <td align="right">1.<t:inputText styleClass="bankReceipt" id="bankReceiptId1" forceId="true" value="#{bankReceiptBean.bankReceiptId1}" size="13"  /></td>
                      <td align="right">2.<t:inputText styleClass="bankReceipt" id="bankReceiptId2" forceId="true"  value="#{bankReceiptBean.bankReceiptId2}" size="13"  /></td>
                      <td align="right">3.<t:inputText styleClass="bankReceipt" id="bankReceiptId3" forceId="true"  value="#{bankReceiptBean.bankReceiptId3}" size="13"  /></td>
                      <td align="right">4.<t:inputText styleClass="bankReceipt" id="bankReceiptId4" forceId="true"  value="#{bankReceiptBean.bankReceiptId4}" size="13"  /></td>
                      <td align="right">5.<t:inputText styleClass="bankReceipt" id="bankReceiptId5" forceId="true"  value="#{bankReceiptBean.bankReceiptId5}" size="13"  /></td>
                   </tr>
                   <tr>
                      <td align="right">6.<t:inputText styleClass="bankReceipt" id="bankReceiptId6" forceId="true"  value="#{bankReceiptBean.bankReceiptId6}" size="13"  /></td>
                      <td align="right">7.<t:inputText styleClass="bankReceipt" id="bankReceiptId7" forceId="true"  value="#{bankReceiptBean.bankReceiptId7}" size="13"  /></td>
                      <td align="right">8.<t:inputText styleClass="bankReceipt" id="bankReceiptId8" forceId="true"  value="#{bankReceiptBean.bankReceiptId8}" size="13"  /></td>
                      <td align="right">9.<t:inputText styleClass="bankReceipt" id="bankReceiptId9" forceId="true"  value="#{bankReceiptBean.bankReceiptId9}" size="13"  /></td>
                      <td align="right">10.<t:inputText styleClass="bankReceipt" id="bankReceiptId10" forceId="true"  value="#{bankReceiptBean.bankReceiptId10}" size="13"  /></td>
                   </tr>
                   <tr>
                      <td align="right">11.<t:inputText styleClass="bankReceipt" id="bankReceiptId11" forceId="true"  value="#{bankReceiptBean.bankReceiptId11}" size="13"  /></td>
                      <td  align="right">12.<t:inputText styleClass="bankReceipt" id="bankReceiptId12" forceId="true"  value="#{bankReceiptBean.bankReceiptId12}" size="13"  /></td>
                      <td  align="right">13.<t:inputText styleClass="bankReceipt" id="bankReceiptId13" forceId="true"  value="#{bankReceiptBean.bankReceiptId13}" size="13"  /></td>
                      <td  align="right">14.<t:inputText styleClass="bankReceipt" id="bankReceiptId14" forceId="true"  value="#{bankReceiptBean.bankReceiptId14}" size="13"  /></td>
                      <td  align="right">15.<t:inputText styleClass="bankReceipt" id="bankReceiptId15" forceId="true"  value="#{bankReceiptBean.bankReceiptId15}" size="13"  /></td>
                   </tr>
                   <tr>
                      <td  align="right">16.<t:inputText styleClass="bankReceipt" id="bankReceiptId16" forceId="true"  value="#{bankReceiptBean.bankReceiptId16}" size="13"  /></td>
                      <td  align="right">17.<t:inputText styleClass="bankReceipt" id="bankReceiptId17" forceId="true"  value="#{bankReceiptBean.bankReceiptId17}" size="13"  /></td>
                      <td  align="right">18.<t:inputText styleClass="bankReceipt" id="bankReceiptId18" forceId="true"  value="#{bankReceiptBean.bankReceiptId18}" size="13"  /></td>
                      <td  align="right">19.<t:inputText styleClass="bankReceipt" id="bankReceiptId19" forceId="true"  value="#{bankReceiptBean.bankReceiptId19}" size="13"  /></td>
                      <td  align="right">20.<t:inputText styleClass="bankReceipt" id="bankReceiptId20" forceId="true"  value="#{bankReceiptBean.bankReceiptId20}" size="13"  /></td>
                   </tr>
                   <tr>
                      <td  align="right">21.<t:inputText styleClass="bankReceipt" id="bankReceiptId21" forceId="true"  value="#{bankReceiptBean.bankReceiptId21}" size="13"  /></td>
                      <td  align="right">22.<t:inputText styleClass="bankReceipt" id="bankReceiptId22" forceId="true"  value="#{bankReceiptBean.bankReceiptId22}" size="13"  /></td>
                      <td  align="right">23.<t:inputText styleClass="bankReceipt" id="bankReceiptId23" forceId="true"  value="#{bankReceiptBean.bankReceiptId23}" size="13"  /></td>
                      <td  align="right">24.<t:inputText styleClass="bankReceipt" id="bankReceiptId24" forceId="true"  value="#{bankReceiptBean.bankReceiptId24}" size="13"  /></td>
                      <td  align="right">25.<t:inputText styleClass="bankReceipt" id="bankReceiptId25" forceId="true"  value="#{bankReceiptBean.bankReceiptId25}" size="13"  /></td>
                   </tr>
                   <tr>
                      <td  align="right">26.<t:inputText styleClass="bankReceipt" id="bankReceiptId26" forceId="true"  value="#{bankReceiptBean.bankReceiptId26}" size="13"  /></td>
                      <td  align="right">27.<t:inputText styleClass="bankReceipt" id="bankReceiptId27" forceId="true"  value="#{bankReceiptBean.bankReceiptId27}" size="13"  /></td>
                      <td  align="right">28.<t:inputText styleClass="bankReceipt" id="bankReceiptId28" forceId="true"  value="#{bankReceiptBean.bankReceiptId28}" size="13"  /></td>
                      <td  align="right">29.<t:inputText styleClass="bankReceipt" id="bankReceiptId29" forceId="true"  value="#{bankReceiptBean.bankReceiptId29}" size="13"  /></td>
                      <td  align="right">30.<t:inputText styleClass="bankReceipt" id="bankReceiptId30" forceId="true"  value="#{bankReceiptBean.bankReceiptId30}" size="13"  /></td>
                   </tr>
			    </table>
			    <div align="center">
			       <t:commandButton value="送出"  id="submitBR" forceId="true"
							   type="submit" action="#{bankReceiptBean.submitBankReceipt}" onclick="return submitForm()"/>
				   &nbsp;&nbsp;
				   <t:commandButton value="退回送金單簽收"  id="backBr" forceId="true"
							   type="submit" action="#{bankReceiptBean.backBr}" onclick="return submitForm()"/>
				</div>
									  
          </fieldset>
        </div>
	</h:form>
</f:view>
