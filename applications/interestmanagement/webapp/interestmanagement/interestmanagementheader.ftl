<script type="text/javascript">
   jQuery(document).ready(function(){
   
   jQuery('input[name="startDate"]').change(function(){
		 
         var startDate = this.value;
         var endDate =  jQuery('input[name="endDate"]').val();
         var reqUrl = '/interestmanagement/control/fixedDepositContractEnd';
          
          if ((startDate.length > 0) && (endDate.length > 0)){
         	calculateDuration(reqUrl, startDate, endDate);
         }
        });
        
      jQuery('input[name="endDate"]').change(function(){
		 
         var endDate = this.value;
         var startDate =  jQuery('input[name="startDate"]').val();
         var reqUrl = '/interestmanagement/control/fixedDepositContractEnd';
          
          if ((startDate.length > 0) && (endDate.length > 0)){
         	calculateDuration(reqUrl, startDate, endDate);
         }
        });

    jQuery('input[name="periodInMonths"]').change(function(){
		 
         var periodInMonths = this.value;
        
        var startDate =  jQuery('input[name="startDate"]').val();
        
        var reqUrl = '/interestmanagement/control/fixedDepositContractDuration';
          if ((startDate.length > 0) && (periodInMonths.length > 0)){
         	calculateEndDate(reqUrl, startDate, periodInMonths );
        }
      
     });		
	
 });

	function calculateDuration(reqUrl, startDate, endDate){
	    jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'startDate': startDate, 'endDate':endDate}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
	     			
				
						
						$('input[name="periodInMonths"]').val(data.periodInMonths);
						
					
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    }
	    
	    function calculateEndDate(reqUrl, startDate, periodInMonths){
	    	jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'startDate': startDate, 'periodInMonths':periodInMonths}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
					 $('input[name="endDate"]').val(data.endDate);
					 $('input[name="endDate_i18n"]').val(data.endDate_i18n);

	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    }
		
</script>