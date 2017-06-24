var detailTxt = "[詳細資料]";
var closeTxt = "[關閉]";
$(document).ready( function init() {
	if($('#selType').val() == 'JOIN_FROM')
		initUI();
	else if($('#selType').val() == 'EPAPER')
		$('#spTotalCount').hide();
});

function initUI() {
	$('#dataList tr').each(function(trIndex) {
		if(trIndex != 0 && $(this).html().indexOf(detailTxt) == -1) { //需排除表頭
			$(this).hide();
		} else {
			$(this).html($(this).html().replace(detailTxt, 
				'<span onclick="showDetail(' + trIndex + 
				')" style="color: #364B2E; cursor: pointer;"><b>' + detailTxt + '</b></span>'));
		}
    });
}

function showDetail(index) {
	var flag = false; //可關閉詳細資料
	$('#dataList tr').each(function(trIndex) {
		if(trIndex != 0) { //需排除表頭
			if(trIndex == index) { //更改詳細資料->關閉
				if($(this).html().indexOf(detailTxt) > -1) { //詳細資料
					$(this).html($(this).html().replace(detailTxt, closeTxt)); 
				} else { //關閉
					$(this).html($(this).html().replace(closeTxt, detailTxt));
					flag = true; //可開啟詳細資料
				}
			}
			if(trIndex > index) {
				if($(this).html().indexOf(detailTxt) > -1 || 
					$(this).html().indexOf(closeTxt) > -1) { //到下一個詳細資料或關閉時就停止
					return false;
				} else {
					if(flag) $(this).hide();
					else $(this).show();
				}
			}
		}
    });	
}
