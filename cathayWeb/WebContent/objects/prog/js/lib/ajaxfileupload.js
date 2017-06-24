//本程式經過Thor修改多次，和網路上抓下來的ajaxfileupload.js有所不同，請不要把網路上抓下來的替換本支程式
//
jQuery.extend({
    createUploadIframe: function(id, uri)
	{
			//create frame
            var frameId = 'jUploadFrame' + id;
            
            if(window.ActiveXObject) {
                var io = document.createElement('<iframe id="' + frameId + '" name="' + frameId + '" />');
                if(typeof uri== 'boolean'){
                    io.src = 'javascript:false';
                }
                else if(typeof uri== 'string'){
                    io.src = uri;
                }
            }
            else {
                var io = document.createElement('iframe');
                io.id = frameId;
                io.name = frameId;
            }
            io.style.position = 'absolute';
            io.style.top = '-1000px';
            io.style.left = '-1000px';

            document.body.appendChild(io);

            return io;			
    },
    createUploadForm: function(id, fileElementId)
	{
		//create form	
		var formId = 'jUploadForm' + id;
		var fileId = 'jUploadFile' + id;
		var form = $('<form  action="" method="POST" name="' + formId + '" id="' + formId + '" enctype="multipart/form-data"></form>');	
		var oldElement = $(fileElementId);
		var newElement = $(oldElement).clone();
		
		//$(oldElement).attr('id', fileId);
		$(newElement).attr('id', fileId); //給予clone出來的DOM元素新id
		$(oldElement).before(newElement);//新元素插入在舊元素之前
		$(oldElement).appendTo(form);//舊元素放到新產生的form之後
		//set attributes
		$(form).css('position', 'absolute');
		$(form).css('top', '-1200px');
		$(form).css('left', '-1200px');
		$(form).appendTo('body');		
		return form;
    },

    ajaxFileUpload: function(s) {
    	// TODO introduce global settings, allowing the client to modify them for all requests, not only timeout		
        s = jQuery.extend({}, jQuery.ajaxSettings, s);
        var id = new Date().getTime();        
		var form = jQuery.createUploadForm(id, s.fileElementId);
		var io = jQuery.createUploadIframe(id, s.secureuri);
		var frameId = 'jUploadFrame' + id;
		var formId = 'jUploadForm' + id;
		var newUploadFileId = '#jUploadFile' + id; //新元素的id
		if(s.async == false){   //增加async的設定
		  jQuery.ajaxSetup( {
			async: false
		  } ); 
		}
		
        // Watch for a new set of requests
        if ( s.global && ! jQuery.active++ )
		{
			jQuery.event.trigger( "ajaxStart" );
		}            
        var requestDone = false;
        // Create the request object
        var xml = {};
        if ( s.global ){
        	jQuery.event.trigger("ajaxSend", [xml, s]);
        }
        //alert(456);	
        // Wait for a response to come back
        var uploadCallback = function(isTimeout)
		{			
			var io = document.getElementById(frameId);
            try 
			{				
				if(io.contentWindow)
				{
					 xml.responseText = io.contentWindow.document.body?io.contentWindow.document.body.innerHTML:null;
                	 xml.responseXML = io.contentWindow.document.XMLDocument?io.contentWindow.document.XMLDocument:io.contentWindow.document;
					 
				}else if(io.contentDocument)
				{
					 xml.responseText = io.contentDocument.document.body?io.contentDocument.document.body.innerHTML:null;
                	xml.responseXML = io.contentDocument.document.XMLDocument?io.contentDocument.document.XMLDocument:io.contentDocument.document;
				}						
            }catch(e)
			{
				jQuery.handleError(s, xml, null, e);
			}
            if ( xml || isTimeout == "timeout") 
			{				
                requestDone = true;
                var status;
                try {
                    status = isTimeout != "timeout" ? "success" : "error";
                    // Make sure that the request was successful or notmodified
                    if ( status != "error" )
					{
                        // process the data (runs the xml through httpData regardless of callback)
                        var data = jQuery.uploadHttpData( xml, s.dataType );    
                        // If a local callback was specified, fire it and pass it the data
                        if ( s.success )
                            s.success( data, status );
    
                        // Fire the global callback
                        if( s.global )
                            jQuery.event.trigger( "ajaxSuccess", [xml, s] );
                    } else
                        jQuery.handleError(s, xml, status);
                } catch(e) 
				{
                    status = "error";
                    jQuery.handleError(s, xml, status, e);
                }

                // The request was completed
                if( s.global )
                    jQuery.event.trigger( "ajaxComplete", [xml, s] );

                // Handle the global AJAX counter
                if ( s.global && ! --jQuery.active )
                    jQuery.event.trigger( "ajaxStop" );

                // Process result
                if ( s.complete )
                    s.complete(xml, status);

                jQuery(io).unbind();

                setTimeout(function()
									{	try 
										{
											//alert("into")
											$(newUploadFileId).after($(s.fileElementId));//舊元素移回新元素之後
										    $(io).remove();
										    $(newUploadFileId).remove(); //新元素移除掉
											$(form).remove();	
											
										} catch(e) 
										{
											jQuery.handleError(s, xml, null, e);
										}									

									}, 100);

                xml = null;

            }
        };
        // Timeout checker
        if ( s.timeout > 0 ) 
		{
        	//alert("into timeout");
            setTimeout(function(){
                // Check to see if the request is still happening
                if( !requestDone ) uploadCallback( "timeout" );
            }, s.timeout);
        }
        //alert(789);
        try 
		{
           // var io = $('#' + frameId);
			var form = $('#' + formId);
			$(form).attr('action', s.url);
			$(form).attr('method', 'POST');
			$(form).attr('target', frameId);
            if(form.encoding)
			{
                form.encoding = 'multipart/form-data';				
            }
            else
			{				
                form.enctype = 'multipart/form-data';
            }			
            $(form).submit();

        } catch(e) 
		{			
            jQuery.handleError(s, xml, null, e);
        }
        
        if(window.attachEvent){
//        	alert("into fileUploadAAA");
            document.getElementById(frameId).attachEvent('onload', uploadCallback);
        }
        else{
        
        	//alert("into fileUploadBBB");
            document.getElementById(frameId).addEventListener('load', uploadCallback, false);
        } 		
        
        return {abort: function () {}};	

    },

    uploadHttpData: function( r, type ) {
    	var data = !type;
         data = (type == "xml") || data ? r.responseXML : r.responseText;
        // If the type is "script", eval it in global context
        if ( type == "script" )
            jQuery.globalEval( data );
        // Get the JavaScript object, if JSON is used.
        if ( type == "json" ){
        	//alert("1:"+data);
        	if(data.indexOf('<pre>') > -1) {          
        		  data = data.substring(5, data.length-6);    //去除<pre>標籤，因為不需要使用
        		//  alert("1.2" + data);
            }
        	var returnData = new Object();
        	returnData.data = data;
        	data = JSON.stringify(data); //使用標準的JSON方法，不再使用eval，
        	                             //因為ie8為了安全性的考量取消eval函式轉JSON物件的功能了
        	                             //但改用JSON.stringify時，則必須引入json2函式庫
            //eval( "data = " + data );
            //alert("1.5:"+data);
        }
        // evaluate scripts within html
        if ( type == "html" ){
            jQuery("<div>").html(data).evalScripts();
			//alert($('param', data).each(function(){alert($(this).attr('value'));}));
        }
    	//alert("2:"+data);        
    	return data;
    }
});

