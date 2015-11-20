<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
 <script type="text/javascript">
    
    function accountsListing(partyId){
    	 var reqUrl = '/atmmanagement/control/memberaccountlist';
         memberAccountsList(reqUrl, partyId);
         
           var loanProductId = '';
         var reqUrlMobile = '/loans/control/memberdetails';
         getMobileNumber(reqUrlMobile, partyId, loanProductId);
    }
    
     function memberAccountsList(reqUrl, partyId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'partyId': partyId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				var options =  jQuery('#NewMSaccoApplication select[name="memberAccountId"]');
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
    
    function validateCardApplication(cardApplicationId){
    	
    	
    	var reqUrl = '/atmmanagement/control/applyforATMCard';
     	
    	    var balanceStatus = 'NOTENOUGH';
    
    jQuery.ajax({

     url    : reqUrl,
     async	: false,
     type   : 'GET',
     data   : {'cardApplicationId': cardApplicationId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				   balanceStatus = data.balanceStatus
               },
      error : function(errorData){
              	   alert("Some error occurred while processing the request");
              }
    });

    
	    if (balanceStatus == 'ENOUGH'){
	    	alert('Card Applied Successfully!');
	    	return true;
	    } else{
	    	alert('Could not apply for the CARD due to insufficent funds in the member account!');
	    	return false;
	    }
    	
    }
    
      function getMobileNumber(reqUrlMobile, partyId, loanProductId){
    jQuery.ajax({

     url    : reqUrlMobile,
     async	: false,
     type   : 'GET',
     data   : {'memberId': partyId, 'loanProductId': loanProductId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){

				 $('#NewMSaccoApplication input[name="mobilePhoneNumber"]').val(data.mobileNumber);
                 $('#NewMSaccoApplication input[name="mobilePhoneNo"]').val(data.mobileNumber);
		
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
   
</script>
