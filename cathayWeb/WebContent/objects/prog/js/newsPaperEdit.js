function dataChk() {
	if($("#newsTitle").val() == "") {
		alert("標題不可為空值！");
		return false;
	} else {
		if($("#newsTitle").val().indexOf(">") > -1 || 
			$("#newsTitle").val().indexOf("<") > -1) {
			alert("標題不可輸入特殊符號<>");
			return false;
		}	
	}
	
	var dt = document.getElementById("sbForm:dtSelect1");
	if($(dt).val() == "") {
		alert("新聞發布日期不可為空值！");
		return false;
	} else {
		if(!isDate(dt)) {
			return false;
		}
	}
	var txtNewsContent = document.getElementById("txtNewsContent")
	if($("#txtNewsContent").val() == "") {
		alert("新聞內容不可為空值！");
		return false;
	} else {
		if($("#txtNewsContent").val().indexOf(">") > -1 || 
			$("#txtNewsContent").val().indexOf("<") > -1) {
			alert("新聞內容不可輸入特殊符號<>");
			return false;
		}
	}
	return true;
}

function preview() {
		
	if(!dataChk()) return;
	$("#btnPreview").hide();
	$("#btnReturn").show();
	$("#divCriteria").hide();
	$("#divPreview").show();
	$("#spNewsTitle").html($("#newsTitle").val());
	$("#spNewsContent").html(addBr($("#txtNewsContent").val()).replace(/\[b\]/g, "<b>").replace(/\[\/b\]/g, "</b>"));
	$("#spNewsDate").html($("#sbForm\\:dtSelect1").val().replace(/\//g, "."));
}

function goBack() {
	$("#btnPreview").show();
	$("#btnReturn").hide();
	$("#divCriteria").show();
	$("#divPreview").hide();
}