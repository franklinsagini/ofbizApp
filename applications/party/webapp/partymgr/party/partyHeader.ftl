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
        });
 </script>