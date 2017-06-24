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
<script src="../objects/prog/js/src/jqModal.js" type="text/javascript"></script>
<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';
	$().ready(function(){
		var prevClass = "";
		var prevTr = "";
		var rowCount = 1;
		var exceptionRow = false;
        $('#dialog-form').jqm();
		
		$("#backCathay").click(function(){
			$('#dialog-form').jqmShow();
			$("#substractModifiderName").select();
		});
		$("#cancelBack").click(function(){
			$('#dialog-form').jqmHide();
			$("#substractModifiderName").val("");
		});		
		
		//調整rowspan及
		$('#dataList > tbody > tr').each(function(){
			var classes = $(this).attr("class").split(" ");			
			if(classes.length > 1){			   
			   if(classes[1] === prevClass){
				   if(!exceptionRow)
					  exceptionRow = (classes[0] === "exception_row");
			       
				   rowCount++;
				   var firstTd = $(this).find("td").first();
				   $(firstTd).hide();
				   $(firstTd).next().hide();
				   $(firstTd).next().next().hide();
				   $(firstTd).next().next().next().hide();
				   $(firstTd).next().next().next().next().next().next().next().hide();
				   $(firstTd).next().next().next().next().next().next().next().next().hide();
			   }else{
				   if(rowCount != 1){
					   //加入rowspan
					   var firstTd = $(prevTr).find("td").first();					   
					   $(firstTd).attr("rowspan", rowCount);
					   $(firstTd).next().attr("rowspan", rowCount);
					   $(firstTd).next().next().attr("rowspan", rowCount);
					   $(firstTd).next().next().next().attr("rowspan", rowCount);
					   $(firstTd).next().next().next().next().next().next().next().attr("rowspan", rowCount);
					   $(firstTd).next().next().next().next().next().next().next().next().attr("rowspan", rowCount);
					   if(exceptionRow){
						   $(firstTd).addClass("exception_row");
						   $(firstTd).next().addClass("exception_row");
						   $(firstTd).next().next().addClass("exception_row");
						   $(firstTd).next().next().next().addClass("exception_row");
						   $(firstTd).next().next().next().next().next().next().next().addClass("exception_row");
						   $(firstTd).next().next().next().next().next().next().next().next().addClass("exception_row");
					   }
				   }
				   exceptionRow = (classes[0] === "exception_row");
				   rowCount = 1;
			       prevTr = this;
			       prevClass = classes[1];
		      }
	      }
	   });
	});
	function selectOne(obj){
		var thisId = $(obj).attr('id');		
		var hidden = document.getElementById(thisId + 'Hidden');
		var thisValue = hidden.value;
		return thisValue;
	}
	function getId(){
		if(confirm("確認退回國壽？")){
		   var substractModifiderName = $("#substractModifiderName").val();
		   if(substractModifiderName == ""){
			   alert("請填入要求抽件人員名字");
			   return false;
		   }
		   var applyIds = "";
    	   $('#dataList').find("input:checkbox:checked").each(function(){    		 
			    var applyId = selectOne(this);			 
			    applyIds = applyIds  + applyId + ",";			     		
		   });
		   if(applyIds != "")
			   applyIds = applyIds.substring(0, applyIds.length - 1);				
		   $("#oldBatchNames").val(applyIds);
		   if(applyIds === ""){
			  alert("請勾選後再送出");
			  return false;
		   }else{
		      return true;
		   }
		}else{
	        $('#dialog-form').jqmHide();			
			return false;
		}
	}
	
  
</script>
	
	<title>[FX人員功能&gt;送金單未送達保單]</title>

	<h:form id="sbForm">
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">送金單未送達保單</th>
					</tr>
				</table>
				</td>
			</tr>
			<tr>
			    <td>							   
				   <button type="button"  id="backCathay" >直接退回國壽</button>
				   <t:inputHidden value="#{noBankReceiptBean.oldBatchNames}" id="oldBatchNames" forceId="true"/>                            							 
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
					style="color: red" value="#{noBankReceiptBean.result}" /></td>
			</tr>
			<tr>
			    <td align="center">
			       <t:dataTable value="#{noBankReceiptBean.dataModel}"
										id="dataList" 
										forceId="true" 
										var="applyData" 
										binding="#{noBankReceiptBean.dataTable}"
										columnClasses="col_center,col_center,col_center,col_center,col_center,col_center,col_center,col_center" 																				
										rowClasses="#{noBankReceiptBean.rowClass}" 
										width="100%" renderedIfEmpty="false">
									<t:column>
										<f:facet name="header">
										   <t:outputText value=""/>											
										</f:facet>
										<t:selectBooleanCheckbox id="cbSelOne" />
										<t:inputHidden id="cbSelOneHidden" value="#{applyData.oldBatchName}" />
									</t:column>		
									<t:column >
										<f:facet name="header">
											<t:outputText value="受理號碼"></t:outputText>
										</f:facet>
										<t:outputText  value="#{applyData.applyNo}" />
									</t:column>
									<t:column>
										<f:facet name="header">
											<t:outputText value="保單號碼"></t:outputText>
										</f:facet>
										<t:outputText  value="#{applyData.policyNos}">
										</t:outputText>
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="右上角编號 "></t:outputText>
										</f:facet>
										<t:outputText  value="#{applyData.uniqueNo}" />
									</t:column>									
									<t:column >
										<f:facet name="header">
											<t:outputText value="送金單號碼 "></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.bankReceiptId}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="國壽發送人員"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.center}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="送金單狀態"></t:outputText>
										</f:facet>
										<t:outputText value="#{applyData.exceptionStatus}" />
									</t:column>
									<t:column >
										<f:facet name="header">
											<t:outputText value="原來狀態"></t:outputText>
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
																																			
									
							</t:dataTable>
			    </td>
			</tr>	
		</table>
		<div id="dialog-form" class="jqmWindow" title="輸入國壽要求抽件人員名字">
          <fieldset>
                <label for="substractModifiderName">填入名字
                   <table id="input_criteria" >
                      <tr>
                         <th align="right">國壽要求抽件人員名字：</th>
                         <td align="right"><t:inputText id="substractModifiderName" forceId="true" value="#{noBankReceiptBean.substractModifiderName}"   /></td>
                      </tr>
			       </table>
			    </label>			 
			    <div align="center">
			       <t:commandButton value="送出"  
							   type="submit" action="#{noBankReceiptBean.backCathay}" onclick="return getId();"/>
					&nbsp;&nbsp;
				   <button type="button" id="cancelBack" >取消</button>
				</div>
									  
          </fieldset>
        </div>
		
	</h:form>
</f:view>
