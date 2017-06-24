$(document).ready( function init() {
	chkErr();
});

function selAllCb(obj, dtId) {
	$(dtId).find("input:checkbox").each(function(){
		this.checked = $(obj).is(":checked");
	});
}

function chkErr() {
//	var msg = document.getElementById("dataResult");
//	if(msg && $(msg).html() != "") alert($(msg).html());
}

