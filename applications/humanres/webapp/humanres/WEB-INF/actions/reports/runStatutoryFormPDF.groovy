
statutoryType = parameters.statutoryType

statutoryTypeUpper = statutoryType.toUpperCase();
String nhif = "nhif"
String nssf = "nssf"
String pinNo = "pinNo"

if (statutoryType.equalsIgnoreCase(nhif) ) {
    nhifList = delegator.findByAnd("Person", [isSeparated : "N" ], null, false);
     
    nhifList.eachWithIndex { nhifItem, index ->
    
     }

   context.statutoryTypeUpper = statutoryTypeUpper;
   context.nhifList = nhifList;
   return
}

if (statutoryType.equalsIgnoreCase(nssf) ) {
    nssfList = delegator.findByAnd("Person", [isSeparated : "N" ], null, false);
     
    nssfList.eachWithIndex { nssfItem, index ->
    
     }

   context.statutoryTypeUpper = statutoryTypeUpper;
   context.nssfList = nssfList;
   return
}

if (statutoryType.equalsIgnoreCase(pinNo) ) {
    pinList = delegator.findByAnd("Person", [isSeparated : "N" ], null, false);
     
    pinList.eachWithIndex { pinItem, index ->
    
     }

   context.statutoryTypeUpper = statutoryTypeUpper;
   context.pinList = pinList;
   return
}

employeeList = delegator.findByAnd("Person", [isSeparated : "N" ],  null, false);

employeeList.eachWithIndex { staffItem, index ->
   
     }

 context.statutoryTypeUpper = statutoryTypeUpper;
 context.employeeList = employeeList;









