<script type="text/javascript">
   jQuery(document).ready(function(){
    
     jQuery('select[name="leaveTypeId"]').change(function(){
		 //jQuery('input[name="leaveBalance"]').show();
         var leaveTypeId = this.value;
         //console.log(leaveTypeId);
         if (leaveTypeId =="ANNUAL_LEAVE"){
          var appointmentdate =  jQuery('input[name="appointmentdate"]').val();
         var partyId =  jQuery('input[name="partyId"]').val();
         
         var reqUrl = '/humanres/control/emplleavebalance';
          if ((partyId.length > 0) && (leaveTypeId.length > 0) && (appointmentdate.length > 0)){
         	calculateBalance(reqUrl, leaveTypeId, partyId,appointmentdate);
                  }
             }
         if(leaveTypeId !="ANNUAL_LEAVE"){
         	jQuery('input[name="approvedLeaveSumed"]').val("");
         	jQuery('input[name="accruedLeaveDays"]').val("");
         	jQuery('input[name="leaveBalance"]').val("");
         	jQuery('input[name="leaveDuration"]').val("");
         }
         
        });

   jQuery('input[name="fromDate"]').change(function(){
		 
         var fromDate = this.value;
          var leaveTypeId =  jQuery('input[name="leaveTypeId"]').val();
        // if(leaveTypeId == "ANNUAL_LEAVE"){        
         var thruDate =  jQuery('input[name="thruDate"]').val();
         var reqUrl = '/humanres/control/emplleaveduration';
          if ((fromDate.length > 0) && (thruDate.length > 0)){
         	calculateDuration(reqUrl, fromDate, thruDate);
         }
         
         var leaveDuration = jQuery('input[name="leaveDuration"]').val();
         var leaveBalance = jQuery('input[name="leaveBalance"]').val();

         var excessdays =	leaveDuration - leaveBalance;
         var reqUrlLeaveEnd = '/humanres/control/emplleaveend';
         if ((fromDate.length > 0) && (leaveDuration.length > 0) && (excessdays <= 0) ){
         	calculateLeaveEndDate(reqUrlLeaveEnd, fromDate, leaveDuration);
         }
         else if((excessdays > 0)){
         	alert("Leave taken must be less than your leave balance of " +leaveBalance+ "days.");
         	$('input[name="leaveDuration"]').val("");
            $('input[name="thruDate_i18n"]').val("");
          
                 }
         //   }
       });
        
   jQuery('input[name="thruDate"]').change(function(){
		 
         var thruDate = this.value;
         var fromDate =  jQuery('input[name="fromDate"]').val();
         var reqUrl = '/humanres/control/emplleaveduration';
          
          if ((fromDate.length > 0) && (thruDate.length > 0)){
         	calculateDuration(reqUrl, fromDate, thruDate);
         }
        });
        
        
    jQuery('input[name="leaveDuration"]').change(function(){
		 
         var leaveDuration = this.value;
         var leaveTypeId =  jQuery('input[name="leaveTypeId"]').val();
         //if (leaveTypeId =="ANNUAL_LEAVE"){        
        var fromDate =  jQuery('input[name="fromDate"]').val();
        
        var leaveBalance =  jQuery('input[name="leaveBalance"]').val();

        var diff = leaveDuration - leaveBalance;
        var reqUrl = '/humanres/control/emplleaveend';
          if ((fromDate.length > 0) && (leaveDuration.length > 0) && (diff <= 0)){
         	calculateLeaveEndDate(reqUrl, fromDate, leaveDuration);
         }
         else if((diff > 0)){
         	alert("Leave taken must be less than your leave balance of " +leaveBalance+ "days.");
         	$('input[name="leaveDuration"]').val("");
         $('input[name="thruDate_i18n"]').val("");
          
         }
     //}

        });
		
		 });
     
    function calculateBalance(reqUrl, leaveTypeId, partyId, appointmentdate){
	    jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'leaveTypeId': leaveTypeId, 'partyId':partyId ,"appointmentdate" :appointmentdate}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
	     			 $('input[name="approvedLeaveSumed"]').val(data.approvedLeaveSumed);
					 $('input[name="accruedLeaveDays"]').val(data.accruedLeaveDays);
					 $('input[name="leaveBalance"]').val(data.leaveBalance);
					 //$('input[name="leaveBalance"]').val(data.leaveBalance);
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    } 

	  function calculateDuration(reqUrl, fromDate, thruDate){
	    jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'fromDate': fromDate, 'thruDate':thruDate}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
	     			$('input[name="leaveDuration"]').val("");
					var leaveBalance =  $('input[name="leaveBalance"]').val();
					var leave =  data.leaveDuration;
					var excessdays =leave -leaveBalance;
					if(excessdays <= 0){
						
						$('input[name="leaveDuration"]').val(data.leaveDuration);
					}
					else if (excessdays > 0){
					alert("Leave longer than your leave balance");
					 $('input[name="leaveDuration"]').val("");
					 $('input[name="thruDate_i18n"]').val("");

					 }
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    }
	    
	    function calculateLeaveEndDate(reqUrl, fromDate, leaveDuration){
	    	jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'fromDate': fromDate, 'leaveDuration':leaveDuration}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
					 $('input[name="thruDate"]').val(data.thruDate);
					  $('input[name="thruDate_i18n"]').val(data.thruDate_i18n);
					 
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    }
   
   </script>