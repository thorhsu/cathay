$().ready(function loadMenu() {
  	$.ajax({
	    url: '/salmatPAS/secure/menu.serx',
	    error: 	function(xhr) {},
	  	success:function(response) {
	  				$("#menuSpan").html(response);
	  				
	  				$("#menuList").treeview({
	  					collapsed: false,
	  					//animated: "medium",
	  					//control:"#sidetreecontrol",
	  					persist: "location",
	  					unique: false
	  				});
	  				
  					setTimeout(function(){}, 10000);
	  			}
	 });
});


