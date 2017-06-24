A4J.AJAX.onError = function(req, status, message) {
    window.alert("Custom onError handler "+status+" msg="+message);
    var btnDel = document.sbForm['sbForm:btnDel'];
	if(btnDel != undefined)
    	$(btnDel).attr('disabled', false);
};