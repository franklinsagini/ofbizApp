<script type="text/javascript">
   jQuery(document).ready(function(){
    
     jQuery('select[name="leaveTypeId"]').change(function(){
	
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
             else if (leaveTypeId =="COMPASSIONATE_LEAVE"){
         var partyId =  jQuery('input[name="partyId"]').val();
         
         var reqUrl = '/humanres/control/emplCompassionateleavebalance';
          if ((partyId.length > 0) && (leaveTypeId.length > 0)){
         	calculateCompassionateLeaveBalance(reqUrl, leaveTypeId, partyId)
                  }
             }
         
         
        });

   jQuery('input[name="fromDate"]').change(function(){
		 
         var fromDate = this.value;
          var leaveTypeId =  jQuery('select[name="leaveTypeId"]').val();
        if(leaveTypeId == "ANNUAL_LEAVE"){        
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
        
        
        jQuery('input[name="callBackDate"]').change(function(){
		 
         var callBackDate = this.value;
         var callBackDate =  jQuery('input[name="callBackDate"]').val();
         var leaveId =  jQuery('input[name="leaveId"]').val();
         var reqUrl = '/humanres/control/getNewLeaveDuration';
          
          if (callBackDate.length > 0){
         	calculateNewLeaveDuration(reqUrl, callBackDate, leaveId);
         }
        });
        
             
   jQuery('select[name="employmentStatusEnumId"]').change(function(){
		 
         var employmentStatusEnumId = this.value;
         var appointmentdate =  jQuery('input[name="appointmentdate"]').val();
         var employmentStatusEnumId= jQuery('select[name="employmentStatusEnumId"]').val();
         var reqUrl = '/humanres/control/emplConfirmDate';
          
          if ((appointmentdate.length > 0) && (employmentStatusEnumId.length > 0)){
         	calculateConfirmDate(reqUrl, appointmentdate, employmentStatusEnumId);
         }
        });
        
     
       
        
               jQuery('input[name="birthDate"]').change(function(){
		 
         var birthDate = this.value;
         var birthDate =  jQuery('input[name="birthDate"]').val();
         var reqUrl = '/humanres/control/emplRetireDate';
          
          if (birthDate.length > 0){
         	calculateRetireDate(reqUrl, birthDate);
         }
        });
        
        
        
    jQuery('input[name="leaveDuration"]').change(function(){
		 
         var leaveDuration = this.value;
         var leaveTypeId =  jQuery('select[name="leaveTypeId"]').val();
         if (leaveTypeId =="ANNUAL_LEAVE"){        
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
	     			 $('input[name="approvedLeaveSumed"]').val(data.approvedLeaveSumed);
					 $('input[name="accruedLeaveDays"]').val(data.accruedLeaveDays);
					 $('input[name="leaveBalance"]').val(data.leaveBalance);
					 
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    } 
	    
	     function calculateCompassionateLeaveBalance(reqUrl, leaveTypeId, partyId){
	    jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'leaveTypeId': leaveTypeId, 'partyId':partyId}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
	     			 $('input[name="approvedLeaveSumed"]').val(data.approvedLeaveSumed);
					 $('input[name="accruedLeaveDays"]').val(data.accruedLeaveDays);
					 $('input[name="leaveBalance"]').val(data.leaveBalance);
					 
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
						
						$('input[name="resumptionDate"]').val(data.resumptionDate);
					  	$('input[name="resumptionDate_i18n"]').val(data.resumptionDate_i18n);
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
	    
	    
	    function calculateNewLeaveDuration(reqUrl, callBackDate, leaveId){
	    	jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'callBackDate': callBackDate, 'leaveId': leaveId}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
					  
					   $('input[name="newDuration"]').val(data.newDuration);
					  
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
					  
					   $('input[name="resumptionDate"]').val(data.resumptionDate);
					  $('input[name="resumptionDate_i18n"]').val(data.resumptionDate_i18n);
					  
					  
					 
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    }
	    
	     function calculateConfirmDate(reqUrl, appointmentdate, employmentStatusEnumId){
	    	jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'appointmentdate': appointmentdate, 'employmentStatusEnumId': employmentStatusEnumId}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
					 $('input[name="confirmationdate"]').val(data.confirmationdate);
					  $('input[name="confirmationdate_i18n"]').val(data.confirmationdate_i18n);
					  
					  
					 
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    }
	    
	    function calculateRetireDate(reqUrl, birthDate){
	    	jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {'birthDate': birthDate}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
					 $('input[name="retirementdate"]').val(data.retirementdate);
					  $('input[name="retirementdate_i18n"]').val(data.retirementdate_i18n);
					  
					  
					 
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    }
	    
	     function calculatePayrollNumber(reqUrl){
	    	jQuery.ajax({
	
	     url    : reqUrl,
	     type   : 'GET',
	     data   : {}, //here you can pass the parameters to  
	                                                   //the request if any.
	     success : function(data){
					 $('input[name="employeeNumber"]').val(data.employeeNumber);
					  $('input[name="employeeNumber_i18n"]').val(data.employeeNumber_i18n);
					  
					  
					 
	               },
	      error : function(errorData){
	
	              alert("Some error occurred while processing the request");
	              }
	    });
	    }
	    
	   
	   /** ==================EMPLOYEE VALIDATION ==========================================**/
	   
	      function employeeRegistrationFormValidation(){
		/** alert(' Checking for unique fields ... '); **/
		var nationalIDNumber = jQuery('input[name="nationalIDNumber"]').val();
    	var pinNumber  = jQuery('input[name="pinNumber"]').val();
		var mobNo = jQuery('input[name="mobNo"]').val();
    	var emailAddress  = jQuery('input[name="emailAddress"]').val();
		var employeeNumber = jQuery('input[name="employeeNumber"]').val();
		var socialSecurityNumber = jQuery('input[name="socialSecurityNumber"]').val();
    	var nhifNumber  = jQuery('input[name="nhifNumber"]').val();
		var passportNumber = jQuery('input[name="passportNumber"]').val();

    	var isValid = true;
    	var nationalIDNumberState = '';
    	var pinNumberState = '';
    	var passportNumberState = '';
    	var mobileNumberState = '';
		var employeeNumberState = '';
		var nhifNumberState = '';
    	var socialSecurityNumberState = '';
		var emailAddressState = '';
		var idNumberSize = '';

    	var reqUrl = '/humanres/control/employeeRegistrationFormValidation';

    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'nationalIDNumber': nationalIDNumber, 'pinNumber': pinNumber, 'mobNo': mobNo, 'emailAddress': emailAddress, 'employeeNumber': employeeNumber, 'socialSecurityNumber': socialSecurityNumber, 'nhifNumber': nhifNumber, 'passportNumber': passportNumber},
			     success : function(data){

							nationalIDNumberState = data.nationalIDNumberState;
							pinNumberState =  data.pinNumberState;
							passportNumberState =  data.passportNumberState;
							mobileNumberState =  data.mobileNumberState;
							employeeNumberState =  data.employeeNumberState;
							nhifNumberState =  data.nhifNumberState;
							socialSecurityNumberState =  data.socialSecurityNumberState;
							emailAddressState =  data.emailAddressState;
							idNumberSize =  data.idNumberSize;


			               },
			      error : function(errorData){

			              alert("Some error occurred while validating Employee");
			              }


		});

    	var message = '';
    	if ((nationalIDNumberState == 'USED')){
    		message = "ID Number already used, Try another one ! ";
    		isValid = false;
    	}

		if ((pinNumberState == 'USED')){
    		message = message+" PIN Number already used,Try another one  ! ";
    		isValid = false;
    	}

		if ((passportNumberState == 'USED')){
    		message = message+" Passport Number already used, Try another one  ! ";
    		isValid = false;
    	}

		if ((mobileNumberState == 'USED')){
    		message = message+" Mobile Number already used, Try another one  ! ";
    		isValid = false;
    	}

		if ((employeeNumberState == 'USED')){
    		message = "Employee Payroll Number already used, Try another one  ! ";
    		isValid = false;
    	}
    	
		if ((nhifNumberState == 'USED')){
    		message = "NHIF Number already used, Try another one  ! ";
    		isValid = false;
    	}
    	
    	
		if ((socialSecurityNumberState == 'USED')){
    		message = "NSSF Number already used, Try another one  ! ";
    		isValid = false;
    	}
    	
    	
		if ((emailAddressState == 'USED')){
    		message = "Email address already used, Try another one  ! ";
    		isValid = false;
    	}

    	if ((idNumberSize == 'LESS')){
    		message = message+"  ID Number must be greater or equal to 6 characters ! ";
    		isValid = false;
    	}

    	if ((idNumberSize == 'MORE')){
    		message = message+"  ID Number must be less or equal to 8 characters ! ";
    		isValid = false;
    	}


    	if (!isValid){
    		alert(message);
    	} else{
    		
    	}


    	return isValid;

    }
    
    
   
   
   
   
   
   
    /** ==================LEAVE APPLICATION VALIDATION ==========================================**/
	   
	      function staffLeaveFormValidation(){
		/** alert(' Checking for unique fields ... '); **/
		
		
		var leaveTypeId =  jQuery('select[name="leaveTypeId"]').val();
    	var fromDate  = jQuery('input[name="fromDate"]').val();
    	var partyId  = jQuery('input[name="partyId"]').val();
    	var leaveDuration  = jQuery('input[name="leaveDuration"]').val();
    	
    	var isValid = true;
    	var GenderState = '';
    	var NoticePeriodState = '';
    	var durationState = '';

    	 var reqUrl = '/humanres/control/leaveFormValidation';

    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'leaveTypeId': leaveTypeId, 'fromDate': fromDate, 'partyId': partyId, 'leaveDuration': leaveDuration},
			     success : function(data){

							GenderState = data.GenderState;
							NoticePeriodState =  data.NoticePeriodState;
							durationState = data.durationState;
			               },
			      error : function(errorData){

			              alert("Some error occurred while validating Leave");
			              }


		});

    	var message = '';
    	if ((GenderState == 'INVALID')){
    		message = "Leave Not allowed for your Gender !!";
    		isValid = false;
    	}

		if ((NoticePeriodState == 'INVALID')){
    		message = message+" Leave notice too short !!";
    		isValid = false;
    	}
    	
    	if ((durationState == 'INVALID')){
    		message = message+" Given Duration not allowed for this type of leave!!";
    		isValid = false;
    	}
    	
    	

    	
    	if (!isValid){
    		alert(message);
    	} else{
    		
    	}


    	return isValid;
    	
    }
    
    
    
    
    
    
  
    
    
   </script>