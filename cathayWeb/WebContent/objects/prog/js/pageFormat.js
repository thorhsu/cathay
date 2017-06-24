$(document).ready( function init() {
	$("#data_info").hide();
	var currPageIndex = $("#currPageIndex");
	var lastPageIndex = $("#lastPageIndex");
	if(currPageIndex != null) {
		if($(currPageIndex).html() == 1) {
			var first = document.getElementById("sbForm:data_scrollerfirst");
			var previous = document.getElementById("sbForm:data_scrollerprevious");
			if(first != null)
				$(first).outerHTML($(first).html());
			if(previous != null)
				$(previous).outerHTML($(previous).html());
		}
	}
	if(currPageIndex != null && lastPageIndex != null) {
		if($(currPageIndex).html() == $(lastPageIndex).html()) {
			var next = document.getElementById("sbForm:data_scrollernext");
			var last = document.getElementById("sbForm:data_scrollerlast");
			if(next != null)
				$(next).outerHTML($(next).html());
			if(last != null)
				$(last).outerHTML($(last).html());
		}
	}
	
	if(lastPageIndex != null && lastPageIndex != "undefined" && lastPageIndex != "") {
		for(var n = 1; n <= lastPageIndex.html(); n++) {
			var currIndex = document.getElementById("sbForm:data_scrolleridx" + n);
			if(currIndex != null) {
				$(currIndex).html($(currIndex).html() + ".");
				if(n == 1) {
					var tmptd = $(currIndex).parent().parent().find("td:first");
					var newtd = document.createElement("td");
					$(newtd).append("第　");
					$(newtd).insertBefore($(tmptd));
				} else if(n == lastPageIndex.html()) {
					var tmptd = $(currIndex).parent().parent().find("td:last");
					var newtd = document.createElement("td");
					$(newtd).append("　頁");
					$(newtd).insertAfter($(tmptd));
				}
			}
		}
	}
	
	if(currPageIndex != null) {
		var selIndex = document.getElementById("sbForm:data_scrolleridx" + currPageIndex.html());
		if(selIndex != null) {
			$(selIndex).outerHTML($(selIndex).html());
		}
	}
	$("#data_info").show();
});