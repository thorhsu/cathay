$(document).ready(function loadDate() {
	var objDt = document.getElementById("sbForm:dtBirthday");
	if(objDt != null) {
		$(objDt).datepicker();
	}
	objDt = document.getElementById("sbForm:dtSelect1");
	if(objDt != null) {
		$(objDt).datepicker({
			dateFormat: "yy/mm/dd",
			changeMonth: true,
		    changeYear: true
		});
	}
	objDt = document.getElementById("sbForm:dtSelect2");
	if(objDt != null) {
		$(objDt).datepicker({
			dateFormat: "yy/mm/dd",
			changeMonth: true,
		    changeYear: true
		});
	}
	objDt = document.getElementById("sbForm:dtSelect3");
	if(objDt != null) {
		$(objDt).datepicker({
			dateFormat: "yy/mm/dd",
			changeMonth: true,
		    changeYear: true
		});
	}
	objDt = document.getElementById("sbForm:dtSelect4");
	if(objDt != null) {
		$(objDt).datepicker({
			dateFormat: "yy/mm/dd",
			changeMonth: true,
		    changeYear: true
		});
	}
	objDt = document.getElementById("sbForm:dtSelect5");
	if(objDt != null) {
		$(objDt).datepicker({
			dateFormat: "yy/mm/dd",
			changeMonth: true,
		    changeYear: true
		});
	}
	objDt = document.getElementById("sbForm:dtSelect6");
	if(objDt != null) {
		$(objDt).datepicker({
			dateFormat: "yy/mm/dd",
			changeMonth: true,
		    changeYear: true
		});
	}
});

