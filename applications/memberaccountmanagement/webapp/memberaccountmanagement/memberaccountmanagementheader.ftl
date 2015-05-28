<script type="text/javascript">

   jQuery(document).ready(function(){
	
	jQuery('select[name="sourceMemberAccountId"]').change(function(){
         var memberAccountId = this.value;
         var reqUrl = '/accountholdertransactions/control/availableamount';
         getSourceAccountBalance(reqUrl, memberAccountId);
        });
      
     jQuery('select[name="destMemberAccountId"]').change(function(){
         var memberAccountId = this.value;
         var reqUrl = '/accountholdertransactions/control/availableamount';
         getDestinationAccountBalance(reqUrl, memberAccountId);
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
     			
				 $('input[name="amountInSourceRepaid"]').val(data.amountInSourceRepaid);

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
   
   </script>