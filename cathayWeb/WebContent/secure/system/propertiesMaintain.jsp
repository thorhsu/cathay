<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<link href="../../objects/prog/css/jquery.datepick.css" rel="stylesheet" type="text/css" />
<title>[系統管理&gt;參數修改]</title>
<f:view>
<script lang="text/javascript">
   var checkPwd = function(){
	   if(window.confirm('確認修改資料？')){
	      if($("#password1").val() !== $("#password2").val()){
		      alert("影像Server User Password兩個不相等");
		      return false;
	      }
	      if($("#gpPassword1").val() !== $("#gpPassword2").val()){
		      alert("團險影像Server User Password兩個不相等");
		      return false;
	      }
	      if($("#sfpwd1").val() !== $("#sfpwd2").val()){
		      alert("FXDMS SFTP Password兩個不相等");
		      return false;
	      }
	      if($("#mailPwd1").val() !== $("#mailPwd2").val()){
		      alert("Mail Server Password兩個不相等");
		      return false;
	      }
	      if($("#fileServerPwd1").val() !== $("#fileServerPwd2").val()){
		      alert("File Server Password兩個不相等");
		      return false;
	      }
	      
	      
	      return true;
	   }else{
		   return false;
	   }
   }

</script>
<h:form id="sbForm">
<table width="1000" border="0" cellspacing="0" cellpadding="0">
	<tr><td align="center"><t:outputText id="dataResult" forceId="true" style="color: red" value="#{propertiesMaintainBean.result}"/></td></tr>
	<tr>
		<td align="center" valign="top">
		<table width="1000" border="0" align="center" cellpadding="0"
			cellspacing="0" id="table_criteria">
			<tr>
				<td align="center" valign="top">
				<table width="100%" border="0" cellpadding="0" cellspacing="0"
					class="outline">
					<tr>
						<td class="title">參數修改</td>
					</tr>
					<tr>
						<td>
							<div id="divCriteria">
								<table width="100%" border="0" cellpadding="0" cellspacing="1"
									class="content" id="tbCriteria">
									<tr>
									   <th colspan="8">
									              影像server參數
									   </th>
									</tr>
									<tr>
										<th>Server位置</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.imgServer}" size="5"/>												

										</td>
										<th>User</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.imgServerUser}" size="4"/>												

										</td>
										<th>User Password</th>
										<td >
											<t:inputSecret id="password1" forceId="true" redisplay="true" value="#{propertiesMaintainBean.myProperties.imgServerPwd}" size="4"/>												

										</td>
										<th>Confirm Password</th>
										<td >
											<t:inputSecret id="password2" forceId="true" redisplay="true" value="#{propertiesMaintainBean.myProperties.imgServerPwd}" size="4"/>												

										</td>
									</tr>
									<tr>
									    <th>User Domain</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.imgServerDomain}" size="4"/>												

										</td>
									
										<th>影像檔目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.imgPath}" />												

										</td>
										<th>條款檔目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.lawPath}" />												

										</td>
										<th>測試影像檔目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.testImgPath}" />												

										</td>

									</tr>
									<tr>
									    <th>測試條款檔目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.testLawPath}" />												
										</td>
										<th></th>
										<td ></td>
										<th></th>
										<td ></td>
										<th></th>
										<td ></td>
									</tr>
									<tr>
										<th>團險影像Server</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.gpImgServer}" size="5"/>												

										</td>
										<th>團險User</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.gpImgServerUser}" size="4"/>												

										</td>
										<th>User Password</th>
										<td >
											<t:inputSecret id="gpPassword1" forceId="true" redisplay="true" value="#{propertiesMaintainBean.myProperties.gpImgServerPwd}" size="4"/>												

										</td>
										<th>Confirm Password</th>
										<td >
											<t:inputSecret id="gpPassword2" forceId="true" redisplay="true" value="#{propertiesMaintainBean.myProperties.gpImgServerPwd}" size="4"/>												

										</td>
									</tr>
									<tr>
									    <th>團險Domain</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.gpImgServerDomain}" size="4"/>												

										</td>									
										<th>團險影像檔目錄</th>
										<td >
										   <h:inputText value="#{propertiesMaintainBean.myProperties.remoteGroupImgFolder}" />										
										</td>
										<th>團險條款檔目錄</th>
										<td >
										   <h:inputText value="#{propertiesMaintainBean.myProperties.remoteGroupLawFolder}" />
										</td>
										<th></th>
										<td >																					
										</td>
									</tr>
									<tr><td colspan="8"></td></tr>
									<tr>
									   <th colspan="8">
									             Pres Server參數
									   </th>
									</tr>
									<tr>
									    <th>Batch保單文字檔目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.localPolicyTxtPath}" />												

										</td>								
										<th>測試保單文字檔目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.localTestPolicyPath}" />												

										</td>
										<th>Online保單文字檔目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.localPolicyOnlinePath}" />												

										</td>
										<th>迴歸測試保單目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.localReturnPath}" />												
										</td>
									</tr>
									<tr>
									    <th>要保書影像檔目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.localImgPath}" />												
										</td>									
										<th>測試要保書影像檔目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.localTestImgPath}" />												
										</td>
										<th>OK檔目錄</th>
										<td >                                                
											<h:inputText value="#{propertiesMaintainBean.myProperties.localOKPath}" />												
										</td>
										<th>通過檢核保單目錄</th>
										<td >                                                
											<h:inputText value="#{propertiesMaintainBean.myProperties.checkedOkPath}" />												
										</td>
									    									
									</tr>
									<tr>
									    <th>保單檔案名稱錯誤目錄</th>
										<td >                                                
											<h:inputText value="#{propertiesMaintainBean.myProperties.errorFileNmPath}" />												
										</td>
									    <th>zip檔暫存目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.zipTmpPath}" />												

										</td>								
										<th>備份資料目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.backupFolder}" />												

										</td>
										<th>通過檢核簽收回條目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.receiptOkPath}" />												

										</td>									    										
									</tr>
									<tr>
									    <th>影像檔不全保單目錄</th>
										<td >                                                
											<h:inputText value="#{propertiesMaintainBean.myProperties.imgUncompletePath}" />												
										</td>
									    <th>PRES影像檔目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.imgPostProcessedPath}" />												

										</td>								
										<th>PRES保單目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.presPath}" />												

										</td>
										<th>AFP目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.afpPath}" />												

										</td>									    
									</tr>
									<tr>
									    <th>迴歸測試比對PDF目錄</th>
										<td >                                                
											<h:inputText value="#{propertiesMaintainBean.myProperties.localReturnPdf}" />												
										</td>
									    <th>迴歸測試待使用者確認目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.localReturnForCheck}" />												
										</td>								
										<th>gpg目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.gpgExePath}" />												
										</td>										
										<th>回饋資料目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.feedbackFolder}" />												
										</td>
                                    </tr>
									<tr>
									    <th>AFPtoPdf目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.afpToPdfPath}" />												
										</td>
									    <th>國壽一般備份保留天數</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.filesKeepDays}" />												
										</td>										
										<th>FXDMS備份保留天數</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.fxFilesKeepDays}" />												
										</td>
										<th></th>
										<td></td>										
									</tr>		
									<c:forEach var="backupFolder" items="${propertiesMaintainBean.myProperties.backupFoldersList}" varStatus="status" >									   
									   <c:if test="${status.index % 4 == 0}">
									       <tr>
									   </c:if>
									       <th>指定備份目錄保留天數${status.count}</th>
									       <td>
									          <input type="text" value="${backupFolder}" name="backupFolders" size="11"/>
									          <input type="text" value="${propertiesMaintainBean.myProperties.backupKeepDaysList[status.index]}" name="backupFoldersKeepDays" size="1"/>
									       </td>
									   <c:if test="${status.index % 4 == 3 || status.last}">
									       </tr>
									   </c:if>   									   								
									</c:forEach>
																   
									<tr>
									    <th>團險輸入目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.groupInFolder}" />												
										</td>
									    <th>團險輸出目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.groupOutFolder}" />												
										</td>
										<th>團險備份目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.gpBackupFolder}" />												
										</td>
										<th></th>
										<td >
																							
										</td>
										
									</tr>									    
									
									
									<tr><td colspan="8"></td></tr>
									<tr>
									   <th colspan="8">
									             FXDMS SFTP參數
									   </th>
									</tr>
									<tr>
									    <th>國泰連結SFTP IP</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.fxdmsIP}" />												

										</td>								
										<th>SFTP User</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.fxdmsUser}" />												

										</td>
										<th>SFTP Password</th>
										<td >
											<t:inputSecret id="sfpwd1" redisplay="true" forceId="true" value="#{propertiesMaintainBean.myProperties.fxdmsPwd}" />												
										</td>
										<th>Confirm Password</th>
										<td >
											<t:inputSecret id="sfpwd2" redisplay="true" forceId="true" value="#{propertiesMaintainBean.myProperties.fxdmsPwd}" />												

										</td>										
									</tr>
									<tr>
									    <th>國壽到FXDMS目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.fxdmsUploadPath}" />												

										</td>								
										<th>FXDMS到國壽目錄</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.fxdmsDownloadPath}" />												

										</td>
										<th>fxdms內部到SFTP IP</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.fxSftpIp}" />												
										</td>
										<th></th>
										<td >																							

										</td>										
									</tr>
									
									<tr><td colspan="8"></td></tr>
									<tr>
									   <th colspan="8">
									             Mail Sever相關資訊
									   </th>
									</tr>
									<tr>
									    <th>Mail Server</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.mailHost}" />												

										</td>								
										<th>Mail User</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.mailUserNm}" />												

										</td>
										<th>Mail Password</th>
										<td >
											<t:inputSecret id="mailPwd1" redisplay="true" forceId="true" value="#{propertiesMaintainBean.myProperties.mailPwd}" />												
										</td>
										<th>Confirm Password</th>
										<td >
											<t:inputSecret id="mailPwd2" redisplay="true" forceId="true" value="#{propertiesMaintainBean.myProperties.mailPwd}" />												
										</td>										
									</tr>									
									<tr>
									   <th colspan="2">
									         email通知人員─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.emails}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th colspan="2">
									                   迴歸測試通知人員─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.returnEmails}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th colspan="2">
									                    完成轉檔後通知國壽人員─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.commonEmails}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th colspan="2">
									                       日報表寄送人員─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.dailyReportEmails}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th colspan="2">
									                       北二寄件狀況通知人員─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.tpe2Mail}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th colspan="2">
									               完成傳檔後通知FXDMS人員─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.fxdmsEmails}" size="100"/>
									   </td>
									</tr>
									
									<tr><td colspan="8"></td></tr>
									<tr>
									   <th colspan="8">
									             簡訊通知人員設定
									   </th>
									</tr>
									<tr>
									   <th colspan="2">
									               正常轉檔通知手機號碼─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.commonPhones}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th colspan="2">
									                 異常轉檔通知手機號碼─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.errorPhones}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th colspan="2">
									                 迴歸異常通知手機號碼─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.returnPhones}" size="100"/>
									   </td>
									</tr>
									
									<tr><td colspan="8"></td></tr>
									<tr>
									   <th colspan="8">
									             File Sever相關資訊
									   </th>
									</tr>
									<tr>
									    <th>File Server</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.fileServerIp}" />												

										</td>								
										<th>File Server User</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.fileServerUser}" />												

										</td>
										<th>File Server Password</th>
										<td >
											<t:inputSecret id="fileServerPwd1" redisplay="true" forceId="true" value="#{propertiesMaintainBean.myProperties.fileServerPwd}" />												
										</td>
										<th>Confirm Password</th>
										<td >
											<t:inputSecret id="fileServerPwd2" redisplay="true" forceId="true" value="#{propertiesMaintainBean.myProperties.fileServerPwd}" />												
										</td>										
									</tr>
									<tr>
									    <th>AFPtoPDF路徑</th>
										<td >
											<h:inputText value="#{propertiesMaintainBean.myProperties.fsafpToPdfPath}" />												
										</td>									
									   <th >
									         PDF儲放路徑
									   </th>
									   <td >
									         <h:inputText value="#{propertiesMaintainBean.myProperties.fileServerPdfFolder}" />
									   </td>
									</tr>
									<tr>
									   <th colspan="8">
									               檔案特殊處理
									   </th>
									</tr>
									<tr>
									   <th colspan="2">
									                   暫停處理檔案─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.holdFiles}" size="100"/>
									   </td>
									</tr>
									<tr>
									   <th colspan="2">
									                強迫改為正常件檔案─請用半形逗點(,)分開
									   </th>
									   <td colspan="6">
									         <h:inputText value="#{propertiesMaintainBean.myProperties.forceNormFiles}" size="100"/>
									   </td>
									</tr>									
								</table>
							</div>
						</td>
					</tr>
					<tr>
						<td colspan="4" align="center">
							<br/>
							<t:commandButton value="確定修改" 
								id="btnModify"
								forceId="true"
								type="submit" 
								action="#{propertiesMaintainBean.persist}"
								onclick="return checkPwd()" />
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
