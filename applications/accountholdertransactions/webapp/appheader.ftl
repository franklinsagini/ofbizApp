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
 <#-- h2>${uiLabelMap.ExampleApplication}</h2 -->
<#-- #assign selected = headerItem?default("void")>
<div id="app-navigation">
  <ul>
    <li>
     
      <h2>Loans Management</h2>
      <ul>
      <#if userLogin?has_content>
        <li <#if selected = "${uiLabelMap.ExampleExample}"> class="selected"</#if>><a href="<@ofbizUrl>FindExample?portalPageId=${uiLabelMap.ExampleExample}</@ofbizUrl>">${uiLabelMap.ExampleExample}</a></li>
        <li <#if selected = "${uiLabelMap.ExampleFeature}"> class="selected"</#if>><a href="<@ofbizUrl>FindExampleFeature?portalPageId=${uiLabelMap.ExampleFeature}</@ofbizUrl>">${uiLabelMap.ExampleFeature}</a></li>
        <li <#if selected = "${uiLabelMap.ExampleFormWidgetExamples}"> class="selected"</#if>><a href="<@ofbizUrl>FormWidgetExamples?portalPageId=${uiLabelMap.ExampleFormWidgetExamples}</@ofbizUrl>">${uiLabelMap.ExampleFormWidgetExamples}</a></li>
        <li <#if selected = "${uiLabelMap.ExampleAjaxExamples}"> class="selected"</#if>><a href="<@ofbizUrl>authview/findExampleAjax?portalPageId=${uiLabelMap.ExampleAjaxExamples}</@ofbizUrl>">${uiLabelMap.ExampleAjaxExamples}</a></li>
        <#if portalPages?has_content>
            <#list portalPages as page>
              <#if page.portalPageName?has_content>
                <li<#if selected = "${page.portalPageId}"> class="selected"</#if>><a href="<@ofbizUrl>showPortalPage?portalPageId=${page.portalPageId}</@ofbizUrl>"><#if page.portalPageName?exists>${page.portalPageName}<#else>?</#if></a></li>
              </#if>
            </#list>
        </#if>
        <li class="opposed"><a href="<@ofbizUrl>ManagePortalPages?parentPortalPageId=EXAMPLE</@ofbizUrl>">${uiLabelMap.CommonDashboard}</a></li>
      </#if>
      </ul>
    </li>
  </ul>
  <br class="clear" />
