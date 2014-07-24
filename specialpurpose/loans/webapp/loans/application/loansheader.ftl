<script type="text/javascript">
   jQuery(document).ready(function(){


   jQuery('select[name="partyId"]').change(function(){
		 /** var memberId = jQuery('select[name="partyId"]').val;
         alert(memberId); **/
         var memberId = this.value;
         var reqUrl = '/loans/control/memberdetails';
         sendAjaxRequest(reqUrl, memberId);
         
         

        });
        
        
        jQuery('select[name="saccoProductId"]').change(function(){
		 /** var saccoProductId = jQuery('select[name="saccoProductId"]').val;
         alert(saccoProductId); **/
         var saccoProductId = this.value;
         var reqUrl = '/loans/control/loandetails';
         populateLoanDetails(reqUrl, saccoProductId);
         
         

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
     function populateLoanDetails(reqUrl, saccoProductId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'saccoProductId': saccoProductId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			//alert(' ID No, Member Type, Member Number, Mobile No for '+memberId);
				//alert(data.firstName);
				 $('input[name="percentInterestPerMonthAmt"]').val(data.percentInterestPerMonthAmt);
				 $('input[name="maxRepaymentPeriod"]').val(data.maxRepaymentPeriod);
				 $('input[name="loanamt"]').val(data.loanamt);
				// $('input[name="selectedRepaymentPeriod"]').val(data.selectedRepaymentPeriod);
              //You handle the response here like displaying in required div etc. 
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }


    });

   }
   </script>