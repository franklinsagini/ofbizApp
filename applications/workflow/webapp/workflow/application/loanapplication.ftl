<div id="content">
   
    <h1>Loan Application</h1>
  <!-- form name="user" action="add.html" method="post" -->
  <form method="post" action="<@ofbizUrl>createLoanApplication</@ofbizUrl>" name="loanApplication">
  <table>
  
  	<tr><td></td><td><input type="hidden" name="loanApplicationId" /></td></tr>
  	<tr><td>Member</td><td>
  	<#if applicationContext?exists>
  	<!-- select>
 		<option value="1000">Japheth Odonya</option>
  		<option value="1002">Philemon Samoei</option>
	</select -->
	    <select name="partyId">
	    	  <option value="${applicationContext.partyId?if_exists}">${applicationContext.firstName?if_exists} ${applicationContext.lastName?if_exists}</option>
              <#list membersList as member>
              	<#if ((member.firstName)?exists) && ((member.lastName)?exists)>
                <option value="${member.partyId}">${member.firstName} ${member.lastName}</option>
                </#if>
              </#list>
            </select>
	
	</td></tr>
  	<tr><td>Firstname</td><td><input type="text" name="firstName" value="${applicationContext.firstName?if_exists}" /></td></tr>
  	<tr><td>Middle Name</td><td><input type="text" name="middleName" value="${applicationContext.middleName?if_exists}" /></td></tr>
  	<tr><td>Lastname</td><td><input type="text" name="lastName" value="${applicationContext.lastName?if_exists}" /></td></tr>
  	<tr><td>ID Number</td><td><input type="text" name="idNumber" value="${applicationContext.idNumber?if_exists}" /></td></tr>
  	<tr><td>Member Type</td><td><input type="text" name="memberType" value="${applicationContext.memberType?if_exists}" /></td></tr>
  	<tr><td>Member Number</td><td><input type="text" name="memberNumber" value="${applicationContext.memberNumber?if_exists}" /></td></tr>
  	<tr><td>Mobile Number</td><td><input type="text" name="mobileNumber" value="${applicationContext.mobileNumber?if_exists}" /></td></tr>
  	<tr><td>Loan Type</td><td><select name="saccoProductId">
  			<#assign saccoProduct = applicationContext.getRelatedOne("SaccoProduct")?if_exists>
  			 <option value="${applicationContext.saccoProductId?if_exists}">${saccoProduct.name?if_exists} - ${saccoProduct.code?if_exists}</option>
 			<#list productsList as product>
              	<#if ((product.name)?exists) && ((product.code)?exists)>
                <option value="${product.saccoProductId}">${product.name} - ${product.code}</option>
                </#if>
              </#list>
	</select></td></tr>
	
	<tr><td>Interest Rate (Per Month)</td><td><input type="text" name="percentInterestPerMonthAmt" value="${applicationContext.percentInterestPerMonthAmt?if_exists}"/></td></tr>
	<tr><td>Maximum Repayment Period</td><td><input type="text" name="maxRepaymentPeriod" value="${applicationContext.maxRepaymentPeriod?if_exists}" /></td></tr>
	<tr><td>Loan Amount</td><td><input type="text" name="loanamt" value="${applicationContext.loanamt?if_exists}" /></td></tr>
	<tr><td>Repayment Period</td><td><input type="text" name="selectedRepaymentPeriod" value="${applicationContext.selectedRepaymentPeriod?if_exists}" /></td></tr>
	
	<tr><td>Payment Method</td><td><select name="paymentMethodTypeId">
		<#assign apaymentMethod = applicationContext.getRelatedOne("PaymentMethodType")?if_exists>
		<option value="${applicationContext.paymentMethodTypeId?if_exists}">${apaymentMethod.description?if_exists}</option>
 		<#list paymentMethodsList as paymentMethod>
              	<#if ((paymentMethod.description)?exists)>
                <option value="${paymentMethod.paymentMethodTypeId}">${paymentMethod.description} </option>
                </#if>
              </#list>
	</select></td></tr>
	<tr><td>Status</td><td><select name="loanStatusId">
		<#assign aloanStatus = applicationContext.getRelatedOne("LoanStatus")?if_exists>
		<option value="${applicationContext.loanStatusId?if_exists}">${aloanStatus.name?if_exists}</option>
 		<#list loanStatusList as loanStatus>
              	<#if ((loanStatus.name)?exists)>
                <option value="${loanStatus.loanStatusId}">${loanStatus.name} </option>
                </#if>
              </#list>
	</select></td></tr>
	
	<#else>
	
	  <select name="partyId">
	    	  <option value="">Select member ...</option>
              <#list membersList as member>
              	<#if ((member.firstName)?exists) && ((member.lastName)?exists)>
                <option value="${member.partyId}">${member.firstName} ${member.lastName}</option>
                </#if>
              </#list>
            </select>
	
	</td></tr>
  	<tr><td>Firstname</td><td><input type="text" name="firstName" value="" /></td></tr>
  	<tr><td>Middle Name</td><td><input type="text" name="middleName" value="" /></td></tr>
  	<tr><td>Lastname</td><td><input type="text" name="lastName" value="" /></td></tr>
  	<tr><td>ID Number</td><td><input type="text" name="idNumber" value="" /></td></tr>
  	<tr><td>Member Type</td><td><input type="text" name="memberType" value="" /></td></tr>
  	<tr><td>Member Number</td><td><input type="text" name="memberNumber" value="" /></td></tr>
  	<tr><td>Mobile Number</td><td><input type="text" name="mobileNumber" value="" /></td></tr>
  	<tr><td>Loan Type</td><td><select name="saccoProductId">
  			
  			 <option value="">Select loan ....</option>
 			<#list productsList as product>
              	<#if ((product.name)?exists) && ((product.code)?exists)>
                <option value="${product.saccoProductId}">${product.name} - ${product.code}</option>
                </#if>
              </#list>
	</select></td></tr>
	
	<tr><td>Interest Rate (Per Month)</td><td><input type="text" name="percentInterestPerMonthAmt" value=""/></td></tr>
	<tr><td>Maximum Repayment Period</td><td><input type="text" name="maxRepaymentPeriod" value="" /></td></tr>
	<tr><td>Loan Amount</td><td><input type="text" name="loanamt" value="" /></td></tr>
	<tr><td>Repayment Period</td><td><input type="text" name="selectedRepaymentPeriod" value="" /></td></tr>
	
	<tr><td>Payment Method</td><td><select name="paymentMethodTypeId">
		<option value="">Select payment method</option>
 		<#list paymentMethodsList as paymentMethod>
              	<#if ((paymentMethod.description)?exists)>
                <option value="${paymentMethod.paymentMethodTypeId}">${paymentMethod.description} </option>
                </#if>
              </#list>
	</select></td></tr>
	<tr><td>Status</td><td><select name="loanStatusId">
		<!-- option value="">Select Status</option -->
 		<#list loanStatusList as loanStatus>
              	<#if ((loanStatus.name)?exists)>
                <option value="${loanStatus.loanStatusId}">${loanStatus.name} </option>
                </#if>
              </#list>
	</select></td></tr>
	</#if>
  	<tr><td><input type="submit" value="   Save   " /></td><td></td></tr>
  </table>
    
  </form>
  <br/>
  
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
</div>