</div -->
 <script type="text/javascript">
   jQuery(document).ready(function(){


   jQuery('select[name="partyId"]').change(function(){
         var partyId = this.value;
         var reqUrl = '/accountholdertransactions/control/accountslist';
         memberAccounts(reqUrl, partyId);
        });
		
		
		  jQuery('select[name="memberAccountId"]').change(function(){
         var memberAccountId = this.value;
         var reqUrl = '/accountholdertransactions/control/availableamount';
         availableAmount(reqUrl, memberAccountId);
        });
        
        
          jQuery('select[name="loanApplicationId"]').change(function(){
         var loanApplicationId = this.value;
         var reqUrl = '/accountholdertransactions/control/getLoanApplicationDetails';
         getLoanApplicationDetails(reqUrl, loanApplicationId);
        });
        
        
      
    
		
     });
     
      function memberAccounts(reqUrl, partyId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'partyId': partyId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				var options =  jQuery('select[name="memberAccountId"]');
				options.empty();
				options.append($("<option />").val('').text('Please select Member Account ..'));
				$.each(data, function(item, itemvalue) {
				    options.append($("<option />").val(item).text(itemvalue));
				});
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
	
	function availableAmount(reqUrl, memberAccountId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'memberAccountId': memberAccountId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				
				//$.each(data, function(item, itemvalue) {
				   
					jQuery('input[name="availableAmount"]').val(data.availableAmount);
					jQuery('input[name="bookBalanceAmount"]').val(data.bookBalanceAmount);
				//});
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    function checkRemittanceEnough(stationNumber, month){
     var reqUrl = '/accountholdertransactions/control/checkremittance';
    var isEnough = isRemittanceEnough(reqUrl, stationNumber, month);
     if (isEnough == true){
    	return isEnough;
    } else{
    	alert("Remitted total is less that the amount posted on cheque");
    }
    
    }
    
    function checkAvailableBalance(){
    
    	var isAvailable = false;
    	
    	var availableBalance = jQuery('input[name="availableAmount"]').val();
    	var transactionAmount = jQuery('input[name="transactionAmount"]').val();
    	transactionAmount = transactionAmount.replace(/,/g, '');
    	
    	var memberAccountId = jQuery('select[name="memberAccountId"]').val();
    	memberAccountId = memberAccountId.replace(/,/g, '');
    	
    	var treasuryId = jQuery('input[name="treasuryId"]').val();
    	treasuryId = treasuryId.replace(/,/g, '');
     	
     	
     	var reqUrl = '/accountholdertransactions/control/isTellerBalanceEnough';
    	var isEnough = isTellerBalanceEnough(reqUrl, treasuryId, memberAccountId, transactionAmount);
	     if (isEnough != true){
	    	alert("Teller balance is not enough - you must have enough money in the teller to transact");
	    	return false;
	    }
     	
     	
    	if (parseFloat(transactionAmount, 10) < parseFloat(availableBalance, 10)){
    		isAvailable = true;
    	} else{
    		alert('Not enough Balance, Transanction Declined');
    		isAvailable = false;
    	}
    	return isAvailable;
    }
    
    function getLoanApplicationDetails(reqUrl, loanApplicationId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'loanApplicationId': loanApplicationId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				
				//$.each(data, function(item, itemvalue) {
				   
					jQuery('input[name="loanNo"]').val(data.loanNo);
					jQuery('select[name="loanTypeId"]').val(data.loanTypeId);
					/** jQuery('input[name="loanBalanceAmt"]').val(data.loanAmt); **/
					jQuery('input[name="loanAmt"]').val(data.loanAmt);
					jQuery('input[name="loanBalanceAmt"]').val(data.loanBalanceAmt);
					
					jQuery('input[name="totalLoanDue"]').val(data.totalLoanDue);
					jQuery('input[name="totalInterestDue"]').val(data.totalInterestDue);
					jQuery('input[name="totalInsuranceDue"]').val(data.totalInsuranceDue);
					jQuery('input[name="totalPrincipalDue"]').val(data.totalPrincipalDue);
					/** jQuery('input[name="transactionAmount"]').val(data.transactionAmount); **/
					
				//});
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }
    });
    }
    
    function isRemittanceEnough(reqUrl, stationNumber, month){
        var isEnough = false;
    	jQuery.ajax({

     		url    : reqUrl,
     		type   : 'GET',
     		data   : {'stationNumber': stationNumber, 'month': month}, //here you can pass the parameters to  
                                                   //the request if any.
     		success : function(data){
				
					if (data.REMITANCEENOUGH == 'YES')
					{
						isEnough = true;
					} else{
						isEnough = false;
					}
				   
			
               },
      		async : false
    	});
    
   		return isEnough;
    }
    
  
    function isTellerBalanceEnough(reqUrl, treasuryId, memberAccountId, transactionAmount){
        var isEnough = false;
    	jQuery.ajax({

     		url    : reqUrl,
     		type   : 'GET',
    		data   : {'treasuryId': treasuryId, 'memberAccountId': memberAccountId, 'transactionAmount':transactionAmount}, //here you can pass the parameters to  
                                                   //the request if any.
     			success : function(data){
				
					if (data.TELLERBALANCEENOUGH == true)
					{
						isEnough = true;
					} else{
						isEnough = false;
					}
			
               },
      		async : false
    		});
    
   			return isEnough;
    }
    
    function checkTellerLimit(){
    
    	var transactionAmount = jQuery('input[name="transactionAmount"]').val();
    	transactionAmount = transactionAmount.replace(/,/g, '');
    	
      	//isTellerBalanceEnough
     	var reqUrl = '/accountholdertransactions/control/isTellerOverLimit';
    	var isTellerOverLimit = tellerOverLimit(reqUrl, transactionAmount);
	     if (isTellerOverLimit == true){
	    	alert("This transaction will overflow the teller limit, please transfer to treasury first before continuing with the transaction!");
	    	return false;
	    }
    	return true;
    }
    
    
     function tellerOverLimit(reqUrl, transactionAmount){
        var isOverLimit = false;
    	jQuery.ajax({

     		url    : reqUrl,
     		type   : 'GET',
    		data   : {'transactionAmount':transactionAmount}, //here you can pass the parameters to  
                                                   //the request if any.
     			success : function(data){
				
					if (data.TELLEROVERLIMIT == true)
					{
						isOverLimit = true;
					} else{
						isOverLimit = false;
					}
			
               },
      		async : false
    		});
    
   			return isOverLimit;
    }
</script>
