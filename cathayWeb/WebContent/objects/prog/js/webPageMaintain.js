$(document).ready( function() {
	var urlMap = $("#urlMap").html();
	if(urlMap == "") return false;
	var items = JSON.parse(urlMap);
	var urlMapTxt = "";

	for(var n = 0; n < items.length; n++)
		urlMapTxt += ">" + "<a href='webPageMaintain.jspx?pid=" + items[n][0] + "'>" + items[n][1] + "</a>";
	$("#spUrlMap").html(urlMapTxt);
	
	if(items == undefined || items.length <= 1) $("#divPrev").hide();
	else $("#divPrev").show();
	if(items.length >= 3) {
		$("#dataList td").each(function() {
			if($(this).html().indexOf("子項") > -1) {
				$(this).html("子項");
			};
		});
	}
});
