<script>
        $(document).ready(function(){
        
			checkMemberType();

            $("#NewMember").validate({

                rules:{
                    firstName:{"required": true},
                    lastName:{"required": true}

                },
                messages:{
                    firstName:"<a font style='color:red'>  FirstName is Required</a>"   ,
                    lastName:"<a font style='color:red'> Last Name is Required </a>"

                }
            });


         jQuery('select[name="accountProductId"]').change(function(){

         	var accountProductId = this.value;
         	var partyId = jQuery('input[name="partyId"]').val();
        	var reqUrl = '/partymgr/control/generateAccountNumber';

         	if ((accountProductId.length > 0)){

				generateAccountNumber(reqUrl, partyId, accountProductId );
         	}

        });
        
     	 
     	
     	jQuery('select[name="memberTypeId"]').change(function(){
			selectedItem = jQuery('select[name="memberTypeId"] option:selected').text();
			selectedItem = selectedItem.toUpperCase();
			
			if (selectedItem == 'BUSINESS'){
         		jQuery('input[name="employeeNumber"]').parent().parent().hide();
         		//jQuery('#NewMember_employeeNumber_title').hide();
         		
         		
         		jQuery('input[name="payrollNumber"]').parent().parent().hide();
         		
         		jQuery('select[name="employmentTypeId"]').parent().parent().parent().hide();
         		
         		
         	} else{
         		jQuery('input[name="employeeNumber"]').parent().parent().show();
         		//jQuery('#NewMember_employeeNumber_title').show();
         		
         		jQuery('input[name="payrollNumber"]').parent().parent().show();
         		jQuery('select[name="employmentTypeId"]').parent().parent().parent().show();
         	}
         	
         	
         	//$('#NewMember').reset();
        });  
        
     });

 /***
 	Generate Account Number Branch-Product-MemberNo-Sequence
 */
 function generateAccountNumber(reqUrl, partyId, accountProductId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'partyId': partyId, 'accountProductId': accountProductId}, //here you can pass the parameters to
                                                   //the request if any.
     success : function(data){
				 $('input[name="accountNo"]').val(data.accountNumber);


               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }


    });

   }
   
   function checkMemberType(){
           	selectedItem = jQuery('select[name="memberTypeId"] option:selected').text();
			selectedItem = selectedItem.toUpperCase();
			
			if (selectedItem == 'BUSINESS'){
         		jQuery('input[name="employeeNumber"]').parent().parent().hide();
         		//jQuery('#NewMember_employeeNumber_title').hide();
         		
         		
         		//jQuery('input[name="payrollNumber"]').parent().parent().hide();
         		
         		jQuery('select[name="employmentTypeId"]').parent().parent().parent().hide();
         		
         		
         	} else{
         		jQuery('input[name="employeeNumber"]').parent().parent().show();
         		//jQuery('#NewMember_employeeNumber_title').show();
         		
         		//jQuery('input[name="payrollNumber"]').parent().parent().show();
         		jQuery('select[name="employmentTypeId"]').parent().parent().parent().show();
         	}
   }

   /***
   	Validate Member details - ID Number, Payroll Number, Pin Number, Employee Number must all be unique
   **/
    function memberRegistrationFormValidation(){
		/** alert(' Checking for unique fields ... '); **/
		var partyId = jQuery('input[name="partyId"]').val();
		var idNumber = jQuery('input[name="idNumber"]').val();
    	var pinNumber  = jQuery('input[name="pinNumber"]').val();
		var payrollNumber = jQuery('input[name="payrollNumber"]').val();
    	var mobileNumber  = jQuery('input[name="mobileNumber"]').val();
		var employeeNumber = jQuery('input[name="employeeNumber"]').val();
		
		var memberType = jQuery('select[name="memberTypeId"] option:selected').text();
		memberType = memberType.toUpperCase();

    	var isValid = true;
    	var idNumberState = '';
    	var pinNumberState = '';
    	var payrollNumberState = '';
    	var mobileNumberState = '';
		var employeeNumberState = '';
		var idNumberSize = '';
		
		if (partyId != ''){
			return true;
		}

    	var reqUrl = '/partymgr/control/memberRegistrationFormValidation';

    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'idNumber': idNumber, 'pinNumber': pinNumber, 'payrollNumber': payrollNumber, 'mobileNumber': mobileNumber, 'mobileNumber': mobileNumber, 'memberType': memberType},
			     success : function(data){

							idNumberState = data.idNumberState;
							pinNumberState =  data.pinNumberState;
							payrollNumberState =  data.payrollNumberState;
							mobileNumberState =  data.mobileNumberState;
							employeeNumberState =  data.employeeNumberState;
							idNumberSize =  data.idNumberSize;
							jQuery('input[name="payrollNumber"]').val(data.payrollNumber);

					    	//alert('collateralsAvailable  inanon'+collateralsAvailable);
							//alert('guarantorsAvailable inanon'+guarantorsAvailable);
							//alert('guarantorsTotalDepositsEnough  inanon'+guarantorsTotalDepositsEnough);
							//alert('eacherGuarantorGreaterThanAverage  inanon '+eacherGuarantorGreaterThanAverage);


			               },
			      error : function(errorData){

			              alert("Some error occurred while validating member");
			              }


		});

    	var message = '';
    	if ((idNumberState == 'USED')){
    		message = "ID Number already used, it must be unique ! ";
    		isValid = false;
    	}

		if ((pinNumberState == 'USED')){
    		message = message+" PIN Number already used, it must be unique ! ";
    		isValid = false;
    	}

		if ((payrollNumberState == 'USED')){
    		message = message+" Payroll Number already used, it must be unique ! ";
    		isValid = false;
    	}

		if ((mobileNumberState == 'USED')){
    		message = message+" Mobile Number already used, it must be unique ! ";
    		isValid = false;
    	}

		if ((employeeNumberState == 'USED') && (memberType != 'BUSINESS')){
    		message = "Employee Number already used, it must be unique ! ";
    		isValid = false;
    	}

    	if ((idNumberSize == 'LESS')){
    		message = message+"  ID Number must be greater than or equal to 6 characters ! ";
    		isValid = false;
    	}

    	if ((idNumberSize == 'MORE')){
    		message = message+"  ID Number must be less than or equal to 8 characters ! ";
    		isValid = false;
    	}


    	if (!isValid){
    		alert(message);
    	} 

    	return isValid;

    }
 </script>
