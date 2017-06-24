function addTableRow(table, startRowIndex, maxCount, btnDelId, currRow, realCurrRow) {
	
    if ($(table + ' tr').length > maxCount) {
        alert("超過新增上限");
        return;
    }
    var rawRow = $(table + ' tr:first');
    for (var n = 0; n < startRowIndex; n++)
        rawRow = $(rawRow).next();        
    var firstRowInputs = $(rawRow).find('input').get();
    var nameList = new Array(firstRowInputs.length);
    $.each(firstRowInputs, function(n, item) {
        nameList[n] = $(item).attr("name");
    });

    var firstTbSelects = $(table).find('select').get();
    var selNameList = new Array(firstTbSelects.length);
    $.each(firstTbSelects, function(n, item) {
        selNameList[n] = $(item).attr("name");
    });
    var newRow = rawRow.clone();
    alert(2);
    if(btnDelId != null)
      $(btnDelId, newRow).show();
    else
    	newRow.show();
    alert(3);
    $.each($(newRow).find('input').get(), function(n, item) {
        $(item).attr("value", "");
        $(item).css("background-color", "");
    });
    if(realCurrRow != null)
    	$(newRow).insertBefore($(realCurrRow));
    else if(currRow != null)
       $(newRow).insertBefore($(currRow).parent().parent());
    $(newRow).show();
    var rows = $(table).find('tbody > tr').get();
    var custIndex = 0;
    $.each(rows, function(index, row) {
    var oInputs = $(this).find('input').get();
    var oSelects = $(this).find('select').get();
    var oHrefs = $(this).find('a').get();
        if (index > startRowIndex) {
            $.each(oInputs, function(n, item) {
                $(this).attr("name", nameList[n] + "_" + custIndex);
                $(this).attr("id", nameList[n] + "_" + custIndex);
                if(nameList[n].indexOf("Text1") >= 0 || nameList[n].indexOf("Text5") >= 0 || nameList[n].indexOf("Text6") >= 0){                    
                   	$(this).datepick({
        			   showMonthAfterYear: true, 
        			   showWeeks: true,
        			   dateFormat: 'yy/mm/dd'  
        		    });
                }
            });
            $.each(oSelects, function(n, item) {
                $(this).attr("id", selNameList[n] + "_" + custIndex);
                $(this).attr("name", selNameList[n] + "_" + custIndex);
            });            
            custIndex++;
        }
    });
}
function delTableRow(currRow) {
    var row = $(currRow).parent().parent().parent();
    var delId = $(currRow).parent().parent().prev().prev().children().val();
    deleteRows = deleteRows + delId + ",";
    $("#delIds").val(deleteRows);
    row.fadeOut('normal', function() {
        row.remove();
    });
}
