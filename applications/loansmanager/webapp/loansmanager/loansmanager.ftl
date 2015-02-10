<script type="text/javascript">

    
    
    
        function isAmountExcess(loanAmt){
    	
		//if (!hasSavingsAccount){
			approvedAmt  = jQuery('input[name="approvedAmt"]').val();
			approvedAmt = approvedAmt.replace(/,/g, '');
			//alert('Loan Amount '+loanAmt+' Appraised '+appraisedAmt)
			
			if (parseFloat(loanAmt) < parseFloat(approvedAmt))
			{
				alert('Cannot approve more than applied amount ');
				return false;
			} else{
				//alert('Will approved ');
				return true;
			}
			
			//alert(' Cannot appraise more than applied');
		//}
    	//return false;
    }
   </script>