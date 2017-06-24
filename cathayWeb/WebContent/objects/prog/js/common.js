function removeArray(array, attachId) {
	for ( var i = 0, n = 0; i < array.length; i++) {
		if (array[i] != attachId) {
			array[n++] = array[i];
		}
	}
	array.length -= 1;
}

Array.prototype.remove = function(obj) {
	return removeArray(this, obj);
};

//Thor 12/30增加
String.prototype.endsWith = function(t, i) {
	 if (i==false) { 
		 return (t == this.substring(this.length - t.length)); 
	} else { 
		return (t.toLowerCase() == this.substring(
			this.length - t.length).toLowerCase()); 
   } 
} 



function openUrl(url, opt) {
	if (opt == null || opt == 0) // current window
		window.location = url;
	else if (opt == 1) // new window
		window.open(url);
	else if (opt == 2) { // background window
		window.open(url);
		self.focus();
	}
}

jQuery.fn.outerHTML = function(s) {
	return (s)
	? this.before(s).remove()
	: jQuery("<p>").append(this.eq(0).clone()).html();
};

function scroll2Top() {
	$('html, body').animate({scrollTop:0}, 'medium');
}

function getPara(name) {
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var regexS = "[\\?&]"+name+"=([^&#]*)";
	var regex = new RegExp( regexS );
	var results = regex.exec(window.location.href);
	if( results == null )
		return "";
	else
		return results[1];
}

/**
 * For ajax checking session timeout
 */
function isLoginTimeout(resp) {
	if(resp.indexOf("id=\"login_main\"") > -1)
		return true;
	return false;
}

function addBr(val) {
	return val.replace(/\n/g, "<br>");
}