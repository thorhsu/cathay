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
	            colNames: ['cycle date', '列印檔名', '說明', '列印順序','處理狀態','最近更新時間','VIP 設定人','轄區','頁數','本數','張數'],
	            colModel: [{name: 'cycleDateStr', index: 'cycleDate', sorttype: "date", width: 100, align: "center", sortable:false},
	                       {name: 'newBatchName', index: 'newBatchName', align: "left", sortable:false},	                        
	                       {name: 'description', index: 'description', width: 130, align: "left", sortable:false},
	       	               {name: 'newBatchNo', index: 'newBatchNo', width: 50 , align: "right", sortable:false},
	       	               {name: 'status', index: 'status', width: 100 , align: "center", sortable:false},	       	            
	       	               {name: 'updateDateStr', index: 'updateDateStr', width: 160 , align: "center", sortable:false},
	       	               {name: 'vipModifierName', index: 'policyNos', width: 70 , align: "center", sortable:false},
	       	               {name: 'center', index: 'center', width: 70 , align: "center", sortable:false},
	       	               {name: 'pages', index: 'pages', width: 80 , align: "right", sortable:false},
	       	               {name: 'volumns', index: 'volumns', width: 100 , align: "right", sortable:false},
	       	               {name: 'sheets', index: 'sheets', width: 80 , align: "right", sortable:false}],
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
                subGridUrl: contextPath + "/secure/preview/pdfQueryServlet.serx?query=afpSubGrid", 
                subGridModel: [{ 
                                 name : [ '序號', '受理編號','要保人', '被保險人ID','保單號碼','中心代碼','服務中心','目前狀態','簽收單', '抽件','最近更新時間'], 
                                 width : [  20,  100     , 70    ,    80       ,240      ,70        ,120      ,  70     , 50    , 50   , 150     ] ,
                                 align : [ 'right','left','left','left','left','left','left','left','center','center','left' ] ,  
                                 params: ['newBatchName'] 
                                }],   
                loadError: function(xhr, status, error){ alert(error);alert(JSON.stringify(xhr)); },                                                                                 
	            caption: "查詢結果"
	       }).setGridParam({lastpage:<h:outputText value="#{afpQueryBean.totalPage}" />});
	       
	       var mydata = <h:outputText escape="false" value="#{afpQueryBean.jsonResult}" />;

	       if(mydata != ''){
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

	<title>[國壽人員功能&gt;列印檔查詢]</title>

	<h:form id="sbForm">
		<table width="1000" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellspacing="0" cellpadding="0"
					id="table_criteria">
					<tr>
						<th class="title">列印檔查詢</th>
					</tr>

					<tr>
						<td align="center">
						<table width="100%" border="0" cellspacing="1" cellpadding="1"
							 bgcolor="#006F3F">
							<tr>
								<td align="center">
								<table id="input_criteria" width="800" border="0" cellspacing="1" cellpadding="6"
									bgcolor="#4AAE66" >
									<tr>
										<td width="15%" align="center" bgcolor="#DFF4DD">*Cycle Date區間</td>
										<td width="35%"  bgcolor="#F4FAF3" align="left">
										    <t:inputText 
											  id="dtSelect1" value="#{afpQueryBean.cycleDate}"
											  size="12" maxlength="10" required="true">
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect1" style="color:red" /> 
										   ~
										   <t:inputText 
											  id="dtSelect2" value="#{afpQueryBean.cycleDateEnd}"
											  size="12" maxlength="10" required="true">
											  <f:convertDateTime timeZone="GMT+8" pattern="yyyy/MM/dd" />
											  <f:validator validatorId="validator.Date" />
										   </t:inputText> <h:message for="dtSelect2" style="color:red" />
										</td>
										<td width="15%" align="center" bgcolor="#DFF4DD">行政中心</td>
										<td width="35%" bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="center" forceId="true" value="#{afpQueryBean.center}" >
                                                  <f:selectItems  value="#{afpQueryBean.myCenters}" />                                                  
                                             </t:selectOneMenu>
										   
										</td>
									</tr>
									<tr>										
										<td align="center" bgcolor="#DFF4DD">保單/簽收回條</td>
										<td  bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="receipt" value="#{afpQueryBean.receipt}" >
                                                  <f:selectItem   itemLabel="全部" itemValue=''/>                                                  
                                                  <f:selectItem   itemLabel="保單" itemValue='CA'/>
                                                  <f:selectItem   itemLabel="簽收回條" itemValue='SG'/>
                                                  <f:selectItem   itemLabel="團險證" itemValue='PD'/>
                                             </t:selectOneMenu>										   
										</td>
										<td align="center" bgcolor="#DFF4DD">團險/個險</td>
										<td  bgcolor="#F4FAF3" align="left" >
										    <t:selectOneMenu id="groupInsure" value="#{afpQueryBean.groupInsure}" >
                                                  <f:selectItem   itemLabel="全部" itemValue='all'/>                                                  
                                                  <f:selectItem   itemLabel="個險" itemValue='personal'/>
                                                  <f:selectItem   itemLabel="團險" itemValue='group'/>
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
							   type="submit" action="#{afpQueryBean.doQuery}" />
						   &nbsp;&nbsp;&nbsp;
						   <t:commandButton value="輸出excel" id="exportExcel" forceId="true"
							   type="submit" action="#{afpQueryBean.exportExcel}" />
							
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
					style="color: red" value="#{afpQueryBean.result}" /></td>
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
								         <table>		                                    
		                                    <tr>
		                                       <th>新契約保單數</th>
		                                       <td align="right">${afpQueryBean.norms}</td>
		                                       <th>新契約保單張數</th>
		                                       <td align="right">${afpQueryBean.normSheets}</td>		                                       
		                                       <th>保補契轉保單數</th>
		                                       <td align="right">${afpQueryBean.convs}</td>
		                                       <th>保補契轉保單張數</th>
		                                       <td align="right">${afpQueryBean.convSheets}</td>                             		                                       		                                       		                                      		                                       
		                                    </tr>
		                                    <tr>
		                                       <th></th>
		                                       <td></td>
		                                       <th>個險印製簽收回條數</th>
		                                       <td align="right">${afpQueryBean.printedReceipts}</td>
		                                       <th>個險免印製簽收回條數</th>
		                                       <td align="right">${afpQueryBean.notPrinteds}</td>
		                                       <th></th>
		                                       <td></td>		                                    
		                                    </tr>
		                                    <tr>
		                                       <th>團險保單數</th>
		                                       <td align="right">${afpQueryBean.groups}</td>
		                                       <th>團險保單張數</th>
		                                       <td align="right">${afpQueryBean.groupSheets}</td>		                                       
		                                       <th>團險簽收回條數</th>
		                                       <td align="right">${afpQueryBean.groupReceipts}</td>
		                                       <th>團險證張數</th>
		                                       <td align="right">${afpQueryBean.insureCards}</td>                             		                                       		                                       		                                      
		                                    </tr>
		                                    <tr>
		                                       <th>列印檔數</th>
		                                       <td align="right">${afpQueryBean.totalFiles}</td>
		                                       <th>全部保單數</th>
		                                       <td align="right">${afpQueryBean.totalBooks}</td>		                                       
		                                       <th>全部簽收回條數</th>
		                                       <td align="right">${afpQueryBean.totalReceipts}</td>		                                       
		                                       <th>總列印張數</th>
		                                       <td align="right">${afpQueryBean.totalSheets}</td>		                                       
		                                    </tr>
		                                 </table>
		                                 <table id="list4" class="scroll" align="left"></table>		                                 		                          
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
