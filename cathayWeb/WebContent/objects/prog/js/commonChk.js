var errColor = "#FFFFDD";
function isEng(obj, space){
	var reg;
	var msg = "此欄位僅接受英文！";
	$(obj).css("background-color", ""); 
	if(space) {
		reg = /[^a-zA-Z ]/;
	} else {
		reg = /[^a-zA-Z]/;
		msg = "此欄位僅接受英文，不接受空白與符號！";
	}
    if(reg.test(obj.value)){
    	$(obj).css("background-color", errColor); 
    	alert(msg);
    	return false;
    }
    return true;
}

function isEngAndNum(obj, space){
	var reg;
	var msg = "此欄位僅接受英文與數字！";
	$(obj).css("background-color", ""); 
	if(space) {
		reg = /[^0-9a-zA-Z ]/;
	} else {
		reg = /[^0-9a-zA-Z]/;
		 msg = "此欄位僅接受英文與數字，不接受空白與符號！";
	}
    if(reg.test(obj.value)){
    	$(obj).css("background-color", errColor); 
    	alert(msg);
    	return false;
    }
    return true;
}

function isEngAndPun(space) {
	var reg;
	var msg;
	$(obj).css("background-color", ""); 
	if(space) {
		reg = /[^,.a-zA-Z ]/;
		msg = "此欄位僅接受英文！";
	} else {
		reg = /[^,.a-zA-Z]/;
		msg = "此欄位僅接受英文，不接受空白！";
	}
    if(reg.test(obj.value)){
    	$(obj).css("background-color", errColor); 
    	alert(msg);
    	return false;
    }
    return true;
}

function isChn(obj, space){
	var reg;
	var msg = "此欄位僅接受中文！";
	$(obj).css("background-color", ""); 
	if(space) {
		reg = /[^\u4E00-\u9FA5 ]/;
	} else {
		reg = /[^\u4E00-\u9FA5]/;
		msg = "此欄位僅接受中文，不接受空白與符號！";
	}
    if(reg.test(obj.value)){
    	$(obj).css("background-color", errColor); 
    	alert(msg);
    	return false;
    }
    return true;
}

function isChnAndEng(obj, space){
	var msg = "此欄位僅接受中英文！";
	$(obj).css("background-color", ""); 
	if(space) {
		reg = /[^a-zA-Z\u4E00-\u9FA5 ]/;
	} else {
		reg = /[^a-zA-Z\u4E00-\u9FA5]/;
		msg = "此欄位僅接受中英文，不接受空白與符號！";
	}
    if(reg.test(obj.value)){
    	$(obj).css("background-color", errColor); 
    	alert(msg);
    	return false;
    }
    return true;
}

function isChnAndEngAndNum(obj, space){
	var reg;
	var msg = "此欄位僅接受中英文與數字！";
	$(obj).css("background-color", ""); 
	if(space) {
		reg = /[^0-9a-zA-Z\u4E00-\u9FA5 ]/;
	} else {
		reg = /[^0-9a-zA-Z\u4E00-\u9FA5]/;
		msg = "此欄位僅接受中英文與數字，不接受空白與符號！";
	}
    if(reg.test(obj.value)){
    	$(obj).css("background-color", errColor); 
    	alert(msg);
    	return false;
    }
    return true;
}

function isChnAndEngAndNumAndPun(obj, space){
	var reg;
	var msg = "此欄位僅接受中英文與數字！";
	$(obj).css("background-color", ""); 
	if(space) {
		reg = /[^.,0-9a-zA-Z\u4E00-\u9FA5\u2027\uff0c\u3001-\u3030\uff1a-\uff1f ]/;
	} else {
		reg = /[^.,0-9a-zA-Z\u4E00-\u9FA5\u2027\uff0c\u3001-\u3030\uff1a-\uff1f]/;
	}
    if(reg.test(obj.value)){
    	$(obj).css("background-color", errColor); 
    	alert(msg);
    	return false;
    }
    return true;
}

function isInt(obj, isPositive){
	$(obj).css("background-color", "");
	if(obj.value == "") return true;
	if(isPositive) {
		if(parseInt(obj.value) == obj.value && parseInt(obj.value) > 0) {
			obj.value = parseInt(obj.value);
		} else {
			$(obj).css("background-color", errColor); 
			alert("此欄位僅接受正整數！");
			return false;
		}
	} else {
		if(!(parseInt(obj.value) == obj.value)) {
			$(obj).css("background-color", errColor); 
			alert("此欄位僅接受整數！");
			return false;
		}
	}
	return true;
}

function chkYearByRange(obj, minYear, maxYear) {
	$(obj).css("background-color", ""); 
	if(obj.value == "") return true;
	if(parseInt(obj.value) == obj.value && 
			minYear <= parseInt(obj.value) && 
			parseInt(obj.value) <= maxYear) {
		obj.value = parseInt(obj.value);
	} else {
		$(obj).css("background-color", errColor); 
		alert("此欄位僅接受西元" + minYear + "至" + maxYear + "年！");
		return false;
	}
	return true;
}

function chkYear(obj) {
	$(obj).css("background-color", ""); 
	if(obj.value == "") return true;
	if(parseInt(obj.value) == obj.value && 1901 <= parseInt(obj.value) && parseInt(obj.value) <= (new Date()).getFullYear()) {
		obj.value = parseInt(obj.value);
	} else {
		$(obj).css("background-color", errColor); 
		alert("此欄位僅接受西元1901至" + (new Date()).getFullYear() + "年！");
		return false;
	}
	return true;
}

