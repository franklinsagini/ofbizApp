<script type="text/javascript">

   jQuery(document).ready(function(){
   
   	jQuery('select[name="sourceType"]').change(function(){
         var sourceType = this.value;
         
        // alert(sourceType);
        // var reqUrl = '/accountholdertransactions/control/availableamount';
        // getSourceAccountBalance(reqUrl, memberAccountId);
        var memberAccountId =  jQuery('select[name="sourceMemberAccountId"]').val();
        sourceType = sourceType.toUpperCase();
			
			if ((sourceType == 'ACCOUNT')){
         		jQuery('#generalMemberVoucher select[name="sourceLoanApplicationId"]').parent().parent().parent().hide();
         		jQuery('#generalMemberVoucher select[name="sourceMemberAccountId"]').parent().parent().parent().show();
         		
         		var reqUrl = '/accountholdertransactions/control/glaccount';
         		getSourceMemberAccountLegerAccount(reqUrl, memberAccountId);
         	} 
         	
         	
         	if ((sourceType == 'PRINCIPAL') || (sourceType == 'INTERESTCHARGE') || (sourceType == 'INTERESTPAID') || (sourceType == 'INSURANCECHARGE') || (sourceType == 'INSURANCEPAYMENT')){
         		jQuery('#generalMemberVoucher select[name="sourceLoanApplicationId"]').parent().parent().parent().show();
         		jQuery('#generalMemberVoucher select[name="sourceMemberAccountId"]').parent().parent().parent().hide();
         		
         		
         		var reqUrl = '/accountholdertransactions/control/glloanaccount';
         		getSourceLoanAccountLegerAccount(reqUrl, sourceType);
 
         	} 
         	
        
        });
        
      jQuery('select[name="destinationType"]').change(function(){
         var destinationType = this.value;
         
        // alert(destinationType);
        // var reqUrl = '/accountholdertransactions/control/availableamount';
        // getSourceAccountBalance(reqUrl, memberAccountId);
        
        var memberAccountId =  jQuery('select[name="destMemberAccountId"]').val();
           destinationType = destinationType.toUpperCase();
			
			if ((destinationType == 'ACCOUNT')){
         		jQuery('#generalMemberVoucher select[name="destLoanApplicationId"]').parent().parent().parent().hide();
         		jQuery('#generalMemberVoucher select[name="destMemberAccountId"]').parent().parent().parent().show();
         		
         		var reqUrl = '/accountholdertransactions/control/glaccount';
         		getDestinationMemberAccountLegerAccount(reqUrl, memberAccountId);
         	} 
         	
         	
         	if ((destinationType == 'PRINCIPAL') || (destinationType == 'INTERESTCHARGE') || (destinationType == 'INTERESTPAID') || (destinationType == 'INSURANCECHARGE') || (destinationType == 'INSURANCEPAYMENT')){
         		jQuery('#generalMemberVoucher select[name="destLoanApplicationId"]').parent().parent().parent().show();
         		jQuery('#generalMemberVoucher select[name="destMemberAccountId"]').parent().parent().parent().hide();
         		
         		var reqUrl = '/accountholdertransactions/control/glloanaccount';
         		getDestinationLoanAccountLegerAccount(reqUrl, destinationType);
         		
         	} 
        });
	
	jQuery('select[name="sourceMemberAccountId"]').change(function(){
         var memberAccountId = this.value;
         var reqUrl = '/accountholdertransactions/control/availableamount';
         getSourceAccountBalance(reqUrl, memberAccountId);
         
         var reqUrl = '/accountholdertransactions/control/glaccount';
         getSourceMemberAccountLegerAccount(reqUrl, memberAccountId);
        });
      
     jQuery('select[name="destMemberAccountId"]').change(function(){
         var memberAccountId = this.value;
         var reqUrl = '/accountholdertransactions/control/availableamount';
         getDestinationAccountBalance(reqUrl, memberAccountId);
         
         
         var reqUrl = '/accountholdertransactions/control/glaccount';
         getDestinationMemberAccountLegerAccount(reqUrl, memberAccountId);
        });
        
        //Get Loan Repaid Total
        
      jQuery('select[name="sourceLoanApplicationId"]').change(function(){
         var loanApplicationId = this.value;
         var reqUrl = '/memberaccountmanagement/control/totalrepaid';
         getSourceRepaidAmount(reqUrl, loanApplicationId);
        });
      
     jQuery('select[name="destLoanApplicationId"]').change(function(){
         var loanApplicationId = this.value;
         var reqUrl = '/memberaccountmanagement/control/totalrepaid';
         getDestinationRepaidAmount(reqUrl, loanApplicationId);
        });
        
     jQuery('input[name="principalAmount"]').change(function(){

         var principalAmount = this.value;
         var interestAmount = jQuery('input[name="interestAmount"]').val();
         var insuranceAmount = jQuery('input[name="insuranceAmount"]').val();
         
         getAmountTotal(principalAmount, interestAmount, insuranceAmount);
        });
        
       
       jQuery('input[name="interestAmount"]').change(function(){

         var interestAmount = this.value;
         var principalAmount = jQuery('input[name="principalAmount"]').val();
         var insuranceAmount = jQuery('input[name="insuranceAmount"]').val();
         
         getAmountTotal(principalAmount, interestAmount, insuranceAmount);
        });
        
        
       jQuery('input[name="insuranceAmount"]').change(function(){

         var insuranceAmount = this.value;
         var interestAmount = jQuery('input[name="interestAmount"]').val();
         var principalAmount = jQuery('input[name="principalAmount"]').val();
         
         getAmountTotal(principalAmount, interestAmount, insuranceAmount);
        });
      

     });
     
   function getSourceAccountBalance(reqUrl, memberAccountId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'memberAccountId': memberAccountId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			
				 $('input[name="amountInSource"]').val(data.amountInSource);

               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    function getDestinationAccountBalance(reqUrl, memberAccountId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'memberAccountId': memberAccountId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			
				 $('input[name="amountInDestination"]').val(data.amountInDestination);

               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
     
   
  
    
 function getSourceMemberAccounts(partyId){
    	 var reqUrl = '/memberaccountmanagement/control/memberaccountlist';
         sourceMemberAccounts(reqUrl, partyId);
    }
    
  function sourceMemberAccounts(reqUrl, partyId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'partyId': partyId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				var options =  jQuery('select[name="sourceMemberAccountId"]');
				options.empty();
				options.append($("<option />").val('').text('Please select Account ..'));
				$.each(data, function(item, itemvalue) {
				    options.append($("<option />").val(item).text(itemvalue));
				});
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    function getDestinationMemberAccounts(partyId){
    	 var reqUrl = '/memberaccountmanagement/control/memberaccountlist';
         destinationMemberAccounts(reqUrl, partyId);
    }
    
    function destinationMemberAccounts(reqUrl, partyId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'partyId': partyId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				var options =  jQuery('select[name="destMemberAccountId"]');
				options.empty();
				options.append($("<option />").val('').text('Please select Account ..'));
				$.each(data, function(item, itemvalue) {
				    options.append($("<option />").val(item).text(itemvalue));
				});
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    
    function getSourceMemberLoans(partyId){
    	 var reqUrl = '/memberaccountmanagement/control/memberloanslist';
         sourceMemberLoans(reqUrl, partyId);
    }
    
    function getDestinationMemberLoans(partyId){
    	 var reqUrl = '/memberaccountmanagement/control/memberloanslist';
         destinationMemberLoans(reqUrl, partyId);
    }
    
    //To Source Loans
    function sourceMemberLoans(reqUrl, partyId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'partyId': partyId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				var options =  jQuery('select[name="sourceLoanApplicationId"]');
				options.empty();
				options.append($("<option />").val('').text('Please select Account ..'));
				$.each(data, function(item, itemvalue) {
				    options.append($("<option />").val(item).text(itemvalue));
				});
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    //To Destination Loans
    function destinationMemberLoans(reqUrl, partyId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'partyId': partyId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				var options =  jQuery('select[name="destLoanApplicationId"]');
				options.empty();
				options.append($("<option />").val('').text('Please select Account ..'));
				$.each(data, function(item, itemvalue) {
				    options.append($("<option />").val(item).text(itemvalue));
				});
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    
    //Repaid Amount
     function getSourceRepaidAmount(reqUrl, loanApplicationId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'loanApplicationId': loanApplicationId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     
     			alert(data.amountInSourceRepaid);
     			
				 $('input[name="amountInSourceRepaid"]').val(data.amountInSourceRepaid);
				 $('input[name="amountInSource"]').val(data.amountInSourceRepaid);

               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    function getDestinationRepaidAmount(reqUrl, loanApplicationId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'loanApplicationId': loanApplicationId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			
				 $('input[name="amountInDestinationRepaid"]').val(data.amountInDestinationRepaid);
				 $('input[name="amountInDestination"]').val(data.amountInDestinationRepaid);


               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    
    function getAmountTotal(principalAmount, interestAmount, insuranceAmount){
    
    	if (principalAmount == '')
    		principalAmount = 0;

    	if (interestAmount == '')
    		interestAmount = 0;


    	if (insuranceAmount == '')
    		insuranceAmount = 0;
    		
    	var amount = parseFloat(principalAmount, 10) + parseFloat(interestAmount, 10) + parseFloat(insuranceAmount, 10);
    	
    	$('input[name="amount"]').val(amount);
    	$('input[name="amountDisplay"]').val(amount);
    	
    }
    
    
    //Get Source Ledger Account
     function getSourceMemberAccountLegerAccount(reqUrl, memberAccountId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'memberAccountId': memberAccountId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			
				// $('input[name="amountInSource"]').val(data.amountInSource);
				var glAccountId = data.glAccountId;
				// alert(glAccountId);
				// $('option[value=glAccountId]').prop('selected',true);
				
				$('#sourceglAccountId').val(glAccountId);
				//$('#sourceglAccountId').text(glAccountId);

               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    //Get Destination Ledger Account
     function getDestinationMemberAccountLegerAccount(reqUrl, memberAccountId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'memberAccountId': memberAccountId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			
				var glAccountId = data.glAccountId;
				 
				// $('option[value=glAccountId]').prop('selected',true);
				$('#destglAccountId').val(glAccountId);
				//$('#destglAccountId').text(glAccountId);
				

               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }  
    
    /***
    	getSourceLoanAccountLegerAccount
    ***/
     
   function getSourceLoanAccountLegerAccount(reqUrl, sourceType){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'sourceType': sourceType}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			
				// $('input[name="amountInSource"]').val(data.amountInSource);
				var glAccountId = data.glAccountId;
				// alert(glAccountId);
				// $('option[value=glAccountId]').prop('selected',true);
				
				$('#sourceglAccountId').val(glAccountId);
				//$('#sourceglAccountId').text(glAccountId);

               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    
    //getDestinationLoanAccountLegerAccount(reqUrl, destinationType);
    
    function getDestinationLoanAccountLegerAccount(reqUrl, destinationType){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'sourceType': destinationType}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			
				// $('input[name="amountInSource"]').val(data.amountInSource);
				var glAccountId = data.glAccountId;
				// alert(glAccountId);
				// $('option[value=glAccountId]').prop('selected',true);
				
				$('#destglAccountId').val(glAccountId);
				//$('#sourceglAccountId').text(glAccountId);

               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
   </script>