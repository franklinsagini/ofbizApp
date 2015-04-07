<script>
   $(document).ready(function(){

        
     	 
     	
     	jQuery('select[name="treasuryTypeId"]').change(function(){
     	
     		//$("#NewTreasury input[name=name]")
			selectedItem = jQuery('#NewTreasury select[name="treasuryTypeId"] option:selected').text();
			selectedItem = selectedItem.toUpperCase();
			//alert(selectedItem);
			
			if ((selectedItem == 'VAULT') || (selectedItem == 'TELLER')){
         		jQuery('#NewTreasury select[name="finAccountId"]').parent().parent().parent().hide();
         		
         		
         		
         	} else{
         		jQuery('#NewTreasury select[name="finAccountId"]').parent().parent().parent().show();
         	}
         	
         	
         	//$('#NewMember').reset();
        });  
        
     });

 
 </script>