function chkBirthday(obj) {
	$(obj).css("background-color", "");
	if(obj.value == "" || isNaN(Date.parse(obj.value))) return true;
	var dt = new Date(obj.value);
	if(1901 > dt.getFullYear() || dt > new Date()) {
		$(obj).css("background-color", errColor); 
		obj.value = "";
		alert("生日僅接受西元1901/01/01至今日！");
		return false;
	}
	return true;
}

function chkMonth(obj) {
	$(obj).css("background-color", "");
	if(obj.value == "") return true;
	if(parseInt(obj.value) == obj.value && 1 <= parseInt(obj.value) && parseInt(obj.value) <= 12) {
		obj.value = parseInt(obj.value);
	} else {
		$(obj).css("background-color", errColor); 
		alert("此欄位僅接受月份！");
		return false;
	}
	return true;
}

function isDate(obj){
	$(obj).css("background-color", ""); 
	if(obj.value == "") return true;
	if(isNaN(Date.parse(obj.value))) {
		$(obj).css("background-color", errColor); 
		obj.value = "";
		alert("日期格式錯誤！");
		return false;
	}
	return true;
}

function isNum(obj, space){
	var reg;
	$(obj).css("background-color", ""); 
	if(space) {
		reg = /[^0-9 ]/;
	} else {
		reg = /[^0-9]/;
	}
    if(reg.test(obj.value)){
    	$(obj).css("background-color", errColor); 
    	alert("此欄位僅接受數字！");
    	return false;
    }
    return true;
}

function chkEmail(obj) {
	$(obj).css("background-color", ""); 
	if(obj.value == "") return true;
	var reg = /^[^\s]+@[^\s]+\.[^\s]{2,3}$/;
	if (!reg.test(obj.value)) {
		$(obj).css("background-color", errColor); 
		alert("請輸入正確的email！");
		return false;
	}
	return true;
}

function chkID(obj) {
	$(obj).css("background-color", ""); 
	obj.value = obj.value.replace(/[\s　]+/g, "");
	obj.value = obj.value.toUpperCase();
	var reg = /^[A-Z]\d{9}$/;
	if (!reg.test(obj.value)) {
		$(obj).css("background-color", errColor); 
		alert("請輸入正確的身分證！");
		return false;
	}

	if (document.regular.gender[0].chked != (2 - obj.value.charAt(1))) {
		alert("性別與身分證不符合！");
		return false;
	}
	var temp = obj.value.charAt(0);
	var tempsum = firstcal = seccal = 0;
	switch (temp) {
		case 'A': temp = 10;break;
		case 'B': temp = 11;break;
		case 'C': temp = 12;break;
		case 'D': temp = 13;break;
		case 'E': temp = 14;break;
		case 'F': temp = 15;break;
		case 'G': temp = 16;break;
		case 'H': temp = 17;break;
		case 'I': temp = 34;break;
		case 'J': temp = 18;break;
		case 'K': temp = 19;break;
		case 'L': temp = 20;break;
		case 'M': temp = 21;break;
		case 'N': temp = 22;break;
		case 'O': temp = 35;break;
		case 'P': temp = 23;break;
		case 'Q': temp = 24;break;
		case 'R': temp = 25;break;
		case 'S': temp = 26;break;
		case 'T': temp = 27;break;
		case 'U': temp = 28;break;
		case 'V': temp = 29;break;
		case 'W': temp = 32;break;
		case 'X': temp = 30;break;
		case 'Y': temp = 31;break;
		case 'Z': temp = 33;break;
	};
	firstcal = seccal = temp;
	with (Math) {
		for (var i = 8, c = 1; i > 0; i--)
			tempsum += obj.value.charAt(i) * (c++);
		firstcal = floor(firstcal / 10);
		seccal %= 10;
		seccal *= 9;
		temp = tempsum + firstcal + seccal;
		temp %= 10;
		if (temp == 0)
			temp = 10;
		var chkd = 10 - temp;
	}

	if (chkd != obj.value.charAt(9)) {
		$(obj).css("background-color", errColor); 
		alert("請輸入正確的身分證！");
		return false;
	}
	return true;
}

function chkTextArea(obj, max) {
	$(obj).css("background-color", ""); 
	if(obj.value.length > max) {
		$(obj).css("background-color", errColor); 
		alert("已達最大字數上限 ：" + max + "字！");
		return false;
	}
	var reg = /[^.,0-9a-zA-Z\u4E00-\u9FA5\u2027\uff0c\u3001-\u3030\uff1a-\uff1f\u000a\u000d []\/]/;
	          
    if(reg.test(obj.value)){
    	$(obj).css("background-color", errColor); 
    	alert("此欄位不可輸入特殊符號！");
		return false;
	}
	return true;
}
function isEmpty(obj) {
	$(obj).css("background-color", "");
	if(jQuery.trim(obj.value) == "") {
		$(obj).css("background-color", errColor); 
		alert("此欄位不可空白！");
		return true;
	}
	return false;
}
function isSelEmpty(obj) {
	$(obj).css("background-color", "");
	if(jQuery.trim(obj.value) == "") {
		$(obj).css("background-color", errColor); 
		alert("請選擇資料！");
		return true;
	}
	return false;
}