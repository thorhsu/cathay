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
	<script src="../objects/prog/js/lib/jquery.jqGrid.min.js"
		type="text/javascript"></script>
		
	<script type="text/javascript">
	var contextPath = '<h:outputText escape="false" value="#{facesContext.externalContext.request.contextPath}" />';

	var fileName = "";
    $(function(){    	
    	
	jQuery("#list4").jqGrid({
	            datatype: "local",
	            height: 500,
	            colNames: ['列印檔名', '處理順序','處理狀態','最近更新時間','VIP 設定人','轄區','頁數','Sheets', '本數'],
	            colModel: [{name: 'newBatchName', index: 'newBatchName', align: "right"},	                        	                       
	       	               {name: 'newBatchNo', index: 'newBatchNo', width: 100 , align: "right"},
	       	               {name: 'status', index: 'status', width: 150 , align: "center"},	       	            
	       	               {name: 'updateDateStr', index: 'updateDateStr', width: 160 , align: "center"},
	       	               {name: 'vipModifierName', index: 'policyNos', width: 100 , align: "center"},
	       	               {name: 'center', index: 'center', width: 70 , align: "center"},
	       	               {name: 'pages', index: 'pages', width: 80 , align: "right"},
	       	               {name: 'sheets', index: 'sheets', width: 80 , align: "right"},
	       	               {name: 'volumns', index: 'volumns', width: 100 , align: "right"}],
	       	    onCellSelect: function(rowId, iCol){
   	                  
                },
                /*
private Date presTime; //pres轉檔時間
	private Date printTime; //列印時間
	private Date bindTime;  //膠裝時間
	private Date verifyTime; //驗單時間
	private Date packTime; //裝箱時間
	private Date deliverTime; //交寄時間
                */
                subGrid : true, 
                subGridUrl: contextPath + "/secure/preview/pdfQueryServlet.serx?query=afpSubGrid&cycleDate=true", 
                subGridModel: [{ 
                                 name : [ '受理編號','收件人', 'cycleDate', '被保險人ID','保單號碼','中心代碼','服務中心','目前狀態','簽收單', '抽件','最近更新時間'], 
                                 width : [  100     , 70    ,'100',    80       ,120      ,70        ,120      ,  70     , 50    , 50   , 150     ] ,
                                 align : ['left','left','left','left','left','left','left','left','center','center','left' ] ,  
                                 params: ['newBatchName'] 
                                }],   
                loadError: function(xhr, status, error){ alert(error);alert(JSON.stringify(xhr)); },                                                                                 
	            caption: "查詢結果"
	       }).setGridParam({lastpage:<h:outputText value="#{fxAfpQueryBean.totalPage}" />});
	       
	       var mydata  <h:outputText escape="false" value="#{fxAfpQueryBean.jsonResult}" />;

	       if(mydata !== undefined && mydata !== null && mydata !== ''){
	    	  jQuery("#jqGridDiv").show();
	          for (var i = 0; i <= mydata.length; i++)
	               jQuery("#list4").addRowData(i + 1, mydata[i]);
	       }else{
	    	   jQuery("#jqGridDiv").hide();
		   }	       
	       jQuery("#list4").jqGrid().setGridParam({datatype : 'json'});
	       
	});
    function getSelectedPdfId(rowId){    	
    	var status = $("#list4").getRowData(rowId).status;
    }
  
</script>	
	<link rel="stylesheet" type="text/css" media="screen"
		href="../objects/prog/css/themes/ui.jqgrid.css" />	

	<title>[FX人員功能&gt;FX列印檔查詢]</title>

	<h:form id="sbForm">
		<table width="1200" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">FX列印檔查詢</th>
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
										<td align="center" bgcolor="#DFF4DD">Cycle Date</td>
										<td  width="300" bgcolor="#F4FAF3" align="left">
										    <t:inputText 
											  id="dtSelect1" value="#{fxAfpQueryBean.cycleDate}"
											  size="12" maxlength="10" required="true">
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect1" style="color:red" /> 
										</td>
										<td align="center" bgcolor="#DFF4DD">轄區</td>
										<td width="300" bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="center" forceId="true" value="#{fxAfpQueryBean.center}" >
                                                  <f:selectItems  value="#{fxAfpQueryBean.myCenters}" />                                                  
                                             </t:selectOneMenu>
										   
										</td>
									</tr>
									<tr>										
										<td align="center" bgcolor="#DFF4DD">保單/簽收回條</td>
										<td width="300" bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="receipt" value="#{fxAfpQueryBean.receipt}" >
                                                  <f:selectItem   itemLabel="全部" itemValue=''/>                                                  
                                                  <f:selectItem   itemLabel="保單" itemValue='CA'/>
                                                  <f:selectItem   itemLabel="簽收回條" itemValue='SG'/>
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
							   type="submit" action="#{fxAfpQueryBean.doQuery}" />
						   &nbsp;&nbsp;&nbsp;
					       <t:commandButton value="輸出重量表excel"  forceId="true"
							   type="submit" action="#{fxAfpQueryBean.exportWeightExcel}" />
						   <t:commandButton value="輸出抽件excel" id="exportExcel" forceId="true" 
							   type="submit" action="#{fxAfpQueryBean.exportExcel}" style="display:none"/>
						   &nbsp;&nbsp;&nbsp;
						   <t:commandButton value="輸出北二回送excel" id="exportAdExcel" forceId="true"
							   type="submit" action="#{fxAfpQueryBean.exportAdCsv}" />
						   

							
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
					style="color: red" value="#{fxAfpQueryBean.result}" /></td>
			</tr>
			
            <tr>
				<td>
				<table width="98%" border="0" align="center" cellpadding="0"
					cellspacing="0" id="table_show">
					<tr>
						<td>
						<table width="100%" border="0" cellpadding="0" cellspacing="0" class="outline">
							<tr>
								<td  class="title" align="center">
								      <div align="center" id="jqGridDiv" style="display:none">
		                                 <table id="list4" class="scroll" align="left"></table>
		                                 <table>
		                                    <tr>
		                                       <th>列印檔數</th>
		                                       <td align="right">${fxAfpQueryBean.totalFiles}</td>
		                                       <td></td>
		                                       <td></td>
		                                       <th>保單數</th>
		                                       <td align="right">${fxAfpQueryBean.totalBooks}</td>
		                                       <td></td>
		                                       <td></td>
		                                       <th>簽收回條數</th>
		                                       <td align="right">${fxAfpQueryBean.totalReceipts}</td>
		                                       
		                                    </tr>
		                                    <tr>
		                                       <th>總頁數</th>
		                                       <td align="right">${fxAfpQueryBean.totalPages}</td>
		                                       <td></td>
		                                       <td></td>		                                       
		                                       <th>總張數</th>
		                                       <td align="right">${fxAfpQueryBean.totalSheets}</td>		                                       
		                                       <td></td>
		                                       <td></td>
		                                       <th></th>
		                                       <td ></td>		                                       
		                                    </tr>
		                                 </table>		                          
		                              </div>    
								</td>								
							</tr>

						</table>
						</td>
					</tr>
				</table>
				</td>
			</tr>			
		</table>
	</h:form>
</f:view>
