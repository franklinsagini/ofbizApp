<script>
   $(document).ready(function(){

        
     	 
     	
     	jQuery('select[name="treasuryTypeId"]').change(function(){
     	
     		//$("#NewTreasury input[name=name]")
			selectedItem = jQuery('#NewTreasury select[name="treasuryTypeId"] option:selected').text();
			selectedItem = selectedItem.toUpperCase();
			//alert(selectedItem);
			
			if ((selectedItem == 'VAULT') || (selectedItem == 'TELLER')){
         		jQuery('#NewTreasury select[name="finAccountId"]').parent().parent().parent().hide();
         		
         		
         		
         	} else{
         		jQuery('#NewTreasury select[name="finAccountId"]').parent().parent().parent().show();
         	}
         	
         	
         	//$('#NewMember').reset();
        });  
        
        jQuery('select[name="glAccountId"]').change(function(){
     	
			selectedItem = jQuery('#NewTreasury select[name="glAccountId"] option:selected').val();
			selectedItem = selectedItem.toUpperCase();
			//alert(selectedItem);
			
			accountHasBeenUsed(selectedItem);
         	
         	//$('#NewMember').reset();
        }); 
        
        
        //employeeResponsible for the employee
        jQuery('select[name="employeeResponsible"]').change(function(){
     	
			selectedItem = jQuery('#NewTreasury select[name="employeeResponsible"] option:selected').val();
			selectedItem = selectedItem.toUpperCase();
			//alert(selectedItem);
			
			employeeHasBeenGivenTreasury(selectedItem);
         	
         	//$('#NewMember').reset();
        }); 
        
        
        //destinationTreasury
         jQuery('#NewTreasuryTransfer select[name="destinationTreasury"]').change(function(){
     	
			selectedItem = jQuery('#NewTreasuryTransfer select[name="destinationTreasury"] option:selected').val();
			selectedItem = selectedItem.toUpperCase();
			//alert(selectedItem);
			
			//employeeHasBeenGivenTreasury(selectedItem);
			getAssignedEmployee(selectedItem);
         	
         	//$('#NewMember').reset();
        }); 
        
     });
     
     
	// Check if account has already been used
	    function accountHasBeenUsed(glAccountId){
		/** alert(' Checking for unique fields ... '); **/
		var isValid = true;
    	//var reqUrl = '/partymgr/control/memberRegistrationFormValidation';
    	var reqUrl = '/treasurymanagement/control/accountHasBeenUsed';

    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'glAccountId': glAccountId},
			     success : function(data){

							usedState = data.usedState;

			               },
			      error : function(errorData){

			              alert("Some error occurred while validating account");
			              }


		});

    	var message = '';
    	//alert(usedState);
    	if ((usedState == true)){
    		message = "Account Has already been assigned to a treasury (Bank/Vault/Teller) please try another ! ";
    		jQuery('#NewTreasury select[name="glAccountId"] option:selected').val('');
    		jQuery('#NewTreasury select[name="glAccountId"] option:selected').text('');
    		
    		jQuery('#NewTreasury select[name="glAccountId"]').parent().parent().hide();
    		jQuery('#NewTreasury select[name="glAccountId"]').parent().parent().show();
    		isValid = false;
    	}

	
    	if (!isValid){
    		alert(message);
    	} 

    	return isValid;

    }
    
    
    //employeeHasBeenGivenTreasury
    //Check that employee has already been given a treasury
	function employeeHasBeenGivenTreasury(employeeResponsible){
		var isValid = true;
    	//var reqUrl = '/partymgr/control/memberRegistrationFormValidation';
    	//var reqUrl = '/treasurymanagement/control/accountHasBeenUsed';
    	
    	var reqUrl = '/treasurymanagement/control/employeeHasBeenGivenTreasury';

    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'employeeResponsible': employeeResponsible},
			     success : function(data){

							usedState = data.usedState;

			               },
			      error : function(errorData){

			              alert("Some error occurred while validating account");
			              }


		});

    	var message = '';
    	//alert(usedState);
    	if ((usedState == true)){
    		message = "Employee Has already been assigned to a treasury (Bank/Vault/Teller) please try another ! ";
    		jQuery('#NewTreasury select[name="employeeResponsible"] option:selected').val('');
    		jQuery('#NewTreasury select[name="employeeResponsible"] option:selected').text('');
    		
    		jQuery('#NewTreasury select[name="employeeResponsible"]').parent().parent().hide();
    		jQuery('#NewTreasury select[name="employeeResponsible"]').parent().parent().show();
    		isValid = false;
    	}

	
    	if (!isValid){
    		alert(message);
    	} 

    	return isValid;

    }
    
    
    //Get Assigned Employee
    function getAssignedEmployee(destinationTreasury){
		var isValid = true;
    	//var reqUrl = '/partymgr/control/memberRegistrationFormValidation';
    	//var reqUrl = '/treasurymanagement/control/accountHasBeenUsed';
    	
    	//var reqUrl = '/treasurymanagement/control/employeeHasBeenGivenTreasury';
    	var reqUrl = '/treasurymanagement/control/getAssignedEmployee';
    	var employeeResponsible;
    	var employeeNames;
    	var bdLimitAmount;
    	var bdTreasuryAvailable;
    	var bdMaxAllowedTransfer;

    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'destinationTreasury': destinationTreasury},
			     success : function(data){

							employeeResponsible = data.employeeResponsible;
							employeeNames = data.employeeNames;
							bdLimitAmount = data.bdLimitAmount;
							bdTreasuryAvailable = data.bdTreasuryAvailable;
							bdMaxAllowedTransfer = data.bdMaxAllowedTransfer;
			               },
			      error : function(errorData){

			              alert("Some error occurred while getting the employee responsible");
			              }


		});

    	
    		jQuery('#NewTreasuryTransfer input[name="employeeResponsible"]').val(employeeResponsible);
    		jQuery('#NewTreasuryTransfer input[name="employeeResponsibleVisible"]').val(employeeNames);
    		
	   		jQuery('#NewTreasuryTransfer input[name="destinationLimit"]').val(bdLimitAmount);
	   		jQuery('#NewTreasuryTransfer input[name="availableInDestination"]').val(bdTreasuryAvailable);
	   		jQuery('#NewTreasuryTransfer input[name="maxAllowedTransfer"]').val(bdMaxAllowedTransfer);
    		
    		
    		jQuery('#NewTreasuryTransfer input[name="employeeResponsibleVisible"]').parent().parent().hide();
    		jQuery('#NewTreasuryTransfer input[name="employeeResponsibleVisible"]').parent().parent().show();
    	

    		jQuery('#NewTreasuryTransfer input[name="destinationLimit"]').parent().parent().hide();
    		jQuery('#NewTreasuryTransfer input[name="destinationLimit"]').parent().parent().show();

    		jQuery('#NewTreasuryTransfer input[name="availableInDestination"]').parent().parent().hide();
    		jQuery('#NewTreasuryTransfer input[name="availableInDestination"]').parent().parent().show();

    		jQuery('#NewTreasuryTransfer input[name="maxAllowedTransfer"]').parent().parent().hide();
    		jQuery('#NewTreasuryTransfer input[name="maxAllowedTransfer"]').parent().parent().show();


    	return "";

    }
    
    function amountValidation(){
    
    	var transactionAmount = jQuery('#NewTreasuryTransfer input[name="transactionAmount"]').val();
    	var maxAllowedTransfer = jQuery('#NewTreasuryTransfer input[name="maxAllowedTransfer"]').val();
    	var availableInSource = jQuery('#NewTreasuryTransfer input[name="availableInSource"]').val();
    	
    	//alert('Before ... '+availableInSource);
    	availableInSource = availableInSource.replace(/,/g, '');
    	
    	
    	//alert('after '+availableInSource);
    	if ((transactionAmount == ''))
    		return false;

//		if (parseFloat(transactionAmount, 10) > parseFloat(availableInSource, 10)){
//    		alert('You cannot transfer more than available in the source treasury,  reduce the amount!');
//    		return false;
 //   	}
    	
    	//if bank return true
    	
    	var isBank = false;
    	var destinationTreasury = jQuery('#NewTreasuryTransfer select[name="destinationTreasury"] option:selected').val();
    	isBank = destinationIsBank(destinationTreasury);
    	
    	if (isBank == true)
    		return true;


		if (parseFloat(transactionAmount, 10) > parseFloat(maxAllowedTransfer, 10)){
    		alert('You cannot transfer more than allowed, treasury has a limt!');
    		return false;
    	}
    	
    	    	
    	//alert(transactionAmount);
    	//alert(maxAllowedTransfer);
    	
    	return true;
    }
    
    
    
    function destinationIsBank(destinationTreasury){
		var isBank = false;
    	//var reqUrl = '/partymgr/control/memberRegistrationFormValidation';
    	//var reqUrl = '/treasurymanagement/control/accountHasBeenUsed';
    	
    	//var reqUrl = '/treasurymanagement/control/employeeHasBeenGivenTreasury';
    	var reqUrl = '/treasurymanagement/control/destinationIsBank';

    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'destinationTreasury': destinationTreasury},
			     success : function(data){

							isBank = data.isBank;

			               },
			      error : function(errorData){
			              alert("Some error occurred while validating if bank");
			              }


		});

    	return isBank;
    }
 
 </script>
