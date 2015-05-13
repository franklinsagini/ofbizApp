<script type="text/javascript">

$( document ).ready(function() {
  jQuery('select[name="branchId"]').change(function(){
    var branchId = this.value;
    var reqUrl =  '/accounting/control/branchAccounts';
    getbranchAccounts(reqUrl, branchId);
  });

  function getbranchAccounts(reqUrl, branchId){
    jQuery.ajax({
         url    : reqUrl,
         type   : 'GET',
         data   : {'branchId': branchId},
         success : function(data){
            var options1 =  jQuery('select[name="debitAccount"]');
            var options2 =  jQuery('select[name="creditAccount"]');
            options1.empty();
            options2.empty();
            options1.append($("<option />").val('').text('Please select accounts ...'));
            options2.append($("<option />").val('').text('Please select accounts ...'));
            $.each(data, function(item, itemvalue) {
                options1.append($("<option />").val(item).text(itemvalue));
                options2.append($("<option />").val(item).text(itemvalue));
            });
               },
      error : function(errorData){
              alert("Some error occurred while processing the request");
              }
    });
    }
});

</script>
