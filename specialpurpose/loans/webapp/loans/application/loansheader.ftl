<script type="text/javascript">
   jQuery(document).ready(function(){


   jQuery('select[name="partyId"]').change(function(){
		 /** var memberId = jQuery('select[name="partyId"]').val;
         alert(memberId); **/
         var memberId = this.value;
         var reqUrl = '/loans/control/memberdetails';
         sendAjaxRequest(reqUrl, memberId);
         
         

        });
        
        
        jQuery('select[name="loanProductId"]').change(function(){
		 /** var loanProductId = jQuery('select[name="loanProductId"]').val;
         alert(loanProductId); **/
         var loanProductId = this.value;
         var reqUrl = '/loans/control/loandetails';
         populateLoanDetails(reqUrl, loanProductId);
         
         

        });
     });
     
     
     

  function sendAjaxRequest(reqUrl, memberId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'memberId': memberId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			//alert(' ID No, Member Type, Member Number, Mobile No for '+memberId);
				//alert(data.firstName);
				 $('input[name="firstName"]').val(data.firstName);
				 $('input[name="middleName"]').val(data.middleName);
				 $('input[name="lastName"]').val(data.lastName);
				 $('input[name="idNumber"]').val(data.idNumber);
				 $('input[name="memberType"]').val(data.memberType);
				 $('input[name="memberNumber"]').val(data.memberNumber);
				 $('input[name="mobileNumber"]').val(data.mobileNumber);
              //You handle the response here like displaying in required div etc. 
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }


    });
    }
    /** 
    	Populate Loan Details
    **/
     function populateLoanDetails(reqUrl, loanProductId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'loanProductId': loanProductId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			//alert(' ID No, Member Type, Member Number, Mobile No for '+memberId);
				//alert(data.firstName);
				 $('input[name="interestRatePM"]').val(data.interestRatePM);
				 $('input[name="maxRepaymentPeriod"]').val(data.maxRepaymentPeriod);
				 $('input[name="maximumAmt"]').val(data.maximumAmt);
				// $('input[name="selectedRepaymentPeriod"]').val(data.selectedRepaymentPeriod);
              //You handle the response here like displaying in required div etc. 
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }


    });

   }
   </script>