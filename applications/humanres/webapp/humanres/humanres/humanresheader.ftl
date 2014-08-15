<script type="text/javascript">
   jQuery(document).ready(function(){
   // Calculate new leave balance //
   jQuery('input[name="leaveDuration"]').change(function(){
		 
         var leaveDuration = this.value;
         var leaveBalance =  jQuery('input[name="leaveBalance"]').val();
               
         var reqUrl = '/humanres/control/emplleaveNewLeaveBalance';
          if ((leaveBalance.length > 0) && (leaveDuration.length > 0)){
         	calculateNewLeaveBalance(reqUrl, leaveDuration, leaveBalance);
         }

        });
     function calculateNewLeaveBalance(reqUrl, leaveDuration, leaveBalance){
	    jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'leaveBalance': leaveBalance, 'leaveDuration':leaveDuration}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
					 $('input[name="newLeaveBalance"]').val(data.newLeaveBalance);
					 
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    } 
   // Calculate annual leave //
   jQuery('select[name="leaveTypeId"]').change(function(){
		 //jQuery('input[name="leaveBalance"]').show();
         var leaveTypeId = this.value;
         //console.log(leaveTypeId);
         if (leaveTypeId =="Annual leave"){
          
         var appointmentdate =  jQuery('input[name="appointmentdate"]').val();
         var partyId =  jQuery('input[name="partyId"]').val();
         
         var reqUrl = '/humanres/control/emplleavebalance';
          if ((partyId.length > 0) && (leaveTypeId.length > 0) && (appointmentdate.length > 0)){
         	calculateBalance(reqUrl, leaveTypeId, partyId,appointmentdate);
         }

         }
         if(leaveTypeId !="Annual leave"){
         	jQuery('input[name="leaveBalance"]').val("");
         	//jQuery('input[name="leaveBalance"]').hide();
         }
         
        });

   jQuery('input[name="fromDate"]').change(function(){
		 
         var fromDate = this.value;
         
         var thruDate =  jQuery('input[name="thruDate"]').val();
         var reqUrl = '/humanres/control/emplleaveduration';
          if ((fromDate.length > 0) && (thruDate.length > 0)){
         	calculateDuration(reqUrl, fromDate, thruDate);
         }
         
         var leaveDuration = jQuery('input[name="leaveDuration"]').val();
         
         var reqUrlLeaveEnd = '/humanres/control/emplleaveend';
         if ((fromDate.length > 0) && (leaveDuration.length > 0)){
         	calculateLeaveEndDate(reqUrlLeaveEnd, fromDate, leaveDuration);
         }
         
         
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
         var fromDate =  jQuery('input[name="fromDate"]').val();
         var leaveBalance =  jQuery('input[name="leaveBalance"]').val();
         var leaveBalances =  parseInt(leaveBalances);
         var leaveDurations =  parseInt(leaveDuration);
         if (leaveDurations > leaveBalances) {
           alert("Leave taken must be less than your leave balance")
         }       
         var reqUrl = '/humanres/control/emplleaveend';
          if ((fromDate.length > 0) && (leaveDuration.length > 0)){
         	calculateLeaveEndDate(reqUrl, fromDate, leaveDuration);
         }
         
         
        });
		
		 });
     
    function calculateBalance(reqUrl, leaveTypeId, partyId, appointmentdate){
	    jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'leaveTypeId': leaveTypeId, 'partyId':partyId ,"appointmentdate" :appointmentdate}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
	     			$('input[name="leaveBalance"]').val(data.leaveBalance);
					 //$('input[name="accruedDay"]').val(data.accruedDay);
					 //$('input[name="balanceDay"]').val(data.balanceDay);
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
					 $('input[name="leaveDuration"]').val(data.leaveDuration);
					 
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