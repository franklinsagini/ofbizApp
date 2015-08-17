<script type="text/javascript">
   jQuery(document).ready(function(){


   jQuery('input[name="partyId"]').change(function(){
		 /** var memberId = jQuery('select[name="partyId"]').val;
         alert(memberId); **/
         var memberId = this.value;
         var loanProductId = jQuery('select[name="loanProductId"]').val();
         var reqUrl = '/loans/control/memberdetails';
         sendAjaxRequest(reqUrl, memberId, loanProductId);
        });
		
	jQuery('select[name="guarantorId"]').change(function(){
         var memberId = this.value;
         var loanProductId = jQuery('input[name="loanProductId"]').val();
         var reqUrl = '/loans/control/memberdetails';
         sendAjaxRequest(reqUrl, memberId, loanProductId);
        });
        
   jQuery('select[name="partyId"]').change(function(){
		 /** var memberId = jQuery('select[name="partyId"]').val;
         alert(memberId); **/
         var memberId = this.value;
         var loanProductId = jQuery('select[name="loanProductId"]').val();
         var reqUrl = '/loans/control/memberdetails';
         sendAjaxRequest(reqUrl, memberId, loanProductId);
         
         if ((jQuery('select[name="loanProductId"]').val().length > 0) && (memberId.length > 0)){
         	var loanProductId = jQuery('select[name="loanProductId"]').val();
         	//alert(loanProductId);
         	
         	/**
         		Compute the maximum loan possible for this Member for the specified product
         		
         	**/
         	var loanMaxCalculationUrl = '/loans/control/loanMaxCalculation';
         	calculateLoanMaxAmount(loanMaxCalculationUrl, loanProductId, memberId);
         } 

        });
        
        
        jQuery('select[name="loanProductId"]').change(function(){
		 /** var loanProductId = jQuery('select[name="loanProductId"]').val;
         alert(loanProductId); **/
         var loanProductId = this.value;
         var reqUrl = '/loans/control/loandetails';
         populateLoanDetails(reqUrl, loanProductId);
         
         if ((jQuery('select[name="partyId"]').val().length > 0) && (loanProductId.length > 0)){
          	var memberId = jQuery('select[name="partyId"]').val();
         	//alert(memberId);
         	
         	/**
         		Compute the maximum loan possible for this Member for the specified product
         		
         	**/
         	var loanMaxCalculationUrl = '/loans/control/loanMaxCalculation';
         	calculateLoanMaxAmount(loanMaxCalculationUrl, loanProductId, memberId);
         }

        });
     });
     
     
     

  function sendAjaxRequest(reqUrl, memberId, loanProductId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'memberId': memberId, 'loanProductId': loanProductId}, //here you can pass the parameters to  
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
				 $('input[name="joinDate_i18n"]').val(data.joinDate);
				 $('input[name="joinDate"]').val(data.inputDate);
				 $('input[name="membershipDuration"]').val(data.membershipDuration);
				 
				 //for the guarantor
				 $('input[name="memberNo"]').val(data.memberNo);
				 $('input[name="payrolNo"]').val(data.payrolNo);
				 $('select[name="currentStationId"]').val(data.currentStationId);
				 $('input[name="depositamt"]').val(data.depositamt);
				 $('input[name="memberClass"]').val(data.memberClass);
				 
				 
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
				 $('input[name="interestRatePMOrig"]').val(data.interestRatePM);
				 
				 $('input[name="maxRepaymentPeriod"]').val(data.maxRepaymentPeriod);
				  $('input[name="maxRepaymentPeriodOrig"]').val(data.maxRepaymentPeriod);
				  
				 $('input[name="repaymentPeriod"]').val(data.maxRepaymentPeriod);
				 
				 $('input[name="maximumAmt"]').val(data.maximumAmt);
				 
				 
				 
				 $('input[name="multipleOfSavingsAmt"]').val(data.multipleOfSavingsAmt);
				 
				 
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }


    });

   }
   
   	/** 
    	Calculate Loan Maximum Amount
    **/
     function calculateLoanMaxAmount(loanMaxCalculationUrl, loanProductId, memberId){
    jQuery.ajax({

     url    : loanMaxCalculationUrl,
     type   : 'GET',
     data   : {'loanProductId': loanProductId, 'memberId': memberId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
     			
     			if ((data.isBasedOnNetSalary == true) && (data.netSalaryIsSet == false)){
     				 jQuery('select[name="loanProductId"]').val("");
     				alert("Must set up Net Salary Amount for the Member (this loan product is based on The Net Salary)");
     			}
     
				 $('input[name="maxLoanAmt"]').val(data.maxLoanAmt);
				 $('input[name="existingLoans"]').val(data.existingLoans);
				 
				 $('input[name="maxLoanAmtOrig"]').val(data.maxLoanAmt);
				 $('input[name="existingLoansOrig"]').val(data.existingLoans);
				 
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }


    });

   }
   
   function validLoanApplication(){
    
    	var isValid = true;
    	
    	
    	var loanAmt = jQuery('input[name="loanAmt"]').val();
    	var maxLoanAmt  = jQuery('input[name="maxLoanAmt"]').val();
    	
    	var repaymentPeriod = jQuery('input[name="repaymentPeriod"]').val();
    	var maxRepaymentPeriod  = jQuery('input[name="maxRepaymentPeriod"]').val();
    	var message;
    	
    	
    	
    	/**
    		loanamt
    		if (maxLoanAmt < loanAmt)
    		
    	**/
     	if (parseFloat(maxLoanAmt, 10) < parseFloat(loanAmt, 10))
    	{
    	
    		isValid = false;
    		message = ' You cannot exceed maximum Loan Amount allowed (3 times your savings) Minus Pre-existing loans';
    	}
    	
    	/** if (maxRepaymentPeriod < repaymentPeriod) **/
    	
    	if (parseFloat(maxRepaymentPeriod, 10) < parseFloat(repaymentPeriod, 10))
    	{
    	
    		if (!isValid){
    			message = message+" and Repayment Perod must be less than "+maxRepaymentPeriod;
    		}else{
    			message = " Repayment Perod must be less than "+maxRepaymentPeriod;
    		}
    	
    		isValid = false;
    		
    	}
    	
    	if (!isValid){
    		alert(message);
    	}
    	
    	return isValid;
    }
    
 	function loanProcessingValidation(loanApplicationId){
    	//alert(loanApplicationId+' Processing ... ');
    	var reqUrl = '/loans/control/otherExistingLoans';
    	var otherLoansProcessing = false;
    	var otherLoanNoRepayment = false;
    	var otherLoanNoRepaymentList = '';
    	var otherLoanUnderpayment = true;
    	var underPaidLoans;
    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'loanApplicationId': loanApplicationId}, 
			     success : function(data){
			     			otherLoansProcessing = data.otherLoansProcessing;
			     			otherLoanNoRepayment = data.otherLoanNoRepayment;
			     			otherLoanUnderpayment = data.otherLoanUnderpayment;
			     			anotherRunningLoanOfSameType = data.anotherRunningLoanOfSameType;
			     			underPaidLoans = data.underPaidLoans;
			     			otherLoanNoRepaymentList = data.otherLoanNoRepaymentList;
			               },
			      error : function(errorData){
			
			              alert("Some error occurred while validating loan application");
			              }
			
			
		});
		
		if (otherLoansProcessing){
			alert(' There is another Loan By the Same Member Being Processed');
			return false;
		}
		
		if (otherLoanNoRepayment){
			alert(' You must have started repaying the previous loan to try and apply for another loan ! ('+otherLoanNoRepaymentList+')');
			return false;
		}
		
		if (otherLoanUnderpayment){
			alert(' Previous loan has been underpaid, make sure you pay atleast the full expected amount ! ('+underPaidLoans+')');
			// The underpaid loans are underPaidLoans
			return false;
		}
		
		if (anotherRunningLoanOfSameType){
			alert(' There is another running loan of same type, you may need to clear first !');
			return false;
		}
		
		
    	return true;
    }
        
    function loanApplicationFormComplete(loanApplicationId){
    
    	var isValid = true;
    	var collateralsAvailable = '';
    	var guarantorsAvailable = '';
    	var guarantorsTotalDepositsEnough = '';
    	var eacherGuarantorGreaterThanAverage = '';
    	
    	var reqUrl = '/loans/control/validateApplicationForm';
    	
    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'loanApplicationId': loanApplicationId}, 
			     success : function(data){
			     
			     			collateralsAvailable = data.collateralsAvailable;
					    	guarantorsAvailable = data.guarantorsAvailable;
					    	guarantorsTotalDepositsEnough = data.guarantorsTotalDepositsEnough;
					    	eacherGuarantorGreaterThanAverage = data.eacherGuarantorGreaterThanAverage;
					    	
					    	
					    	//alert('collateralsAvailable  inanon'+collateralsAvailable);
							//alert('guarantorsAvailable inanon'+guarantorsAvailable);
							//alert('guarantorsTotalDepositsEnough  inanon'+guarantorsTotalDepositsEnough);
							//alert('eacherGuarantorGreaterThanAverage  inanon '+eacherGuarantorGreaterThanAverage);
							

			               },
			      error : function(errorData){
			
			              alert("Some error occurred while validating loan application");
			              }
			
			
		});
		
		//alert('collateralsAvailable '+collateralsAvailable);
		//alert('guarantorsAvailable '+guarantorsAvailable);
		//alert('guarantorsTotalDepositsEnough '+guarantorsTotalDepositsEnough);
		//alert('eacherGuarantorGreaterThanAverage '+eacherGuarantorGreaterThanAverage);
    	
    	var message = '';
    	if ((collateralsAvailable == 'N') && (guarantorsAvailable == 'N')){
    		message = "You must have Guarantors or Collateral to process the loan application ! ";
    		isValid = false;
    	} else{
    		if (guarantorsAvailable == 'Y'){
    			if (guarantorsTotalDepositsEnough == 'N'){
    				message = message+" Total Guarantors deposits must be equal or more than the loan amount applied ";
    				isValid = false;
    			}
    			
    			//if (eacherGuarantorGreaterThanAverage == 'N'){
    			//	isValid = false;
    			//	message = message+" Each Guarantor must be able to pay for his/her share of the loan guaranteed (Equal distribution is assumed)";
    				
    			//}
    		}
    	
    	}
    	
    	if (!isValid){
    		alert(message);
    		window.location.reload();
    	} else{
    		alert(' Loan Application forwarded for review!');
    		window.location.reload();
    	}
    	
    	
    	return isValid;
    
    }
    
    function customerCareLoanValidation(partyId){
    	var reqUrl = '/loans/control/hasSavingsAccount';
    	var hasSavingsAccount = false;
    	var isOldEnough = false;
    	var isFromAnotherSacco = false;
    	
    	
    	
    	var moreThanZero = false;
    	
    	var theLoanAmt = jQuery('input[name="loanAmt"]').val();
     	var baseAmt = 0;
     	
     	//alert(theLoanAmt);
     	if (parseFloat(theLoanAmt, 10) <= parseFloat(baseAmt, 10)){
    		alert('You cannot apply for a loan of ZERO amount or less');
    		return false;
    	}
    	
    	
    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'partyId': partyId}, 
			     success : function(data){
			     			hasSavingsAccount = data.hasSavingsAccount;
			     			isOldEnough = data.isOldEnough;
			     			isFromAnotherSacco = data.isFromAnotherSacco;
			               },
			      error : function(errorData){
			
			              alert("Some error occurred while validating loan application");
			              }
			
			
		});
		if (!hasSavingsAccount){
			alert(' The Member must have a Savings Account - this is the account to which the Loan Will be disbursed');
			return hasSavingsAccount;
		}
			
		if (isOldEnough){
			return true;
		}
		
	
		
		if ((!isOldEnough) && (!isFromAnotherSacco)){
			alert(' You must have been a member for at least 6 months or be from another Sacco to be able to apply for a loan! ');
			return false;
		}
    	
    	return true;
    }
    
    
    /***
    	Check that clearance has 
    */
    
     function confirmNewLoanApplicationForClearance(loanClearId){
     
     	var reqUrl = '/loanclearing/control/hasNewLoan';
    	var hasNewLoan = false;
    	
    	
    	jQuery.ajax({

			     url    : reqUrl,
			      async	: false,
			     type   : 'GET',
			     data   : {'loanClearId': loanClearId}, 
			     success : function(data){
			     			hasNewLoan = data.hasNewLoan;
			     			
			               },
			      error : function(errorData){
			
			              alert("Some error occurred while validating loan clearing");
			              }
			
			
		});
		
		if (hasNewLoan)
		{
			return true;
		}
		
    
    	alert(' Please provide loan application, the new loan and update first ');
    	
    	return false;
    
    }
    
    
   </script>