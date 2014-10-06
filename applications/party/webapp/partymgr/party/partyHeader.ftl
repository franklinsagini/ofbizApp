<script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
    <script src="//ajax.aspnetcdn.com/ajax/jquery.validate/1.9/jquery.validate.js"></script>
<script>
        $(document).ready(function(){   

            $("#NewMember").validate({

                rules:{         
                    firstName:{"required": true},
                    lastName:{"required": true}

                },
                messages:{          
                    firstName:"<a font style='color:red'>  FirstName is Required</a>"   ,
                    lastName:"<a font style='color:red'> Last Name is Required </a>"    

                }   
            }); 
            
            
             jQuery('select[name="accountProductId"]').change(function(){
		
         var accountProductId = this.value;
         var partyId = jQuery('input[name="partyId"]').val();
        var reqUrl = '/partymgr/control/generateAccountNumber';
         
         if ((accountProductId.length > 0)){
 			
			generateAccountNumber(reqUrl, partyId, accountProductId );
         } 

        });
        });
        
 /***
 	Generate Account Number Branch-Product-MemberNo-Sequence
 */       
 function generateAccountNumber(reqUrl, partyId, accountProductId){
    jQuery.ajax({

     url    : reqUrl,
     type   : 'GET',
     data   : {'partyId': partyId, 'accountProductId': accountProductId}, //here you can pass the parameters to  
                                                   //the request if any.
     success : function(data){
				 $('input[name="accountNo"]').val(data.accountNumber);
				
				 
               },
      error : function(errorData){

              alert("Some error occurred while processing the request");
              }


    });

   }
 </script>