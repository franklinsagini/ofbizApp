<script type="text/javascript">
    

    
    /***
    	Member Obligations Validation
    
    ****/
       function memberObligationValidation(partyId){
       
        //alert('Checking Customer !!! Party Is :: '+partyId);
       //return false;
       
       
    	var reqUrl = '/loans/control/hasObligations';
    	var hasLoans = false;
    	var hasGuaranteed = false;
    	var shareCapitalBelowMinimum = false;
    	var memberDepositsLessThanLoans = false;
    	
    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'partyId': partyId}, 
			     success : function(data){
			     			hasLoans = data.hasLoans;
			     			hasGuaranteed = data.hasGuaranteed;
			     			shareCapitalBelowMinimum = data.shareCapitalBelowMinimum;
			     			memberDepositsLessThanLoans = data.memberDepositsLessThanLoans;
			               },
			      error : function(errorData){
			
			              alert("Some error occurred while validating withdrawal");
			              }
			
			
		});
		
		if (hasLoans){
			alert(' The Member still has pending loans ... ');
			return false;
			
		}
		
		if (hasGuaranteed){
			alert(' The Member must have the guarantors replaced first');
			return false;
		}
		
		if (shareCapitalBelowMinimum){
			alert(' The Member must update Share Capital to minimum level');
			return false;
		}
		
    	
    	return true;
    }
    
    
   </script>