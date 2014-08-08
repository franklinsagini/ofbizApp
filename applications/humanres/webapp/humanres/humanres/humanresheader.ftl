<script type="text/javascript">
   jQuery(document).ready(function(){


   jQuery('input[name="fromDate"]').change(function(){
		 
         var fromDate = this.value;
         
         var thruDate =  jQuery('input[name="thruDate"]').val();
         var reqUrl = '/humanres/control/emplleaveduration';
         alert('Changed from Date');
         alert(fromDate);
          if ((fromDate.length > 0) && (thruDate.length > 0)){
         	calculateDuration(reqUrl, fromDate, thruDate);
         }
         
         
        });
        
   jQuery('input[name="thruDate"]').change(function(){
		 
         var thruDate = this.value;
         var fromDate =  jQuery('input[name="fromDate"]').val();
         var reqUrl = '/humanres/control/emplleaveduration';
           alert('Changed thru Date');
           alert(thruDate);
          if ((fromDate.length > 0) && (thruDate.length > 0)){
         	calculateDuration(reqUrl, fromDate, thruDate);
         }
         
         
        });
		
		 });
     
     

	  function calculateDuration(reqUrl, fromDate, thruDate){
	    jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'fromDate': fromDate, 'thruDate':thruDate}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
					 $('input[name="leaveDuration"]').val(data.leaveDuration);
					 
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    }
   
   </script>