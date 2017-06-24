$(document).ready( function() {
	var urlMap = $("#urlMap").html();
	if(urlMap == "") return false;
	var items = JSON.parse(urlMap);
	var urlMapTxt = "";
	for(var n = 0; n < items.length; n++)
		urlMapTxt += ">" + items[n][1];
	$("#spUrlMap").html(urlMapTxt);
	
});
